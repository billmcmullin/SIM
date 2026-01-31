package com.sim.chatserver.widget;

import java.time.Instant;
import java.util.Objects;

public final class WidgetEntry {

    private final int id;
    private final String widgetId;
    private final String displayName;
    private final Instant createdAt;

    public WidgetEntry(int id, String widgetId, String displayName, Instant createdAt) {
        this.id = id;
        this.widgetId = Objects.requireNonNull(widgetId, "widgetId");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public int getId() {
        return id;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
