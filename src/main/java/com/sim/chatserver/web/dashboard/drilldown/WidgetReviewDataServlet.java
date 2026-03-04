package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionIdFormatter;
import com.sim.chatserver.web.dashboard.WidgetReviewStartServlet;
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

/**
 * WidgetReviewDataServlet — returns rows for a selection.
 *
 * Adds "widgetName" (display name) to each row so the UI can show the
 * human-friendly widget name instead of the numeric widgetId.
 */
@WebServlet(name = "WidgetReviewDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/review-data"})
public class WidgetReviewDataServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewDataServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private static final String[] ALLOWED_SORT_COLUMNS = {
        "widget_chat_id", "prompt", "created_at", "session_id"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }
        String selectionId = req.getParameter("selectionId");
        if (selectionId == null || selectionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"selectionId required.\"}");
            return;
        }

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Selection not found.\"}");
            return;
        }

        if (selection.hasSnapshots()) {
            handleSnapshotSelection(selection, req, resp);
            return;
        }

        String widgetId = selection.widgetId;
        String tableName = sanitizeWidgetTableName(widgetId);

        // Attempt to resolve widget display name once for DB-backed path
        String widgetDisplayName = resolveWidgetDisplayName(widgetId);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Table does not exist.\"}");
                return;
            }

            List<String> chatIds = selection.chatIds;
            if (chatIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"No chat IDs specified.\"}");
                return;
            }

            String placeholders = String.join(",", chatIds.stream().map(id -> "?").collect(Collectors.toList()));
            String search = req.getParameter("search");
            String sortColumn = parseSortColumn(req.getParameter("sortColumn"));
            String sortDir = parseSortDirection(req.getParameter("sortDir"));
            int limit = parseInteger(req.getParameter("limit"), 10);
            int page = parseInteger(req.getParameter("page"), 1);
            if (page < 1) {
                page = 1;
            }
            int offset = (page - 1) * limit;

            StringBuilder baseWhere = new StringBuilder(" WHERE widget_chat_id IN (" + placeholders + ")");
            List<String> params = new java.util.ArrayList<>();
            params.addAll(chatIds);

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim() + "%";
                baseWhere.append(" AND (prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }

            String countSql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName) + baseWhere;
            int totalRows = 0;
            try (PreparedStatement countPs = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) {
                    countPs.setString(i + 1, params.get(i));
                }
                try (ResultSet rs = countPs.executeQuery()) {
                    if (rs.next()) {
                        totalRows = rs.getInt(1);
                    }
                }
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            if (totalRows > 0) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM ")
                        .append(quoteIdentifier(tableName))
                        .append(baseWhere)
                        .append(" ORDER BY ")
                        .append(sortColumn)
                        .append(" ")
                        .append(sortDir)
                        .append(" LIMIT ? OFFSET ?");

                try (PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
                    int idx = 1;
                    for (String param : params) {
                        ps.setString(idx++, param);
                    }
                    ps.setInt(idx++, limit);
                    ps.setInt(idx, offset);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String rawSession = nullable(rs, "session_id");
                            String displaySession = SessionIdFormatter.formatForDisplay(rawSession);
                            String chatId = nullable(rs, "widget_chat_id");
                            String prompt = nullable(rs, "prompt");
                            String response = nullable(rs, "response_text");
                            String createdAt = formatTimestamp(rs.getTimestamp("created_at"));
                            JsonObject row = Json.createObjectBuilder()
                                    .add("chatId", chatId)
                                    .add("widgetId", widgetId == null ? "" : widgetId)
                                    .add("widgetName", widgetDisplayName == null ? "" : widgetDisplayName)
                                    .add("prompt", prompt)
                                    .add("response", response)
                                    .add("createdAt", createdAt)
                                    .add("sessionId", rawSession == null ? "" : rawSession)
                                    .add("sessionIdDisplay", displaySession)
                                    .build();
                            arrayBuilder.add(row);
                        }
                    }
                }
            }

            int totalPages = totalRows == 0 ? 1 : (int) Math.ceil((double) totalRows / limit);
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("rows", arrayBuilder)
                    .add("searchTerms", Json.createObjectBuilder()
                            .add("global", selection.searchTerms.global == null ? "" : selection.searchTerms.global)
                            .add("prompt", selection.searchTerms.prompt == null ? "" : selection.searchTerms.prompt)
                            .add("response", selection.searchTerms.response == null ? "" : selection.searchTerms.response))
                    .add("totalRows", totalRows)
                    .add("totalPages", totalPages)
                    .add("page", page)
                    .build();

            resp.setContentType("application/json");
            resp.getWriter().write(body.toString());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to fetch selected rows", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load selection.\"}");
        }
    }

    private void handleSnapshotSelection(WidgetReviewStartServlet.Selection selection,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String search = req.getParameter("search");
        String sortColumn = parseSortColumn(req.getParameter("sortColumn"));
        String sortDir = parseSortDirection(req.getParameter("sortDir"));
        int limit = parseInteger(req.getParameter("limit"), 10);
        int page = parseInteger(req.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }
        int offset = (page - 1) * limit;

        List<TermChatSnapshot> filtered = filterSnapshots(selection.snapshots, search);
        sortSnapshots(filtered, sortColumn, sortDir);

        int totalRows = filtered.size();
        int fromIndex = Math.min(offset, totalRows);
        int toIndex = Math.min(offset + limit, totalRows);
        List<TermChatSnapshot> pageRows = filtered.subList(fromIndex, toIndex);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (TermChatSnapshot snapshot : pageRows) {
            String rawSession = snapshot.getSessionId();
            String displaySession = SessionIdFormatter.formatForDisplay(rawSession);
            String widgetId = snapshot.getWidgetId();
            String widgetName = resolveWidgetDisplayName(widgetId);
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("chatId", snapshot.getChatId())
                    .add("widgetId", widgetId == null ? "" : widgetId)
                    .add("widgetName", widgetName == null ? "" : widgetName)
                    .add("prompt", snapshot.getPrompt())
                    .add("response", snapshot.getResponse())
                    .add("createdAt", formatTimestamp(snapshot.getCreatedAt()))
                    .add("sessionId", rawSession == null ? "" : rawSession)
                    .add("sessionIdDisplay", displaySession));
        }

        int totalPages = totalRows == 0 ? 1 : (int) Math.ceil((double) totalRows / limit);
        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("rows", arrayBuilder)
                .add("searchTerms", Json.createObjectBuilder()
                        .add("global", selection.searchTerms.global == null ? "" : selection.searchTerms.global)
                        .add("prompt", selection.searchTerms.prompt == null ? "" : selection.searchTerms.prompt)
                        .add("response", selection.searchTerms.response == null ? "" : selection.searchTerms.response))
                .add("totalRows", totalRows)
                .add("totalPages", totalPages)
                .add("page", page)
                .build();

        resp.setContentType("application/json");
        resp.getWriter().write(body.toString());
    }

    private List<TermChatSnapshot> filterSnapshots(List<TermChatSnapshot> base, String search) {
        if (search == null || search.isBlank()) {
            return new java.util.ArrayList<>(base);
        }
        String normalized = search.trim().toLowerCase(Locale.ROOT);
        return base.stream()
                .filter(snapshot -> containsIgnoreCase(snapshot.getPrompt(), normalized)
                || containsIgnoreCase(snapshot.getResponse(), normalized)
                || containsIgnoreCase(snapshot.getSessionId(), normalized))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String needle) {
        if (source == null || needle == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(needle);
    }

    private void sortSnapshots(List<TermChatSnapshot> data, String column, String direction) {
        java.util.Comparator<TermChatSnapshot> comparator = switch (column) {
            case "prompt" ->
                java.util.Comparator.comparing(snapshot -> snapshot.getPrompt() == null ? "" : snapshot.getPrompt(),
                String.CASE_INSENSITIVE_ORDER);
            case "session_id" ->
                java.util.Comparator.comparing(snapshot -> snapshot.getSessionId() == null ? "" : snapshot.getSessionId(),
                String.CASE_INSENSITIVE_ORDER);
            case "widget_chat_id" ->
                java.util.Comparator.comparing(snapshot -> snapshot.getChatId() == null ? "" : snapshot.getChatId(),
                String.CASE_INSENSITIVE_ORDER);
            case "created_at" ->
                java.util.Comparator.comparing(snapshot -> snapshot.getCreatedAt() == null ? 0L : snapshot.getCreatedAt().getTime());
            default ->
                java.util.Comparator.comparing(snapshot -> snapshot.getChatId() == null ? "" : snapshot.getChatId(),
                String.CASE_INSENSITIVE_ORDER);
        };
        if ("DESC".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        data.sort(comparator);
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

    private String resolveWidgetDisplayName(String widgetId) {
        if (widgetId == null) {
            return null;
        }
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry w : widgets) {
                if (w != null && widgetId.equals(w.getWidgetId())) {
                    String dn = w.getDisplayName();
                    return dn == null || dn.isBlank() ? widgetId : dn;
                }
            }
        } catch (Exception e) {
            log.log(Level.FINE, "Unable to lookup widget display name", e);
        }
        return widgetId;
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

    private String nullable(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        return ts.toInstant().toString();
    }

    private String parseSortColumn(String column) {
        if (column == null) {
            return "created_at";
        }
        String normalized = column.toLowerCase(Locale.ROOT);
        for (String candidate : ALLOWED_SORT_COLUMNS) {
            if (candidate.equals(normalized)) {
                return candidate;
            }
        }
        return "created_at";
    }

    private String parseSortDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return "ASC";
        }
        return "DESC";
    }

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
