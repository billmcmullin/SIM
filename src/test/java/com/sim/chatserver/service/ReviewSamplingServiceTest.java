package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for ReviewSamplingService
 *
 * @see com.sim.chatserver.service.ReviewSamplingService
 * @author bmcmullin
 */
public class ReviewSamplingServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

        // Then - assertions for result of method dedupeByChatId(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

        // Then - assertions for result of method dedupeByChatId(List)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for keywordTerms(String, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#keywordTerms(String, int)
     * @author bmcmullin
     */
    @Test
    public void testKeywordTerms() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        String text = null; // UTA: configured value
        int maxTerms = 1; // UTA: default value
        List<String> result = underTest.keywordTerms(text, maxTerms);

        // Then - assertions for result of method keywordTerms(String, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for keywordTerms(String, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#keywordTerms(String, int)
     * @author bmcmullin
     */
    @Test
    public void testKeywordTerms2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        String text = "text"; // UTA: configured value
        int maxTerms = 1; // UTA: default value
        List<String> result = underTest.keywordTerms(text, maxTerms);

        // Then - assertions for result of method keywordTerms(String, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = null; // UTA: configured value
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        Set<String> result = underTest.matchedTerms(e, terms);

        // Then - assertions for result of method matchedTerms(SelectedEntry, List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        Set<String> result = underTest.matchedTerms(e, terms);

        // Then - assertions for result of method matchedTerms(SelectedEntry, List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for newest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#newest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testNewest() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.newest(entries, n);

        // Then - assertions for result of method newest(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for newest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#newest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testNewest2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.newest(entries, n);

        // Then - assertions for result of method newest(List, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for oldest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#oldest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testOldest() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.oldest(entries, n);

        // Then - assertions for result of method oldest(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for oldest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#oldest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testOldest2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.oldest(entries, n);

        // Then - assertions for result of method oldest(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for oldest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#oldest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testOldest3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.oldest(entries, n);

        // Then - assertions for result of method oldest(List, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for randomSample(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#randomSample(List, int)
     * @author bmcmullin
     */
    @Test
    public void testRandomSample() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.randomSample(entries, n);

        // Then - assertions for result of method randomSample(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for randomSample(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#randomSample(List, int)
     * @author bmcmullin
     */
    @Test
    public void testRandomSample2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.randomSample(entries, n);

        // Then - assertions for result of method randomSample(List, int)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = null; // UTA: configured value
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int result = underTest.scoreEntry(e, terms);

        // Then - assertions for result of method scoreEntry(SelectedEntry, List)
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int result = underTest.scoreEntry(e, terms);

        // Then - assertions for result of method scoreEntry(SelectedEntry, List)
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for stratifiedSample(List, String, int, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#stratifiedSample(List, String, int, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testStratifiedSample() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        String userMessage = "userMessage"; // UTA: default value
        int topRelevantCount = 1; // UTA: default value
        int newestCount = 1; // UTA: default value
        int oldestCount = 1; // UTA: default value
        int randomCount = 1; // UTA: default value
        List<SelectedEntry> result = underTest.stratifiedSample(entries, userMessage, topRelevantCount, newestCount, oldestCount, randomCount);

        // Then - assertions for result of method stratifiedSample(List, String, int, int, int, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for stratifiedSample(List, String, int, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#stratifiedSample(List, String, int, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testStratifiedSample2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        String userMessage = null; // UTA: configured value
        int topRelevantCount = 1; // UTA: default value
        int newestCount = 1; // UTA: default value
        int oldestCount = 1; // UTA: default value
        int randomCount = 1; // UTA: default value
        List<SelectedEntry> result = underTest.stratifiedSample(entries, userMessage, topRelevantCount, newestCount, oldestCount, randomCount);

        // Then - assertions for result of method stratifiedSample(List, String, int, int, int, int)
        assertNotNull(result);
        assertEquals(4, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for topRelevant(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#topRelevant(List, int)
     * @author bmcmullin
     */
    @Test
    public void testTopRelevant() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.topRelevant(entries, terms, n);

        // Then - assertions for result of method topRelevant(List, List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

}
