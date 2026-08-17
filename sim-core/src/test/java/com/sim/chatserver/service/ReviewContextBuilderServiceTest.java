package com.sim.chatserver.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        String result = underTest.buildBatchDeterministicHeader(totalSelected, totalBatches, batchIndex, batch);

    }

    /**
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        batch.add(item);
        String result = underTest.buildBatchDeterministicHeader(totalSelected, totalBatches, batchIndex, batch);

    }

    /**
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader4() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

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
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader5() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        batch.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);
        batch.add(item2);
        String result = underTest.buildBatchDeterministicHeader(totalSelected, totalBatches, batchIndex, batch);

    }

    /**
     * Parasoft Jtest UTA: Test for buildBatchDeterministicHeader(int, int, int, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildBatchDeterministicHeader(int, int, int, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildBatchDeterministicHeader6() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        int totalSelected = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int batchIndex = 1; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);
        batch.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);
        batch.add(item2);
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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = null; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        String result = underTest.buildContext(userMessage, entries);

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
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        String result = underTest.buildContext(userMessage, entries);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
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
        String result = underTest.buildContext(userMessage, entries);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext5() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);
        stratifiedSampleResult.add(item);
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        String result = underTest.buildContext(userMessage, entries);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext6() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        String result = underTest.buildContext(userMessage, entries);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext7() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext8() throws Throwable
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
        entries.add(item);
        String result = underTest.buildContext(userMessage, entries);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext9() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = null; // UTA: configured value
        int maxChars = 1; // UTA: default value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext10() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext11() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int maxChars = 0; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext12() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext13() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext14() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<SelectedEntry> stratifiedSampleResult = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);
        stratifiedSampleResult.add(item);
        doReturn(stratifiedSampleResult).when(samplingService).stratifiedSample((List) any(), nullable(String.class), anyInt(), anyInt(), anyInt(), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext15() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext16() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for buildContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildContext17() throws Throwable
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
        entries.add(item);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildContext(userMessage, entries, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = null; // UTA: configured value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        batch.add(item);
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext4() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext5() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<String> keywordTermsResult = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        keywordTermsResult.add(item);
        doReturn(keywordTermsResult).when(samplingService).keywordTerms(nullable(String.class), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List, int, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext6() throws Throwable
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
    public void testBuildMapBatchContext7() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = null; // UTA: configured value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext8() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: default value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext9() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        batch.add(item);
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 0; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        expectedChatIds.add(item2);
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext10() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        List<String> expectedChatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        expectedChatIds.add(item);
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext11() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext12() throws Throwable
    {
        // Given
        ReviewSamplingService samplingService = mock(ReviewSamplingService.class);
        List<String> keywordTermsResult = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        keywordTermsResult.add(item);
        doReturn(keywordTermsResult).when(samplingService).keywordTerms(nullable(String.class), anyInt());
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(samplingService);

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<SelectedEntry> batch = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchIndex = 1; // UTA: default value
        int totalBatches = 1; // UTA: default value
        int maxChars = 1; // UTA: configured value
        List<String> expectedChatIds = null; // UTA: configured value
        String result = underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);

    }

    /**
     * Parasoft Jtest UTA: Test for buildMapBatchContext(String, List)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildMapBatchContext(String, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildMapBatchContext13() throws Throwable
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
        List<String> expectedChatIds = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, expectedChatIds);
        });

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        int maxChars = 0; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext5() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        int maxChars = 2; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext6() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext7() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        int maxChars = 0; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext8() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext9() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext10() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext11() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext12() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        int maxChars = 2; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext13() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext14() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        int maxChars = 1; // UTA: configured value
        String result = invokeBuildReduceContextWithFailedBatches(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext15() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<String> allSelectedIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        allSelectedIds.add(item3);
        List<String> missingIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        missingIds.add(item4);
        int maxChars = 0; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext16() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext17() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext18() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext19() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext20() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = new ArrayList<String>(); // UTA: default value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext21() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = new ArrayList<String>(); // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext22() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = null; // UTA: configured value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = null; // UTA: configured value
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 2; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext23() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<String> allSelectedIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        allSelectedIds.add(item3);
        List<String> missingIds = new ArrayList<String>(); // UTA: default value
        String item4 = "item4"; // UTA: default value
        missingIds.add(item4);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext24() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext25() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        mapOutputs.add(item);
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        failedBatchIndexes.add(item2);
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        missingIds.add(item3);
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for buildReduceContext(String, List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#buildReduceContext(String, List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildReduceContext26() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        String userMessage = "userMessage"; // UTA: default value
        List<String> mapOutputs = null; // UTA: configured value
        List<Integer> failedBatchIndexes = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        failedBatchIndexes.add(item);
        List<String> allSelectedIds = null; // UTA: configured value
        List<String> missingIds = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = underTest.buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, allSelectedIds, missingIds, maxChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments4() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments5() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

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

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments6() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments7() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments8() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments9() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments10() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments11() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments12() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments13() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments14() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments15() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments16() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments17() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments18() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments19() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments20() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments21() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments22() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments23() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        String getChatIdResult2 = "getChatIdResult2"; // UTA: default value
        when(item2.getChatId()).thenReturn(getChatIdResult2);

        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getResponseResult2 = "getResponseResult2"; // UTA: default value
        when(item2.getResponse()).thenReturn(getResponseResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        entries.add(item2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments24() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        String getChatIdResult2 = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult, getChatIdResult2);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        String getSessionIdResult2 = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntriesToSegments(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntriesToSegments(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntriesToSegments25() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        String getChatIdResult2 = null; // UTA: configured value
        when(item.getChatId()).thenReturn(getChatIdResult, getChatIdResult2);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(item.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        String getSessionIdResult2 = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        entries.add(item);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = underTest.explodeLargeEntriesToSegments(entries, promptChunkChars, responseChunkChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = null; // UTA: configured value
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = "getResponseResult"; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments4() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments5() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments6() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments7() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments8() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments9() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments10() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments11() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments12() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments13() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        String getChatIdResult2 = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult, getChatIdResult2);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        String getSessionIdResult2 = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments14() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        String getChatIdResult2 = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult, getChatIdResult2);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments15() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = "getChatIdResult"; // UTA: default value
        String getChatIdResult2 = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult, getChatIdResult2);

        String getCreatedAtResult = null; // UTA: configured value
        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        String getSessionIdResult2 = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments16() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        String getSessionIdResult2 = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments17() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = "getCreatedAtResult"; // UTA: default value
        String getCreatedAtResult2 = null; // UTA: configured value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments18() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getChatIdResult = null; // UTA: configured value
        when(entry.getChatId()).thenReturn(getChatIdResult);

        String getCreatedAtResult = null; // UTA: configured value
        String getCreatedAtResult2 = "getCreatedAtResult2"; // UTA: default value
        when(entry.getCreatedAt()).thenReturn(getCreatedAtResult, getCreatedAtResult2);

        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);

        String getResponseResult = null; // UTA: configured value
        when(entry.getResponse()).thenReturn(getResponseResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        String getSessionIdResult2 = null; // UTA: configured value
        when(entry.getSessionId()).thenReturn(getSessionIdResult, getSessionIdResult2);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments19() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(entry.getPrompt()).thenReturn(getPromptResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

    }

    /**
     * Parasoft Jtest UTA: Test for explodeLargeEntryToSegments(SelectedEntry, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#explodeLargeEntryToSegments(SelectedEntry, int, int)
     * @author bmcmullin
     */
    @Test
    public void testExplodeLargeEntryToSegments20() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        SelectedEntry entry = mock(SelectedEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(entry.getPrompt()).thenReturn(getPromptResult);
        int promptChunkChars = 1; // UTA: default value
        int responseChunkChars = 1; // UTA: default value
        List<SelectedEntry> result = invokeExplodeLargeEntryToSegments(underTest, entry, promptChunkChars, responseChunkChars);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int batchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = invokeSplitForMap(underTest, entries, batchSize);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = invokeSplitForMap(underTest, entries, batchSize);

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMap(List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMap(List, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMap3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int batchSize = 0; // UTA: configured value
        List<List<SelectedEntry>> result = invokeSplitForMap(underTest, entries, batchSize);

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMap(List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMap(List, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMap4() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int batchSize = 0; // UTA: configured value
        List<List<SelectedEntry>> result = invokeSplitForMap(underTest, entries, batchSize);

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMap(List, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMap(List, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMap5() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int batchSize = 1; // UTA: configured value
        List<List<SelectedEntry>> result = invokeSplitForMap(underTest, entries, batchSize);

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
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        int preferredBatchSize = 1; // UTA: default value
        int minBatchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = underTest.splitForMapAdaptive(entries, preferredBatchSize, minBatchSize);

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMapAdaptive(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMapAdaptive(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMapAdaptive2() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        int preferredBatchSize = 1; // UTA: default value
        int minBatchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = underTest.splitForMapAdaptive(entries, preferredBatchSize, minBatchSize);

    }

    /**
     * Parasoft Jtest UTA: Test for splitForMapAdaptive(List, int, int)
     *
     * @see com.sim.chatserver.service.ReviewContextBuilderService#splitForMapAdaptive(List, int, int)
     * @author bmcmullin
     */
    @Test
    public void testSplitForMapAdaptive3() throws Throwable
    {
        // Given
        ReviewContextBuilderService underTest = new ReviewContextBuilderService(new ReviewSamplingService());

        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        entries.add(item);
        int preferredBatchSize = 1; // UTA: default value
        int minBatchSize = 1; // UTA: default value
        List<List<SelectedEntry>> result = underTest.splitForMapAdaptive(entries, preferredBatchSize, minBatchSize);

    }

    @SuppressWarnings("unchecked")
    private static List<List<SelectedEntry>> invokeSplitForMap(
            ReviewContextBuilderService underTest,
            List<SelectedEntry> entries,
            int batchSize
    ) throws Exception {
        Method method = ReviewContextBuilderService.class.getDeclaredMethod("splitForMap", List.class, int.class);
        method.setAccessible(true);
        return (List<List<SelectedEntry>>) method.invoke(underTest, entries, batchSize);
    }

    @SuppressWarnings("unchecked")
    private static List<SelectedEntry> invokeExplodeLargeEntryToSegments(
            ReviewContextBuilderService underTest,
            SelectedEntry entry,
            int promptChunkChars,
            int responseChunkChars
    ) throws Exception {
        Method method = ReviewContextBuilderService.class.getDeclaredMethod(
                "explodeLargeEntryToSegments",
                SelectedEntry.class,
                int.class,
                int.class
        );
        method.setAccessible(true);
        return (List<SelectedEntry>) method.invoke(underTest, entry, promptChunkChars, responseChunkChars);
    }

    private static String invokeBuildReduceContextWithFailedBatches(
            ReviewContextBuilderService underTest,
            String userMessage,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            int maxChars
    ) throws Exception {
        Method method = ReviewContextBuilderService.class.getDeclaredMethod(
                "buildReduceContext",
                String.class,
                List.class,
                List.class,
                int.class
        );
        method.setAccessible(true);
        return (String) method.invoke(underTest, userMessage, mapOutputs, failedBatchIndexes, maxChars);
    }
}
