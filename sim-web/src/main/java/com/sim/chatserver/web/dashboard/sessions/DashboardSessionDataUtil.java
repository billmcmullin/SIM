package com.sim.chatserver.web.dashboard.sessions;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.widget.WidgetEntry;

public final class DashboardSessionDataUtil {

    private DashboardSessionDataUtil() {
    }

    public static Map<String, String> mapWidgetDisplayNames(List<WidgetEntry> widgets) {
        Map<String, String> map = new LinkedHashMap<>();
        if (widgets == null) {
            return map;
        }
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String displayName = widget.getDisplayName();
            if (displayName == null || displayName.isBlank()) {
                displayName = widget.getWidgetId();
            }
            map.put(widget.getWidgetId(), displayName);
        }
        return map;
    }

    public static String pickTopWidgetName(Map<String, Integer> widgetCounts, Map<String, String> displayNames) {
        String winner = null;
        int best = -1;
        for (Map.Entry<String, Integer> entry : widgetCounts.entrySet()) {
            Integer count = entry.getValue();
            int value = count == null ? 0 : count.intValue();
            if (value > best) {
                best = value;
                winner = entry.getKey();
            }
        }
        if (winner == null) {
            return "-";
        }
        return displayNames.getOrDefault(winner, winner);
    }

    public static String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    public static boolean tableExists(Connection conn, String tableName, Logger log) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            if (log != null) {
                log.log(Level.FINE, "Unable to inspect widget table metadata", ex);
            }
        }
        return false;
    }
}