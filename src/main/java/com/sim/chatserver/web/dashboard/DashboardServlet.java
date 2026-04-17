package com.sim.chatserver.web.dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard.html";
    private static final String TERM_SNAPSHOT_SESSION_KEY = "termDistributionSnapshots";
    private static final int DEFAULT_RANGE_DAYS = 14;
    private static final int DEFAULT_ACTIVE_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Object TEMPLATE_LOCK = new Object();
    private static volatile String cachedDashboardTemplate;

    private static final long TABLE_EXISTS_TTL_MILLIS = Duration.ofMinutes(5).toMillis();
    private static final Object TABLE_CACHE_LOCK = new Object();
    private static final Map<String, CacheValue<Boolean>> GLOBAL_TABLE_EXISTS_CACHE = new LinkedHashMap<>();

    private static final long WIDGET_STATS_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long TERM_SUMMARY_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long SESSION_OVERVIEW_TTL_MILLIS = Duration.ofSeconds(20).toMillis();

    private static final Object WIDGET_CACHE_LOCK = new Object();
    private static volatile CacheValue<List<WidgetStat>> widgetStatsCache;

    private static final Object TERM_CACHE_LOCK = new Object();
    private static volatile CacheValue<TermSummary> termSummaryCache;

    private static final Object SESSION_CACHE_LOCK = new Object();
    private static final Map<String, CacheValue<SessionOverview>> sessionOverviewCache = new LinkedHashMap<>();

    private static final ExecutorService DASHBOARD_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())),
            new DashboardThreadFactory()
    );

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    TermsStore termsStore;

    @Override
    public void destroy() {
        DASHBOARD_EXECUTOR.shutdown();
        try {
            if (!DASHBOARD_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                DASHBOARD_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            DASHBOARD_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        String adminLink = "ADMIN".equalsIgnoreCase(role)
                ? "<p><a href=\"" + req.getContextPath() + "/admin\">Go to Admin Configuration</a></p>"
                : "";

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard", e);
        }

        LocalDate rangeEnd = parseLocalDate(req.getParameter("rangeEnd"))
                .orElse(LocalDate.now(ZoneId.systemDefault()));
        LocalDate rangeStart = parseLocalDate(req.getParameter("rangeStart"))
                .orElse(rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1));
        if (rangeStart.isAfter(rangeEnd)) {
            rangeStart = rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1);
        }

        final int activeDays = DEFAULT_ACTIVE_DAYS;
        final List<WidgetEntry> widgetsFinal = widgets;
        final LocalDate rangeStartFinal = rangeStart;
        final LocalDate rangeEndFinal = rangeEnd;

        CompletableFuture<List<WidgetStat>> widgetStatsFuture = CompletableFuture.supplyAsync(
                () -> getWidgetStatsCached(widgetsFinal), DASHBOARD_EXECUTOR);

        CompletableFuture<TermSummary> termSummaryFuture = CompletableFuture.supplyAsync(
                () -> getTermSummaryCached(widgetsFinal), DASHBOARD_EXECUTOR);

        CompletableFuture<SessionOverview> sessionOverviewFuture = CompletableFuture.supplyAsync(
                () -> getSessionOverviewCached(widgetsFinal, rangeStartFinal, rangeEndFinal, activeDays), DASHBOARD_EXECUTOR);

        List<WidgetStat> widgetStats = safeJoin(widgetStatsFuture, List.of(), "widget stats");
        int totalChats = widgetStats.stream().mapToInt(stat -> stat.count).sum();
        String statsRows = renderWidgetStatsRows(widgetStats, req.getContextPath());
        String widgetPieChartData = buildWidgetPieChartData(widgetStats);

        TermSummary summary = safeJoin(termSummaryFuture, null, "term summary");
        String termChartJson = summary == null ? "[]" : summary.toJson();
        if (summary != null) {
            storeTermSnapshots(session, summary);
        } else {
            session.removeAttribute(TERM_SNAPSHOT_SESSION_KEY);
        }

        String sessionRows = "<tr><td colspan=\"4\" class=\"empty-row\">No session activity available.</td></tr>";
        String sessionChartJson = buildEmptySessionPayload(rangeStart, rangeEnd);

        int totalUsers = 0;
        int activeUsers = 0;
        int inactiveUsers = 0;

        SessionOverview sessionOverview = safeJoin(sessionOverviewFuture, null, "session overview");
        if (sessionOverview != null) {
            Map<String, SessionLabelStore.SessionLabel> sessionLabels = loadSessionLabels(sessionOverview.topSessions);
            sessionRows = renderSessionRows(sessionOverview.topSessions, sessionLabels, req.getContextPath());
            sessionChartJson = buildSessionChartPayload(sessionOverview, rangeStart, rangeEnd);
            totalUsers = sessionOverview.totalUsers;
            activeUsers = sessionOverview.activeUsers;
            inactiveUsers = sessionOverview.inactiveUsers;
        } else {
            sessionRows = "<tr><td colspan=\"4\" class=\"empty-row\">Unable to load session activity.</td></tr>";
        }

        String template = loadTemplateCached(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));

        String rendered = renderTemplate(template, Map.ofEntries(
                Map.entry("user", escapeHtml(userName)),
                Map.entry("contextPath", req.getContextPath()),
                Map.entry("role", escapeHtml(role)),
                Map.entry("adminLink", adminLink),
                Map.entry("totalChats", escapeHtml(String.valueOf(totalChats))),
                Map.entry("widgetStatsRows", statsRows),
                Map.entry("widgetPieChartData", escapeForJs(widgetPieChartData)),
                Map.entry("termChartData", termChartJson),
                Map.entry("sessionRows", sessionRows),
                Map.entry("sessionRangeStart", escapeHtml(rangeStart.format(DATE_FORMATTER))),
                Map.entry("sessionRangeEnd", escapeHtml(rangeEnd.format(DATE_FORMATTER))),
                Map.entry("sessionChartData", escapeForJs(sessionChartJson)),
                Map.entry("activeDays", escapeHtml(String.valueOf(activeDays))),
                Map.entry("totalUsers", escapeHtml(String.valueOf(totalUsers))),
                Map.entry("activeUsers", escapeHtml(String.valueOf(activeUsers))),
                Map.entry("inactiveUsers", escapeHtml(String.valueOf(inactiveUsers))),
                Map.entry("activeUsersUrl", req.getContextPath() + "/dashboard/sessions?activity=active&activeDays=" + activeDays)
        ));

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private List<WidgetStat> getWidgetStatsCached(List<WidgetEntry> widgets) {
        long now = System.currentTimeMillis();
        CacheValue<List<WidgetStat>> cached = widgetStatsCache;
        if (cached != null && !cached.isExpired(now)) {
            return cached.value;
        }
        synchronized (WIDGET_CACHE_LOCK) {
            cached = widgetStatsCache;
            if (cached != null && !cached.isExpired(now)) {
                return cached.value;
            }
            List<WidgetStat> fresh = buildWidgetStats(widgets);
            widgetStatsCache = new CacheValue<>(fresh, now + WIDGET_STATS_TTL_MILLIS);
            return fresh;
        }
    }

    private TermSummary getTermSummaryCached(List<WidgetEntry> widgets) {
        long now = System.currentTimeMillis();
        CacheValue<TermSummary> cached = termSummaryCache;
        if (cached != null && !cached.isExpired(now)) {
            return cached.value;
        }
        synchronized (TERM_CACHE_LOCK) {
            cached = termSummaryCache;
            if (cached != null && !cached.isExpired(now)) {
                return cached.value;
            }
            try (Connection conn = dsHolder.getDataSource().getConnection()) {
                List<TermDefinition> terms = termsStore.listAll();
                TermSummary fresh = buildTermSummary(conn, widgets, terms);
                termSummaryCache = new CacheValue<>(fresh, now + TERM_SUMMARY_TTL_MILLIS);
                return fresh;
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to compute term summaries", e);
                return null;
            }
        }
    }

    private SessionOverview getSessionOverviewCached(List<WidgetEntry> widgets, LocalDate rangeStart, LocalDate rangeEnd, int activeDays) {
        String key = rangeStart + "|" + rangeEnd + "|" + activeDays;
        long now = System.currentTimeMillis();

        synchronized (SESSION_CACHE_LOCK) {
            CacheValue<SessionOverview> cached = sessionOverviewCache.get(key);
            if (cached != null && !cached.isExpired(now)) {
                return cached.value;
            }
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            SessionOverview fresh = buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);
            synchronized (SESSION_CACHE_LOCK) {
                sessionOverviewCache.put(key, new CacheValue<>(fresh, now + SESSION_OVERVIEW_TTL_MILLIS));
                if (sessionOverviewCache.size() > 32) {
                    String oldest = sessionOverviewCache.keySet().iterator().next();
                    sessionOverviewCache.remove(oldest);
                }
            }
            return fresh;
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to compute session metrics", e);
            return null;
        }
    }

    private <T> T safeJoin(CompletableFuture<T> future, T fallback, String label) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            log.log(Level.WARNING, "Failed to compute " + label, ex.getCause() == null ? ex : ex.getCause());
            return fallback;
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to compute " + label, ex);
            return fallback;
        }
    }

    private String buildWidgetPieChartData(List<WidgetStat> stats) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        if (stats != null) {
            for (WidgetStat stat : stats) {
                arr.add(Json.createObjectBuilder()
                        .add("widgetId", stat.widgetId == null ? "" : stat.widgetId)
                        .add("label", stat.label == null ? "" : stat.label)
                        .add("count", stat.count));
            }
        }
        return arr.build().toString();
    }

    private Map<String, SessionLabelStore.SessionLabel> loadSessionLabels(List<SessionStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }
        Set<String> sessionIds = stats.stream()
                .map(stat -> stat.sessionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (sessionIds.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(sessionIds);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load session labels for dashboard", e);
            return Map.of();
        }
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, DATE_FORMATTER));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildSessionChartPayload(SessionOverview overview, LocalDate rangeStart, LocalDate rangeEnd) {
        JsonArrayBuilder labelBuilder = Json.createArrayBuilder();
        for (String label : overview.timeline.labels) {
            labelBuilder.add(label);
        }

        JsonArrayBuilder seriesBuilder = Json.createArrayBuilder();
        for (SessionStat session : overview.topSessions) {
            JsonArrayBuilder countsBuilder = Json.createArrayBuilder();
            List<Integer> values = overview.timeline.countsBySession.get(session.sessionId);
            if (values != null) {
                for (Integer value : values) {
                    countsBuilder.add(value == null ? 0 : value);
                }
            } else {
                for (int i = 0; i < overview.timeline.labels.size(); i++) {
                    countsBuilder.add(0);
                }
            }
            seriesBuilder.add(Json.createObjectBuilder()
                    .add("sessionId", session.sessionId)
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

    private String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        JsonObject payload = Json.createObjectBuilder()
                .add("labels", Json.createArrayBuilder())
                .add("series", Json.createArrayBuilder())
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build();
        return payload.toString();
    }

    private SessionOverview buildSessionOverview(Connection conn, List<WidgetEntry> widgets, LocalDate rangeStart, LocalDate rangeEnd, int activeDays) throws SQLException {
        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        Map<String, Boolean> tableExistsCache = new LinkedHashMap<>();

        if (widgets != null) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                if (!tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("session_id");
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }
                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.count += rs.getInt("total");
                        Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                        if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                            acc.lastEntry = lastEntry;
                        }
                    }
                }
            }
        }

        int totalUsers = accumulators.size();
        Instant cutoff = Instant.now().minus(activeDays, ChronoUnit.DAYS);

        int inactiveUsers = 0;
        for (SessionAccumulator acc : accumulators.values()) {
            if (acc == null) {
                continue;
            }
            if (acc.lastEntry != null && acc.lastEntry.toInstant().toEpochMilli() < cutoff.toEpochMilli()) {
                inactiveUsers++;
            }
        }
        int activeUsers = Math.max(0, totalUsers - inactiveUsers);

        final int limit = 10;
        PriorityQueue<Map.Entry<String, SessionAccumulator>> pq
                = new PriorityQueue<>(Comparator.comparingInt(e -> e.getValue().count));

        for (Map.Entry<String, SessionAccumulator> entry : accumulators.entrySet()) {
            if (pq.size() < limit) {
                pq.offer(entry);
            } else if (entry.getValue().count > pq.peek().getValue().count) {
                pq.poll();
                pq.offer(entry);
            }
        }

        List<Map.Entry<String, SessionAccumulator>> topEntries = new ArrayList<>(pq);
        topEntries.sort((a, b) -> Integer.compare(b.getValue().count, a.getValue().count));

        List<SessionStat> topSessions = new ArrayList<>(topEntries.size());
        for (Map.Entry<String, SessionAccumulator> entry : topEntries) {
            topSessions.add(new SessionStat(
                    entry.getKey(),
                    entry.getValue().count,
                    formatTimestamp(entry.getValue().lastEntry)));
        }

        List<String> sessionIds = topSessions.stream()
                .map(stat -> stat.sessionId)
                .collect(Collectors.toList());

        SessionTimeline timeline = buildSessionTimeline(conn, widgets, sessionIds, rangeStart, rangeEnd, tableExistsCache);
        return new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);
    }

    private String formatSessionDisplayLabel(String sessionId, Map<String, SessionLabelStore.SessionLabel> labels) {
        if (sessionId == null) {
            return "";
        }
        return SessionLabelStore.resolveDisplayLabel(sessionId, labels == null ? null : labels.get(sessionId));
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        return ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(ENTRY_FORMATTER);
    }

    private SessionTimeline buildSessionTimeline(Connection conn, List<WidgetEntry> widgets, List<String> sessionIds,
            LocalDate rangeStart, LocalDate rangeEnd,
            Map<String, Boolean> tableExistsCache) throws SQLException {
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
            countsBySession.put(sessionId, new ArrayList<>(Collections.nCopies(labels.size(), 0)));
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
            String tableName = sanitizeWidgetTableName(widget.getWidgetId());
            if (!tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

            String sql = "SELECT session_id, CAST(created_at AS DATE) AS day_value, COUNT(*) AS day_count FROM "
                    + quoteIdentifier(tableName)
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
                        String sessionId = rs.getString("session_id");
                        java.sql.Date daySql = rs.getDate("day_value");
                        int dayCount = rs.getInt("day_count");
                        if (sessionId == null || daySql == null) {
                            continue;
                        }
                        List<Integer> bucket = countsBySession.get(sessionId);
                        if (bucket == null) {
                            continue;
                        }
                        LocalDate entryDate = daySql.toLocalDate();
                        long dayIndex = ChronoUnit.DAYS.between(rangeStart, entryDate);
                        if (dayIndex < 0 || dayIndex >= bucket.size()) {
                            continue;
                        }
                        int position = (int) dayIndex;
                        bucket.set(position, bucket.get(position) + dayCount);
                    }
                }
            }
        }

        return new SessionTimeline(labels, countsBySession);
    }

    private String renderSessionRows(List<SessionStat> stats, Map<String, SessionLabelStore.SessionLabel> labels, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"empty-row\">No session activity recorded yet.</td></tr>";
        }
        StringBuilder builder = new StringBuilder(Math.max(256, stats.size() * 180));
        int rank = 1;
        for (SessionStat stat : stats) {
            String display = formatSessionDisplayLabel(stat.sessionId, labels);
            String encodedSession = URLEncoder.encode(stat.sessionId, StandardCharsets.UTF_8);
            String url = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + encodedSession;
            builder.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td><span class=\"session-link\">").append(escapeHtml(display)).append("</span>");
            if (!stat.sessionId.equals(display)) {
                builder.append("<div class=\"session-id-muted\">").append(escapeHtml(stat.sessionId)).append("</div>");
            }
            builder.append("</td>")
                    .append("<td><a class=\"session-count-link\" href=\"").append(url).append("\">")
                    .append(stat.count).append(" chats</a></td>")
                    .append("<td><span class=\"session-last-entry\">").append(escapeHtml(stat.lastEntry)).append("</span></td>")
                    .append("</tr>");
        }
        return builder.toString();
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void storeTermSnapshots(HttpSession session, TermSummary summary) {
        session.setAttribute(TERM_SNAPSHOT_SESSION_KEY, summary.copyTermSnapshots());
    }

    private String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return "<tr><td colspan=\"2\" class=\"empty-row\">No widget chats available.</td></tr>";
        }
        StringBuilder builder = new StringBuilder(Math.max(256, stats.size() * 120));
        for (WidgetStat stat : stats) {
            String widgetUrl = contextPath + "/dashboard/widgets/view?widgetId="
                    + URLEncoder.encode(stat.widgetId, StandardCharsets.UTF_8);

            builder.append("<tr>")
                    .append("<td>").append(escapeHtml(stat.label)).append("</td>")
                    .append("<td>")
                    .append("<button type=\"button\" class=\"ghost-btn\" ")
                    .append("onclick=\"window.location.href='").append(escapeHtml(widgetUrl)).append("'\">")
                    .append(escapeHtml(String.valueOf(stat.count))).append(" chats")
                    .append("</button>")
                    .append("</td>")
                    .append("</tr>");
        }
        return builder.toString();
    }

    private List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        List<WidgetStat> stats = new ArrayList<>();
        if (widgets == null || widgets.isEmpty()) {
            return stats;
        }
        Map<String, Boolean> tableExistsCache = new LinkedHashMap<>();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String displayName = widget.getDisplayName();
                displayName = displayName == null || displayName.isBlank() ? widgetId : displayName;
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }
                int count = countRows(conn, tableName);
                stats.add(new WidgetStat(widgetId, displayName, count));
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to query widget tables", e);
        }
        return stats;
    }

    private TermSummary buildTermSummary(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        TermSummary summary = new TermSummary();
        if (widgets == null || widgets.isEmpty() || terms == null) {
            return summary;
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : terms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(TermMatcher.buildStrictPattern(term));
            summary.ensureTerm(term.getName());
        }

        String otherLabel = "Other Parasoft Match";
        summary.ensureTerm(otherLabel);

        Map<String, Boolean> tableExistsCache = new LinkedHashMap<>();

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }

            String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM " + quoteIdentifier(tableName);
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chatId = rs.getString("widget_chat_id");
                    if (chatId == null) {
                        chatId = "";
                    }
                    String prompt = rs.getString("prompt");
                    if (prompt == null) {
                        prompt = "";
                    }
                    String response = rs.getString("response_text");
                    Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                    String sessionId = rs.getString("session_id");

                    final String sanitizedPrompt = TextSanitizer.sanitizeForMatching(prompt);
                    TermDefinition bestTerm = null;
                    int bestStart = Integer.MAX_VALUE;

                    for (int i = 0; i < compiledPatterns.size(); i++) {
                        Pattern pattern = compiledPatterns.get(i);
                        try {
                            Matcher m = pattern.matcher(sanitizedPrompt);
                            if (m.find()) {
                                int start = m.start();
                                if (start < bestStart) {
                                    bestStart = start;
                                    bestTerm = activeTerms.get(i);
                                    if (bestStart == 0) {
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warning("Pattern match failure for term '" + activeTerms.get(i).getName() + "': " + e.getMessage());
                        }
                    }

                    String snapshotTerm = bestTerm != null ? bestTerm.getName() : otherLabel;

                    TermChatSnapshot snapshot = new TermChatSnapshot(
                            snapshotTerm,
                            widgetId,
                            chatId,
                            prompt,
                            response,
                            createdAt,
                            sessionId
                    );
                    summary.recordMatch(snapshotTerm, snapshot);
                }
            }
        }
        return summary;
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tableExistsCached(Connection conn, String tableName, Map<String, Boolean> requestCache) throws SQLException {
        Boolean req = requestCache.get(tableName);
        if (req != null) {
            return req;
        }

        long now = System.currentTimeMillis();
        String key = conn.getCatalog() + "|" + tableName;
        CacheValue<Boolean> global;
        synchronized (TABLE_CACHE_LOCK) {
            global = GLOBAL_TABLE_EXISTS_CACHE.get(key);
            if (global != null && !global.isExpired(now)) {
                requestCache.put(tableName, global.value);
                return global.value;
            }
        }

        boolean exists = tableExists(conn, tableName);
        synchronized (TABLE_CACHE_LOCK) {
            GLOBAL_TABLE_EXISTS_CACHE.put(key, new CacheValue<>(exists, now + TABLE_EXISTS_TTL_MILLIS));
            if (GLOBAL_TABLE_EXISTS_CACHE.size() > 512) {
                String oldest = GLOBAL_TABLE_EXISTS_CACHE.keySet().iterator().next();
                GLOBAL_TABLE_EXISTS_CACHE.remove(oldest);
            }
        }

        requestCache.put(tableName, exists);
        return exists;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }

        String trimmed = widgetId.trim();
        StringBuilder sb = new StringBuilder(Math.min(trimmed.length() + 2, 64));
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }

        String normalized = sb.length() == 0 ? "widget" : sb.toString();
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String loadTemplateCached(ServletContext context, String path) throws IOException {
        String local = cachedDashboardTemplate;
        if (local != null) {
            return local;
        }
        synchronized (TEMPLATE_LOCK) {
            if (cachedDashboardTemplate == null) {
                cachedDashboardTemplate = loadTemplate(context, path);
            }
            return cachedDashboardTemplate;
        }
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String renderTemplate(String template, Map<String, String> values) {
        String out = template;
        for (Map.Entry<String, String> e : values.entrySet()) {
            out = out.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    private static final class WidgetStat {

        private final String widgetId;
        private final String label;
        private final int count;

        private WidgetStat(String widgetId, String label, int count) {
            this.widgetId = widgetId;
            this.label = label;
            this.count = count;
        }
    }

    private static final class SessionOverview {

        private final List<SessionStat> topSessions;
        private final SessionTimeline timeline;
        private final int totalUsers;
        private final int activeUsers;
        private final int inactiveUsers;
        private final int activeDays;

        private SessionOverview(List<SessionStat> topSessions, SessionTimeline timeline,
                int totalUsers, int activeUsers, int inactiveUsers, int activeDays) {
            this.topSessions = List.copyOf(topSessions);
            this.timeline = timeline;
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.activeDays = activeDays;
        }
    }

    private static final class SessionStat {

        private final String sessionId;
        private final int count;
        private final String lastEntry;

        private SessionStat(String sessionId, int count, String lastEntry) {
            this.sessionId = sessionId;
            this.count = count;
            this.lastEntry = lastEntry;
        }
    }

    private static final class SessionTimeline {

        private final List<String> labels;
        private final Map<String, List<Integer>> countsBySession;

        private SessionTimeline(List<String> labels, Map<String, List<Integer>> countsBySession) {
            this.labels = List.copyOf(labels);
            this.countsBySession = new LinkedHashMap<>(countsBySession);
        }
    }

    private static final class SessionAccumulator {

        private int count = 0;
        private Timestamp lastEntry = null;
    }

    private static final class TermSummary {

        private final Map<String, Integer> termCounts = new LinkedHashMap<>();
        private final Map<String, List<TermChatSnapshot>> termSnapshots = new LinkedHashMap<>();

        private void ensureTerm(String termName) {
            termCounts.putIfAbsent(termName, 0);
            termSnapshots.putIfAbsent(termName, new ArrayList<>());
        }

        private void recordMatch(String termName, TermChatSnapshot snapshot) {
            termCounts.merge(termName, 1, Integer::sum);
            termSnapshots.computeIfAbsent(termName, k -> new ArrayList<>()).add(snapshot);
        }

        private String toJson() {
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
                builder.add(Json.createObjectBuilder()
                        .add("label", entry.getKey())
                        .add("count", entry.getValue())
                        .add("term", entry.getKey()));
            }
            return builder.build().toString();
        }

        private Map<String, List<TermChatSnapshot>> copyTermSnapshots() {
            Map<String, List<TermChatSnapshot>> copies = new LinkedHashMap<>();
            for (Map.Entry<String, List<TermChatSnapshot>> entry : termSnapshots.entrySet()) {
                copies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copies;
        }
    }

    private static final class CacheValue<T> {

        private final T value;
        private final long expiresAt;

        private CacheValue(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }

    private static final class DashboardThreadFactory implements ThreadFactory {

        private int idx = 1;

        @Override
        public synchronized Thread newThread(Runnable r) {
            Thread t = new Thread(r, "dashboard-worker-" + (idx++));
            t.setDaemon(true);
            return t;
        }
    }
}
