package com.sim.chatserver.model.review;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.BatchFailure.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = "message"; // UTA: default value
        BatchFailure result = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

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
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = "reasonCode"; // UTA: configured value
        String message = null; // UTA: configured value
        BatchFailure result = BatchFailure.of(requestId, batchIndex, totalBatches, reasonCode, message);

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
    public void testOf4() throws Throwable
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
    public void testOf5() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 0; // UTA: configured value
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
    public void testOf6() throws Throwable
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
     * Parasoft Jtest UTA: Test for of(String, int, int, String, String)
     *
     * @see com.sim.chatserver.model.review.BatchFailure#of(String, int, int, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOf7() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String reasonCode = null; // UTA: configured value
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

    }
}
