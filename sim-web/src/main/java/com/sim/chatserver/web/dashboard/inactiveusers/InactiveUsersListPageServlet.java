package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InactiveUsersListPageServlet", urlPatterns = {"/dashboard/inactive-users/list"})
public class InactiveUsersListPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(InactiveUsersListPageServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users_list.html";
    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_SEARCH_LENGTH = 128;
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;

    private static final int FRUSTRATION_PROMPT_SCAN_LIMIT = 8;
    private static final Pattern ALL_CAPS_WORD = Pattern.compile("\\b[A-Z]{4,}\\b");
    private static final Pattern LOGGER_TOKEN = Pattern.compile("\\b(INFO|DEBUG|TRACE|WARN|WARNING|ERROR|FATAL)\\b");
    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
            "\\b(fuck|fucking|shit|bullshit|damn|wtf|crap|asshole)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FRUSTRATION_PHRASE_PATTERN = Pattern.compile(
            "\\b(not what i asked|that is not what i asked|you (didn'?t|do not) understand|wrong answer|incorrect answer|"
            + "you (are|re) not listening|this (still )?doesn'?t work|not working|still broken|fix this|"
            + "answer the question|stop ignoring|why is this wrong)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> SAFE_ACRONYMS = Set.of(
            "API", "SDK", "CLI", "GUI", "SQL", "JSON", "XML", "HTTP", "HTTPS", "URL", "URI", "JWT", "SSO", "SAML", "OIDC", "TLS", "SSL",
            "TCP", "UDP", "DNS", "IP", "CPU", "GPU", "RAM", "OS", "DB", "ETL", "CI", "CD", "IDE", "LTS", "JDK", "JVM",
            "MISRA", "OWASP", "CWE", "CVE", "NIST", "ISO", "IEC", "SOC", "PCI", "HIPAA", "GDPR", "PII"
    );

    @Inject
    AppDataSourceHolder dsHolder;

    private static final class Row {

        String sessionId;
        String displayLabel;
        String widgetId;
        String widgetLabel;
        long chatCount;
        Timestamp lastEntry;

        boolean frustrationDetected;
        double frustrationScore;
        String frustrationReason;
    }

    private static final class FrustrationResult {

        boolean detected;
        double score;
        String reason;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = safeContextPath(req.getServletContext().getContextPath());
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String scope = nvl(firstParam(req, "scope")).trim();
        if (!"widget".equalsIgnoreCase(scope)) {
            scope = "all";
        }

        String widgetIdFilter = sanitizeWidgetId(firstParam(req, "widgetId"));
        String search = sanitizeSearch(firstParam(req, "search"));
        String searchLower = search.toLowerCase();
        boolean hasSearch = !searchLower.isBlank();

        int days = parseInt(firstParam(req, "days"), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }

        int page = parseInt(firstParam(req, "page"), 1);
        if (page < 1) {
            page = 1;
        }

        int limit = parseInt(firstParam(req, "limit"), DEFAULT_LIMIT);
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load widgets", e);
            widgets = List.of();
        }

        Map<String, String> widgetNameById = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String id = w.getWidgetId().trim();
            String name = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? id : w.getDisplayName().trim();
            widgetNameById.put(id, name);
        }

        List<Row> allRows = new ArrayList<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if ("widget".equalsIgnoreCase(scope)) {
                if (!widgetIdFilter.isBlank() && widgetNameById.containsKey(widgetIdFilter)) {
                    String table = sanitizeWidgetTableName(widgetIdFilter);
                    if (tableExists(conn, table)) {
                        List<Row> rows = loadWidgetRows(
                                conn,
                                widgetIdFilter,
                                widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter),
                                cutoff
                        );
                        hydrateFrustrationForRows(conn, table, rows);
                        allRows.addAll(rows);
                    }
                }
            } else {
                Map<String, Row> agg = new LinkedHashMap<>();
                for (String wid : widgetNameById.keySet()) {
                    String table = sanitizeWidgetTableName(wid);
                    if (!tableExists(conn, table)) {
                        continue;
                    }

                    String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                            + quoteIdentifier(table)
                            + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

                    try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String sid = rs.getString("session_id");
                            Timestamp last = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                            if (sid == null || sid.isBlank() || last == null) {
                                continue;
                            }

                            Row r = agg.computeIfAbsent(sid.trim(), k -> {
                                Row x = new Row();
                                x.sessionId = k;
                                x.widgetId = "ALL";
                                x.widgetLabel = "All Widgets";
                                x.frustrationDetected = false;
                                x.frustrationScore = 0.0;
                                x.frustrationReason = "";
                                return x;
                            });
                            r.chatCount += rs.getLong("total");
                            if (r.lastEntry == null || last.after(r.lastEntry)) {
                                r.lastEntry = last;
                            }

                            try {
                                List<String> prompts = loadRecentPromptsForSession(conn, table, sid.trim(), FRUSTRATION_PROMPT_SCAN_LIMIT);
                                FrustrationResult fr = detectFrustration(prompts);
                                if (fr.score > r.frustrationScore) {
                                    r.frustrationScore = fr.score;
                                    r.frustrationDetected = fr.detected;
                                    r.frustrationReason = fr.reason;
                                }
                            } catch (IllegalArgumentException ex) {
                                log.log(Level.FINE, "Frustration detection skipped for " + sid + " in " + table, ex);
                            }
                        }
                    }
                }

                for (Row r : agg.values()) {
                    if (r.lastEntry != null && r.lastEntry.toInstant().isBefore(cutoff)) {
                        allRows.add(r);
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to compute inactive users list", e);
        }

        Set<String> ids = allRows.stream().map(r -> r.sessionId).collect(Collectors.toSet());
        Map<String, SessionLabelStore.SessionLabel> labels = Map.of();
        try {
            if (!ids.isEmpty()) {
                labels = SessionLabelStore.mapDisplayNames(ids);
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
        }

        for (Row r : allRows) {
            r.displayLabel = SessionLabelStore.resolveDisplayLabel(r.sessionId, labels.get(r.sessionId));
        }

        if (hasSearch) {
            allRows = allRows.stream().filter(r -> {
                String sid = r.sessionId == null ? "" : r.sessionId.toLowerCase();
                String display = r.displayLabel == null ? "" : r.displayLabel.toLowerCase();
                return sid.contains(searchLower) || display.contains(searchLower);
            }).collect(Collectors.toList());
        }

        allRows.sort(Comparator.comparing((Row r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));

        int total = allRows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / (double) limit));
        if (page > totalPages) {
            page = totalPages;
        }

        int start = Math.min((page - 1) * limit, total);
        int end = Math.min(start + limit, total);
        List<Row> pageRows = allRows.subList(start, end);

        String jsonData = buildJson(pageRows);
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);

        String title = "all".equals(scope)
                ? "All Inactive Users"
                : "Inactive Users: " + widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter);

        String rendered = template
            .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(String.valueOf(s.getAttribute("user"))))
                .replace("${title}", escapeHtml(title))
                .replace("${scope}", escapeHtml(scope))
                .replace("${widgetId}", escapeHtml(widgetIdFilter))
                .replace("${days}", String.valueOf(days))
                .replace("${page}", String.valueOf(page))
                .replace("${limit}", String.valueOf(limit))
                .replace("${total}", String.valueOf(total))
                .replace("${totalPages}", String.valueOf(totalPages))
                .replace("${search}", escapeHtml(search))
                .replace("${rowsJson}", escapeForJs(jsonData));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));
    }

    private void hydrateFrustrationForRows(Connection conn, String table, List<Row> rows) {
        for (Row r : rows) {
            try {
                List<String> prompts = loadRecentPromptsForSession(conn, table, r.sessionId, FRUSTRATION_PROMPT_SCAN_LIMIT);
                FrustrationResult fr = detectFrustration(prompts);
                r.frustrationDetected = fr.detected;
                r.frustrationScore = fr.score;
                r.frustrationReason = fr.reason;
            } catch (IllegalArgumentException ex) {
                r.frustrationDetected = false;
                r.frustrationScore = 0.0;
                r.frustrationReason = "";
                log.log(Level.FINE, "Frustration detection skipped for session " + r.sessionId + " table " + table, ex);
            }
        }
    }

    private List<Row> loadWidgetRows(Connection conn, String widgetId, String widgetLabel, Instant cutoff) throws SQLException {
        List<Row> rows = new ArrayList<>();
        String table = sanitizeWidgetTableName(widgetId);
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sid = rs.getString("session_id");
                Timestamp last = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                if (sid == null || sid.isBlank() || last == null) {
                    continue;
                }
                if (!last.toInstant().isBefore(cutoff)) {
                    continue;
                }

                Row r = new Row();
                r.sessionId = sid.trim();
                r.widgetId = widgetId;
                r.widgetLabel = widgetLabel;
                r.chatCount = rs.getLong("total");
                r.lastEntry = last;
                r.frustrationDetected = false;
                r.frustrationScore = 0.0;
                r.frustrationReason = "";
                rows.add(r);
            }
        }
        return rows;
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) {
        List<String> prompts = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank() || limit < 1) {
            return prompts;
        }

        String[] cols = {"prompt", "prompt_text", "user_prompt"};
        for (String col : cols) {
            String sql = "SELECT " + quoteIdentifier(col) + " AS p FROM " + quoteIdentifier(table)
                    + " WHERE session_id = ? AND " + quoteIdentifier(col) + " IS NOT NULL AND " + quoteIdentifier(col) + " <> ''"
                    + " ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sessionId);
                ps.setMaxRows(limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String p = rs.getString("p");
                        if (p != null && !p.isBlank()) {
                            prompts.add(p);
                        }
                    }
                    if (!prompts.isEmpty()) {
                        return prompts;
                    }
                }
            } catch (SQLException ex) {
                log.log(Level.FINEST, "Prompt column unavailable for frustration scan: " + col, ex);
                // try next candidate column
            }
        }
        return prompts;
    }

    private FrustrationResult detectFrustration(List<String> prompts) {
        FrustrationResult out = new FrustrationResult();
        out.detected = false;
        out.score = 0.0;
        out.reason = "";

        if (prompts == null || prompts.isEmpty()) {
            return out;
        }

        double score = 0.0;
        String reason = "";

        String[] strong = {"frustrated", "angry", "annoyed", "furious", "ridiculous", "useless", "terrible"};
        String[] misunderstood = {"you don't understand", "you dont understand", "not what i asked", "wrong again", "not listening", "misunderstood"};

        boolean consistentCapsStyle = isConsistentCapsStyle(prompts);

        for (String raw : prompts) {
            String t = raw == null ? "" : raw.trim();
            String lower = t.toLowerCase();

            for (String k : strong) {
                if (lower.contains(k)) {
                    score += 0.30;
                    if (reason.isEmpty()) {
                        reason = "keyword:" + k;
                    }
                }
            }
            for (String k : misunderstood) {
                if (lower.contains(k)) {
                    score += 0.35;
                    if (reason.isEmpty()) {
                        reason = "misunderstood:" + k;
                    }
                }
            }
            if (lower.contains("!!!") || lower.contains("???")) {
                score += 0.15;
                if (reason.isEmpty()) {
                    reason = "punctuation";
                }
            }

            boolean nonFrustrationContext = looksLikeCodeText(t) || looksLikeLogText(t) || containsOnlySafeAcronymCaps(t);

            if (!nonFrustrationContext && hasExplicitFrustrationSignal(t) && !consistentCapsStyle) {
                score += 0.20;
                if (reason.isEmpty()) {
                    reason = "frustration_phrase";
                }
            }
        }

        if (score > 1.0) {
            score = 1.0;
        }
        out.score = score;
        out.detected = score >= 0.40;
        out.reason = reason;
        return out;
    }

    private boolean looksLikeCodeText(String t) {
        if (t == null || t.isBlank()) {
            return false;
        }
        String s = t;
        String lower = s.toLowerCase();

        if (s.contains("```")) {
            return true;
        }

        int codeHints = 0;
        String[] keywords = {
            "select ", " from ", " where ", " join ", "insert ", "update ", "delete ",
            " function ", " class ", " public ", " private ", " protected ", " return ",
            " if(", " if (", " for(", " for (", " while(", " while ("
        };
        for (String k : keywords) {
            if (lower.contains(k)) {
                codeHints++;
            }
        }

        if (s.contains("{") || s.contains("}") || s.contains(";") || s.contains("=>") || s.contains("::")) {
            codeHints++;
        }

        int sym = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ("{}[]();=<>/_\\|".indexOf(c) >= 0) {
                sym++;
            }
        }
        double symRatio = s.isEmpty() ? 0.0 : ((double) sym / (double) s.length());
        if (symRatio > 0.08) {
            codeHints++;
        }

        return codeHints >= 2;
    }

    private boolean looksLikeLogText(String t) {
        if (t == null || t.isBlank()) {
            return false;
        }
        String s = t;

        int hints = 0;
        if (LOGGER_TOKEN.matcher(s).find()) {
            hints++;
        }
        if (s.matches(".*\\b\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}.*")) {
            hints++;
        }
        if (s.matches(".*\\b\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?\\b.*")) {
            hints++;
        }
        if (s.contains(" - ") || s.contains(" | ") || s.contains("::")) {
            hints++;
        }
        if (s.contains("Exception") || s.contains("Stacktrace") || s.contains("at com.")) {
            hints++;
        }

        return hints >= 2;
    }

    private boolean containsOnlySafeAcronymCaps(String t) {
        if (t == null || t.isBlank()) {
            return false;
        }
        String[] tokens = t.split("[^A-Za-z0-9_]+");
        boolean sawCapsToken = false;
        for (String tok : tokens) {
            if (tok == null || tok.isBlank()) {
                continue;
            }
            if (tok.length() < 2) {
                continue;
            }
            boolean isAllCaps = tok.equals(tok.toUpperCase()) && tok.matches("[A-Z0-9_]+");
            if (!isAllCaps) {
                continue;
            }
            sawCapsToken = true;
            if (!SAFE_ACRONYMS.contains(tok)) {
                return false;
            }
        }
        return sawCapsToken;
    }

    private boolean hasExplicitFrustrationSignal(String t) {
        if (t == null || t.isBlank()) {
            return false;
        }
        if (PROFANITY_PATTERN.matcher(t).find()) {
            return true;
        }
        return FRUSTRATION_PHRASE_PATTERN.matcher(t).find();
    }

    private boolean isConsistentCapsStyle(List<String> prompts) {
        if (prompts == null || prompts.size() < 3) {
            return false;
        }

        int capsCount = 0;
        int nonCodeCount = 0;

        for (String p : prompts) {
            if (p == null || p.isBlank()) {
                continue;
            }
            if (looksLikeCodeText(p) || looksLikeLogText(p) || containsOnlySafeAcronymCaps(p)) {
                continue;
            }
            nonCodeCount++;
            if (ALL_CAPS_WORD.matcher(p).find()) {
                capsCount++;
            }
        }

        if (nonCodeCount < 3) {
            return false;
        }

        return ((double) capsCount / (double) nonCodeCount) >= 0.60;
    }

    private String buildJson(List<Row> rows) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Row r : rows) {
            arr.add(Json.createObjectBuilder()
                    .add("sessionId", nvl(r.sessionId))
                    .add("displayLabel", nvl(r.displayLabel))
                    .add("widgetId", nvl(r.widgetId))
                    .add("widgetLabel", nvl(r.widgetLabel))
                    .add("chatCount", r.chatCount)
                    .add("lastEntry", formatTimestamp(r.lastEntry))
                    .add("frustrationDetected", r.frustrationDetected)
                    .add("frustrationScore", r.frustrationScore)
                    .add("frustrationReason", nvl(r.frustrationReason)));
        }
        JsonObject obj = Json.createObjectBuilder().add("rows", arr).build();
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(); JsonWriter writer = Json.createWriter(bos)) {
            writer.writeObject(obj);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize inactive users JSON", e);
        }
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
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
        String normalized = widgetId == null ? "widget" : widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
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

    private String quoteIdentifier(String s) {
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private int parseInt(String v, int fallback) {
        if (v == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid numeric parameter value", e);
            return fallback;
        }
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

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String sanitizeWidgetId(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return SAFE_WIDGET_ID.matcher(trimmed).matches() ? trimmed : "";
    }

    private String sanitizeSearch(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_SEARCH_LENGTH) {
            return trimmed.substring(0, MAX_SEARCH_LENGTH);
        }
        return trimmed;
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "" : ISO_INSTANT_FMT.format(value.toInstant());
    }

    private String escapeHtml(String in) {
        if (in == null) {
            return "";
        }
        return in.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String safeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (!trimmed.startsWith("/") || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }
}
