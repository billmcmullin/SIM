package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.ProgressListener;

import jakarta.json.JsonArray;
import jakarta.json.Json;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

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
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);
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
    public void testRun3() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = new ArrayList<SelectedEntry>(); // UTA: default value
        String requestId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun4() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

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
        ProgressListener progress = null; // UTA: configured value
        OrchestrationResult result = underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun5() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        ProgressListener progress = mock(ProgressListener.class);
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun6() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        ProgressListener progress = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun7() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = new ArrayList<SelectedEntry>(); // UTA: default value
        String requestId = null; // UTA: configured value
        ProgressListener progress = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }


    /**
     * Consolidated from WidgetReviewMapReduceOrchestrator_OrchestrationResultTest.java to keep one test class per production source file.
     */
    /**
     * Parasoft Jtest UTA: Test for allSelectedChatIds()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#allSelectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testAllSelectedChatIds() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> result = underTest.allSelectedChatIds();

        // Then - assertions for result of method allSelectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for batchFailures()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#batchFailures()
     * @author bmcmullin
     */
    @Test
    public void testBatchFailures() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<BatchFailure> result = underTest.batchFailures();

        // Then - assertions for result of method batchFailures()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for coverageComplete()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#coverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testCoverageComplete() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        boolean result = underTest.coverageComplete();

        // Then - assertions for result of method coverageComplete()
        assertFalse(result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for coveragePassesUsed()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#coveragePassesUsed()
     * @author bmcmullin
     */
    @Test
    public void testCoveragePassesUsed() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        int result = underTest.coveragePassesUsed();

        // Then - assertions for result of method coveragePassesUsed()
        assertEquals(0, result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failedBatchIndexes()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#failedBatchIndexes()
     * @author bmcmullin
     */
    @Test
    public void testFailedBatchIndexes() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<Integer> result = underTest.failedBatchIndexes();

        // Then - assertions for result of method failedBatchIndexes()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for finalResponse()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#finalResponse()
     * @author bmcmullin
     */
    @Test
    public void testFinalResponse() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        WorkspaceResponse result = underTest.finalResponse();

        // Then - assertions for result of method finalResponse()
        assertNotNull(result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hasFailures()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#hasFailures()
     * @author bmcmullin
     */
    @Test
    public void testHasFailures() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        boolean result = underTest.hasFailures();

        // Then - assertions for result of method hasFailures()
        assertTrue(result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for mapBatchResults()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#mapBatchResults()
     * @author bmcmullin
     */
    @Test
    public void testMapBatchResults() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<MapBatchResult> result = underTest.mapBatchResults();

        // Then - assertions for result of method mapBatchResults()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for mapOutputs()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#mapOutputs()
     * @author bmcmullin
     */
    @Test
    public void testMapOutputs() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> result = underTest.mapOutputs();

        // Then - assertions for result of method mapOutputs()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for missingChatIds()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#missingChatIds()
     * @author bmcmullin
     */
    @Test
    public void testMissingChatIds() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> result = underTest.missingChatIds();

        // Then - assertions for result of method missingChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for reduceRequest()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#reduceRequest()
     * @author bmcmullin
     */
    @Test
    public void testReduceRequest() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        ReduceRequest result = underTest.reduceRequest();

        // Then - assertions for result of method reduceRequest()
        assertNotNull(result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for reduceResult()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#reduceResult()
     * @author bmcmullin
     */
    @Test
    public void testReduceResult() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        ReduceResult result = underTest.reduceResult();

        // Then - assertions for result of method reduceResult()
        assertNotNull(result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for totalBatches()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#totalBatches()
     * @author bmcmullin
     */
    @Test
    public void testTotalBatches() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        int result = underTest.totalBatches();

        // Then - assertions for result of method totalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalSelected());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for totalSelected()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#totalSelected()
     * @author bmcmullin
     */
    @Test
    public void testTotalSelected() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        int result = underTest.totalSelected();

        // Then - assertions for result of method totalSelected()
        assertEquals(1, result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNotNull(underTest.finalResponse());
        }, () -> {
            assertNotNull(underTest.mapOutputs());
            assertEquals(1, underTest.mapOutputs().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(1, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.mapBatchResults());
            assertEquals(1, underTest.mapBatchResults().size());
        }, () -> {
            assertNotNull(underTest.reduceRequest());
        }, () -> {
            assertNotNull(underTest.reduceResult());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertNotNull(underTest.batchFailures());
            assertEquals(0, underTest.batchFailures().size());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertFalse(underTest.coverageComplete());
        }, () -> {
            assertEquals(0, underTest.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for withBatchFailures(List)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withBatchFailures(List)
     * @author bmcmullin
     */
    @Test
    public void testWithBatchFailures() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<BatchFailure> failures = null; // UTA: configured value
        OrchestrationResult result = invokeWithBatchFailures(underTest, failures);

        // Then - assertions for result of method withBatchFailures(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
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
     * Parasoft Jtest UTA: Test for withBatchFailures(List)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withBatchFailures(List)
     * @author bmcmullin
     */
    @Test
    public void testWithBatchFailures2() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<BatchFailure> failures = new ArrayList<BatchFailure>(); // UTA: default value
        BatchFailure item4 = mock(BatchFailure.class);
        failures.add(item4);
        OrchestrationResult result = invokeWithBatchFailures(underTest, failures);

        // Then - assertions for result of method withBatchFailures(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
        }, () -> {
            assertNotNull(result.batchFailures());
            assertEquals(1, result.batchFailures().size());
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
     * Parasoft Jtest UTA: Test for withCoverage(List, boolean, int)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withCoverage(List, boolean,
     *      int)
     * @author bmcmullin
     */
    @Test
    public void testWithCoverage() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = invokeWithCoverage(underTest, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

        // Then - assertions for result of method withCoverage(List, List, boolean, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
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
            assertEquals(1, result.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for withCoverage(List, boolean, int)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withCoverage(List, boolean,
     *      int)
     * @author bmcmullin
     */
    @Test
    public void testWithCoverage2() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        allSelectedChatIds.add(item4);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item5 = "item5"; // UTA: default value
        missingChatIds.add(item5);
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = invokeWithCoverage(underTest, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

        // Then - assertions for result of method withCoverage(List, List, boolean, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
        }, () -> {
            assertNotNull(result.batchFailures());
            assertEquals(0, result.batchFailures().size());
        }, () -> {
            assertNotNull(result.allSelectedChatIds());
            assertEquals(1, result.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.missingChatIds());
            assertEquals(1, result.missingChatIds().size());
        }, () -> {
            assertFalse(result.coverageComplete());
        }, () -> {
            assertEquals(1, result.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for withCoverage(List, boolean, int)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withCoverage(List, boolean,
     *      int)
     * @author bmcmullin
     */
    @Test
    public void testWithCoverage3() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        allSelectedChatIds.add(item4);
        List<String> missingChatIds = null; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = invokeWithCoverage(underTest, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

        // Then - assertions for result of method withCoverage(List, List, boolean, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
        }, () -> {
            assertNotNull(result.batchFailures());
            assertEquals(0, result.batchFailures().size());
        }, () -> {
            assertNotNull(result.allSelectedChatIds());
            assertEquals(1, result.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.missingChatIds());
            assertEquals(0, result.missingChatIds().size());
        }, () -> {
            assertFalse(result.coverageComplete());
        }, () -> {
            assertEquals(1, result.coveragePassesUsed());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for withCoverage(List, boolean, int)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#withCoverage(List, boolean,
     *      int)
     * @author bmcmullin
     */
    @Test
    public void testWithCoverage4() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = mock(WorkspaceResponse.class);
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<MapBatchResult> mapBatchResults = new ArrayList<MapBatchResult>(); // UTA: default value
        MapBatchResult item3 = mock(MapBatchResult.class);
        mapBatchResults.add(item3);
        ReduceRequest reduceRequest = mock(ReduceRequest.class);
        ReduceResult reduceResult = mock(ReduceResult.class);
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        OrchestrationResult underTest = createOrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        missingChatIds.add(item4);
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = invokeWithCoverage(underTest, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

        // Then - assertions for result of method withCoverage(List, List, boolean, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.finalResponse());
        }, () -> {
            assertNotNull(result.mapOutputs());
            assertEquals(1, result.mapOutputs().size());
        }, () -> {
            assertNotNull(result.failedBatchIndexes());
            assertEquals(1, result.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.mapBatchResults());
            assertEquals(1, result.mapBatchResults().size());
        }, () -> {
            assertNotNull(result.reduceRequest());
        }, () -> {
            assertNotNull(result.reduceResult());
        }, () -> {
            assertEquals(1, result.totalSelected());
        }, () -> {
            assertEquals(1, result.totalBatches());
        }, () -> {
            assertNotNull(result.batchFailures());
            assertEquals(0, result.batchFailures().size());
        }, () -> {
            assertNotNull(result.allSelectedChatIds());
            assertEquals(0, result.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.missingChatIds());
            assertEquals(1, result.missingChatIds().size());
        }, () -> {
            assertFalse(result.coverageComplete());
        }, () -> {
            assertEquals(1, result.coveragePassesUsed());
        });

    }

        @Test
        public void testPrivateHelperCoverage_normalizationAndReasoning() throws Throwable
        {
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(
            mock(WorkspaceClient.class),
            new ReviewContextBuilderService(new ReviewSamplingService()),
            new PromptTemplateService()
        );

        assertEquals("", invokePrivate(underTest, "normalizeId", new Class<?>[]{String.class}, (Object) null));
        assertEquals("abc", invokePrivate(underTest, "normalizeId", new Class<?>[]{String.class}, "  AbC  "));

        @SuppressWarnings("unchecked")
        List<String> normalizedIds = (List<String>) invokePrivate(
            underTest,
            "normalizeIds",
            new Class<?>[]{List.class},
            java.util.Arrays.asList("A", "a", " ", null, "B")
        );
        assertEquals(List.of("a", "b"), normalizedIds);

        @SuppressWarnings("unchecked")
        List<String> intersection = (List<String>) invokePrivate(
            underTest,
            "intersect",
            new Class<?>[]{List.class, List.class},
            List.of("A", "b", "c"),
            List.of("b", "C", "")
        );
        assertEquals(List.of("b", "c"), intersection);

        @SuppressWarnings("unchecked")
        List<String> subtraction = (List<String>) invokePrivate(
            underTest,
            "subtract",
            new Class<?>[]{List.class, List.class},
            List.of("A", "b", "c"),
            List.of(" B ")
        );
        assertEquals(List.of("a", "c"), subtraction);

        @SuppressWarnings("unchecked")
        List<Integer> distinctInts = (List<Integer>) invokePrivate(
            underTest,
            "distinctInts",
            new Class<?>[]{List.class},
            java.util.Arrays.asList((Integer) null, Integer.valueOf(0), Integer.valueOf(-1), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(3))
        );
        assertEquals(List.of(Integer.valueOf(2), Integer.valueOf(3)), distinctInts);

        String mapOutput = "### chat c1\ncovered_chat_ids: [c1, C2, ...]\n";
        @SuppressWarnings("unchecked")
        List<String> coveredIds = (List<String>) invokePrivate(
            underTest,
            "parseCoveredIdsContract",
            new Class<?>[]{String.class},
            mapOutput
        );
        assertEquals(List.of("c1", "c2"), coveredIds);

        @SuppressWarnings("unchecked")
        List<String> headingIds = (List<String>) invokePrivate(
            underTest,
            "parseChatIdsFromMapOutput",
            new Class<?>[]{String.class},
            "### chat Alpha-1\nBody\n### CHAT beta.2\nMore"
        );
        assertEquals(List.of("alpha-1", "beta.2"), headingIds);

        assertEquals("context_too_large", invokePrivate(
            underTest,
            "determineReasonCode",
            new Class<?>[]{int.class, boolean.class, boolean.class},
            400,
            true,
            true
        ));
        assertEquals("upstream_4xx", invokePrivate(
            underTest,
            "determineReasonCode",
            new Class<?>[]{int.class, boolean.class, boolean.class},
            404,
            false,
            true
        ));
        assertEquals("upstream_5xx", invokePrivate(
            underTest,
            "determineReasonCode",
            new Class<?>[]{int.class, boolean.class, boolean.class},
            503,
            false,
            true
        ));
        assertEquals("parse_error", invokePrivate(
            underTest,
            "determineReasonCode",
            new Class<?>[]{int.class, boolean.class, boolean.class},
            200,
            false,
            false
        ));
        assertEquals("batch_processing_failure", invokePrivate(
            underTest,
            "determineReasonCode",
            new Class<?>[]{int.class, boolean.class, boolean.class},
            200,
            false,
            true
        ));

        assertTrue((boolean) invokePrivate(underTest, "isDailySummarySession", new Class<?>[]{String.class}, "  DASHBOARD-DAILY-SUMMARY-1  "));
        assertFalse((boolean) invokePrivate(underTest, "isDailySummarySession", new Class<?>[]{String.class}, "session-1"));

        MapBatchResult auth401 = mock(MapBatchResult.class);
        when(auth401.isSuccess()).thenReturn(false);
        when(auth401.getHttpStatus()).thenReturn(401);
        MapBatchResult auth403 = mock(MapBatchResult.class);
        when(auth403.isSuccess()).thenReturn(false);
        when(auth403.getHttpStatus()).thenReturn(403);
        MapBatchResult success = mock(MapBatchResult.class);
        when(success.isSuccess()).thenReturn(true);

        assertTrue((boolean) invokePrivate(
            underTest,
            "allMapFailuresAreAuthFailures",
            new Class<?>[]{List.class},
            List.of(auth401, auth403)
        ));
        assertFalse((boolean) invokePrivate(
            underTest,
            "allMapFailuresAreAuthFailures",
            new Class<?>[]{List.class},
            List.of(auth401, success)
        ));

        assertTrue((boolean) invokePrivate(
            underTest,
            "isLikelyContextLimitError",
            new Class<?>[]{String.class, int.class},
            "maximum context length requested",
            400
        ));
        assertFalse((boolean) invokePrivate(
            underTest,
            "isLikelyContextLimitError",
            new Class<?>[]{String.class, int.class},
            "gateway timeout",
            504
        ));
        }

        @Test
        public void testPrivateHelperCoverage_chunkingAndExtraction() throws Throwable
        {
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(
            mock(WorkspaceClient.class),
            new ReviewContextBuilderService(new ReviewSamplingService()),
            new PromptTemplateService()
        );

        @SuppressWarnings("unchecked")
        List<List<String>> chunked = (List<List<String>>) invokePrivate(
            underTest,
            "chunk",
            new Class<?>[]{List.class, int.class},
            List.of("a", "b", "c", "d", "e"),
            2
        );
        assertEquals(3, chunked.size());
        assertEquals(List.of("a", "b"), chunked.get(0));
        assertEquals(List.of("e"), chunked.get(2));

        @SuppressWarnings("unchecked")
        List<String> bounded = (List<String>) invokePrivate(
            underTest,
            "boundFinalInputs",
            new Class<?>[]{List.class, int.class, int.class},
            List.of("", "abcdef", "xyz"),
            1,
            3
        );
        assertEquals(1, bounded.size());
        assertEquals("abcdef", bounded.get(0));

        @SuppressWarnings("unchecked")
        List<String> fallbackBounded = (List<String>) invokePrivate(
            underTest,
            "boundFinalInputs",
            new Class<?>[]{List.class, int.class, int.class},
            List.of("   ", ""),
            2,
            4
        );
        assertEquals(1, fallbackBounded.size());

        @SuppressWarnings("unchecked")
        List<String> uniqueChatIds = (List<String>) invokePrivate(
            underTest,
            "extractUniqueChatIds",
            new Class<?>[]{List.class},
            java.util.Arrays.asList(entry("A"), entry("a"), entry("B"), null, entry(""))
        );
        assertEquals(List.of("a", "b"), uniqueChatIds);

        @SuppressWarnings("unchecked")
        List<SelectedEntry> filtered = (List<SelectedEntry>) invokePrivate(
            underTest,
            "filterEntriesByChatIds",
            new Class<?>[]{List.class, Set.class},
            List.of(entry("A"), entry("B"), entry("C")),
            new LinkedHashSet<>(List.of("b", "c"))
        );
        assertEquals(2, filtered.size());
        assertEquals("B", filtered.get(0).getChatId());
        assertEquals("C", filtered.get(1).getChatId());

        assertEquals("message text", invokePrivate(
            underTest,
            "extractPrimaryTextFromWorkspaceResponse",
            new Class<?>[]{String.class},
            "{\"message\":\"message text\"}"
        ));
        assertEquals("raw-body", invokePrivate(
            underTest,
            "extractPrimaryTextFromWorkspaceResponse",
            new Class<?>[]{String.class},
            "raw-body"
        ));

        String canonical = (String) invokePrivate(
            underTest,
            "canonicalizeForValidation",
            new Class<?>[]{String.class},
            "A\u0001B\n\tC"
        );
        assertEquals("AB\n\tC", canonical);

        String outbound = (String) invokePrivate(
            underTest,
            "buildOutboundMessage",
            new Class<?>[]{String.class, String.class, int.class},
            "prompt",
            "ctx",
            8
        );
        assertEquals("prompt\n\n", outbound);

        assertEquals("", invokePrivate(underTest, "trimTo", new Class<?>[]{String.class, int.class}, null, 3));
        assertEquals("abc", invokePrivate(underTest, "trimTo", new Class<?>[]{String.class, int.class}, "abcdef", 3));
        }

        @Test
        public void testRun_whenAllMapBatchesUnauthorized_returnsAuthFailureReduceResult() throws Throwable
        {
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(
            workspaceClient,
            new ReviewContextBuilderService(new ReviewSamplingService()),
            new PromptTemplateService()
        );

        when(workspaceClient.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(JsonArray.class), anyString()))
            .thenReturn(new WorkspaceResponse(401, "{\"message\":\"unauthorized\"}", "application/json"));
        when(workspaceClient.isLikelyContextTooLarge(any(WorkspaceResponse.class))).thenReturn(false);

        OrchestrationResult result = underTest.run(
            "https://api.example.com",
            "api-key",
            "Summarize chats",
            "chat",
            "session-1",
            false,
            Json.createArrayBuilder().build(),
            List.of(entry("c1")),
            "req-auth"
        );

        assertEquals(403, result.finalResponse().statusCode());
        assertFalse(result.coverageComplete());
        assertEquals(1, result.totalBatches());
        assertEquals(1, result.failedBatchIndexes().size());
        assertEquals(1, result.missingChatIds().size());
        }

        @Test
        public void testRun_withSuccessfulMap_executesReduceAndReturnsCoverage() throws Throwable
        {
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        WidgetReviewMapReduceOrchestrator underTest = WidgetReviewMapReduceOrchestrator.createDefault(
            workspaceClient,
            new ReviewContextBuilderService(new ReviewSamplingService()),
            new PromptTemplateService()
        );

        String mapBody = "{\"textResponse\":\"### chat c1\\ncovered_chat_ids: [c1]\\nExecutive chat analysis\\nKey metrics\\nRisks and opportunities\\nRecommendations\\nCoverage and methodology\"}";
        String reduceBody = "{\"textResponse\":\"## Executive Chat Analysis\\n## Key Metrics\\n## Risks and Opportunities\\n## Recommendations\\n## Coverage and Methodology\\n- chats provided: 1\\n- chats used in analysis: 1\\n- chats not used: 0\"}";

        when(workspaceClient.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(JsonArray.class), anyString()))
            .thenReturn(
                new WorkspaceResponse(200, mapBody, "application/json"),
                new WorkspaceResponse(200, reduceBody, "application/json")
            );
        when(workspaceClient.isLikelyContextTooLarge(any(WorkspaceResponse.class))).thenReturn(false);

        OrchestrationResult result = underTest.run(
            "https://api.example.com",
            "api-key",
            "Summarize chats",
            "chat",
            "session-1",
            false,
            Json.createArrayBuilder().build(),
            List.of(entry("c1")),
            "req-success"
        );

        assertEquals(1, result.totalBatches());
        assertEquals(1, result.mapBatchResults().size());
        assertEquals(0, result.failedBatchIndexes().size());
        assertEquals(1, result.mapOutputs().size());
        assertTrue(result.coverageComplete());
        assertEquals(0, result.missingChatIds().size());
        assertNotNull(result.reduceRequest());
        assertNotNull(result.reduceResult());
        }

        private static SelectedEntry entry(String chatId) {
        return new SelectedEntry(chatId, "prompt", "response", "2026-01-01T00:00:00Z", "session");
        }

        private static Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Throwable {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause() == null ? ex : ex.getCause();
        }
        }

    private static OrchestrationResult createOrchestrationResult(
            WorkspaceResponse finalResponse,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            List<MapBatchResult> mapBatchResults,
            ReduceRequest reduceRequest,
            ReduceResult reduceResult,
            int totalSelected,
            int totalBatches
    ) throws Throwable {
        try {
            Constructor<OrchestrationResult> ctor = OrchestrationResult.class.getDeclaredConstructor(
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
            return ctor.newInstance(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);
        } catch (InvocationTargetException ex) {
            throw ex.getCause() == null ? ex : ex.getCause();
        }
    }

    private static OrchestrationResult invokeWithBatchFailures(
            OrchestrationResult target,
            List<BatchFailure> failures
    ) throws Throwable {
        try {
            Method method = OrchestrationResult.class.getDeclaredMethod("withBatchFailures", List.class);
            method.setAccessible(true);
            return (OrchestrationResult) method.invoke(target, failures);
        } catch (InvocationTargetException ex) {
            throw ex.getCause() == null ? ex : ex.getCause();
        }
    }

    private static OrchestrationResult invokeWithCoverage(
            OrchestrationResult target,
            List<String> allSelectedChatIds,
            List<String> missingChatIds,
            boolean coverageComplete,
            int coveragePassesUsed
    ) throws Throwable {
        try {
            Method method = OrchestrationResult.class.getDeclaredMethod(
                    "withCoverage",
                    List.class,
                    List.class,
                    boolean.class,
                    int.class
            );
            method.setAccessible(true);
            return (OrchestrationResult) method.invoke(target, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);
        } catch (InvocationTargetException ex) {
            throw ex.getCause() == null ? ex : ex.getCause();
        }
    }
}


