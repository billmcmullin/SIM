package com.sim.chatserver.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.TextIoSanitizerUtil;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * Robust upstream caller for AnythingLLM.
 *
 * Canonical endpoint: /api/v1/workspace/{workspace}/chat - Accepts base origin
 * OR full endpoint URL - Repairs malformed duplicated-scheme bases like
 * https://https://host:443 - Prefers /chat (non-stream) for strict JSON
 * request/response handling - Strict payload:
 * message/mode/reset/sessionId/attachments (attachments optional) - Logs
 * upstream 4xx response body and returns passthrough response data to caller
 */
public class UpstreamRequestService {

    private static final Logger LOG = Logger.getLogger(UpstreamRequestService.class.getCanonicalName());

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);

    private static final String STREAM_CHAT_SUFFIX = "/stream-chat";
    private static final String CHAT_SUFFIX = "/chat";
    private static final String FIXED_PATH_TEMPLATE = "/api/v1/workspace/%s/chat"; // canonical /chat
    private static final String DIAG_COMPONENT = "upstream-request-service";
    private static final String DIAG_EVENT_HTTP_ERROR = "http-error";
    private static final String DIAG_METHOD_POST = CommonLiteralConstants.LOG_KV_METHOD_POST;
    private static final String LOG_KV_URL = CommonLiteralConstants.LOG_KV_URL;
    private static final String HEADER_CONTENT_TYPE = CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String MIME_APPLICATION_JSON = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String DEFAULT_RESPONSE_CONTENT_TYPE = MIME_APPLICATION_JSON + "; charset=UTF-8";
    private static final String BEARER_PREFIX = CommonLiteralConstants.AUTH_BEARER_PREFIX;
    private static final String CODE_UPSTREAM_URL_INVALID = "UPSTREAM_URL_INVALID";
    private static final String CODE_UPSTREAM_TIMEOUT = "UPSTREAM_TIMEOUT";
    private static final String CODE_UPSTREAM_CONNECT_FAILED = "UPSTREAM_CONNECT_FAILED";
    private static final String CODE_UPSTREAM_DNS_FAILED = "UPSTREAM_DNS_FAILED";
    private static final String CODE_UPSTREAM_INTERRUPTED = "UPSTREAM_INTERRUPTED";
    private static final String CODE_UPSTREAM_RUNTIME = "UPSTREAM_RUNTIME";
    private static final String MSG_UPSTREAM_URL_INVALID = "Upstream URL invalid";
    private static final String MSG_TIMEOUT_CONNECT = "Timed out connecting to upstream";
    private static final String MSG_TIMEOUT_CALL = "Timed out calling upstream";
    private static final String MSG_CONNECT_FAILED = "TCP connect failed";
    private static final String MSG_DNS_FAILED = "DNS/host resolution failed";
    private static final String MSG_INTERRUPTED = "Interrupted while calling upstream";
    private static final String MSG_RUNTIME_FAILURE = "Unexpected upstream HTTP client failure";
    private static final int MAX_URL_CHARS = 2048;
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String LOG_FALLBACK_ENDPOINT_ATTEMPT = "[upstream][{0}] fallback endpoint attempt firstStatus={1} from={2} to={3}";
    private static final String MSG_UPSTREAM_URL_BASE_MISSING = "Upstream URL/base is missing";
    private static final String CODE_UPSTREAM_WORKSPACE_MISSING = "UPSTREAM_WORKSPACE_MISSING";
    private static final String MSG_UPSTREAM_WORKSPACE_REQUIRED = "Workspace is required when upstream URL is base origin only";
    private static final String MSG_UPSTREAM_BASE_URL_INVALID = "Upstream base URL invalid";
    private static final String REGEX_DUPLICATE_SCHEME = "^(https?://)(https?://)+";
    private static final String REPLACEMENT_FIRST_SCHEME = "$1";
    private static final String URL_DOUBLE_HTTPS = "https://https://";
    private static final String URL_DOUBLE_HTTP = "http://http://";
    private static final String URL_HTTP_HTTPS = "http://https://";
    private static final String URL_HTTPS_HTTP = "https://http://";
    private static final String SCHEME_HTTP = CommonLiteralConstants.URL_PREFIX_HTTP;
    private static final String SCHEME_HTTPS = CommonLiteralConstants.URL_PREFIX_HTTPS;
    private static final String SCHEME_SEPARATOR = CommonLiteralConstants.SCHEME_SEPARATOR;
    private static final String LOG_FAILED_EXTRACT_ORIGIN = "Failed to extract upstream origin";
    private static final String MSG_UPSTREAM_URL_MISSING_SCHEME_HOST = "Upstream URL missing scheme/host";
    private static final String SCHEME_HTTP_NAME = CommonLiteralConstants.SCHEME_HTTP;
    private static final String SCHEME_HTTPS_NAME = CommonLiteralConstants.SCHEME_HTTPS;
    private static final String MSG_UPSTREAM_URL_SCHEME_INVALID = "Upstream URL scheme must be http/https";
    private static final String MSG_UPSTREAM_URL_BLANK = "Upstream URL is blank";
    private static final String MSG_UPSTREAM_URL_SYNTAX_INVALID = "Upstream URL syntax invalid";
    private static final String WORKSPACE_SLUG_PATTERN = "[A-Za-z0-9._-]{1,80}";
    private static final String CODE_UPSTREAM_WORKSPACE_INVALID = "UPSTREAM_WORKSPACE_INVALID";
    private static final String MSG_UPSTREAM_CONTEXT_TOO_LARGE = "too large";
    private static final String MSG_UPSTREAM_CONTEXT_TOKEN = "token";
    private static final String MSG_UPSTREAM_CONTEXT_LENGTH = "context length";
    private static final String MSG_UPSTREAM_MAXIMUM_CONTEXT = "maximum context";
    private static final String MSG_UPSTREAM_PAYLOAD_TOO_LARGE = "payload too large";
    private static final String MSG_WORKSPACE_SLUG_INVALID = "Workspace slug format invalid";
    private static final String VALUE_SLASH = "/";
    private static final String MODE_CHAT = CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String MODE_QUERY = com.sim.chatserver.util.CommonLiteralConstants.FIELD_QUERY;
    private static final String MODE_AUTOMATIC = "automatic";
    private static final String JSON_FIELD_MESSAGE = CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_FIELD_MODE = CommonLiteralConstants.FIELD_MODE;
    private static final String JSON_FIELD_RESET = com.sim.chatserver.util.CommonLiteralConstants.VALUE_RESET;
    private static final String JSON_FIELD_SESSION_ID = CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_FIELD_ATTACHMENTS = CommonLiteralConstants.FIELD_ATTACHMENTS;
    private static final String JSON_FIELD_NAME = CommonLiteralConstants.FIELD_NAME;
    private static final String JSON_FIELD_MIME = "mime";
    private static final String JSON_FIELD_CONTENT_STRING = "contentString";
    private static final String DIAG_EVENT_HTTP_REQUEST = "http-request";
    private static final String DIAG_EVENT_HTTP_RESPONSE = "http-response";
    private static final String DIAG_PREFIX_BODY_CHARS = "bodyChars=";
    private static final String DIAG_PREFIX_BODY = CommonLiteralConstants.LOG_KV_BODY;
    private static final String DIAG_PREFIX_STATUS = CommonLiteralConstants.LOG_KV_STATUS;
    private static final String DIAG_PREFIX_CONTENT_TYPE = "contentType=";
    private static final String DIAG_PREFIX_RESPONSE_BODY = "responseBody=";
    private static final String LOG_UPSTREAM_PREFIX = "[upstream][";
    private static final String LOG_UNEXPECTED_CLIENT_EXCEPTION_SUFFIX = "] unexpected client exception";
    private static final String DIAG_PREFIX_CODE = "code=";
    private static final String DIAG_PREFIX_MESSAGE = CommonLiteralConstants.LOG_KV_MESSAGE;
    private static final String MSG_UPSTREAM_URL_MAX_LENGTH = "Upstream URL exceeds max length";
    private static final String MSG_UPSTREAM_URL_CONTROL_CHARS = "Upstream URL contains control characters";
    private static final String MSG_DESERIALIZATION_NOT_ALLOWED = CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    /**
     * Backward-compatible overload. If upstreamUrl is full endpoint, used
     * as-is. If it's origin-only, workspace is unknown and will fail fast.
     */
    private UpstreamResponse sendChat(
            String upstreamUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId
    ) throws IOException {
        return sendChat(upstreamUrl, null, apiKey, message, mode, sessionId, reset, attachments, requestId);
    }

    /**
     * Preferred overload: base origin OR full endpoint + workspace.
     */
    private UpstreamResponse sendChat(
            String upstreamBaseOrEndpoint,
            String workspace,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments,
            String requestId
    ) throws IOException {

        final String rid = (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
        final String resolvedUrl = resolveUpstreamUrl(upstreamBaseOrEndpoint, workspace);
        final String body = buildStrictPayload(message, mode, sessionId, reset, attachments).toString();

        // Prefer canonical /chat endpoint. Only fallback to sibling for route mismatch (404/405).
        UpstreamResponse first = doPost(resolvedUrl, apiKey, body, rid);

        if (isRouteMismatch(first.statusCode())) {
            String alt = siblingEndpoint(resolvedUrl);
            if (alt != null && !alt.equals(resolvedUrl)) {
                LOG.log(Level.INFO,
                    LOG_FALLBACK_ENDPOINT_ATTEMPT,
                        new Object[]{rid, first.statusCode(), resolvedUrl, alt});
                return doPost(alt, apiKey, body, rid);
            }
        }

        return first;
    }

    final boolean isLikelyContextTooLarge(UpstreamResponse resp) {
        if (resp == null) {
            return false;
        }

        int sc = resp.statusCode();
        if (sc == 413) {
            return true;
        }

        if (sc == 400 || sc == 422 || sc == 500) {
            String b = resp.body() == null ? EMPTY : resp.body().toLowerCase(Locale.ROOT);
            return b.contains(MSG_UPSTREAM_CONTEXT_TOO_LARGE)
                || b.contains(MSG_UPSTREAM_CONTEXT_TOKEN)
                || b.contains(MSG_UPSTREAM_CONTEXT_LENGTH)
                || b.contains(MSG_UPSTREAM_MAXIMUM_CONTEXT)
                || b.contains(MSG_UPSTREAM_PAYLOAD_TOO_LARGE);
        }
        return false;
    }

    private String resolveUpstreamUrl(String upstreamBaseOrEndpoint, String workspace) throws IOException {
        if (upstreamBaseOrEndpoint == null || upstreamBaseOrEndpoint.isBlank()) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_BASE_MISSING, null);
        }

        String raw = canonicalizeUrlInput(sanitizeBaseOrEndpoint(upstreamBaseOrEndpoint));

        // If caller already passed full endpoint, keep it (normalize to /chat if possible).
        if (endsWithEndpointPath(raw)) {
            validateHttpUrl(raw);
            return forceChatEndpoint(raw);
        }

        // Else treat as base origin + fixed /chat endpoint
        String ws = normalizeWorkspace(workspace);
        if (ws.isBlank()) {
            throw new UpstreamConnectivityException(
                    CODE_UPSTREAM_WORKSPACE_MISSING,
                    MSG_UPSTREAM_WORKSPACE_REQUIRED,
                    null
            );
        }

        String origin = extractOrigin(raw);
        if (origin.isBlank()) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_BASE_URL_INVALID, null);
        }

        String full = canonicalizeUrlInput(stripTrailingSlash(origin) + String.format(FIXED_PATH_TEMPLATE, ws));
        validateHttpUrl(full);
        return full;
    }

    private String sanitizeBaseOrEndpoint(String in) throws IOException {
        String s = canonicalizeUrlInput(in);
        if (s.isBlank()) {
            return s;
        }

        s = s.replaceFirst(REGEX_DUPLICATE_SCHEME, REPLACEMENT_FIRST_SCHEME);
        s = s.replace(URL_DOUBLE_HTTPS, SCHEME_HTTPS)
            .replace(URL_DOUBLE_HTTP, SCHEME_HTTP)
            .replace(URL_HTTP_HTTPS, SCHEME_HTTPS)
            .replace(URL_HTTPS_HTTP, SCHEME_HTTP);

        return s;
    }

    private boolean endsWithEndpointPath(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.endsWith(STREAM_CHAT_SUFFIX) || lower.endsWith(CHAT_SUFFIX);
    }

    private String forceChatEndpoint(String endpointUrl) {
        if (endpointUrl == null) {
            return null;
        }
        if (endpointUrl.endsWith(STREAM_CHAT_SUFFIX)) {
            return TextIoSanitizerUtil.safeSlice(endpointUrl, 0, endpointUrl.length() - STREAM_CHAT_SUFFIX.length()) + CHAT_SUFFIX;
        }
        return endpointUrl;
    }

    private String extractOrigin(String raw) {
        try {
            URI u = parseHttpUri(raw);
            String scheme = u.getScheme();
            String host = u.getHost();
            int port = u.getPort();

            if (scheme == null || scheme.isBlank() || host == null || host.isBlank()) {
                return EMPTY;
            }

            StringBuilder b = new StringBuilder()
                    .append(scheme.toLowerCase(Locale.ROOT))
                    .append(SCHEME_SEPARATOR)
                    .append(host.toLowerCase(Locale.ROOT));
            if (port > 0) {
                b.append((char) 58).append(port);
            }

            return b.toString();
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            LOG.log(Level.FINE, LOG_FAILED_EXTRACT_ORIGIN, e);
            return EMPTY;
        }
    }

    private void validateHttpUrl(String raw) throws IOException {
        URI u = parseHttpUri(raw);
        String scheme = u.getScheme();
        String host = u.getHost();

        if (scheme == null || host == null || host.isBlank()) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_MISSING_SCHEME_HOST, null);
        }

        String s = scheme.toLowerCase(Locale.ROOT);
        if (!SCHEME_HTTP_NAME.equals(s) && !SCHEME_HTTPS_NAME.equals(s)) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_SCHEME_INVALID, null);
        }
    }

    private URI parseHttpUri(String raw) throws IOException {
        String normalized = canonicalizeUrlInput(raw);
        if (normalized.isBlank()) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_BLANK, null);
        }
        try {
            return URI.create(normalized);
        } catch (IllegalArgumentException e) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_SYNTAX_INVALID, e);
        }
    }

    private String normalizeWorkspace(String workspace) throws IOException {
        String ws = workspace == null ? EMPTY : workspace.trim();
        if (ws.isBlank()) {
            return EMPTY;
        }
        if (!ws.matches(WORKSPACE_SLUG_PATTERN)) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_WORKSPACE_INVALID, MSG_WORKSPACE_SLUG_INVALID, null);
        }
        return ws;
    }

    private String stripTrailingSlash(String s) {
        String out = s;
        while (out.endsWith(VALUE_SLASH)) {
            out = TextIoSanitizerUtil.safeSlice(out, 0, out.length() - 1);
        }
        return out;
    }

    private JsonObject buildStrictPayload(
            String message,
            String mode,
            String sessionId,
            boolean reset,
            JsonArray attachments
    ) {
        String normalizedMode = (mode == null || mode.isBlank()) ? MODE_CHAT : mode.trim().toLowerCase(Locale.ROOT);
        if (!normalizedMode.equals(MODE_CHAT) && !normalizedMode.equals(MODE_QUERY) && !normalizedMode.equals(MODE_AUTOMATIC)) {
            normalizedMode = MODE_CHAT;
        }

        JsonObjectBuilder b = Json.createObjectBuilder()
                .add(JSON_FIELD_MESSAGE, message == null ? EMPTY : message)
                .add(JSON_FIELD_MODE, normalizedMode)
                .add(JSON_FIELD_RESET, reset);

        if (sessionId != null && !sessionId.isBlank()) {
            b.add(JSON_FIELD_SESSION_ID, sessionId.trim());
        }

        JsonArray normalized = normalizeAttachments(attachments);
        if (normalized != null && !normalized.isEmpty()) {
            b.add(JSON_FIELD_ATTACHMENTS, normalized);
        }

        return b.build();
    }

    private JsonArray normalizeAttachments(JsonArray attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Json.createArrayBuilder().build();
        }

        var out = Json.createArrayBuilder();
        for (JsonValue v : attachments) {
            if (v == null || v.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            JsonObject o = v.asJsonObject();

            String name = o.getString(JSON_FIELD_NAME, EMPTY).trim();
            String mime = o.getString(JSON_FIELD_MIME, EMPTY).trim();
            String contentString = o.getString(JSON_FIELD_CONTENT_STRING, EMPTY).trim();

            if (name.isBlank() || mime.isBlank() || contentString.isBlank()) {
                continue;
            }

            out.add(Json.createObjectBuilder()
                    .add(JSON_FIELD_NAME, name)
                    .add(JSON_FIELD_MIME, mime)
                    .add(JSON_FIELD_CONTENT_STRING, contentString)
                    .build());
        }
        return out.build();
    }

    private UpstreamResponse doPost(String url, String apiKey, String jsonBody, String requestId) throws IOException {
        try {
            String safeUrl = canonicalizeUrlInput(url);
            ServerDiagnosticsLog.write(
                DIAG_COMPONENT,
                requestId,
                DIAG_EVENT_HTTP_REQUEST,
                DIAG_METHOD_POST
                    + LINE_SEPARATOR + LOG_KV_URL + safeUrl
                    + LINE_SEPARATOR + DIAG_PREFIX_BODY_CHARS + (jsonBody == null ? 0 : jsonBody.length())
                    + LINE_SEPARATOR + DIAG_PREFIX_BODY + (jsonBody == null ? EMPTY : jsonBody)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(safeUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .header(HEADER_ACCEPT, MIME_APPLICATION_JSON)
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
                String responseBody = rsp.body() == null ? EMPTY : rsp.body();

                ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    DIAG_EVENT_HTTP_RESPONSE,
                    DIAG_METHOD_POST
                        + LINE_SEPARATOR + LOG_KV_URL + safeUrl
                        + LINE_SEPARATOR + DIAG_PREFIX_STATUS + rsp.statusCode()
                        + LINE_SEPARATOR + DIAG_PREFIX_CONTENT_TYPE + contentTypeOrDefault(rsp.headers())
                        + LINE_SEPARATOR + DIAG_PREFIX_RESPONSE_BODY + responseBody
                );

            return new UpstreamResponse(
                    rsp.statusCode(),
                    contentTypeOrDefault(rsp.headers()),
                    responseBody
            );

        } catch (HttpConnectTimeoutException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_TIMEOUT, MSG_TIMEOUT_CONNECT, url), e);
            throw new UpstreamConnectivityException(CODE_UPSTREAM_TIMEOUT, MSG_TIMEOUT_CONNECT, e);
        } catch (HttpTimeoutException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_TIMEOUT, MSG_TIMEOUT_CALL, url), e);
            throw new UpstreamConnectivityException(CODE_UPSTREAM_TIMEOUT, MSG_TIMEOUT_CALL, e);
        } catch (ConnectException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_CONNECT_FAILED, MSG_CONNECT_FAILED, url), e);
            throw new UpstreamConnectivityException(CODE_UPSTREAM_CONNECT_FAILED, MSG_CONNECT_FAILED, e);
        } catch (UnknownHostException | UnresolvedAddressException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_DNS_FAILED, MSG_DNS_FAILED, url), e);
            throw new UpstreamConnectivityException(CODE_UPSTREAM_DNS_FAILED, MSG_DNS_FAILED, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_INTERRUPTED, MSG_INTERRUPTED, url), e);
            throw new IOException(MSG_INTERRUPTED, e);
        } catch (IllegalArgumentException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_INVALID, url), e);
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_INVALID, e);
        } catch (UpstreamConnectivityException e) {
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(safe(e.code()), safe(e.getMessage()), url), e);
            throw e;
        } catch (SecurityException | IllegalStateException e) {
            LOG.log(Level.WARNING, LOG_UPSTREAM_PREFIX + requestId + LOG_UNEXPECTED_CLIENT_EXCEPTION_SUFFIX, e);
                ServerDiagnosticsLog.write(DIAG_COMPONENT, requestId, DIAG_EVENT_HTTP_ERROR,
                diagErrorMessage(CODE_UPSTREAM_RUNTIME, MSG_RUNTIME_FAILURE, url), e);
            throw new IOException(MSG_RUNTIME_FAILURE, e);
        }
    }

    private boolean isRouteMismatch(int status) {
        return status == 404 || status == 405;
    }

    private String siblingEndpoint(String url) {
        if (url == null) {
            return null;
        }
        if (url.endsWith(STREAM_CHAT_SUFFIX)) {
            return TextIoSanitizerUtil.safeSlice(url, 0, url.length() - STREAM_CHAT_SUFFIX.length()) + CHAT_SUFFIX;
        }
        if (url.endsWith(CHAT_SUFFIX)) {
            return TextIoSanitizerUtil.safeSlice(url, 0, url.length() - CHAT_SUFFIX.length()) + STREAM_CHAT_SUFFIX;
        }
        return null;
    }

    private String contentTypeOrDefault(HttpHeaders h) {
        return h.firstValue(HEADER_CONTENT_TYPE).orElse(DEFAULT_RESPONSE_CONTENT_TYPE);
    }

    private String safe(String value) {
        return value == null ? EMPTY : value;
    }

    private String diagErrorMessage(String code, String message, String url) {
        return DIAG_PREFIX_CODE + safe(code)
                + LINE_SEPARATOR + DIAG_PREFIX_MESSAGE + safe(message)
                + LINE_SEPARATOR + LOG_KV_URL + safe(url);
    }

    private String canonicalizeUrlInput(String value) throws IOException {
        String raw = value == null ? EMPTY : value;
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            return EMPTY;
        }
        if (normalized.length() > MAX_URL_CHARS) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_MAX_LENGTH, null);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new UpstreamConnectivityException(CODE_UPSTREAM_URL_INVALID, MSG_UPSTREAM_URL_CONTROL_CHARS, null);
        }
        return normalized;
    }

    public record UpstreamResponse(int statusCode, String contentType, String body) {

    }
}


