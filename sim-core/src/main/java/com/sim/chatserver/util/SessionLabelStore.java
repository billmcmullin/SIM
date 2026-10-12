package com.sim.chatserver.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.sim.chatserver.config.Database;

public final class SessionLabelStore {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS session_labels (
                session_id TEXT PRIMARY KEY,
                display_name VARCHAR(256) NOT NULL,
                contact_email VARCHAR(256),
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
            )
            """;
    private static final String ADD_CONTACT_EMAIL_SQL = """
            ALTER TABLE session_labels
            ADD COLUMN IF NOT EXISTS contact_email VARCHAR(256)
            """;
    private static final String MSG_ENSURE_TABLE_FAILED = "Unable to ensure session_labels table exists";
    private static final String MSG_SESSION_ID_REQUIRED = "sessionId is required.";
    private static final String EMPTY = "";
    private static final String SQL_PLACEHOLDER = "?";
    private static final String SQL_JOIN_DELIMITER = ",";
    private static final String SQL_SELECT_SESSION_LABELS_PREFIX = "SELECT session_id, display_name, contact_email FROM session_labels WHERE session_id IN (";
    private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
    private static final String COL_DISPLAY_NAME = "display_name";
    private static final String COL_CONTACT_EMAIL = "contact_email";
    private static final String OWASP_LOGGER = com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP;
    private static final String HANDLED_EXCEPTION = com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION;

    private SessionLabelStore() {
        // utility class
    }

    static {
        ensureTable();
    }

    private static void ensureTable() {
        try (Connection conn = Database.getConnection();
             PreparedStatement create = conn.prepareStatement(CREATE_TABLE_SQL);
             PreparedStatement addContact = conn.prepareStatement(ADD_CONTACT_EMAIL_SQL)) {
            create.executeUpdate();
            addContact.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_ENSURE_TABLE_FAILED, e);
        }
    }

    public static void saveLabel(String sessionId, String displayName, String email) throws SQLException {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(MSG_SESSION_ID_REQUIRED);
        }
        String normalizedSessionId = sessionId.trim();
        String normalizedDisplayName = displayName == null ? EMPTY : displayName.trim();
        String normalizedEmail = email == null ? EMPTY : email.trim();

        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO session_labels (session_id, display_name, contact_email) VALUES (?, ?, ?)
                     ON CONFLICT (session_id) DO UPDATE SET
                         display_name = EXCLUDED.display_name,
                         contact_email = EXCLUDED.contact_email,
                         updated_at = NOW()
                     """)) {
            ps.setString(1, normalizedSessionId);
            ps.setString(2, normalizedDisplayName);
            ps.setString(3, normalizedEmail);
            ps.executeUpdate();
        }
    }

    public static Map<String, SessionLabel> mapDisplayNames(Collection<String> sessionIds) throws SQLException {
        Map<String, SessionLabel> map = new LinkedHashMap<>();
        if (sessionIds == null || sessionIds.isEmpty()) {
            return map;
        }
        String placeholders = sessionIds.stream().map(id -> SQL_PLACEHOLDER).collect(Collectors.joining(SQL_JOIN_DELIMITER));
        String sql = SQL_SELECT_SESSION_LABELS_PREFIX + placeholders + ')';
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (String sessionId : sessionIds) {
                ps.setString(idx++, sessionId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = readDbText(rs, COL_SESSION_ID, 256);
                    if (id.isBlank()) {
                        continue;
                    }
                    map.put(id,
                            new SessionLabel(
                                    readDbText(rs, COL_DISPLAY_NAME, 256),
                                    readDbText(rs, COL_CONTACT_EMAIL, 256)));
                }
            }
        }
        return map;
    }

    private static String readDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return EMPTY;
        }
        Object value = readRawDbObject(rs, column);
        if (value == null) {
            return EMPTY;
        }
        if (value instanceof byte[] bytes) {
            return sanitizeDbText(new String(bytes, java.nio.charset.StandardCharsets.UTF_8), maxChars);
        }
        return sanitizeDbText(String.valueOf(value), maxChars);
    }

    private static Object readRawDbObject(ResultSet rs, String column) {
        try {
            return rs.getObject(column);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(OWASP_LOGGER).log(java.util.logging.Level.FINE, HANDLED_EXCEPTION, ex);
            return null;
        }
    }

    private static String sanitizeDbText(String value, int maxChars) {
        if (value == null) {
            return EMPTY;
        }
        String trimmed = value.trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return new String(trimmed.toCharArray(), 0, maxChars);
    }

    public static String resolveDisplayLabel(String sessionId, SessionLabel label) {
        if (sessionId == null) {
            return EMPTY;
        }
        if (label == null) {
            return sessionId;
        }
        String name = label.getDisplayName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        String email = label.getEmail();
        if (email != null && !email.isBlank()) {
            return email;
        }
        return sessionId;
    }
}


