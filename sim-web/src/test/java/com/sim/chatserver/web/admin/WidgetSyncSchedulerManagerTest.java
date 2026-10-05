package com.sim.chatserver.web.admin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WidgetSyncSchedulerManagerTest {

    private static final Logger LOG = Logger.getLogger(WidgetSyncSchedulerManagerTest.class.getName());

    @Test
    void createScheduler_returnsNull_whenNoManagedExecutorIsAvailable() {
        assertNull(WidgetSyncSchedulerManager.createScheduler(LOG));
    }

    @Test
    void ensureSchedulerRunning_returnsExistingExecutor_whenActive() {
        ScheduledExecutorService current = mock(ScheduledExecutorService.class);
        when(current.isShutdown()).thenReturn(false);
        when(current.isTerminated()).thenReturn(false);

        ScheduledExecutorService result = WidgetSyncSchedulerManager.ensureSchedulerRunning(current, LOG, "sync");

        assertSame(current, result);
    }

    @Test
    void ensureSchedulerRunning_replacesStoppedExecutor_withManagedLookupResult() {
        ScheduledExecutorService current = mock(ScheduledExecutorService.class);
        when(current.isShutdown()).thenReturn(true);

        ScheduledExecutorService result = WidgetSyncSchedulerManager.ensureSchedulerRunning(current, LOG, "sync");

        assertNull(result);
    }

    @Test
    void ensureSchedulerRunning_keepsContainerManagedExecutor_whenLifecycleCheckThrows() {
        ScheduledExecutorService managed = new ManagedScheduledExecutorServiceShim();

        ScheduledExecutorService result = WidgetSyncSchedulerManager.ensureSchedulerRunning(managed, LOG, "managed-sync");

        assertSame(managed, result);
    }

    @Test
    void shutdownExecutorQuietly_callsShutdownNow_forLocalExecutor() {
        ExecutorService executor = mock(ExecutorService.class);
        when(executor.isShutdown()).thenReturn(false);

        WidgetSyncSchedulerManager.shutdownExecutorQuietly(executor, LOG, "sync");

        verify(executor).shutdownNow();
    }

    @Test
    void shutdownExecutorQuietly_skipsShutdown_forContainerManagedExecutor() {
        ExecutorService managed = new ManagedScheduledExecutorServiceShim();

        assertDoesNotThrow(() -> WidgetSyncSchedulerManager.shutdownExecutorQuietly(managed, LOG, "managed-sync"));
    }

    @Test
    void shutdownExecutorQuietly_doesNothing_whenAlreadyShutdown() {
        ExecutorService executor = mock(ExecutorService.class);
        when(executor.isShutdown()).thenReturn(true);

        WidgetSyncSchedulerManager.shutdownExecutorQuietly(executor, LOG, "sync");

        verify(executor, never()).shutdownNow();
    }

    private static final class ManagedScheduledExecutorServiceShim extends AbstractExecutorService implements ScheduledExecutorService {

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            throw new UnsupportedOperationException("container-managed");
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            if (command != null) {
                command.run();
            }
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return new CompletedScheduledFuture<>(null);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            try {
                return new CompletedScheduledFuture<>(callable == null ? null : callable.call());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return new CompletedScheduledFuture<>(null);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return new CompletedScheduledFuture<>(null);
        }
    }

    private static final class CompletedScheduledFuture<V> implements RunnableScheduledFuture<V> {
        private final V value;

        private CompletedScheduledFuture(V value) {
            this.value = value;
        }

        @Override
        public boolean isPeriodic() {
            return false;
        }

        @Override
        public void run() {
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() {
            return value;
        }

        @Override
        public V get(long timeout, TimeUnit unit) {
            return value;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }
    }
}
