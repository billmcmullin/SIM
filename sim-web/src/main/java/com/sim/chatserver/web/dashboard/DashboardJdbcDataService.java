package com.sim.chatserver.web.dashboard;

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

import com.sim.chatserver.model.DashboardViewModels.SessionAccumulator;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.web.dashboard.DashboardLocalViewModels.SessionOverview;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

final class DashboardJdbcDataService {

    private static final Logger log = Logger.getLogger(DashboardJdbcDataService.class.getName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    List<WidgetEntry> loadWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard", e);
            return List.of();
        }
    }

    Map<String, SessionLabelStore.SessionLabel> loadSessionLabels(List<SessionStat> stats) {
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
            log.log(Level.WARNING, "Unable to load session labels", e);
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
            TermSummary allTimeSummary = termService.buildTermSummaryForDashboard(conn, widgets, terms);
            return filterTermSummaryByRange(allTimeSummary, rangeStart, rangeEnd);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to compute term summary", ex);
            return null;
        }
    }

    private TermSummary filterTermSummaryByRange(TermSummary allTimeSummary, LocalDate rangeStart, LocalDate rangeEnd) {
        if (allTimeSummary == null) {
            return null;
        }
        if (rangeStart == null || rangeEnd == null) {
            return allTimeSummary;
        }

        TermSummary filtered = new TermSummary();
        for (String term : allTimeSummary.getTermCounts().keySet()) {
            filtered.ensureTerm(term);
        }

        for (Map.Entry<String, List<TermChatSnapshot>> entry : allTimeSummary.getTermSnapshots().entrySet()) {
            String term = entry.getKey();
            List<TermChatSnapshot> snapshots = entry.getValue();
            if (term == null || snapshots == null || snapshots.isEmpty()) {
                continue;
            }
            for (TermChatSnapshot snapshot : snapshots) {
                if (snapshot == null || !isWithinDateRange(snapshot.getCreatedAt(), rangeStart, rangeEnd)) {
                    continue;
                }
                filtered.recordMatch(term, snapshot);
            }
        }

        return filtered;
    }

    private boolean isWithinDateRange(Timestamp createdAt, LocalDate rangeStart, LocalDate rangeEnd) {
        if (createdAt == null || rangeStart == null || rangeEnd == null) {
            return false;
        }
        LocalDate day = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return !day.isBefore(rangeStart) && !day.isAfter(rangeEnd);
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
            log.log(Level.WARNING, "Unable to compute session overview", ex);
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

                String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, "session_id", 256);
                        if (sessionId.isBlank()) {
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

                String firstSeenSql = "SELECT session_id, MIN(created_at) AS first_seen FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(firstSeenSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = readDbText(rs, "session_id", 256);
                        Timestamp firstSeenTs = SqlTimeUtil.safeTimestamp(rs, "first_seen");
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

        DashboardLocalViewModels.ProgressStat activeUsersProgression =
                new DashboardLocalViewModels.ProgressStat(activeUsers, activeUsersYesterday);

        int newSessionsToday = 0;
        int newSessionsYesterday = 0;
        for (LocalDate d : firstSeenBySession.values()) {
            if (today.equals(d)) {
                newSessionsToday++;
            } else if (yesterday.equals(d)) {
                newSessionsYesterday++;
            }
        }
        DashboardLocalViewModels.ProgressStat newSessionsProgression =
                new DashboardLocalViewModels.ProgressStat(newSessionsToday, newSessionsYesterday);

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
                    + quoteIdentifier(tableName)
                    + " WHERE session_id IN (" + inClause + ") AND created_at >= ? AND created_at < ?"
                    + " GROUP BY session_id, CAST(created_at AS DATE)";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    .add("sessionId", session.getSessionId())
                    .add("counts", countsBuilder));
        }

        return Json.createObjectBuilder()
                .add("labels", labelBuilder)
                .add("series", seriesBuilder)
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build()
                .toString();
    }

    String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        return Json.createObjectBuilder()
                .add("labels", Json.createArrayBuilder())
                .add("series", Json.createArrayBuilder())
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build()
                .toString();
    }

    private LocalDate readLocalDateColumn(ResultSet rs, String column) {
        String columnName = column == null ? "" : column;
        String dayText = readDbText(rs, column, 64);
        if (!dayText.isBlank()) {
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

    private String readDbText(ResultSet rs, String column, int maxLen) {
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

                String sql = "SELECT created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (ts == null) {
                                continue;
                            }

                            LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!totalDaily.containsKey(entryDate)) {
                                continue;
                            }

                            Integer existing = totalDaily.get(entryDate);
                            int currentCount = 0;
                            if (existing != null) {
                                currentCount = existing.intValue();
                            }
                            totalDaily.put(entryDate, Integer.valueOf(currentCount + 1));
                        }
                    }
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to load 5-day trend data", ex);
        }

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            Integer dayCount = entry.getValue();
            int safeCount = 0;
            if (dayCount != null) {
                safeCount = dayCount.intValue();
            }
            values.add(safeCount);
        }

        return Json.createObjectBuilder()
                .add("labels", labels)
                .add("values", values)
                .add("days", 5)
                .build()
                .toString();
    }

    private Connection openConnectionSafe(AppDataSourceHolder dataSourceHolder) {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to open dashboard data connection", ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        if (!identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
