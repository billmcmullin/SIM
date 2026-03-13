package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
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

@WebServlet(name = "DashboardSessionsJsonServlet", urlPatterns = {"/dashboard/sessions.json"})
public class DashboardSessionsJsonServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionsJsonServlet.class.getName());
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().print("{\"status\":\"unauthorized\"}");
            return;
        }

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard sessions", e);
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, SessionAccumulator> accumulators = collectSessionAccumulators(conn, widgets);
            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());
            Map<String, String> widgetNames = mapWidgetDisplayNames(widgets);

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
                    .add("total", accumulators.size())
                    .add("sessions", sessionsArray)
                    .build();

            resp.setContentType("application/json");
            resp.getWriter().print(payload.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to compute top sessions for dashboard", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load session metrics")
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().print(error.toString());
        }
    }

    private Map<String, SessionAccumulator> collectSessionAccumulators(Connection conn, List<WidgetEntry> widgets) throws SQLException {
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
                    acc.widgetCounts.merge(widgetId, total, Integer::sum);
                    Timestamp lastEntry = rs.getTimestamp("last_entry");
                    if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                        acc.lastEntry = lastEntry;
                    }
                }
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
            int value = entry.getValue() == null ? 0 : entry.getValue();
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

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        var meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
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

    private static final class SessionAccumulator {

        private int count = 0;
        private Timestamp lastEntry = null;
        private final Map<String, Integer> widgetCounts = new LinkedHashMap<>();
    }
}
