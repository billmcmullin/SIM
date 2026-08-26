package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
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
import java.util.stream.Collectors;

import com.sim.chatserver.web.dashboard.DashboardLocalViewModels.ProgressStat;
import com.sim.chatserver.web.dashboard.DashboardLocalViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.SessionAccumulator;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
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
    private static final String TERM_INCREASE_SNAPSHOT_SESSION_KEY = "termDistributionIncreaseSnapshots";
    private static final String TERM_YESTERDAY_SNAPSHOT_SESSION_KEY = "termDistributionYesterdaySnapshots";

    private static final int DEFAULT_RANGE_DAYS = 14;
    private static final int DEFAULT_ACTIVE_DAYS = 7;
    private static final int TOP_TOPIC_LIMIT = 3;
    private static final int OTHER_PARASOFT_LATEST_LIMIT = 5;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ExecutorService DASHBOARD_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())),
            new DashboardThreadFactory()
    );

        private static final DashboardCacheRegistry cacheRegistry = new DashboardCacheRegistry();
    @Override
    public void destroy() {
        DASHBOARD_EXECUTOR.shutdown();
        try {
            if (!DASHBOARD_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                DASHBOARD_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.log(Level.FINE, "Dashboard executor shutdown interrupted", e);
            DASHBOARD_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String contextPath = ServletPathUtil.safeContextPathStrict(req.getServletContext().getContextPath());
        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        String adminButtonStyle = isAdmin ? "" : "display:none;";
        String adminHref = contextPath + "/admin";

        String infoMessageHtml = buildInfoMessageHtml(ServletRequestParamUtil.firstParam(req, "msg", 256, true, true));

        List<WidgetEntry> widgets = loadWidgets();

        LocalDate rangeEnd = parseLocalDate(ServletRequestParamUtil.firstParam(req, "rangeEnd", 256, true, true))
                .orElse(LocalDate.now(ZoneId.systemDefault()));
        LocalDate rangeStart = parseLocalDate(ServletRequestParamUtil.firstParam(req, "rangeStart", 256, true, true))
                .orElse(rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1));
        if (rangeStart.isAfter(rangeEnd)) {
            rangeStart = rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1);
        }

        final int activeDays = DEFAULT_ACTIVE_DAYS;
        final List<WidgetEntry> widgetsFinal = widgets;
        final LocalDate rangeStartFinal = rangeStart;
        final LocalDate rangeEndFinal = rangeEnd;

        final LocalDate dayToday = rangeEndFinal;
        final LocalDate dayYesterday = dayToday.minusDays(1);

        DashboardMetricsService metricsService = DashboardMetricsService.create(dataSourceHolder(), termsStore(), TOP_TOPIC_LIMIT);
        DashboardTermService termService = dashboardTermService();

        CompletableFuture<List<WidgetStat>> widgetStatsFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getWidgetStats(() -> metricsService.buildWidgetStats(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(List.of(), 1200, TimeUnit.MILLISECONDS);

        CompletableFuture<ProgressStat> chatProgressionFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getChatProgression(() -> metricsService.buildChatProgression(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(new ProgressStat(0, 0), 900, TimeUnit.MILLISECONDS);

        CompletableFuture<ProgressStat> newUserProgressionFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getNewUserProgression(() -> metricsService.buildNewUserProgression(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(new ProgressStat(0, 0), 900, TimeUnit.MILLISECONDS);

        CompletableFuture<List<OtherParasoftEntry>> otherParasoftFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getOtherParasoftLatest(
                        () -> metricsService.buildLatestOtherParasoftEntries(widgetsFinal, OTHER_PARASOFT_LATEST_LIMIT)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(List.of(), 800, TimeUnit.MILLISECONDS);

        CompletableFuture<List<TopTopic>> topTopicsFuture = CompletableFuture.supplyAsync(
            () -> cacheRegistry.getTopTopics(() -> metricsService.buildTopTopicsTodayVsYesterday(widgetsFinal)),
            DASHBOARD_EXECUTOR
        ).completeOnTimeout(List.of(), 800, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> termSummaryFuture = CompletableFuture.supplyAsync(
            () -> cacheRegistry.getTermSummary(() -> loadTermSummary(termService, widgetsFinal, rangeStartFinal, rangeEndFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 1100, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> todayTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> loadTermSummary(termService, widgetsFinal, dayToday, dayToday),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 450, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> yesterdayTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> loadTermSummary(termService, widgetsFinal, dayYesterday, dayYesterday),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 450, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> allTimeTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> loadTermSummary(termService, widgetsFinal, LocalDate.of(1970, 1, 1), rangeEndFinal),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 550, TimeUnit.MILLISECONDS);

        CompletableFuture<SessionOverview> sessionOverviewFuture = CompletableFuture.supplyAsync(
                () -> {
                String key = new StringBuilder(48)
                    .append(rangeStartFinal)
                    .append('|')
                    .append(rangeEndFinal)
                    .append('|')
                    .append(activeDays)
                    .toString();
                    return cacheRegistry.getSessionOverview(key,
                            () -> loadSessionOverview(widgetsFinal, rangeStartFinal, rangeEndFinal, activeDays));
                },
                DASHBOARD_EXECUTOR
                    ).completeOnTimeout(null, 900, TimeUnit.MILLISECONDS);

        CompletableFuture<String> lastFiveDaysTrendFuture = CompletableFuture.supplyAsync(
                    () -> buildLastFiveDaysTrendJson(widgetsFinal),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout("{\"labels\":[],\"values\":[],\"days\":5}", 700, TimeUnit.MILLISECONDS);

        List<WidgetStat> widgetStats = safeJoin(widgetStatsFuture, List.of(), "widget stats");
        ProgressStat fallbackChatProgression = safeJoin(chatProgressionFuture, new ProgressStat(0, 0), "chat progression");
        ProgressStat newUserProgression = safeJoin(newUserProgressionFuture, new ProgressStat(0, 0), "new user progression");
        List<OtherParasoftEntry> otherParasoftLatest = safeJoin(otherParasoftFuture, List.of(), "other parasoft latest");
        List<TopTopic> dailyTopTopics = safeJoin(topTopicsFuture, List.of(), "top topics today vs yesterday");
        TermSummary termSummary = safeJoin(termSummaryFuture, null, "term summary");
        TermSummary todayTermSummary = safeJoin(todayTermSummaryFuture, null, "today term summary");
        TermSummary yesterdayTermSummary = safeJoin(yesterdayTermSummaryFuture, null, "yesterday term summary");
        TermSummary allTimeTermSummary = safeJoin(allTimeTermSummaryFuture, null, "all-time term summary");
        SessionOverview sessionOverview = safeJoin(sessionOverviewFuture, null, "session overview");
        String lastFiveDaysTrendJson = safeJoin(lastFiveDaysTrendFuture, "{\"labels\":[],\"values\":[],\"days\":5}", "last 5 days trend");

        int totalChats = widgetStats.stream().mapToInt(WidgetStat::getCount).sum();

        ProgressStat chatProgression = fallbackChatProgression;

        int todayChats = Math.max(0, chatProgression.getToday());
        int yesterdayChats = Math.max(0, chatProgression.getYesterday());

        int termsToday = sumTermCounts(todayTermSummary);
        int termsYesterday = sumTermCounts(yesterdayTermSummary);
        ProgressStat termsProgression = new ProgressStat(termsToday, termsYesterday);

        String widgetStatsRows = DashboardRowsRenderer.renderWidgetStatsRows(widgetStats, req.getContextPath());
        String dailyTopTermsRows = DashboardRowsRenderer.renderDailyTopTermsRows(
            dailyTopTopics,
                req.getContextPath()
        );
        String otherParasoftLatestRows = DashboardRowsRenderer.renderOtherParasoftLatestRows(otherParasoftLatest, req.getContextPath());

        String termChartJson = toChartJson(termSummary);

        if (allTimeTermSummary != null) {
            storeTermSnapshots(session, allTimeTermSummary);
        } else {
            session.removeAttribute(TERM_SNAPSHOT_SESSION_KEY);
        }

        Map<String, Integer> increaseMap = buildTermTotalMap(todayTermSummary);
        String termIncreaseMapJson = buildTermIncreaseMapJson(increaseMap);

        Map<String, Integer> totalMap = buildTermTotalMap(allTimeTermSummary);
        String termTotalMapJson = buildTermTotalMapJson(totalMap);

        Map<String, List<TermChatSnapshot>> increaseOnlySnapshots = copySnapshots(todayTermSummary);
        storeIncreaseSnapshots(session, increaseOnlySnapshots);

        // store strict yesterday-only snapshots for Top 3 Yesterday drilldown
        Map<String, List<TermChatSnapshot>> yesterdayOnlySnapshots = copySnapshots(yesterdayTermSummary);
        storeYesterdaySnapshots(session, yesterdayOnlySnapshots);

        String sessionRows;
        String sessionChartJson = buildEmptySessionPayload(rangeStart, rangeEnd);
        int totalUsers = 0;
        int activeUsers = 0;
        int inactiveUsers = 0;

        int newSessionsToday = 0;
        int newSessionsYesterday = 0;
        ProgressStat newSessionsProgression = new ProgressStat(0, 0);

        if (sessionOverview != null) {
            Map<String, SessionLabelStore.SessionLabel> labels = loadSessionLabels(sessionOverview.getTopSessions());
            sessionRows = DashboardRowsRenderer.renderSessionRows(sessionOverview.getTopSessions(), labels, contextPath);
            sessionChartJson = buildSessionChartPayload(sessionOverview, rangeStart, rangeEnd);

            totalUsers = sessionOverview.getTotalUsers();
            activeUsers = sessionOverview.getActiveUsers();
            inactiveUsers = sessionOverview.getInactiveUsers();

            newSessionsToday = sessionOverview.getNewSessionsToday();
            newSessionsYesterday = sessionOverview.getNewSessionsYesterday();
            newSessionsProgression = sessionOverview.getNewSessionsProgression() == null
                    ? new ProgressStat(newSessionsToday, newSessionsYesterday)
                    : sessionOverview.getNewSessionsProgression();
        } else {
            sessionRows = DashboardRowsRenderer.renderSessionRows(List.of(), Map.of(), contextPath);
        }

        String template = DashboardTemplateRenderer.loadTemplateCached(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));

        String rendered = DashboardTemplateRenderer.renderTemplate(template, Map.ofEntries(
                Map.entry("user", DashboardTemplateRenderer.escapeHtml(userName)),
                Map.entry("contextPath", contextPath),
                Map.entry("role", DashboardTemplateRenderer.escapeHtml(role)),
            Map.entry("adminButtonStyle", DashboardTemplateRenderer.escapeHtml(adminButtonStyle)),
            Map.entry("adminHref", DashboardTemplateRenderer.escapeHtml(adminHref)),
                Map.entry("dashboardInfoMessage", infoMessageHtml),
                Map.entry("totalChats", DashboardTemplateRenderer.escapeHtml(String.valueOf(totalChats))),
                Map.entry("todayChats", DashboardTemplateRenderer.escapeHtml(String.valueOf(todayChats))),
                Map.entry("yesterdayChats", DashboardTemplateRenderer.escapeHtml(String.valueOf(yesterdayChats))),
                Map.entry("chatProgression", formatProgressionHtml(chatProgression)),
                Map.entry("chatProgressionDirection", DashboardTemplateRenderer.escapeHtml(chatProgression.getDirection())),
                Map.entry("newUsersToday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newUserProgression.getToday()))),
                Map.entry("newUsersYesterday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newUserProgression.getYesterday()))),
                Map.entry("newUsersProgression", formatProgressionHtml(newUserProgression)),
                Map.entry("newUsersProgressionDirection", DashboardTemplateRenderer.escapeHtml(newUserProgression.getDirection())),
                Map.entry("termsToday", DashboardTemplateRenderer.escapeHtml(String.valueOf(termsToday))),
                Map.entry("termsYesterday", DashboardTemplateRenderer.escapeHtml(String.valueOf(termsYesterday))),
                Map.entry("termsProgression", formatProgressionHtml(termsProgression)),
                Map.entry("termsProgressionDirection", DashboardTemplateRenderer.escapeHtml(termsProgression.getDirection())),
                Map.entry("dailyTopTermsRows", dailyTopTermsRows),
                Map.entry("otherParasoftLatestRows", otherParasoftLatestRows),
                Map.entry("widgetStatsRows", widgetStatsRows),
                Map.entry("widgetPieChartData", escapeForJs(buildWidgetPieChartData(widgetStats))),
                Map.entry("termChartData", termChartJson),
                Map.entry("termIncreaseMapJson", escapeForJs(termIncreaseMapJson)),
                Map.entry("termTotalMapJson", escapeForJs(termTotalMapJson)),
                Map.entry("termLegendDefaultMode", "increase"),
                Map.entry("termIncreaseRangeStart", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreaseRangeEnd", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreasePrevRangeStart", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreasePrevRangeEnd", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("sessionRows", sessionRows),
                Map.entry("sessionRangeStart", DashboardTemplateRenderer.escapeHtml(rangeStart.format(DATE_FORMATTER))),
                Map.entry("sessionRangeEnd", DashboardTemplateRenderer.escapeHtml(rangeEnd.format(DATE_FORMATTER))),
                Map.entry("sessionChartData", escapeForJs(sessionChartJson)),
                Map.entry("activeDays", DashboardTemplateRenderer.escapeHtml(String.valueOf(activeDays))),
                Map.entry("totalUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(totalUsers))),
                Map.entry("activeUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(activeUsers))),
                Map.entry("inactiveUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(inactiveUsers))),
                Map.entry("activeUsersUrl", contextPath + "/dashboard/sessions?activity=active&activeDays=" + activeDays),
                Map.entry("sessionNewToday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newSessionsToday))),
                Map.entry("sessionNewYesterday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newSessionsYesterday))),
                Map.entry("sessionNewProgression", formatProgressionHtml(newSessionsProgression)),
                Map.entry("sessionNewProgressionDirection", DashboardTemplateRenderer.escapeHtml(newSessionsProgression.getDirection())),
                Map.entry("lastFiveDaysTrendData", escapeForJs(lastFiveDaysTrendJson))
        ));

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    
        } catch (IOException | ServletException | IllegalStateException | IllegalArgumentException | SecurityException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error.", ioe);
        }
    }

    private String buildInfoMessageHtml(String msg) {
        String m = msg == null ? "" : msg.trim();
        if ("noIncreaseForTerm".equalsIgnoreCase(m)) {
            return "<div class=\"dashboard-info-banner\" role=\"status\">No increased chats found for that term today.</div>";
        }
        if ("noYesterdayForTerm".equalsIgnoreCase(m)) {
            return "<div class=\"dashboard-info-banner\" role=\"status\">No chats found for that term yesterday.</div>";
        }
        return "";
    }

    private List<WidgetEntry> loadWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard", e);
            return List.of();
        }
    }


    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            log.log(Level.FINE, "Invalid local-date parameter for dashboard: {0}", sanitizeForLog(value));
            return Optional.empty();
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private DashboardTermService dashboardTermService() {
        return CDI.current().select(DashboardTermService.class).get();
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private TermSummary loadTermSummary(
            DashboardTermService termService,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        try (Connection conn = openConnectionSafe()) {
            List<TermDefinition> terms = termsStore().listAll();
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

    private SessionOverview loadSessionOverview(
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int activeDays
    ) {
        try (Connection conn = openConnectionSafe()) {
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

        ProgressStat activeUsersProgression = new ProgressStat(activeUsers, activeUsersYesterday);

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

    private String buildSessionChartPayload(SessionOverview overview, LocalDate rangeStart, LocalDate rangeEnd) {
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

    private String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        return Json.createObjectBuilder()
                .add("labels", Json.createArrayBuilder())
                .add("series", Json.createArrayBuilder())
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build()
                .toString();
    }

    private LocalDate readLocalDateColumn(ResultSet rs, String column) throws SQLException {
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

    private String buildLastFiveDaysTrendJson(List<WidgetEntry> widgets) {
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

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
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

    private Map<String, SessionLabelStore.SessionLabel> loadSessionLabels(List<SessionStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }

        Set<String> sessionIds = stats.stream()
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

    private void storeTermSnapshots(HttpSession session, TermSummary summary) {
        session.setAttribute(TERM_SNAPSHOT_SESSION_KEY, summary.copyTermSnapshots());
    }

    private void storeIncreaseSnapshots(HttpSession session, Map<String, List<TermChatSnapshot>> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            session.removeAttribute(TERM_INCREASE_SNAPSHOT_SESSION_KEY);
            return;
        }

        Map<String, List<TermChatSnapshot>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<TermChatSnapshot>> e : snapshots.entrySet()) {
            copy.put(e.getKey(), e.getValue() == null ? List.of() : new ArrayList<>(e.getValue()));
        }
        session.setAttribute(TERM_INCREASE_SNAPSHOT_SESSION_KEY, copy);
    }

    private void storeYesterdaySnapshots(HttpSession session, Map<String, List<TermChatSnapshot>> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            session.removeAttribute(TERM_YESTERDAY_SNAPSHOT_SESSION_KEY);
            return;
        }

        Map<String, List<TermChatSnapshot>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<TermChatSnapshot>> e : snapshots.entrySet()) {
            copy.put(e.getKey(), e.getValue() == null ? List.of() : new ArrayList<>(e.getValue()));
        }
        session.setAttribute(TERM_YESTERDAY_SNAPSHOT_SESSION_KEY, copy);
    }

    private Map<String, List<TermChatSnapshot>> copySnapshots(TermSummary summary) {
        Map<String, List<TermChatSnapshot>> out = new LinkedHashMap<>();
        if (summary == null) {
            return out;
        }

        for (Map.Entry<String, List<TermChatSnapshot>> e : summary.getTermSnapshots().entrySet()) {
            String term = e.getKey();
            if (term == null || term.isBlank()) {
                continue;
            }
            List<TermChatSnapshot> snaps = e.getValue() == null ? List.of() : e.getValue();
            out.put(term, new ArrayList<>(snaps));
        }
        return out;
    }

    private String buildWidgetPieChartData(List<WidgetStat> stats) {
        jakarta.json.JsonArrayBuilder arr = jakarta.json.Json.createArrayBuilder();
        if (stats != null) {
            for (WidgetStat stat : stats) {
                arr.add(jakarta.json.Json.createObjectBuilder()
                        .add("widgetId", stat.getWidgetId() == null ? "" : stat.getWidgetId())
                        .add("label", stat.getLabel() == null ? "" : stat.getLabel())
                        .add("count", stat.getCount()));
            }
        }
        return arr.build().toString();
    }

    private String toChartJson(TermSummary summary) {
        if (summary == null || summary.getTermCounts().isEmpty()) {
            return "[]";
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Map.Entry<String, Integer> entry : summary.getTermCounts().entrySet()) {
            String label = entry.getKey() == null ? "" : entry.getKey();
            Integer boxedCount = entry.getValue();
            int count = boxedCount == null ? 0 : boxedCount.intValue();

            builder.add(Json.createObjectBuilder()
                    .add("label", label)
                    .add("count", count)
                    .add("term", label));
        }
        return builder.build().toString();
    }

    private String formatProgressionHtml(ProgressStat p) {
        if (p == null) {
            return "0 (0.0%) vs yesterday";
        }

        int delta = p.getDelta();
        long roundedTenthPct = Math.round(p.getPctDelta() * 10.0d);
        boolean negative = roundedTenthPct < 0;
        long abs = Math.abs(roundedTenthPct);
        long whole = abs / 10L;
        long tenths = abs % 10L;

        StringBuilder textBuilder = new StringBuilder();
        if (delta >= 0) {
            textBuilder.append('+');
        }
        textBuilder.append(delta)
                .append(" (");
        if (negative) {
            textBuilder.append('-');
        }
        textBuilder.append(whole)
                .append('.')
                .append(tenths)
                .append("%) vs yesterday");

        String text = textBuilder.toString();
        return DashboardTemplateRenderer.escapeHtml(text);
    }

    private Map<String, Integer> buildTermTotalMap(TermSummary summary) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (summary == null) {
            return out;
        }
        for (Map.Entry<String, Integer> e : summary.getTermCounts().entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) {
                continue;
            }
            out.put(e.getKey(), normalizeNonNegativeInteger(e.getValue()));
        }
        return out;
    }

    private int sumTermCounts(TermSummary summary) {
        if (summary == null || summary.getTermCounts().isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Integer count : summary.getTermCounts().values()) {
            total += normalizeNonNegativeInteger(count).intValue();
        }
        return total;
    }

    private String buildTermIncreaseMapJson(Map<String, Integer> increaseMap) {
        JsonObjectBuilder obj = Json.createObjectBuilder();
        if (increaseMap != null) {
            for (Map.Entry<String, Integer> e : increaseMap.entrySet()) {
                String k = e.getKey();
                Integer safeValue = normalizeNonNegativeInteger(e.getValue());
                if (k != null) {
                    obj.add(k, safeValue.intValue());
                }
            }
        }
        return obj.build().toString();
    }

    private String buildTermTotalMapJson(Map<String, Integer> totalMap) {
        JsonObjectBuilder obj = Json.createObjectBuilder();
        if (totalMap != null) {
            for (Map.Entry<String, Integer> e : totalMap.entrySet()) {
                String k = e.getKey();
                Integer safeValue = normalizeNonNegativeInteger(e.getValue());
                if (k != null) {
                    obj.add(k, safeValue.intValue());
                }
            }
        }
        return obj.build().toString();
    }

    private static Integer normalizeNonNegativeInteger(Integer value) {
        if (value == null) {
            return Integer.valueOf(0);
        }
        return value.compareTo(Integer.valueOf(0)) < 0 ? Integer.valueOf(0) : value;
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\\') {
                out.append("\\\\");
            } else if (ch == '\'') {
                out.append("\\'");
            } else if (ch == '\n') {
                out.append("\\n");
            } else if (ch == '\r') {
                out.append("\\r");
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    private <T> T safeJoin(CompletableFuture<T> future, T fallback, String label) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            log.log(Level.WARNING, "Failed to compute " + label, cause == null ? ex : cause);
            return fallback;
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException ex) {
            log.log(Level.WARNING, "Failed to compute " + label, ex);
            return fallback;
        }
    }

    static final class DashboardThreadFactory implements ThreadFactory {

        private final ThreadFactory delegate = Executors.defaultThreadFactory();
        private int idx = 1;

        @Override
        public synchronized Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName("dashboard-worker-" + (idx++));
            t.setDaemon(true);
            return t;
        }
    }
}
