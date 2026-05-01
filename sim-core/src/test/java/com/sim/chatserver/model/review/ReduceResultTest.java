package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReduceResult.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String requestId = "requestId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        ReduceResult result = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

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

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed4() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        ReduceResult result = ReduceResult.failed(requestId, httpStatus, errorMessage, totalSelected, totalBatches, mapOutputsReceived, failedBatchIndexes);

    }

    /**
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed5() throws Throwable
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
     * Parasoft Jtest UTA: Test for failed(String, int, String, int, int, int, List)
     *
     * @see com.sim.chatserver.model.review.ReduceResult#failed(String, int, String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testFailed6() throws Throwable
    {
        // When
        String requestId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int mapOutputsReceived = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
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

    }
}
