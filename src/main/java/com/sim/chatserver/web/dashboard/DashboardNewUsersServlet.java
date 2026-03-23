package com.sim.chatserver.web.dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(
        name = "DashboardNewUsersServlet",
        urlPatterns = {
            "/dashboard/new-users",
            "/dashboard/new-users/data",
            "/dashboard/new-users/day"
        }
)
public class DashboardNewUsersServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardNewUsersServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_new_users.html";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int DEFAULT_DAYS = 7;
    private static final Set<Integer> ALLOWED_DAYS = Set.of(7, 14, 30, 90);

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                if (req.getServletPath().endsWith("/data") || req.getServletPath().endsWith("/day")) {
                    writeJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/login");
                }
                return;
            }

            String path = req.getServletPath();

            if (path.endsWith("/data")) {
                handleData(req, resp);
                return;
            }
            if (path.endsWith("/day")) {
                handleDay(req, resp);
                return;
            }

            handlePage(req, resp, session);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed in /dashboard/new-users flow", e);
            if (!resp.isCommitted()) {
                if (reqExpectsJson(req)) {
                    writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load new user metrics.");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.getWriter().write("Unable to load new user metrics.");
                }
            }
        }
    }

    private boolean reqExpectsJson(HttpServletRequest req) {
        String path = req.getServletPath();
        return path.endsWith("/data") || path.endsWith("/day");
    }

    private void handlePage(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        int days = parseDays(req.getParameter("days")).orElse(DEFAULT_DAYS);
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(days - 1);

        Metrics metrics = loadMetrics(start, end, req.getContextPath());

        String user = String.valueOf(session.getAttribute("user"));
        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
                .replace("${contextPath}", escapeHtml(req.getContextPath()))
                .replace("${user}", escapeHtml(user))
                .replace("${trendJson}", escapeForJs(metrics.toTrendJson()))
                .replace("${latestRows}", metrics.renderLatestRows())
                .replace("${rangeStart}", escapeHtml(start.format(DATE_FMT)))
                .replace("${rangeEnd}", escapeHtml(end.format(DATE_FMT)))
                .replace("${selectedDays}", String.valueOf(days));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private void handleData(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LocalDate rangeEnd = LocalDate.now(ZoneId.systemDefault());
        int days = parseDays(req.getParameter("days")).orElse(DEFAULT_DAYS);
        LocalDate rangeStart = rangeEnd.minusDays(days - 1);

        Metrics metrics = loadMetrics(rangeStart, rangeEnd, req.getContextPath());

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        metrics.byDay.forEach((d, v) -> {
            labels.add(d.format(DATE_FMT));
            values.add(v);
        });

        JsonArrayBuilder latest = Json.createArrayBuilder();
        int rank = 1;
        for (LatestRow r : metrics.latest) {
            latest.add(Json.createObjectBuilder()
                    .add("rank", rank++)
                    .add("display", safe(r.display))
                    .add("sessionId", safe(r.rawSessionId))
                    .add("firstSeen", safe(r.firstSeen))
                    .add("totalChats", r.totalChats)
                    .add("chatEntriesUrl", safe(r.chatEntriesUrl)));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("rangeStart", rangeStart.format(DATE_FMT))
                .add("rangeEnd", rangeEnd.format(DATE_FMT))
                .add("trend", Json.createObjectBuilder()
                        .add("labels", labels)
                        .add("values", values))
                .add("latest", latest)
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(payload.toString());
    }

    private void handleDay(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Optional<LocalDate> dayOpt = parseLocalDate(req.getParameter("day"));
        if (dayOpt.isEmpty()) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid day.");
            return;
        }
        LocalDate day = dayOpt.get();

        DayResult result = loadDayFirstSeen(day, req.getContextPath());

        JsonArrayBuilder rows = Json.createArrayBuilder();
        int rank = 1;
        for (LatestRow r : result.rows) {
            rows.add(Json.createObjectBuilder()
                    .add("rank", rank++)
                    .add("display", safe(r.display))
                    .add("sessionId", safe(r.rawSessionId))
                    .add("firstSeen", safe(r.firstSeen))
                    .add("totalChats", r.totalChats)
                    .add("chatEntriesUrl", safe(r.chatEntriesUrl)));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("day", day.format(DATE_FMT))
                .add("count", result.rows.size())
                .add("rows", rows)
                .build();

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(payload.toString());
    }

    private Metrics loadMetrics(LocalDate rangeStart, LocalDate rangeEnd, String contextPath) {
        Metrics metrics = new Metrics(rangeStart, rangeEnd);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            List<WidgetEntry> widgets;
            try {
                widgets = WidgetStore.list(null);
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to list widgets", e);
                widgets = List.of();
            }
            computeNewSessionMetrics(conn, widgets, metrics, contextPath);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to compute new-user metrics", e);
        }
        return metrics;
    }

    private DayResult loadDayFirstSeen(LocalDate day, String contextPath) throws Exception {
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            widgets = List.of();
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, Timestamp> earliestBySession = findEarliestBySession(conn, widgets);
            Map<String, Integer> totalsBySession = findTotalChatsBySession(conn, widgets);

            List<Map.Entry<String, Timestamp>> forDay = earliestBySession.entrySet().stream()
                    .filter(e -> {
                        LocalDate d = e.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        return d.equals(day);
                    })
                    .sorted(Map.Entry.<String, Timestamp>comparingByValue(Comparator.reverseOrder()))
                    .toList();

            Set<String> ids = new LinkedHashSet<>();
            for (Map.Entry<String, Timestamp> e : forDay) {
                ids.add(e.getKey());
            }

            Map<String, SessionLabelStore.SessionLabel> labels;
            try {
                labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
            } catch (Exception ex) {
                labels = Map.of();
            }

            List<LatestRow> rows = new ArrayList<>();
            for (Map.Entry<String, Timestamp> e : forDay) {
                String sid = e.getKey();
                Timestamp ts = e.getValue();
                String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
                String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
                int totalChats = totalsBySession.getOrDefault(sid, 0);
                String chatEntriesUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + urlEncode(sid);
                rows.add(new LatestRow(display, sid, firstSeen, totalChats, chatEntriesUrl));
            }

            return new DayResult(rows);
        }
    }

    private void computeNewSessionMetrics(Connection conn, List<WidgetEntry> widgets, Metrics metrics, String contextPath) throws Exception {
        Map<String, Timestamp> earliestBySession = findEarliestBySession(conn, widgets);
        Map<String, Integer> totalsBySession = findTotalChatsBySession(conn, widgets);

        for (Map.Entry<String, Timestamp> e : earliestBySession.entrySet()) {
            LocalDate d = e.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            metrics.incrementDay(d);
        }

        List<Map.Entry<String, Timestamp>> sorted = new ArrayList<>(earliestBySession.entrySet());
        sorted.sort(Map.Entry.<String, Timestamp>comparingByValue(Comparator.reverseOrder()));

        Set<String> ids = new LinkedHashSet<>();
        for (Map.Entry<String, Timestamp> e : sorted) {
            ids.add(e.getKey());
        }

        Map<String, SessionLabelStore.SessionLabel> labels;
        try {
            labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to resolve session labels", e);
            labels = Map.of();
        }

        int take = Math.min(10, sorted.size());
        for (int i = 0; i < take; i++) {
            String sid = sorted.get(i).getKey();
            Timestamp ts = sorted.get(i).getValue();
            String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
            String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
            int totalChats = totalsBySession.getOrDefault(sid, 0);

            String chatEntriesUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + urlEncode(sid);
            metrics.latest.add(new LatestRow(display, sid, firstSeen, totalChats, chatEntriesUrl));
        }
    }

    private Map<String, Timestamp> findEarliestBySession(Connection conn, List<WidgetEntry> widgets) throws Exception {
        Map<String, Timestamp> earliestBySession = new LinkedHashMap<>();
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }
            String table = sanitizeWidgetTableName(widget.getWidgetId());
            if (!tableExists(conn, table)) {
                continue;
            }

            String sql = "SELECT session_id, MIN(created_at) AS first_seen FROM " + quoteIdentifier(table)
                    + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    Timestamp ts = rs.getTimestamp("first_seen");
                    if (sid == null || sid.isBlank() || ts == null) {
                        continue;
                    }
                    sid = sid.trim();

                    Timestamp prev = earliestBySession.get(sid);
                    if (prev == null || ts.before(prev)) {
                        earliestBySession.put(sid, ts);
                    }
                }
            }
        }
        return earliestBySession;
    }

    private Map<String, Integer> findTotalChatsBySession(Connection conn, List<WidgetEntry> widgets) throws Exception {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                continue;
            }
            String table = sanitizeWidgetTableName(widget.getWidgetId());
            if (!tableExists(conn, table)) {
                continue;
            }

            String sql = "SELECT session_id, COUNT(*) AS c FROM " + quoteIdentifier(table)
                    + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    if (sid == null || sid.isBlank()) {
                        continue;
                    }
                    sid = sid.trim();
                    int c = rs.getInt("c");
                    totals.merge(sid, c, Integer::sum);
                }
            }
        }
        return totals;
    }

    private Optional<Integer> parseDays(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            int d = Integer.parseInt(value.trim());
            return ALLOWED_DAYS.contains(d) ? Optional.of(d) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, DATE_FMT));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        for (String c : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, c, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String loadTemplate(HttpServletRequest req, String path) throws IOException {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    b.append(line).append('\n');
                }
                return b.toString();
            }
        }
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String n = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (n.isEmpty()) {
            n = "widget";
        }
        if (!Character.isLetter(n.charAt(0))) {
            n = "w_" + n;
        }
        if (n.length() > 60) {
            n = n.substring(0, 60);
        }
        return n;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String urlEncode(String v) {
        return java.net.URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        JsonObject body = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "Request failed." : message)
                .build();
        resp.getWriter().write(body.toString());
    }

    private static final class Metrics {

        final LocalDate start;
        final LocalDate end;
        final Map<LocalDate, Integer> byDay = new LinkedHashMap<>();
        final List<LatestRow> latest = new ArrayList<>();

        Metrics(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
            long days = ChronoUnit.DAYS.between(start, end);
            for (int i = 0; i <= days; i++) {
                byDay.put(start.plusDays(i), 0);
            }
        }

        void incrementDay(LocalDate day) {
            if (day == null || day.isBefore(start) || day.isAfter(end)) {
                return;
            }
            byDay.put(day, byDay.getOrDefault(day, 0) + 1);
        }

        String toTrendJson() {
            JsonArrayBuilder labels = Json.createArrayBuilder();
            JsonArrayBuilder values = Json.createArrayBuilder();
            byDay.forEach((d, c) -> {
                labels.add(d.format(DATE_FMT));
                values.add(c);
            });
            JsonObject o = Json.createObjectBuilder()
                    .add("labels", labels)
                    .add("values", values)
                    .build();
            return o.toString();
        }

        String renderLatestRows() {
            if (latest.isEmpty()) {
                return "<tr><td colspan=\"4\" class=\"empty-row\">No new session IDs found.</td></tr>";
            }
            StringBuilder b = new StringBuilder();
            int rank = 1;
            for (LatestRow r : latest) {
                b.append("<tr>")
                        .append("<td>").append(rank++).append("</td>")
                        .append("<td>").append(escapeHtmlStatic(r.display)).append("</td>")
                        .append("<td>").append(escapeHtmlStatic(r.firstSeen)).append("</td>")
                        .append("<td><a class=\"session-count-link\" href=\"")
                        .append(escapeHtmlStatic(r.chatEntriesUrl))
                        .append("\">")
                        .append(r.totalChats)
                        .append(" chats</a></td>")
                        .append("</tr>");
            }
            return b.toString();
        }

        private static String escapeHtmlStatic(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }
    }

    private static final class DayResult {

        final List<LatestRow> rows;

        DayResult(List<LatestRow> rows) {
            this.rows = rows;
        }
    }

    private static final class LatestRow {

        final String display;
        final String rawSessionId;
        final String firstSeen;
        final int totalChats;
        final String chatEntriesUrl;

        LatestRow(String display, String rawSessionId, String firstSeen, int totalChats, String chatEntriesUrl) {
            this.display = display;
            this.rawSessionId = rawSessionId;
            this.firstSeen = firstSeen;
            this.totalChats = totalChats;
            this.chatEntriesUrl = chatEntriesUrl;
        }
    }
}
