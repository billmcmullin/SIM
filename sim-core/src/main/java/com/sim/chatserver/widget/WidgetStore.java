package com.sim.chatserver.widget;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.Database;

public final class WidgetStore {

    private static final Logger log = Logger.getLogger(WidgetStore.class.getName());

    private static final String SQL_STATE_UNDEFINED_TABLE = "42P01";
    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS widget_entries (
                id SERIAL PRIMARY KEY,
                widget_id VARCHAR(128) NOT NULL UNIQUE,
                display_name VARCHAR(256) NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
            )
            """;

    private WidgetStore() {
        // utility class
    }

    /**
     * Explicit bootstrap call (instead of static initializer). Call this during
     * application startup.
     */
    private static void ensureTableExists() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ensure widget_entries table exists", e);
        }
    }

    public static List<WidgetEntry> list(String filter) throws SQLException {
        ensureTableExists();

        StringBuilder sql = new StringBuilder(
                "SELECT id, widget_id, display_name, created_at FROM widget_entries");
        String safeFilterInput = filter == null ? "" : filter.trim();
        boolean hasFilter = !safeFilterInput.isBlank();
        if (hasFilter) {
            sql.append(" WHERE widget_id ILIKE ? OR display_name ILIKE ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            if (hasFilter) {
                String pattern = '%' + safeFilterInput + '%';
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
        } catch (SQLException e) {
            if (isUndefinedTable(e)) {
                ensureTableExists();
                return list(filter); // one-shot retry through fresh call path
            }
            throw e;
        }
    }

    private static WidgetEntry create(String widgetId, String displayName) throws SQLException {
        ensureTableExists();

        String normalizedWidgetId = normalizeRequired(widgetId, "widgetId");
        String normalizedDisplayName = normalizeRequired(displayName, "displayName");

        try {
            return doCreate(normalizedWidgetId, normalizedDisplayName);
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new DuplicateWidgetIdException(normalizedWidgetId, e);
            }
            if (isUndefinedTable(e)) {
                ensureTableExists();
                try {
                    return doCreate(normalizedWidgetId, normalizedDisplayName);
                } catch (SQLException retryEx) {
                    if (isUniqueViolation(retryEx)) {
                        throw new DuplicateWidgetIdException(normalizedWidgetId, retryEx);
                    }
                    throw retryEx;
                }
            }
            throw e;
        }
    }

    private static WidgetEntry update(int id, String widgetId, String displayName) throws SQLException {
        ensureTableExists();

        if (id <= 0) {
            throw new IllegalArgumentException("id must be > 0");
        }

        String normalizedWidgetId = normalizeRequired(widgetId, "widgetId");
        String normalizedDisplayName = normalizeRequired(displayName, "displayName");

        try {
            return doUpdate(id, normalizedWidgetId, normalizedDisplayName);
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new DuplicateWidgetIdException(normalizedWidgetId, e);
            }
            if (isUndefinedTable(e)) {
                ensureTableExists();
                try {
                    return doUpdate(id, normalizedWidgetId, normalizedDisplayName);
                } catch (SQLException retryEx) {
                    if (isUniqueViolation(retryEx)) {
                        throw new DuplicateWidgetIdException(normalizedWidgetId, retryEx);
                    }
                    throw retryEx;
                }
            }
            throw e;
        }
    }

    /**
     * Compatibility shim for existing callers still using save(id,...). Migrate
     * callers to create(...) / update(...), then remove this method.
     */
    @Deprecated(forRemoval = true)
    public static WidgetEntry save(Integer id, String widgetId, String displayName) throws SQLException {
        ensureTableExists();

        int resolvedId = id == null ? 0 : id.intValue();
        if (resolvedId <= 0) {
            return create(widgetId, displayName);
        }
        return update(resolvedId, widgetId, displayName);
    }

    public static int deleteBulk(Collection<Integer> ids) throws SQLException {
        ensureTableExists();

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int[] validIds = ids.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .filter(i -> i > 0)
                .toArray();

        if (validIds.length == 0) {
            return 0;
        }

        String placeholders = java.util.Arrays.stream(validIds).mapToObj(i -> "?").collect(Collectors.joining(","));
        String sql = "DELETE FROM widget_entries WHERE id IN (" + placeholders + ')';

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (int value : validIds) {
                statement.setInt(index++, value);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            if (isUndefinedTable(e)) {
                ensureTableExists();
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                    int index = 1;
                    for (int value : validIds) {
                        statement.setInt(index++, value);
                    }
                    return statement.executeUpdate();
                }
            }
            throw e;
        }
    }

    private static WidgetEntry doCreate(String widgetId, String displayName) throws SQLException {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO widget_entries (widget_id, display_name) "
                + "VALUES (?, ?) RETURNING id, widget_id, display_name, created_at")) {
            statement.setString(1, widgetId);
            statement.setString(2, displayName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new IllegalStateException("Unable to persist widget entry.");
    }

    private static WidgetEntry doUpdate(int id, String widgetId, String displayName) throws SQLException {
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE widget_entries "
                + "SET widget_id = ?, display_name = ? "
                + "WHERE id = ? "
                + "RETURNING id, widget_id, display_name, created_at")) {
            statement.setString(1, widgetId);
            statement.setString(2, displayName);
            statement.setInt(3, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new IllegalStateException("No widget entry found for id " + id);
    }

    private static boolean isUndefinedTable(SQLException e) {
        return SQL_STATE_UNDEFINED_TABLE.equals(e.getSQLState());
    }

    private static boolean isUniqueViolation(SQLException e) {
        return SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState());
    }

    private static WidgetEntry mapRow(ResultSet rs) throws SQLException {
        return new WidgetEntry(
                readNonNegativeInt(rs, "id"),
                readSanitizedDbText(rs, "widget_id", 128),
                readSanitizedDbText(rs, "display_name", 256),
                readCreatedAt(rs));
    }

    private static int readNonNegativeInt(ResultSet rs, String column) throws SQLException {
        String text = readSanitizedDbText(rs, column, 32);
        if (text.isBlank() || !text.matches("^-?\\d+$")) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Unable to parse integer value for column " + column, ex);
            return 0;
        }
    }

    private static String readSanitizedDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        try (Reader reader = rs.getCharacterStream(column)) {
            if (reader == null) {
                return "";
            }
            char[] buffer = new char[512];
            StringBuilder value = new StringBuilder(Math.max(64, Math.min(maxChars, 1024)));
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
            return sanitizeDbText(value.toString(), maxChars);
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, "Text read failed for column " + column, ex);
            return "";
        }
    }

    private static Instant readCreatedAt(ResultSet rs) throws SQLException {
        try {
            Timestamp typedTimestamp = rs.getTimestamp("created_at");
            if (typedTimestamp != null) {
                return typedTimestamp.toInstant();
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Typed timestamp read failed for created_at", ex);
        }

        String text = "";
        try {
            text = sanitizeDbText(rs.getString("created_at"), 128);
        } catch (SQLException ex) {
            log.log(Level.FINE, "Text fallback read failed for created_at", ex);
        }

        if (!text.isBlank()) {
            try {
                return Instant.parse(text);
            } catch (DateTimeException e) {
                log.log(Level.FINE, "Instant parse fallback for created_at", e);
                try {
                    return OffsetDateTime.parse(text).toInstant();
                } catch (DateTimeException ex) {
                    log.log(Level.FINE, "OffsetDateTime parse fallback for created_at", ex);
                }
                try {
                    return Timestamp.valueOf(text.replace('T', ' ')).toInstant();
                } catch (IllegalArgumentException ex) {
                    log.log(Level.FINE, "Timestamp fallback parse failed for created_at", ex);
                }
            }
        }

        // Keep widget metadata flows alive even if legacy/bad created_at text is present.
        return Instant.EPOCH;
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be provided");
        }
        return value.trim();
    }

    private static String sanitizeDbText(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        String trimmed = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxChars);
    }

    public static final class DuplicateWidgetIdException extends SQLException {

        private DuplicateWidgetIdException(String widgetId, Throwable cause) {
            super("Widget ID '" + widgetId + "' already exists.", cause);
        }
    }
}
