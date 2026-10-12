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

import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.TextIoSanitizerUtil;

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


    private static final Logger log = Logger.getLogger(WorkspaceClient.class.getCanonicalName());

    private static final int DEFAULT_MAX_RETRIES = 1;
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final int MAX_ERROR_LOG_CHARS = 2000;
    private static final int MAX_LOG_MESSAGE_PREVIEW_CHARS = 800;
    private static final String VERBOSE_WILDFLY_ENV = "SIM_WORKSPACECLIENT_VERBOSE_WILDFLY_LOG";
    private static final String DIAG_COMPONENT = "workspace-client";
    private static final String HEADER_CONTENT_TYPE = CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String HEADER_X_API_KEY = CommonLiteralConstants.HEADER_X_API_KEY;
    private static final String HEADER_AUTHORIZATION_LOWER = CommonLiteralConstants.HEADER_AUTHORIZATION_LOWER;
    private static final String HEADER_X_API_KEY_LOWER = CommonLiteralConstants.HEADER_X_API_KEY_LOWER;
    private static final String MIME_APPLICATION_JSON = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String BEARER_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX;
    private static final String SCHEME_HTTP = CommonLiteralConstants.SCHEME_HTTP;
    private static final String MODE_CHAT = CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String JSON_FIELD_MODE = CommonLiteralConstants.FIELD_MODE;
    private static final String JSON_FIELD_RESET = com.sim.chatserver.util.CommonLiteralConstants.VALUE_RESET;
    private static final String JSON_FIELD_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_FIELD_ATTACHMENTS = CommonLiteralConstants.FIELD_ATTACHMENTS;
    private static final String SESSION_PREFIX_DASHBOARD_DAILY_SUMMARY = "dashboard-daily-summary";
    private static final String ARG_HTTP_CLIENT = "httpClient";
    private static final String AUTH_TRACE_STATUS_SEPARATOR = "=>";
    private static final String DELIMITER_COMMA_SPACE = ", ";
    private static final String LOG_FMT_AUTH_FALLBACK_MODE = "[workspace-client][{0}] auth fallback switching mode after status={1} mode={2} source={3}";
    private static final String LOG_FMT_AUTH_FALLBACK_SOURCE = "[workspace-client][{0}] auth fallback switching source after status={1} source={2}";
    private static final String LOG_FMT_NETWORK_RETRY = "[workspace-client][{0}] network failure, retrying attempt={1} errorRef={2} reason={3}";
    private static final String LOG_FMT_SEND_VERBOSE = "[workspace-client][{0}] send mode={1} reset={2} messageChars={3} payloadChars={4} attachments={5} target={6} messagePreview={7}";
    private static final String LOG_FMT_SEND = "[workspace-client][{0}] send mode={1} reset={2} messageChars={3} payloadChars={4} attachments={5} target={6}";
    private static final String LOG_FMT_UPSTREAM_4XX_VERBOSE = "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3} body={4}";
    private static final String LOG_FMT_UPSTREAM_4XX = "[workspace-client][{0}] upstream 4xx status={1} attempt={2} contextTooLarge={3}";
    private static final String LOG_FMT_RETRYABLE_STATUS = "[workspace-client][{0}] retryable status status={1} attempt={2}";
    private static final String EVENT_SEND = "send";
    private static final String EVENT_NETWORK_FAILURE = "network-failure";
    private static final String EVENT_UPSTREAM_4XX = "upstream-4xx";
    private static final String EVENT_RETRYABLE_STATUS = "retryable-status";
    private static final String LOG_KV_TARGET = "target=";
    private static final String LOG_KV_MODE = "mode=";
    private static final String LOG_KV_RESET = "reset=";
    private static final String LOG_KV_SESSION_ID_INCLUDED = "sessionIdIncluded=";
    private static final String LOG_KV_MESSAGE_CHARS = "messageChars=";
    private static final String LOG_KV_PAYLOAD_CHARS = "payloadChars=";
    private static final String LOG_KV_ATTACHMENTS = "attachments=";
    private static final String LOG_KV_AUTH_CANDIDATES = "authCandidates=";
    private static final String LOG_KV_PAYLOAD = "payload=";
    private static final String LOG_KV_ERROR_REF = "errorRef=";
    private static final String LOG_KV_REASON = "reason=";
    private static final String LOG_KV_AUTH_SOURCE = "authSource=";
    private static final String LOG_KV_AUTH_MODE = "authMode=";
    private static final String LOG_KV_AUTH_ATTEMPT_TRACE = "authAttemptTrace=";
    private static final String LOG_KV_CONTEXT_TOO_LARGE = "contextTooLarge=";
    private static final String JSON_FIELD_MESSAGE = CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_FIELD_ERROR = CommonLiteralConstants.KEY_ERROR;
    private static final String JSON_FIELD_DETAIL = "detail";
    private static final String JSON_FIELD_CODE = "code";
    private static final String JSON_FIELD_TYPE = "type";
    private static final String JSON_FIELD_REASON = "reason";
    private static final String LOG_KV_STATUS = CommonLiteralConstants.LOG_KV_STATUS;
    private static final String LOG_KV_ATTEMPT = "attempt=";
    private static final String LOG_KV_CONTENT_TYPE = "contentType=";
    private static final String LOG_KV_RESPONSE_BODY = "responseBody=";
    private static final String LOG_KV_MESSAGE = CommonLiteralConstants.LOG_KV_MESSAGE;
    private static final String ERR_WORKSPACE_TARGET_REQUIRED = "Workspace target URL is required.";
    private static final String ERR_WORKSPACE_API_KEY_REQUIRED = "Workspace API key is required.";
    private static final String ERR_WORKSPACE_CALL_FAILED_NO_RESPONSE = "Workspace call failed before receiving a response.";
    private static final String ERR_CONNECTION_NOT_AVAILABLE = "Connection is not available.";
    private static final String ERR_EXPECTED_HTTPS_CONNECTION = "Expected HTTPS connection for secure target URI.";
    private static final String ERR_UNSUPPORTED_URL_CONNECTION_TYPE = "Unsupported URL connection type.";
    private static final String ERR_WORKSPACE_TARGET_HTTP_SCHEME = "Workspace target URL must be http/https with a host.";
    private static final String ERR_WORKSPACE_TARGET_INVALID = "Workspace target URL is invalid.";
    private static final String ERR_PARSE_JSON = "Unable to parse workspace response as JSON.";
    private static final String ERR_INVALID_TARGET_SAFE_TEXT = "Invalid target URL while preparing safe target text.";
    private static final String INVALID_TARGET = "(invalid-target)";
    private static final String HTTP_METHOD_POST = CommonLiteralConstants.HTTP_METHOD_POST;
    private static final String CONTEXT_LIMIT_MAXIMUM_CONTEXT_LENGTH = "maximum context length";
    private static final String CONTEXT_LIMIT_CONTEXT_LENGTH_EXCEEDED = "context length exceeded";
    private static final String CONTEXT_LIMIT_CONTEXT_WINDOW = "context window";
    private static final String CONTEXT_LIMIT_TOKEN_LIMIT = "token limit";
    private static final String CONTEXT_LIMIT_TOO_MANY_TOKENS = "too many tokens";
    private static final String CONTEXT_LIMIT_MAX_TOKENS = "max tokens";
    private static final String CONTEXT_LIMIT_PROMPT_TOO_LONG = "prompt is too long";
    private static final String CONTEXT_LIMIT_INPUT_TOO_LONG = "input is too long";
    private static final String CONTEXT_LIMIT_PAYLOAD_TOO_LARGE = "payload too large";
    private static final String CONTEXT_LIMIT_REQUEST_ENTITY_TOO_LARGE = "request entity too large";
    private static final String CONTEXT_LIMIT_FAILED_TO_EMBED = "failed_to_embed";
    private static final String CONTEXT_LIMIT_EMBEDDING_INPUT_TOO_LONG = "embedding input too long";
    private static final String CONTEXT_LIMIT_TOO_LARGE = "too large";
    private static final String BOOL_TRUE = CommonLiteralConstants.VALUE_TRUE;
    private static final String BOOL_ONE = "1";
    private static final String BOOL_YES = CommonLiteralConstants.VALUE_YES;
    private static final String BOOL_Y = "y";
    private static final String BOOL_ON = "on";
    private static final String SUFFIX_TRUNCATED = "...(truncated)";
    private static final String SCHEME_HTTPS = CommonLiteralConstants.SCHEME_HTTPS;
    private static final String EMPTY = "";
    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static final boolean VERBOSE_WILDFLY_LOGS = isTruthy(readEnvSanitized(VERBOSE_WILDFLY_ENV));
    private static final String LINE_SEPARATOR = System.lineSeparator();

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
        this.httpClient = Objects.requireNonNull(httpClient, ARG_HTTP_CLIENT);
        this.maxRetries = Math.max(0, maxRetries);
        this.requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
    }

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
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
        final String safeMessage = message == null ? EMPTY : message;
        final String safeMode = (mode == null || mode.isBlank()) ? MODE_CHAT : mode;
        final JsonArray safeAttachments = attachments == null ? Json.createArrayBuilder().build() : attachments;
        final URI targetUri = toSafeHttpUri(targetUrl);
        final String safeRequestId = safe(requestId);
        final boolean hasExplicitApiKey = ApiAuthResolver.normalizeApiKeyToken(apiKey) != null;
        final ResolvedApiAuth primaryAuth = hasExplicitApiKey
            ? ApiAuthResolver.resolveForServerConfigOutbound(apiKey)
            : ApiAuthResolver.resolveForOutbound(apiKey);
        final ResolvedApiAuth secondaryAuth = hasExplicitApiKey
            ? ApiAuthResolver.emptyResolvedApiAuth()
            : ApiAuthResolver.resolveForOutbound(null);
        final List<ResolvedApiAuth> authCandidates = buildAuthCandidates(primaryAuth, secondaryAuth);

        if (authCandidates.isEmpty()) {
            throw new IOException(ERR_WORKSPACE_API_KEY_REQUIRED);
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
                String usedAuthSource = EMPTY;

                if (bearerCompatOnly) {
                    ResolvedApiAuth auth = authCandidates.get(0);
                    usedAuthSource = safe(auth.source());
                    usedMode = AuthHeaderMode.AUTH_BEARER;
                    authAttemptTrace.append(usedAuthSource)
                            .append('|')
                            .append(usedMode.name());

                    WorkspaceResponse wrapped = sendBearerCompatViaHttpURLConnection(targetUri, body, auth);
                    int status = wrapped.statusCode();
                    String responseBody = wrapped.body() == null ? EMPTY : wrapped.body();
                    String contentType = wrapped.contentType() == null ? EMPTY : wrapped.contentType();

                    authAttemptTrace.append(AUTH_TRACE_STATUS_SEPARATOR).append(status);

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
                    ResolvedApiAuth auth = authCandidates.get(authIndex);
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
                            authAttemptTrace.append(DELIMITER_COMMA_SPACE);
                        }
                        authAttemptTrace.append(safe(auth.source()))
                            .append('|')
                            .append(authModeCandidate.name())
                            .append(AUTH_TRACE_STATUS_SEPARATOR)
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
                                    LOG_FMT_AUTH_FALLBACK_MODE,
                                        new Object[]{safeRequestId, candidateStatus, authModeCandidate.name(), safe(auth.source())});
                                continue;
                            }
                            if (hasMoreAuth) {
                                log.log(Level.INFO,
                                    LOG_FMT_AUTH_FALLBACK_SOURCE,
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
                    throw new IOException(ERR_WORKSPACE_CALL_FAILED_NO_RESPONSE);
                }

                int status = response.statusCode();
                String responseBody = response.body() == null ? EMPTY : response.body();
                String contentType = response.headers().firstValue(HEADER_CONTENT_TYPE).orElse(EMPTY);

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
                            LOG_FMT_NETWORK_RETRY,
                            new Object[]{
                                safeRequestId,
                                attempt,
                                errorRef,
                                truncateOneLine(safe(ex.getMessage()), 220)
                            });

                    ServerDiagnosticsLog.write(
                            DIAG_COMPONENT,
                            safeRequestId,
                            EVENT_NETWORK_FAILURE,
                            LOG_KV_ATTEMPT + attempt
                                + LINE_SEPARATOR + LOG_KV_ERROR_REF + errorRef
                                + LINE_SEPARATOR + LOG_KV_REASON + safe(ex.getMessage()),
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
            List<ResolvedApiAuth> authCandidates,
            String body
    ) {
        if (VERBOSE_WILDFLY_LOGS) {
            log.log(Level.INFO,
                    LOG_FMT_SEND_VERBOSE,
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
                    LOG_FMT_SEND,
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
            DIAG_COMPONENT,
                safeRequestId,
                EVENT_SEND,
                LOG_KV_TARGET + targetUri
                    + LINE_SEPARATOR + LOG_KV_MODE + safeMode
                    + LINE_SEPARATOR + LOG_KV_RESET + reset
                    + LINE_SEPARATOR + LOG_KV_SESSION_ID_INCLUDED + includeSessionId
                    + LINE_SEPARATOR + LOG_KV_MESSAGE_CHARS + safeMessage.length()
                    + LINE_SEPARATOR + LOG_KV_PAYLOAD_CHARS + body.length()
                    + LINE_SEPARATOR + LOG_KV_ATTACHMENTS + safeAttachments.size()
                    + LINE_SEPARATOR + LOG_KV_AUTH_CANDIDATES + summarizeAuthCandidates(authCandidates)
                    + LINE_SEPARATOR + LOG_KV_MESSAGE + safeMessage
                    + LINE_SEPARATOR + LOG_KV_PAYLOAD + body
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
                    LOG_FMT_UPSTREAM_4XX_VERBOSE,
                    new Object[]{
                        safeRequestId,
                        status,
                        attempt,
                        isLikelyContextTooLarge(response),
                        truncate(responseBody, MAX_ERROR_LOG_CHARS)
                    });
        } else {
            log.log(Level.WARNING,
                    LOG_FMT_UPSTREAM_4XX,
                    new Object[]{
                        safeRequestId,
                        status,
                        attempt,
                        isLikelyContextTooLarge(response)
                    });
        }

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
                safeRequestId,
                EVENT_UPSTREAM_4XX,
                LOG_KV_STATUS + status
                    + LINE_SEPARATOR + LOG_KV_ATTEMPT + attempt
                    + LINE_SEPARATOR + LOG_KV_AUTH_SOURCE + authSource
                    + LINE_SEPARATOR + LOG_KV_AUTH_MODE + authMode.name()
                    + LINE_SEPARATOR + LOG_KV_AUTH_ATTEMPT_TRACE + authAttemptTrace
                    + LINE_SEPARATOR + LOG_KV_CONTEXT_TOO_LARGE + isLikelyContextTooLarge(response)
                    + LINE_SEPARATOR + LOG_KV_CONTENT_TYPE + contentType
                    + LINE_SEPARATOR + LOG_KV_RESPONSE_BODY + responseBody
        );
    }

    private boolean shouldRetryStatus(int status, int attempt, String safeRequestId, String contentType, String responseBody) {
        boolean retryable = isRetryableStatus(status);
        if (!retryable || attempt > (maxRetries + 1)) {
            return false;
        }

        log.log(Level.WARNING,
            LOG_FMT_RETRYABLE_STATUS,
                new Object[]{safeRequestId, status, attempt});

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
                safeRequestId,
                EVENT_RETRYABLE_STATUS,
                LOG_KV_STATUS + status
                    + LINE_SEPARATOR + LOG_KV_ATTEMPT + attempt
                    + LINE_SEPARATOR + LOG_KV_CONTENT_TYPE + contentType
                    + LINE_SEPARATOR + LOG_KV_RESPONSE_BODY + responseBody
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
        String body = response.body() == null ? EMPTY : response.body();
        String lower = body.toLowerCase(Locale.ROOT);

        if (status == 413) {
            return true;
        }

        if (containsContextLimitPhrase(lower)) {
            return true;
        }

        JsonObject json = parseJsonObject(body);
        String merged = new StringBuilder(256)
            .append(safeLower(json.getString(JSON_FIELD_MESSAGE, EMPTY))).append(' ')
            .append(safeLower(json.getString(JSON_FIELD_ERROR, EMPTY))).append(' ')
            .append(safeLower(json.getString(JSON_FIELD_DETAIL, EMPTY))).append(' ')
            .append(safeLower(json.getString(JSON_FIELD_CODE, EMPTY))).append(' ')
            .append(safeLower(json.getString(JSON_FIELD_TYPE, EMPTY))).append(' ')
            .append(safeLower(json.getString(JSON_FIELD_REASON, EMPTY)))
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
                .add(JSON_FIELD_MESSAGE, message == null ? EMPTY : message)
            .add(JSON_FIELD_MODE, (mode == null || mode.isBlank()) ? MODE_CHAT : mode)
                .add(JSON_FIELD_RESET, reset);

        if (includeSessionId && sessionId != null && !sessionId.isBlank()) {
            b.add(JSON_FIELD_SESSION_ID, sessionId);
        }
        if (attachments != null && !attachments.isEmpty()) {
            b.add(JSON_FIELD_ATTACHMENTS, attachments);
        }

        return b.build();
    }

    private boolean shouldIncludeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        String normalizedSession = sessionId.trim().toLowerCase(Locale.ROOT);
        if (normalizedSession.startsWith(SESSION_PREFIX_DASHBOARD_DAILY_SUMMARY)) {
            return false;
        }

        return true;
    }

    private AuthHeaderMode resolvePrimaryAuthMode(String preferredHeaderName) {
        String header = preferredHeaderName == null ? EMPTY : preferredHeaderName.trim();
        if (HEADER_X_API_KEY_LOWER.equalsIgnoreCase(header)) {
            return AuthHeaderMode.X_API_KEY;
        }
        if (!header.isBlank() && !HEADER_AUTHORIZATION_LOWER.equalsIgnoreCase(header)) {
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

    private List<ResolvedApiAuth> buildAuthCandidates(
            ResolvedApiAuth primary,
            ResolvedApiAuth secondary
    ) {
        Map<String, ResolvedApiAuth> unique = new LinkedHashMap<>();
        for (ResolvedApiAuth candidate : Arrays.asList(primary, secondary)) {
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

    private String summarizeAuthCandidates(List<ResolvedApiAuth> authCandidates) {
        if (authCandidates == null || authCandidates.isEmpty()) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (ResolvedApiAuth auth : authCandidates) {
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
            ResolvedApiAuth auth
    ) throws IOException {
        if (targetUri == null) {
            throw new IOException(ERR_WORKSPACE_TARGET_REQUIRED);
        }
        if (auth == null || auth.token() == null || auth.token().isBlank()) {
            throw new IOException(ERR_WORKSPACE_API_KEY_REQUIRED);
        }

        URLConnection rawConn = targetUri.toURL().openConnection();
        String scheme = targetUri.getScheme() == null ? EMPTY : targetUri.getScheme().toLowerCase(Locale.ROOT);
        if (SCHEME_HTTPS.equals(scheme)) {
            if (!(rawConn instanceof HttpsURLConnection httpsConn)) {
                throw new IOException(ERR_EXPECTED_HTTPS_CONNECTION);
            }
            return sendViaHttpConnection(httpsConn, body, auth);
        } else {
            if (!(rawConn instanceof HttpURLConnection)) {
                throw new IOException(ERR_UNSUPPORTED_URL_CONNECTION_TYPE);
            }
            return sendViaHttpConnection((HttpURLConnection) rawConn, body, auth);
        }
    }

    private WorkspaceResponse sendViaHttpConnection(
            HttpURLConnection conn,
            String body,
            ResolvedApiAuth auth
    ) throws IOException {
        byte[] bytes = (body == null ? EMPTY : body).getBytes(StandardCharsets.UTF_8);
        try {
            conn.setRequestMethod(HTTP_METHOD_POST);
            conn.setConnectTimeout(toSafeInt(Duration.ofSeconds(20).toMillis()));
            conn.setReadTimeout(toSafeInt(requestTimeout.toMillis()));
            conn.setDoOutput(true);
            conn.setRequestProperty(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON);
            conn.setRequestProperty(HEADER_ACCEPT, MIME_APPLICATION_JSON);
            conn.setRequestProperty(HEADER_AUTHORIZATION, BEARER_PREFIX + auth.token());
            conn.setFixedLengthStreamingMode(bytes.length);

            conn.getOutputStream().write(bytes);

            int status = safeResponseCode(conn);
            String contentType = safeContentType(conn);

            InputStream stream = selectResponseStream(conn, status);
            String responseBody = EMPTY;
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

    private HttpRequest buildRequest(URI targetUri, String body, ResolvedApiAuth auth, AuthHeaderMode mode) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(targetUri)
                .timeout(requestTimeout)
            .header(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
            .header(HEADER_ACCEPT, MIME_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        String token = auth == null ? null : auth.token();
        String rawValue = auth == null ? null : auth.rawValue();
        String preferredHeader = auth == null ? null : auth.preferredHeaderName();

        if (token == null || token.isBlank()) {
            return requestBuilder.build();
        }

        switch (mode) {
            case CUSTOM_HEADER -> applyCustomHeader(requestBuilder, preferredHeader, rawValue, token);
            case AUTH_RAW -> requestBuilder.header(HEADER_AUTHORIZATION, normalizeRawAuthorizationValue(rawValue, token));
            case X_API_KEY -> requestBuilder.header(HEADER_X_API_KEY, token);
            case AUTH_BEARER_AND_X_API_KEY -> requestBuilder
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .header(HEADER_X_API_KEY, token);
            case AUTH_BEARER -> requestBuilder.header(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
            default -> requestBuilder.header(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
        }

        return requestBuilder.build();
    }

    private void applyCustomHeader(HttpRequest.Builder requestBuilder, String headerName, String rawValue, String token) {
        String normalizedHeader = headerName == null ? EMPTY : headerName.trim();
        if (normalizedHeader.isBlank()) {
            return;
        }

        if (HEADER_AUTHORIZATION_LOWER.equalsIgnoreCase(normalizedHeader)) {
            requestBuilder.header(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
            return;
        }
        if (HEADER_X_API_KEY_LOWER.equalsIgnoreCase(normalizedHeader)) {
            requestBuilder.header(HEADER_X_API_KEY, token);
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

        return lower.contains(CONTEXT_LIMIT_MAXIMUM_CONTEXT_LENGTH)
            || lower.contains(CONTEXT_LIMIT_CONTEXT_LENGTH_EXCEEDED)
            || lower.contains(CONTEXT_LIMIT_CONTEXT_WINDOW)
            || lower.contains(CONTEXT_LIMIT_TOKEN_LIMIT)
            || lower.contains(CONTEXT_LIMIT_TOO_MANY_TOKENS)
            || lower.contains(CONTEXT_LIMIT_MAX_TOKENS)
            || lower.contains(CONTEXT_LIMIT_PROMPT_TOO_LONG)
            || lower.contains(CONTEXT_LIMIT_INPUT_TOO_LONG)
            || lower.contains(CONTEXT_LIMIT_PAYLOAD_TOO_LARGE)
            || lower.contains(CONTEXT_LIMIT_REQUEST_ENTITY_TOO_LARGE)
            || lower.contains(CONTEXT_LIMIT_FAILED_TO_EMBED)
            || lower.contains(CONTEXT_LIMIT_EMBEDDING_INPUT_TOO_LONG)
            || lower.contains(CONTEXT_LIMIT_TOO_LARGE);
    }

    private JsonObject parseJsonObject(String body) {
        if (body == null || body.isBlank()) {
            return EMPTY_JSON_OBJECT;
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        } catch (JsonException | ClassCastException | IllegalStateException ex) {
            log.log(Level.FINE, ERR_PARSE_JSON, ex);
            return EMPTY_JSON_OBJECT;
        }
    }

    private URI toSafeHttpUri(String targetUrl) throws IOException {
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IOException(ERR_WORKSPACE_TARGET_REQUIRED);
        }
        try {
            URI uri = URI.create(targetUrl.trim()).normalize();
            String scheme = uri.getScheme() == null ? EMPTY : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            if ((!SCHEME_HTTP.equals(scheme) && !SCHEME_HTTPS.equals(scheme)) || host == null || host.isBlank()) {
                throw new IOException(ERR_WORKSPACE_TARGET_HTTP_SCHEME);
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new IOException(ERR_WORKSPACE_TARGET_INVALID, ex);
        }
    }

    private String safeLower(String v) {
        return v == null ? EMPTY : v.toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return EMPTY;
        }
        if (value.length() <= max) {
            return value;
        }
        return TextIoSanitizerUtil.safeSlice(value, 0, max) + SUFFIX_TRUNCATED;
    }

    private String truncateOneLine(String value, int max) {
        if (value == null) {
            return EMPTY;
        }
        String oneLine = value.replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ').replace(System.lineSeparator().charAt(0), ' ');
        if (oneLine.length() <= max) {
            return oneLine;
        }
        return TextIoSanitizerUtil.safeSlice(oneLine, 0, max) + SUFFIX_TRUNCATED;
    }

    private String safe(String v) {
        return v == null ? EMPTY : v.trim();
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
            String host = u.getHost() == null ? EMPTY : u.getHost();
            String path = u.getPath() == null ? EMPTY : u.getPath();
            return host + path;
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, ERR_INVALID_TARGET_SAFE_TEXT, e);
            return INVALID_TARGET;
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
            throw new IOException(ERR_CONNECTION_NOT_AVAILABLE);
        }
        int rawCode = conn.getResponseCode();
        return validateTaintedStatusCode(rawCode);
    }

    private String safeContentType(HttpURLConnection conn) {
        if (conn == null) {
            return EMPTY;
        }
        String raw = conn.getHeaderField(HEADER_CONTENT_TYPE);
        String canonical = raw == null ? EMPTY : Normalizer.normalize(raw, Normalizer.Form.NFKC);
        String headerValue = sanitizeHeaderValue(canonical);
        return sanitizeContentTypeValue(headerValue);
    }

    private int validateTaintedStatusCode(int rawCode) {
        if (rawCode < 100 || rawCode > 599) {
            return 500;
        }
        return sanitizeStatusCode(rawCode);
    }

    private InputStream selectResponseStream(HttpURLConnection conn, int status) throws IOException {
        if (conn == null) {
            throw new IOException(ERR_CONNECTION_NOT_AVAILABLE);
        }
        if (status >= 400) {
            return conn.getErrorStream();
        }
        return conn.getInputStream();
    }

    private String sanitizeHeaderValue(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return EMPTY;
        }
        String canonical = Normalizer.normalize(headerValue, Normalizer.Form.NFKC);
        String normalized = canonical.replace(System.lineSeparator().charAt(0), ' ').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ').trim();
        return normalized.length() > 256 ? TextIoSanitizerUtil.safeSlice(normalized, 0, 256) : normalized;
    }

    private String sanitizeContentTypeValue(String headerValue) {
        String normalized = sanitizeHeaderValue(headerValue);
        if (normalized.isBlank()) {
            return EMPTY;
        }

        int semicolon = normalized.indexOf((char) 59);
        String mime = semicolon >= 0 ? TextIoSanitizerUtil.safeSlice(normalized, 0, semicolon).trim().toLowerCase(Locale.ROOT) : normalized.toLowerCase(Locale.ROOT);
        if (mime.isBlank()) {
            return EMPTY;
        }

        int slash = mime.indexOf('/');
        if (slash <= 0 || slash == mime.length() - 1) {
            return EMPTY;
        }

        String type = mime.substring(0, slash);
        String subtype = mime.substring(slash + 1);
        if (!isSafeMimeToken(type) || !isSafeMimeToken(subtype)) {
            return EMPTY;
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
        String raw = ENV.get(key);
        if (raw == null) {
            return null;
        }
        String normalized = raw.replace(System.lineSeparator().charAt(0), ' ').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ').trim();
        return normalized.length() > 64 ? TextIoSanitizerUtil.safeSlice(normalized, 0, 64) : normalized;
    }

    private static boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return BOOL_TRUE.equals(normalized)
            || BOOL_ONE.equals(normalized)
            || BOOL_YES.equals(normalized)
            || BOOL_Y.equals(normalized)
            || BOOL_ON.equals(normalized);
    }

}


