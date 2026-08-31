package com.sim.chatserver.web;

import java.lang.reflect.Constructor;
import java.time.Instant;

import com.sim.chatserver.widget.WidgetEntry;

public final class TestWidgetEntryFactory {

    private TestWidgetEntryFactory() {
    }

    public static WidgetEntry newWidgetEntry(int id, String widgetId, String displayName, Instant createdAt) {
        try {
            Constructor<WidgetEntry> ctor = WidgetEntry.class.getDeclaredConstructor(int.class, String.class, String.class, Instant.class);
            ctor.setAccessible(true);
            return ctor.newInstance(Integer.valueOf(id), widgetId, displayName, createdAt);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate WidgetEntry for test", ex);
        }
    }
}
