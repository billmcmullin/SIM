package com.sim.chatserver.web.util;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;

public final class JsonPrimaryTextUtil {

    private static final String EMPTY = "";
    private static final String MSG_PARSE_PRIMARY_TEXT_JSON_FAILED = "Unable to parse primary text response as JSON";

    private static final String[] PRIMARY_TEXT_KEYS = new String[]{com.sim.chatserver.util.CommonLiteralConstants.FIELD_TEXT_RESPONSE, com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE, com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "answer", "output"};

    private JsonPrimaryTextUtil() {
    }

    public static String extractPrimaryText(String body, Logger log, String parseFailureMessage) {
        if (body == null || body.isBlank()) {
            return EMPTY;
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject object = reader.readObject();
            for (String key : PRIMARY_TEXT_KEYS) {
                String value = object.getString(key, EMPTY);
                if (!value.isBlank()) {
                    return value;
                }
            }
            return body;
        } catch (JsonException | ClassCastException ex) {
            if (log != null) {
                log.log(Level.FINE, parseFailureMessage == null ? MSG_PARSE_PRIMARY_TEXT_JSON_FAILED : parseFailureMessage, ex);
            }
            return body;
        }
    }
}