package com.sim.chatserver.web.dashboard.drilldown;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

final class WidgetTableDataService {

    private static final Logger log = Logger.getLogger(WidgetTableDataServlet.class.getCanonicalName());
    private static final Logger owaspLog = Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    static final String EMPTY = "";
    private static final String PARAM_WIDGET_ID = com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID;
    private static final String PARAM_DATE = com.sim.chatserver.util.CommonLiteralConstants.PARAM_DATE;
    private static final String PARAM_LIMIT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT;
    private static final String PARAM_PAGE = CommonLiteralConstants.PARAM_PAGE;
    private static final String PARAM_SEARCH = CommonLiteralConstants.PARAM_SEARCH;
    private static final String PARAM_SORT_COLUMN = "sortColumn";
    private static final String PARAM_SORT_DIR = "sortDir";
    private static final String PARAM_FILTER_PROMPT = "filterPrompt";
    private static final String PARAM_FILTER_RESPONSE = "filterResponse";
    private static final String SESSION_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String DB_COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String DB_COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String DB_COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String DB_COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String JSON_KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String JSON_STATUS_OK = "ok";
    private static final String JSON_KEY_ROWS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ROWS;
    private static final String JSON_KEY_TOTAL_ROWS = "totalRows";
    private static final String JSON_KEY_TOTAL_PAGES = "totalPages";
    private static final String JSON_KEY_PAGE = CommonLiteralConstants.PARAM_PAGE;
    private static final String JSON_KEY_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CHAT_ID;
    private static final String JSON_KEY_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE;
    private static final String JSON_KEY_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CREATED_AT;
    private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_KEY_SESSION_ID_DISPLAY = "sessionIdDisplay";
    private static final String SORT_ASC_LOWER = "asc";
    private static final String SORT_ASC = "ASC";
    private static final String SORT_DESC = "DESC";
    private static final String TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
    private static final String DOUBLE_QUOTE = "\"";
    private static final String DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String SQL_COUNT_PREFIX = "SELECT COUNT(*) FROM ";
    private static final String SQL_SELECT_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
    private static final String SQL_ORDER_BY = " ORDER BY ";
    private static final String SQL_LIMIT_OFFSET = " LIMIT ? OFFSET ?";
    static final String SQL_FILTER_PROMPT = "prompt ILIKE ?";
    static final String SQL_FILTER_RESPONSE = "response_text ILIKE ?";
    static final String SQL_FILTER_GLOBAL = "(prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)";
    static final String SQL_FILTER_DATE = "created_at >= ? AND created_at < ?";
    static final String SQL_WHERE_PREFIX = " WHERE ";
    static final String SQL_AND = " AND ";
    private static final String MSG_WIDGET_ID_REQUIRED = "widgetId required.";
    private static final String MSG_INVALID_DATE = "Invalid date. Expected YYYY-MM-DD.";
    private static final String MSG_WIDGET_NOT_FOUND = "Widget not found.";
    private static final String MSG_WIDGET_TABLE_MISSING = "Table for widget does not exist.";
    private static final String MSG_UNABLE_READ_WIDGET_ROWS = "Unable to read widget rows";
    private static final String MSG_UNABLE_LOAD_WIDGET_DATA = "Unable to load widget data.";
    private static final String MSG_UNHANDLED_DOGGET = com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET;
    private static final String MSG_REQUEST_HANDLING_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED;
    private static final String MSG_FAILED_FALLBACK_SERVER_ERROR = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD;
    private static final String MSG_INVALID_DATE_PARAM_LOG = "Invalid widget table data date parameter";
    private static final String MSG_UNABLE_COUNT_ROWS = "Unable to count widget rows";
    private static final String MSG_UNABLE_FETCH_ROWS = "Unable to fetch widget rows";
    private static final String MSG_IGNORE_RS_CLOSE = "Ignoring ResultSet close failure";
    private static final String MSG_IGNORE_STATEMENT_CLOSE = "Ignoring Statement close failure";
    private static final String MSG_AUTH_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED;
    private static final String MSG_UNABLE_WRITE_AUTH_ERROR = "Unable to write auth error response";
    private static final String MSG_FALLBACK_SEND_ERROR_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
    private static final String MSG_UNABLE_LIST_WIDGETS = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS;
    private static final String MSG_INVALID_LIMIT = "Invalid limit parameter";
    private static final String MSG_INVALID_PAGE = "Invalid page parameter";
    private static final String MSG_UNABLE_BIND_PARAMS = "Unable to bind SQL parameters";
    private static final String MSG_UNABLE_INSPECT_TABLE_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;

