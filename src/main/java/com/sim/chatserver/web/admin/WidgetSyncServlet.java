package com.sim.chatserver.web.admin;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
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

    // Tuned HTTP client
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private static final long DEFAULT_INTERVAL_SECONDS = 300L;
    private static final long MIN_INTERVAL_SECONDS = 30L;

    // Persist lastSynced periodically instead of every run
    private static final int LAST_SYNC_FLUSH_EVERY_N_RUNS = 5;

    // Parallel fetch pool
    private static final int DEFAULT_SYNC_PARALLELISM = 4;

    private static final Pattern NON_ALNUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9_]");

    @Inject
    AppDataSourceHolder dsHolder;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "widget-sync-timer");
        t.setDaemon(true);
        return t;
    });

    private final ExecutorService syncPool = Executors.newFixedThreadPool(DEFAULT_SYNC_PARALLELISM, r -> {
        Thread t = new Thread(r, "widget-sync-worker");
        t.setDaemon(true);
        return t;
    });

    // cache table readiness to avoid repeated DDL/metadata checks
    private final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();

    private ScheduledFuture<?> scheduledFuture;
    private volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private volatile Timestamp lastSynced;
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);
    private final AtomicInteger runsSinceLastSyncPersist = new AtomicInteger(0);

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
        syncPool.shutdownNow();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isTimerRequest(req)) {
            handleTimerStatus(resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isTimerRequest(req)) {
            handleTimerUpdate(req, resp);
            return;
        }

        if (!authorizeAdmin(req, resp)) {
            return;
        }

        if (!syncRunning.compareAndSet(false, true)) {
            jsonError(resp, HttpServletResponse.SC_CONFLICT, "Sync already in progress.");
            return;
        }

        try {
            List<WidgetSyncStatus> statuses = runSync(req.getParameter("widgetId"));
            updateLastSyncedMaybePersist(true);

            JsonArrayBuilder arr = Json.createArrayBuilder();
            statuses.forEach(s -> arr.add(s.toJson()));

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("widgetStatus", arr)
                    .build();

            resp.setContentType("application/json");
            resp.getWriter().write(payload.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "Widget sync failed", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Widget sync failed: " + e.getMessage());
        } finally {
            syncRunning.set(false);
        }
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

        long intervalSeconds;
        try {
            intervalSeconds = Long.parseLong(req.getParameter("intervalSeconds"));
            if (intervalSeconds < MIN_INTERVAL_SECONDS) {
                intervalSeconds = MIN_INTERVAL_SECONDS;
            }
        } catch (Exception e) {
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
        scheduledFuture = scheduler.scheduleWithFixedDelay(
                this::runScheduledSync, syncIntervalSeconds, syncIntervalSeconds, TimeUnit.SECONDS);
    }

    private void runScheduledSync() {
        if (!syncRunning.compareAndSet(false, true)) {
            log.fine("Skipping scheduled sync because another sync is running.");
            return;
        }
        try {
            List<WidgetSyncStatus> statuses = runSync(null);
            updateLastSyncedMaybePersist(false);
            log.info("Automatic widget sync completed. Synced " + statuses.size() + " widget entries.");
        } catch (Exception e) {
            log.log(Level.WARNING, "Automatic widget sync failed", e);
        } finally {
            syncRunning.set(false);
        }
    }

    private List<WidgetSyncStatus> runSync(String requestedWidgetId) throws Exception {
        ServerConfig config = EncryptedDbConfigStore.load();
        if (config == null) {
            throw new IOException("Server configuration is missing.");
        }

        List<WidgetEntry> widgets = WidgetStore.list(null);
        if (requestedWidgetId != null && !requestedWidgetId.isBlank()) {
            widgets.removeIf(w -> w == null || !requestedWidgetId.equals(w.getWidgetId()));
        }

        List<WidgetEntry> valid = widgets.stream()
                .filter(w -> w != null && w.getWidgetId() != null && !w.getWidgetId().isBlank())
                .toList();

        List<Callable<WidgetSyncStatus>> tasks = new ArrayList<>();
        for (WidgetEntry widget : valid) {
            tasks.add(() -> syncSingleWidget(config, widget.getWidgetId()));
        }

        List<WidgetSyncStatus> statuses = new ArrayList<>(tasks.size());
        List<Future<WidgetSyncStatus>> futures = syncPool.invokeAll(tasks);

        for (Future<WidgetSyncStatus> f : futures) {
            try {
                statuses.add(f.get());
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                statuses.add(new WidgetSyncStatus("unknown", "unknown", false, false,
                        "Sync failed: " + (cause == null ? "unknown error" : cause.getMessage())));
            }
        }
        return statuses;
    }

    private WidgetSyncStatus syncSingleWidget(ServerConfig config, String widgetId) {
        String tableName = sanitizeWidgetTableName(widgetId);

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            ensureTable(conn, tableName); // create-if-not-exists + ensure unique index + cache

            List<JsonObject> chats = fetchWidgetChats(config, widgetId);
            int inserted = insertWidgetChats(conn, tableName, chats);

            String message = chats.isEmpty()
                    ? "No chat rows returned from server."
                    : String.format("Fetched %d chat(s), inserted %d new chat(s).", chats.size(), inserted);

            return new WidgetSyncStatus(widgetId, tableName, true, true, message);
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to sync widget " + widgetId, e);
            return new WidgetSyncStatus(widgetId, tableName, false, false, "Sync failed: " + e.getMessage());
        }
    }

    private void updateLastSyncedMaybePersist(boolean forcePersist) {
        lastSynced = Timestamp.from(Instant.now());
        int n = runsSinceLastSyncPersist.incrementAndGet();
        if (forcePersist || n >= LAST_SYNC_FLUSH_EVERY_N_RUNS) {
            runsSinceLastSyncPersist.set(0);
            persistSyncSettings();
        }
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
                return new SyncSettings(rs.getLong(1), rs.getTimestamp(2));
            }
        }
        upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        return new SyncSettings(syncIntervalSeconds, lastSynced);
    }

    private void upsertSyncSettings(Connection conn, long intervalSeconds, Timestamp lastSynced) throws SQLException {
        String sql = "INSERT INTO widget_sync_settings (id, interval_seconds, last_synced) VALUES (1, ?, ?) "
                + "ON CONFLICT (id) DO UPDATE SET interval_seconds = EXCLUDED.interval_seconds, last_synced = EXCLUDED.last_synced";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, intervalSeconds);
            ps.setTimestamp(2, lastSynced);
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

    // Enhancement:
    // - Ensure table exists
    // - Ensure unique index exists for ON CONFLICT(widget_chat_id)
    // - Best-effort dedupe when legacy/imported tables contain duplicate widget_chat_id
    // - Cache readiness to avoid repeated DDL checks
    private void ensureTable(Connection conn, String tableName) throws SQLException {
        if (ensuredTables.contains(tableName)) {
            return;
        }

        String quotedTable = quoteIdentifier(tableName);

        // Ensure base table exists (allow imported legacy schema to remain)
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + quotedTable
                + " (db_id BIGSERIAL PRIMARY KEY, widget_chat_id TEXT, prompt TEXT, response_text TEXT, "
                + "created_at TIMESTAMP, session_id TEXT, username TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        }

        // Ensure unique index for ON CONFLICT(widget_chat_id)
        String idxName = (tableName + "_widget_chat_id_uidx");
        if (idxName.length() > 63) {
            idxName = idxName.substring(0, 63);
        }
        String quotedIdx = quoteIdentifier(idxName);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                    + " ON " + quotedTable + " (widget_chat_id)");
        } catch (SQLException uniqueErr) {
            // Likely duplicates already present; dedupe then retry index creation once
            dedupeByWidgetChatId(conn, tableName);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                        + " ON " + quotedTable + " (widget_chat_id)");
            }
        }

        ensuredTables.add(tableName);
    }

    private void dedupeByWidgetChatId(Connection conn, String tableName) throws SQLException {
        String quoted = quoteIdentifier(tableName);

        // Keep newest row by created_at/db_id per widget_chat_id, delete older duplicates.
        String sql = "DELETE FROM " + quoted + " a "
                + "USING " + quoted + " b "
                + "WHERE a.widget_chat_id = b.widget_chat_id "
                + "AND a.widget_chat_id IS NOT NULL "
                + "AND (a.created_at < b.created_at "
                + "     OR (a.created_at = b.created_at AND a.db_id < b.db_id) "
                + "     OR (a.created_at IS NULL AND b.created_at IS NOT NULL) "
                + "     OR (a.created_at IS NULL AND b.created_at IS NULL AND a.db_id < b.db_id))";

        try (Statement stmt = conn.createStatement()) {
            int removed = stmt.executeUpdate(sql);
            if (removed > 0) {
                log.info("Removed " + removed + " duplicate rows from " + tableName + " before creating unique index.");
            }
        }
    }

    // DB-level dedupe + transaction/batch
    private int insertWidgetChats(Connection conn, String tableName, List<JsonObject> chats) throws SQLException {
        if (chats == null || chats.isEmpty()) {
            return 0;
        }

        String sql = "INSERT INTO " + quoteIdentifier(tableName)
                + " (widget_chat_id, prompt, response_text, created_at, session_id, username) "
                + "VALUES (?,?,?,?,?,?) ON CONFLICT (widget_chat_id) DO NOTHING";

        boolean originalAutoCommit = conn.getAutoCommit();
        int attempted = 0;

        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (JsonObject chat : chats) {
                String chatId = getString(chat, "id");
                if (chatId == null || chatId.isBlank()) {
                    continue;
                }

                ps.setString(1, chatId);
                ps.setString(2, getString(chat, "prompt"));
                ps.setString(3, formatResponseText(chat));
                ps.setTimestamp(4, parseCreatedAt(chat));
                ps.setString(5, getString(chat, "session_id"));
                ps.setString(6, getString(chat, "username"));
                ps.addBatch();
                attempted++;
            }
            if (attempted > 0) {
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
        return attempted;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = NON_ALNUM_UNDERSCORE.matcher(widgetId.trim()).replaceAll("_");
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

    private String formatResponseText(JsonObject chat) {
        JsonValue responseValue = chat.get("response");
        String text = extractText(responseValue);
        if (text == null) {
            JsonValue raw = chat.get("raw_chat");
            if (raw instanceof JsonObject rawObj) {
                text = extractText(rawObj.get("response"));
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
        String t = raw.trim();
        if (!(t.startsWith("{") || t.startsWith("["))) {
            return raw; // fast path
        }

        try (JsonReader reader = Json.createReader(new StringReader(raw))) {
            JsonStructure structure = reader.read();
            if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
                String text = getString(structure.asJsonObject(), "text");
                if (text != null) {
                    return text;
                }
            }
        } catch (JsonException ignored) {
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
            return Timestamp.from(OffsetDateTime.parse(created).toInstant());
        } catch (DateTimeParseException e) {
            log.fine("Unable to parse timestamp: " + created);
            return null;
        }
    }

    private List<JsonObject> fetchWidgetChats(ServerConfig config, String widgetId) throws IOException, InterruptedException {
        URI uri = buildSyncUri(config, widgetId);

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
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
            throw new IOException(message);
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!contentType.toLowerCase().contains("application/json")) {
            throw new IOException("Unexpected content type '" + contentType + "'");
        }

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return normalizeResponse(reader.read());
        } catch (JsonException je) {
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
            String path = base.getPath() == null ? "" : base.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
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
            root.asJsonArray().forEach(v -> {
                if (v instanceof JsonObject o) {
                    normalized.add(o);
                }
            });
            return normalized;
        }

        if (root.getValueType() == JsonValue.ValueType.OBJECT) {
            JsonObject obj = root.asJsonObject();
            for (String key : List.of("items", "data", "results", "chats", "entries")) {
                JsonValue v = obj.get(key);
                if (v != null && v.getValueType() == JsonValue.ValueType.ARRAY) {
                    v.asJsonArray().forEach(e -> {
                        if (e instanceof JsonObject o) {
                            normalized.add(o);
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
                    .add("widgetId", widgetId == null ? "" : widgetId)
                    .add("tableName", tableName == null ? "" : tableName)
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
