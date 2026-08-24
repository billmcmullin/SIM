package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.spi.CDI;

public final class DashboardSessionAggregationQueryService {

    private final Logger log;

    public DashboardSessionAggregationQueryService(Logger log) {
        this.log = log;
    }

    public Map<String, SessionAccumulatorData> collectAccumulators(List<WidgetEntry> widgets, String sessionIdFilter) {
        Map<String, SessionAccumulatorData> accumulators = new LinkedHashMap<>();
        if (widgets == null || widgets.isEmpty()) {
            return accumulators;
        }

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                StringBuilder sql = new StringBuilder("SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM ")
                        .append(quoteIdentifier(tableName))
                        .append(" WHERE session_id IS NOT NULL");

                boolean hasFilter = sessionIdFilter != null && !sessionIdFilter.isBlank();
                if (hasFilter) {
                    sql.append(" AND session_id ILIKE ?");
                }
                sql.append(" GROUP BY session_id");

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    if (hasFilter) {
                        String trimmedFilter = sessionIdFilter.trim();
                        ps.setString(1, "%" + trimmedFilter + "%");
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String sessionId = rs.getString("session_id");
                            if (sessionId == null || sessionId.isBlank()) {
                                continue;
                            }

                            sessionId = sessionId.trim();
                            SessionAccumulatorData acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulatorData());
                            int total = rs.getInt("total");
                            acc.count += total;

                            Integer existingCount = acc.widgetCounts.get(widgetId);
                            int mergedCount = total;
                            if (existingCount != null) {
                                mergedCount += existingCount.intValue();
                            }
                            acc.widgetCounts.put(widgetId, Integer.valueOf(mergedCount));

                            Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                            if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                                acc.lastEntry = lastEntry;
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to aggregate sessions for table " + tableName, ex);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to collect session accumulators", ex);
        }

        return accumulators;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank() || !identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    public static final class SessionAccumulatorData {
        public int count = 0;
        public Timestamp lastEntry = null;
        public final Map<String, Integer> widgetCounts = new LinkedHashMap<>();
    }
}