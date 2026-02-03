package com.sim.chatserver.web;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.ConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetSyncServlet", urlPatterns = {"/admin/widgets/sync"})
public class WidgetSyncServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetSyncServlet.class.getName());
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }

        ServerConfig config;
        try {
            config = ConfigStore.load();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to load server config", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load server configuration.");
            return;
        }
        if (config == null) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Server configuration is missing.");
            return;
        }

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to load widget registry", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load widget registry.");
            return;
        }

        String requestedWidgetId = req.getParameter("widgetId");
        if (requestedWidgetId != null && !requestedWidgetId.isBlank()) {
            widgets.removeIf(entry -> entry == null || !requestedWidgetId.equals(entry.getWidgetId()));
        }

        List<WidgetSyncStatus> statuses = new ArrayList<>();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                boolean tableExists = ensureTable(conn, tableName);
                boolean synced = false;
                String message = tableExists ? "Table ready" : "Table missing and could not be created.";
                if (tableExists) {
                    try {
                        List<JsonObject> chats = fetchWidgetChats(config, widgetId);
                        insertWidgetChats(conn, tableName, chats);
                        synced = true;
                        message = chats.isEmpty() ? "No chat rows returned from server."
                                : String.format("Synced %d chat(s).", chats.size());
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failed to sync widget " + widgetId, e);
                        message = "Sync failed: " + e.getMessage();
                    }
                }
                statuses.add(new WidgetSyncStatus(widgetId, tableName, tableExists, synced, message));
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error while syncing widgets", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to access the database.");
            return;
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        statuses.forEach(status -> arrayBuilder.add(status.toJson()));
        JsonObject responsePayload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("widgetStatus", arrayBuilder)
                .build();

        resp.setContentType("application/json");
        resp.getWriter().write(responsePayload.toString());
    }

    private boolean authorizeAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            jsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private boolean ensureTable(Connection conn, String tableName) throws SQLException {
        if (tableExists(conn, tableName)) {
            return true;
        }
        createWidgetDataTable(conn, tableName);
        return tableExists(conn, tableName);
    }

    private void createWidgetDataTable(Connection conn, String tableName) throws SQLException {
        String sql = "CREATE TABLE " + quoteIdentifier(tableName)
                + " (id BIGSERIAL PRIMARY KEY, chat_id TEXT, prompt TEXT, text TEXT, translated_text TEXT,"
                + " language TEXT, created_at TIMESTAMP, session_id TEXT, payload TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private void insertWidgetChats(Connection conn, String tableName, List<JsonObject> chats) throws SQLException {
        if (chats == null || chats.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO " + quoteIdentifier(tableName)
                + " (chat_id, prompt, text, translated_text, language, created_at, session_id, payload) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (JsonObject chat : chats) {
                ps.setString(1, getString(chat, "chat_id"));
                ps.setString(2, getString(chat, "prompt"));
                ps.setString(3, getString(chat, "text"));
                ps.setString(4, getString(chat, "translated_text"));
                ps.setString(5, getString(chat, "language"));
                Timestamp ts = parseTimestamp(chat);
                if (ts != null) {
                    ps.setTimestamp(6, ts);
                } else {
                    ps.setTimestamp(6, null);
                }
                ps.setString(7, getString(chat, "session_id"));
                ps.setString(8, chat.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Timestamp parseTimestamp(JsonObject chat) {
        String created = getString(chat, "created_at");
        if (created == null) {
            created = getString(chat, "prompt_date");
        }
        if (created == null) {
            return null;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(created);
            return Timestamp.from(odt.toInstant());
        } catch (DateTimeParseException e) {
            log.fine("Unable to parse timestamp: " + created);
            return null;
        }
    }

    private List<JsonObject> fetchWidgetChats(ServerConfig config, String widgetId) throws IOException, InterruptedException {
        URI uri = buildSyncUri(config, widgetId);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .GET();

        String apiKey = config.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
            builder.header("X-API-Key", apiKey);
        }

        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body();
        if (response.statusCode() >= 300) {
            String message = String.format("Sync API returned %d: %s", response.statusCode(), truncateBody(body));
            log.warning(() -> "Widget sync failed for " + widgetId + ": " + message);
            throw new IOException(message);
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!contentType.toLowerCase().contains("application/json")) {
            String message = String.format("Unexpected content type '%s' when syncing %s", contentType, widgetId);
            log.warning(() -> message + " payload: " + truncateBody(body));
            throw new IOException(message);
        }

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonStructure root = reader.read();
            return normalizeResponse(root);
        } catch (JsonException je) {
            log.log(Level.WARNING, "Invalid JSON payload for widget " + widgetId + ": " + truncateBody(body), je);
            throw new IOException("Invalid JSON received from sync API", je);
        }
    }

    private URI buildSyncUri(ServerConfig config, String widgetId) {
        String host = config.getServerHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Server host configuration is missing");
        }

        String normalizedHost = host.trim();
        if (!normalizedHost.startsWith("http://") && !normalizedHost.startsWith("https://")) {
            normalizedHost = "https://" + normalizedHost;
        }

        try {
            URI base = new URI(normalizedHost);
            String path = base.getPath() != null && base.getPath().endsWith("/") ? base.getPath().substring(0, base.getPath().length() - 1) : base.getPath();
            if (path == null) {
                path = "";
            }
            if (!path.contains("/api")) {
                path = path + "/api";
            }
            String apiPath = path + "/v1/embed/" + URLEncoder.encode(widgetId, StandardCharsets.UTF_8) + "/chats";
            URI resolved = new URI(base.getScheme(), base.getAuthority(), apiPath, null, null);
            return resolved;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid base URL for sync endpoint", e);
        }
    }

    private List<JsonObject> normalizeResponse(JsonStructure root) {
        List<JsonObject> normalized = new ArrayList<>();
        if (root == null) {
            return normalized;
        }
        if (root.getValueType() == JsonValue.ValueType.ARRAY) {
            var array = root.asJsonArray();
            array.forEach(value -> {
                if (value instanceof JsonObject obj) {
                    normalized.add(obj);
                }
            });
            return normalized;
        }

        if (root.getValueType() == JsonValue.ValueType.OBJECT) {
            JsonObject obj = root.asJsonObject();
            for (String candidate : List.of("items", "data", "results", "chats", "entries")) {
                JsonValue candidateValue = obj.get(candidate);
                if (candidateValue != null && candidateValue.getValueType() == JsonValue.ValueType.ARRAY) {
                    var array = candidateValue.asJsonArray();
                    array.forEach(value -> {
                        if (value instanceof JsonObject entry) {
                            normalized.add(entry);
                        }
                    });
                    return normalized;
                }
            }
            normalized.add(obj);
        }
        return normalized;
    }

    private String truncateBody(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 512 ? body.substring(0, 512) + "…" : body;
    }

    private String getString(JsonObject source, String key) {
        if (source == null || key == null || !source.containsKey(key)) {
            return null;
        }
        JsonValue value = source.get(key);
        if (value == null || value == JsonValue.NULL) {
            return null;
        }
        if (value instanceof JsonString js) {
            return js.getString();
        }
        return value.toString();
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private void jsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
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

    private static final class WidgetSyncStatus {

        private final String widgetId;
        private final String tableName;
        private final boolean tableExists;
        private final boolean synced;
        private final String message;

        private WidgetSyncStatus(String widgetId, String tableName, boolean tableExists, boolean synced, String message) {
            this.widgetId = widgetId;
            this.tableName = tableName;
            this.tableExists = tableExists;
            this.synced = synced;
            this.message = message;
        }

        private JsonObject toJson() {
            return Json.createObjectBuilder()
                    .add("widgetId", widgetId)
                    .add("tableName", tableName)
                    .add("tableExists", tableExists)
                    .add("synced", synced)
                    .add("message", message == null ? "" : message)
                    .build();
        }
    }
}
