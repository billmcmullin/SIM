package com.sim.chatserver.widget;

import java.time.Instant;
import java.util.Objects;

public final class WidgetEntry {

    private static final String MSG_WIDGET_ID_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID;
    private static final String MSG_DISPLAY_NAME_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_NAME;
    private static final String MSG_CREATED_AT_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CREATED_AT;

    private final int id;
    private final String widgetId;
    private final String displayName;
    private final Instant createdAt;

    WidgetEntry(int id, String widgetId, String displayName, Instant createdAt) {
        this.id = id;
        this.widgetId = Objects.requireNonNull(widgetId, MSG_WIDGET_ID_REQUIRED);
        this.displayName = Objects.requireNonNull(displayName, MSG_DISPLAY_NAME_REQUIRED);
        this.createdAt = Objects.requireNonNull(createdAt, MSG_CREATED_AT_REQUIRED);
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

    Instant getCreatedAt() {
        return createdAt;
    }
}
