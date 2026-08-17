package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
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

@WebServlet(name = "WidgetTableDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/data"})
public class WidgetTableDataServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableDataServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final String[] COLUMNS = {
        "widget_chat_id",
        "prompt",
        "response_text",
        "created_at",
        "session_id"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        String widgetId = ServletRequestParamUtil.firstParam(req, "widgetId", 256, true, true);
        if (widgetId == null || widgetId.isBlank()) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId required.");
            return;
        }

        // NEW: optional date filter
        LocalDate selectedDate = null;
        String dateRaw = ServletRequestParamUtil.firstParam(req, "date", 256, true, true);
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid widget table data date parameter", ex);
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid date. Expected YYYY-MM-DD.");
                return;
            }
        }

        WidgetEntry plugin = findWidget(widgetId);
        if (plugin == null) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Widget not found.");
            return;
        }

        String tableName = sanitizeWidgetTableName(widgetId);
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Table for widget does not exist.");
                return;
            }
            JsonObject body = buildRowsPayload(conn, tableName, req, selectedDate);
            ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to read widget rows", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load widget data.");
        }
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private JsonObject buildRowsPayload(Connection conn, String tableName, HttpServletRequest req, LocalDate selectedDate)
            throws SQLException {
        int limit = parseLimit(ServletRequestParamUtil.firstParam(req, "limit", 256, true, true));
        int page = parsePage(ServletRequestParamUtil.firstParam(req, "page", 256, true, true));
        int offset = (page - 1) * limit;
        String search = ServletRequestParamUtil.firstParam(req, "search", 256, true, true);
        String sortColumn = parseSortColumn(ServletRequestParamUtil.firstParam(req, "sortColumn", 256, true, true));
        String sortDir = parseSortDirection(ServletRequestParamUtil.firstParam(req, "sortDir", 256, true, true));

        FilterState filters = new FilterState(
                ServletRequestParamUtil.firstParam(req, "filterPrompt", 256, true, true),
                ServletRequestParamUtil.firstParam(req, "filterResponse", 256, true, true),
                search,
                selectedDate
        );

        String whereClause = filters.buildWhereClause();
        List<Object> whereParams = filters.params();
        int totalRows = countRows(conn, tableName, whereClause, whereParams);
        JsonArrayBuilder arrayBuilder = totalRows > 0
                ? fetchRowArray(conn, tableName, whereClause, whereParams, sortColumn, sortDir, limit, offset)
                : Json.createArrayBuilder();

        int totalPages = Math.max(1, (totalRows + limit - 1) / limit);
        return Json.createObjectBuilder()
                .add("status", "ok")
                .add("rows", arrayBuilder)
                .add("totalRows", totalRows)
                .add("totalPages", totalPages)
                .add("page", page)
                .build();
    }

    private int countRows(Connection conn, String tableName, String whereClause, List<Object> whereParams) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName) + whereClause;
        try (PreparedStatement countPs = conn.prepareStatement(countSql)) {
            bindParams(countPs, whereParams);
            try (ResultSet rs = countPs.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private JsonArrayBuilder fetchRowArray(
            Connection conn,
            String tableName,
            String whereClause,
            List<Object> whereParams,
            String sortColumn,
            String sortDir,
            int limit,
            int offset
    ) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM ")
                .append(quoteIdentifier(tableName))
                .append(whereClause)
                .append(" ORDER BY ")
                .append(sortColumn)
                .append(' ')
                .append(sortDir)
                .append(" LIMIT ? OFFSET ?");

        List<ChatRow> rows = new ArrayList<>();
        Set<String> sessionIds = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            int idx = bindParams(ps, whereParams);
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sessionId = rs.getString("session_id");
                    if (sessionId != null && !sessionId.isBlank()) {
                        sessionIds.add(sessionId);
                    }

                    Timestamp createdAtTs = SqlTimeUtil.safeTimestamp(rs, "created_at");
                    rows.add(new ChatRow(
                            rs.getString("widget_chat_id"),
                            rs.getString("prompt"),
                            rs.getString("response_text"),
                            formatTimestampNullable(createdAtTs),
                            sessionId
                    ));
                }
            }
        }

        Map<String, SessionLabelStore.SessionLabel> labels = sessionIds.isEmpty()
                ? Collections.emptyMap()
                : SessionLabelStore.mapDisplayNames(sessionIds);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (ChatRow row : rows) {
            JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
            addNullableJson(rowBuilder, "chatId", row.chatId);
            addNullableJson(rowBuilder, "prompt", row.prompt);
            addNullableJson(rowBuilder, "response", row.response);
            addNullableJson(rowBuilder, "createdAt", row.createdAt);
            addNullableJson(rowBuilder, "sessionId", row.sessionId);

            String friendly = SessionLabelStore.resolveDisplayLabel(row.sessionId, labels.get(row.sessionId));
            rowBuilder.add("sessionIdDisplay", friendly);
            arrayBuilder.add(rowBuilder.build());
        }
        return arrayBuilder;
    }

    private void addNullableJson(JsonObjectBuilder rowBuilder, String key, String value) {
        if (value == null) {
            rowBuilder.addNull(key);
            return;
        }
        rowBuilder.add(key, value);
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            try {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } catch (IOException e) {
                log.log(Level.FINE, "Unable to write auth error response", e);
                if (resp != null && !resp.isCommitted()) {
                    try {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                    } catch (IOException ioe) {
                        log.log(Level.FINEST, "Fallback sendError failed", ioe);
                    }
                }
            }
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
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
        }
        return null;
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String formatTimestampNullable(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return ts.toInstant().toString();
    }

    private int parseLimit(String limitParam) {
        if (limitParam == null || limitParam.isBlank()) {
            return 10;
        }
        try {
            int limit = Integer.parseInt(limitParam);
            if (limit == 10 || limit == 20 || limit == 25 || limit == 50 || limit == 100) {
                return limit;
            }
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid limit parameter", ex);
        }
        return 10;
    }

    private int parsePage(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return Math.max(1, page);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid page parameter", ex);
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

    private int bindParams(PreparedStatement ps, List<Object> params) {
        int idx = 1;
        try {
            for (Object p : params) {
                if (p instanceof String s) {
                    ps.setString(idx++, s); 
                } else if (p instanceof Timestamp ts) {
                    ps.setTimestamp(idx++, ts); 
                } else {
                    ps.setObject(idx++, p);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to bind SQL parameters", e);
        }
        return idx;
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

    private static final class ChatRow {

        final String chatId;
        final String prompt;
        final String response;
        final String createdAt;
        final String sessionId;

        private ChatRow(String chatId, String prompt, String response, String createdAt, String sessionId) {
            this.chatId = chatId;
            this.prompt = prompt;
            this.response = response;
            this.createdAt = createdAt;
            this.sessionId = sessionId;
        }
    }

    private static final class FilterState {

        private final String prompt;
        private final String response;
        private final String global;
        private final LocalDate date; // NEW

        private FilterState(String prompt, String response, String global, LocalDate date) {
            this.prompt = prompt;
            this.response = response;
            this.global = global;
            this.date = date;
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
            if (date != null) {
                pieces.add("created_at >= ? AND created_at < ?");
            }

            if (pieces.isEmpty()) {
                return "";
            }
            return " WHERE " + String.join(" AND ", pieces);
        }

        private List<Object> params() {
            List<Object> params = new ArrayList<>();
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
            if (date != null) {
                params.add(Timestamp.valueOf(date.atStartOfDay()));
                params.add(Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            }
            return params;
        }

        private boolean hasValue(String val) {
            return val != null && !val.isBlank();
        }

        private String pattern(String input) {
            String trimmed = input.trim();
            return new StringBuilder(trimmed.length() + 2).append('%').append(trimmed).append('%').toString();
        }
    }
}
