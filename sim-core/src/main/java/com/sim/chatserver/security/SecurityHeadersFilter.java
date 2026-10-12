package com.sim.chatserver.security;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.sim.chatserver.util.CommonLiteralConstants;

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
@WebFilter(SecurityHeadersFilter.WEB_FILTER_PATTERN)
public class SecurityHeadersFilter implements Filter {

    public static final String WEB_FILTER_PATTERN = "/*";
    private static final long STATIC_CACHE_SECONDS = 604800L; // 7 days
    private static final int MAX_HEADER_CHARS = 512;
    private static final int MAX_URI_CHARS = 2048;
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);

    private static final String STATIC_CACHE_CONTROL = "public, max-age=" + STATIC_CACHE_SECONDS + ", immutable";

    private static final String DYNAMIC_CACHE_CONTROL = "no-store, no-cache, must-revalidate, max-age=0";
        private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
        private static final String VALUE_NOSNIFF = "nosniff";
        private static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
        private static final String VALUE_DENY = "DENY";
        private static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
        private static final String VALUE_NO_REFERRER = "no-referrer";
        private static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";
        private static final String VALUE_PERMISSIONS_POLICY = "geolocation=(), microphone=(), camera=(), payment=(), usb=(), accelerometer=(), gyroscope=()";
        private static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
        private static final String VALUE_CSP = "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
            + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
            + "img-src 'self' data: https:; "
            + "font-src 'self' https://cdn.jsdelivr.net data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "form-action 'self'";
        private static final String HEADER_CACHE_CONTROL = "Cache-Control";
        private static final String HEADER_EXPIRES = "Expires";
        private static final String HEADER_PRAGMA = "Pragma";
        private static final String VALUE_NO_CACHE = "no-cache";
        private static final String HEADER_SET_COOKIE = "Set-Cookie";
        private static final String COOKIE_JSESSIONID_PREFIX = "JSESSIONID=";
        private static final String COOKIE_ATTR_HTTP_ONLY = "HttpOnly";
        private static final String COOKIE_ATTR_SAMESITE_LAX = "SameSite=Lax";
        private static final String COOKIE_ATTR_SECURE = "Secure";
        private static final String COOKIE_ATTR_SEPARATOR = "; ";
        private static final String EMPTY = "";
        private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
        private static final String VALUE_HTTPS = CommonLiteralConstants.SCHEME_HTTPS;
        private static final String HEADER_X_FORWARDED_SSL = "X-Forwarded-Ssl";
        private static final String HEADER_FRONT_END_HTTPS = "Front-End-Https";
        private static final String VALUE_ON = "on";
        private static final String PATH_FAVICON = "/favicon.ico";
        private static final String PATH_ASSETS_PREFIX = "/assets/";
        private static final String EXT_CSS = ".css";
        private static final String EXT_JS = ".js";
        private static final String EXT_MJS = ".mjs";
        private static final String EXT_PNG = ".png";
        private static final String EXT_JPG = ".jpg";
        private static final String EXT_JPEG = ".jpeg";
        private static final String EXT_GIF = ".gif";
        private static final String EXT_SVG = ".svg";
        private static final String EXT_ICO = ".ico";
        private static final String EXT_WEBP = ".webp";
        private static final String EXT_WOFF = ".woff";
        private static final String EXT_WOFF2 = ".woff2";
        private static final String EXT_TTF = ".ttf";
        private static final String EXT_MAP = ".map";

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = request instanceof HttpServletRequest req ? req : null;
        HttpServletResponse httpResp = response instanceof HttpServletResponse resp ? resp : null;

        if (httpResp != null) {
            // MIME sniff protection
            httpResp.setHeader(HEADER_X_CONTENT_TYPE_OPTIONS, VALUE_NOSNIFF);

            // Clickjacking protection
            httpResp.setHeader(HEADER_X_FRAME_OPTIONS, VALUE_DENY);

            // Referrer leakage protection
            httpResp.setHeader(HEADER_REFERRER_POLICY, VALUE_NO_REFERRER);

            // Limit browser feature access
            httpResp.setHeader(HEADER_PERMISSIONS_POLICY, VALUE_PERMISSIONS_POLICY);

            // Conservative baseline CSP
            // NOTE: If you use CDN scripts/styles (e.g. marked, DOMPurify), adjust this policy accordingly.
                httpResp.setHeader(HEADER_CONTENT_SECURITY_POLICY, VALUE_CSP);

            if (isStaticAssetRequest(request)) {
                // Allow browser/proxy caching for static assets to avoid FOUC on navigation.
                httpResp.setHeader(HEADER_CACHE_CONTROL, STATIC_CACHE_CONTROL);
                httpResp.setDateHeader(HEADER_EXPIRES, Instant.now().toEpochMilli() + (STATIC_CACHE_SECONDS * 1000L));
            } else {
                // Keep dynamic/authenticated pages non-cacheable.
                httpResp.setHeader(HEADER_CACHE_CONTROL, DYNAMIC_CACHE_CONTROL);
                httpResp.setHeader(HEADER_PRAGMA, VALUE_NO_CACHE);
            }
        }

        chain.doFilter(request, response);

        if (httpReq != null && httpResp != null) {
            hardenSessionCookie(httpReq, httpResp);
        }
    }

    private void hardenSessionCookie(HttpServletRequest req, HttpServletResponse resp) {
        Collection<String> cookieHeaders = resp.getHeaders(HEADER_SET_COOKIE);
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
            if (startsWithIgnoreCase(out, COOKIE_JSESSIONID_PREFIX)) {
                out = appendCookieAttributeIfMissing(out, COOKIE_ATTR_HTTP_ONLY);
                out = appendCookieAttributeIfMissing(out, COOKIE_ATTR_SAMESITE_LAX);
                if (secureRequest) {
                    out = appendCookieAttributeIfMissing(out, COOKIE_ATTR_SECURE);
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

        resp.setHeader(HEADER_SET_COOKIE, rewritten.get(0));
        for (int i = 1; i < rewritten.size(); i++) {
            resp.addHeader(HEADER_SET_COOKIE, rewritten.get(i));
        }
    }

    private String appendCookieAttributeIfMissing(String cookieHeader, String attribute) {
        if (cookieHeader == null || cookieHeader.isBlank() || attribute == null || attribute.isBlank()) {
            return cookieHeader;
        }
        if (hasCookieAttribute(cookieHeader, attribute)) {
            return cookieHeader;
        }
        return cookieHeader + COOKIE_ATTR_SEPARATOR + attribute;
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

        String[] parts = cookieHeader.split(String.valueOf((char) 59));
        for (String part : parts) {
            String token = part == null ? EMPTY : part.trim();
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

        String xForwardedProto = readHeaderCanonical(req, HEADER_X_FORWARDED_PROTO, MAX_HEADER_CHARS);
        if (xForwardedProto != null) {
            String first = firstCsvToken(xForwardedProto);
            if (VALUE_HTTPS.equalsIgnoreCase(first)) {
                return true;
            }
        }

        String xForwardedSsl = readHeaderCanonical(req, HEADER_X_FORWARDED_SSL, MAX_HEADER_CHARS);
        if (VALUE_ON.equalsIgnoreCase(xForwardedSsl)) {
            return true;
        }

        String frontEndHttps = readHeaderCanonical(req, HEADER_FRONT_END_HTTPS, MAX_HEADER_CHARS);
        return VALUE_ON.equalsIgnoreCase(frontEndHttps);
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

        if (PATH_FAVICON.equals(path) || path.startsWith(PATH_ASSETS_PREFIX)) {
            return true;
        }

        String lowerPath = path.toLowerCase(Locale.ROOT);
        return lowerPath.endsWith(EXT_CSS)
                || lowerPath.endsWith(EXT_JS)
                || lowerPath.endsWith(EXT_MJS)
                || lowerPath.endsWith(EXT_PNG)
                || lowerPath.endsWith(EXT_JPG)
                || lowerPath.endsWith(EXT_JPEG)
                || lowerPath.endsWith(EXT_GIF)
                || lowerPath.endsWith(EXT_SVG)
                || lowerPath.endsWith(EXT_ICO)
                || lowerPath.endsWith(EXT_WEBP)
                || lowerPath.endsWith(EXT_WOFF)
                || lowerPath.endsWith(EXT_WOFF2)
                || lowerPath.endsWith(EXT_TTF)
                || lowerPath.endsWith(EXT_MAP);
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
            return new String(normalized.toCharArray(), 0, maxChars);
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
