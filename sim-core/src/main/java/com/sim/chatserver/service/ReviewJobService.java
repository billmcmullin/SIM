// src/main/java/com/sim/chatserver/service/ReviewJobService.java
package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.review.ReviewJobStatus;

/**
 * In-memory async job tracker for review map-reduce execution.
 *
 * Coverage behavior: - progress derives from deterministic all/used/missing
 * sets - during MAP/REDUCE updates, missing IDs are preserved and evolved
 * across passes - completion success tied to coverageComplete + HTTP success -
 * in fixed-window batch mode, all IDs must be covered for success
 *
 * UX progress behavior: - supports step-level status details via message: "Map
 * round 1/3 - batch 2/8 running", "Reduce synthesis in progress", etc.
 */
public class ReviewJobService {

    private static final Logger log = Logger.getLogger(ReviewJobService.class.getCanonicalName());
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String STATUS_QUEUED = "Queued";
    private static final String STATUS_CANCELLED = "Cancelled";
    private static final String STATUS_FAILED = "Failed";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String ERR_NULL_RESULT = "Job completed with null result.";
    private static final String ERR_ASYNC_FAILED_DEFAULT = "Async job failed.";
    private static final String ERR_CANCELLED_BY_USER = "Job cancelled by user.";
    private static final String ERR_JOB_FAILED_DEFAULT = "Job failed.";
    private static final String MSG_REDUCE_IN_PROGRESS = "Reduce synthesis in progress...";
    private static final String MSG_MAP_ROUND_PREFIX = "Map round ";
    private static final String MSG_MAP_BATCH_SEPARATOR = " - batch ";
    private static final String MSG_MAP_RUNNING_SUFFIX = " running...";
    private static final String MSG_MAP_COMPLETED_SUFFIX = " completed";
    private static final String LOG_ASYNC_JOB_FAILED_PREFIX = "[review-job][";
    private static final String LOG_ASYNC_JOB_FAILED_SUFFIX = "] async job failed";
    private static final String LOG_CANCEL_FUTURE_FAILED = "Unable to cancel review job future cleanly";
    private static final String MIME_APPLICATION_JSON = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String EMPTY = "";

    @FunctionalInterface
    public interface JobTask {

        JobResult run(String jobId);
    }

    private final ExecutorService executor;
    private final Map<String, ReviewJobStatus> statuses = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> futures = new ConcurrentHashMap<>();

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private ReviewJobService() {
        this(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
    }

    private ReviewJobService(int poolSize) {
        this.executor = Executors.newFixedThreadPool(Math.max(1, poolSize));
    }

    public static ReviewJobService createDefault() {
        return new ReviewJobService();
    }

    static ReviewJobService createWithPoolSize(int poolSize) {
        return new ReviewJobService(poolSize);
    }

    private long nowEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    public String submit(String requestId, int totalSelected, JobTask task) {
        String jobId = UUID.randomUUID().toString();

        long now = nowEpochMillis();
        ReviewJobStatus queued = ReviewJobStatus.builder()
                .jobId(jobId)
                .requestId(requestId)
                .phase(ReviewJobStatus.Phase.QUEUED)
                .done(false)
                .success(false)
                .totalSelected(Math.max(0, totalSelected))
                .startedAtEpochMs(now)
                .updatedAtEpochMs(now)
                .message(STATUS_QUEUED)
                .allSelectedChatIds(List.of())
                .usedChatIds(List.of())
                .missingChatIds(List.of())
                .coveragePercent(0)
                .coverageComplete(false)
                .build();

        statuses.put(jobId, queued);

        Future<?> f = CompletableFuture.supplyAsync(() -> task.run(jobId), executor)
                .thenAccept(result -> {
                    if (result == null) {
                        failJob(jobId, 500, ERR_NULL_RESULT);
                        return;
                    }
                    completeJob(
                            jobId,
                            result.httpStatus(),
                            result.success(),
                            result.message(),
                            result.errorMessage(),
                            result.totalBatches(),
                            result.completedBatches(),
                            result.failedBatches(),
                            result.retries(),
                            result.allSelectedChatIds(),
                            result.usedChatIds(),
                            result.missingChatIds(),
                            result.failedBatchIndexes(),
                            result.warnings(),
                            result.finalReport(),
                            result.rawResponseBody(),
                            result.contentType()
                    );
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.log(Level.SEVERE, LOG_ASYNC_JOB_FAILED_PREFIX + jobId + LOG_ASYNC_JOB_FAILED_SUFFIX, cause);
                    failJob(jobId, 500, cause.getMessage() == null ? ERR_ASYNC_FAILED_DEFAULT : cause.getMessage());
                    return null;
                })
                .whenComplete((unused, ex) -> futures.remove(jobId));

        futures.put(jobId, f);
        return jobId;
    }

    public ReviewJobStatus getStatus(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return null;
        }
        return statuses.get(jobId);
    }

