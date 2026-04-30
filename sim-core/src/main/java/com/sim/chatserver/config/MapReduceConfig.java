// src/main/java/com/sim/chatserver/config/MapReduceConfig.java
package com.sim.chatserver.config;

import java.time.Duration;
import java.util.Locale;

/**
 * Central configuration for widget-review map-reduce orchestration.
 *
 * Loads from System properties first, then environment variables, then
 * defaults.
 */
public final class MapReduceConfig {

    // Defaults
    private static final int D_SINGLE_PASS_MAX_SELECTED = 200;
    private static final int D_BATCH_SIZE = 50;
    private static final int D_MAX_PARALLEL = 3;

    private static final int D_SINGLE_PASS_MESSAGE_MAX_CHARS = 60000;
    private static final int D_SINGLE_PASS_CONTEXT_MAX_CHARS = 52000;

    private static final int D_MAP_MESSAGE_MAX_CHARS = 45000;
    private static final int D_MAP_CONTEXT_MAX_CHARS = 38000;

    private static final int D_REDUCE_MESSAGE_MAX_CHARS = 55000;
    private static final int D_REDUCE_CONTEXT_MAX_CHARS = 48000;

    private static final int D_RETRY_CONTEXT_CHARS = 18000;
    private static final int D_RETRY_MESSAGE_MAX_CHARS = 24000;

    private static final int D_WORKSPACE_TIMEOUT_SECONDS = 90;
    private static final int D_WORKSPACE_MAX_RETRIES = 1;

    // strict/exhaustive controls
    private static final boolean D_EXHAUSTIVE_MODE = true;
    private static final int D_MIN_BATCH_SIZE = 1;
    private static final int D_MAX_COVERAGE_PASSES = 3;
    private static final boolean D_REBATCH_ON_CONTEXT_LIMIT = true;
    private static final boolean D_RETRY_REDUCE_ON_CONTEXT_LIMIT = true;

    // segmentation controls
    private static final int D_SEGMENT_PROMPT_CHARS = 3500;
    private static final int D_SEGMENT_RESPONSE_CHARS = 5000;

    // strict fixed-window map mode controls
    private static final boolean D_STRICT_FIXED_BATCH_MODE = true;
    private static final int D_FIXED_BATCH_SIZE = 5;

    // UI progress hints
    private static final boolean D_PROGRESS_ENABLED = true;
    private static final int D_PROGRESS_POLL_MS = 1200;

    // New hierarchical reduce performance/safety controls
    private static final int D_REDUCE_INITIAL_CHUNK_SIZE = 6;
    private static final int D_REDUCE_MIN_CHUNK_SIZE = 2;
    private static final int D_REDUCE_MAX_LEVELS = 3;
    private static final int D_REDUCE_CHUNK_SUMMARY_MAX_CHARS = 900;

    private static final int D_FINAL_REDUCE_MAX_SUMMARIES = 3;
    private static final int D_FINAL_REDUCE_SUMMARY_MAX_CHARS = 900;
    private static final int D_FINAL_REDUCE_MAX_ATTEMPTS = 2;

    // Loaded values
    private final int singlePassMaxSelected;
    private final int batchSize;
    private final int minBatchSize;
    private final int maxParallel;
    private final int maxCoveragePasses;

    private final boolean exhaustiveMode;
    private final boolean rebatchOnContextLimit;
    private final boolean retryReduceOnContextLimit;

    private final int singlePassMessageMaxChars;
    private final int singlePassContextMaxChars;

    private final int mapMessageMaxChars;
    private final int mapContextMaxChars;

    private final int reduceMessageMaxChars;
    private final int reduceContextMaxChars;

    private final int retryContextChars;
    private final int retryMessageMaxChars;

    private final Duration workspaceTimeout;
    private final int workspaceMaxRetries;

    private final int segmentPromptChars;
    private final int segmentResponseChars;

    private final boolean strictFixedBatchMode;
    private final int fixedBatchSize;

    private final boolean progressEnabled;
    private final int progressPollMs;

    // New
    private final int reduceInitialChunkSize;
    private final int reduceMinChunkSize;
    private final int reduceMaxLevels;
    private final int reduceChunkSummaryMaxChars;

