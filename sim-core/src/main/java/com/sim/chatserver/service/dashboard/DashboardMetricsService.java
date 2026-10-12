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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.ProgressStat;
import com.sim.chatserver.model.TermDayCount;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

public class DashboardMetricsService {


    private static final Logger LOG = Logger.getLogger(DashboardMetricsService.class.getCanonicalName());
    private static final String EMPTY = "";
    private static final String LOG_SKIP_INVALID_WIDGET_ID = "Skipping widget with invalid id during stats build";
    private static final String LOG_BUILD_WIDGET_STATS_FALLBACK = "buildWidgetStats fallback to empty list";
    private static final String LOG_BUILD_CHAT_PROGRESSION_FALLBACK = "buildChatProgression fallback to zeros";
    private static final String LOG_BUILD_NEW_USER_PROGRESSION_FALLBACK = "buildNewUserProgression fallback to zeros";
    private static final String LOG_BUILD_DASHBOARD_PROGRESS_FALLBACK = "buildDashboardProgressMetrics fallback to zeros";
    private static final String LOG_BUILD_TOP_TOPICS_TERM_LOAD_FAILED = "buildTopTopicsTodayVsYesterday term load failed";
    private static final String LOG_BUILD_TOP_TOPICS_FALLBACK = "buildTopTopicsTodayVsYesterday fallback to empty list";
    private static final String LOG_BUILD_OTHER_PARASOFT_TERM_LOAD_FAILED = "buildLatestOtherParasoftEntries term load failed";
    private static final String LOG_BUILD_OTHER_PARASOFT_FALLBACK = "buildLatestOtherParasoftEntries fallback to empty list";
    private static final String LOG_COUNT_TERM_ASSIGNMENTS_TERM_LOAD_FAILED = "countTermAssignmentsForDays term load failed";
    private static final String LOG_TOPIC_PATTERN_EVALUATION_FAILED = "Topic pattern evaluation failed";
    private static final String LOG_OTHER_PARASOFT_MATCHER_EVALUATION_FAILED = "Other Parasoft matcher evaluation failed";
        private static final String SQL_SELECT_PROMPT_CREATED_PREFIX = "SELECT prompt, created_at FROM ";
        private static final String SQL_SELECT_SESSION_FIRST_SEEN_PREFIX =
            "SELECT session_id, MIN(created_at) AS first_seen FROM ";
        private static final String SQL_SELECT_PROMPT_SESSION_CREATED_PREFIX =
            "SELECT prompt, session_id, created_at FROM ";
        private static final String SQL_ORDER_BY_CREATED_DESC_LIMIT_500 = " ORDER BY created_at DESC LIMIT 500";

    public static final String OTHER_PARASOFT_LABEL = TermsStore.OTHER_PARASOFT_LABEL;
    private static final ProgressStat ZERO_PROGRESS = new ProgressStat(0, 0);
    private static final DashboardProgressMetrics ZERO_DASHBOARD_PROGRESS = DashboardProgressMetrics.of(0, 0, 0, 0);
    private static final Supplier<TermDayCount> TERM_DAY_COUNT_FACTORY = TermDayCount::new;

    private final AppDataSourceHolder dsHolder;
    private final TermsStore termsStore;
    private final int topTopicLimit;

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private DashboardMetricsService(AppDataSourceHolder dsHolder, TermsStore termsStore, int topTopicLimit) {
        this.dsHolder = dsHolder;
        this.termsStore = termsStore;
        this.topTopicLimit = topTopicLimit;
    }

