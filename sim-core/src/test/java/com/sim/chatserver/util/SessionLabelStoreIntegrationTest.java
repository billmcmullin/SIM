package com.sim.chatserver.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.Database;
import com.sim.chatserver.util.SessionLabelStore.SessionLabel;

class SessionLabelStoreIntegrationTest {

    private static final String TEST_PREFIX = "it-session-label-";

    @BeforeEach
    void requireDbEnvForIntegration() {
        Assumptions.assumeTrue(hasRequiredDbEnv(),
                "Skipping SessionLabelStore integration tests because DB_* env vars are not fully set.");
    }

    @AfterEach
    void cleanupInsertedRows() throws SQLException {
        if (!hasRequiredDbEnv()) {
            return;
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM session_labels WHERE session_id LIKE ?")) {
            ps.setString(1, TEST_PREFIX + "%");
            ps.executeUpdate();
        }
    }

    @Test
    void saveLabel_andMapDisplayNames_roundTrip() throws Exception {
        String sessionId = testSessionId();

        SessionLabelStore.saveLabel(sessionId, "Display One", "one@example.com");

        Map<String, SessionLabel> labels = SessionLabelStore.mapDisplayNames(List.of(sessionId, "missing"));

        assertTrue(labels.containsKey(sessionId), "Expected session id to be present in mapping.");
        assertEquals("Display One", labels.get(sessionId).getDisplayName());
        assertEquals("one@example.com", labels.get(sessionId).getEmail());
        assertEquals("Display One", SessionLabelStore.resolveDisplayLabel(sessionId, labels.get(sessionId)));
    }

    @Test
    void saveLabel_updatesExistingRow() throws Exception {
        String sessionId = testSessionId();

        SessionLabelStore.saveLabel(sessionId, "First Name", "first@example.com");
        SessionLabelStore.saveLabel(sessionId, "Second Name", "second@example.com");

        Map<String, SessionLabel> labels = SessionLabelStore.mapDisplayNames(List.of(sessionId));
        assertEquals("Second Name", labels.get(sessionId).getDisplayName());
        assertEquals("second@example.com", labels.get(sessionId).getEmail());
    }

    @Test
    void saveLabel_rejectsBlankSessionId() {
        assertThrows(IllegalArgumentException.class,
                () -> SessionLabelStore.saveLabel("  ", "Display", "user@example.com"));
    }

    private static String testSessionId() {
        return TEST_PREFIX + UUID.randomUUID();
    }

    private static boolean hasRequiredDbEnv() {
        return hasText(System.getenv("DB_HOST"))
                && hasText(System.getenv("DB_PORT"))
                && hasText(System.getenv("DB_NAME"))
                && hasText(System.getenv("DB_USER"))
                && hasText(System.getenv("DB_PASSWORD"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
