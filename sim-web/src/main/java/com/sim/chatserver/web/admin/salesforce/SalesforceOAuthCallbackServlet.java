package com.sim.chatserver.web.admin.salesforce;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handles Salesforce OAuth callback, exchanges authorization code for tokens,
 * and stores access_token + refresh_token + instance_url securely.
 *
 * Security: - Requires authenticated admin session - Validates OAuth state +
 * TTL - Clears one-time state after use - Avoids logging sensitive token values
 *
 * Operational hardening: - Reverse-proxy aware redirect_uri construction -
 * Sanitized error handling for upstream failures
 */
//@WebServlet(name = "SalesforceOAuthCallbackServlet", urlPatterns = {"/admin/salesforce/oauth/callback"})
public class SalesforceOAuthCallbackServlet extends HttpServlet {
    // parasoft-suppress SERVLET.CETS "Checked servlet exceptions are intentionally handled at endpoint boundaries or container-dispatched paths."
    // parasoft-suppress SERVLET.IF "Servlet instance fields are immutable runtime collaborators and do not hold mutable request state."
    // parasoft-suppress SECURITY.ESD.SIF "Servlet fields store framework/runtime handles only and are not serialized sensitive payloads."
    // parasoft-suppress SECURITY.BV.ADT "OAuth state TTL comparison intentionally uses currentTimeMillis for request-time freshness validation."

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(SalesforceOAuthCallbackServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    private static final String OAUTH_STATE_KEY = "sf_oauth_state";
    private static final String OAUTH_STATE_TS_KEY = "sf_oauth_state_ts";
    private static final long OAUTH_STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private static final Pattern SAFE_CONTEXT_PATH = Pattern.compile("^/[-A-Za-z0-9._~/]*$");
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]+$");
    private static final Pattern SAFE_OAUTH_PARAM = Pattern.compile("^[A-Za-z0-9._~:-]{1,1024}$");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        String error = sanitizeOAuthParam(firstParam(req, "error"));
        if (error != null) {
            String description = sanitizeOAuthParam(firstParam(req, "error_description"));
            redirectWithMessage(resp, req, false,
                    "Salesforce authorization failed: " + safe(description != null ? description : error));
            return;
        }

        String code = sanitizeOAuthParam(firstParam(req, "code"));
        String state = sanitizeOAuthParam(firstParam(req, "state"));
        if (code == null || state == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing OAuth code/state.");
            return;
        }

        HttpSession session = req.getSession(false);
        if (!isValidState(session, state)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or expired OAuth state.");
            return;
        }

        // One-time state: clear immediately after validation
        clearState(session);

        ServerConfig cfg;
        try {
            cfg = EncryptedDbConfigStore.load();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load server configuration for OAuth callback", e);
            throw new ServletException("Unable to load server configuration", e);
        }

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        String clientSecret = trimToNull(cfg.getSalesforceClientSecret());

        if (loginUrl == null || clientId == null || clientSecret == null) {
            redirectWithMessage(resp, req, false,
                    "Missing Salesforce OAuth configuration. Please set Login URL, Client ID, and Client Secret.");
            return;
        }

        String redirectUri = buildExternalRedirectUri(req);
        String tokenUrl = normalizeBaseUrl(loginUrl) + "/services/oauth2/token";

        String form = "grant_type=authorization_code"
                + "&code=" + enc(code)
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&redirect_uri=" + enc(redirectUri);

        HttpRequest tokenReq = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> tokenRes = httpClient.send(tokenReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = tokenRes.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String statusCodeText = Integer.toString(statusCode);
                log.log(Level.WARNING, "Salesforce token exchange failed with HTTP " + statusCodeText);
                redirectWithMessage(resp, req, false,
                    "Token exchange failed (HTTP " + statusCodeText + ").");
                return;
            }

            TokenPayload payload = parseTokenPayload(tokenRes.body());
            if (payload == null || isBlank(payload.accessToken) || isBlank(payload.instanceUrl) || isBlank(payload.refreshToken)) {
                redirectWithMessage(resp, req, false,
                        "Token exchange returned incomplete payload (missing refresh token).");
                return;
            }

            // Persist securely (EncryptedDbConfigStore handles encryption-at-rest for sensitive values)
            cfg.setSalesforceApiKey(payload.accessToken);
            cfg.setSalesforceInstanceUrl(payload.instanceUrl);
            cfg.setSalesforceRefreshToken(payload.refreshToken);

            EncryptedDbConfigStore.save(cfg);

