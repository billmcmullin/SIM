package com.sim.chatserver.util;

import java.time.Instant;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared DB helpers for dashboard-related code.
 */
public final class DashboardDbUtil {

    private DashboardDbUtil() {
    }

    public static String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }

        String trimmed = widgetId.trim();
        StringBuilder sb = new StringBuilder(Math.min(trimmed.length() + 2, 64));
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }

        String normalized = sb.length() == 0 ? "widget" : sb.toString();
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = new String(normalized.toCharArray(), 0, 60);
        }
        return normalized;
    }

    public static String quoteIdentifier(String identifier) {
        if (identifier == null) {
            return "\"\"";
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        if (conn == null || tableName == null || tableName.isBlank()) {
            return false;
        }
        DatabaseMetaData meta = conn.getMetaData();
        if (meta == null) {
            // If metadata is unavailable, allow callers to probe via normal query path.
            return true;
        }
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Request-scoped cached table existence check.
     */
    public static boolean tableExistsCached(Connection conn, String tableName, Map<String, Boolean> requestCache) throws SQLException {
        if (conn == null || tableName == null || tableName.isBlank()) {
            return false;
        }
        if (requestCache == null) {
            requestCache = new LinkedHashMap<>();
        }

        Boolean cachedExists = requestCache.get(tableName);
        if (cachedExists != null) {
            return cachedExists.booleanValue();
        }

        boolean exists = tableExists(conn, tableName);

        requestCache.put(tableName, Boolean.valueOf(exists));
        return exists;
    }

    public static Map<String, Boolean> newRequestTableCache() {
        return new LinkedHashMap<>();
    }
}
