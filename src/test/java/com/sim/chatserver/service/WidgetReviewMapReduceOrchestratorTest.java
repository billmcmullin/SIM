package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewMapReduceOrchestrator
 *
 * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator
 * @author bmcmullin
 */
public class WidgetReviewMapReduceOrchestratorTest
{

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String)
     * @author bmcmullin
     */
    @Test
    public void testRun() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        OrchestrationResult result = underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);

        // Then - assertions for result of method run(String, String, String, String, String, boolean, JsonArray, List, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertEquals(400, result.finalResponse().statusCode());
        }, () -> {
            assertEquals("{\"status\":\"error\",\"message\":\"No selected entries provided.\"}", result.finalResponse().body());
        }, () -> {
            assertEquals("application/json", result.finalResponse().contentType());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(0, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(0, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(0, result.mapBatchResults().size());
        }, () -> {
            assertNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals("requestId", result.reduceResult().getRequestId());
        }, () -> {
            assertEquals(400, result.reduceResult().getHttpStatus());
        }, () -> {
            assertFalse(result.reduceResult().isSuccess());
        }, () -> {
            assertFalse(result.reduceResult().isRetryUsed());
        }, () -> {
            assertFalse(result.reduceResult().isContextTooLargeDetected());
        }, () -> {
            assertEquals("", result.reduceResult().getFinalReport());
        }, () -> {
            assertEquals("No selected entries provided.", result.reduceResult().getErrorMessage());
        }, () -> {
            assertEquals(0, result.reduceResult().getTotalSelected());
        }, () -> {
            assertEquals(0, result.reduceResult().getTotalBatches());
        }, () -> {
            assertEquals(0, result.reduceResult().getMapOutputsReceived());
        }, () -> {
            assertNotNull(result.reduceResult().getFailedBatchIndexes());
            assertEquals(0, result.reduceResult().getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.reduceResult().getFailedBatchReasons());
            assertEquals(1, result.reduceResult().getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(result.reduceResult().getAllSelectedChatIds());
            assertEquals(0, result.reduceResult().getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.reduceResult().getUsedChatIds());
            assertEquals(0, result.reduceResult().getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.reduceResult().getMissingChatIds());
            assertEquals(0, result.reduceResult().getMissingChatIds().size());
        }, () -> {
            assertTrue(result.reduceResult().isCoverageComplete());
        }, () -> {
            assertEquals(0, result.reduceResult().getCoveragePercent());
        }, () -> {
            assertEquals(0L, result.reduceResult().getLatencyMs());
        }, () -> {
            assertEquals(0, result.totalSelected());
        }, () -> {
            assertEquals(0, result.totalBatches());
        }, () -> {
            assertNotNull(result.batchFailures());
            assertEquals(0, result.batchFailures().size());
        }, () -> {
            assertNotNull(result.allSelectedChatIds());
            assertEquals(0, result.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.missingChatIds());
            assertEquals(0, result.missingChatIds().size());
        }, () -> {
            assertFalse(result.coverageComplete());
        }, () -> {
            assertEquals(0, result.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String)
     * @author bmcmullin
     */
    @Test
    public void testRun2() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        selectedEntries.add(item);
        String requestId = "requestId"; // UTA: default value
        OrchestrationResult result = underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);

    }

}
