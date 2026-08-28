package com.sim.chatserver.web.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.web.dashboard.DashboardMetricsService.DashboardProgressMetrics;

class DashboardCacheRegistryTest {

    @Test
    void getWidgetStats_usesCacheUntilCleared() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger loads = new AtomicInteger(0);

        List<WidgetStat> first = registry.getWidgetStats(() -> List.of(
                new WidgetStat("w" + loads.incrementAndGet(), "Widget", 1)));
        List<WidgetStat> second = registry.getWidgetStats(() -> List.of(
                new WidgetStat("w" + loads.incrementAndGet(), "Widget", 1)));

        assertSame(first, second);
        assertEquals(1, loads.get());

        registry.clearAll();
        List<WidgetStat> third = registry.getWidgetStats(() -> List.of(
                new WidgetStat("w" + loads.incrementAndGet(), "Widget", 1)));

        assertNotSame(first, third);
        assertEquals(2, loads.get());
    }

    @Test
    void getWidgetStats_invalidatesWhenDayKeyChanges() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger loads = new AtomicInteger(0);

        List<WidgetStat> first = registry.getWidgetStats(() -> List.of(
                new WidgetStat("w" + loads.incrementAndGet(), "Widget", 1)));

        setPrivateField(registry, DashboardCacheRegistry.class, "widgetStatsDayKey", "1900-01-01");

        List<WidgetStat> second = registry.getWidgetStats(() -> List.of(
                new WidgetStat("w" + loads.incrementAndGet(), "Widget", 1)));

        assertNotSame(first, second);
        assertEquals(2, loads.get());
    }

    @Test
    void getSessionOverview_evictsOldestEntryWhenCapacityExceeded() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger key0Loads = new AtomicInteger(0);

        registry.getSessionOverview("k0", () -> {
            key0Loads.incrementAndGet();
            return overview("k0", 1);
        });

        for (int i = 1; i <= 64; i++) {
            final int marker = i;
            registry.getSessionOverview("k" + marker, () -> overview("k" + marker, marker));
        }

        DashboardLocalViewModels.SessionOverview reloaded = registry.getSessionOverview("k0", () -> {
            key0Loads.incrementAndGet();
            return overview("k0", 99);
        });

        assertEquals(2, key0Loads.get());
        assertEquals("k0-99", reloaded.getTopSessions().get(0).getSessionId());
    }

    @Test
    void getTermSummary_returnsFallbackWhenLoaderThrowsAfterExpiry() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();

        TermSummary initial = new TermSummary();
        initial.ensureTerm("alpha");
        TermSummary cached = registry.getTermSummary(() -> initial);

        Object cacheEntry = getPrivateField(registry, DashboardCacheRegistry.class, "termSummaryCache");
        setPrivateField(cacheEntry, cacheEntry.getClass(), "expiresAt", System.currentTimeMillis() - 5000L);
        setPrivateField(cacheEntry, cacheEntry.getClass(), "staleUntil", System.currentTimeMillis() - 1000L);

        TermSummary fallback = registry.getTermSummary(() -> {
            throw new RuntimeException("loader failure");
        });

        assertSame(cached, fallback);
    }

    @Test
    void getSessionOverview_returnsFallbackWhenLoaderThrowsAfterExpiry() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();

        DashboardLocalViewModels.SessionOverview initial = registry.getSessionOverview("key", () -> overview("key", 1));

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) getPrivateField(
                registry,
                DashboardCacheRegistry.class,
                "sessionOverviewCache");
        Object entry = map.get("key");
        setPrivateField(entry, entry.getClass(), "expiresAt", System.currentTimeMillis() - 5000L);
        setPrivateField(entry, entry.getClass(), "staleUntil", System.currentTimeMillis() - 1000L);

        DashboardLocalViewModels.SessionOverview fallback = registry.getSessionOverview("key", () -> {
            throw new RuntimeException("loader failure");
        });

        assertSame(initial, fallback);
    }

    @Test
    void getDashboardProgressMetrics_reloadsWhenDayKeyChanges() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger loads = new AtomicInteger(0);

        DashboardProgressMetrics first = registry.getDashboardProgressMetrics(
                () -> DashboardProgressMetrics.of(loads.incrementAndGet(), 0, 1, 0));

        setPrivateField(registry, DashboardCacheRegistry.class, "dashboardProgressDayKey", "1900-01-01");

        DashboardProgressMetrics second = registry.getDashboardProgressMetrics(
                () -> DashboardProgressMetrics.of(loads.incrementAndGet(), 0, 2, 0));

        assertEquals(2, loads.get());
        assertEquals(1, first.getChatsToday());
        assertEquals(2, second.getChatsToday());
    }

    @Test
    void getTermSummary_withGraceEntry_triggersAsyncRefresh() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger refreshLoads = new AtomicInteger(0);

        TermSummary initial = new TermSummary();
        initial.ensureTerm("alpha");
        TermSummary cached = registry.getTermSummary(() -> initial);

        Object entry = getPrivateField(registry, DashboardCacheRegistry.class, "termSummaryCache");
        long now = System.currentTimeMillis();
        setPrivateField(entry, entry.getClass(), "expiresAt", now - 1000L);
        setPrivateField(entry, entry.getClass(), "staleUntil", now + 30_000L);
        setPrivateField(entry, entry.getClass(), "refreshing", false);

        TermSummary stale = registry.getTermSummary(() -> {
            refreshLoads.incrementAndGet();
            TermSummary refreshed = new TermSummary();
            refreshed.ensureTerm("beta");
            return refreshed;
        });
        assertSame(cached, stale);

        assertTrue(waitForCondition(() -> refreshLoads.get() >= 1, 2500L));

        TermSummary refreshedResult = registry.getTermSummary(() -> cached);
        assertNotSame(cached, refreshedResult);
    }

    @Test
    void getSessionOverview_withGraceEntry_triggersAsyncRefresh() {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();
        AtomicInteger refreshLoads = new AtomicInteger(0);

        DashboardLocalViewModels.SessionOverview initial = registry.getSessionOverview("key", () -> overview("key", 1));

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) getPrivateField(
                registry,
                DashboardCacheRegistry.class,
                "sessionOverviewCache");
        Object entry = map.get("key");
        long now = System.currentTimeMillis();
        setPrivateField(entry, entry.getClass(), "expiresAt", now - 1000L);
        setPrivateField(entry, entry.getClass(), "staleUntil", now + 30_000L);
        setPrivateField(entry, entry.getClass(), "refreshing", false);

        DashboardLocalViewModels.SessionOverview stale = registry.getSessionOverview("key", () -> {
            refreshLoads.incrementAndGet();
            return overview("key", 2);
        });
        assertSame(initial, stale);

        assertTrue(waitForCondition(() -> refreshLoads.get() >= 1, 2500L));

        DashboardLocalViewModels.SessionOverview refreshed = registry.getSessionOverview("key", () -> overview("key", 3));
        assertNotSame(initial, refreshed);
        assertEquals("key-2", refreshed.getTopSessions().get(0).getSessionId());
    }

    @Test
    void serializationGuards_throwNotSerializableException() throws Exception {
        DashboardCacheRegistry registry = new DashboardCacheRegistry();

        Method readObject = DashboardCacheRegistry.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
        readObject.setAccessible(true);
        Exception readFailure = assertThrows(Exception.class, () -> readObject.invoke(registry, new Object[]{null}));
        assertTrue(readFailure.getCause() instanceof java.io.NotSerializableException);

        Method writeObject = DashboardCacheRegistry.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        writeObject.setAccessible(true);
        Exception writeFailure = assertThrows(Exception.class, () -> writeObject.invoke(registry, new Object[]{null}));
        assertTrue(writeFailure.getCause() instanceof java.io.NotSerializableException);
    }

    private static DashboardLocalViewModels.SessionOverview overview(String key, int marker) {
        return new DashboardLocalViewModels.SessionOverview(
                List.of(new SessionStat(key + "-" + marker, marker, "last")),
                new SessionTimeline(List.of("day-1"), Map.of(key, List.of(marker))),
                10,
                6,
                4,
                2,
                2,
                1,
                new DashboardLocalViewModels.ProgressStat(2, 1),
                5,
                new DashboardLocalViewModels.ProgressStat(6, 5));
    }

    private static void setPrivateField(Object object, Class<?> fieldClass, String fieldName, Object value) {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new AssertionError("Unable to set private field " + fieldName, ex);
        }
    }

    private static Object getPrivateField(Object object, Class<?> fieldClass, String fieldName) {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new AssertionError("Unable to read private field " + fieldName, ex);
        }
    }

    private static boolean waitForCondition(BooleanSupplier condition, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return condition.getAsBoolean();
            }
        }
        return condition.getAsBoolean();
    }
}