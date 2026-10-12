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

import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.widget.WidgetEntry;

public final class DashboardSessionDataUtil {

    private static final String VALUE_DASH = "-";
    private static final String LOG_WIDGET_TABLE_METADATA_UNAVAILABLE = "Unable to inspect widget table metadata";

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
            Object count = entry.getValue();
            int value = 0;
            if (count != null) {
                value = Integer.parseInt(count.toString());
            }
            if (value > best) {
                best = value;
                winner = entry.getKey();
            }
        }
        if (winner == null) {
            return VALUE_DASH;
        }
        return displayNames.getOrDefault(winner, winner);
    }

    public static String sanitizeWidgetTableName(String widgetId) {
        return DashboardDbUtil.sanitizeWidgetTableName(widgetId);
    }

    public static boolean tableExists(Connection conn, String tableName, Logger log) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            if (log != null) {
                log.log(Level.FINE, LOG_WIDGET_TABLE_METADATA_UNAVAILABLE, ex);
            }
        }
        return false;
    }
}
