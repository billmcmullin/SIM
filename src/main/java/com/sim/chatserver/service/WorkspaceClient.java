// src/main/java/com/sim/chatserver/service/WorkspaceClient.java
package com.sim.chatserver.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Centralized AnythingLLM workspace HTTP client.
 *
 * Security: - does not log API keys - caps error-body logs
 *
 * Reliability: - request timeout - simple retry for transient failures (5xx /
 * network)
 *
 * Context-limit detection: - robust status + body + JSON field matching -
 * designed to drive adaptive rebatching in orchestrator
 *
 * IMPORTANT: - This client sends the full message it receives. - It does not
 * trim message/context payload. - If payload is too large, caller must
 * rebatch/retry with smaller context.
 */
public class WorkspaceClient {

    private static final Logger log = Logger.getLogger(WorkspaceClient.class.getName());

    private static final int DEFAULT_MAX_RETRIES = 1;
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final int MAX_ERROR_LOG_CHARS = 2000;
    private static final int MAX_LOG_MESSAGE_PREVIEW_CHARS = 800;

    private final HttpClient httpClient;
    private final int maxRetries;
    private final Duration requestTimeout;

    public WorkspaceClient() {
        this(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build(), DEFAULT_MAX_RETRIES, DEFAULT_REQUEST_TIMEOUT);
    }

    public WorkspaceClient(HttpClient httpClient, int maxRetries, Duration requestTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.maxRetries = Math.max(0, maxRetries);
        this.requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
    }

    public WorkspaceResponse sendChat(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId
    ) throws IOException, InterruptedException {

        // FULL message as-is (no truncation here)
        final String safeMessage = message == null ? "" : message;
        final String safeMode = (mode == null || mode.isBlank()) ? "chat" : mode;
        final JsonArray safeAttachments = attachments == null ? Json.createArrayBuilder().build() : attachments;

        JsonObject payload = buildPayload(safeMessage, safeMode, sessionId, reset, safeAttachments);
        String body = payload.toString();

        int attempt = 0;
        while (true) {
            attempt++;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            try {
                if (attempt == 1) {
                    log.info("[workspace-client][" + safe(requestId) + "] send"
                            + " mode=" + safeMode
                            + " reset=" + reset
                            + " messageChars=" + safeMessage.length()
                            + " payloadChars=" + body.length()
                            + " attachments=" + safeAttachments.size()
                            + " target=" + safeTarget(targetUrl)
                            + " messagePreview=" + truncateOneLine(safeMessage, MAX_LOG_MESSAGE_PREVIEW_CHARS));
                }

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                );

                int status = response.statusCode();
                String responseBody = response.body() == null ? "" : response.body();
                String contentType = response.headers().firstValue("Content-Type").orElse("");

                WorkspaceResponse wrapped = new WorkspaceResponse(status, responseBody, contentType);

                if (status >= 400 && status < 500) {
                    log.warning("[workspace-client][" + safe(requestId) + "] upstream 4xx"
                            + " status=" + status
                            + " attempt=" + attempt
                            + " contextTooLarge=" + isLikelyContextTooLarge(wrapped)
                            + " body=" + truncate(responseBody, MAX_ERROR_LOG_CHARS));
                }

                boolean retryable = isRetryableStatus(status);
                if (retryable && attempt <= (maxRetries + 1)) {
                    log.warning("[workspace-client][" + safe(requestId) + "] retryable status"
                            + " status=" + status
                            + " attempt=" + attempt);
                    continue;
                }

                return wrapped;
            } catch (IOException ex) {
                if (attempt <= (maxRetries + 1)) {
                    log.log(Level.WARNING, "[workspace-client][" + safe(requestId) + "] network failure, retrying attempt=" + attempt, ex);
                    continue;
                }
                throw ex;
            }
        }
    }

    /**
     * Robust detection for context/size/token overflow style upstream failures.
     *
     * Signals checked: - HTTP 413 directly - textual body patterns (plain text
     * or JSON text) - common JSON error fields: message, error, detail, code,
     * type
     */
    public boolean isLikelyContextTooLarge(WorkspaceResponse response) {
        if (response == null) {
            return false;
        }

        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body();
        String lower = body.toLowerCase(Locale.ROOT);

        if (status == 413) {
            return true;
        }

        if (containsContextLimitPhrase(lower)) {
            return true;
        }

        JsonObject json = parseJsonObject(body);
        if (json != null) {
            String merged = (safeLower(json.getString("message", "")) + " "
                    + safeLower(json.getString("error", "")) + " "
                    + safeLower(json.getString("detail", "")) + " "
                    + safeLower(json.getString("code", "")) + " "
                    + safeLower(json.getString("type", "")) + " "
                    + safeLower(json.getString("reason", ""))).trim();

            if (containsContextLimitPhrase(merged)) {
                return true;
            }
        }

        return false;
    }

    private JsonObject buildPayload(
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments
    ) {
        var b = Json.createObjectBuilder()
                .add("message", message == null ? "" : message)
                .add("mode", (mode == null || mode.isBlank()) ? "chat" : mode)
                .add("reset", reset);

        if (sessionId != null && !sessionId.isBlank()) {
            b.add("sessionId", sessionId);
        }
        if (attachments != null && !attachments.isEmpty()) {
            b.add("attachments", attachments);
        }

        return b.build();
    }

    private boolean isRetryableStatus(int status) {
        return status == 429 || (status >= 500 && status <= 599);
    }

    private boolean containsContextLimitPhrase(String lower) {
        if (lower == null || lower.isBlank()) {
            return false;
        }

        return lower.contains("maximum context length")
                || lower.contains("context length exceeded")
                || lower.contains("context window")
                || lower.contains("token limit")
                || lower.contains("too many tokens")
                || lower.contains("max tokens")
                || lower.contains("prompt is too long")
                || lower.contains("input is too long")
                || lower.contains("payload too large")
                || lower.contains("request entity too large")
                || lower.contains("failed_to_embed")
                || lower.contains("embedding input too long")
                || lower.contains("too large");
    }

    private JsonObject parseJsonObject(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safeLower(String v) {
        return v == null ? "" : v.toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...(truncated)";
    }

    private String truncateOneLine(String value, int max) {
        if (value == null) {
            return "";
        }
        String oneLine = value.replace('\n', ' ').replace('\r', ' ');
        if (oneLine.length() <= max) {
            return oneLine;
        }
        return oneLine.substring(0, max) + "...(truncated)";
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String safeTarget(String targetUrl) {
        try {
            URI u = URI.create(targetUrl);
            String host = u.getHost() == null ? "" : u.getHost();
            String path = u.getPath() == null ? "" : u.getPath();
            return host + path;
        } catch (Exception e) {
            return "(invalid-target)";
        }
    }

    /**
     * Lightweight response DTO.
     */
    public static final class WorkspaceResponse {

        private final int statusCode;
        private final String body;
        private final String contentType;

        public WorkspaceResponse(int statusCode, String body, String contentType) {
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
            this.contentType = contentType == null ? "" : contentType;
        }

        public int statusCode() {
            return statusCode;
        }

        public String body() {
            return body;
        }

        public String contentType() {
            return contentType;
        }

        public boolean isError() {
            return statusCode >= 400;
        }
    }
}
