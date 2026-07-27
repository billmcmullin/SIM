package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final long DEFAULT_INTERVAL_SECONDS = 300L;
    private static final long MIN_INTERVAL_SECONDS = 30L;
    private static final int LAST_SYNC_FLUSH_EVERY_N_RUNS = 5;
    private static final int DEFAULT_SYNC_PARALLELISM = 4;
    private static final Pattern NON_ALNUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9_]");

    private static final int HTTP_MAX_ATTEMPTS = 3;
    private static final long HTTP_RETRY_BASE_MS = 500L;
    private static final long HTTP_RETRY_MAX_MS = 5000L;
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    private static volatile DashboardDailySummaryStore summaryStore;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "widget-sync-timer");
        t.setDaemon(true);
        return t;
    });

    private static final ExecutorService syncPool = Executors.newFixedThreadPool(DEFAULT_SYNC_PARALLELISM, r -> {
        Thread t = new Thread(r, "widget-sync-worker");
        t.setDaemon(true);
        return t;
    });

    private static final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();

    private static volatile ScheduledFuture<?> scheduledFuture;
    private static volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private static volatile Timestamp lastSynced;
    private static final AtomicBoolean syncRunning = new AtomicBoolean(false);
    private static final AtomicInteger runsSinceLastSyncPersist = new AtomicInteger(0);

    private static volatile MapReduceConfig mrConfig;
    private static volatile WorkspaceClient workspaceClient;
    private static volatile WidgetReviewMapReduceOrchestrator orchestrator;
    private static volatile TrustedUrlValidator trustedUrlValidator;
    private static volatile ReviewOutputValidator reviewOutputValidator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            mrConfig = MapReduceConfig.load();
            workspaceClient = new WorkspaceClient(
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
                    mrConfig.getWorkspaceMaxRetries(),
                    mrConfig.getWorkspaceTimeout()
            );
            reviewOutputValidator = new ReviewOutputValidator();

            Set<String> allowedHosts = parseCsvToSet(System.getenv("REVIEW_TRUSTED_HOSTS"));
            Set<String> allowedSuffixes = parseCsvToSet(System.getenv("REVIEW_TRUSTED_HOST_SUFFIXES"));
            boolean allowPrivate = Boolean.parseBoolean(defaultIfBlank(System.getenv("REVIEW_ALLOW_PRIVATE_NETWORKS"), "false"));
            trustedUrlValidator = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivate);

            orchestrator = new WidgetReviewMapReduceOrchestrator(
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
        } catch (IllegalStateException e) {
            throw new ServletException("Unable to initialize summary orchestrator", e);
        }

        try {
            summaryStore = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
            summaryStore.ensureTable();
            loadSyncSettings();
        } catch (SQLException | IllegalStateException e) {
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
            List<WidgetSyncStatus> statuses = runSync(firstParam(req, "widgetId"));
            updateLastSyncedMaybePersist(true);

            // Manual sync now also triggers daily summary generation.
            try {
                runDailySummaryGeneration();
            } catch (SQLException | IOException | InterruptedException summaryEx) {
                log.log(Level.WARNING, "Daily summary generation failed after manual sync", summaryEx);
                if (summaryEx instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }

            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (WidgetSyncStatus status : statuses) {
                arr.add(status.toJson());
            }

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("widgetStatus", arr)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Widget sync failed", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Widget sync failed. Check server logs.");
        } finally {
            syncRunning.set(false);
        }
    }

    private boolean isTimerRequest(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return false;
        }
        String pattern = req.getHttpServletMapping().getPattern();
        return "/admin/widgets/sync/timer".equals(pattern);
    }

    private void handleTimerStatus(HttpServletResponse resp) throws IOException {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("intervalSeconds", syncIntervalSeconds)
                .add("lastSynced", lastSynced == null ? "" : lastSynced.toInstant().toString())
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private void handleTimerUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }

        RequestParamContext params = RequestParamContext.from(req);
        long intervalSeconds;
        try {
            String raw = params.first("intervalSeconds");
            intervalSeconds = Long.parseLong(raw == null ? "" : raw.trim());
            if (intervalSeconds < MIN_INTERVAL_SECONDS) {
                intervalSeconds = MIN_INTERVAL_SECONDS;
            }
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid timer intervalSeconds parameter", e);
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
        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private synchronized void updateInterval(long newIntervalSeconds) {
        syncIntervalSeconds = newIntervalSeconds;
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
            } catch (SQLException | IOException | InterruptedException summaryEx) {
                log.log(Level.WARNING, "Daily summary generation failed", summaryEx);
                if (summaryEx instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }

            log.log(Level.INFO, () -> "Automatic widget sync completed. Synced " + statuses.size() + " widget entries.");
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Automatic widget sync failed", e);
        } finally {
            syncRunning.set(false);
        }
    }

    private List<WidgetSyncStatus> runSync(String requestedWidgetId) throws SQLException, IOException, InterruptedException {
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
                log.log(Level.WARNING, "Widget sync task failed", ee);
                statuses.add(new WidgetSyncStatus("unknown", "unknown", false, false,
                        "Sync failed. Check server logs."));
            }
        }
        return statuses;
    }

    private WidgetSyncStatus syncSingleWidget(ServerConfig config, String widgetId) {
        String tableName = sanitizeWidgetTableName(widgetId);

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureTable(conn, tableName);

            List<JsonObject> chats = fetchWidgetChatsWithRetry(config, widgetId);
            int inserted = insertWidgetChats(conn, tableName, chats);

            String message = chats.isEmpty()
                    ? "No chat rows returned from server."
                    : "Fetched " + chats.size() + " chat(s), inserted " + inserted + " new chat(s).";

            return new WidgetSyncStatus(widgetId, tableName, true, true, message);
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Failed to sync widget " + String.valueOf(widgetId), e);
            return new WidgetSyncStatus(widgetId, tableName, false, false, "Sync failed. Check server logs.");
        }
    }

    // ---------- Daily summary generation + persistence (via DashboardDailySummaryStore) ----------
    private void runDailySummaryGeneration() throws SQLException, IOException, InterruptedException {
        if (summaryStore == null) {
            summaryStore = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
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

        String canonicalTargetUrl = canonicalizeHttpUrl(targetUrl);
        if (canonicalTargetUrl.isBlank()) {
            summaryStore.upsertSummary(day, slot, "error", 100, "Workspace URL canonicalization failed.",
                "Unable to generate summary: workspace URL canonicalization failed.", "—", "—", "—", entries.size(), false, true);
            return;
        }

        if (!apiKey.isBlank() && !isHttpsUrl(canonicalTargetUrl)) {
            summaryStore.upsertSummary(day, slot, "error", 100, "Workspace URL must be HTTPS when API key is configured.",
                    "Unable to generate summary: API key requires HTTPS workspace URL.", "—", "—", "—", entries.size(), false, true);
            return;
        }

        TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(canonicalTargetUrl);
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

        WorkspaceResponse finalResp = runSummaryOrchestration(
            canonicalTargetUrl,
            apiKey,
            summaryPrompt,
            entries,
            "daily-summary-" + day + "-slot-" + slot
        );

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
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Unable to list widgets for daily summary", ex);
            return out;
        }

        Timestamp start = Timestamp.valueOf(day.atStartOfDay());
        Timestamp end = Timestamp.valueOf(day.plusDays(1).atStartOfDay());

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
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

                            String chatId = readDbText(rs, "widget_chat_id", 256);
                            String prompt = readDbText(rs, "prompt", 8000);
                            String response = readDbText(rs, "response_text", 32000);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = readDbText(rs, "session_id", 256);

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
        } catch (SQLException ex) {
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
        } catch (JsonException | ClassCastException ex) {
            log.log(Level.FINE, "Unable to parse primary text response as JSON", ex);
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
                    log.log(Level.INFO, "Widget {0} sync fetch succeeded on retry attempt {1} in {2}ms",
                            new Object[]{widgetId, attempt, ms});
                }
                return result;
            } catch (InterruptedException ie) {
                log.log(Level.FINE, "Widget sync fetch interrupted", ie);
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
                log.log(Level.WARNING,
                    "Transient sync fetch failure for widget {0} (attempt {1}/{2}), retrying in {3}ms: {4}",
                    new Object[]{widgetId, attempt, HTTP_MAX_ATTEMPTS, backoff, ioe.getMessage()});
                TimeUnit.MILLISECONDS.sleep(backoff);
            }
        }

        if (lastInterrupted != null) {
            throw lastInterrupted;
        }
        throw lastIo == null ? new IOException("Sync fetch failed with unknown IO error") : lastIo;
    }

    private List<JsonObject> fetchWidgetChatsOnce(ServerConfig config, String widgetId) throws IOException, InterruptedException {
        URI uri = buildSyncUri(config, widgetId);
        if (uri == null) {
            throw new IOException("Sync API URL is missing");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(HTTP_REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET();

        String apiKey = config.getApiKey();
        boolean apiKeyConfigured = apiKey != null && !apiKey.isBlank();
        if (apiKeyConfigured && !isHttpsUri(uri)) {
            throw new IOException("API key configured but sync URL is not HTTPS");
        }
        if (apiKeyConfigured) {
            builder.header("Authorization", "Bearer " + apiKey);
            builder.header("X-API-Key", apiKey);
        }

        HttpResponse<InputStream> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream bodyStream = response.body()) {
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (response.statusCode() >= 500) {
                throw new IOException("Sync API transient server error " + response.statusCode()
                        + " (contentType=" + contentType + ')');
            }
            if (response.statusCode() >= 300) {
                throw new IOException("Sync API returned " + response.statusCode()
                        + " (contentType=" + contentType + ')');
            }

            if (!contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
                throw new IOException(String.format("Unexpected content type '%s'", contentType));
            }

            try {
                JsonNode root = OBJECT_MAPPER.readTree(bodyStream);
                return normalizeResponse(root);
            } catch (JsonProcessingException je) {
                throw new IOException("Invalid JSON received from sync API", je);
            }
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
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            SyncSettings settings = readSyncSettings(conn);
            if (settings.intervalSeconds > 0) {
                syncIntervalSeconds = settings.intervalSeconds;
            }
            lastSynced = settings.lastSynced;
        }
    }

    private void persistSyncSettings() {
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to persist sync settings", e);
        }
    }

    private void ensureSyncSettingsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS widget_sync_settings ("
                + "id INTEGER PRIMARY KEY, interval_seconds BIGINT NOT NULL, last_synced TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private SyncSettings readSyncSettings(Connection conn) throws SQLException {
        String sql = "SELECT interval_seconds, last_synced FROM widget_sync_settings WHERE id = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long persistedInterval = readPersistedIntervalSeconds(rs, "interval_seconds", syncIntervalSeconds);
                if (persistedInterval < MIN_INTERVAL_SECONDS || persistedInterval > TimeUnit.DAYS.toSeconds(30)) {
                    persistedInterval = syncIntervalSeconds;
                }

                Timestamp persistedLastSynced = sanitizePersistedTimestamp(readDbTimestamp(rs, "last_synced"));
                return new SyncSettings(persistedInterval, persistedLastSynced);
            }
        }
        upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        return new SyncSettings(syncIntervalSeconds, lastSynced);
    }

    private Timestamp sanitizePersistedTimestamp(Timestamp value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        Instant min = Instant.parse("2000-01-01T00:00:00Z");
        Instant max = Instant.now().plus(Duration.ofDays(1));
        if (instant.isBefore(min) || instant.isAfter(max)) {
            return null;
        }
        return value;
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
        if (req == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
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
        try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
            ps.execute();
        }

        String idxName = (tableName + "_widget_chat_id_uidx");
        if (idxName.length() > 63) {
            idxName = idxName.substring(0, 63);
        }
        String quotedIdx = quoteIdentifier(idxName);

        try (PreparedStatement ps = conn.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                + " ON " + quotedTable + " (widget_chat_id)")) {
            ps.execute();
        } catch (SQLException uniqueErr) {
            log.log(Level.FINE, "Unique index creation failed, attempting dedupe before retry", uniqueErr);
            dedupeByWidgetChatId(conn, tableName);
            try (PreparedStatement ps = conn.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                    + " ON " + quotedTable + " (widget_chat_id)")) {
                ps.execute();
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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int removed = ps.executeUpdate();
            if (removed > 0) {
                log.log(Level.INFO, "Removed {0} duplicate rows from {1}", new Object[]{removed, tableName});
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
                String normalizedResponse = getString(chat, "response_text");
                ps.setString(3, normalizedResponse != null ? normalizedResponse : formatResponseText(chat));
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
        if (t.isEmpty()) {
            return raw;
        }
        char first = t.charAt(0);
        if (first != '{' && first != '[') {
            return raw;
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(raw);
            if (node != null && node.isObject()) {
                String text = textValue(node, "text");
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        } catch (JsonProcessingException ex) {
            log.log(Level.FINE, "Response text is not JSON object payload", ex);
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
            log.log(Level.FINE, "Unable to parse timestamp: {0}", created);
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
            URI base = URI.create(normalizedHost);
            String path = base.getPath() == null ? "" : base.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (!path.contains("/api")) {
                path = path + "/api";
            }
            String apiPath = path + "/v1/embed/" + URLEncoder.encode(widgetId, StandardCharsets.UTF_8) + "/chats";
            String baseAuthority = base.getScheme() + "://" + base.getAuthority();
            return URI.create(baseAuthority + apiPath);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base URL for sync endpoint", e);
        }
    }

    private List<JsonObject> normalizeResponse(JsonNode root) {
        List<JsonObject> normalized = new ArrayList<>();
        if (root == null) {
            return normalized;
        }

        if (root.isArray()) {
            for (JsonNode node : root) {
                addNormalizedObject(normalized, node);
            }
            return normalized;
        }

        if (root.isObject()) {
            for (String key : List.of("items", "data", "results", "chats", "entries")) {
                JsonNode candidate = root.get(key);
                if (candidate != null && candidate.isArray()) {
                    for (JsonNode node : candidate) {
                        addNormalizedObject(normalized, node);
                    }
                    return normalized;
                }
            }
            addNormalizedObject(normalized, root);
        }
        return normalized;
    }

    private void addNormalizedObject(List<JsonObject> normalized, JsonNode node) {
        if (node == null || !node.isObject()) {
            return;
        }
        normalized.add(toFlatSyncObject(node));
    }

    private JsonObject toFlatSyncObject(JsonNode node) {
        String createdAt = textValue(node, "createdAt");
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = textValue(node, "created_at");
        }

        String responseText = extractResponseText(node);

        return Json.createObjectBuilder()
                .add("id", defaultString(textValue(node, "id")))
                .add("prompt", defaultString(textValue(node, "prompt")))
                .add("response_text", defaultString(responseText))
                .add("created_at", defaultString(createdAt))
                .add("session_id", defaultString(textValue(node, "session_id")))
                .add("username", defaultString(textValue(node, "username")))
                .build();
    }

    private String extractResponseText(JsonNode chatNode) {
        if (chatNode == null || !chatNode.isObject()) {
            return null;
        }

        String text = extractText(chatNode.get("response"));
        if (text == null || text.isBlank()) {
            JsonNode rawChat = chatNode.get("raw_chat");
            if (rawChat != null && rawChat.isObject()) {
                text = extractText(rawChat.get("response"));
            }
        }
        return humanize(normalizeToJsonText(text));
    }

    private String extractText(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isObject()) {
            return textValue(value, "text");
        }
        if (value.isTextual()) {
            return value.asText();
        }
        return value.toString();
    }

    private String textValue(JsonNode source, String key) {
        if (source == null || key == null || key.isBlank() || !source.isObject()) {
            return null;
        }
        JsonNode value = source.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isTextual() ? value.asText() : value.toString();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
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
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private void jsonError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, payload);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(payload);
        }
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
            URI u = URI.create(s);
            String scheme = u.getScheme();
            String host = u.getHost();
            int port = u.getPort();
            if (scheme == null || host == null || host.isBlank()) {
                return "";
            }
                String normalized = scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT);
                return port > 0 ? normalized + ':' + port : normalized;
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid base URL: {0}", s);
            return "";
        }
    }

    private WorkspaceResponse runSummaryOrchestration(
            String targetUrl,
            String apiKey,
            String summaryPrompt,
            List<SelectedEntry> entries,
            String requestId
    ) throws IOException, InterruptedException {
        return SummaryOrchestrationRunner.run(
                orchestrator,
                targetUrl,
                apiKey,
                summaryPrompt,
                entries,
                requestId
        );
    }

    private String canonicalizeHttpUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "";
        }
        try {
            URI parsed = URI.create(rawUrl.trim()).normalize();
            String scheme = parsed.getScheme();
            String host = parsed.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return "";
            }
            String schemeLower = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(schemeLower) && !"https".equals(schemeLower)) {
                return "";
            }
            String hostLower = host.toLowerCase(Locale.ROOT);
            int port = parsed.getPort();
            String path = parsed.getPath() == null ? "" : parsed.getPath();
            String query = parsed.getQuery();

            StringBuilder canonical = new StringBuilder();
            canonical.append(schemeLower).append("://").append(hostLower);
            if (port >= 0) {
                canonical.append(':').append(port);
            }
            canonical.append(path);
            if (query != null && !query.isBlank()) {
                canonical.append('?').append(query);
            }
            return URI.create(canonical.toString()).normalize().toString();
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to canonicalize URL", e);
            return "";
        }
    }

    private boolean isHttpsUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            return isHttpsUri(uri);
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid URL in HTTPS check", e);
            return false;
        }
    }

    private boolean isHttpsUri(URI uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme());
    }

    private long readPersistedIntervalSeconds(ResultSet rs, String columnName, long fallback) throws SQLException {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 64).trim();
        if (trimmed.isBlank()) {
            return fallback;
        }
        if (!trimmed.matches("^\\d{1,12}$")) {
            return fallback;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid persisted sync interval value", ex);
            return fallback;
        }
    }

    private Timestamp readDbTimestamp(ResultSet rs, String columnName) throws SQLException {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return null;
        }
        String normalized = readDbText(rs, columnName, 128).trim();
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            try {
                return Timestamp.from(OffsetDateTime.parse(normalized).toInstant());
            } catch (DateTimeParseException ignored) {
                try {
                    return Timestamp.from(Instant.parse(normalized));
                } catch (DateTimeParseException ignoredToo) {
                    log.log(Level.FINE, "Unable to parse persisted timestamp value", ex);
                    return null;
                }
            }
        }
    }

    private String readDbText(ResultSet rs, String columnName, int maxLen) throws SQLException {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return "";
        }
        String text = rs.getString(columnName);
        if (text == null) {
            return "";
        }
        text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        text = stripControlCharacters(text);
        if (text.indexOf('\u0000') >= 0) {
            text = text.replace('\u0000', ' ');
        }
        if (maxLen > 0 && text.length() > maxLen) {
            return text.substring(0, maxLen);
        }
        return text;
    }

    private String firstParam(HttpServletRequest req, String name) {
        return RequestParamContext.from(req).first(name);
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String stripControlCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || !Character.isISOControl(c)) {
                sanitized.append(c);
            }
        }
        return sanitized.toString();
    }

    private static final class RequestParamContext {

        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest req) {
            return new RequestParamContext(req);
        }

        private String first(String name) {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (request == null) {
                return null;
            }
            String value = request.getParameter(name);
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\r", "").replace("\n", "").trim();
            return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
        }
    }

    private static final class WidgetSyncStatus {

        final String widgetId;
        final String tableName;
        final boolean tableExists;
        final boolean synced;
        final String message;

        private WidgetSyncStatus(String widgetId, String tableName, boolean tableExists, boolean synced, String message) {
            this.widgetId = widgetId;
            this.tableName = tableName;
            this.tableExists = tableExists;
            this.synced = synced;
            this.message = message;
        }

        JsonObject toJson() {
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

        final long intervalSeconds;
        final Timestamp lastSynced;

        private SyncSettings(long intervalSeconds, Timestamp lastSynced) {
            this.intervalSeconds = intervalSeconds;
            this.lastSynced = lastSynced;
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

}