    private static DashboardMetricsService create(AppDataSourceHolder dsHolder, TermsStore termsStore, int topTopicLimit) {
        return new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);
    }

    public static DashboardMetricsService createForDashboard(AppDataSourceHolder dsHolder, TermsStore termsStore, int topTopicLimit) {
        return create(dsHolder, termsStore, topTopicLimit);
    }

    private final List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
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
                if (isWidgetMissingId(widget)) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String displayName = widget.getDisplayName();
                displayName = (displayName == null || displayName.isBlank()) ? widgetId : displayName;

                String tableName;
                try {
                    tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.FINE, LOG_SKIP_INVALID_WIDGET_ID, ex);
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
            LOG.log(Level.FINE, LOG_BUILD_WIDGET_STATS_FALLBACK, e);
            return List.of();
        }

        return stats;
    }

    private final ProgressStat buildChatProgression(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            int todayCount = countChatsForDate(conn, widgets, today, tableExistsCache);
            int yesterdayCount = countChatsForDate(conn, widgets, yesterday, tableExistsCache);
            return new ProgressStat(todayCount, yesterdayCount);
        } catch (SQLException e) {
            LOG.log(Level.FINE, LOG_BUILD_CHAT_PROGRESSION_FALLBACK, e);
            return ZERO_PROGRESS;
        }
    }

    private final ProgressStat buildNewUserProgression(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            int todayCount = countDistinctSessionsFirstSeenOnDate(conn, widgets, today);
            int yesterdayCount = countDistinctSessionsFirstSeenOnDate(conn, widgets, yesterday);
            return new ProgressStat(todayCount, yesterdayCount);
        } catch (SQLException e) {
            LOG.log(Level.FINE, LOG_BUILD_NEW_USER_PROGRESSION_FALLBACK, e);
            return ZERO_PROGRESS;
        }
    }

    private final DashboardProgressMetrics buildDashboardProgressMetrics(List<WidgetEntry> widgets) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            int chatsToday = countChatsForDate(conn, widgets, today, tableExistsCache);
            int chatsYesterday = countChatsForDate(conn, widgets, yesterday, tableExistsCache);

            // Enhancement: term counts use best-topic-per-chat semantics (same as topics data endpoint),
            // so dashboard termsToday/termsYesterday match "new entries categorized in terms".
            TermDayCount termCounts = countTermAssignmentsForDays(conn, widgets, today, yesterday, tableExistsCache);
            if (termCounts == null) {
                return new DashboardProgressMetrics(chatsToday, chatsYesterday, 0, 0);
            }

            return new DashboardProgressMetrics(
                    chatsToday,
                    chatsYesterday,
                    termCounts.getToday(),
                    termCounts.getYesterday()
            );
        } catch (SQLException e) {
            LOG.log(Level.FINE, LOG_BUILD_DASHBOARD_PROGRESS_FALLBACK, e);
            return ZERO_DASHBOARD_PROGRESS;
        }
    }

    private final List<TopTopic> buildTopTopicsTodayVsYesterday(List<WidgetEntry> widgets) {
        if (widgets == null || widgets.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, LOG_BUILD_TOP_TOPICS_TERM_LOAD_FAILED, e);
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
                counts.put(name, newTermDayCount());
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
                collectTopTopicDayCountsForWidget(
                        conn,
                        widget,
                        tableExistsCache,
                        startTs,
                        endTs,
                        today,
                        yesterday,
                        activeTerms,
                        compiledPatterns,
                        counts
                );
            }
        } catch (SQLException e) {
            LOG.log(Level.FINE, LOG_BUILD_TOP_TOPICS_FALLBACK, e);
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

    private final List<OtherParasoftEntry> buildLatestOtherParasoftEntries(List<WidgetEntry> widgets, int limit) {
        if (widgets == null || widgets.isEmpty() || limit <= 0) {
            return List.of();
        }

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, LOG_BUILD_OTHER_PARASOFT_TERM_LOAD_FAILED, e);
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
                collectOtherParasoftForWidget(conn, widget, tableExistsCache, compiledPatterns, all);
            }
        } catch (SQLException e) {
            LOG.log(Level.FINE, LOG_BUILD_OTHER_PARASOFT_FALLBACK, e);
            return List.of();
        }

        all.sort(Comparator.comparing(OtherParasoftEntry::getCreatedAt).reversed());
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    public final List<WidgetStat> collectWidgetStats(List<WidgetEntry> widgets) {
        return buildWidgetStats(widgets);
    }

    public final ProgressStat collectChatProgression(List<WidgetEntry> widgets) {
        return buildChatProgression(widgets);
    }

    public final ProgressStat collectNewUserProgression(List<WidgetEntry> widgets) {
        return buildNewUserProgression(widgets);
    }

    public final DashboardProgressMetrics collectDashboardProgressMetrics(List<WidgetEntry> widgets) {
        return buildDashboardProgressMetrics(widgets);
    }

    public final List<TopTopic> collectTopTopicsTodayVsYesterday(List<WidgetEntry> widgets) {
        return buildTopTopicsTodayVsYesterday(widgets);
    }

    public final List<OtherParasoftEntry> collectLatestOtherParasoftEntries(List<WidgetEntry> widgets, int limit) {
        return buildLatestOtherParasoftEntries(widgets, limit);
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        String sql = SqlLiteralConstants.SQL_SELECT_COUNT_FROM + DashboardDbUtil.quoteIdentifier(tableName);
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
            total += countChatsForWidgetOnDate(conn, widget, start, end, tableExistsCache);
        }
        return total;
    }

    private int countChatsForWidgetOnDate(
            Connection conn,
            WidgetEntry widget,
            Timestamp start,
            Timestamp end,
            Map<String, Boolean> tableExistsCache
    ) throws SQLException {
        if (isWidgetMissingId(widget)) {
            return 0;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
        if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
            return 0;
        }

        return countRowsBetween(conn, tableName, start, end);
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
        TermDayCount out = newTermDayCount();
        if (widgets == null || widgets.isEmpty()) {
            return out;
        }

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (IllegalStateException e) {
            LOG.log(Level.FINE, LOG_COUNT_TERM_ASSIGNMENTS_TERM_LOAD_FAILED, e);
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
            collectTermAssignmentsForWidget(
                    conn,
                    widget,
                    tableExistsCache,
                    startTs,
                    endTs,
                    today,
                    yesterday,
                    patterns,
                    out
            );
        }

        return out;
    }

    private TermDayCount newTermDayCount() {
        return TERM_DAY_COUNT_FACTORY.get();
    }

    private void collectTopTopicDayCountsForWidget(
            Connection conn,
            WidgetEntry widget,
            Map<String, Boolean> tableExistsCache,
            Timestamp startTs,
            Timestamp endTs,
            LocalDate today,
            LocalDate yesterday,
            List<TermDefinition> activeTerms,
            List<Pattern> compiledPatterns,
            Map<String, TermDayCount> counts
    ) throws SQLException {
        if (isWidgetMissingId(widget)) {
            return;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
        if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
            return;
        }

        String sql = SQL_SELECT_PROMPT_CREATED_PREFIX + DashboardDbUtil.quoteIdentifier(tableName)
                + SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, startTs);
            ps.setTimestamp(2, endTs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                    if (createdAt == null) {
                        continue;
                    }

                    LocalDate day = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (!day.equals(today) && !day.equals(yesterday)) {
                        continue;
                    }

                    String label = resolveBestMatchingLabel(
                            rs.getString(SqlLiteralConstants.COL_PROMPT),
                            activeTerms,
                            compiledPatterns);
                    if (label == null || label.isBlank() || OTHER_PARASOFT_LABEL.equalsIgnoreCase(label)) {
                        continue;
                    }

                    TermDayCount count = counts.get(label);
                    if (count == null) {
                        continue;
                    }
                    if (day.equals(today)) {
                        count.incToday();
                    } else {
                        count.incYesterday();
                    }
                }
            }
        }
    }

    private String resolveBestMatchingLabel(String promptRaw, List<TermDefinition> activeTerms, List<Pattern> compiledPatterns) {
        String sanitizedPrompt = TextSanitizer.sanitizeForMatching(promptRaw == null ? EMPTY : promptRaw);
        int bestStart = Integer.MAX_VALUE;
        String bestTermName = null;

        int bound = Math.min(activeTerms.size(), compiledPatterns.size());
        for (int i = 0; i < bound; i++) {
            int candidateStart = findPatternStart(compiledPatterns.get(i), sanitizedPrompt);
            if (candidateStart >= bestStart) {
                continue;
            }

            bestStart = candidateStart;
            TermDefinition term = activeTerms.get(i);
            bestTermName = term == null ? null : term.getName();
            if (bestStart == 0) {
                break;
            }
        }

        return bestTermName;
    }

    private int findPatternStart(Pattern pattern, String text) {
        if (pattern == null) {
            return Integer.MAX_VALUE;
        }

        try {
            Matcher matcher = pattern.matcher(text);
            return matcher.find() ? matcher.start() : Integer.MAX_VALUE;
        } catch (IllegalStateException ex) {
            LOG.log(Level.FINE, LOG_TOPIC_PATTERN_EVALUATION_FAILED, ex);
            return Integer.MAX_VALUE;
        }
    }

    private void collectOtherParasoftForWidget(
            Connection conn,
            WidgetEntry widget,
            Map<String, Boolean> tableExistsCache,
            List<Pattern> compiledPatterns,
            List<OtherParasoftEntry> out
    ) throws SQLException {
        if (isWidgetMissingId(widget)) {
            return;
        }

        String widgetId = widget.getWidgetId();
        String widgetName = (widget.getDisplayName() == null || widget.getDisplayName().isBlank())
                ? widgetId
                : widget.getDisplayName();

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
            return;
        }

        String sql = SQL_SELECT_PROMPT_SESSION_CREATED_PREFIX
                + DashboardDbUtil.quoteIdentifier(tableName)
                + SQL_ORDER_BY_CREATED_DESC_LIMIT_500;

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String prompt = rs.getString(SqlLiteralConstants.COL_PROMPT);
                Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                if (createdAt == null) {
                    continue;
                }
                if (matchesAnyPattern(prompt, compiledPatterns)) {
                    continue;
                }

                String sessionId = rs.getString(SqlLiteralConstants.COL_SESSION_ID);
                out.add(new OtherParasoftEntry(
                        widgetId,
                        widgetName,
                        prompt == null ? EMPTY : prompt,
                        sessionId == null ? EMPTY : sessionId,
                        createdAt
                ));
            }
        }
    }

    private boolean matchesAnyPattern(String promptRaw, List<Pattern> patterns) {
        String sanitized = TextSanitizer.sanitizeForMatching(promptRaw == null ? EMPTY : promptRaw);
        for (Pattern pattern : patterns) {
            try {
                Matcher matcher = pattern.matcher(sanitized);
                if (matcher.find()) {
                    return true;
                }
            } catch (IllegalStateException ex) {
                LOG.log(Level.FINE, LOG_OTHER_PARASOFT_MATCHER_EVALUATION_FAILED, ex);
            }
        }
        return false;
    }

    private void collectTermAssignmentsForWidget(
            Connection conn,
            WidgetEntry widget,
            Map<String, Boolean> tableExistsCache,
            Timestamp startTs,
            Timestamp endTs,
            LocalDate today,
            LocalDate yesterday,
            List<Pattern> patterns,
            TermDayCount out
    ) throws SQLException {
        if (isWidgetMissingId(widget)) {
            return;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
        if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
            return;
        }

        String sql = SQL_SELECT_PROMPT_CREATED_PREFIX + DashboardDbUtil.quoteIdentifier(tableName)
                + SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, startTs);
            ps.setTimestamp(2, endTs);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
                    if (createdAt == null) {
                        continue;
                    }

                    LocalDate day = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (!day.equals(today) && !day.equals(yesterday)) {
                        continue;
                    }

                    if (!matchesAnyPattern(rs.getString(SqlLiteralConstants.COL_PROMPT), patterns)) {
                        continue;
                    }

                    if (day.equals(today)) {
                        out.incToday();
                    } else {
                        out.incYesterday();
                    }
                }
            }
        }
    }

    private boolean isWidgetMissingId(WidgetEntry widget) {
        return widget == null || widget.getWidgetId() == null;
    }

    private int countDistinctSessionsFirstSeenOnDate(Connection conn, List<WidgetEntry> widgets, LocalDate date) throws SQLException {
        if (date == null || widgets == null || widgets.isEmpty()) {
            return 0;
        }

        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
        Map<String, Timestamp> earliestBySession = new LinkedHashMap<>();

        for (WidgetEntry widget : widgets) {
            collectEarliestBySession(conn, widget, tableExistsCache, earliestBySession);
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

    private void collectEarliestBySession(
            Connection conn,
            WidgetEntry widget,
            Map<String, Boolean> tableExistsCache,
            Map<String, Timestamp> earliestBySession
    ) throws SQLException {
        if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
            return;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
        if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
            return;
        }

        String sql = SQL_SELECT_SESSION_FIRST_SEEN_PREFIX
                + DashboardDbUtil.quoteIdentifier(tableName)
                + SqlLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mergeEarliestSession(
                        earliestBySession,
                        rs.getString(SqlLiteralConstants.COL_SESSION_ID),
                        SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_FIRST_SEEN));
            }
        }
    }

    private void mergeEarliestSession(Map<String, Timestamp> earliestBySession, String sessionId, Timestamp firstSeen) {
        if (sessionId == null || sessionId.isBlank() || firstSeen == null) {
            return;
        }

        String normalized = sessionId.trim();
        Timestamp previous = earliestBySession.get(normalized);
        if (previous == null || firstSeen.before(previous)) {
            earliestBySession.put(normalized, firstSeen);
        }
    }

    private int countRowsBetween(Connection conn, String tableName, Timestamp start, Timestamp end) throws SQLException {
        String sql = SqlLiteralConstants.SQL_SELECT_COUNT_FROM + DashboardDbUtil.quoteIdentifier(tableName)
            + SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}

