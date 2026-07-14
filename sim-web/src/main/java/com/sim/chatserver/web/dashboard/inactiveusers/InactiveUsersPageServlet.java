package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InactiveUsersPageServlet", urlPatterns = {"/dashboard/inactive-users"})
public class InactiveUsersPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(InactiveUsersPageServlet.class.getName());
    private static final int DEFAULT_DAYS = 7;
    private static final int TOP_N = 5;
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users.html";

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

    private static final class InactiveRow {

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

    private static final class FrustrationResult {

        boolean detected;
        double score;
        String reason;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        int days = parseInt(firstParam(req, "days"), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | RuntimeException e) {
            log.log(Level.WARNING, "Unable to load widgets", e);
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

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
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
                    } catch (SQLException | RuntimeException ex) {
                        row.frustrationDetected = false;
                        row.frustrationScore = 0.0;
                        row.frustrationReason = "";
                        log.log(Level.FINE, "Frustration detection skipped for session " + row.sessionId + " table " + table, ex);
                    }
                }

                Set<String> widgetSessionIds = perWidgetAgg.keySet();
                Map<String, SessionLabelStore.SessionLabel> widgetLabels = mapLabelsSafe(widgetSessionIds);

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
                    InactiveRow ar = allAgg.computeIfAbsent(wr.sessionId, k -> {
                        InactiveRow x = new InactiveRow();
                        x.sessionId = k;
                        x.widgetId = "ALL";
                        x.widgetLabel = "All Widgets";
                        x.chats = 0;
                        x.lastEntry = null;
                        x.frustrationDetected = false;
                        x.frustrationScore = 0.0;
                        x.frustrationReason = "";
                        return x;
                    });
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
        } catch (SQLException | RuntimeException e) {
            log.log(Level.SEVERE, "Unable to compute inactive users", e);
        }

        List<InactiveRow> allRows = new ArrayList<>();
        Map<String, SessionLabelStore.SessionLabel> allLabels = mapLabelsSafe(allAgg.keySet());
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
        payload.put("ALL", allRows);
        payload.putAll(byWidget);

        String jsonData = buildInactiveUsersJson(payload, widgetNameById);
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String user = String.valueOf(httpSession.getAttribute("user"));
        String contextPath = safeContextPath(req.getContextPath());
        String jsonDataB64 = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));

        String rendered = template
            .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(user))
                .replace("${defaultDays}", String.valueOf(days))
            .replace("${inactiveUsersDataB64}", jsonDataB64);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, SessionLabelStore.SessionLabel> mapLabelsSafe(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException | RuntimeException e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
            return Map.of();
        }
    }

    private boolean isInactive(Timestamp lastEntry, Instant cutoff) {
        if (lastEntry == null) {
            return false;
        }
        return lastEntry.toInstant().toEpochMilli() < cutoff.toEpochMilli();
    }

    private Map<String, InactiveRow> querySessionAggregateForTable(Connection conn, String table, String widgetId, String widgetLabel) throws SQLException {
        Map<String, InactiveRow> out = new LinkedHashMap<>();
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sid = safeDbText(rs.getString("session_id"), 256);
                Timestamp last = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                if (sid == null || sid.isBlank()) {
                    continue;
                }

                InactiveRow r = new InactiveRow();
                r.sessionId = sid.trim();
                r.displayLabel = r.sessionId;
                r.widgetId = widgetId;
                r.widgetLabel = widgetLabel;
                r.lastEntry = last;
                r.chats = rs.getLong("total");
                r.frustrationDetected = false;
                r.frustrationScore = 0.0;
                r.frustrationReason = "";
                out.put(r.sessionId, r);
            }
        }
        return out;
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) throws SQLException {
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
                        String p = safeDbText(rs.getString("p"), 2000);
                        if (p != null && !p.isBlank()) {
                            prompts.add(p);
                        }
                    }
                    if (!prompts.isEmpty()) {
                        return prompts;
                    }
                }
            } catch (SQLException ex) {
                log.log(Level.FINE, "Prompt column not available for table " + table + ": " + col, ex);
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

    private String buildInactiveUsersJson(Map<String, List<InactiveRow>> byWidget, Map<String, String> widgetNameById) {
        JsonArrayBuilder allArr = Json.createArrayBuilder();
        JsonObjectBuilder widgetsObj = Json.createObjectBuilder();
        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();

        for (Map.Entry<String, String> e : widgetNameById.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue() == null ? e.getKey() : e.getValue());
        }

        for (InactiveRow r : byWidget.getOrDefault("ALL", List.of())) {
            allArr.add(toJson(r));
        }

        for (Map.Entry<String, List<InactiveRow>> e : byWidget.entrySet()) {
            if ("ALL".equals(e.getKey())) {
                continue;
            }
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (InactiveRow r : e.getValue()) {
                arr.add(toJson(r));
            }
            widgetsObj.add(e.getKey(), arr);
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("all", allArr)
                .add("widgets", widgetsObj)
                .add("widgetNames", widgetNamesObj)
                .build();

        return payload.toString();
    }

    private JsonObject toJson(InactiveRow r) {
        return Json.createObjectBuilder()
                .add("sessionId", nvl(r.sessionId))
                .add("displayLabel", nvl(r.displayLabel))
                .add("widgetId", nvl(r.widgetId))
                .add("widgetLabel", nvl(r.widgetLabel))
                .add("chatCount", r.chats)
                .add("lastEntry", r.lastEntry == null ? "" : r.lastEntry.toInstant().toString())
                .add("frustrationDetected", r.frustrationDetected)
                .add("frustrationScore", r.frustrationScore)
                .add("frustrationReason", nvl(r.frustrationReason))
                .build();
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
        if (v == null || v.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (RuntimeException e) {
            log.log(Level.FINE, "Invalid integer request parameter value: {0}", sanitizeForLog(v));
            return fallback;
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String[] values = req.getParameterValues(name);
        if (values == null || values.length == 0) {
            return null;
        }
        String value = values[0];
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
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

    private String safeDbText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u0000', ' ').replace("\r", "").trim();
        if (normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }
        return normalized;
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    b.append(line).append('\n');
                }
                return b.toString();
            }
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
