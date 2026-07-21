package com.sim.chatserver.security;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Applies baseline security headers to all responses.
 *
 * If you already set some headers elsewhere, this safely overwrites them with
 * secure defaults.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    private static final long STATIC_CACHE_SECONDS = 604800L; // 7 days
    private static final int MAX_HEADER_CHARS = 512;
    private static final int MAX_URI_CHARS = 2048;
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");

    private static final String STATIC_CACHE_CONTROL = "public, max-age=" + STATIC_CACHE_SECONDS + ", immutable";

    private static final String DYNAMIC_CACHE_CONTROL = "no-store, no-cache, must-revalidate, max-age=0";

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = request instanceof HttpServletRequest req ? req : null;
        HttpServletResponse httpResp = response instanceof HttpServletResponse resp ? resp : null;

        if (httpResp != null) {
            // MIME sniff protection
            httpResp.setHeader("X-Content-Type-Options", "nosniff");

            // Clickjacking protection
            httpResp.setHeader("X-Frame-Options", "DENY");

            // Referrer leakage protection
            httpResp.setHeader("Referrer-Policy", "no-referrer");

            // Limit browser feature access
            httpResp.setHeader("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=(), payment=(), usb=(), accelerometer=(), gyroscope=()");

            // Conservative baseline CSP
            // NOTE: If you use CDN scripts/styles (e.g. marked, DOMPurify), adjust this policy accordingly.
                httpResp.setHeader("Content-Security-Policy",
                    "default-src 'self'; "
                    + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                    + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                    + "img-src 'self' data: https:; "
                    + "font-src 'self' https://cdn.jsdelivr.net data:; "
                    + "connect-src 'self'; "
                    + "frame-ancestors 'none'; "
                    + "base-uri 'self'; "
                    + "form-action 'self'");

            if (isStaticAssetRequest(request)) {
                // Allow browser/proxy caching for static assets to avoid FOUC on navigation.
                httpResp.setHeader("Cache-Control", STATIC_CACHE_CONTROL);
                httpResp.setDateHeader("Expires", Instant.now().toEpochMilli() + (STATIC_CACHE_SECONDS * 1000L));
            } else {
                // Keep dynamic/authenticated pages non-cacheable.
                httpResp.setHeader("Cache-Control", DYNAMIC_CACHE_CONTROL);
                httpResp.setHeader("Pragma", "no-cache");
            }
        }

        chain.doFilter(request, response);

        if (httpReq != null && httpResp != null) {
            hardenSessionCookie(httpReq, httpResp);
        }
    }

    private void hardenSessionCookie(HttpServletRequest req, HttpServletResponse resp) {
        Collection<String> cookieHeaders = resp.getHeaders("Set-Cookie");
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            return;
        }

        boolean secureRequest = isSecureRequest(req);
        boolean changed = false;
        List<String> rewritten = new ArrayList<>(cookieHeaders.size());

        for (String header : cookieHeaders) {
            if (header == null) {
                continue;
            }

            String out = header;
            if (startsWithIgnoreCase(out, "JSESSIONID=")) {
                out = appendCookieAttributeIfMissing(out, "HttpOnly");
                out = appendCookieAttributeIfMissing(out, "SameSite=Lax");
                if (secureRequest) {
                    out = appendCookieAttributeIfMissing(out, "Secure");
                }
            }

            if (!out.equals(header)) {
                changed = true;
            }
            rewritten.add(out);
        }

        if (!changed || rewritten.isEmpty()) {
            return;
        }

        resp.setHeader("Set-Cookie", rewritten.get(0));
        for (int i = 1; i < rewritten.size(); i++) {
            resp.addHeader("Set-Cookie", rewritten.get(i));
        }
    }

    private String appendCookieAttributeIfMissing(String cookieHeader, String attribute) {
        if (cookieHeader == null || cookieHeader.isBlank() || attribute == null || attribute.isBlank()) {
            return cookieHeader;
        }
        if (hasCookieAttribute(cookieHeader, attribute)) {
            return cookieHeader;
        }
        return cookieHeader + "; " + attribute;
    }

    private boolean hasCookieAttribute(String cookieHeader, String attribute) {
        String attr = attribute.trim();
        if (attr.isEmpty()) {
            return false;
        }

        int eq = attr.indexOf('=');
        String attrName = (eq >= 0 ? attr.substring(0, eq) : attr).trim().toLowerCase(Locale.ROOT);
        if (attrName.isEmpty()) {
            return false;
        }

        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String token = part == null ? "" : part.trim();
            if (token.isEmpty()) {
                continue;
            }

            int tokenEq = token.indexOf('=');
            String tokenName = (tokenEq >= 0 ? token.substring(0, tokenEq) : token).trim().toLowerCase(Locale.ROOT);
            if (attrName.equals(tokenName)) {
                return true;
            }
        }
        return false;
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null && prefix != null && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private boolean isSecureRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        if (req.isSecure()) {
            return true;
        }

        String xForwardedProto = readHeaderCanonical(req, "X-Forwarded-Proto", MAX_HEADER_CHARS);
        if (xForwardedProto != null) {
            String first = firstCsvToken(xForwardedProto);
            if ("https".equalsIgnoreCase(first)) {
                return true;
            }
        }

        String xForwardedSsl = readHeaderCanonical(req, "X-Forwarded-Ssl", MAX_HEADER_CHARS);
        if ("on".equalsIgnoreCase(xForwardedSsl)) {
            return true;
        }

        String frontEndHttps = readHeaderCanonical(req, "Front-End-Https", MAX_HEADER_CHARS);
        return "on".equalsIgnoreCase(frontEndHttps);
    }

    private boolean isStaticAssetRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest req)) {
            return false;
        }

        String uri = readCanonicalRequestUri(req);
        if (uri == null || uri.isEmpty()) {
            return false;
        }

        String contextPath = req.getContextPath();
        String path = uri;
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());
        }

        if ("/favicon.ico".equals(path) || path.startsWith("/assets/")) {
            return true;
        }

        String lowerPath = path.toLowerCase(Locale.ROOT);
        return lowerPath.endsWith(".css")
                || lowerPath.endsWith(".js")
                || lowerPath.endsWith(".mjs")
                || lowerPath.endsWith(".png")
                || lowerPath.endsWith(".jpg")
                || lowerPath.endsWith(".jpeg")
                || lowerPath.endsWith(".gif")
                || lowerPath.endsWith(".svg")
                || lowerPath.endsWith(".ico")
                || lowerPath.endsWith(".webp")
                || lowerPath.endsWith(".woff")
                || lowerPath.endsWith(".woff2")
                || lowerPath.endsWith(".ttf")
                || lowerPath.endsWith(".map");
    }

    private String readCanonicalRequestUri(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        return canonicalizeInput(req.getRequestURI(), MAX_URI_CHARS);
    }

    private String readHeaderCanonical(HttpServletRequest req, String headerName, int maxChars) {
        if (req == null || headerName == null || headerName.isBlank()) {
            return null;
        }
        return canonicalizeInput(req.getHeader(headerName), maxChars);
    }

    private String canonicalizeInput(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (normalized.isEmpty() || CONTROL_CHARS.matcher(normalized).find()) {
            return null;
        }
        if (maxChars > 0 && normalized.length() > maxChars) {
            return normalized.substring(0, maxChars);
        }
        return normalized;
    }

    private String firstCsvToken(String value) {
        if (value == null) {
            return null;
        }
        int comma = value.indexOf(',');
        String token = comma >= 0 ? value.substring(0, comma) : value;
        return token.trim();
    }
}
