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

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionAccumulator;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class DashboardSessionService {

    private static final Logger log = Logger.getLogger(DashboardSessionService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    SessionOverview buildSessionOverview(
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
                String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                        + DashboardDbUtil.quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";

                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, "session_id", 256);
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }

                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.addCount(rs.getInt("total"));

                        Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                        if (lastEntry != null && (acc.getLastEntry() == null || lastEntry.after(acc.getLastEntry()))) {
                            acc.setLastEntry(lastEntry);
                        }
                    }
                }

                // Existing new-sessions support: first-seen per session in this table
                String firstSeenSql = "SELECT session_id, MIN(created_at) AS first_seen FROM "
                        + DashboardDbUtil.quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";

                try (PreparedStatement ps = conn.prepareStatement(firstSeenSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, "session_id", 256);
                        Timestamp firstSeenTs = SqlTimeUtil.safeTimestamp(rs, "first_seen");
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
        int inactiveUsers = 0;
        for (SessionAccumulator acc : accumulators.values()) {
            if (acc.getLastEntry() != null && acc.getLastEntry().toInstant().isBefore(cutoffNow)) {
                inactiveUsers++;
            }
        }
        int activeUsers = Math.max(0, totalUsers - inactiveUsers);

        // NEW: yesterday active/inactive baseline (same activeDays window, shifted by 1 day)
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

        String inClause = String.join(", ", Collections.nCopies(sessionIds.size(), "?"));
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

            String sql = "SELECT session_id, CAST(created_at AS DATE) AS day_value, COUNT(*) AS day_count FROM "
                    + DashboardDbUtil.quoteIdentifier(tableName)
                    + " WHERE session_id IN (" + inClause + ") AND created_at >= ? AND created_at < ?"
                    + " GROUP BY session_id, CAST(created_at AS DATE)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String sessionId : sessionIds) {
                    ps.setString(idx++, sessionId);
                }
                ps.setTimestamp(idx++, startTs);
                ps.setTimestamp(idx, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, "session_id", 256);
                        LocalDate entryDate = readLocalDateColumn(rs, "day_value");
                        int dayCount = rs.getInt("day_count");
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
                    .add("sessionId", session.getSessionId())
                    .add("counts", countsBuilder));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("labels", labelBuilder)
                .add("series", seriesBuilder)
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build();

        return payload.toString();
    }

    public String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        JsonObject payload = Json.createObjectBuilder()
                .add("labels", Json.createArrayBuilder())
                .add("series", Json.createArrayBuilder())
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build();
        return payload.toString();
    }

    private LocalDate readLocalDateColumn(ResultSet rs, String column) throws SQLException {
        String columnName = column == null ? "" : column;
        String dayText = readDbText(rs, column, 64);
        if (dayText != null && !dayText.isBlank()) {
            try {
                return LocalDate.parse(dayText.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "LocalDate parse fallback for column " + columnName, ex);
            }

            try {
                return Instant.parse(dayText.trim())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Instant parse fallback for column " + columnName, ex);
            }

            try {
                return Timestamp.valueOf(dayText.replace('T', ' '))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (IllegalArgumentException ex) {
                log.log(Level.FINE, "Timestamp parse fallback for column " + columnName, ex);
            }
        }

        return null;
    }

    private String readDbText(ResultSet rs, String column, int maxLen) throws SQLException {
        if (column == null || column.isBlank()) {
            return "";
        }
        String raw = readRawDbText(rs, column);
        if (raw == null) {
            return "";
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
        try {
            Reader reader = rs.getCharacterStream(column);
            if (reader != null) {
                try (Reader closeable = reader) {
                    return TextIoSanitizerUtil.validateCanonicalized(
                            TextIoSanitizerUtil.readAtMostChars(closeable, 4096), 4096);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to read DB character stream for column " + column, ex);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to decode DB character stream for column " + column, ex);
        }

        try {
            byte[] rawBytes = rs.getBytes(column);
            if (rawBytes != null) {
                return TextIoSanitizerUtil.validateCanonicalized(
                        new String(rawBytes, StandardCharsets.UTF_8), 4096);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to read DB bytes for column " + column, ex);
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return TextIoSanitizerUtil.validateCanonicalized(raw, 4096);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to read DB string for column " + column, ex);
        }

        try {
            Object raw = rs.getObject(column);
            if (raw == null) {
                return null;
            }
            return TextIoSanitizerUtil.validateCanonicalized(String.valueOf(raw), 4096);
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to read DB object text for column " + column, ex);
            return null;
        }
    }


    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "-";
        }
        return ts.toInstant().atZone(ZoneId.systemDefault()).format(ENTRY_FORMATTER);
    }
}
