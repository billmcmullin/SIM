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
import java.util.Objects;
import java.util.stream.Collectors;

import com.sim.chatserver.config.Database;

public final class WidgetStore {

    private WidgetStore() {
        // utility class
    }

    /**
     * Explicit bootstrap call (instead of static initializer). Call this during
     * application startup.
     */
    public static void ensureTableExists() {
        try (Connection connection = Database.getConnection(); Statement statement = connection.createStatement()) {
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
        StringBuilder sql = new StringBuilder(
                "SELECT id, widget_id, display_name, created_at FROM widget_entries");
        boolean hasFilter = filter != null && !filter.isBlank();
        if (hasFilter) {
            sql.append(" WHERE widget_id ILIKE ? OR display_name ILIKE ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {

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

    public static WidgetEntry create(String widgetId, String displayName) throws SQLException {
        String normalizedWidgetId = normalizeRequired(widgetId, "widgetId");
        String normalizedDisplayName = normalizeRequired(displayName, "displayName");

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO widget_entries (widget_id, display_name) "
                + "VALUES (?, ?) RETURNING id, widget_id, display_name, created_at")) {
            statement.setString(1, normalizedWidgetId);
            statement.setString(2, normalizedDisplayName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateWidgetIdException(normalizedWidgetId, e);
            }
            throw e;
        }

        throw new IllegalStateException("Unable to persist widget entry.");
    }

    public static WidgetEntry update(int id, String widgetId, String displayName) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be > 0");
        }

        String normalizedWidgetId = normalizeRequired(widgetId, "widgetId");
        String normalizedDisplayName = normalizeRequired(displayName, "displayName");

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE widget_entries "
                + "SET widget_id = ?, display_name = ? "
                + "WHERE id = ? "
                + "RETURNING id, widget_id, display_name, created_at")) {
            statement.setString(1, normalizedWidgetId);
            statement.setString(2, normalizedDisplayName);
            statement.setInt(3, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateWidgetIdException(normalizedWidgetId, e);
            }
            throw e;
        }

        throw new IllegalStateException("No widget entry found for id " + id);
    }

    /**
     * Compatibility shim for existing callers still using save(id,...). Migrate
     * callers to create(...) / update(...), then remove this method.
     */
    @Deprecated(forRemoval = true)
    public static WidgetEntry save(Integer id, String widgetId, String displayName) throws SQLException {
        if (id == null || id <= 0) {
            return create(widgetId, displayName);
        }
        return update(id, widgetId, displayName);
    }

    public static int deleteBulk(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<Integer> validIds = ids.stream()
                .filter(Objects::nonNull)
                .filter(i -> i > 0)
                .toList();

        if (validIds.isEmpty()) {
            return 0;
        }

        String placeholders = validIds.stream().map(i -> "?").collect(Collectors.joining(","));
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM widget_entries WHERE id IN (" + placeholders + ")")) {
            int index = 1;
            for (Integer value : validIds) {
                statement.setInt(index++, value);
            }
            return statement.executeUpdate();
        }
    }

    private static WidgetEntry mapRow(ResultSet rs) throws SQLException {
        return new WidgetEntry(
                rs.getInt("id"),
                rs.getString("widget_id"),
                rs.getString("display_name"),
                toInstantRequired(rs.getTimestamp("created_at")));
    }

    private static Instant toInstantRequired(Timestamp timestamp) {
        return Objects.requireNonNull(timestamp, "created_at timestamp must not be null").toInstant();
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be provided");
        }
        return value.trim();
    }

    public static final class DuplicateWidgetIdException extends SQLException {

        public DuplicateWidgetIdException(String widgetId, Throwable cause) {
            super("Widget ID '" + widgetId + "' already exists.", cause);
        }
    }
}
