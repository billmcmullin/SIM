package com.sim.chatserver.model.review;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReviewJobStatus.Builder;
import com.sim.chatserver.model.review.ReviewJobStatus.Phase;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        // Then - assertions for result of method builder()
        assertNotNull(result);

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

        // Then - assertions for result of method getAllSelectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941385L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941385L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getCompletedBatches()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941448L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941448L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getContentType()
        assertEquals("application/json", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941532L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941532L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getCoveragePercent()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertEquals(1777300941429L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941429L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getErrorMessage()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941393L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941393L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getFailedBatchIndexes()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941480L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941480L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getFailedBatches()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941433L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941433L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getFinalReport()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941445L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941445L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getFinishedAtEpochMs()
        assertEquals(0L, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941474L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941474L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getHttpStatus()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941389L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941389L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getJobId()
        assertEquals("jobId", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941396L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941396L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getMessage()
        assertEquals("Queued", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941482L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941482L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getMissingChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941443L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941443L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getPhase()
        assertEquals(Phase.QUEUED, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941400L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941400L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getRawResponseBody()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941486L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941486L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getRequestId()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941495L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941495L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getRetries()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941484L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941484L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getStartedAtEpochMs()
        assertEquals(1777300941535L, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941535L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getTotalBatches()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941491L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941491L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getTotalSelected()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941428L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941428L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getUpdatedAtEpochMs()
        assertEquals(1777300941404L, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941404L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getUsedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(0, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getMissingChatIds());
            assertEquals(0, underTest.getMissingChatIds().size());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        }, () -> {
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941440L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941440L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method getWarnings()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941528L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941528L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        });

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

        // Then - assertions for result of method hasFinalReport()
        assertFalse(result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941476L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941476L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method isCoverageComplete()
        assertTrue(result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertEquals(1777300941478L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941478L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method isDone()
        assertFalse(result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941488L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941488L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method isSuccess()
        assertFalse(result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941381L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941381L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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
        int result = underTest.progressPercent();

        // Then - assertions for result of method progressPercent()
        assertEquals(5, result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300940052L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300940052L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(27, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941498L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941498L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

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

        // Then - assertions for result of method toString()
        assertEquals("ReviewJobStatus{jobId='jobId', requestId='', phase=QUEUED, done=false, success=false, totalSelected=1, totalBatches=0, completedBatches=0, failedBatches=0, retries=0, allSelectedChatIds=0, usedChatIds=0, missingChatIds=0, coveragePercent=0, coverageComplete=true, httpStatus=0}", result);

        // Then - assertions for this instance of ReviewJobStatus
        assertAll(() -> {
            assertEquals("jobId", underTest.getJobId());
        }, () -> {
            assertEquals("", underTest.getRequestId());
        }, () -> {
            assertEquals(Phase.QUEUED, underTest.getPhase());
        }, () -> {
            assertFalse(underTest.isDone());
        }, () -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals(1, underTest.getTotalSelected());
        }, () -> {
            assertEquals(0, underTest.getTotalBatches());
        }, () -> {
            assertEquals(0, underTest.getCompletedBatches());
        }, () -> {
            assertEquals(0, underTest.getFailedBatches());
        }, () -> {
            assertEquals(0, underTest.getRetries());
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
            assertTrue(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(1777300941410L, underTest.getStartedAtEpochMs());
        }, () -> {
            assertEquals(1777300941410L, underTest.getUpdatedAtEpochMs());
        }, () -> {
            assertEquals(0L, underTest.getFinishedAtEpochMs());
        }, () -> {
            assertEquals(0, underTest.getHttpStatus());
        }, () -> {
            assertEquals("Queued", underTest.getMessage());
        }, () -> {
            assertEquals("", underTest.getErrorMessage());
        }, () -> {
            assertEquals("", underTest.getFinalReport());
        }, () -> {
            assertEquals("", underTest.getRawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.getContentType());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(0, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.getWarnings());
            assertEquals(0, underTest.getWarnings().size());
        });

    }
}
