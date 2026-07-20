package com.sim.chatserver.security;

import java.io.IOException;
import java.util.Locale;

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

    private static final String STATIC_CACHE_CONTROL = "public, max-age=" + STATIC_CACHE_SECONDS + ", immutable";

    private static final String DYNAMIC_CACHE_CONTROL = "no-store, no-cache, must-revalidate, max-age=0";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse resp) {
            // MIME sniff protection
            resp.setHeader("X-Content-Type-Options", "nosniff");

            // Clickjacking protection
            resp.setHeader("X-Frame-Options", "DENY");

            // Referrer leakage protection
            resp.setHeader("Referrer-Policy", "no-referrer");

            // Limit browser feature access
            resp.setHeader("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=(), payment=(), usb=(), accelerometer=(), gyroscope=()");

            // Conservative baseline CSP
            // NOTE: If you use CDN scripts/styles (e.g. marked, DOMPurify), adjust this policy accordingly.
            resp.setHeader("Content-Security-Policy",
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
                resp.setHeader("Cache-Control", STATIC_CACHE_CONTROL);
                resp.setDateHeader("Expires", System.currentTimeMillis() + (STATIC_CACHE_SECONDS * 1000L));
            } else {
                // Keep dynamic/authenticated pages non-cacheable.
                resp.setHeader("Cache-Control", DYNAMIC_CACHE_CONTROL);
                resp.setHeader("Pragma", "no-cache");
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isStaticAssetRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest req)) {
            return false;
        }

        String uri = req.getRequestURI();
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
}
