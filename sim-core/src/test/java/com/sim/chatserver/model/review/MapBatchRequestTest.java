package com.sim.chatserver.model.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;

class MapBatchRequestTest {

    @Test
    void build_appliesDefaultsAndDerivesExpectedIdsFromEntries() {
        MapBatchRequest request = MapBatchRequest.builder()
                .requestId("req-1")
                .totalSelected(3)
                .totalBatches(2)
                .batchIndex(1)
                .entries(List.of(
                        entry("CHAT-001"),
                        entry("chat-001"),
                        entry("chat-002"),
                        entry("   ")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("Summarize this")
                .build();

        assertEquals("batch-1", request.getBatchId());
        assertEquals("chat", request.getMode());
        assertEquals("", request.getSessionId());
        assertFalse(request.isReset());
        assertEquals(List.of("chat-001", "chat-002"), request.getExpectedChatIds());
        assertEquals(request.getExpectedChatIds(), request.batchChatIds());
        assertEquals(0, request.getAuthoritativeAllSelectedChatIds().size());
        assertFalse(request.hasAuthoritativeAllIds());

        var json = request.toJson();
        assertEquals("req-1", json.getString("requestId"));
        assertEquals(2, json.getJsonArray("batchChatIds").size());
        assertEquals("chat-001", json.getJsonArray("batchChatIds").getString(0));
    }

    @Test
    void build_usesProvidedExpectedIdsWhenValid() {
        MapBatchRequest request = MapBatchRequest.builder()
                .requestId("req-2")
                .totalSelected(2)
                .totalBatches(2)
                .batchIndex(2)
                .entries(List.of(entry("A"), entry("b")))
                .targetUrl("http://localhost/map")
                .mode("analysis")
                .sessionId("s-1")
                .reset(true)
                .controlledPrompt("do work")
                .expectedChatIds(List.of("B", "a", "a", " "))
                .authoritativeAllSelectedChatIds(List.of("A", "B", "A"))
                .build();

        assertEquals("analysis", request.getMode());
        assertEquals("s-1", request.getSessionId());
        assertTrue(request.isReset());
        assertEquals(List.of("b", "a"), request.getExpectedChatIds());
        assertEquals(List.of("a", "b"), request.getAuthoritativeAllSelectedChatIds());
        assertTrue(request.hasAuthoritativeAllIds());
    }

    @Test
    void build_rejectsExpectedIdsOutsideEntrySet() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> MapBatchRequest.builder()
                .requestId("req-3")
                .totalSelected(1)
                .totalBatches(1)
                .batchIndex(1)
                .entries(List.of(entry("only-id")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("prompt")
                .expectedChatIds(List.of("different-id"))
                .build());

        assertTrue(ex.getMessage().contains("expectedChatIds must be subset"));
    }

    @Test
    void build_rejectsAuthoritativeIdsLargerThanTotalSelected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> MapBatchRequest.builder()
                .requestId("req-4")
                .totalSelected(1)
                .totalBatches(1)
                .batchIndex(1)
                .entries(List.of(entry("x")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("prompt")
                .authoritativeAllSelectedChatIds(List.of("x", "y"))
                .build());

        assertTrue(ex.getMessage().contains("cannot exceed totalSelected"));
    }

    @Test
    void build_rejectsInvalidBatchIndexAndMissingRequiredFields() {
        IllegalArgumentException indexEx = assertThrows(IllegalArgumentException.class, () -> MapBatchRequest.builder()
                .requestId("req-5")
                .totalSelected(1)
                .totalBatches(1)
                .batchIndex(2)
                .entries(List.of(entry("x")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("prompt")
                .build());
        assertTrue(indexEx.getMessage().contains("batchIndex cannot be greater"));

        IllegalArgumentException requiredEx = assertThrows(IllegalArgumentException.class, () -> MapBatchRequest.builder()
                .requestId(" ")
                .totalSelected(1)
                .totalBatches(1)
                .batchIndex(1)
                .entries(List.of(entry("x")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("prompt")
                .build());
        assertTrue(requiredEx.getMessage().contains("requestId is required"));
    }

    @Test
    void collectionsAreImmutableAndToStringContainsSummary() {
        MapBatchRequest request = MapBatchRequest.builder()
                .requestId("req-6")
                .totalSelected(2)
                .totalBatches(1)
                .batchIndex(1)
                .entries(List.of(entry("id-1"), entry("id-2")))
                .targetUrl("http://localhost/map")
                .controlledPrompt("prompt")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> request.getEntries().add(entry("id-3")));
        assertThrows(UnsupportedOperationException.class, () -> request.getExpectedChatIds().add("id-3"));

        String text = request.toString();
        assertTrue(text.contains("requestId='req-6'"));
        assertTrue(text.contains("batchSize=2"));
        assertTrue(text.contains("expectedChatIds=2"));
    }

    private static SelectedEntry entry(String chatId) {
        return new SelectedEntry(chatId, "prompt", "response", "2026-01-01T00:00:00Z", "s1");
    }
}
