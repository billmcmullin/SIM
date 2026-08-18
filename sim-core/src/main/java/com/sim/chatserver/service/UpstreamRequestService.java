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

import com.sim.chatserver.util.ServerDiagnosticsLog;

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

    private static final Logger LOG = Logger.getLogger(UpstreamRequestService.class.getName());

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);

    private static final String STREAM_CHAT_SUFFIX = "/stream-chat";
    private static final String CHAT_SUFFIX = "/chat";
    private static final String FIXED_PATH_TEMPLATE = "/api/v1/workspace/%s/chat"; // canonical /chat
    private static final int MAX_URL_CHARS = 2048;
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    /**
     * Backward-compatible overload. If upstreamUrl is full endpoint, used
     * as-is. If it's origin-only, workspace is unknown and will fail fast.
     */
    final UpstreamResponse sendChat(
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
    final UpstreamResponse sendChat(
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
                        "[upstream][{0}] fallback endpoint attempt firstStatus={1} from={2} to={3}",
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
            String b = resp.body() == null ? "" : resp.body().toLowerCase(Locale.ROOT);
            return b.contains("too large")
                    || b.contains("token")
                    || b.contains("context length")
                    || b.contains("maximum context")
                    || b.contains("payload too large");
        }
        return false;
    }

    private String resolveUpstreamUrl(String upstreamBaseOrEndpoint, String workspace) throws IOException {
        if (upstreamBaseOrEndpoint == null || upstreamBaseOrEndpoint.isBlank()) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL/base is missing", null);
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
                    "UPSTREAM_WORKSPACE_MISSING",
                    "Workspace is required when upstream URL is base origin only",
                    null
            );
        }

        String origin = extractOrigin(raw);
        if (origin.isBlank()) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream base URL invalid", null);
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

        s = s.replaceFirst("^(https?://)(https?://)+", "$1");
        s = s.replace("https://https://", "https://")
                .replace("http://http://", "http://")
                .replace("http://https://", "https://")
                .replace("https://http://", "http://");

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
            return endpointUrl.substring(0, endpointUrl.length() - STREAM_CHAT_SUFFIX.length()) + CHAT_SUFFIX;
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
                return "";
            }

            StringBuilder b = new StringBuilder()
                    .append(scheme.toLowerCase(Locale.ROOT))
                    .append("://")
                    .append(host.toLowerCase(Locale.ROOT));
            if (port > 0) {
                b.append(':').append(port);
            }

            return b.toString();
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            LOG.log(Level.FINE, "Failed to extract upstream origin", e);
            return "";
        }
    }

    private void validateHttpUrl(String raw) throws IOException {
        URI u = parseHttpUri(raw);
        String scheme = u.getScheme();
        String host = u.getHost();

        if (scheme == null || host == null || host.isBlank()) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL missing scheme/host", null);
        }

        String s = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(s) && !"https".equals(s)) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL scheme must be http/https", null);
        }
    }

    private URI parseHttpUri(String raw) throws IOException {
        String normalized = canonicalizeUrlInput(raw);
        if (normalized.isBlank()) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL is blank", null);
        }
        try {
            return URI.create(normalized);
        } catch (IllegalArgumentException e) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL syntax invalid", e);
        }
    }

    private String normalizeWorkspace(String workspace) throws IOException {
        String ws = workspace == null ? "" : workspace.trim();
        if (ws.isBlank()) {
            return "";
        }
        if (!ws.matches("[A-Za-z0-9._-]{1,80}")) {
            throw new UpstreamConnectivityException("UPSTREAM_WORKSPACE_INVALID", "Workspace slug format invalid", null);
        }
        return ws;
    }

    private String stripTrailingSlash(String s) {
        String out = s;
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
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
        String normalizedMode = (mode == null || mode.isBlank()) ? "chat" : mode.trim().toLowerCase(Locale.ROOT);
        if (!normalizedMode.equals("chat") && !normalizedMode.equals("query") && !normalizedMode.equals("automatic")) {
            normalizedMode = "chat";
        }

        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("message", message == null ? "" : message)
                .add("mode", normalizedMode)
                .add("reset", reset);

        if (sessionId != null && !sessionId.isBlank()) {
            b.add("sessionId", sessionId.trim());
        }

        JsonArray normalized = normalizeAttachments(attachments);
        if (normalized != null && !normalized.isEmpty()) {
            b.add("attachments", normalized);
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

            String name = o.getString("name", "").trim();
            String mime = o.getString("mime", "").trim();
            String contentString = o.getString("contentString", "").trim();

            if (name.isBlank() || mime.isBlank() || contentString.isBlank()) {
                continue;
            }

            out.add(Json.createObjectBuilder()
                    .add("name", name)
                    .add("mime", mime)
                    .add("contentString", contentString)
                    .build());
        }
        return out.build();
    }

    private UpstreamResponse doPost(String url, String apiKey, String jsonBody, String requestId) throws IOException {
        try {
            String safeUrl = canonicalizeUrlInput(url);
            ServerDiagnosticsLog.write(
                "upstream-request-service",
                requestId,
                "http-request",
                "method=POST"
                    + "\nurl=" + safeUrl
                    + "\nbodyChars=" + (jsonBody == null ? 0 : jsonBody.length())
                    + "\nbody=" + (jsonBody == null ? "" : jsonBody)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(safeUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
                String responseBody = rsp.body() == null ? "" : rsp.body();

                ServerDiagnosticsLog.write(
                    "upstream-request-service",
                    requestId,
                    "http-response",
                    "method=POST"
                        + "\nurl=" + safeUrl
                        + "\nstatus=" + rsp.statusCode()
                        + "\ncontentType=" + contentTypeOrDefault(rsp.headers())
                        + "\nresponseBody=" + responseBody
                );

            return new UpstreamResponse(
                    rsp.statusCode(),
                    contentTypeOrDefault(rsp.headers()),
                    responseBody
            );

        } catch (HttpConnectTimeoutException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_TIMEOUT\nmessage=Timed out connecting to upstream\nurl=" + safe(url), e);
            throw new UpstreamConnectivityException("UPSTREAM_TIMEOUT", "Timed out connecting to upstream", e);
        } catch (HttpTimeoutException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_TIMEOUT\nmessage=Timed out calling upstream\nurl=" + safe(url), e);
            throw new UpstreamConnectivityException("UPSTREAM_TIMEOUT", "Timed out calling upstream", e);
        } catch (ConnectException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_CONNECT_FAILED\nmessage=TCP connect failed\nurl=" + safe(url), e);
            throw new UpstreamConnectivityException("UPSTREAM_CONNECT_FAILED", "TCP connect failed", e);
        } catch (UnknownHostException | UnresolvedAddressException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_DNS_FAILED\nmessage=DNS/host resolution failed\nurl=" + safe(url), e);
            throw new UpstreamConnectivityException("UPSTREAM_DNS_FAILED", "DNS/host resolution failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_INTERRUPTED\nmessage=Interrupted while calling upstream\nurl=" + safe(url), e);
            throw new IOException("Interrupted while calling upstream", e);
        } catch (IllegalArgumentException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_URL_INVALID\nmessage=Upstream URL invalid\nurl=" + safe(url), e);
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL invalid", e);
        } catch (UpstreamConnectivityException e) {
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=" + safe(e.code()) + "\nmessage=" + safe(e.getMessage()) + "\nurl=" + safe(url), e);
            throw e;
        } catch (SecurityException | IllegalStateException e) {
            LOG.log(Level.WARNING, "[upstream][" + requestId + "] unexpected client exception", e);
                ServerDiagnosticsLog.write("upstream-request-service", requestId, "http-error",
                    "code=UPSTREAM_RUNTIME\nmessage=Unexpected upstream HTTP client failure\nurl=" + safe(url), e);
            throw new IOException("Unexpected upstream HTTP client failure", e);
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
            return url.substring(0, url.length() - STREAM_CHAT_SUFFIX.length()) + CHAT_SUFFIX;
        }
        if (url.endsWith(CHAT_SUFFIX)) {
            return url.substring(0, url.length() - CHAT_SUFFIX.length()) + STREAM_CHAT_SUFFIX;
        }
        return null;
    }

    private String contentTypeOrDefault(HttpHeaders h) {
        return h.firstValue("Content-Type").orElse("application/json; charset=UTF-8");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String canonicalizeUrlInput(String value) throws IOException {
        String raw = value == null ? "" : value;
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.length() > MAX_URL_CHARS) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL exceeds max length", null);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new UpstreamConnectivityException("UPSTREAM_URL_INVALID", "Upstream URL contains control characters", null);
        }
        return normalized;
    }

    public record UpstreamResponse(int statusCode, String contentType, String body) {

    }

    public static class UpstreamConnectivityException extends IOException {

        private final transient String code;

        UpstreamConnectivityException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        final String code() {
            return code == null ? "" : code;
        }
    }
}
