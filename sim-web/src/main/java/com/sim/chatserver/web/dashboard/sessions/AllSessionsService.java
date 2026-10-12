package com.sim.chatserver.web.dashboard.sessions;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.TextIoSanitizerUtil;
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

    private static final Logger log = Logger.getLogger(AllSessionsServlet.class.getCanonicalName());
    private static final int ACTIVE_DAYS = 7;
    private static final int MAX_SEARCH_LENGTH = 128;
    private static final int MAX_SESSION_ID_LENGTH = 128;
    private static final Pattern SAFE_SESSION_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");
    private static final Pattern SAFE_CHAT_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final Pattern SAFE_INT_PARAM = Pattern.compile("^-?\\d{1,10}$");
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final String JSON_KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String JSON_KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_STATUS_OK = "ok";
    private static final String JSON_STATUS_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String JSON_KEY_SELECTED_CHAT_IDS = com.sim.chatserver.util.CommonLiteralConstants.KEY_SELECTED_CHAT_IDS;
    private static final String JSON_KEY_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String REGEX_WIDGET_TABLE = com.sim.chatserver.util.CommonLiteralConstants.REGEX_NON_ALNUM_UNDERSCORE;
    private static final String REPLACEMENT_UNDERSCORE = "_";
    private static final String SESSION_USER_ATTR = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SQL_IDENTIFIER_INVALID_MSG = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String SQL_QUOTE = "\"";
    private static final String SQL_QUOTE_ESCAPED = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String VALUE_WIDGET = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
    private static final String VALUE_WIDGET_PREFIX = "w_";
    private static final String VALUE_TRUE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
    private static final String PARAM_ALL = com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL;
    private static final String PARAM_LABELED_ONLY = "labeledOnly";
    private static final String PARAM_SEARCH = CommonLiteralConstants.PARAM_SEARCH;
    private static final String PARAM_ACTIVITY = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ACTIVITY;
    private static final String PARAM_LIMIT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT;
    private static final String PARAM_PAGE = CommonLiteralConstants.PARAM_PAGE;
    private static final String PARAM_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String DB_COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String DB_COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String DB_COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String DB_COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String DB_COL_CNT = "cnt";
    private static final String DB_COL_FIRST_TS = "first_ts";
    private static final String DB_COL_LAST_TS = "last_ts";
    private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_KEY_SESSION_ID_DISPLAY = "sessionIdDisplay";
    private static final String JSON_KEY_TOTAL_COUNT = "totalCount";
    private static final String JSON_KEY_FIRST_SEEN = "firstSeen";
    private static final String JSON_KEY_LAST_SEEN = "lastSeen";
    private static final String JSON_KEY_WIDGETS = "widgets";
    private static final String JSON_KEY_ACTIVE_DAYS = "activeDays";
    private static final String JSON_KEY_ACTIVITY = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ACTIVITY;
    private static final String JSON_KEY_LABELED_ONLY = "labeledOnly";
    private static final String JSON_KEY_TOTAL_USERS = "totalUsers";
    private static final String JSON_KEY_ACTIVE_USERS = "activeUsers";
    private static final String JSON_KEY_INACTIVE_USERS = "inactiveUsers";
    private static final String JSON_KEY_TOTAL_SESSIONS = "totalSessions";
    private static final String JSON_KEY_TOTAL_PAGES = "totalPages";
    private static final String JSON_KEY_PAGE = CommonLiteralConstants.PARAM_PAGE;
    private static final String JSON_KEY_WIDGET_NAMES = "widgetNames";
    private static final String JSON_KEY_SESSIONS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSIONS;
    private static final String JSON_KEY_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CHAT_ID;
    private static final String JSON_KEY_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CREATED_AT;
    private static final String JSON_KEY_ROWS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ROWS;
    private static final String JSON_KEY_SELECTION_ID = com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID;
    private static final String JSON_KEY_COUNT = com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT;
    private static final String MSG_AUTH_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED;
    private static final String MSG_INVALID_JSON_PAYLOAD = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD;
    private static final String MSG_UNABLE_TO_CREATE_SELECTION = "Unable to create selection.";
    private static final String MSG_METHOD_NOT_ALLOWED = "Method not allowed.";
    private static final String MSG_SESSION_ID_REQUIRED = "sessionId required.";
    private static final String MSG_NO_VALID_CHAT_IDS = "No valid chat IDs provided.";
    private static final String MSG_NO_MATCHING_CHATS = "No matching chats found for selection.";
    private static final String MSG_REQUIRED_SUFFIX = " required.";
    private static final String MSG_REQUEST_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED;
    private static final String MSG_UNABLE_COMPUTE_SUMMARY = "Unable to compute session summary";
    private static final String MSG_UNABLE_LOAD_SESSIONS = "Unable to load sessions.";
    private static final String MSG_UNABLE_APPLY_SEARCH_FILTER = "Unable to apply prompt/response search filter";
    private static final String MSG_UNABLE_LOAD_CHATS = "Unable to load chats for session";
    private static final String MSG_UNABLE_LOAD_CHATS_RESPONSE = "Unable to load chats for session.";
    private static final String MSG_UNABLE_COLLECT_SELECTED_CHATS = "Unable to collect selected session chats";
    private static final String MSG_UNABLE_LOAD_SESSION_LABELS = "Unable to load session labels";
    private static final String MSG_UNABLE_GATHER_SESSION_IDS = "Unable to gather session IDs for search";
    private static final String MSG_UNABLE_EXECUTE_AGGREGATE = "Unable to execute aggregate query for table ";
    private static final String MSG_UNABLE_AGGREGATE_SESSION_DATA = "Unable to aggregate session data for table ";
    private static final String MSG_UNABLE_LIST_WIDGETS = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_LIST_WIDGETS;
    private static final String MSG_UNABLE_INSPECT_TABLE_METADATA = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
    private static final String MSG_UNABLE_WRITE_JSON_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_RESPONSE;
    private static final String MSG_UNABLE_WRITE_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE;
    private static final String MSG_FALLBACK_SEND_ERROR_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
    private static final String MSG_UNABLE_WRITE_JSON_ERROR = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_ERROR_RESPONSE;
    private static final String MSG_INVALID_INTEGER_PARAM = "Invalid integer parameter value";
    private static final String MSG_UNABLE_OPEN_DB_CONNECTION = "Unable to open database connection";
    private static final String MSG_UNABLE_PREPARE_SQL = "Unable to prepare SQL statement";
    private static final String MSG_DB_TEXT_READ_FAILED = "String DB text read failed for column ";
    private static final String MSG_INVALID_DB_INT = "Invalid DB integer for column ";
    private static final String MSG_INSTANT_PARSE_FALLBACK = "Instant parse fallback for DB timestamp column ";
    private static final String MSG_TIMESTAMP_PARSE_FAILED = "Timestamp parse failed for DB column ";
    private static final String SQL_SELECT_CHATS_BY_SESSION = "SELECT widget_chat_id, prompt, created_at FROM ";
    private static final String SQL_WHERE_SESSION_ORDER = " WHERE session_id = ? ORDER BY created_at DESC";
    private static final String SQL_SELECT_CHATS_BY_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
    private static final String SQL_WHERE_WIDGET_CHAT_ID = " WHERE widget_chat_id = ?";
    private static final String SQL_SELECT_DISTINCT_SESSION = "SELECT DISTINCT session_id FROM ";
    private static final String SQL_WHERE_PROMPT_OR_RESPONSE = " WHERE (prompt ILIKE ? OR response_text ILIKE ?)";
    private static final String SQL_SELECT_SESSION_AGG = "SELECT session_id, COUNT(*) AS cnt, MIN(created_at) AS first_ts, MAX(created_at) AS last_ts FROM ";
    private static final String SQL_WHERE_GROUP_BY_SESSION = com.sim.chatserver.util.CommonLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;
    private static final String PATH_REDIRECT_DASHBOARD_SESSIONS = "/dashboard/sessions";
    private static final String LABEL_SELECTED_SESSION_CHATS = "Selected Session Chats";
    private static final String ACTIVITY_ALL = com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL;
    private static final String ACTIVITY_ACTIVE = "active";
    private static final String ACTIVITY_INACTIVE = "inactive";

    private static final String PATH_DATA = "/dashboard/sessions/data";
    private static final String PATH_CHATS = "/dashboard/sessions/chats";
    private static final String PATH_SELECT = "/dashboard/sessions/select";

    void handleGet(HttpServletRequest req, HttpServletResponse resp) {
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

        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, MSG_METHOD_NOT_ALLOWED);
    }

    private void handleSummary(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        String allParam = ServletRequestParamUtil.firstParam(req, PARAM_ALL, 256, false, false);
        String labeledOnlyParam = ServletRequestParamUtil.firstParam(req, PARAM_LABELED_ONLY, 256, false, false);
        String searchParam = ServletRequestParamUtil.firstParam(req, PARAM_SEARCH, 256, false, false);
        String activityParam = ServletRequestParamUtil.firstParam(req, PARAM_ACTIVITY, 256, false, false);
        String limitParam = ServletRequestParamUtil.firstParam(req, PARAM_LIMIT, 256, false, false);
        String pageParam = ServletRequestParamUtil.firstParam(req, PARAM_PAGE, 256, false, false);

        boolean returnAll = parseBooleanParam(allParam);
        boolean labeledOnly = parseBooleanParam(labeledOnlyParam);

        String search = sanitizeTextParam(searchParam, MAX_SEARCH_LENGTH);
        String searchTerm = search == null ? EMPTY : search.trim();
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
        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

        if (!widgets.isEmpty()) {
            Connection conn = openConnectionSafe();
            try (conn) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                    if (!tableExists(conn, tableName, tableExistsCache)) {
                        continue;
                    }
                    aggregateSessions(conn, tableName, sessions, widget.getWidgetId(), null);
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, MSG_UNABLE_COMPUTE_SUMMARY, e);
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_LOAD_SESSIONS);
                return;
            }
        }

        List<SessionSummary> sessionList = new ArrayList<>(sessions.values());

        if (hasSearch && !sessionList.isEmpty()) {
            Set<String> matchedSessionIds = new HashSet<>();

            matchedSessionIds.addAll(gatherSessionIdsByIdMatch(sessions, search));

            Map<String, SessionLabel> allLabels = mapSessionLabels(sessionList);
            matchedSessionIds.addAll(gatherSessionIdsByLabelMatch(allLabels, search));

            Connection conn = openConnectionSafe();
            try (conn) {
                for (WidgetEntry widget : widgets) {
                    if (widget == null || widget.getWidgetId() == null) {
                        continue;
                    }
                    String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                    if (!tableExists(conn, tableName, tableExistsCache)) {
                        continue;
                    }
                    matchedSessionIds.addAll(gatherSessionIdsForSearch(conn, tableName, normalizedSearch));
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, MSG_UNABLE_APPLY_SEARCH_FILTER, e);
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
            Map<String, SessionLabel> labelsForFilter = mapSessionLabels(sessionList);
            sessionList.removeIf(s -> {
                if (s == null || s.sessionId == null) {
                    return true;
                }
                SessionLabel label = labelsForFilter.get(s.sessionId);
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

        Map<String, SessionLabel> labels = mapSessionLabels(sessionList);

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (SessionSummary summary : sessionList) {
            String displayLabel = SessionLabelStore.resolveDisplayLabel(summary.sessionId, labels.get(summary.sessionId));
            array.add(Json.createObjectBuilder()
                    .add(JSON_KEY_SESSION_ID, summary.sessionId)
                    .add(JSON_KEY_SESSION_ID_DISPLAY, displayLabel)
                    .add(JSON_KEY_TOTAL_COUNT, summary.totalCount)
                    .add(JSON_KEY_FIRST_SEEN, formatInstant(summary.firstSeen))
                    .add(JSON_KEY_LAST_SEEN, formatInstant(summary.lastSeen))
                    .add(JSON_KEY_WIDGETS, Json.createArrayBuilder(summary.widgetIds))
                    .build());
        }

        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();
        for (Map.Entry<String, String> e : widgetNames.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue());
        }

        JsonObject body = Json.createObjectBuilder()
            .add(JSON_KEY_STATUS, JSON_STATUS_OK)
                .add(JSON_KEY_ACTIVE_DAYS, ACTIVE_DAYS)
                .add(JSON_KEY_ACTIVITY, activity)
                .add(JSON_KEY_LABELED_ONLY, labeledOnly)
                .add(JSON_KEY_TOTAL_USERS, totalUsers)
                .add(JSON_KEY_ACTIVE_USERS, activeUsers)
                .add(JSON_KEY_INACTIVE_USERS, inactiveUsers)
                .add(JSON_KEY_TOTAL_SESSIONS, totalSessions)
                .add(JSON_KEY_TOTAL_PAGES, totalPages)
                .add(JSON_KEY_PAGE, page)
                .add(JSON_KEY_WIDGET_NAMES, widgetNamesObj.build())
                .add(JSON_KEY_SESSIONS, array)
                .build();
            writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    private void handleChats(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        String sessionId = sanitizeSessionId(ServletRequestParamUtil.firstParam(req, PARAM_SESSION_ID, 256, false, false));
        if (sessionId == null || sessionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_SESSION_ID_REQUIRED);
            return;
        }

        List<WidgetEntry> widgets = listWidgets();
        List<ChatRow> rows = new ArrayList<>();
        String sid = sessionId;

        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
        Connection conn = openConnectionSafe();
        try (conn) {
            forEachExistingWidgetTable(conn, widgets, tableExistsCache, (widgetId, tableName) -> {
                String sql = SQL_SELECT_CHATS_BY_SESSION + quoteIdentifier(tableName)
                    + SQL_WHERE_SESSION_ORDER;
                PreparedStatement ps = prepareStatementSafe(conn, sql);
                try (ps) {
                    ps.setString(1, sid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = sanitizeChatId(readDbText(rs, DB_COL_WIDGET_CHAT_ID, MAX_SESSION_ID_LENGTH));
                            rows.add(new ChatRow(
                                    chatId == null ? EMPTY : chatId,
                                    readDbText(rs, DB_COL_PROMPT, 4000),
                                    readDbTimestamp(rs, DB_COL_CREATED_AT)
                            ));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_UNABLE_LOAD_CHATS, e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_LOAD_CHATS_RESPONSE);
            return;
        }

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (ChatRow r : rows) {
            array.add(Json.createObjectBuilder()
                    .add(JSON_KEY_CHAT_ID, r.chatId == null ? EMPTY : r.chatId)
                    .add(JSON_KEY_PROMPT, r.prompt == null ? EMPTY : r.prompt)
                    .add(JSON_KEY_CREATED_AT, formatTimestamp(r.createdAt))
                    .build());
        }

        JsonObject body = Json.createObjectBuilder()
                .add(JSON_KEY_STATUS, JSON_STATUS_OK)
            .add(JSON_KEY_ROWS, array)
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    private void handleSelect(HttpServletRequest req, HttpServletResponse resp) {
        if (!requireAuth(req, resp)) {
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            JsonObject body = Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_INVALID_JSON_PAYLOAD)
                    .build();
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, body);
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        if (!payload.containsKey(JSON_KEY_SELECTED_CHAT_IDS)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, JSON_KEY_SELECTED_CHAT_IDS + MSG_REQUIRED_SUFFIX);
            return;
        }

        Set<String> selected = new LinkedHashSet<>();
        var selectedIdsArray = payload.getJsonArray(JSON_KEY_SELECTED_CHAT_IDS);
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
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_NO_VALID_CHAT_IDS);
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
        Connection conn = openConnectionSafe();
        try (conn) {
            forEachExistingWidgetTable(conn, widgets, tableExistsCache, (widgetId, tableName) -> {
                String sql = SQL_SELECT_CHATS_BY_ID
                        + quoteIdentifier(tableName)
                    + SQL_WHERE_WIDGET_CHAT_ID;

                PreparedStatement ps = prepareStatementSafe(conn, sql);
                try (ps) {
                    for (String chatId : selected) {
                        ps.setString(1, chatId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String foundChatId = sanitizeChatId(readDbText(rs, DB_COL_WIDGET_CHAT_ID, MAX_SESSION_ID_LENGTH));
                                String prompt = readDbText(rs, DB_COL_PROMPT, 4000);
                                String responseText = readDbText(rs, DB_COL_RESPONSE_TEXT, 8000);
                                Timestamp createdAt = readDbTimestamp(rs, DB_COL_CREATED_AT);
                                String sessionId = sanitizeSessionId(readDbText(rs, DB_COL_SESSION_ID, MAX_SESSION_ID_LENGTH));

                                snapshots.add(new TermChatSnapshot(
                                    LABEL_SELECTED_SESSION_CHATS,
                                        widgetId,
                                        foundChatId == null ? EMPTY : foundChatId,
                                        prompt,
                                        responseText,
                                        createdAt,
                                        sessionId
                                ));
                            }
                        }
                    }
                }
            });
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_UNABLE_COLLECT_SELECTED_CHATS, e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_CREATE_SELECTION);
            return;
        }

        if (snapshots.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, MSG_NO_MATCHING_CHATS);
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                req.getSession(false),
                LABEL_SELECTED_SESSION_CHATS,
                snapshots,
            ServletPathUtil.safeContextPathNoEmptyGuard(req.getServletContext().getContextPath()) + PATH_REDIRECT_DASHBOARD_SESSIONS
        );

        if (selectionId == null || selectionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_CREATE_SELECTION);
            return;
        }

        JsonObject body = Json.createObjectBuilder()
            .add(JSON_KEY_STATUS, JSON_STATUS_OK)
                .add(JSON_KEY_SELECTION_ID, selectionId)
                .add(JSON_KEY_COUNT, snapshots.size())
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    @FunctionalInterface
    private interface WidgetTableOperation {
        void apply(String widgetId, String tableName) throws SQLException;
    }

    private void forEachExistingWidgetTable(
            Connection conn,
            List<WidgetEntry> widgets,
            Map<String, Boolean> tableExistsCache,
            WidgetTableOperation operation)
            throws SQLException {
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }

            String widgetId = widget.getWidgetId();
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName, tableExistsCache)) {
                continue;
            }

            operation.apply(widgetId, tableName);
        }
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

    private Map<String, SessionLabel> mapSessionLabels(List<SessionSummary> summaries) {
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
            log.log(Level.WARNING, MSG_UNABLE_LOAD_SESSION_LABELS, e);
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

    private Set<String> gatherSessionIdsByLabelMatch(Map<String, SessionLabel> labels, String search) {
        if (search == null || search.isBlank() || labels == null || labels.isEmpty()) {
            return Collections.emptySet();
        }
        String q = search.trim().toLowerCase();
        Set<String> out = new HashSet<>();
        for (Map.Entry<String, SessionLabel> e : labels.entrySet()) {
            String sid = e.getKey();
            SessionLabel label = e.getValue();
            if (sid == null || label == null) {
                continue;
            }

            String displayName = label.getDisplayName() == null ? EMPTY : label.getDisplayName();
            String email = label.getEmail() == null ? EMPTY : label.getEmail();

            if (displayName.toLowerCase().contains(q) || email.toLowerCase().contains(q)) {
                out.add(sid);
            }
        }
        return out;
    }

    private Set<String> gatherSessionIdsForSearch(Connection conn, String tableName, String pattern) {
        Set<String> ids = new HashSet<>();
        String sql = SQL_SELECT_DISTINCT_SESSION + quoteIdentifier(tableName)
            + SQL_WHERE_PROMPT_OR_RESPONSE;
        PreparedStatement ps = prepareStatementSafe(conn, sql);
        try (ps) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = sanitizeSessionId(readDbText(rs, DB_COL_SESSION_ID, 256));
                    if (sid != null) {
                        ids.add(sid);
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_GATHER_SESSION_IDS, e);
        }
        return ids;
    }

    private void aggregateSessions(Connection conn, String tableName, Map<String, SessionSummary> sessions, String widgetId, Set<String> filter) {
        String sql = SQL_SELECT_SESSION_AGG
            + quoteIdentifier(tableName) + SQL_WHERE_GROUP_BY_SESSION;
        PreparedStatement ps = prepareStatementSafe(conn, sql);
        try (ps) {
            ResultSet queryResult;
            try {
                queryResult = ps.executeQuery();
            } catch (SQLException ex) {
                log.log(Level.FINE, MSG_UNABLE_EXECUTE_AGGREGATE + tableName, ex);
                return;
            }

            try (ResultSet rs = queryResult) {
                while (rs.next()) {
                    String sid = sanitizeSessionId(readDbText(rs, DB_COL_SESSION_ID, MAX_SESSION_ID_LENGTH));
                    if (sid == null) {
                        continue;
                    }
                    if (filter != null && !filter.contains(sid)) {
                        continue;
                    }

                    int count = readDbNonNegativeInt(rs, DB_COL_CNT, 0);
                    Timestamp first = readDbTimestamp(rs, DB_COL_FIRST_TS);
                    Timestamp last = readDbTimestamp(rs, DB_COL_LAST_TS);

                    SessionSummary summary = sessions.computeIfAbsent(sid, SessionSummary::new);
                    summary.accept(first, count, widgetId);
                    summary.accept(last, 0, widgetId);
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_AGGREGATE_SESSION_DATA + tableName, e);
        }
    }

    private boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null) {
            JsonObject body = Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_AUTH_REQUIRED)
                    .build();
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, body);
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ATTR) == null) {
            JsonObject body = Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_AUTH_REQUIRED)
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
            log.log(Level.WARNING, MSG_UNABLE_LIST_WIDGETS, e);
            return List.of();
        }
    }

    private boolean tableExists(Connection conn, String tableName, Map<String, Boolean> tableExistsCache) {
        try {
            return DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache);
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_INSPECT_TABLE_METADATA + tableName, e);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return VALUE_WIDGET;
        }
        String normalized = widgetId.trim().replaceAll(REGEX_WIDGET_TABLE, REPLACEMENT_UNDERSCORE);
        if (normalized.isEmpty()) {
            normalized = VALUE_WIDGET;
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = VALUE_WIDGET_PREFIX + normalized;
        }
        if (normalized.length() > 60) {
            normalized = TextIoSanitizerUtil.safeSlice(normalized, 0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(SQL_IDENTIFIER_INVALID_MSG);
        }
        return '"' + identifier.replace(SQL_QUOTE, SQL_QUOTE_ESCAPED) + '"';
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, MSG_UNABLE_WRITE_JSON_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, MSG_UNABLE_WRITE_JSON_ERROR, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? MSG_REQUEST_FAILED : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
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
            log.log(Level.FINE, MSG_INVALID_INTEGER_PARAM, ignored);
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
            throw new IllegalStateException(MSG_UNABLE_OPEN_DB_CONNECTION, ex);
        }
    }

    private PreparedStatement prepareStatementSafe(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_PREPARE_SQL, ex);
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
            log.log(Level.FINE, MSG_DB_TEXT_READ_FAILED + column, ex);
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
            log.log(Level.FINE, MSG_INVALID_DB_INT + column, ex);
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
            log.log(Level.FINE, MSG_INSTANT_PARSE_FALLBACK + column, ex);
        }

        try {
            return Timestamp.valueOf(trimmed.replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, MSG_TIMESTAMP_PARSE_FAILED + column, ex);
            return null;
        }
    }

    private String safeDbText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replace('\u0000', ' ')
            .replace(System.lineSeparator(), EMPTY)
            .replace(System.lineSeparator(), EMPTY)
                .trim();
        return normalized.length() > maxLen ? TextIoSanitizerUtil.safeSlice(normalized, 0, maxLen) : normalized;
    }

    private String validateTaintedText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String canonical = canonicalizeForValidation(value);
        String normalized = ServletRequestParamUtil.normalizeBodyText(canonical, maxLen, false);
        if (normalized == null || normalized.isBlank()) {
            return EMPTY;
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
        return VALUE_TRUE.equalsIgnoreCase(value);
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
        return value == null ? EMPTY : ISO_INSTANT_FMT.format(value);
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? EMPTY : ISO_INSTANT_FMT.format(value.toInstant());
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
            return EMPTY;
        }
        if (trimmed.length() > maxLength) {
            return TextIoSanitizerUtil.safeSlice(trimmed, 0, maxLength);
        }
        return trimmed;
    }

    private String sanitizeActivity(String raw) {
        if (raw == null || raw.isBlank()) {
            return ACTIVITY_ALL;
        }
        String normalized = raw.trim().toLowerCase();
        if (ACTIVITY_ACTIVE.equals(normalized) || ACTIVITY_INACTIVE.equals(normalized) || ACTIVITY_ALL.equals(normalized)) {
            return normalized;
        }
        return ACTIVITY_ALL;
    }

    private String applyActivityFilter(List<SessionSummary> sessionList, String requestedActivity) {
        String activity = sanitizeActivity(requestedActivity);
        Instant cutoff = Instant.now().minus(ACTIVE_DAYS, ChronoUnit.DAYS);
        switch (activity) {
            case ACTIVITY_INACTIVE -> sessionList.removeIf(s -> s == null || s.lastSeen == null || !s.lastSeen.isBefore(cutoff));
            case ACTIVITY_ACTIVE -> sessionList.removeIf(s -> s != null && s.lastSeen != null && s.lastSeen.isBefore(cutoff));
            default -> {
                return ACTIVITY_ALL;
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

final class ChatRow {

    final String chatId;
    final String prompt;
    final Timestamp createdAt;

    ChatRow(String chatId, String prompt, Timestamp createdAt) {
        this.chatId = chatId;
        this.prompt = prompt;
        this.createdAt = createdAt;
    }
}

final class SessionSummary {

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


