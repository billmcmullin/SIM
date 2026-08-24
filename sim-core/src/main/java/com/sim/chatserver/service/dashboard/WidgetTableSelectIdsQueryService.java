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

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableSelectIdsQueryService {

    private final Logger log;

    public WidgetTableSelectIdsQueryService(Logger log) {
        this.log = log;
    }

    public List<String> selectIds(String widgetId, String search, String filterPrompt, String filterResponse, LocalDate selectedDate) {
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                throw new java.util.NoSuchElementException("Widget data not found");
            }

            List<String> conditions = new ArrayList<>();
            List<String> values = new ArrayList<>();
            conditions.add("widget_chat_id IS NOT NULL");

            if (search != null && !search.isBlank()) {
                conditions.add("(LOWER(widget_chat_id) LIKE ? OR LOWER(prompt) LIKE ? OR LOWER(response_text) LIKE ?)");
                String pattern = '%' + search + '%';
                values.add(pattern);
                values.add(pattern);
                values.add(pattern);
            }
            if (filterPrompt != null && !filterPrompt.isBlank()) {
                conditions.add("LOWER(prompt) LIKE ?");
                values.add('%' + filterPrompt + '%');
            }
            if (filterResponse != null && !filterResponse.isBlank()) {
                conditions.add("LOWER(response_text) LIKE ?");
                values.add('%' + filterResponse + '%');
            }

            Timestamp startTs = null;
            Timestamp endTs = null;
            if (selectedDate != null) {
                conditions.add("created_at >= ? AND created_at < ?");
                startTs = Timestamp.valueOf(selectedDate.atStartOfDay());
                endTs = Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay());
            }

            StringBuilder sql = new StringBuilder("SELECT widget_chat_id FROM ")
                    .append(quoteIdentifier(tableName));
            if (!conditions.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            sql.append(" ORDER BY created_at DESC");

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
                        String chatId = rs.getString("widget_chat_id");
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId.trim());
                        }
                    }
                }
            }
            return chatIds;
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to fetch chat IDs", ex);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to verify widget table existence", ex);
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
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }
}