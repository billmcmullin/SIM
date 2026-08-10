package com.sim.chatserver.email;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
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

    private static final Logger LOG = Logger.getLogger(GraphTokenClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Object DIAG_LOCK = new Object();

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final String PROP_ENABLED = "sim.server.diagnostic.log.enabled";

    private static volatile boolean warnedDiagFailure;

    private final GraphEmailConfig config;

    private volatile String cachedAccessToken;
    private volatile Instant cachedExpiresAt;

    GraphTokenClient(GraphEmailConfig config) {
        this.config = Objects.requireNonNull(config, "GraphEmailConfig is required");
    }

    final synchronized String getAccessToken() {
        if (!config.isUsable()) {
            throw new EmailException(
                    "Graph config is incomplete (tenantId/clientId/clientSecret/senderUser required)",
                    new IllegalArgumentException("Invalid GraphEmailConfig")
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
            String tokenUrl = "https://" + config.effectiveAuthorityHost().trim()
                    + '/' + enc(config.tenantId().trim())
                    + "/oauth2/v2.0/token";

                writeDiagnostics("graph-token-client", requestId, "token-request", "method=POST\nurl=" + tokenUrl, null);

            conn = openConnection(tokenUrl);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "client_id=" + enc(config.clientId().trim())
                    + "&client_secret=" + enc(config.clientSecret())
                    + "&scope=" + enc("https://graph.microsoft.com/.default")
                    + "&grant_type=client_credentials";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            byte[] respBytes = (status >= 200 && status < 300)
                    ? conn.getInputStream().readAllBytes()
                    : (conn.getErrorStream() == null ? new byte[0] : conn.getErrorStream().readAllBytes());

            String respText = new String(respBytes, StandardCharsets.UTF_8);

                writeDiagnostics("graph-token-client", requestId, "token-response",
                    "status=" + status + "\nbody=" + truncate(redactTokenPayload(respText)), null);

            if (status < 200 || status >= 300) {
                throw new EmailException(
                        "Failed to acquire Graph token. HTTP " + status + " body=" + respText,
                        new RuntimeException("graph_token_http_" + status)
                );
            }

            JsonNode root = MAPPER.readTree(respText);
            String accessToken = root.path("access_token").asText("");
            long expiresIn = root.path("expires_in").asLong(3600L);

            if (accessToken == null || accessToken.isBlank()) {
                throw new EmailException(
                        "Graph token response missing access_token",
                        new RuntimeException("missing_access_token")
                );
            }

            return new TokenResponse(accessToken, expiresIn);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Graph token acquisition failed", e);
            writeDiagnostics("graph-token-client", requestId, "token-error", "message=" + safe(e.getMessage()), e);
            throw new EmailException("Graph token acquisition failed", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    HttpsURLConnection openConnection(String tokenUrl) throws IOException {
        URL url = URI.create(tokenUrl).toURL();
        return (HttpsURLConnection) url.openConnection();
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 512 ? body.substring(0, 512) + "..." : body;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String redactTokenPayload(String payload) {
        String text = payload == null ? "" : payload;
        text = text.replaceAll("\"access_token\"\\s*:\\s*\"[^\"]*\"", "\"access_token\":\"[REDACTED]\"");
        text = text.replaceAll("\"refresh_token\"\\s*:\\s*\"[^\"]*\"", "\"refresh_token\":\"[REDACTED]\"");
        return text;
    }

    private void writeDiagnostics(String component, String requestId, String event, String details, Throwable error) {
        if (!diagnosticsEnabled()) {
            return;
        }

        String comp = safeToken(component, "graph-token-client");
        String req = safeToken(requestId, "");
        String evt = safeToken(event, "event");

        synchronized (DIAG_LOCK) {
            try {
                Path dir = resolveDiagnosticsDir();
                Files.createDirectories(dir);

                String fileName = safeFileToken(comp) + '-' + LocalDate.now() + ".log";
                Path file = dir.resolve(fileName);

                String lineSep = System.lineSeparator();
                StringBuilder entry = new StringBuilder(1024);
                entry.append("================================================================================")
                        .append(lineSep)
                        .append("Timestamp : ").append(Instant.now()).append(lineSep)
                        .append("Component : ").append(comp).append(lineSep)
                        .append("RequestId : ").append(req.isBlank() ? "(none)" : req).append(lineSep)
                        .append("Event     : ").append(evt).append(lineSep);

                if (details != null && !details.isBlank()) {
                    entry.append("Details:").append(lineSep)
                            .append(details)
                            .append(lineSep);
                }

                if (error != null) {
                    StringWriter sw = new StringWriter();
                    error.printStackTrace(new PrintWriter(sw));
                    entry.append("Stack Trace:")
                            .append(lineSep)
                            .append(sw)
                            .append(lineSep);
                }

                entry.append("================================================================================")
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
                            "Failed writing graph token diagnostics log. Disable with " + ENV_ENABLED + "=false",
                            ex);
                }
            }
        }
    }

    private boolean diagnosticsEnabled() {
        String enabledRaw = trimToNull(System.getProperty(PROP_ENABLED));
        return isTruthy(enabledRaw);
    }

    private Path resolveDiagnosticsDir() {
        return Paths.get(DEFAULT_DIR_NAME).toAbsolutePath().normalize();
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "on".equals(normalized);
    }

    private String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace('\r', ' ').replace('\n', ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String safeFileToken(String value) {
        String input = value == null ? "graph-token-client" : value;
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
        return out.isEmpty() ? "graph-token-client" : out.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class TokenResponse {

        final String accessToken;
        final long expiresIn;

        private TokenResponse(String accessToken, long expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }
    }
}
