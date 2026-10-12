package com.sim.chatserver.web.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shared servlet path normalization helpers.
 */
public final class ServletPathUtil {

    private static final String EMPTY = "";
    private static final String URI_SCHEME_SEPARATOR = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR;
    private static final String ROOT_PATH = "/";
    private static final String TRAILING_SLASH = "/";
    private static final String LOGIN_DISPATCH_PATH = "/log" + "in";

    private ServletPathUtil() {
        // utility
    }

    public static String safeContextPathStrict(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return EMPTY;
        }
        String trimmed = contextPath.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '/' || trimmed.contains(URI_SCHEME_SEPARATOR) || trimmed.contains(System.lineSeparator()) || trimmed.contains(System.lineSeparator())) {
            return EMPTY;
        }
        return trimmed;
    }

    public static String safeContextPathNoEmptyGuard(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return EMPTY;
        }
        String trimmed = contextPath.trim();
        if (trimmed.charAt(0) != '/' || trimmed.contains(URI_SCHEME_SEPARATOR) || trimmed.contains(System.lineSeparator()) || trimmed.contains(System.lineSeparator())) {
            return EMPTY;
        }
        return trimmed;
    }

    public static String safeContextPathEnsureLeadingSlash(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return EMPTY;
        }
        String trimmed = contextPath.trim().replace(System.lineSeparator(), EMPTY).replace(System.lineSeparator(), EMPTY);
        if (trimmed.isEmpty()) {
            return EMPTY;
        }
        return trimmed.charAt(0) == '/' ? trimmed : '/' + trimmed;
    }

    public static String safeContextPathNoTrailingSlash(String contextPath) {
        if (contextPath == null) {
            return EMPTY;
        }
        String trimmed = contextPath.trim();
        if (trimmed.isEmpty() || ROOT_PATH.equals(trimmed)) {
            return EMPTY;
        }
        return trimmed.endsWith(TRAILING_SLASH) ? new String(trimmed.toCharArray(), 0, trimmed.length() - 1) : trimmed;
    }

    public static void forwardToLogin(HttpServletRequest req, HttpServletResponse resp, Logger log) {
        if (req == null || resp == null) {
            return;
        }
        if (!isAllowedForwardPath(LOGIN_DISPATCH_PATH)) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_FORWARD_PATH, log);
            return;
        }
        try {
            req.getRequestDispatcher(LOGIN_DISPATCH_PATH).forward(req, resp);
        } catch (IOException | ServletException ex) {
            if (log != null) {
                log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNABLE_TO_FORWARD_LOGIN, ex);
            }
            sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED, log);
        }
    }

    private static boolean isAllowedForwardPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String p = path.trim();
        if (p.isEmpty() || p.charAt(0) != '/' || p.contains(URI_SCHEME_SEPARATOR)) {
            return false;
        }
        return LOGIN_DISPATCH_PATH.equals(p);
    }

    private static void sendErrorSafe(HttpServletResponse resp, int status, String message, Logger log) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            if (log != null) {
                log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
            }
        }
    }
}
