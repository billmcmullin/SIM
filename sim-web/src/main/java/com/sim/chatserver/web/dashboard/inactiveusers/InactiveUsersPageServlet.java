package com.sim.chatserver.web.dashboard.inactiveusers;

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
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
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
    private static final int MAX_SESSION_ID_LENGTH = 128;
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users.html";
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    private static final int FRUSTRATION_PROMPT_SCAN_LIMIT = 8;
    private static final Pattern SAFE_SESSION_ID = Pattern.compile("^[A-Za-z0-9_:\\-.]+$");

    static final class InactiveRow {

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

    static final class FrustrationResult {

        boolean detected;
        double score;
        String reason;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        int days = parseInt(ServletRequestParamUtil.firstParam(req, "days", 256, false, false), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
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
                    } catch (Throwable ex) {
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
                    InactiveRow ar = allAgg.get(wr.sessionId);
                    if (ar == null) {
                        ar = new InactiveRow();
                        ar.sessionId = wr.sessionId;
                        ar.widgetId = "ALL";
                        ar.widgetLabel = "All Widgets";
                        ar.chats = 0;
                        ar.lastEntry = null;
                        ar.frustrationDetected = false;
                        ar.frustrationScore = 0.0;
                        ar.frustrationReason = "";
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
        String user = safeSessionUser(httpSession);
        String contextPath = ServletPathUtil.safeContextPathNoEmptyGuard(req.getContextPath());
        String jsonDataB64 = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));

        String rendered = template
            .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(user))
                .replace("${defaultDays}", String.valueOf(days))
            .replace("${inactiveUsersDataB64}", jsonDataB64);

        byte[] renderedBytes = rendered.getBytes(StandardCharsets.UTF_8);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.setContentLength(renderedBytes.length);
        try (var output = resp.getOutputStream()) {
            output.write(renderedBytes);
        }
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private Map<String, SessionLabelStore.SessionLabel> mapLabelsSafe(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(ids);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
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

    private Map<String, InactiveRow> querySessionAggregateForTable(Connection conn, String table, String widgetId, String widgetLabel) {
        Map<String, InactiveRow> out = new LinkedHashMap<>();
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String sid = sanitizeSessionId(readDbText(rs, "session_id", MAX_SESSION_ID_LENGTH));
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
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to aggregate inactive sessions for table " + table, e);
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

        String[] cols = {"prompt", "prompt_text", "user_prompt"};
        for (String col : cols) {
            String sql = "SELECT " + quoteIdentifier(col) + " AS p FROM " + quoteIdentifier(table)
                    + " WHERE session_id = ? AND " + quoteIdentifier(col) + " IS NOT NULL AND " + quoteIdentifier(col) + " <> ''"
                    + " ORDER BY created_at DESC";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = conn.prepareStatement(sql);
                ps.setString(1, sessionId);
                ps.setMaxRows(limit);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String p = safeDbText(rs.getString("p"), 2000);
                    if (p != null && !p.isBlank()) {
                        prompts.add(p);
                    }
                }
                if (!prompts.isEmpty()) {
                    return prompts;
                }
            } catch (SQLException ex) {
                log.log(Level.FINE, "Prompt column not available for table " + table + ": " + col, ex);
            } finally {
                closeQuietly(rs);
                closeQuietly(ps);
            }
        }
        return prompts;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                java.util.logging.Logger.getLogger("OWASP").log(java.util.logging.Level.FINE, "Handled exception", e);
                // ignore close failure
            }
        }
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

        boolean consistentCapsStyle = InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(prompts);

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

            boolean nonFrustrationContext = InactiveUsersFrustrationTextUtil.isNonFrustrationContext(t);

            if (!nonFrustrationContext
                    && InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal(t)
                    && !consistentCapsStyle) {
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

        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(payload);
            return sw.toString();
        }
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
        if (s == null || !SAFE_SQL_IDENTIFIER.matcher(s).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private int parseInt(String v, int fallback) {
        if (v == null || v.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid integer request parameter value", e);
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
            log.log(Level.FINE, "Unable to read DB text for column " + column, e);
            return null;
        }
        String value = raw == null ? null : String.valueOf(raw);
        return safeDbText(value, maxLen);
    }

    private String safeDbText(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u0000', ' ').replace("\r", "").replace("\n", "").trim();
        if (normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }
        return normalized;
    }

    private String loadTemplate(ServletContext context, String path) {
        if (context == null || path == null || path.isBlank()) {
            return "";
        }
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template not found: {0}", path);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    b.append(line).append('\n');
                }
                return b.toString();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load template " + path, e);
            return "";
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String safeSessionUser(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object userAttr = session.getAttribute("user");
        if (!(userAttr instanceof String user)) {
            return "";
        }
        String trimmed = user.trim();
        return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
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
