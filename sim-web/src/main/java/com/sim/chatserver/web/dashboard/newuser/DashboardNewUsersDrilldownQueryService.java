package com.sim.chatserver.web.dashboard.newuser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

final class DashboardNewUsersDrilldownQueryService {

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    DashboardNewUsersDrilldownQueryService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    Map<String, Timestamp> findEarliestBySession(List<WidgetEntry> widgets) {
        Map<String, Timestamp> earliest = new LinkedHashMap<>();
        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }
                String table = sanitizeWidgetTableName(widget.getWidgetId());
                if (!tableExists(conn, table)) {
                    continue;
                }

                String sql = "SELECT session_id, MIN(created_at) AS first_seen FROM " + quoteIdentifier(table)
                        + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to prepare earliest-session query for table " + table, ex);
                    continue;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, "Unable to execute earliest-session query for table " + table, ex);
                        continue;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = rs.getString("session_id");
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "first_seen");
                            if (sid == null || sid.isBlank() || ts == null) {
                                continue;
                            }
                            sid = sid.trim();

                            Timestamp existing = earliest.get(sid);
                            if (existing == null || ts.before(existing)) {
                                earliest.put(sid, ts);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to query earliest sessions for table " + table, ex);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to load earliest sessions for new users drilldown", ex);
        }
        return earliest;
    }

    Map<String, Integer> findTotalChatsBySession(List<WidgetEntry> widgets) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }
                String table = sanitizeWidgetTableName(widget.getWidgetId());
                if (!tableExists(conn, table)) {
                    continue;
                }

                String sql = "SELECT session_id, COUNT(*) AS c FROM " + quoteIdentifier(table)
                        + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to prepare total-chats query for table " + table, ex);
                    continue;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, "Unable to execute total-chats query for table " + table, ex);
                        continue;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = rs.getString("session_id");
                            if (sid == null || sid.isBlank()) {
                                continue;
                            }
                            sid = sid.trim();
                            int count = rs.getInt("c");
                            int existingCount = safeInt(totals.get(sid));
                            totals.put(sid, Integer.valueOf(existingCount + count));
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to query total chats for table " + table, ex);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to load total chats for new users drilldown", ex);
        }
        return totals;
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, ex);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
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

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value.intValue();
    }
}
