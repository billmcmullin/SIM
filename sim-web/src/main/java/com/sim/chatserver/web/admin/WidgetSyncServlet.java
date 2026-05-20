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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.MapReduceConfig;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.security.review.ReviewOutputValidator;
import com.sim.chatserver.security.review.TrustedUrlValidator;
import com.sim.chatserver.service.PromptTemplateService;
import com.sim.chatserver.service.ReviewContextBuilderService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.dashboard.summary.DashboardDailySummaryStore;
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

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private static final long DEFAULT_INTERVAL_SECONDS = 300L;
    private static final long MIN_INTERVAL_SECONDS = 30L;
    private static final int LAST_SYNC_FLUSH_EVERY_N_RUNS = 5;
    private static final int DEFAULT_SYNC_PARALLELISM = 4;
    private static final Pattern NON_ALNUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9_]");

    private static final int HTTP_MAX_ATTEMPTS = 3;
    private static final long HTTP_RETRY_BASE_MS = 500L;
    private static final long HTTP_RETRY_MAX_MS = 5000L;
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(90);

    @Inject
    AppDataSourceHolder dsHolder;

    private transient DashboardDailySummaryStore summaryStore;

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

    private final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();

    private ScheduledFuture<?> scheduledFuture;
    private volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private volatile Timestamp lastSynced;
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);
    private final AtomicInteger runsSinceLastSyncPersist = new AtomicInteger(0);

    private transient MapReduceConfig mrConfig;
    private transient WorkspaceClient workspaceClient;
    private transient WidgetReviewMapReduceOrchestrator orchestrator;
    private transient TrustedUrlValidator trustedUrlValidator;
    private transient ReviewOutputValidator reviewOutputValidator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            this.mrConfig = MapReduceConfig.load();
            this.workspaceClient = new WorkspaceClient(
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
                    mrConfig.getWorkspaceMaxRetries(),
                    mrConfig.getWorkspaceTimeout()
            );
            this.reviewOutputValidator = new ReviewOutputValidator();

            Set<String> allowedHosts = parseCsvToSet(System.getenv("REVIEW_TRUSTED_HOSTS"));
            Set<String> allowedSuffixes = parseCsvToSet(System.getenv("REVIEW_TRUSTED_HOST_SUFFIXES"));
            boolean allowPrivate = Boolean.parseBoolean(defaultIfBlank(System.getenv("REVIEW_ALLOW_PRIVATE_NETWORKS"), "false"));
            this.trustedUrlValidator = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivate);

            this.orchestrator = new WidgetReviewMapReduceOrchestrator(
                    workspaceClient,
                    new ReviewContextBuilderService(),
                    new PromptTemplateService(),
                    reviewOutputValidator,
                    mrConfig.getBatchSize(),
                    mrConfig.getMaxParallel(),
                    mrConfig.getMapMessageMaxChars(),
                    mrConfig.getMapContextMaxChars(),
                    mrConfig.getReduceMessageMaxChars(),
                    mrConfig.getReduceContextMaxChars(),
                    mrConfig.getRetryContextChars(),
                    mrConfig.getRetryMessageMaxChars(),
                    mrConfig.getMaxCoveragePasses(),
                    mrConfig.getMinBatchSize(),
                    mrConfig.getSegmentPromptChars(),
                    mrConfig.getSegmentResponseChars(),
                    mrConfig.getReduceInitialChunkSize(),
                    mrConfig.getReduceMinChunkSize(),
                    mrConfig.getReduceMaxLevels(),
                    mrConfig.getReduceChunkSummaryMaxChars(),
                    mrConfig.getFinalReduceMaxSummaries(),
                    mrConfig.getFinalReduceSummaryMaxChars(),
                    mrConfig.getFinalReduceMaxAttempts()
            );
        } catch (Exception e) {
            throw new ServletException("Unable to initialize summary orchestrator", e);
        }

        try {
            this.summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
            this.summaryStore.ensureTable();
            loadSyncSettings();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to initialize sync settings/store", e);
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

            // Manual sync now also triggers daily summary generation.
            try {
                runDailySummaryGeneration();
            } catch (Exception summaryEx) {
                log.log(Level.WARNING, "Daily summary generation failed after manual sync", summaryEx);
            }

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

            try {
                runDailySummaryGeneration();
            } catch (Exception summaryEx) {
                log.log(Level.WARNING, "Daily summary generation failed", summaryEx);
            }

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
            ensureTable(conn, tableName);

            List<JsonObject> chats = fetchWidgetChatsWithRetry(config, widgetId);
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

    // ---------- Daily summary generation + persistence (via DashboardDailySummaryStore) ----------
    private void runDailySummaryGeneration() throws Exception {
        if (summaryStore == null) {
            summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
            summaryStore.ensureTable();
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = LocalDate.now(zone);
        int slot = resolveCurrentSlot(LocalTime.now(zone));

        summaryStore.upsertProgress(day, slot, "running", 5, "Preparing daily summary context...", 0, true, false);

        List<SelectedEntry> entries = loadEntriesForDay(day, 1200);
        if (entries.isEmpty()) {
            summaryStore.upsertSummary(day, slot, "success", 100, "No entries available for this day yet.",
                    "No entries available for this day yet.", "—", "—", "—", 0, false, true);
            return;
        }

        summaryStore.upsertProgress(day, slot, "running", 25, "Analyzing entries...", entries.size(), false, false);

        ServerConfig cfg = EncryptedDbConfigStore.load();
        if (cfg == null) {
            summaryStore.upsertSummary(day, slot, "error", 100, "Server configuration missing.",
                    "Unable to generate summary: missing server configuration.", "—", "—", "—", entries.size(), false, true);
            return;
        }

        String workspaceSlug = buildSlug(cfg.getWorkspaceName());
        String baseUrl = sanitizeBaseUrl(buildBaseUrl(cfg));
        String apiKey = cfg.getApiKey();

        if (workspaceSlug == null || workspaceSlug.isBlank() || baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            summaryStore.upsertSummary(day, slot, "error", 100, "Workspace configuration incomplete.",
                    "Unable to generate summary: workspace configuration incomplete.", "—", "—", "—", entries.size(), false, true);
            return;
        }

        String targetUrl = stripTrailingSlash(baseUrl)
                + "/api/v1/workspace/"
                + URLEncoder.encode(workspaceSlug, StandardCharsets.UTF_8)
                + "/chat";

        TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(targetUrl);
        if (!trust.isValid()) {
            summaryStore.upsertSummary(day, slot, "error", 100, "Workspace URL trust validation failed.",
                    "Unable to generate summary: workspace URL trust validation failed.", "—", "—", "—", entries.size(), false, true);
            return;
        }

        summaryStore.upsertProgress(day, slot, "running", 45, "Sending analysis request...", entries.size(), false, false);

        String summaryPrompt = """
                You are analyzing today's widget usage and service performance.
                Return concise markdown with these exact sections:
                ## Overall
                ## Quality
                ## Response
                ## Usage

                Constraints:
                - Focus on observable behavior from provided chats only.
                - Mention strengths, issues, and practical recommendations.
                - Be concise and actionable.
                - Do not invent data.
                """;

        WorkspaceResponse finalResp = orchestrator.run(
                targetUrl,
                apiKey,
                summaryPrompt,
                "chat",
                "dashboard-daily-summary",
                true,
                Json.createArrayBuilder().build(),
                entries,
                "daily-summary-" + day + "-slot-" + slot
        ).finalResponse();

        String raw = finalResp == null ? "" : extractPrimaryText(finalResp.body());
        if (raw == null || raw.isBlank()) {
            raw = "No summary generated.";
        }

        String overall = section(raw, "Overall");
        String quality = section(raw, "Quality");
        String response = section(raw, "Response");
        String usage = section(raw, "Usage");

        if (overall.isBlank()) {
            overall = raw.length() > 1200 ? raw.substring(0, 1200) : raw;
        }
        if (quality.isBlank()) {
            quality = "No specific quality notes generated.";
        }
        if (response.isBlank()) {
            response = "No specific response notes generated.";
        }
        if (usage.isBlank()) {
            usage = "No specific usage notes generated.";
        }

        summaryStore.upsertSummary(day, slot, "success", 100, "Summary generated.",
                overall, quality, response, usage, entries.size(), false, true);
    }

    private List<SelectedEntry> loadEntriesForDay(LocalDate day, int maxRows) {
        List<SelectedEntry> out = new ArrayList<>();
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to list widgets for daily summary", ex);
            return out;
        }

        Timestamp start = Timestamp.valueOf(day.atStartOfDay());
        Timestamp end = Timestamp.valueOf(day.plusDays(1).atStartOfDay());

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String tableName = sanitizeWidgetTableName(w.getWidgetId());
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, start);
                    ps.setTimestamp(2, end);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (out.size() >= maxRows) {
                                return out;
                            }

                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String response = rs.getString("response_text");
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = rs.getString("session_id");

                            out.add(new SelectedEntry(
                                    chatId == null ? "" : chatId,
                                    prompt == null ? "" : prompt,
                                    response == null ? "" : response,
                                    createdAt == null ? "" : createdAt.toInstant().toString(),
                                    sessionId == null ? "" : sessionId
                            ));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to load day entries for daily summary", ex);
        }

        return out;
    }

    private int resolveCurrentSlot(LocalTime now) {
        if (now.getHour() < 6) {
            return 0;
        }
        if (now.getHour() < 12) {
            return 1;
        }
        if (now.getHour() < 18) {
            return 2;
        }
        return 3;
    }

    private String section(String markdown, String heading) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        String needle = "## " + heading;
        int start = markdown.indexOf(needle);
        if (start < 0) {
            return "";
        }
        int from = start + needle.length();
        int next = markdown.indexOf("## ", from);
        return (next < 0 ? markdown.substring(from) : markdown.substring(from, next)).trim();
    }

    private String extractPrimaryText(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject o = reader.readObject();
            String t = o.getString("textResponse", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("response", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("message", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("answer", "");
            if (!t.isBlank()) {
                return t;
            }
            t = o.getString("output", "");
            if (!t.isBlank()) {
                return t;
            }
            return body;
        } catch (Exception ignored) {
            return body;
        }
    }

    // ---------- Existing sync implementation ----------
    private List<JsonObject> fetchWidgetChatsWithRetry(ServerConfig config, String widgetId) throws IOException, InterruptedException {
        IOException lastIo = null;
        InterruptedException lastInterrupted = null;

        for (int attempt = 1; attempt <= HTTP_MAX_ATTEMPTS; attempt++) {
            long start = System.nanoTime();
            try {
                List<JsonObject> result = fetchWidgetChatsOnce(config, widgetId);
                long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                if (attempt > 1) {
                    log.info("Widget " + widgetId + " sync fetch succeeded on retry attempt " + attempt + " in " + ms + "ms");
                }
                return result;
            } catch (InterruptedException ie) {
                lastInterrupted = ie;
                Thread.currentThread().interrupt();
                break;
            } catch (IOException ioe) {
                lastIo = ioe;
                boolean retryable = isRetryable(ioe);
                if (!retryable || attempt == HTTP_MAX_ATTEMPTS) {
                    break;
                }

                long backoff = computeBackoffWithJitterMs(attempt);
                log.log(Level.WARNING, "Transient sync fetch failure for widget " + widgetId
                        + " (attempt " + attempt + "/" + HTTP_MAX_ATTEMPTS + "), retrying in "
                        + backoff + "ms: " + ioe.getMessage());
                Thread.sleep(backoff);
            }
        }

        if (lastInterrupted != null) {
            throw lastInterrupted;
        }
        throw lastIo == null ? new IOException("Sync fetch failed with unknown IO error") : lastIo;
    }

    private List<JsonObject> fetchWidgetChatsOnce(ServerConfig config, String widgetId) throws IOException, InterruptedException {
        URI uri = buildSyncUri(config, widgetId);

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(HTTP_REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET();

        String apiKey = config.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
            builder.header("X-API-Key", apiKey);
        }

        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body();

        if (response.statusCode() >= 500) {
            throw new IOException("Sync API transient server error " + response.statusCode() + ": " + truncateBody(body));
        }
        if (response.statusCode() >= 300) {
            throw new IOException("Sync API returned " + response.statusCode() + ": " + truncateBody(body));
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

    private boolean isRetryable(IOException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return true;
        }
        String m = msg.toLowerCase();
        return m.contains("fixed content-length")
                || m.contains("bytes received")
                || m.contains("buffer_underflow")
                || m.contains("connection reset")
                || m.contains("broken pipe")
                || m.contains("timed out")
                || m.contains("transient")
                || m.contains("server error 5");
    }

    private long computeBackoffWithJitterMs(int attempt) {
        long exp = HTTP_RETRY_BASE_MS * (1L << Math.max(0, attempt - 1));
        long capped = Math.min(exp, HTTP_RETRY_MAX_MS);
        long jitter = ThreadLocalRandom.current().nextLong(100, 350);
        return Math.min(capped + jitter, HTTP_RETRY_MAX_MS + 500);
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
                + "id INTEGER PRIMARY KEY, interval_seconds BIGINT NOT NULL, last_synced TIMESTAMP)";
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

    private void ensureTable(Connection conn, String tableName) throws SQLException {
        if (ensuredTables.contains(tableName)) {
            return;
        }

        String quotedTable = quoteIdentifier(tableName);
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + quotedTable
                + " (db_id BIGSERIAL PRIMARY KEY, widget_chat_id TEXT, prompt TEXT, response_text TEXT, "
                + "created_at TIMESTAMP, session_id TEXT, username TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        }

        String idxName = (tableName + "_widget_chat_id_uidx");
        if (idxName.length() > 63) {
            idxName = idxName.substring(0, 63);
        }
        String quotedIdx = quoteIdentifier(idxName);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                    + " ON " + quotedTable + " (widget_chat_id)");
        } catch (SQLException uniqueErr) {
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
        String sql = "DELETE FROM " + quoted + " a USING " + quoted + " b WHERE a.widget_chat_id = b.widget_chat_id "
                + "AND a.widget_chat_id IS NOT NULL "
                + "AND (a.created_at < b.created_at "
                + "OR (a.created_at = b.created_at AND a.db_id < b.db_id) "
                + "OR (a.created_at IS NULL AND b.created_at IS NOT NULL) "
                + "OR (a.created_at IS NULL AND b.created_at IS NULL AND a.db_id < b.db_id))";
        try (Statement stmt = conn.createStatement()) {
            int removed = stmt.executeUpdate(sql);
            if (removed > 0) {
                log.info("Removed " + removed + " duplicate rows from " + tableName);
            }
        }
    }

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
            return raw;
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

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        var meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
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
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", " ");
    }

    private Set<String> parseCsvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<String> out = ConcurrentHashMap.newKeySet();
        for (String p : csv.split(",")) {
            if (p != null && !p.isBlank()) {
                out.add(p.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
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
        }else {
            builder.append("https://").append(normalized);
        }

        boolean hasPort = normalized.matches(".*:\\d+$");
        if (!hasPort && config.getServerPort() > 0) {
            builder.append(':').append(config.getServerPort());
        }

        return stripTrailingSlash(builder.toString());
    }

    private String sanitizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String s = raw.trim();
        s = s.replaceFirst("^(https?://)+(https?://)", "$2");
        s = s.replaceFirst("^(https?://)(https?://)+", "$1");
        s = s.replace("https://https://", "https://")
                .replace("http://http://", "http://")
                .replace("http://https://", "https://")
                .replace("https://http://", "http://");
        try {
            URI u = new URI(s);
            String scheme = u.getScheme();
            String host = u.getHost();
            int port = u.getPort();
            if (scheme == null || host == null || host.isBlank()) {
                return "";
            }
            return port > 0
                    ? scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT) + ":" + port
                    : scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            return "";
        }
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
        return (value != null && value.endsWith("/")) ? value.substring(0, value.length() - 1) : value;
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