    public boolean exists(String jobId) {
        return jobId != null && statuses.containsKey(jobId);
    }

    public boolean cancel(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return false;
        }

        Future<?> f = futures.remove(jobId);
        boolean cancelled = false;
        if (f != null) {
            cancelled = f.cancel(true);
        }

        ReviewJobStatus old = statuses.get(jobId);
        if (old != null) {
            long now = nowEpochMillis();

            List<String> allIds = normalizeIds(old.getAllSelectedChatIds());
            List<String> usedIds = normalizeIds(old.getUsedChatIds());
            List<String> missingIds = computeMissing(allIds, usedIds, old.getMissingChatIds());

            ReviewJobStatus cancelledStatus = ReviewJobStatus.builder()
                    .jobId(old.getJobId())
                    .requestId(old.getRequestId())
                    .phase(ReviewJobStatus.Phase.CANCELLED)
                    .done(true)
                    .success(false)
                    .totalSelected(old.getTotalSelected())
                    .totalBatches(old.getTotalBatches())
                    .completedBatches(old.getCompletedBatches())
                    .failedBatches(old.getFailedBatches())
                    .retries(old.getRetries())
                    .allSelectedChatIds(allIds)
                    .usedChatIds(usedIds)
                    .missingChatIds(missingIds)
                    .coveragePercent(deriveCoveragePercent(allIds, usedIds, missingIds, old.getCoveragePercent()))
                    .coverageComplete(missingIds.isEmpty())
                    .startedAtEpochMs(old.getStartedAtEpochMs())
                    .updatedAtEpochMs(now)
                    .finishedAtEpochMs(now)
                    .httpStatus(499)
                    .message(STATUS_CANCELLED)
                    .errorMessage(ERR_CANCELLED_BY_USER)
                    .failedBatchIndexes(old.getFailedBatchIndexes())
                    .warnings(old.getWarnings())
                    .finalReport(old.getFinalReport())
                    .rawResponseBody(old.getRawResponseBody())
                    .contentType(old.getContentType())
                    .build();

            statuses.put(jobId, cancelledStatus);
        }