    private static final String[] COLUMNS = {
        DB_COL_WIDGET_CHAT_ID,
        DB_COL_PROMPT,
        DB_COL_RESPONSE_TEXT,
        DB_COL_CREATED_AT,
        DB_COL_SESSION_ID
    };

    void handleGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        String widgetId = ServletRequestParamUtil.firstParam(req, PARAM_WIDGET_ID, 256, true, true);
        if (widgetId == null || widgetId.isBlank()) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_WIDGET_ID_REQUIRED);
            return;
        }

        // NEW: optional date filter
        LocalDate selectedDate = null;
        String dateRaw = ServletRequestParamUtil.firstParam(req, PARAM_DATE, 256, true, true);
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, MSG_INVALID_DATE_PARAM_LOG, ex);
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_INVALID_DATE);
                return;
            }
        }

        WidgetEntry plugin = findWidget(widgetId);
        if (plugin == null) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, MSG_WIDGET_NOT_FOUND);
            return;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_WIDGET_TABLE_MISSING);
                return;
            }
            JsonObject body = buildRowsPayload(conn, tableName, req, selectedDate);
            ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_UNABLE_READ_WIDGET_ROWS, e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_LOAD_WIDGET_DATA);
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            owaspLog.log(java.util.logging.Level.WARNING, MSG_UNHANDLED_DOGGET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    owaspLog.log(java.util.logging.Level.FINE, MSG_FAILED_FALLBACK_SERVER_ERROR, ioe);
                }
            }
        }
    }

    private JsonObject buildRowsPayload(Connection conn, String tableName, HttpServletRequest req, LocalDate selectedDate) {
        int limit = parseLimit(ServletRequestParamUtil.firstParam(req, PARAM_LIMIT, 256, true, true));
        int page = parsePage(ServletRequestParamUtil.firstParam(req, PARAM_PAGE, 256, true, true));
        int offset = (page - 1) * limit;
        String search = ServletRequestParamUtil.firstParam(req, PARAM_SEARCH, 256, true, true);
        String sortColumn = parseSortColumn(ServletRequestParamUtil.firstParam(req, PARAM_SORT_COLUMN, 256, true, true));
        String sortDir = parseSortDirection(ServletRequestParamUtil.firstParam(req, PARAM_SORT_DIR, 256, true, true));

        WidgetTableDataFilterState filters = new WidgetTableDataFilterState(
                ServletRequestParamUtil.firstParam(req, PARAM_FILTER_PROMPT, 256, true, true),
                ServletRequestParamUtil.firstParam(req, PARAM_FILTER_RESPONSE, 256, true, true),
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
            .add(JSON_KEY_STATUS, JSON_STATUS_OK)
            .add(JSON_KEY_ROWS, arrayBuilder)
            .add(JSON_KEY_TOTAL_ROWS, totalRows)
            .add(JSON_KEY_TOTAL_PAGES, totalPages)
            .add(JSON_KEY_PAGE, page)
                .build();
    }

    private int countRows(Connection conn, String tableName, String whereClause, List<Object> whereParams) {
        String countSql = SQL_COUNT_PREFIX + quoteIdentifier(tableName) + whereClause;
        PreparedStatement countPs = null;
        ResultSet rs = null;
        try {
            countPs = conn.prepareStatement(countSql);
            bindParams(countPs, whereParams);
            rs = countPs.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_COUNT_ROWS, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(countPs);
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
    ) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SQL_SELECT_PREFIX)
                .append(quoteIdentifier(tableName))
                .append(whereClause)
            .append(SQL_ORDER_BY)
                .append(sortColumn)
                .append(' ')
                .append(sortDir)
            .append(SQL_LIMIT_OFFSET);

        List<WidgetTableDataChatRow> rows = new ArrayList<>();
        Set<String> sessionIds = new HashSet<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sqlBuilder.toString());
            int idx = bindParams(ps, whereParams);
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            rs = ps.executeQuery();
            while (rs.next()) {
                String sessionId = rs.getString(DB_COL_SESSION_ID);
                if (sessionId != null && !sessionId.isBlank()) {
                    sessionIds.add(sessionId);
                }

                Timestamp createdAtTs = SqlTimeUtil.safeTimestamp(rs, DB_COL_CREATED_AT);
                rows.add(new WidgetTableDataChatRow(
                        rs.getString(DB_COL_WIDGET_CHAT_ID),
                        rs.getString(DB_COL_PROMPT),
                        rs.getString(DB_COL_RESPONSE_TEXT),
                        formatTimestampNullable(createdAtTs),
                        sessionId
                ));
            }

            Map<String, SessionLabel> labels = sessionIds.isEmpty()
                    ? Collections.emptyMap()
                    : SessionLabelStore.mapDisplayNames(sessionIds);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (WidgetTableDataChatRow row : rows) {
                JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
                addNullableJson(rowBuilder, JSON_KEY_CHAT_ID, row.chatId);
                addNullableJson(rowBuilder, DB_COL_PROMPT, row.prompt);
                addNullableJson(rowBuilder, JSON_KEY_RESPONSE, row.response);
                addNullableJson(rowBuilder, JSON_KEY_CREATED_AT, row.createdAt);
                addNullableJson(rowBuilder, JSON_KEY_SESSION_ID, row.sessionId);

                String friendly = SessionLabelStore.resolveDisplayLabel(row.sessionId, labels.get(row.sessionId));
                rowBuilder.add(JSON_KEY_SESSION_ID_DISPLAY, friendly);
                arrayBuilder.add(rowBuilder.build());
            }
            return arrayBuilder;
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_FETCH_ROWS, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    private static void closeQuietly(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            log.log(Level.FINEST, MSG_IGNORE_RS_CLOSE, e);
        }
    }

    private static void closeQuietly(Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
            log.log(Level.FINEST, MSG_IGNORE_STATEMENT_CLOSE, e);
        }
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
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            try {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, MSG_AUTH_REQUIRED);
            } catch (IOException e) {
                log.log(Level.FINE, MSG_UNABLE_WRITE_AUTH_ERROR, e);
                if (!resp.isCommitted()) {
                    try {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, MSG_AUTH_REQUIRED);
                    } catch (IOException ioe) {
                        log.log(Level.FINEST, MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
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
            log.log(Level.WARNING, MSG_UNABLE_LIST_WIDGETS, e);
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
            log.log(Level.FINE, MSG_INVALID_LIMIT, ex);
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
            log.log(Level.FINE, MSG_INVALID_PAGE, ex);
        }
        return 1;
    }

    private String parseSortColumn(String column) {
        if (column == null) {
            return DB_COL_CREATED_AT;
        }
        String normalized = column.toLowerCase(Locale.ROOT);
        for (String candidate : COLUMNS) {
            if (candidate.equals(normalized)) {
                return candidate;
            }
        }
        return DB_COL_CREATED_AT;
    }

    private String parseSortDirection(String direction) {
        if (direction == null) {
            return SORT_DESC;
        }
        return SORT_ASC_LOWER.equalsIgnoreCase(direction) ? SORT_ASC : SORT_DESC;
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
            throw new IllegalStateException(MSG_UNABLE_BIND_PARAMS, e);
        }
        return idx;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace(DOUBLE_QUOTE, DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{TABLE_TYPE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_INSPECT_TABLE_PREFIX + tableName, e);
        }
        return false;
    }

}

