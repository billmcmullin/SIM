package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;

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

    private static final Logger LOGGER = Logger.getLogger(SelectedEntry.class.getCanonicalName());
    private static final String FIELD_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_CHAT_ID;
    private static final String FIELD_PROMPT = CommonLiteralConstants.FIELD_PROMPT;
    private static final String FIELD_RESPONSE = CommonLiteralConstants.FIELD_RESPONSE;
    private static final String FIELD_CREATED_AT = CommonLiteralConstants.FIELD_CREATED_AT;
    private static final String FIELD_SESSION_ID = CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String EMPTY = "";
    private static final String MSG_PARSE_JSON_FIELD_FAILED = "Unable to parse SelectedEntry JSON field: {0}";
    private static final String TO_STRING_PREFIX = "SelectedEntry{";
    private static final String TO_STRING_CHAT_ID = "chatId='";
    private static final String TO_STRING_CREATED_AT = ", createdAt='";
    private static final String TO_STRING_SESSION_ID = ", sessionId='";

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
        b.add(FIELD_CHAT_ID, chatId);
        b.add(FIELD_PROMPT, prompt);
        b.add(FIELD_RESPONSE, response);
        b.add(FIELD_CREATED_AT, createdAt);
        b.add(FIELD_SESSION_ID, sessionId);
        return b.build();
    }

    private static SelectedEntry fromJson(JsonObject o) {
        if (o == null) {
            return new SelectedEntry(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
        }

        return new SelectedEntry(
                str(o, FIELD_CHAT_ID),
                str(o, FIELD_PROMPT),
                str(o, FIELD_RESPONSE),
                str(o, FIELD_CREATED_AT),
                str(o, FIELD_SESSION_ID)
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
                    return o.getString(key, EMPTY);
                }
                return String.valueOf(value);
            }
        } catch (ClassCastException | IllegalStateException ex) {
            LOGGER.log(Level.FINE, MSG_PARSE_JSON_FIELD_FAILED, key);
        }
        return EMPTY;
    }

    private static String safe(String s) {
        return s == null ? EMPTY : s;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX
                + TO_STRING_CHAT_ID + chatId + '\''
                + TO_STRING_CREATED_AT + createdAt + '\''
                + TO_STRING_SESSION_ID + sessionId + '\''
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
