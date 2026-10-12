package com.sim.chatserver.email;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Acquires and caches Microsoft Graph access token using client credentials
 * flow.
 */
public class GraphTokenClient {

    private static final Logger LOG = Logger.getLogger(GraphTokenClient.class.getCanonicalName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Object DIAG_LOCK = new Object();
    private static final String LS = System.lineSeparator();

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = "unused";
    private static final String COMPONENT_GRAPH_TOKEN_CLIENT = "graph-token-client";
    private static final String MSG_GRAPH_CONFIG_REQUIRED = "GraphEmailConfig is required";
    private static final String MSG_GRAPH_CONFIG_INCOMPLETE = "Graph config is incomplete (tenantId/clientId/clientSecret/senderUser required)";
    private static final String MSG_INVALID_GRAPH_CONFIG = "Invalid GraphEmailConfig";
    private static final String URL_PREFIX_HTTPS = "https://";
    private static final String TOKEN_PATH_SUFFIX = "/oauth2/v2.0/token";
    private static final String EVENT_TOKEN_REQUEST = "token-request";
    private static final String EVENT_TOKEN_RESPONSE = "token-response";
    private static final String EVENT_TOKEN_ERROR = "token-error";
    private static final String METHOD_POST = "POST";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String MIME_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String FORM_CLIENT_ID = "client_id=";
    private static final String FORM_CLIENT_SECRET = "&client_secret=";
    private static final String FORM_SCOPE = "&scope=";
    private static final String FORM_GRANT_TYPE_CLIENT_CREDENTIALS = "&grant_type=client_credentials";
    private static final String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
    private static final String KV_METHOD_POST = "method=POST";
    private static final String KV_URL = "url=";
    private static final String KV_STATUS = "status=";
    private static final String KV_BODY = "body=";
    private static final String ERR_BODY_SEGMENT = " body=";
    private static final String KV_MESSAGE = "message=";
    private static final String MSG_FAILED_GRAPH_TOKEN_HTTP_PREFIX = "Failed to acquire Graph token. HTTP ";
    private static final String ERR_GRAPH_TOKEN_HTTP_PREFIX = "graph_token_http_";
    private static final String JSON_ACCESS_TOKEN = "access_token";
    private static final String JSON_EXPIRES_IN = "expires_in";
    private static final String MSG_MISSING_ACCESS_TOKEN = "Graph token response missing access_token";
    private static final String ERR_MISSING_ACCESS_TOKEN = "missing_access_token";
    private static final String MSG_TOKEN_ACQUISITION_FAILED = "Graph token acquisition failed";
    private static final String ELLIPSIS = "...";
    private static final String REDACT_ACCESS_TOKEN_REGEX = "\"access_token\"\\s*:\\s*\"[^\"]*\"";
    private static final String REDACT_REFRESH_TOKEN_REGEX = "\"refresh_token\"\\s*:\\s*\"[^\"]*\"";
    private static final String REDACT_ACCESS_TOKEN_REPLACEMENT = "\"access_token\":\"[REDACTED]\"";
    private static final String REDACT_REFRESH_TOKEN_REPLACEMENT = "\"refresh_token\":\"[REDACTED]\"";
    private static final String FALLBACK_EVENT = "event";
    private static final String LOG_EXTENSION = ".log";
    private static final String ENTRY_SEPARATOR = "================================================================================";
    private static final String LABEL_TIMESTAMP = "Timestamp : ";
    private static final String LABEL_COMPONENT = "Component : ";
    private static final String LABEL_REQUEST_ID = "RequestId : ";
    private static final String LABEL_EVENT = "Event     : ";
    private static final String LABEL_DETAILS = "Details:";
    private static final String LABEL_STACK_TRACE = "Stack Trace:";
    private static final String REQUEST_ID_NONE = "(none)";
    private static final String LOG_DIAG_WRITE_FAILED_PREFIX = "Failed writing graph token diagnostics log. Disable with ";
    private static final String DIAG_DISABLED_SUFFIX = "=false";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_ONE = "1";
    private static final String BOOLEAN_YES = "yes";
    private static final String BOOLEAN_Y = "y";
    private static final String BOOLEAN_ON = "on";

    private static volatile boolean warnedDiagFailure;

    private final GraphEmailConfig config;

    private volatile String cachedAccessToken;
    private volatile Instant cachedExpiresAt;

    GraphTokenClient(GraphEmailConfig config) {
        this.config = Objects.requireNonNull(config, MSG_GRAPH_CONFIG_REQUIRED);
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    final synchronized String getAccessToken() {
        if (!config.isUsable()) {
            throw new EmailException(
                    MSG_GRAPH_CONFIG_INCOMPLETE,
                    new IllegalArgumentException(MSG_INVALID_GRAPH_CONFIG)
            );
        }

        if (cachedAccessToken != null && cachedExpiresAt != null
                && Instant.now().isBefore(cachedExpiresAt.minusSeconds(60))) {
            return cachedAccessToken;
        }

        TokenResponse tr = fetchToken();
        this.cachedAccessToken = tr.accessToken;
        this.cachedExpiresAt = Instant.now().plusSeconds(Math.max(60, tr.expiresIn));
        return this.cachedAccessToken;
    }

    private TokenResponse fetchToken() {
        HttpsURLConnection conn = null;
        String requestId = UUID.randomUUID().toString();
        try {
            String tokenUrl = URL_PREFIX_HTTPS + config.effectiveAuthorityHost().trim()
                    + '/' + enc(config.tenantId().trim())
                    + TOKEN_PATH_SUFFIX;

                writeDiagnostics(COMPONENT_GRAPH_TOKEN_CLIENT, requestId, EVENT_TOKEN_REQUEST, KV_METHOD_POST + LS + KV_URL + tokenUrl, null);

            conn = openConnection(tokenUrl);
            conn.setRequestMethod(METHOD_POST);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty(HEADER_CONTENT_TYPE, MIME_FORM_URLENCODED);

            String body = FORM_CLIENT_ID + enc(config.clientId().trim())
                    + FORM_CLIENT_SECRET + enc(config.clientSecret())
                    + FORM_SCOPE + enc(GRAPH_DEFAULT_SCOPE)
                    + FORM_GRANT_TYPE_CLIENT_CREDENTIALS;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            byte[] respBytes = (status >= 200 && status < 300)
                    ? conn.getInputStream().readAllBytes()
                    : (conn.getErrorStream() == null ? new byte[0] : conn.getErrorStream().readAllBytes());

            String respText = new String(respBytes, StandardCharsets.UTF_8);

                writeDiagnostics(COMPONENT_GRAPH_TOKEN_CLIENT, requestId, EVENT_TOKEN_RESPONSE,
                    KV_STATUS + status + LS + KV_BODY + truncate(redactTokenPayload(respText)), null);

            if (status < 200 || status >= 300) {
                throw new EmailException(
                        MSG_FAILED_GRAPH_TOKEN_HTTP_PREFIX + status + ERR_BODY_SEGMENT + respText,
                        new RuntimeException(ERR_GRAPH_TOKEN_HTTP_PREFIX + status)
                );
            }

            JsonNode root = MAPPER.readTree(respText);
            String accessToken = root.path(JSON_ACCESS_TOKEN).asText(EMPTY);
            long expiresIn = root.path(JSON_EXPIRES_IN).asLong(3600L);

            if (accessToken == null || accessToken.isBlank()) {
                throw new EmailException(
                        MSG_MISSING_ACCESS_TOKEN,
                        new RuntimeException(ERR_MISSING_ACCESS_TOKEN)
                );
            }

            return new TokenResponse(accessToken, expiresIn);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, MSG_TOKEN_ACQUISITION_FAILED, e);
            writeDiagnostics(COMPONENT_GRAPH_TOKEN_CLIENT, requestId, EVENT_TOKEN_ERROR, KV_MESSAGE + safe(e.getMessage()), e);
            throw new EmailException(MSG_TOKEN_ACQUISITION_FAILED, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpsURLConnection openConnection(String tokenUrl) throws IOException {
        URL url = URI.create(tokenUrl).toURL();
        return (HttpsURLConnection) url.openConnection();
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? EMPTY : v, StandardCharsets.UTF_8);
    }

    private String truncate(String body) {
        if (body == null) {
            return EMPTY;
        }
        return body.length() > 512 ? new String(body.toCharArray(), 0, 512) + ELLIPSIS : body;
    }

    private String safe(String value) {
        return value == null ? EMPTY : value;
    }

    private String redactTokenPayload(String payload) {
        String text = payload == null ? EMPTY : payload;
        text = text.replaceAll(REDACT_ACCESS_TOKEN_REGEX, REDACT_ACCESS_TOKEN_REPLACEMENT);
        text = text.replaceAll(REDACT_REFRESH_TOKEN_REGEX, REDACT_REFRESH_TOKEN_REPLACEMENT);
        return text;
    }

    private void writeDiagnostics(String component, String requestId, String event, String details, Throwable error) {
        if (!diagnosticsEnabled()) {
            return;
        }

        String comp = safeToken(component, COMPONENT_GRAPH_TOKEN_CLIENT);
        String req = safeToken(requestId, EMPTY);
        String evt = safeToken(event, FALLBACK_EVENT);

        synchronized (DIAG_LOCK) {
            try {
                Path dir = resolveDiagnosticsDir();
                Files.createDirectories(dir);

                String fileName = safeFileToken(comp) + '-' + LocalDate.now() + LOG_EXTENSION;
                Path file = dir.resolve(fileName);

                String lineSep = System.lineSeparator();
                StringBuilder entry = new StringBuilder(1024);
                entry.append(ENTRY_SEPARATOR)
                        .append(lineSep)
                    .append(LABEL_TIMESTAMP).append(Instant.now()).append(lineSep)
                    .append(LABEL_COMPONENT).append(comp).append(lineSep)
                    .append(LABEL_REQUEST_ID).append(req.isBlank() ? REQUEST_ID_NONE : req).append(lineSep)
                    .append(LABEL_EVENT).append(evt).append(lineSep);

                if (details != null && !details.isBlank()) {
                    entry.append(LABEL_DETAILS).append(lineSep)
                            .append(details)
                            .append(lineSep);
                }

                if (error != null) {
                    StringWriter sw = new StringWriter();
                    error.printStackTrace(new PrintWriter(sw));
                    entry.append(LABEL_STACK_TRACE)
                            .append(lineSep)
                            .append(sw)
                            .append(lineSep);
                }

                entry.append(ENTRY_SEPARATOR)
                        .append(lineSep)
                        .append(lineSep);

                Files.writeString(
                        file,
                        entry,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE
                );
            } catch (IOException ex) {
                if (!warnedDiagFailure) {
                    warnedDiagFailure = true;
                    LOG.log(Level.WARNING,
                            LOG_DIAG_WRITE_FAILED_PREFIX + ENV_ENABLED + DIAG_DISABLED_SUFFIX,
                            ex);
                }
            }
        }
    }

    private boolean diagnosticsEnabled() {
        String enabledRaw = readEnvCanonical(ENV_ENABLED);
        return isTruthy(enabledRaw);
    }

    private String readEnvCanonical(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalizedKey = key.trim();
        String raw = new ProcessBuilder().environment().get(normalizedKey);
        if (raw == null) {
            return null;
        }
        String canonical = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replace('\u0000', ' ')
                .replace(System.lineSeparator(), EMPTY)
                .replace(System.lineSeparator(), EMPTY)
                .trim();
        if (canonical.isEmpty()) {
            return null;
        }
        return canonical.length() > 128 ? new String(canonical.toCharArray(), 0, 128) : canonical;
    }

    private Path resolveDiagnosticsDir() {
        return Paths.get(DEFAULT_DIR_NAME).toAbsolutePath().normalize();
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? EMPTY : value.trim().toLowerCase(Locale.ROOT);
        return BOOLEAN_TRUE.equals(normalized)
            || BOOLEAN_ONE.equals(normalized)
            || BOOLEAN_YES.equals(normalized)
            || BOOLEAN_Y.equals(normalized)
            || BOOLEAN_ON.equals(normalized);
    }

    private String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace(System.lineSeparator().charAt(0), ' ').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String safeFileToken(String value) {
        String input = value == null ? COMPONENT_GRAPH_TOKEN_CLIENT : value;
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.isEmpty() ? COMPONENT_GRAPH_TOKEN_CLIENT : out.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }



}

final class TokenResponse {

    final String accessToken;
    final long expiresIn;

    TokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}

