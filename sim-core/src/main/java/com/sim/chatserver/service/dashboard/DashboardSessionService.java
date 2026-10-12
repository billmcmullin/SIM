package com.sim.chatserver.service.dashboard;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.ProgressStat;
import com.sim.chatserver.model.SessionAccumulator;
import com.sim.chatserver.model.SessionOverview;
import com.sim.chatserver.model.SessionStat;
import com.sim.chatserver.model.SessionTimeline;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class DashboardSessionService {

    private static final String EMPTY = "";
        private static final String DASH = "-";
        private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
        private static final String JSON_KEY_COUNTS = "counts";
        private static final String JSON_KEY_LABELS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LABELS;
        private static final String JSON_KEY_SERIES = "series";
        private static final String JSON_KEY_RANGE_START = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_START;
        private static final String JSON_KEY_RANGE_END = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RANGE_END;
        private static final String COL_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
        private static final String COL_LAST_ENTRY = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_LAST_ENTRY;
        private static final String COL_DAY_VALUE = "day_value";
        private static final String COL_DAY_COUNT = "day_count";
        private static final String SQL_SELECT_SESSION_COUNTS_PREFIX =
            com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_SESSION_COUNTS_PREFIX;
        private static final String SQL_SELECT_FIRST_SEEN_PREFIX =
            "SELECT session_id, MIN(created_at) AS first_seen FROM ";
        private static final String SQL_SELECT_SESSION_DAY_COUNTS_PREFIX =
            "SELECT session_id, CAST(created_at AS DATE) AS day_value, COUNT(*) AS day_count FROM ";
        private static final String SQL_GROUP_BY_SESSION_DAY = " GROUP BY session_id, CAST(created_at AS DATE)";
        private static final String SQL_SESSION_ID_IN_PREFIX = " WHERE session_id IN (";
        private static final String SQL_SESSION_ID_IN_DATE_RANGE_SUFFIX = ") AND created_at >= ? AND created_at < ?";
        private static final String LOG_LOCALDATE_PARSE_FALLBACK = "LocalDate parse fallback for column ";
        private static final String LOG_INSTANT_PARSE_FALLBACK = "Instant parse fallback for column ";
        private static final String LOG_TIMESTAMP_PARSE_FALLBACK = "Timestamp parse fallback for column ";


    private static final Logger log = Logger.getLogger(DashboardSessionService.class.getCanonicalName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern(com.sim.chatserver.util.CommonLiteralConstants.DATETIME_PATTERN_SECONDS);

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    final SessionOverview buildSessionOverview(
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

        // First-seen date by session across ALL widgets (earliest wins)
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

                // Existing totals/last-entry aggregation
                String sql = SQL_SELECT_SESSION_COUNTS_PREFIX
                        + DashboardDbUtil.quoteIdentifier(tableName)
                    + SqlLiteralConstants.SQL_SESSION_ID_NOT_NULL_GROUP_BY;

                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, SqlLiteralConstants.COL_SESSION_ID, 256);
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }

                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.addCount(rs.getInt(COL_TOTAL));

                        Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, COL_LAST_ENTRY);
                        if (lastEntry != null && (acc.getLastEntry() == null || lastEntry.after(acc.getLastEntry()))) {
                            acc.setLastEntry(lastEntry);
                        }
                    }
                }

                // Existing new-sessions support: first-seen per session in this table
                String firstSeenSql = SQL_SELECT_FIRST_SEEN_PREFIX
                        + DashboardDbUtil.quoteIdentifier(tableName)
                        + SqlLiteralConstants.SQL_SESSION_ID_NOT_NULL_GROUP_BY;

                try (PreparedStatement ps = conn.prepareStatement(firstSeenSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, SqlLiteralConstants.COL_SESSION_ID, 256);
                        Timestamp firstSeenTs = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_FIRST_SEEN);
                        if (sessionId == null || sessionId.isBlank() || firstSeenTs == null) {
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

        // Current active/inactive
        Instant cutoffNow = Instant.now().minus(activeDays, ChronoUnit.DAYS);
        int inactiveUsers = countInactiveUsers(accumulators, cutoffNow, false);
        int activeUsers = Math.max(0, totalUsers - inactiveUsers);

        // NEW: yesterday active/inactive baseline (same activeDays window, shifted by 1 day)
        Instant cutoffYesterday = Instant.now()
                .minus(1, ChronoUnit.DAYS)
                .minus(activeDays, ChronoUnit.DAYS);

        int inactiveUsersYesterday = countInactiveUsers(accumulators, cutoffYesterday, true);
        int activeUsersYesterday = Math.max(0, totalUsers - inactiveUsersYesterday);

        ProgressStat activeUsersProgression = new ProgressStat(activeUsers, activeUsersYesterday);

        // Existing new sessions today vs yesterday
        int newSessionsToday = 0;
        int newSessionsYesterday = 0;
        for (LocalDate d : firstSeenBySession.values()) {
            if (today.equals(d)) {
                newSessionsToday++;
            } else if (yesterday.equals(d)) {
                newSessionsYesterday++;
            }
        }
        ProgressStat newSessionsProgression = new ProgressStat(newSessionsToday, newSessionsYesterday);

        List<Map.Entry<String, SessionAccumulator>> topEntries = selectTopSessionEntries(accumulators, 10);

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

        // Keeps existing behavior + adds active day-over-day metrics
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

        List<LocalDate> labelDates = buildLabelDates(rangeStart, rangeEnd);

        List<String> labels = buildDateLabels(labelDates);

        Map<String, List<Integer>> countsBySession = new LinkedHashMap<>();
        int labelCount = labels.size();
        for (String sessionId : sessionIds) {
            countsBySession.put(sessionId, buildZeroCounts(labelCount));
        }

        if (sessionIds.isEmpty() || widgets == null || widgets.isEmpty() || labels.isEmpty()) {
            return new SessionTimeline(labels, countsBySession);
        }

        String inClause = String.join(SqlLiteralConstants.COMMA_SPACE,
                Collections.nCopies(sessionIds.size(), SqlLiteralConstants.QUESTION_MARK));
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

                String sql = SQL_SELECT_SESSION_DAY_COUNTS_PREFIX
                    + DashboardDbUtil.quoteIdentifier(tableName)
                    + SQL_SESSION_ID_IN_PREFIX + inClause + SQL_SESSION_ID_IN_DATE_RANGE_SUFFIX
                    + SQL_GROUP_BY_SESSION_DAY;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String sessionId : sessionIds) {
                    ps.setString(idx++, sessionId);
                }
                ps.setTimestamp(idx++, startTs);
                ps.setTimestamp(idx, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, SqlLiteralConstants.COL_SESSION_ID, 256);
                        LocalDate entryDate = readLocalDateColumn(rs, COL_DAY_VALUE);
                        int dayCount = rs.getInt(COL_DAY_COUNT);
                        if (sessionId == null || entryDate == null) {
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

    private int countInactiveUsers(
            Map<String, SessionAccumulator> accumulators,
            Instant cutoff,
            boolean countMissingLastEntryAsInactive) {
        int inactiveUsers = 0;
        for (SessionAccumulator acc : accumulators.values()) {
            Timestamp last = acc.getLastEntry();
            if (last == null) {
                if (countMissingLastEntryAsInactive) {
                    inactiveUsers++;
                }
                continue;
            }
            if (last.toInstant().isBefore(cutoff)) {
                inactiveUsers++;
            }
        }
        return inactiveUsers;
    }

    private List<Map.Entry<String, SessionAccumulator>> selectTopSessionEntries(
            Map<String, SessionAccumulator> accumulators,
            int limit) {
        PriorityQueue<Map.Entry<String, SessionAccumulator>> queue =
                new PriorityQueue<>(Comparator.comparingInt(e -> e.getValue().getCount()));

        for (Map.Entry<String, SessionAccumulator> entry : accumulators.entrySet()) {
            if (queue.size() < limit) {
                queue.offer(entry);
            } else if (entry.getValue().getCount() > queue.peek().getValue().getCount()) {
                queue.poll();
                queue.offer(entry);
            }
        }

        List<Map.Entry<String, SessionAccumulator>> topEntries = new ArrayList<>(queue);
        topEntries.sort((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()));
        return topEntries;
    }

    private List<LocalDate> buildLabelDates(LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> labelDates = new ArrayList<>();
        LocalDate cursor = rangeStart;
        if (!cursor.isAfter(rangeEnd)) {
            while (!cursor.isAfter(rangeEnd)) {
                labelDates.add(cursor);
                cursor = cursor.plusDays(1);
            }
            return labelDates;
        }
        labelDates.add(rangeStart);
        return labelDates;
    }

    private List<String> buildDateLabels(List<LocalDate> labelDates) {
        List<String> labels = new ArrayList<>();
        if (labelDates == null || labelDates.isEmpty()) {
            return labels;
        }
        for (LocalDate date : labelDates) {
            labels.add(date.format(DATE_FORMATTER));
        }
        return labels;
    }

    private List<Integer> buildZeroCounts(int size) {
        if (size <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Collections.nCopies(size, Integer.valueOf(0)));
    }

    public String buildSessionChartPayload(SessionOverview overview, LocalDate rangeStart, LocalDate rangeEnd) {
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

        JsonObject payload = Json.createObjectBuilder()
                .add(JSON_KEY_LABELS, labelBuilder)
                .add(JSON_KEY_SERIES, seriesBuilder)
                .add(JSON_KEY_RANGE_START, rangeStart.format(DATE_FORMATTER))
                .add(JSON_KEY_RANGE_END, rangeEnd.format(DATE_FORMATTER))
                .build();

        return payload.toString();
    }

    public String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        JsonObject payload = Json.createObjectBuilder()
                .add(JSON_KEY_LABELS, Json.createArrayBuilder())
                .add(JSON_KEY_SERIES, Json.createArrayBuilder())
                .add(JSON_KEY_RANGE_START, rangeStart.format(DATE_FORMATTER))
                .add(JSON_KEY_RANGE_END, rangeEnd.format(DATE_FORMATTER))
                .build();
        return payload.toString();
    }

    private LocalDate readLocalDateColumn(ResultSet rs, String column) throws SQLException {
        String columnName = column == null ? EMPTY : column;
        String dayText = readDbText(rs, column, 64);
        if (dayText != null && !dayText.isBlank()) {
            try {
                return LocalDate.parse(dayText.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, LOG_LOCALDATE_PARSE_FALLBACK + columnName, ex);
            }

            try {
                return Instant.parse(dayText.trim())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, LOG_INSTANT_PARSE_FALLBACK + columnName, ex);
            }

            try {
                return Timestamp.valueOf(dayText.replace('T', ' '))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (IllegalArgumentException ex) {
                log.log(Level.FINE, LOG_TIMESTAMP_PARSE_FALLBACK + columnName, ex);
            }
        }

        return null;
    }

    private String readDbText(ResultSet rs, String column, int maxLen) throws SQLException {
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
        return currentValue == null ? 0 : currentValue.intValue();
    }

    private String readRawDbText(ResultSet rs, String column) {
        return TextIoSanitizerUtil.readRawDbText(rs, column, 4096, log);
    }


    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return DASH;
        }
        return ts.toInstant().atZone(ZoneId.systemDefault()).format(ENTRY_FORMATTER);
    }
}

