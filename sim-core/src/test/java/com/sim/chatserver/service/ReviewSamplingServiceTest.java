package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;

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
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId5() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId6() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        entries.add(item2);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId7() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);
        entries.add(item2);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

    }

    /**
     * Parasoft Jtest UTA: Test for dedupeByChatId(List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#dedupeByChatId(List)
     * @author bmcmullin
     */
    @Test
    public void testDedupeByChatId8() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);
        entries.add(item2);
        List<SelectedEntry> result = underTest.dedupeByChatId(entries);

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
        List<String> terms = null; // UTA: configured value
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms4() throws Throwable
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
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms5() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms6() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms7() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms8() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms9() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms10() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms11() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms12() throws Throwable
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
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms13() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms14() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        Set<String> result = underTest.matchedTerms(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for matchedTerms(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#matchedTerms(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testMatchedTerms15() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        Set<String> result = underTest.matchedTerms(e, terms);

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
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.newest(entries, n);

    }

    /**
     * Parasoft Jtest UTA: Test for newest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#newest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testNewest3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 0; // UTA: configured value
        List<SelectedEntry> result = underTest.newest(entries, n);

    }

    /**
     * Parasoft Jtest UTA: Test for newest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#newest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testNewest4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.newest(entries, n);

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
        int n = 0; // UTA: configured value
        List<SelectedEntry> result = underTest.oldest(entries, n);

    }

    /**
     * Parasoft Jtest UTA: Test for oldest(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#oldest(List, int)
     * @author bmcmullin
     */
    @Test
    public void testOldest4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.oldest(entries, n);

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
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.randomSample(entries, n);

    }

    /**
     * Parasoft Jtest UTA: Test for randomSample(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#randomSample(List, int)
     * @author bmcmullin
     */
    @Test
    public void testRandomSample3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 0; // UTA: configured value
        List<SelectedEntry> result = underTest.randomSample(entries, n);

    }

    /**
     * Parasoft Jtest UTA: Test for randomSample(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#randomSample(List, int)
     * @author bmcmullin
     */
    @Test
    public void testRandomSample4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.randomSample(entries, n);

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
        List<String> terms = null; // UTA: configured value
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry4() throws Throwable
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
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry5() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry6() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry7() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry8() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry9() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry10() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry11() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry12() throws Throwable
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
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry13() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry14() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: default value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int result = underTest.scoreEntry(e, terms);

    }

    /**
     * Parasoft Jtest UTA: Test for scoreEntry(SelectedEntry, List)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#scoreEntry(SelectedEntry, List)
     * @author bmcmullin
     */
    @Test
    public void testScoreEntry15() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        SelectedEntry e = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(e.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(e.getResponse()).thenReturn(getResponseResult);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int result = underTest.scoreEntry(e, terms);

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
        String userMessage = "userMessage"; // UTA: default value
        int topRelevantCount = 1; // UTA: default value
        int newestCount = 1; // UTA: default value
        int oldestCount = 1; // UTA: default value
        int randomCount = 1; // UTA: default value
        List<SelectedEntry> result = underTest.stratifiedSample(entries, userMessage, topRelevantCount, newestCount, oldestCount, randomCount);

    }

    /**
     * Parasoft Jtest UTA: Test for stratifiedSample(List, String, int, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#stratifiedSample(List, String, int, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testStratifiedSample3() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for stratifiedSample(List, String, int, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#stratifiedSample(List, String, int, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testStratifiedSample4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        String userMessage = "userMessage"; // UTA: configured value
        int topRelevantCount = 1; // UTA: default value
        int newestCount = 1; // UTA: default value
        int oldestCount = 1; // UTA: default value
        int randomCount = 1; // UTA: default value
        List<SelectedEntry> result = underTest.stratifiedSample(entries, userMessage, topRelevantCount, newestCount, oldestCount, randomCount);

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

    }

    /**
     * Parasoft Jtest UTA: Test for topRelevant(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#topRelevant(List, int)
     * @author bmcmullin
     */
    @Test
    public void testTopRelevant2() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        terms.add(item);
        int n = 1; // UTA: default value
        List<SelectedEntry> result = underTest.topRelevant(entries, terms, n);

    }

    /**
     * Parasoft Jtest UTA: Test for topRelevant(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#topRelevant(List, int)
     * @author bmcmullin
     */
    @Test
    public void testTopRelevant3() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int n = 0; // UTA: configured value
        List<SelectedEntry> result = underTest.topRelevant(entries, terms, n);

    }

    /**
     * Parasoft Jtest UTA: Test for topRelevant(List, int)
     *
     * @see com.sim.chatserver.service.ReviewSamplingService#topRelevant(List, int)
     * @author bmcmullin
     */
    @Test
    public void testTopRelevant4() throws Throwable
    {
        // Given
        ReviewSamplingService underTest = new ReviewSamplingService();

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        List<String> terms = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        terms.add(item2);
        int n = 1; // UTA: configured value
        List<SelectedEntry> result = underTest.topRelevant(entries, terms, n);

    }
}
