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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import com.sim.chatserver.service.ApiAuthResolver;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.ServerDiagnosticsLog;
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

@WebServlet(name = "WidgetSyncServlet", urlPatterns = {"/admin/widgets/sync", "/admin/widgets/sync/timer", "/admin/widgets/summary/retry"})
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
    private static final int MAX_SYNC_PARALLELISM = 16;
    private static final int SYNC_PARALLELISM = parseBoundedIntEnv(
            "SIM_WIDGET_SYNC_PARALLELISM",
            DEFAULT_SYNC_PARALLELISM,
            1,
            MAX_SYNC_PARALLELISM
    );
    private static final long DEFAULT_SYNC_MIN_REQUEST_GAP_MS = 200L;
    private static final long SYNC_MIN_REQUEST_GAP_MS = parseBoundedLongEnv(
            "SIM_WIDGET_SYNC_MIN_REQUEST_GAP_MS",
            DEFAULT_SYNC_MIN_REQUEST_GAP_MS,
            0L,
            10_000L
    );
    private static final Pattern NON_ALNUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9_]");

    private static final int HTTP_MAX_ATTEMPTS = 3;
    private static final long HTTP_RETRY_BASE_MS = 500L;
    private static final long HTTP_RETRY_MAX_MS = 5000L;
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final String SYNC_DISABLED_PROPERTY = "sim.widget.sync.disabled";
    private static final String REQUIRE_HTTPS_WITH_AUTH_ENV = "WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH";
    private static final String REQUIRE_HTTPS_WITH_AUTH_PROP = "sim.widget.sync.require.https.with.auth";
    private static final String SUMMARY_RETRY_PATTERN = "/admin/widgets/summary/retry";
    private static final String SUMMARY_FAILURE_QUALITY_PLACEHOLDER = "Summary unavailable due to generation failure.";
    private static final String SUMMARY_FAILURE_RESPONSE_PLACEHOLDER = "No response analysis available until a manual summary retry succeeds.";
    private static final String SUMMARY_FAILURE_USAGE_PLACEHOLDER = "Usage analysis is temporarily unavailable until summary generation resumes.";

    private enum AuthHeaderMode {
        CUSTOM_HEADER,
        AUTH_BEARER,
        AUTH_RAW,
        X_API_KEY,
        AUTH_BEARER_AND_X_API_KEY
    }

    private static final class SyncHttpResult {
        final HttpResponse<InputStream> response;
        final String authSource;
        final AuthHeaderMode authMode;

        private SyncHttpResult(HttpResponse<InputStream> response, String authSource, AuthHeaderMode authMode) {
            this.response = response;
            this.authSource = authSource;
            this.authMode = authMode;
        }
    }

    private static volatile DashboardDailySummaryStore summaryStore;

    private static volatile ScheduledExecutorService scheduler = createScheduler();
    private static volatile ExecutorService syncPool = createSyncPool();

    private static final Object syncRateLimitLock = new Object();
    private static volatile long lastSyncRequestAtMs;

    private static final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();

    private static volatile ScheduledFuture<?> scheduledFuture;
    private static volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private static volatile Timestamp lastSynced;
    private static final AtomicBoolean syncRunning = new AtomicBoolean(false);
    private static final AtomicInteger runsSinceLastSyncPersist = new AtomicInteger(0);
    private static final Object syncProgressLock = new Object();
    private static volatile Instant syncStartedAt;
    private static volatile Instant syncFinishedAt;
    private static volatile String syncPhase = "idle";
    private static volatile String syncStatusMessage = "";
    private static volatile int syncTotalWidgets;
    private static final AtomicInteger syncCompletedWidgets = new AtomicInteger(0);
    private static final AtomicInteger syncSucceededWidgets = new AtomicInteger(0);
    private static final AtomicInteger syncFailedWidgets = new AtomicInteger(0);
    private static volatile int syncProgressPercent;
    private static volatile boolean summaryAutoPausedUntilManualSuccess;
    private static volatile String summaryAutoPausedReason = "";

    private static volatile MapReduceConfig mrConfig;
    private static volatile WorkspaceClient workspaceClient;
    private static volatile WidgetReviewMapReduceOrchestrator orchestrator;
    private static volatile TrustedUrlValidator trustedUrlValidator;
    private static volatile ReviewOutputValidator reviewOutputValidator;
    private AppDataSourceHolder dsHolder;

    private static ScheduledExecutorService createScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "widget-sync-timer");
            t.setDaemon(true);
            return t;
        });
    }

    private static ExecutorService createSyncPool() {
        return Executors.newFixedThreadPool(SYNC_PARALLELISM, r -> {
            Thread t = new Thread(r, "widget-sync-worker");
            t.setDaemon(true);
            return t;
        });
    }

    private static synchronized void ensureExecutorsRunning() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = createScheduler();
        }
        if (syncPool == null || syncPool.isShutdown() || syncPool.isTerminated()) {
            syncPool = createSyncPool();
        }
    }

    private static boolean isSyncDisabled() {
        return Boolean.parseBoolean(System.getProperty(SYNC_DISABLED_PROPERTY, "false"));
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        log.log(Level.INFO,
                "Widget sync runtime config: parallelism={0}, minRequestGapMs={1}",
                new Object[]{SYNC_PARALLELISM, SYNC_MIN_REQUEST_GAP_MS});

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

        ensureExecutorsRunning();
        if (isSyncDisabled()) {
            log.info("Widget sync scheduler disabled by system property for this runtime.");
        } else {
            scheduleSyncTask();
        }
    }

    @Override
    public void destroy() {
        synchronized (WidgetSyncServlet.class) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }
            if (syncPool != null && !syncPool.isShutdown()) {
                syncPool.shutdownNow();
            }
        }
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

        if (isSummaryRetryRequest(req)) {
            handleManualSummaryRetry(req, resp);
            return;
        }

        if (!authorizeAdmin(req, resp)) {
            return;
        }

        if (!syncRunning.compareAndSet(false, true)) {
            jsonError(resp, HttpServletResponse.SC_CONFLICT, "Sync already in progress.");
            return;
        }

        beginSyncProgress("manual_sync", "Manual sync started.");
        try {
            List<WidgetSyncStatus> statuses = runSync(firstParam(req, "widgetId"));
            updateLastSyncedMaybePersist(true);

            boolean summaryRan = false;
            boolean summarySuccess = true;

            // Manual sync keeps summary generation behavior, but does not bypass pause-on-failure policy.
            if (summaryAutoPausedUntilManualSuccess) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because automatic summaries are paused. reason={0}",
                        defaultIfBlank(summaryAutoPausedReason, "manual retry required"));
            } else {
                summaryRan = true;
                updateSyncProgress("summary_generation", "Generating daily summary...", 92);
                summarySuccess = runDailySummaryGeneration(false);
            }

            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (WidgetSyncStatus status : statuses) {
                arr.add(status.toJson());
            }

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("widgetStatus", arr)
                    .build();

            String completionMessage;
            if (!summaryRan) {
                completionMessage = "Sync completed. Summary generation is paused until manual retry.";
            } else if (!summarySuccess) {
                completionMessage = "Sync completed. Summary generation failed and automatic summaries are now paused.";
            } else {
                completionMessage = "Sync completed successfully.";
            }
            finishSyncProgress(true, completionMessage);

            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Widget sync failed", e);
            finishSyncProgress(false, "Sync failed. Check server logs.");
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

    private boolean isSummaryRetryRequest(HttpServletRequest req) {
        if (req == null || req.getHttpServletMapping() == null) {
            return false;
        }
        String pattern = req.getHttpServletMapping().getPattern();
        return SUMMARY_RETRY_PATTERN.equals(pattern);
    }

    private void handleManualSummaryRetry(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }

        if (!syncRunning.compareAndSet(false, true)) {
            jsonError(resp, HttpServletResponse.SC_CONFLICT, "Sync or summary generation is already in progress.");
            return;
        }

        beginSyncProgress("manual_summary_retry", "Manual summary retry started.");
        try {
            updateSyncProgress("summary_generation", "Generating daily summary...", 40);
            boolean success = runDailySummaryGeneration(true);
            String message = success
                    ? "Manual summary retry succeeded. Automatic summary generation resumed."
                    : defaultIfBlank(summaryAutoPausedReason, "Manual summary retry failed. Automatic summary remains paused.");

            finishSyncProgress(success, message);

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", success ? "ok" : "error")
                    .add("message", message)
                    .add("summaryAutoPaused", summaryAutoPausedUntilManualSuccess)
                    .build();
            writeJson(resp, success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_GATEWAY, payload);
        } finally {
            syncRunning.set(false);
        }
    }

    private void handleTimerStatus(HttpServletResponse resp) throws IOException {
        boolean running = syncRunning.get();
        String startedAt = syncStartedAt == null ? "" : syncStartedAt.toString();
        String finishedAt = syncFinishedAt == null ? "" : syncFinishedAt.toString();
        int totalWidgets = Math.max(0, syncTotalWidgets);
        int completedWidgets = Math.max(0, syncCompletedWidgets.get());
        int succeededWidgets = Math.max(0, syncSucceededWidgets.get());
        int failedWidgets = Math.max(0, syncFailedWidgets.get());
        int percent = computeSyncProgressPercent(running, totalWidgets, completedWidgets);
        long runningSeconds = computeRunningSeconds(running);

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("intervalSeconds", syncIntervalSeconds)
            .add("syncRunning", running)
                .add("lastSynced", lastSynced == null ? "" : lastSynced.toInstant().toString())
            .add("syncStartedAt", startedAt)
            .add("syncFinishedAt", finishedAt)
            .add("syncPhase", defaultIfBlank(syncPhase, running ? "running" : "idle"))
            .add("syncMessage", defaultIfBlank(syncStatusMessage, ""))
            .add("widgetsTotal", totalWidgets)
            .add("widgetsCompleted", completedWidgets)
            .add("widgetsSucceeded", succeededWidgets)
            .add("widgetsFailed", failedWidgets)
            .add("progressPercent", percent)
            .add("runningSeconds", runningSeconds)
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
        if (isSyncDisabled()) {
            return;
        }
        ensureExecutorsRunning();
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
        beginSyncProgress("scheduled_sync", "Scheduled sync started.");
        try {
            List<WidgetSyncStatus> statuses = runSync(null);
            updateLastSyncedMaybePersist(false);

            boolean summaryRan = false;
            boolean summarySuccess = true;

            if (summaryAutoPausedUntilManualSuccess) {
                log.log(Level.INFO,
                        "Skipping scheduled summary generation while paused. reason={0}",
                        defaultIfBlank(summaryAutoPausedReason, "manual retry required"));
            } else {
                summaryRan = true;
                updateSyncProgress("summary_generation", "Generating daily summary...", 92);
                summarySuccess = runDailySummaryGeneration(false);
            }

            if (!summaryRan) {
                finishSyncProgress(true, "Scheduled sync completed. Summary generation remains paused.");
            } else if (!summarySuccess) {
                finishSyncProgress(true, "Scheduled sync completed. Summary generation failed and was paused.");
            } else {
                finishSyncProgress(true, "Scheduled sync completed successfully.");
            }

            log.log(Level.INFO, () -> "Automatic widget sync completed. Synced " + statuses.size() + " widget entries.");
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Automatic widget sync failed", e);
            finishSyncProgress(false, "Scheduled sync failed. Check server logs.");
        } finally {
            syncRunning.set(false);
        }
    }

    private void logDailySummaryFailure(String context, Exception failure) {
        String errorRef = UUID.randomUUID().toString();
        log.log(Level.WARNING,
            "Daily summary generation failed context={0} errorRef={1}. Full stack trace saved to server diagnostics log.",
            new Object[]{context, errorRef});

        ServerDiagnosticsLog.write(
                "widget-sync",
                errorRef,
                "daily-summary-failed",
                "context=" + context
                        + "\nmessage=" + (failure == null ? "" : String.valueOf(failure.getMessage())),
                failure
        );
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
            .sorted((left, right) -> left.getWidgetId().compareToIgnoreCase(right.getWidgetId()))
                .toList();

        startWidgetSyncProgress(valid.size());

        List<Callable<WidgetSyncStatus>> tasks = new ArrayList<>();
        for (WidgetEntry widget : valid) {
            tasks.add(() -> syncSingleWidget(config, widget.getWidgetId()));
        }

        List<WidgetSyncStatus> statuses = new ArrayList<>(tasks.size());
        ensureExecutorsRunning();
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
        boolean success = false;

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureTable(conn, tableName);

            List<JsonObject> chats = fetchWidgetChatsWithRetry(config, widgetId);
            int inserted = insertWidgetChats(conn, tableName, chats);

            String message = chats.isEmpty()
                    ? "No chat rows returned from server."
                    : "Fetched " + chats.size() + " chat(s), inserted " + inserted + " new chat(s).";

            success = true;
            return new WidgetSyncStatus(widgetId, tableName, true, true, message);
        } catch (SQLException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Failed to sync widget " + String.valueOf(widgetId), e);
            return new WidgetSyncStatus(widgetId, tableName, false, false, "Sync failed. Check server logs.");
        } finally {
            markWidgetSyncCompletion(success);
        }
    }

    private void beginSyncProgress(String phase, String message) {
        synchronized (syncProgressLock) {
            syncStartedAt = Instant.now();
            syncFinishedAt = null;
            syncPhase = defaultIfBlank(phase, "running");
            syncStatusMessage = defaultIfBlank(message, "Sync running.");
            syncTotalWidgets = 0;
            syncCompletedWidgets.set(0);
            syncSucceededWidgets.set(0);
            syncFailedWidgets.set(0);
            syncProgressPercent = 2;
        }
    }

    private void startWidgetSyncProgress(int totalWidgets) {
        synchronized (syncProgressLock) {
            syncPhase = "syncing_widgets";
            syncTotalWidgets = Math.max(0, totalWidgets);
            syncCompletedWidgets.set(0);
            syncSucceededWidgets.set(0);
            syncFailedWidgets.set(0);

            if (syncTotalWidgets == 0) {
                syncProgressPercent = Math.max(syncProgressPercent, 90);
                syncStatusMessage = "No widgets available to sync.";
                return;
            }

            syncProgressPercent = Math.max(syncProgressPercent, 5);
            syncStatusMessage = "Syncing widget tables...";
        }
    }

    private void markWidgetSyncCompletion(boolean success) {
        int completed = syncCompletedWidgets.incrementAndGet();
        if (success) {
            syncSucceededWidgets.incrementAndGet();
        } else {
            syncFailedWidgets.incrementAndGet();
        }

        synchronized (syncProgressLock) {
            int total = Math.max(0, syncTotalWidgets);
            if (total > 0) {
                int widgetPercent = (int) Math.round((completed * 100.0d) / total);
                int scaledPercent = 5 + (int) Math.round(widgetPercent * 0.85d);
                syncProgressPercent = Math.max(syncProgressPercent, Math.min(90, scaledPercent));
            }

            syncStatusMessage = "Processed "
                    + completed
                    + "/"
                    + Math.max(total, completed)
                    + " widgets ("
                    + syncSucceededWidgets.get()
                    + " succeeded, "
                    + syncFailedWidgets.get()
                    + " failed).";
        }
    }

    private void updateSyncProgress(String phase, String message, int minimumPercent) {
        synchronized (syncProgressLock) {
            if (phase != null && !phase.isBlank()) {
                syncPhase = phase;
            }
            if (message != null && !message.isBlank()) {
                syncStatusMessage = message;
            }
            syncProgressPercent = Math.max(syncProgressPercent, clampPercent(minimumPercent));
        }
    }

    private void finishSyncProgress(boolean success, String message) {
        synchronized (syncProgressLock) {
            syncFinishedAt = Instant.now();
            syncPhase = success ? "completed" : "failed";
            syncStatusMessage = defaultIfBlank(message, success ? "Sync completed." : "Sync failed.");
            syncProgressPercent = success ? 100 : Math.max(1, Math.min(99, syncProgressPercent));
        }
    }

    private int computeSyncProgressPercent(boolean running, int totalWidgets, int completedWidgets) {
        int percent = clampPercent(syncProgressPercent);
        if (totalWidgets > 0) {
            int computed = (int) Math.round((Math.max(0, completedWidgets) * 100.0d) / totalWidgets);
            percent = Math.max(percent, clampPercent(computed));
        }
        if (running && percent >= 100) {
            return 99;
        }
        return clampPercent(percent);
    }

    private long computeRunningSeconds(boolean running) {
        Instant started = syncStartedAt;
        if (started == null) {
            return 0L;
        }
        Instant end = running ? Instant.now() : (syncFinishedAt == null ? Instant.now() : syncFinishedAt);
        return Math.max(0L, Duration.between(started, end).toSeconds());
    }

    private int clampPercent(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    // ---------- Daily summary generation + persistence (via DashboardDailySummaryStore) ----------
    private boolean runDailySummaryGeneration(boolean manualTrigger) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = LocalDate.now(zone);
        int slot = resolveCurrentSlot(LocalTime.now(zone));
        int entryCount = 0;

        try {
            if (summaryStore == null) {
                summaryStore = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
                summaryStore.ensureTable();
            }

            String startMessage = manualTrigger
                    ? "Manual summary retry started..."
                    : "Preparing daily summary context...";
            summaryStore.upsertProgress(day, slot, "running", 5, startMessage, 0, true, false);

            List<SelectedEntry> entries = loadEntriesForDay(day, 1200);
            entryCount = entries.size();

            if (entries.isEmpty()) {
                summaryStore.upsertSummary(day, slot, "success", 100, "No entries available for this day yet.",
                        "No entries available for this day yet.", "—", "—", "—", 0, false, true);
                resumeAutomaticSummaryGeneration("No entries available; automatic summary generation remains enabled.");
                return true;
            }

            summaryStore.upsertProgress(day, slot, "running", 25, "Analyzing entries...", entries.size(), false, false);

            ServerConfig cfg = EncryptedDbConfigStore.load();
            if (cfg == null) {
                return failDailySummary(day, slot, entryCount,
                        "Server configuration missing.",
                        "Unable to generate summary: missing server configuration.");
            }

            String workspaceSlug = buildSlug(cfg.getWorkspaceName());
            String baseUrl = sanitizeBaseUrl(buildBaseUrl(cfg));
            String apiKey = cfg.getApiKey();

            if (workspaceSlug == null || workspaceSlug.isBlank() || baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace configuration incomplete.",
                        "Unable to generate summary: workspace configuration incomplete.");
            }

            String targetUrl = stripTrailingSlash(baseUrl)
                    + "/api/v1/workspace/"
                    + URLEncoder.encode(workspaceSlug, StandardCharsets.UTF_8)
                    + "/chat";

            String canonicalTargetUrl = canonicalizeHttpUrl(targetUrl);
            if (canonicalTargetUrl.isBlank()) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace URL canonicalization failed.",
                        "Unable to generate summary: workspace URL canonicalization failed.");
            }

            boolean requireHttpsWithAuth = isHttpsRequiredWithAuth();
            if (!apiKey.isBlank() && !isHttpsUrl(canonicalTargetUrl) && requireHttpsWithAuth) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace URL must be HTTPS when API key is configured.",
                        "Unable to generate summary: API key requires HTTPS workspace URL.");
            }

            if (!apiKey.isBlank() && !isHttpsUrl(canonicalTargetUrl) && !requireHttpsWithAuth) {
                log.info(() -> "Widget summary generation allowing HTTP workspace URL with API key because "
                        + REQUIRE_HTTPS_WITH_AUTH_ENV + " is disabled. url=" + canonicalTargetUrl);
            }

            TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(canonicalTargetUrl);
            if (!trust.isValid()) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace URL trust validation failed.",
                        "Unable to generate summary: workspace URL trust validation failed.");
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

            String summaryRequestId = "daily-summary-" + day + "-slot-" + slot;

            WorkspaceResponse finalResp = runSummaryOrchestration(
                    canonicalTargetUrl,
                    apiKey,
                    summaryPrompt,
                    entries,
                    summaryRequestId
            );

            if (finalResp == null) {
                return failDailySummary(day, slot, entryCount,
                        "Summary request failed.",
                        "Summary request returned no response. Use manual retry to resume automatic summaries.");
            }

            int statusCode = finalResp.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String upstreamText = extractPrimaryText(finalResp.body());
                if (upstreamText == null || upstreamText.isBlank()) {
                    upstreamText = "Workspace returned HTTP " + statusCode + ".";
                }
                String truncated = upstreamText.length() > 1200 ? upstreamText.substring(0, 1200) : upstreamText;

                return failDailySummary(day, slot, entryCount,
                        "Summary request failed with upstream status " + statusCode + ".",
                        truncated);
            }

            String raw = extractPrimaryText(finalResp.body());
            if (raw == null || raw.isBlank()) {
                return failDailySummary(day, slot, entryCount,
                        "Summary request returned no message.",
                        "Summary generation failed: no summary message was returned by the workspace service.");
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
            resumeAutomaticSummaryGeneration("Summary generated successfully.");
            return true;
        } catch (SQLException | IOException | RuntimeException e) {
            logDailySummaryFailure(manualTrigger ? "manual-summary-retry" : "auto-summary", e);
            return failDailySummary(day, slot, Math.max(0, entryCount),
                    "Summary generation failed.",
                    "Summary generation failed due to an internal error: " + defaultString(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logDailySummaryFailure(manualTrigger ? "manual-summary-retry" : "auto-summary", e);
            return failDailySummary(day, slot, Math.max(0, entryCount),
                    "Summary generation interrupted.",
                    "Summary generation was interrupted before completion.");
        }
    }

    private boolean failDailySummary(
            LocalDate day,
            int slot,
            int entryCount,
            String message,
            String overall
    ) {
        String pausedMessage = defaultIfBlank(message, "Summary generation failed.")
                + " Automatic summary generation is paused until an ADMIN runs manual retry.";

        if (summaryStore != null) {
            summaryStore.upsertSummary(
                day,
                slot,
                "error",
                100,
                pausedMessage,
                defaultIfBlank(overall, "Summary generation failed. Manual retry is required to continue."),
                SUMMARY_FAILURE_QUALITY_PLACEHOLDER,
                SUMMARY_FAILURE_RESPONSE_PLACEHOLDER,
                SUMMARY_FAILURE_USAGE_PLACEHOLDER,
                Math.max(0, entryCount),
                false,
                true
            );
        } else {
            log.log(Level.WARNING,
                "Summary store unavailable while recording summary failure. day={0}, slot={1}, message={2}",
                new Object[]{String.valueOf(day), Integer.valueOf(slot), pausedMessage});
        }
        pauseAutomaticSummaryGeneration(pausedMessage);
        return false;
    }

    private void pauseAutomaticSummaryGeneration(String reason) {
        summaryAutoPausedUntilManualSuccess = true;
        summaryAutoPausedReason = defaultIfBlank(reason, "Automatic summary generation paused until manual retry succeeds.");
    }

    private void resumeAutomaticSummaryGeneration(String reason) {
        summaryAutoPausedUntilManualSuccess = false;
        summaryAutoPausedReason = "";
        if (reason != null && !reason.isBlank()) {
            log.log(Level.INFO, "Automatic summary generation resumed: {0}", reason);
        }
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

        throttleSyncRequestRate();

        String requestId = UUID.randomUUID().toString();
        ServerDiagnosticsLog.write(
                "widget-sync",
                requestId,
                "sync-request",
            "method=GET\nurl=" + uri + "\nwidgetId=" + defaultString(widgetId)
        );

        String apiKey = config.getApiKey();
        boolean apiKeyConfigured = apiKey != null && !apiKey.isBlank();
        boolean requireHttpsWithAuth = isHttpsRequiredWithAuth();
        if (apiKeyConfigured && !isHttpsUri(uri) && requireHttpsWithAuth) {
            throw new IOException("API key configured but sync URL is not HTTPS");
        }
        if (apiKeyConfigured && !isHttpsUri(uri) && !requireHttpsWithAuth) {
            log.info(() -> "Widget sync allowing HTTP URL with API key because "
                    + REQUIRE_HTTPS_WITH_AUTH_ENV + " is disabled. url=" + uri);
        }

        try {
            SyncHttpResult result = sendSyncRequest(uri, apiKey);
            HttpResponse<InputStream> response = result.response;
            try (InputStream bodyStream = response.body()) {
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                int statusCode = response.statusCode();

                ServerDiagnosticsLog.write(
                        "widget-sync",
                        requestId,
                        "sync-response",
                    "status=" + statusCode + "\ncontentType=" + contentType + "\nwidgetId=" + defaultString(widgetId)
                            + "\nauthSource=" + defaultString(result.authSource)
                            + "\nauthMode=" + (result.authMode == null ? "" : result.authMode.name())
                );

                if (statusCode >= 500) {
                    String upstreamBody = readUpstreamBodySnippet(bodyStream);
                    if (!upstreamBody.isBlank()) {
                    ServerDiagnosticsLog.write(
                        "widget-sync",
                        requestId,
                        "sync-response-body",
                        "status=" + statusCode + "\nwidgetId=" + defaultString(widgetId) + "\nbody=" + upstreamBody
                    );
                    }
                    throw new IOException("Sync API transient server error " + statusCode
                        + " (contentType=" + contentType + ", upstreamBody=" + upstreamBody + ')');
                }
                if (statusCode >= 300) {
                    String upstreamBody = readUpstreamBodySnippet(bodyStream);
                    if (!upstreamBody.isBlank()) {
                    ServerDiagnosticsLog.write(
                        "widget-sync",
                        requestId,
                        "sync-response-body",
                        "status=" + statusCode + "\nwidgetId=" + defaultString(widgetId) + "\nbody=" + upstreamBody
                    );
                    }
                    throw new IOException("Sync API returned " + statusCode
                        + " (contentType=" + contentType + ", upstreamBody=" + upstreamBody + ')');
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
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            ServerDiagnosticsLog.write(
                    "widget-sync",
                    requestId,
                    "sync-error",
                    "url=" + uri + "\nwidgetId=" + defaultString(widgetId) + "\nmessage=" + defaultString(e.getMessage()),
                    e
            );
            throw e;
        }
    }

    private SyncHttpResult sendSyncRequest(URI uri, String apiKey) throws IOException, InterruptedException {
        ApiAuthResolver.ResolvedApiAuth primaryAuth = ApiAuthResolver.resolveForOutbound(apiKey);
        ApiAuthResolver.ResolvedApiAuth secondaryAuth = ApiAuthResolver.resolveForOutbound(null);
        List<ApiAuthResolver.ResolvedApiAuth> candidates = buildAuthCandidates(primaryAuth, secondaryAuth);
        if (candidates.isEmpty()) {
            throw new IOException("Sync API key is required.");
        }

        SyncHttpResult lastResult = null;
        for (int authIndex = 0; authIndex < candidates.size(); authIndex++) {
            ApiAuthResolver.ResolvedApiAuth auth = candidates.get(authIndex);
            AuthHeaderMode mode = resolvePrimaryAuthMode(auth.preferredHeaderName());
            HttpRequest request = buildSyncRequest(uri, auth, mode);
            HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();

            boolean hasMoreAuth = authIndex < (candidates.size() - 1);
            if ((status == 401 || status == 403) && hasMoreAuth) {
                try (InputStream ignored = response.body()) {
                    // Drain/close before retrying fallback.
                }

                log.log(Level.INFO,
                    "Widget sync auth fallback switching source after status={0} source={1}",
                    new Object[]{status, defaultString(auth.source())});
                continue;
            }

            lastResult = new SyncHttpResult(response, auth.source(), mode);
            return lastResult;
        }

        if (lastResult == null) {
            throw new IOException("Sync request failed before receiving a response.");
        }
        return lastResult;
    }

    private List<ApiAuthResolver.ResolvedApiAuth> buildAuthCandidates(
            ApiAuthResolver.ResolvedApiAuth primary,
            ApiAuthResolver.ResolvedApiAuth secondary
    ) {
        Map<String, ApiAuthResolver.ResolvedApiAuth> unique = new LinkedHashMap<>();
        for (ApiAuthResolver.ResolvedApiAuth candidate : Arrays.asList(primary, secondary)) {
            if (candidate == null || !candidate.hasToken()) {
                continue;
            }
            String token = ApiAuthResolver.normalizeApiKeyToken(candidate.rawValue());
            if (token == null || token.isBlank()) {
                continue;
            }
            String key = token + "|" + defaultString(candidate.preferredHeaderName());
            unique.putIfAbsent(key, candidate);
        }
        return new ArrayList<>(unique.values());
    }

    private void throttleSyncRequestRate() throws InterruptedException {
        if (SYNC_MIN_REQUEST_GAP_MS <= 0L) {
            return;
        }

        long waitMs = 0L;
        synchronized (syncRateLimitLock) {
            long now = System.currentTimeMillis();
            long nextAllowed = lastSyncRequestAtMs + SYNC_MIN_REQUEST_GAP_MS;
            if (now < nextAllowed) {
                waitMs = nextAllowed - now;
                lastSyncRequestAtMs = nextAllowed;
            } else {
                lastSyncRequestAtMs = now;
            }
        }

        if (waitMs > 0L) {
            Thread.sleep(waitMs);
        }
    }

    private AuthHeaderMode resolvePrimaryAuthMode(String preferredHeaderName) {
        String header = preferredHeaderName == null ? "" : preferredHeaderName.trim();
        if ("x-api-key".equalsIgnoreCase(header)) {
            return AuthHeaderMode.X_API_KEY;
        }
        if (!header.isBlank() && !"authorization".equalsIgnoreCase(header)) {
            return AuthHeaderMode.CUSTOM_HEADER;
        }
        return AuthHeaderMode.AUTH_BEARER;
    }

    private HttpRequest buildSyncRequest(URI uri, ApiAuthResolver.ResolvedApiAuth auth, AuthHeaderMode mode) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(HTTP_REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET();

        String token = auth == null ? null : auth.token();
        String rawValue = auth == null ? null : auth.rawValue();
        String preferredHeader = auth == null ? null : auth.preferredHeaderName();

        if (token != null && !token.isBlank()) {
            switch (mode) {
                case CUSTOM_HEADER -> applyPreferredHeader(builder, preferredHeader, rawValue, token);
                case AUTH_RAW -> builder.header("Authorization", normalizeRawAuthorizationValue(rawValue, token));
                case X_API_KEY -> builder.header("X-API-Key", token);
                case AUTH_BEARER_AND_X_API_KEY -> builder
                        .header("Authorization", "Bearer " + token)
                        .header("X-API-Key", token);
                case AUTH_BEARER -> builder.header("Authorization", "Bearer " + token);
            }
        }

        return builder.build();
    }

    private void applyPreferredHeader(HttpRequest.Builder builder, String headerName, String rawValue, String token) {
        String normalizedHeader = headerName == null ? "" : headerName.trim();
        if (normalizedHeader.isBlank()) {
            return;
        }

        if ("authorization".equalsIgnoreCase(normalizedHeader)) {
            builder.header("Authorization", "Bearer " + token);
            return;
        }
        if ("x-api-key".equalsIgnoreCase(normalizedHeader)) {
            builder.header("X-API-Key", token);
            return;
        }

        String headerValue = rawValue;
        if (headerValue == null || headerValue.isBlank()) {
            headerValue = token;
        }
        builder.header(normalizedHeader, headerValue);
    }

    private String normalizeRawAuthorizationValue(String rawValue, String token) {
        String raw = ApiAuthResolver.stripAuthorizationPrefix(rawValue);
        if (raw == null || raw.isBlank()) {
            return token;
        }
        return raw;
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
        String baseUrl = sanitizeBaseUrl(buildBaseUrl(config));
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Server host configuration is missing");
        }

        try {
            URI base = URI.create(baseUrl);
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

    private String readUpstreamBodySnippet(InputStream bodyStream) {
        if (bodyStream == null) {
            return "";
        }
        try {
            byte[] bytes = bodyStream.readNBytes(2048);
            if (bytes.length == 0) {
                return "";
            }
            String text = new String(bytes, StandardCharsets.UTF_8);
            String sanitized = stripControlCharacters(text);
            if (sanitized.length() > 512) {
                sanitized = sanitized.substring(0, 512) + "...";
            }
            return sanitized;
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to read upstream sync error response body", e);
            return "";
        }
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

    private static int parseBoundedIntEnv(String envName, int fallback, int min, int max) {
        String raw = System.getenv(envName);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < min) {
                return min;
            }
            if (parsed > max) {
                return max;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static long parseBoundedLongEnv(String envName, long fallback, long min, long max) {
        String raw = System.getenv(envName);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            long parsed = Long.parseLong(raw.trim());
            if (parsed < min) {
                return min;
            }
            if (parsed > max) {
                return max;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String buildBaseUrl(ServerConfig config) {
        String host = config.getServerHost();
        if (host == null || host.isBlank()) {
            String connectionInfo = config.getConnectionInfo();
            if (connectionInfo != null && !connectionInfo.isBlank()) {
                return stripTrailingSlash(connectionInfo.trim());
            }
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

    private boolean isHttpsRequiredWithAuth() {
        String prop = System.getProperty(REQUIRE_HTTPS_WITH_AUTH_PROP);
        if (prop != null && !prop.isBlank()) {
            return Boolean.parseBoolean(prop.trim());
        }

        String env = System.getenv(REQUIRE_HTTPS_WITH_AUTH_ENV);
        if (env == null || env.isBlank()) {
            return false;
        }
        return Boolean.parseBoolean(env.trim());
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
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    void setDataSourceHolder(AppDataSourceHolder dsHolder) {
        this.dsHolder = dsHolder;
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
