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
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

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
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(SalesforceOAuthCallbackServlet.class.getCanonicalName());
    private static final String LS = System.lineSeparator();

    private static final String SESSION_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SESSION_ROLE = com.sim.chatserver.util.CommonLiteralConstants.ROLE;

    private static final String OAUTH_STATE_KEY = "sf_oauth_state";
    private static final String OAUTH_STATE_TS_KEY = "sf_oauth_state_ts";
    private static final long OAUTH_STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private static final Pattern SAFE_CONTEXT_PATH = Pattern.compile("^/[-A-Za-z0-9._~/]*$");
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]+$");
    private static final Pattern SAFE_OAUTH_PARAM = Pattern.compile("^[A-Za-z0-9._~:-]{1,1024}$");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        String error = sanitizeOAuthParam(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR, 1024, true, true));
        if (error != null) {
            String description = sanitizeOAuthParam(ServletRequestParamUtil.firstParam(req, "error_description", 1024, true, true));
            redirectWithMessage(resp, req, false,
                    "Salesforce authorization failed: " + safe(description != null ? description : error));
            return;
        }

        String code = sanitizeOAuthParam(ServletRequestParamUtil.firstParam(req, "code", 1024, true, true));
        String state = sanitizeOAuthParam(ServletRequestParamUtil.firstParam(req, "state", 1024, true, true));
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
            throw new ServletException(com.sim.chatserver.util.CommonLiteralConstants.MSG_LOAD_SERVER_CONFIGURATION, e);
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
                .header(com.sim.chatserver.util.CommonLiteralConstants.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(com.sim.chatserver.util.CommonLiteralConstants.HEADER_ACCEPT, com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        String requestId = UUID.randomUUID().toString();

        try {
            ServerDiagnosticsLog.write(
                "salesforce-oauth-callback",
                requestId,
                "token-request",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_METHOD_POST + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + tokenUrl
            );

            HttpResponse<String> tokenRes = HTTP_CLIENT.send(tokenReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = tokenRes.statusCode();
            String body = tokenRes.body() == null ? "" : tokenRes.body();

            ServerDiagnosticsLog.write(
                "salesforce-oauth-callback",
                requestId,
                "token-response",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS + statusCode + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY + redactOauthPayload(body)
            );

            if (statusCode < 200 || statusCode >= 300) {
                String statusCodeText = Integer.toString(statusCode);
                log.log(Level.WARNING, "Salesforce token exchange failed with HTTP {0}", statusCodeText);
                redirectWithMessage(resp, req, false,
                    "Token exchange failed (HTTP " + statusCodeText + ").");
                return;
            }

            TokenPayload payload = parseTokenPayload(body);
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, "Salesforce OAuth callback failed", e);
            ServerDiagnosticsLog.write(
                    "salesforce-oauth-callback",
                    requestId,
                    "token-error",
                    com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + tokenUrl + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE + safe(e.getMessage()),
                    e
            );
            redirectWithMessage(resp, req, false, "Salesforce OAuth connection failed.");
        } catch (IOException | SQLException e) {
            log.log(Level.WARNING, "Salesforce OAuth callback failed", e);
            ServerDiagnosticsLog.write(
                    "salesforce-oauth-callback",
                    requestId,
                    "token-error",
                    com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + tokenUrl + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE + safe(e.getMessage()),
                    e
            );
            redirectWithMessage(resp, req, false, "Salesforce OAuth callback failed.");
        }
    
        } catch (IOException | ServletException | IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN.equalsIgnoreCase(role);
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
            ts = Long.parseLong(String.valueOf(tsObj));
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid OAuth state timestamp in session", e);
            return false;
        }

        long age = Instant.now().toEpochMilli() - ts;
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

    private void redirectWithMessage(HttpServletResponse resp, HttpServletRequest req, boolean ok, String message) {
        String status = ok ? "ok" : com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
        String safeMessage = safe(message);

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.setAttribute("salesforceOAuthStatus", status);
            session.setAttribute("salesforceOAuthMessage", safeMessage);
        }

        req.setAttribute("salesforceOAuthStatus", status);
        req.setAttribute("salesforceOAuthMessage", safeMessage);
        try {
            req.getRequestDispatcher("/admin").forward(req, resp);
        } catch (IOException | ServletException e) {
            throw new IllegalStateException("Unable to forward OAuth callback response", e);
        }
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
            scheme = req.isSecure() ? com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS : com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP;
        }

        String hostHeader = readForwardedHeader(req, "X-Forwarded-Host");
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            String h = hostHeader.trim();
            int idx = h.lastIndexOf((char) 58);
            if (idx > 0 && idx < h.length() - 1 && h.indexOf(']') < 0) {
                host = sanitizeHost(h.substring(0, idx));
                port = parsePort(h.substring(idx + 1));
            } else {
                host = sanitizeHost(h);
            }
        } else {
            host = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
        }

        if (isBlank(host)) {
            host = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
        }

        String forwardedPort = readForwardedHeader(req, "X-Forwarded-Port");
        if (!isBlank(forwardedPort)) {
            int parsedPort = parsePort(forwardedPort);
            if (parsedPort > 0) {
                port = parsedPort;
            }
        }

        if (port <= 0) {
            port = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS.equalsIgnoreCase(scheme) ? 443 : 80;
        }

        boolean defaultPort = (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP.equalsIgnoreCase(scheme) && port == 80)
                || (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS.equalsIgnoreCase(scheme) && port == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR).append(host);
        if (port > 0 && !defaultPort) {
            sb.append((char) 58).append(port);
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
        String raw = req.getHeader(headerName);
        if (raw == null) {
            return null;
        }
        String token = firstToken(raw);
        if (token == null || token.isBlank()) {
            return null;
        }
        String normalized = token.replace(System.lineSeparator(), "").replace(System.lineSeparator(), "").trim();
        return normalized.length() > 256 ? new String(normalized.toCharArray(), 0, 256) : normalized;
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP) && !x.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
            x = com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS + x;
        }
        return x.replaceAll(com.sim.chatserver.util.CommonLiteralConstants.REGEX_TRAILING_SLASHES, "");
    }

    private String normalizeScheme(String scheme) {
        if (scheme == null) {
            return null;
        }
        String normalized = scheme.trim().toLowerCase();
        if (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP.equals(normalized) || com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS.equals(normalized)) {
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
        if (!SAFE_CONTEXT_PATH.matcher(trimmed).matches() || trimmed.contains(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR) || trimmed.contains(System.lineSeparator()) || trimmed.contains(System.lineSeparator())) {
            return "";
        }
        return trimmed;
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String redactOauthPayload(String payload) {
        String text = payload == null ? "" : payload;
        text = text.replaceAll("\"access_token\"\\s*:\\s*\"[^\"]*\"", "\"access_token\":\"[REDACTED]\"");
        text = text.replaceAll("\"refresh_token\"\\s*:\\s*\"[^\"]*\"", "\"refresh_token\":\"[REDACTED]\"");
        return text.length() > 1024 ? new String(text.toCharArray(), 0, 1024) + com.sim.chatserver.util.CommonLiteralConstants.ELLIPSIS : text;
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
        return s.replace(System.lineSeparator(), " ").replace(System.lineSeparator(), " ");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }



}

final class TokenPayload {

    String accessToken;
    String refreshToken;
    String instanceUrl;
}

