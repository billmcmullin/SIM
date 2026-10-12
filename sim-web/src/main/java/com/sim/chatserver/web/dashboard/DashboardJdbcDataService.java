package com.sim.chatserver.web.dashboard;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.model.SessionAccumulator;
import com.sim.chatserver.model.SessionStat;
import com.sim.chatserver.model.SessionTimeline;
import com.sim.chatserver.model.TermSummary;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

final class DashboardJdbcDataService {

    private static final Logger log = Logger.getLogger(DashboardJdbcDataService.class.getCanonicalName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern(com.sim.chatserver.util.CommonLiteralConstants.DATETIME_PATTERN_SECONDS);
    private static final String EMPTY = "";
    private static final String DASH = "-";
    private static final String SQL_PARAM = "?";
    private static final String COMMA_SPACE = ", ";
    private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String DB_COL_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
    private static final String DB_COL_LAST_ENTRY = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_LAST_ENTRY;
    private static final String DB_COL_FIRST_SEEN = "first_seen";
    private static final String DB_COL_DAY_VALUE = "day_value";
    private static final String DB_COL_DAY_COUNT = "day_count";
    private static final String DB_COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_KEY_COUNTS = "counts";
    private static final String JSON_KEY_LABELS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LABELS;
    private static final String JSON_KEY_SERIES = "series";
    private static final String JSON_KEY_RANGE_START = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_START;
    private static final String JSON_KEY_RANGE_END = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_END;
    private static final String JSON_KEY_VALUES = com.sim.chatserver.util.CommonLiteralConstants.FIELD_VALUES;
    private static final String JSON_KEY_DAYS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS;
    private static final String SQL_SELECT_SESSION_TOTAL_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_SESSION_COUNTS_PREFIX;
    private static final String SQL_SELECT_SESSION_FIRST_SEEN_PREFIX = "SELECT session_id, MIN(created_at) AS first_seen FROM ";
    private static final String SQL_WHERE_SESSION_NOT_NULL_GROUP_BY_SESSION = " WHERE session_id IS NOT NULL GROUP BY session_id";
    private static final String SQL_SELECT_TIMELINE_PREFIX = "SELECT session_id, CAST(created_at AS DATE) AS day_value, COUNT(*) AS day_count FROM ";
    private static final String SQL_WHERE_SESSION_IN = " WHERE session_id IN (";
    private static final String SQL_CREATED_AT_RANGE = ") AND created_at >= ? AND created_at < ?";
    private static final String SQL_GROUP_BY_TIMELINE = " GROUP BY session_id, CAST(created_at AS DATE)";
    private static final String SQL_SELECT_CREATED_AT_PREFIX = "SELECT created_at FROM ";
    private static final String SQL_WHERE_CREATED_AT_RANGE = com.sim.chatserver.util.CommonLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    private static final String SQL_IDENTIFIER_REGEX = com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String SQL_IDENTIFIER_INVALID_MSG = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String QUOTE_ESCAPED = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String MSG_UNABLE_LOAD_WIDGET_REGISTRY = "Unable to load widget registry for dashboard";
    private static final String MSG_UNABLE_LOAD_SESSION_LABELS = "Unable to load session labels";
    private static final String MSG_UNABLE_COMPUTE_TERM_SUMMARY = "Unable to compute term summary";
    private static final String MSG_UNABLE_COMPUTE_SESSION_OVERVIEW = "Unable to compute session overview";
    private static final String MSG_LOCALDATE_PARSE_FALLBACK = "LocalDate parse fallback for column ";
    private static final String MSG_INSTANT_PARSE_FALLBACK = "Instant parse fallback for column ";
    private static final String MSG_TIMESTAMP_PARSE_FALLBACK = "Timestamp parse fallback for column ";
    private static final String MSG_UNABLE_READ_DB_CHAR_STREAM = "Unable to read DB character stream for column ";
    private static final String MSG_UNABLE_DECODE_DB_CHAR_STREAM = "Unable to decode DB character stream for column ";
    private static final String MSG_UNABLE_READ_DB_BYTES = "Unable to read DB bytes for column ";
    private static final String MSG_UNABLE_READ_DB_STRING = "Unable to read DB string for column ";
    private static final String MSG_UNABLE_READ_DB_OBJECT_TEXT = "Unable to read DB object text for column ";
    private static final String MSG_UNABLE_LOAD_5_DAY_TREND = "Unable to load 5-day trend data";
    private static final String MSG_UNABLE_OPEN_DASHBOARD_CONNECTION = "Unable to open dashboard data connection";

    List<WidgetEntry> loadWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_UNABLE_LOAD_WIDGET_REGISTRY, e);
            return List.of();
        }
    }

    Map<String, SessionLabel> loadSessionLabels(List<SessionStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }

        java.util.Set<String> sessionIds = stats.stream()
                .map(SessionStat::getSessionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (sessionIds.isEmpty()) {
            return Map.of();
        }

        try {
            return SessionLabelStore.mapDisplayNames(sessionIds);
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_UNABLE_LOAD_SESSION_LABELS, e);
            return Map.of();
        }
    }

    TermSummary loadTermSummary(
            DashboardTermService termService,
            TermsStore termsStore,
            AppDataSourceHolder dataSourceHolder,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        try (Connection conn = openConnectionSafe(dataSourceHolder)) {
            List<TermDefinition> terms = termsStore.listAll();
            if (terms == null) {
                terms = List.of();
            }
            if (rangeStart != null && rangeEnd != null) {
                return termService.computeTermSummaryForDashboard(conn, widgets, terms, rangeStart, rangeEnd);
            }
            return termService.computeTermSummaryForDashboard(conn, widgets, terms);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_UNABLE_COMPUTE_TERM_SUMMARY, ex);
            return null;
        }
    }

    SessionOverview loadSessionOverview(
            AppDataSourceHolder dataSourceHolder,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int activeDays
    ) {
        try (Connection conn = openConnectionSafe(dataSourceHolder)) {
            return buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_UNABLE_COMPUTE_SESSION_OVERVIEW, ex);
            return null;
        }
    }

    private SessionOverview buildSessionOverview(
            Connection conn,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int activeDays
    ) throws SQLException {

        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        Map<String, LocalDate> firstSeenBySession = new LinkedHashMap<>();

        if (widgets != null) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = SQL_SELECT_SESSION_TOTAL_PREFIX
                        + quoteIdentifier(tableName)
                    + SQL_WHERE_SESSION_NOT_NULL_GROUP_BY_SESSION;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, DB_COL_SESSION_ID, 256);
                        if (sessionId.isBlank()) {
                            continue;
                        }

                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.addCount(rs.getInt(DB_COL_TOTAL));

                        Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, DB_COL_LAST_ENTRY);
                        if (lastEntry != null && (acc.getLastEntry() == null || lastEntry.after(acc.getLastEntry()))) {
                            acc.setLastEntry(lastEntry);
                        }
                    }
                }

                String firstSeenSql = SQL_SELECT_SESSION_FIRST_SEEN_PREFIX
                        + quoteIdentifier(tableName)
                    + SQL_WHERE_SESSION_NOT_NULL_GROUP_BY_SESSION;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(firstSeenSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, DB_COL_SESSION_ID, 256);
                        Timestamp firstSeenTs = SqlTimeUtil.safeTimestamp(rs, DB_COL_FIRST_SEEN);
                        if (sessionId.isBlank() || firstSeenTs == null) {
                            continue;
                        }

                        sessionId = sessionId.trim();
                        LocalDate firstSeenDate = firstSeenTs.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        LocalDate existing = firstSeenBySession.get(sessionId);
                        if (existing == null || firstSeenDate.isBefore(existing)) {
                            firstSeenBySession.put(sessionId, firstSeenDate);
                        }
                    }
                }
            }
        }

        int totalUsers = accumulators.size();

        Instant cutoffNow = Instant.now().minus(activeDays, ChronoUnit.DAYS);
        int inactiveUsers = 0;
        for (SessionAccumulator acc : accumulators.values()) {
            if (acc.getLastEntry() != null && acc.getLastEntry().toInstant().isBefore(cutoffNow)) {
                inactiveUsers++;
            }
        }
        int activeUsers = Math.max(0, totalUsers - inactiveUsers);

        Instant cutoffYesterday = Instant.now()
                .minus(1, ChronoUnit.DAYS)
                .minus(activeDays, ChronoUnit.DAYS);

        int inactiveUsersYesterday = 0;
        for (SessionAccumulator acc : accumulators.values()) {
            Timestamp last = acc.getLastEntry();
            if (last == null || last.toInstant().isBefore(cutoffYesterday)) {
                inactiveUsersYesterday++;
            }
        }
        int activeUsersYesterday = Math.max(0, totalUsers - inactiveUsersYesterday);

        ProgressStat activeUsersProgression =
            new ProgressStat(activeUsers, activeUsersYesterday);

        int newSessionsToday = 0;
        int newSessionsYesterday = 0;
        for (LocalDate d : firstSeenBySession.values()) {
            if (today.equals(d)) {
                newSessionsToday++;
            } else if (yesterday.equals(d)) {
                newSessionsYesterday++;
            }
        }
        ProgressStat newSessionsProgression =
            new ProgressStat(newSessionsToday, newSessionsYesterday);

        final int limit = 10;
        PriorityQueue<Map.Entry<String, SessionAccumulator>> pq
                = new PriorityQueue<>(Comparator.comparingInt(e -> e.getValue().getCount()));

        for (Map.Entry<String, SessionAccumulator> entry : accumulators.entrySet()) {
            if (pq.size() < limit) {
                pq.offer(entry);
            } else if (entry.getValue().getCount() > pq.peek().getValue().getCount()) {
                pq.poll();
                pq.offer(entry);
            }
        }

        List<Map.Entry<String, SessionAccumulator>> topEntries = new ArrayList<>(pq);
        topEntries.sort((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()));

        List<SessionStat> topSessions = new ArrayList<>(topEntries.size());
        for (Map.Entry<String, SessionAccumulator> entry : topEntries) {
            topSessions.add(new SessionStat(
                    entry.getKey(),
                    entry.getValue().getCount(),
                    formatTimestamp(entry.getValue().getLastEntry())
            ));
        }

        List<String> sessionIds = topSessions.stream()
                .map(SessionStat::getSessionId)
                .collect(Collectors.toList());

        SessionTimeline timeline = buildSessionTimeline(conn, widgets, sessionIds, rangeStart, rangeEnd, tableExistsCache);

        return new SessionOverview(
                topSessions,
                timeline,
                totalUsers,
                activeUsers,
                inactiveUsers,
                activeDays,
                newSessionsToday,
                newSessionsYesterday,
                newSessionsProgression,
                activeUsersYesterday,
                activeUsersProgression
        );
    }

    private SessionTimeline buildSessionTimeline(
            Connection conn,
            List<WidgetEntry> widgets,
            List<String> sessionIds,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            Map<String, Boolean> tableExistsCache
    ) throws SQLException {

        List<LocalDate> labelDates = new ArrayList<>();
        LocalDate cursor = rangeStart;
        if (!cursor.isAfter(rangeEnd)) {
            while (!cursor.isAfter(rangeEnd)) {
                labelDates.add(cursor);
                cursor = cursor.plusDays(1);
            }
        } else {
            labelDates.add(rangeStart);
        }

        List<String> labels = new ArrayList<>(labelDates.size());
        for (LocalDate date : labelDates) {
            labels.add(date.format(DATE_FORMATTER));
        }

        Map<String, List<Integer>> countsBySession = new LinkedHashMap<>();
        for (String sessionId : sessionIds) {
            countsBySession.put(sessionId, new ArrayList<>(Collections.nCopies(labels.size(), Integer.valueOf(0))));
        }

        if (sessionIds.isEmpty() || widgets == null || widgets.isEmpty() || labels.isEmpty()) {
            return new SessionTimeline(labels, countsBySession);
        }

        String inClause = String.join(COMMA_SPACE, Collections.nCopies(sessionIds.size(), SQL_PARAM));
        Timestamp startTs = Timestamp.valueOf(rangeStart.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(rangeEnd.plusDays(1).atStartOfDay());

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }

            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

                String sql = SQL_SELECT_TIMELINE_PREFIX
                    + quoteIdentifier(tableName)
                    + SQL_WHERE_SESSION_IN + inClause + SQL_CREATED_AT_RANGE
                    + SQL_GROUP_BY_TIMELINE;

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String sessionId : sessionIds) {
                    ps.setString(idx++, sessionId);
                }
                ps.setTimestamp(idx++, startTs);
                ps.setTimestamp(idx, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, DB_COL_SESSION_ID, 256);
                        LocalDate entryDate = readLocalDateColumn(rs, DB_COL_DAY_VALUE);
                        int dayCount = rs.getInt(DB_COL_DAY_COUNT);
                        if (sessionId.isBlank() || entryDate == null) {
                            continue;
                        }

                        List<Integer> bucket = countsBySession.get(sessionId);
                        if (bucket == null) {
                            continue;
                        }

                        long dayIndex = ChronoUnit.DAYS.between(rangeStart, entryDate);
                        if (dayIndex < 0 || dayIndex >= bucket.size()) {
                            continue;
                        }

                        int position = Math.toIntExact(dayIndex);
                        int current = readCountValue(bucket, position);
                        bucket.set(position, Integer.valueOf(current + dayCount));
                    }
                }
            }
        }

        return new SessionTimeline(labels, countsBySession);
    }

    String buildSessionChartPayload(SessionOverview overview, LocalDate rangeStart, LocalDate rangeEnd) {
        JsonArrayBuilder labelBuilder = Json.createArrayBuilder();
        for (String label : overview.getTimeline().getLabels()) {
            labelBuilder.add(label);
        }

        JsonArrayBuilder seriesBuilder = Json.createArrayBuilder();
        for (SessionStat session : overview.getTopSessions()) {
            JsonArrayBuilder countsBuilder = Json.createArrayBuilder();
            List<Integer> values = overview.getTimeline().getCountsBySession().get(session.getSessionId());
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    countsBuilder.add(readCountValue(values, i));
                }
            } else {
                for (int i = 0; i < overview.getTimeline().getLabels().size(); i++) {
                    countsBuilder.add(0);
                }
            }

            seriesBuilder.add(Json.createObjectBuilder()
                    .add(JSON_KEY_SESSION_ID, session.getSessionId())
                    .add(JSON_KEY_COUNTS, countsBuilder));
        }

        return Json.createObjectBuilder()
                .add(JSON_KEY_LABELS, labelBuilder)
                .add(JSON_KEY_SERIES, seriesBuilder)
                .add(JSON_KEY_RANGE_START, rangeStart.format(DATE_FORMATTER))
                .add(JSON_KEY_RANGE_END, rangeEnd.format(DATE_FORMATTER))
                .build()
                .toString();
    }

    String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        return Json.createObjectBuilder()
            .add(JSON_KEY_LABELS, Json.createArrayBuilder())
            .add(JSON_KEY_SERIES, Json.createArrayBuilder())
            .add(JSON_KEY_RANGE_START, rangeStart.format(DATE_FORMATTER))
            .add(JSON_KEY_RANGE_END, rangeEnd.format(DATE_FORMATTER))
                .build()
                .toString();
    }

    private LocalDate readLocalDateColumn(ResultSet rs, String column) {
        String columnName = column == null ? EMPTY : column;
        String dayText = readDbText(rs, column, 64);
        if (!dayText.isBlank()) {
            try {
                return LocalDate.parse(dayText.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, MSG_LOCALDATE_PARSE_FALLBACK + columnName, ex);
            }

            try {
                return Instant.parse(dayText.trim())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, MSG_INSTANT_PARSE_FALLBACK + columnName, ex);
            }

            try {
                return Timestamp.valueOf(dayText.replace('T', ' '))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (IllegalArgumentException ex) {
                log.log(Level.FINE, MSG_TIMESTAMP_PARSE_FALLBACK + columnName, ex);
            }
        }

        return null;
    }

    private String readDbText(ResultSet rs, String column, int maxLen) {
        if (column == null || column.isBlank()) {
            return EMPTY;
        }
        String raw = readRawDbText(rs, column);
        if (raw == null) {
            return EMPTY;
        }
        return TextIoSanitizerUtil.validateCanonicalized(raw, maxLen);
    }

    private int readCountValue(List<Integer> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return 0;
        }
        Number currentValue = values.get(index);
        if (currentValue == null) {
            return 0;
        }
        return Integer.parseInt(currentValue.toString());
    }

    private String readRawDbText(ResultSet rs, String column) {
        try {
            Reader reader = rs.getCharacterStream(column);
            if (reader != null) {
                try (Reader closeable = reader) {
                    return TextIoSanitizerUtil.validateCanonicalized(
                            TextIoSanitizerUtil.readAtMostChars(closeable, 4096), 4096);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_READ_DB_CHAR_STREAM + column, ex);
        } catch (IOException ex) {
            log.log(Level.FINE, MSG_UNABLE_DECODE_DB_CHAR_STREAM + column, ex);
        }

        try {
            byte[] rawBytes = rs.getBytes(column);
            if (rawBytes != null) {
                return TextIoSanitizerUtil.validateCanonicalized(
                        new String(rawBytes, StandardCharsets.UTF_8), 4096);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_READ_DB_BYTES + column, ex);
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return TextIoSanitizerUtil.validateCanonicalized(raw, 4096);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_READ_DB_STRING + column, ex);
        }

        try {
            Object raw = rs.getObject(column);
            if (raw == null) {
                return null;
            }
            return TextIoSanitizerUtil.validateCanonicalized(String.valueOf(raw), 4096);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_READ_DB_OBJECT_TEXT + column, ex);
            return null;
        }
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return DASH;
        }
        return ts.toInstant().atZone(ZoneId.systemDefault()).format(ENTRY_FORMATTER);
    }

    String buildLastFiveDaysTrendJson(AppDataSourceHolder dataSourceHolder, List<WidgetEntry> widgets) {
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(4);

        Map<LocalDate, Integer> totalDaily = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            totalDaily.put(start.plusDays(i), Integer.valueOf(0));
        }

        try (Connection conn = openConnectionSafe(dataSourceHolder)) {
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
                    + SQL_WHERE_CREATED_AT_RANGE;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, DB_COL_CREATED_AT);
                            if (ts == null) {
                                continue;
                            }

                            LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!totalDaily.containsKey(entryDate)) {
                                continue;
                            }

                            Object existing = totalDaily.get(entryDate);
                            int currentCount = 0;
                            if (existing != null) {
                                currentCount = Integer.parseInt(existing.toString());
                            }
                            totalDaily.put(entryDate, Integer.valueOf(currentCount + 1));
                        }
                    }
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_UNABLE_LOAD_5_DAY_TREND, ex);
        }

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            Object dayCount = entry.getValue();
            int safeCount = 0;
            if (dayCount != null) {
                safeCount = Integer.parseInt(dayCount.toString());
            }
            values.add(safeCount);
        }

        return Json.createObjectBuilder()
            .add(JSON_KEY_LABELS, labels)
            .add(JSON_KEY_VALUES, values)
            .add(JSON_KEY_DAYS, 5)
                .build()
                .toString();
    }

    private Connection openConnectionSafe(AppDataSourceHolder dataSourceHolder) {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_OPEN_DASHBOARD_CONNECTION, ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(SQL_IDENTIFIER_INVALID_MSG);
        }
        if (!identifier.matches(SQL_IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(SQL_IDENTIFIER_INVALID_MSG);
        }
        return '"' + identifier.replace(QUOTE, QUOTE_ESCAPED) + '"';
    }
}


