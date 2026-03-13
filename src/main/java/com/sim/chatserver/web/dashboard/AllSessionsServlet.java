package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AllSessionsServlet", urlPatterns = {
    "/dashboard/sessions/data",
    "/dashboard/sessions/chats",
    "/dashboard/sessions/select"
})
public class AllSessionsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AllSessionsServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private static final class SessionSummary {

        final String sessionId;
        long totalCount = 0;
        Instant firstSeen;
        Instant lastSeen;
        final Set<String> widgetIds = new HashSet<>();

        SessionSummary(String sessionId) {
            this.sessionId = sessionId;
        }

        void accept(Timestamp ts, int count, String widgetId) {
            if (ts != null) {
                Instant instant = ts.toInstant();
                if (firstSeen == null || instant.isBefore(firstSeen)) {
                    firstSeen = instant;
                }
                if (lastSeen == null || instant.isAfter(lastSeen)) {
                    lastSeen = instant;
                }
            }
            totalCount += count;
            if (widgetId != null && !widgetId.isBlank()) {
                widgetIds.add(widgetId);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path != null && path.endsWith("/chats")) {
            handleChats(req, resp);
        } else {
            handleSummary(req, resp);
        }
    }

    private void handleSummary(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        boolean returnAll = "true".equalsIgnoreCase(req.getParameter("all"));
        String search = req.getParameter("search");
        boolean hasSearch = search != null && !search.isBlank();
        String normalizedSearch = hasSearch ? "%" + search.trim() + "%" : null;

        int limit = parseInteger(req.getParameter("limit"), 10);
        int page = parseInteger(req.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        Map<String, SessionSummary> sessions = new HashMap<>();
        List<WidgetEntry> widgets = listWidgets();
        if (!widgets.isEmpty()) {
            try (Connection conn = dsHolder.getDataSource().getConnection()) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }

                    Set<String> matchingIds = null;
                    if (hasSearch) {
                        matchingIds = gatherSessionIdsForSearch(conn, tableName, normalizedSearch);
                        if (matchingIds.isEmpty()) {
                            continue;
                        }
                    }

                    aggregateSessions(conn, tableName, sessions, widget.getWidgetId(), hasSearch ? matchingIds : null);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Unable to compute session summary", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load sessions.\"}");
                return;
            }
        }

        List<SessionSummary> sessionList = new ArrayList<>(sessions.values());
        sessionList.sort((a, b) -> {
            int cmp = Long.compare(b.totalCount, a.totalCount);
            if (cmp != 0) {
                return cmp;
            }
            Instant ia = a.lastSeen == null ? Instant.EPOCH : a.lastSeen;
            Instant ib = b.lastSeen == null ? Instant.EPOCH : b.lastSeen;
            return ib.compareTo(ia);
        });

        int totalSessions = sessionList.size();
        int totalPages = 1;
        if (!returnAll && limit > 0) {
            totalPages = (int) Math.ceil((double) totalSessions / (double) limit);
            if (page > totalPages) {
                page = totalPages;
            }
            int start = Math.min((page - 1) * limit, totalSessions);
            int end = Math.min(start + limit, totalSessions);
            sessionList = sessionList.subList(start, end);
        }

        Map<String, SessionLabelStore.SessionLabel> labels = mapSessionLabels(sessionList);

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (SessionSummary summary : sessionList) {
            String displayLabel = SessionLabelStore.resolveDisplayLabel(summary.sessionId, labels.get(summary.sessionId));
            array.add(Json.createObjectBuilder()
                    .add("sessionId", summary.sessionId)
                    .add("sessionIdDisplay", displayLabel)
                    .add("totalCount", summary.totalCount)
                    .add("firstSeen", summary.firstSeen == null ? "" : summary.firstSeen.toString())
                    .add("lastSeen", summary.lastSeen == null ? "" : summary.lastSeen.toString())
                    .add("widgets", Json.createArrayBuilder(summary.widgetIds))
                    .build());
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("totalSessions", totalSessions)
                .add("totalPages", totalPages)
                .add("page", page)
                .add("sessions", array)
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private Map<String, SessionLabelStore.SessionLabel> mapSessionLabels(List<SessionSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> ids = new HashSet<>();
        for (SessionSummary summary : summaries) {
            if (summary != null && summary.sessionId != null) {
                ids.add(summary.sessionId);
            }
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
            return Collections.emptyMap();
        }
    }

    private Set<String> gatherSessionIdsForSearch(Connection conn, String tableName, String pattern) throws SQLException {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT DISTINCT session_id FROM " + quoteIdentifier(tableName)
                + " WHERE (prompt ILIKE ? OR response_text ILIKE ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    if (sid != null && !sid.isBlank()) {
                        ids.add(sid);
                    }
                }
            }
        }
        return ids;
    }

    private void aggregateSessions(Connection conn, String tableName, Map<String, SessionSummary> sessions, String widgetId, Set<String> filter) throws SQLException {
        String sql = "SELECT session_id, COUNT(*) AS cnt, MIN(created_at) AS first_ts, MAX(created_at) AS last_ts FROM "
                + quoteIdentifier(tableName) + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sid = rs.getString("session_id");
                if (sid == null || sid.isBlank()) {
                    continue;
                }
                if (filter != null && !filter.contains(sid)) {
                    continue;
                }
                int count = rs.getInt("cnt");
                Timestamp first = rs.getTimestamp("first_ts");
                Timestamp last = rs.getTimestamp("last_ts");
                SessionSummary summary = sessions.computeIfAbsent(sid, SessionSummary::new);
                summary.accept(first, count, widgetId);
                summary.accept(last, 0, widgetId);
            }
        }
    }

    private void handleChats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAuth(req, resp)) {
            return;
        }
        String sessionId = req.getParameter("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"sessionId required.\"}");
            return;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                if (!tableExists(conn, tableName)) {
                    continue;
                }
                String sql = "SELECT widget_chat_id, prompt, response_text, created_at FROM "
                        + quoteIdentifier(tableName) + " WHERE session_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("widgetId", widget.getWidgetId());
                            String widgetName = widget.getDisplayName();
                            row.put("widgetName", widgetName == null || widgetName.isBlank() ? widget.getWidgetId() : widgetName);
                            row.put("chatId", rs.getString("widget_chat_id"));
                            row.put("prompt", rs.getString("prompt"));
                            row.put("response", rs.getString("response_text"));
                            Timestamp ts = rs.getTimestamp("created_at");
                            row.put("createdAt", ts == null ? "" : ts.toInstant().toString());
                            rows.add(row);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load session chats", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load chats.\"}");
            return;
        }

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Map<String, Object> row : rows) {
            array.add(Json.createObjectBuilder()
                    .add("widgetId", safe(row.get("widgetId")))
                    .add("widgetName", safe(row.get("widgetName")))
                    .add("chatId", safe(row.get("chatId")))
                    .add("prompt", safe(row.get("prompt")))
                    .add("response", safe(row.get("response")))
                    .add("createdAt", safe(row.get("createdAt")))
                    .build());
        }

        JsonObject body = Json.createObjectBuilder().add("status", "ok").add("rows", array).build();
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
            return List.of();
        }
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

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
