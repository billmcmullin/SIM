package com.sim.chatserver.security;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class SalesforceAuthClient {

    private final HttpClient httpClient;

    public SalesforceAuthClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public SalesforceAuthClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AuthResult refreshAccessToken() throws Exception {
        ServerConfig cfg = EncryptedDbConfigStore.load();
        if (cfg == null) {
            throw new IllegalStateException("ServerConfig not found.");
        }

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        String clientSecret = trimToNull(cfg.getSalesforceClientSecret());
        String refreshToken = trimToNull(cfg.getSalesforceRefreshToken());

        if (loginUrl == null || clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException("Missing Salesforce OAuth refresh configuration.");
        }

        String tokenUrl = normalizeBaseUrl(loginUrl) + "/services/oauth2/token";

        String form = "grant_type=refresh_token"
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&refresh_token=" + enc(refreshToken);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IllegalStateException("Salesforce token refresh failed: HTTP " + res.statusCode() + " body=" + res.body());
        }

        AuthResult result = parseAuthResult(res.body());
        if (result == null || isBlank(result.accessToken) || isBlank(result.instanceUrl)) {
            throw new IllegalStateException("Salesforce token refresh returned incomplete payload.");
        }

        // Persist new access token + instance URL
        cfg.setSalesforceApiKey(result.accessToken);
        cfg.setSalesforceInstanceUrl(result.instanceUrl);
        EncryptedDbConfigStore.save(cfg);

        return result;
    }

    private AuthResult parseAuthResult(String body) {
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject o = reader.readObject();
            AuthResult r = new AuthResult();
            r.accessToken = o.getString("access_token", null);
            r.instanceUrl = o.getString("instance_url", null);
            return r;
        }
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith("http://") && !x.startsWith("https://")) {
            x = "https://" + x;
        }
        return x.replaceAll("/+$", "");
    }

    private String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    public static final class AuthResult {

        public String accessToken;
        public String instanceUrl;
    }
}
