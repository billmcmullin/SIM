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
            throw new IllegalStateException("Unable to ensure session_labels table exists", e);
        }
    }

    public static void saveLabel(String sessionId, String displayName, String email) throws SQLException {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required.");
        }
        String normalizedSessionId = sessionId.trim();
        String normalizedDisplayName = displayName == null ? "" : displayName.trim();
        String normalizedEmail = email == null ? "" : email.trim();

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
        String placeholders = sessionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT session_id, display_name, contact_email FROM session_labels WHERE session_id IN (" + placeholders + ")";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (String sessionId : sessionIds) {
                ps.setString(idx++, sessionId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = readDbText(rs, "session_id", 256);
                    if (id.isBlank()) {
                        continue;
                    }
                    map.put(id,
                            new SessionLabel(
                                    readDbText(rs, "display_name", 256),
                                    readDbText(rs, "contact_email", 256)));
                }
            }
        }
        return map;
    }

    private static String readDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return "";
        }
        String value = rs.getString(column);
        return sanitizeDbText(value, maxChars);
    }

    private static String sanitizeDbText(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxChars);
    }

    public static String resolveDisplayLabel(String sessionId, SessionLabel label) {
        if (sessionId == null) {
            return "";
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

    public static final class SessionLabel {

        private final String displayName;
        private final String email;

        public SessionLabel(String displayName, String email) {
            this.displayName = displayName == null ? "" : displayName;
            this.email = email == null ? "" : email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }
    }
}
