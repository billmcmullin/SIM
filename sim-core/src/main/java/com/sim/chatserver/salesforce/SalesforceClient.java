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
import com.sim.chatserver.security.AuthResult;
import com.sim.chatserver.security.SalesforceAuthClient;
import com.sim.chatserver.util.CommonLiteralConstants;
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

    private static final String EMPTY = "";


    private static final Logger log = Logger.getLogger(SalesforceClient.class.getCanonicalName());
    private static final String API_VERSION = "v61.0";
    private static final int MAX_ERROR_BODY_LEN = 512;
    private static final String LS = System.lineSeparator();
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String CONTENT_TYPE_JSON = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String AUTH_BEARER_PREFIX = CommonLiteralConstants.AUTH_BEARER_PREFIX;
    private static final String HTTP_PREFIX = CommonLiteralConstants.URL_PREFIX_HTTP;
    private static final String HTTPS_PREFIX = CommonLiteralConstants.URL_PREFIX_HTTPS;
    private static final String TRAILING_SLASH_REGEX = CommonLiteralConstants.REGEX_TRAILING_SLASHES;
    private static final String PATH_SERVICES_DATA = "/services/data/";
    private static final String PATH_QUERY_Q = "/query?q=";
    private static final String SINGLE_QUOTE = "'";
    private static final String ESCAPED_SINGLE_QUOTE = CommonLiteralConstants.ESCAPED_SINGLE_QUOTE;
    private static final String ELLIPSIS = CommonLiteralConstants.ELLIPSIS;
    private static final String SPACE = " ";
    private static final String DIAG_COMPONENT_SALESFORCE_CLIENT = "salesforce-client";
    private static final String DIAG_EVENT_QUERY_ERROR = "query-error";
    private static final String DIAG_EVENT_QUERY_REQUEST = "query-request";
    private static final String DIAG_EVENT_QUERY_RESPONSE = "query-response";
    private static final String LOG_KV_STATUS = CommonLiteralConstants.LOG_KV_STATUS;
    private static final String LOG_KV_BODY = CommonLiteralConstants.LOG_KV_BODY;
    private static final String LOG_KV_SOQL = "soql=";
    private static final String LOG_KV_METHOD_GET = "method=GET";
    private static final String LOG_KV_URL = CommonLiteralConstants.LOG_KV_URL;
    private static final String LOG_KV_MESSAGE = CommonLiteralConstants.LOG_KV_MESSAGE;
    private static final String MSG_MISSING_CONFIG = "Salesforce configuration is missing.";
    private static final String MSG_INVALID_SESSION_REFRESHING = "Salesforce session invalid; attempting token refresh.";
    private static final String MSG_REFRESH_SUCCESS = "Salesforce access token refreshed successfully.";
    private static final String MSG_QUERY_FAILED_AFTER_REFRESH_PREFIX = "Salesforce query failed after refresh with status ";
    private static final String MSG_QUERY_FAILED_PREFIX = "Salesforce query failed with status ";
    private static final String SOQL_SELECT_CONTACT_PREFIX = "SELECT Id, Name, Email, Phone, Title, Department, AccountId, LastModifiedDate ";
    private static final String SOQL_FROM_CONTACT = "FROM Contact ";
    private static final String SOQL_WHERE_NAME = "WHERE Name = ";
    private static final String SOQL_ORDER_BY_LAST_MODIFIED = "ORDER BY LastModifiedDate DESC ";
    private static final String SOQL_LIMIT_ONE = "LIMIT 1";
    private static final String JSON_FIELD_RECORDS = "records";
    private static final String JSON_FIELD_ID = "Id";
    private static final String JSON_FIELD_NAME = "Name";
    private static final String JSON_FIELD_EMAIL = "Email";
    private static final String JSON_FIELD_PHONE = "Phone";
    private static final String JSON_FIELD_TITLE = "Title";
    private static final String JSON_FIELD_DEPARTMENT = "Department";
    private static final String JSON_FIELD_ACCOUNT_ID = "AccountId";
    private static final String INVALID_SESSION_ID_TOKEN = "INVALID_SESSION_ID";
    private static final String INVALID_SESSION_TEXT = "Session expired or invalid";
    private static final String DESERIALIZATION_NOT_ALLOWED = CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED;

    private final HttpClient httpClient;
    private final SalesforceAuthClient authClient;

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.InvalidObjectException(DESERIALIZATION_NOT_ALLOWED);
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.InvalidObjectException(DESERIALIZATION_NOT_ALLOWED);
    }

    private SalesforceClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public static SalesforceClient create() {
        return new SalesforceClient();
    }

    private SalesforceClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.authClient = new SalesforceAuthClient(httpClient);
    }

    /**
     * Uses persisted Salesforce config and searches by friendly name.
     */
    private final SalesforceCustomerMatch findBestCustomerMatch(String friendlyName)
            throws IOException, InterruptedException, SQLException, SalesforceClientException {
        return findBestCustomerMatch(friendlyName, null, null);
    }

    public SalesforceCustomerMatch lookupBestCustomerMatch(String friendlyName)
            throws IOException, InterruptedException, SQLException, SalesforceClientException {
        return findBestCustomerMatch(friendlyName);
    }

    /**
     * Search Salesforce Contact by friendly name. If instanceUrl/apiKey are
     * null/blank, falls back to persisted config.
     */
    private SalesforceCustomerMatch findBestCustomerMatch(String friendlyName, String instanceUrl, String apiKey)
            throws IOException, InterruptedException, SQLException, SalesforceClientException {
        String searchName = trimToNull(friendlyName);
        if (searchName == null) {
            return null;
        }

        Credentials creds = resolveCredentials(instanceUrl, apiKey);
        if (creds == null) {
            throw new IllegalStateException(MSG_MISSING_CONFIG);
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
            log.info(MSG_INVALID_SESSION_REFRESHING);
            AuthResult refreshed = authClient.refreshAccessToken();
            log.info(MSG_REFRESH_SUCCESS);

            HttpResponse<String> second = executeQuery(refreshed.instanceUrl, refreshed.accessToken, soql, requestId);
            if (isSuccess(second.statusCode())) {
                return parseFirstRecord(second.body());
            }

            String body2 = safeErrorBody(second.body());
            throw new SalesforceClientException(
                    second.statusCode(),
                    body2 != null ? body2 : (MSG_QUERY_FAILED_AFTER_REFRESH_PREFIX + second.statusCode()));
        }

        String body = safeErrorBody(first.body());
        throw new SalesforceClientException(
                first.statusCode(),
            body != null ? body : (MSG_QUERY_FAILED_PREFIX + first.statusCode()));
    }

    private HttpResponse<String> executeQuery(String instanceUrl, String apiKey, String soql, String requestId)
            throws IOException, InterruptedException {
        String endpoint = buildQueryEndpoint(instanceUrl, soql);

        ServerDiagnosticsLog.write(
                DIAG_COMPONENT_SALESFORCE_CLIENT,
                requestId,
                DIAG_EVENT_QUERY_REQUEST,
            LOG_KV_METHOD_GET + LS + LOG_KV_URL + endpoint + LS + LOG_KV_SOQL + soql
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header(HEADER_AUTHORIZATION, AUTH_BEARER_PREFIX + apiKey)
                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT_SALESFORCE_CLIENT,
                    requestId,
                    DIAG_EVENT_QUERY_RESPONSE,
                    LOG_KV_STATUS + response.statusCode() + LS + LOG_KV_BODY + safeErrorBody(response.body())
            );
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT_SALESFORCE_CLIENT,
                    requestId,
                    DIAG_EVENT_QUERY_ERROR,
                    LOG_KV_URL + endpoint + LS + LOG_KV_MESSAGE + safe(e.getMessage()),
                    e
            );
            throw e;
        } catch (IOException e) {
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT_SALESFORCE_CLIENT,
                    requestId,
                    DIAG_EVENT_QUERY_ERROR,
                    LOG_KV_URL + endpoint + LS + LOG_KV_MESSAGE + safe(e.getMessage()),
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
            AuthResult refreshed = authClient.refreshAccessToken();
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
        if (!normalized.startsWith(HTTP_PREFIX) && !normalized.startsWith(HTTPS_PREFIX)) {
            normalized = HTTPS_PREFIX + normalized;
        }
        normalized = normalized.replaceAll(TRAILING_SLASH_REGEX, EMPTY);
        return normalized
            + PATH_SERVICES_DATA
                + API_VERSION
            + PATH_QUERY_Q
                + URLEncoder.encode(soql, StandardCharsets.UTF_8);
    }

    private String buildContactByNameSoql(String friendlyName) {
        String safe = friendlyName.replace(SINGLE_QUOTE, ESCAPED_SINGLE_QUOTE);
        return SOQL_SELECT_CONTACT_PREFIX
                + SOQL_FROM_CONTACT
            + SOQL_WHERE_NAME + SINGLE_QUOTE + safe + SINGLE_QUOTE + SPACE
                + SOQL_ORDER_BY_LAST_MODIFIED
                + SOQL_LIMIT_ONE;
    }

    private SalesforceCustomerMatch parseFirstRecord(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject root = reader.readObject();
            JsonArray records = root.getJsonArray(JSON_FIELD_RECORDS);
            if (records == null || records.isEmpty()) {
                return null;
            }

            JsonObject rec = records.getJsonObject(0);

            SalesforceCustomerMatch m = new SalesforceCustomerMatch();
            m.setContactId(getString(rec, JSON_FIELD_ID));
            m.setName(getString(rec, JSON_FIELD_NAME));
            m.setEmail(getString(rec, JSON_FIELD_EMAIL));
            m.setPhone(getString(rec, JSON_FIELD_PHONE));
            m.setTitle(getString(rec, JSON_FIELD_TITLE));
            m.setDepartment(getString(rec, JSON_FIELD_DEPARTMENT));
            m.setAccountId(getString(rec, JSON_FIELD_ACCOUNT_ID));
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
        return body.contains(INVALID_SESSION_ID_TOKEN) || body.contains(INVALID_SESSION_TEXT);
    }

    private String safeErrorBody(String body) {
        String b = trimToNull(body);
        if (b == null) {
            return null;
        }
        b = b.replace(System.lineSeparator(), SPACE).replace(System.lineSeparator(), SPACE);
        if (b.length() > MAX_ERROR_BODY_LEN) {
            return new String(b.toCharArray(), 0, MAX_ERROR_BODY_LEN) + ELLIPSIS;
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
        return value == null ? EMPTY : value;
    }
}

final class Credentials {

    final String instanceUrl;
    final String apiKey;

    Credentials(String instanceUrl, String apiKey) {
        this.instanceUrl = instanceUrl;
        this.apiKey = apiKey;
    }
}

