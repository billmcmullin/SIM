package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for OrchestrationResult
 *
 * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult
 * @author bmcmullin
 */
public class WidgetReviewMapReduceOrchestrator_OrchestrationResultTest
{

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("OrchestrationResult{status=0, mapOutputs=1, failedBatches=[1], mapBatchResults=1, totalSelected=1, totalBatches=1, coverageComplete=false, missingChatIds=0, coveragePassesUsed=0}", result);

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
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString2() throws Throwable
    {
        // Given
        WorkspaceResponse finalResponse = null; // UTA: configured value
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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("OrchestrationResult{status=-1, mapOutputs=1, failedBatches=[1], mapBatchResults=1, totalSelected=1, totalBatches=1, coverageComplete=false, missingChatIds=0, coveragePassesUsed=0}", result);

        // Then - assertions for this instance of WidgetReviewMapReduceOrchestrator.OrchestrationResult
        assertAll(() -> {
            assertNull(underTest.finalResponse());
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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<BatchFailure> failures = null; // UTA: configured value
        OrchestrationResult result = underTest.withBatchFailures(failures);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<BatchFailure> failures = new ArrayList<BatchFailure>(); // UTA: default value
        BatchFailure item4 = mock(BatchFailure.class);
        failures.add(item4);
        OrchestrationResult result = underTest.withBatchFailures(failures);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = underTest.withCoverage(allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        allSelectedChatIds.add(item4);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item5 = "item5"; // UTA: default value
        missingChatIds.add(item5);
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = underTest.withCoverage(allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        allSelectedChatIds.add(item4);
        List<String> missingChatIds = null; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = underTest.withCoverage(allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

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
        OrchestrationResult underTest = new OrchestrationResult(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult, totalSelected, totalBatches);

        // When
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        missingChatIds.add(item4);
        boolean coverageComplete = false; // UTA: default value
        int coveragePassesUsed = 1; // UTA: default value
        OrchestrationResult result = underTest.withCoverage(allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed);

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
}
