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
import com.sim.chatserver.web.dashboard.sessions.DashboardSessionDataUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

final class WidgetExportQueryService {

    private static final int DB_ID_CHUNK_SIZE = 500;
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

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

        String tableName = sanitizeWidgetTableName(widgetId);
        Connection conn = openConnectionSafe();
        try (conn) {
            if (!DashboardSessionDataUtil.tableExists(conn, tableName, log)) {
                return exportRows;
            }

            for (List<String> chunk : chunk(selectedChatIds, DB_ID_CHUNK_SIZE)) {
                if (chunk.isEmpty()) {
                    continue;
                }

                String placeholders = chunk.stream().map(x -> "?").collect(Collectors.joining(","));
                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE widget_chat_id IN (" + placeholders + ')';

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (String id : chunk) {
                        ps.setString(idx++, id);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = readDbText(rs, "widget_chat_id", 512);
                            Timestamp created = readDbTimestamp(rs, "created_at");
                            String prompt = readDbText(rs, "prompt", 32000);
                            String responseText = readDbText(rs, "response_text", 32000);
                            String sessionId = readDbText(rs, "session_id", 256);

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
            throw new IllegalStateException("Unable to resolve export rows", ex);
        }

        return exportRows;
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to open export database connection", ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
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
            return "";
        }
        try (Reader reader = rs.getCharacterStream(columnName)) {
            if (reader == null) {
                return "";
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
            log.log(Level.FINE, "Typed DB text read failed for column " + columnName, ex);
            return "";
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
            log.log(Level.FINE, "Typed DB timestamp read failed for column " + columnName, ex);
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
            log.log(Level.FINE, "Falling back to SQL timestamp parsing", ex);
            return Timestamp.valueOf(text.replace('T', ' '));
        }
    }
}
