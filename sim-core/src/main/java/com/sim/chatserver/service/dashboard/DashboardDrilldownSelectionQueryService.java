package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;

public final class DashboardDrilldownSelectionQueryService {

    private static final String EMPTY = "";
        private static final String LABEL_LATEST_CHATS = "Latest Chats";
        private static final String SQL_SELECT_WIDGET_ROWS = SqlLiteralConstants.SQL_SELECT_WIDGET_CHAT_PROMPT_RESPONSE_CREATED_SESSION_FROM;
        private static final String SQL_SELECT_WIDGET_ROWS_NO_SESSION =
            "SELECT widget_chat_id, prompt, response_text, created_at FROM ";
        private static final String SQL_WHERE_SESSION_EQ_ORDER_BY_CREATED_DESC =
            " WHERE session_id = ? ORDER BY created_at DESC";
        private static final String SQL_ORDER_BY_CREATED_DESC_LIMIT = " ORDER BY created_at DESC LIMIT ?";
        private static final String SQL_READ_DATE_RANGE_ORDER_BY_CREATED_DESC =
            SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE + SqlLiteralConstants.SQL_ORDER_BY_CREATED_AT_DESC;
        private static final String MSG_UNABLE_TO_READ_DATE_ENTRIES = "Unable to read date entries from table ";
        private static final String MSG_UNABLE_TO_COLLECT_DATE_ENTRIES = "Unable to collect date entries";
        private static final String MSG_UNABLE_TO_COLLECT_LATEST_CHATS = "Unable to collect latest chats";
        private static final String MSG_QUERY_FAILED_FOR_WIDGET_TABLE = "Query failed for widget table ";
        private static final String MSG_UNABLE_TO_COLLECT_SESSION_ENTRIES = "Unable to collect session entries for review";
        private static final String MSG_UNABLE_TO_LIST_WIDGETS = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS;
        private static final String LOG_UNABLE_TO_READ_DATE_ENTRIES_PREFIX = "Unable to read date entries from table ";
        private static final String MESSAGE_SEPARATOR = ": ";

    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(CommonLiteralConstants.SQL_IDENTIFIER_REGEX);

    private final Logger log;

    public DashboardDrilldownSelectionQueryService(Logger log) {
        this.log = log;
    }

    public List<TermChatSnapshot> collectDateEntries(LocalDate date) {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }

        Timestamp startTs = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            forEachExistingWidgetTable(conn, widgets, tableExistsCache, (widgetId, tableName) -> {
                String sql = SQL_SELECT_WIDGET_ROWS
                        + quoteIdentifier(tableName)
                    + SQL_READ_DATE_RANGE_ORDER_BY_CREATED_DESC;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, startTs);
                    ps.setTimestamp(2, endTs);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString(SqlLiteralConstants.COL_WIDGET_CHAT_ID);
                            String prompt = rs.getString(SqlLiteralConstants.COL_PROMPT);
                            String response = rs.getString(SqlLiteralConstants.COL_RESPONSE_TEXT);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                            String sessionId = rs.getString(SqlLiteralConstants.COL_SESSION_ID);

                            snapshots.add(new TermChatSnapshot(
                                    date.toString(),
                                    widgetId,
                                    chatId == null ? EMPTY : chatId,
                                    prompt == null ? EMPTY : prompt,
                                    response == null ? EMPTY : response,
                                    createdAt,
                                    sessionId == null ? EMPTY : sessionId
                            ));
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, LOG_UNABLE_TO_READ_DATE_ENTRIES_PREFIX + tableName, ex);
                }
            });
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_UNABLE_TO_COLLECT_DATE_ENTRIES, ex);
        }

        return snapshots;
    }

    public List<TermChatSnapshot> collectLatestChats(int limit) {
        List<TermChatSnapshot> all = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            forEachExistingWidgetTable(conn, widgets, tableExistsCache, (widgetId, tableName) -> {
                String sql = SQL_SELECT_WIDGET_ROWS
                        + quoteIdentifier(tableName)
                    + SQL_ORDER_BY_CREATED_DESC_LIMIT;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Math.max(limit, 1));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString(SqlLiteralConstants.COL_WIDGET_CHAT_ID);
                            String prompt = rs.getString(SqlLiteralConstants.COL_PROMPT);
                            String response = rs.getString(SqlLiteralConstants.COL_RESPONSE_TEXT);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                            String sessionId = rs.getString(SqlLiteralConstants.COL_SESSION_ID);

                            all.add(new TermChatSnapshot(
                                LABEL_LATEST_CHATS,
                                    widgetId,
                                    chatId == null ? EMPTY : chatId,
                                    prompt,
                                    response,
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                }
            });
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_UNABLE_TO_COLLECT_LATEST_CHATS, ex);
        }

        all.sort(Comparator.comparing(
                (TermChatSnapshot s) -> s.getCreatedAt() == null ? new Timestamp(0) : s.getCreatedAt()
        ).reversed());

        if (all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    public List<TermChatSnapshot> collectSessionEntries(String sessionId) {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            forEachExistingWidgetTable(conn, widgets, tableExistsCache, (widgetId, tableName) -> {
                String sql = SQL_SELECT_WIDGET_ROWS_NO_SESSION
                        + quoteIdentifier(tableName)
                        + SQL_WHERE_SESSION_EQ_ORDER_BY_CREATED_DESC;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString(SqlLiteralConstants.COL_WIDGET_CHAT_ID);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                            snapshots.add(new TermChatSnapshot(
                                    sessionId,
                                    widgetId,
                                    chatId == null ? EMPTY : chatId,
                                    rs.getString(SqlLiteralConstants.COL_PROMPT),
                                    rs.getString(SqlLiteralConstants.COL_RESPONSE_TEXT),
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.WARNING, MSG_QUERY_FAILED_FOR_WIDGET_TABLE + tableName + MESSAGE_SEPARATOR + ex.getMessage(), ex);
                }
            });
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_UNABLE_TO_COLLECT_SESSION_ENTRIES, ex);
        }
        return snapshots;
    }

    @FunctionalInterface
    private interface WidgetTableOperation {
        void apply(String widgetId, String tableName) throws SQLException;
    }

    private void forEachExistingWidgetTable(
            Connection conn,
            List<WidgetEntry> widgets,
            Map<String, Boolean> tableExistsCache,
            WidgetTableOperation operation
    ) throws SQLException {
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }
            operation.apply(widgetId, tableName);
        }
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_UNABLE_TO_LIST_WIDGETS, ex);
            return List.of();
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(SqlLiteralConstants.SQL_INVALID_IDENTIFIER);
        }
        return '"' + identifier.replace(SqlLiteralConstants.DOUBLE_QUOTE, SqlLiteralConstants.DOUBLE_DOUBLE_QUOTE) + '"';
    }
}