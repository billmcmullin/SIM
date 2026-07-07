package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/admin/test-connection"})
public class TestConnectionServlet extends HttpServlet {

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
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }

        String host = req.getParameter("serverHost");
        String port = req.getParameter("serverPort");
        String apiKey = req.getParameter("apiKey");

        resp.setContentType("application/json");

        if (host == null || host.isBlank() || port == null || port.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Host and port are required.\"}");
            return;
        }

        if (apiKey == null || apiKey.isBlank()) {
            try {
                ServerConfig config = EncryptedDbConfigStore.load();
                apiKey = config != null ? config.getApiKey() : "";
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load stored API key.\"}");
                return;
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"API key is required.\"}");
            return;
        }

        String endpoint;
        try {
            endpoint = buildEndpoint(host.trim(), port.trim());
        } catch (IllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid host or port format.\"}");
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
                resp.getWriter().write("{\"status\":\"ok\"}");
            } else {
                resp.setStatus(response.statusCode());
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Connection test failed with upstream status.\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Connection test failed.\"}");
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

        String normalizedHost = host;
        if (!normalizedHost.startsWith("http://") && !normalizedHost.startsWith("https://")) {
            normalizedHost = "https://" + normalizedHost;
        }

        URI base = URI.create(normalizedHost);
        String scheme = base.getScheme();
        String hostPart = base.getHost();
        if (scheme == null || hostPart == null || !HOST_PATTERN.matcher(hostPart).matches()) {
            throw new IllegalArgumentException("Invalid host");
        }

        return scheme + "://" + hostPart + ":" + portNumber + "/api/v1/auth";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
