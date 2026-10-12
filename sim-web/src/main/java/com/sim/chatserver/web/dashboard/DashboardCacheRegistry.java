package com.sim.chatserver.web.dashboard;

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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.NamedDaemonThreadFactory;
import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.TermSummary;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;

/**
 * Thread-safe dashboard cache registry with: - TTL caching -
 * Stale-while-revalidate grace window - Bounded keyed session-overview cache -
 * Day-keyed refresh behavior for "today/yesterday" sensitive metrics
 */
final class DashboardCacheRegistry {

    private static final String LOG_CACHE_REFRESH_SATURATED = "Dashboard cache refresh executor saturated; running task inline";
    private static final String LOG_CACHE_REFRESH_LOADER_FAILED = "Dashboard cache refresh loader failed; using fallback value";
    private static final String CACHE_REFRESH_THREAD_PREFIX = "dashboard-cache-refresh-";

    private static final Logger log = Logger.getLogger(DashboardCacheRegistry.class.getCanonicalName());
    private static final ExecutorService CACHE_REFRESH_EXECUTOR = Executors.newFixedThreadPool(
            2,
        NamedDaemonThreadFactory.of(CACHE_REFRESH_THREAD_PREFIX)
    );

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
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
    private volatile DashboardCacheEntry<List<WidgetStat>> widgetStatsCache;

    private final Object termLock = new Object();
    private volatile DashboardCacheEntry<TermSummary> termSummaryCache;

    private final Object chatProgLock = new Object();
    private volatile DashboardCacheEntry<ProgressStat> chatProgressionCache;

    private final Object newUserProgLock = new Object();
    private volatile DashboardCacheEntry<ProgressStat> newUserProgressionCache;

    private final Object topTopicsLock = new Object();
    private volatile DashboardCacheEntry<List<TopTopic>> topTopicsCache;

    private final Object otherParasoftLock = new Object();
    private volatile DashboardCacheEntry<List<OtherParasoftEntry>> otherParasoftLatestCache;

    private final Object dashboardProgressLock = new Object();
    private volatile DashboardCacheEntry<DashboardProgressMetrics> dashboardProgressCache;

    // Keyed cache
    private final Object sessionLock = new Object();
    private final LinkedHashMap<String, DashboardCacheEntry<SessionOverview>> sessionOverviewCache
            = new LinkedHashMap<>(16, 0.75f, true);

    // Day keys so stale "today/yesterday" values are dropped when date rolls over.
    private final Object dayKeyLock = new Object();
    private volatile String chatProgressionDayKey;
    private volatile String newUserProgressionDayKey;
    private volatile String topTopicsDayKey;
    private volatile String dashboardProgressDayKey;
    private volatile String widgetStatsDayKey;

