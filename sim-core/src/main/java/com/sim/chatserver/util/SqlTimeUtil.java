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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class SqlTimeUtil {

    private static final Logger LOG = Logger.getLogger(SqlTimeUtil.class.getName());
    private static final int MAX_TIMESTAMP_TEXT_LENGTH = 96;
    private static final Pattern SAFE_TIMESTAMP_TEXT = Pattern.compile("^[0-9TtZz:\\-+\\. ]{1,96}$");

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

        String raw = safeReadRawText(rs, column);
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

    private static String safeReadRawText(ResultSet rs, String column) throws SQLException {
        try {
            return sanitizeTimestampCandidate(rs.getString(column));
        } catch (SQLException primaryEx) {
            LOG.log(Level.FINER, "Timestamp text fallback to getNString for column {0}", column);
            try {
                return sanitizeTimestampCandidate(rs.getNString(column));
            } catch (SQLException secondaryEx) {
                LOG.log(Level.FINER, "Timestamp text read failed for column " + column, secondaryEx);
                return "";
            }
        }
    }

    private static String sanitizeTimestampCandidate(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.length() > MAX_TIMESTAMP_TEXT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_TIMESTAMP_TEXT_LENGTH);
        }
        if (!SAFE_TIMESTAMP_TEXT.matcher(trimmed).matches()) {
            return "";
        }
        return trimmed;
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