        futures.remove(jobId);
        return cancelled;
    }

    // ---------- Existing phase update APIs ----------
    final void updateMapProgress(
            String jobId,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            int coveragePercent,
            boolean coverageComplete,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            String message
    ) {
        updatePhase(
                jobId,
                ReviewJobStatus.Phase.MAP,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                List.of(),
                List.of(),
                missingChatIds,
                coveragePercent,
                coverageComplete,
                failedBatchIndexes,
                message,
                null
        );
    }

    public void updateMapProgress(
            String jobId,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            String message
    ) {
        updatePhase(
                jobId,
                ReviewJobStatus.Phase.MAP,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                allSelectedChatIds,
                usedChatIds,
                missingChatIds,
                -1,
                false,
                failedBatchIndexes,
                message,
                null
        );
    }

    final void updateReduceProgress(
            String jobId,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            int coveragePercent,
            boolean coverageComplete,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            String message
    ) {
        updatePhase(
                jobId,
                ReviewJobStatus.Phase.REDUCE,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                List.of(),
                List.of(),
                missingChatIds,
                coveragePercent,
                coverageComplete,
                failedBatchIndexes,
                message,
                null
        );
    }

    public void updateReduceProgress(
            String jobId,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            String message
    ) {
        updatePhase(
                jobId,
                ReviewJobStatus.Phase.REDUCE,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                allSelectedChatIds,
                usedChatIds,
                missingChatIds,
                -1,
                false,
                failedBatchIndexes,
                message,
                null
        );
    }

    // ---------- New convenience progress APIs (for orchestrator hooks) ----------
    final void updateMapBatchStarted(
            String jobId,
            int round,
            int maxRounds,
            int batchIndex,
            int totalBatches,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds
    ) {
        String msg = MSG_MAP_ROUND_PREFIX + round + '/' + Math.max(1, maxRounds)
            + MSG_MAP_BATCH_SEPARATOR + batchIndex + '/' + Math.max(1, totalBatches)
                + MSG_MAP_RUNNING_SUFFIX;
        updateMapProgress(
                jobId,
                totalBatches,
                Math.max(0, batchIndex - 1),
                0,
                0,
                allSelectedChatIds,
                usedChatIds,
                missingChatIds,
                List.of(),
                msg
        );
    }

    final void updateMapBatchCompleted(
            String jobId,
            int round,
            int maxRounds,
            int batchIndex,
            int totalBatches,
            int failedBatches,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds
    ) {
        String msg = MSG_MAP_ROUND_PREFIX + round + '/' + Math.max(1, maxRounds)
            + MSG_MAP_BATCH_SEPARATOR + batchIndex + '/' + Math.max(1, totalBatches)
            + MSG_MAP_COMPLETED_SUFFIX;
        updateMapProgress(
                jobId,
                totalBatches,
                Math.max(0, batchIndex),
                Math.max(0, failedBatches),
                0,
                allSelectedChatIds,
                usedChatIds,
                missingChatIds,
                List.of(),
                msg
        );
    }

    final void updateReduceStarted(
            String jobId,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds
    ) {
        updateReduceProgress(
                jobId,
                totalBatches,
                completedBatches,
                failedBatches,
                0,
                allSelectedChatIds,
                usedChatIds,
                missingChatIds,
                List.of(),
                MSG_REDUCE_IN_PROGRESS
        );
    }

    private void failJob(String jobId, int httpStatus, String errorMessage) {
        ReviewJobStatus old = statuses.get(jobId);
        if (old == null) {
            return;
        }

        long now = nowEpochMillis();

        List<String> allIds = normalizeIds(old.getAllSelectedChatIds());
        List<String> usedIds = normalizeIds(old.getUsedChatIds());
        List<String> missingIds = computeMissing(allIds, usedIds, old.getMissingChatIds());

        int cov = deriveCoveragePercent(allIds, usedIds, missingIds, old.getCoveragePercent());
        boolean complete = missingIds.isEmpty();

        ReviewJobStatus failed = ReviewJobStatus.builder()
                .jobId(old.getJobId())
                .requestId(old.getRequestId())
                .phase(ReviewJobStatus.Phase.FAILED)
                .done(true)
                .success(false)
                .totalSelected(old.getTotalSelected())
                .totalBatches(old.getTotalBatches())
                .completedBatches(old.getCompletedBatches())
                .failedBatches(Math.max(old.getFailedBatches(), 1))
                .retries(old.getRetries())
                .allSelectedChatIds(allIds)
                .usedChatIds(usedIds)
                .missingChatIds(missingIds)
                .coveragePercent(cov)
                .coverageComplete(complete)
                .startedAtEpochMs(old.getStartedAtEpochMs())
                .updatedAtEpochMs(now)
                .finishedAtEpochMs(now)
                .httpStatus(httpStatus)
                .message(STATUS_FAILED)
                .errorMessage(errorMessage == null ? ERR_JOB_FAILED_DEFAULT : errorMessage)
                .failedBatchIndexes(old.getFailedBatchIndexes())
                .warnings(old.getWarnings())
                .finalReport(old.getFinalReport())
                .rawResponseBody(old.getRawResponseBody())
                .contentType(old.getContentType())
                .build();

        statuses.put(jobId, failed);
    }

    private void completeJob(
            String jobId,
            int httpStatus,
            boolean success,
            String message,
            String errorMessage,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            int coveragePercent,
            boolean coverageComplete,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            List<String> warnings,
            String finalReport,
            String rawResponseBody,
            String contentType
    ) {
        completeJob(
                jobId,
                httpStatus,
                success,
                message,
                errorMessage,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                List.of(),
                List.of(),
                missingChatIds,
                failedBatchIndexes,
                warnings,
                finalReport,
                rawResponseBody,
                contentType
        );
    }

    private void completeJob(
            String jobId,
            int httpStatus,
            boolean success,
            String message,
            String errorMessage,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            List<String> warnings,
            String finalReport,
            String rawResponseBody,
            String contentType
    ) {
        ReviewJobStatus old = statuses.get(jobId);
        if (old == null) {
            return;
        }

        long now = nowEpochMillis();

        List<String> allIds = !allSelectedChatIds.isEmpty() ? normalizeIds(allSelectedChatIds) : normalizeIds(old.getAllSelectedChatIds());
        List<String> usedIds = !usedChatIds.isEmpty() ? normalizeIds(usedChatIds) : normalizeIds(old.getUsedChatIds());
        List<String> missingIds = computeMissing(allIds, usedIds, missingChatIds);

        int cov = deriveCoveragePercent(allIds, usedIds, missingIds, old.getCoveragePercent());
        boolean complete = missingIds.isEmpty();

        boolean finalSuccess = success && httpStatus < 400 && complete;

        ReviewJobStatus completed = ReviewJobStatus.builder()
                .jobId(old.getJobId())
                .requestId(old.getRequestId())
                .phase(finalSuccess ? ReviewJobStatus.Phase.COMPLETED : ReviewJobStatus.Phase.FAILED)
                .done(true)
                .success(finalSuccess)
                .totalSelected(old.getTotalSelected())
                .totalBatches(totalBatches)
                .completedBatches(completedBatches)
                .failedBatches(failedBatches)
                .retries(retries)
                .allSelectedChatIds(allIds)
                .usedChatIds(usedIds)
                .missingChatIds(missingIds)
                .coveragePercent(cov)
                .coverageComplete(complete)
                .startedAtEpochMs(old.getStartedAtEpochMs())
                .updatedAtEpochMs(now)
                .finishedAtEpochMs(now)
                .httpStatus(httpStatus)
                .message(message == null ? (finalSuccess ? STATUS_COMPLETED : STATUS_FAILED) : message)
                .errorMessage(errorMessage == null ? EMPTY : errorMessage)
                .failedBatchIndexes(failedBatchIndexes == null ? List.of() : failedBatchIndexes)
                .warnings(warnings == null ? List.of() : warnings)
                .finalReport(finalReport == null ? EMPTY : finalReport)
                .rawResponseBody(rawResponseBody == null ? EMPTY : rawResponseBody)
                .contentType(contentType == null || contentType.isBlank() ? MIME_APPLICATION_JSON : contentType)
                .build();

        statuses.put(jobId, completed);
    }

    final int cleanupFinishedBefore(long cutoffEpochMs) {
        int removed = 0;
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ReviewJobStatus> e : statuses.entrySet()) {
            ReviewJobStatus s = e.getValue();
            if (s == null || !s.isDone()) {
                continue;
            }
            if (s.getFinishedAtEpochMs() > 0 && s.getFinishedAtEpochMs() < cutoffEpochMs) {
                toRemove.add(e.getKey());
            }
        }

        for (String jobId : toRemove) {
            statuses.remove(jobId);
            futures.remove(jobId);
            removed++;
        }
        return removed;
    }

    final int size() {
        return statuses.size();
    }

    final void shutdownNow() {
        for (Future<?> f : futures.values()) {
            try {
                f.cancel(true);
            } catch (IllegalStateException ex) {
                log.log(Level.FINE, LOG_CANCEL_FUTURE_FAILED, ex);
            }
        }
        futures.clear();
        executor.shutdownNow();
    }

    private void updatePhase(
            String jobId,
            ReviewJobStatus.Phase phase,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds,
            int coveragePercent,
            boolean coverageComplete,
            List<Integer> failedBatchIndexes,
            String message,
            List<String> warnings
    ) {
        ReviewJobStatus old = statuses.get(jobId);
        if (old == null || old.isDone()) {
            return;
        }

        List<String> allIds = !allSelectedChatIds.isEmpty() ? normalizeIds(allSelectedChatIds) : normalizeIds(old.getAllSelectedChatIds());
        List<String> usedIds = mergeUsedIds(old.getUsedChatIds(), usedChatIds);

        List<String> missingIds = computeMissing(
                allIds,
                usedIds,
                missingChatIds == null ? old.getMissingChatIds() : missingChatIds
        );

        int cov = coveragePercent >= 0
                ? Math.max(0, Math.min(100, coveragePercent))
                : deriveCoveragePercent(allIds, usedIds, missingIds, old.getCoveragePercent());

        boolean complete = coverageComplete || missingIds.isEmpty();

        ReviewJobStatus updated = ReviewJobStatus.builder()
                .jobId(old.getJobId())
                .requestId(old.getRequestId())
                .phase(phase)
                .done(false)
                .success(false)
                .totalSelected(old.getTotalSelected())
                .totalBatches(totalBatches)
                .completedBatches(completedBatches)
                .failedBatches(failedBatches)
                .retries(retries)
                .allSelectedChatIds(allIds)
                .usedChatIds(usedIds)
                .missingChatIds(missingIds)
                .coveragePercent(cov)
                .coverageComplete(complete)
                .startedAtEpochMs(old.getStartedAtEpochMs())
                .updatedAtEpochMs(nowEpochMillis())
                .finishedAtEpochMs(0)
                .httpStatus(0)
                .message(message == null ? phase.name() : message)
                .errorMessage(EMPTY)
                .failedBatchIndexes(failedBatchIndexes == null ? List.of() : failedBatchIndexes)
                .warnings(warnings == null ? old.getWarnings() : warnings)
                .finalReport(old.getFinalReport())
                .rawResponseBody(old.getRawResponseBody())
                .contentType(old.getContentType())
                .build();

        statuses.put(jobId, updated);
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                out.add(id.trim().toLowerCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(out);
    }

    private List<String> mergeUsedIds(List<String> existingUsed, List<String> incomingUsed) {
        Set<String> out = new LinkedHashSet<>();
        out.addAll(normalizeIds(existingUsed));
        out.addAll(normalizeIds(incomingUsed));
        return new ArrayList<>(out);
    }

    /**
     * STRICT missing computation: - If all is known, missing is always computed
     * as (all - used). - providedMissing is only used when all is unknown.
     */
    private List<String> computeMissing(List<String> all, List<String> used, List<String> providedMissing) {
        List<String> allNorm = normalizeIds(all);
        List<String> usedNorm = normalizeIds(used);
        List<String> missingNorm = normalizeIds(providedMissing);

        if (!allNorm.isEmpty()) {
            Set<String> computed = new LinkedHashSet<>(allNorm);
            Set<String> usedSet = new LinkedHashSet<>(usedNorm);
            for (String usedId : usedSet) {
                computed.remove(usedId);
            }
            return new ArrayList<>(computed);
        }

        return missingNorm;
    }

    private int deriveCoveragePercent(List<String> all, List<String> used, List<String> missing, int fallback) {
        int total = all == null ? 0 : all.size();
        if (total <= 0) {
            return Math.max(0, Math.min(100, fallback));
        }

        int usedCount = (used == null ? 0 : used.size());
        if (missing != null && !missing.isEmpty()) {
            usedCount = Math.max(0, total - missing.size());
        }

        int p = Math.toIntExact(Math.round((usedCount * 100.0) / total));
        return Math.max(0, Math.min(100, p));
    }

}


