package com.sim.chatserver.web.admin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

final class WidgetSyncSchedulerManager {

    private static final String MANAGED_SCHEDULER_JNDI = "java:comp/DefaultManagedScheduledExecutorService";
    private static final String LOG_MANAGED_SCHEDULER_UNAVAILABLE =
        "Managed scheduled executor is unavailable; automatic widget sync scheduling is disabled.";
    private static final String LOG_SKIP_LIFECYCLE_CHECK =
        "Skipping lifecycle check for container-managed executor {0}";
    private static final String LOG_EXECUTOR_LIFECYCLE_CHECK_FAILED_PREFIX = "Executor lifecycle check failed for ";
    private static final String LOG_RECREATE_LOCAL_EXECUTOR_SUFFIX = "; recreating local executor";
    private static final String TYPE_MANAGED_EXECUTOR_SERVICE = "ManagedExecutorService";
    private static final String TYPE_MANAGED_SCHEDULED_EXECUTOR_SERVICE = "ManagedScheduledExecutorService";
    private static final String TYPE_JBOSS_MANAGED_EXECUTOR_ADAPTER = "jboss.as.ee.concurrent.adapter";
    private static final String LOG_SKIP_SHUTDOWN = "Skipping shutdown for container-managed executor {0}";
    private static final String LOG_EXECUTOR_SHUTDOWN_FAILED_PREFIX = "Executor shutdown failed for ";
    private static final String LOG_JNDI_RESOURCE_WRONG_TYPE = "JNDI resource {0} is not a {1}";
    private static final String LOG_LOOKUP_MANAGED_EXECUTOR_FAILED = "Unable to lookup managed executor {0}";
    private static final String LOG_MANAGED_EXECUTOR_LOOKUP_FAILURE_DETAILS = "Managed executor lookup failure details";
    private static final String LOG_CLOSE_INITIAL_CONTEXT_FAILED = "Failed to close InitialContext";

    private WidgetSyncSchedulerManager() {
    }

    static ScheduledExecutorService createScheduler(Logger log) {
        return lookupManagedScheduledExecutor(log);
    }

    static ScheduledExecutorService ensureSchedulerRunning(
            ScheduledExecutorService current,
            Logger log,
            String executorName
    ) {
        if (current == null || isExecutorStopped(current, log, executorName)) {
            ScheduledExecutorService replacement = createScheduler(log);
            if (replacement == null) {
                log.warning(LOG_MANAGED_SCHEDULER_UNAVAILABLE);
            }
            return replacement;
        }
        return current;
    }

    private static boolean isExecutorStopped(ExecutorService executor, Logger log, String executorName) {
        if (executor == null) {
            return true;
        }
        try {
            return executor.isShutdown() || executor.isTerminated();
        } catch (IllegalStateException | UnsupportedOperationException | SecurityException ex) {
            if (isContainerManagedExecutor(executor)) {
                log.log(Level.FINE, LOG_SKIP_LIFECYCLE_CHECK, executorName);
                return false;
            }
            log.log(Level.WARNING,
                    LOG_EXECUTOR_LIFECYCLE_CHECK_FAILED_PREFIX + executorName + LOG_RECREATE_LOCAL_EXECUTOR_SUFFIX,
                    ex);
            return true;
        }
    }

    private static boolean isContainerManagedExecutor(ExecutorService executor) {
        if (executor == null) {
            return false;
        }
        String typeName = executor.getClass().getCanonicalName();
        return typeName.contains(TYPE_MANAGED_EXECUTOR_SERVICE)
            || typeName.contains(TYPE_MANAGED_SCHEDULED_EXECUTOR_SERVICE)
            || typeName.contains(TYPE_JBOSS_MANAGED_EXECUTOR_ADAPTER);
    }

    static void shutdownExecutorQuietly(ExecutorService executor, Logger log, String executorName) {
        if (executor == null) {
            return;
        }
        if (isContainerManagedExecutor(executor)) {
            log.log(Level.FINE, LOG_SKIP_SHUTDOWN, executorName);
            return;
        }
        try {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        } catch (IllegalStateException | UnsupportedOperationException | SecurityException ex) {
            log.log(Level.FINE, LOG_EXECUTOR_SHUTDOWN_FAILED_PREFIX + executorName, ex);
        }
    }

    private static ScheduledExecutorService lookupManagedScheduledExecutor(Logger log) {
        return lookupExecutor(MANAGED_SCHEDULER_JNDI, ScheduledExecutorService.class, log);
    }

    private static <T> T lookupExecutor(String jndiName, Class<T> type, Logger log) {
        if (jndiName == null || type == null) {
            return null;
        }

        InitialContext context = null;
        try {
            context = new InitialContext();
            Object value = context.lookup(jndiName);
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            log.log(Level.WARNING, LOG_JNDI_RESOURCE_WRONG_TYPE, new Object[]{jndiName, type.getSimpleName()});
            return null;
        } catch (NamingException ex) {
            log.log(Level.WARNING, LOG_LOOKUP_MANAGED_EXECUTOR_FAILED, jndiName);
            log.log(Level.FINE, LOG_MANAGED_EXECUTOR_LOOKUP_FAILURE_DETAILS, ex);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException closeEx) {
                    log.log(Level.FINE, LOG_CLOSE_INITIAL_CONTEXT_FAILED, closeEx);
                }
            }
        }
    }
}
