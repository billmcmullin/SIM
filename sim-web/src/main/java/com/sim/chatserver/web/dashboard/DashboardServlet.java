package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.render.DashboardRowsRenderer;
import com.sim.chatserver.service.dashboard.DashboardCacheRegistry;
import com.sim.chatserver.service.dashboard.DashboardMetricsService;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;
import com.sim.chatserver.service.dashboard.DashboardSessionService;
import com.sim.chatserver.service.dashboard.DashboardServletQueryService;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.util.SessionLabelStore;
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

    private static final ExecutorService DASHBOARD_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())),
            new DashboardThreadFactory()
    );

        private static final DashboardCacheRegistry cacheRegistry = new DashboardCacheRegistry();
    private final transient DashboardServletQueryService queryService = new DashboardServletQueryService(log);

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

        DashboardMetricsService metricsService = new DashboardMetricsService(dataSourceHolder(), termsStore(), TOP_TOPIC_LIMIT);
        DashboardTermService termService = new DashboardTermService(termsStore());
        DashboardSessionService sessionService = new DashboardSessionService();

        CompletableFuture<List<WidgetStat>> widgetStatsFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getWidgetStats(() -> metricsService.buildWidgetStats(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(List.of(), 1200, TimeUnit.MILLISECONDS);

        CompletableFuture<ProgressStat> chatProgressionFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getChatProgression(() -> metricsService.buildChatProgression(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(new ProgressStat(0, 0), 900, TimeUnit.MILLISECONDS);

        CompletableFuture<DashboardProgressMetrics> dashboardProgressFuture = CompletableFuture.supplyAsync(
                () -> cacheRegistry.getDashboardProgressMetrics(
                        () -> metricsService.buildDashboardProgressMetrics(widgetsFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(new DashboardProgressMetrics(0, 0, 0, 0), 900, TimeUnit.MILLISECONDS);

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
            () -> cacheRegistry.getTermSummary(() -> queryService.loadTermSummary(termService, widgetsFinal, rangeStartFinal, rangeEndFinal)),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 1100, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> todayTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> queryService.loadTermSummary(termService, widgetsFinal, dayToday, dayToday),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 450, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> yesterdayTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> queryService.loadTermSummary(termService, widgetsFinal, dayYesterday, dayYesterday),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout(null, 450, TimeUnit.MILLISECONDS);

        CompletableFuture<TermSummary> allTimeTermSummaryFuture = CompletableFuture.supplyAsync(
            () -> queryService.loadTermSummary(termService, widgetsFinal, LocalDate.of(1970, 1, 1), rangeEndFinal),
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
                            () -> queryService.loadSessionOverview(sessionService, widgetsFinal, rangeStartFinal, rangeEndFinal, activeDays));
                },
                DASHBOARD_EXECUTOR
                    ).completeOnTimeout(null, 900, TimeUnit.MILLISECONDS);

        CompletableFuture<String> lastFiveDaysTrendFuture = CompletableFuture.supplyAsync(
            () -> queryService.buildLastFiveDaysTrendJson(widgetsFinal),
                DASHBOARD_EXECUTOR
        ).completeOnTimeout("{\"labels\":[],\"values\":[],\"days\":5}", 700, TimeUnit.MILLISECONDS);

        List<WidgetStat> widgetStats = safeJoin(widgetStatsFuture, List.of(), "widget stats");
        ProgressStat fallbackChatProgression = safeJoin(chatProgressionFuture, new ProgressStat(0, 0), "chat progression");
        DashboardProgressMetrics dashboardProgress = safeJoin(
                dashboardProgressFuture,
                new DashboardProgressMetrics(0, 0, 0, 0),
                "dashboard progress metrics"
        );
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

        ProgressStat chatProgression = dashboardProgress.getChatsProgression() != null
                ? dashboardProgress.getChatsProgression()
                : fallbackChatProgression;

        int todayChats = dashboardProgress.getChatsToday();
        int yesterdayChats = dashboardProgress.getChatsYesterday();

        int termsToday = Math.max(0, dashboardProgress.getTermsToday());
        int termsYesterday = Math.max(0, dashboardProgress.getTermsYesterday());
        ProgressStat termsProgression = dashboardProgress.getTermsProgression() == null
                ? new ProgressStat(termsToday, termsYesterday)
                : dashboardProgress.getTermsProgression();

        String widgetStatsRows = DashboardRowsRenderer.renderWidgetStatsRows(widgetStats, req.getContextPath());
        String dailyTopTermsRows = DashboardRowsRenderer.renderDailyTopTermsRows(
            dailyTopTopics,
                req.getContextPath()
        );
        String otherParasoftLatestRows = DashboardRowsRenderer.renderOtherParasoftLatestRows(otherParasoftLatest, req.getContextPath());

        String termChartJson = termService.toChartJson(termSummary);

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
        String sessionChartJson = sessionService.buildEmptySessionPayload(rangeStart, rangeEnd);
        int totalUsers = 0;
        int activeUsers = 0;
        int inactiveUsers = 0;

        int newSessionsToday = 0;
        int newSessionsYesterday = 0;
        ProgressStat newSessionsProgression = new ProgressStat(0, 0);

        if (sessionOverview != null) {
            Map<String, SessionLabelStore.SessionLabel> labels = loadSessionLabels(sessionOverview.getTopSessions());
            sessionRows = DashboardRowsRenderer.renderSessionRows(sessionOverview.getTopSessions(), labels, contextPath);
            sessionChartJson = sessionService.buildSessionChartPayload(sessionOverview, rangeStart, rangeEnd);

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
                Map.entry("widgetPieChartData", DashboardTemplateRenderer.escapeForJs(buildWidgetPieChartData(widgetStats))),
                Map.entry("termChartData", termChartJson),
                Map.entry("termIncreaseMapJson", DashboardTemplateRenderer.escapeForJs(termIncreaseMapJson)),
                Map.entry("termTotalMapJson", DashboardTemplateRenderer.escapeForJs(termTotalMapJson)),
                Map.entry("termLegendDefaultMode", "increase"),
                Map.entry("termIncreaseRangeStart", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreaseRangeEnd", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreasePrevRangeStart", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("termIncreasePrevRangeEnd", DashboardTemplateRenderer.escapeHtml(dayToday.format(DATE_FORMATTER))),
                Map.entry("sessionRows", sessionRows),
                Map.entry("sessionRangeStart", DashboardTemplateRenderer.escapeHtml(rangeStart.format(DATE_FORMATTER))),
                Map.entry("sessionRangeEnd", DashboardTemplateRenderer.escapeHtml(rangeEnd.format(DATE_FORMATTER))),
                Map.entry("sessionChartData", DashboardTemplateRenderer.escapeForJs(sessionChartJson)),
                Map.entry("activeDays", DashboardTemplateRenderer.escapeHtml(String.valueOf(activeDays))),
                Map.entry("totalUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(totalUsers))),
                Map.entry("activeUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(activeUsers))),
                Map.entry("inactiveUsers", DashboardTemplateRenderer.escapeHtml(String.valueOf(inactiveUsers))),
                Map.entry("activeUsersUrl", contextPath + "/dashboard/sessions?activity=active&activeDays=" + activeDays),
                Map.entry("sessionNewToday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newSessionsToday))),
                Map.entry("sessionNewYesterday", DashboardTemplateRenderer.escapeHtml(String.valueOf(newSessionsYesterday))),
                Map.entry("sessionNewProgression", formatProgressionHtml(newSessionsProgression)),
                Map.entry("sessionNewProgressionDirection", DashboardTemplateRenderer.escapeHtml(newSessionsProgression.getDirection())),
                Map.entry("lastFiveDaysTrendData", DashboardTemplateRenderer.escapeForJs(lastFiveDaysTrendJson))
        ));

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    
        } catch (IOException | ServletException | RuntimeException e) {
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

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
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

    private String formatProgressionHtml(ProgressStat p) {
        if (p == null) {
            return "0 (0.0%) vs yesterday";
        }

        String text = String.format("%+d (%.1f%%) vs yesterday", p.getDelta(), p.getPctDelta());
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
            Integer value = e.getValue();
            int safeValue = value == null ? 0 : value.intValue();
            out.put(e.getKey(), Math.max(0, safeValue));
        }
        return out;
    }

    private String buildTermIncreaseMapJson(Map<String, Integer> increaseMap) {
        JsonObjectBuilder obj = Json.createObjectBuilder();
        if (increaseMap != null) {
            for (Map.Entry<String, Integer> e : increaseMap.entrySet()) {
                String k = e.getKey();
                Integer rawValue = e.getValue();
                int v = rawValue == null ? 0 : Math.max(0, rawValue.intValue());
                if (k != null) {
                    obj.add(k, v);
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
                Integer rawValue = e.getValue();
                int v = rawValue == null ? 0 : Math.max(0, rawValue.intValue());
                if (k != null) {
                    obj.add(k, v);
                }
            }
        }
        return obj.build().toString();
    }

    private <T> T safeJoin(CompletableFuture<T> future, T fallback, String label) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            log.log(Level.WARNING, "Failed to compute " + label, cause == null ? ex : cause);
            return fallback;
        } catch (Throwable ex) {
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
