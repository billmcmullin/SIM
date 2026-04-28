package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReduceResult.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for ReduceResult
 *
 * @see com.sim.chatserver.model.review.ReduceResult
 * @author bmcmullin
 */
public class ReduceResultTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = ReduceResult.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        ReduceResult result = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // Then - assertions for result of method failed(String, int, String, int, int, int, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("requestId", result.getRequestId());
        }, () -> {
            assertEquals(1, result.getHttpStatus());
        }, () -> {
            assertFalse(result.isSuccess());
        }, () -> {
            assertFalse(result.isRetryUsed());
        }, () -> {
            assertFalse(result.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", result.getFinalReport());
        }, () -> {
            assertEquals("", result.getErrorMessage());
        }, () -> {
            assertEquals(1, result.getTotalSelected());
        }, () -> {
            assertEquals(1, result.getTotalBatches());
        }, () -> {
            assertEquals(1, result.getMapOutputsReceived());
        }, () -> {
            assertNotNull(result.getFailedBatchIndexes());
            assertEquals(0, result.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.getFailedBatchReasons());
            assertEquals(0, result.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(result.getAllSelectedChatIds());
            assertEquals(0, result.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.getUsedChatIds());
            assertEquals(0, result.getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertTrue(result.isCoverageComplete());
        }, () -> {
            assertEquals(0, result.getCoveragePercent());
        }, () -> {
            assertEquals(0L, result.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed2() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        ReduceResult result = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // Then - assertions for result of method failed(String, int, String, int, int, int, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("requestId", result.getRequestId());
        }, () -> {
            assertEquals(1, result.getHttpStatus());
        }, () -> {
            assertFalse(result.isSuccess());
        }, () -> {
            assertFalse(result.isRetryUsed());
        }, () -> {
            assertFalse(result.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", result.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", result.getErrorMessage());
        }, () -> {
            assertEquals(1, result.getTotalSelected());
        }, () -> {
            assertEquals(1, result.getTotalBatches());
        }, () -> {
            assertEquals(1, result.getMapOutputsReceived());
        }, () -> {
            assertNotNull(result.getFailedBatchIndexes());
            assertEquals(1, result.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(result.getFailedBatchReasons());
            assertEquals(0, result.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(result.getAllSelectedChatIds());
            assertEquals(0, result.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.getUsedChatIds());
            assertEquals(0, result.getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getMissingChatIds());
            assertEquals(0, result.getMissingChatIds().size());
        }, () -> {
            assertTrue(result.isCoverageComplete());
        }, () -> {
            assertEquals(0, result.getCoveragePercent());
        }, () -> {
            assertEquals(0L, result.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed3() throws Throwable
    {
        // When
        String requestId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        assertThrows(IllegalArgumentException.class, () -> {
            ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getAllSelectedChatIds()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getAllSelectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetAllSelectedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        List<String> result = underTest.getAllSelectedChatIds();

        // Then - assertions for result of method getAllSelectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCoveragePercent()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getCoveragePercent()
     * @author bmcmullin
     */
    @Test
    public void testGetCoveragePercent() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getCoveragePercent();

        // Then - assertions for result of method getCoveragePercent()
        assertEquals(0, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getErrorMessage()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getErrorMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetErrorMessage() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        String result = underTest.getErrorMessage();

        // Then - assertions for result of method getErrorMessage()
        assertEquals("errorMessage", result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchCount()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getFailedBatchCount()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchCount() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getFailedBatchCount();

        // Then - assertions for result of method getFailedBatchCount()
        assertEquals(0, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchIndexes()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getFailedBatchIndexes()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchIndexes() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        List<Integer> result = underTest.getFailedBatchIndexes();

        // Then - assertions for result of method getFailedBatchIndexes()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchReasons()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getFailedBatchReasons()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchReasons() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        List<String> result = underTest.getFailedBatchReasons();

        // Then - assertions for result of method getFailedBatchReasons()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFinalReport()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getFinalReport()
     * @author bmcmullin
     */
    @Test
    public void testGetFinalReport() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        String result = underTest.getFinalReport();

        // Then - assertions for result of method getFinalReport()
        assertEquals("", result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
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
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getHttpStatus()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getHttpStatus()
     * @author bmcmullin
     */
    @Test
    public void testGetHttpStatus() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getHttpStatus();

        // Then - assertions for result of method getHttpStatus()
        assertEquals(1, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLatencyMs()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getLatencyMs()
     * @author bmcmullin
     */
    @Test
    public void testGetLatencyMs() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        long result = underTest.getLatencyMs();

        // Then - assertions for result of method getLatencyMs()
        assertEquals(0L, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMapOutputsReceived()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getMapOutputsReceived()
     * @author bmcmullin
     */
    @Test
    public void testGetMapOutputsReceived() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getMapOutputsReceived();

        // Then - assertions for result of method getMapOutputsReceived()
        assertEquals(1, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMissingChatIds()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getMissingChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetMissingChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        List<String> result = underTest.getMissingChatIds();

        // Then - assertions for result of method getMissingChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRequestId()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getRequestId()
     * @author bmcmullin
     */
    @Test
    public void testGetRequestId() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        String result = underTest.getRequestId();

        // Then - assertions for result of method getRequestId()
        assertEquals("requestId", result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalBatches()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getTotalBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalBatches() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getTotalBatches();

        // Then - assertions for result of method getTotalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalSelected()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getTotalSelected()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalSelected() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        int result = underTest.getTotalSelected();

        // Then - assertions for result of method getTotalSelected()
        assertEquals(1, result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsedChatIds()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#getUsedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetUsedChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        List<String> result = underTest.getUsedChatIds();

        // Then - assertions for result of method getUsedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hasFailures()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#hasFailures()
     * @author bmcmullin
     */
    @Test
    public void testHasFailures() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.hasFailures();

        // Then - assertions for result of method hasFailures()
        assertFalse(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hasFinalReport()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#hasFinalReport()
     * @author bmcmullin
     */
    @Test
    public void testHasFinalReport() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.hasFinalReport();

        // Then - assertions for result of method hasFinalReport()
        assertFalse(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isContextTooLargeDetected()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#isContextTooLargeDetected()
     * @author bmcmullin
     */
    @Test
    public void testIsContextTooLargeDetected() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.isContextTooLargeDetected();

        // Then - assertions for result of method isContextTooLargeDetected()
        assertFalse(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isCoverageComplete()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#isCoverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testIsCoverageComplete() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.isCoverageComplete();

        // Then - assertions for result of method isCoverageComplete()
        assertTrue(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRetryUsed()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#isRetryUsed()
     * @author bmcmullin
     */
    @Test
    public void testIsRetryUsed() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.isRetryUsed();

        // Then - assertions for result of method isRetryUsed()
        assertFalse(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isSuccess()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#isSuccess()
     * @author bmcmullin
     */
    @Test
    public void testIsSuccess() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        boolean result = underTest.isSuccess();

        // Then - assertions for result of method isSuccess()
        assertFalse(result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        JsonObject result = underTest.toJson();

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(19, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.review.ReduceResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult underTest = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("ReduceResult{requestId='requestId', httpStatus=1, success=false, retryUsed=false, contextTooLargeDetected=false, totalSelected=1, totalBatches=1, mapOutputsReceived=1, failedBatchIndexes=[], coverageComplete=true, coveragePercent=0, missingChatIds=0, latencyMs=0}", result);

        // Then - assertions for this instance of ReduceResult
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertFalse(underTest.isRetryUsed());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("errorMessage", underTest.getErrorMessage());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getMapOutputsReceived());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getFailedBatchReasons());
            assertEquals(0, underTest.getFailedBatchReasons().size());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }
}
