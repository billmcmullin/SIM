package com.sim.chatserver.web.util;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;

public final class JsonPrimaryTextUtil {

    private JsonPrimaryTextUtil() {
    }

    public static String extractPrimaryText(String body, Logger log, String parseFailureMessage) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject object = reader.readObject();
            for (String key : new String[]{"textResponse", "response", "message", "answer", "output"}) {
                String value = object.getString(key, "");
                if (!value.isBlank()) {
                    return value;
                }
            }
            return body;
        } catch (JsonException | ClassCastException ex) {
            if (log != null) {
                log.log(Level.FINE, parseFailureMessage == null ? "Unable to parse primary text response as JSON" : parseFailureMessage, ex);
            }
            return body;
        }
    }
}