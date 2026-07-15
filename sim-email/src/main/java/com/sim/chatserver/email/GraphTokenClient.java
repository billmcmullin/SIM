package com.sim.chatserver.email;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
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

    private final GraphEmailConfig config;

    private volatile String cachedAccessToken;
    private volatile Instant cachedExpiresAt;

    public GraphTokenClient(GraphEmailConfig config) {
        this.config = Objects.requireNonNull(config, "GraphEmailConfig is required");
    }

    public synchronized String getAccessToken() {
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

        try {
            TokenResponse tr = fetchToken();
            this.cachedAccessToken = tr.accessToken;
            this.cachedExpiresAt = Instant.now().plusSeconds(Math.max(60, tr.expiresIn));
            return this.cachedAccessToken;
        } catch (RuntimeException e) {
            if (e instanceof EmailException ee) {
                throw ee;
            }
            throw new EmailException("Graph token acquisition failed", e);
        }
    }

    private TokenResponse fetchToken() {
        HttpsURLConnection conn = null;
        try {
            String tokenUrl = "https://" + config.effectiveAuthorityHost().trim()
                    + "/" + enc(config.tenantId().trim())
                    + "/oauth2/v2.0/token";

            URL url = URI.create(tokenUrl).toURL();
            conn = (HttpsURLConnection) url.openConnection();
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
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.SEVERE, "Graph token acquisition failed", e);
            if (e instanceof EmailException ee) {
                throw ee;
            }
            throw new EmailException("Graph token acquisition failed", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
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
