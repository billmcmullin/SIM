package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableSelectIdsQueryService {

    private static final String MSG_WIDGET_DATA_NOT_FOUND = "Widget data not found";
    private static final String MSG_UNABLE_TO_FETCH_CHAT_IDS = "Unable to fetch chat IDs";
    private static final String MSG_UNABLE_TO_VERIFY_WIDGET_TABLE = "Unable to verify widget table existence";
    private static final String SQL_WIDGET_CHAT_ID_NOT_NULL = "widget_chat_id IS NOT NULL";
    private static final String SQL_SEARCH_ALL_COLUMNS =
            "(LOWER(widget_chat_id) LIKE ? OR LOWER(prompt) LIKE ? OR LOWER(response_text) LIKE ?)";
    private static final String SQL_LOWER_PROMPT_LIKE = "LOWER(prompt) LIKE ?";
    private static final String SQL_LOWER_RESPONSE_TEXT_LIKE = "LOWER(response_text) LIKE ?";
    private static final String SQL_CREATED_AT_RANGE = "created_at >= ? AND created_at < ?";

    private final Logger log;

    public WidgetTableSelectIdsQueryService(Logger log) {
        this.log = log;
    }

    public List<String> selectIds(String widgetId, String search, String filterPrompt, String filterResponse, LocalDate selectedDate) {
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                throw new java.util.NoSuchElementException(MSG_WIDGET_DATA_NOT_FOUND);
            }

            List<String> conditions = new ArrayList<>();
            List<String> values = new ArrayList<>();
            conditions.add(SQL_WIDGET_CHAT_ID_NOT_NULL);

            if (search != null && !search.isBlank()) {
                conditions.add(SQL_SEARCH_ALL_COLUMNS);
                String pattern = '%' + search + '%';
                values.add(pattern);
                values.add(pattern);
                values.add(pattern);
            }
            if (filterPrompt != null && !filterPrompt.isBlank()) {
                conditions.add(SQL_LOWER_PROMPT_LIKE);
                values.add('%' + filterPrompt + '%');
            }
            if (filterResponse != null && !filterResponse.isBlank()) {
                conditions.add(SQL_LOWER_RESPONSE_TEXT_LIKE);
                values.add('%' + filterResponse + '%');
            }

            Timestamp startTs = null;
            Timestamp endTs = null;
            if (selectedDate != null) {
                conditions.add(SQL_CREATED_AT_RANGE);
                startTs = Timestamp.valueOf(selectedDate.atStartOfDay());
                endTs = Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay());
            }

            StringBuilder sql = new StringBuilder(SqlLiteralConstants.SQL_SELECT_WIDGET_CHAT_ID_FROM)
                    .append(quoteIdentifier(tableName));
            if (!conditions.isEmpty()) {
                sql.append(SqlLiteralConstants.SQL_WHERE).append(String.join(SqlLiteralConstants.SQL_AND, conditions));
            }
            sql.append(SqlLiteralConstants.SQL_ORDER_BY_CREATED_AT_DESC);

            List<String> chatIds = new ArrayList<>();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String value : values) {
                    ps.setString(idx++, value);
                }
                if (selectedDate != null) {
                    ps.setTimestamp(idx++, startTs);
                    ps.setTimestamp(idx, endTs);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString(SqlLiteralConstants.COL_WIDGET_CHAT_ID);
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId.trim());
                        }
                    }
                }
            }
            return chatIds;
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_TO_FETCH_CHAT_IDS, ex);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{CommonLiteralConstants.DB_TABLE_TYPE_TABLE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_TO_VERIFY_WIDGET_TABLE, ex);
        }
        return false;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace(SqlLiteralConstants.DOUBLE_QUOTE, SqlLiteralConstants.DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }
}