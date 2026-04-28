package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for ReviewContextBuilderService
 *
 * @see com.sim.chatserver.service.ReviewContextBuilderService
 * @author bmcmullin
 */
public class ReviewContextBuilderServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = null; // UTA: configured value
        String result = underTest.buildBatchDeterministicHeader(totalSelected, totalBatches, batchIndex, batch);

    }

    /**
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        batch.add(item);
        String result = underTest.buildBatchDeterministicHeader(totalSelected, totalBatches, batchIndex, batch);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = null; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries);

        // Then - assertions for result of method buildContext(String, List)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext2() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        String result = underTest.buildContext(userMessage, entries);

        // Then - assertions for result of method buildContext(String, List)
        assertEquals("Selected chats context\n- total_selected: 1\n- sampled_for_evidence: 0\n- omitted_from_inline_evidence: 1\n\nCoverage index (sampled subset):\n\nPer-chat evidence:\n\nDeterministic coverage metadata:\n- exact_total_selected: 1\n- exact_included_count: 0\n- exact_omitted_count: 0\n- exact_included_ids: []\n- exact_omitted_ids: []\n\nCarry-forward IDs (not in inline evidence):\n- (none)\n\nBatch signals:\n", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext3() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        stratifiedSampleResult.add(item);
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item2 = mock(SelectedEntry.class);
        entries.add(item2);
        String result = underTest.buildContext(userMessage, entries);

        // Then - assertions for result of method buildContext(String, List)
        assertEquals("Selected chats context\n- total_selected: 1\n- sampled_for_evidence: 1\n- omitted_from_inline_evidence: 0\n\nCoverage index (sampled subset):\n(unknown)|da39a3ee5e6b4b0d3255bfef95601890afd80709\n\nPer-chat evidence:\n### Chat (unknown)\n- Created At: ?\n- Session ID: (none)\n- Prompt:\n(empty)\n- Response:\n(empty)\n\n\nDeterministic coverage metadata:\n- exact_total_selected: 1\n- exact_included_count: 1\n- exact_omitted_count: 0\n- exact_included_ids: [(unknown)]\n- exact_omitted_ids: []\n\nCarry-forward IDs (not in inline evidence):\n- (none)\n\nBatch signals:\n- Batch 1/1 size=1 avgPromptChars=0 avgResponseChars=0 matchedTerms=[]\r\n", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext4() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

        // Then - assertions for result of method buildContext(String, List, int)
        assertEquals("S", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext5() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        stratifiedSampleResult.add(item);
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item2 = mock(SelectedEntry.class);
        entries.add(item2);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

        // Then - assertions for result of method buildContext(String, List, int)
        assertEquals("S", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = null; // UTA: configured value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

        // Then - assertions for result of method buildMapBatchContext(String, List, int, int, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext2() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        batch.add(item);
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext3() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        batch.add(item);
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        expectedChatIds.add(item2);
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        int maxChars = 0; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

        // Then - assertions for result of method buildReduceContext(String, List, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

        // Then - assertions for result of method buildReduceContext(String, List, int)
        assertEquals("M", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

        // Then - assertions for result of method buildReduceContext(String, List, int)
        assertEquals("M", result);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext4() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

        // Then - assertions for result of method explodeLargeEntriesToSegments(List, int, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

        // Then - assertions for result of method explodeLargeEntriesToSegments(List, int, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

        // Then - assertions for result of method explodeLargeEntriesToSegments(List, int, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        SelectedEntry entry = null; // UTA: configured value
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntryToSegments(entry, promptChunkChars, responseChunkChars);

        // Then - assertions for result of method explodeLargeEntryToSegments(SelectedEntry, int, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMap(List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMap(List, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMap() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int batchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = underTest.splitForMap(entries, batchSize);

        // Then - assertions for result of method splitForMap(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMap(List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMap(List, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMap2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int batchSize = 0; // UTA: configured value
        List<List<SelectedEntry>> result = underTest.splitForMap(entries, batchSize);

        // Then - assertions for result of method splitForMap(List, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMapAdaptive(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMapAdaptive(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMapAdaptive() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int preferredBatchSize = 1; // UTA: default value
        int minBatchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = underTest.splitForMapAdaptive(entries, preferredBatchSize, minBatchSize);

        // Then - assertions for result of method splitForMapAdaptive(List, int, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

}
