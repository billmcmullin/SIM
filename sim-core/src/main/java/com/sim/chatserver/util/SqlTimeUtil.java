package com.sim.chatserver.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public final class SqlTimeUtil {

    private SqlTimeUtil() {
    }

    public static Timestamp safeTimestamp(ResultSet rs, String column) throws SQLException {
        Object rawValue = rs.getObject(column);
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Timestamp ts) {
            return ts;
        }
        if (rawValue instanceof java.sql.Date date) {
            return new Timestamp(date.getTime());
        }

        String raw = String.valueOf(rawValue).trim();
        if (raw.isBlank()) {
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
            return Timestamp.valueOf(raw.replace('T', ' ').replace("Z", "")); // fallback
        } catch (IllegalArgumentException ignore) {
            throw new SQLException("Unable to parse timestamp for column " + column);
        }
    }
}
