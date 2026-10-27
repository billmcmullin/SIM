package com.sim.chatserver.service;

import javax.sql.DataSource;
import java.util.function.Supplier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorkflowRuntime {
    private static final Logger LOG = Logger.getLogger(WorkflowRuntime.class.getCanonicalName());
    private static final String RETENTION_THREAD_NAME = "workflow-retention";
    private static final String MSG_CLEANUP_FAILED = "Workflow retention cleanup failed";
    private static final String MSG_NOT_INITIALIZED = "Durable workflow runtime has not initialized";
    private static volatile WorkflowRecordStore records;
    private static ScheduledExecutorService cleanup;

    private WorkflowRuntime() { }

    public static void initialize(DataSource dataSource) {
        WorkflowSchema.migrate(dataSource);
        records = new WorkflowRecordStore(() -> dataSource);
        records.purgeExpired();
        if (cleanup != null) {
            cleanup.shutdownNow();
        }
        cleanup = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, RETENTION_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
        cleanup.scheduleWithFixedDelay(() -> {
            try {
                records().purgeExpired();
            } catch (IllegalStateException ex) {
                LOG.log(Level.WARNING, MSG_CLEANUP_FAILED, ex);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    public static boolean isInitialized() {
        return records != null;
    }

    public static void shutdown() {
        if (cleanup != null) {
            cleanup.shutdownNow();
        }
        records = null;
    }

    public static DatabaseTaskLock tryTaskLock(Supplier<DataSource> dataSource, String task) {
        return isInitialized() ? DatabaseTaskLock.tryAcquire(dataSource.get(), task) : DatabaseTaskLock.localOnly();
    }

    public static WorkflowRecordStore records() {
        WorkflowRecordStore store = records;
        if (store == null) {
            throw new IllegalStateException(MSG_NOT_INITIALIZED);
        }
        return store;
    }
}