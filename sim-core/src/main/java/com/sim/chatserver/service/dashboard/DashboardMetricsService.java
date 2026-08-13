package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.TermDayCount;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

public class DashboardMetricsService {

    private static final Logger LOG = Logger.getLogger(DashboardMetricsService.class.getName());

    public static final String OTHER_PARASOFT_LABEL = "Other Parasoft Match";

    private final AppDataSourceHolder dsHolder;
    private final TermsStore termsStore;
    private final int topTopicLimit;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public DashboardMetricsService(AppDataSourceHolder dsHolder, TermsStore termsStore, int topTopicLimit) {
        this.dsHolder = dsHolder;
        this.termsStore = termsStore;
        this.topTopicLimit = topTopicLimit;
    }

    public static final class DashboardProgressMetrics {

        private final int chatsToday;
        private final int chatsYesterday;
        private final ProgressStat chatsProgression;

        private final int termsToday;
        private final int termsYesterday;
        private final ProgressStat termsProgression;

        public DashboardProgressMetrics(int chatsToday, int chatsYesterday, int termsToday, int termsYesterday) {
            this.chatsToday = chatsToday;
            this.chatsYesterday = chatsYesterday;
            this.chatsProgression = new ProgressStat(chatsToday, chatsYesterday);

            this.termsToday = termsToday;
            this.termsYesterday = termsYesterday;
            this.termsProgression = new ProgressStat(termsToday, termsYesterday);
        }

        public int getChatsToday() {
            return chatsToday;
        }

        public int getChatsYesterday() {
            return chatsYesterday;
        }

        public ProgressStat getChatsProgression() {
            return chatsProgression;
        }

        public int getTermsToday() {
            return termsToday;
        }

        public int getTermsYesterday() {
            return termsYesterday;
        }

        public ProgressStat getTermsProgression() {
            return termsProgression;
        }
    }

    public List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        List<WidgetStat> stats = new ArrayList<>();
        if (widgets == null || widgets.isEmpty()) {
            return stats;
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp tomorrowStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
        Timestamp yesterdayStart = Timestamp.valueOf(yesterday.atStartOfDay());

        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String displayName = widget.getDisplayName();
                displayName = (displayName == null || displayName.isBlank()) ? widgetId : displayName;

                String tableName;
                try {
                    tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.FINE, "Skipping widget with invalid id during stats build", ex);
                    continue;
                }
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                int totalCount = countRows(conn, tableName);
                int todayCount = countRowsBetween(conn, tableName, todayStart, tomorrowStart);
                int yesterdayCount = countRowsBetween(conn, tableName, yesterdayStart, todayStart);

