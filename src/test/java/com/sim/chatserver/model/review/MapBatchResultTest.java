package com.sim.chatserver.model.review;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.MapBatchResult.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for MapBatchResult
 *
 * @see com.sim.chatserver.model.review.MapBatchResult
 * @author bmcmullin
 */
public class MapBatchResultTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = MapBatchResult.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, int, String, int, String)
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#failed(String, int, int, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailed() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
        MapBatchResult result = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // Then - assertions for result of method failed(String, int, int, String, int, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("requestId", result.getRequestId());
        }, () -> {
            assertEquals(1, result.getBatchIndex());
        }, () -> {
            assertEquals(1, result.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", result.getBatchId());
        }, () -> {
            assertEquals(1, result.getHttpStatus());
        }, () -> {
            assertFalse(result.isSuccess());
        }, () -> {
            assertFalse(result.isRetryUsed());
        }, () -> {
            assertFalse(result.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", result.getModelOutput());
        }, () -> {
            assertEquals("", result.getErrorMessage());
        }, () -> {
            assertEquals(0, result.getInputEntriesCount());
        }, () -> {
            assertEquals(0, result.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, result.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(result.getBatchChatIds());
            assertEquals(0, result.getBatchChatIds().size());
        }, () -> {
            assertNotNull(result.getUsedChatIds());
            assertEquals(0, result.getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getOmittedChatIds());
            assertEquals(0, result.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(result.getExpectedChatIds());
            assertEquals(0, result.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(result.getFoundChatIds());
            assertEquals(0, result.getFoundChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingExpectedChatIds());
            assertEquals(0, result.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(result.isCoverageComplete());
        }, () -> {
            assertEquals(0L, result.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, int, String, int, String)
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#failed(String, int, int, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailed2() throws Throwable
    {
        // When
        String requestId = null; // UTA: configured value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, int, String, int, String)
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#failed(String, int, int, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailed3() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 0; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, int, String, int, String)
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#failed(String, int, int, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailed4() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 2; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getBatchChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getBatchChatIds();

        // Then - assertions for result of method getBatchChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchId()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getBatchId()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchId() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        String result = underTest.getBatchId();

        // Then - assertions for result of method getBatchId()
        assertEquals("batchId", result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchIndex()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getBatchIndex()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchIndex() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getBatchIndex();

        // Then - assertions for result of method getBatchIndex()
        assertEquals(1, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getErrorMessage()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getErrorMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetErrorMessage() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        String result = underTest.getErrorMessage();

        // Then - assertions for result of method getErrorMessage()
        assertEquals("errorMessage", result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getExpectedChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getExpectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetExpectedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getExpectedChatIds();

        // Then - assertions for result of method getExpectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFoundChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getFoundChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetFoundChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getFoundChatIds();

        // Then - assertions for result of method getFoundChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getHttpStatus()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getHttpStatus()
     * @author bmcmullin
     */
    @Test
    public void testGetHttpStatus() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getHttpStatus();

        // Then - assertions for result of method getHttpStatus()
        assertEquals(1, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getInputEntriesCount()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getInputEntriesCount()
     * @author bmcmullin
     */
    @Test
    public void testGetInputEntriesCount() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getInputEntriesCount();

        // Then - assertions for result of method getInputEntriesCount()
        assertEquals(0, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLatencyMs()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getLatencyMs()
     * @author bmcmullin
     */
    @Test
    public void testGetLatencyMs() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        long result = underTest.getLatencyMs();

        // Then - assertions for result of method getLatencyMs()
        assertEquals(0L, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMissingExpectedChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getMissingExpectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetMissingExpectedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getMissingExpectedChatIds();

        // Then - assertions for result of method getMissingExpectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getModelOutput()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getModelOutput()
     * @author bmcmullin
     */
    @Test
    public void testGetModelOutput() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        String result = underTest.getModelOutput();

        // Then - assertions for result of method getModelOutput()
        assertEquals("", result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getOmittedChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getOmittedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetOmittedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getOmittedChatIds();

        // Then - assertions for result of method getOmittedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getOmittedEntriesCount()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getOmittedEntriesCount()
     * @author bmcmullin
     */
    @Test
    public void testGetOmittedEntriesCount() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getOmittedEntriesCount();

        // Then - assertions for result of method getOmittedEntriesCount()
        assertEquals(0, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRequestId()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getRequestId()
     * @author bmcmullin
     */
    @Test
    public void testGetRequestId() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        String result = underTest.getRequestId();

        // Then - assertions for result of method getRequestId()
        assertEquals("requestId", result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalBatches()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getTotalBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalBatches() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getTotalBatches();

        // Then - assertions for result of method getTotalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsedChatIds()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getUsedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetUsedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        List<String> result = underTest.getUsedChatIds();

        // Then - assertions for result of method getUsedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsedEntriesCount()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#getUsedEntriesCount()
     * @author bmcmullin
     */
    @Test
    public void testGetUsedEntriesCount() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        int result = underTest.getUsedEntriesCount();

        // Then - assertions for result of method getUsedEntriesCount()
        assertEquals(0, result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hasModelOutput()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#hasModelOutput()
     * @author bmcmullin
     */
    @Test
    public void testHasModelOutput() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        boolean result = underTest.hasModelOutput();

        // Then - assertions for result of method hasModelOutput()
        assertFalse(result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isContextTooLargeDetected()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#isContextTooLargeDetected()
     * @author bmcmullin
     */
    @Test
    public void testIsContextTooLargeDetected() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        boolean result = underTest.isContextTooLargeDetected();

        // Then - assertions for result of method isContextTooLargeDetected()
        assertFalse(result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isCoverageComplete()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#isCoverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testIsCoverageComplete() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        boolean result = underTest.isCoverageComplete();

        // Then - assertions for result of method isCoverageComplete()
        assertTrue(result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRetryUsed()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#isRetryUsed()
     * @author bmcmullin
     */
    @Test
    public void testIsRetryUsed() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        boolean result = underTest.isRetryUsed();

        // Then - assertions for result of method isRetryUsed()
        assertFalse(result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isSuccess()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#isSuccess()
     * @author bmcmullin
     */
    @Test
    public void testIsSuccess() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        boolean result = underTest.isSuccess();

        // Then - assertions for result of method isSuccess()
        assertFalse(result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        JsonObject result = underTest.toJson();

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(21, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult underTest = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("MapBatchResult{requestId='requestId', batchIndex=1, totalBatches=1, batchId='batchId', httpStatus=1, success=false, retryUsed=false, contextTooLargeDetected=false, inputEntriesCount=0, usedEntriesCount=0, omittedEntriesCount=0, coverageComplete=true, missingExpectedChatIds=0, latencyMs=0}", result);

        // Then - assertions for this instance of MapBatchResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batchId", underTest.getBatchId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getModelOutput());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(0, underTest.getInputEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getUsedEntriesCount());
        }, () -> {
            assertEquals(0, underTest.getOmittedEntriesCount());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getOmittedChatIds());
            assertEquals(0, underTest.getOmittedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getExpectedChatIds());
            assertEquals(0, underTest.getExpectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getFoundChatIds());
            assertEquals(0, underTest.getFoundChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingExpectedChatIds());
            assertEquals(0, underTest.getMissingExpectedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }
}
