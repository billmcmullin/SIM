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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.ServerDiagnosticsLog;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
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
    private static final String VERBOSE_WILDFLY_ENV = "SIM_WORKSPACECLIENT_VERBOSE_WILDFLY_LOG";
    private static final boolean VERBOSE_WILDFLY_LOGS = isTruthy(System.getenv(VERBOSE_WILDFLY_ENV));

    private final HttpClient httpClient;
    private final int maxRetries;
    private final Duration requestTimeout;

    private enum AuthHeaderMode {
        CUSTOM_HEADER,
        AUTH_BEARER,
        AUTH_RAW,
        X_API_KEY,
        AUTH_BEARER_AND_X_API_KEY
    }

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
        final URI targetUri = toSafeHttpUri(targetUrl);
        final String safeRequestId = safe(requestId);
        final ApiAuthResolver.ResolvedApiAuth primaryAuth = ApiAuthResolver.resolveForOutbound(apiKey);
        final ApiAuthResolver.ResolvedApiAuth secondaryAuth = ApiAuthResolver.resolveForOutbound(null);
        final List<ApiAuthResolver.ResolvedApiAuth> authCandidates = buildAuthCandidates(primaryAuth, secondaryAuth);

        if (authCandidates.isEmpty()) {
            throw new IOException("Workspace API key is required.");
        }

        JsonObject payload = buildPayload(safeMessage, safeMode, sessionId, reset, safeAttachments);
        String body = payload.toString();

        int attempt = 0;
        while (true) {
            attempt++;

            try {
                if (attempt == 1) {
                    if (VERBOSE_WILDFLY_LOGS) {
                        log.log(Level.INFO,
                            "[workspace-client][{0}] send mode={1} reset={2} messageChars={3} payloadChars={4} attachments={5} target={6} messagePreview={7}",
                            new Object[]{
                                safeRequestId,
                                safeMode,
                                    reset,
                                    safeMessage.length(),
                                    body.length(),
                                    safeAttachments.size(),
                                safeTarget(targetUrl),
                                truncateOneLine(safeMessage, MAX_LOG_MESSAGE_PREVIEW_CHARS)
                            });
                    } else {
                        log.log(Level.INFO,
                            "[workspace-client][{0}] send mode={1} reset={2} messageChars={3} payloadChars={4} attachments={5} target={6}",
                            new Object[]{
                                safeRequestId,
                                safeMode,
                                    reset,
                                    safeMessage.length(),
                                    body.length(),
                                    safeAttachments.size(),
                                safeTarget(targetUrl)
                            });
                    }

                    ServerDiagnosticsLog.write(
                        "workspace-client",
                            safeRequestId,
                        "send",
                        "target=" + targetUri
                            + "\nmode=" + safeMode
                            + "\nreset=" + reset
                            + "\nmessageChars=" + safeMessage.length()
                            + "\npayloadChars=" + body.length()
                            + "\nattachments=" + safeAttachments.size()
                            + "\nauthCandidates=" + summarizeAuthCandidates(authCandidates)
                            + "\nmessage=" + safeMessage
                            + "\npayload=" + body
                    );
                }

                HttpResponse<String> response = null;
                AuthHeaderMode usedMode = AuthHeaderMode.AUTH_BEARER;
                String usedAuthSource = "";

                for (int authIndex = 0; authIndex < authCandidates.size(); authIndex++) {
                    ApiAuthResolver.ResolvedApiAuth auth = authCandidates.get(authIndex);
                    AuthHeaderMode authModeCandidate = resolvePrimaryAuthMode(auth.preferredHeaderName());
                    HttpRequest request = buildRequest(targetUri, body, auth, authModeCandidate);
                    HttpResponse<String> candidate = httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                    );
                    int candidateStatus = candidate.statusCode();

                    boolean hasMoreAuth = authIndex < (authCandidates.size() - 1);
                    if ((candidateStatus == 401 || candidateStatus == 403) && hasMoreAuth) {
                        log.log(Level.INFO,
                                "[workspace-client][{0}] auth fallback switching source after status={1} source={2}",
                                new Object[]{safeRequestId, candidateStatus, safe(auth.source())});
                        continue;
                    }

                    response = candidate;
                    usedMode = authModeCandidate;
                    usedAuthSource = safe(auth.source());
                    break;
                }

                if (response == null) {
                    throw new IOException("Workspace call failed before receiving a response.");
                }

                int status = response.statusCode();
                String responseBody = response.body() == null ? "" : response.body();
                String contentType = response.headers().firstValue("Content-Type").orElse("");

                WorkspaceResponse wrapped = new WorkspaceResponse(status, responseBody, contentType);

                if (status >= 400 && status < 500) {
                    if (VERBOSE_WILDFLY_LOGS) {
                        log.log(Level.WARNING,
                            "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3} body={4}",
                            new Object[]{
                                safeRequestId,
                                    status,
                                    attempt,
                                    isLikelyContextTooLarge(wrapped),
                                truncate(responseBody, MAX_ERROR_LOG_CHARS)
                            });
                    } else {
                        log.log(Level.WARNING,
                            "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3}",
                            new Object[]{
                                safeRequestId,
                                    status,
                                    attempt,
                                    isLikelyContextTooLarge(wrapped)
                            });
                    }

                    ServerDiagnosticsLog.write(
                        "workspace-client",
                            safeRequestId,
                        "upstream-4xx",
                        "status=" + status
                            + "\nattempt=" + attempt
                            + "\nauthSource=" + usedAuthSource
                            + "\nauthMode=" + usedMode.name()
                            + "\ncontextTooLarge=" + isLikelyContextTooLarge(wrapped)
                            + "\ncontentType=" + contentType
                            + "\nresponseBody=" + responseBody
                    );
                }

                boolean retryable = isRetryableStatus(status);
                if (retryable && attempt <= (maxRetries + 1)) {
                    log.log(Level.WARNING,
                        "[workspace-client][{0}] retryable status status={1} attempt={2}",
                            new Object[]{safeRequestId, status, attempt});

                    ServerDiagnosticsLog.write(
                        "workspace-client",
                        safeRequestId,
                        "retryable-status",
                        "status=" + status
                            + "\nattempt=" + attempt
                            + "\ncontentType=" + contentType
                            + "\nresponseBody=" + responseBody
                    );
                    continue;
                }

                return wrapped;
            } catch (IOException ex) {
                if (attempt <= (maxRetries + 1)) {
                    if (VERBOSE_WILDFLY_LOGS) {
                        log.log(Level.WARNING,
                            "[workspace-client][" + safeRequestId + "] network failure, retrying attempt=" + attempt,
                                ex);
                    } else {
                        log.log(Level.WARNING,
                            "[workspace-client][{0}] network failure, retrying attempt={1} reason={2}",
                            new Object[]{
                                safeRequestId,
                                    attempt,
                                truncateOneLine(safe(ex.getMessage()), 220)
                            });
                    }

                    ServerDiagnosticsLog.write(
                            "workspace-client",
                            safeRequestId,
                            "network-failure",
                            "attempt=" + attempt
                                    + "\nreason=" + safe(ex.getMessage()),
                            ex
                    );
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

    private AuthHeaderMode resolvePrimaryAuthMode(String preferredHeaderName) {
        String header = preferredHeaderName == null ? "" : preferredHeaderName.trim();
        if ("x-api-key".equalsIgnoreCase(header)) {
            return AuthHeaderMode.X_API_KEY;
        }
        if (!header.isBlank() && !"authorization".equalsIgnoreCase(header)) {
            return AuthHeaderMode.CUSTOM_HEADER;
        }
        return AuthHeaderMode.AUTH_BEARER;
    }

    private List<ApiAuthResolver.ResolvedApiAuth> buildAuthCandidates(
            ApiAuthResolver.ResolvedApiAuth primary,
            ApiAuthResolver.ResolvedApiAuth secondary
    ) {
        Map<String, ApiAuthResolver.ResolvedApiAuth> unique = new LinkedHashMap<>();
        for (ApiAuthResolver.ResolvedApiAuth candidate : Arrays.asList(primary, secondary)) {
            if (candidate == null || !candidate.hasToken()) {
                continue;
            }
            String token = ApiAuthResolver.normalizeApiKeyToken(candidate.rawValue());
            if (token == null || token.isBlank()) {
                continue;
            }
            String key = token + "|" + safe(candidate.preferredHeaderName());
            unique.putIfAbsent(key, candidate);
        }
        return new ArrayList<>(unique.values());
    }

    private String summarizeAuthCandidates(List<ApiAuthResolver.ResolvedApiAuth> authCandidates) {
        if (authCandidates == null || authCandidates.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ApiAuthResolver.ResolvedApiAuth auth : authCandidates) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(safe(auth.source()))
                    .append('|')
                    .append(safe(auth.preferredHeaderName()));
        }
        return sb.toString();
    }

    private HttpRequest buildRequest(URI targetUri, String body, ApiAuthResolver.ResolvedApiAuth auth, AuthHeaderMode mode) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(targetUri)
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        String token = auth == null ? null : auth.token();
        String rawValue = auth == null ? null : auth.rawValue();
        String preferredHeader = auth == null ? null : auth.preferredHeaderName();

        if (token == null || token.isBlank()) {
            return requestBuilder.build();
        }

        switch (mode) {
            case CUSTOM_HEADER -> applyCustomHeader(requestBuilder, preferredHeader, rawValue, token);
            case AUTH_RAW -> requestBuilder.header("Authorization", normalizeRawAuthorizationValue(rawValue, token));
            case X_API_KEY -> requestBuilder.header("X-API-Key", token);
            case AUTH_BEARER_AND_X_API_KEY -> requestBuilder
                    .header("Authorization", "Bearer " + token)
                    .header("X-API-Key", token);
            case AUTH_BEARER -> requestBuilder.header("Authorization", "Bearer " + token);
        }

        return requestBuilder.build();
    }

    private void applyCustomHeader(HttpRequest.Builder requestBuilder, String headerName, String rawValue, String token) {
        String normalizedHeader = headerName == null ? "" : headerName.trim();
        if (normalizedHeader.isBlank()) {
            return;
        }

        if ("authorization".equalsIgnoreCase(normalizedHeader)) {
            requestBuilder.header("Authorization", "Bearer " + token);
            return;
        }
        if ("x-api-key".equalsIgnoreCase(normalizedHeader)) {
            requestBuilder.header("X-API-Key", token);
            return;
        }

        String headerValue = rawValue;
        if (headerValue == null || headerValue.isBlank()) {
            headerValue = token;
        }
        requestBuilder.header(normalizedHeader, headerValue);
    }

    private String normalizeRawAuthorizationValue(String rawValue, String token) {
        String raw = ApiAuthResolver.stripAuthorizationPrefix(rawValue);
        if (raw == null || raw.isBlank()) {
            return token;
        }
        return raw;
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
        } catch (JsonException | ClassCastException | IllegalStateException ignored) {
            return null;
        }
    }

    private URI toSafeHttpUri(String targetUrl) throws IOException {
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IOException("Workspace target URL is required.");
        }
        try {
            URI uri = URI.create(targetUrl.trim()).normalize();
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            if ((!"http".equals(scheme) && !"https".equals(scheme)) || host == null || host.isBlank()) {
                throw new IOException("Workspace target URL must be http/https with a host.");
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new IOException("Workspace target URL is invalid.", ex);
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
        } catch (IllegalArgumentException e) {
            return "(invalid-target)";
        }
    }

    private static boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "on".equals(normalized);
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
