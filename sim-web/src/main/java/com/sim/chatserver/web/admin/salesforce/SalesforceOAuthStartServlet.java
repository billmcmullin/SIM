package com.sim.chatserver.web.admin.salesforce;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.web.util.ServletPathUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
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
    private static final Logger log = Logger.getLogger(SalesforceOAuthStartServlet.class.getCanonicalName());

    private static final String SESSION_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SESSION_ROLE = com.sim.chatserver.util.CommonLiteralConstants.ROLE;

    private static final String OAUTH_STATE_KEY = "sf_oauth_state";
    private static final String OAUTH_STATE_TS_KEY = "sf_oauth_state_ts";
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}$");
    private static final Pattern SAFE_PORT = Pattern.compile("^\\d{1,5}$");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            return;
        }

        ServerConfig cfg;
        try {
            cfg = EncryptedDbConfigStore.load();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load server configuration for Salesforce OAuth start", e);
            throw new ServletException(com.sim.chatserver.util.CommonLiteralConstants.MSG_LOAD_SERVER_CONFIGURATION, e);
        }

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        if (loginUrl == null || clientId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing Salesforce OAuth configuration: login URL and client ID are required.");
            return;
        }

        URI baseUri = normalizeBaseUri(loginUrl);
        if (baseUri == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Salesforce login URL configuration.");
            return;
        }

        String redirectUri = buildExternalRedirectUri(req);

        String state = generateState();
        HttpSession session = req.getSession(true);
        session.setAttribute(OAUTH_STATE_KEY, state);
        session.setAttribute(OAUTH_STATE_TS_KEY, String.valueOf(Instant.now().toEpochMilli()));

        String authorizeUrl = baseUri.toString()
                + "/services/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(state)
                + "&scope=" + enc("api refresh_token")
                + "&prompt=" + enc("consent");

        String safeAuthorizeUrl = toSafeAuthorizeUrl(authorizeUrl);
        if (safeAuthorizeUrl == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Salesforce authorize URL.");
            return;
        }
        if (!isSafeAuthorizeUrl(safeAuthorizeUrl)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsafe Salesforce authorize URL.");
            return;
        }

        log.info(() -> "Redirecting admin to Salesforce authorize endpoint.");
        safeRedirect(resp, safeAuthorizeUrl);
    
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

    /**
     * Reverse-proxy aware redirect URI builder. Prefers X-Forwarded-* headers
     * when present.
     */
    private String buildExternalRedirectUri(HttpServletRequest req) {
        String scheme = sanitizeScheme(readForwardedHeader(req, "X-Forwarded-Proto"));
        if (isBlank(scheme)) {
            scheme = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS;
        }

        String hostHeader = readForwardedHeader(req, "X-Forwarded-Host");
        String host;
        int port = -1;

        if (!isBlank(hostHeader)) {
            // X-Forwarded-Host may be "example.com" or "example.com:8443"
            String h = hostHeader.trim();
            int idx = h.lastIndexOf((char) 58);
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
            host = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
        }

        String forwardedPort = readForwardedHeader(req, "X-Forwarded-Port");
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
            host = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
        }

        boolean defaultPort = (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP.equalsIgnoreCase(scheme) && port == 80)
                || (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS.equalsIgnoreCase(scheme) && port == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR).append(host);
        if (port > 0 && !defaultPort) {
            sb.append((char) 58).append(port);
        }
        sb.append(ServletPathUtil.safeContextPathEnsureLeadingSlash(req.getContextPath()))
            .append("/admin/salesforce/oauth/callback");
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

    private String readForwardedHeader(HttpServletRequest req, String headerName) {
        if (req == null || headerName == null || headerName.isBlank()) {
            return null;
        }

        Enumeration<String> headers = req.getHeaders(headerName);
        if (headers == null) {
            return null;
        }

        while (headers.hasMoreElements()) {
            String raw = headers.nextElement();
            String token = firstToken(raw);
            if (token == null || token.isBlank()) {
                continue;
            }
            String normalized = token.replace(System.lineSeparator(), "").replace(System.lineSeparator(), "").trim();
            if (normalized.isEmpty()) {
                continue;
            }
            return normalized.length() > 256 ? new String(normalized.toCharArray(), 0, 256) : normalized;
        }

        return null;
    }

    private static String generateState() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private URI normalizeBaseUri(String url) {
        String x = url == null ? "" : url.trim();
        if (!x.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP) && !x.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
            x = com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS + x;
        }
        x = x.replaceAll(com.sim.chatserver.util.CommonLiteralConstants.REGEX_TRAILING_SLASHES, "");
        try {
            URI parsed = URI.create(x).normalize();
            String scheme = sanitizeScheme(parsed.getScheme());
            String host = sanitizeHost(parsed.getHost());
            if (isBlank(scheme) || isBlank(host)) {
                return null;
            }
            StringBuilder b = new StringBuilder();
            b.append(scheme).append(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR).append(host);
            if (parsed.getPort() > 0) {
                b.append((char) 58).append(parsed.getPort());
            }
            return URI.create(b.toString());
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid Salesforce base URL", ex);
            return null;
        }
    }

    private boolean isSafeAuthorizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (url.contains(System.lineSeparator()) || url.contains(System.lineSeparator())) {
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

    private String toSafeAuthorizeUrl(String candidate) {
        if (!isSafeAuthorizeUrl(candidate)) {
            return null;
        }
        try {
            java.net.URI parsed = java.net.URI.create(candidate).normalize();
            String path = parsed.getPath();
            if (path == null || !path.endsWith("/services/oauth2/authorize")) {
                return null;
            }
            return parsed.toString();
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Unable to normalize authorize URL", ex);
            return null;
        }
    }

    private void safeRedirect(HttpServletResponse resp, String target) {
        if (target == null || target.isBlank() || target.contains(System.lineSeparator()) || target.contains(System.lineSeparator())) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsafe Salesforce authorize URL.");
            return;
        }
        java.net.URI parsed;
        try {
            parsed = java.net.URI.create(target).normalize();
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid authorize redirect URL", ex);
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsafe Salesforce authorize URL.");
            return;
        }
        String scheme = sanitizeScheme(parsed.getScheme());
        String host = sanitizeHost(parsed.getHost());
        String path = parsed.getPath();
        if (isBlank(scheme) || isBlank(host) || path == null || !path.endsWith("/services/oauth2/authorize")) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsafe Salesforce authorize URL.");
            return;
        }
        String safeLocation = parsed.toString();
        resp.setStatus(HttpServletResponse.SC_FOUND);
        resp.setHeader("Location", safeLocation);
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to send OAuth start error response", e);
        }
    }

    private String sanitizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalized = host.trim().toLowerCase(java.util.Locale.ROOT);
        return SAFE_HOST.matcher(normalized).matches() ? normalized : null;
    }

    private String sanitizeScheme(String scheme) {
        if (scheme == null || scheme.isBlank()) {
            return null;
        }
        String normalized = scheme.trim().toLowerCase(java.util.Locale.ROOT);
        if (com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP.equals(normalized) || com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS.equals(normalized)) {
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}

