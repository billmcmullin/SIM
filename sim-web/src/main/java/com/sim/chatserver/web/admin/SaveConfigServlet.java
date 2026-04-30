package com.sim.chatserver.web.admin;

import java.io.IOException;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SaveConfigServlet", urlPatterns = {"/admin/save-config"})
public class SaveConfigServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String serverHostParam = req.getParameter("serverHost");
        String serverPortValue = req.getParameter("serverPort");
        String connectionInfoParam = req.getParameter("connectionInfo");
        String apiKeyParam = req.getParameter("apiKey");

        // Salesforce params
        String salesforceInstanceUrlParam = req.getParameter("salesforceInstanceUrl");
        String salesforceApiKeyParam = req.getParameter("salesforceApiKey");

        // Salesforce OAuth refresh params
        String salesforceLoginUrlParam = req.getParameter("salesforceLoginUrl");
        String salesforceClientIdParam = req.getParameter("salesforceClientId");
        String salesforceClientSecretParam = req.getParameter("salesforceClientSecret");
        String salesforceRefreshTokenParam = req.getParameter("salesforceRefreshToken");

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

            // Preserve workspace name
            String workspaceName = defaultString(existingConfig.getWorkspaceName());

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

            ServerConfig config = new ServerConfig(serverHost, serverPort, connectionInfo, apiKey, workspaceName);
            config.setSalesforceInstanceUrl(salesforceInstanceUrl);
            config.setSalesforceApiKey(salesforceApiKey);

            config.setSalesforceLoginUrl(salesforceLoginUrl);
            config.setSalesforceClientId(salesforceClientId);
            config.setSalesforceClientSecret(salesforceClientSecret);
            config.setSalesforceRefreshToken(salesforceRefreshToken);

            EncryptedDbConfigStore.save(config);

            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (Exception e) {
            throw new ServletException("Unable to save server configuration", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
