package com.sim.chatserver.web.admin;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class WidgetSyncExecutionCoordinator {

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

    static final class OrchestrationResult<T> {
        private final List<T> statuses;
        private final String completionMessage;

        private OrchestrationResult(List<T> statuses, String completionMessage) {
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
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(syncRunner, "syncRunner");
        Objects.requireNonNull(lastSyncedUpdater, "lastSyncedUpdater");
        Objects.requireNonNull(progressUpdater, "progressUpdater");
        Objects.requireNonNull(summaryRunner, "summaryRunner");
        Objects.requireNonNull(summaryPausedSupplier, "summaryPausedSupplier");
        Objects.requireNonNull(summaryPausedReasonSupplier, "summaryPausedReasonSupplier");
        Objects.requireNonNull(summaryAutoEnabledSupplier, "summaryAutoEnabledSupplier");
        Objects.requireNonNull(summaryDueSupplier, "summaryDueSupplier");
        Objects.requireNonNull(nextSummaryRunAtSupplier, "nextSummaryRunAtSupplier");
        Objects.requireNonNull(log, "log");

        List<T> statuses = syncRunner.run(requestedWidgetId);
        if (statuses == null) {
            statuses = List.of();
        }

        lastSyncedUpdater.update(mode == Mode.MANUAL);

        boolean summaryRan = false;
        boolean summarySuccess = true;

        if (summaryPausedSupplier.getAsBoolean()) {
            String reason = defaultIfBlank(summaryPausedReasonSupplier.get(), "manual summary generation required");
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because automatic summaries are paused. reason={0}",
                        reason);
            } else {
                log.log(Level.INFO,
                        "Skipping scheduled summary generation while paused. reason={0}",
                        reason);
            }
        } else if (!summaryAutoEnabledSupplier.getAsBoolean()) {
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because automatic summary generation is disabled.");
            } else {
                log.log(Level.INFO,
                        "Skipping scheduled summary generation because automatic summary is disabled.");
            }
        } else if (!summaryDueSupplier.getAsBoolean()) {
            String nextRunAt = nextSummaryRunAtSupplier.get();
            if (mode == Mode.MANUAL) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because summary interval has not elapsed. nextRunAt={0}",
                        nextRunAt);
            } else {
                log.log(Level.INFO,
                        "Skipping scheduled summary generation because summary interval has not elapsed. nextRunAt={0}",
                        nextRunAt);
            }
        } else {
            summaryRan = true;
            progressUpdater.update("summary_generation", "Generating daily summary...", 92);
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
                        ? "Sync completed. Summary generation is paused until an admin generates a summary."
                        : (!summaryAutoEnabled
                        ? "Sync completed. Automatic summary generation is disabled."
                        : "Sync completed. Summary generation skipped until the configured interval elapses.");
            }
            if (!summarySuccess) {
                return "Sync completed. Summary generation failed and automatic summaries are now paused.";
            }
            return "Sync completed successfully.";
        }

        if (!summaryRan) {
            return summaryPaused
                    ? "Scheduled sync completed. Summary generation remains paused."
                    : (!summaryAutoEnabled
                    ? "Scheduled sync completed. Automatic summary generation is disabled."
                    : "Scheduled sync completed. Summary generation skipped until the configured interval elapses.");
        }
        if (!summarySuccess) {
            return "Scheduled sync completed. Summary generation failed and was paused.";
        }
        return "Scheduled sync completed successfully.";
    }

    private static String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