    private final int finalReduceMaxSummaries;
    private final int finalReduceSummaryMaxChars;
    private final int finalReduceMaxAttempts;

    private MapReduceConfig(
            int singlePassMaxSelected,
            int batchSize,
            int minBatchSize,
            int maxParallel,
            int maxCoveragePasses,
            boolean exhaustiveMode,
            boolean rebatchOnContextLimit,
            boolean retryReduceOnContextLimit,
            int singlePassMessageMaxChars,
            int singlePassContextMaxChars,
            int mapMessageMaxChars,
            int mapContextMaxChars,
            int reduceMessageMaxChars,
            int reduceContextMaxChars,
            int retryContextChars,
            int retryMessageMaxChars,
            Duration workspaceTimeout,
            int workspaceMaxRetries,
            int segmentPromptChars,
            int segmentResponseChars,
            boolean strictFixedBatchMode,
            int fixedBatchSize,
            boolean progressEnabled,
            int progressPollMs,
            int reduceInitialChunkSize,
            int reduceMinChunkSize,
            int reduceMaxLevels,
            int reduceChunkSummaryMaxChars,
            int finalReduceMaxSummaries,
            int finalReduceSummaryMaxChars,
            int finalReduceMaxAttempts
    ) {
        this.singlePassMaxSelected = singlePassMaxSelected;
        this.batchSize = batchSize;
        this.minBatchSize = minBatchSize;
        this.maxParallel = maxParallel;
        this.maxCoveragePasses = maxCoveragePasses;

        this.exhaustiveMode = exhaustiveMode;
        this.rebatchOnContextLimit = rebatchOnContextLimit;
        this.retryReduceOnContextLimit = retryReduceOnContextLimit;

        this.singlePassMessageMaxChars = singlePassMessageMaxChars;
        this.singlePassContextMaxChars = singlePassContextMaxChars;
        this.mapMessageMaxChars = mapMessageMaxChars;
        this.mapContextMaxChars = mapContextMaxChars;
        this.reduceMessageMaxChars = reduceMessageMaxChars;
        this.reduceContextMaxChars = reduceContextMaxChars;
        this.retryContextChars = retryContextChars;
        this.retryMessageMaxChars = retryMessageMaxChars;

        this.workspaceTimeout = workspaceTimeout;
        this.workspaceMaxRetries = workspaceMaxRetries;

        this.segmentPromptChars = segmentPromptChars;
        this.segmentResponseChars = segmentResponseChars;

        this.strictFixedBatchMode = strictFixedBatchMode;
        this.fixedBatchSize = fixedBatchSize;

        this.progressEnabled = progressEnabled;
        this.progressPollMs = progressPollMs;

        this.reduceInitialChunkSize = reduceInitialChunkSize;
        this.reduceMinChunkSize = reduceMinChunkSize;
        this.reduceMaxLevels = reduceMaxLevels;
        this.reduceChunkSummaryMaxChars = reduceChunkSummaryMaxChars;

        this.finalReduceMaxSummaries = finalReduceMaxSummaries;
        this.finalReduceSummaryMaxChars = finalReduceSummaryMaxChars;
        this.finalReduceMaxAttempts = finalReduceMaxAttempts;
    }

