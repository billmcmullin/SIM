package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
final class InactiveUsersListQueryService {
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String VALUE_WIDGET_SCOPE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
    private static final String VALUE_ALL = "ALL";
    private static final String VALUE_ALL_WIDGETS = "All Widgets";
    private static final String VALUE_UNKNOWN = "<unknown>";
    private static final String QUOTE = "\"";
    private static final String QUOTE_ESCAPED = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String SQL_IDENTIFIER_INVALID_MSG = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String REGEX_LONG_INT = "^-?\\d{1,18}$";
    private static final String DB_COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String DB_COL_LAST_ENTRY = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_LAST_ENTRY;
    private static final String DB_COL_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
    private static final String DB_COL_PROMPT_ALIAS = "p";
    private static final String SQL_SELECT_SESSION_AGG = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_SESSION_COUNTS_PREFIX;
    private static final String SQL_WHERE_GROUP_BY_SESSION = com.sim.chatserver.util.CommonLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;
    private static final String SQL_SELECT_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_PREFIX;
    private static final String SQL_AS_P_FROM = " AS p FROM ";
    private static final String SQL_WHERE_SESSION_PROMPT = " WHERE session_id = ? AND ";
    private static final String SQL_IS_NOT_NULL_AND = " IS NOT NULL AND ";
    private static final String SQL_NOT_EMPTY = " <> ''";
    private static final String SQL_ORDER_BY_CREATED_DESC = " ORDER BY created_at DESC";
    private static final String MSG_PREPARE_AGGREGATE_QUERY = "Unable to prepare aggregate query for table ";
    private static final String MSG_EXECUTE_AGGREGATE_QUERY = "Unable to execute aggregate query for table ";
    private static final String MSG_COMPUTE_INACTIVE_USERS = "Unable to compute inactive users list";
    private static final String MSG_PREPARE_WIDGET_QUERY = "Unable to prepare widget-row query for table ";
    private static final String MSG_EXECUTE_WIDGET_QUERY = "Unable to execute widget-row query for table ";
    private static final String MSG_LOAD_WIDGET_ROWS = "Unable to load widget rows for ";
    private static final String MSG_FRUSTRATION_SKIPPED = "Frustration detection skipped for session ";
    private static final String MSG_IN = " in ";
    private static final String MSG_PREPARE_PROMPT_QUERY = "Unable to prepare prompt query for column ";
    private static final String MSG_EXECUTE_PROMPT_QUERY = "Unable to execute prompt query for column ";
    private static final String MSG_PROMPT_COLUMN_UNAVAILABLE = "Prompt column unavailable for frustration scan: ";
    private static final String MSG_INSPECT_TABLE_METADATA = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
    private static final String MSG_INVALID_NUMERIC_COLUMN = "Invalid numeric text in column ";
    private static final String MSG_INVALID_TIMESTAMP_COLUMN = "Invalid timestamp text in column ";
    private static final String MSG_RESULT_SET_STRING_FAILED = "ResultSet#getString(String) failed for column ";
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String[] PROMPT_COLUMNS = {com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT, "prompt_text", "user_prompt"};

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    InactiveUsersListQueryService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    List<Row> loadRows(
            String scope,
            String widgetIdFilter,
            Map<String, String> widgetNameById,
            Instant cutoff,
            int promptScanLimit,
            Function<List<String>, InactiveUsersListFrustrationResult> frustrationAnalyzer) {
        List<Row> allRows = new ArrayList<>();

        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            if (VALUE_WIDGET_SCOPE.equalsIgnoreCase(scope)) {
                if (!widgetIdFilter.isBlank() && widgetNameById.containsKey(widgetIdFilter)) {
                    String table = sanitizeWidgetTableName(widgetIdFilter);
                    if (tableExists(conn, table)) {
                        List<Row> rows = loadWidgetRows(
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

            Map<String, Row> aggregateRows = new LinkedHashMap<>();
            for (String widgetId : widgetNameById.keySet()) {
                String table = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, table)) {
                    continue;
                }

                String sql = SQL_SELECT_SESSION_AGG
                        + quoteIdentifier(table)
                    + SQL_WHERE_GROUP_BY_SESSION;

                PreparedStatement preparedStatement;
                try {
                    preparedStatement = conn.prepareStatement(sql);
                } catch (SQLException ex) {
                    log.log(Level.FINE, MSG_PREPARE_AGGREGATE_QUERY + table, ex);
                    continue;
                }

                try (PreparedStatement ps = preparedStatement) {
                    ResultSet queryResult;
                    try {
                        queryResult = ps.executeQuery();
                    } catch (SQLException ex) {
                        log.log(Level.FINE, MSG_EXECUTE_AGGREGATE_QUERY + table, ex);
                        continue;
                    }

                    try (ResultSet rs = queryResult) {
                        while (rs.next()) {
                            String sid = readSafeDbText(rs, DB_COL_SESSION_ID, 256);
                            Instant last = readSafeInstant(rs, DB_COL_LAST_ENTRY);
                            if (sid.isBlank() || last == null) {
                                continue;
                            }

                            String normalizedSessionId = sid.trim();
                            Row row = getOrCreateAggregateRow(aggregateRows, normalizedSessionId);
                            row.chatCount += readNonNegativeLong(rs, DB_COL_TOTAL);
                            if (row.lastEntry == null || last.isAfter(row.lastEntry.toInstant())) {
                                row.lastEntry = Timestamp.from(last);
                            }

                            InactiveUsersListFrustrationResult frustrationResult = detectFrustrationForSession(
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

            for (Row row : aggregateRows.values()) {
                if (row.lastEntry != null && row.lastEntry.toInstant().isBefore(cutoff)) {
                    allRows.add(row);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, MSG_COMPUTE_INACTIVE_USERS, ex);
        }

        return allRows;
    }

    private List<Row> loadWidgetRows(
            Connection conn,
            String widgetId,
            String widgetLabel,
            Instant cutoff,
            String table,
            int promptScanLimit,
            Function<List<String>, InactiveUsersListFrustrationResult> frustrationAnalyzer) {
        List<Row> rows = new ArrayList<>();
        if (table == null || table.isBlank()) {
            return rows;
        }
        String safeTable = table;
        String sql = SQL_SELECT_SESSION_AGG
                + quoteIdentifier(table)
            + SQL_WHERE_GROUP_BY_SESSION;

        PreparedStatement preparedStatement;
        try {
            preparedStatement = conn.prepareStatement(sql);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_PREPARE_WIDGET_QUERY + safeTable, ex);
            return rows;
        }

        try (PreparedStatement ps = preparedStatement) {
            ResultSet queryResult;
            try {
                queryResult = ps.executeQuery();
            } catch (SQLException ex) {
                log.log(Level.FINE, MSG_EXECUTE_WIDGET_QUERY + safeTable, ex);
                return rows;
            }

            try (ResultSet rs = queryResult) {
                while (rs.next()) {
                        String sid = readSafeDbText(rs, DB_COL_SESSION_ID, 256);
                        Instant last = readSafeInstant(rs, DB_COL_LAST_ENTRY);
                        if (sid.isBlank() || last == null || !last.isBefore(cutoff)) {
                        continue;
                    }

                    String normalizedSessionId = sid.trim();
                        Row row = createWidgetRow(
                                normalizedSessionId,
                                widgetId,
                                widgetLabel,
                                Timestamp.from(last),
                                readNonNegativeLong(rs, DB_COL_TOTAL)
                        );
                    InactiveUsersListFrustrationResult frustrationResult = detectFrustrationForSession(
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
            log.log(Level.FINE, MSG_LOAD_WIDGET_ROWS + safeTable, ex);
        }
        return rows;
    }

            private InactiveUsersListFrustrationResult detectFrustrationForSession(
            Connection conn,
            String table,
            String sessionId,
            int promptScanLimit,
                Function<List<String>, InactiveUsersListFrustrationResult> frustrationAnalyzer) {
        try {
            List<String> prompts = loadRecentPromptsForSession(conn, table, sessionId, promptScanLimit);
            return frustrationAnalyzer.apply(prompts);
        } catch (IllegalArgumentException ex) {
            String safeTable = table == null ? VALUE_UNKNOWN : table;
            log.log(Level.FINE, MSG_FRUSTRATION_SKIPPED + sessionId + MSG_IN + safeTable, ex);
            return emptyFrustrationResult();
        }
    }

    private List<String> loadRecentPromptsForSession(Connection conn, String table, String sessionId, int limit) {
        List<String> prompts = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank() || table == null || table.isBlank() || limit < 1) {
            return prompts;
        }

        for (String column : PROMPT_COLUMNS) {
            String sql = SQL_SELECT_PREFIX + quoteIdentifier(column) + SQL_AS_P_FROM + quoteIdentifier(table)
                + SQL_WHERE_SESSION_PROMPT + quoteIdentifier(column) + SQL_IS_NOT_NULL_AND + quoteIdentifier(column) + SQL_NOT_EMPTY
                + SQL_ORDER_BY_CREATED_DESC;

            PreparedStatement preparedStatement;
            try {
                preparedStatement = conn.prepareStatement(sql);
            } catch (SQLException ex) {
                log.log(Level.FINEST, MSG_PREPARE_PROMPT_QUERY + column, ex);
                continue;
            }

            try (PreparedStatement ps = preparedStatement) {
                ps.setString(1, sessionId);
                ps.setMaxRows(limit);

                ResultSet queryResult;
                try {
                    queryResult = ps.executeQuery();
                } catch (SQLException ex) {
                    log.log(Level.FINEST, MSG_EXECUTE_PROMPT_QUERY + column, ex);
                    continue;
                }

                try (ResultSet rs = queryResult) {
                    while (rs.next()) {
                        String prompt = readSafeDbText(rs, DB_COL_PROMPT_ALIAS, 12000);
                        if (!prompt.isBlank()) {
                            prompts.add(prompt);
                        }
                    }
                    if (!prompts.isEmpty()) {
                        return prompts;
                    }
                }
            } catch (SQLException ex) {
                log.log(Level.FINEST, MSG_PROMPT_COLUMN_UNAVAILABLE + column, ex);
            }
        }

        return prompts;
    }

    private Row getOrCreateAggregateRow(
            Map<String, Row> aggregateRows,
            String normalizedSessionId) {
        Row existing = aggregateRows.get(normalizedSessionId);
        if (existing != null) {
            return existing;
        }

        Row created = createAggregateRow(normalizedSessionId);
        aggregateRows.put(normalizedSessionId, created);
        return created;
    }

    private Row createAggregateRow(String normalizedSessionId) {
        Row row = new Row();
        row.sessionId = normalizedSessionId;
        row.widgetId = VALUE_ALL;
        row.widgetLabel = VALUE_ALL_WIDGETS;
        row.frustrationDetected = false;
        row.frustrationScore = 0.0;
        row.frustrationReason = EMPTY;
        return row;
    }

    private Row createWidgetRow(
            String normalizedSessionId,
            String widgetId,
            String widgetLabel,
            Timestamp lastEntry,
            long chatCount) {
        Row row = new Row();
        row.sessionId = normalizedSessionId;
        row.widgetId = widgetId;
        row.widgetLabel = widgetLabel;
        row.chatCount = chatCount;
        row.lastEntry = lastEntry;
        row.frustrationDetected = false;
        row.frustrationScore = 0.0;
        row.frustrationReason = EMPTY;
        return row;
    }

    private InactiveUsersListFrustrationResult emptyFrustrationResult() {
        InactiveUsersListFrustrationResult result = new InactiveUsersListFrustrationResult();
        result.detected = false;
        result.score = 0.0;
        result.reason = EMPTY;
        return result;
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
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_INSPECT_TABLE_METADATA + tableName, ex);
        }
        return false;
    }

    private long readNonNegativeLong(ResultSet rs, String column) {
        String safeColumn = normalizeColumnName(column);
        String text = readSafeDbText(rs, safeColumn, 32);
        if (text.isBlank() || !text.matches(REGEX_LONG_INT)) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(text));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_NUMERIC_COLUMN + safeColumn, ex);
            return 0L;
        }
    }

    private Instant readSafeInstant(ResultSet rs, String column) {
        String safeColumn = normalizeColumnName(column);
        String text = readSafeDbText(rs, safeColumn, 128);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeException ex) {
            try {
                return Timestamp.valueOf(text.replace('T', ' ')).toInstant();
            } catch (IllegalArgumentException secondEx) {
                log.log(Level.FINE, MSG_INVALID_TIMESTAMP_COLUMN + safeColumn, secondEx);
                return null;
            }
        }
    }

    private String readSafeDbText(ResultSet rs, String column, int maxChars) {
        int safeLimit = maxChars > 0 ? maxChars : 8192;
        if (rs == null || column == null || column.isBlank()) {
            return EMPTY;
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return validateDbText(canonicalizeDbText(raw), safeLimit);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RESULT_SET_STRING_FAILED + safeColumnName(column), ex);
        }

        return EMPTY;
    }

    private String validateDbText(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }

        String canonical = canonicalizeDbText(value);
        if (canonical.isBlank()) {
            return EMPTY;
        }

        StringBuilder safe = new StringBuilder(canonical.length());
        for (int i = 0; i < canonical.length(); i++) {
            char ch = canonical.charAt(i);
            if (Character.isISOControl(ch) && ch != System.lineSeparator().charAt(System.lineSeparator().length() - 1) && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }

        String normalized = safe.toString().replace(System.lineSeparator(), SPACE).trim();
        if (maxChars > 0 && normalized.length() > maxChars) {
            return TextIoSanitizerUtil.safeSlice(normalized, 0, maxChars);
        }
        return normalized;
    }

    private String canonicalizeDbText(String value) {
        if (value == null) {
            return EMPTY;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u0000', ' ')
                .replace(System.lineSeparator(), EMPTY);
    }

    private String safeColumnName(String column) {
        if (column == null || column.isBlank()) {
            return VALUE_UNKNOWN;
        }
        return column;
    }

    private String normalizeColumnName(String column) {
        return column == null ? EMPTY : column;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        return DashboardDbUtil.sanitizeWidgetTableName(widgetId);
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, QUOTE_ESCAPED) + '"';
    }
}

