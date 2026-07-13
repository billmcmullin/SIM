package com.sim.chatserver.web.admin.salesforce;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Starts Salesforce OAuth Authorization Code flow.
 *
 * Security: - Requires authenticated admin session - Uses state nonce to
 * prevent CSRF - Stores state in session with short TTL
 *
 * Operational hardening: - Reverse-proxy aware redirect_uri construction
 * (X-Forwarded-* support) - No sensitive values in logs
 */
//@WebServlet(name = "SalesforceOAuthStartServlet", urlPatterns = {"/admin/salesforce/oauth/start"})
public class SalesforceOAuthStartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(SalesforceOAuthStartServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    private static final String OAUTH_STATE_KEY = "sf_oauth_state";
    private static final String OAUTH_STATE_TS_KEY = "sf_oauth_state_ts";
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}$");
    private static final Pattern SAFE_PORT = Pattern.compile("^\\d{1,5}$");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        ServerConfig cfg;
        try {
            cfg = EncryptedDbConfigStore.load();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load server configuration for Salesforce OAuth start", e);
            throw new ServletException("Unable to load server configuration", e);
        }

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        if (loginUrl == null || clientId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing Salesforce OAuth configuration: login URL and client ID are required.");
            return;
        }

        String redirectUri = buildExternalRedirectUri(req);

        String state = generateState();
        HttpSession session = req.getSession(true);
        session.setAttribute(OAUTH_STATE_KEY, state);
        session.setAttribute(OAUTH_STATE_TS_KEY, String.valueOf(System.currentTimeMillis()));

        String authorizeUrl = normalizeBaseUrl(loginUrl)
                + "/services/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(state)
                + "&scope=" + enc("api refresh_token")
                + "&prompt=" + enc("consent");

        if (!isSafeAuthorizeUrl(authorizeUrl)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Salesforce authorize URL.");
            return;
        }

        log.info(() -> "Redirecting admin to Salesforce authorize endpoint.");
        resp.sendRedirect(resp.encodeRedirectURL(authorizeUrl));
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

    /**
     * Reverse-proxy aware redirect URI builder. Prefers X-Forwarded-* headers
     * when present.
     */
    private String buildExternalRedirectUri(HttpServletRequest req) {
        String scheme = sanitizeScheme(firstToken(req.getHeader("X-Forwarded-Proto")));
        if (isBlank(scheme)) {
            scheme = "https";
        }

        String hostHeader = firstToken(req.getHeader("X-Forwarded-Host"));
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            // X-Forwarded-Host may be "example.com" or "example.com:8443"
            String h = hostHeader.trim();
            int idx = h.lastIndexOf(':');
            if (idx > 0 && idx < h.length() - 1 && h.indexOf(']') < 0) { // simplistic IPv6-safe check
                host = sanitizeHost(h.substring(0, idx));
                try {
                    String hostPort = h.substring(idx + 1).trim();
                    if (SAFE_PORT.matcher(hostPort).matches()) {
                        port = Integer.parseInt(hostPort);
                    }
                } catch (NumberFormatException ex) {
                    log.log(Level.FINE, "Invalid forwarded host port", ex);
                    port = -1;
                }
            } else {
                host = sanitizeHost(h);
            }
        } else {
            host = sanitizeHost(req.getServerName());
        }

        String forwardedPort = firstToken(req.getHeader("X-Forwarded-Port"));
        if (!isBlank(forwardedPort)) {
            try {
                String normalized = forwardedPort.trim();
                if (SAFE_PORT.matcher(normalized).matches()) {
                    port = Integer.parseInt(normalized);
                }
            } catch (NumberFormatException ex) {
                log.log(Level.FINE, "Invalid forwarded port header", ex);
            }
        }

        if (isBlank(host)) {
            host = "localhost";
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
        return token.trim();
    }

    private static String generateState() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith("http://") && !x.startsWith("https://")) {
            x = "https://" + x;
        }
        return x.replaceAll("/+$", "");
    }

    private boolean isSafeAuthorizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (url.contains("\r") || url.contains("\n")) {
            return false;
        }
        try {
            java.net.URI parsed = java.net.URI.create(url);
            String scheme = sanitizeScheme(parsed.getScheme());
            String host = sanitizeHost(parsed.getHost());
            return !isBlank(scheme) && !isBlank(host);
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid authorize URL", ex);
            return false;
        }
    }

    private String sanitizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalized = host.trim().toLowerCase();
        return SAFE_HOST.matcher(normalized).matches() ? normalized : null;
    }

    private String sanitizeScheme(String scheme) {
        if (scheme == null || scheme.isBlank()) {
            return null;
        }
        String normalized = scheme.trim().toLowerCase();
        if ("http".equals(normalized) || "https".equals(normalized)) {
            return normalized;
        }
        return null;
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

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
