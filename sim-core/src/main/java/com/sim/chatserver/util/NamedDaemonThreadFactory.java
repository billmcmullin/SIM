package com.sim.chatserver.util;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedDaemonThreadFactory implements ThreadFactory {

    private static final String DEFAULT_WORKER_PREFIX = "worker-";
    private static final String MSG_TASK_REQUIRED = "task";

    private final String prefix;
    private final AtomicInteger index = new AtomicInteger(1);

    private NamedDaemonThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public static ThreadFactory of(String prefix) {
        String safePrefix = prefix == null ? DEFAULT_WORKER_PREFIX : prefix.trim();
        if (safePrefix.isEmpty()) {
            safePrefix = DEFAULT_WORKER_PREFIX;
        }
        return new NamedDaemonThreadFactory(safePrefix);
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new Thread(Objects.requireNonNull(task, MSG_TASK_REQUIRED), prefix + index.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
