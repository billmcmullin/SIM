package com.sim.chatserver.web.dashboard.topics;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;

@ApplicationScoped
public class DashboardTopicsQueryService {

    private static final Logger log = Logger.getLogger(DashboardTopicsQueryService.class.getCanonicalName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final String DEFAULT_WIDGET_TABLE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
    private static final String TABLE_PREFIX = "w_";
    private static final String NORMALIZE_NON_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.REGEX_NON_ALNUM_UNDERSCORE;
    private static final String NORMALIZE_REPLACEMENT = "_";
    private static final String MSG_UNABLE_INSPECT_TABLE_METADATA = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

        private TopicCountResult collectTopicCounts(
            List<WidgetEntry> widgets,
            LocalDate startInclusive,
            LocalDate endExclusive,
            Function<String, Set<String>> topicMatcher
    ) throws SQLException {
        Map<String, Integer> globalCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                collectWidgetTopicCounts(conn, widget, startInclusive, endExclusive, topicMatcher, globalCounts, byWidgetCounts);
            }
        }

        return new TopicCountResult(globalCounts, byWidgetCounts);
    }

    private void collectWidgetTopicCounts(
            Connection conn,
            WidgetEntry widget,
            LocalDate startInclusive,
            LocalDate endExclusive,
            Function<String, Set<String>> topicMatcher,
            Map<String, Integer> globalCounts,
            Map<String, Map<String, Integer>> byWidgetCounts
    ) throws SQLException {
        if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
            return;
        }

        String widgetId = widget.getWidgetId();
        String widgetName = widget.getDisplayName() == null || widget.getDisplayName().isBlank()
                ? widgetId : widget.getDisplayName();
        String tableName = sanitizeWidgetTableName(widgetId);
        if (!tableExists(conn, tableName)) {
            return;
        }

        Map<String, Integer> widgetMap = byWidgetCounts.computeIfAbsent(widgetName, key -> new LinkedHashMap<>());
        String sql = SqlLiteralConstants.SQL_SELECT_PROMPT_CREATED_AT_FROM + quoteIdentifier(tableName)
            + SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startInclusive.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                collectRowsTopicCounts(rs, topicMatcher, globalCounts, widgetMap);
            }
        }
    }

    private void collectRowsTopicCounts(
            ResultSet rs,
            Function<String, Set<String>> topicMatcher,
            Map<String, Integer> globalCounts,
            Map<String, Integer> widgetMap
    ) throws SQLException {
        while (rs.next()) {
            SqlTimeUtil.safeTimestamp(rs, SqlLiteralConstants.COL_CREATED_AT);
            String prompt = rs.getString(SqlLiteralConstants.COL_PROMPT);
            if (prompt == null || prompt.isBlank()) {
                continue;
            }

            Set<String> matchedTopics = topicMatcher.apply(prompt);
            if (matchedTopics == null || matchedTopics.isEmpty()) {
                continue;
            }
            for (String topic : matchedTopics) {
                if (topic != null && !topic.isBlank()) {
                    incrementCount(globalCounts, topic);
                    incrementCount(widgetMap, topic);
                }
            }
        }
    }

    private void incrementCount(Map<String, Integer> counter, String key) {
        Object current = counter.get(key);
        int next = 1;
        if (current != null) {
            next = Integer.parseInt(current.toString()) + 1;
        }
        counter.put(key, Integer.valueOf(next));
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_INSPECT_TABLE_METADATA + tableName, e);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return DEFAULT_WIDGET_TABLE;
        }
        String normalized = widgetId.trim().replaceAll(NORMALIZE_NON_IDENTIFIER, NORMALIZE_REPLACEMENT);
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = TABLE_PREFIX + normalized;
        }
        if (normalized.length() > 60) {
            normalized = new String(normalized.toCharArray(), 0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(SqlLiteralConstants.SQL_INVALID_IDENTIFIER);
        }
        return '"' + identifier.replace(SqlLiteralConstants.DOUBLE_QUOTE, SqlLiteralConstants.DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }
}

final class TopicCountResult {
    private final Map<String, Integer> globalCounts;
    private final Map<String, Map<String, Integer>> byWidgetCounts;

    TopicCountResult(Map<String, Integer> globalCounts, Map<String, Map<String, Integer>> byWidgetCounts) {
        this.globalCounts = globalCounts;
        this.byWidgetCounts = byWidgetCounts;
    }

    Map<String, Integer> globalCounts() {
        return globalCounts;
    }

    Map<String, Map<String, Integer>> byWidgetCounts() {
        return byWidgetCounts;
    }
}

