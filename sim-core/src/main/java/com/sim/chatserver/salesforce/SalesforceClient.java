package com.sim.chatserver.salesforce;

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
import java.util.UUID;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.security.SalesforceAuthClient;
import com.sim.chatserver.util.ServerDiagnosticsLog;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * Small Salesforce REST client for customer profile sync.
 *
 * Strategy: - Read instance URL + API key from EncryptedDbConfigStore - Query
 * Contact by exact Name (friendlyName) - Return newest record (LastModifiedDate
 * DESC LIMIT 1)
 *
 * Added: - Auto-refresh token + one retry on INVALID_SESSION_ID
 */
public class SalesforceClient {

    private static final Logger log = Logger.getLogger(SalesforceClient.class.getName());
    private static final String API_VERSION = "v61.0";
    private static final int MAX_ERROR_BODY_LEN = 512;

    private final HttpClient httpClient;
    private final SalesforceAuthClient authClient;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public SalesforceClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public SalesforceClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.authClient = new SalesforceAuthClient(httpClient);
    }

    /**
     * Uses persisted Salesforce config and searches by friendly name.
     */
    public SalesforceCustomerMatch findBestCustomerMatch(String friendlyName)
            throws IOException, InterruptedException, SQLException, SalesforceClientException {
        return findBestCustomerMatch(friendlyName, null, null);
    }

    /**
     * Search Salesforce Contact by friendly name. If instanceUrl/apiKey are
     * null/blank, falls back to persisted config.
     */
    public SalesforceCustomerMatch findBestCustomerMatch(String friendlyName, String instanceUrl, String apiKey)
            throws IOException, InterruptedException, SQLException, SalesforceClientException {
        String searchName = trimToNull(friendlyName);
        if (searchName == null) {
            return null;
        }

        Credentials creds = resolveCredentials(instanceUrl, apiKey);
        if (creds == null) {
            throw new IllegalStateException("Salesforce configuration is missing.");
        }

        String soql = buildContactByNameSoql(searchName);
        String requestId = UUID.randomUUID().toString();

        // Attempt #1
        HttpResponse<String> first = executeQuery(creds.instanceUrl, creds.apiKey, soql, requestId);
        if (isSuccess(first.statusCode())) {
            return parseFirstRecord(first.body());
        }

        // If token/session expired, refresh and retry once
        if (isInvalidSession(first.statusCode(), first.body())) {
            log.info("Salesforce session invalid; attempting token refresh.");
            SalesforceAuthClient.AuthResult refreshed = authClient.refreshAccessToken();
            log.info("Salesforce access token refreshed successfully.");

            HttpResponse<String> second = executeQuery(refreshed.instanceUrl, refreshed.accessToken, soql, requestId);
            if (isSuccess(second.statusCode())) {
                return parseFirstRecord(second.body());
            }

            String body2 = safeErrorBody(second.body());
            throw new SalesforceClientException(
                    second.statusCode(),
                    body2 != null ? body2 : ("Salesforce query failed after refresh with status " + second.statusCode()));
        }

        String body = safeErrorBody(first.body());
        throw new SalesforceClientException(
                first.statusCode(),
                body != null ? body : ("Salesforce query failed with status " + first.statusCode()));
    }

    private HttpResponse<String> executeQuery(String instanceUrl, String apiKey, String soql, String requestId)
            throws IOException, InterruptedException {
        String endpoint = buildQueryEndpoint(instanceUrl, soql);

        ServerDiagnosticsLog.write(
                "salesforce-client",
                requestId,
                "query-request",
                "method=GET\nurl=" + endpoint + "\nsoql=" + soql
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            ServerDiagnosticsLog.write(
                    "salesforce-client",
                    requestId,
                    "query-response",
                    "status=" + response.statusCode() + "\nbody=" + safeErrorBody(response.body())
            );
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ServerDiagnosticsLog.write(
                    "salesforce-client",
                    requestId,
                    "query-error",
                    "url=" + endpoint + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            throw e;
        } catch (IOException e) {
            ServerDiagnosticsLog.write(
                    "salesforce-client",
                    requestId,
                    "query-error",
                    "url=" + endpoint + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            throw e;
        }
    }

        private Credentials resolveCredentials(String instanceUrl, String apiKey)
            throws IOException, InterruptedException, SQLException {
        String url = trimToNull(instanceUrl);
        String key = trimToNull(apiKey);

        if (url != null && key != null) {
            return new Credentials(url, key);
        }

        ServerConfig cfg = EncryptedDbConfigStore.load();
        if (cfg == null) {
            return null;
        }

        if (url == null) {
            url = trimToNull(cfg.getSalesforceInstanceUrl());
        }
        if (key == null) {
            key = trimToNull(cfg.getSalesforceApiKey());
        }

        // If no current access token is stored, attempt configured auth flows
        // (OAuth refresh or username/password/API token login).
        if (key == null) {
            SalesforceAuthClient.AuthResult refreshed = authClient.refreshAccessToken();
            if (refreshed != null) {
                key = trimToNull(refreshed.accessToken);
                if (url == null) {
                    url = trimToNull(refreshed.instanceUrl);
                }
            }
        }

        if (url == null || key == null) {
            return null;
        }

        return new Credentials(url, key);
    }

    private String buildQueryEndpoint(String instanceUrl, String soql) {
        String normalized = instanceUrl.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        normalized = normalized.replaceAll("/+$", "");
        return normalized
                + "/services/data/"
                + API_VERSION
                + "/query?q="
                + URLEncoder.encode(soql, StandardCharsets.UTF_8);
    }

    private String buildContactByNameSoql(String friendlyName) {
        String safe = friendlyName.replace("'", "\\'");
        return "SELECT Id, Name, Email, Phone, Title, Department, AccountId, LastModifiedDate "
                + "FROM Contact "
                + "WHERE Name = '" + safe + "' "
                + "ORDER BY LastModifiedDate DESC "
                + "LIMIT 1";
    }

    private SalesforceCustomerMatch parseFirstRecord(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject root = reader.readObject();
            JsonArray records = root.getJsonArray("records");
            if (records == null || records.isEmpty()) {
                return null;
            }

            JsonObject rec = records.getJsonObject(0);

            SalesforceCustomerMatch m = new SalesforceCustomerMatch();
            m.setContactId(getString(rec, "Id"));
            m.setName(getString(rec, "Name"));
            m.setEmail(getString(rec, "Email"));
            m.setPhone(getString(rec, "Phone"));
            m.setTitle(getString(rec, "Title"));
            m.setDepartment(getString(rec, "Department"));
            m.setAccountId(getString(rec, "AccountId"));
            m.setRawJson(rec.toString());

            return m;
        }
    }

    private String getString(JsonObject o, String key) {
        if (o == null || key == null || !o.containsKey(key)) {
            return null;
        }
        JsonValue v = o.get(key);
        if (v == null || v.getValueType() == JsonValue.ValueType.NULL) {
            return null;
        }
        if (v.getValueType() == JsonValue.ValueType.STRING) {
            return o.getString(key, null);
        }
        return v.toString();
    }

    private boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }

    private boolean isInvalidSession(int status, String body) {
        if (status != 401 && status != 403) {
            return false;
        }
        if (body == null) {
            return false;
        }
        return body.contains("INVALID_SESSION_ID") || body.contains("Session expired or invalid");
    }

    private String safeErrorBody(String body) {
        String b = trimToNull(body);
        if (b == null) {
            return null;
        }
        b = b.replace("\n", " ").replace("\r", " ");
        if (b.length() > MAX_ERROR_BODY_LEN) {
            return b.substring(0, MAX_ERROR_BODY_LEN) + "...";
        }
        return b;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class Credentials {

        final String instanceUrl;
        final String apiKey;

        Credentials(String instanceUrl, String apiKey) {
            this.instanceUrl = instanceUrl;
            this.apiKey = apiKey;
        }
    }

    public static class SalesforceClientException extends Exception {

        private final transient int statusCode;

        public SalesforceClientException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
