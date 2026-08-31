package com.sim.chatserver.model.review;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReviewJobStatus.Builder;
import com.sim.chatserver.model.review.ReviewJobStatus.Phase;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Parasoft Jtest UTA: Test class for ReviewJobStatus
 *
 * @see com.sim.chatserver.model.review.ReviewJobStatus
 * @author bmcmullin
 */
public class ReviewJobStatusTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = ReviewJobStatus.builder();

    }

    /**
     * Parasoft Jtest UTA: Test for getActivity()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getActivity()
     * @author bmcmullin
     */
    @Test
    public void testGetActivity() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getActivity();

    }

    /**
     * Parasoft Jtest UTA: Test for getAllSelectedChatIds()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getAllSelectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetAllSelectedChatIds() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        List<String> result = underTest.getAllSelectedChatIds();

    }

    /**
     * Parasoft Jtest UTA: Test for getBatchProgressPercent()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getBatchProgressPercent()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchProgressPercent() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getBatchProgressPercent();

    }

    /**
     * Parasoft Jtest UTA: Test for getCompletedBatches()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getCompletedBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetCompletedBatches() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getCompletedBatches();

    }

    /**
     * Parasoft Jtest UTA: Test for getContentType()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getContentType()
     * @author bmcmullin
     */
    @Test
    public void testGetContentType() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getContentType();

    }

    /**
     * Parasoft Jtest UTA: Test for getCoveragePercent()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getCoveragePercent()
     * @author bmcmullin
     */
    @Test
    public void testGetCoveragePercent() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getCoveragePercent();

    }

    /**
     * Parasoft Jtest UTA: Test for getErrorMessage()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getErrorMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetErrorMessage() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getErrorMessage();

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchIndexes()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getFailedBatchIndexes()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchIndexes() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        List<Integer> result = underTest.getFailedBatchIndexes();

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatches()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getFailedBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatches() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getFailedBatches();

    }

    /**
     * Parasoft Jtest UTA: Test for getFinalReport()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getFinalReport()
     * @author bmcmullin
     */
    @Test
    public void testGetFinalReport() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getFinalReport();

    }

    /**
     * Parasoft Jtest UTA: Test for getFinishedAtEpochMs()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getFinishedAtEpochMs()
     * @author bmcmullin
     */
    @Test
    public void testGetFinishedAtEpochMs() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        long result = underTest.getFinishedAtEpochMs();

    }

    /**
     * Parasoft Jtest UTA: Test for getHttpStatus()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getHttpStatus()
     * @author bmcmullin
     */
    @Test
    public void testGetHttpStatus() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getHttpStatus();

    }

    /**
     * Parasoft Jtest UTA: Test for getJobId()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getJobId()
     * @author bmcmullin
     */
    @Test
    public void testGetJobId() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getJobId();

    }

    /**
     * Parasoft Jtest UTA: Test for getMessage()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetMessage() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getMessage();

    }

    /**
     * Parasoft Jtest UTA: Test for getMissingChatIds()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getMissingChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetMissingChatIds() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        List<String> result = underTest.getMissingChatIds();

    }

    /**
     * Parasoft Jtest UTA: Test for getPhase()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getPhase()
     * @author bmcmullin
     */
    @Test
    public void testGetPhase() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        Phase result = underTest.getPhase();

    }

    /**
     * Parasoft Jtest UTA: Test for getRawResponseBody()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getRawResponseBody()
     * @author bmcmullin
     */
    @Test
    public void testGetRawResponseBody() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getRawResponseBody();

    }

    /**
     * Parasoft Jtest UTA: Test for getRequestId()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getRequestId()
     * @author bmcmullin
     */
    @Test
    public void testGetRequestId() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.getRequestId();

    }

    /**
     * Parasoft Jtest UTA: Test for getRetries()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getRetries()
     * @author bmcmullin
     */
    @Test
    public void testGetRetries() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getRetries();

    }

    /**
     * Parasoft Jtest UTA: Test for getStartedAtEpochMs()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getStartedAtEpochMs()
     * @author bmcmullin
     */
    @Test
    public void testGetStartedAtEpochMs() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        long result = underTest.getStartedAtEpochMs();

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalBatches()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getTotalBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalBatches() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getTotalBatches();

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalSelected()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getTotalSelected()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalSelected() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = underTest.getTotalSelected();

    }

    /**
     * Parasoft Jtest UTA: Test for getUpdatedAtEpochMs()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getUpdatedAtEpochMs()
     * @author bmcmullin
     */
    @Test
    public void testGetUpdatedAtEpochMs() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        long result = underTest.getUpdatedAtEpochMs();

    }

    /**
     * Parasoft Jtest UTA: Test for getUsedChatIds()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getUsedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetUsedChatIds() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        List<String> result = underTest.getUsedChatIds();

    }

    /**
     * Parasoft Jtest UTA: Test for getWarnings()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#getWarnings()
     * @author bmcmullin
     */
    @Test
    public void testGetWarnings() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        List<String> result = underTest.getWarnings();

    }

    /**
     * Parasoft Jtest UTA: Test for hasFinalReport()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#hasFinalReport()
     * @author bmcmullin
     */
    @Test
    public void testHasFinalReport() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        boolean result = underTest.hasFinalReport();

    }

    /**
     * Parasoft Jtest UTA: Test for isCoverageComplete()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#isCoverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testIsCoverageComplete() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        boolean result = underTest.isCoverageComplete();

    }

    /**
     * Parasoft Jtest UTA: Test for isDone()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#isDone()
     * @author bmcmullin
     */
    @Test
    public void testIsDone() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        boolean result = underTest.isDone();

    }

    /**
     * Parasoft Jtest UTA: Test for isRunning()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#isRunning()
     * @author bmcmullin
     */
    @Test
    public void testIsRunning() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        boolean result = underTest.isRunning();

    }

    /**
     * Parasoft Jtest UTA: Test for isSuccess()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#isSuccess()
     * @author bmcmullin
     */
    @Test
    public void testIsSuccess() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        boolean result = underTest.isSuccess();

    }

    /**
     * Parasoft Jtest UTA: Test for progressPercent()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#progressPercent()
     * @author bmcmullin
     */
    @Test
    public void testProgressPercent() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        int result = invokeProgressPercent(underTest);

    }

    /**
     * Parasoft Jtest UTA: Test for queued(String, String, int)
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#queued(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testQueued() throws Throwable
    {
        // When
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus result = ReviewJobStatus.queued(jobId, requestId, totalSelected);

    }

    /**
     * Parasoft Jtest UTA: Test for queued(String, String, int)
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#queued(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testQueued2() throws Throwable
    {
        // When
        String jobId = "jobId"; // UTA: default value
        String requestId = "requestId"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus result = ReviewJobStatus.queued(jobId, requestId, totalSelected);

    }

    /**
     * Parasoft Jtest UTA: Test for queued(String, String, int)
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#queued(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testQueued3() throws Throwable
    {
        // When
        String jobId = null; // UTA: configured value
        String requestId = "requestId"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        assertThrows(IllegalArgumentException.class, () -> {
            ReviewJobStatus.queued(jobId, requestId, totalSelected);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        JsonObject result = underTest.toJson();

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.review.ReviewJobStatus#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String jobId = "jobId"; // UTA: configured value
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        ReviewJobStatus underTest = ReviewJobStatus.queued(jobId, requestId, totalSelected);

        // When
        String result = underTest.toString();

    }

    private static int invokeProgressPercent(ReviewJobStatus underTest) throws Exception {
        Method method = ReviewJobStatus.class.getDeclaredMethod("progressPercent");
        method.setAccessible(true);
        return ((Integer) method.invoke(underTest)).intValue();
    }
}
