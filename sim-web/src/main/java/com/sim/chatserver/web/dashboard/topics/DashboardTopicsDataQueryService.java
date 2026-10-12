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
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;

final class DashboardTopicsDataQueryService {

    private static final String SQL_SELECT_TOPIC_ROWS_PREFIX = "SELECT widget_chat_id, prompt, created_at FROM ";
    private static final String SQL_SELECT_TOPIC_ROWS_SUFFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    private static final String COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String EMPTY = "";
    private static final String COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String MSG_PARSE_CREATED_AT_FAILED = "Unable to parse created_at timestamp for topic row";
    private static final String MSG_COLLECT_TOPIC_DATA_FAILED_PREFIX = "Unable to collect topic data for widget ";
    private static final String MSG_BUILD_TOPICS_DATA_FAILED = "Unable to build topics data";
    private static final String MSG_LIST_WIDGETS_FAILED = "Unable to list widgets for dashboard topics";
    private static final String MSG_LIST_TERMS_FAILED = "Unable to list terms for dashboard topics";
    private static final String IDENTIFIER_REGEX = com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;

    private final Logger log;

    DashboardTopicsDataQueryService(Logger log) {
        this.log = log;
    }

    TopicsAggregation collect(
            DateWindow window,
            boolean includeOther,
            String otherLabel
    ) {
        List<WidgetEntry> widgets = listWidgets();
        List<TopicPattern> realTopics = listTopicPatterns(otherLabel);

        TopicsAggregation aggregation = new TopicsAggregation();
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                String widgetName = (w.getDisplayName() == null || w.getDisplayName().isBlank())
                        ? widgetId : w.getDisplayName();

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
                if (!com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil.tableExists(conn, tableName, log)) {
                    continue;
                }

                Map<String, Integer> widgetMap = aggregation.byWidgetCounts.computeIfAbsent(widgetName, k -> new java.util.LinkedHashMap<>());
                Map<String, Set<String>> widgetTopicChatIds = aggregation.byWidgetChatIds.computeIfAbsent(widgetName, k -> new java.util.LinkedHashMap<>());

                String sql = SQL_SELECT_TOPIC_ROWS_PREFIX + quoteIdentifier(tableName)
                    + SQL_SELECT_TOPIC_ROWS_SUFFIX;

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(window.startInclusive.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(window.endExclusive.atStartOfDay()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString(COL_WIDGET_CHAT_ID);
                            if (chatId == null || chatId.isBlank()) {
                                continue;
                            }

                            String prompt = rs.getString(COL_PROMPT);
                            if (prompt == null) {
                                prompt = EMPTY;
                            }

                            try {
                                SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                            } catch (SQLException ignored) {
                                log.log(Level.FINE, MSG_PARSE_CREATED_AT_FAILED, ignored);
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
                    log.log(Level.WARNING, MSG_COLLECT_TOPIC_DATA_FAILED_PREFIX + widgetId, ex);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_BUILD_TOPICS_DATA_FAILED, ex);
        }

        return aggregation;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_LIST_WIDGETS_FAILED, ex);
            return List.of();
        }
    }

    private List<TopicPattern> listTopicPatterns(String otherLabel) {
        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_LIST_TERMS_FAILED, ex);
            allTerms = List.of();
        }

        List<TopicPattern> realTopics = new java.util.ArrayList<>();
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
                realTopics.add(new TopicPattern(name.trim(), p));
            }
        }
        return realTopics;
    }

    private void recordMatchedTopics(
            TopicsAggregation aggregation,
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
        Object current = counts.get(topic);
        int next = 1;
        if (current != null) {
            next = Integer.parseInt(current.toString()) + 1;
        }
        counts.put(topic, Integer.valueOf(next));
    }

    private Set<String> matchTopics(String prompt, List<TopicPattern> topics) {
        String sanitized = TextSanitizer.sanitizeForMatching(prompt == null ? EMPTY : prompt);
        Set<String> matched = new LinkedHashSet<>();
        for (TopicPattern tp : topics) {
            if (tp.pattern.matcher(sanitized).find()) {
                matched.add(tp.name);
            }
        }
        return matched;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank() || !identifier.matches(IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }
}
