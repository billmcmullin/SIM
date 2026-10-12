package com.sim.chatserver.web.dashboard.drilldown;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.Selection;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Optimized WidgetReviewDataServlet: - supports returning all selected entries
 * when limit=all or limit<=0 - single-pass data query (count via window
 * function) - PostgreSQL array binding instead of giant IN (?, ?, ...) - cached
 * table existence checks - cached widget display names
 */
final class WidgetReviewDataService {
    private static final Logger log = Logger.getLogger(WidgetReviewDataServlet.class.getCanonicalName());
    private static final Logger owaspLog = Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP);
    private static final String JSON_UTF8 = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final Pattern SAFE_SELECTION_ID = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final String JSON_KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String JSON_KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_STATUS_OK = "ok";
    private static final String JSON_STATUS_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String JSON_KEY_ROWS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ROWS;
    private static final String JSON_KEY_SEARCH_TERMS = "searchTerms";
    private static final String JSON_KEY_GLOBAL = "global";
    private static final String JSON_KEY_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String JSON_KEY_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE;
    private static final String JSON_KEY_TOTAL_ROWS = "totalRows";
    private static final String JSON_KEY_TOTAL_PAGES = "totalPages";
    private static final String JSON_KEY_PAGE = CommonLiteralConstants.PARAM_PAGE;
    private static final String JSON_KEY_LIMIT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT;
    private static final String JSON_KEY_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CHAT_ID;
    private static final String JSON_KEY_WIDGET_ID = com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID;
    private static final String JSON_KEY_WIDGET_NAME = "widgetName";
    private static final String JSON_KEY_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CREATED_AT;
    private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_KEY_SESSION_ID_DISPLAY = "sessionIdDisplay";
    private static final String EMPTY = "";
    private static final String DB_COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String DB_COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String DB_COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String DB_COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String SORT_ASC = "ASC";
    private static final String SORT_DESC = "DESC";
    private static final String TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
    private static final String MSG_UNABLE_TO_WRITE_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE;
    private static final String LOG_FALLBACK_SEND_ERROR_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
    private static final String SESSION_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String PARAM_SELECTION_ID = com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID;
    private static final String PARAM_DATE = com.sim.chatserver.util.CommonLiteralConstants.PARAM_DATE;
    private static final String PARAM_LIMIT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT;
    private static final String PARAM_PAGE = JSON_KEY_PAGE;
    private static final String PARAM_SEARCH = CommonLiteralConstants.PARAM_SEARCH;
    private static final String PARAM_SORT_COLUMN = "sortColumn";
    private static final String PARAM_SORT_DIR = "sortDir";
    private static final String DB_COL_TOTAL_COUNT = "total_count";
    private static final String VALUE_ONE = "1";
    private static final String VALUE_ZERO = "0";
    private static final String VALUE_ALL = com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL;
    private static final String VALUE_MAX = "max";
    private static final String VALUE_UNBOUNDED = "unbounded";
    private static final String SORT_ASC_LOWER = "asc";
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String DOUBLE_QUOTE = "\"";
    private static final String DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String MSG_REQUEST_HANDLING_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED;
    private static final String LOG_UNHANDLED_DOGGET = com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET;
    private static final String LOG_FAILED_FALLBACK_SERVER_ERROR = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD;
    private static final String LOG_REVIEW_TIMINGS = "review-data timings ms: prep={0} db={1} json={2} total={3} rows={4} totalRows={5} limit={6} page={7} unbounded={8}";
    private static final String LOG_REVIEW_SNAPSHOT_TIMINGS = "review-data snapshot timings ms: total={0} rows={1} totalRows={2} limit={3} page={4} unbounded={5}";
    private static final String LOG_UNABLE_FETCH_SELECTED_ROWS = "Unable to fetch selected rows";
    private static final String LOG_UNABLE_SESSION_LABELS = "Unable to resolve session labels for snapshots";
    private static final String LOG_INVALID_DATE_PARAM = "Invalid review-data date parameter";
    private static final String LOG_SELECTION_DATE_NOT_ISO = "Selection date is not in ISO format";
    private static final String LOG_UNABLE_WIDGET_NAME = "Unable to lookup widget display name";
    private static final String LOG_UNABLE_TABLE_METADATA_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
    private static final String LOG_INVALID_INT_PARAM = "Invalid integer parameter value";
    private static final String LOG_UNABLE_READ_DB_COL_PREFIX = "Unable to read DB column ";
    private static final String LOG_UNABLE_WRITE_JSON_TEXT = "Unable to write JSON text response";
    private static final String LOG_UNABLE_WRITE_JSON_OBJ = "Unable to write JSON object response";
    private static final String SQL_WHERE_WIDGET_CHAT_ID_ANY = " WHERE widget_chat_id = ANY (?)";
    private static final String SQL_AND_OPEN = " AND (";
    private static final String SQL_AND = " AND ";
    private static final String SQL_SELECT = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_PREFIX;
    private static final String SQL_FROM = "FROM ";
    private static final String SQL_ORDER_BY = " ORDER BY ";
    private static final String SQL_LIMIT_OFFSET = " LIMIT ? OFFSET ?";
        private static final String SQL_SEARCH_ANY_ILIKE =
            SQL_AND_OPEN + DB_COL_PROMPT + SqlLiteralConstants.SQL_ILIKE_OR + DB_COL_RESPONSE_TEXT
                + SqlLiteralConstants.SQL_ILIKE_OR + DB_COL_SESSION_ID + " ILIKE ?)";
        private static final String SQL_DATE_RANGE_FILTER =
            SQL_AND + DB_COL_CREATED_AT + " >= ? AND " + DB_COL_CREATED_AT + " < ?";
        private static final String SQL_REVIEW_SELECT_COLUMNS = DB_COL_WIDGET_CHAT_ID + SqlLiteralConstants.COMMA_SPACE
            + DB_COL_PROMPT + SqlLiteralConstants.COMMA_SPACE
            + DB_COL_RESPONSE_TEXT + SqlLiteralConstants.COMMA_SPACE
            + DB_COL_CREATED_AT + SqlLiteralConstants.COMMA_SPACE
            + DB_COL_SESSION_ID + ", COUNT(*) OVER() AS " + DB_COL_TOTAL_COUNT + ' ';
    private static final String JSON_ERR_SELECTION_ID_REQUIRED = "{\"status\":\"error\",\"message\":\"selectionId required.\"}";
    private static final String JSON_ERR_SELECTION_NOT_FOUND = "{\"status\":\"error\",\"message\":\"Selection not found.\"}";
    private static final String JSON_ERR_NO_CHAT_IDS = "{\"status\":\"error\",\"message\":\"No chat IDs specified.\"}";
    private static final String JSON_ERR_TABLE_NOT_EXISTS = "{\"status\":\"error\",\"message\":\"Table does not exist.\"}";
    private static final String JSON_ERR_UNABLE_LOAD_SELECTION = "{\"status\":\"error\",\"message\":\"Unable to load selection.\"}";
    private static final String JSON_ERR_INVALID_DATE = "{\"status\":\"error\",\"message\":\"Invalid date. Expected YYYY-MM-DD.\"}";
    private static final String JSON_ERR_AUTH_REQUIRED = "{\"status\":\"error\",\"message\":\"Authentication required.\"}";

    private static final String[] ALLOWED_SORT_COLUMNS = {
        DB_COL_WIDGET_CHAT_ID, DB_COL_PROMPT, DB_COL_CREATED_AT, DB_COL_SESSION_ID
    };

    private static final int DEFAULT_LIMIT = 50;
    // Raised to allow full retrieval from UI (was 200).
    private static final int MAX_LIMIT = 20000;
    private static final int DEFAULT_PAGE = 1;

    private final Map<String, String> tableExistsCache = new ConcurrentHashMap<>();
    private final Map<String, String> widgetNameCache = new ConcurrentHashMap<>();

    void handleGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        final long t0 = System.nanoTime();

        if (!isLoggedIn(req, resp)) {
            return;
        }

        String selectionId = sanitizeSelectionId(ServletRequestParamUtil.firstParam(req, PARAM_SELECTION_ID, 256, true, true));
        if (selectionId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, JSON_ERR_SELECTION_ID_REQUIRED);
            return;
        }

        LocalDate selectedDate = parseDate(ServletRequestParamUtil.firstParam(req, PARAM_DATE, 256, true, true), resp);
        if (selectedDate == LocalDate.MIN) {
            return;
        }

        HttpSession session = req.getSession(false);
        Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(resp, JSON_ERR_SELECTION_NOT_FOUND);
            return;
        }

        if (selectedDate == null && selection.date != null && !selection.date.isBlank()) {
            try {
                selectedDate = LocalDate.parse(selection.date.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, LOG_SELECTION_DATE_NOT_ISO, ex);
                // no date filter
            }
        }

        if (selection.hasSnapshots()) {
            handleSnapshotSelection(selection, req, resp, t0);
            return;
        }

        String widgetId = selection.widgetId;
        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        String widgetDisplayName = resolveWidgetDisplayNameCached(widgetId);

        String limitParam = ServletRequestParamUtil.firstParam(req, PARAM_LIMIT, 256, true, true);
        Integer rawLimit = parseIntegerOrNull(limitParam);
        boolean unboundedRequested = isUnlimitedLimit(limitParam, rawLimit);

        int page = Math.max(DEFAULT_PAGE, parseInteger(ServletRequestParamUtil.firstParam(req, PARAM_PAGE, 256, true, true), DEFAULT_PAGE));
        int offset;
        int limit;

        List<String> chatIds = selection.chatIds == null ? Collections.emptyList() : selection.chatIds;
        if (chatIds.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, JSON_ERR_NO_CHAT_IDS);
            return;
        }

        if (unboundedRequested) {
            // Return all selected IDs in one response (bounded by chatIds size).
            limit = chatIds.size() <= 0 ? MAX_LIMIT : Math.min(chatIds.size(), MAX_LIMIT);
            page = 1;
            offset = 0;
        } else {
            int parsed = valueOrDefault(rawLimit, DEFAULT_LIMIT);
            limit = clampLimit(parsed);
            offset = (page - 1) * limit;
        }

        String search = trimToNull(ServletRequestParamUtil.firstParam(req, PARAM_SEARCH, 256, true, true));
        String sortColumn = parseSortColumn(ServletRequestParamUtil.firstParam(req, PARAM_SORT_COLUMN, 256, true, true));
        String sortDir = parseSortDirection(ServletRequestParamUtil.firstParam(req, PARAM_SORT_DIR, 256, true, true));

        final long t1 = System.nanoTime();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            if (!tableExistsCached(conn, tableName)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(resp, JSON_ERR_TABLE_NOT_EXISTS);
                return;
            }

            QueryParts qp = buildQuery(tableName, sortColumn, sortDir, search, selectedDate);

            int totalRows = 0;
            List<ChatRow> rows = new ArrayList<>();
            Set<String> sessionIds = ConcurrentHashMap.newKeySet();

            try (PreparedStatement ps = conn.prepareStatement(qp.sql)) {
                int idx = 1;

                Array chatIdArray = conn.createArrayOf(SqlLiteralConstants.DB_TYPE_TEXT, chatIds.toArray(String[]::new));
                ps.setArray(idx++, chatIdArray);

                if (search != null) {
                    String pattern = '%' + search + '%';
                    ps.setString(idx++, pattern);
                    ps.setString(idx++, pattern);
                    ps.setString(idx++, pattern);
                }

                if (selectedDate != null) {
                    ps.setTimestamp(idx++, Timestamp.valueOf(selectedDate.atStartOfDay()));
                    ps.setTimestamp(idx++, Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay()));
                }

                ps.setInt(idx++, limit);
                ps.setInt(idx, offset);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (totalRows == 0) {
                            totalRows = rs.getInt(DB_COL_TOTAL_COUNT);
                        }

                        String sessionId = rs.getString(DB_COL_SESSION_ID);
                        if (sessionId != null && !sessionId.isBlank()) {
                            sessionIds.add(sessionId);
                        }

                        rows.add(new ChatRow(
                            nullable(rs, DB_COL_WIDGET_CHAT_ID),
                            nullable(rs, DB_COL_PROMPT),
                            rs.getString(DB_COL_RESPONSE_TEXT),
                            formatTimestamp(SqlTimeUtil.safeTimestamp(rs, DB_COL_CREATED_AT)),
                                sessionId
                        ));
                    }
                }
            }

            final long t2 = System.nanoTime();

            Map<String, SessionLabel> labels = sessionIds.isEmpty()
                    ? Collections.emptyMap()
                    : SessionLabelStore.mapDisplayNames(sessionIds);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (ChatRow row : rows) {
                String displaySession = SessionLabelStore.resolveDisplayLabel(row.sessionId, labels.get(row.sessionId));
                JsonObject jsonRow = Json.createObjectBuilder()
                    .add(JSON_KEY_CHAT_ID, row.chatId)
                    .add(JSON_KEY_WIDGET_ID, widgetId == null ? EMPTY : widgetId)
                    .add(JSON_KEY_WIDGET_NAME, widgetDisplayName == null ? EMPTY : widgetDisplayName)
                    .add(JSON_KEY_PROMPT, row.prompt)
                    .add(JSON_KEY_RESPONSE, row.response == null ? EMPTY : row.response)
                    .add(JSON_KEY_CREATED_AT, row.createdAt)
                    .add(JSON_KEY_SESSION_ID, row.sessionId == null ? EMPTY : row.sessionId)
                    .add(JSON_KEY_SESSION_ID_DISPLAY, displaySession)
                        .build();
                arrayBuilder.add(jsonRow);
            }

            int totalPages = computeTotalPages(totalRows, limit);
            SearchTerms st = normalizeSearchTerms(selection);

            JsonObject body = buildSuccessBody(arrayBuilder, st, totalRows, totalPages, page, limit);

            writeJson(resp, body);

            final long t3 = System.nanoTime();
            log.log(Level.INFO,
                    LOG_REVIEW_TIMINGS,
                    new Object[]{
                        twoDecimals(nsToMs(t1 - t0)),
                        twoDecimals(nsToMs(t2 - t1)),
                        twoDecimals(nsToMs(t3 - t2)),
                        twoDecimals(nsToMs(t3 - t0)),
                        Integer.toString(rows.size()),
                        Integer.toString(totalRows),
                        Integer.toString(limit),
                        Integer.toString(page),
                        Boolean.toString(unboundedRequested)
                    });
        } catch (SQLException e) {
            log.log(Level.SEVERE, LOG_UNABLE_FETCH_SELECTED_ROWS, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, JSON_ERR_UNABLE_LOAD_SELECTION);
        }
    
        } catch (IOException | IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            owaspLog.log(java.util.logging.Level.WARNING, LOG_UNHANDLED_DOGGET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    owaspLog.log(java.util.logging.Level.FINE, LOG_FAILED_FALLBACK_SERVER_ERROR, ioe);
                }
            }
        }
    }

    private void handleSnapshotSelection(
            Selection selection,
            HttpServletRequest req,
            HttpServletResponse resp,
            long t0) throws IOException {

        String search = trimToNull(ServletRequestParamUtil.firstParam(req, PARAM_SEARCH, 256, true, true));
        String sortColumn = parseSortColumn(ServletRequestParamUtil.firstParam(req, PARAM_SORT_COLUMN, 256, true, true));
        String sortDir = parseSortDirection(ServletRequestParamUtil.firstParam(req, PARAM_SORT_DIR, 256, true, true));

        String limitParam = ServletRequestParamUtil.firstParam(req, PARAM_LIMIT, 256, true, true);
        Integer rawLimit = parseIntegerOrNull(limitParam);
        boolean unboundedRequested = isUnlimitedLimit(limitParam, rawLimit);

        int page = Math.max(DEFAULT_PAGE, parseInteger(ServletRequestParamUtil.firstParam(req, PARAM_PAGE, 256, true, true), DEFAULT_PAGE));
        int limit;
        int offset;

        List<TermChatSnapshot> base = selection.snapshots == null ? Collections.emptyList() : selection.snapshots;
        if (unboundedRequested) {
            limit = Math.min(base.size() <= 0 ? MAX_LIMIT : base.size(), MAX_LIMIT);
            page = 1;
            offset = 0;
        } else {
            limit = clampLimit(valueOrDefault(rawLimit, DEFAULT_LIMIT));
            offset = (page - 1) * limit;
        }

        List<TermChatSnapshot> filtered = filterSnapshots(base, search);
        sortSnapshots(filtered, sortColumn, sortDir);

        int totalRows = filtered.size();
        int fromIndex = Math.min(offset, totalRows);
        int toIndex = Math.min(offset + limit, totalRows);
        List<TermChatSnapshot> pageRows = filtered.subList(fromIndex, toIndex);

        Set<String> sessionIds = pageRows.stream()
                .map(TermChatSnapshot::getSessionId)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toSet());

        Map<String, SessionLabel> labels = Collections.emptyMap();
        if (!sessionIds.isEmpty()) {
            try {
                labels = SessionLabelStore.mapDisplayNames(sessionIds);
            } catch (SQLException e) {
                log.log(Level.WARNING, LOG_UNABLE_SESSION_LABELS, e);
            }
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (TermChatSnapshot snapshot : pageRows) {
            String rawSession = snapshot.getSessionId();
            String displaySession = SessionLabelStore.resolveDisplayLabel(rawSession, labels.get(rawSession));
            String widgetId = snapshot.getWidgetId();
            String widgetName = resolveWidgetDisplayNameCached(widgetId);

            arrayBuilder.add(Json.createObjectBuilder()
                    .add(JSON_KEY_CHAT_ID, nullToEmpty(snapshot.getChatId()))
                    .add(JSON_KEY_WIDGET_ID, nullToEmpty(widgetId))
                    .add(JSON_KEY_WIDGET_NAME, nullToEmpty(widgetName))
                    .add(JSON_KEY_PROMPT, nullToEmpty(snapshot.getPrompt()))
                    .add(JSON_KEY_RESPONSE, nullToEmpty(snapshot.getResponse()))
                    .add(JSON_KEY_CREATED_AT, formatTimestamp(snapshot.getCreatedAt()))
                    .add(JSON_KEY_SESSION_ID, nullToEmpty(rawSession))
                    .add(JSON_KEY_SESSION_ID_DISPLAY, displaySession));
        }

        SearchTerms st = normalizeSearchTerms(selection);
        int totalPages = computeTotalPages(totalRows, limit);

        JsonObject body = buildSuccessBody(arrayBuilder, st, totalRows, totalPages, page, limit);

        writeJson(resp, body);

        final long t1 = System.nanoTime();
        log.log(Level.INFO,
            LOG_REVIEW_SNAPSHOT_TIMINGS,
            new Object[]{
                twoDecimals(nsToMs(t1 - t0)),
                Integer.toString(pageRows.size()),
                Integer.toString(totalRows),
                Integer.toString(limit),
                Integer.toString(page),
                Boolean.toString(unboundedRequested)
            });
    }

    private QueryParts buildQuery(String tableName, String sortColumn, String sortDir, String search, LocalDate selectedDate) {
        StringBuilder where = new StringBuilder(SQL_WHERE_WIDGET_CHAT_ID_ANY);

        if (search != null) {
            where.append(SQL_SEARCH_ANY_ILIKE);
        }

        if (selectedDate != null) {
            where.append(SQL_DATE_RANGE_FILTER);
        }

        String sql = SQL_SELECT + SQL_REVIEW_SELECT_COLUMNS
                + SQL_FROM + quoteIdentifier(tableName)
                + where
            + SQL_ORDER_BY + sortColumn + ' ' + sortDir
                + SQL_LIMIT_OFFSET;

        return new QueryParts(sql);
    }

    private LocalDate parseDate(String raw, HttpServletResponse resp) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            log.log(Level.FINE, LOG_INVALID_DATE_PARAM, ex);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, JSON_ERR_INVALID_DATE);
            return LocalDate.MIN;
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, JSON_ERR_AUTH_REQUIRED);
            return false;
        }
        return true;
    }

    private String resolveWidgetDisplayNameCached(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return null;
        }
        return widgetNameCache.computeIfAbsent(widgetId, this::resolveWidgetDisplayNameUncached);
    }

    private String resolveWidgetDisplayNameUncached(String widgetId) {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry w : widgets) {
                if (w != null && widgetId.equals(w.getWidgetId())) {
                    String dn = w.getDisplayName();
                    return (dn == null || dn.isBlank()) ? widgetId : dn;
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_UNABLE_WIDGET_NAME, e);
        }
        return widgetId;
    }

    private boolean tableExistsCached(Connection conn, String tableName) {
        String cached = tableExistsCache.get(tableName);
        if (cached != null) {
            return VALUE_ONE.equals(cached);
        }

        boolean exists = tableExists(conn, tableName);
        tableExistsCache.put(tableName, exists ? VALUE_ONE : VALUE_ZERO);
        return exists;
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{TABLE_TYPE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_UNABLE_TABLE_METADATA_PREFIX + tableName, e);
        }
        return false;
    }

    private int clampLimit(int raw) {
        if (raw < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(raw, MAX_LIMIT);
    }

    private boolean isUnlimitedLimit(String rawLimitParam, Integer parsed) {
        if (rawLimitParam == null) {
            return false;
        }
        String t = rawLimitParam.trim().toLowerCase(Locale.ROOT);
        if (t.isEmpty()) {
            return false;
        }
        if (VALUE_ALL.equals(t) || VALUE_MAX.equals(t) || VALUE_UNBOUNDED.equals(t)) {
            return true;
        }
        Object parsedValue = parsed;
        return parsedValue != null && Integer.parseInt(parsedValue.toString()) <= 0;
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, LOG_INVALID_INT_PARAM, ex);
            return null;
        }
    }

    private int valueOrDefault(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return Integer.parseInt(value.toString());
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String sanitizeSelectionId(String rawSelectionId) {
        String value = trimToNull(rawSelectionId);
        if (value == null || !SAFE_SELECTION_ID.matcher(value).matches()) {
            return null;
        }
        return value;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(DOUBLE_QUOTE, DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private List<TermChatSnapshot> filterSnapshots(List<TermChatSnapshot> base, String search) {
        if (search == null || search.isBlank()) {
            return new ArrayList<>(base);
        }
        String normalized = search.trim().toLowerCase(Locale.ROOT);
        return base.stream()
                .filter(snapshot
                        -> containsIgnoreCase(snapshot.getPrompt(), normalized)
                || containsIgnoreCase(snapshot.getResponse(), normalized)
                || containsIgnoreCase(snapshot.getSessionId(), normalized))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String needle) {
        return source != null && needle != null && source.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String nullable(ResultSet rs, String column) {
        if (rs == null || column == null || column.isBlank()) {
            return EMPTY;
        }
        try {
            String value = rs.getString(column);
            return value == null ? EMPTY : value;
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_UNABLE_READ_DB_COL_PREFIX + column, e);
            return EMPTY;
        }
    }

    private String formatTimestamp(Timestamp ts) {
        return ts == null ? EMPTY : ISO_INSTANT_FMT.format(ts.toInstant());
    }

    private String parseSortColumn(String column) {
        if (column == null) {
            return DB_COL_CREATED_AT;
        }
        String normalized = column.toLowerCase(Locale.ROOT);
        for (String candidate : ALLOWED_SORT_COLUMNS) {
            if (candidate.equals(normalized)) {
                return candidate;
            }
        }
        return DB_COL_CREATED_AT;
    }

    private String parseSortDirection(String direction) {
        return SORT_ASC_LOWER.equalsIgnoreCase(direction) ? SORT_ASC : SORT_DESC;
    }

    private int parseInteger(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, LOG_INVALID_INT_PARAM, ex);
            return fallback;
        }
    }

    private void sortSnapshots(List<TermChatSnapshot> data, String column, String direction) {
        Comparator<TermChatSnapshot> comparator = switch (column) {
            case DB_COL_PROMPT ->
                Comparator.comparing(s -> nullToEmpty(s.getPrompt()), String.CASE_INSENSITIVE_ORDER);
            case DB_COL_SESSION_ID ->
                Comparator.comparing(s -> nullToEmpty(s.getSessionId()), String.CASE_INSENSITIVE_ORDER);
            case DB_COL_WIDGET_CHAT_ID ->
                Comparator.comparing(s -> nullToEmpty(s.getChatId()), String.CASE_INSENSITIVE_ORDER);
            case DB_COL_CREATED_AT ->
                Comparator.comparing(s -> s.getCreatedAt() == null ? 0L : s.getCreatedAt().getTime());
            default ->
                Comparator.comparing(s -> nullToEmpty(s.getChatId()), String.CASE_INSENSITIVE_ORDER);
        };
        if (SORT_DESC.equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        data.sort(comparator);
    }

    private SearchTerms normalizeSearchTerms(Selection selection) {
        String g = EMPTY;
        String p = EMPTY;
        String r = EMPTY;
        if (selection != null && selection.searchTerms != null) {
            g = nullToEmpty(selection.searchTerms.global);
            p = nullToEmpty(selection.searchTerms.prompt);
            r = nullToEmpty(selection.searchTerms.response);
        }
        return new SearchTerms(g, p, r);
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String nullToEmpty(String s) {
        return s == null ? EMPTY : s;
    }

    private double nsToMs(long ns) {
        return ns / 1_000_000.0;
    }

    private String twoDecimals(double value) {
        long scaled = Math.round(value * 100.0);
        long whole = scaled / 100;
        long fraction = Math.abs(scaled % 100);
        StringBuilder b = new StringBuilder();
        b.append(whole).append('.');
        if (fraction < 10) {
            b.append('0');
        }
        b.append(fraction);
        return b.toString();
    }

    private int computeTotalPages(int totalRows, int limit) {
        long safeTotal = Math.max(0L, (long) totalRows);
        long safeLimit = Math.max(1L, (long) limit);
        long pages = (safeTotal + safeLimit - 1L) / safeLimit;
        long normalized = Math.max(1L, pages);
        if (normalized >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(normalized);
    }

    private JsonObject buildSuccessBody(
            JsonArrayBuilder rows,
            SearchTerms st,
            int totalRows,
            int totalPages,
            int page,
            int limit) {
        return Json.createObjectBuilder()
                .add(JSON_KEY_STATUS, JSON_STATUS_OK)
                .add(JSON_KEY_ROWS, rows)
                .add(JSON_KEY_SEARCH_TERMS, Json.createObjectBuilder()
                        .add(JSON_KEY_GLOBAL, st.global)
                        .add(JSON_KEY_PROMPT, st.prompt)
                        .add(JSON_KEY_RESPONSE, st.response))
                .add(JSON_KEY_TOTAL_ROWS, totalRows)
                .add(JSON_KEY_TOTAL_PAGES, totalPages)
                .add(JSON_KEY_PAGE, page)
                .add(JSON_KEY_LIMIT, limit)
                .build();
    }

    private void writeJson(HttpServletResponse resp, String body) {
        try {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType(JSON_UTF8);
            resp.getWriter().write(body);
        } catch (IOException e) {
            log.log(Level.WARNING, LOG_UNABLE_WRITE_JSON_TEXT, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, LOG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, JsonObject body) {
        int status = resp.getStatus();
        if (status <= 0) {
            status = HttpServletResponse.SC_OK;
        }
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, LOG_UNABLE_WRITE_JSON_OBJ, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, LOG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

}

final class QueryParts {

    final String sql;

    QueryParts(String sql) {
        this.sql = sql;
    }
}

final class SearchTerms {

    final String global;
    final String prompt;
    final String response;

    SearchTerms(String global, String prompt, String response) {
        this.global = global;
        this.prompt = prompt;
        this.response = response;
    }
}

final class ChatRow {

    final String chatId;
    final String prompt;
    final String response;
    final String createdAt;
    final String sessionId;

    ChatRow(String chatId, String prompt, String response, String createdAt, String sessionId) {
        this.chatId = chatId;
        this.prompt = prompt;
        this.response = response;
        this.createdAt = createdAt;
        this.sessionId = sessionId;
    }
}

