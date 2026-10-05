package com.sim.chatserver.web.admin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WidgetSyncExecutionCoordinatorTest {

    private static final Logger LOG = Logger.getLogger(WidgetSyncExecutionCoordinatorTest.class.getName());

    @Test
    void execute_requiresNonNullMode() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> WidgetSyncExecutionCoordinator.execute(
                        null,
                        null,
                        ignored -> List.of(),
                        ignored -> {},
                        (phase, msg, min) -> {},
                        ignored -> true,
                        () -> false,
                        () -> "",
                        () -> true,
                        () -> true,
                        () -> "",
                        LOG
                )
        );

        assertEquals("mode", ex.getMessage());
    }

    @Test
    void execute_manualMode_whenSummaryPaused_usesPausedMessage_andDefaultsBlankReason() {
        AtomicBoolean forcePersist = new AtomicBoolean(false);
        AtomicInteger summaryCalls = new AtomicInteger();

        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.MANUAL,
                "widget-1",
                ignored -> List.of("ok"),
                forcePersist::set,
                (phase, message, minimumPercent) -> {
                },
                manualTrigger -> {
                    summaryCalls.incrementAndGet();
                    return true;
                },
                () -> true,
                () -> "   ",
                () -> true,
                () -> true,
                () -> "next",
                LOG
        );

        assertTrue(forcePersist.get());
        assertEquals(0, summaryCalls.get());
        assertEquals(1, result.statuses().size());
        assertEquals("Sync completed. Summary generation is paused until an admin generates a summary.",
                result.completionMessage());
    }

    @Test
    void execute_manualMode_whenAutoDisabled_returnsDisabledMessage() {
        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.MANUAL,
                null,
                ignored -> List.of("ok"),
                ignored -> {
                },
                (phase, message, minimumPercent) -> {
                },
                manualTrigger -> true,
                () -> false,
                () -> "ignored",
                () -> false,
                () -> true,
                () -> "ignored",
                LOG
        );

        assertEquals("Sync completed. Automatic summary generation is disabled.", result.completionMessage());
    }

    @Test
    void execute_manualMode_whenNotDue_returnsIntervalMessage() {
        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.MANUAL,
                null,
                ignored -> List.of("ok"),
                ignored -> {
                },
                (phase, message, minimumPercent) -> {
                },
                manualTrigger -> true,
                () -> false,
                () -> "ignored",
                () -> true,
                () -> false,
                () -> "2026-10-01T10:00:00Z",
                LOG
        );

        assertEquals("Sync completed. Summary generation skipped until the configured interval elapses.",
                result.completionMessage());
    }

    @Test
    void execute_manualMode_whenDueAndSummaryFails_reportsFailure_andEmitsProgressUpdate() {
        AtomicReference<String> progressPhase = new AtomicReference<>();
        AtomicInteger progressMinPercent = new AtomicInteger(-1);

        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.MANUAL,
                null,
                ignored -> List.of("ok"),
                ignored -> {
                },
                (phase, message, minimumPercent) -> {
                    progressPhase.set(phase);
                    progressMinPercent.set(minimumPercent);
                },
                manualTrigger -> false,
                () -> false,
                () -> "ignored",
                () -> true,
                () -> true,
                () -> "ignored",
                LOG
        );

        assertEquals("summary_generation", progressPhase.get());
        assertEquals(92, progressMinPercent.get());
        assertEquals("Sync completed. Summary generation failed and automatic summaries are now paused.",
                result.completionMessage());
    }

    @Test
    void execute_scheduledMode_whenDueAndSummarySucceeds_returnsScheduledSuccess_andHandlesNullStatuses() {
        AtomicBoolean forcePersist = new AtomicBoolean(true);

        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.SCHEDULED,
                null,
                ignored -> null,
                forcePersist::set,
                (phase, message, minimumPercent) -> {
                },
                manualTrigger -> true,
                () -> false,
                () -> "ignored",
                () -> true,
                () -> true,
                () -> "ignored",
                LOG
        );

        assertTrue(result.statuses().isEmpty());
        assertEquals("Scheduled sync completed successfully.", result.completionMessage());
        assertEquals(false, forcePersist.get());
    }

    @Test
    void execute_scheduledMode_whenNotDue_returnsScheduledIntervalMessage() {
        WidgetSyncExecutionCoordinator.OrchestrationResult<String> result = WidgetSyncExecutionCoordinator.execute(
                WidgetSyncExecutionCoordinator.Mode.SCHEDULED,
                null,
                ignored -> List.of("ok"),
                ignored -> {
                },
                (phase, message, minimumPercent) -> {
                },
                manualTrigger -> true,
                () -> false,
                () -> "ignored",
                () -> true,
                () -> false,
                () -> "next-run",
                LOG
        );

        assertEquals("Scheduled sync completed. Summary generation skipped until the configured interval elapses.",
                result.completionMessage());
    }
}
