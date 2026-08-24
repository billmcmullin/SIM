package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

final class AllSessionsService {

    private static final Logger log = Logger.getLogger(AllSessionsServlet.class.getName());
    private static final int ACTIVE_DAYS = 7;
    private static final int MAX_SEARCH_LENGTH = 128;
    private static final int MAX_SESSION_ID_LENGTH = 128;
    private static final Pattern SAFE_SESSION_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");
    private static final Pattern SAFE_CHAT_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final Pattern SAFE_INT_PARAM = Pattern.compile("^-?\\d{1,10}$");
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;

    private static final String PATH_DATA = "/dashboard/sessions/data";
    private static final String PATH_CHATS = "/dashboard/sessions/chats";
    private static final String PATH_SELECT = "/dashboard/sessions/select";

    private static final class SessionSummary {

        final String sessionId;
        long totalCount = 0;
        Instant firstSeen;
        Instant lastSeen;
        final Set<String> widgetIds = new HashSet<>();

        SessionSummary(String sessionId) {
            this.sessionId = sessionId;
        }

        private void accept(Timestamp ts, int count, String widgetId) {
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

        private ChatRow(String chatId, String prompt, Timestamp createdAt) {
            this.chatId = chatId;
            this.prompt = prompt;
            this.createdAt = createdAt;
        }
    }    void handleGet(HttpServletRequest req, HttpServletResponse resp) {
        String path = resolveRequestPath(req);
        if (PATH_CHATS.equals(path)) {
            handleChats(req, resp);
        } else {
            handleSummary(req, resp);
        }
    }    void handlePost(HttpServletRequest req, HttpServletResponse resp) {
        String path = resolveRequestPath(req);
        if (PATH_SELECT.equals(path)) {
            handleSelect(req, resp);
            return;
        }

        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed.");
    }

    private void handleSummary(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        String allParam = ServletRequestParamUtil.firstParam(req, "all", 256, false, false);
        String labeledOnlyParam = ServletRequestParamUtil.firstParam(req, "labeledOnly", 256, false, false);
        String searchParam = ServletRequestParamUtil.firstParam(req, "search", 256, false, false);
        String activityParam = ServletRequestParamUtil.firstParam(req, "activity", 256, false, false);
        String limitParam = ServletRequestParamUtil.firstParam(req, "limit", 256, false, false);
        String pageParam = ServletRequestParamUtil.firstParam(req, "page", 256, false, false);

        boolean returnAll = parseBooleanParam(allParam);
        boolean labeledOnly = parseBooleanParam(labeledOnlyParam);

        String search = sanitizeTextParam(searchParam, MAX_SEARCH_LENGTH);
        String searchTerm = search == null ? "" : search.trim();
        boolean hasSearch = !searchTerm.isBlank();
        String normalizedSearch = hasSearch ? '%' + searchTerm + '%' : null;

        String activity = sanitizeActivity(activityParam);

        int limit = clamp(parseInteger(limitParam, 10), 1, 200);
        int page = clamp(parseInteger(pageParam, 1), 1, Integer.MAX_VALUE);
        if (page < 1) {
            page = 1;
        }

        Map<String, SessionSummary> sessions = new HashMap<>();
        List<WidgetEntry> widgets = listWidgets();
        Map<String, String> widgetNames = buildWidgetDisplayNameMap(widgets);

        if (!widgets.isEmpty()) {
            Connection conn = openConnectionSafe();
            try (conn) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }
                    aggregateSessions(conn, tableName, sessions, widget.getWidgetId(), null);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Unable to compute session summary", e);
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load sessions.");
                return;
            }
        }

        List<SessionSummary> sessionList = new ArrayList<>(sessions.values());

        if (hasSearch && !sessionList.isEmpty()) {
            Set<String> matchedSessionIds = new HashSet<>();

            matchedSessionIds.addAll(gatherSessionIdsByIdMatch(sessions, search));

            Map<String, SessionLabelStore.SessionLabel> allLabels = mapSessionLabels(sessionList);
            matchedSessionIds.addAll(gatherSessionIdsByLabelMatch(allLabels, search));

            Connection conn = openConnectionSafe();
            try (conn) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                    if (!tableExists(conn, tableName)) {
                        continue;
                    }
                    matchedSessionIds.addAll(gatherSessionIdsForSearch(conn, tableName, normalizedSearch));
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to apply prompt/response search filter", e);
            }

