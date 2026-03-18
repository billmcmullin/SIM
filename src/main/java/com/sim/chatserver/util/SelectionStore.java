package com.sim.chatserver.util;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory selection store.
 *
 * NOTE: - This is process-local (not shared across clustered nodes). - Data is
 * cleared on app restart. - Good for immediate functionality and development.
 */
public final class SelectionStore {

    private static final Map<String, SelectionRecord> STORE = new ConcurrentHashMap<>();
    private static final long TTL_SECONDS = 60L * 60L * 24L; // 24 hours

    private SelectionStore() {
    }

    public static void saveSelection(String selectionId, List<String> selectedChatIds) throws SQLException {
        if (selectionId == null || selectionId.isBlank()) {
            throw new SQLException("selectionId required");
        }
        if (selectedChatIds == null || selectedChatIds.isEmpty()) {
            throw new SQLException("selectedChatIds required");
        }

        List<String> copy = new ArrayList<>();
        for (String id : selectedChatIds) {
            if (id != null && !id.isBlank()) {
                copy.add(id.trim());
            }
        }
        if (copy.isEmpty()) {
            throw new SQLException("No valid chat IDs to save");
        }

        STORE.put(selectionId, new SelectionRecord(copy, Instant.now()));
        cleanupExpired();
    }

    public static List<String> getSelection(String selectionId) {
        if (selectionId == null || selectionId.isBlank()) {
            return List.of();
        }

        SelectionRecord record = STORE.get(selectionId);
        if (record == null) {
            return List.of();
        }

        if (isExpired(record.createdAt)) {
            STORE.remove(selectionId);
            return List.of();
        }

        return Collections.unmodifiableList(record.chatIds);
    }

    public static boolean deleteSelection(String selectionId) {
        if (selectionId == null || selectionId.isBlank()) {
            return false;
        }
        return STORE.remove(selectionId) != null;
    }

    private static boolean isExpired(Instant createdAt) {
        return createdAt == null || Instant.now().isAfter(createdAt.plusSeconds(TTL_SECONDS));
    }

    private static void cleanupExpired() {
        for (Map.Entry<String, SelectionRecord> e : STORE.entrySet()) {
            if (isExpired(e.getValue().createdAt)) {
                STORE.remove(e.getKey());
            }
        }
    }

    private static final class SelectionRecord {

        private final List<String> chatIds;
        private final Instant createdAt;

        private SelectionRecord(List<String> chatIds, Instant createdAt) {
            this.chatIds = chatIds;
            this.createdAt = createdAt;
        }
    }
}
