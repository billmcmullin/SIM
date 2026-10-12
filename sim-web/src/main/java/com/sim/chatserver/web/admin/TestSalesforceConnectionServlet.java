package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.security.AuthResult;
import com.sim.chatserver.security.SalesforceAuthClient;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "TestSalesforceConnectionServlet", urlPatterns = {"/admin/test-salesforce-connection"})
public class TestSalesforceConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestSalesforceConnectionServlet.class.getCanonicalName());
    private static final String LS = System.lineSeparator();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR).add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED).build());
            return;
        }

        String instanceUrl = ServletRequestParamUtil.firstParam(req, "salesforceInstanceUrl", 512, true, true);
        String apiKey = ServletRequestParamUtil.firstParam(req, "salesforceApiKey", 4096, true, true);
        String loginUrl = ServletRequestParamUtil.firstParam(req, "salesforceLoginUrl", 512, true, true);
        String username = ServletRequestParamUtil.firstParam(req, "salesforceUsername", 256, true, true);
        String password = ServletRequestParamUtil.firstParam(req, "salesforcePassword", 4096, true, true);
        String apiToken = ServletRequestParamUtil.firstParam(req, "salesforceApiToken", 4096, true, true);

        // fallback to stored values
        if (isBlank(instanceUrl) || isBlank(apiKey) || isBlank(loginUrl)
                || isBlank(username) || isBlank(password) || isBlank(apiToken)) {
            try {
                ServerConfig config = loadConfig();
                if (isBlank(instanceUrl) && config != null) {
                    instanceUrl = config.getSalesforceInstanceUrl();
                }
                if (isBlank(apiKey) && config != null) {
                    apiKey = config.getSalesforceApiKey();
                }
                if (isBlank(loginUrl) && config != null) {
                    loginUrl = config.getSalesforceLoginUrl();
                }
                if (isBlank(username) && config != null) {
                    username = config.getSalesforceUsername();
                }
                if (isBlank(password) && config != null) {
                    password = config.getSalesforcePassword();
                }
                if (isBlank(apiToken) && config != null) {
                    apiToken = config.getSalesforceApiToken();
                }
                } catch (IllegalStateException | IllegalArgumentException e) {
                log.log(Level.WARNING, "Unable to load stored Salesforce configuration", e);
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to load stored Salesforce configuration.").build());
                return;
            }
        }

        // If direct access token is not available, try username/password/API-token login flow.
        if (isBlank(apiKey)) {
            ServerConfig authConfig = new ServerConfig();
            authConfig.setSalesforceLoginUrl(loginUrl);
            authConfig.setSalesforceUsername(username);
            authConfig.setSalesforcePassword(password);
            authConfig.setSalesforceApiToken(apiToken);

            try {
                AuthResult authResult = new SalesforceAuthClient(getHttpClient())
                        .refreshAccessToken(authConfig);
                apiKey = authResult == null ? null : authResult.accessToken;
                if (isBlank(instanceUrl)) {
                    instanceUrl = authResult == null ? null : authResult.instanceUrl;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.log(Level.WARNING, "Salesforce token acquisition failed during connection test", e);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to acquire Salesforce access token from configured credentials.").build());
                return;
                } catch (IOException | SQLException | IllegalStateException | IllegalArgumentException | SecurityException e) {
                log.log(Level.WARNING, "Salesforce token acquisition failed during connection test", e);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to acquire Salesforce access token from configured credentials.").build());
                return;
            }
        }

        if (isBlank(instanceUrl)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Salesforce instance URL is required.").build());
            return;
        }

        if (isBlank(apiKey)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Salesforce API key is required.").build());
            return;
        }

        String normalizedInstanceUrl = instanceUrl == null ? "" : instanceUrl.trim();
        String normalizedApiKey = apiKey == null ? "" : apiKey.trim();

        String endpoint = buildSalesforceDataEndpoint(normalizedInstanceUrl);
        String requestId = UUID.randomUUID().toString();

        try {
            ServerDiagnosticsLog.write(
                "test-salesforce-connection-servlet",
                requestId,
                "http-request",
                "method=GET" + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + endpoint
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header(CommonLiteralConstants.HEADER_AUTHORIZATION, com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX + normalizedApiKey)
                    .header(com.sim.chatserver.util.CommonLiteralConstants.HEADER_ACCEPT, com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .GET()
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            ServerDiagnosticsLog.write(
                "test-salesforce-connection-servlet",
                requestId,
                "http-response",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS + response.statusCode() + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY + truncate(response.body())
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                writeJson(resp, HttpServletResponse.SC_OK,
                        Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Salesforce connection successful.").build());
            } else {
                String body = response.body();
                if (isBlank(body)) {
                    body = "Salesforce returned status " + response.statusCode();
                }
                writeJson(resp, response.statusCode(),
                        Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR).add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, body).build());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, "Salesforce connection test failed", e);
            ServerDiagnosticsLog.write(
                "test-salesforce-connection-servlet",
                requestId,
                "http-error",
                com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + endpoint + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE + safe(e.getMessage()),
                e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to contact Salesforce endpoint.").build());
        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Salesforce connection test failed", e);
            ServerDiagnosticsLog.write(
                    "test-salesforce-connection-servlet",
                    requestId,
                    "http-error",
                    com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL + endpoint + LS + com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE + safe(e.getMessage()),
                    e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                            .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Salesforce connection test failed.").build());
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private HttpClient getHttpClient() {
        return CLIENT;
    }

    private ServerConfig loadConfig() {
        try {
            return EncryptedDbConfigStore.load();
        } catch (SQLException e) {
            log.log(Level.SEVERE, com.sim.chatserver.util.CommonLiteralConstants.MSG_LOAD_SERVER_CONFIGURATION, e);
            throw new IllegalStateException(com.sim.chatserver.util.CommonLiteralConstants.MSG_LOAD_SERVER_CONFIGURATION, e);
        }
    }

    private String buildSalesforceDataEndpoint(String instanceUrl) {
        String normalized = instanceUrl;
        if (!normalized.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP) && !normalized.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
            normalized = com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS + normalized;
        }
        normalized = normalized.replaceAll(com.sim.chatserver.util.CommonLiteralConstants.REGEX_TRAILING_SLASHES, "");
        return normalized + "/services/data/";
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to write test-salesforce response", e);
            throw new IllegalStateException(com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE_NO_PERIOD, e);
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
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


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
