package com.sim.chatserver.service.dashboard;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;

/**
 * Thread-safe dashboard cache registry with: - TTL caching -
 * Stale-while-revalidate grace window - Bounded keyed session-overview cache -
 * Day-keyed refresh behavior for "today/yesterday" sensitive metrics
 */
public class DashboardCacheRegistry {

    private static final Logger log = Logger.getLogger(DashboardCacheRegistry.class.getName());
    private static final AtomicInteger REFRESH_THREAD_INDEX = new AtomicInteger(1);
    private static final ExecutorService CACHE_REFRESH_EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "dashboard-cache-refresh-" + REFRESH_THREAD_INDEX.getAndIncrement());
        t.setDaemon(true);
        return t;
    });

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    // TTLs (shorter for day-sensitive metrics)
    private static final long WIDGET_STATS_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long TERM_SUMMARY_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long SESSION_OVERVIEW_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long CHAT_PROGRESSION_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long NEW_USER_PROGRESSION_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long TOP_TOPICS_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long OTHER_PARASOFT_LATEST_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long DASHBOARD_PROGRESS_TTL_MILLIS = Duration.ofSeconds(15).toMillis();

    private static final long STALE_GRACE_MILLIS = Duration.ofSeconds(8).toMillis();

    private static final int SESSION_OVERVIEW_CACHE_MAX = 64;

    // Single-value caches
    private final Object widgetLock = new Object();
    private volatile Entry<List<WidgetStat>> widgetStatsCache;

    private final Object termLock = new Object();
    private volatile Entry<TermSummary> termSummaryCache;

    private final Object chatProgLock = new Object();
    private volatile Entry<ProgressStat> chatProgressionCache;

    private final Object newUserProgLock = new Object();
    private volatile Entry<ProgressStat> newUserProgressionCache;

    private final Object topTopicsLock = new Object();
    private volatile Entry<List<TopTopic>> topTopicsCache;

    private final Object otherParasoftLock = new Object();
    private volatile Entry<List<OtherParasoftEntry>> otherParasoftLatestCache;

    private final Object dashboardProgressLock = new Object();
    private volatile Entry<DashboardProgressMetrics> dashboardProgressCache;

    // Keyed cache
    private final Object sessionLock = new Object();
    private final LinkedHashMap<String, Entry<SessionOverview>> sessionOverviewCache
            = new LinkedHashMap<>(16, 0.75f, true);

    // Day keys so stale "today/yesterday" values are dropped when date rolls over.
    private final Object dayKeyLock = new Object();
    private volatile String chatProgressionDayKey;
    private volatile String newUserProgressionDayKey;
    private volatile String topTopicsDayKey;
    private volatile String dashboardProgressDayKey;
    private volatile String widgetStatsDayKey;

    public List<WidgetStat> getWidgetStats(Supplier<List<WidgetStat>> loader) {
        String dayKey = currentDayKey();
        boolean invalidate = false;
        synchronized (dayKeyLock) {
            if (widgetStatsDayKey == null || !widgetStatsDayKey.equals(dayKey)) {
                widgetStatsDayKey = dayKey;
                invalidate = true;
            }
        }
        if (invalidate) {
            synchronized (widgetLock) {
                widgetStatsCache = null;
            }
        }
        return getSingle(widgetLock, () -> widgetStatsCache, v -> widgetStatsCache = v, WIDGET_STATS_TTL_MILLIS, loader);
    }

    public TermSummary getTermSummary(Supplier<TermSummary> loader) {
        return getSingle(termLock, () -> termSummaryCache, v -> termSummaryCache = v, TERM_SUMMARY_TTL_MILLIS, loader);
    }

    public ProgressStat getChatProgression(Supplier<ProgressStat> loader) {
        String dayKey = currentDayKey();
        boolean invalidate = false;
        synchronized (dayKeyLock) {
            if (chatProgressionDayKey == null || !chatProgressionDayKey.equals(dayKey)) {
                chatProgressionDayKey = dayKey;
                invalidate = true;
            }
        }
        if (invalidate) {
            synchronized (chatProgLock) {
                chatProgressionCache = null;
            }
        }
        return getSingle(chatProgLock, () -> chatProgressionCache, v -> chatProgressionCache = v, CHAT_PROGRESSION_TTL_MILLIS, loader);
    }

    public ProgressStat getNewUserProgression(Supplier<ProgressStat> loader) {
        String dayKey = currentDayKey();
        boolean invalidate = false;
        synchronized (dayKeyLock) {
            if (newUserProgressionDayKey == null || !newUserProgressionDayKey.equals(dayKey)) {
                newUserProgressionDayKey = dayKey;
                invalidate = true;
            }
        }
        if (invalidate) {
            synchronized (newUserProgLock) {
                newUserProgressionCache = null;
            }
        }
        return getSingle(newUserProgLock, () -> newUserProgressionCache, v -> newUserProgressionCache = v, NEW_USER_PROGRESSION_TTL_MILLIS, loader);
    }

    public List<TopTopic> getTopTopics(Supplier<List<TopTopic>> loader) {
        String dayKey = currentDayKey();
        boolean invalidate = false;
        synchronized (dayKeyLock) {
            if (topTopicsDayKey == null || !topTopicsDayKey.equals(dayKey)) {
                topTopicsDayKey = dayKey;
                invalidate = true;
            }
        }
        if (invalidate) {
            synchronized (topTopicsLock) {
                topTopicsCache = null;
            }
        }
        return getSingle(topTopicsLock, () -> topTopicsCache, v -> topTopicsCache = v, TOP_TOPICS_TTL_MILLIS, loader);
    }

    public List<OtherParasoftEntry> getOtherParasoftLatest(Supplier<List<OtherParasoftEntry>> loader) {
        return getSingle(otherParasoftLock, () -> otherParasoftLatestCache, v -> otherParasoftLatestCache = v, OTHER_PARASOFT_LATEST_TTL_MILLIS, loader);
    }

    public DashboardProgressMetrics getDashboardProgressMetrics(Supplier<DashboardProgressMetrics> loader) {
        String dayKey = currentDayKey();
        boolean invalidate = false;
        synchronized (dayKeyLock) {
            if (dashboardProgressDayKey == null || !dashboardProgressDayKey.equals(dayKey)) {
                dashboardProgressDayKey = dayKey;
                invalidate = true;
            }
        }
        if (invalidate) {
            synchronized (dashboardProgressLock) {
                dashboardProgressCache = null;
            }
        }

        return getSingle(
                dashboardProgressLock,
                () -> dashboardProgressCache,
                v -> dashboardProgressCache = v,
                DASHBOARD_PROGRESS_TTL_MILLIS,
                loader
        );
    }

    public SessionOverview getSessionOverview(String key, Supplier<SessionOverview> loader) {
        long now = nowMillis();
        boolean triggerRefresh = false;

        Entry<SessionOverview> current;
        synchronized (sessionLock) {
            current = sessionOverviewCache.get(key);
            if (isFresh(current, now)) {
                return current.value;
            }
            if (isWithinGrace(current, now) && current.refreshing) {
                return current.value;
            }
            if (isWithinGrace(current, now) && !current.refreshing) {
                current.refreshing = true;
                triggerRefresh = true;
            }
        }
        if (triggerRefresh) {
            startAsyncSessionRefresh(key, loader);
            return current == null ? null : current.value;
        }

        synchronized (sessionLock) {
            Entry<SessionOverview> second = sessionOverviewCache.get(key);
            long now2 = nowMillis();
            if (isFresh(second, now2)) {
                return second.value;
            }

            SessionOverview fallback = second == null ? null : second.value;
            SessionOverview fresh = safeLoad(loader, fallback);
            Entry<SessionOverview> updated = Entry.of(
                    fresh,
                    now2 + SESSION_OVERVIEW_TTL_MILLIS,
                    now2 + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS
            );

            sessionOverviewCache.put(key, updated);
            evictIfNeeded(sessionOverviewCache, SESSION_OVERVIEW_CACHE_MAX);
            return updated.value;
        }
    }

    final void clearAll() {
        synchronized (widgetLock) {
            widgetStatsCache = null;
        }
        synchronized (termLock) {
            termSummaryCache = null;
        }
        synchronized (chatProgLock) {
            chatProgressionCache = null;
        }
        synchronized (newUserProgLock) {
            newUserProgressionCache = null;
        }
        synchronized (topTopicsLock) {
            topTopicsCache = null;
        }
        synchronized (otherParasoftLock) {
            otherParasoftLatestCache = null;
        }
        synchronized (dashboardProgressLock) {
            dashboardProgressCache = null;
        }
        synchronized (sessionLock) {
            sessionOverviewCache.clear();
        }
        synchronized (dayKeyLock) {
            chatProgressionDayKey = null;
            newUserProgressionDayKey = null;
            topTopicsDayKey = null;
            dashboardProgressDayKey = null;
            widgetStatsDayKey = null;
        }
    }

    private <T> T getSingle(
            Object lock,
            Supplier<Entry<T>> getter,
            java.util.function.Consumer<Entry<T>> setter,
            long ttlMillis,
            Supplier<T> loader
    ) {
        long now = nowMillis();
        Entry<T> current = getter.get();
        boolean triggerRefresh = false;

        if (isFresh(current, now)) {
            return current.value;
        }

        if (isWithinGrace(current, now)) {
            if (!current.refreshing) {
                T staleValue;
                synchronized (lock) {
                    Entry<T> c2 = getter.get();
                    long now2 = nowMillis();
                    if (isFresh(c2, now2)) {
                        return c2.value;
                    }
                    if (isWithinGrace(c2, now2) && !c2.refreshing) {
                        c2.refreshing = true;
                        triggerRefresh = true;
                    }
                    staleValue = c2 == null ? null : c2.value;
                }
                if (triggerRefresh) {
                    startAsyncSingleRefresh(lock, getter, setter, ttlMillis, loader);
                }
                return staleValue;
            }
            return current.value;
        }

        synchronized (lock) {
            Entry<T> c3 = getter.get();
            long now3 = nowMillis();
            if (isFresh(c3, now3)) {
                return c3.value;
            }

            T fallback = c3 == null ? null : c3.value;
            T fresh = safeLoad(loader, fallback);
            Entry<T> updated = Entry.of(
                    fresh,
                    now3 + ttlMillis,
                    now3 + ttlMillis + STALE_GRACE_MILLIS
            );
            setter.accept(updated);
            return updated.value;
        }
    }

    private <T> void startAsyncSingleRefresh(
            Object lock,
            Supplier<Entry<T>> getter,
            java.util.function.Consumer<Entry<T>> setter,
            long ttlMillis,
            Supplier<T> loader
    ) {
        submitRefreshTask(() -> {
            T fallback;
            synchronized (lock) {
                Entry<T> snapshot = getter.get();
                fallback = snapshot == null ? null : snapshot.value;
            }

            try {
                T fresh = safeLoad(loader, fallback);
                long now = nowMillis();

                synchronized (lock) {
                    Entry<T> current = getter.get();
                    if (current == null) {
                        setter.accept(Entry.of(
                                fresh,
                                now + ttlMillis,
                                now + ttlMillis + STALE_GRACE_MILLIS
                        ));
                    } else {
                        current.value = fresh;
                        current.expiresAt = now + ttlMillis;
                        current.staleUntil = now + ttlMillis + STALE_GRACE_MILLIS;
                        current.refreshing = false;
                    }
                }
            } finally {
                synchronized (lock) {
                    Entry<T> current = getter.get();
                    if (current != null) {
                        current.refreshing = false;
                    }
                }
            }
        });
    }

    private void startAsyncSessionRefresh(String key, Supplier<SessionOverview> loader) {
        submitRefreshTask(() -> {
            SessionOverview fallback;
            synchronized (sessionLock) {
                Entry<SessionOverview> snapshot = sessionOverviewCache.get(key);
                fallback = snapshot == null ? null : snapshot.value;
            }

            try {
                SessionOverview fresh = safeLoad(loader, fallback);
                long now = nowMillis();

                synchronized (sessionLock) {
                    Entry<SessionOverview> current = sessionOverviewCache.get(key);
                    if (current == null) {
                        sessionOverviewCache.put(key, Entry.of(
                                fresh,
                                now + SESSION_OVERVIEW_TTL_MILLIS,
                                now + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS
                        ));
                        evictIfNeeded(sessionOverviewCache, SESSION_OVERVIEW_CACHE_MAX);
                    } else {
                        current.value = fresh;
                        current.expiresAt = now + SESSION_OVERVIEW_TTL_MILLIS;
                        current.staleUntil = now + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS;
                        current.refreshing = false;
                    }
                }
            } finally {
                synchronized (sessionLock) {
                    Entry<SessionOverview> current = sessionOverviewCache.get(key);
                    if (current != null) {
                        current.refreshing = false;
                    }
                }
            }
        });
    }

    private static void submitRefreshTask(Runnable task) {
        try {
            CACHE_REFRESH_EXECUTOR.execute(task);
        } catch (RejectedExecutionException ex) {
            log.log(Level.FINE, "Dashboard cache refresh executor saturated; running task inline", ex);
            task.run();
        }
    }

    private static boolean isFresh(Entry<?> e, long now) {
        return e != null && now < e.expiresAt;
    }

    private static boolean isWithinGrace(Entry<?> e, long now) {
        return e != null && now >= e.expiresAt && now < e.staleUntil;
    }

    private static long nowMillis() {
        return Instant.now().toEpochMilli();
    }

    private static <T> T safeLoad(Supplier<T> loader, T fallback) {
        return CompletableFuture.completedFuture(loader)
                .thenApply(Supplier::get)
                .handle((value, ex) -> {
                    if (ex == null) {
                        return value != null ? value : fallback;
                    }
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.log(Level.WARNING, "Dashboard cache refresh loader failed; using fallback value", cause);
                    return fallback;
                })
                .join();
    }

    private static <K, V> void evictIfNeeded(LinkedHashMap<K, V> map, int maxSize) {
        while (map.size() > maxSize) {
            K eldest = map.keySet().iterator().next();
            map.remove(eldest);
        }
    }

    private static String currentDayKey() {
        return LocalDate.now(ZoneId.systemDefault()).toString();
    }

    static final class Entry<T> {

        volatile T value;
        volatile long expiresAt;
        volatile long staleUntil;
        volatile boolean refreshing;

        Entry(T value, long expiresAt, long staleUntil) {
            this.value = value;
            this.expiresAt = expiresAt;
            this.staleUntil = staleUntil;
            this.refreshing = false;
        }

        private static <T> Entry<T> of(T value, long expiresAt, long staleUntil) {
            return new Entry<>(value, expiresAt, staleUntil);
        }
    }
}
