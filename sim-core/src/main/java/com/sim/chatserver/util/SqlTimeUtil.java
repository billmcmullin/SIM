package com.sim.chatserver.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

public final class SqlTimeUtil {

    private static final DateTimeFormatter FLEX_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart().appendOffset("+HH:MM", "Z").optionalEnd()
            .optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
            .optionalStart().appendOffset("+HH", "Z").optionalEnd()
            .toFormatter();

    private SqlTimeUtil() {
    }

    /**
     * Reads a timestamp defensively from mixed legacy/imported DB values.
     *
     * IMPORTANT: Do not throw when a single value is unparsable. Dashboard loaders
     * aggregate many rows; one bad timestamp should be skipped (null) rather than
     * failing term/session/trend widgets with SQLExceptions.
     */
    public static Timestamp safeTimestamp(ResultSet rs, String column) throws SQLException {
        try {
            Timestamp typedTimestamp = rs.getTimestamp(column);
            if (typedTimestamp != null) {
                return typedTimestamp;
            }
        } catch (SQLException ignore) {
            // Fall back to tolerant parsing for legacy/imported timestamp values.
        }

        Object rawValue = rs.getObject(column);
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Timestamp ts) {
            return ts;
        }
        if (rawValue instanceof Instant instant) {
            return Timestamp.from(instant);
        }
        if (rawValue instanceof OffsetDateTime offsetDateTime) {
            return Timestamp.from(offsetDateTime.toInstant());
        }
        if (rawValue instanceof ZonedDateTime zonedDateTime) {
            return Timestamp.from(zonedDateTime.toInstant());
        }
        if (rawValue instanceof LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }
        if (rawValue instanceof LocalDate localDate) {
            return Timestamp.valueOf(localDate.atStartOfDay());
        }
        if (rawValue instanceof java.sql.Date date) {
            return new Timestamp(date.getTime());
        }

        String raw = String.valueOf(rawValue).trim();
        if (raw.isBlank()) {
            return null;
        }

        Timestamp parsed = parseTimestampString(raw);
        if (parsed != null) {
            return parsed;
        }

        String normalized = normalizeTimestampText(raw);
        if (!normalized.equals(raw)) {
            parsed = parseTimestampString(normalized);
            if (parsed != null) {
                return parsed;
            }
        }

        // Keep this null-return behavior: returning null prevents a single malformed
        // timestamp value from breaking dashboard computations after deployment/import.
        return null;
    }

    private static Timestamp parseTimestampString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Timestamp.from(Instant.parse(raw)); // ISO-8601 Z
        } catch (DateTimeParseException ignore) {
            // Fall through to next supported format.
        }

        try {
            return Timestamp.from(OffsetDateTime.parse(raw).toInstant()); // offset formats
        } catch (DateTimeParseException ignore) {
            // Fall through to SQL timestamp fallback.
        }

        try {
            return Timestamp.from(ZonedDateTime.parse(raw).toInstant());
        } catch (DateTimeParseException ignore) {
            // Fall through to flexible local/offset parser.
        }

        try {
            TemporalAccessor parsed = FLEX_TIMESTAMP_FORMATTER.parseBest(raw, OffsetDateTime::from, LocalDateTime::from);
            if (parsed instanceof OffsetDateTime offsetDateTime) {
                return Timestamp.from(offsetDateTime.toInstant());
            }
            if (parsed instanceof LocalDateTime localDateTime) {
                return Timestamp.valueOf(localDateTime);
            }
        } catch (DateTimeParseException ignore) {
            // Fall through to SQL timestamp fallback.
        }

        try {
            return Timestamp.valueOf(raw.replace('T', ' ').replace("Z", "")); // fallback
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    private static String normalizeTimestampText(String raw) {
        String normalized = raw.trim();
        if (!normalized.contains("T") && normalized.contains(" ")) {
            normalized = normalized.replaceFirst(" ", "T");
        }
        return normalized;
    }
}
