package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Shared model for selected review/chat entries.
 *
 * This replaces the inner static model previously embedded in the servlet.
 */
public final class SelectedEntry {

    private static final Logger LOGGER = Logger.getLogger(SelectedEntry.class.getName());

    private final String chatId;
    private final String prompt;
    private final String response;
    private final String createdAt;
    private final String sessionId;

    public SelectedEntry(String chatId, String prompt, String response, String createdAt, String sessionId) {
        this.chatId = safe(chatId);
        this.prompt = safe(prompt);
        this.response = safe(response);
        this.createdAt = safe(createdAt);
        this.sessionId = safe(sessionId);
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    private JsonObject toJson() {
        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("chatId", chatId);
        b.add("prompt", prompt);
        b.add("response", response);
        b.add("createdAt", createdAt);
        b.add("sessionId", sessionId);
        return b.build();
    }

    private static SelectedEntry fromJson(JsonObject o) {
        if (o == null) {
            return new SelectedEntry("", "", "", "", "");
        }

        return new SelectedEntry(
                str(o, "chatId"),
                str(o, "prompt"),
                str(o, "response"),
                str(o, "createdAt"),
                str(o, "sessionId")
        );
    }

    static JsonArray toJsonArray(List<SelectedEntry> entries) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        if (entries != null) {
            for (SelectedEntry e : entries) {
                if (e != null) {
                    b.add(e.toJson());
                }
            }
        }
        return b.build();
    }

    static List<SelectedEntry> fromJsonArray(JsonArray arr, int maxItems) {
        if (arr == null || arr.isEmpty() || maxItems <= 0) {
            return Collections.emptyList();
        }

        List<SelectedEntry> out = new ArrayList<>();
        int count = 0;

        for (var v : arr) {
            if (count >= maxItems) {
                break;
            }
            if (v != null && v.getValueType() == jakarta.json.JsonValue.ValueType.OBJECT) {
                out.add(fromJson(v.asJsonObject()));
                count++;
            }
        }

        return out;
    }

    private static String str(JsonObject o, String key) {
        try {
            var value = o.get(key);
            if (value != null) {
                if (value.getValueType() == jakarta.json.JsonValue.ValueType.STRING) {
                    return o.getString(key, "");
                }
                return String.valueOf(value);
            }
        } catch (ClassCastException | IllegalStateException ex) {
            LOGGER.log(Level.FINE, "Unable to parse SelectedEntry JSON field: {0}", key);
        }
        return "";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String toString() {
        return "SelectedEntry{"
                + "chatId='" + chatId + '\''
                + ", createdAt='" + createdAt + '\''
                + ", sessionId='" + sessionId + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectedEntry that)) {
            return false;
        }
        return Objects.equals(chatId, that.chatId)
                && Objects.equals(prompt, that.prompt)
                && Objects.equals(response, that.response)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, prompt, response, createdAt, sessionId);
    }
}
