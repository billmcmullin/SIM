package com.sim.chatserver.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shared JSON response helpers for servlet endpoints.
 */
public final class ServletJsonResponseUtil {

    private static final String EMPTY = "";

    private static final Logger LOG = Logger.getLogger(ServletJsonResponseUtil.class.getCanonicalName());
    private static final String JSON_UTF8 = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8;
    private static final String KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String KEY_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String LOG_OUTPUT_STREAM_UNAVAILABLE = "Output stream unavailable; falling back to writer.";
    private static final String LOG_OUTPUT_STREAM_NULL_BACKED =
            "Output stream writer was null-backed; falling back to character writer.";
    private static final String MSG_RESPONSE_WRITER_UNAVAILABLE = "Response writer unavailable";

    private ServletJsonResponseUtil() {
        // utility
    }

    public static void writeError(HttpServletResponse response,
            int status,
            String message) throws IOException {
        JsonObject payload = Json.createObjectBuilder()
            .add(KEY_STATUS, KEY_ERROR)
            .add(KEY_MESSAGE, message == null ? EMPTY : message)
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
            LOG.log(Level.FINE, LOG_OUTPUT_STREAM_UNAVAILABLE, ex);
            out = null;
        }

        if (out != null) {
            try (JsonWriter writer = Json.createWriter(out)) {
                writer.writeObject(safePayload);
                return;
            } catch (NullPointerException ex) {
                // Some mocked servlet responses return null-backed streams in tests.
                LOG.log(Level.FINE, LOG_OUTPUT_STREAM_NULL_BACKED, ex);
            }
        }

        Writer fallbackWriter = response.getWriter();
        if (fallbackWriter == null) {
            throw new IOException(MSG_RESPONSE_WRITER_UNAVAILABLE);
        }
        try (JsonWriter writer = Json.createWriter(fallbackWriter)) {
            writer.writeObject(safePayload);
        }
    }
}