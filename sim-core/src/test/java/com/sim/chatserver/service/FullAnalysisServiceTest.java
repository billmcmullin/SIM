package com.sim.chatserver.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.json.Json;
import jakarta.json.JsonObject;

class FullAnalysisServiceTest {
    private WorkflowRecordStore records;
    private final Map<String, WorkflowRecordStore.StoredRecord> data = new ConcurrentHashMap<>();
    private final AtomicLong clock = new AtomicLong(1_800_000_000_000L);
    private WorkspaceClient client;
    private final FullAnalysisService.Target target = new FullAnalysisService.Target("https://example.test/api/v1/workspace/test/chat", "test-token");

    @BeforeEach
    void setup() throws Exception {
        records = mock(WorkflowRecordStore.class);
        when(records.currentTimeMillis()).thenAnswer(invocation -> clock.get());
        when(records.tryTaskLock(anyString())).thenAnswer(invocation -> DatabaseTaskLock.localOnly());
        when(records.find(anyString(), anyString())).thenAnswer(invocation -> data.get(key(invocation.getArgument(0), invocation.getArgument(1))));
        doAnswer(invocation -> {
            String key = key(invocation.getArgument(0), invocation.getArgument(1));
            if (data.putIfAbsent(key, new WorkflowRecordStore.StoredRecord(invocation.getArgument(2), clock.get())) != null) {
                throw new IllegalStateException("Duplicate record");
            }
            return null;
        }).when(records).insert(anyString(), anyString(), anyString(), anyLong());
        when(records.replace(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong())).thenAnswer(invocation -> {
            String key = key(invocation.getArgument(0), invocation.getArgument(1));
            var previous = data.get(key);
            if (previous == null || !previous.payload().equals(invocation.getArgument(2))) {
                return false;
            }
            data.put(key, new WorkflowRecordStore.StoredRecord(invocation.getArgument(3), clock.get()));
            return true;
        });
        doAnswer(invocation -> data.remove(key(invocation.getArgument(0), invocation.getArgument(1))))
                .when(records).delete(anyString(), anyString());
        when(records.list(anyString())).thenAnswer(invocation -> {
            String prefix = invocation.getArgument(0) + "/";
            Map<String, WorkflowRecordStore.StoredRecord> result = new LinkedHashMap<>();
            data.forEach((key, value) -> { if (key.startsWith(prefix)) result.put(key.substring(prefix.length()), value); });
            return result;
        });
        client = spy(new WorkspaceClient(mock(HttpClient.class), 0, Duration.ofSeconds(5)));
        doAnswer(invocation -> result(invocation.getArgument(2))).when(client)
                .sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void segmentationPreservesEveryCharacterAndStoresFullSnapshot() throws Exception {
        String question = "Full question \u00e9 \ud83d\ude00\n".repeat(80);
        String answer = "Complete answer with detailed steps and closing guidance.\n".repeat(100);
        try (var service = service(List.of(new FullAnalysisService.Chat("chat-1", question, answer)))) {
            String id = service.start("admin", LocalDate.of(2026, 11, 10), FullAnalysisService.Limits.defaults());
            service.advance(id);
            JsonObject status = service.status(id, "admin");
            assertTrue(status.getInt("mapSteps") > 1);
            StringBuilder reconstructed = new StringBuilder();
            for (int offset = 0; offset < status.getInt("mapSteps"); offset += 5) {
                for (var value : service.page(id, "admin", offset, 5).getJsonArray("items")) {
                    reconstructed.append(value.asJsonObject().getString("text"));
                }
            }
            assertEquals("Question:\n" + question + "\nAnswer:\n" + answer, reconstructed.toString());
            assertTrue(data.values().stream().anyMatch(record -> record.payload().contains("Full question")));
            verify(client, never()).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void pacingQuotasAndManualContinuationDoNotResendCompletedChats() throws Exception {
        var limits = new FullAnalysisService.Limits(4096, 2048, 10, 1, 15);
        try (var service = service(List.of(new FullAnalysisService.Chat("first", "First question", "First answer"),
                new FullAnalysisService.Chat("second", "Second question", "Second answer")))) {
            String id = service.start("admin", LocalDate.now(), limits);
            service.advance(id);
            service.advance(id);
            assertEquals(1, service.status(id, "admin").getInt("completedChats"));
            service.advance(id);
            verify(client, times(1)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
            clock.addAndGet(10000);
            service.advance(id);
            assertEquals("PAUSED", service.status(id, "admin").getString("state"));
            service.control(id, "admin", "resume", false);
            service.advance(id);
            assertEquals(2, service.status(id, "admin").getInt("completedChats"));
                verify(records, atLeastOnce()).replace(eq("full-analysis-step"), anyString(), anyString(), anyString(),
                    longThat(retention -> retention < java.util.concurrent.TimeUnit.DAYS.toMillis(30)), anyLong());
            verify(client, times(2)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
            for (int tick = 0; tick < 30 && !"COMPLETED".equals(service.status(id, "admin").getString("state")); tick++) {
                clock.addAndGet(10000);
                service.advance(id);
                if ("PAUSED".equals(service.status(id, "admin").getString("state"))) {
                    service.control(id, "admin", "resume", false);
                }
            }
            assertEquals("COMPLETED", service.status(id, "admin").getString("state"));
            assertEquals(2, service.status(id, "admin").getInt("completedChats"));
            assertTrue(service.status(id, "admin").containsKey("overview"));
        }
    }

    @Test
    void timeoutPausesWithoutReplayAndRequiresExplicitUncertainAcknowledgement() throws Exception {
        doThrow(new HttpTimeoutException("timeout")).when(client)
                .sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            service.advance(id);
            assertEquals("PAUSED", service.status(id, "admin").getString("state"));
            assertTrue(service.status(id, "admin").getBoolean("uncertain"));
            service.advance(id);
            assertThrows(IllegalArgumentException.class, () -> service.control(id, "admin", "resume", false));
            verify(client, times(1)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
            service.control(id, "admin", "cancel", false);
            assertEquals("CANCELLED", service.status(id, "admin").getString("state"));
        }
    }

    @Test
    void restartDetectsInFlightUncertaintyAndDoesNotReplay() throws Exception {
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            JsonObject original = parse(data.get(key("full-analysis-job", id)).payload());
            data.put(key("full-analysis-job", id), new WorkflowRecordStore.StoredRecord(
                    Json.createObjectBuilder(original).add("state", "SENDING").build().toString(), clock.get()));
            try (var restarted = service(List.of())) {
                assertEquals("PAUSED", restarted.status(id, "admin").getString("state"));
                assertTrue(restarted.status(id, "admin").getBoolean("uncertain"));
                assertThrows(IllegalArgumentException.class, () -> restarted.control(id, "admin", "resume", false));
                verify(client, never()).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
            }
        }
    }

    @Test
    void ownershipEmptyDataAndCancellationAreSafe() throws Exception {
        try (var service = service(List.of())) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            assertThrows(SecurityException.class, () -> service.status(id, "other-user"));
            service.advance(id);
            assertEquals("COMPLETED", service.status(id, "admin").getString("state"));
            assertEquals(0, service.status(id, "admin").getInt("requests"));
            String cancelled = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.control(cancelled, "admin", "cancel", false);
            service.advance(cancelled);
            assertEquals("CANCELLED", service.status(cancelled, "admin").getString("state"));
            verify(client, never()).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void detailedResultsAreNotCutAtLegacySummaryFieldLimits() throws Exception {
        String longOverview = "Detailed finding. ".repeat(1000);
        doAnswer(invocation -> {
            JsonObject normal = parse(result(invocation.getArgument(2)).body());
            return new WorkspaceResponse(200, Json.createObjectBuilder(normal).add("overview", longOverview).build().toString(), "application/json");
        }).when(client).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            service.advance(id);
            assertEquals(longOverview, service.page(id, "admin", 0, 5).getJsonArray("items")
                    .getJsonObject(0).getJsonObject("result").getString("overview"));
        }
    }

    @Test
    void invalidModelEvidenceIsSavedForReviewButNeverCountedOrAutomaticallyRetried() throws Exception {
        String body = Json.createObjectBuilder().add("overview", "Unverified finding")
                .add("questions", Json.createArrayBuilder().add(Json.createObjectBuilder()
                    .add("asked", "Question").add("answerSummary", "Claimed answer").add("answerCoverage", "partial")
                    .add("sourceQuote", "fabricated quote not in the evidence").add("gapKind", "candidate_kb_gap")
                    .add("gapDescription", "Candidate").add("improvement", "Verify source").add("confidence", "limited")))
                .build().toString();
        doReturn(new WorkspaceResponse(200, body, "application/json")).when(client)
                .sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            service.advance(id);
            JsonObject status = service.status(id, "admin");
            assertEquals("PAUSED", status.getString("state"));
            assertFalse(status.getBoolean("uncertain"));
            assertTrue(status.getBoolean("validationFailed"));
            assertEquals(0, status.getInt("completedChats"));
            assertEquals(body, service.page(id, "admin", 0, 5).getJsonArray("items").getJsonObject(0).getString("receivedResponse"));
            service.advance(id);
            verify(client, times(1)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void savedResultsSurviveInterruptedCheckpointWithoutResending() throws Exception {
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            service.advance(id);
            JsonObject checkpoint = parse(data.get(key("full-analysis-job", id)).payload());
            data.put(key("full-analysis-job", id), new WorkflowRecordStore.StoredRecord(
                    Json.createObjectBuilder(checkpoint).add("state", "SENDING").add("cursor", 0)
                        .add("mapCompleted", 0).add("completedChats", 0).add("resultBytes", 0).build().toString(), clock.get()));
            assertFalse(service.status(id, "admin").getBoolean("uncertain"));
            service.control(id, "admin", "resume", false);
            service.advance(id);
            assertEquals(1, service.status(id, "admin").getInt("completedChats"));
            verify(client, times(1)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void cancellationDuringTimeoutStaysCancelledAndCanConfirmIdleWithoutResending() throws Exception {
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            doAnswer(invocation -> {
                service.control(id, "admin", "cancel", false);
                throw new HttpTimeoutException("unknown upstream outcome");
            }).when(client).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
            service.advance(id);
            assertEquals("CANCELLED", service.status(id, "admin").getString("state"));
            assertTrue(service.status(id, "admin").getBoolean("uncertain"));
            service.control(id, "admin", "confirmIdle", true);
            assertEquals("CANCELLED", service.status(id, "admin").getString("state"));
            assertTrue(service.status(id, "admin").getBoolean("upstreamIdleConfirmed"));
            service.advance(id);
            verify(client, times(1)).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void expiredJobsCannotReplayPendingWork() throws Exception {
        try (var service = service(List.of(new FullAnalysisService.Chat("chat", "Question", "Answer")))) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            service.advance(id);
            clock.addAndGet(java.util.concurrent.TimeUnit.DAYS.toMillis(31));
            assertThrows(IllegalArgumentException.class, () -> service.status(id, "admin"));
            assertTrue(service.list("admin").isEmpty());
            verify(client, never()).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    void disappearingCheckpointPreservesFailureWithoutSendingInference() throws Exception {
        try (var service = service(List.of())) {
            String id = service.start("admin", LocalDate.now(), FullAnalysisService.Limits.defaults());
            var checkpoint = data.get(key("full-analysis-job", id));
            when(records.find("full-analysis-job", id)).thenReturn(checkpoint).thenReturn(null);
            clearInvocations(records);

            assertThrows(NullPointerException.class, () -> service.control(id, "admin", "pause", false));

            verify(records, never()).replace(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong());
            verify(client, never()).sendChatStrictOnce(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        }
    }

    private FullAnalysisService service(List<FullAnalysisService.Chat> chats) {
        return new FullAnalysisService(records, client, (day, cutoff) -> chats, () -> target, false);
    }

    private WorkspaceResponse result(String message) {
        JsonObject input = parse(message.substring(message.lastIndexOf('\n') + 1));
        if (message.startsWith("Synthesize")) {
            return new WorkspaceResponse(200, Json.createObjectBuilder().add("overview", "Demand overview based on saved findings")
                    .add("themes", Json.createArrayBuilder().add("Support questions"))
                    .add("candidateGaps", Json.createArrayBuilder()).add("improvements", Json.createArrayBuilder()).build().toString(), "application/json");
        }
        String evidence = input.getString("evidence");
        JsonObject finding = Json.createObjectBuilder().add("asked", "Support question")
            .add("answerSummary", "The shown answer provides support guidance")
            .add("gapDescription", "Exact diagnostics require additional user context")
                .add("answerCoverage", "partial").add("sourceQuote", evidence.substring(0, Math.min(20, evidence.length())))
                .add("gapKind", "missing_user_context").add("improvement", "Request exact diagnostics")
                .add("confidence", "limited").build();
        return new WorkspaceResponse(200, Json.createObjectBuilder().add("overview", "User support needs")
                .add("questions", Json.createArrayBuilder().add(finding)).build().toString(), "application/json");
    }

    private String key(String kind, String id) { return kind + "/" + id; }
    private JsonObject parse(String text) {
        try (var reader = Json.createReader(new StringReader(text))) { return reader.readObject(); }
    }
}