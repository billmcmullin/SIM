package com.sim.chatserver.model;

import java.sql.Timestamp;

public final class OtherParasoftEntry {

    private final String widgetId;
    private final String widgetName;
    private final String prompt;
    private final String sessionId;
    private final Timestamp createdAt;

    public OtherParasoftEntry(String widgetId, String widgetName, String prompt, String sessionId, Timestamp createdAt) {
        this.widgetId = widgetId;
        this.widgetName = widgetName;
        this.prompt = prompt;
        this.sessionId = sessionId;
        this.createdAt = createdAt;
    }

    String getWidgetId() {
        return widgetId;
    }

    public String getWidgetName() {
        return widgetName;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
