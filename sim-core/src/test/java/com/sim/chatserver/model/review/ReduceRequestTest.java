package com.sim.chatserver.model.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReduceRequestTest {

    @Test
    void build_computesMissingIdsAndCoverageDeterministically() {
        ReduceRequest request = baseBuilder()
                .allSelectedChatIds(List.of("A", "b", "A"))
                .usedChatIds(List.of("a"))
                .build();

        assertEquals(List.of("a", "b"), request.getAllSelectedChatIds());
        assertEquals(List.of("a"), request.getUsedChatIds());
        assertEquals(List.of("b"), request.getMissingChatIds());
        assertFalse(request.isCoverageComplete());
        assertFalse(request.hasFailures());
        assertTrue(request.hasMapOutputs());
    }

    @Test
    void build_ignoresMissingOverrideAndCoverageOverride() {
        ReduceRequest request = baseBuilder()
                .allSelectedChatIds(List.of("A", "B"))
                .usedChatIds(List.of("a", "b"))
                .missingChatIds(List.of("should", "be", "ignored"))
                .coverageComplete(false)
                .build();

        assertEquals(List.of(), request.getMissingChatIds());
        assertTrue(request.isCoverageComplete());
    }

    @Test
    void build_normalizesFailedBatchesAndMapOutputs() {
        ReduceRequest request = baseBuilder()
                .mapOutputs(Arrays.asList("first", null, "second"))
                .failedBatchIndexes(Arrays.asList(2, 2, -3, 0, 1, null))
                .failedBatchReasons(Arrays.asList("timeout", null, "invalid data"))
                .allSelectedChatIds(List.of("A"))
                .usedChatIds(List.of())
                .build();

        assertEquals(List.of("first", "second"), request.getMapOutputs());
        assertEquals(List.of(2, 1), request.getFailedBatchIndexes());
        assertEquals(List.of("timeout", "invalid data"), request.getFailedBatchReasons());
        assertEquals(2, request.getMapOutputsCount());
        assertEquals(2, request.getFailedBatchCount());
        assertTrue(request.hasFailures());
    }

    @Test
    void build_rejectsInvalidRequiredFieldsAndNegativeTotals() {
        IllegalArgumentException requiredEx = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .requestId(" ")
                .build());
        assertTrue(requiredEx.getMessage().contains("requestId is required"));

        IllegalArgumentException totalSelectedEx = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .totalSelected(-1)
                .build());
        assertTrue(totalSelectedEx.getMessage().contains("totalSelected must be >= 0"));

        IllegalArgumentException totalBatchesEx = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .totalBatches(-1)
                .build());
        assertTrue(totalBatchesEx.getMessage().contains("totalBatches must be >= 0"));
    }

    @Test
    void build_rejectsTotalSelectedLessThanAllSelectedSize() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> baseBuilder()
                .totalSelected(1)
                .allSelectedChatIds(List.of("a", "b"))
                .usedChatIds(List.of("a"))
                .build());

        assertTrue(ex.getMessage().contains("cannot be less than allSelectedChatIds.size"));
    }

    @Test
    void toJsonAndToStringExposeComputedValues() {
        ReduceRequest request = baseBuilder()
                .requestId("reduce-1")
                .mode("analysis")
                .sessionId("session-9")
                .reset(true)
                .allSelectedChatIds(List.of("A", "B"))
                .usedChatIds(List.of("a"))
                .build();

        var json = request.toJson();
        assertEquals("reduce-1", json.getString("requestId"));
        assertEquals("analysis", json.getString("mode"));
        assertEquals("session-9", json.getString("sessionId"));
        assertEquals(1, json.getJsonArray("missingChatIds").size());
        assertFalse(json.getBoolean("coverageComplete"));

        String text = request.toString();
        assertTrue(text.contains("requestId='reduce-1'"));
        assertTrue(text.contains("coverageComplete=false"));
        assertTrue(text.contains("missingChatIds=1"));
    }

    @Test
    void exposedCollectionsAreImmutable() {
        ReduceRequest request = baseBuilder()
                .allSelectedChatIds(List.of("a"))
                .usedChatIds(List.of())
                .build();

        assertThrows(UnsupportedOperationException.class, () -> request.getAllSelectedChatIds().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> request.getUsedChatIds().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> request.getMissingChatIds().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> request.getMapOutputs().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> request.getFailedBatchIndexes().add(3));
    }

    private static ReduceRequest.Builder baseBuilder() {
        return ReduceRequest.builder()
                .requestId("req")
                .targetUrl("http://localhost/reduce")
                .mode("")
                .sessionId(null)
                .reset(false)
                .controlledPrompt("summarize")
                .totalSelected(2)
                .totalBatches(1)
                .mapOutputs(List.of("output"))
                .failedBatchIndexes(List.of())
                .failedBatchReasons(List.of())
                .allSelectedChatIds(List.of())
                .usedChatIds(List.of());
    }
}
