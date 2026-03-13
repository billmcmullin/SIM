package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

@WebServlet(name = "DashboardSessionNamesJsonServlet", urlPatterns = {"/dashboard/session-names.json"})
public class DashboardSessionNamesJsonServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesJsonServlet.class.getName());
    private static final int DEFAULT_LIMIT = 300;

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
            log.log(Level.WARNING, "Unable to list widgets for session catalog", e);
        }

        String query = req.getParameter("q");
        int limit = parsePositiveInteger(req.getParameter("limit"), DEFAULT_LIMIT);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, SessionAccumulator> accumulators = collectSessionAccumulators(conn, widgets, query);
            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());

            JsonArrayBuilder sessions = Json.createArrayBuilder();
            accumulators.entrySet()
                    .stream()
                    .sorted((a, b) -> {
                        int cmp = Integer.compare(b.getValue().count, a.getValue().count);
                        if (cmp != 0) {
                            return cmp;
                        }
                        return a.getKey().compareTo(b.getKey());
                    })
                    .limit(limit)
                    .forEach(entry -> {
                        SessionAccumulator acc = entry.getValue();
                        SessionLabelStore.SessionLabel label = labels.get(entry.getKey());
                        String displayLabel = formatLabel(entry.getKey(), label);
                        JsonObjectBuilder builder = Json.createObjectBuilder()
                                .add("sessionId", entry.getKey())
                                .add("displayLabel", displayLabel)
                                .add("count", acc.count)
                                .add("lastEntry", acc.lastEntry == null ? "—" : acc.lastEntry.toString())
                                .add("reviewUrl", buildReviewUrl(req, entry.getKey()))
                                .add("displayName", label == null ? "" : label.getDisplayName())
                                .add("email", label == null ? "" : label.getEmail());
                        sessions.add(builder);
                    });

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("total", accumulators.size())
                    .add("sessions", sessions)
                    .build();

            resp.setContentType("application/json");
            resp.getWriter().print(payload.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to collect session catalog", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load session catalog")
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().print(error.toString());
        }
    }

    private String buildReviewUrl(HttpServletRequest req, String sessionId) {
        return req.getContextPath() + "/dashboard/sessions/drilldown/session-review?sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
    }

    private int parsePositiveInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                return fallback;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String formatLabel(String sessionId, SessionLabelStore.SessionLabel label) {
        return SessionLabelStore.resolveDisplayLabel(sessionId, label);
    }

    private Map<String, SessionAccumulator> collectSessionAccumulators(Connection conn, List<WidgetEntry> widgets, String filter) throws SQLException {
        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        if (widgets == null || widgets.isEmpty()) {
            return accumulators;
        }

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String tableName = sanitizeWidgetTableName(widget.getWidgetId());
            if (!tableExists(conn, tableName)) {
                continue;
            }

            StringBuilder sql = new StringBuilder("SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM ")
                    .append(quoteIdentifier(tableName))
                    .append(" WHERE session_id IS NOT NULL");
            if (filter != null && !filter.isBlank()) {
                sql.append(" AND session_id ILIKE ?");
            }
            sql.append(" GROUP BY session_id");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                if (filter != null && !filter.isBlank()) {
                    ps.setString(1, "%" + filter.trim() + "%");
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("session_id");
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }
                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.count += rs.getInt("total");
                        Timestamp lastEntry = rs.getTimestamp("last_entry");
                        if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                            acc.lastEntry = lastEntry;
                        }
                    }
                }
            }
        }

        return accumulators;
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

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private static final class SessionAccumulator {

        private int count = 0;
        private Timestamp lastEntry = null;
    }
}
