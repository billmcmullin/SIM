package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewManualMessageServlet", urlPatterns = {"/dashboard/widgets/drilldown/review/manual-message"})
public class WidgetReviewManualMessageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewManualMessageServlet.class.getName());
    private static final String CHAT_API_PATH_TEMPLATE = "/api/v1/workspace/%s/chat";

    @Inject
    AppDataSourceHolder dsHolder;

    private transient HttpClient httpClient;

    @Override
    public void init() throws ServletException {
        super.init();
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        // Ensure we interpret incoming request body as UTF-8
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (Exception ex) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String message = payload.getString("message", "").trim();
        if (message.isEmpty()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "message is required.");
            return;
        }

        EncryptedDbConfigStore.setAppDataSourceHolder(dsHolder);
        ServerConfig config;
        try {
            config = EncryptedDbConfigStore.load();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Unable to load server configuration", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration not available.");
            return;
        }

        if (config == null) {
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration missing.");
            return;
        }

        String slug = buildSlug(config.getWorkspaceName());
        if (slug == null || slug.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace slug not configured.");
            return;
        }

        String baseUrl = buildBaseUrl(config);
        if (baseUrl == null || baseUrl.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Server connection information is incomplete.");
            return;
        }

        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "API key not configured.");
            return;
        }

        String sessionId = req.getSession().getId();
        JsonObject requestBody = Json.createObjectBuilder()
                .add("message", message)
                .add("mode", "chat")
                .add("sessionId", sessionId == null ? "" : sessionId)
                .add("reset", false)
                .build();

        String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        String targetUrl = baseUrl + String.format(CHAT_API_PATH_TEMPLATE, encodedSlug);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> remoteResponse;
        try {
            remoteResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.log(Level.SEVERE, "Manual message request interrupted", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request interrupted.");
            return;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to contact workspace endpoint", ex);
            respondWithError(resp, HttpServletResponse.SC_BAD_GATEWAY, "Unable to reach workspace API.");
            return;
        }

        // Mirror upstream status
        resp.setStatus(remoteResponse.statusCode());

        // Prefer to mirror upstream Content-Type if provided; otherwise default to application/json; charset=UTF-8
        String upstreamContentType = remoteResponse.headers().firstValue("Content-Type").orElse(null);
        if (upstreamContentType != null && !upstreamContentType.isBlank()) {
            // Ensure charset is present; if not, specify UTF-8 as default
            if (!upstreamContentType.toLowerCase().contains("charset")) {
                upstreamContentType = upstreamContentType + "; charset=UTF-8";
            }
            resp.setContentType(upstreamContentType);
        } else {
            resp.setContentType("application/json; charset=UTF-8");
        }
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Write raw upstream body bytes to response output stream to preserve characters exactly
        byte[] bytes = remoteResponse.body() == null ? new byte[0] : remoteResponse.body().getBytes(StandardCharsets.UTF_8);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().flush();
    }

    private String buildBaseUrl(ServerConfig config) {
        String connectionInfo = config.getConnectionInfo();
        if (connectionInfo != null && !connectionInfo.isBlank()) {
            return stripTrailingSlash(connectionInfo.trim());
        }
        String host = config.getServerHost();
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalized = host.trim();
        StringBuilder builder = new StringBuilder();
        if (normalized.contains("://")) {
            builder.append(normalized);
        } else {
            builder.append("https://").append(normalized);
        }
        boolean hasPort = normalized.matches(".*:\\d+$");
        if (!hasPort && config.getServerPort() > 0) {
            builder.append(':').append(config.getServerPort());
        }
        return stripTrailingSlash(builder.toString());
    }

    private String buildSlug(String workspaceName) {
        if (workspaceName == null) {
            return "";
        }
        String normalized = workspaceName.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceFirst("^-+", "");
        normalized = normalized.replaceFirst("-+$", "");
        return normalized.isBlank() ? "" : normalized;
    }

    private String stripTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private void respondWithError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", " ");
    }
}
