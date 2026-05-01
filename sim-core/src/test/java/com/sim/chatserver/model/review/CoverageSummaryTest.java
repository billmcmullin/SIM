package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.CoverageSummary.Builder;

import jakarta.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        List<String> usedChatIds = null; // UTA: configured value
        List<String> reasonsChatsNotUsed = null; // UTA: configured value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds4() throws Throwable
    {
        // When
        List<String> allChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        reasonsChatsNotUsed.add(item);
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds5() throws Throwable
    {
        // When
        List<String> allChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        reasonsChatsNotUsed.add(item);
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        CoverageSummary result = CoverageSummary.fromIds(allChatIds, usedChatIds, reasonsChatsNotUsed, totalBatches, successfulBatches, failedBatchIndexes);

    }

    /**
     * Parasoft Jtest UTA: Test for fromIds(List)
     *
     * @see com.sim.chatserver.model.review.CoverageSummary#fromIds(List)
     * @author bmcmullin
     */
    @Test
    public void testFromIds6() throws Throwable
    {
        // When
        List<String> allChatIds = null; // UTA: configured value
        List<String> usedChatIds = null; // UTA: configured value
        List<String> reasonsChatsNotUsed = new ArrayList<String>(); // UTA: default value
        int totalBatches = 1; // UTA: default value
        int successfulBatches = 1; // UTA: default value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
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

    }
}
