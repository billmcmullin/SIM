package com.sim.chatserver.web.dashboard.inactiveusers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SqlTimeUtil;

final class InactiveUsersListQueryService {
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    InactiveUsersListQueryService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    List<InactiveUsersListPageServlet.Row> loadRows(
            String scope,
            String widgetIdFilter,
            Map<String, String> widgetNameById,
            Instant cutoff,
            int promptScanLimit,
            Function<List<String>, InactiveUsersListPageServlet.FrustrationResult> frustrationAnalyzer) {
        List<InactiveUsersListPageServlet.Row> allRows = new ArrayList<>();

        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            if ("widget".equalsIgnoreCase(scope)) {
                if (!widgetIdFilter.isBlank() && widgetNameById.containsKey(widgetIdFilter)) {
                    String table = sanitizeWidgetTableName(widgetIdFilter);
                    if (tableExists(conn, table)) {
                        List<InactiveUsersListPageServlet.Row> rows = loadWidgetRows(
                                conn,
                                widgetIdFilter,
                                widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter),
                                cutoff,
                                table,
                                promptScanLimit,
                                frustrationAnalyzer
                        );
                        allRows.addAll(rows);
                    }
                }
                return allRows;
            }

            Map<String, InactiveUsersListPageServlet.Row> aggregateRows = new LinkedHashMap<>();
            for (String widgetId : widgetNameById.keySet()) {
                String table = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, table)) {
                    continue;
                }

                String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                        + quoteIdentifier(table)
                        + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, "Unable to prepare aggregate query for table " + table, ex);
                    continue;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, "Unable to execute aggregate query for table " + table, ex);
                        continue;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = rs.getString("session_id");
                            Timestamp last = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                            if (sid == null || sid.isBlank() || last == null) {
                                continue;
                            }

                            String normalizedSessionId = sid.trim();
                            InactiveUsersListPageServlet.Row row = getOrCreateAggregateRow(aggregateRows, normalizedSessionId);
                            row.chatCount += rs.getLong("total");
                            if (row.lastEntry == null || last.after(row.lastEntry)) {
                                row.lastEntry = last;
                            }

                            InactiveUsersListPageServlet.FrustrationResult frustrationResult = detectFrustrationForSession(
                                    conn,
                                    table,
                                    normalizedSessionId,
                                    promptScanLimit,
                                    frustrationAnalyzer
                            );
                            if (frustrationResult.score > row.frustrationScore) {
                                row.frustrationScore = frustrationResult.score;
                                row.frustrationDetected = frustrationResult.detected;
                                row.frustrationReason = frustrationResult.reason;
                            }
                        }
                    }
                }
            }

            for (InactiveUsersListPageServlet.Row row : aggregateRows.values()) {
                if (row.lastEntry != null && row.lastEntry.toInstant().isBefore(cutoff)) {
                    allRows.add(row);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Unable to compute inactive users list", ex);
        }

        return allRows;
    }

    private List<InactiveUsersListPageServlet.Row> loadWidgetRows(
            Connection conn,
            String widgetId,
            String widgetLabel,
            Instant cutoff,
            String table,
            int promptScanLimit,
            Function<List<String>, InactiveUsersListPageServlet.FrustrationResult> frustrationAnalyzer) {
        List<InactiveUsersListPageServlet.Row> rows = new ArrayList<>();
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = conn.prepareStatement(sql);
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to prepare widget-row query for table " + table, ex);
            return rows;
        }

        try (PreparedStatement ps = preparedStatement) {
            ResultSet queryResult;
            try {
                queryResult = ps.executeQuery();
            } catch (SQLException ex) {
                log.log(Level.FINE, "Unable to execute widget-row query for table " + table, ex);
                return rows;
            }

            try (ResultSet rs = queryResult) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    Timestamp last = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                    if (sid == null || sid.isBlank() || last == null || !last.toInstant().isBefore(cutoff)) {
                        continue;
                    }

                    String normalizedSessionId = sid.trim();
                    InactiveUsersListPageServlet.Row row = createWidgetRow(normalizedSessionId, widgetId, widgetLabel, last, rs.getLong("total"));
                    InactiveUsersListPageServlet.FrustrationResult frustrationResult = detectFrustrationForSession(
                            conn,
                            table,
                            normalizedSessionId,
                            promptScanLimit,
                            frustrationAnalyzer
                    );
                    row.frustrationDetected = frustrationResult.detected;
                    row.frustrationScore = frustrationResult.score;
                    row.frustrationReason = frustrationResult.reason;
                    rows.add(row);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to load widget rows for " + table, ex);
        }
        return rows;
    }

    private InactiveUsersListPageServlet.FrustrationResult detectFrustrationForSession(
            Connection conn,
            String table,
            String sessionId,
            int promptScanLimit,
            Function<List<String>, InactiveUsersListPageServlet.FrustrationResult> frustrationAnalyzer) {
        try {
            List<String> prompts = loadRecentPromptsForSession(conn, table, sessionId, promptScanLimit);
            return frustrationAnalyzer.apply(prompts);
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Frustration detection skipped for session " + sessionId + " in " + table, ex);
            return emptyFrustrationResult();
        }
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) {
        List<String> prompts = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank() || limit < 1) {
            return prompts;
        }

        String[] columns = {"prompt", "prompt_text", "user_prompt"};
        for (String column : columns) {
            String sql = "SELECT " + quoteIdentifier(column) + " AS p FROM " + quoteIdentifier(table)
                    + " WHERE session_id = ? AND " + quoteIdentifier(column) + " IS NOT NULL AND " + quoteIdentifier(column) + " <> ''"
                    + " ORDER BY created_at DESC";

            PreparedStatement preparedStatement;
            try {
                preparedStatement = conn.prepareStatement(sql);
            } catch (SQLException ex) {
                log.log(Level.FINEST, "Unable to prepare prompt query for column " + column, ex);
                continue;
            }

            try (PreparedStatement ps = preparedStatement) {
                ps.setString(1, sessionId);
                ps.setMaxRows(limit);

                ResultSet queryResult;
                try {
                    queryResult = ps.executeQuery();
                } catch (SQLException ex) {
                    log.log(Level.FINEST, "Unable to execute prompt query for column " + column, ex);
                    continue;
                }

                try (ResultSet rs = queryResult) {
                    while (rs.next()) {
                        String prompt = rs.getString("p");
                        if (prompt != null && !prompt.isBlank()) {
                            prompts.add(prompt);
                        }
                    }
                    if (!prompts.isEmpty()) {
                        return prompts;
                    }
                }
            } catch (SQLException ex) {
                log.log(Level.FINEST, "Prompt column unavailable for frustration scan: " + column, ex);
            }
        }

        return prompts;
    }

    private InactiveUsersListPageServlet.Row getOrCreateAggregateRow(
            Map<String, InactiveUsersListPageServlet.Row> aggregateRows,
            String normalizedSessionId) {
        InactiveUsersListPageServlet.Row existing = aggregateRows.get(normalizedSessionId);
        if (existing != null) {
            return existing;
        }

        InactiveUsersListPageServlet.Row created = createAggregateRow(normalizedSessionId);
        aggregateRows.put(normalizedSessionId, created);
        return created;
    }

    private InactiveUsersListPageServlet.Row createAggregateRow(String normalizedSessionId) {
        InactiveUsersListPageServlet.Row row = new InactiveUsersListPageServlet.Row();
        row.sessionId = normalizedSessionId;
        row.widgetId = "ALL";
        row.widgetLabel = "All Widgets";
        row.frustrationDetected = false;
        row.frustrationScore = 0.0;
        row.frustrationReason = "";
        return row;
    }

    private InactiveUsersListPageServlet.Row createWidgetRow(
            String normalizedSessionId,
            String widgetId,
            String widgetLabel,
            Timestamp lastEntry,
            long chatCount) {
        InactiveUsersListPageServlet.Row row = new InactiveUsersListPageServlet.Row();
        row.sessionId = normalizedSessionId;
        row.widgetId = widgetId;
        row.widgetLabel = widgetLabel;
        row.chatCount = chatCount;
        row.lastEntry = lastEntry;
        row.frustrationDetected = false;
        row.frustrationScore = 0.0;
        row.frustrationReason = "";
        return row;
    }

    private InactiveUsersListPageServlet.FrustrationResult emptyFrustrationResult() {
        InactiveUsersListPageServlet.FrustrationResult result = new InactiveUsersListPageServlet.FrustrationResult();
        result.detected = false;
        result.score = 0.0;
        result.reason = "";
        return result;
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
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, ex);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        String normalized = widgetId == null ? "widget" : widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
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
}
