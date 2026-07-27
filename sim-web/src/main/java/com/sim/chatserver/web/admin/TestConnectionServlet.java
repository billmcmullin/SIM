package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/admin/test-connection"})
public class TestConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestConnectionServlet.class.getName());

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Pattern HOST_PATTERN = Pattern.compile("[A-Za-z0-9.-]{1,253}");
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build());
            return;
        }

        String host = firstParam(req, "serverHost");
        String port = firstParam(req, "serverPort");
        String apiKey = firstParam(req, "apiKey");

        if (host == null || host.isBlank() || port == null || port.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Host and port are required.")
                    .build());
            return;
        }

        if (apiKey == null || apiKey.isBlank()) {
            try {
                ServerConfig config = EncryptedDbConfigStore.load();
                apiKey = config != null ? config.getApiKey() : "";
            } catch (SQLException | RuntimeException e) {
                log.log(Level.WARNING, "Unable to load stored API key", e);
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Unable to load stored API key.")
                        .build());
                return;
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "API key is required.")
                    .build());
            return;
        }

        String endpoint;
        try {
            endpoint = buildEndpoint(host.trim(), port.trim());
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid host or port format.")
                    .build());
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
            } else {
                writeJson(resp, response.statusCode(), Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Connection test failed with upstream status.")
                        .build());
            }
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Connection test failed", e);
            writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Connection test failed.")
                    .build());
        }
    }

    private String buildEndpoint(String host, String port) {
        if (!PORT_PATTERN.matcher(port).matches()) {
            throw new IllegalArgumentException("Invalid port");
        }
        int portNumber = Integer.parseInt(port);
        if (portNumber < 1 || portNumber > 65535) {
            throw new IllegalArgumentException("Port out of range");
        }

        String hostPart = sanitizeHost(host);
        if (hostPart == null || !HOST_PATTERN.matcher(hostPart).matches()) {
            throw new IllegalArgumentException("Invalid host");
        }

        return "https://" + hostPart + ":" + portNumber + "/api/v1/auth";
    }

    private String sanitizeHost(String rawHost) {
        if (rawHost == null || rawHost.isBlank()) {
            return null;
        }
        String trimmed = rawHost.trim();
        if (trimmed.startsWith("http://")) {
            trimmed = trimmed.substring("http://".length());
        } else if (trimmed.startsWith("https://")) {
            trimmed = trimmed.substring("https://".length());
        }

        int slash = trimmed.indexOf('/');
        if (slash >= 0) {
            trimmed = trimmed.substring(0, slash);
        }
        int colon = trimmed.indexOf(':');
        if (colon >= 0) {
            trimmed = trimmed.substring(0, colon);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(payload == null ? Json.createObjectBuilder().build() : payload);
        }
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
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.length() > 512 ? normalized.substring(0, 512) : normalized;
    }
}
