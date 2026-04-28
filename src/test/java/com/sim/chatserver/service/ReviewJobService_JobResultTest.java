package com.sim.chatserver.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.ReviewJobService.JobResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for JobResult
 *
 * @see com.sim.chatserver.service.ReviewJobService.JobResult
 * @author bmcmullin
 */
public class ReviewJobService_JobResultTest
{

    /**
     * Parasoft Jtest UTA: Test for allSelectedChatIds()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#allSelectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testAllSelectedChatIds() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        List<String> result = underTest.allSelectedChatIds();

        // Then - assertions for result of method allSelectedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for completedBatches()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#completedBatches()
     * @author bmcmullin
     */
    @Test
    public void testCompletedBatches() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.completedBatches();

        // Then - assertions for result of method completedBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for contentType()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#contentType()
     * @author bmcmullin
     */
    @Test
    public void testContentType() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        String result = underTest.contentType();

        // Then - assertions for result of method contentType()
        assertEquals("application/json", result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for coverageComplete()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#coverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testCoverageComplete() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        boolean result = underTest.coverageComplete();

        // Then - assertions for result of method coverageComplete()
        assertTrue(result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for coveragePercent()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#coveragePercent()
     * @author bmcmullin
     */
    @Test
    public void testCoveragePercent() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.coveragePercent();

        // Then - assertions for result of method coveragePercent()
        assertEquals(0, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for errorMessage()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#errorMessage()
     * @author bmcmullin
     */
    @Test
    public void testErrorMessage() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        String result = underTest.errorMessage();

        // Then - assertions for result of method errorMessage()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failedBatchIndexes()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#failedBatchIndexes()
     * @author bmcmullin
     */
    @Test
    public void testFailedBatchIndexes() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        List<Integer> result = underTest.failedBatchIndexes();

        // Then - assertions for result of method failedBatchIndexes()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for failedBatches()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#failedBatches()
     * @author bmcmullin
     */
    @Test
    public void testFailedBatches() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.failedBatches();

        // Then - assertions for result of method failedBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for finalReport()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#finalReport()
     * @author bmcmullin
     */
    @Test
    public void testFinalReport() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        String result = underTest.finalReport();

        // Then - assertions for result of method finalReport()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for httpStatus()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#httpStatus()
     * @author bmcmullin
     */
    @Test
    public void testHttpStatus() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.httpStatus();

        // Then - assertions for result of method httpStatus()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for message()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#message()
     * @author bmcmullin
     */
    @Test
    public void testMessage() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        String result = underTest.message();

        // Then - assertions for result of method message()
        assertEquals("message", result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for missingChatIds()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#missingChatIds()
     * @author bmcmullin
     */
    @Test
    public void testMissingChatIds() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        List<String> result = underTest.missingChatIds();

        // Then - assertions for result of method missingChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for rawResponseBody()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#rawResponseBody()
     * @author bmcmullin
     */
    @Test
    public void testRawResponseBody() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        String result = underTest.rawResponseBody();

        // Then - assertions for result of method rawResponseBody()
        assertEquals("", result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for retries()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#retries()
     * @author bmcmullin
     */
    @Test
    public void testRetries() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.retries();

        // Then - assertions for result of method retries()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for success()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#success()
     * @author bmcmullin
     */
    @Test
    public void testSuccess() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        boolean result = underTest.success();

        // Then - assertions for result of method success()
        assertFalse(result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for totalBatches()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#totalBatches()
     * @author bmcmullin
     */
    @Test
    public void testTotalBatches() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        int result = underTest.totalBatches();

        // Then - assertions for result of method totalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for usedChatIds()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#usedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testUsedChatIds() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        List<String> result = underTest.usedChatIds();

        // Then - assertions for result of method usedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertNotNull(underTest.warnings());
            assertEquals(0, underTest.warnings().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for warnings()
     *
     * @see com.sim.chatserver.service.ReviewJobService.JobResult#warnings()
     * @author bmcmullin
     */
    @Test
    public void testWarnings() throws Throwable
    {
        // Given
        int httpStatus = 1; // UTA: default value
        boolean success = false; // UTA: default value
        String message = "message"; // UTA: default value
        String errorMessage = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int completedBatches = 1; // UTA: default value
        int failedBatches = 1; // UTA: default value
        int retries = 1; // UTA: default value
        List<String> allSelectedChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> missingChatIds = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> warnings = null; // UTA: configured value
        String finalReport = null; // UTA: configured value
        String rawResponseBody = null; // UTA: configured value
        String contentType = null; // UTA: configured value
        JobResult underTest = new JobResult(httpStatus, success, message, errorMessage, totalBatches, completedBatches, failedBatches, retries, allSelectedChatIds, usedChatIds, missingChatIds, failedBatchIndexes, warnings, finalReport, rawResponseBody, contentType);

        // When
        List<String> result = underTest.warnings();

        // Then - assertions for result of method warnings()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of ReviewJobService.JobResult
        assertAll(() -> {
            assertEquals(1, underTest.httpStatus());
        }, () -> {
            assertFalse(underTest.success());
        }, () -> {
            assertEquals("message", underTest.message());
        }, () -> {
            assertEquals("", underTest.errorMessage());
        }, () -> {
            assertEquals(1, underTest.totalBatches());
        }, () -> {
            assertEquals(1, underTest.completedBatches());
        }, () -> {
            assertEquals(1, underTest.failedBatches());
        }, () -> {
            assertEquals(1, underTest.retries());
        }, () -> {
            assertNotNull(underTest.allSelectedChatIds());
            assertEquals(0, underTest.allSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.usedChatIds());
            assertEquals(0, underTest.usedChatIds().size());
        }, () -> {
            assertNotNull(underTest.missingChatIds());
            assertEquals(0, underTest.missingChatIds().size());
        }, () -> {
            assertNotNull(underTest.failedBatchIndexes());
            assertEquals(0, underTest.failedBatchIndexes().size());
        }, () -> {
            assertEquals("", underTest.finalReport());
        }, () -> {
            assertEquals("", underTest.rawResponseBody());
        }, () -> {
            assertEquals("application/json", underTest.contentType());
        });

    }
}
