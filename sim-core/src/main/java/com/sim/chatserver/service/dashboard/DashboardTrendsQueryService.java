package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;

public final class DashboardTrendsQueryService {

    private static final String MSG_LOAD_TREND_DATA_FAILED = "Unable to load trend data";
    private static final String SQL_SELECT_SNAPSHOTS_PREFIX = CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
    private static final String SQL_SELECT_SNAPSHOTS_SUFFIX = " WHERE DATE(created_at) = ? ORDER BY created_at DESC";
    private static final String COL_WIDGET_CHAT_ID = CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String COL_PROMPT = SqlLiteralConstants.COL_PROMPT;
    private static final String COL_RESPONSE_TEXT = CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String COL_CREATED_AT = SqlLiteralConstants.COL_CREATED_AT;
    private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String TERM_ENTRY_TRENDS = "Entry Trends";
    private static final String EMPTY = "";
    private static final String MSG_COLLECT_CHATS_FOR_DAY_FAILED = "Unable to collect chats for day";
    private static final String MSG_VALIDATE_WIDGET_TABLE_FAILED_PREFIX = "Unable to validate widget table ";
    private static final String SQL_SELECT_CREATED_AT_PREFIX = "SELECT created_at FROM ";
    private static final String SQL_SELECT_CREATED_AT_SUFFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    private static final String MSG_COLLECT_TREND_DATA_FAILED_PREFIX = "Unable to collect trend data for widget ";
    private static final String DEFAULT_ZERO_TEXT = "0";
    private static final String MSG_PARSE_TREND_COUNT_FALLBACK = "Unable to parse trend count; using zero fallback";
    private static final String MSG_INVALID_SQL_IDENTIFIER = SqlLiteralConstants.SQL_INVALID_IDENTIFIER;
    private static final String IDENTIFIER_REGEX = CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = SqlLiteralConstants.DOUBLE_DOUBLE_QUOTE;

    private final Logger log;

    public DashboardTrendsQueryService(Logger log) {
        this.log = log;
    }

    public TrendResult loadTrendData(LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> totalDaily = new LinkedHashMap<>();
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        for (int i = 0; i < days; i++) {
            totalDaily.put(start.plusDays(i), Integer.valueOf(0));
        }

        Map<String, Map<LocalDate, Integer>> widgetDaily = new LinkedHashMap<>();
        Map<String, String> widgetNameToId = new LinkedHashMap<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            for (WidgetEntry widget : widgets) {
                collectWidgetTrend(conn, widget, start, end, totalDaily, widgetDaily, widgetNameToId, tableExistsCache);
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
            throw new IllegalStateException(MSG_LOAD_TREND_DATA_FAILED, ex);
        }

        return new TrendResult(totalDaily, widgetDaily, widgetNameToId);
    }

    public List<TermChatSnapshot> collectSnapshotsForDay(LocalDate targetDate, String widgetIdFilter) {
        java.util.List<TermChatSnapshot> snapshots = new java.util.ArrayList<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                if (widgetIdFilter != null && !widgetIdFilter.isBlank() && !widgetIdFilter.equals(widgetId)) {
                    continue;
                }

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = SQL_SELECT_SNAPSHOTS_PREFIX
                        + quoteIdentifier(tableName)
                    + SQL_SELECT_SNAPSHOTS_SUFFIX;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, java.sql.Date.valueOf(targetDate));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString(COL_WIDGET_CHAT_ID);
                            String prompt = rs.getString(COL_PROMPT);
                            String responseText = rs.getString(COL_RESPONSE_TEXT);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                            String sessionId = rs.getString(COL_SESSION_ID);

                            snapshots.add(new TermChatSnapshot(
                                    TERM_ENTRY_TRENDS,
                                    widgetId,
                                    chatId == null ? EMPTY : chatId,
                                    prompt,
                                    responseText,
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException(MSG_COLLECT_CHATS_FOR_DAY_FAILED, ex);
        }

        return snapshots;
    }

    private void collectWidgetTrend(
            Connection conn,
            WidgetEntry widget,
            LocalDate start,
            LocalDate end,
            Map<LocalDate, Integer> totalDaily,
            Map<String, Map<LocalDate, Integer>> widgetDaily,
            Map<String, String> widgetNameToId,
            Map<String, Boolean> tableExistsCache
    ) {
        if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
            return;
        }

        String widgetId = widget.getWidgetId();
        String widgetName = widget.getDisplayName() == null || widget.getDisplayName().isBlank()
                ? widgetId : widget.getDisplayName();
        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        try {
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                return;
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_VALIDATE_WIDGET_TABLE_FAILED_PREFIX + tableName, ex);
            return;
        }

        Map<LocalDate, Integer> series = zeroSeries(totalDaily);
        String sql = SQL_SELECT_CREATED_AT_PREFIX + quoteIdentifier(tableName)
                + SQL_SELECT_CREATED_AT_SUFFIX;

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                    if (ts == null) {
                        continue;
                    }

                    LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (totalDaily.containsKey(entryDate)) {
                        incrementDate(totalDaily, entryDate);
                        incrementDate(series, entryDate);
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, MSG_COLLECT_TREND_DATA_FAILED_PREFIX + widgetId, ex);
        }

        widgetDaily.put(widgetName, series);
        widgetNameToId.put(widgetName, widgetId);
    }

    private Map<LocalDate, Integer> zeroSeries(Map<LocalDate, Integer> totalDaily) {
        Map<LocalDate, Integer> series = new LinkedHashMap<>();
        for (LocalDate day : totalDaily.keySet()) {
            series.put(day, Integer.valueOf(0));
        }
        return series;
    }

    private void incrementDate(Map<LocalDate, Integer> counts, LocalDate date) {
        String valueText = Objects.toString(counts.get(date), DEFAULT_ZERO_TEXT);
        int value;
        try {
            value = Integer.parseInt(valueText);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_PARSE_TREND_COUNT_FALLBACK, ex);
            value = 0;
        }
        counts.put(date, Integer.valueOf(value + 1));
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        String normalized = identifier.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        if (!normalized.matches(IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + normalized.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }
}


