package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SaveConfigServlet", urlPatterns = {"/admin/save-config"})
public class SaveConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(SaveConfigServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        String serverHostParam = ServletRequestParamUtil.firstParam(req, "serverHost", 1024, true, true);
        String serverPortValue = ServletRequestParamUtil.firstParam(req, "serverPort", 1024, true, true);
        String connectionInfoParam = ServletRequestParamUtil.firstParam(req, "connectionInfo", 1024, true, true);
        String apiKeyParam = ServletRequestParamUtil.firstParam(req, "apiKey", 1024, true, true);
        String workspaceNameParam = ServletRequestParamUtil.firstParam(req, "workspaceName", 1024, true, true);

        // Salesforce params
        String salesforceInstanceUrlParam = ServletRequestParamUtil.firstParam(req, "salesforceInstanceUrl", 1024, true, true);
        String salesforceApiKeyParam = ServletRequestParamUtil.firstParam(req, "salesforceApiKey", 1024, true, true);

        // Salesforce OAuth refresh params
        String salesforceLoginUrlParam = ServletRequestParamUtil.firstParam(req, "salesforceLoginUrl", 1024, true, true);
        String salesforceClientIdParam = ServletRequestParamUtil.firstParam(req, "salesforceClientId", 1024, true, true);
        String salesforceClientSecretParam = ServletRequestParamUtil.firstParam(req, "salesforceClientSecret", 1024, true, true);
        String salesforceRefreshTokenParam = ServletRequestParamUtil.firstParam(req, "salesforceRefreshToken", 1024, true, true);

        // Salesforce username + password + API token params
        String salesforceUsernameParam = ServletRequestParamUtil.firstParam(req, "salesforceUsername", 1024, true, true);
        String salesforcePasswordParam = ServletRequestParamUtil.firstParam(req, "salesforcePassword", 1024, true, true);
        String salesforceApiTokenParam = ServletRequestParamUtil.firstParam(req, "salesforceApiToken", 1024, true, true);

        // AWS params
        String awsRegionParam = ServletRequestParamUtil.firstParam(req, "awsRegion", 128, true, true);
        String awsInstanceIdParam = ServletRequestParamUtil.firstParam(req, "awsInstanceId", 128, true, true);
        String awsAccessKeyIdParam = ServletRequestParamUtil.firstParam(req, "awsAccessKeyId", 1024, true, true);
        String awsSecretAccessKeyParam = ServletRequestParamUtil.firstParam(req, "awsSecretAccessKey", 4096, true, true);

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

            // Preserve existing AWS region when blank
            String awsRegion = awsRegionParam;
            if (isBlank(awsRegion)) {
                awsRegion = defaultString(existingConfig.getAwsRegion());
            }

            // Preserve existing AWS instance ID when blank
            String awsInstanceId = awsInstanceIdParam;
            if (isBlank(awsInstanceId)) {
                awsInstanceId = defaultString(existingConfig.getAwsInstanceId());
            }

            // Preserve existing AWS access key ID when blank
            String awsAccessKeyId = awsAccessKeyIdParam;
            if (isBlank(awsAccessKeyId)) {
                awsAccessKeyId = defaultString(existingConfig.getAwsAccessKeyId());
            }

            // Preserve existing AWS secret access key when blank
            String awsSecretAccessKey = awsSecretAccessKeyParam;
            if (isBlank(awsSecretAccessKey)) {
                awsSecretAccessKey = defaultString(existingConfig.getAwsSecretAccessKey());
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
            config.setAwsRegion(awsRegion);
            config.setAwsInstanceId(awsInstanceId);
            config.setAwsAccessKeyId(awsAccessKeyId);
            config.setAwsSecretAccessKey(awsSecretAccessKey);

            EncryptedDbConfigStore.save(config);
            writeJson(resp, HttpServletResponse.SC_OK,
                    Json.createObjectBuilder().add("status", "ok").build());
        } catch (Exception e) {
            throw new ServletException("Unable to save server configuration", e);
        }
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to write save config response", e);
            throw new IllegalStateException("Unable to write response", e);
        }
    }
}
