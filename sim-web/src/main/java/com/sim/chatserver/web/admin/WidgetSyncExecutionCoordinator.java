package com.sim.chatserver.web.admin;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class WidgetSyncExecutionCoordinator {

    private static final String ARG_MODE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MODE;
    private static final String ARG_SYNC_RUNNER = "syncRunner";
    private static final String ARG_LAST_SYNCED_UPDATER = "lastSyncedUpdater";
    private static final String ARG_PROGRESS_UPDATER = "progressUpdater";
    private static final String ARG_SUMMARY_RUNNER = "summaryRunner";
    private static final String ARG_SUMMARY_PAUSED_SUPPLIER = "summaryPausedSupplier";
    private static final String ARG_SUMMARY_PAUSED_REASON_SUPPLIER = "summaryPausedReasonSupplier";
    private static final String ARG_SUMMARY_AUTO_ENABLED_SUPPLIER = "summaryAutoEnabledSupplier";
    private static final String ARG_SUMMARY_DUE_SUPPLIER = "summaryDueSupplier";
    private static final String ARG_NEXT_SUMMARY_RUN_AT_SUPPLIER = "nextSummaryRunAtSupplier";
    private static final String ARG_LOG = "log";

    private static final String DEFAULT_MANUAL_SUMMARY_REQUIRED = "manual summary generation required";
    private static final String LOG_MANUAL_PAUSED = "Skipping summary generation during manual sync because automatic summaries are paused. reason={0}";
    private static final String LOG_SCHEDULED_PAUSED = "Skipping scheduled summary generation while paused. reason={0}";
    private static final String LOG_MANUAL_AUTO_DISABLED = "Skipping summary generation during manual sync because automatic summary generation is disabled.";
    private static final String LOG_SCHEDULED_AUTO_DISABLED = "Skipping scheduled summary generation because automatic summary is disabled.";
    private static final String LOG_MANUAL_INTERVAL_NOT_ELAPSED = "Skipping summary generation during manual sync because summary interval has not elapsed. nextRunAt={0}";
    private static final String LOG_SCHEDULED_INTERVAL_NOT_ELAPSED = "Skipping scheduled summary generation because summary interval has not elapsed. nextRunAt={0}";

    private static final String PHASE_SUMMARY_GENERATION = "summary_generation";
    private static final String MSG_GENERATING_DAILY_SUMMARY = "Generating daily summary...";

    private static final String MSG_SYNC_PAUSED = "Sync completed. Summary generation is paused until an admin generates a summary.";
    private static final String MSG_SYNC_AUTO_DISABLED = "Sync completed. Automatic summary generation is disabled.";
    private static final String MSG_SYNC_INTERVAL_SKIP = "Sync completed. Summary generation skipped until the configured interval elapses.";
    private static final String MSG_SYNC_SUMMARY_FAILED = "Sync completed. Summary generation failed and automatic summaries are now paused.";
    private static final String MSG_SYNC_SUCCESS = "Sync completed successfully.";
    private static final String MSG_SCHEDULED_PAUSED = "Scheduled sync completed. Summary generation remains paused.";
    private static final String MSG_SCHEDULED_AUTO_DISABLED = "Scheduled sync completed. Automatic summary generation is disabled.";
    private static final String MSG_SCHEDULED_INTERVAL_SKIP = "Scheduled sync completed. Summary generation skipped until the configured interval elapses.";
    private static final String MSG_SCHEDULED_SUMMARY_FAILED = "Scheduled sync completed. Summary generation failed and was paused.";
    private static final String MSG_SCHEDULED_SUCCESS = "Scheduled sync completed successfully.";

    enum Mode {
        MANUAL,
        SCHEDULED
    }

    @FunctionalInterface
    interface SyncRunner<T> {
        List<T> run(String requestedWidgetId);
    }

    @FunctionalInterface
    interface LastSyncedUpdater {
        void update(boolean forcePersist);
    }

    @FunctionalInterface
    interface ProgressUpdater {
        void update(String phase, String message, int minimumPercent);
    }

    @FunctionalInterface
    interface SummaryRunner {
        boolean run(boolean manualTrigger);
    }

    private WidgetSyncExecutionCoordinator() {
    }

    static <T> OrchestrationResult<T> execute(
            Mode mode,
            String requestedWidgetId,
            SyncRunner<T> syncRunner,
            LastSyncedUpdater lastSyncedUpdater,
            ProgressUpdater progressUpdater,
            SummaryRunner summaryRunner,
            BooleanSupplier summaryPausedSupplier,
            Supplier<String> summaryPausedReasonSupplier,
            BooleanSupplier summaryAutoEnabledSupplier,
            BooleanSupplier summaryDueSupplier,
            Supplier<String> nextSummaryRunAtSupplier,
            Logger log
    ) {
        mode = Objects.requireNonNull(mode, ARG_MODE);
        syncRunner = Objects.requireNonNull(syncRunner, ARG_SYNC_RUNNER);
        lastSyncedUpdater = Objects.requireNonNull(lastSyncedUpdater, ARG_LAST_SYNCED_UPDATER);
        progressUpdater = Objects.requireNonNull(progressUpdater, ARG_PROGRESS_UPDATER);
        summaryRunner = Objects.requireNonNull(summaryRunner, ARG_SUMMARY_RUNNER);
        summaryPausedSupplier = Objects.requireNonNull(summaryPausedSupplier, ARG_SUMMARY_PAUSED_SUPPLIER);
        summaryPausedReasonSupplier = Objects.requireNonNull(summaryPausedReasonSupplier, ARG_SUMMARY_PAUSED_REASON_SUPPLIER);
        summaryAutoEnabledSupplier = Objects.requireNonNull(summaryAutoEnabledSupplier, ARG_SUMMARY_AUTO_ENABLED_SUPPLIER);
        summaryDueSupplier = Objects.requireNonNull(summaryDueSupplier, ARG_SUMMARY_DUE_SUPPLIER);
        nextSummaryRunAtSupplier = Objects.requireNonNull(nextSummaryRunAtSupplier, ARG_NEXT_SUMMARY_RUN_AT_SUPPLIER);
        log = Objects.requireNonNull(log, ARG_LOG);

        List<T> statuses = syncRunner.run(requestedWidgetId);
        if (statuses == null) {
            statuses = List.of();
        }

        lastSyncedUpdater.update(mode == Mode.MANUAL);

        boolean summaryRan = false;
        boolean summarySuccess = true;

        if (summaryPausedSupplier.getAsBoolean()) {
            String reason = defaultIfBlank(summaryPausedReasonSupplier.get(), DEFAULT_MANUAL_SUMMARY_REQUIRED);
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        LOG_MANUAL_PAUSED,
                        reason);
            } else {
                log.log(Level.INFO,
                        LOG_SCHEDULED_PAUSED,
                        reason);
            }
        } else if (!summaryAutoEnabledSupplier.getAsBoolean()) {
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        LOG_MANUAL_AUTO_DISABLED);
            } else {
                log.log(Level.INFO,
                        LOG_SCHEDULED_AUTO_DISABLED);
            }
        } else if (!summaryDueSupplier.getAsBoolean()) {
            String nextRunAt = nextSummaryRunAtSupplier.get();
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        LOG_MANUAL_INTERVAL_NOT_ELAPSED,
                        nextRunAt);
            } else {
                log.log(Level.INFO,
                        LOG_SCHEDULED_INTERVAL_NOT_ELAPSED,
                        nextRunAt);
            }
        } else {
            summaryRan = true;
            progressUpdater.update(PHASE_SUMMARY_GENERATION, MSG_GENERATING_DAILY_SUMMARY, 92);
            summarySuccess = summaryRunner.run(false);
        }

        String completionMessage = resolveCompletionMessage(
                mode,
                summaryRan,
                summarySuccess,
                summaryPausedSupplier.getAsBoolean(),
                summaryAutoEnabledSupplier.getAsBoolean()
        );
        return new OrchestrationResult<>(statuses, completionMessage);
    }

    private static String resolveCompletionMessage(
            Mode mode,
            boolean summaryRan,
            boolean summarySuccess,
            boolean summaryPaused,
            boolean summaryAutoEnabled
    ) {
        if (mode == Mode.MANUAL) {
            if (!summaryRan) {
                return summaryPaused
                        ? MSG_SYNC_PAUSED
                        : (!summaryAutoEnabled
                        ? MSG_SYNC_AUTO_DISABLED
                        : MSG_SYNC_INTERVAL_SKIP);
            }
            if (!summarySuccess) {
                return MSG_SYNC_SUMMARY_FAILED;
            }
            return MSG_SYNC_SUCCESS;
        }

        if (!summaryRan) {
            return summaryPaused
                    ? MSG_SCHEDULED_PAUSED
                    : (!summaryAutoEnabled
                    ? MSG_SCHEDULED_AUTO_DISABLED
                    : MSG_SCHEDULED_INTERVAL_SKIP);
        }
        if (!summarySuccess) {
            return MSG_SCHEDULED_SUMMARY_FAILED;
        }
        return MSG_SCHEDULED_SUCCESS;
    }

    private static String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }


}

final class OrchestrationResult<T> {
    private final List<T> statuses;
    private final String completionMessage;

    OrchestrationResult(List<T> statuses, String completionMessage) {
        this.statuses = statuses;
        this.completionMessage = completionMessage;
    }

    List<T> statuses() {
        return statuses;
    }

    String completionMessage() {
        return completionMessage;
    }
}

