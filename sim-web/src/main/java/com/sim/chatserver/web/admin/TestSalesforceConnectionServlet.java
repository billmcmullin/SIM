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
import com.sim.chatserver.security.SalesforceAuthClient;
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

@WebServlet(name = "TestSalesforceConnectionServlet", urlPatterns = {"/admin/test-salesforce-connection"})
public class TestSalesforceConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestSalesforceConnectionServlet.class.getName());

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "error").add("message", "Authentication required.").build());
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
                        Json.createObjectBuilder().add("status", "error")
                                .add("message", "Unable to load stored Salesforce configuration.").build());
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
                SalesforceAuthClient.AuthResult authResult = new SalesforceAuthClient(getHttpClient())
                        .refreshAccessToken(authConfig);
                apiKey = authResult == null ? null : authResult.accessToken;
                if (isBlank(instanceUrl)) {
                    instanceUrl = authResult == null ? null : authResult.instanceUrl;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.log(Level.WARNING, "Salesforce token acquisition failed during connection test", e);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder().add("status", "error")
                                .add("message", "Unable to acquire Salesforce access token from configured credentials.").build());
                return;
            } catch (IOException | SQLException | RuntimeException e) {
                log.log(Level.WARNING, "Salesforce token acquisition failed during connection test", e);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Json.createObjectBuilder().add("status", "error")
                                .add("message", "Unable to acquire Salesforce access token from configured credentials.").build());
                return;
            }
        }

        if (isBlank(instanceUrl)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder().add("status", "error")
                            .add("message", "Salesforce instance URL is required.").build());
            return;
        }

        if (isBlank(apiKey)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Json.createObjectBuilder().add("status", "error")
                            .add("message", "Salesforce API key is required.").build());
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
                "method=GET\nurl=" + endpoint
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + normalizedApiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            ServerDiagnosticsLog.write(
                "test-salesforce-connection-servlet",
                requestId,
                "http-response",
                "status=" + response.statusCode() + "\nbody=" + truncate(response.body())
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                writeJson(resp, HttpServletResponse.SC_OK,
                        Json.createObjectBuilder().add("status", "ok")
                                .add("message", "Salesforce connection successful.").build());
            } else {
                String body = response.body();
                if (isBlank(body)) {
                    body = "Salesforce returned status " + response.statusCode();
                }
                writeJson(resp, response.statusCode(),
                        Json.createObjectBuilder().add("status", "error").add("message", body).build());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, "Salesforce connection test failed", e);
            ServerDiagnosticsLog.write(
                "test-salesforce-connection-servlet",
                requestId,
                "http-error",
                "url=" + endpoint + "\nmessage=" + safe(e.getMessage()),
                e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                Json.createObjectBuilder().add("status", "error")
                    .add("message", "Unable to contact Salesforce endpoint.").build());
        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Salesforce connection test failed", e);
            ServerDiagnosticsLog.write(
                    "test-salesforce-connection-servlet",
                    requestId,
                    "http-error",
                    "url=" + endpoint + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    Json.createObjectBuilder().add("status", "error")
                            .add("message", "Salesforce connection test failed.").build());
        }
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
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
            log.log(Level.SEVERE, "Unable to load server configuration", e);
            throw new IllegalStateException("Unable to load server configuration", e);
        }
    }

    private String buildSalesforceDataEndpoint(String instanceUrl) {
        String normalized = instanceUrl;
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        normalized = normalized.replaceAll("/+$", "");
        return normalized + "/services/data/";
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to write test-salesforce response", e);
            throw new IllegalStateException("Unable to write response", e);
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
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
