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
import com.sim.chatserver.util.DashboardDbUtil;

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableSelectionQueryService {

    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private final Logger log;

    public WidgetTableSelectionQueryService(Logger log) {
        this.log = log;
    }

    public List<String> selectChatIds(String widgetId, String filterPrompt, String filterResponse, String globalSearch) {
        String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                throw new java.util.NoSuchElementException("Table for widget does not exist.");
            }

            FilterState filters = new FilterState(filterPrompt, filterResponse, globalSearch);
            String sql = "SELECT widget_chat_id FROM " + quoteIdentifier(tableName) + filters.buildWhereClause()
                    + " ORDER BY created_at DESC";

            List<String> chatIds = new ArrayList<>();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String param : filters.params()) {
                    ps.setString(idx++, param);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId);
                        }
                    }
                }
            }
            return chatIds;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to collect chat ids", ex);
        }
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

    static final class FilterState {

        private final String prompt;
        private final String response;
        private final String global;

        private FilterState(String prompt, String response, String global) {
            this.prompt = prompt;
            this.response = response;
            this.global = global;
        }

        private String buildWhereClause() {
            List<String> pieces = new ArrayList<>();
            if (hasValue(prompt)) {
                pieces.add("prompt ILIKE ?");
            }
            if (hasValue(response)) {
                pieces.add("response_text ILIKE ?");
            }
            if (hasValue(global)) {
                pieces.add("(prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)");
            }
            if (pieces.isEmpty()) {
                return "";
            }
            return " WHERE " + String.join(" AND ", pieces);
        }

        private List<String> params() {
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
}