package com.sim.chatserver.widget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sim.chatserver.config.Database;

public final class WidgetStore {

    static {
        ensureTable();
    }

    private WidgetStore() {
        // utility class
    }

    private static void ensureTable() {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS widget_entries (
                    id SERIAL PRIMARY KEY,
                    widget_id VARCHAR(128) NOT NULL UNIQUE,
                    display_name VARCHAR(256) NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
                )
                """);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ensure widget_entries table exists", e);
        }
    }

    public static List<WidgetEntry> list(String filter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, widget_id, display_name, created_at FROM widget_entries");
        boolean hasFilter = filter != null && !filter.isBlank();
        if (hasFilter) {
            sql.append(" WHERE widget_id ILIKE ? OR display_name ILIKE ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            if (hasFilter) {
                String pattern = "%" + filter.trim() + "%";
                statement.setString(1, pattern);
                statement.setString(2, pattern);
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<WidgetEntry> widgets = new ArrayList<>();
                while (rs.next()) {
                    widgets.add(mapRow(rs));
                }
                return widgets;
            }
        }
    }

    public static WidgetEntry save(Integer id, String widgetId, String displayName) throws SQLException {
        if (widgetId == null || widgetId.isBlank() || displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("widgetId and displayName must be provided");
        }
        try {
            if (id == null || id <= 0) {
                return insertWidget(widgetId.trim(), displayName.trim());
            }
            return updateWidget(id, widgetId.trim(), displayName.trim());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateWidgetIdException(widgetId.trim(), e);
            }
            throw e;
        }
    }

    private static WidgetEntry insertWidget(String widgetId, String displayName) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO widget_entries (widget_id, display_name) VALUES (?, ?) RETURNING id, created_at")) {
            statement.setString(1, widgetId);
            statement.setString(2, displayName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new WidgetEntry(
                            rs.getInt("id"),
                            widgetId,
                            displayName,
                            toInstant(rs.getTimestamp("created_at")));
                }
            }
        }
        throw new IllegalStateException("Unable to persist widget entry.");
    }

    private static WidgetEntry updateWidget(int id, String widgetId, String displayName) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE widget_entries SET widget_id = ?, display_name = ? WHERE id = ?")) {
            statement.setString(1, widgetId);
            statement.setString(2, displayName);
            statement.setInt(3, id);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("No widget entry found for id " + id);
            }
        }
        return findById(id).orElseThrow(() -> new IllegalStateException("Widget entry disappeared after update."));
    }

    public static int deleteBulk(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM widget_entries WHERE id IN (" + placeholders + ")")) {
            int index = 1;
            for (Integer id : ids) {
                statement.setInt(index++, id);
            }
            return statement.executeUpdate();
        }
    }

    private static Optional<WidgetEntry> findById(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, widget_id, display_name, created_at FROM widget_entries WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private static WidgetEntry mapRow(ResultSet rs) throws SQLException {
        return new WidgetEntry(
                rs.getInt("id"),
                rs.getString("widget_id"),
                rs.getString("display_name"),
                toInstant(rs.getTimestamp("created_at")));
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : Instant.now();
    }

    public static final class DuplicateWidgetIdException extends SQLException {
        public DuplicateWidgetIdException(String widgetId, Throwable cause) {
            super("Widget ID '" + widgetId + "' already exists.", cause);
        }
    }
}
