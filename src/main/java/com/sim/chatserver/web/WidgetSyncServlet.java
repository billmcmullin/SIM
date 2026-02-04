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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetSyncServlet", urlPatterns = {"/admin/widgets/sync", "/admin/widgets/sync/timer"})
public class WidgetSyncServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetSyncServlet.class.getName());
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final long DEFAULT_INTERVAL_SECONDS = 300L;

    @Inject
    AppDataSourceHolder dsHolder;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "widget-sync-timer");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> scheduledFuture;
    private volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private volatile Timestamp lastSynced;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            loadSyncSettings();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load sync settings", e);
        }
        scheduleSyncTask();
    }

    @Override
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduler.shutdownNow();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isTimerRequest(req)) {
            handleTimerStatus(resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isTimerRequest(req)) {
            handleTimerUpdate(req, resp);
            return;
        }

        if (!authorizeAdmin(req, resp)) {
            return;
        }

        List<WidgetSyncStatus> statuses;
        try {
            statuses = runSync(req.getParameter("widgetId"));
        } catch (Exception e) {
            log.log(Level.WARNING, "Widget sync failed", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Widget sync failed: " + e.getMessage());
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

    private boolean isTimerRequest(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri != null && uri.endsWith("/timer");
    }

    private void handleTimerStatus(HttpServletResponse resp) throws IOException {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("intervalSeconds", syncIntervalSeconds)
                .add("lastSynced", lastSynced == null ? "" : lastSynced.toInstant().toString())
                .build();
        resp.setContentType("application/json");
        resp.getWriter().write(payload.toString());
    }

    private void handleTimerUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        String intervalParam = req.getParameter("intervalSeconds");
        long intervalSeconds;
        try {
            intervalSeconds = Long.parseLong(intervalParam);
            if (intervalSeconds < 30) {
                intervalSeconds = 30;
            }
        } catch (NumberFormatException e) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid interval specified.");
            return;
        }

        updateInterval(intervalSeconds);
        persistSyncSettings();

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("intervalSeconds", syncIntervalSeconds)
                .add("lastSynced", lastSynced == null ? "" : lastSynced.toInstant().toString())
                .build();
        resp.setContentType("application/json");
        resp.getWriter().write(payload.toString());
    }

    private synchronized void updateInterval(long newIntervalSeconds) {
        this.syncIntervalSeconds = newIntervalSeconds;
        scheduleSyncTask();
    }

    private synchronized void scheduleSyncTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::runScheduledSync,
                syncIntervalSeconds, syncIntervalSeconds, TimeUnit.SECONDS);
    }

    private void runScheduledSync() {
        try {
            List<WidgetSyncStatus> statuses = runSync(null);
            updateLastSynced();
            log.info("Automatic widget sync completed. Synced " + statuses.size() + " widget entries.");
        } catch (Exception e) {
            log.log(Level.WARNING, "Automatic widget sync failed", e);
        }
    }

    private List<WidgetSyncStatus> runSync(String requestedWidgetId) throws Exception {
        ServerConfig config = ConfigStore.load();
        if (config == null) {
            throw new IOException("Server configuration is missing.");
        }

        List<WidgetEntry> widgets = WidgetStore.list(null);
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
        }

        return statuses;
    }

    private void updateLastSynced() {
        lastSynced = Timestamp.from(Instant.now());
        persistSyncSettings();
    }

    private synchronized void loadSyncSettings() throws SQLException {
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            SyncSettings settings = readSyncSettings(conn);
            if (settings.intervalSeconds > 0) {
                syncIntervalSeconds = settings.intervalSeconds;
            }
            lastSynced = settings.lastSynced;
        }
    }

    private void persistSyncSettings() {
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to persist sync settings", e);
        }
    }

    private void ensureSyncSettingsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS widget_sync_settings ("
                + "id INTEGER PRIMARY KEY, "
                + "interval_seconds BIGINT NOT NULL, "
                + "last_synced TIMESTAMP)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private SyncSettings readSyncSettings(Connection conn) throws SQLException {
        String sql = "SELECT interval_seconds, last_synced FROM widget_sync_settings WHERE id = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long interval = rs.getLong(1);
                Timestamp last = rs.getTimestamp(2);
                return new SyncSettings(interval, last);
            }
        }
        upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        return new SyncSettings(syncIntervalSeconds, lastSynced);
    }

    private void upsertSyncSettings(Connection conn, long intervalSeconds, Timestamp lastSynced) throws SQLException {
        String sql = "INSERT INTO widget_sync_settings (id, interval_seconds, last_synced) "
                + "VALUES (1, ?, ?) "
                + "ON CONFLICT (id) DO UPDATE SET interval_seconds = EXCLUDED.interval_seconds, last_synced = EXCLUDED.last_synced";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, intervalSeconds);
            if (lastSynced != null) {
                ps.setTimestamp(2, lastSynced);
            } else {
                ps.setTimestamp(2, null);
            }
            ps.executeUpdate();
        }
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
                + " (db_id BIGSERIAL PRIMARY KEY, widget_chat_id TEXT UNIQUE, prompt TEXT, response_text TEXT,"
                + " created_at TIMESTAMP, session_id TEXT, username TEXT)";
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
        Set<String> existingIds = fetchExistingChatIds(conn, tableName);
        String sql = "INSERT INTO " + quoteIdentifier(tableName)
                + " (widget_chat_id, prompt, response_text, created_at, session_id, username) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int inserted = 0;
            for (JsonObject chat : chats) {
                String chatId = getString(chat, "id");
                if (chatId == null || existingIds.contains(chatId)) {
                    continue;
                }
                ps.setString(1, chatId);
                ps.setString(2, getString(chat, "prompt"));
                ps.setString(3, formatResponseText(chat));
                Timestamp ts = parseCreatedAt(chat);
                ps.setTimestamp(4, ts);
                ps.setString(5, getString(chat, "session_id"));
                ps.setString(6, getString(chat, "username"));
                ps.addBatch();
                inserted++;
            }
            if (inserted > 0) {
                ps.executeBatch();
            }
        }
    }

    private Set<String> fetchExistingChatIds(Connection conn, String tableName) throws SQLException {
        Set<String> existing = new HashSet<>();
        String sql = "SELECT widget_chat_id FROM " + quoteIdentifier(tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                existing.add(rs.getString(1));
            }
        }
        return existing;
    }

    private String formatResponseText(JsonObject chat) {
        JsonValue responseValue = chat.get("response");
        String text = extractText(responseValue);
        if (text == null) {
            JsonValue rawChatValue = chat.get("raw_chat");
            if (rawChatValue instanceof JsonObject rawChat) {
                text = extractText(rawChat.get("response"));
            }
        }
        return humanize(normalizeToJsonText(text));
    }

    private String extractText(JsonValue value) {
        if (value == null || value == JsonValue.NULL) {
            return null;
        }
        if (value instanceof JsonObject obj) {
            return getString(obj, "text");
        }
        if (value instanceof JsonString js) {
            return js.getString();
        }
        return value.toString();
    }

    private String normalizeToJsonText(String raw) {
        if (raw == null) {
            return null;
        }
        try (JsonReader reader = Json.createReader(new StringReader(raw))) {
            JsonStructure structure = reader.read();
            if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
                JsonObject obj = structure.asJsonObject();
                String text = getString(obj, "text");
                if (text != null) {
                    return text;
                }
            }
        } catch (JsonException e) {
            // raw is not valid JSON
        }
        return raw;
    }

    private String humanize(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\\n", "\n").replace("\\r", "\r").trim();
    }

    private Timestamp parseCreatedAt(JsonObject chat) {
        String created = getString(chat, "createdAt");
        if (created == null) {
            created = getString(chat, "created_at");
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
            return new URI(base.getScheme(), base.getAuthority(), apiPath, null, null);
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

    private static final class SyncSettings {

        private final long intervalSeconds;
        private final Timestamp lastSynced;

        private SyncSettings(long intervalSeconds, Timestamp lastSynced) {
            this.intervalSeconds = intervalSeconds;
            this.lastSynced = lastSynced;
        }
    }
}
