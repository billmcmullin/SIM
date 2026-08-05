package com.sim.chatserver.web.util;

/**
 * Shared servlet path normalization helpers.
 */
public final class ServletPathUtil {

    private ServletPathUtil() {
        // utility
    }

    public static String safeContextPathStrict(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '/' || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    public static String safeContextPathNoEmptyGuard(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (trimmed.charAt(0) != '/' || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    public static String safeContextPathEnsureLeadingSlash(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim().replace("\r", "").replace("\n", "");
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.charAt(0) == '/' ? trimmed : '/' + trimmed;
    }

    public static String safeContextPathNoTrailingSlash(String contextPath) {
        if (contextPath == null) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (trimmed.isEmpty() || "/".equals(trimmed)) {
            return "";
        }
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
