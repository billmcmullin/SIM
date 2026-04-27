package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.CoverageSummary.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for CoverageSummary
 *
 * @see com.sim.chatserver.model.review.CoverageSummary
 * @author bmcmullin
 */
public class CoverageSummaryTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = CoverageSummary.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds() throws Throwable
    {
        // When
        List<String> allChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // Then - assertions for result of method fromIds(List, List, List, int, int, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getChatsProvided());
        }, () -> {
            assertEquals(0, result.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(0, result.getChatsNotUsed());
        }, () -> {
            assertNotNull(result.getAllSelectedChatIds());
            assertEquals(0, result.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.getUsedChatIds());
            assertEquals(0, result.getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getNotUsedChatIds());
            assertEquals(0, result.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getReasonsChatsNotUsed());
            assertEquals(0, result.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, result.getTotalBatches());
        }, () -> {
            assertEquals(1, result.getSuccessfulBatches());
        }, () -> {
            assertNotNull(result.getFailedBatchIndexes());
            assertEquals(0, result.getFailedBatchIndexes().size());
        }, () -> {
            assertTrue(result.isCoverageComplete());
        }, () -> {
            assertEquals(0, result.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds2() throws Throwable
    {
        // When
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        usedChatIds.add(item2);
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        reasonsChatsNotUsed.add(item3);
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item4 = 1; // UTA: default value
        failedBatchIndexes.add(item4);
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // Then - assertions for result of method fromIds(List, List, List, int, int, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(1, result.getChatsProvided());
        }, () -> {
            assertEquals(1, result.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, result.getChatsNotUsed());
        }, () -> {
            assertNotNull(result.getAllSelectedChatIds());
            assertEquals(1, result.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(result.getUsedChatIds());
            assertEquals(1, result.getUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getNotUsedChatIds());
            assertEquals(1, result.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(result.getReasonsChatsNotUsed());
            assertEquals(1, result.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, result.getTotalBatches());
        }, () -> {
            assertEquals(1, result.getSuccessfulBatches());
        }, () -> {
            assertNotNull(result.getFailedBatchIndexes());
            assertEquals(1, result.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(result.isCoverageComplete());
        }, () -> {
            assertEquals(100, result.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds3() throws Throwable
    {
        // When
        List<String> allChatIds = null; // UTA: configured value
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        usedChatIds.add(item);
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        reasonsChatsNotUsed.add(item2);
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item3 = 1; // UTA: default value
        failedBatchIndexes.add(item3);
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

    }

    /**
     * Parasoft Jtest UTA: Test for getAllSelectedChatIds()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getAllSelectedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetAllSelectedChatIds() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        List<String> result = underTest.getAllSelectedChatIds();

        // Then - assertions for result of method getAllSelectedChatIds()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsNotUsed()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getChatsNotUsed()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsNotUsed() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getChatsNotUsed();

        // Then - assertions for result of method getChatsNotUsed()
        assertEquals(1, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsProvided()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getChatsProvided()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsProvided() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getChatsProvided();

        // Then - assertions for result of method getChatsProvided()
        assertEquals(1, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsUsedInAnalysis()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getChatsUsedInAnalysis()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsUsedInAnalysis() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getChatsUsedInAnalysis();

        // Then - assertions for result of method getChatsUsedInAnalysis()
        assertEquals(0, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCoveragePercent()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getCoveragePercent()
     * @author bmcmullin
     */
    @Test
    public void testGetCoveragePercent() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getCoveragePercent();

        // Then - assertions for result of method getCoveragePercent()
        assertEquals(0, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchCount()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getFailedBatchCount()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchCount() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getFailedBatchCount();

        // Then - assertions for result of method getFailedBatchCount()
        assertEquals(1, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFailedBatchIndexes()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getFailedBatchIndexes()
     * @author bmcmullin
     */
    @Test
    public void testGetFailedBatchIndexes() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        List<Integer> result = underTest.getFailedBatchIndexes();

        // Then - assertions for result of method getFailedBatchIndexes()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getNotUsedChatIds()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getNotUsedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetNotUsedChatIds() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        List<String> result = underTest.getNotUsedChatIds();

        // Then - assertions for result of method getNotUsedChatIds()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getReasonsChatsNotUsed()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getReasonsChatsNotUsed()
     * @author bmcmullin
     */
    @Test
    public void testGetReasonsChatsNotUsed() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        List<String> result = underTest.getReasonsChatsNotUsed();

        // Then - assertions for result of method getReasonsChatsNotUsed()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSuccessfulBatches()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getSuccessfulBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetSuccessfulBatches() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getSuccessfulBatches();

        // Then - assertions for result of method getSuccessfulBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalBatches()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getTotalBatches()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalBatches() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        int result = underTest.getTotalBatches();

        // Then - assertions for result of method getTotalBatches()
        assertEquals(1, result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsedChatIds()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#getUsedChatIds()
     * @author bmcmullin
     */
    @Test
    public void testGetUsedChatIds() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        List<String> result = underTest.getUsedChatIds();

        // Then - assertions for result of method getUsedChatIds()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hasFailures()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#hasFailures()
     * @author bmcmullin
     */
    @Test
    public void testHasFailures() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        boolean result = underTest.hasFailures();

        // Then - assertions for result of method hasFailures()
        assertTrue(result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isCoverageComplete()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#isCoverageComplete()
     * @author bmcmullin
     */
    @Test
    public void testIsCoverageComplete() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        boolean result = underTest.isCoverageComplete();

        // Then - assertions for result of method isCoverageComplete()
        assertFalse(result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        JsonObject result = underTest.toJson();

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(13, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        List<String> allChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        allChatIds.add(item);
        List<String> usedChatIds = new ArrayList<String>(); // UTA: default value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary underTest = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("CoverageSummary{chatsProvided=1, chatsUsedInAnalysis=0, chatsNotUsed=1, totalBatches=1, successfulBatches=1, failedBatchIndexes=[1], coverageComplete=false, coveragePercent=0}", result);

        // Then - assertions for this instance of CoverageSummary
        assertAll(() -> {
            assertEquals(1, underTest.getChatsProvided());
        }, () -> {
            assertEquals(0, underTest.getChatsUsedInAnalysis());
        }, () -> {
            assertEquals(1, underTest.getChatsNotUsed());
        }, () -> {
            assertNotNull(underTest.getAllSelectedChatIds());
            assertEquals(1, underTest.getAllSelectedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getUsedChatIds());
            assertEquals(0, underTest.getUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getNotUsedChatIds());
            assertEquals(1, underTest.getNotUsedChatIds().size());
        }, () -> {
            assertNotNull(underTest.getReasonsChatsNotUsed());
            assertEquals(0, underTest.getReasonsChatsNotUsed().size());
        }, () -> {
            assertEquals(1, underTest.getTotalBatches());
        }, () -> {
            assertEquals(1, underTest.getSuccessfulBatches());
        }, () -> {
            assertNotNull(underTest.getFailedBatchIndexes());
            assertEquals(1, underTest.getFailedBatchIndexes().size());
        }, () -> {
            assertFalse(underTest.isCoverageComplete());
        }, () -> {
            assertEquals(0, underTest.getCoveragePercent());
        });

    }
}
