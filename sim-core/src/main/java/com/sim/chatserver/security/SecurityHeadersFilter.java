package com.sim.chatserver.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Applies baseline security headers to all responses.
 *
 * If you already set some headers elsewhere, this safely overwrites them with
 * secure defaults.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

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

            // Disable caching for dynamic authenticated pages by default
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            resp.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(request, response);
    }
}
