package com.sim.chatserver.security;

import java.util.Base64;
import java.util.Locale;
import java.util.Set;

import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Sanitizes and validates attachment payloads before forwarding upstream.
 *
 * Expected input object shape: { com.sim.chatserver.util.CommonLiteralConstants.FIELD_NAME: "file.pdf", "mime": "application/pdf",
 * "contentString": "<base64>" }
 */
public class AttachmentSanitizer {

    private static final int DEFAULT_MAX_ATTACHMENT_COUNT = 10;
    private static final int DEFAULT_MAX_NAME_CHARS = 200;
    private static final int DEFAULT_MAX_MIME_CHARS = 120;
    private static final int DEFAULT_MAX_CONTENT_CHARS = 3_000_000; // base64 chars

    private static final String UNUSED_SUPPRESSION = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String FIELD_NAME = CommonLiteralConstants.FIELD_NAME;
    private static final String FIELD_MIME = "mime";
    private static final String FIELD_CONTENT_STRING = "contentString";
    private static final String MIME_IMAGE_PREFIX = "image/";
    private static final String EMPTY = "";
    private static final String PATH_BACKSLASH = "\\";
    private static final String PATH_FORWARD_SLASH = "/";
    private static final String UNDERSCORE = "_";
    private static final String DOT_DOT = "..";
    private static final String BASE64_ALLOWED_REGEX = "^[A-Za-z0-9+/=\\r\\n]+$";
    private static final String OWASP_LOGGER = CommonLiteralConstants.LOGGER_OWASP;
    private static final String HANDLED_EXCEPTION = CommonLiteralConstants.LOG_HANDLED_EXCEPTION;

    private static final Set<String> ALLOWED_EXACT_MIME = Set.of(
            "application/anythingllm-document",
            "application/pdf",
            "text/plain"
    );

    private final int maxAttachmentCount;
    private final int maxNameChars;
    private final int maxMimeChars;
    private final int maxContentChars;
    private final boolean validateBase64Payload;

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    AttachmentSanitizer() {
        this(
                DEFAULT_MAX_ATTACHMENT_COUNT,
                DEFAULT_MAX_NAME_CHARS,
                DEFAULT_MAX_MIME_CHARS,
                DEFAULT_MAX_CONTENT_CHARS,
                true
        );
    }

    AttachmentSanitizer(int maxAttachmentCount, int maxNameChars, int maxMimeChars, int maxContentChars) {
        this(maxAttachmentCount, maxNameChars, maxMimeChars, maxContentChars, true);
    }

    private AttachmentSanitizer(int maxAttachmentCount,
            int maxNameChars,
            int maxMimeChars,
            int maxContentChars,
            boolean validateBase64Payload) {
        this.maxAttachmentCount = Math.max(1, maxAttachmentCount);
        this.maxNameChars = Math.max(20, maxNameChars);
        this.maxMimeChars = Math.max(20, maxMimeChars);
        this.maxContentChars = Math.max(1024, maxContentChars);
        this.validateBase64Payload = validateBase64Payload;
    }

    final JsonArray sanitize(JsonArray rawAttachments) {
        if (rawAttachments == null || rawAttachments.isEmpty()) {
            return Json.createArrayBuilder().build();
        }

        JsonArrayBuilder out = Json.createArrayBuilder();
        int accepted = 0;

        for (JsonValue value : rawAttachments) {
            if (accepted >= maxAttachmentCount) {
                break;
            }

            if (value == null || value.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            JsonObject obj = value.asJsonObject();

            String name = sanitizeName(trimTo(str(obj, FIELD_NAME), maxNameChars));
            String mime = normalizeMime(trimTo(str(obj, FIELD_MIME), maxMimeChars));
            String contentString = trimTo(str(obj, FIELD_CONTENT_STRING), maxContentChars);

            if (name.isBlank() || mime.isBlank() || contentString.isBlank()) {
                continue;
            }

            if (!isAllowedMime(mime)) {
                continue;
            }

            if (validateBase64Payload && !isValidBase64(contentString)) {
                continue;
            }

            JsonObject clean = Json.createObjectBuilder()
            .add(FIELD_NAME, name)
            .add(FIELD_MIME, mime)
            .add(FIELD_CONTENT_STRING, contentString)
                    .build();

            out.add(clean);
            accepted++;
        }

        return out.build();
    }

    private boolean isAllowedMime(String mime) {
        if (mime == null || mime.isBlank()) {
            return false;
        }
        if (mime.startsWith(MIME_IMAGE_PREFIX)) {
            return true;
        }
        return ALLOWED_EXACT_MIME.contains(mime);
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return EMPTY;
        }
        // strip path traversal / separators
        String n = name.replace(PATH_BACKSLASH, UNDERSCORE).replace(PATH_FORWARD_SLASH, UNDERSCORE);
        n = n.replace(DOT_DOT, UNDERSCORE);
        return n.trim();
    }

    private String normalizeMime(String mime) {
        if (mime == null) {
            return EMPTY;
        }
        return mime.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isValidBase64(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        // quick character sanity check (base64 charset + padding)
        String v = value.trim();
        if (!v.matches(BASE64_ALLOWED_REGEX)) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(v);
            return decoded != null;
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(OWASP_LOGGER).log(java.util.logging.Level.FINE, HANDLED_EXCEPTION, ex);
            return false;
        }
    }

    private String str(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return EMPTY;
        }
        JsonValue v = obj.get(key);
        if (v == null) {
            return EMPTY;
        }
        if (v.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) v).getString();
        }
        return v.toString();
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return EMPTY;
        }
        String s = value.trim();
        return s.length() <= maxChars ? s : new String(s.toCharArray(), 0, maxChars);
    }
}
