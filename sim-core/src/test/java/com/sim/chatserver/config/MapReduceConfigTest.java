package com.sim.chatserver.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;
/**
 * Parasoft Jtest UTA: Test class for MapReduceConfig
 *
 * @see com.sim.chatserver.config.MapReduceConfig
 * @author bmcmullin
 */
public class MapReduceConfigTest
{

    /**
     * Parasoft Jtest UTA: Test for getBatchSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getBatchSize()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getBatchSize();

    }

    /**
     * Parasoft Jtest UTA: Test for getFinalReduceMaxAttempts()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getFinalReduceMaxAttempts()
     * @author bmcmullin
     */
    @Test
    public void testGetFinalReduceMaxAttempts() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getFinalReduceMaxAttempts();

    }

    /**
     * Parasoft Jtest UTA: Test for getFinalReduceMaxSummaries()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getFinalReduceMaxSummaries()
     * @author bmcmullin
     */
    @Test
    public void testGetFinalReduceMaxSummaries() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getFinalReduceMaxSummaries();

    }

    /**
     * Parasoft Jtest UTA: Test for getFinalReduceSummaryMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getFinalReduceSummaryMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetFinalReduceSummaryMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getFinalReduceSummaryMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getFixedBatchSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getFixedBatchSize()
     * @author bmcmullin
     */
    @Test
    public void testGetFixedBatchSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getFixedBatchSize();

    }

    /**
     * Parasoft Jtest UTA: Test for getMapContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMapContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetMapContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMapContextMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getMapMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMapMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetMapMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMapMessageMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getMaxCoveragePasses()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMaxCoveragePasses()
     * @author bmcmullin
     */
    @Test
    public void testGetMaxCoveragePasses() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMaxCoveragePasses();

    }

    /**
     * Parasoft Jtest UTA: Test for getMaxParallel()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMaxParallel()
     * @author bmcmullin
     */
    @Test
    public void testGetMaxParallel() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMaxParallel();

    }

    /**
     * Parasoft Jtest UTA: Test for getMinBatchSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMinBatchSize()
     * @author bmcmullin
     */
    @Test
    public void testGetMinBatchSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMinBatchSize();

    }

    /**
     * Parasoft Jtest UTA: Test for getProgressPollMs()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getProgressPollMs()
     * @author bmcmullin
     */
    @Test
    public void testGetProgressPollMs() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getProgressPollMs();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceChunkSummaryMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceChunkSummaryMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceChunkSummaryMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceChunkSummaryMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceContextMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceInitialChunkSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceInitialChunkSize()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceInitialChunkSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceInitialChunkSize();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceMaxLevels()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceMaxLevels()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceMaxLevels() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceMaxLevels();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceMessageMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceMinChunkSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceMinChunkSize()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceMinChunkSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceMinChunkSize();

    }

    /**
     * Parasoft Jtest UTA: Test for getRetryContextChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getRetryContextChars()
     * @author bmcmullin
     */
    @Test
    public void testGetRetryContextChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getRetryContextChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getRetryMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getRetryMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetRetryMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getRetryMessageMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getSegmentPromptChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSegmentPromptChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSegmentPromptChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSegmentPromptChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getSegmentResponseChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSegmentResponseChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSegmentResponseChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSegmentResponseChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassContextMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassMaxSelected()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassMaxSelected()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassMaxSelected() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassMaxSelected();

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassMessageMaxChars();

    }

    /**
     * Parasoft Jtest UTA: Test for getWorkspaceMaxRetries()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getWorkspaceMaxRetries()
     * @author bmcmullin
     */
    @Test
    public void testGetWorkspaceMaxRetries() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getWorkspaceMaxRetries();

    }

    /**
     * Parasoft Jtest UTA: Test for getWorkspaceTimeout()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getWorkspaceTimeout()
     * @author bmcmullin
     */
    @Test
    public void testGetWorkspaceTimeout() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        Duration result = underTest.getWorkspaceTimeout();

    }

    /**
     * Parasoft Jtest UTA: Test for isExhaustiveMode()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isExhaustiveMode()
     * @author bmcmullin
     */
    @Test
    public void testIsExhaustiveMode() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isExhaustiveMode();

    }

    /**
     * Parasoft Jtest UTA: Test for isProgressEnabled()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isProgressEnabled()
     * @author bmcmullin
     */
    @Test
    public void testIsProgressEnabled() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isProgressEnabled();

    }

    /**
     * Parasoft Jtest UTA: Test for isRebatchOnContextLimit()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isRebatchOnContextLimit()
     * @author bmcmullin
     */
    @Test
    public void testIsRebatchOnContextLimit() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isRebatchOnContextLimit();

    }

    /**
     * Parasoft Jtest UTA: Test for isRetryReduceOnContextLimit()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isRetryReduceOnContextLimit()
     * @author bmcmullin
     */
    @Test
    public void testIsRetryReduceOnContextLimit() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isRetryReduceOnContextLimit();

    }

    /**
     * Parasoft Jtest UTA: Test for isStrictFixedBatchMode()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isStrictFixedBatchMode()
     * @author bmcmullin
     */
    @Test
    public void testIsStrictFixedBatchMode() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isStrictFixedBatchMode();

    }

    /**
     * Parasoft Jtest UTA: Test for load()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#load()
     * @author bmcmullin
     */
    @Test
    public void testLoad() throws Throwable
    {
        // When
        MapReduceConfig result = MapReduceConfig.load();

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        String result = underTest.toString();

    }
}
