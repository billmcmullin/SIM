package com.sim.chatserver.service.dashboard;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;

/**
 * Compilation-safe, thread-safe cache registry for dashboard data. - Simple TTL
 * caching - Stale-while-revalidate grace window - Bounded session-overview
 * keyed cache
 */
public class DashboardCacheRegistry {

    // TTLs
    private static final long WIDGET_STATS_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long TERM_SUMMARY_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long SESSION_OVERVIEW_TTL_MILLIS = Duration.ofSeconds(20).toMillis();
    private static final long CHAT_PROGRESSION_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long NEW_USER_PROGRESSION_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long TOP_TOPICS_TTL_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final long OTHER_PARASOFT_LATEST_TTL_MILLIS = Duration.ofSeconds(30).toMillis();

    // NEW: combined dashboard progress metrics TTL
    private static final long DASHBOARD_PROGRESS_TTL_MILLIS = Duration.ofSeconds(30).toMillis();

    // Grace period to serve stale value while refresh happens
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

    // NEW: combined dashboard progress cache
    private final Object dashboardProgressLock = new Object();
    private volatile Entry<DashboardProgressMetrics> dashboardProgressCache;

    // Keyed cache
    private final Object sessionLock = new Object();
    private final LinkedHashMap<String, Entry<SessionOverview>> sessionOverviewCache
            = new LinkedHashMap<>(16, 0.75f, true);

    public List<WidgetStat> getWidgetStats(Supplier<List<WidgetStat>> loader) {
        return getSingle(widgetLock, () -> widgetStatsCache, v -> widgetStatsCache = v, WIDGET_STATS_TTL_MILLIS, loader);
    }

    public TermSummary getTermSummary(Supplier<TermSummary> loader) {
        return getSingle(termLock, () -> termSummaryCache, v -> termSummaryCache = v, TERM_SUMMARY_TTL_MILLIS, loader);
    }

    public ProgressStat getChatProgression(Supplier<ProgressStat> loader) {
        return getSingle(chatProgLock, () -> chatProgressionCache, v -> chatProgressionCache = v, CHAT_PROGRESSION_TTL_MILLIS, loader);
    }

    public ProgressStat getNewUserProgression(Supplier<ProgressStat> loader) {
        return getSingle(newUserProgLock, () -> newUserProgressionCache, v -> newUserProgressionCache = v, NEW_USER_PROGRESSION_TTL_MILLIS, loader);
    }

    public List<TopTopic> getTopTopics(Supplier<List<TopTopic>> loader) {
        return getSingle(topTopicsLock, () -> topTopicsCache, v -> topTopicsCache = v, TOP_TOPICS_TTL_MILLIS, loader);
    }

    public List<OtherParasoftEntry> getOtherParasoftLatest(Supplier<List<OtherParasoftEntry>> loader) {
        return getSingle(otherParasoftLock, () -> otherParasoftLatestCache, v -> otherParasoftLatestCache = v, OTHER_PARASOFT_LATEST_TTL_MILLIS, loader);
    }

    // NEW
    public DashboardProgressMetrics getDashboardProgressMetrics(Supplier<DashboardProgressMetrics> loader) {
        return getSingle(
                dashboardProgressLock,
                () -> dashboardProgressCache,
                v -> dashboardProgressCache = v,
                DASHBOARD_PROGRESS_TTL_MILLIS,
                loader
        );
    }

    public SessionOverview getSessionOverview(String key, Supplier<SessionOverview> loader) {
        long now = System.currentTimeMillis();

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
                startAsyncSessionRefresh(key, loader);
                return current.value;
            }
        }

        synchronized (sessionLock) {
            Entry<SessionOverview> second = sessionOverviewCache.get(key);
            long now2 = System.currentTimeMillis();
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

    public void clearAll() {
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
    }

    private <T> T getSingle(
            Object lock,
            Supplier<Entry<T>> getter,
            java.util.function.Consumer<Entry<T>> setter,
            long ttlMillis,
            Supplier<T> loader
    ) {
        long now = System.currentTimeMillis();
        Entry<T> current = getter.get();

        if (isFresh(current, now)) {
            return current.value;
        }

        if (isWithinGrace(current, now)) {
            if (!current.refreshing) {
                synchronized (lock) {
                    Entry<T> c2 = getter.get();
                    long now2 = System.currentTimeMillis();
                    if (isFresh(c2, now2)) {
                        return c2.value;
                    }
                    if (isWithinGrace(c2, now2) && !c2.refreshing) {
                        c2.refreshing = true;
                        startAsyncSingleRefresh(lock, getter, setter, ttlMillis, loader);
                    }
                    return c2 == null ? null : c2.value;
                }
            }
            return current.value;
        }

        synchronized (lock) {
            Entry<T> c3 = getter.get();
            long now3 = System.currentTimeMillis();
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
        Thread t = new Thread(() -> {
            T fresh = safeLoad(loader, null);
            long now = System.currentTimeMillis();

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
        }, "dashboard-cache-refresh-single");
        t.setDaemon(true);
        t.start();
    }

    private void startAsyncSessionRefresh(String key, Supplier<SessionOverview> loader) {
        Thread t = new Thread(() -> {
            SessionOverview fresh = safeLoad(loader, null);
            long now = System.currentTimeMillis();

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
        }, "dashboard-cache-refresh-session");
        t.setDaemon(true);
        t.start();
    }

    private static boolean isFresh(Entry<?> e, long now) {
        return e != null && now < e.expiresAt;
    }

    private static boolean isWithinGrace(Entry<?> e, long now) {
        return e != null && now >= e.expiresAt && now < e.staleUntil;
    }

    private static <T> T safeLoad(Supplier<T> loader, T fallback) {
        try {
            T value = loader.get();
            return value != null ? value : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static <K, V> void evictIfNeeded(LinkedHashMap<K, V> map, int maxSize) {
        while (map.size() > maxSize) {
            K eldest = map.keySet().iterator().next();
            map.remove(eldest);
        }
    }

    private static final class Entry<T> {

        private volatile T value;
        private volatile long expiresAt;
        private volatile long staleUntil;
        private volatile boolean refreshing;

        private Entry(T value, long expiresAt, long staleUntil) {
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