                stats.add(new WidgetStat(widgetId, displayName, totalCount, todayCount, yesterdayCount));
            }
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, "buildWidgetStats fallback to empty list", e);
            return List.of();
        }

        return stats;
    }

    public ProgressStat buildChatProgression(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            int todayCount = countChatsForDate(conn, widgets, today, tableExistsCache);
            int yesterdayCount = countChatsForDate(conn, widgets, yesterday, tableExistsCache);
            return new ProgressStat(todayCount, yesterdayCount);
        } catch (SQLException e) {
            LOG.log(Level.FINE, "buildChatProgression fallback to zeros", e);
            return new ProgressStat(0, 0);
        }
    }

    public ProgressStat buildNewUserProgression(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            int todayCount = countDistinctSessionsFirstSeenOnDate(conn, widgets, today);
            int yesterdayCount = countDistinctSessionsFirstSeenOnDate(conn, widgets, yesterday);
            return new ProgressStat(todayCount, yesterdayCount);
        } catch (SQLException e) {
            LOG.log(Level.FINE, "buildNewUserProgression fallback to zeros", e);
            return new ProgressStat(0, 0);
        }
    }

    public DashboardProgressMetrics buildDashboardProgressMetrics(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            int chatsToday = countChatsForDate(conn, widgets, today, tableExistsCache);
            int chatsYesterday = countChatsForDate(conn, widgets, yesterday, tableExistsCache);

            // Enhancement: term counts use best-topic-per-chat semantics (same as topics data endpoint),
            // so dashboard termsToday/termsYesterday match "new entries categorized in terms".
            TermDayCount termCounts = countTermAssignmentsForDays(conn, widgets, today, yesterday, tableExistsCache);

            return new DashboardProgressMetrics(
                    chatsToday,
                    chatsYesterday,
                    termCounts.getToday(),
                    termCounts.getYesterday()
            );
        } catch (SQLException e) {
            LOG.log(Level.FINE, "buildDashboardProgressMetrics fallback to zeros", e);
            return new DashboardProgressMetrics(0, 0, 0, 0);
        }
    }

    public List<TopTopic> buildTopTopicsTodayVsYesterday(List<WidgetEntry> widgets) {
        if (widgets == null || widgets.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, "buildTopTopicsTodayVsYesterday term load failed", e);
            return List.of();
        }

        if (terms == null || terms.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : terms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            String name = term.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            Pattern p = TermMatcher.buildStrictPattern(term);
            if (p == null) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(p);
        }

        if (activeTerms.isEmpty()) {
            return List.of();
        }

        Map<String, TermDayCount> counts = new LinkedHashMap<>();
        for (TermDefinition t : activeTerms) {
            String name = t.getName();
            if (name != null && !name.isBlank() && !OTHER_PARASOFT_LABEL.equalsIgnoreCase(name)) {
                counts.put(name, new TermDayCount());
            }
        }

        if (counts.isEmpty()) {
            return List.of();
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            Timestamp startTs = Timestamp.valueOf(yesterday.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = "SELECT prompt, created_at FROM " + DashboardDbUtil.quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, startTs);
                    ps.setTimestamp(2, endTs);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String prompt = rs.getString("prompt");
                            if (prompt == null) {
                                prompt = "";
                            }

                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (createdAt == null) {
                                continue;
                            }

                            LocalDate d = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!d.equals(today) && !d.equals(yesterday)) {
                                continue;
                            }

                            String sanitizedPrompt = TextSanitizer.sanitizeForMatching(prompt);

                            TermDefinition bestTerm = null;
                            int bestStart = Integer.MAX_VALUE;

                            for (int i = 0; i < compiledPatterns.size(); i++) {
                                try {
                                    Matcher m = compiledPatterns.get(i).matcher(sanitizedPrompt);
                                    if (m.find()) {
                                        int s = m.start();
                                        if (s < bestStart) {
                                            bestStart = s;
                                            bestTerm = activeTerms.get(i);
                                            if (bestStart == 0) {
                                                break;
                                            }
                                        }
                                    }
                                } catch (IllegalStateException ex) {
                                    LOG.log(Level.FINE, "Topic pattern evaluation failed", ex);
                                }
                            }

                            if (bestTerm == null) {
                                continue;
                            }
                            String label = bestTerm.getName();
                            if (label == null || label.isBlank() || OTHER_PARASOFT_LABEL.equalsIgnoreCase(label)) {
                                continue;
                            }

                            TermDayCount c = counts.get(label);
                            if (c == null) {
                                continue;
                            }

                            if (d.equals(today)) {
                                c.incToday();
                            } else {
                                c.incYesterday();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.FINE, "buildTopTopicsTodayVsYesterday fallback to empty list", e);
            return List.of();
        }

        return counts.entrySet().stream()
                .map(e -> new TopTopic(e.getKey(), e.getValue().getToday(), e.getValue().getYesterday()))
                .filter(t -> t.getTotal() > 0)
                .sorted(Comparator
                        .comparingInt(TopTopic::getTotal).reversed()
                        .thenComparing(Comparator.comparingInt(TopTopic::getToday).reversed())
                        .thenComparing(TopTopic::getLabel, String.CASE_INSENSITIVE_ORDER))
                .limit(topTopicLimit)
                .collect(Collectors.toList());
    }

    public List<OtherParasoftEntry> buildLatestOtherParasoftEntries(List<WidgetEntry> widgets, int limit) {
        if (widgets == null || widgets.isEmpty() || limit <= 0) {
            return List.of();
        }

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, "buildLatestOtherParasoftEntries term load failed", e);
            return List.of();
        }

        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : terms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            Pattern p = TermMatcher.buildStrictPattern(term);
            if (p != null) {
                compiledPatterns.add(p);
            }
        }

        List<OtherParasoftEntry> all = new ArrayList<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String widgetName = (widget.getDisplayName() == null || widget.getDisplayName().isBlank())
                        ? widgetId
                        : widget.getDisplayName();

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = "SELECT prompt, session_id, created_at FROM "
                        + DashboardDbUtil.quoteIdentifier(tableName)
                        + " ORDER BY created_at DESC LIMIT 500";

                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        String prompt = rs.getString("prompt");
                        String sessionId = rs.getString("session_id");
                        Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                        if (createdAt == null) {
                            continue;
                        }

                        String sanitized = TextSanitizer.sanitizeForMatching(prompt == null ? "" : prompt);

                        boolean matchedKnownTerm = false;
                        for (Pattern p : compiledPatterns) {
                            try {
                                Matcher m = p.matcher(sanitized);
                                if (m.find()) {
                                    matchedKnownTerm = true;
                                    break;
                                }
                            } catch (IllegalStateException ex) {
                                LOG.log(Level.FINE, "Other Parasoft matcher evaluation failed", ex);
                            }
                        }

                        if (!matchedKnownTerm) {
                            all.add(new OtherParasoftEntry(
                                    widgetId,
                                    widgetName,
                                    prompt == null ? "" : prompt,
                                    sessionId == null ? "" : sessionId,
                                    createdAt
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.FINE, "buildLatestOtherParasoftEntries fallback to empty list", e);
            return List.of();
        }

        all.sort(Comparator.comparing(OtherParasoftEntry::getCreatedAt).reversed());
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DashboardDbUtil.quoteIdentifier(tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int countChatsForDate(Connection conn, List<WidgetEntry> widgets, LocalDate date,
            Map<String, Boolean> tableExistsCache) throws SQLException {
        if (widgets == null || widgets.isEmpty()) {
            return 0;
        }

        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        int total = 0;
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }

            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

            String sql = "SELECT COUNT(*) FROM " + DashboardDbUtil.quoteIdentifier(tableName)
                    + " WHERE created_at >= ? AND created_at < ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, start);
                ps.setTimestamp(2, end);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getInt(1);
                    }
                }
            }
        }
        return total;
    }

    /**
     * Counts assigned term entries for today and yesterday (excluding Other
     * Parasoft Match). Uses "best first match" logic so each chat contributes
     * to at most one term.
     */
    private TermDayCount countTermAssignmentsForDays(Connection conn,
            List<WidgetEntry> widgets,
            LocalDate today,
            LocalDate yesterday,
            Map<String, Boolean> tableExistsCache) throws SQLException {
        TermDayCount out = new TermDayCount();
        if (widgets == null || widgets.isEmpty()) {
            return out;
        }

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (IllegalStateException e) {
            LOG.log(Level.FINE, "countTermAssignmentsForDays term load failed", e);
            return out;
        }

        List<Pattern> patterns = new ArrayList<>();
        for (TermDefinition t : terms) {
            if (t == null || t.isSystemFlag()) {
                continue;
            }
            String name = t.getName();
            if (name == null || name.isBlank() || OTHER_PARASOFT_LABEL.equalsIgnoreCase(name.trim())) {
                continue;
            }
            Pattern p = TermMatcher.buildStrictPattern(t);
            if (p != null) {
                patterns.add(p);
            }
        }

        if (patterns.isEmpty()) {
            return out;
        }

        Timestamp startTs = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }

            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

            String sql = "SELECT prompt, created_at FROM " + DashboardDbUtil.quoteIdentifier(tableName)
                    + " WHERE created_at >= ? AND created_at < ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                        if (createdAt == null) {
                            continue;
                        }

                        LocalDate d = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        if (!d.equals(today) && !d.equals(yesterday)) {
                            continue;
                        }

                        String promptRaw = rs.getString("prompt");
                        String prompt = TextSanitizer.sanitizeForMatching(promptRaw == null ? "" : promptRaw);

                        int bestStart = Integer.MAX_VALUE;
                        boolean matched = false;

                        for (Pattern p : patterns) {
                            try {
                                Matcher m = p.matcher(prompt);
                                if (m.find()) {
                                    int s = m.start();
                                    if (s < bestStart) {
                                        bestStart = s;
                                        matched = true;
                                        if (bestStart == 0) {
                                            break;
                                        }
                                    }
                                }
                            } catch (IllegalStateException ex) {
                                LOG.log(Level.FINE, "Term assignment matcher evaluation failed", ex);
                            }
                        }

                        if (!matched) {
                            continue;
                        }

                        if (d.equals(today)) {
                            out.incToday();
                        } else {
                            out.incYesterday();
                        }
                    }
                }
            }
        }

        return out;
    }

    private int countDistinctSessionsFirstSeenOnDate(Connection conn, List<WidgetEntry> widgets, LocalDate date) throws SQLException {
        if (date == null || widgets == null || widgets.isEmpty()) {
            return 0;
        }

        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
        Map<String, Timestamp> earliestBySession = new LinkedHashMap<>();

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }

            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

            String sql = "SELECT session_id, MIN(created_at) AS first_seen FROM "
                    + DashboardDbUtil.quoteIdentifier(tableName)
                    + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sessionId = rs.getString("session_id");
                    Timestamp firstSeen = SqlTimeUtil.safeTimestamp(rs, "first_seen");
                    if (sessionId == null || sessionId.isBlank() || firstSeen == null) {
                        continue;
                    }

                    sessionId = sessionId.trim();
                    Timestamp prev = earliestBySession.get(sessionId);
                    if (prev == null || firstSeen.before(prev)) {
                        earliestBySession.put(sessionId, firstSeen);
                    }
                }
            }
        }

        int count = 0;
        ZoneId zone = ZoneId.systemDefault();
        for (Timestamp ts : earliestBySession.values()) {
            LocalDate firstDay = ts.toInstant().atZone(zone).toLocalDate();
            if (date.equals(firstDay)) {
                count++;
            }
        }
        return count;
    }

    private int countRowsBetween(Connection conn, String tableName, Timestamp start, Timestamp end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DashboardDbUtil.quoteIdentifier(tableName)
                + " WHERE created_at >= ? AND created_at < ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
