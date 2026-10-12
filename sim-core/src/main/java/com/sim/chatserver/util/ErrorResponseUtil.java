package com.sim.chatserver.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Standardized JSON error response writer.
 */
public final class ErrorResponseUtil {

    private static final int MAX_MESSAGE_CHARS = 500;
    private static final int MAX_CODE_CHARS = 80;
    private static final String DEFAULT_ERROR_CODE = "internal_error";
    private static final String DEFAULT_ERROR_MESSAGE = "Unexpected error.";
    private static final String RESPONSE_CONTENT_TYPE = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8;
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String HEADER_CACHE_CONTROL_VALUE = "no-store";
    private static final String HEADER_PRAGMA = "Pragma";
    private static final String HEADER_PRAGMA_VALUE = "no-cache";
    private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String HEADER_X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff";
    private static final String JSON_KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String JSON_KEY_CODE = "code";
    private static final String JSON_KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_KEY_TIMESTAMP = "timestamp";
    private static final String JSON_KEY_REQUEST_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String JSON_VALUE_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String EMPTY = "";

    private ErrorResponseUtil() {
        // util
    }

    private static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeError(resp, status, DEFAULT_ERROR_CODE, message, null);
    }

    static void writeError(HttpServletResponse resp, int status, String message, String requestId) throws IOException {
        writeError(resp, status, DEFAULT_ERROR_CODE, message, requestId);
    }

    private static void writeError(HttpServletResponse resp,
            int status,
            String code,
            String message,
            String requestId) throws IOException {
        if (resp == null) {
            return;
        }

        String safeCode = trimTo(defaultIfBlank(code, DEFAULT_ERROR_CODE), MAX_CODE_CHARS);
        String safeMessage = trimTo(defaultIfBlank(message, DEFAULT_ERROR_MESSAGE), MAX_MESSAGE_CHARS);
        String timestamp = Instant.now().toString();

        resp.setStatus(status);
        resp.setContentType(RESPONSE_CONTENT_TYPE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE);
        resp.setHeader(HEADER_PRAGMA, HEADER_PRAGMA_VALUE);
        resp.setHeader(HEADER_X_CONTENT_TYPE_OPTIONS, HEADER_X_CONTENT_TYPE_OPTIONS_VALUE);

        JsonObjectBuilder json = Json.createObjectBuilder()
                .add(JSON_KEY_STATUS, JSON_VALUE_ERROR)
                .add(JSON_KEY_CODE, safeCode)
                .add(JSON_KEY_MESSAGE, safeMessage)
                .add(JSON_KEY_TIMESTAMP, timestamp);

        if (requestId != null && !requestId.isBlank()) {
            json.add(JSON_KEY_REQUEST_ID, requestId.trim());
        }

        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(json.build());
        }
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return EMPTY;
        }
        return value.length() <= maxChars ? value : new String(value.toCharArray(), 0, maxChars);
    }

}