            sessionList.removeIf(s -> s == null || s.sessionId == null || !matchedSessionIds.contains(s.sessionId));
        }

        List<SessionSummary> allSessionsForCounts = new ArrayList<>(sessionList);
        Instant cutoffForCounts = Instant.now().minus(ACTIVE_DAYS, ChronoUnit.DAYS);
        int totalUsers = allSessionsForCounts.size();
        int inactiveUsers = 0;
        for (SessionSummary s : allSessionsForCounts) {
            if (s != null && s.lastSeen != null && s.lastSeen.isBefore(cutoffForCounts)) {
                inactiveUsers++;
            }
        }
        int activeUsers = Math.max(0, totalUsers - inactiveUsers);

        activity = applyActivityFilter(sessionList, activity);

        // New: only show sessions with friendly name and/or email if labeledOnly=true
        if (labeledOnly && !sessionList.isEmpty()) {
            Map<String, SessionLabelStore.SessionLabel> labelsForFilter = mapSessionLabels(sessionList);
            sessionList.removeIf(s -> {
                if (s == null || s.sessionId == null) {
                    return true;
                }
                SessionLabelStore.SessionLabel label = labelsForFilter.get(s.sessionId);
                if (label == null) {
                    return true;
                }
                boolean hasName = label.getDisplayName() != null && !label.getDisplayName().isBlank();
                boolean hasEmail = label.getEmail() != null && !label.getEmail().isBlank();
                return !(hasName || hasEmail);
            });
        }

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
            totalPages = (totalSessions + limit - 1) / limit;
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
                    .add("firstSeen", formatInstant(summary.firstSeen))
                    .add("lastSeen", formatInstant(summary.lastSeen))
                    .add("widgets", Json.createArrayBuilder(summary.widgetIds))
                    .build());
        }

        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();
        for (Map.Entry<String, String> e : widgetNames.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue());
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("activeDays", ACTIVE_DAYS)
                .add("activity", activity)
                .add("labeledOnly", labeledOnly)
                .add("totalUsers", totalUsers)
                .add("activeUsers", activeUsers)
                .add("inactiveUsers", inactiveUsers)
                .add("totalSessions", totalSessions)
                .add("totalPages", totalPages)
                .add("page", page)
                .add("widgetNames", widgetNamesObj.build())
                .add("sessions", array)
                .build();
            writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    private void handleChats(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        String sessionId = sanitizeSessionId(ServletRequestParamUtil.firstParam(req, "sessionId", 256, false, false));
        if (sessionId == null || sessionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "sessionId required.");
            return;
        }

        List<WidgetEntry> widgets = listWidgets();
        List<ChatRow> rows = new ArrayList<>();
        String sid = sessionId;

        Connection conn = openConnectionSafe();
        try (conn) {
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
                PreparedStatement ps = prepareStatementSafe(conn, sql);
                try (ps) {
                    ps.setString(1, sid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = sanitizeChatId(readDbText(rs, "widget_chat_id", MAX_SESSION_ID_LENGTH));
                            rows.add(new ChatRow(
                                    chatId == null ? "" : chatId,
                                    readDbText(rs, "prompt", 4000),
                                    readDbTimestamp(rs, "created_at")
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to load chats for session", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load chats for session.");
            return;
        }

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (ChatRow r : rows) {
            array.add(Json.createObjectBuilder()
                    .add("chatId", r.chatId == null ? "" : r.chatId)
                    .add("prompt", r.prompt == null ? "" : r.prompt)
                    .add("createdAt", formatTimestamp(r.createdAt))
                    .build());
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("rows", array)
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    private void handleSelect(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid JSON payload.")
                    .build();
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, body);
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        if (!payload.containsKey("selectedChatIds")) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "selectedChatIds required.");
            return;
        }

        Set<String> selected = new LinkedHashSet<>();
        var selectedIdsArray = payload.getJsonArray("selectedChatIds");
        if (selectedIdsArray != null) {
            for (JsonValue value : selectedIdsArray) {
                if (!(value instanceof JsonString js)) {
                    continue;
                }
                String val = sanitizeChatId(js.getString());
                if (val != null) {
                    selected.add(val);
                }
            }
        }

        if (selected.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "No valid chat IDs provided.");
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        Connection conn = openConnectionSafe();
        try (conn) {
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

                PreparedStatement ps = prepareStatementSafe(conn, sql);
                try (ps) {
                    for (String chatId : selected) {
                        ps.setString(1, chatId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String foundChatId = sanitizeChatId(readDbText(rs, "widget_chat_id", MAX_SESSION_ID_LENGTH));
                                String prompt = readDbText(rs, "prompt", 4000);
                                String responseText = readDbText(rs, "response_text", 8000);
                                Timestamp createdAt = readDbTimestamp(rs, "created_at");
                                String sessionId = sanitizeSessionId(readDbText(rs, "session_id", MAX_SESSION_ID_LENGTH));

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
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        if (snapshots.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "No matching chats found for selection.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                req.getSession(false),
                "Selected Session Chats",
                snapshots,
            ServletPathUtil.safeContextPathNoEmptyGuard(req.getServletContext().getContextPath()) + "/dashboard/sessions"
        );

        if (selectionId == null || selectionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("count", snapshots.size())
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, body);
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

    private Set<String> gatherSessionIdsByIdMatch(Map<String, SessionSummary> sessions, String search) {
        if (search == null || search.isBlank() || sessions == null || sessions.isEmpty()) {
            return Collections.emptySet();
        }
        String q = search.trim().toLowerCase();
        Set<String> out = new HashSet<>();
        for (SessionSummary s : sessions.values()) {
            if (s == null || s.sessionId == null) {
                continue;
            }
            if (s.sessionId.toLowerCase().contains(q)) {
                out.add(s.sessionId);
            }
        }
        return out;
    }

    private Set<String> gatherSessionIdsByLabelMatch(Map<String, SessionLabelStore.SessionLabel> labels, String search) {
        if (search == null || search.isBlank() || labels == null || labels.isEmpty()) {
            return Collections.emptySet();
        }
        String q = search.trim().toLowerCase();
        Set<String> out = new HashSet<>();
        for (Map.Entry<String, SessionLabelStore.SessionLabel> e : labels.entrySet()) {
            String sid = e.getKey();
            SessionLabelStore.SessionLabel label = e.getValue();
            if (sid == null || label == null) {
                continue;
            }

            String displayName = label.getDisplayName() == null ? "" : label.getDisplayName();
            String email = label.getEmail() == null ? "" : label.getEmail();

            if (displayName.toLowerCase().contains(q) || email.toLowerCase().contains(q)) {
                out.add(sid);
            }
        }
        return out;
    }

    private Set<String> gatherSessionIdsForSearch(Connection conn, String tableName, String pattern) {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT DISTINCT session_id FROM " + quoteIdentifier(tableName)
                + " WHERE (prompt ILIKE ? OR response_text ILIKE ?)";
        PreparedStatement ps = prepareStatementSafe(conn, sql);
        try (ps) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = sanitizeSessionId(readDbText(rs, "session_id", 256));
                    if (sid != null) {
                        ids.add(sid);
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to gather session IDs for search", e);
        }
        return ids;
    }

    private void aggregateSessions(Connection conn, String tableName, Map<String, SessionSummary> sessions, String widgetId, Set<String> filter) {
        String sql = "SELECT session_id, COUNT(*) AS cnt, MIN(created_at) AS first_ts, MAX(created_at) AS last_ts FROM "
                + quoteIdentifier(tableName) + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
        PreparedStatement ps = prepareStatementSafe(conn, sql);
        try (ps) {
            ResultSet queryResult;
            try {
                queryResult = ps.executeQuery();
            } catch (SQLException ex) {
                log.log(Level.FINE, "Unable to execute aggregate query for table " + tableName, ex);
                return;
            }

            try (ResultSet rs = queryResult) {
                while (rs.next()) {
                    String sid = sanitizeSessionId(readDbText(rs, "session_id", MAX_SESSION_ID_LENGTH));
                    if (sid == null) {
                        continue;
                    }
                    if (filter != null && !filter.contains(sid)) {
                        continue;
                    }

                    int count = readDbNonNegativeInt(rs, "cnt", 0);
                    Timestamp first = readDbTimestamp(rs, "first_ts");
                    Timestamp last = readDbTimestamp(rs, "last_ts");

                    SessionSummary summary = sessions.computeIfAbsent(sid, SessionSummary::new);
                    summary.accept(first, count, widgetId);
                    summary.accept(last, 0, widgetId);
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to aggregate session data for table " + tableName, e);
        }
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null) {
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build();
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, body);
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build();
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, body);
            return false;
        }
        return true;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
            return List.of();
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, e);
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
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON error response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? "Request failed." : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

    private int parseInteger(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || !SAFE_INT_PARAM.matcher(trimmed).matches()) {
            return fallback;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            log.log(Level.FINE, "Invalid integer parameter value", ignored);
            return fallback;
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to open database connection", ex);
        }
    }

    private PreparedStatement prepareStatementSafe(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to prepare SQL statement", ex);
        }
    }

    private String readDbText(ResultSet rs, String column, int maxLen) {
        if (rs == null || column == null || column.isBlank()) {
            return null;
        }

        try {
            String raw = rs.getString(column);
            String canonicalRaw = canonicalizeForValidation(safeDbText(raw, maxLen));
            return validateTaintedText(canonicalRaw, maxLen);
        } catch (SQLException ex) {
            log.log(Level.FINE, "String DB text read failed for column " + column, ex);
            return null;
        }
    }

    private int readDbNonNegativeInt(ResultSet rs, String column, int fallback) {
        String value = readDbText(rs, column, 64);
        if (value == null || value.isBlank() || !SAFE_INT_PARAM.matcher(value.trim()).matches()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed < 0 ? fallback : parsed;
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid DB integer for column " + column, ex);
            return fallback;
        }
    }

    private Timestamp readDbTimestamp(ResultSet rs, String column) {
        String value = readDbText(rs, column, 128);
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        try {
            return Timestamp.from(Instant.parse(trimmed));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Instant parse fallback for DB timestamp column " + column, ex);
        }

        try {
            return Timestamp.valueOf(trimmed.replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Timestamp parse failed for DB column " + column, ex);
            return null;
        }
    }

    private String safeDbText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replace('\u0000', ' ')
            .replace("\r", "")
            .replace("\n", "")
                .trim();
        return normalized.length() > maxLen ? normalized.substring(0, maxLen) : normalized;
    }

    private String validateTaintedText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String canonical = canonicalizeForValidation(value);
        String normalized = ServletRequestParamUtil.normalizeBodyText(canonical, maxLen, false);
        if (normalized == null || normalized.isBlank()) {
            return "";
        }
        StringBuilder safe = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isISOControl(ch) && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        return safe.toString();
    }

    private String canonicalizeForValidation(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC);
    }

    private boolean parseBooleanParam(String value) {
        return "true".equalsIgnoreCase(value);
    }

    private String normalizeServletPath(String servletPath) {
        if (servletPath == null) {
            return PATH_DATA;
        }
        String normalized = servletPath.trim();
        if (PATH_CHATS.equals(normalized) || PATH_SELECT.equals(normalized) || PATH_DATA.equals(normalized)) {
            return normalized;
        }
        return PATH_DATA;
    }

    private String resolveRequestPath(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return PATH_DATA;
        }
        return normalizeServletPath(req.getHttpServletMapping().getPattern());
    }

    private String formatInstant(Instant value) {
        return value == null ? "" : ISO_INSTANT_FMT.format(value);
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "" : ISO_INSTANT_FMT.format(value.toInstant());
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private String sanitizeTextParam(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    private String sanitizeActivity(String raw) {
        if (raw == null || raw.isBlank()) {
            return "all";
        }
        String normalized = raw.trim().toLowerCase();
        if ("active".equals(normalized) || "inactive".equals(normalized) || "all".equals(normalized)) {
            return normalized;
        }
        return "all";
    }

    private String applyActivityFilter(List<SessionSummary> sessionList, String requestedActivity) {
        String activity = sanitizeActivity(requestedActivity);
        Instant cutoff = Instant.now().minus(ACTIVE_DAYS, ChronoUnit.DAYS);
        switch (activity) {
            case "inactive" -> sessionList.removeIf(s -> s == null || s.lastSeen == null || !s.lastSeen.isBefore(cutoff));
            case "active" -> sessionList.removeIf(s -> s != null && s.lastSeen != null && s.lastSeen.isBefore(cutoff));
            default -> {
                return "all";
            }
        }
        return activity;
    }

    private String sanitizeSessionId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        String trimmed = sessionId.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_SESSION_ID_LENGTH) {
            return null;
        }
        if (!SAFE_SESSION_ID.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }

    private String sanitizeChatId(String chatId) {
        if (chatId == null) {
            return null;
        }
        String trimmed = chatId.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_SESSION_ID_LENGTH) {
            return null;
        }
        if (!SAFE_CHAT_ID.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }
}

