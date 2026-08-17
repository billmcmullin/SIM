// src/main/java/com/sim/chatserver/service/WorkspaceClient.java
package com.sim.chatserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

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
    private static final boolean VERBOSE_WILDFLY_LOGS = isTruthy(readEnvSanitized(VERBOSE_WILDFLY_ENV));

    private final HttpClient httpClient;
    private final int maxRetries;
    private final Duration requestTimeout;
    private static final JsonObject EMPTY_JSON_OBJECT = Json.createObjectBuilder().build();

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

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
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
        return sendChatInternal(targetUrl, apiKey, message, mode, sessionId, reset, attachments, requestId, false);
        }

        /**
         * Compatibility mode for endpoints that require exactly Authorization: Bearer
         * semantics (matching successful curl/test-connection behavior).
         */
        public WorkspaceResponse sendChatBearerCompat(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId
        ) throws IOException, InterruptedException {
        return sendChatInternal(targetUrl, apiKey, message, mode, sessionId, reset, attachments, requestId, true);
        }

        private WorkspaceResponse sendChatInternal(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId,
            boolean bearerCompatOnly
        ) throws IOException, InterruptedException {

        // FULL message as-is (no truncation here)
        final String safeMessage = message == null ? "" : message;
        final String safeMode = (mode == null || mode.isBlank()) ? "chat" : mode;
        final JsonArray safeAttachments = attachments == null ? Json.createArrayBuilder().build() : attachments;
        final URI targetUri = toSafeHttpUri(targetUrl);
        final String safeRequestId = safe(requestId);
        final boolean hasExplicitApiKey = ApiAuthResolver.normalizeApiKeyToken(apiKey) != null;
        final ApiAuthResolver.ResolvedApiAuth primaryAuth = hasExplicitApiKey
            ? ApiAuthResolver.resolveForServerConfigOutbound(apiKey)
            : ApiAuthResolver.resolveForOutbound(apiKey);
        final ApiAuthResolver.ResolvedApiAuth secondaryAuth = hasExplicitApiKey
            ? ApiAuthResolver.emptyResolvedApiAuth()
            : ApiAuthResolver.resolveForOutbound(null);
        final List<ApiAuthResolver.ResolvedApiAuth> authCandidates = buildAuthCandidates(primaryAuth, secondaryAuth);

        if (authCandidates.isEmpty()) {
            throw new IOException("Workspace API key is required.");
        }

        boolean includeSessionId = shouldIncludeSessionId(sessionId);
        JsonObject payload = buildPayload(safeMessage, safeMode, sessionId, includeSessionId, reset, safeAttachments);
        String body = payload.toString();
        StringBuilder authAttemptTrace = new StringBuilder();

        int attempt = 0;
        while (true) {
            attempt++;
            authAttemptTrace.setLength(0);

            try {
                if (attempt == 1) {
                    logInitialSendAttempt(
                            safeRequestId,
                            safeMode,
                            reset,
                            safeMessage,
                            safeAttachments,
                            targetUrl,
                            targetUri,
                            includeSessionId,
                            authCandidates,
                            body
                    );
                }

                HttpResponse<String> response = null;
                AuthHeaderMode usedMode = AuthHeaderMode.AUTH_BEARER;
                String usedAuthSource = "";

                if (bearerCompatOnly) {
                    ApiAuthResolver.ResolvedApiAuth auth = authCandidates.get(0);
                    usedAuthSource = safe(auth.source());
                    usedMode = AuthHeaderMode.AUTH_BEARER;
                    authAttemptTrace.append(usedAuthSource)
                            .append('|')
                            .append(usedMode.name());

                    WorkspaceResponse wrapped = sendBearerCompatViaHttpURLConnection(targetUri, body, auth);
                    int status = wrapped.statusCode();
                    String responseBody = wrapped.body() == null ? "" : wrapped.body();
                    String contentType = wrapped.contentType() == null ? "" : wrapped.contentType();

                    authAttemptTrace.append("=>").append(status);

                    logUpstream4xxIfNeeded(
                            safeRequestId,
                            status,
                            attempt,
                            wrapped,
                            usedAuthSource,
                            usedMode,
                            authAttemptTrace.toString(),
                            contentType,
                            responseBody
                    );

                    if (shouldRetryStatus(status, attempt, safeRequestId, contentType, responseBody)) {
                        continue;
                    }

                    return wrapped;
                }

                for (int authIndex = 0; authIndex < authCandidates.size(); authIndex++) {
                    ApiAuthResolver.ResolvedApiAuth auth = authCandidates.get(authIndex);
                        List<AuthHeaderMode> modeCandidates = bearerCompatOnly
                            ? List.of(AuthHeaderMode.AUTH_BEARER)
                            : resolveModeCandidates(auth.preferredHeaderName());

                    for (int modeIndex = 0; modeIndex < modeCandidates.size(); modeIndex++) {
                        AuthHeaderMode authModeCandidate = modeCandidates.get(modeIndex);
                        HttpRequest request = buildRequest(targetUri, body, auth, authModeCandidate);
                        HttpResponse<String> candidate = httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                        );
                        int candidateStatus = candidate.statusCode();

                        if (authAttemptTrace.length() > 0) {
                            authAttemptTrace.append(", ");
                        }
                        authAttemptTrace.append(safe(auth.source()))
                            .append('|')
                            .append(authModeCandidate.name())
                            .append("=>")
                            .append(candidateStatus);

                        response = candidate;
                        usedMode = authModeCandidate;
                        usedAuthSource = safe(auth.source());

                        if (candidateStatus >= 200 && candidateStatus < 300) {
                            break;
                        }

                        boolean hasMoreModes = modeIndex < (modeCandidates.size() - 1);
                        boolean hasMoreAuth = authIndex < (authCandidates.size() - 1);

                        if (isAuthModeRetryableStatus(candidateStatus) && (hasMoreModes || hasMoreAuth)) {
                            if (hasMoreModes) {
                                log.log(Level.INFO,
                                        "[workspace-client][{0}] auth fallback switching mode after status={1} mode={2} source={3}",
                                        new Object[]{safeRequestId, candidateStatus, authModeCandidate.name(), safe(auth.source())});
                                continue;
                            }
                            if (hasMoreAuth) {
                                log.log(Level.INFO,
                                        "[workspace-client][{0}] auth fallback switching source after status={1} source={2}",
                                        new Object[]{safeRequestId, candidateStatus, safe(auth.source())});
                                break;
                            }
                        }

                        break;
                    }

                    if (response != null && response.statusCode() >= 200 && response.statusCode() < 300) {
                        break;
                    }
                }

                if (response == null) {
                    throw new IOException("Workspace call failed before receiving a response.");
                }

                int status = response.statusCode();
                String responseBody = response.body() == null ? "" : response.body();
                String contentType = response.headers().firstValue("Content-Type").orElse("");

                WorkspaceResponse wrapped = new WorkspaceResponse(status, responseBody, contentType);

                logUpstream4xxIfNeeded(
                        safeRequestId,
                        status,
                        attempt,
                        wrapped,
                        usedAuthSource,
                        usedMode,
                        authAttemptTrace.toString(),
                        contentType,
                        responseBody
                );

                if (shouldRetryStatus(status, attempt, safeRequestId, contentType, responseBody)) {
                    continue;
                }

                return wrapped;
            } catch (IOException ex) {
                if (attempt <= (maxRetries + 1)) {
                    String errorRef = UUID.randomUUID().toString();
                    log.log(Level.WARNING,
                            "[workspace-client][{0}] network failure, retrying attempt={1} errorRef={2} reason={3}",
                            new Object[]{
                                safeRequestId,
                                attempt,
                                errorRef,
                                truncateOneLine(safe(ex.getMessage()), 220)
                            });

                    ServerDiagnosticsLog.write(
                            "workspace-client",
                            safeRequestId,
                            "network-failure",
                            "attempt=" + attempt
                                    + "\nerrorRef=" + errorRef
                                    + "\nreason=" + safe(ex.getMessage()),
                            ex
                    );
                    continue;
                }
                throw ex;
            }
        }
    }

    private void logInitialSendAttempt(
            String safeRequestId,
            String safeMode,
            boolean reset,
            String safeMessage,
            JsonArray safeAttachments,
            String targetUrl,
            URI targetUri,
            boolean includeSessionId,
            List<ApiAuthResolver.ResolvedApiAuth> authCandidates,
            String body
    ) {
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
                    + "\nsessionIdIncluded=" + includeSessionId
                    + "\nmessageChars=" + safeMessage.length()
                    + "\npayloadChars=" + body.length()
                    + "\nattachments=" + safeAttachments.size()
                    + "\nauthCandidates=" + summarizeAuthCandidates(authCandidates)
                    + "\nmessage=" + safeMessage
                    + "\npayload=" + body
        );
    }

    private void logUpstream4xxIfNeeded(
            String safeRequestId,
            int status,
            int attempt,
            WorkspaceResponse response,
            String authSource,
            AuthHeaderMode authMode,
            String authAttemptTrace,
            String contentType,
            String responseBody
    ) {
        if (status < 400 || status >= 500 || response == null) {
            return;
        }

        if (VERBOSE_WILDFLY_LOGS) {
            log.log(Level.WARNING,
                    "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3} body={4}",
                    new Object[]{
                        safeRequestId,
                        status,
                        attempt,
                        isLikelyContextTooLarge(response),
                        truncate(responseBody, MAX_ERROR_LOG_CHARS)
                    });
        } else {
            log.log(Level.WARNING,
                    "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3}",
                    new Object[]{
                        safeRequestId,
                        status,
                        attempt,
                        isLikelyContextTooLarge(response)
                    });
        }

        ServerDiagnosticsLog.write(
                "workspace-client",
                safeRequestId,
                "upstream-4xx",
                "status=" + status
                    + "\nattempt=" + attempt
                    + "\nauthSource=" + authSource
                    + "\nauthMode=" + authMode.name()
                    + "\nauthAttemptTrace=" + authAttemptTrace
                    + "\ncontextTooLarge=" + isLikelyContextTooLarge(response)
                    + "\ncontentType=" + contentType
                    + "\nresponseBody=" + responseBody
        );
    }

    private boolean shouldRetryStatus(int status, int attempt, String safeRequestId, String contentType, String responseBody) {
        boolean retryable = isRetryableStatus(status);
        if (!retryable || attempt > (maxRetries + 1)) {
            return false;
        }

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
        return true;
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
        String merged = new StringBuilder(256)
                .append(safeLower(json.getString("message", ""))).append(' ')
                .append(safeLower(json.getString("error", ""))).append(' ')
                .append(safeLower(json.getString("detail", ""))).append(' ')
                .append(safeLower(json.getString("code", ""))).append(' ')
                .append(safeLower(json.getString("type", ""))).append(' ')
                .append(safeLower(json.getString("reason", "")))
                .toString()
                .trim();

        if (containsContextLimitPhrase(merged)) {
            return true;
        }

        return false;
    }

        private JsonObject buildPayload(
            String message,
            String mode,
            String sessionId,
            boolean includeSessionId,
            boolean reset,
            JsonArray attachments
    ) {
        var b = Json.createObjectBuilder()
                .add("message", message == null ? "" : message)
                .add("mode", (mode == null || mode.isBlank()) ? "chat" : mode)
                .add("reset", reset);

        if (includeSessionId && sessionId != null && !sessionId.isBlank()) {
            b.add("sessionId", sessionId);
        }
        if (attachments != null && !attachments.isEmpty()) {
            b.add("attachments", attachments);
        }

        return b.build();
    }

    private boolean shouldIncludeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        String normalizedSession = sessionId.trim().toLowerCase(Locale.ROOT);
        if (normalizedSession.startsWith("dashboard-daily-summary")) {
            return false;
        }

        return true;
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

    private List<AuthHeaderMode> resolveModeCandidates(String preferredHeaderName) {
        AuthHeaderMode primary = resolvePrimaryAuthMode(preferredHeaderName);
        LinkedHashSet<AuthHeaderMode> ordered = new LinkedHashSet<>();
        ordered.add(primary);

        // Compat fallback set: different deployments validate auth header formats differently.
        ordered.add(AuthHeaderMode.AUTH_BEARER);
        ordered.add(AuthHeaderMode.X_API_KEY);
        ordered.add(AuthHeaderMode.AUTH_RAW);
        ordered.add(AuthHeaderMode.AUTH_BEARER_AND_X_API_KEY);

        return new ArrayList<>(ordered);
    }

    private boolean isAuthModeRetryableStatus(int status) {
        // 400 is typically payload/contract related, not an auth-header mode issue.
        // Restrict auth fallback to explicit auth failures to avoid noisy retries.
        return status == 401 || status == 403;
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
            String key = token + '|' + safe(candidate.preferredHeaderName());
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

    private WorkspaceResponse sendBearerCompatViaHttpURLConnection(
            URI targetUri,
            String body,
            ApiAuthResolver.ResolvedApiAuth auth
    ) throws IOException {
        if (targetUri == null) {
            throw new IOException("Workspace target URL is required.");
        }
        if (auth == null || auth.token() == null || auth.token().isBlank()) {
            throw new IOException("Workspace API key is required.");
        }

        URLConnection rawConn = targetUri.toURL().openConnection();
        String scheme = targetUri.getScheme() == null ? "" : targetUri.getScheme().toLowerCase(Locale.ROOT);
        if ("https".equals(scheme)) {
            if (!(rawConn instanceof HttpsURLConnection httpsConn)) {
                throw new IOException("Expected HTTPS connection for secure target URI.");
            }
            return sendViaHttpConnection(httpsConn, body, auth);
        } else {
            if (!(rawConn instanceof HttpURLConnection)) {
                throw new IOException("Unsupported URL connection type.");
            }
            return sendViaHttpConnection((HttpURLConnection) rawConn, body, auth);
        }
    }

    private WorkspaceResponse sendViaHttpConnection(
            HttpURLConnection conn,
            String body,
            ApiAuthResolver.ResolvedApiAuth auth
    ) throws IOException {
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(toSafeInt(Duration.ofSeconds(20).toMillis()));
            conn.setReadTimeout(toSafeInt(requestTimeout.toMillis()));
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + auth.token());
            conn.setFixedLengthStreamingMode(bytes.length);

            conn.getOutputStream().write(bytes);

            int status = safeResponseCode(conn);
            String contentType = safeContentType(conn);

            InputStream stream = selectResponseStream(conn, status);
            String responseBody = "";
            if (stream != null) {
                try (InputStream in = stream) {
                    responseBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            return new WorkspaceResponse(status, responseBody, contentType);
        } finally {
            conn.disconnect();
        }
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
            default -> requestBuilder.header("Authorization", "Bearer " + token);
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
            return EMPTY_JSON_OBJECT;
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        } catch (JsonException | ClassCastException | IllegalStateException ex) {
            log.log(Level.FINE, "Unable to parse workspace response as JSON.", ex);
            return EMPTY_JSON_OBJECT;
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

    private int toSafeInt(long value) {
        if (value <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(value);
    }

    private String safeTarget(String targetUrl) {
        try {
            URI u = URI.create(targetUrl);
            String host = u.getHost() == null ? "" : u.getHost();
            String path = u.getPath() == null ? "" : u.getPath();
            return host + path;
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid target URL while preparing safe target text.", e);
            return "(invalid-target)";
        }
    }

    private int sanitizeStatusCode(int status) {
        if (status < 100 || status > 599) {
            return 500;
        }
        return status;
    }

    private int safeResponseCode(HttpURLConnection conn) throws IOException {
        if (conn == null) {
            throw new IOException("Connection is not available.");
        }
        int rawCode = conn.getResponseCode();
        return validateTaintedStatusCode(rawCode);
    }

    private String safeContentType(HttpURLConnection conn) {
        if (conn == null) {
            return "";
        }
        String headerValue = validateTaintedHeaderValue(conn.getHeaderField("Content-Type"));
        String sanitizedHeader = sanitizeHeaderValue(headerValue);
        return sanitizeContentTypeValue(sanitizedHeader);
    }

    private int validateTaintedStatusCode(int rawCode) {
        String normalizedCode = validateTaintedHeaderValue(Integer.toString(rawCode));
        if (normalizedCode.length() != 3 || !isAsciiDigitsOnly(normalizedCode)) {
            return 500;
        }
        try {
            return sanitizeStatusCode(Integer.parseInt(normalizedCode));
        } catch (NumberFormatException ex) {
            return 500;
        }
    }

    private InputStream selectResponseStream(HttpURLConnection conn, int status) throws IOException {
        if (conn == null) {
            throw new IOException("Connection is not available.");
        }
        if (status >= 400) {
            return conn.getErrorStream();
        }
        return conn.getInputStream();
    }

    private String sanitizeHeaderValue(String headerValue) {
        if (headerValue == null) {
            return "";
        }
        return validateTaintedHeaderValue(headerValue);
    }

    private String validateTaintedHeaderValue(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return "";
        }

        String canonical = Normalizer.normalize(headerValue, Normalizer.Form.NFKC);
        String normalized = canonical.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private String sanitizeContentTypeValue(String headerValue) {
        String normalized = sanitizeHeaderValue(headerValue);
        if (normalized.isBlank()) {
            return "";
        }

        int semicolon = normalized.indexOf(';');
        String mime = semicolon >= 0 ? normalized.substring(0, semicolon).trim().toLowerCase(Locale.ROOT) : normalized.toLowerCase(Locale.ROOT);
        if (mime.isBlank()) {
            return "";
        }

        int slash = mime.indexOf('/');
        if (slash <= 0 || slash == mime.length() - 1) {
            return "";
        }

        String type = mime.substring(0, slash);
        String subtype = mime.substring(slash + 1);
        if (!isSafeMimeToken(type) || !isSafeMimeToken(subtype)) {
            return "";
        }

        return mime;
    }

    private boolean isSafeMimeToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            boolean lowerAlpha = ch >= 'a' && ch <= 'z';
            boolean digit = ch >= '0' && ch <= '9';
            boolean safePunct = ch == '!' || ch == '#' || ch == '$' || ch == '&' || ch == '^' || ch == '_' || ch == '.' || ch == '+' || ch == '-';
            if (!(lowerAlpha || digit || safePunct)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAsciiDigitsOnly(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    private static String readEnvSanitized(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String raw = System.getenv().get(key);
        if (raw == null) {
            return null;
        }
        String normalized = raw.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
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

        WorkspaceResponse(int statusCode, String body, String contentType) {
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