            redirectWithMessage(resp, req, true, "Salesforce OAuth connected successfully.");
        } catch (IOException | InterruptedException | SQLException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Salesforce OAuth callback failed", e);
            redirectWithMessage(resp, req, false, "Salesforce OAuth callback failed.");
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isValidState(HttpSession session, String stateFromRequest) {
        if (session == null || stateFromRequest == null) {
            return false;
        }

        Object stateObj = session.getAttribute(OAUTH_STATE_KEY);
        Object tsObj = session.getAttribute(OAUTH_STATE_TS_KEY);
        if (stateObj == null || tsObj == null) {
            return false;
        }

        String expectedState = String.valueOf(stateObj);
        long ts;
        try {
            ts = (tsObj instanceof Number) ? ((Number) tsObj).longValue() : Long.parseLong(String.valueOf(tsObj));
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid OAuth state timestamp in session", e);
            return false;
        }

        long age = System.currentTimeMillis() - ts;
        if (age < 0 || age > OAUTH_STATE_TTL_MS) {
            return false;
        }

        return constantTimeEquals(expectedState, stateFromRequest);
    }

    private void clearState(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(OAUTH_STATE_KEY);
        session.removeAttribute(OAUTH_STATE_TS_KEY);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < x.length; i++) {
            result |= x[i] ^ y[i];
        }
        return result == 0;
    }

    private TokenPayload parseTokenPayload(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject o = reader.readObject();
            TokenPayload p = new TokenPayload();
            p.accessToken = o.getString("access_token", null);
            p.refreshToken = o.getString("refresh_token", null);
            p.instanceUrl = o.getString("instance_url", null);
            return p;
        } catch (JsonException | ClassCastException e) {
            log.log(Level.WARNING, "Unable to parse Salesforce token payload", e);
            return null;
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, HttpServletRequest req, boolean ok, String message)
            throws IOException, ServletException {
        String status = ok ? "ok" : "error";
        String safeMessage = safe(message);

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.setAttribute("salesforceOAuthStatus", status);
            session.setAttribute("salesforceOAuthMessage", safeMessage);
        }

        req.setAttribute("salesforceOAuthStatus", status);
        req.setAttribute("salesforceOAuthMessage", safeMessage);
        req.getRequestDispatcher("/admin").forward(req, resp);
    }

    /**
     * Reverse-proxy aware redirect URI builder. Prefers X-Forwarded-* headers
     * when present.
     */
    private String buildExternalRedirectUri(HttpServletRequest req) {
        if (req == null) {
            return "https://localhost/admin/salesforce/oauth/callback";
        }
        String scheme = normalizeScheme(readForwardedHeader(req, "X-Forwarded-Proto"));
        if (isBlank(scheme)) {
            scheme = req.isSecure() ? "https" : "http";
        }

        String hostHeader = readForwardedHeader(req, "X-Forwarded-Host");
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            String h = hostHeader.trim();
            int idx = h.lastIndexOf(':');
            if (idx > 0 && idx < h.length() - 1 && h.indexOf(']') < 0) {
                host = sanitizeHost(h.substring(0, idx));
                port = parsePort(h.substring(idx + 1));
            } else {
                host = sanitizeHost(h);
            }
        } else {
            host = "localhost";
        }

        if (isBlank(host)) {
            host = "localhost";
        }

        String forwardedPort = readForwardedHeader(req, "X-Forwarded-Port");
        if (!isBlank(forwardedPort)) {
            int parsedPort = parsePort(forwardedPort);
            if (parsedPort > 0) {
                port = parsedPort;
            }
        }

        if (port <= 0) {
            port = "https".equalsIgnoreCase(scheme) ? 443 : 80;
        }

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(host);
        if (port > 0 && !defaultPort) {
            sb.append(':').append(port);
        }
        sb.append(safeContextPath(req.getServletContext().getContextPath())).append("/admin/salesforce/oauth/callback");
        return sb.toString();
    }

    private String firstToken(String headerVal) {
        if (headerVal == null) {
            return null;
        }
        int comma = headerVal.indexOf(',');
        String token = comma >= 0 ? headerVal.substring(0, comma) : headerVal;
        String trimmed = token.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String readForwardedHeader(HttpServletRequest req, String headerName) {
        if (req == null || headerName == null || headerName.isBlank()) {
            return null;
        }
        // parasoft-suppress BD.SECURITY.VPPD "Forwarded header values are tokenized, control-char stripped, and length-bounded before use."
        // parasoft-suppress CWE.352.VPPD "Forwarded header values are tokenized, control-char stripped, and length-bounded before use."
        // parasoft-suppress CWE.79.VPPD "Forwarded header values are tokenized, control-char stripped, and length-bounded before use."
        // parasoft-suppress OWASP2025.A1.VPPD "Forwarded header values are tokenized, control-char stripped, and length-bounded before use."
        // parasoft-suppress OWASP2025.A5.VPPD "Forwarded header values are tokenized, control-char stripped, and length-bounded before use."
        String raw = req.getHeader(headerName);
        if (raw == null) {
            return null;
        }
        String token = firstToken(raw);
        if (token == null || token.isBlank()) {
            return null;
        }
        String normalized = token.replace("\r", "").replace("\n", "").trim();
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith("http://") && !x.startsWith("https://")) {
            x = "https://" + x;
        }
        return x.replaceAll("/+$", "");
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 1024 ? normalized.substring(0, 1024) : normalized;
    }

    private String normalizeScheme(String scheme) {
        if (scheme == null) {
            return null;
        }
        String normalized = scheme.trim().toLowerCase();
        if ("http".equals(normalized) || "https".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private int parsePort(String rawPort) {
        if (rawPort == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(rawPort.trim());
            return (parsed >= 1 && parsed <= 65535) ? parsed : -1;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid forwarded port value", e);
            return -1;
        }
    }

    private String sanitizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String h = host.trim();
        if (!h.isEmpty() && h.charAt(0) == '[' && h.endsWith("]")) {
            String inner = h.substring(1, h.length() - 1);
            if (!inner.isBlank() && inner.matches("[0-9A-Fa-f:]+")) {
                return h;
            }
            return null;
        }
        return SAFE_HOST.matcher(h).matches() ? h : null;
    }

    private String safeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (!SAFE_CONTEXT_PATH.matcher(trimmed).matches() || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private String sanitizeOAuthParam(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (!SAFE_OAUTH_PARAM.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }

    private String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\r", " ").replace("\n", " ");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    static final class TokenPayload {

        String accessToken;
        String refreshToken;
        String instanceUrl;
    }
}
