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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;

public final class DashboardTrendsQueryService {

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
            throw new IllegalStateException("Unable to load trend data", ex);
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

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE DATE(created_at) = ? ORDER BY created_at DESC";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, java.sql.Date.valueOf(targetDate));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String responseText = rs.getString("response_text");
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = rs.getString("session_id");

                            snapshots.add(new TermChatSnapshot(
                                    "Entry Trends",
                                    widgetId,
                                    chatId == null ? "" : chatId,
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
            throw new IllegalStateException("Unable to collect chats for day", ex);
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
            log.log(Level.WARNING, "Unable to validate widget table " + tableName, ex);
            return;
        }

        Map<LocalDate, Integer> series = zeroSeries(totalDaily);
        String sql = "SELECT created_at FROM " + quoteIdentifier(tableName)
                + " WHERE created_at >= ? AND created_at < ?";

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "created_at");
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
            log.log(Level.WARNING, "Unable to collect trend data for widget " + widgetId, ex);
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
        Integer current = counts.get(date);
        int value = current == null ? 0 : current.intValue();
        counts.put(date, Integer.valueOf(value + 1));
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        String normalized = identifier.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        if (!normalized.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + normalized.replace("\"", "\"\"") + '"';
    }

    public static final class TrendResult {
        public final Map<LocalDate, Integer> totalDaily;
        public final Map<String, Map<LocalDate, Integer>> widgetDaily;
        public final Map<String, String> widgetNameToId;

        private TrendResult(
                Map<LocalDate, Integer> totalDaily,
                Map<String, Map<LocalDate, Integer>> widgetDaily,
                Map<String, String> widgetNameToId
        ) {
            this.totalDaily = totalDaily;
            this.widgetDaily = widgetDaily;
            this.widgetNameToId = widgetNameToId;
        }
    }
}