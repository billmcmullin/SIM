package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.service.ApiAuthResolver;
import com.sim.chatserver.service.ResolvedApiAuth;
import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.ThrowableUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/admin/test-connection"})
public class TestConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestConnectionServlet.class.getCanonicalName());
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final Pattern HOST_PATTERN = Pattern.compile("[A-Za-z0-9.-]{1,253}");
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");
    private static final String CHAT_PROBE_PAYLOAD = "{\"message\":\"connection test\",\"mode\":\"chat\",\"reset\":true}";
    private static final String CHAT_PROBE_PAYLOAD_NO_RESET = "{\"message\":\"connection test\",\"mode\":\"chat\",\"reset\":false}";
    private static final String CHAT_PROBE_PAYLOAD_MINIMAL_RESET = "{\"message\":\"connection test\",\"reset\":true}";
    private static final String CHAT_PROBE_PAYLOAD_MINIMAL_NO_RESET = "{\"message\":\"connection test\",\"reset\":false}";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String COMPONENT_TEST_CONNECTION = CommonLiteralConstants.COMPONENT_TEST_CONNECTION_SERVLET;
    private static final String TEXT_NO_VALID_API_KEY = CommonLiteralConstants.TEXT_NO_VALID_API_KEY;
    private static final String TEXT_UNAUTHORIZED = CommonLiteralConstants.TEXT_UNAUTHORIZED;
    private static final String MESSAGE_UNABLE_TO_WRITE_RESPONSE = CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE;
    private static final String LOG_FALLBACK_SEND_ERROR_FAILED = CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
    private static final String REQUEST_FAILED_MESSAGE = CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED;

    enum AuthHeaderMode {
        CUSTOM_HEADER,
        AUTH_BEARER,
        AUTH_RAW,
        X_API_KEY,
        AUTH_BEARER_AND_X_API_KEY
    }

    private enum ProbeKind {
        SYSTEM,
        CHAT
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED)
                    .build());
            return;
        }

        String host = ServletRequestParamUtil.firstParam(req, "serverHost", 512, true, true);
        String port = ServletRequestParamUtil.firstParam(req, "serverPort", 512, true, true);
        String apiKey = ServletRequestParamUtil.firstParam(req, "apiKey", 512, true, true);
        String workspaceName = ServletRequestParamUtil.firstParam(req, "workspaceName", 512, true, true);
        ServerConfig storedConfig = null;

        if (host == null || host.isBlank() || port == null || port.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Host and port are required.")
                    .build());
            return;
        }

        try {
            storedConfig = EncryptedDbConfigStore.load();
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to load stored server config for test-connection", e);
        }

        if ((workspaceName == null || workspaceName.isBlank()) && storedConfig != null) {
            workspaceName = storedConfig.getWorkspaceName();
        }

        ResolvedApiAuth resolvedAuth = ApiAuthResolver.resolveForServerConfigOutbound(apiKey);
        List<ResolvedApiAuth> authCandidates = buildAuthCandidates(resolvedAuth, null);
        if (authCandidates.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "API key is required.")
                    .build());
            return;
        }

        String baseUrl;
        try {
            baseUrl = buildBaseUrl(host.trim(), port.trim());
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP).log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION, ex);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Invalid host or port format.")
                    .build());
            return;
        }

        String requestId = UUID.randomUUID().toString();
        String systemEndpoint = baseUrl + "/api/v1/system";
        String workspaceSlug = buildSlug(workspaceName);
        String chatEndpoint = workspaceSlug.isBlank()
                ? ""
                : baseUrl + "/api/v1/workspace/" + URLEncoder.encode(workspaceSlug, StandardCharsets.UTF_8) + "/chat";

        try {
            ServerDiagnosticsLog.write(
                COMPONENT_TEST_CONNECTION,
                requestId,
                "http-request",
                "systemUrl=" + systemEndpoint
                        + LINE_SEPARATOR + "chatUrl=" + safe(chatEndpoint)
                        + LINE_SEPARATOR + "authSource=" + resolvedAuth.source()
                        + LINE_SEPARATOR + "preferredHeader=" + safe(resolvedAuth.preferredHeaderName())
                        + LINE_SEPARATOR + "authCandidates=" + summarizeAuthCandidates(authCandidates)
            );

            ProbeResponse systemResponse = executeProbe(systemEndpoint, authCandidates, ProbeKind.SYSTEM);

            ServerDiagnosticsLog.write(
                COMPONENT_TEST_CONNECTION,
                requestId,
                "http-response-system",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS + systemResponse.status
                        + LINE_SEPARATOR + "authMode=" + systemResponse.mode.name()
                        + LINE_SEPARATOR + "authSource=" + safe(systemResponse.authSource)
                        + LINE_SEPARATOR + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY + truncate(systemResponse.body)
            );

            if (systemResponse.status < 200 || systemResponse.status >= 300) {
                String failureMessage = buildProbeFailureMessage(
                    ProbeKind.SYSTEM,
                    systemResponse.status,
                    systemResponse.body,
                    workspaceSlug,
                    systemEndpoint
                );
                writeJson(resp, systemResponse.status, Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, failureMessage)
                        .add("probe", "system")
                        .add("upstreamStatus", systemResponse.status)
                        .add("authMode", systemResponse.mode.name())
                        .add("authSource", safe(systemResponse.authSource))
                        .add("upstreamBody", truncate(systemResponse.body))
                        .build());
                return;
            }

            if (chatEndpoint.isBlank()) {
                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Connection successful (system endpoint only; workspace not configured).")
                        .add("authSource", safe(systemResponse.authSource))
                        .build());
                return;
            }

            ProbeResponse chatResponse = executeProbe(chatEndpoint, authCandidates, ProbeKind.CHAT);

            if (isNullIdAbortResponse(chatResponse)) {
                ProbeResponse retryResponse = retryChatProbeWithAlternatePayloads(chatEndpoint, authCandidates, requestId);
                if (retryResponse != null) {
                    chatResponse = retryResponse;
                }
            }

            ServerDiagnosticsLog.write(
                COMPONENT_TEST_CONNECTION,
                requestId,
                "http-response-chat",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS + chatResponse.status
                        + LINE_SEPARATOR + "authMode=" + chatResponse.mode.name()
                    + LINE_SEPARATOR + "authSource=" + safe(chatResponse.authSource)
                        + LINE_SEPARATOR + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY + truncate(chatResponse.body)
            );

            if (chatResponse.status >= 200 && chatResponse.status < 300) {
                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Connection successful.")
                    .add("authSource", safe(chatResponse.authSource))
                        .build());
            } else {
                String failureMessage = buildProbeFailureMessage(
                    ProbeKind.CHAT,
                    chatResponse.status,
                    chatResponse.body,
                    workspaceSlug,
                    chatEndpoint
                );
                writeJson(resp, chatResponse.status, Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, failureMessage)
                        .add("probe", com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT)
                        .add("upstreamStatus", chatResponse.status)
                        .add("authMode", chatResponse.mode.name())
                        .add("authSource", safe(chatResponse.authSource))
                        .add("upstreamBody", truncate(chatResponse.body))
                        .build());
            }
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            if (causedByInterrupted(e)) {
                Thread.currentThread().interrupt();
            }
            String errorRef = UUID.randomUUID().toString();
            log.log(Level.WARNING,
                "Connection test failed. errorRef={0} requestId={1}. See server diagnostics log for stack trace.",
                new Object[]{errorRef, requestId});
            ServerDiagnosticsLog.write(
                    COMPONENT_TEST_CONNECTION,
                    requestId,
                    "http-error",
                    "errorRef=" + errorRef + LINE_SEPARATOR + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + safe(systemEndpoint) + LINE_SEPARATOR + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE + safe(e.getMessage()),
                    e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY, Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Connection test failed.")
                    .build());
        }
    
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, REQUEST_FAILED_MESSAGE);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private ProbeResponse executeProbe(String endpoint, List<ResolvedApiAuth> authCandidates, ProbeKind probeKind)
            {
        return executeProbe(endpoint, authCandidates, probeKind, CHAT_PROBE_PAYLOAD);
    }

    private ProbeResponse executeProbe(String endpoint, List<ResolvedApiAuth> authCandidates, ProbeKind probeKind, String chatPayload)
            {
        ProbeResponse lastResponse = null;

        for (int authIndex = 0; authIndex < authCandidates.size(); authIndex++) {
            ResolvedApiAuth auth = authCandidates.get(authIndex);
            AuthHeaderMode mode = resolvePrimaryAuthMode(auth.preferredHeaderName());
            HttpRequest request = buildProbeRequest(endpoint, auth, mode, probeKind, chatPayload);
            HttpResponse<String> response;
            try {
                response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Connection probe interrupted", e);
            } catch (IOException e) {
                throw new IllegalStateException("Connection probe transport failed", e);
            }
            int status = response.statusCode();
            lastResponse = new ProbeResponse(status, response.body() == null ? "" : response.body(), mode, auth.source());

            boolean hasMoreAuth = authIndex < (authCandidates.size() - 1);
            if (isAuthFailureStatus(status) && hasMoreAuth) {
                log.log(Level.INFO,
                        "Test connection auth fallback switching source after status={0} source={1}",
                        new Object[]{status, safe(auth.source())});
                continue;
            }

            return lastResponse;
        }

        if (lastResponse == null) {
            throw new IllegalStateException("Connection probe failed before receiving a response.");
        }
        return lastResponse;
    }

    private AuthHeaderMode resolvePrimaryAuthMode(String preferredHeaderName) {
        String header = preferredHeaderName == null ? "" : preferredHeaderName.trim();
        if (CommonLiteralConstants.HEADER_X_API_KEY_LOWER.equalsIgnoreCase(header)) {
            return AuthHeaderMode.X_API_KEY;
        }
        if (!header.isBlank() && !CommonLiteralConstants.HEADER_AUTHORIZATION_LOWER.equalsIgnoreCase(header)) {
            return AuthHeaderMode.CUSTOM_HEADER;
        }
        return AuthHeaderMode.AUTH_BEARER;
    }

    private HttpRequest buildProbeRequest(
            String endpoint,
            ResolvedApiAuth auth,
            AuthHeaderMode mode,
                ProbeKind probeKind,
                String chatPayload
    ) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header(com.sim.chatserver.util.CommonLiteralConstants.HEADER_ACCEPT, com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON);

        if (probeKind == ProbeKind.CHAT) {
                String payload = chatPayload == null || chatPayload.isBlank() ? CHAT_PROBE_PAYLOAD : chatPayload;
            builder.header(com.sim.chatserver.util.CommonLiteralConstants.HEADER_CONTENT_TYPE, com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }

        String token = auth == null ? null : auth.token();
        String safeToken = token == null ? "" : token;
        String rawValue = auth == null ? null : auth.rawValue();
        String preferredHeader = auth == null ? null : auth.preferredHeaderName();

        switch (mode) {
            case CUSTOM_HEADER -> applyPreferredHeader(builder, preferredHeader, rawValue, safeToken);
            case AUTH_RAW -> builder.header(CommonLiteralConstants.HEADER_AUTHORIZATION, normalizeRawAuthorizationValue(rawValue, safeToken));
            case X_API_KEY -> builder.header(CommonLiteralConstants.HEADER_X_API_KEY, safeToken);
            case AUTH_BEARER_AND_X_API_KEY -> builder
                .header(CommonLiteralConstants.HEADER_AUTHORIZATION, com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX + safeToken)
                .header(CommonLiteralConstants.HEADER_X_API_KEY, safeToken);
            case AUTH_BEARER -> builder.header(CommonLiteralConstants.HEADER_AUTHORIZATION, com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX + safeToken);
            default -> builder.header(CommonLiteralConstants.HEADER_AUTHORIZATION, com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX + safeToken);
        }

        return builder.build();
    }

    private void applyPreferredHeader(HttpRequest.Builder builder, String headerName, String rawValue, String token) {
        String normalizedHeader = headerName == null ? "" : headerName.trim();
        if (normalizedHeader.isBlank()) {
            return;
        }

        String normalizedKey = normalizedHeader.toLowerCase(Locale.ROOT);
        switch (normalizedKey) {
            case CommonLiteralConstants.HEADER_AUTHORIZATION_LOWER -> builder.header(CommonLiteralConstants.HEADER_AUTHORIZATION, com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX + token);
            case CommonLiteralConstants.HEADER_X_API_KEY_LOWER -> builder.header(CommonLiteralConstants.HEADER_X_API_KEY, token);
            default -> builder.header(normalizedHeader, defaultIfBlank(rawValue, token));
        }
    }

    private String normalizeRawAuthorizationValue(String rawValue, String token) {
        String raw = ApiAuthResolver.stripAuthorizationPrefix(rawValue);
        if (raw == null || raw.isBlank()) {
            return token;
        }
        return raw;
    }

    private boolean isAuthFailureStatus(int status) {
        return status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN;
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
            return "";
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

    private ProbeResponse retryChatProbeWithAlternatePayloads(
            String endpoint,
            List<ResolvedApiAuth> authCandidates,
            String requestId
        ) {
        String[] payloads = new String[]{
                CHAT_PROBE_PAYLOAD_NO_RESET,
                CHAT_PROBE_PAYLOAD_MINIMAL_RESET,
                CHAT_PROBE_PAYLOAD_MINIMAL_NO_RESET
        };

        ProbeResponse last = null;
        for (String payload : payloads) {
            ServerDiagnosticsLog.write(
                    COMPONENT_TEST_CONNECTION,
                    requestId,
                    "chat-probe-retry",
                    "reason=null-id-abort" + LINE_SEPARATOR + "payload=" + payload
            );
                ProbeResponse retry = executeProbe(endpoint, authCandidates, ProbeKind.CHAT, payload);
            last = retry;
            if (retry.status >= 200 && retry.status < 300) {
                return retry;
            }
            if (!isNullIdAbortResponse(retry)) {
                return retry;
            }
        }

        return last;
    }

    private boolean isNullIdAbortResponse(ProbeResponse response) {
        if (response == null || response.status != HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            return false;
        }
        String body = response.body == null ? "" : response.body;
        String normalized = body.toLowerCase(Locale.ROOT);
        return normalized.contains("cannot read properties of null") && normalized.contains("reading 'id'");
    }

    private String buildProbeFailureMessage(
            ProbeKind probeKind,
            int status,
            String responseBody,
            String workspaceSlug,
            String endpoint
    ) {
        String reason = extractUpstreamReason(responseBody);
        String lower = reason.toLowerCase(Locale.ROOT);
        String safeWorkspace = safe(workspaceSlug);

        if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN
            || lower.contains(TEXT_NO_VALID_API_KEY)
            || lower.contains(TEXT_UNAUTHORIZED)
                || lower.contains("forbidden")) {
            return "Authentication failed for " + probeLabel(probeKind)
                    + " endpoint. Verify the API key in Server and Workspace configuration.";
        }

        if (lower.contains("not a valid workspace") || lower.contains("workspace") && lower.contains("invalid")) {
            return "Workspace is invalid for this server: '" + defaultIfBlank(safeWorkspace, "(blank)")
                    + "'. Update workspace name in Server and Workspace settings.";
        }

        if (status == HttpServletResponse.SC_BAD_REQUEST) {
            return "Upstream rejected the " + probeLabel(probeKind)
                    + " request (HTTP 400). Verify server URL/path, workspace name, and proxy rules."
                    + suffixFromReason(reason);
        }

        return probeLabel(probeKind) + " endpoint failed with upstream HTTP " + status + suffixFromReason(reason)
                + ". endpoint=" + safe(endpoint);
    }

    private String probeLabel(ProbeKind probeKind) {
        return probeKind == ProbeKind.CHAT ? com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT : "system";
    }

    private String extractUpstreamReason(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }

        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject object = reader.readObject();
            String error = object.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR, "");
            if (!error.isBlank()) {
                return error;
            }
            String message = object.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "");
            if (!message.isBlank()) {
                return message;
            }
            String textResponse = object.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_TEXT_RESPONSE, "");
            if (!textResponse.isBlank()) {
                return textResponse;
            }
        } catch (JsonException | ClassCastException ignored) {
            log.log(Level.FINE, "Unable to parse structured error body; falling back to raw response.", ignored);
        }

        String trimmed = body.trim();
        return trimmed.length() > 300 ? new String(trimmed.toCharArray(), 0, 300) : trimmed;
    }

    private String suffixFromReason(String reason) {
        String value = safe(reason).trim();
        if (value.isBlank()) {
            return "";
        }
        return " Reason: " + value;
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String buildBaseUrl(String host, String port) {
        if (!PORT_PATTERN.matcher(port).matches()) {
            throw new IllegalArgumentException("Invalid port");
        }
        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid port", ex);
        }
        if (portNumber < 1 || portNumber > 65535) {
            throw new IllegalArgumentException("Port out of range");
        }

        String hostPart = sanitizeHost(host);
        if (hostPart == null || !HOST_PATTERN.matcher(hostPart).matches()) {
            throw new IllegalArgumentException("Invalid host");
        }

        String normalizedHost = host.trim();
        String scheme = extractScheme(normalizedHost);
        if (scheme == null) {
            scheme = (portNumber == 443 || portNumber == 8443) ? com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS : com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP;
        }

        return scheme + com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR + hostPart + (char) 58 + portNumber;
    }

    private String extractScheme(String rawHost) {
        if (rawHost == null) {
            return null;
        }
        String normalized = rawHost.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
            return com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS;
        }
        if (normalized.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP)) {
            return com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP;
        }
        return null;
    }

    private String buildSlug(String workspaceName) {
        if (workspaceName == null) {
            return "";
        }
        String normalized = workspaceName.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceFirst("^-+", "");
        normalized = normalized.replaceFirst("-+$", "");
        return normalized.isBlank() ? "" : normalized;
    }

    private String sanitizeHost(String rawHost) {
        if (rawHost == null || rawHost.isBlank()) {
            return null;
        }
        String trimmed = rawHost.trim();
        if (trimmed.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP)) {
            trimmed = trimmed.substring(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP.length());
        } else if (trimmed.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
            trimmed = trimmed.substring(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS.length());
        }

        int slash = trimmed.indexOf('/');
        if (slash >= 0) {
            trimmed = trimmed.substring(0, slash);
        }
        int colon = trimmed.indexOf((char) 58);
        if (colon >= 0) {
            trimmed = trimmed.substring(0, colon);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write test-connection JSON response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MESSAGE_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, LOG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 512 ? new String(value.toCharArray(), 0, 512) + com.sim.chatserver.util.CommonLiteralConstants.ELLIPSIS : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean causedByInterrupted(Throwable throwable) {
        return ThrowableUtil.hasInterruptedCause(throwable);
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }



}

final class ProbeResponse {
    final int status;
    final String body;
    final TestConnectionServlet.AuthHeaderMode mode;
    final String authSource;

    ProbeResponse(int status, String body, TestConnectionServlet.AuthHeaderMode mode, String authSource) {
        this.status = status;
        this.body = body;
        this.mode = mode;
        this.authSource = authSource;
    }
}

