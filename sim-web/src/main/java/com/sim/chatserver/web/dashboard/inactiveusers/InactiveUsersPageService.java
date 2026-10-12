package com.sim.chatserver.web.dashboard.inactiveusers;

import com.sim.chatserver.util.SessionLabel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

final class InactiveUsersPageService {
    private static final Logger log = Logger.getLogger(InactiveUsersPageServlet.class.getCanonicalName());
    private static final Logger owaspLog = Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP);
    private static final int DEFAULT_DAYS = 7;
    private static final int TOP_N = 5;
    private static final int MAX_SESSION_ID_LENGTH = 128;
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users.html";
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);

    private static final int FRUSTRATION_PROMPT_SCAN_LIMIT = 8;
    private static final Pattern SAFE_SESSION_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");
        private static final String EMPTY = "";
        private static final String SESSION_ATTR_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
        private static final String PATH_LOGIN = com.sim.chatserver.util.CommonLiteralConstants.PATH_LOGIN;
        private static final String PARAM_DAYS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS;
        private static final String WIDGET_ID_ALL = "ALL";
        private static final String WIDGET_LABEL_ALL = "All Widgets";
        private static final String TEMPLATE_TOKEN_CONTEXT_PATH = com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH;
        private static final String TEMPLATE_TOKEN_USER = com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER;
        private static final String TEMPLATE_TOKEN_DEFAULT_DAYS = "${defaultDays}";
        private static final String TEMPLATE_TOKEN_INACTIVE_USERS_B64 = "${inactiveUsersDataB64}";
        private static final String CONTENT_TYPE_HTML_UTF8 = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8_SPACED;
        private static final String MSG_UNABLE_LOAD_WIDGETS = "Unable to load widgets";
        private static final String MSG_UNABLE_COMPUTE_INACTIVE_USERS = "Unable to compute inactive users";
        private static final String MSG_REQUEST_HANDLING_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED;
        private static final String MSG_UNHANDLED_EXCEPTION_DOGGET = com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET;
        private static final String MSG_FAILED_FALLBACK_SERVER_ERROR = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD;
        private static final String MSG_UNABLE_LOAD_SESSION_LABELS = "Unable to load session labels";
        private static final String SQL_SELECT_SESSION_AGG_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_SESSION_COUNTS_PREFIX;
        private static final String SQL_SESSION_GROUP_SUFFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;
        private static final String SQL_SELECT_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_PREFIX;
        private static final String SQL_PROMPT_QUERY_MIDDLE = " WHERE session_id = ? AND ";
        private static final String SQL_PROMPT_QUERY_SUFFIX = " ORDER BY created_at DESC";
        private static final String DB_COL_P = "p";
        private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
        private static final String DB_COL_LAST_ENTRY = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_LAST_ENTRY;
        private static final String DB_COL_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
        private static final String SQL_AS = " AS ";
        private static final String SQL_FROM = " FROM ";
        private static final String SQL_IS_NOT_NULL_AND = " IS NOT NULL AND ";
        private static final String SQL_NOT_EMPTY = " <> ''";
        private static final String SQL_DOUBLE_QUOTE = "\"";
        private static final String SQL_DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
        private static final String COLON_SPACE = ": ";
        private static final String LOG_PREFIX_AGGREGATE_INACTIVE_TABLE = "Unable to aggregate inactive sessions for table ";
        private static final String LOG_PREFIX_PROMPT_COLUMN_UNAVAILABLE = "Prompt column not available for table ";
        private static final String LOG_PREFIX_FRUSTRATION_DETECTION_SKIPPED_FOR_SESSION = "Frustration detection skipped for session ";
        private static final String LOG_INFIX_TABLE = " table ";
        private static final String LOG_PREFIX_UNABLE_INSPECT_TABLE_METADATA_FOR = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
        private static final String LOG_HANDLED_EXCEPTION = com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION;
        private static final String[] PROMPT_COLUMNS = {com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT, "prompt_text", "user_prompt"};
        private static final String[] FRUSTRATION_STRONG_KEYWORDS = {
            "frustrated", "angry", "annoyed", "furious", "ridiculous", "useless", "terrible"
        };
        private static final String[] FRUSTRATION_MISUNDERSTOOD_PHRASES = {
            "you don't understand", "you dont understand", "not what i asked", "wrong again", "not listening", "misunderstood"
        };
        private static final String FRUSTRATION_REASON_KEYWORD_PREFIX = "keyword:";
        private static final String FRUSTRATION_REASON_MISUNDERSTOOD_PREFIX = "misunderstood:";
        private static final String FRUSTRATION_REASON_PUNCTUATION = "punctuation";
        private static final String FRUSTRATION_REASON_PHRASE = "frustration_phrase";
        private static final String PUNCT_EXCL_TRIPLE = "!!!";
        private static final String PUNCT_Q_TRIPLE = "???";
        private static final String JSON_KEY_ALL = com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL;
        private static final String JSON_KEY_WIDGETS = "widgets";
        private static final String JSON_KEY_WIDGET_NAMES = "widgetNames";
        private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
        private static final String JSON_KEY_DISPLAY_LABEL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_LABEL;
        private static final String JSON_KEY_WIDGET_ID = com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID;
        private static final String JSON_KEY_WIDGET_LABEL = "widgetLabel";
        private static final String JSON_KEY_CHAT_COUNT = "chatCount";
        private static final String JSON_KEY_LAST_ENTRY = "lastEntry";
        private static final String JSON_KEY_FRUSTRATION_DETECTED = "frustrationDetected";
        private static final String JSON_KEY_FRUSTRATION_SCORE = "frustrationScore";
        private static final String JSON_KEY_FRUSTRATION_REASON = "frustrationReason";
        private static final String DB_METADATA_TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
        private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
        private static final String MSG_INVALID_INTEGER_PARAM = "Invalid integer request parameter value";
        private static final String LOG_PREFIX_UNABLE_READ_DB_TEXT = "Unable to read DB text for column ";
        private static final String MSG_TEMPLATE_NOT_FOUND = com.sim.chatserver.util.CommonLiteralConstants.MSG_TEMPLATE_NOT_FOUND_FORMAT;
        private static final String LOG_PREFIX_UNABLE_LOAD_TEMPLATE = "Unable to load template ";
        private static final String HTML_ENTITY_AMP = com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_AMP;
        private static final String HTML_ENTITY_LT = com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_LT;
        private static final String HTML_ENTITY_GT = com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_GT;
        private static final String HTML_ENTITY_QUOT = com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_QUOT;
        private static final String HTML_ENTITY_APOS = com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_APOS;

    void handleGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute(SESSION_ATTR_USER) == null) {
            req.getRequestDispatcher(PATH_LOGIN).forward(req, resp);
            return;
        }

        int days = parseInt(ServletRequestParamUtil.firstParam(req, PARAM_DAYS, 256, false, false), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, MSG_UNABLE_LOAD_WIDGETS, e);
            widgets = List.of();
        }

        Map<String, String> widgetNameById = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String id = w.getWidgetId().trim();
            String friendly = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? id : w.getDisplayName().trim();
            widgetNameById.put(id, friendly);
        }

        Map<String, List<InactiveRow>> byWidget = new LinkedHashMap<>();
        Map<String, InactiveRow> allAgg = new LinkedHashMap<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String wid = w.getWidgetId().trim();
                String widgetLabel = widgetNameById.getOrDefault(wid, wid);
                String table = sanitizeWidgetTableName(wid);
                if (!tableExists(conn, table)) {
                    continue;
                }

                Map<String, InactiveRow> perWidgetAgg = querySessionAggregateForTable(conn, table, wid, widgetLabel);

                for (InactiveRow row : perWidgetAgg.values()) {
                    try {
                        List<String> prompts = loadRecentPromptsForSession(conn, table, row.sessionId, FRUSTRATION_PROMPT_SCAN_LIMIT);
                        FrustrationResult fr = detectFrustration(prompts);
                        row.frustrationDetected = fr.detected;
                        row.frustrationScore = fr.score;
                        row.frustrationReason = fr.reason;
                    } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
                        row.frustrationDetected = false;
                        row.frustrationScore = 0.0;
                        row.frustrationReason = EMPTY;
                        log.log(Level.FINE, LOG_PREFIX_FRUSTRATION_DETECTION_SKIPPED_FOR_SESSION + row.sessionId + LOG_INFIX_TABLE + table, ex);
                    }
                }

                Set<String> widgetSessionIds = perWidgetAgg.keySet();
                Map<String, SessionLabel> widgetLabels = mapLabelsSafe(widgetSessionIds);

                List<InactiveRow> inactiveRows = new ArrayList<>();
                for (InactiveRow row : perWidgetAgg.values()) {
                    if (!isInactive(row.lastEntry, cutoff)) {
                        continue;
                    }
                    row.displayLabel = SessionLabelStore.resolveDisplayLabel(row.sessionId, widgetLabels.get(row.sessionId));
                    inactiveRows.add(row);
                }

                inactiveRows.sort(Comparator.comparing((InactiveRow r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));
                if (inactiveRows.size() > TOP_N) {
                    inactiveRows = inactiveRows.subList(0, TOP_N);
                }

                byWidget.put(wid, inactiveRows);

                for (InactiveRow wr : perWidgetAgg.values()) {
                    InactiveRow ar = allAgg.get(wr.sessionId);
                    if (ar == null) {
                        ar = new InactiveRow();
                        ar.sessionId = wr.sessionId;
                        ar.widgetId = WIDGET_ID_ALL;
                        ar.widgetLabel = WIDGET_LABEL_ALL;
                        ar.chats = 0;
                        ar.lastEntry = null;
                        ar.frustrationDetected = false;
                        ar.frustrationScore = 0.0;
                        ar.frustrationReason = EMPTY;
                        allAgg.put(wr.sessionId, ar);
                    }
                    ar.chats += wr.chats;
                    if (ar.lastEntry == null || (wr.lastEntry != null && wr.lastEntry.after(ar.lastEntry))) {
                        ar.lastEntry = wr.lastEntry;
                    }
                    if (wr.frustrationScore > ar.frustrationScore) {
                        ar.frustrationScore = wr.frustrationScore;
                        ar.frustrationDetected = wr.frustrationDetected;
                        ar.frustrationReason = wr.frustrationReason;
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, MSG_UNABLE_COMPUTE_INACTIVE_USERS, e);
        }

        List<InactiveRow> allRows = new ArrayList<>();
        Map<String, SessionLabel> allLabels = mapLabelsSafe(allAgg.keySet());
        for (InactiveRow row : allAgg.values()) {
            if (!isInactive(row.lastEntry, cutoff)) {
                continue;
            }
            row.displayLabel = SessionLabelStore.resolveDisplayLabel(row.sessionId, allLabels.get(row.sessionId));
            allRows.add(row);
        }

        allRows.sort(Comparator.comparing((InactiveRow r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));
        if (allRows.size() > TOP_N) {
            allRows = allRows.subList(0, TOP_N);
        }

        Map<String, List<InactiveRow>> payload = new LinkedHashMap<>();
        payload.put(WIDGET_ID_ALL, allRows);
        payload.putAll(byWidget);

        String jsonData = buildInactiveUsersJson(payload, widgetNameById);
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String user = safeSessionUser(httpSession);
        String contextPath = ServletPathUtil.safeContextPathNoEmptyGuard(req.getContextPath());
        String jsonDataB64 = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));

        String rendered = template
            .replace(TEMPLATE_TOKEN_CONTEXT_PATH, escapeHtml(contextPath))
                .replace(TEMPLATE_TOKEN_USER, escapeHtml(user))
                .replace(TEMPLATE_TOKEN_DEFAULT_DAYS, String.valueOf(days))
            .replace(TEMPLATE_TOKEN_INACTIVE_USERS_B64, jsonDataB64);

        byte[] renderedBytes = rendered.getBytes(StandardCharsets.UTF_8);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(CONTENT_TYPE_HTML_UTF8);
        resp.setContentLength(renderedBytes.length);
        try (var output = resp.getOutputStream()) {
            output.write(renderedBytes);
        }
    
        } catch (ServletException | IOException | IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            owaspLog.log(java.util.logging.Level.WARNING, MSG_UNHANDLED_EXCEPTION_DOGGET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    owaspLog.log(java.util.logging.Level.FINE, MSG_FAILED_FALLBACK_SERVER_ERROR, ioe);
                }
            }
        }
    }

    private Map<String, SessionLabel> mapLabelsSafe(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, MSG_UNABLE_LOAD_SESSION_LABELS, e);
            return Map.of();
        }
    }

    private boolean isInactive(Timestamp lastEntry, Instant cutoff) {
        if (lastEntry == null) {
            return false;
        }
        return lastEntry.toInstant().toEpochMilli() < cutoff.toEpochMilli();
    }

    private Map<String, InactiveRow> querySessionAggregateForTable(Connection conn, String table, String widgetId, String widgetLabel) {
        Map<String, InactiveRow> out = new LinkedHashMap<>();
        String sql = SQL_SELECT_SESSION_AGG_PREFIX
                + quoteIdentifier(table)
            + SQL_SESSION_GROUP_SUFFIX;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String sid = sanitizeSessionId(readDbText(rs, DB_COL_SESSION_ID, MAX_SESSION_ID_LENGTH));
                Timestamp last = SqlTimeUtil.safeTimestamp(rs, DB_COL_LAST_ENTRY);
                if (sid == null || sid.isBlank()) {
                    continue;
                }

                InactiveRow r = new InactiveRow();
                r.sessionId = sid.trim();
                r.displayLabel = r.sessionId;
                r.widgetId = widgetId;
                r.widgetLabel = widgetLabel;
                r.lastEntry = last;
                r.chats = rs.getLong(DB_COL_TOTAL);
                r.frustrationDetected = false;
                r.frustrationScore = 0.0;
                r.frustrationReason = EMPTY;
                out.put(r.sessionId, r);
            }
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_PREFIX_AGGREGATE_INACTIVE_TABLE + table, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return out;
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) {
        List<String> prompts = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank() || limit < 1) {
            return prompts;
        }

        String[] cols = PROMPT_COLUMNS;
        for (String col : cols) {
            String sql = SQL_SELECT_PREFIX + quoteIdentifier(col) + SQL_AS + DB_COL_P + SQL_FROM + quoteIdentifier(table)
                + SQL_PROMPT_QUERY_MIDDLE + quoteIdentifier(col) + SQL_IS_NOT_NULL_AND + quoteIdentifier(col) + SQL_NOT_EMPTY
                + SQL_PROMPT_QUERY_SUFFIX;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = conn.prepareStatement(sql);
                ps.setString(1, sessionId);
                ps.setMaxRows(limit);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String p = safeDbText(rs.getString(DB_COL_P), 2000);
                    if (p != null && !p.isBlank()) {
                        prompts.add(p);
                    }
                }
                if (!prompts.isEmpty()) {
                    return prompts;
                }
            } catch (SQLException ex) {
                log.log(Level.FINE, LOG_PREFIX_PROMPT_COLUMN_UNAVAILABLE + table + COLON_SPACE + col, ex);
            } finally {
                closeQuietly(rs);
                closeQuietly(ps);
            }
        }
        return prompts;
    }

    private static void closeQuietly(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException | IllegalStateException | UnsupportedOperationException e) {
            owaspLog.log(java.util.logging.Level.FINE, LOG_HANDLED_EXCEPTION, e);
            // ignore close failure
        }
    }

    private static void closeQuietly(PreparedStatement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException | IllegalStateException | UnsupportedOperationException e) {
            owaspLog.log(java.util.logging.Level.FINE, LOG_HANDLED_EXCEPTION, e);
            // ignore close failure
        }
    }

    private FrustrationResult detectFrustration(List<String> prompts) {
        FrustrationResult out = new FrustrationResult();
        out.detected = false;
        out.score = 0.0;
        out.reason = EMPTY;

        if (prompts == null || prompts.isEmpty()) {
            return out;
        }

        int scorePoints = 0;
        String reason = EMPTY;

        String[] strong = FRUSTRATION_STRONG_KEYWORDS;
        String[] misunderstood = FRUSTRATION_MISUNDERSTOOD_PHRASES;

        boolean consistentCapsStyle = InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(prompts);

        for (String raw : prompts) {
            String t = raw == null ? EMPTY : raw.trim();
            String lower = t.toLowerCase();

            for (String k : strong) {
                if (lower.contains(k)) {
                    scorePoints += 30;
                    if (reason.isEmpty()) {
                        reason = FRUSTRATION_REASON_KEYWORD_PREFIX + k;
                    }
                }
            }
            for (String k : misunderstood) {
                if (lower.contains(k)) {
                    scorePoints += 35;
                    if (reason.isEmpty()) {
                        reason = FRUSTRATION_REASON_MISUNDERSTOOD_PREFIX + k;
                    }
                }
            }
            if (lower.contains(PUNCT_EXCL_TRIPLE) || lower.contains(PUNCT_Q_TRIPLE)) {
                scorePoints += 15;
                if (reason.isEmpty()) {
                    reason = FRUSTRATION_REASON_PUNCTUATION;
                }
            }

            boolean nonFrustrationContext = InactiveUsersFrustrationTextUtil.isNonFrustrationContext(t);

            if (!nonFrustrationContext
                    && InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal(t)
                    && !consistentCapsStyle) {
                scorePoints += 20;
                if (reason.isEmpty()) {
                    reason = FRUSTRATION_REASON_PHRASE;
                }
            }
        }

        if (scorePoints > 100) {
            scorePoints = 100;
        }
        out.score = scorePoints / 100.0d;
        out.detected = scorePoints >= 40;
        out.reason = reason;
        return out;
    }

    private String buildInactiveUsersJson(Map<String, List<InactiveRow>> byWidget, Map<String, String> widgetNameById) {
        JsonArrayBuilder allArr = Json.createArrayBuilder();
        JsonObjectBuilder widgetsObj = Json.createObjectBuilder();
        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();

        for (Map.Entry<String, String> e : widgetNameById.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue() == null ? e.getKey() : e.getValue());
        }

        for (InactiveRow r : byWidget.getOrDefault(WIDGET_ID_ALL, List.of())) {
            allArr.add(toJson(r));
        }

        for (Map.Entry<String, List<InactiveRow>> e : byWidget.entrySet()) {
            if (WIDGET_ID_ALL.equals(e.getKey())) {
                continue;
            }
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (InactiveRow r : e.getValue()) {
                arr.add(toJson(r));
            }
            widgetsObj.add(e.getKey(), arr);
        }

        JsonObject payload = Json.createObjectBuilder()
            .add(JSON_KEY_ALL, allArr)
            .add(JSON_KEY_WIDGETS, widgetsObj)
            .add(JSON_KEY_WIDGET_NAMES, widgetNamesObj)
                .build();

        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(payload);
            return sw.toString();
        }
    }

    private JsonObject toJson(InactiveRow r) {
        return Json.createObjectBuilder()
                .add(JSON_KEY_SESSION_ID, nvl(r.sessionId))
                .add(JSON_KEY_DISPLAY_LABEL, nvl(r.displayLabel))
                .add(JSON_KEY_WIDGET_ID, nvl(r.widgetId))
                .add(JSON_KEY_WIDGET_LABEL, nvl(r.widgetLabel))
                .add(JSON_KEY_CHAT_COUNT, r.chats)
                .add(JSON_KEY_LAST_ENTRY, r.lastEntry == null ? EMPTY : r.lastEntry.toInstant().toString())
                .add(JSON_KEY_FRUSTRATION_DETECTED, r.frustrationDetected)
                .add(JSON_KEY_FRUSTRATION_SCORE, r.frustrationScore)
                .add(JSON_KEY_FRUSTRATION_REASON, nvl(r.frustrationReason))
                .build();
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{DB_METADATA_TABLE_TYPE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_PREFIX_UNABLE_INSPECT_TABLE_METADATA_FOR + tableName, e);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        return DashboardDbUtil.sanitizeWidgetTableName(widgetId);
    }

    private String quoteIdentifier(String s) {
        if (s == null || !SAFE_SQL_IDENTIFIER.matcher(s).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + s.replace(SQL_DOUBLE_QUOTE, SQL_DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private int parseInt(String v, int fallback) {
        if (v == null || v.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            log.log(Level.FINE, MSG_INVALID_INTEGER_PARAM, e);
            return fallback;
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String readDbText(ResultSet rs, String column, int maxLen) {
        if (rs == null || column == null || column.isBlank()) {
            return null;
        }
        Object raw;
        try {
            raw = rs.getObject(column);
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_PREFIX_UNABLE_READ_DB_TEXT + column, e);
            return null;
        }
        String value = raw == null ? null : String.valueOf(raw);
        return safeDbText(value, maxLen);
    }

    private String safeDbText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u0000', ' ').replace(System.lineSeparator(), EMPTY).replace(System.lineSeparator(), EMPTY).trim();
        if (normalized.length() > maxLen) {
            return TextIoSanitizerUtil.safeSlice(normalized, 0, maxLen);
        }
        return normalized;
    }

    private String loadTemplate(ServletContext context, String path) {
        if (context == null || path == null || path.isBlank()) {
            return EMPTY;
        }
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, MSG_TEMPLATE_NOT_FOUND, path);
                return EMPTY;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    b.append(line).append(System.lineSeparator());
                }
                return b.toString();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, LOG_PREFIX_UNABLE_LOAD_TEMPLATE + path, e);
            return EMPTY;
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return EMPTY;
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    escaped.append(HTML_ENTITY_AMP);
                    break;
                case '<':
                    escaped.append(HTML_ENTITY_LT);
                    break;
                case '>':
                    escaped.append(HTML_ENTITY_GT);
                    break;
                case '"':
                    escaped.append(HTML_ENTITY_QUOT);
                    break;
                case '\'':
                    escaped.append(HTML_ENTITY_APOS);
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

    private String nvl(String s) {
        return s == null ? EMPTY : s;
    }

    private String safeSessionUser(HttpSession session) {
        if (session == null) {
            return EMPTY;
        }
        Object userAttr = session.getAttribute(SESSION_ATTR_USER);
        if (!(userAttr instanceof String user)) {
            return EMPTY;
        }
        String trimmed = user.trim();
        return trimmed.length() > 256 ? TextIoSanitizerUtil.safeSlice(trimmed, 0, 256) : trimmed;
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

}

final class FrustrationResult {

    boolean detected;
    double score;
    String reason;
}

final class InactiveRow {

    String sessionId;
    String displayLabel;
    String widgetId;
    String widgetLabel;
    Timestamp lastEntry;
    long chats;

    boolean frustrationDetected;
    double frustrationScore;
    String frustrationReason;
}


