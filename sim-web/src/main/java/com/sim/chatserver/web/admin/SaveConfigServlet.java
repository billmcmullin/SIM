package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SaveConfigServlet", urlPatterns = {"/admin/save-config"})
public class SaveConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(SaveConfigServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String serverHostParam = firstParam(req, "serverHost");
        String serverPortValue = firstParam(req, "serverPort");
        String connectionInfoParam = firstParam(req, "connectionInfo");
        String apiKeyParam = firstParam(req, "apiKey");
        String workspaceNameParam = firstParam(req, "workspaceName");

        // Salesforce params
        String salesforceInstanceUrlParam = firstParam(req, "salesforceInstanceUrl");
        String salesforceApiKeyParam = firstParam(req, "salesforceApiKey");

        // Salesforce OAuth refresh params
        String salesforceLoginUrlParam = firstParam(req, "salesforceLoginUrl");
        String salesforceClientIdParam = firstParam(req, "salesforceClientId");
        String salesforceClientSecretParam = firstParam(req, "salesforceClientSecret");
        String salesforceRefreshTokenParam = firstParam(req, "salesforceRefreshToken");

        // Salesforce username + password + API token params
        String salesforceUsernameParam = firstParam(req, "salesforceUsername");
        String salesforcePasswordParam = firstParam(req, "salesforcePassword");
        String salesforceApiTokenParam = firstParam(req, "salesforceApiToken");

        try {
            ServerConfig existingConfig = EncryptedDbConfigStore.load();
            if (existingConfig == null) {
                existingConfig = new ServerConfig();
            }

            // Preserve existing host when omitted
            String serverHost = serverHostParam;
            if (isBlank(serverHost)) {
                serverHost = defaultString(existingConfig.getServerHost());
            }

            // Preserve existing port when omitted/invalid
            int serverPort = existingConfig.getServerPort();
            if (!isBlank(serverPortValue)) {
                try {
                    serverPort = Integer.parseInt(serverPortValue);
                } catch (NumberFormatException ignored) {
                    log.log(Level.FINE, "Invalid serverPort value, preserving existing port: {0}", serverPortValue);
                    // keep existing port
                }
            }

            // Preserve existing connectionInfo when omitted
            String connectionInfo = connectionInfoParam;
            if (isBlank(connectionInfo)) {
                connectionInfo = defaultString(existingConfig.getConnectionInfo());
            }

            // Preserve existing AnythingLLM API key when blank
            String apiKey = apiKeyParam;
            if (isBlank(apiKey)) {
                apiKey = defaultString(existingConfig.getApiKey());
            }

            // Preserve workspace name when omitted.
            String workspaceName = workspaceNameParam;
            if (isBlank(workspaceName)) {
                workspaceName = defaultString(existingConfig.getWorkspaceName());
            }

            // Preserve existing Salesforce instance URL when blank
            String salesforceInstanceUrl = salesforceInstanceUrlParam;
            if (isBlank(salesforceInstanceUrl)) {
                salesforceInstanceUrl = defaultString(existingConfig.getSalesforceInstanceUrl());
            }

            // Preserve existing Salesforce API key when blank
            String salesforceApiKey = salesforceApiKeyParam;
            if (isBlank(salesforceApiKey)) {
                salesforceApiKey = defaultString(existingConfig.getSalesforceApiKey());
            }

            // Preserve existing Salesforce login URL when blank
            String salesforceLoginUrl = salesforceLoginUrlParam;
            if (isBlank(salesforceLoginUrl)) {
                salesforceLoginUrl = defaultString(existingConfig.getSalesforceLoginUrl());
            }

            // Preserve existing Salesforce client ID when blank
            String salesforceClientId = salesforceClientIdParam;
            if (isBlank(salesforceClientId)) {
                salesforceClientId = defaultString(existingConfig.getSalesforceClientId());
            }

            // Preserve existing Salesforce client secret when blank
            String salesforceClientSecret = salesforceClientSecretParam;
            if (isBlank(salesforceClientSecret)) {
                salesforceClientSecret = defaultString(existingConfig.getSalesforceClientSecret());
            }

            // Preserve existing Salesforce refresh token when blank
            String salesforceRefreshToken = salesforceRefreshTokenParam;
            if (isBlank(salesforceRefreshToken)) {
                salesforceRefreshToken = defaultString(existingConfig.getSalesforceRefreshToken());
            }

            // Preserve existing Salesforce username when blank
            String salesforceUsername = salesforceUsernameParam;
            if (isBlank(salesforceUsername)) {
                salesforceUsername = defaultString(existingConfig.getSalesforceUsername());
            }

            // Preserve existing Salesforce password when blank
            String salesforcePassword = salesforcePasswordParam;
            if (isBlank(salesforcePassword)) {
                salesforcePassword = defaultString(existingConfig.getSalesforcePassword());
            }

            // Preserve existing Salesforce API token when blank
            String salesforceApiToken = salesforceApiTokenParam;
            if (isBlank(salesforceApiToken)) {
                salesforceApiToken = defaultString(existingConfig.getSalesforceApiToken());
            }

            ServerConfig config = new ServerConfig(serverHost, serverPort, connectionInfo, apiKey, workspaceName);
            config.setSalesforceInstanceUrl(salesforceInstanceUrl);
            config.setSalesforceApiKey(salesforceApiKey);

            config.setSalesforceLoginUrl(salesforceLoginUrl);
            config.setSalesforceClientId(salesforceClientId);
            config.setSalesforceClientSecret(salesforceClientSecret);
            config.setSalesforceRefreshToken(salesforceRefreshToken);
            config.setSalesforceUsername(salesforceUsername);
            config.setSalesforcePassword(salesforcePassword);
            config.setSalesforceApiToken(salesforceApiToken);

            EncryptedDbConfigStore.save(config);
            writeJson(resp, HttpServletResponse.SC_OK,
                    Json.createObjectBuilder().add("status", "ok").build());
        } catch (SQLException e) {
            throw new ServletException("Unable to save server configuration", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
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
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 1024 ? normalized.substring(0, 1024) : normalized;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
    }
}
