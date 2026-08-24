package com.sim.chatserver.web.dashboard.topics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;

final class DashboardTopicsDataQueryService {

    private final Logger log;

    DashboardTopicsDataQueryService(Logger log) {
        this.log = log;
    }

    DashboardTopicsDataServlet.TopicsAggregation collect(
            DashboardTopicsDataServlet.DateWindow window,
            boolean includeOther,
            String otherLabel
    ) {
        List<WidgetEntry> widgets = listWidgets();
        List<DashboardTopicsDataServlet.TopicPattern> realTopics = listTopicPatterns(otherLabel);

        DashboardTopicsDataServlet.TopicsAggregation aggregation = new DashboardTopicsDataServlet.TopicsAggregation();
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                String widgetName = (w.getDisplayName() == null || w.getDisplayName().isBlank())
                        ? widgetId : w.getDisplayName();

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil.tableExists(conn, tableName, log)) {
                    continue;
                }

                Map<String, Integer> widgetMap = aggregation.byWidgetCounts.computeIfAbsent(widgetName, k -> new java.util.LinkedHashMap<>());
                Map<String, Set<String>> widgetTopicChatIds = aggregation.byWidgetChatIds.computeIfAbsent(widgetName, k -> new java.util.LinkedHashMap<>());

                String sql = "SELECT widget_chat_id, prompt, created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(window.startInclusive.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(window.endExclusive.atStartOfDay()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            if (chatId == null || chatId.isBlank()) {
                                continue;
                            }

                            String prompt = rs.getString("prompt");
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
                                recordMatchedTopics(aggregation, widgetMap, widgetTopicChatIds, chatId, matchedRealTopics);
                                continue;
                            }

                            if (includeOther) {
                                recordMatchedTopics(aggregation, widgetMap, widgetTopicChatIds, chatId, Set.of(otherLabel));
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.log(Level.WARNING, "Unable to collect topic data for widget " + widgetId, ex);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to build topics data", ex);
        }

        return aggregation;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to list widgets for dashboard topics", ex);
            return List.of();
        }
    }

    private List<DashboardTopicsDataServlet.TopicPattern> listTopicPatterns(String otherLabel) {
        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to list terms for dashboard topics", ex);
            allTerms = List.of();
        }

        List<DashboardTopicsDataServlet.TopicPattern> realTopics = new java.util.ArrayList<>();
        for (TermDefinition t : allTerms) {
            if (t == null || t.isSystemFlag()) {
                continue;
            }

            String name = t.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            if (otherLabel.equalsIgnoreCase(name.trim())) {
                continue;
            }

            java.util.regex.Pattern p = TermMatcher.buildStrictPattern(t);
            if (p != null) {
                realTopics.add(new DashboardTopicsDataServlet.TopicPattern(name.trim(), p));
            }
        }
        return realTopics;
    }

    private void recordMatchedTopics(
            DashboardTopicsDataServlet.TopicsAggregation aggregation,
            Map<String, Integer> widgetMap,
            Map<String, Set<String>> widgetTopicChatIds,
            String chatId,
            Set<String> topics
    ) {
        for (String topic : topics) {
            incrementTopicCount(aggregation.globalCounts, topic);
            aggregation.globalChatIdsByTopic.computeIfAbsent(topic, k -> new java.util.LinkedHashSet<>()).add(chatId);

            incrementTopicCount(widgetMap, topic);
            widgetTopicChatIds.computeIfAbsent(topic, k -> new java.util.LinkedHashSet<>()).add(chatId);

            aggregation.totalMentions++;
        }
        aggregation.allMatchedChatIds.add(chatId);
    }

    private void incrementTopicCount(Map<String, Integer> counts, String topic) {
        if (counts == null || topic == null || topic.isBlank()) {
            return;
        }
        Integer current = counts.get(topic);
        int next = 1;
        if (current != null) {
            next = current.intValue() + 1;
        }
        counts.put(topic, Integer.valueOf(next));
    }

    private Set<String> matchTopics(String prompt, List<DashboardTopicsDataServlet.TopicPattern> topics) {
        String sanitized = TextSanitizer.sanitizeForMatching(prompt == null ? "" : prompt);
        Set<String> matched = new LinkedHashSet<>();
        for (DashboardTopicsDataServlet.TopicPattern tp : topics) {
            if (tp.pattern.matcher(sanitized).find()) {
                matched.add(tp.name);
            }
        }
        return matched;
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
        if (identifier == null || identifier.isBlank() || !identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }
}