    public static MapReduceConfig load() {
        int singlePassMaxSelected = boundedInt("REVIEW_MR_SINGLE_PASS_MAX_SELECTED", D_SINGLE_PASS_MAX_SELECTED, 1, 20000);
        int batchSize = boundedInt("REVIEW_MR_BATCH_SIZE", D_BATCH_SIZE, 1, 500);
        int minBatchSize = boundedInt("REVIEW_MR_MIN_BATCH_SIZE", D_MIN_BATCH_SIZE, 1, 128);
        int maxParallel = boundedInt("REVIEW_MR_MAX_PARALLEL", D_MAX_PARALLEL, 1, 16);
        int maxCoveragePasses = boundedInt("REVIEW_MR_MAX_COVERAGE_PASSES", D_MAX_COVERAGE_PASSES, 1, 20);

        boolean exhaustiveMode = booleanFromPropertyOrEnv("REVIEW_MR_EXHAUSTIVE_MODE", D_EXHAUSTIVE_MODE);
        boolean rebatchOnContextLimit = booleanFromPropertyOrEnv("REVIEW_MR_REBATCH_ON_CONTEXT_LIMIT", D_REBATCH_ON_CONTEXT_LIMIT);
        boolean retryReduceOnContextLimit = booleanFromPropertyOrEnv("REVIEW_MR_RETRY_REDUCE_ON_CONTEXT_LIMIT", D_RETRY_REDUCE_ON_CONTEXT_LIMIT);

        int singlePassMessageMaxChars = boundedInt("REVIEW_MR_SINGLE_PASS_MESSAGE_MAX_CHARS", D_SINGLE_PASS_MESSAGE_MAX_CHARS, 1000, 500000);
        int singlePassContextMaxChars = boundedInt("REVIEW_MR_SINGLE_PASS_CONTEXT_MAX_CHARS", D_SINGLE_PASS_CONTEXT_MAX_CHARS, 1000, 500000);

        int mapMessageMaxChars = boundedInt("REVIEW_MR_MAP_MESSAGE_MAX_CHARS", D_MAP_MESSAGE_MAX_CHARS, 1000, 500000);
        int mapContextMaxChars = boundedInt("REVIEW_MR_MAP_CONTEXT_MAX_CHARS", D_MAP_CONTEXT_MAX_CHARS, 1000, 500000);

        int reduceMessageMaxChars = boundedInt("REVIEW_MR_REDUCE_MESSAGE_MAX_CHARS", D_REDUCE_MESSAGE_MAX_CHARS, 1000, 500000);
        int reduceContextMaxChars = boundedInt("REVIEW_MR_REDUCE_CONTEXT_MAX_CHARS", D_REDUCE_CONTEXT_MAX_CHARS, 1000, 500000);

        int retryContextChars = boundedInt("REVIEW_MR_RETRY_CONTEXT_CHARS", D_RETRY_CONTEXT_CHARS, 500, 500000);
        int retryMessageMaxChars = boundedInt("REVIEW_MR_RETRY_MESSAGE_MAX_CHARS", D_RETRY_MESSAGE_MAX_CHARS, 500, 500000);

        int timeoutSec = boundedInt("REVIEW_MR_WORKSPACE_TIMEOUT_SECONDS", D_WORKSPACE_TIMEOUT_SECONDS, 5, 600);
        int maxRetries = boundedInt("REVIEW_MR_WORKSPACE_MAX_RETRIES", D_WORKSPACE_MAX_RETRIES, 0, 10);

        int segmentPromptChars = boundedInt("REVIEW_MR_SEGMENT_PROMPT_CHARS", D_SEGMENT_PROMPT_CHARS, 200, 200000);
        int segmentResponseChars = boundedInt("REVIEW_MR_SEGMENT_RESPONSE_CHARS", D_SEGMENT_RESPONSE_CHARS, 200, 200000);

        boolean strictFixedBatchMode = booleanFromPropertyOrEnv("REVIEW_MR_STRICT_FIXED_BATCH_MODE", D_STRICT_FIXED_BATCH_MODE);
        int fixedBatchSize = boundedInt("REVIEW_MR_FIXED_BATCH_SIZE", D_FIXED_BATCH_SIZE, 1, 128);

        boolean progressEnabled = booleanFromPropertyOrEnv("REVIEW_MR_PROGRESS_ENABLED", D_PROGRESS_ENABLED);
        int progressPollMs = boundedInt("REVIEW_MR_PROGRESS_POLL_MS", D_PROGRESS_POLL_MS, 250, 15000);

        int reduceInitialChunkSize = boundedInt("REVIEW_MR_REDUCE_INITIAL_CHUNK_SIZE", D_REDUCE_INITIAL_CHUNK_SIZE, 2, 64);
        int reduceMinChunkSize = boundedInt("REVIEW_MR_REDUCE_MIN_CHUNK_SIZE", D_REDUCE_MIN_CHUNK_SIZE, 1, 64);
        int reduceMaxLevels = boundedInt("REVIEW_MR_REDUCE_MAX_LEVELS", D_REDUCE_MAX_LEVELS, 1, 20);
        int reduceChunkSummaryMaxChars = boundedInt("REVIEW_MR_REDUCE_CHUNK_SUMMARY_MAX_CHARS", D_REDUCE_CHUNK_SUMMARY_MAX_CHARS, 200, 20000);

        int finalReduceMaxSummaries = boundedInt("REVIEW_MR_FINAL_REDUCE_MAX_SUMMARIES", D_FINAL_REDUCE_MAX_SUMMARIES, 1, 20);
        int finalReduceSummaryMaxChars = boundedInt("REVIEW_MR_FINAL_REDUCE_SUMMARY_MAX_CHARS", D_FINAL_REDUCE_SUMMARY_MAX_CHARS, 200, 20000);
        int finalReduceMaxAttempts = boundedInt("REVIEW_MR_FINAL_REDUCE_MAX_ATTEMPTS", D_FINAL_REDUCE_MAX_ATTEMPTS, 1, 20);

        // Safety normalization
        if (singlePassContextMaxChars > singlePassMessageMaxChars) {
            singlePassContextMaxChars = singlePassMessageMaxChars;
        }
        if (mapContextMaxChars > mapMessageMaxChars) {
            mapContextMaxChars = mapMessageMaxChars;
        }
        if (reduceContextMaxChars > reduceMessageMaxChars) {
            reduceContextMaxChars = reduceMessageMaxChars;
        }
        if (retryContextChars > retryMessageMaxChars) {
            retryContextChars = retryMessageMaxChars;
        }

        if (minBatchSize > batchSize) {
            minBatchSize = batchSize;
        }

        if (strictFixedBatchMode) {
            batchSize = Math.max(minBatchSize, fixedBatchSize);
        }

        if (reduceMinChunkSize > reduceInitialChunkSize) {
            reduceMinChunkSize = reduceInitialChunkSize;
        }

        return new MapReduceConfig(
                singlePassMaxSelected,
                batchSize,
                minBatchSize,
                maxParallel,
                maxCoveragePasses,
                exhaustiveMode,
                rebatchOnContextLimit,
                retryReduceOnContextLimit,
                singlePassMessageMaxChars,
                singlePassContextMaxChars,
                mapMessageMaxChars,
                mapContextMaxChars,
                reduceMessageMaxChars,
                reduceContextMaxChars,
                retryContextChars,
                retryMessageMaxChars,
                Duration.ofSeconds(timeoutSec),
                maxRetries,
                segmentPromptChars,
                segmentResponseChars,
                strictFixedBatchMode,
                fixedBatchSize,
                progressEnabled,
                progressPollMs,
                reduceInitialChunkSize,
                reduceMinChunkSize,
                reduceMaxLevels,
                reduceChunkSummaryMaxChars,
                finalReduceMaxSummaries,
                finalReduceSummaryMaxChars,
                finalReduceMaxAttempts
        );
    }

