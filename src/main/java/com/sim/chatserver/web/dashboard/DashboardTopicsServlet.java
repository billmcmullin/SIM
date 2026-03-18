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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsServlet", urlPatterns = {"/dashboard/topics"})
public class DashboardTopicsServlet extends HttpServlet {

    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_topics.html";
    private static final String EXCLUDED_TOPIC = "Other Parasoft Match";

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

        String user = String.valueOf(session.getAttribute("user"));

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            widgets = List.of();
        }

        List<TermDefinition> terms;
        try {
            terms = termsStore.listAll();
        } catch (Exception e) {
            terms = List.of();
        }

        // Prepare active terms (exclude system flags + "Other Parasoft Match")
        List<TopicPattern> activeTopics = buildActiveTopicPatterns(terms);

        Map<String, Integer> globalCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                String widgetName = (w.getDisplayName() == null || w.getDisplayName().isBlank())
                        ? widgetId : w.getDisplayName();

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                Map<String, Integer> widgetMap = byWidgetCounts.computeIfAbsent(widgetName, k -> new LinkedHashMap<>());

                String sql = "SELECT prompt FROM " + quoteIdentifier(tableName);
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        String prompt = rs.getString("prompt");
                        if (prompt == null || prompt.isBlank()) {
                            continue;
                        }

                        Set<String> matchedTopics = matchTopics(prompt, activeTopics);
                        for (String topic : matchedTopics) {
                            globalCounts.merge(topic, 1, Integer::sum);
                            widgetMap.merge(topic, 1, Integer::sum);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to build popular topics report", e);
        }

        String globalRows = renderTopicRows(globalCounts, 20);
        String perWidgetTables = renderPerWidgetTables(byWidgetCounts);

        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
                .replace("${contextPath}", escapeHtml(req.getContextPath()))
                .replace("${user}", escapeHtml(user))
                .replace("${globalTopicRows}", globalRows)
                .replace("${perWidgetTopicTables}", perWidgetTables);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private List<TopicPattern> buildActiveTopicPatterns(List<TermDefinition> terms) {
        List<TopicPattern> list = new ArrayList<>();
        if (terms == null) {
            return list;
        }

        for (TermDefinition t : terms) {
            if (t == null) {
                continue;
            }
            if (t.isSystemFlag()) {
                continue;
            }
            String name = t.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            if (EXCLUDED_TOPIC.equalsIgnoreCase(name.trim())) {
                continue;
            }

            Pattern p = TermMatcher.buildStrictPattern(t);
            if (p != null) {
                list.add(new TopicPattern(name.trim(), p));
            }
        }
        return list;
    }

    private Set<String> matchTopics(String prompt, List<TopicPattern> topics) {
        String text = prompt == null ? "" : prompt;
        Set<String> matched = new LinkedHashSet<>();
        for (TopicPattern tp : topics) {
            try {
                if (tp.pattern.matcher(text).find()) {
                    matched.add(tp.name);
                }
            } catch (Exception ignored) {
            }
        }
        return matched;
    }

    private String renderTopicRows(Map<String, Integer> counts, int limit) {
        List<Map.Entry<String, Integer>> sorted = counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue())
                        .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
                .limit(limit)
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"empty-row\">No matching topics found.</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> e : sorted) {
            sb.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td>").append(escapeHtml(e.getKey())).append("</td>")
                    .append("<td>").append(e.getValue()).append("</td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    private String renderPerWidgetTables(Map<String, Map<String, Integer>> byWidgetCounts) {
        if (byWidgetCounts.isEmpty()) {
            return "<section class=\"section\"><p class=\"empty-row\">No widget topic data found.</p></section>";
        }

        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> e : byWidgetCounts.entrySet()) {
            String widgetName = e.getKey();
            String rows = renderTopicRows(e.getValue(), 10);

            out.append("<section class=\"section\">")
                    .append("<h3>").append(escapeHtml(widgetName)).append("</h3>")
                    .append("<div class=\"table-scroll\">")
                    .append("<table class=\"widget-table\">")
                    .append("<thead><tr><th>Rank</th><th>Topic</th><th>Mentions</th></tr></thead>")
                    .append("<tbody>").append(rows).append("</tbody>")
                    .append("</table>")
                    .append("</div>")
                    .append("</section>");
        }
        return out.toString();
    }

    private String loadTemplate(HttpServletRequest req, String path) throws IOException {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
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

    private static final class TopicPattern {

        private final String name;
        private final Pattern pattern;

        private TopicPattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }
}
