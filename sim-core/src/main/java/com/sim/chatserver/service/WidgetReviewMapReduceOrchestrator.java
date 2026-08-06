// src/main/java/com/sim/chatserver/service/WidgetReviewMapReduceOrchestrator.java
package com.sim.chatserver.service;

import java.io.IOException;
import java.io.StringReader;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchRequest;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.security.review.ReviewOutputValidator;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;

public class WidgetReviewMapReduceOrchestrator {

    private static final Logger log = Logger.getLogger(WidgetReviewMapReduceOrchestrator.class.getName());

    private static final Pattern CHAT_HEADING_PATTERN
            = Pattern.compile("(?im)^###\\s*chat\\s+([\\w\\-:.]+)\\s*$");

    private static final Pattern COVERED_IDS_LINE
            = Pattern.compile("(?im)^\\s*covered_chat_ids\\s*:\\s*\\[(.*)]\\s*$");

    private static final int DEFAULT_MAX_RETRY_ROUNDS = 3;
    private static final int DEFAULT_MIN_BATCH_SIZE = 1;
    private static final int FIXED_BATCH_SIZE = 5;

    private static final int DEFAULT_SEGMENT_PROMPT_CHARS = 3500;
    private static final int DEFAULT_SEGMENT_RESPONSE_CHARS = 5000;

    private static final int DEFAULT_REDUCE_MAX_LEVELS = 3;
    private static final int DEFAULT_REDUCE_INITIAL_CHUNK_SIZE = 6;
    private static final int DEFAULT_REDUCE_MIN_CHUNK_SIZE = 2;
    private static final int DEFAULT_REDUCE_CHUNK_SUMMARY_MAX_CHARS = 900;

    private static final int DEFAULT_FINAL_REDUCE_MAX_SUMMARIES = 3;
    private static final int DEFAULT_FINAL_REDUCE_SUMMARY_MAX_CHARS = 900;
    private static final int DEFAULT_FINAL_REDUCE_MAX_ATTEMPTS = 2;

    private final WorkspaceClient workspaceClient;
    private final ReviewContextBuilderService contextBuilderService;
    private final PromptTemplateService promptTemplateService;
    private final ReviewOutputValidator reviewOutputValidator;

    private final int mapMaxParallel;
    private final int mapMessageMaxChars;
    private final int mapContextMaxChars;

    private final int reduceMessageMaxChars;
    private final int reduceContextMaxChars;

    private final int retryContextChars;
    private final int retryTotalMessageChars;

    private final int maxRetryRounds;
    private final int minBatchSize;

    private final int segmentPromptChars;
    private final int segmentResponseChars;

    private final int reduceInitialChunkSize;
    private final int reduceMinChunkSize;
    private final int reduceMaxLevels;
    private final int reduceChunkSummaryMaxChars;

    private final int finalReduceMaxSummaries;
    private final int finalReduceSummaryMaxChars;
    private final int finalReduceMaxAttempts;

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public interface ProgressListener {

        default void onMapRoundStarted(String requestId, int round, int totalRounds, int remainingBeforeRound, int entriesInRound) {
        }

        default void onMapBatchStarted(String requestId, int batchIndex, int totalBatchesSoFar, int expectedIdsInBatch, int round) {
        }

        default void onMapBatchCompleted(String requestId, int batchIndex, int totalBatchesSoFar, boolean success, int usedSoFar, int missingSoFar, int round) {
        }

        default void onMapRoundCompleted(String requestId, int round, int totalRounds, int usedSoFar, int missingSoFar) {
        }

        default void onReduceStarted(String requestId, int totalSelected, int totalBatches, int mapOutputsCount, int missingCount) {
        }

        default void onReduceChunkStarted(String requestId, int level, int chunkIndex, int totalChunksAtLevel, int chunkSize, int currentChunkSizeConfig) {
        }

        default void onReduceChunkCompleted(String requestId, int level, int chunkIndex, int totalChunksAtLevel, boolean success, int httpStatus) {
        }

        default void onReduceLevelCompleted(String requestId, int level, int totalChunksAtLevel, int producedSummaries) {
        }

        default void onReduceCompleted(String requestId, boolean success, int httpStatus, int missingCount) {
        }

        default void onCompleted(String requestId, boolean coverageComplete, int usedCount, int missingCount, int totalBatches) {
        }
    }

    public static final ProgressListener NOOP_PROGRESS_LISTENER = new ProgressListener() {
    };

    public WidgetReviewMapReduceOrchestrator(
            WorkspaceClient workspaceClient,
            ReviewContextBuilderService contextBuilderService,
            PromptTemplateService promptTemplateService
    ) {
        this(
                workspaceClient, contextBuilderService, promptTemplateService, new ReviewOutputValidator(),
                FIXED_BATCH_SIZE, 3, 45000, 38000, 55000, 48000, 18000, 24000,
                DEFAULT_MAX_RETRY_ROUNDS, DEFAULT_MIN_BATCH_SIZE,
                DEFAULT_SEGMENT_PROMPT_CHARS, DEFAULT_SEGMENT_RESPONSE_CHARS,
                DEFAULT_REDUCE_INITIAL_CHUNK_SIZE, DEFAULT_REDUCE_MIN_CHUNK_SIZE, DEFAULT_REDUCE_MAX_LEVELS,
                DEFAULT_REDUCE_CHUNK_SUMMARY_MAX_CHARS,
                DEFAULT_FINAL_REDUCE_MAX_SUMMARIES, DEFAULT_FINAL_REDUCE_SUMMARY_MAX_CHARS, DEFAULT_FINAL_REDUCE_MAX_ATTEMPTS
        );
    }

    public WidgetReviewMapReduceOrchestrator(
            WorkspaceClient workspaceClient,
            ReviewContextBuilderService contextBuilderService,
            PromptTemplateService promptTemplateService,
            int mapBatchSizeIgnored,
            int mapMaxParallel,
            int mapMessageMaxChars,
            int mapContextMaxChars,
            int reduceMessageMaxChars,
            int reduceContextMaxChars,
            int retryContextChars,
            int retryTotalMessageChars
    ) {
        this(
                workspaceClient, contextBuilderService, promptTemplateService, new ReviewOutputValidator(),
                FIXED_BATCH_SIZE, mapMaxParallel, mapMessageMaxChars, mapContextMaxChars,
                reduceMessageMaxChars, reduceContextMaxChars, retryContextChars, retryTotalMessageChars,
                DEFAULT_MAX_RETRY_ROUNDS, DEFAULT_MIN_BATCH_SIZE,
                DEFAULT_SEGMENT_PROMPT_CHARS, DEFAULT_SEGMENT_RESPONSE_CHARS,
                DEFAULT_REDUCE_INITIAL_CHUNK_SIZE, DEFAULT_REDUCE_MIN_CHUNK_SIZE, DEFAULT_REDUCE_MAX_LEVELS,
                DEFAULT_REDUCE_CHUNK_SUMMARY_MAX_CHARS,
                DEFAULT_FINAL_REDUCE_MAX_SUMMARIES, DEFAULT_FINAL_REDUCE_SUMMARY_MAX_CHARS, DEFAULT_FINAL_REDUCE_MAX_ATTEMPTS
        );
    }

