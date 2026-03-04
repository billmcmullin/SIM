package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionIdFormatter;
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

/**
 * AllSessionsServlet — API endpoints. Mapped to /dashboard/sessions/data,
 * /dashboard/sessions/chats, /dashboard/sessions/select so UI page can live at
 * /dashboard/sessions without getting raw JSON.
 */
@WebServlet(name = "AllSessionsServlet", urlPatterns = {"/dashboard/sessions/data", "/dashboard/sessions/chats",
    "/dashboard/sessions/select"})
public class AllSessionsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AllSessionsServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private static final class SessionSummary {

        String sessionId;
        long totalCount = 0;
        Instant firstSeen = null;
        Instant lastSeen = null;
        Set<String> widgetIds = new HashSet<>();

        SessionSummary(String sessionId) {
            this.sessionId = sessionId;
        }

        void accept(Timestamp ts, int count, String widgetId) {
            if (ts != null) {
                Instant inst = ts.toInstant();
                if (firstSeen == null || inst.isBefore(firstSeen)) {
                    firstSeen = inst;
                }
                if (lastSeen == null || inst.isAfter(lastSeen)) {
                    lastSeen = inst;
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
        String servletPath = req.getServletPath();
        if (servletPath != null && servletPath.endsWith("/chats")) {
            handleChatsForSession(req, resp);
        } else {
            handleListSessions(req, resp);
        }
    }

    // default limit param if not specified -> top N sessions by totalCount
    private void handleListSessions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        String search = req.getParameter("search");
        boolean hasSearch = search != null && !search.isBlank();
        String normalizedSearch = hasSearch ? "%" + search.trim() + "%" : null;

        int limit = parseInteger(req.getParameter("limit"), 10); // default top 10
        int page = parseInteger(req.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }
        boolean returnAll = "true".equalsIgnoreCase(req.getParameter("all"));

        Map<String, SessionSummary> map = new HashMap<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            JsonObject body = Json.createObjectBuilder().add("status", "ok").add("totalSessions", 0).add("totalChats", 0)
                    .add("page", 1).add("totalPages", 1).add("sessions", Json.createArrayBuilder().build())
                    .add("widgetNames", Json.createObjectBuilder().build()).build();
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(body.toString());
            return;
        }

        // Build widgetId -> displayName map for client convenience
        JsonObjectBuilder widgetNamesBuilder = Json.createObjectBuilder();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null) {
                continue;
            }
            String id = w.getWidgetId();
            String dn = w.getDisplayName();
            if (dn == null || dn.isBlank()) {
                dn = id;
            }
            widgetNamesBuilder.add(id, dn);
        }

        long totalChatsAcrossAll = 0L;
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                try {
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Unable to check table " + tableName, e);
                    continue;
                }

                Set<String> matchingSessionIds = null;
                if (hasSearch) {
                    matchingSessionIds = new HashSet<>();
                    String sqlMatch = "SELECT DISTINCT session_id FROM " + quoteIdentifier(tableName)
                            + " WHERE (prompt ILIKE ? OR response_text ILIKE ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlMatch)) {
                        ps.setString(1, normalizedSearch);
                        ps.setString(2, normalizedSearch);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String sid = rs.getString("session_id");
                                if (sid != null && !sid.isBlank()) {
                                    matchingSessionIds.add(sid);
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.log(Level.WARNING, "Search query failed for table " + tableName + ": " + e.getMessage(), e);
                        continue;
                    }
                    if (matchingSessionIds.isEmpty()) {
                        continue;
                    }
                }

                String aggSql = "SELECT session_id, COUNT(*) AS cnt, MIN(created_at) AS first_ts, MAX(created_at) AS last_ts "
                        + "FROM " + quoteIdentifier(tableName) + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
                try (PreparedStatement ps = conn.prepareStatement(aggSql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sid = rs.getString("session_id");
                        if (sid == null || sid.isBlank()) {
                            continue;
                        }
                        if (hasSearch && (matchingSessionIds == null || !matchingSessionIds.contains(sid))) {
                            continue;
                        }
                        int cnt = rs.getInt("cnt");
                        Timestamp first = rs.getTimestamp("first_ts");
                        Timestamp last = rs.getTimestamp("last_ts");
                        SessionSummary summary = map.computeIfAbsent(sid, SessionSummary::new);
                        summary.accept(first, cnt, widgetId);
                        summary.accept(last, 0, widgetId);
                        totalChatsAcrossAll += cnt;
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Aggregation failed for table " + tableName + ": " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to list sessions", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to list sessions.\"}");
            return;
        }

        List<SessionSummary> sessions = new ArrayList<>(map.values());
        sessions.sort((a, b) -> {
            int c = Long.compare(b.totalCount, a.totalCount);
            if (c != 0) {
                return c;
            }
            Instant ia = a.lastSeen == null ? Instant.EPOCH : a.lastSeen;
            Instant ib = b.lastSeen == null ? Instant.EPOCH : b.lastSeen;
            return ib.compareTo(ia);
        });

        int totalSessions = sessions.size();

        int totalPages = 1;
        if (returnAll || limit <= 0) {
            totalPages = 1;
        } else {
            totalPages = (int) Math.ceil((double) totalSessions / (double) limit);
            if (page > totalPages) {
                page = totalPages;
            }
            if (page < 1) {
                page = 1;
            }
            int start = (page - 1) * limit;
            int end = Math.min(start + limit, totalSessions);
            if (start < end) {
                sessions = sessions.subList(start, end);
            } else {
                sessions = new ArrayList<>();
            }
        }

        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (SessionSummary s : sessions) {
            JsonObject obj = Json.createObjectBuilder().add("sessionId", s.sessionId)
                    .add("sessionIdDisplay", SessionIdFormatter.formatForDisplay(s.sessionId)).add("totalCount", s.totalCount)
                    .add("firstSeen", s.firstSeen == null ? "" : s.firstSeen.toString())
                    .add("lastSeen", s.lastSeen == null ? "" : s.lastSeen.toString()).add("widgetsCount", s.widgetIds.size())
                    .add("widgets", Json.createArrayBuilder(s.widgetIds).build()).build();
            arr.add(obj);
        }

        JsonObject body = Json.createObjectBuilder().add("status", "ok").add("totalSessions", totalSessions)
                .add("totalChats", totalChatsAcrossAll).add("page", page).add("totalPages", totalPages).add("sessions", arr)
                .add("widgetNames", widgetNamesBuilder).build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    private void handleChatsForSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req, resp)) {
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
        if (widgets.isEmpty()) {
            JsonObject body = Json.createObjectBuilder().add("status", "ok").add("rows", Json.createArrayBuilder()).build();
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(body.toString());
            return;
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String widgetName = widget.getDisplayName();
                if (widgetName == null || widgetName.isBlank()) {
                    widgetName = widgetId;
                }
                String tableName = sanitizeWidgetTableName(widgetId);
                try {
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Unable to check table " + tableName, e);
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at FROM "
                        + quoteIdentifier(tableName) + " WHERE session_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("widgetId", widgetId);
                            row.put("widgetName", widgetName);
                            row.put("chatId", rs.getString("widget_chat_id"));
                            row.put("prompt", rs.getString("prompt"));
                            row.put("response", rs.getString("response_text"));
                            Timestamp ts = rs.getTimestamp("created_at");
                            row.put("createdAt", ts == null ? "" : ts.toInstant().toString());
                            rows.add(row);
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Query failed for widget " + widgetId + ": " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to fetch session chats", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load session chats.\"}");
            return;
        }

        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Map<String, Object> r : rows) {
            JsonObject obj = Json.createObjectBuilder()
                    .add("widgetId", r.get("widgetId") == null ? "" : String.valueOf(r.get("widgetId")))
                    .add("widgetName", r.get("widgetName") == null ? "" : String.valueOf(r.get("widgetName")))
                    .add("chatId", r.get("chatId") == null ? "" : String.valueOf(r.get("chatId")))
                    .add("prompt", r.get("prompt") == null ? "" : String.valueOf(r.get("prompt")))
                    .add("response", r.get("response") == null ? "" : String.valueOf(r.get("response")))
                    .add("createdAt", r.get("createdAt") == null ? "" : String.valueOf(r.get("createdAt")))
                    .build();
            ab.add(obj);
        }

        JsonObject body = Json.createObjectBuilder().add("status", "ok").add("rows", ab).build();
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().endsWith("/select")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!isLoggedIn(req, resp)) {
            return;
        }

        jakarta.json.JsonObject payload;
        try (jakarta.json.JsonReader jr = Json.createReader(req.getInputStream())) {
            payload = jr.readObject();
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body.");
            return;
        }

        // New: handle selectedChatIds array (create snapshot from arbitrary chat ids)
        if (payload.containsKey("selectedChatIds")) {
            List<String> chatIds = new ArrayList<>();
            try {
                payload.getJsonArray("selectedChatIds").forEach(v -> {
                    if (v != null) {
                        String s = v.toString();
                        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
                            s = s.substring(1, s.length() - 1);
                        }
                        chatIds.add(s);
                    }
                });
            } catch (Exception ex) {
                // ignore malformed input
            }

            if (chatIds.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "selectedChatIds required.");
                return;
            }

            List<TermChatSnapshot> snapshots = new ArrayList<>();
            List<WidgetEntry> widgets = listWidgets();
            try (Connection conn = dsHolder.getDataSource().getConnection()) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String wid = widget.getWidgetId();
                    String tableName = sanitizeWidgetTableName(wid);
                    try {
                        if (!tableExists(conn, tableName)) {
                            continue;
                        }
                    } catch (SQLException e) {
                        log.log(Level.WARNING, "Unable to check table " + tableName, e);
                        continue;
                    }
                    String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM " + quoteIdentifier(tableName)
                            + " WHERE widget_chat_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (String cid : chatIds) {
                            ps.setString(1, cid);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    String chatId = rs.getString("widget_chat_id");
                                    Timestamp created = rs.getTimestamp("created_at");
                                    String prompt = rs.getString("prompt");
                                    String respText = rs.getString("response_text");
                                    String sessionId = rs.getString("session_id");
                                    snapshots.add(new TermChatSnapshot(sessionId == null ? "" : sessionId, wid, chatId == null ? "" : chatId, prompt, respText, created, sessionId));
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.log(Level.WARNING, "Query failed for widget " + wid + ": " + e.getMessage(), e);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Unable to collect snapshots for selected chat IDs", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to collect chats for selection.");
                return;
            }

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for provided chat IDs.");
                return;
            }

            HttpSession httpSession = req.getSession(false);
            String displayName = "Selected Chats";
            String backUrl = req.getContextPath() + "/dashboard/sessions";
            String selectionId = WidgetReviewStartServlet.createSnapshotSelection(httpSession, displayName, snapshots, backUrl);
            if (selectionId == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
                return;
            }

            JsonObject body = Json.createObjectBuilder().add("status", "ok").add("selectionId", selectionId).build();
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("application/json; charset=UTF-8");
            try (OutputStream out = resp.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return;
        }

        String sessionId = payload.getString("sessionId", "").trim();
        if (sessionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId required.");
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String wid = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(wid);
                try {
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Unable to check table " + tableName, e);
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE session_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            Timestamp created = rs.getTimestamp("created_at");
                            snapshots.add(new TermChatSnapshot(sessionId, wid, chatId == null ? "" : chatId, rs.getString("prompt"),
                                    rs.getString("response_text"), created, sessionId));
                        }
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Query failed for widget " + wid + ": " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to collect snapshots for session", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to collect session chats.");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for session.");
            return;
        }

        HttpSession httpSession = req.getSession(false);
        String displayName = "Session " + sessionId;
        String backUrl = req.getContextPath() + "/dashboard/sessions";
        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(httpSession, displayName, snapshots, backUrl);
        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        JsonObject body = Json.createObjectBuilder().add("status", "ok").add("selectionId", selectionId).build();
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (OutputStream out = resp.getOutputStream()) {
            out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

    // helpers
    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets for session listing", e);
            return List.of();
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
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

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
