package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableSelectionQueryService {

    static final String EMPTY = "";
    private static final String MSG_TABLE_FOR_WIDGET_NOT_EXISTS = "Table for widget does not exist.";
    private static final String MSG_UNABLE_TO_COLLECT_CHAT_IDS = "Unable to collect chat ids";
    static final String SQL_PROMPT_ILIKE = "prompt ILIKE ?";
    static final String SQL_RESPONSE_TEXT_ILIKE = "response_text ILIKE ?";
    static final String SQL_GLOBAL_ILIKE = "(prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)";
    static final String SQL_WHERE = " WHERE ";
    static final String SQL_AND = " AND ";

    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    private final Logger log;

    public WidgetTableSelectionQueryService(Logger log) {
        this.log = log;
    }

    public List<String> selectChatIds(String widgetId, String filterPrompt, String filterResponse, String globalSearch) {
        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                throw new java.util.NoSuchElementException(MSG_TABLE_FOR_WIDGET_NOT_EXISTS);
            }

            FilterState filters = new FilterState(filterPrompt, filterResponse, globalSearch);
            String sql = SqlLiteralConstants.SQL_SELECT_WIDGET_CHAT_ID_FROM + quoteIdentifier(tableName)
                    + filters.buildWhereClause() + SqlLiteralConstants.SQL_ORDER_BY_CREATED_AT_DESC;

            List<String> chatIds = new ArrayList<>();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String param : filters.params()) {
                    ps.setString(idx++, param);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString(SqlLiteralConstants.COL_WIDGET_CHAT_ID);
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId);
                        }
                    }
                }
            }
            return chatIds;
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_TO_COLLECT_CHAT_IDS, ex);
        }
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

final class FilterState {

    private final String prompt;
    private final String response;
    private final String global;

    FilterState(String prompt, String response, String global) {
        this.prompt = prompt;
        this.response = response;
        this.global = global;
    }

    String buildWhereClause() {
        List<String> pieces = new ArrayList<>();
        if (hasValue(prompt)) {
            pieces.add(WidgetTableSelectionQueryService.SQL_PROMPT_ILIKE);
        }
        if (hasValue(response)) {
            pieces.add(WidgetTableSelectionQueryService.SQL_RESPONSE_TEXT_ILIKE);
        }
        if (hasValue(global)) {
            pieces.add(WidgetTableSelectionQueryService.SQL_GLOBAL_ILIKE);
        }
        if (pieces.isEmpty()) {
            return WidgetTableSelectionQueryService.EMPTY;
        }
        return WidgetTableSelectionQueryService.SQL_WHERE + String.join(WidgetTableSelectionQueryService.SQL_AND, pieces);
    }

    List<String> params() {
        List<String> params = new ArrayList<>();
        if (hasValue(prompt)) {
            params.add(pattern(prompt));
        }
        if (hasValue(response)) {
            params.add(pattern(response));
        }
        if (hasValue(global)) {
            String globalPattern = pattern(global);
            for (int i = 0; i < 3; i++) {
                params.add(globalPattern);
            }
        }
        return params;
    }

    private boolean hasValue(String val) {
        return val != null && !val.isBlank();
    }

    private String pattern(String input) {
        return '%' + input.trim() + '%';
    }
}