    public WidgetReviewMapReduceOrchestrator(
            WorkspaceClient workspaceClient,
            ReviewContextBuilderService contextBuilderService,
            PromptTemplateService promptTemplateService,
            ReviewOutputValidator reviewOutputValidator,
            int mapBatchSizeIgnored,
            int mapMaxParallel,
            int mapMessageMaxChars,
            int mapContextMaxChars,
            int reduceMessageMaxChars,
            int reduceContextMaxChars,
            int retryContextChars,
            int retryTotalMessageChars
    ) {
        this(
                workspaceClient, contextBuilderService, promptTemplateService, reviewOutputValidator,
                FIXED_BATCH_SIZE, mapMaxParallel, mapMessageMaxChars, mapContextMaxChars,
                reduceMessageMaxChars, reduceContextMaxChars, retryContextChars, retryTotalMessageChars,
                DEFAULT_MAX_RETRY_ROUNDS, DEFAULT_MIN_BATCH_SIZE,
                DEFAULT_SEGMENT_PROMPT_CHARS, DEFAULT_SEGMENT_RESPONSE_CHARS,
                DEFAULT_REDUCE_INITIAL_CHUNK_SIZE, DEFAULT_REDUCE_MIN_CHUNK_SIZE, DEFAULT_REDUCE_MAX_LEVELS,
                DEFAULT_REDUCE_CHUNK_SUMMARY_MAX_CHARS,
                DEFAULT_FINAL_REDUCE_MAX_SUMMARIES, DEFAULT_FINAL_REDUCE_SUMMARY_MAX_CHARS, DEFAULT_FINAL_REDUCE_MAX_ATTEMPTS
        );
    }

    public WidgetReviewMapReduceOrchestrator(
            WorkspaceClient workspaceClient,
            ReviewContextBuilderService contextBuilderService,
            PromptTemplateService promptTemplateService,
            ReviewOutputValidator reviewOutputValidator,
            int mapBatchSizeIgnored,
            int mapMaxParallel,
            int mapMessageMaxChars,
            int mapContextMaxChars,
            int reduceMessageMaxChars,
            int reduceContextMaxChars,
            int retryContextChars,
            int retryTotalMessageChars,
            int maxRetryRounds,
            int minBatchSize,
            int segmentPromptChars,
            int segmentResponseChars
    ) {
        this(
                workspaceClient,
                contextBuilderService,
                promptTemplateService,
                reviewOutputValidator,
                mapBatchSizeIgnored,
                mapMaxParallel,
                mapMessageMaxChars,
                mapContextMaxChars,
                reduceMessageMaxChars,
                reduceContextMaxChars,
                retryContextChars,
                retryTotalMessageChars,
                maxRetryRounds,
                minBatchSize,
                segmentPromptChars,
                segmentResponseChars,
                DEFAULT_REDUCE_INITIAL_CHUNK_SIZE,
                DEFAULT_REDUCE_MIN_CHUNK_SIZE,
                DEFAULT_REDUCE_MAX_LEVELS,
                DEFAULT_REDUCE_CHUNK_SUMMARY_MAX_CHARS,
                DEFAULT_FINAL_REDUCE_MAX_SUMMARIES,
                DEFAULT_FINAL_REDUCE_SUMMARY_MAX_CHARS,
                DEFAULT_FINAL_REDUCE_MAX_ATTEMPTS
        );
    }

    public WidgetReviewMapReduceOrchestrator(
            WorkspaceClient workspaceClient,
            ReviewContextBuilderService contextBuilderService,
            PromptTemplateService promptTemplateService,
            ReviewOutputValidator reviewOutputValidator,
            int mapBatchSizeIgnored,
            int mapMaxParallel,
            int mapMessageMaxChars,
            int mapContextMaxChars,
            int reduceMessageMaxChars,
            int reduceContextMaxChars,
            int retryContextChars,
            int retryTotalMessageChars,
            int maxRetryRounds,
            int minBatchSize,
            int segmentPromptChars,
            int segmentResponseChars,
            int reduceInitialChunkSize,
            int reduceMinChunkSize,
            int reduceMaxLevels,
            int reduceChunkSummaryMaxChars,
            int finalReduceMaxSummaries,
            int finalReduceSummaryMaxChars,
            int finalReduceMaxAttempts
    ) {
        this.workspaceClient = Objects.requireNonNull(workspaceClient, "workspaceClient");
        this.contextBuilderService = Objects.requireNonNull(contextBuilderService, "contextBuilderService");
        this.promptTemplateService = Objects.requireNonNull(promptTemplateService, "promptTemplateService");
        this.reviewOutputValidator = Objects.requireNonNull(reviewOutputValidator, "reviewOutputValidator");

        this.mapMaxParallel = Math.max(1, mapMaxParallel);
        this.mapMessageMaxChars = Math.max(1000, mapMessageMaxChars);
        this.mapContextMaxChars = Math.max(1000, mapContextMaxChars);
        this.reduceMessageMaxChars = Math.max(1000, reduceMessageMaxChars);
        this.reduceContextMaxChars = Math.max(1000, reduceContextMaxChars);
        this.retryContextChars = Math.max(500, retryContextChars);
        this.retryTotalMessageChars = Math.max(500, retryTotalMessageChars);
        this.maxRetryRounds = Math.max(1, maxRetryRounds);
        this.minBatchSize = Math.max(1, minBatchSize);
        this.segmentPromptChars = Math.max(200, segmentPromptChars);
        this.segmentResponseChars = Math.max(200, segmentResponseChars);

        this.reduceInitialChunkSize = Math.max(2, reduceInitialChunkSize);
        this.reduceMinChunkSize = Math.max(1, Math.min(this.reduceInitialChunkSize, reduceMinChunkSize));
        this.reduceMaxLevels = Math.max(1, reduceMaxLevels);
        this.reduceChunkSummaryMaxChars = Math.max(200, reduceChunkSummaryMaxChars);

        this.finalReduceMaxSummaries = Math.max(1, finalReduceMaxSummaries);
        this.finalReduceSummaryMaxChars = Math.max(200, finalReduceSummaryMaxChars);
        this.finalReduceMaxAttempts = Math.max(1, finalReduceMaxAttempts);
    }

