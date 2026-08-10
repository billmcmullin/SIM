package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.PromptTemplateService;
import com.sim.chatserver.service.ReviewContextBuilderService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import jakarta.json.Json;

class WidgetReviewOrchestrationRunnerTest {

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void run_returnsResult_onSuccess() {
        WidgetReviewMapReduceOrchestrator.OrchestrationResult expected = orchestrationResult(
                workspaceResponse(202, "{\"status\":\"queued\"}", "application/json")
        );
        WidgetReviewMapReduceOrchestrator orchestrator = new WidgetReviewMapReduceOrchestrator(
                mock(WorkspaceClient.class),
                mock(ReviewContextBuilderService.class),
                mock(PromptTemplateService.class)
        ) {
            @Override
            public OrchestrationResult run(
                    String targetUrl,
                    String apiKey,
                    String userMessage,
                    String mode,
                    String sessionId,
                    boolean requestReset,
                    jakarta.json.JsonArray attachments,
                    List<com.sim.chatserver.model.SelectedEntry> selectedEntries,
                    String requestId,
                    ProgressListener progressListener
            ) {
                return expected;
            }
        };

        WidgetReviewMapReduceOrchestrator.OrchestrationResult actual = assertDoesNotThrowRun(orchestrator);
        assertSame(expected, actual);
    }

    @Test
    void run_rethrowsInterruptedException_andInterruptsThread() {
        WidgetReviewMapReduceOrchestrator orchestrator = new WidgetReviewMapReduceOrchestrator(
                mock(WorkspaceClient.class),
                mock(ReviewContextBuilderService.class),
                mock(PromptTemplateService.class)
        ) {
            @Override
            public OrchestrationResult run(
                    String targetUrl,
                    String apiKey,
                    String userMessage,
                    String mode,
                    String sessionId,
                    boolean requestReset,
                    jakarta.json.JsonArray attachments,
                    List<com.sim.chatserver.model.SelectedEntry> selectedEntries,
                    String requestId,
                    ProgressListener progressListener
            ) throws InterruptedException {
                throw new InterruptedException("interrupted");
            }
        };

        assertThrows(InterruptedException.class, () -> runWithDefaults(orchestrator));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void run_wrapsUnexpectedExceptions_withIOException() {
        RuntimeException boom = new RuntimeException("boom");
        WidgetReviewMapReduceOrchestrator orchestrator = new WidgetReviewMapReduceOrchestrator(
                mock(WorkspaceClient.class),
                mock(ReviewContextBuilderService.class),
                mock(PromptTemplateService.class)
        ) {
            @Override
            public OrchestrationResult run(
                    String targetUrl,
                    String apiKey,
                    String userMessage,
                    String mode,
                    String sessionId,
                    boolean requestReset,
                    jakarta.json.JsonArray attachments,
                    List<com.sim.chatserver.model.SelectedEntry> selectedEntries,
                    String requestId,
                    ProgressListener progressListener
            ) {
                throw boom;
            }
        };

        IOException ex = assertThrows(IOException.class, () -> runWithDefaults(orchestrator));
        assertEquals("Map-reduce orchestration failed", ex.getMessage());
        assertSame(boom, ex.getCause());
    }

    private static WidgetReviewMapReduceOrchestrator.OrchestrationResult assertDoesNotThrowRun(
            WidgetReviewMapReduceOrchestrator orchestrator
    ) {
        try {
            return runWithDefaults(orchestrator);
        } catch (IOException | InterruptedException e) {
            throw new AssertionError("Expected runWithDefaults to succeed", e);
        }
    }

    private static WidgetReviewMapReduceOrchestrator.OrchestrationResult runWithDefaults(
            WidgetReviewMapReduceOrchestrator orchestrator
    ) throws IOException, InterruptedException {
        return WidgetReviewOrchestrationRunner.run(
                orchestrator,
                "https://example.test",
                "token",
                "message",
                "chat",
                "session-id",
                true,
                Json.createArrayBuilder().build(),
                List.of(),
                "request-id",
                WidgetReviewMapReduceOrchestrator.NOOP_PROGRESS_LISTENER
        );
    }

    @SuppressWarnings("unchecked")
    private static WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestrationResult(WorkspaceResponse response) {
        try {
            Constructor<WidgetReviewMapReduceOrchestrator.OrchestrationResult> ctor =
                    WidgetReviewMapReduceOrchestrator.OrchestrationResult.class.getDeclaredConstructor(
                            WorkspaceResponse.class,
                            List.class,
                            List.class,
                            List.class,
                            ReduceRequest.class,
                            ReduceResult.class,
                            int.class,
                            int.class
                    );
            ctor.setAccessible(true);
            return ctor.newInstance(response, List.of(), List.of(), List.of(), null, null, 0, 0);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to construct OrchestrationResult for test", e);
        }
    }

    private static WorkspaceResponse workspaceResponse(int statusCode, String body, String contentType) {
        try {
            Constructor<WorkspaceResponse> ctor =
                    WorkspaceResponse.class.getDeclaredConstructor(int.class, String.class, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(statusCode, body, contentType);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to construct WorkspaceResponse for test", e);
        }
    }
}
