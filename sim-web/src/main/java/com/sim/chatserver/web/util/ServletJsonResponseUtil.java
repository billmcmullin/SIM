package com.sim.chatserver.web.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shared JSON response helpers for servlet endpoints.
 */
public final class ServletJsonResponseUtil {

    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    private ServletJsonResponseUtil() {
        // utility
    }

    public static void writeError(HttpServletResponse response,
            int status,
            String message) throws IOException {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(response, status, payload);
    }

    public static void writeJson(HttpServletResponse response,
            int status,
            JsonObject payload) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(JSON_UTF8);
        try (JsonWriter writer = Json.createWriter(response.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
    }
}
