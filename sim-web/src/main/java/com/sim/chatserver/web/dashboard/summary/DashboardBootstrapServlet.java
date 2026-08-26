package com.sim.chatserver.web.dashboard.summary;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardSessionAggregationQueryService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardBootstrapServlet", urlPatterns = {"/dashboard/bootstrap.json"})
public class DashboardBootstrapServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardBootstrapServlet.class.getName());
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ACTIVE_DAYS = 7;
    private static final int SESSION_LIMIT = 10;
    private static final String SUMMARY_STORE_KEY = DashboardBootstrapServlet.class.getName() + ".summaryStore";
        private static final DashboardSessionAggregationQueryService SESSION_QUERY_SERVICE =
            new DashboardSessionAggregationQueryService(log);

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ensureSummaryStoreInitialized();
        } catch (IllegalStateException e) {
            throw new ServletException("Failed to initialize daily summary store", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        JsonObjectBuilder sections = Json.createObjectBuilder();

        JsonObject sessionsPayload = buildSessionsData(req);
        if (sessionsPayload == null) {
            sections.add("sessions", Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load sessions."));
        } else {
            sections.add("sessions", Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("data", sessionsPayload));
        }

        JsonObject summaryPayload = buildSummaryData();
        if (summaryPayload == null) {
            sections.add("summary", Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load summary."));
        } else {
            sections.add("summary", Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("data", summaryPayload));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("generatedAt", Instant.now().toString())
                .add("sections", sections)
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private JsonObject buildSessionsData(HttpServletRequest req) {
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard bootstrap", e);
            widgets = List.of();
        }

        try {
            Map<String, DashboardSessionAggregationQueryService.SessionAccumulatorData> accumulators =
                SESSION_QUERY_SERVICE.collectAccumulators(widgets, null);
            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());
            Map<String, String> widgetNames = DashboardSessionDataUtil.mapWidgetDisplayNames(widgets);

            int totalUsers = accumulators.size();
            Instant cutoff = Instant.now().minusSeconds(ACTIVE_DAYS * 24L * 60L * 60L);

            int inactiveUsers = 0;
            for (DashboardSessionAggregationQueryService.SessionAccumulatorData acc : accumulators.values()) {
                if (acc != null && acc.lastEntry != null && acc.lastEntry.toInstant().isBefore(cutoff)) {
                    inactiveUsers++;
                }
            }
            int activeUsers = Math.max(0, totalUsers - inactiveUsers);

            Instant cutoffYesterday = Instant.now().minusSeconds((ACTIVE_DAYS + 1L) * 24L * 60L * 60L);
            int inactiveUsersYesterday = 0;
            for (DashboardSessionAggregationQueryService.SessionAccumulatorData acc : accumulators.values()) {
                if (acc == null || acc.lastEntry == null || acc.lastEntry.toInstant().isBefore(cutoffYesterday)) {
                    inactiveUsersYesterday++;
                }
            }
            int activeUsersYesterday = Math.max(0, totalUsers - inactiveUsersYesterday);

            int activeUsersDelta = activeUsers - activeUsersYesterday;
            double activeUsersDeltaPct = activeUsersYesterday == 0
                    ? (activeUsers > 0 ? 100.0 : 0.0)
                    : (activeUsersDelta * 100.0) / activeUsersYesterday;
            String activeUsersDirection = activeUsersDelta > 0 ? "up"
                    : activeUsersDelta < 0 ? "down"
                            : "flat";

            JsonArrayBuilder sessionsArray = Json.createArrayBuilder();
            accumulators.entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, DashboardSessionAggregationQueryService.SessionAccumulatorData>>comparingInt(e -> -e.getValue().count))
                    .limit(SESSION_LIMIT)
                    .forEach(entry -> {
                        String sessionId = entry.getKey();
                        DashboardSessionAggregationQueryService.SessionAccumulatorData acc = entry.getValue();
                        String last = formatTimestamp(acc.lastEntry);
                        String topWidget = DashboardSessionDataUtil.pickTopWidgetName(acc.widgetCounts, widgetNames);
                        String displayLabel = SessionLabelStore.resolveDisplayLabel(sessionId, labels.get(sessionId));
                        String reviewUrl = req.getContextPath()
                                + "/dashboard/sessions/drilldown/session-review?sessionId="
                                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                        JsonObjectBuilder obj = Json.createObjectBuilder()
                                .add("sessionId", sessionId)
                                .add("displayLabel", displayLabel)
                                .add("count", acc.count)
                                .add("last", last)
                                .add("topWidgetName", topWidget)
                                .add("reviewUrl", reviewUrl);
                        sessionsArray.add(obj);
                    });

            return Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("total", totalUsers)
                    .add("activeDays", ACTIVE_DAYS)
                    .add("activeUsers", activeUsers)
                    .add("activeUsersYesterday", activeUsersYesterday)
                    .add("activeUsersDelta", activeUsersDelta)
                    .add("activeUsersDeltaPct", activeUsersDeltaPct)
                    .add("activeUsersDirection", activeUsersDirection)
                    .add("inactiveUsers", inactiveUsers)
                    .add("sessions", sessionsArray)
                    .build();
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to compute sessions for dashboard bootstrap", e);
            return Json.createObjectBuilder().build();
        }
    }

    private JsonObject buildSummaryData() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = LocalDate.now(zone);
        int slot = resolveCurrentSlot(zone);

        try {
            DashboardDailySummaryStore store = ensureSummaryStoreInitialized();
            return store.fetchExactOrLatest(day, slot);
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load summary for dashboard bootstrap", e);
            return Json.createObjectBuilder().build();
        }
    }

    private int resolveCurrentSlot(ZoneId zone) {
        int hour = LocalTime.now(zone).getHour();
        if (hour < 6) {
            return 0;
        }
        if (hour < 12) {
            return 1;
        }
        if (hour < 18) {
            return 2;
        }
        return 3;
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        return ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(ENTRY_FORMATTER);
    }

    private DashboardDailySummaryStore ensureSummaryStoreInitialized() {
        jakarta.servlet.ServletContext context = getServletContext();
        DashboardDailySummaryStore local = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
        if (local != null) {
            return local;
        }

        synchronized (context) {
            return getOrCreateSummaryStore(context);
        }
    }

    private DashboardDailySummaryStore getOrCreateSummaryStore(jakarta.servlet.ServletContext context) {
        DashboardDailySummaryStore local = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
        if (local != null) {
            return local;
        }

        try {
            DashboardDailySummaryStore created = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
            created.ensureTable();
            context.setAttribute(SUMMARY_STORE_KEY, created);
            return created;
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore for bootstrap", e);
            throw new IllegalStateException("Failed to initialize daily summary store", e);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write dashboard bootstrap payload", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback dashboard bootstrap error", sendErrorFailure);
            }
        }
    }

}
