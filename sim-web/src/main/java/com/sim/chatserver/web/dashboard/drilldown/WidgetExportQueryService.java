package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

final class WidgetExportQueryService {

    private static final int DB_ID_CHUNK_SIZE = 500;
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private static final String SQL_PLACEHOLDER = "?";
    private static final String SQL_JOIN_DELIMITER = ",";
    private static final String SQL_SELECT_ROWS_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
    private static final String SQL_SELECT_ROWS_SUFFIX = " WHERE widget_chat_id IN (";
    private static final String COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String MSG_RESOLVE_EXPORT_ROWS_FAILED = "Unable to resolve export rows";
    private static final String MSG_OPEN_EXPORT_DB_CONNECTION_FAILED = "Unable to open export database connection";
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String EMPTY = "";
    private static final String MSG_TYPED_DB_TEXT_READ_FAILED_PREFIX = "Typed DB text read failed for column ";
    private static final String MSG_TYPED_DB_TIMESTAMP_READ_FAILED_PREFIX = "Typed DB timestamp read failed for column ";
    private static final String MSG_SQL_TIMESTAMP_PARSE_FALLBACK = "Falling back to SQL timestamp parsing";

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    WidgetExportQueryService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    List<TermChatSnapshot> loadRows(String widgetId, List<String> selectedChatIds) {
        List<TermChatSnapshot> exportRows = new ArrayList<>();
        if (selectedChatIds == null || selectedChatIds.isEmpty()) {
            return exportRows;
        }

        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        Connection conn = openConnectionSafe();
        try (conn) {
            if (!DashboardSessionDataUtil.tableExists(conn, tableName, log)) {
                return exportRows;
            }

            for (List<String> chunk : chunk(selectedChatIds, DB_ID_CHUNK_SIZE)) {
                if (chunk.isEmpty()) {
                    continue;
                }

                String placeholders = chunk.stream().map(x -> SQL_PLACEHOLDER).collect(Collectors.joining(SQL_JOIN_DELIMITER));
                String sql = SQL_SELECT_ROWS_PREFIX
                        + quoteIdentifier(tableName)
                    + SQL_SELECT_ROWS_SUFFIX + placeholders + ')';

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (String id : chunk) {
                        ps.setString(idx++, id);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = readDbText(rs, COL_WIDGET_CHAT_ID, 512);
                            Timestamp created = readDbTimestamp(rs, COL_CREATED_AT);
                            String prompt = readDbText(rs, COL_PROMPT, 32000);
                            String responseText = readDbText(rs, COL_RESPONSE_TEXT, 32000);
                            String sessionId = readDbText(rs, COL_SESSION_ID, 256);

                            exportRows.add(new TermChatSnapshot(
                                    sessionId,
                                    widgetId,
                                    chatId,
                                    prompt,
                                    responseText,
                                    created,
                                    sessionId
                            ));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_RESOLVE_EXPORT_ROWS_FAILED, ex);
        }

        return exportRows;
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_OPEN_EXPORT_DB_CONNECTION_FAILED, ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }

    private List<List<String>> chunk(List<String> input, int size) {
        List<List<String>> out = new ArrayList<>();
        if (input == null || input.isEmpty() || size <= 0) {
            return out;
        }
        int total = input.size();
        for (int i = 0; i < total; i += size) {
            out.add(input.subList(i, Math.min(i + size, total)));
        }
        return out;
    }

    private String readDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return EMPTY;
        }
        try (Reader reader = rs.getCharacterStream(columnName)) {
            if (reader == null) {
                return EMPTY;
            }
            char[] buffer = new char[256];
            StringBuilder value = new StringBuilder(Math.max(64, Math.min(maxLen, 512)));
            int total = 0;
            int read;
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (maxLen > 0 && total > maxLen) {
                    int remaining = Math.max(0, maxLen - (total - read));
                    if (remaining > 0) {
                        value.append(buffer, 0, remaining);
                    }
                    break;
                }
                value.append(buffer, 0, read);
            }
            return ServletRequestParamUtil.normalizeBodyText(value.toString(), maxLen, true);
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, MSG_TYPED_DB_TEXT_READ_FAILED_PREFIX + columnName, ex);
            return EMPTY;
        }
    }

    private Timestamp readDbTimestamp(ResultSet rs, String columnName) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return null;
        }
        Timestamp ts;
        try {
            ts = rs.getTimestamp(columnName);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_TYPED_DB_TIMESTAMP_READ_FAILED_PREFIX + columnName, ex);
            ts = null;
        }
        if (ts != null) {
            return ts;
        }

        String text = readDbText(rs, columnName, 128);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Timestamp.from(Instant.parse(text));
        } catch (DateTimeException | IllegalArgumentException ex) {
            log.log(Level.FINE, MSG_SQL_TIMESTAMP_PARSE_FALLBACK, ex);
            return Timestamp.valueOf(text.replace('T', ' '));
        }
    }
}