    List<WidgetStat> getWidgetStats(Supplier<List<WidgetStat>> loader) {
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

    TermSummary getTermSummary(Supplier<TermSummary> loader) {
        return getSingle(termLock, () -> termSummaryCache, v -> termSummaryCache = v, TERM_SUMMARY_TTL_MILLIS, loader);
    }

    ProgressStat getChatProgression(Supplier<ProgressStat> loader) {
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

    ProgressStat getNewUserProgression(Supplier<ProgressStat> loader) {
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

    List<TopTopic> getTopTopics(Supplier<List<TopTopic>> loader) {
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

    List<OtherParasoftEntry> getOtherParasoftLatest(Supplier<List<OtherParasoftEntry>> loader) {
        return getSingle(otherParasoftLock, () -> otherParasoftLatestCache, v -> otherParasoftLatestCache = v, OTHER_PARASOFT_LATEST_TTL_MILLIS, loader);
    }

    final DashboardProgressMetrics getDashboardProgressMetrics(Supplier<DashboardProgressMetrics> loader) {
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

    SessionOverview getSessionOverview(String key, Supplier<SessionOverview> loader) {
        boolean startRefresh = false;
        SessionOverview valueToReturn = null;
        synchronized (sessionLock) {
            long now = nowMillis();
            DashboardCacheEntry<SessionOverview> current = sessionOverviewCache.get(key);
            if (isFresh(current, now)) {
                return current.value;
            }

            if (isWithinGrace(current, now)) {
                if (!current.refreshing) {
                    current.refreshing = true;
                    startRefresh = true;
                }
                valueToReturn = current.value;
            } else {
                SessionOverview fallback = current == null ? null : current.value;
                SessionOverview fresh = safeLoad(loader, fallback);
                DashboardCacheEntry<SessionOverview> updated = DashboardCacheEntry.of(
                        fresh,
                        now + SESSION_OVERVIEW_TTL_MILLIS,
                        now + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS
                );

                sessionOverviewCache.put(key, updated);
                evictIfNeeded(sessionOverviewCache, SESSION_OVERVIEW_CACHE_MAX);
                return updated.value;
            }
        }

        if (startRefresh) {
            startAsyncSessionRefresh(key, loader);
        }
        return valueToReturn;
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
                Supplier<DashboardCacheEntry<T>> getter,
                java.util.function.Consumer<DashboardCacheEntry<T>> setter,
            long ttlMillis,
            Supplier<T> loader
    ) {
        boolean startRefresh = false;
        T valueToReturn = null;
        synchronized (lock) {
            long now = nowMillis();
            DashboardCacheEntry<T> current = getter.get();

            if (isFresh(current, now)) {
                return current.value;
            }

            if (isWithinGrace(current, now)) {
                if (!current.refreshing) {
                    current.refreshing = true;
                    startRefresh = true;
                }
                valueToReturn = current.value;
            } else {
                T fallback = current == null ? null : current.value;
                T fresh = safeLoad(loader, fallback);
                DashboardCacheEntry<T> updated = DashboardCacheEntry.of(
                        fresh,
                        now + ttlMillis,
                        now + ttlMillis + STALE_GRACE_MILLIS
                );
                setter.accept(updated);
                return updated.value;
            }
        }

        if (startRefresh) {
            startAsyncSingleRefresh(lock, getter, setter, ttlMillis, loader);
        }
        return valueToReturn;
    }

    private <T> void startAsyncSingleRefresh(
            Object lock,
            Supplier<DashboardCacheEntry<T>> getter,
            java.util.function.Consumer<DashboardCacheEntry<T>> setter,
            long ttlMillis,
            Supplier<T> loader
    ) {
        submitRefreshTask(() -> {
            DashboardCacheEntry<T> snapshot = getter.get();
            T fallback = snapshot == null ? null : snapshot.value;
            T fresh = safeLoad(loader, fallback);
            long now = nowMillis();

            synchronized (lock) {
                DashboardCacheEntry<T> current = getter.get();
                DashboardCacheEntry<T> target;
                if (current == null) {
                    target = DashboardCacheEntry.of(
                            fresh,
                            now + ttlMillis,
                            now + ttlMillis + STALE_GRACE_MILLIS
                    );
                    setter.accept(target);
                } else {
                    current.value = fresh;
                    current.expiresAt = now + ttlMillis;
                    current.staleUntil = now + ttlMillis + STALE_GRACE_MILLIS;
                    target = current;
                }
                target.refreshing = false;
            }
        });
    }

    private void startAsyncSessionRefresh(String key, Supplier<SessionOverview> loader) {
        submitRefreshTask(() -> {
            DashboardCacheEntry<SessionOverview> snapshot = currentSessionOverviewEntry(key);

            SessionOverview fallback = snapshot == null ? null : snapshot.value;
            SessionOverview fresh = safeLoad(loader, fallback);
            long now = nowMillis();

            synchronized (sessionLock) {
                DashboardCacheEntry<SessionOverview> current = sessionOverviewCache.get(key);
                DashboardCacheEntry<SessionOverview> target;
                if (current == null) {
                    target = DashboardCacheEntry.of(
                            fresh,
                            now + SESSION_OVERVIEW_TTL_MILLIS,
                            now + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS
                    );
                    sessionOverviewCache.put(key, target);
                    evictIfNeeded(sessionOverviewCache, SESSION_OVERVIEW_CACHE_MAX);
                } else {
                    current.value = fresh;
                    current.expiresAt = now + SESSION_OVERVIEW_TTL_MILLIS;
                    current.staleUntil = now + SESSION_OVERVIEW_TTL_MILLIS + STALE_GRACE_MILLIS;
                    target = current;
                }
                target.refreshing = false;
            }
        });
    }

    private DashboardCacheEntry<SessionOverview> currentSessionOverviewEntry(String key) {
        synchronized (sessionLock) {
            return sessionOverviewCache.get(key);
        }
    }

    private static void submitRefreshTask(Runnable task) {
        try {
            CACHE_REFRESH_EXECUTOR.execute(task);
        } catch (RejectedExecutionException ex) {
            log.log(Level.FINE, LOG_CACHE_REFRESH_SATURATED, ex);
            task.run();
        }
    }

    private static boolean isFresh(DashboardCacheEntry<?> e, long now) {
        return e != null && now < e.expiresAt;
    }

    private static boolean isWithinGrace(DashboardCacheEntry<?> e, long now) {
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
                    log.log(Level.WARNING, LOG_CACHE_REFRESH_LOADER_FAILED, cause);
                    return fallback;
                })
                .join();
    }

    private static <K, V> void evictIfNeeded(LinkedHashMap<K, V> map, int maxSize) {
        var iterator = map.entrySet().iterator();
        while (map.size() > maxSize && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    private static String currentDayKey() {
        return LocalDate.now(ZoneId.systemDefault()).toString();
    }



}


