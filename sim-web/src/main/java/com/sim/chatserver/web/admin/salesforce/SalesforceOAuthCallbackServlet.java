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

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.json.Json;
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
    private static final Logger log = Logger.getLogger(SalesforceOAuthCallbackServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    private static final String OAUTH_STATE_KEY = "sf_oauth_state";
    private static final String OAUTH_STATE_TS_KEY = "sf_oauth_state_ts";
    private static final long OAUTH_STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes

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

        String error = trimToNull(req.getParameter("error"));
        if (error != null) {
            String description = trimToNull(req.getParameter("error_description"));
            redirectWithMessage(resp, req, false,
                    "Salesforce authorization failed: " + safe(description != null ? description : error));
            return;
        }

        String code = trimToNull(req.getParameter("code"));
        String state = trimToNull(req.getParameter("state"));
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
            if (tokenRes.statusCode() < 200 || tokenRes.statusCode() >= 300) {
                log.log(Level.WARNING, "Salesforce token exchange failed with HTTP {0}", tokenRes.statusCode());
                redirectWithMessage(resp, req, false,
                        "Token exchange failed (HTTP " + tokenRes.statusCode() + ").");
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to parse Salesforce token payload", e);
            return null;
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, HttpServletRequest req, boolean ok, String message) throws IOException {
        String url = req.getContextPath()
                + "/admin?salesforceOAuthStatus=" + (ok ? "ok" : "error")
                + "&salesforceOAuthMessage=" + enc(message);
        resp.sendRedirect(url);
    }

    /**
     * Reverse-proxy aware redirect URI builder. Prefers X-Forwarded-* headers
     * when present.
     */
    private String buildExternalRedirectUri(HttpServletRequest req) {
        String scheme = firstToken(req.getHeader("X-Forwarded-Proto"));
        if (isBlank(scheme)) {
            scheme = req.getScheme();
        }

        String hostHeader = firstToken(req.getHeader("X-Forwarded-Host"));
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            String h = hostHeader.trim();
            int idx = h.lastIndexOf(':');
            if (idx > 0 && idx < h.length() - 1 && h.indexOf(']') < 0) {
                host = h.substring(0, idx);
                try {
                    port = Integer.parseInt(h.substring(idx + 1));
                } catch (NumberFormatException ignored) {
                    port = -1;
                }
            } else {
                host = h;
            }
        } else {
            host = req.getServerName();
            port = req.getServerPort();
        }

        String forwardedPort = firstToken(req.getHeader("X-Forwarded-Port"));
        if (!isBlank(forwardedPort)) {
            try {
                port = Integer.parseInt(forwardedPort);
            } catch (NumberFormatException ignored) {
                // keep prior value
            }
        }

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(host);
        if (port > 0 && !defaultPort) {
            sb.append(':').append(port);
        }
        sb.append(req.getContextPath()).append("/admin/salesforce/oauth/callback");
        return sb.toString();
    }

    private String firstToken(String headerVal) {
        if (headerVal == null) {
            return null;
        }
        int comma = headerVal.indexOf(',');
        String token = comma >= 0 ? headerVal.substring(0, comma) : headerVal;
        return token == null ? null : token.trim();
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith("http://") && !x.startsWith("https://")) {
            x = "https://" + x;
        }
        return x.replaceAll("/+$", "");
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

    private String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\r", " ").replace("\n", " ");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static final class TokenPayload {

        String accessToken;
        String refreshToken;
        String instanceUrl;
    }
}
