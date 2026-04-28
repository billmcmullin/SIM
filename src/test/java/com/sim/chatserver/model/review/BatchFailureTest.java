package com.sim.chatserver.model.review;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.BatchFailure.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for BatchFailure
 *
 * @see com.sim.chatserver.model.review.BatchFailure
 * @author bmcmullin
 */
public class BatchFailureTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = BatchFailure.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchChatIds()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getBatchChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchChatIds() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        List<String> result = underTest.getBatchChatIds();

        // Then - assertions for result of method getBatchChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchId()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getBatchId()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchId() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.getBatchId();

        // Then - assertions for result of method getBatchId()
        assertEquals("batch-1", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchIndex()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getBatchIndex()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchIndex() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        int result = underTest.getBatchIndex();

        // Then - assertions for result of method getBatchIndex()
        assertEquals(1, result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getHttpStatus()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getHttpStatus()
     * @author bmcmullin
     */
    @Test
    public void testGetHttpStatus() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        int result = underTest.getHttpStatus();

        // Then - assertions for result of method getHttpStatus()
        assertEquals(0, result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLatencyMs()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getLatencyMs()
     * @author bmcmullin
     */
    @Test
    public void testGetLatencyMs() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        long result = underTest.getLatencyMs();

        // Then - assertions for result of method getLatencyMs()
        assertEquals(0L, result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMessage()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetMessage() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.getMessage();

        // Then - assertions for result of method getMessage()
        assertEquals("message", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getReasonCode()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getReasonCode()
     * @author bmcmullin
     */
    @Test
    public void testGetReasonCode() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.getReasonCode();

        // Then - assertions for result of method getReasonCode()
        assertEquals("reasoncode", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRequestId()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getRequestId()
     * @author bmcmullin
     */
    @Test
    public void testGetRequestId() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.getRequestId();

        // Then - assertions for result of method getRequestId()
        assertEquals("requestId", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalBatches()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#getTotalBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalBatches() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        int result = underTest.getTotalBatches();

        // Then - assertions for result of method getTotalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isContextTooLargeDetected()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#isContextTooLargeDetected()
     * @author bmcmullin
     */
    @Test
    public void testIsContextTooLargeDetected() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        boolean result = underTest.isContextTooLargeDetected();

        // Then - assertions for result of method isContextTooLargeDetected()
        assertFalse(result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRetryAttempted()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#isRetryAttempted()
     * @author bmcmullin
     */
    @Test
    public void testIsRetryAttempted() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        boolean result = underTest.isRetryAttempted();

        // Then - assertions for result of method isRetryAttempted()
        assertFalse(result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRetrySucceeded()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#isRetrySucceeded()
     * @author bmcmullin
     */
    @Test
    public void testIsRetrySucceeded() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        boolean result = underTest.isRetrySucceeded();

        // Then - assertions for result of method isRetrySucceeded()
        assertFalse(result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for of(String, int, int, String, String)
     *
     * @see com.sim.chatserver.model.review.BatchFailure#of(String, int, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOf() throws Throwable
    {
        // When
        String requestId = null; // UTA: configured value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        String reasonCode = "reasonCode"; // UTA: default value
        String message = "message"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for of(String, int, int, String, String)
     *
     * @see com.sim.chatserver.model.review.BatchFailure#of(String, int, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOf2() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 0; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        String reasonCode = "reasonCode"; // UTA: default value
        String message = "message"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for of(String, int, int, String, String)
     *
     * @see com.sim.chatserver.model.review.BatchFailure#of(String, int, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOf3() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 2; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: default value
        String message = "message"; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for reasonForCoverage()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#reasonForCoverage()
     * @author bmcmullin
     */
    @Test
    public void testReasonForCoverage() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.reasonForCoverage();

        // Then - assertions for result of method reasonForCoverage()
        assertEquals("batch processing failure", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        JsonObject result = underTest.toJson();

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(12, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.review.BatchFailure#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure underTest = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("BatchFailure{requestId='requestId', batchIndex=1, totalBatches=1, batchId='batch-1', reasonCode='reasoncode', httpStatus=0, retryAttempted=false, retrySucceeded=false, contextTooLargeDetected=false, latencyMs=0}", result);

        // Then - assertions for this instance of BatchFailure
        assertAll(() -> {
            assertEquals("requestId", underTest.getRequestId());
        }, () -> {
            assertEquals(1, underTest.getBatchIndex());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals("batch-1", underTest.getBatchId());
        }, () -> {
            assertEquals("reasoncode", underTest.getReasonCode());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertFalse(underTest.isRetryAttempted());
        }, () -> {
            assertFalse(underTest.isRetrySucceeded());
        }, () -> {
            assertFalse(underTest.isContextTooLargeDetected());
        }, () -> {
            assertEquals(0L, underTest.getLatencyMs());
        }, () -> {
            assertNotNull(underTest.getBatchChatIds());
            assertEquals(0, underTest.getBatchChatIds().size());
        });

    }
}
