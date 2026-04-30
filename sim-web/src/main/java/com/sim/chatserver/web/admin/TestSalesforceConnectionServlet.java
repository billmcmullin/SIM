package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TestSalesforceConnectionServlet", urlPatterns = {"/admin/test-salesforce-connection"})
public class TestSalesforceConnectionServlet extends HttpServlet {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }

        String instanceUrl = req.getParameter("salesforceInstanceUrl");
        String apiKey = req.getParameter("salesforceApiKey");

        // fallback to stored values
        if (isBlank(instanceUrl) || isBlank(apiKey)) {
            try {
                ServerConfig config = loadConfig();
                if (isBlank(instanceUrl) && config != null) {
                    instanceUrl = config.getSalesforceInstanceUrl();
                }
                if (isBlank(apiKey) && config != null) {
                    apiKey = config.getSalesforceApiKey();
                }
            } catch (Exception e) {
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "{\"status\":\"error\",\"message\":\"Unable to load stored Salesforce configuration.\"}");
                return;
            }
        }

        if (isBlank(instanceUrl)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"error\",\"message\":\"Salesforce instance URL is required.\"}");
            return;
        }

        if (isBlank(apiKey)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"error\",\"message\":\"Salesforce API key is required.\"}");
            return;
        }

        String endpoint = buildSalesforceDataEndpoint(instanceUrl.trim());

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                writeJson(resp, HttpServletResponse.SC_OK,
                        "{\"status\":\"ok\",\"message\":\"Salesforce connection successful.\"}");
            } else {
                String body = response.body();
                if (isBlank(body)) {
                    body = "Salesforce returned status " + response.statusCode();
                }
                writeJson(resp, response.statusCode(),
                        "{\"status\":\"error\",\"message\":\"" + escapeJson(body) + "\"}");
            }
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    // ---- test seams (package-private) ----
    HttpClient getHttpClient() {
        return CLIENT;
    }

    ServerConfig loadConfig() throws Exception {
        return EncryptedDbConfigStore.load();
    }

    private String buildSalesforceDataEndpoint(String instanceUrl) {
        String normalized = instanceUrl;
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        normalized = normalized.replaceAll("/+$", "");
        return normalized + "/services/data/";
    }

    private void writeJson(HttpServletResponse resp, int status, String payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(payload);
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
