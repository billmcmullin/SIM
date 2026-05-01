package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.service.ReviewJobService.JobTask;

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

    }

    /**
     * Parasoft Jtest UTA: Test for cleanupFinishedBefore(long)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cleanupFinishedBefore(long)
     * @author bmcmullin
     */
    @Test
    public void testCleanupFinishedBefore2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        long cutoffEpochMs = 1; // UTA: configured value
        int result = underTest.cleanupFinishedBefore(cutoffEpochMs);

    }

    /**
     * Parasoft Jtest UTA: Test for cleanupFinishedBefore(long)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cleanupFinishedBefore(long)
     * @author bmcmullin
     */
    @Test
    public void testCleanupFinishedBefore3() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        long cutoffEpochMs = 2; // UTA: configured value
        int result = underTest.cleanupFinishedBefore(cutoffEpochMs);

    }

    /**
     * Parasoft Jtest UTA: Test for cleanupFinishedBefore(long)
     *
     * @see com.sim.chatserver.service.ReviewJobService#cleanupFinishedBefore(long)
     * @author bmcmullin
     */
    @Test
    public void testCleanupFinishedBefore4() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        long cutoffEpochMs = 0; // UTA: configured value
        int result = underTest.cleanupFinishedBefore(cutoffEpochMs);

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
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, int, boolean,
     * List, String, String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, int, boolean, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob2() throws Throwable
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob3() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = true; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob4() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = null; // UTA: configured value
        String errorMessage = "errorMessage"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob5() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        warnings.add(item);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob6() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = null; // UTA: configured value
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob7() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob8() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob9() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob10() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        warnings.add(item2);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob11() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: configured value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        warnings.add(item);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
    public void testCompleteJob12() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: configured value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob13() throws Throwable
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
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item5 = "item5"; // UTA: default value
        warnings.add(item5);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob14() throws Throwable
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
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob15() throws Throwable
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
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob16() throws Throwable
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
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob17() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = true; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = "errorMessage"; // UTA: default value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob18() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = null; // UTA: configured value
        String errorMessage = "errorMessage"; // UTA: default value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob19() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        warnings.add(item3);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob20() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = null; // UTA: configured value
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob21() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = "finalReport"; // UTA: default value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob22() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = "rawResponseBody"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob23() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = "contentType"; // UTA: default value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob24() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        warnings.add(item4);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob25() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: configured value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        warnings.add(item3);
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

    }

    /**
     * Parasoft Jtest UTA: Test for completeJob(String, int, boolean, String, String, int, int, int, int, List, String,
     * String, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#completeJob(String, int, boolean, String, String, int, int, int,
     *      int, List, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCompleteJob26() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: configured value
        String message = "message"; // UTA: configured value
        String errorMessage = null; // UTA: configured value
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
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        List<String> warnings = new ArrayList<String>(); // UTA: default value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        underTest.completeJob(jobId, httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

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
     * Parasoft Jtest UTA: Test for failJob(String, int, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#failJob(String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testFailJob2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int httpStatus = 1; // UTA: default value
        String errorMessage = null; // UTA: configured value
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
        String requestId = "requestId"; // UTA: default value
        int totalSelected = 1; // UTA: default value
        JobTask task = mock(JobTask.class);
        String result = underTest.submit(requestId, totalSelected, task);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchCompleted(String, int, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchCompleted(String, int, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchCompleted() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingChatIds.add(item3);
        underTest.updateMapBatchCompleted(jobId, round, maxRounds, batchIndex, totalBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchCompleted(String, int, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchCompleted(String, int, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchCompleted2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateMapBatchCompleted(jobId, round, maxRounds, batchIndex, totalBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchCompleted(String, int, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchCompleted(String, int, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchCompleted3() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        underTest.updateMapBatchCompleted(jobId, round, maxRounds, batchIndex, totalBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchCompleted(String, int, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchCompleted(String, int, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchCompleted4() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateMapBatchCompleted(jobId, round, maxRounds, batchIndex, totalBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchCompleted(String, int, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchCompleted(String, int, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchCompleted5() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        underTest.updateMapBatchCompleted(jobId, round, maxRounds, batchIndex, totalBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchStarted(String, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchStarted(String, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchStarted() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingChatIds.add(item3);
        underTest.updateMapBatchStarted(jobId, round, maxRounds, batchIndex, totalBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchStarted(String, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchStarted(String, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchStarted2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateMapBatchStarted(jobId, round, maxRounds, batchIndex, totalBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchStarted(String, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchStarted(String, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchStarted3() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        underTest.updateMapBatchStarted(jobId, round, maxRounds, batchIndex, totalBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchStarted(String, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchStarted(String, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchStarted4() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateMapBatchStarted(jobId, round, maxRounds, batchIndex, totalBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapBatchStarted(String, int, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapBatchStarted(String, int, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapBatchStarted5() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int round = 1; // UTA: default value
        int maxRounds = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        underTest.updateMapBatchStarted(jobId, round, maxRounds, batchIndex, totalBatches, allSelectedChatIds, usedChatIds, missingChatIds);

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
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
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
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress3() throws Throwable
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
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress4() throws Throwable
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
        int coveragePercent = -1; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress5() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = null; // UTA: configured value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress6() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress7() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress8() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress9() throws Throwable
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
        int coveragePercent = 1; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress10() throws Throwable
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
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress11() throws Throwable
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
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress12() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress13() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress14() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress15() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = null; // UTA: configured value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress16() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        String message = "message"; // UTA: default value
        underTest.updateMapProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateMapProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateMapProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateMapProgress17() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
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
        int coveragePercent = 1; // UTA: default value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress3() throws Throwable
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
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress4() throws Throwable
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
        int coveragePercent = -1; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress5() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = null; // UTA: configured value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress6() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress7() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress8() throws Throwable
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
        int coveragePercent = 0; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, int, boolean, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, int, boolean,
     *      List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress9() throws Throwable
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
        int coveragePercent = 1; // UTA: configured value
        boolean coverageComplete = false; // UTA: default value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, coveragePercent, coverageComplete, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress10() throws Throwable
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

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress11() throws Throwable
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
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress12() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress13() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress14() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress15() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        String message = null; // UTA: configured value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress16() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        String message = "message"; // UTA: default value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceProgress(String, int, int, int, int, List, String)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceProgress(String, int, int, int, int, List, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceProgress17() throws Throwable
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        String message = "message"; // UTA: configured value
        underTest.updateReduceProgress(jobId, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, message);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceStarted(String, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceStarted(String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceStarted() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingChatIds.add(item3);
        underTest.updateReduceStarted(jobId, totalBatches, completedBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceStarted(String, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceStarted(String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceStarted2() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateReduceStarted(jobId, totalBatches, completedBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceStarted(String, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceStarted(String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceStarted3() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        underTest.updateReduceStarted(jobId, totalBatches, completedBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceStarted(String, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceStarted(String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceStarted4() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        missingChatIds.add(item2);
        underTest.updateReduceStarted(jobId, totalBatches, completedBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for updateReduceStarted(String, int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewJobService#updateReduceStarted(String, int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testUpdateReduceStarted5() throws Throwable
    {
        // Given
        int poolSize = 1; // UTA: default value
        ReviewJobService underTest = new ReviewJobService(poolSize);

        // When
        String jobId = "jobId"; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        List<String> allSelectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allSelectedChatIds.add(item);
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = new ArrayList<String>(); // UTA: default value
        underTest.updateReduceStarted(jobId, totalBatches, completedBatches, failedBatches, allSelectedChatIds, usedChatIds, missingChatIds);

    }

}
