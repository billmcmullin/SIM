package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsServlet", urlPatterns = {"/dashboard/topics"})
public class DashboardTopicsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsServlet.class.getName());

    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_topics.html";
    private static final String EXCLUDED_TOPIC = "Other Parasoft Match";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String contextPath = ServletPathUtil.safeContextPathNoTrailingSlash(req.getContextPath());
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                req.getRequestDispatcher("/login").forward(req, resp);
                return;
            }

            String user = String.valueOf(session.getAttribute("user"));
            boolean includeOther = parseBooleanFlag(ServletRequestParamUtil.firstParam(req, "includeOther", 256, false, false));

            DateWindow window = resolveDateWindow(
                    ServletRequestParamUtil.firstParam(req, "day", 256, false, false),
                    ServletRequestParamUtil.firstParam(req, "start", 256, false, false),
                    ServletRequestParamUtil.firstParam(req, "end", 256, false, false)
            );

            List<WidgetEntry> widgets;
            try {
                widgets = WidgetStore.list(null);
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to list widgets for topics dashboard", e);
                widgets = List.of();
            }

            List<TermDefinition> terms;
            try {
                terms = termsStore().listAll();
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to load term definitions for topics dashboard", e);
                terms = List.of();
            }

            List<TopicPattern> activeTopics = buildActiveTopicPatterns(terms, includeOther);

            DashboardTopicsQueryService.TopicCountResult counts;
            try {
                counts = queryService().collectTopicCounts(
                        widgets,
                        window.startInclusive,
                        window.endExclusive,
                        prompt -> matchTopics(prompt, activeTopics)
                );
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to build popular topics report", e);
                throw new ServletException("Unable to build popular topics report", e);
            }

            String globalRows = buildGlobalTopicRows(counts.globalCounts());
            String perWidgetTables = buildPerWidgetTables(counts.byWidgetCounts());

            String template = loadTemplate(req, TEMPLATE_PATH);
            String rendered = template
                    .replace("${contextPath}", escapeHtml(contextPath))
                    .replace("${user}", escapeHtml(user))
                    .replace("${globalTopicRows}", globalRows)
                    .replace("${perWidgetTopicTables}", perWidgetTables);

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("text/html; charset=UTF-8");
            resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));

        } catch (IOException | ServletException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error.", ioe);
        }
    }

    private DateWindow resolveDateWindow(String dayParam, String startParam, String endParam) {
        Optional<LocalDate> dayOpt = parseLocalDate(dayParam);
        if (dayOpt.isPresent()) {
            LocalDate d = dayOpt.get();
            return new DateWindow(d, d.plusDays(1));
        }

        Optional<LocalDate> startOpt = parseLocalDate(startParam);
        Optional<LocalDate> endOpt = parseLocalDate(endParam);

        if (startOpt.isPresent() || endOpt.isPresent()) {
            LocalDate s = startOpt.orElseGet(endOpt::get);
            LocalDate e = endOpt.orElseGet(startOpt::get);
            if (e.isBefore(s)) {
                LocalDate tmp = s;
                s = e;
                e = tmp;
            }
            return new DateWindow(s, e.plusDays(1));
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return new DateWindow(today, today.plusDays(1));
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), DATE_FMT));
        } catch (DateTimeParseException e) {
            log.log(Level.FINE, "Invalid dashboard topics date parameter", e);
            return Optional.empty();
        }
    }

    private boolean parseBooleanFlag(String raw) {
        if (raw == null) {
            return false;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private List<TopicPattern> buildActiveTopicPatterns(List<TermDefinition> terms, boolean includeOther) {
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
            if (!includeOther && EXCLUDED_TOPIC.equalsIgnoreCase(name.trim())) {
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
            if (tp.pattern.matcher(text).find()) {
                matched.add(tp.name);
            }
        }
        return matched;
    }

    private String loadTemplate(HttpServletRequest req, String path) {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template not found: {0}", path);
                return "";
            }
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load topics template: " + path, e);
            return "";
        }
    }

    private String buildGlobalTopicRows(Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return "<tr><td colspan=\"2\">No topic matches found for the selected range.</td></tr>";
        }

        StringBuilder html = new StringBuilder();
        counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(valueOrZero(b.getValue()), valueOrZero(a.getValue())))
                .forEach(entry -> {
                    String topic = escapeHtml(entry.getKey());
                    int count = valueOrZero(entry.getValue());
                    html.append("<tr><td>")
                            .append(topic)
                            .append("</td><td>")
                            .append(count)
                            .append("</td></tr>");
                });
        return html.toString();
    }

    private String buildPerWidgetTables(Map<String, Map<String, Integer>> byWidgetCounts) {
        if (byWidgetCounts == null || byWidgetCounts.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        byWidgetCounts.forEach((widgetName, topicCounts) -> {
            html.append("<section class=\"widget-topic-table\">")
                    .append("<h3>")
                    .append(escapeHtml(widgetName))
                    .append("</h3>")
                    .append("<table><thead><tr><th>Topic</th><th>Count</th></tr></thead><tbody>")
                    .append(buildGlobalTopicRows(topicCounts))
                    .append("</tbody></table></section>");
        });
        return html.toString();
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private DashboardTopicsQueryService queryService() {
        return CDI.current().select(DashboardTopicsQueryService.class).get();
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private static final class TopicPattern {

        final String name;
        final Pattern pattern;

        private TopicPattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }

    private static final class DateWindow {

        final LocalDate startInclusive;
        final LocalDate endExclusive;

        private DateWindow(LocalDate startInclusive, LocalDate endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }
    }
}
