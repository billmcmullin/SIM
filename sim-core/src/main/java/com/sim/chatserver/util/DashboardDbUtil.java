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

    private static final String DEFAULT_WIDGET_TABLE_NAME = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
    private static final String WIDGET_TABLE_PREFIX = "w_";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String DB_TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;

    private DashboardDbUtil() {
    }

    public static String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return DEFAULT_WIDGET_TABLE_NAME;
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

        String normalized = sb.length() == 0 ? DEFAULT_WIDGET_TABLE_NAME : sb.toString();
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = WIDGET_TABLE_PREFIX + normalized;
        }
        if (normalized.length() > 60) {
            normalized = new String(normalized.toCharArray(), 0, 60);
        }
        return normalized;
    }

    public static String quoteIdentifier(String identifier) {
        if (identifier == null) {
            return DOUBLE_DOUBLE_QUOTE;
        }
        return DOUBLE_QUOTE + identifier.replace(DOUBLE_QUOTE, DOUBLE_DOUBLE_QUOTE) + DOUBLE_QUOTE;
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
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{DB_TABLE_TYPE})) {
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
            return Boolean.TRUE.equals(cachedExists);
        }

        boolean exists = tableExists(conn, tableName);

        requestCache.put(tableName, exists ? Boolean.TRUE : Boolean.FALSE);
        return exists;
    }

    public static Map<String, Boolean> newRequestTableCache() {
        return new LinkedHashMap<>();
    }
}
