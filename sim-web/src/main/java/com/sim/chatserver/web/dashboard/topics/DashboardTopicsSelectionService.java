package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;

final class DashboardTopicsSelectionService {

    private static final int IN_CLAUSE_BATCH_SIZE = 200;
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");

    private final AppDataSourceHolder dataSourceHolder;
    private final Logger log;

    DashboardTopicsSelectionService(AppDataSourceHolder dataSourceHolder, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.log = log;
    }

    SelectionResolution resolveSelectedChats(Set<String> requestedIds, Map<String, WidgetEntry> widgetById) {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        Set<String> foundIds = new LinkedHashSet<>();

        Connection conn = openConnectionSafe();
        try (conn) {
            List<String> idList = new ArrayList<>(requestedIds);

            for (Map.Entry<String, WidgetEntry> entry : widgetById.entrySet()) {
                String widgetId = entry.getKey();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                for (int from = 0; from < idList.size(); from += IN_CLAUSE_BATCH_SIZE) {
                    int to = Math.min(from + IN_CLAUSE_BATCH_SIZE, idList.size());
                    List<String> chunk = idList.subList(from, to);

                    String inClause = String.join(",", chunk.stream().map(id -> "?").toList());
                    String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                            + quoteIdentifier(tableName)
                            + " WHERE widget_chat_id IN (" + inClause + ')';

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        int idx = 1;
                        for (String id : chunk) {
                            ps.setString(idx++, id);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String chatId = readDbText(rs, "widget_chat_id", 512);
                                if (chatId == null || chatId.isBlank()) {
                                    continue;
                                }

                                String prompt = readDbText(rs, "prompt", 64000);
                                String responseText = readDbText(rs, "response_text", 64000);
                                Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                                String sessionId = readDbText(rs, "session_id", 512);

                                snapshots.add(new TermChatSnapshot(
                                        "Popular Topics",
                                        widgetId,
                                        chatId,
                                        prompt,
                                        responseText,
                                        createdAt,
                                        sessionId
                                ));
                                foundIds.add(chatId);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to resolve selected chats", ex);
        }

        return new SelectionResolution(snapshots, foundIds);
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to open dashboard topics connection", ex);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
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
        if (identifier == null || !SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String readDbText(ResultSet rs, String column, int maxChars) {
        try (Reader reader = rs.getCharacterStream(column)) {
            if (reader == null) {
                return null;
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

            return ServletRequestParamUtil.normalizeBodyText(value.toString(), maxChars, true);
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, "Typed DB text read failed for column " + column, ex);
            return null;
        }
    }

    record SelectionResolution(List<TermChatSnapshot> snapshots, Set<String> foundIds) {
    }
}