    public OrchestrationResult run(
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId
    ) throws IOException, InterruptedException {
        return run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, NOOP_PROGRESS_LISTENER);
    }

    public OrchestrationResult run(
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId,
            ProgressListener progress
            ) throws IOException, InterruptedException {

        ProgressListener listener = progress == null ? NOOP_PROGRESS_LISTENER : progress;
        log.log(Level.INFO, "[MR-ORCH-PROCESSING-COVERAGE-V8] ACTIVE requestId={0}", requestId);

        if (selectedEntries == null || selectedEntries.isEmpty()) {
            WorkspaceResponse empty = new WorkspaceResponse(400, "{\"status\":\"error\",\"message\":\"No selected entries provided.\"}", "application/json");
            ReduceResult rr = ReduceResult.builder()
                    .requestId(requestId).httpStatus(400).success(false).finalReport("")
                    .errorMessage("No selected entries provided.")
                    .totalSelected(0).totalBatches(0).mapOutputsReceived(0)
                    .failedBatchIndexes(List.of()).failedBatchReasons(List.of("no input"))
                    .allSelectedChatIds(List.of()).usedChatIds(List.of()).missingChatIds(List.of())
                    .coverageComplete(false).build();

            listener.onCompleted(requestId, false, 0, 0, 0);

            return new OrchestrationResult(empty, List.of(), List.of(), List.of(), null, rr, 0, 0)
                    .withCoverage(List.of(), List.of(), false, 0);
        }

        String controlledPrompt = promptTemplateService.buildControlledPrompt(userMessage, true, false, true);
        List<String> authoritativeAllIds = extractUniqueChatIds(selectedEntries);

        List<SelectedEntry> segmentedEntries = contextBuilderService.explodeLargeEntriesToSegments(
                selectedEntries, segmentPromptChars, segmentResponseChars
        );
        if (segmentedEntries == null || segmentedEntries.isEmpty()) {
            segmentedEntries = selectedEntries;
        }

        List<String> mapOutputs = new ArrayList<>();
        List<Integer> failedBatches = new ArrayList<>();
        List<MapBatchResult> allMapBatchResults = new ArrayList<>();
        List<BatchFailure> allBatchFailures = new ArrayList<>();
        List<CompletableFuture<MapBatchExecutionResult>> futures = new ArrayList<>();
        List<MapBatchRequest> reqs = new ArrayList<>();

        int totalSelected = selectedEntries.size();
        int cumulativeBatchCounter = 0;

        Set<String> usedByProcessing = new LinkedHashSet<>();
        Set<String> remainingMissing = new LinkedHashSet<>(authoritativeAllIds);

        for (int round = 1; round <= maxRetryRounds && !remainingMissing.isEmpty(); round++) {
            List<SelectedEntry> roundEntries = filterEntriesByChatIds(segmentedEntries, remainingMissing);
            if (roundEntries.isEmpty()) {
                break;
            }

            boolean roundAnySuccess = false;
            boolean roundAllAuthFailures = true;

            listener.onMapRoundStarted(requestId, round, maxRetryRounds, remainingMissing.size(), roundEntries.size());

            List<List<SelectedEntry>> batches = contextBuilderService.splitForMapAdaptive(
                    roundEntries, FIXED_BATCH_SIZE, Math.min(FIXED_BATCH_SIZE, minBatchSize)
            );
            int totalBatchesThisRound = batches.size();
                int roundBatchUpperBound = cumulativeBatchCounter + totalBatchesThisRound;

            ExecutorService executor = Executors.newFixedThreadPool(mapMaxParallel);
            try {
                futures.clear();
                reqs.clear();

                for (int i = 0; i < totalBatchesThisRound; i++) {
                    int batchIndex = ++cumulativeBatchCounter;
                    List<SelectedEntry> batch = batches.get(i);
                    List<String> expectedBatchIds = extractUniqueChatIds(batch);
                    boolean statelessSummaryFlow = isDailySummarySession(sessionId);

                    MapBatchRequest req = MapBatchRequest.builder()
                            .requestId(requestId)
                            .totalSelected(totalSelected)
                            .totalBatches(roundBatchUpperBound)
                            .batchIndex(batchIndex)
                            .batchId("round-" + round + "-batch-" + (i + 1))
                            .entries(batch)
                            .targetUrl(targetUrl)
                            .mode(mode)
                            .sessionId(sessionId)
                            .reset(statelessSummaryFlow || (requestReset && round == 1 && i == 0))
                            .controlledPrompt(controlledPrompt)
                            .expectedChatIds(expectedBatchIds)
                            .authoritativeAllSelectedChatIds(authoritativeAllIds)
                            .build();

                    reqs.add(req);
                    listener.onMapBatchStarted(requestId, batchIndex, cumulativeBatchCounter, expectedBatchIds.size(), round);

                    final int rFinal = round;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            return executeMapBatchWithAdaptiveRebatchAndRecovery(apiKey, attachments, req, requestId, rFinal);
                        } catch (IOException | InterruptedException ex) {
                            throw new CompletionException(ex);
                        }
                    }, executor));
                }

                for (int i = 0; i < futures.size(); i++) {
                    MapBatchRequest req = reqs.get(i);
                    List<String> expectedForReq = normalizeIds(req.batchChatIds());

                    try {
                        MapBatchExecutionResult r = futures.get(i).join();
                        allMapBatchResults.add(r.result);
                        boolean mapBatchSuccess = r.result.isSuccess();
                        int httpStatus = r.result.getHttpStatus();

                        if (mapBatchSuccess) {
                            roundAnySuccess = true;
                            usedByProcessing.addAll(expectedForReq);
                            if (r.outputText != null && !r.outputText.isBlank()) {
                                mapOutputs.add("### Batch " + req.getBatchIndex() + '\n' + r.outputText);
                            }
                        } else {
                            failedBatches.add(Integer.valueOf(req.getBatchIndex()));
                            if (r.failure != null) {
                                allBatchFailures.add(r.failure);
                            }
                        }

                        if (httpStatus != 401 && httpStatus != 403) {
                            roundAllAuthFailures = false;
                        }

                        int missingSoFar = Math.max(0, authoritativeAllIds.size() - usedByProcessing.size());
                        listener.onMapBatchCompleted(
                                requestId,
                                req.getBatchIndex(),
                                cumulativeBatchCounter,
                                mapBatchSuccess,
                                usedByProcessing.size(),
                                missingSoFar,
                                round
                        );
                    } catch (CompletionException ex) {
                        failedBatches.add(Integer.valueOf(req.getBatchIndex()));
                        log.log(Level.WARNING, "[map-reduce][" + requestId + "] batch failed batchIndex=" + req.getBatchIndex(), ex);

                        BatchFailure failure = BatchFailure.builder()
                                .requestId(requestId).batchIndex(req.getBatchIndex()).totalBatches(req.getTotalBatches())
                                .batchId(req.getBatchId()).reasonCode("batch_processing_failure")
                                .message("Unhandled map batch failure").httpStatus(0)
                                .retryAttempted(true).retrySucceeded(false).contextTooLargeDetected(false)
                                .batchChatIds(req.batchChatIds()).build();
                        allBatchFailures.add(failure);

                        allMapBatchResults.add(MapBatchResult.builder()
                                .requestId(requestId).batchIndex(req.getBatchIndex()).totalBatches(req.getTotalBatches())
                                .batchId(req.getBatchId()).httpStatus(0).success(false).retryUsed(true)
                                .contextTooLargeDetected(false).modelOutput("").errorMessage("Unhandled map batch failure")
                                .inputEntriesCount(req.batchSize()).usedEntriesCount(0).omittedEntriesCount(req.batchSize())
                                .batchChatIds(req.batchChatIds()).usedChatIds(List.of()).omittedChatIds(req.batchChatIds())
                                .expectedChatIds(req.batchChatIds()).foundChatIds(List.of())
                                .missingExpectedChatIds(req.batchChatIds()).coverageComplete(false).latencyMs(0).build());

                            roundAllAuthFailures = false;

                        int missingSoFar = Math.max(0, authoritativeAllIds.size() - usedByProcessing.size());
                        listener.onMapBatchCompleted(
                                requestId,
                                req.getBatchIndex(),
                                cumulativeBatchCounter,
                                false,
                                usedByProcessing.size(),
                                missingSoFar,
                                round
                        );
                    }
                }
            } finally {
                executor.shutdownNow();
            }

            remainingMissing = new LinkedHashSet<>(authoritativeAllIds);
            remainingMissing.removeAll(usedByProcessing);

            listener.onMapRoundCompleted(requestId, round, maxRetryRounds, usedByProcessing.size(), remainingMissing.size());

            if (!roundAnySuccess && roundAllAuthFailures) {
                log.log(Level.WARNING,
                        "[map-reduce][{0}] stopping after round {1} due to authentication failures across all map batches.",
                        new Object[]{requestId, Integer.valueOf(round)});
                break;
            }
        }

        List<String> missingIds = new ArrayList<>(remainingMissing);
        boolean coverageComplete = missingIds.isEmpty();
        List<String> usedIds = subtract(authoritativeAllIds, missingIds);
        List<Integer> failedDistinct = distinctInts(failedBatches);
        int totalBatches = cumulativeBatchCounter;

        if (mapOutputs.isEmpty() && allMapFailuresAreAuthFailures(allMapBatchResults)) {
            listener.onReduceStarted(requestId, totalSelected, totalBatches, 0, missingIds.size());

            String authError = "Workspace authentication failed for all summary map batches (HTTP 401/403).";
            WorkspaceResponse authFailureResponse = new WorkspaceResponse(
                403,
                "{\"error\":\"No valid api key found.\",\"message\":\"" + authError + "\"}",
                "application/json"
            );
            ReduceResult authFailureReduce = ReduceResult.builder()
                .requestId(requestId)
                .httpStatus(403)
                .success(false)
                .retryUsed(false)
                .contextTooLargeDetected(false)
                .finalReport("")
                .errorMessage(authError)
                .totalSelected(totalSelected)
                .totalBatches(totalBatches)
                .mapOutputsReceived(0)
                .failedBatchIndexes(failedDistinct)
                .failedBatchReasons(List.of("upstream_4xx_auth_failure"))
                .allSelectedChatIds(authoritativeAllIds)
                .usedChatIds(usedIds)
                .missingChatIds(missingIds)
                .coverageComplete(false)
                .latencyMs(0)
                .build();

            listener.onReduceCompleted(requestId, false, 403, missingIds.size());
            listener.onCompleted(requestId, false, usedIds.size(), missingIds.size(), totalBatches);

            return new OrchestrationResult(
                authFailureResponse,
                mapOutputs,
                failedDistinct,
                allMapBatchResults,
                null,
                authFailureReduce,
                totalSelected,
                totalBatches
            ).withBatchFailures(allBatchFailures)
                .withCoverage(authoritativeAllIds, missingIds, false, maxRetryRounds);
        }

        listener.onReduceStarted(requestId, totalSelected, totalBatches, mapOutputs.size(), missingIds.size());

        ReduceExecutionResult reduceExecution = executeReduceHierarchical(
                targetUrl, apiKey, controlledPrompt, mode, sessionId, attachments,
                totalSelected, totalBatches, mapOutputs, failedDistinct, requestId,
                coverageComplete, missingIds, authoritativeAllIds, usedIds,
                listener
        );

        listener.onReduceCompleted(
                requestId,
                reduceExecution.reduceResult != null && reduceExecution.reduceResult.isSuccess(),
                reduceExecution.response == null ? 500 : reduceExecution.response.statusCode(),
                missingIds.size()
        );

        listener.onCompleted(requestId, coverageComplete, usedIds.size(), missingIds.size(), totalBatches);

        return new OrchestrationResult(
                reduceExecution.response, mapOutputs, failedDistinct, allMapBatchResults,
                reduceExecution.reduceRequest, reduceExecution.reduceResult, totalSelected, totalBatches
        ).withBatchFailures(allBatchFailures)
                .withCoverage(authoritativeAllIds, missingIds, coverageComplete, maxRetryRounds);
    }

    private ReduceExecutionResult executeReduceHierarchical(
            String targetUrl, String apiKey, String controlledPrompt, String mode, String sessionId, JsonArray attachments,
            int totalSelected, int totalBatches, List<String> mapOutputs, List<Integer> failedBatches, String requestId,
            boolean coverageComplete, List<String> missingChatIds, List<String> allSelectedChatIds, List<String> usedChatIds,
            ProgressListener listener
    ) throws IOException, InterruptedException {

        List<String> current = mapOutputs == null ? List.of() : new ArrayList<>(mapOutputs);
        int level = 1;
        int chunkSize = reduceInitialChunkSize;

        while (current.size() > 1 && level <= reduceMaxLevels) {
            boolean levelSucceeded = false;
            int localChunk = chunkSize;
            List<String> nextLevel = new ArrayList<>();

            while (!levelSucceeded && localChunk >= reduceMinChunkSize) {
                List<List<String>> chunks = chunk(current, localChunk);
                nextLevel.clear();
                boolean anyChunkFailed = false;

                for (int i = 0; i < chunks.size(); i++) {
                    int chunkIndex = i + 1;
                    List<String> chunkOutputs = chunks.get(i);

                    listener.onReduceChunkStarted(requestId, level, chunkIndex, chunks.size(), chunkOutputs.size(), localChunk);

                    ReduceExecutionResult chunkResult = executeReduce(
                            targetUrl, apiKey, controlledPrompt, mode, sessionId, attachments,
                            totalSelected, totalBatches, chunkOutputs, failedBatches, requestId + "-L" + level + "-C" + chunkIndex,
                            coverageComplete, missingChatIds, allSelectedChatIds, usedChatIds,
                            false
                    );

                    boolean ok = chunkResult.reduceResult != null
                            && chunkResult.response != null
                            && chunkResult.response.statusCode() < 400
                            && chunkResult.reduceResult.getFinalReport() != null
                            && !chunkResult.reduceResult.getFinalReport().isBlank();

                    listener.onReduceChunkCompleted(
                            requestId, level, chunkIndex, chunks.size(), ok,
                            chunkResult.response == null ? 500 : chunkResult.response.statusCode()
                    );

                    if (!ok) {
                        anyChunkFailed = true;
                        break;
                    }

                    String summary = trimTo(chunkResult.reduceResult.getFinalReport(), reduceChunkSummaryMaxChars);
                    nextLevel.add("### Reduce Level " + level + " Chunk " + chunkIndex + '\n' + summary);
                }

                if (anyChunkFailed) {
                    localChunk = localChunk / 2;
                } else {
                    current = new ArrayList<>(nextLevel);
                    levelSucceeded = true;
                    listener.onReduceLevelCompleted(requestId, level, chunks.size(), nextLevel.size());
                }
            }

            if (!levelSucceeded) {
                break;
            }

            level++;
            chunkSize = Math.max(reduceMinChunkSize, chunkSize - 1);
        }

        List<String> finalInputs = current.isEmpty() ? mapOutputs : current;
        List<String> boundedFinalInputs = boundFinalInputs(finalInputs, finalReduceMaxSummaries, finalReduceSummaryMaxChars);

        int attempts = 0;
        int maxSummaries = finalReduceMaxSummaries;
        int perSummaryChars = finalReduceSummaryMaxChars;

        ReduceExecutionResult last = null;
        while (attempts < finalReduceMaxAttempts) {
            attempts++;
            List<String> trialInputs = boundFinalInputs(boundedFinalInputs, maxSummaries, perSummaryChars);

            listener.onReduceChunkStarted(requestId, 999, attempts, finalReduceMaxAttempts, trialInputs.size(), maxSummaries);

            last = executeReduce(
                    targetUrl, apiKey, controlledPrompt, mode, sessionId, attachments,
                    totalSelected, totalBatches, trialInputs, failedBatches, requestId,
                    coverageComplete, missingChatIds, allSelectedChatIds, usedChatIds,
                    true
            );

            boolean ok = last != null
                    && last.response != null
                    && last.response.statusCode() < 400
                    && last.reduceResult != null
                    && last.reduceResult.isSuccess();

            listener.onReduceChunkCompleted(
                    requestId, 999, attempts, finalReduceMaxAttempts, ok,
                    last == null || last.response == null ? 500 : last.response.statusCode()
            );

            if (ok) {
                return last;
            }

            String body = last == null || last.response == null ? "" : (last.response.body() == null ? "" : last.response.body());
            boolean tooLarge = (last != null && last.reduceResult != null && last.reduceResult.isContextTooLargeDetected())
                    || isLikelyContextLimitError(body, last == null || last.response == null ? 0 : last.response.statusCode());

            if (!tooLarge) {
                return last;
            }

            maxSummaries = Math.max(1, maxSummaries - 1);
            perSummaryChars = Math.max(300, perSummaryChars / 2);
        }

        return last;
    }

    private List<String> boundFinalInputs(List<String> inputs, int maxItems, int maxCharsPerItem) {
        List<String> out = new ArrayList<>();
        if (inputs == null || inputs.isEmpty()) {
            return out;
        }
        int limit = Math.max(1, maxItems);
        int chars = Math.max(200, maxCharsPerItem);

        for (int i = 0; i < inputs.size() && out.size() < limit; i++) {
            String s = inputs.get(i);
            if (s == null || s.isBlank()) {
                continue;
            }
            out.add(trimTo(s, chars));
        }

        if (out.isEmpty()) {
            out.add(trimTo(inputs.get(0), chars));
        }
        return out;
    }

    private boolean isLikelyContextLimitError(String body, int status) {
        String b = body == null ? "" : body.toLowerCase(Locale.ROOT);
        return status == 400
                && (b.contains("maximum context length")
                || b.contains("requested")
                || b.contains("failed_to_embed")
                || b.contains("reduce your prompt"));
    }

    private List<List<String>> chunk(List<String> src, int size) {
        List<List<String>> out = new ArrayList<>();
        if (src == null || src.isEmpty()) {
            return out;
        }
        int s = Math.max(1, size);
        for (int i = 0; i < src.size(); i += s) {
            out.add(new ArrayList<>(src.subList(i, Math.min(src.size(), i + s))));
        }
        return out;
    }

    private MapBatchExecutionResult executeMapBatchWithAdaptiveRebatchAndRecovery(
            String apiKey, JsonArray attachments, MapBatchRequest req, String requestId, int round
    ) throws IOException, InterruptedException {

        MapBatchExecutionResult primary = executeMapBatchWithAdaptiveRebatch(apiKey, attachments, req, requestId, round);

        if (primary.result == null) {
            return primary;
        }
        if (primary.result.isSuccess()) {
            // Successful batch already contributes expected coverage; avoid duplicate recovery calls.
            return primary;
        }

        int primaryStatus = primary.result.getHttpStatus();
        if (primaryStatus == 401 || primaryStatus == 403) {
            // Authentication failures do not improve with marker recovery retries.
            return primary;
        }

        List<String> expected = normalizeIds(req.batchChatIds());
        Set<String> missingByMarkers = new LinkedHashSet<>(expected);
        missingByMarkers.removeAll(normalizeIds(primary.usedIdsDetected));

        if (missingByMarkers.isEmpty()) {
            return primary;
        }

        List<SelectedEntry> missingEntries = filterEntriesByChatIds(req.getEntries(), missingByMarkers);
        if (missingEntries.isEmpty()) {
            return primary;
        }

        MapBatchRequest recoveryReq = MapBatchRequest.builder()
                .requestId(req.getRequestId())
                .totalSelected(req.getTotalSelected())
                .totalBatches(req.getTotalBatches())
                .batchIndex(req.getBatchIndex())
                .batchId(req.getBatchId() + "-recovery")
                .entries(missingEntries)
                .targetUrl(req.getTargetUrl())
                .mode(req.getMode())
                .sessionId(req.getSessionId())
                .reset(false)
                .controlledPrompt(req.getControlledPrompt())
                .expectedChatIds(new ArrayList<>(missingByMarkers))
                .authoritativeAllSelectedChatIds(req.getAuthoritativeAllSelectedChatIds())
                .build();

        MapBatchExecutionResult recovery = executeMapBatchWithAdaptiveRebatch(apiKey, attachments, recoveryReq, requestId, round);

        List<String> mergedMarkerUsed = new ArrayList<>();
        mergedMarkerUsed.addAll(normalizeIds(primary.usedIdsDetected));
        mergedMarkerUsed.addAll(normalizeIds(recovery.usedIdsDetected));
        mergedMarkerUsed = normalizeIds(mergedMarkerUsed);

        Set<String> markerMissingAfterRecovery = new LinkedHashSet<>(expected);
        markerMissingAfterRecovery.removeAll(mergedMarkerUsed);

        boolean mergedSuccess = primary.result.isSuccess() || recovery.result.isSuccess();

        String mergedOutput = (primary.outputText == null ? "" : primary.outputText)
                + ((recovery.outputText == null || recovery.outputText.isBlank()) ? "" : "\n\n" + recovery.outputText);

        String mergedErr = mergedSuccess ? "" : "Batch processing failed (primary+recovery).";

        MapBatchResult merged = MapBatchResult.builder()
                .requestId(req.getRequestId())
                .batchIndex(req.getBatchIndex())
                .totalBatches(req.getTotalBatches())
                .batchId(req.getBatchId())
                .httpStatus(Math.max(primary.result.getHttpStatus(), recovery.result.getHttpStatus()))
                .success(mergedSuccess)
                .retryUsed(primary.result.isRetryUsed() || recovery.result.isRetryUsed())
                .contextTooLargeDetected(primary.result.isContextTooLargeDetected() || recovery.result.isContextTooLargeDetected())
                .modelOutput(mergedOutput)
                .errorMessage(mergedErr)
                .inputEntriesCount(req.batchSize())
                .usedEntriesCount(mergedSuccess ? expected.size() : 0)
                .omittedEntriesCount(mergedSuccess ? 0 : expected.size())
                .batchChatIds(expected)
                .usedChatIds(mergedSuccess ? expected : List.of())
                .omittedChatIds(mergedSuccess ? List.of() : expected)
                .expectedChatIds(expected)
                .foundChatIds(mergedMarkerUsed)
                .missingExpectedChatIds(new ArrayList<>(markerMissingAfterRecovery))
                .coverageComplete(mergedSuccess)
                .latencyMs(primary.result.getLatencyMs() + recovery.result.getLatencyMs())
                .build();

        if (mergedSuccess) {
            return new MapBatchExecutionResult(mergedOutput, merged, null, mergedMarkerUsed);
        }

        BatchFailure failure = BatchFailure.builder()
                .requestId(req.getRequestId())
                .batchIndex(req.getBatchIndex())
                .totalBatches(req.getTotalBatches())
                .batchId(req.getBatchId())
                .reasonCode("batch_processing_failure")
                .message(mergedErr)
                .httpStatus(merged.getHttpStatus())
                .retryAttempted(true)
                .retrySucceeded(false)
                .contextTooLargeDetected(merged.isContextTooLargeDetected())
                .latencyMs(merged.getLatencyMs())
                .batchChatIds(expected)
                .build();

        return new MapBatchExecutionResult("", merged, failure, mergedMarkerUsed);
    }

    private MapBatchExecutionResult executeMapBatchWithAdaptiveRebatch(
            String apiKey, JsonArray attachments, MapBatchRequest req, String requestId, int round
    ) throws IOException, InterruptedException {
        try {
            return executeMapBatch(apiKey, attachments, req, requestId);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            boolean likelyTooLarge = msg.contains("too large") || msg.contains("re-batch");
            if (!likelyTooLarge || req.batchSize() <= minBatchSize) {
                throw ex;
            }

            int nextSize = Math.max(minBatchSize, req.batchSize() / 2);
            if (nextSize >= req.batchSize()) {
                throw ex;
            }

            List<List<SelectedEntry>> split = contextBuilderService.splitForMapAdaptive(req.getEntries(), nextSize, minBatchSize);

            List<String> mergedOutputs = new ArrayList<>();
            List<String> mergedMarkers = new ArrayList<>();
            List<String> mergedExpected = new ArrayList<>();
            List<String> subErrors = new ArrayList<>();
            boolean anySuccess = false;
            long maxLatency = 0;
            int worstHttp = 200;
            boolean anyContextTooLarge = false;
            boolean anyRetry = false;

            int subIdx = 0;
            for (List<SelectedEntry> sub : split) {
                subIdx++;
                List<String> expectedSub = extractUniqueChatIds(sub);

                MapBatchRequest subReq = MapBatchRequest.builder()
                        .requestId(req.getRequestId())
                        .totalSelected(req.getTotalSelected())
                        .totalBatches(req.getTotalBatches())
                        .batchIndex(req.getBatchIndex())
                        .batchId(req.getBatchId() + "-sub-" + subIdx)
                        .entries(sub)
                        .targetUrl(req.getTargetUrl())
                        .mode(req.getMode())
                        .sessionId(req.getSessionId())
                        .reset(req.isReset() && subIdx == 1)
                        .controlledPrompt(req.getControlledPrompt())
                        .expectedChatIds(expectedSub)
                        .authoritativeAllSelectedChatIds(req.getAuthoritativeAllSelectedChatIds())
                        .build();

                MapBatchExecutionResult sr = executeMapBatchWithAdaptiveRebatch(apiKey, attachments, subReq, requestId, round);

                MapBatchResult r = sr.result;
                mergedOutputs.add(sr.outputText == null ? "" : sr.outputText);
                mergedMarkers.addAll(normalizeIds(sr.usedIdsDetected));
                mergedExpected.addAll(normalizeIds(r.getExpectedChatIds()));
                boolean subSuccess = r.isSuccess();

                anySuccess = anySuccess || subSuccess;
                if (!subSuccess && r.getErrorMessage() != null && !r.getErrorMessage().isBlank()) {
                    subErrors.add(r.getErrorMessage());
                }

                maxLatency = Math.max(maxLatency, r.getLatencyMs());
                worstHttp = Math.max(worstHttp, r.getHttpStatus());
                anyContextTooLarge = anyContextTooLarge || r.isContextTooLargeDetected();
                anyRetry = anyRetry || r.isRetryUsed();
            }

            List<String> expectedDistinct = normalizeIds(mergedExpected);
            List<String> markerDistinct = normalizeIds(mergedMarkers);

            boolean success = anySuccess;
            String modelOutput = String.join("\n\n", mergedOutputs);
            String err = success ? "" : (subErrors.isEmpty() ? "Adaptive sub-batches all failed." : String.join("; ", subErrors));

            MapBatchResult mergedResult = MapBatchResult.builder()
                    .requestId(req.getRequestId())
                    .batchIndex(req.getBatchIndex())
                    .totalBatches(req.getTotalBatches())
                    .batchId(req.getBatchId())
                    .httpStatus(worstHttp)
                    .success(success)
                    .retryUsed(anyRetry)
                    .contextTooLargeDetected(anyContextTooLarge)
                    .modelOutput(modelOutput)
                    .errorMessage(err)
                    .inputEntriesCount(req.batchSize())
                    .usedEntriesCount(success ? expectedDistinct.size() : 0)
                    .omittedEntriesCount(success ? 0 : expectedDistinct.size())
                    .batchChatIds(expectedDistinct)
                    .usedChatIds(success ? expectedDistinct : List.of())
                    .omittedChatIds(success ? List.of() : expectedDistinct)
                    .expectedChatIds(expectedDistinct)
                    .foundChatIds(markerDistinct)
                    .missingExpectedChatIds(success ? List.of() : expectedDistinct)
                    .coverageComplete(success)
                    .latencyMs(maxLatency)
                    .build();

            if (success) {
                return new MapBatchExecutionResult(modelOutput, mergedResult, null, markerDistinct);
            }

            BatchFailure failure = BatchFailure.builder()
                    .requestId(req.getRequestId())
                    .batchIndex(req.getBatchIndex())
                    .totalBatches(req.getTotalBatches())
                    .batchId(req.getBatchId())
                    .reasonCode("batch_processing_failure")
                    .message(err)
                    .httpStatus(worstHttp)
                    .retryAttempted(true)
                    .retrySucceeded(false)
                    .contextTooLargeDetected(anyContextTooLarge)
                    .latencyMs(maxLatency)
                    .batchChatIds(expectedDistinct)
                    .build();

            return new MapBatchExecutionResult("", mergedResult, failure, markerDistinct);
        }
    }

    private MapBatchExecutionResult executeMapBatch(
            String apiKey, JsonArray attachments, MapBatchRequest req, String requestId
        ) throws IOException, InterruptedException {
        long start = Instant.now().toEpochMilli();

        String deterministicHeader = contextBuilderService.buildBatchDeterministicHeader(
                req.getTotalSelected(), req.getTotalBatches(), req.getBatchIndex(), req.getEntries()
        );

        List<String> expectedIds = normalizeIds(req.batchChatIds());

        String mapPrompt = req.getControlledPrompt() + "\n\n" + deterministicHeader + "\nExpected chat IDs in this batch: " + expectedIds
                + "\nBatch Task:\nProduce markdown analysis for this batch with aggregate insights and metrics.\n"
                + "Do NOT produce per-chat sections unless explicitly requested.\n"
                + "Include covered_chat_ids: [..] contract line if possible.";

        String mapContext = contextBuilderService.buildMapBatchContext(
                mapPrompt, req.getEntries(), req.getBatchIndex(), req.getTotalBatches(), mapContextMaxChars, expectedIds
        );
        String mapMessage = buildOutboundMessage(mapPrompt, mapContext, mapMessageMaxChars);

        WorkspaceResponse response = workspaceClient.sendChat(
                req.getTargetUrl(), apiKey, mapMessage, req.getMode(), req.getSessionId(), req.isReset(), attachments, requestId
        );

        boolean contextTooLarge = workspaceClient.isLikelyContextTooLarge(response);
        boolean retryUsed = false;

        if (contextTooLarge) {
            retryUsed = true;
            String retryContext = contextBuilderService.buildMapBatchContext(
                    mapPrompt, req.getEntries(), req.getBatchIndex(), req.getTotalBatches(), retryContextChars, expectedIds
            );
            String retryMessage = buildOutboundMessage(mapPrompt, retryContext, retryTotalMessageChars);

            response = workspaceClient.sendChat(
                    req.getTargetUrl(), apiKey, retryMessage, req.getMode(), req.getSessionId(), true, attachments, requestId
            );
        }

        int status = response.statusCode();
        String mapText = status >= 400 ? "" : extractPrimaryTextFromWorkspaceResponse(response.body());
        String canonicalMapText = canonicalizeForValidation(mapText);

        ReviewOutputValidator.ValidationResult validation = reviewOutputValidator.validateMapOutput(canonicalMapText, mapMessageMaxChars);
        boolean mapOutputValid = validation.isValid();
        if (!mapOutputValid) {
            log.log(
                Level.INFO,
                "[map-reduce][{0}][map] non-fatal validation errors batch={1} errors={2}",
                new Object[]{requestId, req.getBatchIndex(), validation.getErrors()}
            );
        }

        List<String> contractIds = parseCoveredIdsContract(mapText);
        List<String> headingIds = parseChatIdsFromMapOutput(mapText);
        List<String> markerDetected = !contractIds.isEmpty() ? contractIds : headingIds;
        markerDetected = intersect(expectedIds, normalizeIds(markerDetected));

        boolean success = status < 400 && mapText != null && !mapText.isBlank();

        String err = success ? "" : ("Map call failed or empty output (status=" + status + ')');

        List<String> usedForCoverage = success ? expectedIds : List.of();
        List<String> omittedForCoverage = success ? List.of() : expectedIds;

        MapBatchResult result = MapBatchResult.builder()
                .requestId(req.getRequestId())
                .batchIndex(req.getBatchIndex())
                .totalBatches(req.getTotalBatches())
                .batchId(req.getBatchId())
                .httpStatus(status)
                .success(success)
                .retryUsed(retryUsed)
                .contextTooLargeDetected(contextTooLarge)
                .modelOutput(mapText == null ? "" : mapText)
                .errorMessage(err)
                .inputEntriesCount(req.batchSize())
                .usedEntriesCount(usedForCoverage.size())
                .omittedEntriesCount(omittedForCoverage.size())
                .batchChatIds(expectedIds)
                .usedChatIds(usedForCoverage)
                .omittedChatIds(omittedForCoverage)
                .expectedChatIds(expectedIds)
                .foundChatIds(markerDetected)
                .missingExpectedChatIds(subtract(expectedIds, markerDetected))
                .coverageComplete(success)
                .latencyMs(Instant.now().toEpochMilli() - start)
                .build();

        if (success) {
            return new MapBatchExecutionResult(mapText, result, null, markerDetected);
        }

        BatchFailure failure = BatchFailure.builder()
                .requestId(req.getRequestId())
                .batchIndex(req.getBatchIndex())
                .totalBatches(req.getTotalBatches())
                .batchId(req.getBatchId())
                .reasonCode(determineReasonCode(status, contextTooLarge, mapOutputValid))
                .message(result.getErrorMessage())
                .httpStatus(status)
                .retryAttempted(retryUsed)
                .retrySucceeded(false)
                .contextTooLargeDetected(contextTooLarge)
                .latencyMs(result.getLatencyMs())
                .batchChatIds(expectedIds)
                .build();

        return new MapBatchExecutionResult("", result, failure, markerDetected);
    }

    private ReduceExecutionResult executeReduce(
            String targetUrl, String apiKey, String controlledPrompt, String mode, String sessionId, JsonArray attachments,
            int totalSelected, int totalBatches, List<String> mapOutputs, List<Integer> failedBatches, String requestId,
            boolean coverageComplete, List<String> missingChatIds, List<String> allSelectedChatIds, List<String> usedChatIds,
            boolean minimalHeader
    ) throws IOException, InterruptedException {

        long start = Instant.now().toEpochMilli();

        List<String> allIdsNorm = normalizeIds(allSelectedChatIds);
        List<String> missingIdsNorm = normalizeIds(missingChatIds);
        List<String> usedIdsNorm = normalizeIds(usedChatIds);

        missingIdsNorm = intersect(allIdsNorm, missingIdsNorm);
        if (usedIdsNorm.isEmpty()) {
            usedIdsNorm = subtract(allIdsNorm, missingIdsNorm);
        } else {
            usedIdsNorm = intersect(allIdsNorm, usedIdsNorm);
        }
        boolean effectiveCoverageComplete = coverageComplete && missingIdsNorm.isEmpty();

        String deterministicReduceHeader = minimalHeader
                ? """
                Deterministic metadata (compact):
                - exact_total_selected: %d
                - exact_total_batches: %d
                - exact_map_outputs_received: %d
                - exact_failed_batch_indexes: %s
                - coverage_complete: %s
                - used_count: %d
                - missing_count: %d
                """.formatted(
                    Integer.valueOf(totalSelected),
                    Integer.valueOf(totalBatches),
                    Integer.valueOf(mapOutputs.size()),
                    failedBatches.toString(),
                    String.valueOf(effectiveCoverageComplete),
                    Integer.valueOf(usedIdsNorm.size()),
                    Integer.valueOf(missingIdsNorm.size())
                )
                : """
                Deterministic metadata (use exactly; do not estimate):
                - exact_total_selected: %d
                - exact_total_batches: %d
                - exact_map_outputs_received: %d
                - exact_failed_batch_indexes: %s
                - coverage_complete: %s
                - all_selected_ids_count: %d
                - used_ids_count: %d
                - missing_ids_count: %d
                """.formatted(
                    Integer.valueOf(totalSelected),
                    Integer.valueOf(totalBatches),
                    Integer.valueOf(mapOutputs.size()),
                    failedBatches.toString(),
                    String.valueOf(effectiveCoverageComplete),
                    Integer.valueOf(allIdsNorm.size()),
                    Integer.valueOf(usedIdsNorm.size()),
                    Integer.valueOf(missingIdsNorm.size())
                );

        String reducePrompt = controlledPrompt + "\n\n" + deterministicReduceHeader + "\nReduce Task:\n"
                + "Synthesize a manager-ready final report.\n"
                + "Do NOT include 'Per-Chat Analysis'.\n"
                + "Provide overall executive analysis, metrics, risks/opportunities, recommendations, and coverage.";

        String reduceContext = contextBuilderService.buildReduceContext(
                reducePrompt, mapOutputs, failedBatches, allIdsNorm, missingIdsNorm, minimalHeader ? Math.max(800, retryContextChars) : reduceContextMaxChars
        );
        String reduceMessage = buildOutboundMessage(
                reducePrompt,
                reduceContext,
                minimalHeader ? Math.max(1200, retryTotalMessageChars) : reduceMessageMaxChars
        );

        ReduceRequest reduceRequest = ReduceRequest.builder()
                .requestId(requestId)
                .targetUrl(targetUrl)
                .mode(mode)
                .sessionId(sessionId)
                .reset(true)
                .controlledPrompt(controlledPrompt)
                .totalSelected(totalSelected)
                .totalBatches(totalBatches)
                .mapOutputs(mapOutputs)
                .failedBatchIndexes(failedBatches)
                .failedBatchReasons(List.of("batch processing failure"))
                .allSelectedChatIds(allIdsNorm)
                .usedChatIds(usedIdsNorm)
                .missingChatIds(missingIdsNorm)
                .coverageComplete(effectiveCoverageComplete)
                .build();

        WorkspaceResponse reduceResponse = workspaceClient.sendChat(
                targetUrl, apiKey, reduceMessage, mode, sessionId, true, attachments, requestId
        );

        boolean retryUsed = false;
        boolean contextTooLarge = workspaceClient.isLikelyContextTooLarge(reduceResponse);

        if (contextTooLarge) {
            retryUsed = true;
            String retryReduceContext = contextBuilderService.buildReduceContext(
                    reducePrompt, mapOutputs, failedBatches, allIdsNorm, missingIdsNorm, Math.max(600, retryContextChars)
            );
            String retryReduceMessage = buildOutboundMessage(reducePrompt, retryReduceContext, Math.max(1000, retryTotalMessageChars));

            reduceResponse = workspaceClient.sendChat(
                    targetUrl, apiKey, retryReduceMessage, mode, sessionId, true, attachments, requestId
            );
        }

        String reduceText = extractPrimaryTextFromWorkspaceResponse(reduceResponse.body());
        String canonicalReduceText = canonicalizeForValidation(reduceText);

        ReviewOutputValidator.ValidationResult validation
            = reviewOutputValidator.validateFinalReportHierarchical(canonicalReduceText, allIdsNorm, Math.max(1200, reduceMessageMaxChars));
        boolean reduceOutputValid = validation.isValid();

        boolean reduceSuccess = reduceResponse.statusCode() < 400 && reduceOutputValid && effectiveCoverageComplete;

        String err = "";
        if (!reduceSuccess) {
            if (reduceResponse.statusCode() >= 400) {
                err = "Reduce call failed with status " + reduceResponse.statusCode();
            } else if (!reduceOutputValid) {
                err = String.join("; ", validation.getErrors());
            } else {
                err = "Coverage incomplete. Missing chat IDs: " + missingIdsNorm;
            }
        }

        ReduceResult reduceResult = ReduceResult.builder()
                .requestId(requestId)
                .httpStatus(reduceResponse.statusCode())
                .success(reduceSuccess)
                .retryUsed(retryUsed)
                .contextTooLargeDetected(contextTooLarge || isLikelyContextLimitError(reduceResponse.body(), reduceResponse.statusCode()))
                .finalReport(reduceText)
                .errorMessage(err)
                .totalSelected(totalSelected)
                .totalBatches(totalBatches)
                .mapOutputsReceived(mapOutputs.size())
                .failedBatchIndexes(failedBatches)
                .failedBatchReasons(List.of("batch processing failure"))
                .allSelectedChatIds(allIdsNorm)
                .usedChatIds(usedIdsNorm)
                .missingChatIds(missingIdsNorm)
                .coverageComplete(effectiveCoverageComplete)
                .latencyMs(Instant.now().toEpochMilli() - start)
                .build();

        return new ReduceExecutionResult(reduceResponse, reduceRequest, reduceResult);
    }

    private List<String> parseCoveredIdsContract(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Matcher m = COVERED_IDS_LINE.matcher(text);
        if (!m.find()) {
            return List.of();
        }
        String inner = m.group(1);
        if (inner == null || inner.isBlank()) {
            return List.of();
        }

        String[] parts = inner.split(",");
        Set<String> out = new LinkedHashSet<>();
        for (String p : parts) {
            String id = normalizeId(p);
            if (!id.isBlank() && !"...".equals(id)) {
                out.add(id);
            }
        }
        return new ArrayList<>(out);
    }

    private List<String> parseChatIdsFromMapOutput(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        Matcher m = CHAT_HEADING_PATTERN.matcher(text);
        while (m.find()) {
            String id = normalizeId(m.group(1));
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return new ArrayList<>(ids);
    }

    private String determineReasonCode(int status, boolean contextTooLarge, boolean validMapOutput) {
        if (contextTooLarge) {
            return "context_too_large";
        }
        if (status >= 400 && status < 500) {
            return "upstream_4xx";
        }
        if (status >= 500) {
            return "upstream_5xx";
        }
        if (!validMapOutput) {
            return "parse_error";
        }
        return "batch_processing_failure";
    }

    private boolean isDailySummarySession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        String normalized = sessionId.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("dashboard-daily-summary");
    }

    private boolean allMapFailuresAreAuthFailures(List<MapBatchResult> results) {
        if (results == null || results.isEmpty()) {
            return false;
        }

        boolean sawFailure = false;
        for (MapBatchResult result : results) {
            if (result == null) {
                continue;
            }
            if (result.isSuccess()) {
                return false;
            }

            sawFailure = true;
            int status = result.getHttpStatus();
            if (status != 401 && status != 403) {
                return false;
            }
        }
        return sawFailure;
    }

    private List<String> extractUniqueChatIds(List<SelectedEntry> entries) {
        Set<String> ids = new LinkedHashSet<>();
        if (entries != null) {
            for (SelectedEntry e : entries) {
                if (e == null) {
                    continue;
                }
                String id = normalizeId(e.getChatId());
                if (!id.isBlank()) {
                    ids.add(id);
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private List<SelectedEntry> filterEntriesByChatIds(List<SelectedEntry> entries, Set<String> wanted) {
        if (entries == null || entries.isEmpty() || wanted == null || wanted.isEmpty()) {
            return List.of();
        }
        List<SelectedEntry> out = new ArrayList<>();
        for (SelectedEntry e : entries) {
            if (e == null) {
                continue;
            }
            String id = normalizeId(e.getChatId());
            if (wanted.contains(id)) {
                out.add(e);
            }
        }
        return out;
    }

    private String normalizeId(String v) {
        return v == null ? "" : v.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> normalizeIds(List<String> ids) {
        Set<String> out = new LinkedHashSet<>();
        if (ids != null) {
            for (String id : ids) {
                String n = normalizeId(id);
                if (!n.isBlank()) {
                    out.add(n);
                }
            }
        }
        return new ArrayList<>(out);
    }

    private List<String> intersect(List<String> a, List<String> b) {
        Set<String> sa = new LinkedHashSet<>(normalizeIds(a));
        Set<String> sb = new LinkedHashSet<>(normalizeIds(b));
        sa.retainAll(sb);
        return new ArrayList<>(sa);
    }

    private List<Integer> distinctInts(List<Integer> src) {
        Set<Integer> s = new LinkedHashSet<>();
        if (src != null) {
            for (Integer i : src) {
                if (i != null && i.intValue() > 0) {
                    s.add(Integer.valueOf(i.intValue()));
                }
            }
        }
        return new ArrayList<>(s);
    }

    private List<String> subtract(List<String> all, List<String> remove) {
        Set<String> a = new LinkedHashSet<>(normalizeIds(all));
        a.removeAll(new LinkedHashSet<>(normalizeIds(remove)));
        return new ArrayList<>(a);
    }

    private String extractPrimaryTextFromWorkspaceResponse(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject o = reader.readObject();
            String t = o.getString("textResponse", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("response", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("message", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("answer", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("output", "");
            if (!t.isBlank()) {
                return t;
            }
            return body;
        } catch (JsonException ex) {
            log.log(Level.FINE, "[map-reduce] failed parsing workspace response; returning raw body", ex);
            return body;
        }
    }

    private String canonicalizeForValidation(String value) {
        String normalized = Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFKC);
        return normalized.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
    }

    private String buildOutboundMessage(String userMessage, String context, int maxTotalChars) {
        String base = userMessage == null ? "" : userMessage.trim();
        if (context == null || context.isBlank()) {
            return trimTo(base, maxTotalChars);
        }

        String suffix = "\n\nSelected chats context:\n" + context;
        String combined = base + suffix;
        if (combined.length() <= maxTotalChars) {
            return combined;
        }

        int roomForSuffix = Math.max(0, maxTotalChars - base.length());
        if (roomForSuffix <= 0) {
            return trimTo(base, maxTotalChars);
        }
        return base + trimTo(suffix, roomForSuffix);
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    static final class MapBatchExecutionResult {

        final String outputText;
        final MapBatchResult result;
        final BatchFailure failure;
        final List<String> usedIdsDetected;

        private MapBatchExecutionResult(
            String outputText, MapBatchResult result, BatchFailure failure, List<String> usedIdsDetected
        ) {
            this.outputText = outputText;
            this.result = result;
            this.failure = failure;
            this.usedIdsDetected = usedIdsDetected == null ? List.of() : List.copyOf(usedIdsDetected);
        }
    }

    static final class ReduceExecutionResult {

        final WorkspaceResponse response;
        final ReduceRequest reduceRequest;
        final ReduceResult reduceResult;

        private ReduceExecutionResult(WorkspaceResponse response, ReduceRequest reduceRequest, ReduceResult reduceResult) {
            this.response = response;
            this.reduceRequest = reduceRequest;
            this.reduceResult = reduceResult;
        }
    }

    public static final class OrchestrationResult {

        private final WorkspaceResponse finalResponse;
        private final List<String> mapOutputs;
        private final List<Integer> failedBatchIndexes;
        private final List<MapBatchResult> mapBatchResults;
        private final ReduceRequest reduceRequest;
        private final ReduceResult reduceResult;
        private final int totalSelected;
        private final int totalBatches;
        private final List<BatchFailure> batchFailures;

        private final List<String> allSelectedChatIds;
        private final List<String> missingChatIds;
        private final boolean coverageComplete;
        private final int coveragePassesUsed;

        OrchestrationResult(
                WorkspaceResponse finalResponse,
                List<String> mapOutputs,
                List<Integer> failedBatchIndexes,
                List<MapBatchResult> mapBatchResults,
                ReduceRequest reduceRequest,
                ReduceResult reduceResult,
                int totalSelected,
                int totalBatches
        ) {
            this(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                    totalSelected, totalBatches, List.of(), List.of(), List.of(), false, 0);
        }

        OrchestrationResult(
                WorkspaceResponse finalResponse,
                List<String> mapOutputs,
                List<Integer> failedBatchIndexes,
                List<MapBatchResult> mapBatchResults,
                ReduceRequest reduceRequest,
                ReduceResult reduceResult,
                int totalSelected,
                int totalBatches,
                List<BatchFailure> batchFailures,
                List<String> allSelectedChatIds,
                List<String> missingChatIds,
                boolean coverageComplete,
                int coveragePassesUsed
        ) {
            this.finalResponse = finalResponse;
            this.mapOutputs = mapOutputs == null ? List.of() : List.copyOf(mapOutputs);
            this.failedBatchIndexes = failedBatchIndexes == null ? List.of() : List.copyOf(failedBatchIndexes);
            this.mapBatchResults = mapBatchResults == null ? List.of() : List.copyOf(mapBatchResults);
            this.reduceRequest = reduceRequest;
            this.reduceResult = reduceResult;
            this.totalSelected = totalSelected;
            this.totalBatches = totalBatches;
            this.batchFailures = batchFailures == null ? List.of() : List.copyOf(batchFailures);

            this.allSelectedChatIds = allSelectedChatIds == null ? List.of() : List.copyOf(allSelectedChatIds);
            this.missingChatIds = missingChatIds == null ? List.of() : List.copyOf(missingChatIds);
            this.coverageComplete = coverageComplete;
            this.coveragePassesUsed = Math.max(0, coveragePassesUsed);
        }

        OrchestrationResult withBatchFailures(List<BatchFailure> failures) {
            return new OrchestrationResult(
                    finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                    totalSelected, totalBatches, failures, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed
            );
        }

        OrchestrationResult withCoverage(
                List<String> allSelectedChatIds, List<String> missingChatIds, boolean coverageComplete, int coveragePassesUsed
        ) {
            return new OrchestrationResult(
                    finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                    totalSelected, totalBatches, batchFailures, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed
            );
        }

        public WorkspaceResponse finalResponse() {
            return finalResponse;
        }

        public List<String> mapOutputs() {
            return mapOutputs;
        }

        public List<Integer> failedBatchIndexes() {
            return failedBatchIndexes;
        }

        public List<MapBatchResult> mapBatchResults() {
            return mapBatchResults;
        }

        public ReduceRequest reduceRequest() {
            return reduceRequest;
        }

        public ReduceResult reduceResult() {
            return reduceResult;
        }

        public int totalSelected() {
            return totalSelected;
        }

        public int totalBatches() {
            return totalBatches;
        }

        public List<BatchFailure> batchFailures() {
            return batchFailures;
        }

        public List<String> allSelectedChatIds() {
            return allSelectedChatIds;
        }

        public List<String> missingChatIds() {
            return missingChatIds;
        }

        public boolean coverageComplete() {
            return coverageComplete;
        }

        public int coveragePassesUsed() {
            return coveragePassesUsed;
        }

        public boolean hasFailures() {
            return !failedBatchIndexes.isEmpty();
        }
    }
}
