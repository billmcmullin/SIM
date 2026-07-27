package com.sim.chatserver.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * Safe JSON request parsing helpers.
 */
public final class JsonRequestParserUtil {

    private static final Logger log = Logger.getLogger(JsonRequestParserUtil.class.getName());

    /**
     * Default max request body to parse (1 MiB).
     */
    private static final int DEFAULT_MAX_BODY_BYTES = 1_048_576;

    private JsonRequestParserUtil() {
        // util
    }

    /**
     * Backward-compatible parser. Returns empty object on any failure (existing
     * behavior), with better logging.
     */
    public static JsonObject parseObject(jakarta.servlet.http.HttpServletRequest req) {
        return parseObject(req, DEFAULT_MAX_BODY_BYTES);
    }

    /**
     * Safe parser with body-size guard. Returns empty object on failure.
     */
    public static JsonObject parseObject(jakarta.servlet.http.HttpServletRequest req, int maxBodyBytes) {
        if (req == null) {
            return emptyObject();
        }

        int max = Math.max(1, maxBodyBytes);
        long declaredLength = req.getContentLengthLong();
        if (declaredLength > max) {
            log.warning(() -> "JSON request body exceeds declared limit: maxBytes=" + max + ", contentLength=" + declaredLength);
            return emptyObject();
        }

        try {
            String body = readAtMost(req.getReader(), max);

            if (body.isBlank()) {
                return emptyObject();
            }

            try (JsonReader reader = Json.createReader(new StringReader(body))) {
                JsonStructure structure = reader.read();

                if (structure == null) {
                    return emptyObject();
                }

                if (structure.getValueType() != JsonValue.ValueType.OBJECT) {
                    log.warning(() -> "JSON payload is not an object. valueType=" + structure.getValueType());
                    return emptyObject();
                }

                JsonObject obj = structure.asJsonObject();
                return obj == null ? emptyObject() : obj;
            }
        } catch (BodyTooLargeException ex) {
            log.warning(() -> "JSON request body exceeds limit: " + ex.getMessage());
            return emptyObject();
        } catch (IOException | JsonException ex) {
            log.log(Level.WARNING, "Failed to parse JSON request body", ex);
            return emptyObject();
        }
    }

    public static String getString(JsonObject obj, String key, int maxChars) {
        String value = getString(obj, key, "");
        return trimTo(value, maxChars);
    }

    public static String getString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return defaultValue;
        }
        JsonValue v = obj.get(key);
        if (v == null) {
            return defaultValue;
        }

        if (v.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) v).getString();
        }

        // Non-string values converted to string safely
        return v.toString();
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return defaultValue;
        }
        try {
            JsonValue v = obj.get(key);
            if (v == null) {
                return defaultValue;
            }
            return switch (v.getValueType()) {
                case TRUE ->
                    true;
                case FALSE ->
                    false;
                case STRING ->
                    Boolean.parseBoolean(((JsonString) v).getString());
                default ->
                    defaultValue;
            };
        } catch (ClassCastException ex) {
            log.log(Level.FINE, "Invalid boolean JSON value type for key " + key, ex);
            return defaultValue;
        }
    }

    public static int getInt(JsonObject obj, String key, int defaultValue, int min, int max) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return defaultValue;
        }

        int parsed = defaultValue;
        JsonValue v = obj.get(key);
        if (v == null) {
            return clamp(defaultValue, min, max);
        }

        try {
            if (v.getValueType() == JsonValue.ValueType.NUMBER) {
                parsed = obj.getInt(key, defaultValue);
            } else if (v.getValueType() == JsonValue.ValueType.STRING) {
                String s = ((JsonString) v).getString();
                parsed = Integer.parseInt(s.trim());
            }
        } catch (NumberFormatException | ClassCastException ex) {
            log.log(Level.FINE, "Invalid integer JSON value for key " + key, ex);
            parsed = defaultValue;
        }

        return clamp(parsed, min, max);
    }

    public static JsonArray getArray(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return Json.createArrayBuilder().build();
        }
        JsonValue v = obj.get(key);
        if (v == null || v.getValueType() != JsonValue.ValueType.ARRAY) {
            return Json.createArrayBuilder().build();
        }
        JsonArray arr = obj.getJsonArray(key);
        return arr == null ? Json.createArrayBuilder().build() : arr;
    }

    public static List<JsonObject> getObjectArray(JsonObject obj, String key, int maxItems) {
        JsonArray arr = getArray(obj, key);
        int cap = Math.max(0, maxItems);

        List<JsonObject> out = new ArrayList<>();
        int count = 0;

        for (JsonValue v : arr) {
            if (count >= cap) {
                break;
            }
            if (v != null && v.getValueType() == JsonValue.ValueType.OBJECT) {
                out.add(v.asJsonObject());
                count++;
            }
        }

        return out;
    }

    private static String readAtMost(java.io.Reader reader, int maxChars) throws IOException, BodyTooLargeException {
        char[] buffer = new char[2048];
        int total = 0;
        StringBuilder payload = new StringBuilder(Math.min(maxChars, 4096));

        int read;
        while ((read = reader.read(buffer)) != -1) {
            total += read;
            if (total > maxChars) {
                throw new BodyTooLargeException("maxChars=" + maxChars + ", actual>" + maxChars);
            }
            payload.append(buffer, 0, read);
        }
        return payload.toString();
    }

    private static JsonObject emptyObject() {
        return Json.createObjectBuilder().build();
    }

    private static int clamp(int value, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        return Math.max(lo, Math.min(hi, value));
    }

    private static String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    @SuppressWarnings("serial")
    private static final class BodyTooLargeException extends IOException {

        private BodyTooLargeException(String message) {
            super(message);
        }
    }
}
