package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionsJsonServlet", urlPatterns = {"/dashboard/sessions.json"})
public class DashboardSessionsJsonServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionsJsonServlet.class.getName());
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ACTIVE_DAYS = 7;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard sessions", e);
        }

        try (Connection conn = resolveDataSourceHolder().getDataSource().getConnection()) {
            Map<String, SessionAccumulator> accumulators = collectSessionAccumulators(conn, widgets);
            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());
            Map<String, String> widgetNames = mapWidgetDisplayNames(widgets);

            int totalUsers = accumulators.size();
            Instant cutoff = Instant.now().minus(ACTIVE_DAYS, ChronoUnit.DAYS);

            int inactiveUsers = 0;
            for (SessionAccumulator acc : accumulators.values()) {
                if (acc != null && acc.lastEntry != null && acc.lastEntry.toInstant().isBefore(cutoff)) {
                    inactiveUsers++;
                }
            }
            int activeUsers = Math.max(0, totalUsers - inactiveUsers);

            // Yesterday baseline for active-users day-over-day comparison
            Instant cutoffYesterday = Instant.now()
                    .minus(1, ChronoUnit.DAYS)
                    .minus(ACTIVE_DAYS, ChronoUnit.DAYS);

            int inactiveUsersYesterday = 0;
            for (SessionAccumulator acc : accumulators.values()) {
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
                    .sorted(Comparator.<Map.Entry<String, SessionAccumulator>>comparingInt(e -> -e.getValue().count))
                    .limit(10)
                    .forEach(entry -> {
                        String sessionId = entry.getKey();
                        SessionAccumulator acc = entry.getValue();
                        String last = formatTimestamp(acc.lastEntry);
                        String topWidget = pickTopWidgetName(acc.widgetCounts, widgetNames);
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

            JsonObject payload = Json.createObjectBuilder()
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

            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to compute top sessions for dashboard", e);
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load session metrics")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

    private Map<String, SessionAccumulator> collectSessionAccumulators(Connection conn, List<WidgetEntry> widgets) {
        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        if (widgets == null || widgets.isEmpty()) {
            return accumulators;
        }
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
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
                    int total = rs.getInt("total");
                    acc.count += total;

                    Integer existingCount = acc.widgetCounts.get(widgetId);
                    int mergedCount = (existingCount == null ? 0 : existingCount.intValue()) + total;
                    acc.widgetCounts.put(widgetId, Integer.valueOf(mergedCount));

                    Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                    if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                        acc.lastEntry = lastEntry;
                    }
                }
            } catch (SQLException ex) {
                log.log(Level.FINE, "Skipping widget session aggregation due to SQL error", ex);
            }
        }
        return accumulators;
    }

    private Map<String, String> mapWidgetDisplayNames(List<WidgetEntry> widgets) {
        Map<String, String> map = new LinkedHashMap<>();
        if (widgets == null) {
            return map;
        }
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String displayName = widget.getDisplayName();
            if (displayName == null || displayName.isBlank()) {
                displayName = widget.getWidgetId();
            }
            map.put(widget.getWidgetId(), displayName);
        }
        return map;
    }

    private String pickTopWidgetName(Map<String, Integer> widgetCounts, Map<String, String> displayNames) {
        String winner = null;
        int best = -1;
        for (Map.Entry<String, Integer> entry : widgetCounts.entrySet()) {
            Integer count = entry.getValue();
            int value = count == null ? 0 : count.intValue();
            if (value > best) {
                best = value;
                winner = entry.getKey();
            }
        }
        if (winner == null) {
            return "—";
        }
        return displayNames.getOrDefault(winner, winner);
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to inspect widget table metadata", ex);
        }
        return false;
    }

    private AppDataSourceHolder resolveDataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try {
            try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
                writer.writeObject(payload);
            }
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

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        return ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(ENTRY_FORMATTER);
    }

    static final class SessionAccumulator {

        int count = 0;
        Timestamp lastEntry = null;
        final Map<String, Integer> widgetCounts = new LinkedHashMap<>();
    }
}
