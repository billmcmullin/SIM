package com.sim.chatserver.web.dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard.html";
    private static final String TERM_SNAPSHOT_SESSION_KEY = "termDistributionSnapshots";
    private static final int DEFAULT_RANGE_DAYS = 14;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ENTRY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();
        String adminLink = "ADMIN".equalsIgnoreCase(role)
                ? "<p><a href=\"" + req.getContextPath() + "/admin\">Go to Admin Configuration</a></p>"
                : "";

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widget registry for dashboard", e);
        }

        List<WidgetStat> widgetStats = buildWidgetStats(widgets);
        int totalChats = widgetStats.stream().mapToInt(stat -> stat.count).sum();
        String statsRows = renderWidgetStatsRows(widgetStats, req.getContextPath());
        String widgetPieChartData = buildWidgetPieChartData(widgetStats);

        LocalDate rangeEnd = parseLocalDate(req.getParameter("rangeEnd"))
                .orElse(LocalDate.now(ZoneId.systemDefault()));
        LocalDate rangeStart = parseLocalDate(req.getParameter("rangeStart"))
                .orElse(rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1));
        if (rangeStart.isAfter(rangeEnd)) {
            rangeStart = rangeEnd.minusDays(DEFAULT_RANGE_DAYS - 1);
        }

        String termChartJson = "[]";
        TermSummary summary = null;
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            List<TermDefinition> terms = termsStore.listAll();
            summary = buildTermSummary(conn, widgets, terms);
            termChartJson = summary.toJson();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to compute term summaries", e);
        }

        if (summary != null) {
            storeTermSnapshots(session, summary);
        } else {
            session.removeAttribute(TERM_SNAPSHOT_SESSION_KEY);
        }

        String sessionRows = "<tr><td colspan=\"4\" class=\"empty-row\">No session activity available.</td></tr>";
        String sessionChartJson = buildEmptySessionPayload(rangeStart, rangeEnd);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            SessionOverview sessionOverview = buildSessionOverview(conn, widgets, rangeStart, rangeEnd);
            Map<String, SessionLabelStore.SessionLabel> sessionLabels = loadSessionLabels(sessionOverview.topSessions);
            sessionRows = renderSessionRows(sessionOverview.topSessions, sessionLabels, req.getContextPath());
            sessionChartJson = buildSessionChartPayload(sessionOverview, rangeStart, rangeEnd);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to compute session metrics", e);
            sessionRows = "<tr><td colspan=\"4\" class=\"empty-row\">Unable to load session activity.</td></tr>";
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String userName = String.valueOf(session.getAttribute("user"));
        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${contextPath}", req.getContextPath())
                .replace("${role}", escapeHtml(role))
                .replace("${adminLink}", adminLink)
                .replace("${totalChats}", escapeHtml(String.valueOf(totalChats)))
                .replace("${widgetStatsRows}", statsRows)
                .replace("${widgetPieChartData}", escapeForJs(widgetPieChartData))
                .replace("${termChartData}", termChartJson)
                .replace("${sessionRows}", sessionRows)
                .replace("${sessionRangeStart}", escapeHtml(rangeStart.format(DATE_FORMATTER)))
                .replace("${sessionRangeEnd}", escapeHtml(rangeEnd.format(DATE_FORMATTER)))
                .replace("${sessionChartData}", escapeForJs(sessionChartJson));

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private String buildWidgetPieChartData(List<WidgetStat> stats) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        if (stats != null) {
            for (WidgetStat stat : stats) {
                arr.add(Json.createObjectBuilder()
                        .add("widgetId", stat.widgetId == null ? "" : stat.widgetId)
                        .add("label", stat.label == null ? "" : stat.label)
                        .add("count", stat.count));
            }
        }
        return arr.build().toString();
    }

    private Map<String, SessionLabelStore.SessionLabel> loadSessionLabels(List<SessionStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }
        Set<String> sessionIds = stats.stream()
                .map(stat -> stat.sessionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (sessionIds.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(sessionIds);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load session labels for dashboard", e);
            return Map.of();
        }
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, DATE_FORMATTER));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildSessionChartPayload(SessionOverview overview, LocalDate rangeStart, LocalDate rangeEnd) {
        JsonArrayBuilder labelBuilder = Json.createArrayBuilder();
        for (String label : overview.timeline.labels) {
            labelBuilder.add(label);
        }

        JsonArrayBuilder seriesBuilder = Json.createArrayBuilder();
        for (SessionStat session : overview.topSessions) {
            JsonArrayBuilder countsBuilder = Json.createArrayBuilder();
            List<Integer> values = overview.timeline.countsBySession.get(session.sessionId);
            if (values != null) {
                for (Integer value : values) {
                    countsBuilder.add(value == null ? 0 : value);
                }
            } else {
                for (int i = 0; i < overview.timeline.labels.size(); i++) {
                    countsBuilder.add(0);
                }
            }
            seriesBuilder.add(Json.createObjectBuilder()
                    .add("sessionId", session.sessionId)
                    .add("counts", countsBuilder));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("labels", labelBuilder)
                .add("series", seriesBuilder)
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build();
        return payload.toString();
    }

    private String buildEmptySessionPayload(LocalDate rangeStart, LocalDate rangeEnd) {
        JsonObject payload = Json.createObjectBuilder()
                .add("labels", Json.createArrayBuilder())
                .add("series", Json.createArrayBuilder())
                .add("rangeStart", rangeStart.format(DATE_FORMATTER))
                .add("rangeEnd", rangeEnd.format(DATE_FORMATTER))
                .build();
        return payload.toString();
    }

    private SessionOverview buildSessionOverview(Connection conn, List<WidgetEntry> widgets, LocalDate rangeStart, LocalDate rangeEnd) throws SQLException {
        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        if (widgets != null) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String tableName = sanitizeWidgetTableName(widget.getWidgetId());
                if (!tableExists(conn, tableName)) {
                    continue;
                }
                String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE session_id IS NOT NULL GROUP BY session_id";
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("session_id");
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }
                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.count += rs.getInt("total");
                        Timestamp lastEntry = rs.getTimestamp("last_entry");
                        if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                            acc.lastEntry = lastEntry;
                        }
                    }
                }
            }
        }

        List<SessionStat> topSessions = accumulators.entrySet()
                .stream()
                .sorted(Map.Entry.<String, SessionAccumulator>comparingByValue(Comparator.comparingInt(a -> -a.count)))
                .limit(10)
                .map(entry -> new SessionStat(
                entry.getKey(),
                entry.getValue().count,
                formatTimestamp(entry.getValue().lastEntry)))
                .collect(Collectors.toList());

        List<String> sessionIds = topSessions.stream()
                .map(stat -> stat.sessionId)
                .collect(Collectors.toList());

        SessionTimeline timeline = buildSessionTimeline(conn, widgets, sessionIds, rangeStart, rangeEnd);
        return new SessionOverview(topSessions, timeline);
    }

    private String formatSessionDisplayLabel(String sessionId, Map<String, SessionLabelStore.SessionLabel> labels) {
        if (sessionId == null) {
            return "";
        }
        return SessionLabelStore.resolveDisplayLabel(sessionId, labels == null ? null : labels.get(sessionId));
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        return ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(ENTRY_FORMATTER);
    }

    private SessionTimeline buildSessionTimeline(Connection conn, List<WidgetEntry> widgets, List<String> sessionIds, LocalDate rangeStart, LocalDate rangeEnd) throws SQLException {
        List<LocalDate> labelDates = new ArrayList<>();
        LocalDate cursor = rangeStart;
        if (!cursor.isAfter(rangeEnd)) {
            while (!cursor.isAfter(rangeEnd)) {
                labelDates.add(cursor);
                cursor = cursor.plusDays(1);
            }
        } else {
            labelDates.add(rangeStart);
        }

        List<String> labels = new ArrayList<>();
        for (LocalDate date : labelDates) {
            labels.add(date.format(DATE_FORMATTER));
        }

        Map<String, List<Integer>> countsBySession = new LinkedHashMap<>();
        for (String sessionId : sessionIds) {
            countsBySession.put(sessionId, new ArrayList<>(Collections.nCopies(labels.size(), 0)));
        }

        if (sessionIds.isEmpty() || widgets == null || widgets.isEmpty() || labels.isEmpty()) {
            return new SessionTimeline(labels, countsBySession);
        }

        String inClause = sessionIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String rangeSuffix = " AND created_at >= ? AND created_at < ?";

        Timestamp startTs = Timestamp.valueOf(rangeStart.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(rangeEnd.plusDays(1).atStartOfDay());

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                continue;
            }
            String sql = "SELECT session_id, created_at FROM " + quoteIdentifier(tableName)
                    + " WHERE session_id IN (" + inClause + ")" + rangeSuffix;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String sessionId : sessionIds) {
                    ps.setString(idx++, sessionId);
                }
                ps.setTimestamp(idx++, startTs);
                ps.setTimestamp(idx, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("session_id");
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (sessionId == null || createdAt == null) {
                            continue;
                        }
                        List<Integer> bucket = countsBySession.get(sessionId);
                        if (bucket == null) {
                            continue;
                        }
                        LocalDate entryDate = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        long dayIndex = ChronoUnit.DAYS.between(rangeStart, entryDate);
                        if (dayIndex < 0 || dayIndex >= bucket.size()) {
                            continue;
                        }
                        int position = (int) dayIndex;
                        bucket.set(position, bucket.get(position) + 1);
                    }
                }
            }
        }

        return new SessionTimeline(labels, countsBySession);
    }

    private String renderSessionRows(List<SessionStat> stats, Map<String, SessionLabelStore.SessionLabel> labels, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"empty-row\">No session activity recorded yet.</td></tr>";
        }
        StringBuilder builder = new StringBuilder();
        int rank = 1;
        for (SessionStat stat : stats) {
            String display = formatSessionDisplayLabel(stat.sessionId, labels);
            String encodedSession = URLEncoder.encode(stat.sessionId, StandardCharsets.UTF_8);
            String url = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + encodedSession;
            builder.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td><span class=\"session-link\">").append(escapeHtml(display)).append("</span>");
            if (!stat.sessionId.equals(display)) {
                builder.append("<div class=\"session-id-muted\">").append(escapeHtml(stat.sessionId)).append("</div>");
            }
            builder.append("</td>")
                    .append("<td><a class=\"session-count-link\" href=\"").append(url).append("\">")
                    .append(stat.count).append(" chats</a></td>")
                    .append("<td><span class=\"session-last-entry\">").append(escapeHtml(stat.lastEntry)).append("</span></td>")
                    .append("</tr>");
        }
        return builder.toString();
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

    private void storeTermSnapshots(HttpSession session, TermSummary summary) {
        Map<String, List<TermChatSnapshot>> copies = summary.copyTermSnapshots();
        session.setAttribute(TERM_SNAPSHOT_SESSION_KEY, copies);
    }

    private String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats.isEmpty()) {
            return "<tr><td colspan=\"2\" class=\"empty-row\">No widget chats available.</td></tr>";
        }
        StringBuilder builder = new StringBuilder();
        for (WidgetStat stat : stats) {
            String widgetUrl = contextPath + "/dashboard/widgets/view?widgetId="
                    + URLEncoder.encode(stat.widgetId, StandardCharsets.UTF_8);

            builder.append("<tr>")
                    .append("<td>").append(escapeHtml(stat.label)).append("</td>")
                    .append("<td>")
                    .append("<button type=\"button\" class=\"ghost-btn\" ")
                    .append("onclick=\"window.location.href='").append(escapeHtml(widgetUrl)).append("'\">")
                    .append(escapeHtml(String.valueOf(stat.count))).append(" chats")
                    .append("</button>")
                    .append("</td>")
                    .append("</tr>");
        }
        return builder.toString();
    }

    private List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        List<WidgetStat> stats = new ArrayList<>();
        if (widgets == null || widgets.isEmpty()) {
            return stats;
        }
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String displayName = widget.getDisplayName();
                displayName = displayName == null || displayName.isBlank() ? widgetId : displayName;
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }
                int count = countRows(conn, tableName);
                stats.add(new WidgetStat(widgetId, displayName, count));
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to query widget tables", e);
        }
        return stats;
    }

    private TermSummary buildTermSummary(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        TermSummary summary = new TermSummary();
        if (widgets == null || widgets.isEmpty() || terms == null) {
            return summary;
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : terms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            activeTerms.add(term);
            Pattern p = TermMatcher.buildStrictPattern(term);
            compiledPatterns.add(p);
            summary.ensureTerm(term.getName());
        }

        String otherLabel = "Other Parasoft Match";
        summary.ensureTerm(otherLabel);

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }
            String widgetId = widget.getWidgetId();
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                continue;
            }
            String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM " + quoteIdentifier(tableName);
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chatId = rs.getString("widget_chat_id");
                    if (chatId == null) {
                        chatId = "";
                    }
                    String prompt = rs.getString("prompt");
                    if (prompt == null) {
                        prompt = "";
                    }
                    String response = rs.getString("response_text");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    String sessionId = rs.getString("session_id");

                    final String sanitizedPrompt = TextSanitizer.sanitizeForMatching(prompt);
                    TermDefinition bestTerm = null;
                    int bestStart = Integer.MAX_VALUE;

                    for (int i = 0; i < compiledPatterns.size(); i++) {
                        Pattern pattern = compiledPatterns.get(i);
                        try {
                            Matcher m = pattern.matcher(sanitizedPrompt);
                            if (m.find()) {
                                int start = m.start();
                                if (start < bestStart) {
                                    bestStart = start;
                                    bestTerm = activeTerms.get(i);
                                    if (bestStart == 0) {
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warning("Pattern match failure for term '" + activeTerms.get(i).getName() + "': " + e.getMessage());
                        }
                    }

                    String snapshotTerm = otherLabel;
                    if (bestTerm != null) {
                        snapshotTerm = bestTerm.getName();
                    }

                    TermChatSnapshot snapshot = new TermChatSnapshot(
                            snapshotTerm,
                            widgetId,
                            chatId,
                            prompt,
                            response,
                            createdAt,
                            sessionId
                    );
                    summary.recordMatch(snapshotTerm, snapshot);
                }
            }
        }
        return summary;
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
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

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class WidgetStat {

        private final String widgetId;
        private final String label;
        private final int count;

        private WidgetStat(String widgetId, String label, int count) {
            this.widgetId = widgetId;
            this.label = label;
            this.count = count;
        }
    }

    private static final class SessionOverview {

        private final List<SessionStat> topSessions;
        private final SessionTimeline timeline;

        private SessionOverview(List<SessionStat> topSessions, SessionTimeline timeline) {
            this.topSessions = List.copyOf(topSessions);
            this.timeline = timeline;
        }
    }

    private static final class SessionStat {

        private final String sessionId;
        private final int count;
        private final String lastEntry;

        private SessionStat(String sessionId, int count, String lastEntry) {
            this.sessionId = sessionId;
            this.count = count;
            this.lastEntry = lastEntry;
        }
    }

    private static final class SessionTimeline {

        private final List<String> labels;
        private final Map<String, List<Integer>> countsBySession;

        private SessionTimeline(List<String> labels, Map<String, List<Integer>> countsBySession) {
            this.labels = List.copyOf(labels);
            this.countsBySession = new LinkedHashMap<>(countsBySession);
        }
    }

    private static final class SessionAccumulator {

        private int count = 0;
        private Timestamp lastEntry = null;
    }

    private static final class TermSummary {

        private final Map<String, Integer> termCounts = new LinkedHashMap<>();
        private final Map<String, List<TermChatSnapshot>> termSnapshots = new LinkedHashMap<>();

        private void ensureTerm(String termName) {
            termCounts.putIfAbsent(termName, 0);
            termSnapshots.putIfAbsent(termName, new ArrayList<>());
        }

        private void recordMatch(String termName, TermChatSnapshot snapshot) {
            termCounts.merge(termName, 1, Integer::sum);
            termSnapshots.computeIfAbsent(termName, k -> new ArrayList<>()).add(snapshot);
        }

        private String toJson() {
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
                builder.add(Json.createObjectBuilder()
                        .add("label", entry.getKey())
                        .add("count", entry.getValue())
                        .add("term", entry.getKey()));
            }
            return builder.build().toString();
        }

        private Map<String, List<TermChatSnapshot>> copyTermSnapshots() {
            Map<String, List<TermChatSnapshot>> copies = new LinkedHashMap<>();
            for (Map.Entry<String, List<TermChatSnapshot>> entry : termSnapshots.entrySet()) {
                copies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copies;
        }
    }
}
