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
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;

@ApplicationScoped
public class DashboardTopicsQueryService {
    private static final Logger log = Logger.getLogger(DashboardTopicsQueryService.class.getName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    public TopicCountResult collectTopicCounts(
            List<WidgetEntry> widgets,
            LocalDate startInclusive,
            LocalDate endExclusive,
            Function<String, Set<String>> topicMatcher
    ) throws SQLException {
        Map<String, Integer> globalCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String widgetName = (widget.getDisplayName() == null || widget.getDisplayName().isBlank())
                        ? widgetId : widget.getDisplayName();

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                Map<String, Integer> widgetMap = byWidgetCounts.computeIfAbsent(widgetName, key -> new LinkedHashMap<>());

                String sql = "SELECT prompt, created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(startInclusive.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(endExclusive.atStartOfDay()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ignoredTs = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (ignoredTs == null) {
                                // Prompt text is still usable even when timestamp normalization fails.
                            }

                            String prompt = rs.getString("prompt");
                            if (prompt == null || prompt.isBlank()) {
                                continue;
                            }

                            Set<String> matchedTopics = topicMatcher.apply(prompt);
                            if (matchedTopics == null || matchedTopics.isEmpty()) {
                                continue;
                            }
                            for (String topic : matchedTopics) {
                                if (topic == null || topic.isBlank()) {
                                    continue;
                                }
                                incrementCount(globalCounts, topic);
                                incrementCount(widgetMap, topic);
                            }
                        }
                    }
                }
            }
        }

        return new TopicCountResult(globalCounts, byWidgetCounts);
    }

    private void incrementCount(Map<String, Integer> counter, String key) {
        Integer current = counter.get(key);
        int next = current == null ? 1 : current.intValue() + 1;
        counter.put(key, Integer.valueOf(next));
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
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    public static final class TopicCountResult {
        private final Map<String, Integer> globalCounts;
        private final Map<String, Map<String, Integer>> byWidgetCounts;

        public TopicCountResult(Map<String, Integer> globalCounts, Map<String, Map<String, Integer>> byWidgetCounts) {
            this.globalCounts = globalCounts;
            this.byWidgetCounts = byWidgetCounts;
        }

        public Map<String, Integer> globalCounts() {
            return globalCounts;
        }

        public Map<String, Map<String, Integer>> byWidgetCounts() {
            return byWidgetCounts;
        }
    }
}
