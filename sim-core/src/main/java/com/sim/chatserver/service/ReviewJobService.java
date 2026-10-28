// src/main/java/com/sim/chatserver/service/ReviewJobService.java
package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
public class ReviewJobService implements AutoCloseable {

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
    private static final String OWNER_RECORD_KIND = "review-job-owner";
    private static final String HEARTBEAT_THREAD_NAME = "review-job-heartbeats";
    private static final String ERR_PERSISTENCE_INITIALIZATION = "Persistence must initialize before accepting review jobs";
    private static final String ERR_OWNER_REQUIRED = "An authenticated job owner is required";
    private static final String LOG_STORAGE_UNAVAILABLE = "Review job stopped because durable storage is unavailable";
    private static final String LOG_OWNER_CLEANUP_FAILED = "Unable to remove review job owner after submission failed";
    private static final String LOG_SHUTDOWN_INTERRUPTED = "Review job shutdown interrupted";
    private static final String LOG_COMPLETION_INTERRUPTED = "Interrupted while inspecting completed review job";

    @FunctionalInterface
    public interface JobTask {

        JobResult run(String jobId);
    }

    private final ExecutorService executor;
    private Map<String, ReviewJobStatus> statuses = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> futures = new ConcurrentHashMap<>();
    private final Map<String, String> owners = new ConcurrentHashMap<>();
    private WorkflowRecordStore persistence;
    private ScheduledExecutorService heartbeatExecutor;

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
        int workers = Math.max(1, poolSize);
        this.executor = new ThreadPoolExecutor(workers, workers, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.AbortPolicy());
    }

    public void enablePersistence(WorkflowRecordStore store) {
        if (!futures.isEmpty() || !statuses.isEmpty()) {
            throw new IllegalStateException(ERR_PERSISTENCE_INITIALIZATION);
        }
        JdbcReviewJobMap persistent = new JdbcReviewJobMap(store);
        statuses = persistent;
        persistence = store;
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, HEARTBEAT_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
        heartbeatExecutor.scheduleWithFixedDelay(() -> {
            for (var entry : futures.entrySet()) {
                try {
                    ReviewJobStatus status = persistent.get(entry.getKey());
                    if (status == null || status.isDone()) {
                        entry.getValue().cancel(true);
                        futures.remove(entry.getKey(), entry.getValue());
                    } else if (!entry.getValue().isDone()) {
                        persistent.heartbeat(entry.getKey());
                    }
                } catch (IllegalStateException ex) {
                    entry.getValue().cancel(true);
                    log.log(Level.WARNING, LOG_STORAGE_UNAVAILABLE, ex);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
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

    final String submit(String requestId, int totalSelected, JobTask task) {
        return submitInternal(null, requestId, totalSelected, task);
    }

    public String submitForUser(String owner, String requestId, int totalSelected, JobTask task) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException(ERR_OWNER_REQUIRED);
        }
        return submitInternal(owner, requestId, totalSelected, task);
    }

    public ReviewJobStatus getStatusForUser(String jobId, String owner) {
        return isOwnedBy(jobId, owner) ? getStatus(jobId) : null;
    }

    public boolean cancelForUser(String jobId, String owner) {
        return isOwnedBy(jobId, owner) && cancel(jobId);
    }

    private boolean isOwnedBy(String jobId, String owner) {
        if (jobId == null || jobId.isBlank() || owner == null || owner.isBlank()) {
            return false;
        }
        if (persistence != null) {
            var record = persistence.find(OWNER_RECORD_KIND, jobId);
            return record != null && owner.equals(record.payload());
        }
        return owner.equals(owners.get(jobId));
    }

    private void removeOwner(String jobId) {
        owners.remove(jobId);
        if (persistence != null) {
            persistence.delete(OWNER_RECORD_KIND, jobId);
        }
    }

    private String submitInternal(String owner, String requestId, int totalSelected, JobTask task) {
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

        if (owner != null) {
            if (persistence != null) {
                persistence.insert(OWNER_RECORD_KIND, jobId, owner, 7L * 24 * 60 * 60 * 1000);
            } else {
                owners.put(jobId, owner);
            }
        }
        boolean statusStored = false;
        try {
            statuses.put(jobId, queued);
            statusStored = true;
        } finally {
            if (!statusStored) {
                try {
                    removeOwner(jobId);
                } catch (IllegalStateException cleanupFailure) {
                    log.log(Level.WARNING, LOG_OWNER_CLEANUP_FAILED, cleanupFailure);
                }
            }
        }

        FutureTask<Void> future = new FutureTask<>(() -> {
                    JobResult result = task.run(jobId);
                    if (result == null) {
                        failJob(jobId, 500, ERR_NULL_RESULT);
                        return null;
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
                return null;
        }) {
            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        get();
                    }
                } catch (ExecutionException ex) {
                    log.log(Level.SEVERE, LOG_ASYNC_JOB_FAILED_PREFIX + jobId + LOG_ASYNC_JOB_FAILED_SUFFIX, ex);
                    Throwable failure = ex.getCause();
                    if (failure instanceof RuntimeException) {
                        failJob(jobId, 500, failure.getMessage() == null ? ERR_ASYNC_FAILED_DEFAULT : failure.getMessage());
                    }
                } catch (InterruptedException ex) {
                    log.log(Level.FINE, LOG_COMPLETION_INTERRUPTED, ex);
                    Thread.currentThread().interrupt();
                } finally {
                    futures.remove(jobId);
                }
            }
        };
        futures.put(jobId, future);
        try {
            executor.execute(future);
        } catch (RejectedExecutionException ex) {
            futures.remove(jobId);
            statuses.remove(jobId);
            removeOwner(jobId);
            throw ex;
        }
        return jobId;
    }

    public ReviewJobStatus getStatus(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return null;
        }
        return statuses.get(jobId);
    }

    final boolean exists(String jobId) {
        return jobId != null && statuses.containsKey(jobId);
    }

    public boolean cancel(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return false;
        }

        while (true) {
        ReviewJobStatus old = statuses.get(jobId);
        if (old == null || old.isDone()) {
            return false;
        }
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

            if (statuses.replace(jobId, old, cancelledStatus)) {
                Future<?> future = futures.remove(jobId);
                if (future != null) {
                    future.cancel(true);
                }
                return true;
            }
        }
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
        if (old == null || old.isDone()) {
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

        statuses.replace(jobId, old, failed);
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
        if (old == null || old.isDone()) {
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

        statuses.replace(jobId, old, completed);
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
            removeOwner(jobId);
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
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                shutdownNow();
            }
        } catch (InterruptedException ex) {
            log.log(Level.FINE, LOG_SHUTDOWN_INTERRUPTED, ex);
            shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdownNow();
            }
        }
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

        statuses.replace(jobId, old, updated);
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


