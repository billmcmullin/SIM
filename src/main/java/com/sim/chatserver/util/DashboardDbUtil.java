package com.sim.chatserver.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sim.chatserver.model.DashboardViewModels.CacheValue;

/**
 * Shared DB helpers for dashboard-related code.
 */
public final class DashboardDbUtil {

    private static final long TABLE_EXISTS_TTL_MILLIS = 5 * 60 * 1000L; // 5 min
    private static final int TABLE_EXISTS_CACHE_MAX = 512;

    private static final Object TABLE_CACHE_LOCK = new Object();
    private static final Map<String, CacheValue<Boolean>> GLOBAL_TABLE_EXISTS_CACHE = new LinkedHashMap<>();

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
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    public static String quoteIdentifier(String identifier) {
        if (identifier == null) {
            return "\"\"";
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    public static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
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
     * Request-scoped + global cached table existence check.
     */
    public static boolean tableExistsCached(Connection conn, String tableName, Map<String, Boolean> requestCache) throws SQLException {
        if (requestCache == null) {
            requestCache = new LinkedHashMap<>();
        }

        Boolean req = requestCache.get(tableName);
        if (req != null) {
            return req;
        }

        long now = System.currentTimeMillis();
        String catalog = conn.getCatalog();
        String key = (catalog == null ? "" : catalog) + "|" + tableName;

        CacheValue<Boolean> global;
        synchronized (TABLE_CACHE_LOCK) {
            global = GLOBAL_TABLE_EXISTS_CACHE.get(key);
            if (global != null && !global.isExpired(now)) {
                requestCache.put(tableName, global.getValue());
                return global.getValue();
            }
        }

        boolean exists = tableExists(conn, tableName);

        synchronized (TABLE_CACHE_LOCK) {
            GLOBAL_TABLE_EXISTS_CACHE.put(key, new CacheValue<>(exists, now + TABLE_EXISTS_TTL_MILLIS));
            if (GLOBAL_TABLE_EXISTS_CACHE.size() > TABLE_EXISTS_CACHE_MAX) {
                String oldest = GLOBAL_TABLE_EXISTS_CACHE.keySet().iterator().next();
                GLOBAL_TABLE_EXISTS_CACHE.remove(oldest);
            }
        }

        requestCache.put(tableName, exists);
        return exists;
    }

    public static Map<String, Boolean> newRequestTableCache() {
        return new LinkedHashMap<>();
    }
}
