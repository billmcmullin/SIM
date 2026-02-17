package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
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

@WebServlet(name = "WidgetTableDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/data"})
public class WidgetTableDataServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableDataServlet.class.getName());

    private static final String[] COLUMNS = {
        "widget_chat_id",
        "prompt",
        "response_text",
        "created_at",
        "session_id"
    };

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ensure response uses UTF-8 for all outputs (early errors and final JSON)
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        if (!isLoggedIn(req, resp)) {
            return;
        }
        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"widgetId required.\"}");
            return;
        }

        WidgetEntry plugin = findWidget(widgetId);
        if (plugin == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Widget not found.\"}");
            return;
        }

        String tableName = sanitizeWidgetTableName(widgetId);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Table for widget does not exist.\"}");
                return;
            }

            int limit = parseLimit(req.getParameter("limit"));
            int page = parsePage(req.getParameter("page"));
            int offset = (page - 1) * limit;
            String search = req.getParameter("search");
            String sortColumn = parseSortColumn(req.getParameter("sortColumn"));
            String sortDir = parseSortDirection(req.getParameter("sortDir"));

            FilterState filters = new FilterState(
                    req.getParameter("filterPrompt"),
                    req.getParameter("filterResponse"),
                    search
            );

            String whereClause = filters.buildWhereClause();
            List<String> whereParams = filters.params();

            String countSql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName) + whereClause;
            int totalRows;
            try (PreparedStatement countPs = conn.prepareStatement(countSql)) {
                int idx = 1;
                for (String param : whereParams) {
                    countPs.setString(idx++, param);
                }
                try (ResultSet rs = countPs.executeQuery()) {
                    rs.next();
                    totalRows = rs.getInt(1);
                }
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            if (totalRows > 0) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM ")
                        .append(quoteIdentifier(tableName))
                        .append(whereClause)
                        .append(" ORDER BY ")
                        .append(sortColumn)
                        .append(" ")
                        .append(sortDir)
                        .append(" LIMIT ? OFFSET ?");

                try (PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
                    int idx = 1;
                    for (String param : whereParams) {
                        ps.setString(idx++, param);
                    }
                    ps.setInt(idx++, limit);
                    ps.setInt(idx, offset);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            // Return DB column values exactly as stored (including whitespace and special characters).
                            // Use addNull when the column value is SQL NULL so the JSON represents null instead of empty string.
                            JsonObjectBuilder rowBuilder = Json.createObjectBuilder();

                            String chatId = rs.getString("widget_chat_id");
                            if (chatId == null) {
                                rowBuilder.addNull("chatId");
                            } else {
                                rowBuilder.add("chatId", chatId);
                            }

                            String prompt = rs.getString("prompt");
                            if (prompt == null) {
                                rowBuilder.addNull("prompt");
                            } else {
                                rowBuilder.add("prompt", prompt);
                            }

                            String response = rs.getString("response_text");
                            if (response == null) {
                                rowBuilder.addNull("response");
                            } else {
                                rowBuilder.add("response", response);
                            }

                            Timestamp createdTs = rs.getTimestamp("created_at");
                            String createdAt = formatTimestampNullable(createdTs); // returns null when ts is null
                            if (createdAt == null) {
                                rowBuilder.addNull("createdAt");
                            } else {
                                rowBuilder.add("createdAt", createdAt);
                            }

                            String sessionId = rs.getString("session_id");
                            if (sessionId == null) {
                                rowBuilder.addNull("sessionId");
                            } else {
                                rowBuilder.add("sessionId", sessionId);
                            }

                            arrayBuilder.add(rowBuilder.build());
                        }
                    }
                }
            }

            int totalPages = totalRows == 0 ? 1 : (int) Math.ceil((double) totalRows / limit);
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("rows", arrayBuilder)
                    .add("totalRows", totalRows)
                    .add("totalPages", totalPages)
                    .add("page", page)
                    .build();

            // Stream JSON to client using JsonWriter over the response OutputStream (ensures UTF-8 bytes).
            try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
                writer.writeObject(body);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to read widget rows", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load widget data.\"}");
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

    private WidgetEntry findWidget(String widgetId) {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry widget : widgets) {
                if (widget != null && widgetId.equals(widget.getWidgetId())) {
                    return widget;
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
        }
        return null;
    }

    private String formatTimestampNullable(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return ts.toInstant().toString();
    }

    private int parseLimit(String limitParam) {
        try {
            int limit = Integer.parseInt(limitParam);
            if (limit == 10 || limit == 25 || limit == 50 || limit == 100) {
                return limit;
            }
        } catch (NumberFormatException ignored) {
        }
        return 10;
    }

    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam);
            return Math.max(1, page);
        } catch (NumberFormatException ignored) {
        }
        return 1;
    }

    private String parseSortColumn(String column) {
        if (column == null) {
            return "created_at";
        }
        String normalized = column.toLowerCase(Locale.ROOT);
        for (String candidate : COLUMNS) {
            if (candidate.equals(normalized)) {
                return candidate;
            }
        }
        return "created_at";
    }

    private String parseSortDirection(String direction) {
        if (direction == null) {
            return "DESC";
        }
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
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

    private static final class FilterState {

        private final String prompt;
        private final String response;
        private final String global;

        private FilterState(String prompt, String response, String global) {
            this.prompt = prompt;
            this.response = response;
            this.global = global;
        }

        private String buildWhereClause() {
            List<String> pieces = new ArrayList<>();
            if (hasValue(prompt)) {
                pieces.add("prompt ILIKE ?");
            }
            if (hasValue(response)) {
                pieces.add("response_text ILIKE ?");
            }
            if (hasValue(global)) {
                pieces.add("(prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)");
            }
            if (pieces.isEmpty()) {
                return "";
            }
            return " WHERE " + String.join(" AND ", pieces);
        }

        private List<String> params() {
            List<String> params = new ArrayList<>();
            if (hasValue(prompt)) {
                params.add(pattern(prompt));
            }
            if (hasValue(response)) {
                params.add(pattern(response));
            }
            if (hasValue(global)) {
                String globalPattern = pattern(global);
                for (int i = 0; i < 3; i++) {
                    params.add(globalPattern);
                }
            }
            return params;
        }

        private boolean hasValue(String val) {
            return val != null && !val.isBlank();
        }

        private String pattern(String input) {
            return "%" + input.trim() + "%";
        }
    }
}
