package com.sim.chatserver.web.admin;

import java.io.IOException;
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
import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.util.ServerDiagnosticsLog;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/admin/test-connection"})
public class TestConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestConnectionServlet.class.getName());
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

    private enum AuthHeaderMode {
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

    private static final class ProbeResponse {
        final int status;
        final String body;
        final AuthHeaderMode mode;
        final String authSource;

        private ProbeResponse(int status, String body, AuthHeaderMode mode, String authSource) {
            this.status = status;
            this.body = body;
            this.mode = mode;
            this.authSource = authSource;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build());
            return;
        }

        String host = firstParam(req, "serverHost");
        String port = firstParam(req, "serverPort");
        String apiKey = firstParam(req, "apiKey");
        String workspaceName = firstParam(req, "workspaceName");
        ServerConfig storedConfig = null;

        if (host == null || host.isBlank() || port == null || port.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Host and port are required.")
                    .build());
            return;
        }

        try {
            storedConfig = EncryptedDbConfigStore.load();
        } catch (SQLException | RuntimeException e) {
            log.log(Level.FINE, "Unable to load stored server config for test-connection", e);
        }

        if ((workspaceName == null || workspaceName.isBlank()) && storedConfig != null) {
            workspaceName = storedConfig.getWorkspaceName();
        }

        ApiAuthResolver.ResolvedApiAuth resolvedAuth = ApiAuthResolver.resolveForOutbound(apiKey);
        ApiAuthResolver.ResolvedApiAuth fallbackAuth = ApiAuthResolver.resolveForOutbound(null);
        List<ApiAuthResolver.ResolvedApiAuth> authCandidates = buildAuthCandidates(resolvedAuth, fallbackAuth);
        if (authCandidates.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "API key is required.")
                    .build());
            return;
        }

        String baseUrl;
        try {
            baseUrl = buildBaseUrl(host.trim(), port.trim());
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid host or port format.")
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
                "test-connection-servlet",
                requestId,
                "http-request",
                "systemUrl=" + systemEndpoint
                        + "\nchatUrl=" + safe(chatEndpoint)
                        + "\nauthSource=" + resolvedAuth.source()
                        + "\npreferredHeader=" + safe(resolvedAuth.preferredHeaderName())
                        + "\nauthCandidates=" + summarizeAuthCandidates(authCandidates)
            );

            ProbeResponse systemResponse = executeProbe(systemEndpoint, authCandidates, ProbeKind.SYSTEM);

            ServerDiagnosticsLog.write(
                "test-connection-servlet",
                requestId,
                "http-response-system",
                "status=" + systemResponse.status
                        + "\nauthMode=" + systemResponse.mode.name()
                        + "\nauthSource=" + safe(systemResponse.authSource)
                        + "\nbody=" + truncate(systemResponse.body)
            );

            if (systemResponse.status < 200 || systemResponse.status >= 300) {
                writeJson(resp, systemResponse.status, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "System endpoint probe failed with upstream status.")
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
                        .add("status", "ok")
                        .add("message", "Connection successful (system endpoint only; workspace not configured).")
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
                "test-connection-servlet",
                requestId,
                "http-response-chat",
                "status=" + chatResponse.status
                        + "\nauthMode=" + chatResponse.mode.name()
                    + "\nauthSource=" + safe(chatResponse.authSource)
                        + "\nbody=" + truncate(chatResponse.body)
            );

            if (chatResponse.status >= 200 && chatResponse.status < 300) {
                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("message", "Connection successful.")
                    .add("authSource", safe(chatResponse.authSource))
                        .build());
            } else {
                writeJson(resp, chatResponse.status, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Chat endpoint probe failed with upstream status.")
                        .add("probe", "chat")
                        .add("upstreamStatus", chatResponse.status)
                        .add("authMode", chatResponse.mode.name())
                        .add("authSource", safe(chatResponse.authSource))
                        .add("upstreamBody", truncate(chatResponse.body))
                        .build());
            }
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Connection test failed", e);
            ServerDiagnosticsLog.write(
                    "test-connection-servlet",
                    requestId,
                    "http-error",
                    "url=" + safe(systemEndpoint) + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Connection test failed.")
                    .build());
        }
    }

    private ProbeResponse executeProbe(String endpoint, List<ApiAuthResolver.ResolvedApiAuth> authCandidates, ProbeKind probeKind)
            throws IOException, InterruptedException {
        return executeProbe(endpoint, authCandidates, probeKind, CHAT_PROBE_PAYLOAD);
    }

    private ProbeResponse executeProbe(String endpoint, List<ApiAuthResolver.ResolvedApiAuth> authCandidates, ProbeKind probeKind, String chatPayload)
            throws IOException, InterruptedException {
        ProbeResponse lastResponse = null;

        for (int authIndex = 0; authIndex < authCandidates.size(); authIndex++) {
            ApiAuthResolver.ResolvedApiAuth auth = authCandidates.get(authIndex);
            AuthHeaderMode mode = resolvePrimaryAuthMode(auth.preferredHeaderName());
            HttpRequest request = buildProbeRequest(endpoint, auth, mode, probeKind, chatPayload);
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
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
            throw new IOException("Connection probe failed before receiving a response.");
        }
        return lastResponse;
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

    private HttpRequest buildProbeRequest(
            String endpoint,
            ApiAuthResolver.ResolvedApiAuth auth,
            AuthHeaderMode mode,
                ProbeKind probeKind,
                String chatPayload
    ) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");

        if (probeKind == ProbeKind.CHAT) {
                String payload = chatPayload == null || chatPayload.isBlank() ? CHAT_PROBE_PAYLOAD : chatPayload;
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }

        String token = auth == null ? null : auth.token();
        String rawValue = auth == null ? null : auth.rawValue();
        String preferredHeader = auth == null ? null : auth.preferredHeaderName();

        switch (mode) {
            case CUSTOM_HEADER -> applyPreferredHeader(builder, preferredHeader, rawValue, token);
            case AUTH_RAW -> builder.header("Authorization", normalizeRawAuthorizationValue(rawValue, token));
            case X_API_KEY -> builder.header("X-API-Key", token);
            case AUTH_BEARER_AND_X_API_KEY -> builder
                    .header("Authorization", "Bearer " + token)
                    .header("X-API-Key", token);
            case AUTH_BEARER -> builder.header("Authorization", "Bearer " + token);
        }

        return builder.build();
    }

    private void applyPreferredHeader(HttpRequest.Builder builder, String headerName, String rawValue, String token) {
        String normalizedHeader = headerName == null ? "" : headerName.trim();
        if (normalizedHeader.isBlank()) {
            return;
        }

        if ("authorization".equalsIgnoreCase(normalizedHeader)) {
            builder.header("Authorization", "Bearer " + token);
            return;
        }
        if ("x-api-key".equalsIgnoreCase(normalizedHeader)) {
            builder.header("X-API-Key", token);
            return;
        }

        String headerValue = rawValue;
        if (headerValue == null || headerValue.isBlank()) {
            headerValue = token;
        }
        builder.header(normalizedHeader, headerValue);
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
                sb.append(",");
            }
            sb.append(safe(auth.source()))
              .append('|')
              .append(safe(auth.preferredHeaderName()));
        }
        return sb.toString();
    }

    private ProbeResponse retryChatProbeWithAlternatePayloads(
            String endpoint,
            List<ApiAuthResolver.ResolvedApiAuth> authCandidates,
            String requestId
    ) throws IOException, InterruptedException {
        String[] payloads = new String[]{
                CHAT_PROBE_PAYLOAD_NO_RESET,
                CHAT_PROBE_PAYLOAD_MINIMAL_RESET,
                CHAT_PROBE_PAYLOAD_MINIMAL_NO_RESET
        };

        ProbeResponse last = null;
        for (String payload : payloads) {
            ServerDiagnosticsLog.write(
                    "test-connection-servlet",
                    requestId,
                    "chat-probe-retry",
                    "reason=null-id-abort\npayload=" + payload
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

    private String buildBaseUrl(String host, String port) {
        if (!PORT_PATTERN.matcher(port).matches()) {
            throw new IllegalArgumentException("Invalid port");
        }
        int portNumber = Integer.parseInt(port);
        if (portNumber < 1 || portNumber > 65535) {
            throw new IllegalArgumentException("Port out of range");
        }

        String hostPart = sanitizeHost(host);
        if (hostPart == null || !HOST_PATTERN.matcher(hostPart).matches()) {
            throw new IllegalArgumentException("Invalid host");
        }

        String normalizedHost = host == null ? "" : host.trim();
        String scheme = extractScheme(normalizedHost);
        if (scheme == null) {
            scheme = (portNumber == 443 || portNumber == 8443) ? "https" : "http";
        }

        return scheme + "://" + hostPart + ":" + portNumber;
    }

    private String extractScheme(String rawHost) {
        if (rawHost == null) {
            return null;
        }
        String normalized = rawHost.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("https://")) {
            return "https";
        }
        if (normalized.startsWith("http://")) {
            return "http";
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
        if (trimmed.startsWith("http://")) {
            trimmed = trimmed.substring("http://".length());
        } else if (trimmed.startsWith("https://")) {
            trimmed = trimmed.substring("https://".length());
        }

        int slash = trimmed.indexOf('/');
        if (slash >= 0) {
            trimmed = trimmed.substring(0, slash);
        }
        int colon = trimmed.indexOf(':');
        if (colon >= 0) {
            trimmed = trimmed.substring(0, colon);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }

        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }

        String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.length() > 512 ? normalized.substring(0, 512) : normalized;
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 512 ? value.substring(0, 512) + "..." : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
