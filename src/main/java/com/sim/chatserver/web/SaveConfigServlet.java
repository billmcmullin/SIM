package com.sim.chatserver.web;

import java.io.IOException;

import com.sim.chatserver.config.ConfigStore;
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
        String serverHost = req.getParameter("serverHost");
        String serverPortValue = req.getParameter("serverPort");
        String connectionInfo = req.getParameter("connectionInfo");
        String apiKeyParam = req.getParameter("apiKey");

        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(serverPortValue);
        } catch (NumberFormatException ignored) {
        }

        try {
            ServerConfig existingConfig = ConfigStore.load();
            String apiKey = apiKeyParam;
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = existingConfig != null ? existingConfig.getApiKey() : "";
            }
            String workspaceName = existingConfig != null ? existingConfig.getWorkspaceName() : "";

            ServerConfig config = new ServerConfig(serverHost, serverPort, connectionInfo, apiKey, workspaceName);
            ConfigStore.save(config);

            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (Exception e) {
            throw new ServletException("Unable to save server configuration", e);
        }
    }
}
