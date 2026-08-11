package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DateTimeException;
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
                            String sid = readSafeDbText(rs, "session_id", 256);
                            Instant last = readSafeInstant(rs, "last_entry");
                            if (sid.isBlank() || last == null) {
                                continue;
                            }

                            String normalizedSessionId = sid.trim();
                            InactiveUsersListPageServlet.Row row = getOrCreateAggregateRow(aggregateRows, normalizedSessionId);
                            row.chatCount += readNonNegativeLong(rs, "total");
                            if (row.lastEntry == null || last.isAfter(row.lastEntry.toInstant())) {
                                row.lastEntry = Timestamp.from(last);
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
        if (table == null || table.isBlank()) {
            return rows;
        }
        String safeTable = table;
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = conn.prepareStatement(sql);
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to prepare widget-row query for table " + safeTable, ex);
            return rows;
        }

        try (PreparedStatement ps = preparedStatement) {
            ResultSet queryResult;
            try {
                queryResult = ps.executeQuery();
            } catch (SQLException ex) {
                log.log(Level.FINE, "Unable to execute widget-row query for table " + safeTable, ex);
                return rows;
            }

            try (ResultSet rs = queryResult) {
                while (rs.next()) {
                        String sid = readSafeDbText(rs, "session_id", 256);
                        Instant last = readSafeInstant(rs, "last_entry");
                        if (sid.isBlank() || last == null || !last.isBefore(cutoff)) {
                        continue;
                    }

                    String normalizedSessionId = sid.trim();
                        InactiveUsersListPageServlet.Row row = createWidgetRow(
                                normalizedSessionId,
                                widgetId,
                                widgetLabel,
                                Timestamp.from(last),
                                readNonNegativeLong(rs, "total")
                        );
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
            log.log(Level.FINE, "Unable to load widget rows for " + safeTable, ex);
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
            String safeTable = table == null ? "<unknown>" : table;
            log.log(Level.FINE, "Frustration detection skipped for session " + sessionId + " in " + safeTable, ex);
            return emptyFrustrationResult();
        }
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) {
        List<String> prompts = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank() || table == null || table.isBlank() || limit < 1) {
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
                        String prompt = readSafeDbText(rs, "p", 12000);
                        if (!prompt.isBlank()) {
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

    private long readNonNegativeLong(ResultSet rs, String column) {
        String text = readSafeDbText(rs, column, 32);
        if (text.isBlank() || !text.matches("^-?\\d{1,18}$")) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(text));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid numeric text in column " + column, ex);
            return 0L;
        }
    }

    private Instant readSafeInstant(ResultSet rs, String column) {
        String text = readSafeDbText(rs, column, 128);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeException ex) {
            try {
                return Timestamp.valueOf(text.replace('T', ' ')).toInstant();
            } catch (IllegalArgumentException secondEx) {
                log.log(Level.FINE, "Invalid timestamp text in column " + column, secondEx);
                return null;
            }
        }
    }

    private String readSafeDbText(ResultSet rs, String column, int maxChars) {
        try (Reader reader = rs.getCharacterStream(column)) {
            if (reader == null) {
                return "";
            }
            char[] buffer = new char[256];
            StringBuilder value = new StringBuilder(Math.max(64, Math.min(maxChars, 512)));
            int total = 0;
            int read;
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (maxChars > 0 && total > maxChars) {
                    int remaining = Math.max(0, maxChars - (total - read));
                    if (remaining > 0) {
                        value.append(buffer, 0, remaining);
                    }
                    break;
                }
                value.append(buffer, 0, read);
            }

            String normalized = value.toString().replace('\u0000', ' ').replace("\r", "").replace("\n", " ").trim();
            if (normalized.length() > maxChars) {
                return normalized.substring(0, maxChars);
            }
            return normalized;
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, "Unable to read text column " + column, ex);
            return "";
        }
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
