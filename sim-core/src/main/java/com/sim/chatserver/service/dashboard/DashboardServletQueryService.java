package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.SessionOverview;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

public final class DashboardServletQueryService {

    private static final String MSG_COMPUTE_TERM_SUMMARY_FAILED = "Unable to compute term summary";
    private static final String MSG_COMPUTE_SESSION_OVERVIEW_FAILED = "Unable to compute session overview";
    private static final String SQL_SELECT_CREATED_AT_PREFIX = "SELECT created_at FROM ";
    private static final String SQL_SELECT_CREATED_AT_SUFFIX = CommonLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    private static final String COL_CREATED_AT = CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String DEFAULT_ZERO_TEXT = "0";
    private static final String MSG_PARSE_EXISTING_DAILY_COUNT = "Unable to parse existing daily count; using zero fallback";
    private static final String MSG_LOAD_5_DAY_TREND_FAILED = "Unable to load 5-day trend data";
    private static final String MSG_PARSE_DAILY_CHART_COUNT = "Unable to parse daily chart count; using zero fallback";
    private static final String JSON_LABELS = CommonLiteralConstants.FIELD_LABELS;
    private static final String JSON_VALUES = CommonLiteralConstants.FIELD_VALUES;
    private static final String JSON_DAYS = CommonLiteralConstants.FIELD_DAYS;
    private static final String MSG_OPEN_DASHBOARD_DATA_CONN_FAILED = "Unable to open dashboard data connection";
    private static final String MSG_INVALID_SQL_IDENTIFIER = CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String IDENTIFIER_REGEX = CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;

    private final Logger log;

    private DashboardServletQueryService(Logger log) {
        this.log = log;
    }

    static DashboardServletQueryService create(Logger log) {
        return new DashboardServletQueryService(log);
    }

    com.sim.chatserver.model.TermSummary loadTermSummary(
            DashboardTermService termService,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        try (Connection conn = openConnectionSafe()) {
            var terms = termService.loadAllTerms();
            return termService.buildTermSummary(conn, widgets, terms, rangeStart, rangeEnd);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_COMPUTE_TERM_SUMMARY_FAILED, ex);
            return null;
        }
    }

    SessionOverview loadSessionOverview(
            DashboardSessionService sessionService,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int activeDays
    ) {
        try (Connection conn = openConnectionSafe()) {
            return sessionService.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_COMPUTE_SESSION_OVERVIEW_FAILED, ex);
            return null;
        }
    }

    String buildLastFiveDaysTrendJson(List<WidgetEntry> widgets) {
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(4);

        Map<LocalDate, Integer> totalDaily = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            totalDaily.put(start.plusDays(i), Integer.valueOf(0));
        }

        try (Connection conn = openConnectionSafe()) {
            List<WidgetEntry> sourceWidgets = widgets == null ? List.of() : widgets;

            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            for (WidgetEntry widget : sourceWidgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = SQL_SELECT_CREATED_AT_PREFIX + quoteIdentifier(tableName)
                    + SQL_SELECT_CREATED_AT_SUFFIX;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                            if (ts == null) {
                                continue;
                            }

                            LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!totalDaily.containsKey(entryDate)) {
                                continue;
                            }

                            String existingText = Objects.toString(totalDaily.get(entryDate), DEFAULT_ZERO_TEXT);
                            int currentCount;
                            try {
                                currentCount = Integer.parseInt(existingText);
                            } catch (NumberFormatException ex) {
                                log.log(Level.FINE, MSG_PARSE_EXISTING_DAILY_COUNT, ex);
                                currentCount = 0;
                            }
                            totalDaily.put(entryDate, Integer.valueOf(currentCount + 1));
                        }
                    }
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_LOAD_5_DAY_TREND_FAILED, ex);
        }

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            String dayCountText = Objects.toString(entry.getValue(), DEFAULT_ZERO_TEXT);
            int safeCount;
            try {
                safeCount = Integer.parseInt(dayCountText);
            } catch (NumberFormatException ex) {
                log.log(Level.FINE, MSG_PARSE_DAILY_CHART_COUNT, ex);
                safeCount = 0;
            }
            values.add(safeCount);
        }

        return Json.createObjectBuilder()
                .add(JSON_LABELS, labels)
                .add(JSON_VALUES, values)
                .add(JSON_DAYS, 5)
                .build()
                .toString();
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_OPEN_DASHBOARD_DATA_CONN_FAILED, ex);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        if (!identifier.matches(IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }
}

