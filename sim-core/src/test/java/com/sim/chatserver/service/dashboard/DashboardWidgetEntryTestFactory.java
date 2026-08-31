package com.sim.chatserver.service.dashboard;

import java.lang.reflect.Constructor;
import java.time.Instant;

import com.sim.chatserver.widget.WidgetEntry;

final class DashboardWidgetEntryTestFactory {

    private DashboardWidgetEntryTestFactory() {
    }

    static WidgetEntry newWidgetEntry(int id, String widgetId, String displayName, Instant createdAt) {
        try {
            Constructor<WidgetEntry> ctor = WidgetEntry.class.getDeclaredConstructor(int.class, String.class, String.class, Instant.class);
            ctor.setAccessible(true);
            return ctor.newInstance(Integer.valueOf(id), widgetId, displayName, createdAt);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate WidgetEntry for test", ex);
        }
    }
}
