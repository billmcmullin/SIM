package com.sim.chatserver.model.review;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.MapBatchResult.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult result = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

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
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        MapBatchResult result = MapBatchResult.failed(requestId, batchIndex, totalBatches, batchId, httpStatus, errorMessage);

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
    public void testFailed5() throws Throwable
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
    public void testFailed6() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 1; // UTA: configured value
        int totalBatches = 0; // UTA: configured value
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
    public void testFailed7() throws Throwable
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
     * Parasoft Jtest UTA: Test for failed(String, int, int, String, int, String)
     *
     * @see com.sim.chatserver.model.review.MapBatchResult#failed(String, int, int, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailed8() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 2; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = null; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
    public void testFailed9() throws Throwable
    {
        // When
        String requestId = "requestId"; // UTA: configured value
        int batchIndex = 2; // UTA: configured value
        int totalBatches = 1; // UTA: configured value
        String batchId = "batchId"; // UTA: configured value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
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

    }
}
