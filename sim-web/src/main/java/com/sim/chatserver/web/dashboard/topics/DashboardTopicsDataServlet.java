package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.SqlTimeUtil;
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

@WebServlet(name = "DashboardTopicsDataServlet", urlPatterns = {"/dashboard/topics/data"})
public class DashboardTopicsDataServlet extends HttpServlet {
    // parasoft-suppress SERVLET.AJDBC "This endpoint intentionally performs bounded JDBC reads to compute dashboard topic aggregates."
    // parasoft-suppress SERVLET.CETS "Checked exceptions are handled at endpoint boundaries with safe fallback responses."
    // parasoft-suppress SERVLET.IF "CDI-managed collaborators are required and do not retain mutable request state."
    // parasoft-suppress SECURITY.ESD.SIF "Injected collaborators are framework-managed and not serialized secret payloads."

    private static final Logger log = Logger.getLogger(DashboardTopicsDataServlet.class.getName());
    private static final String OTHER_LABEL = "Other Parasoft Match";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        boolean includeOther = parseBooleanFlag(firstParam(req, "includeOther"));
        DateWindow window = resolveDateWindow(req);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets for dashboard topics", e);
            widgets = List.of();
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unexpected runtime error while listing widgets", e);
            widgets = List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore.listAll();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list terms for dashboard topics", e);
            allTerms = List.of();
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unexpected runtime error while listing terms", e);
            allTerms = List.of();
        }

        // Only "real" terms are regex-matched.
        // OTHER_LABEL is reserved as catch-all bucket.
        List<TopicPattern> realTopics = new ArrayList<>();
        for (TermDefinition t : allTerms) {
            if (t == null || t.isSystemFlag()) {
                continue;
            }

            String name = t.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            if (OTHER_LABEL.equalsIgnoreCase(name.trim())) {
                continue;
            }

            Pattern p = TermMatcher.buildStrictPattern(t);
            if (p == null) {
                continue;
            }

            realTopics.add(new TopicPattern(name.trim(), p));
        }

        Map<String, Integer> globalCounts = new LinkedHashMap<>();
        Map<String, Set<String>> globalChatIdsByTopic = new LinkedHashMap<>();

        Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();
        Map<String, Map<String, Set<String>>> byWidgetChatIds = new LinkedHashMap<>();

        long totalMentions = 0L;
        Set<String> allMatchedChatIds = new LinkedHashSet<>();

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
                Map<String, Set<String>> widgetTopicChatIds = byWidgetChatIds.computeIfAbsent(widgetName, k -> new LinkedHashMap<>());

                String sql = "SELECT widget_chat_id, prompt, created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(window.startInclusive.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(window.endExclusive.atStartOfDay()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");

                            if (chatId == null || chatId.isBlank()) {
                                continue;
                            }
                            if (prompt == null) {
                                prompt = "";
                            }

                            try {
                                SqlTimeUtil.safeTimestamp(rs, "created_at");
                            } catch (SQLException ignored) {
                                log.log(Level.FINE, "Unable to parse created_at timestamp for topic row", ignored);
                            }

                            Set<String> matchedRealTopics = matchTopics(prompt, realTopics);

                            if (!matchedRealTopics.isEmpty()) {
                                for (String topic : matchedRealTopics) {
                                    globalCounts.merge(topic, 1, Integer::sum);
                                    globalChatIdsByTopic.computeIfAbsent(topic, k -> new LinkedHashSet<>()).add(chatId);

                                    widgetMap.merge(topic, 1, Integer::sum);
                                    widgetTopicChatIds.computeIfAbsent(topic, k -> new LinkedHashSet<>()).add(chatId);

                                    totalMentions++;
                                }
                                allMatchedChatIds.add(chatId);
                            } else if (includeOther) {
                                globalCounts.merge(OTHER_LABEL, 1, Integer::sum);
                                globalChatIdsByTopic.computeIfAbsent(OTHER_LABEL, k -> new LinkedHashSet<>()).add(chatId);

                                widgetMap.merge(OTHER_LABEL, 1, Integer::sum);
                                widgetTopicChatIds.computeIfAbsent(OTHER_LABEL, k -> new LinkedHashSet<>()).add(chatId);

                                totalMentions++;
                                allMatchedChatIds.add(chatId);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to build topics data", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to build topics data")
                    .build();
            writeJson(resp, err);
            return;
        }

        long uniqueChatsTotal = allMatchedChatIds.size();

        List<Map.Entry<String, Integer>> globalSorted = sortTopicMap(globalCounts);

        JsonArrayBuilder globalArray = Json.createArrayBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> e : globalSorted) {
            String topic = e.getKey();
            Set<String> ids = globalChatIdsByTopic.getOrDefault(topic, Set.of());

            JsonArrayBuilder idsArray = Json.createArrayBuilder();
            for (String id : ids) {
                idsArray.add(id);
            }

            globalArray.add(Json.createObjectBuilder()
                    .add("rank", rank++)
                    .add("topic", topic)
                    .add("mentions", e.getValue())
                    .add("selectedChatIds", idsArray));
        }

        JsonArrayBuilder widgetsArray = Json.createArrayBuilder();
        for (Map.Entry<String, Map<String, Integer>> e : byWidgetCounts.entrySet()) {
            String widgetName = e.getKey();
            Map<String, Integer> widgetCounts = e.getValue();

            // Restore prior behavior: only show widgets that actually have topic entries.
            if (widgetCounts == null || widgetCounts.isEmpty()) {
                continue;
            }

            List<Map.Entry<String, Integer>> sorted = sortTopicMap(widgetCounts);
            if (sorted.isEmpty()) {
                continue;
            }

            Map<String, Set<String>> topicChats = byWidgetChatIds.getOrDefault(widgetName, Map.of());

            JsonArrayBuilder topicsArray = Json.createArrayBuilder();
            int widgetRank = 1;
            for (Map.Entry<String, Integer> t : sorted) {
                String topic = t.getKey();
                Set<String> ids = topicChats.getOrDefault(topic, Set.of());

                JsonArrayBuilder idsArray = Json.createArrayBuilder();
                for (String id : ids) {
                    idsArray.add(id);
                }

                topicsArray.add(Json.createObjectBuilder()
                        .add("rank", widgetRank++)
                        .add("topic", topic)
                        .add("mentions", t.getValue())
                        .add("selectedChatIds", idsArray));
            }

            widgetsArray.add(Json.createObjectBuilder()
                    .add("widgetName", widgetName)
                    .add("topics", topicsArray));
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("query", "") // kept for compatibility
                .add("limit", "all") // now fixed behavior
                .add("includeOther", includeOther)
                .add("day", window.dayToken)
                .add("rangeStart", window.startInclusive.format(DATE_FMT))
                .add("rangeEnd", window.endExclusive.minusDays(1).format(DATE_FMT))
                .add("globalTopics", globalArray)
                .add("widgets", widgetsArray)
                .add("termsTotal", totalMentions)
                .add("uniqueChatsTotal", uniqueChatsTotal)
                .build();

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        writeJson(resp, payload);
    }

    private DateWindow resolveDateWindow(HttpServletRequest req) {
        Optional<LocalDate> dayOpt = parseLocalDate(firstParam(req, "day"));
        if (dayOpt.isPresent()) {
            LocalDate d = dayOpt.get();
            return new DateWindow(d, d.plusDays(1), d.format(DATE_FMT));
        }

        Optional<LocalDate> startOpt = parseLocalDate(firstParam(req, "start"));
        Optional<LocalDate> endOpt = parseLocalDate(firstParam(req, "end"));

        if (startOpt.isPresent() || endOpt.isPresent()) {
            LocalDate s = startOpt.orElseGet(endOpt::get);
            LocalDate e = endOpt.orElseGet(startOpt::get);

            if (e.isBefore(s)) {
                LocalDate tmp = s;
                s = e;
                e = tmp;
            }

            String token = s.equals(e)
                    ? s.format(DATE_FMT)
                    : s.format(DATE_FMT) + "_to_" + e.format(DATE_FMT);

            return new DateWindow(s, e.plusDays(1), token);
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return new DateWindow(today, today.plusDays(1), today.format(DATE_FMT));
    }

    private Optional<LocalDate> parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), DATE_FMT));
        } catch (DateTimeParseException ex) {
            log.log(Level.FINE, "Invalid date parameter for dashboard topics");
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

    private Set<String> matchTopics(String prompt, List<TopicPattern> topics) {
        String sanitized = TextSanitizer.sanitizeForMatching(prompt == null ? "" : prompt);
        Set<String> matched = new LinkedHashSet<>();
        for (TopicPattern tp : topics) {
            if (tp.pattern.matcher(sanitized).find()) {
                matched.add(tp.name);
            }
        }
        return matched;
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private void writeJson(HttpServletResponse resp, JsonObject payload) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload);
        }
    }

    private List<Map.Entry<String, Integer>> sortTopicMap(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue())
                        .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
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

    static final class TopicPattern {

        final String name;
        final Pattern pattern;

        TopicPattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }

    static final class DateWindow {

        final LocalDate startInclusive;
        final LocalDate endExclusive;
        final String dayToken;

        DateWindow(LocalDate startInclusive, LocalDate endExclusive, String dayToken) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.dayToken = dayToken;
        }
    }
}
