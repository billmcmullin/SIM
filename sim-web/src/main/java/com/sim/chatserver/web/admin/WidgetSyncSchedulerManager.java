package com.sim.chatserver.web.admin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

final class WidgetSyncSchedulerManager {

    private static final String MANAGED_SCHEDULER_JNDI = "java:comp/DefaultManagedScheduledExecutorService";

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
                log.warning("Managed scheduled executor is unavailable; automatic widget sync scheduling is disabled.");
            }
            return replacement;
        }
        return current;
    }

    static boolean isExecutorStopped(ExecutorService executor, Logger log, String executorName) {
        if (executor == null) {
            return true;
        }
        try {
            return executor.isShutdown() || executor.isTerminated();
        } catch (IllegalStateException | UnsupportedOperationException | SecurityException ex) {
            if (isContainerManagedExecutor(executor)) {
                log.log(Level.FINE, "Skipping lifecycle check for container-managed executor {0}", executorName);
                return false;
            }
            log.log(Level.WARNING, "Executor lifecycle check failed for " + executorName + "; recreating local executor", ex);
            return true;
        }
    }

    static boolean isContainerManagedExecutor(ExecutorService executor) {
        if (executor == null) {
            return false;
        }
        String typeName = executor.getClass().getName();
        return typeName.contains("ManagedExecutorService")
                || typeName.contains("ManagedScheduledExecutorService")
                || typeName.contains("jboss.as.ee.concurrent.adapter");
    }

    static void shutdownExecutorQuietly(ExecutorService executor, Logger log, String executorName) {
        if (executor == null) {
            return;
        }
        if (isContainerManagedExecutor(executor)) {
            log.log(Level.FINE, "Skipping shutdown for container-managed executor {0}", executorName);
            return;
        }
        try {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        } catch (IllegalStateException | UnsupportedOperationException | SecurityException ex) {
            log.log(Level.FINE, "Executor shutdown failed for " + executorName, ex);
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
            log.log(Level.WARNING, "JNDI resource {0} is not a {1}", new Object[]{jndiName, type.getSimpleName()});
            return null;
        } catch (NamingException ex) {
            log.log(Level.WARNING, "Unable to lookup managed executor {0}", jndiName);
            log.log(Level.FINE, "Managed executor lookup failure details", ex);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException closeEx) {
                    log.log(Level.FINE, "Failed to close InitialContext", closeEx);
                }
            }
        }
    }
}
