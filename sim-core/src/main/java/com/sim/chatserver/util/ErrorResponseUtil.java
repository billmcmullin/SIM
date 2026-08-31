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

    private ErrorResponseUtil() {
        // util
    }

    private static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeError(resp, status, DEFAULT_ERROR_CODE, message, null);
    }

    static void writeError(HttpServletResponse resp, int status, String message, String requestId) throws IOException {
        writeError(resp, status, DEFAULT_ERROR_CODE, message, requestId);
    }

    static void writeError(HttpServletResponse resp,
            int status,
            String code,
            String message,
            String requestId) throws IOException {
        if (resp == null) {
            return;
        }

        String safeCode = trimTo(defaultIfBlank(code, DEFAULT_ERROR_CODE), MAX_CODE_CHARS);
        String safeMessage = trimTo(defaultIfBlank(message, "Unexpected error."), MAX_MESSAGE_CHARS);
        String timestamp = Instant.now().toString();

        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("status", "error")
                .add("code", safeCode)
                .add("message", safeMessage)
                .add("timestamp", timestamp);

        if (requestId != null && !requestId.isBlank()) {
            json.add("requestId", requestId.trim());
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
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

}