final class WidgetTableDataChatRow {

    final String chatId;
    final String prompt;
    final String response;
    final String createdAt;
    final String sessionId;

    WidgetTableDataChatRow(String chatId, String prompt, String response, String createdAt, String sessionId) {
        this.chatId = chatId;
        this.prompt = prompt;
        this.response = response;
        this.createdAt = createdAt;
        this.sessionId = sessionId;
    }
}

final class WidgetTableDataFilterState {

    private final String prompt;
    private final String response;
    private final String global;
    private final LocalDate date;

    WidgetTableDataFilterState(String prompt, String response, String global, LocalDate date) {
        this.prompt = prompt;
        this.response = response;
        this.global = global;
        this.date = date;
    }

    String buildWhereClause() {
        List<String> pieces = new ArrayList<>();
        if (hasValue(prompt)) {
            pieces.add(WidgetTableDataService.SQL_FILTER_PROMPT);
        }
        if (hasValue(response)) {
            pieces.add(WidgetTableDataService.SQL_FILTER_RESPONSE);
        }
        if (hasValue(global)) {
            pieces.add(WidgetTableDataService.SQL_FILTER_GLOBAL);
        }
        if (date != null) {
            pieces.add(WidgetTableDataService.SQL_FILTER_DATE);
        }

        if (pieces.isEmpty()) {
            return WidgetTableDataService.EMPTY;
        }
        return WidgetTableDataService.SQL_WHERE_PREFIX + String.join(WidgetTableDataService.SQL_AND, pieces);
    }

    List<Object> params() {
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


