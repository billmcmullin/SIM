package com.sim.chatserver.web.dashboard.drilldown;

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
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Optimized WidgetReviewDataServlet: - supports returning all selected entries
 * when limit=all or limit<=0 - single-pass data query (count via window
 * function) - PostgreSQL array binding instead of giant IN (?, ?, ...) - cached
 * table existence checks - cached widget display names
 */
@WebServlet(name = "WidgetReviewDataServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/review-data"})
public class WidgetReviewDataServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewDataServlet.class.getName());
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final Pattern SAFE_SELECTION_ID = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");

    private static final String[] ALLOWED_SORT_COLUMNS = {
        "widget_chat_id", "prompt", "created_at", "session_id"
    };

    private static final int DEFAULT_LIMIT = 50;
    // Raised to allow full retrieval from UI (was 200).
    private static final int MAX_LIMIT = 20000;
    private static final int DEFAULT_PAGE = 1;

    private final Map<String, Boolean> tableExistsCache = new ConcurrentHashMap<>();
    private final Map<String, String> widgetNameCache = new ConcurrentHashMap<>();

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final long t0 = System.nanoTime();

        if (!isLoggedIn(req, resp)) {
            return;
        }

        String selectionId = sanitizeSelectionId(firstParam(req, "selectionId"));
        if (selectionId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"selectionId required.\"}");
            return;
        }

        LocalDate selectedDate = parseDate(firstParam(req, "date"), resp);
        if (selectedDate == LocalDate.MIN) {
            return;
        }

        HttpSession session = req.getSession(false);
        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Selection not found.\"}");
            return;
        }

        if (selectedDate == null && selection.date != null && !selection.date.isBlank()) {
            try {
                selectedDate = LocalDate.parse(selection.date.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Selection date is not in ISO format", ex);
                // no date filter
            }
        }

        if (selection.hasSnapshots()) {
            handleSnapshotSelection(selection, req, resp, t0);
            return;
        }

        String widgetId = selection.widgetId;
        String tableName = sanitizeWidgetTableName(widgetId);
        String widgetDisplayName = resolveWidgetDisplayNameCached(widgetId);

        String limitParam = firstParam(req, "limit");
        Integer rawLimit = parseIntegerOrNull(limitParam);
        boolean unboundedRequested = isUnlimitedLimit(limitParam, rawLimit);

        int page = Math.max(DEFAULT_PAGE, parseInteger(firstParam(req, "page"), DEFAULT_PAGE));
        int offset;
        int limit;

        List<String> chatIds = selection.chatIds == null ? Collections.emptyList() : selection.chatIds;
        if (chatIds.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"No chat IDs specified.\"}");
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

        String search = trimToNull(firstParam(req, "search"));
        String sortColumn = parseSortColumn(firstParam(req, "sortColumn"));
        String sortDir = parseSortDirection(firstParam(req, "sortDir"));

        final long t1 = System.nanoTime();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if (!tableExistsCached(conn, tableName)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(resp, "{\"status\":\"error\",\"message\":\"Table does not exist.\"}");
                return;
            }

            QueryParts qp = buildQuery(tableName, sortColumn, sortDir, search, selectedDate);

            int totalRows = 0;
            List<ChatRow> rows = new ArrayList<>();
            Set<String> sessionIds = ConcurrentHashMap.newKeySet();

            try (PreparedStatement ps = conn.prepareStatement(qp.sql)) {
                int idx = 1;

                Array chatIdArray = conn.createArrayOf("text", chatIds.toArray(String[]::new));
                ps.setArray(idx++, chatIdArray);

                if (search != null) {
                    String pattern = "%" + search + "%";
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
                            totalRows = rs.getInt("total_count");
                        }

                        String sessionId = rs.getString("session_id");
                        if (sessionId != null && !sessionId.isBlank()) {
                            sessionIds.add(sessionId);
                        }

                        rows.add(new ChatRow(
                                nullable(rs, "widget_chat_id"),
                                nullable(rs, "prompt"),
                                rs.getString("response_text"),
                                formatTimestamp(SqlTimeUtil.safeTimestamp(rs, "created_at")),
                                sessionId
                        ));
                    }
                }
            }

            final long t2 = System.nanoTime();

            Map<String, SessionLabelStore.SessionLabel> labels = sessionIds.isEmpty()
                    ? Collections.emptyMap()
                    : SessionLabelStore.mapDisplayNames(sessionIds);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (ChatRow row : rows) {
                String displaySession = SessionLabelStore.resolveDisplayLabel(row.sessionId, labels.get(row.sessionId));
                JsonObject jsonRow = Json.createObjectBuilder()
                        .add("chatId", row.chatId)
                        .add("widgetId", widgetId == null ? "" : widgetId)
                        .add("widgetName", widgetDisplayName == null ? "" : widgetDisplayName)
                        .add("prompt", row.prompt)
                        .add("response", row.response == null ? "" : row.response)
                        .add("createdAt", row.createdAt)
                        .add("sessionId", row.sessionId == null ? "" : row.sessionId)
                        .add("sessionIdDisplay", displaySession)
                        .build();
                arrayBuilder.add(jsonRow);
            }

            int totalPages = totalRows == 0 ? 1 : (int) Math.ceil((double) totalRows / Math.max(1, limit));
            SearchTerms st = normalizeSearchTerms(selection);

            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("rows", arrayBuilder)
                    .add("searchTerms", Json.createObjectBuilder()
                            .add("global", st.global)
                            .add("prompt", st.prompt)
                            .add("response", st.response))
                    .add("totalRows", totalRows)
                    .add("totalPages", totalPages)
                    .add("page", page)
                    .add("limit", limit)
                    .build();

            writeJson(resp, body);

            final long t3 = System.nanoTime();
            log.log(Level.INFO,
                    "review-data timings ms: prep={0} db={1} json={2} total={3} rows={4} totalRows={5} limit={6} page={7} unbounded={8}",
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
            log.log(Level.SEVERE, "Unable to fetch selected rows", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Unable to load selection.\"}");
        }
    }

    private void handleSnapshotSelection(
            WidgetReviewStartServlet.Selection selection,
            HttpServletRequest req,
            HttpServletResponse resp,
            long t0) throws IOException {

        String search = trimToNull(firstParam(req, "search"));
        String sortColumn = parseSortColumn(firstParam(req, "sortColumn"));
        String sortDir = parseSortDirection(firstParam(req, "sortDir"));

        String limitParam = firstParam(req, "limit");
        Integer rawLimit = parseIntegerOrNull(limitParam);
        boolean unboundedRequested = isUnlimitedLimit(limitParam, rawLimit);

        int page = Math.max(DEFAULT_PAGE, parseInteger(firstParam(req, "page"), DEFAULT_PAGE));
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

        Map<String, SessionLabelStore.SessionLabel> labels = Collections.emptyMap();
        if (!sessionIds.isEmpty()) {
            try {
                labels = SessionLabelStore.mapDisplayNames(sessionIds);
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to resolve session labels for snapshots", e);
            }
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (TermChatSnapshot snapshot : pageRows) {
            String rawSession = snapshot.getSessionId();
            String displaySession = SessionLabelStore.resolveDisplayLabel(rawSession, labels.get(rawSession));
            String widgetId = snapshot.getWidgetId();
            String widgetName = resolveWidgetDisplayNameCached(widgetId);

            arrayBuilder.add(Json.createObjectBuilder()
                    .add("chatId", nullToEmpty(snapshot.getChatId()))
                    .add("widgetId", nullToEmpty(widgetId))
                    .add("widgetName", nullToEmpty(widgetName))
                    .add("prompt", nullToEmpty(snapshot.getPrompt()))
                    .add("response", nullToEmpty(snapshot.getResponse()))
                    .add("createdAt", formatTimestamp(snapshot.getCreatedAt()))
                    .add("sessionId", nullToEmpty(rawSession))
                    .add("sessionIdDisplay", displaySession));
        }

        SearchTerms st = normalizeSearchTerms(selection);
        int totalPages = totalRows == 0 ? 1 : (int) Math.ceil((double) totalRows / Math.max(1, limit));

        JsonObject body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("rows", arrayBuilder)
                .add("searchTerms", Json.createObjectBuilder()
                        .add("global", st.global)
                        .add("prompt", st.prompt)
                        .add("response", st.response))
                .add("totalRows", totalRows)
                .add("totalPages", totalPages)
                .add("page", page)
                .add("limit", limit)
                .build();

        writeJson(resp, body);

        final long t1 = System.nanoTime();
        log.log(Level.INFO,
            "review-data snapshot timings ms: total={0} rows={1} totalRows={2} limit={3} page={4} unbounded={5}",
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
        StringBuilder where = new StringBuilder(" WHERE widget_chat_id = ANY (?)");

        if (search != null) {
            where.append(" AND (prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)");
        }

        if (selectedDate != null) {
            where.append(" AND created_at >= ? AND created_at < ?");
        }

        String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id, COUNT(*) OVER() AS total_count "
                + "FROM " + quoteIdentifier(tableName)
                + where
                + " ORDER BY " + sortColumn + " " + sortDir
                + " LIMIT ? OFFSET ?";

        return new QueryParts(sql);
    }

    private LocalDate parseDate(String raw, HttpServletResponse resp) throws IOException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Invalid date. Expected YYYY-MM-DD.\"}");
            return LocalDate.MIN;
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Authentication required.\"}");
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
            log.log(Level.FINE, "Unable to lookup widget display name", e);
        }
        return widgetId;
    }

    private boolean tableExistsCached(Connection conn, String tableName) throws SQLException {
        Boolean cached = tableExistsCache.get(tableName);
        if (cached != null) {
            return Boolean.TRUE.equals(cached);
        }

        boolean exists = tableExists(conn, tableName);
        tableExistsCache.put(tableName, exists ? Boolean.TRUE : Boolean.FALSE);
        return exists;
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
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
        if ("all".equals(t) || "max".equals(t) || "unbounded".equals(t)) {
            return true;
        }
        return parsed != null && parsed.compareTo(0) <= 0;
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer parameter value", ex);
            return null;
        }
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String firstParam(HttpServletRequest req, String name) {
        Map<String, String[]> params = req.getParameterMap();
        if (params == null) {
            return null;
        }
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    private String sanitizeSelectionId(String rawSelectionId) {
        String value = trimToNull(rawSelectionId);
        if (value == null || !SAFE_SELECTION_ID.matcher(value).matches()) {
            return null;
        }
        return value;
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

    private String nullable(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }

    private String formatTimestamp(Timestamp ts) {
        return ts == null ? "" : ISO_INSTANT_FMT.format(ts.toInstant());
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
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    private int parseInteger(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer parameter value", ex);
            return fallback;
        }
    }

    private void sortSnapshots(List<TermChatSnapshot> data, String column, String direction) {
        Comparator<TermChatSnapshot> comparator = switch (column) {
            case "prompt" ->
                Comparator.comparing(s -> nullToEmpty(s.getPrompt()), String.CASE_INSENSITIVE_ORDER);
            case "session_id" ->
                Comparator.comparing(s -> nullToEmpty(s.getSessionId()), String.CASE_INSENSITIVE_ORDER);
            case "widget_chat_id" ->
                Comparator.comparing(s -> nullToEmpty(s.getChatId()), String.CASE_INSENSITIVE_ORDER);
            case "created_at" ->
                Comparator.comparing(s -> s.getCreatedAt() == null ? 0L : s.getCreatedAt().getTime());
            default ->
                Comparator.comparing(s -> nullToEmpty(s.getChatId()), String.CASE_INSENSITIVE_ORDER);
        };
        if ("DESC".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        data.sort(comparator);
    }

    private SearchTerms normalizeSearchTerms(WidgetReviewStartServlet.Selection selection) {
        String g = "";
        String p = "";
        String r = "";
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
        return s == null ? "" : s;
    }

    private double nsToMs(long ns) {
        return ns / 1_000_000.0;
    }

    private String twoDecimals(double value) {
        long scaled = Math.round(value * 100.0);
        long whole = scaled / 100;
        long fraction = Math.abs(scaled % 100);
        return whole + "." + (fraction < 10 ? "0" : "") + fraction;
    }

    private void writeJson(HttpServletResponse resp, String body) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        resp.getWriter().write(body);
    }

    private void writeJson(HttpServletResponse resp, JsonObject body) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(body);
        }
    }

    private static final class QueryParts {

        final String sql;

        private QueryParts(String sql) {
            this.sql = sql;
        }
    }

    private static final class SearchTerms {

        final String global;
        final String prompt;
        final String response;

        private SearchTerms(String global, String prompt, String response) {
            this.global = global;
            this.prompt = prompt;
            this.response = response;
        }
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
}
