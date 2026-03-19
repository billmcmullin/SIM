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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
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

    private static final class ChatRow {

        final String chatId;
        final String prompt;
        final Timestamp createdAt;

        ChatRow(String chatId, String prompt, Timestamp createdAt) {
            this.chatId = chatId;
            this.prompt = prompt;
            this.createdAt = createdAt;
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path != null && path.endsWith("/select")) {
            handleSelect(req, resp);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"status\":\"error\",\"message\":\"Method not allowed.\"}");
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
        Map<String, String> widgetNames = buildWidgetDisplayNameMap(widgets);

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
                resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                resp.setContentType("application/json; charset=UTF-8");
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
            if (totalPages < 1) {
                totalPages = 1;
            }
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

        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();
        for (Map.Entry<String, String> e : widgetNames.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue());
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("totalSessions", totalSessions)
                .add("totalPages", totalPages)
                .add("page", page)
                .add("widgetNames", widgetNamesObj.build())
                .add("sessions", array)
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private void handleChats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        String sessionId = req.getParameter("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"sessionId required.\"}");
            return;
        }

        List<WidgetEntry> widgets = listWidgets();
        List<ChatRow> rows = new ArrayList<>();
        String sid = sessionId.trim();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE session_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new ChatRow(
                                    rs.getString("widget_chat_id"),
                                    rs.getString("prompt"),
                                    rs.getTimestamp("created_at")
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load chats for session", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load chats for session.\"}");
            return;
        }

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (ChatRow r : rows) {
            array.add(Json.createObjectBuilder()
                    .add("chatId", r.chatId == null ? "" : r.chatId)
                    .add("prompt", r.prompt == null ? "" : r.prompt)
                    .add("createdAt", r.createdAt == null ? "" : r.createdAt.toInstant().toString())
                    .build());
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("rows", array)
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private void handleSelect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAuth(req, resp)) {
            return;
        }

        JsonObject payload;
        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        if (!payload.containsKey("selectedChatIds")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"selectedChatIds required.\"}");
            return;
        }

        Set<String> selected = new LinkedHashSet<>();
        try {
            payload.getJsonArray("selectedChatIds").forEach(v -> {
                String val = v == null ? "" : v.toString().replace("\"", "").trim();
                if (!val.isBlank()) {
                    selected.add(val);
                }
            });
        } catch (Exception ignored) {
        }

        if (selected.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No valid chat IDs provided.\"}");
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE widget_chat_id = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (String chatId : selected) {
                        ps.setString(1, chatId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String foundChatId = rs.getString("widget_chat_id");
                                String prompt = rs.getString("prompt");
                                String responseText = rs.getString("response_text");
                                Timestamp createdAt = rs.getTimestamp("created_at");
                                String sessionId = rs.getString("session_id");

                                snapshots.add(new TermChatSnapshot(
                                        "Selected Session Chats",
                                        widgetId,
                                        foundChatId == null ? "" : foundChatId,
                                        prompt,
                                        responseText,
                                        createdAt,
                                        sessionId
                                ));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to collect selected session chats", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to create selection.\"}");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No matching chats found for selection.\"}");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                req.getSession(false),
                "Selected Session Chats",
                snapshots,
                req.getContextPath() + "/dashboard/sessions"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to create selection.\"}");
            return;
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("count", snapshots.size())
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private Map<String, String> buildWidgetDisplayNameMap(List<WidgetEntry> widgets) {
        if (widgets == null || widgets.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String dn = w.getDisplayName();
            map.put(w.getWidgetId(), (dn == null || dn.isBlank()) ? w.getWidgetId() : dn);
        }
        return map;
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

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
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
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
