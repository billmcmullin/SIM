package com.sim.chatserver.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;

public final class SqlTimeUtil {

    private SqlTimeUtil() {
    }

    public static Timestamp safeTimestamp(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getTimestamp(column);
        } catch (SQLException ex) {
            String raw = rs.getString(column);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            raw = raw.trim();

            try {
                return Timestamp.from(Instant.parse(raw)); // ISO-8601 Z
            } catch (Exception ignore) {
            }

            try {
                return Timestamp.from(OffsetDateTime.parse(raw).toInstant()); // offset formats
            } catch (Exception ignore) {
            }

            try {
                return Timestamp.valueOf(raw.replace('T', ' ').replace("Z", "")); // fallback
            } catch (Exception ignore) {
            }

            throw ex;
        }
    }
}
