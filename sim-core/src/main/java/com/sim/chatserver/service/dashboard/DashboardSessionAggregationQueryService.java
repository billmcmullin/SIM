package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.spi.CDI;

public final class DashboardSessionAggregationQueryService {

    private static final String SQL_SESSION_AGG_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_SESSION_COUNTS_PREFIX;
    private static final String SQL_SESSION_NOT_NULL = " WHERE session_id IS NOT NULL";
    private static final String SQL_SESSION_FILTER = " AND session_id ILIKE ?";
    private static final String SQL_SESSION_GROUP_BY = " GROUP BY session_id";
    private static final String COL_SESSION_ID = SqlLiteralConstants.COL_SESSION_ID;
    private static final String COL_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
    private static final String DEFAULT_ZERO_TEXT = "0";
    private static final String MSG_PARSE_EXISTING_WIDGET_COUNT = "Unable to parse existing widget count; using zero fallback";
    private static final String COL_LAST_ENTRY = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_LAST_ENTRY;
    private static final String MSG_AGGREGATE_TABLE_FAILED_PREFIX = "Unable to aggregate sessions for table ";
    private static final String MSG_COLLECT_ACCUMULATORS_FAILED = "Unable to collect session accumulators";
    private static final String IDENTIFIER_REGEX = CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String MSG_INVALID_SQL_IDENTIFIER = SqlLiteralConstants.SQL_INVALID_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = SqlLiteralConstants.DOUBLE_DOUBLE_QUOTE;

    private final Logger log;

    public DashboardSessionAggregationQueryService(Logger log) {
        this.log = log;
    }

    public Map<String, SessionAccumulatorData> collectAccumulators(List<WidgetEntry> widgets, String sessionIdFilter) {
        Map<String, SessionAccumulatorData> accumulators = new LinkedHashMap<>();
        if (widgets == null || widgets.isEmpty()) {
            return accumulators;
        }

        boolean hasFilter = sessionIdFilter != null && !sessionIdFilter.isBlank();
        String likePattern = null;
        if (hasFilter) {
            likePattern = '%' + sessionIdFilter.trim() + '%';
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

                StringBuilder sql = new StringBuilder(SQL_SESSION_AGG_PREFIX)
                        .append(quoteIdentifier(tableName))
                        .append(SQL_SESSION_NOT_NULL);

                if (hasFilter) {
                    sql.append(SQL_SESSION_FILTER);
                }
                sql.append(SQL_SESSION_GROUP_BY);

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    if (hasFilter) {
                        ps.setString(1, likePattern);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String sessionId = rs.getString(COL_SESSION_ID);
                            if (sessionId == null || sessionId.isBlank()) {
                                continue;
                            }

                            sessionId = sessionId.trim();
                            SessionAccumulatorData acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulatorData());
                            int total = rs.getInt(COL_TOTAL);
                            acc.count += total;

                            String existingCountText = Objects.toString(acc.widgetCounts.get(widgetId), DEFAULT_ZERO_TEXT);
                            int existingCount;
                            try {
                                existingCount = Integer.parseInt(existingCountText);
                            } catch (NumberFormatException ex) {
                                log.log(Level.FINE, MSG_PARSE_EXISTING_WIDGET_COUNT, ex);
                                existingCount = 0;
                            }
                            acc.widgetCounts.put(widgetId, Integer.valueOf(total + existingCount));

                            Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, COL_LAST_ENTRY);
                            if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                                acc.lastEntry = lastEntry;
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_AGGREGATE_TABLE_FAILED_PREFIX + tableName, ex);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_COLLECT_ACCUMULATORS_FAILED, ex);
        }

        return accumulators;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank() || !identifier.matches(IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }
}

