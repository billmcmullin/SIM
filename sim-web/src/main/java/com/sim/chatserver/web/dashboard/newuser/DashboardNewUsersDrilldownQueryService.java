package com.sim.chatserver.web.dashboard.newuser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

final class DashboardNewUsersDrilldownQueryService {

    private static final String SQL_EARLIEST_BY_SESSION_PREFIX = "SELECT session_id, MIN(created_at) AS first_seen FROM ";
    private static final String SQL_SESSION_GROUP_BY = com.sim.chatserver.util.CommonLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;
    private static final String MSG_PREPARE_EARLIEST_QUERY_FAILED_PREFIX = "Unable to prepare earliest-session query for table ";
    private static final String MSG_EXECUTE_EARLIEST_QUERY_FAILED_PREFIX = "Unable to execute earliest-session query for table ";
    private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String COL_FIRST_SEEN = "first_seen";
    private static final String MSG_QUERY_EARLIEST_SESSIONS_FAILED_PREFIX = "Unable to query earliest sessions for table ";
    private static final String MSG_LOAD_EARLIEST_SESSIONS_FAILED = "Failed to load earliest sessions for new users drilldown";
    private static final String SQL_TOTAL_BY_SESSION_PREFIX = "SELECT session_id, COUNT(*) AS c FROM ";
    private static final String MSG_PREPARE_TOTAL_CHATS_QUERY_FAILED_PREFIX = "Unable to prepare total-chats query for table ";
    private static final String MSG_EXECUTE_TOTAL_CHATS_QUERY_FAILED_PREFIX = "Unable to execute total-chats query for table ";
    private static final String COL_CHAT_COUNT = "c";
    private static final String MSG_QUERY_TOTAL_CHATS_FAILED_PREFIX = "Unable to query total chats for table ";
    private static final String MSG_LOAD_TOTAL_CHATS_FAILED = "Failed to load total chats for new users drilldown";
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    DashboardNewUsersDrilldownQueryService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    Map<String, Timestamp> findEarliestBySession(List<WidgetEntry> widgets) {
        Map<String, Timestamp> earliest = new LinkedHashMap<>();
        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            forEachWidgetTable(conn, widgets, (widgetId, table) -> {
                String sql = SQL_EARLIEST_BY_SESSION_PREFIX + quoteIdentifier(table)
                        + SQL_SESSION_GROUP_BY;

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_PREPARE_EARLIEST_QUERY_FAILED_PREFIX + table, ex);
                    return;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, MSG_EXECUTE_EARLIEST_QUERY_FAILED_PREFIX + table, ex);
                        return;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = rs.getString(COL_SESSION_ID);
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, COL_FIRST_SEEN);
                            if (sid == null || sid.isBlank() || ts == null) continue;
                            sid = sid.trim();

                            Timestamp existing = earliest.get(sid);
                            if (existing == null || ts.before(existing)) {
                                earliest.put(sid, ts);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_QUERY_EARLIEST_SESSIONS_FAILED_PREFIX + table, ex);
                }
            });
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_LOAD_EARLIEST_SESSIONS_FAILED, ex);
        }
        return earliest;
    }

    Map<String, Integer> findTotalChatsBySession(List<WidgetEntry> widgets) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            forEachWidgetTable(conn, widgets, (widgetId, table) -> {
                String sql = SQL_TOTAL_BY_SESSION_PREFIX + quoteIdentifier(table)
                        + SQL_SESSION_GROUP_BY;

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_PREPARE_TOTAL_CHATS_QUERY_FAILED_PREFIX + table, ex);
                    return;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, MSG_EXECUTE_TOTAL_CHATS_QUERY_FAILED_PREFIX + table, ex);
                        return;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = rs.getString(COL_SESSION_ID);
                            if (sid == null || sid.isBlank()) {
                                continue;
                            }
                            sid = sid.trim();
                            int count = rs.getInt(COL_CHAT_COUNT);
                            int existingCount = safeInt(totals.get(sid));
                            totals.put(sid, Integer.valueOf(existingCount + count));
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_QUERY_TOTAL_CHATS_FAILED_PREFIX + table, ex);
                }
            });
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_LOAD_TOTAL_CHATS_FAILED, ex);
        }
        return totals;
    }

    private void forEachWidgetTable(
            Connection conn,
            List<WidgetEntry> widgets,
            BiConsumer<String, String> consumer) {
        if (widgets == null || consumer == null) {
            return;
        }

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String table = sanitizeWidgetTableName(widgetId);
            if (!com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil.tableExists(conn, table, log)) {
                continue;
            }
            consumer.accept(widgetId, table);
        }
    }

    private String sanitizeWidgetTableName(String widgetId) {
        return DashboardDbUtil.sanitizeWidgetTableName(widgetId);
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }

    private int safeInt(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }
}

