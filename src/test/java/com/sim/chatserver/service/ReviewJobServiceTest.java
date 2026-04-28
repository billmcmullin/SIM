package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.service.ReviewJobService.JobTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for ReviewJobService
 *
 * @see com.sim.chatserver.service.ReviewJobService
 * @author bmcmullin
 */
public class ReviewJobServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for cancel(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cancel(String)
     * @author bmcmullin
     */
    @Test
    public void testCancel() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = null; // UTA: configured value
        boolean result = underTest.cancel(jobId);

        // Then - assertions for result of method cancel(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for cancel(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cancel(String)
     * @author bmcmullin
     */
    @Test
    public void testCancel2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: configured value
        boolean result = underTest.cancel(jobId);

        // Then - assertions for result of method cancel(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for cleanupFinishedBefore(long)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cleanupFinishedBefore(long)
     * @author bmcmullin
     */
    @Test
    public void testCleanupFinishedBefore() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        long cutoffEpochMs = 1L; // UTA: default value
        int result = underTest.cleanupFinishedBefore(cutoffEpochMs);

        // Then - assertions for result of method cleanupFinishedBefore(long)
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, int, boolean,
     * List, String, String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, int, boolean, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        missingChatIds.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        warnings.add(item3);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for exists(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#exists(String)
     * @author bmcmullin
     */
    @Test
    public void testExists() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = null; // UTA: configured value
        boolean result = underTest.exists(jobId);

        // Then - assertions for result of method exists(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for exists(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#exists(String)
     * @author bmcmullin
     */
    @Test
    public void testExists2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        boolean result = underTest.exists(jobId);

        // Then - assertions for result of method exists(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for failJob(String, int, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#failJob(String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailJob() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        underTest.failJob(jobId, httpStatus, errorMessage);

    }

    /**
     * Parasoft Jtest UTA: Test for getStatus(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#getStatus(String)
     * @author bmcmullin
     */
    @Test
    public void testGetStatus() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = null; // UTA: configured value
        ReviewJobStatus result = underTest.getStatus(jobId);

        // Then - assertions for result of method getStatus(String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getStatus(String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#getStatus(String)
     * @author bmcmullin
     */
    @Test
    public void testGetStatus2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: configured value
        ReviewJobStatus result = underTest.getStatus(jobId);

        // Then - assertions for result of method getStatus(String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for shutdownNow()
     *
     * @see com.sim.chatserver.service.ReviewJobService#shutdownNow()
     * @author bmcmullin
     */
    @Test
    public void testShutdownNow() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        underTest.shutdownNow();

    }

    /**
     * Parasoft Jtest UTA: Test for size()
     *
     * @see com.sim.chatserver.service.ReviewJobService#size()
     * @author bmcmullin
     */
    @Test
    public void testSize() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        int result = underTest.size();

        // Then - assertions for result of method size()
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for submit(String, int, JobTask)
     *
     * @see com.sim.chatserver.service.ReviewJobService#submit(String, int, JobTask)
     * @author bmcmullin
     */
    @Test
    public void testSubmit() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String requestId = null; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        JobTask task = mock(JobTask.class);
        String result = underTest.submit(requestId, totalSelected, task);

        // Then - assertions for result of method submit(String, int, ReviewJobService.JobTask)
        assertEquals("585a6bdf-2565-4246-a952-2bebfade9cef", result);

    }

    /**
     * Parasoft Jtest UTA: Test for submit(String, int, JobTask)
     *
     * @see com.sim.chatserver.service.ReviewJobService#submit(String, int, JobTask)
     * @author bmcmullin
     */
    @Test
    public void testSubmit2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String requestId = "requestId"; // UTA: configured value
        int totalSelected = 1; // UTA: default value
        JobTask task = mock(JobTask.class);
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.submit(requestId, totalSelected, task);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        missingChatIds.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingChatIds.add(item3);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item4 = 1; // UTA: default value
        failedBatchIndexes.add(item4);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        missingChatIds.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingChatIds.add(item3);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item4 = 1; // UTA: default value
        failedBatchIndexes.add(item4);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

}
