package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
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
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TestSalesforceConnectionServlet", urlPatterns = {"/admin/test-salesforce-connection"})
public class TestSalesforceConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestSalesforceConnectionServlet.class.getName());

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "error").add("message", "Authentication required.").build());
            return;
        }

        String instanceUrl = firstParam(req, "salesforceInstanceUrl");
        String apiKey = firstParam(req, "salesforceApiKey");

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
            } catch (SQLException | RuntimeException e) {
                log.log(Level.WARNING, "Unable to load stored Salesforce configuration", e);
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Json.createObjectBuilder().add("status", "error")
                                .add("message", "Unable to load stored Salesforce configuration.").build());
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
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Salesforce connection test failed", e);
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    Json.createObjectBuilder().add("status", "error")
                            .add("message", "Salesforce connection test failed.").build());
        }
    }

    HttpClient getHttpClient() {
        return CLIENT;
    }

    private ServerConfig loadConfig() throws SQLException {
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

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private String firstParam(HttpServletRequest req, String name) {
        Map<String, String[]> params = req.getParameterMap();
        if (params == null) {
            return null;
        }
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }
}
