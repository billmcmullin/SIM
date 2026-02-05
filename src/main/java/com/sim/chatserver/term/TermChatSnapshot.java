package com.sim.chatserver.term;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Snapshot representing a single chat that matched a specific term.
 */
public final class TermChatSnapshot {

    private final String termName;
    private final String widgetId;
    private final String chatId;
    private final String prompt;
    private final String response;
    private final Timestamp createdAt;
    private final String sessionId;

    public TermChatSnapshot(String termName,
            String widgetId,
            String chatId,
            String prompt,
            String response,
            Timestamp createdAt,
            String sessionId) {
        this.termName = Objects.requireNonNull(termName, "termName required");
        this.widgetId = Objects.requireNonNull(widgetId, "widgetId required");
        this.chatId = Objects.requireNonNull(chatId, "chatId required");
        this.prompt = prompt == null ? "" : prompt;
        this.response = response == null ? "" : response;
        this.createdAt = createdAt;
        this.sessionId = sessionId == null ? "" : sessionId;
    }

    public String getTermName() {
        return termName;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getResponse() {
        return response;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(termName, widgetId, chatId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TermChatSnapshot other) {
            return termName.equals(other.termName)
                    && widgetId.equals(other.widgetId)
                    && chatId.equals(other.chatId);
        }
        return false;
    }
}
