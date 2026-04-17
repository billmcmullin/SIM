package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsDataServlet", urlPatterns = {"/dashboard/topics/data"})
public class DashboardTopicsDataServlet extends HttpServlet {

    private static final String OTHER_LABEL = "Other Parasoft Match";

    @Inject
    AppDataSourceHolder dsHolder;

    @Inject
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().print("{\"status\":\"unauthorized\"}");
            return;
        }

        String q = req.getParameter("q");
        String limitRaw = req.getParameter("limit");
        int limit = parseLimit(limitRaw, 5);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            widgets = List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore.listAll();
        } catch (Exception e) {
            allTerms = List.of();
        }

        List<TopicPattern> visibleTopics = new ArrayList<>();
        List<TopicPattern> allTopicPatterns = new ArrayList<>();

        for (TermDefinition t : allTerms) {
            if (t == null || t.isSystemFlag()) {
                continue;
            }
            String name = t.getName();
            if (name == null || name.isBlank()) {
                continue;
            }

            Pattern p = TermMatcher.buildStrictPattern(t);
            if (p == null) {
                continue;
            }

            TopicPattern tp = new TopicPattern(name.trim(), p);
            allTopicPatterns.add(tp);

            if (!OTHER_LABEL.equalsIgnoreCase(name.trim())) {
                visibleTopics.add(tp);
            }
        }

        Map<String, Integer> globalCounts = new LinkedHashMap<>();
        Map<String, Set<String>> globalChatIdsByTopic = new LinkedHashMap<>();

        Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();
        Map<String, Map<String, Set<String>>> byWidgetChatIds = new LinkedHashMap<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                String widgetName = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? widgetId : w.getDisplayName();

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                Map<String, Integer> widgetMap = byWidgetCounts.computeIfAbsent(widgetName, k -> new LinkedHashMap<>());
                Map<String, Set<String>> widgetTopicChatIds = byWidgetChatIds.computeIfAbsent(widgetName, k -> new LinkedHashMap<>());

                String sql = "SELECT widget_chat_id, prompt, created_at FROM " + quoteIdentifier(tableName);
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        String prompt = rs.getString("prompt");

                        if (chatId == null || chatId.isBlank()) {
                            continue;
                        }
                        if (prompt == null) {
                            prompt = "";
                        }

                        // Defensive read for mixed timestamp representations after import.
                        try {
                            Timestamp ignored = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (ignored == null) {
                                // keep going; timestamp is not needed for topic classification itself
                            }
                        } catch (SQLException ignored) {
                            // Continue processing prompt-based topic matching.
                        }

                        String chosenTopic = resolveTopic(prompt, visibleTopics, allTopicPatterns);

                        if (chosenTopic == null || chosenTopic.isBlank() || OTHER_LABEL.equalsIgnoreCase(chosenTopic)) {
                            continue;
                        }

                        globalCounts.merge(chosenTopic, 1, Integer::sum);
                        globalChatIdsByTopic.computeIfAbsent(chosenTopic, k -> new LinkedHashSet<>()).add(chatId);

                        widgetMap.merge(chosenTopic, 1, Integer::sum);
                        widgetTopicChatIds.computeIfAbsent(chosenTopic, k -> new LinkedHashSet<>()).add(chatId);
                    }
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to build topics data")
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().print(err.toString());
            return;
        }

        Map<String, Integer> filteredGlobal = filterTopicMap(globalCounts, q);
        Map<String, Map<String, Integer>> filteredByWidget = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : byWidgetCounts.entrySet()) {
            Map<String, Integer> filtered = filterTopicMap(e.getValue(), q);
            if (!filtered.isEmpty()) {
                filteredByWidget.put(e.getKey(), filtered);
            }
        }

        List<Map.Entry<String, Integer>> globalSorted = sortTopicMap(filteredGlobal, limit);

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
        for (Map.Entry<String, Map<String, Integer>> e : filteredByWidget.entrySet()) {
            String widgetName = e.getKey();
            List<Map.Entry<String, Integer>> sorted = sortTopicMap(e.getValue(), limit);
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
                .add("query", q == null ? "" : q)
                .add("limit", "all".equalsIgnoreCase(limitRaw == null ? "" : limitRaw.trim()) ? "all" : String.valueOf(limit))
                .add("globalTopics", globalArray)
                .add("widgets", widgetsArray)
                .build();

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().print(payload.toString());
    }

    private String resolveTopic(String prompt, List<TopicPattern> visibleTopics, List<TopicPattern> allTopicPatterns) {
        String sanitized = TextSanitizer.sanitizeForMatching(prompt == null ? "" : prompt);

        String bestVisible = firstBestMatchName(sanitized, visibleTopics);
        if (bestVisible != null) {
            return bestVisible;
        }

        String bestAny = firstBestMatchName(sanitized, allTopicPatterns);
        if (bestAny != null && !OTHER_LABEL.equalsIgnoreCase(bestAny)) {
            return bestAny;
        }

        return null;
    }

    private String firstBestMatchName(String text, List<TopicPattern> patterns) {
        String winner = null;
        int bestStart = Integer.MAX_VALUE;
        for (TopicPattern tp : patterns) {
            try {
                Matcher m = tp.pattern.matcher(text);
                if (m.find()) {
                    int start = m.start();
                    if (start < bestStart) {
                        bestStart = start;
                        winner = tp.name;
                        if (bestStart == 0) {
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return winner;
    }

    private Map<String, Integer> filterTopicMap(Map<String, Integer> source, String q) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        if (q == null || q.isBlank()) {
            return source;
        }

        String needle = q.trim().toLowerCase(Locale.ROOT);
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : source.entrySet()) {
            if (e.getKey() != null && e.getKey().toLowerCase(Locale.ROOT).contains(needle)) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    private List<Map.Entry<String, Integer>> sortTopicMap(Map<String, Integer> map, int limit) {
        return map.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue())
                        .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private int parseLimit(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if ("all".equals(v)) {
            return Integer.MAX_VALUE;
        }
        try {
            int n = Integer.parseInt(v);
            if (n <= 0) {
                return fallback;
            }
            return Math.min(n, 10000);
        } catch (Exception e) {
            return fallback;
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

    private static final class TopicPattern {

        final String name;
        final Pattern pattern;

        TopicPattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }
}
