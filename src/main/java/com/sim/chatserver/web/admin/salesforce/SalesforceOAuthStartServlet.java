package com.sim.chatserver.web.admin.salesforce;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final long OAUTH_STATE_TTL_MS = 10 * 60 * 1000L; // 10 minutes

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
        session.setAttribute(OAUTH_STATE_TS_KEY, System.currentTimeMillis());

        String authorizeUrl = normalizeBaseUrl(loginUrl)
                + "/services/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(state)
                + "&scope=" + enc("api refresh_token")
                + "&prompt=" + enc("consent");

        log.info(() -> "Redirecting admin to Salesforce authorize endpoint.");
        resp.sendRedirect(authorizeUrl);
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
        String scheme = firstToken(req.getHeader("X-Forwarded-Proto"));
        if (isBlank(scheme)) {
            scheme = req.getScheme();
        }

        String hostHeader = firstToken(req.getHeader("X-Forwarded-Host"));
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            // X-Forwarded-Host may be "example.com" or "example.com:8443"
            String h = hostHeader.trim();
            int idx = h.lastIndexOf(':');
            if (idx > 0 && idx < h.length() - 1 && h.indexOf(']') < 0) { // simplistic IPv6-safe check
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
