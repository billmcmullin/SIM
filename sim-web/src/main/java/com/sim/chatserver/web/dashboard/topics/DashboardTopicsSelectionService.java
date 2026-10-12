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
    private static final String MSG_RESOLVE_SELECTED_CHATS_FAILED = "Unable to resolve selected chats";
    private static final String SQL_JOIN_DELIMITER = ",";
    private static final String SQL_PLACEHOLDER = "?";
    private static final String SQL_SELECT_CHAT_ROWS_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
    private static final String SQL_SELECT_CHAT_ROWS_SUFFIX = " WHERE widget_chat_id IN (";
    private static final String COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    private static final String TOPIC_POPULAR_TOPICS = "Popular Topics";
    private static final String COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    private static final String COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    private static final String COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String MSG_OPEN_CONNECTION_FAILED = "Unable to open dashboard topics connection";
    private static final String MSG_TABLE_METADATA_INSPECTION_FAILED_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
    private static final String DEFAULT_WIDGET_TABLE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET;
    private static final String NON_IDENTIFIER_REGEX = com.sim.chatserver.util.CommonLiteralConstants.REGEX_NON_ALNUM_UNDERSCORE;
    private static final String UNDERSCORE = "_";
    private static final String WIDGET_PREFIX = "w_";
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    private static final String MSG_TYPED_DB_TEXT_READ_FAILED_PREFIX = "Typed DB text read failed for column ";

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
            int idCount = idList.size();

            for (Map.Entry<String, WidgetEntry> entry : widgetById.entrySet()) {
                resolveWidgetChats(conn, entry.getKey(), idList, idCount, snapshots, foundIds);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_RESOLVE_SELECTED_CHATS_FAILED, ex);
        }

        return new SelectionResolution(snapshots, foundIds);
    }

    private void resolveWidgetChats(
            Connection conn,
            String widgetId,
            List<String> idList,
            int idCount,
            List<TermChatSnapshot> snapshots,
            Set<String> foundIds
    ) throws SQLException {
        String tableName = sanitizeWidgetTableName(widgetId);
        if (!tableExists(conn, tableName)) {
            return;
        }

        for (int from = 0; from < idCount; from += IN_CLAUSE_BATCH_SIZE) {
            int to = Math.min(from + IN_CLAUSE_BATCH_SIZE, idCount);
            resolveChunkChats(conn, widgetId, tableName, idList.subList(from, to), snapshots, foundIds);
        }
    }

    private void resolveChunkChats(
            Connection conn,
            String widgetId,
            String tableName,
            List<String> chunk,
            List<TermChatSnapshot> snapshots,
            Set<String> foundIds
    ) throws SQLException {
            String inClause = String.join(SQL_JOIN_DELIMITER, chunk.stream().map(id -> SQL_PLACEHOLDER).toList());
            String sql = SQL_SELECT_CHAT_ROWS_PREFIX
                + quoteIdentifier(tableName)
                + SQL_SELECT_CHAT_ROWS_SUFFIX + inClause + ')';

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (String id : chunk) {
                ps.setString(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                addResolvedRows(rs, widgetId, snapshots, foundIds);
            }
        }
    }

    private void addResolvedRows(
            ResultSet rs,
            String widgetId,
            List<TermChatSnapshot> snapshots,
            Set<String> foundIds
    ) throws SQLException {
        while (rs.next()) {
            String chatId = readDbText(rs, COL_WIDGET_CHAT_ID, 512);
            if (chatId == null || chatId.isBlank()) {
                continue;
            }

            snapshots.add(new TermChatSnapshot(
                    TOPIC_POPULAR_TOPICS,
                    widgetId,
                    chatId,
                    readDbText(rs, COL_PROMPT, 64000),
                    readDbText(rs, COL_RESPONSE_TEXT, 64000),
                    SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT),
                    readDbText(rs, COL_SESSION_ID, 512)
            ));
            foundIds.add(chatId);
        }
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder.getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_OPEN_CONNECTION_FAILED, ex);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_TABLE_METADATA_INSPECTION_FAILED_PREFIX + tableName, ex);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return DEFAULT_WIDGET_TABLE;
        }
        String normalized = widgetId.trim().replaceAll(NON_IDENTIFIER_REGEX, UNDERSCORE);
        if (normalized.isEmpty()) {
            normalized = DEFAULT_WIDGET_TABLE;
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = WIDGET_PREFIX + normalized;
        }
        if (normalized.length() > 60) {
            normalized = new String(normalized.toCharArray(), 0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
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
            log.log(Level.FINE, MSG_TYPED_DB_TEXT_READ_FAILED_PREFIX + column, ex);
            return null;
        }
    }

    record SelectionResolution(List<TermChatSnapshot> snapshots, Set<String> foundIds) {
    }
}