    private static int boundedInt(String key, int defaultValue, int min, int max) {
        int v = parseIntFromPropertyOrEnv(key, defaultValue);
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    private static boolean booleanFromPropertyOrEnv(String key, boolean defaultValue) {
        String v = valueFromPropertyOrEnv(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }

        String n = v.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(n) || "true".equals(n) || "yes".equals(n) || "y".equals(n) || "on".equals(n)) {
            return true;
        }
        if ("0".equals(n) || "false".equals(n) || "no".equals(n) || "n".equals(n) || "off".equals(n)) {
            return false;
        }
        return defaultValue;
    }

    private static int parseIntFromPropertyOrEnv(String key, int defaultValue) {
        String v = valueFromPropertyOrEnv(key);
        if (v != null && !v.isBlank()) {
            Integer parsed = parseInt(v);
            if (parsed != null) {
                return parsed;
            }
        }
        return defaultValue;
    }

    private static String valueFromPropertyOrEnv(String key) {
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }

        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }

        String alias = key.toLowerCase(Locale.ROOT).replace('_', '.');
        String propAlias = System.getProperty(alias);
        if (propAlias != null && !propAlias.isBlank()) {
            return propAlias;
        }

        return null;
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    public int getSinglePassMaxSelected() {
        return singlePassMaxSelected;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getMinBatchSize() {
        return minBatchSize;
    }

    public int getMaxParallel() {
        return maxParallel;
    }

    public int getMaxCoveragePasses() {
        return maxCoveragePasses;
    }

    public boolean isExhaustiveMode() {
        return exhaustiveMode;
    }

    public boolean isRebatchOnContextLimit() {
        return rebatchOnContextLimit;
    }

    public boolean isRetryReduceOnContextLimit() {
        return retryReduceOnContextLimit;
    }

    public int getSinglePassMessageMaxChars() {
        return singlePassMessageMaxChars;
    }

    public int getSinglePassContextMaxChars() {
        return singlePassContextMaxChars;
    }

    public int getMapMessageMaxChars() {
        return mapMessageMaxChars;
    }

    public int getMapContextMaxChars() {
        return mapContextMaxChars;
    }

    public int getReduceMessageMaxChars() {
        return reduceMessageMaxChars;
    }

    public int getReduceContextMaxChars() {
        return reduceContextMaxChars;
    }

    public int getRetryContextChars() {
        return retryContextChars;
    }

    public int getRetryMessageMaxChars() {
        return retryMessageMaxChars;
    }

    public Duration getWorkspaceTimeout() {
        return workspaceTimeout;
    }

    public int getWorkspaceMaxRetries() {
        return workspaceMaxRetries;
    }

    public int getSegmentPromptChars() {
        return segmentPromptChars;
    }

    public int getSegmentResponseChars() {
        return segmentResponseChars;
    }

    public boolean isStrictFixedBatchMode() {
        return strictFixedBatchMode;
    }

    public int getFixedBatchSize() {
        return fixedBatchSize;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public int getProgressPollMs() {
        return progressPollMs;
    }

    public int getReduceInitialChunkSize() {
        return reduceInitialChunkSize;
    }

    public int getReduceMinChunkSize() {
        return reduceMinChunkSize;
    }

    public int getReduceMaxLevels() {
        return reduceMaxLevels;
    }

    public int getReduceChunkSummaryMaxChars() {
        return reduceChunkSummaryMaxChars;
    }

    public int getFinalReduceMaxSummaries() {
        return finalReduceMaxSummaries;
    }

    public int getFinalReduceSummaryMaxChars() {
        return finalReduceSummaryMaxChars;
    }

    public int getFinalReduceMaxAttempts() {
        return finalReduceMaxAttempts;
    }

    @Override
    public String toString() {
        return "MapReduceConfig{"
                + "singlePassMaxSelected=" + singlePassMaxSelected
                + ", batchSize=" + batchSize
                + ", minBatchSize=" + minBatchSize
                + ", maxParallel=" + maxParallel
                + ", maxCoveragePasses=" + maxCoveragePasses
                + ", exhaustiveMode=" + exhaustiveMode
                + ", rebatchOnContextLimit=" + rebatchOnContextLimit
                + ", retryReduceOnContextLimit=" + retryReduceOnContextLimit
                + ", singlePassMessageMaxChars=" + singlePassMessageMaxChars
                + ", singlePassContextMaxChars=" + singlePassContextMaxChars
                + ", mapMessageMaxChars=" + mapMessageMaxChars
                + ", mapContextMaxChars=" + mapContextMaxChars
                + ", reduceMessageMaxChars=" + reduceMessageMaxChars
                + ", reduceContextMaxChars=" + reduceContextMaxChars
                + ", retryContextChars=" + retryContextChars
                + ", retryMessageMaxChars=" + retryMessageMaxChars
                + ", workspaceTimeout=" + workspaceTimeout
                + ", workspaceMaxRetries=" + workspaceMaxRetries
                + ", segmentPromptChars=" + segmentPromptChars
                + ", segmentResponseChars=" + segmentResponseChars
                + ", strictFixedBatchMode=" + strictFixedBatchMode
                + ", fixedBatchSize=" + fixedBatchSize
                + ", progressEnabled=" + progressEnabled
                + ", progressPollMs=" + progressPollMs
                + ", reduceInitialChunkSize=" + reduceInitialChunkSize
                + ", reduceMinChunkSize=" + reduceMinChunkSize
                + ", reduceMaxLevels=" + reduceMaxLevels
                + ", reduceChunkSummaryMaxChars=" + reduceChunkSummaryMaxChars
                + ", finalReduceMaxSummaries=" + finalReduceMaxSummaries
                + ", finalReduceSummaryMaxChars=" + finalReduceSummaryMaxChars
                + ", finalReduceMaxAttempts=" + finalReduceMaxAttempts
                + '}';
    }
}
