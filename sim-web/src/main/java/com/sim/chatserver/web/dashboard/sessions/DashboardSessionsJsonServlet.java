package com.sim.chatserver.web.dashboard.sessions;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardSessionAggregationQueryService;
import com.sim.chatserver.service.dashboard.SessionAccumulatorData;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

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


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardSessionsJsonServlet", urlPatterns = {"/dashboard/sessions.json"})
public class DashboardSessionsJsonServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionsJsonServlet.class.getCanonicalName());
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern(com.sim.chatserver.util.CommonLiteralConstants.DATETIME_PATTERN_SECONDS);
    private static final int ACTIVE_DAYS = 7;
        private static final DashboardSessionAggregationQueryService QUERY_SERVICE =
            new DashboardSessionAggregationQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.TEXT_UNAUTHORIZED).build());
            return;
        }

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard sessions", e);
        }

        try {
                Map<String, SessionAccumulatorData> accumulators =
                    QUERY_SERVICE.collectAccumulators(widgets, null);
            Map<String, SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());
            Map<String, String> widgetNames = DashboardSessionDataUtil.mapWidgetDisplayNames(widgets);

            int totalUsers = accumulators.size();
            Instant cutoff = Instant.now().minus(ACTIVE_DAYS, ChronoUnit.DAYS);

            int inactiveUsers = countInactiveUsers(accumulators, cutoff);
            int activeUsers = Math.max(0, totalUsers - inactiveUsers);

            // Yesterday baseline for active-users day-over-day comparison
            Instant cutoffYesterday = Instant.now()
                    .minus(1, ChronoUnit.DAYS)
                    .minus(ACTIVE_DAYS, ChronoUnit.DAYS);

            int inactiveUsersYesterday = countInactiveUsers(accumulators, cutoffYesterday);
            int activeUsersYesterday = Math.max(0, totalUsers - inactiveUsersYesterday);

            int activeUsersDelta = activeUsers - activeUsersYesterday;
            double activeUsersDeltaPct = activeUsersYesterday == 0
                    ? (activeUsers > 0 ? 100.0 : 0.0)
                    : (activeUsersDelta * 100.0) / activeUsersYesterday;
            String activeUsersDirection = activeUsersDelta > 0 ? "up"
                    : activeUsersDelta < 0 ? com.sim.chatserver.util.CommonLiteralConstants.VALUE_DOWN_LOWER
                            : "flat";

            JsonArrayBuilder sessionsArray = Json.createArrayBuilder();
            accumulators.entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, SessionAccumulatorData>>comparingInt(e -> -e.getValue().count))
                    .limit(10)
                    .forEach(entry -> {
                    sessionsArray.add(buildSessionJson(entry, labels, widgetNames, req.getContextPath()));
                    });

            JsonObject payload = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL, totalUsers)
                    .add("activeDays", ACTIVE_DAYS)
                    .add("activeUsers", activeUsers)
                    .add("activeUsersYesterday", activeUsersYesterday)
                    .add("activeUsersDelta", activeUsersDelta)
                    .add("activeUsersDeltaPct", activeUsersDeltaPct)
                    .add("activeUsersDirection", activeUsersDirection)
                    .add("inactiveUsers", inactiveUsers)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSIONS, sessionsArray)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to compute top sessions for dashboard", e);
            JsonObject error = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to load session metrics")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    
        } catch (IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write dashboard sessions payload", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback dashboard sessions error", sendErrorFailure);
            }
        }
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA;
        }
        return ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(ENTRY_FORMATTER);
    }

    private int countInactiveUsers(
            Map<String, SessionAccumulatorData> accumulators,
            Instant cutoff) {
        int inactive = 0;
        for (SessionAccumulatorData acc : accumulators.values()) {
            if (acc == null || acc.lastEntry == null || acc.lastEntry.toInstant().isBefore(cutoff)) {
                inactive++;
            }
        }
        return inactive;
    }

    private JsonObject buildSessionJson(
            Map.Entry<String, SessionAccumulatorData> entry,
            Map<String, SessionLabel> labels,
            Map<String, String> widgetNames,
            String contextPath) {
        String sessionId = entry.getKey();
        SessionAccumulatorData acc = entry.getValue();
        String last = formatTimestamp(acc.lastEntry);
        String topWidget = DashboardSessionDataUtil.pickTopWidgetName(acc.widgetCounts, widgetNames);
        String displayLabel = SessionLabelStore.resolveDisplayLabel(sessionId, labels.get(sessionId));
        String reviewUrl = contextPath
                + "/dashboard/sessions/drilldown/session-review?sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);

        return Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, sessionId)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_LABEL, displayLabel)
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, acc.count)
                .add("last", last)
                .add("topWidgetName", topWidget)
                .add("reviewUrl", reviewUrl)
                .build();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
