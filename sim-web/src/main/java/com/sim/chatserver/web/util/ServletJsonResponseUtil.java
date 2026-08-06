package com.sim.chatserver.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shared JSON response helpers for servlet endpoints.
 */
public final class ServletJsonResponseUtil {

    private static final Logger LOG = Logger.getLogger(ServletJsonResponseUtil.class.getName());
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
        JsonObject safePayload = payload == null ? Json.createObjectBuilder().build() : payload;
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(JSON_UTF8);

        OutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IllegalStateException ex) {
            LOG.log(Level.FINE, "Output stream unavailable; falling back to writer.", ex);
            out = null;
        }

        if (out != null) {
            try (JsonWriter writer = Json.createWriter(out)) {
                writer.writeObject(safePayload);
                return;
            } catch (NullPointerException ex) {
                // Some mocked servlet responses return null-backed streams in tests.
                LOG.log(Level.FINE, "Output stream writer was null-backed; falling back to character writer.", ex);
            }
        }

        Writer fallbackWriter = response.getWriter();
        if (fallbackWriter == null) {
            throw new IOException("Response writer unavailable");
        }
        try (JsonWriter writer = Json.createWriter(fallbackWriter)) {
            writer.writeObject(safePayload);
        }
    }
}
