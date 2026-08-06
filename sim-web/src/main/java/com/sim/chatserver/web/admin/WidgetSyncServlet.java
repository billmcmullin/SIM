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
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
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
    private static final long DEFAULT_SUMMARY_INTERVAL_SECONDS = 3600L;
    private static final boolean DEFAULT_SUMMARY_AUTO_ENABLED = true;
    private static final long MIN_SUMMARY_INTERVAL_SECONDS = 300L;
    private static final long MAX_SUMMARY_INTERVAL_SECONDS = TimeUnit.DAYS.toSeconds(30);
    private static final int DEFAULT_SUMMARY_MAX_ROWS = 1200;
    private static final int MIN_SUMMARY_MAX_ROWS = 50;
    private static final int MAX_SUMMARY_MAX_ROWS = 5000;
    private static final int MAX_SUMMARY_PROMPT_CHARS = 12000;
    private static final int SUMMARY_DIRECT_MAX_MESSAGE_CHARS = 42000;
    private static final int SUMMARY_DIRECT_PROMPT_CHARS = 1200;
    private static final int SUMMARY_DIRECT_RESPONSE_CHARS = 1800;
    private static final int SUMMARY_DIRECT_COMPACT_MAX_MESSAGE_CHARS = 2200;
    private static final int SUMMARY_DIRECT_COMPACT_PROMPT_CHARS = 320;
    private static final int SUMMARY_DIRECT_COMPACT_RESPONSE_CHARS = 700;
    private static final int SUMMARY_DIRECT_TINY_MAX_MESSAGE_CHARS = 900;
    private static final int SUMMARY_DIRECT_TINY_PROMPT_CHARS = 120;
    private static final int SUMMARY_DIRECT_TINY_RESPONSE_CHARS = 180;
    private static final int DEFAULT_SUMMARY_MAX_UPSTREAM_ENTRIES = 40;
    private static final int MIN_SUMMARY_MAX_UPSTREAM_ENTRIES = 5;
    private static final int MAX_SUMMARY_MAX_UPSTREAM_ENTRIES = 200;
    private static final int DEFAULT_SUMMARY_MAX_MESSAGE_CHARS = 2200;
    private static final int MIN_SUMMARY_MAX_MESSAGE_CHARS = 600;
    private static final int MAX_SUMMARY_MAX_MESSAGE_CHARS = 12000;
    private static final int DEFAULT_SUMMARY_MAX_REQUEST_BYTES = 4096;
    private static final int MIN_SUMMARY_MAX_REQUEST_BYTES = 1024;
    private static final int MAX_SUMMARY_MAX_REQUEST_BYTES = 65536;
    private static final int SUMMARY_GENTLE_PROMPT_CHARS = 160;
    private static final int SUMMARY_GENTLE_RESPONSE_CHARS = 220;
    private static final int SUMMARY_GENTLE_GUIDANCE_CHARS = 520;
    private static final int SUMMARY_INCREMENTAL_BATCH_MAX_CHARS = 850;
    private static final int SUMMARY_INCREMENTAL_PROMPT_CHARS = 180;
    private static final int SUMMARY_INCREMENTAL_RESPONSE_CHARS = 240;
    private static final int SUMMARY_INCREMENTAL_MAX_BATCHES = 40;
    private static final int SUMMARY_MAX_TERMS_FOR_PROMPT = 80;
    private static final int SUMMARY_DIAGNOSTIC_BODY_CHARS = 1800;
    private static final int LAST_SYNC_FLUSH_EVERY_N_RUNS = 5;
    private static final int DEFAULT_SYNC_PARALLELISM = 4;
    private static final int MAX_SYNC_PARALLELISM = 16;
        private static final int DEFAULT_SYNC_RECENT_CHAT_ID_CACHE_SIZE = 10000;
        private static final int MAX_SYNC_RECENT_CHAT_ID_CACHE_SIZE = 200000;
    private static final int SYNC_PARALLELISM = parseBoundedIntEnv(
            "SIM_WIDGET_SYNC_PARALLELISM",
            DEFAULT_SYNC_PARALLELISM,
            1,
            MAX_SYNC_PARALLELISM
    );
        private static final int SYNC_RECENT_CHAT_ID_CACHE_SIZE = parseBoundedIntEnv(
            "SIM_WIDGET_SYNC_RECENT_CHAT_ID_CACHE_SIZE",
            DEFAULT_SYNC_RECENT_CHAT_ID_CACHE_SIZE,
            0,
            MAX_SYNC_RECENT_CHAT_ID_CACHE_SIZE
        );
    private static final long DEFAULT_SYNC_MIN_REQUEST_GAP_MS = 200L;
    private static final long SYNC_MIN_REQUEST_GAP_MS = parseBoundedLongEnv(
            "SIM_WIDGET_SYNC_MIN_REQUEST_GAP_MS",
            DEFAULT_SYNC_MIN_REQUEST_GAP_MS,
            0L,
            10_000L
    );
    private static final Pattern NON_ALNUM_UNDERSCORE = Pattern.compile("[^A-Za-z0-9_]");
    private static final Pattern SUMMARY_INCLUDED_COUNT_PATTERN = Pattern.compile("coverage:\\s*included=(\\d+)");

    private static final int HTTP_MAX_ATTEMPTS = 3;
    private static final long HTTP_RETRY_BASE_MS = 500L;
    private static final long HTTP_RETRY_MAX_MS = 5000L;
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final String SYNC_DISABLED_PROPERTY = "sim.widget.sync.disabled";
    private static final String REQUIRE_HTTPS_WITH_AUTH_ENV = "WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH";
    private static final String REQUIRE_HTTPS_WITH_AUTH_PROP = "sim.widget.sync.require.https.with.auth";
    private static final String REQUIRE_UPSTREAM_SUMMARY_ENV = "WIDGET_SUMMARY_REQUIRE_UPSTREAM";
    private static final String REQUIRE_UPSTREAM_SUMMARY_PROP = "sim.widget.summary.require.upstream";
    private static final String SUMMARY_RETRY_PATTERN = "/admin/widgets/summary/retry";
    private static final String SUMMARY_FAILURE_QUALITY_PLACEHOLDER = "Summary unavailable due to generation failure.";
    private static final String SUMMARY_FAILURE_RESPONSE_PLACEHOLDER = "No response analysis available until an admin runs manual summary generation.";
    private static final String SUMMARY_FAILURE_USAGE_PLACEHOLDER = "Usage analysis is temporarily unavailable until summary generation resumes.";
    private static final String LEGACY_DEFAULT_SUMMARY_PROMPT = """
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
    private static final String PREVIOUS_DEFAULT_SUMMARY_PROMPT = """
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
            - In Overall, include: chats used in summary and term coverage metrics.
            - In Quality, include: frustration detected (yes/no), frustration level (none/low/medium/high), and top frustration points.
            - In Response, include: answer quality assessment and whether users likely accepted answers.
            - In Usage, include: actionable feedback and article topics that would improve answer quality and user experience.
            """;
        private static final String PREVIOUS_DEFAULT_SUMMARY_PROMPT_V2 = """
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
                        - In Overall, include: chats used in summary and term coverage metrics.
                        - In Quality, include: frustration detected (yes/no), frustration level (none/low/medium/high), and top frustration points.
                        - In Response, include: answer quality assessment and whether users likely accepted answers.
                        - In Usage, include two short subsections:
                            1) "Actionable improvements" with concrete content/article updates.
                            2) "Next steps" with 3-5 specific asks for users to provide next time.
                        - In "Next steps", each ask must be concrete, for example:
                            product/version, environment details, exact error text or screenshot, expected result,
                            steps already tried, and relevant logs or configuration snippets.
                        """;
        private static final String DEFAULT_SUMMARY_PROMPT = """
                        You are producing an in-depth daily analysis of today's widget chats.
                        Return markdown with these exact sections:
                        ## Overall
                        ## Quality
                        ## Response
                        ## Usage

                        Constraints:
                        - Use only observable evidence from the provided chats for today.
                        - Do not invent facts, outcomes, or metrics.
                        - In each section, explain what is working, what is not working, and why.
                        - Include quantitative signals when possible (counts, ratios, repeated issue frequency).
                        - If data is missing, explicitly say: "Not observable from supplied chats."
                        - Be detailed enough for decision-making, but avoid filler text.
                        - In Overall, include: chats analyzed, coverage context, and an overall effectiveness score (0-100) with brief rationale.
                        - Include section scores for Quality, Response, and Usage as 0.0-5.0 values.
                        - In Quality, include: frustration detected (yes/no), frustration level (none/low/medium/high), top frustration drivers, and correctness/completeness risk patterns.
                        - In Response, include: how well answers solved user intent, where answers were unclear/incomplete, and whether users appeared satisfied.
                        - In Usage, include two subsections:
                            1) "Actionable improvements": top 3-5 prioritized improvements with expected impact.
                            2) "Next steps": specific user inputs to request next time so answers can be more accurate.
                        - In "Next steps", ask concretely for: product/version, environment details, exact error text or screenshot, expected result, steps already tried, and relevant logs/config snippets.
                        """;

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

    private static final class SummaryPayloadPlan {
        final String message;
        final int includedEntries;
        final int requestBytes;
        final boolean budgetReduced;

        private SummaryPayloadPlan(String message, int includedEntries, int requestBytes, boolean budgetReduced) {
            this.message = message == null ? "" : message;
            this.includedEntries = Math.max(0, includedEntries);
            this.requestBytes = Math.max(0, requestBytes);
            this.budgetReduced = budgetReduced;
        }
    }

    private static final class RecentChatIdCache {
        private final int maxSize;
        private final Deque<String> order = new ArrayDeque<>();
        private final Set<String> ids = new HashSet<>();

        private RecentChatIdCache(int maxSize) {
            this.maxSize = Math.max(0, maxSize);
        }

        private synchronized List<String> missingFromCache(List<String> chatIds) {
            List<String> missing = new ArrayList<>();
            if (chatIds == null || chatIds.isEmpty()) {
                return missing;
            }
            for (String chatId : chatIds) {
                if (chatId == null || chatId.isBlank()) {
                    continue;
                }
                if (!ids.contains(chatId)) {
                    missing.add(chatId);
                }
            }
            return missing;
        }

        private synchronized void recordAll(List<String> chatIds) {
            if (maxSize <= 0 || chatIds == null || chatIds.isEmpty()) {
                return;
            }
            for (String chatId : chatIds) {
                if (chatId == null || chatId.isBlank()) {
                    continue;
                }
                if (ids.add(chatId)) {
                    order.addLast(chatId);
                }
            }

            while (ids.size() > maxSize) {
                String evicted = order.pollFirst();
                if (evicted == null) {
                    break;
                }
                ids.remove(evicted);
            }
        }
    }

    private static volatile DashboardDailySummaryStore summaryStore;

    private static volatile ScheduledExecutorService scheduler = createScheduler();
    private static volatile ExecutorService syncPool = createSyncPool();

    private static final Object syncRateLimitLock = new Object();
    private static volatile long lastSyncRequestAtMs;

    private static final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();
    private static final Map<String, RecentChatIdCache> recentChatIdsByWidget = new ConcurrentHashMap<>();

    private static volatile ScheduledFuture<?> scheduledFuture;
    private static volatile long syncIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private static volatile long summaryIntervalSeconds = DEFAULT_SUMMARY_INTERVAL_SECONDS;
    private static volatile boolean summaryAutoEnabled = DEFAULT_SUMMARY_AUTO_ENABLED;
    private static volatile int summaryMaxRows = DEFAULT_SUMMARY_MAX_ROWS;
    private static volatile int summaryMaxUpstreamEntries = DEFAULT_SUMMARY_MAX_UPSTREAM_ENTRIES;
    private static volatile int summaryMaxMessageChars = DEFAULT_SUMMARY_MAX_MESSAGE_CHARS;
    private static volatile int summaryMaxRequestBytes = DEFAULT_SUMMARY_MAX_REQUEST_BYTES;
    private static volatile String summaryPromptTemplate = DEFAULT_SUMMARY_PROMPT;
    private static volatile Timestamp lastSynced;
    private static volatile Timestamp summaryLastRun;
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
    private static volatile String syncCurrentWidgetId = "";
    private static volatile String syncCurrentWidgetTable = "";
    private static volatile int syncCurrentWidgetIndex;
    private static volatile int syncProgressPercent;
    private static volatile boolean summaryAutoPausedUntilManualSuccess;
    private static volatile String summaryAutoPausedReason = "";

    private static volatile MapReduceConfig mrConfig;
    private static volatile WorkspaceClient workspaceClient;
    private static volatile WidgetReviewMapReduceOrchestrator orchestrator;
    private static volatile TrustedUrlValidator trustedUrlValidator;
    private static volatile ReviewOutputValidator reviewOutputValidator;
    private static volatile AppDataSourceHolder dsHolder;
    private TermsStore termsStore;

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
        String prop = readSystemPropertySanitized(SYNC_DISABLED_PROPERTY, 16);
        if (prop == null || prop.isBlank()) {
            return false;
        }
        return Boolean.parseBoolean(prop);
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

            Set<String> allowedHosts = parseCsvToSet(readEnvSanitized("REVIEW_TRUSTED_HOSTS", 2048));
            Set<String> allowedSuffixes = parseCsvToSet(readEnvSanitized("REVIEW_TRUSTED_HOST_SUFFIXES", 2048));
            boolean allowPrivate = Boolean.parseBoolean(defaultIfBlank(readEnvSanitized("REVIEW_ALLOW_PRIVATE_NETWORKS", 16), "false"));
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
        } catch (IllegalStateException e) {
            logWarningWithDiagnostics(
                    "init-settings-store-failed",
                    "Unable to initialize sync settings/store",
                    "phase=servlet-init",
                    e
            );
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (isTimerRequest(req)) {
            handleTimerStatus(resp);
        } else {
            sendErrorSafe(resp, HttpServletResponse.SC_NOT_FOUND, null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
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
                        defaultIfBlank(summaryAutoPausedReason, "manual summary generation required"));
            } else if (!summaryAutoEnabled) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because automatic summary generation is disabled.");
            } else if (!isSummaryRunDueNow()) {
                log.log(Level.INFO,
                        "Skipping summary generation during manual sync because summary interval has not elapsed. nextRunAt={0}",
                        computeNextSummaryRunAtIso());
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
                completionMessage = summaryAutoPausedUntilManualSuccess
                        ? "Sync completed. Summary generation is paused until an admin generates a summary."
                        : (!summaryAutoEnabled
                        ? "Sync completed. Automatic summary generation is disabled."
                        : "Sync completed. Summary generation skipped until the configured interval elapses.");
            } else if (!summarySuccess) {
                completionMessage = "Sync completed. Summary generation failed and automatic summaries are now paused.";
            } else {
                completionMessage = "Sync completed successfully.";
            }
            finishSyncProgress(true, completionMessage);

            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (RuntimeException e) {
            if (causedByInterrupted(e)) {
                Thread.currentThread().interrupt();
            }
            logWarningWithDiagnostics(
                    "manual-sync-failed",
                    "Widget sync failed",
                    "widgetId=" + defaultString(firstParam(req, "widgetId")),
                    e
            );
            finishSyncProgress(false, "Sync failed. Check server logs.");
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Widget sync failed. Check server logs.");
        } finally {
            syncRunning.set(false);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int statusCode, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            if (message == null || message.isBlank()) {
                resp.sendError(statusCode);
            } else {
                resp.sendError(statusCode, message);
            }
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending error response", ioe);
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

    private void handleManualSummaryRetry(HttpServletRequest req, HttpServletResponse resp) {
        if (!authorizeAdmin(req, resp)) {
            return;
        }

        if (!summaryAutoEnabled) {
            jsonError(resp, HttpServletResponse.SC_CONFLICT,
                    "Automatic summary generation is disabled. Enable it in Summary Configuration before generating summaries.");
            return;
        }

        if (!syncRunning.compareAndSet(false, true)) {
            jsonError(resp, HttpServletResponse.SC_CONFLICT, "Sync or summary generation is already in progress.");
            return;
        }

        beginSyncProgress("manual_summary_retry", "Manual summary generation started.");
        try {
            updateSyncProgress("summary_generation", "Generating daily summary...", 40);
            boolean success = runDailySummaryGeneration(true);
            String nextRunAt = computeNextSummaryRunAtIso();
            String message = success
                ? "Manual summary generated successfully. The automatic summary timer was reset from this run. Next scheduled summary: "
                + defaultIfBlank(nextRunAt, "based on configured interval.")
                : defaultIfBlank(summaryAutoPausedReason, "Manual summary generation failed. Automatic summary remains paused.");

            finishSyncProgress(success, message);

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", success ? "ok" : "error")
                    .add("message", message)
                    .add("summaryAutoPaused", summaryAutoPausedUntilManualSuccess)
                .add("summaryLastRunAt", summaryLastRun == null ? "" : summaryLastRun.toInstant().toString())
                .add("summaryNextRunAt", computeNextSummaryRunAtIso())
                    .build();
            writeJson(resp, success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_GATEWAY, payload);
        } finally {
            syncRunning.set(false);
        }
    }

    private void handleTimerStatus(HttpServletResponse resp) {
        boolean running = syncRunning.get();
        String startedAt = syncStartedAt == null ? "" : syncStartedAt.toString();
        String finishedAt = syncFinishedAt == null ? "" : syncFinishedAt.toString();
        String summaryLastRunAt = summaryLastRun == null ? "" : summaryLastRun.toInstant().toString();
        String summaryNextRunAt = computeNextSummaryRunAtIso();
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
            .add("currentWidgetId", defaultIfBlank(syncCurrentWidgetId, ""))
            .add("currentWidgetTable", defaultIfBlank(syncCurrentWidgetTable, ""))
            .add("currentWidgetIndex", Math.max(0, syncCurrentWidgetIndex))
            .add("progressPercent", percent)
            .add("runningSeconds", runningSeconds)
                .add("summaryIntervalSeconds", summaryIntervalSeconds)
                .add("summaryAutoEnabled", summaryAutoEnabled)
                .add("summaryMaxRows", summaryMaxRows)
                .add("summaryMaxUpstreamEntries", summaryMaxUpstreamEntries)
                .add("summaryMaxMessageChars", summaryMaxMessageChars)
                .add("summaryMaxRequestBytes", summaryMaxRequestBytes)
                .add("summaryPrompt", resolveSummaryPrompt())
                .add("summaryAutoPaused", summaryAutoPausedUntilManualSuccess)
                .add("summaryAutoPausedReason", defaultIfBlank(summaryAutoPausedReason, ""))
                .add("summaryLastRunAt", summaryLastRunAt)
                .add("summaryNextRunAt", summaryNextRunAt)
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private void handleTimerUpdate(HttpServletRequest req, HttpServletResponse resp) {
        if (!authorizeAdmin(req, resp)) {
            return;
        }

        String syncIntervalRaw = firstParam(req, "intervalSeconds");
        String summaryIntervalRaw = firstParam(req, "summaryIntervalSeconds");
        String summaryAutoEnabledRaw = firstParam(req, "summaryAutoEnabled");
        String summaryMaxRowsRaw = firstParam(req, "summaryMaxRows");
        String summaryMaxUpstreamEntriesRaw = firstParam(req, "summaryMaxUpstreamEntries");
        String summaryMaxMessageCharsRaw = firstParam(req, "summaryMaxMessageChars");
        String summaryMaxRequestBytesRaw = firstParam(req, "summaryMaxRequestBytes");
        String summaryPromptRaw = readMultilineParam(req, "summaryPrompt", MAX_SUMMARY_PROMPT_CHARS);

        boolean changed = false;
        try {
            if (syncIntervalRaw != null && !syncIntervalRaw.isBlank()) {
                long intervalSeconds = Long.parseLong(syncIntervalRaw.trim());
                if (intervalSeconds < MIN_INTERVAL_SECONDS) {
                    intervalSeconds = MIN_INTERVAL_SECONDS;
                }
                updateInterval(intervalSeconds);
                changed = true;
            }

            if (summaryIntervalRaw != null && !summaryIntervalRaw.isBlank()) {
                long parsedSummaryInterval = Long.parseLong(summaryIntervalRaw.trim());
                summaryIntervalSeconds = clampSummaryIntervalSeconds(parsedSummaryInterval);
                changed = true;
            }

            if (summaryAutoEnabledRaw != null && !summaryAutoEnabledRaw.isBlank()) {
                summaryAutoEnabled = Boolean.parseBoolean(summaryAutoEnabledRaw.trim());
                changed = true;
            }

            if (summaryMaxRowsRaw != null && !summaryMaxRowsRaw.isBlank()) {
                int parsedSummaryMaxRows = Integer.parseInt(summaryMaxRowsRaw.trim());
                summaryMaxRows = clampSummaryMaxRows(parsedSummaryMaxRows);
                changed = true;
            }

            if (summaryMaxUpstreamEntriesRaw != null && !summaryMaxUpstreamEntriesRaw.isBlank()) {
                int parsedSummaryMaxUpstreamEntries = Integer.parseInt(summaryMaxUpstreamEntriesRaw.trim());
                summaryMaxUpstreamEntries = clampSummaryMaxUpstreamEntries(parsedSummaryMaxUpstreamEntries);
                changed = true;
            }

            if (summaryMaxMessageCharsRaw != null && !summaryMaxMessageCharsRaw.isBlank()) {
                int parsedSummaryMaxMessageChars = Integer.parseInt(summaryMaxMessageCharsRaw.trim());
                summaryMaxMessageChars = clampSummaryMaxMessageChars(parsedSummaryMaxMessageChars);
                changed = true;
            }

            if (summaryMaxRequestBytesRaw != null && !summaryMaxRequestBytesRaw.isBlank()) {
                int parsedSummaryMaxRequestBytes = Integer.parseInt(summaryMaxRequestBytesRaw.trim());
                summaryMaxRequestBytes = clampSummaryMaxRequestBytes(parsedSummaryMaxRequestBytes);
                changed = true;
            }
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid sync/summary timer configuration parameter", e);
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid interval or summary configuration value.");
            return;
        }

        if (summaryPromptRaw != null) {
            summaryPromptTemplate = normalizeSummaryPrompt(summaryPromptRaw);
            changed = true;
        }

        if (!changed) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "No timer or summary settings were provided.");
            return;
        }

        persistSyncSettings();

        JsonObject payload = Json.createObjectBuilder()
                .add("status", "ok")
                .add("intervalSeconds", syncIntervalSeconds)
                .add("lastSynced", lastSynced == null ? "" : lastSynced.toInstant().toString())
                .add("summaryIntervalSeconds", summaryIntervalSeconds)
                .add("summaryAutoEnabled", summaryAutoEnabled)
                .add("summaryMaxRows", summaryMaxRows)
                .add("summaryMaxUpstreamEntries", summaryMaxUpstreamEntries)
                .add("summaryMaxMessageChars", summaryMaxMessageChars)
                .add("summaryMaxRequestBytes", summaryMaxRequestBytes)
                .add("summaryPrompt", resolveSummaryPrompt())
                .add("summaryAutoPaused", summaryAutoPausedUntilManualSuccess)
                .add("summaryAutoPausedReason", defaultIfBlank(summaryAutoPausedReason, ""))
                .add("summaryLastRunAt", summaryLastRun == null ? "" : summaryLastRun.toInstant().toString())
                .add("summaryNextRunAt", computeNextSummaryRunAtIso())
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
                        defaultIfBlank(summaryAutoPausedReason, "manual summary generation required"));
            } else if (!summaryAutoEnabled) {
                log.log(Level.INFO, "Skipping scheduled summary generation because automatic summary is disabled.");
            } else if (!isSummaryRunDueNow()) {
                log.log(Level.INFO,
                        "Skipping scheduled summary generation because summary interval has not elapsed. nextRunAt={0}",
                        computeNextSummaryRunAtIso());
            } else {
                summaryRan = true;
                updateSyncProgress("summary_generation", "Generating daily summary...", 92);
                summarySuccess = runDailySummaryGeneration(false);
            }

            if (!summaryRan) {
                finishSyncProgress(true,
                        summaryAutoPausedUntilManualSuccess
                                ? "Scheduled sync completed. Summary generation remains paused."
                        : (!summaryAutoEnabled
                        ? "Scheduled sync completed. Automatic summary generation is disabled."
                        : "Scheduled sync completed. Summary generation skipped until the configured interval elapses."));
            } else if (!summarySuccess) {
                finishSyncProgress(true, "Scheduled sync completed. Summary generation failed and was paused.");
            } else {
                finishSyncProgress(true, "Scheduled sync completed successfully.");
            }

            log.log(Level.INFO, () -> "Automatic widget sync completed. Synced " + statuses.size() + " widget entries.");
        } catch (RuntimeException e) {
            if (causedByInterrupted(e)) {
                Thread.currentThread().interrupt();
            }
            logWarningWithDiagnostics(
                    "scheduled-sync-failed",
                    "Automatic widget sync failed",
                    "intervalSeconds=" + syncIntervalSeconds,
                    e
            );
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

    private String logWarningWithDiagnostics(String event, String summary, String details, Throwable failure) {
        String errorRef = UUID.randomUUID().toString();
        log.log(Level.WARNING,
                "{0}. errorRef={1}. See server diagnostics log for stack trace.",
                new Object[]{defaultIfBlank(summary, "Operation failed"), errorRef});

        ServerDiagnosticsLog.write(
                "widget-sync",
                errorRef,
                defaultIfBlank(event, "error"),
                "summary=" + defaultString(summary)
                        + (details == null || details.isBlank() ? "" : "\n" + details),
                failure
        );
        return errorRef;
    }

    private boolean causedByInterrupted(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InterruptedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private List<WidgetSyncStatus> runSync(String requestedWidgetId) {
        ServerConfig config;
        try {
            config = EncryptedDbConfigStore.load();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load server configuration for widget sync.", e);
        }
        if (config == null) {
            throw new IllegalStateException("Server configuration is missing.");
        }

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load widget entries for sync.", e);
        }
        if (requestedWidgetId != null && !requestedWidgetId.isBlank()) {
            widgets.removeIf(w -> w == null || !requestedWidgetId.equals(w.getWidgetId()));
        }

        List<WidgetEntry> valid = widgets.stream()
                .filter(w -> w != null && w.getWidgetId() != null && !w.getWidgetId().isBlank())
            .sorted((left, right) -> left.getWidgetId().compareToIgnoreCase(right.getWidgetId()))
                .toList();

        startWidgetSyncProgress(valid.size());

        List<WidgetSyncStatus> statuses = new ArrayList<>(valid.size());
        int totalWidgets = valid.size();

        for (int i = 0; i < totalWidgets; i++) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Widget sync interrupted before processing next widget.");
            }

            WidgetEntry widget = valid.get(i);
            String widgetId = widget == null ? "" : defaultString(widget.getWidgetId());
            updateCurrentWidgetProgress(widgetId, sanitizeWidgetTableName(widgetId), i + 1, totalWidgets);

            WidgetSyncStatus status = syncSingleWidget(config, widgetId);
            statuses.add(status);
        }

        syncCurrentWidgetId = "";
        syncCurrentWidgetTable = "";
        syncCurrentWidgetIndex = 0;
        return statuses;
    }

    private WidgetSyncStatus syncSingleWidget(ServerConfig config, String widgetId) {
        String tableName = sanitizeWidgetTableName(widgetId);
        boolean success = false;

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureTable(conn, tableName);

            updateSyncProgress(
                "syncing_widgets",
                currentWidgetStepMessage(widgetId, "Calling API for chat messages..."),
                syncProgressPercent
            );

            List<JsonObject> chats = fetchWidgetChatsWithRetry(config, widgetId);
            List<String> candidateChatIds = collectUniqueChatIds(chats);
            RecentChatIdCache recentCache = recentChatIdCacheForWidget(tableName);

            boolean skippedByRecentCache = false;
            List<JsonObject> chatsForUpsert = chats;
            if (recentCache != null && !candidateChatIds.isEmpty()) {
                List<String> missingChatIds = recentCache.missingFromCache(candidateChatIds);
                if (missingChatIds.isEmpty()) {
                    skippedByRecentCache = true;
                    updateSyncProgress(
                        "syncing_widgets",
                        currentWidgetStepMessage(widgetId, "Payload unchanged from recent syncs. Skipping DB upsert."),
                        syncProgressPercent
                    );
                } else if (missingChatIds.size() < candidateChatIds.size()) {
                    Set<String> missingIdSet = new LinkedHashSet<>(missingChatIds);
                    chatsForUpsert = filterChatsByIds(chats, missingIdSet);
                    int filteredCount = Math.max(0, candidateChatIds.size() - missingChatIds.size());
                    updateSyncProgress(
                        "syncing_widgets",
                        currentWidgetStepMessage(widgetId, "Filtered " + filteredCount + " known chat(s) before DB upsert."),
                        syncProgressPercent
                    );
                }
            }

            updateSyncProgress(
                "syncing_widgets",
                currentWidgetStepMessage(widgetId, "API returned " + chats.size() + " chat(s). Checking for new entries..."),
                syncProgressPercent
            );

            int inserted = 0;
            if (!skippedByRecentCache && !chatsForUpsert.isEmpty()) {
            updateSyncProgress(
                "syncing_widgets",
                currentWidgetStepMessage(widgetId, "Upserting chat(s) into database..."),
                syncProgressPercent
            );
                inserted = insertWidgetChats(conn, tableName, chatsForUpsert);
            updateSyncProgress(
                "syncing_widgets",
                currentWidgetStepMessage(widgetId, "Added " + inserted + " new chat(s) to database."),
                syncProgressPercent
            );
            } else {
            updateSyncProgress(
                "syncing_widgets",
                currentWidgetStepMessage(widgetId, "No new chats detected. Database unchanged."),
                syncProgressPercent
            );
            }

                if (recentCache != null && !candidateChatIds.isEmpty()) {
                recentCache.recordAll(candidateChatIds);
                }

            String message = chats.isEmpty()
                    ? "No chat rows returned from server."
                    : (skippedByRecentCache
                    ? "Fetched " + chats.size() + " chat(s), payload unchanged from recent sync cache. Table unchanged."
                : (inserted <= 0
                    ? "Fetched " + chats.size() + " chat(s), no new chat entries detected. Table unchanged."
                    : "Fetched " + chats.size() + " chat(s), inserted " + inserted + " new chat(s)."));

            success = true;
            return new WidgetSyncStatus(widgetId, tableName, true, true, message);
        } catch (SQLException | RuntimeException e) {
            if (causedByInterrupted(e)) {
                Thread.currentThread().interrupt();
            }
            logWarningWithDiagnostics(
                    "sync-single-widget-failed",
                    "Failed to sync widget " + String.valueOf(widgetId),
                    "widgetId=" + defaultString(widgetId) + "\ntableName=" + defaultString(tableName),
                    e
            );
            return new WidgetSyncStatus(widgetId, tableName, false, false, "Sync failed. Check server logs.");
        } finally {
            markWidgetSyncCompletion(widgetId, success);
        }
    }

    private String currentWidgetStepMessage(String widgetId, String action) {
        int index = Math.max(1, syncCurrentWidgetIndex);
        int total = Math.max(index, syncTotalWidgets);
        String safeWidget = defaultIfBlank(widgetId, "unknown");
        return "Widget " + index + "/" + total + " (" + safeWidget + "): " + defaultIfBlank(action, "processing...");
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
            syncCurrentWidgetId = "";
            syncCurrentWidgetTable = "";
            syncCurrentWidgetIndex = 0;
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
            syncCurrentWidgetId = "";
            syncCurrentWidgetTable = "";
            syncCurrentWidgetIndex = 0;

            if (syncTotalWidgets == 0) {
                syncProgressPercent = Math.max(syncProgressPercent, 90);
                syncStatusMessage = "No widgets available to sync.";
                return;
            }

            syncProgressPercent = Math.max(syncProgressPercent, 5);
            syncStatusMessage = "Syncing widget tables...";
        }
    }

    private void updateCurrentWidgetProgress(String widgetId, String tableName, int widgetIndex, int totalWidgets) {
        synchronized (syncProgressLock) {
            syncCurrentWidgetId = defaultIfBlank(widgetId, "");
            syncCurrentWidgetTable = defaultIfBlank(tableName, "");
            syncCurrentWidgetIndex = Math.max(0, widgetIndex);

            int safeTotal = Math.max(0, totalWidgets);
            if (safeTotal > 0) {
                int scaledPercent = 5 + (int) Math.round(((Math.max(0, widgetIndex - 1) * 100.0d) / safeTotal) * 0.85d);
                syncProgressPercent = Math.max(syncProgressPercent, Math.min(90, scaledPercent));
            }

            if (!syncCurrentWidgetId.isBlank()) {
                syncStatusMessage = "Syncing widget "
                        + Math.max(1, widgetIndex)
                        + "/"
                        + Math.max(1, safeTotal)
                        + ": "
                        + syncCurrentWidgetId;
            }
        }
    }

    private void markWidgetSyncCompletion(String widgetId, boolean success) {
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
                    + " failed)."
                    + (widgetId == null || widgetId.isBlank() ? "" : " Last widget: " + widgetId + '.');
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
            syncCurrentWidgetId = "";
            syncCurrentWidgetTable = "";
            syncCurrentWidgetIndex = 0;
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
        summaryLastRun = Timestamp.from(Instant.now());
        persistSyncSettings();

        try {
            if (summaryStore == null) {
                summaryStore = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
                summaryStore.ensureTable();
            }

            String startMessage = manualTrigger
                    ? "Manual summary generation started..."
                    : "Preparing daily summary context...";
            summaryStore.upsertProgress(day, slot, "running", 5, startMessage, 0, true, false);

            int effectiveMaxRows = clampSummaryMaxRows(summaryMaxRows);
            List<SelectedEntry> entries = loadEntriesForDay(day, effectiveMaxRows);
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
            // Daily summary must use the API key from Server/Workspace configuration only.
            // Do not silently fall back to Widget Health credentials.
            String apiKey = defaultIfBlank(cfg.getApiKey(), null);
            ApiAuthResolver.ResolvedApiAuth resolvedSummaryAuth = ApiAuthResolver.resolveForServerConfigOutbound(apiKey);
            boolean hasSummaryAuth = resolvedSummaryAuth.hasToken();

            if (workspaceSlug == null || workspaceSlug.isBlank() || baseUrl == null || baseUrl.isBlank()) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace configuration incomplete.",
                        "Unable to generate summary: workspace configuration incomplete.");
            }

            if (!hasSummaryAuth) {
                return failDailySummary(day, slot, entryCount,
                        "Server/workspace API authentication configuration incomplete.",
                        "Unable to generate summary: configure API key in the Server and Workspace section.");
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
            if (hasSummaryAuth && !isHttpsUrl(canonicalTargetUrl) && requireHttpsWithAuth) {
                return failDailySummary(day, slot, entryCount,
                        "Workspace URL must be HTTPS when API key is configured.",
                        "Unable to generate summary: API key requires HTTPS workspace URL.");
            }

            if (hasSummaryAuth && !isHttpsUrl(canonicalTargetUrl) && !requireHttpsWithAuth) {
                log.info(() -> "Widget summary generation allowing HTTP workspace URL with API key because "
                        + REQUIRE_HTTPS_WITH_AUTH_ENV + " is disabled. url=" + canonicalTargetUrl);
            }

            TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(canonicalTargetUrl);
            if (!trust.isValid()) {
                String trustReason = defaultIfBlank(trust.getReason(), "unspecified trust validation failure.");
                if (isSummaryTargetFromConfiguredServer(canonicalTargetUrl, cfg)) {
                    log.log(Level.INFO,
                        "Bypassing summary URL trust validation for DB-configured workspace target. reason={0}, url={1}",
                        new Object[]{trustReason, canonicalTargetUrl});
                } else {
                return failDailySummary(day, slot, entryCount,
                        "Workspace URL trust validation failed.",
                    "Unable to generate summary: workspace URL trust validation failed. Reason: " + trustReason);
                }
            }

            summaryStore.upsertProgress(day, slot, "running", 45, "Sending compact summary request...", entries.size(), false, false);

            List<TermDefinition> termDefinitions = loadSummaryTerms();
            List<SelectedEntry> upstreamEntries = limitSummaryEntriesForUpstream(entries);
            String promptGuide = normalizeSummarySnippet(resolveSummaryPrompt(), SUMMARY_GENTLE_GUIDANCE_CHARS);
            SummaryPayloadPlan payloadPlan = buildPerformanceSafeSummaryRequestMessage(promptGuide, upstreamEntries, entries.size());
            String singlePassMessage = payloadPlan.message;
            int entryCountSent = payloadPlan.includedEntries;

            String summaryRequestId = "daily-summary-" + day + "-slot-" + slot;

            ServerDiagnosticsLog.write(
                    "widget-sync",
                    summaryRequestId,
                    "summary-request",
                    "targetUrl=" + canonicalTargetUrl
                            + "\nentryCountTotal=" + entries.size()
                            + "\nentryCountSent=" + entryCountSent
                            + "\nmessageChars=" + singlePassMessage.length()
                            + "\nrequestBytes=" + payloadPlan.requestBytes
                            + "\nbudgetReduced=" + payloadPlan.budgetReduced
                            + "\nauthSource=" + defaultString(resolvedSummaryAuth.source())
                            + "\npreferredHeader=" + defaultString(resolvedSummaryAuth.preferredHeaderName())
            );

            if (payloadPlan.budgetReduced) {
                log.log(Level.INFO,
                        "Summary payload reduced for upstream safety. entryCountSent={0}, requestBytes={1}",
                        new Object[]{entryCountSent, payloadPlan.requestBytes});
            }

            WorkspaceResponse finalResp;
            try {
                finalResp = runSinglePassSummaryChat(
                        canonicalTargetUrl,
                        apiKey,
                        singlePassMessage,
                        summaryRequestId
                );
            } catch (IllegalArgumentException | IllegalStateException ex) {
                finalResp = null;
                String summary = isUpstreamSummaryRequired()
                    ? "Single-pass summary request failed while upstream summary is required"
                    : "Single-pass summary request failed; falling back locally";
                logWarningWithDiagnostics(
                        "summary-single-pass-failed",
                    summary,
                        "requestId=" + summaryRequestId + "\nentryCountSent=" + entryCountSent,
                        ex
                );
            }

            if (finalResp != null) {
                ServerDiagnosticsLog.write(
                        "widget-sync",
                        summaryRequestId,
                        "summary-response-single-pass",
                        "status=" + finalResp.statusCode()
                                + "\ncontentType=" + defaultString(finalResp.contentType())
                                + "\nbodySnippet=" + summarizeBodyForDiagnostics(finalResp.body())
                );
            }

            if (finalResp == null) {
                String reason = "Summary request returned no response.";
                if (shouldUseLocalSummaryFallback(0)) {
                    String fallbackMarkdown = buildLocalSummaryMarkdown(entries, termDefinitions, 0, reason);
                    persistLocalFallbackSummary(day, slot, entries, fallbackMarkdown, 0, reason);
                    return true;
                }
                return failDailySummary(day, slot, entryCount,
                        "Summary request failed.",
                        reason + " Upstream summary is required.");
            }

            int statusCode = finalResp.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (shouldRetryDirectSummaryFallback(finalResp)) {
                    try {
                        log.log(Level.INFO,
                                "Retrying summary using direct fallback after single-pass upstream status={0}",
                                new Object[]{statusCode});
                        WorkspaceResponse directFallback = runDirectSummaryChat(
                                canonicalTargetUrl,
                                apiKey,
                                resolveSummaryPrompt(),
                                upstreamEntries,
                                summaryRequestId + "-direct-fallback"
                        );

                        if (directFallback != null) {
                            ServerDiagnosticsLog.write(
                                    "widget-sync",
                                    summaryRequestId,
                                    "summary-response-direct-fallback",
                                    "status=" + directFallback.statusCode()
                                            + "\ncontentType=" + defaultString(directFallback.contentType())
                                            + "\nbodySnippet=" + summarizeBodyForDiagnostics(directFallback.body())
                            );
                            finalResp = directFallback;
                            statusCode = finalResp.statusCode();
                        }
                    } catch (IllegalArgumentException | IllegalStateException ex) {
                        logWarningWithDiagnostics(
                                "summary-direct-fallback-failed",
                                "Direct summary fallback failed after single-pass upstream failure",
                                "requestId=" + summaryRequestId + "\nstatusCode=" + statusCode,
                                ex
                        );
                    }
                }

                String upstreamText = extractPrimaryText(finalResp.body());
                if (upstreamText == null || upstreamText.isBlank()) {
                    upstreamText = "Workspace returned HTTP " + statusCode + ".";
                }
                String truncated = upstreamText.length() > 1200 ? upstreamText.substring(0, 1200) : upstreamText;
                String friendlyFailure = buildUserFacingSummaryFailureMessage(
                        statusCode,
                        truncated,
                        workspaceSlug,
                        canonicalTargetUrl
                );

                if (shouldUseLocalSummaryFallback(statusCode, truncated)) {
                    String fallbackMarkdown = buildLocalSummaryMarkdown(entries, termDefinitions, statusCode, truncated);
                    persistLocalFallbackSummary(day, slot, entries, fallbackMarkdown, statusCode, truncated);
                    return true;
                }

                return failDailySummary(day, slot, entryCount,
                        friendlyFailure,
                        truncated);
            }

            String raw = extractPrimaryText(finalResp.body());
            if (raw == null || raw.isBlank()) {
                String reason = "No summary message returned by upstream service.";
                if (shouldUseLocalSummaryFallback(finalResp.statusCode())) {
                    String fallbackMarkdown = buildLocalSummaryMarkdown(entries, termDefinitions, finalResp.statusCode(), reason);
                    persistLocalFallbackSummary(day, slot, entries, fallbackMarkdown, finalResp.statusCode(), reason);
                    return true;
                }
                return failDailySummary(day, slot, entryCount,
                        "Summary response was empty.",
                        reason + " Upstream summary is required.");
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
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            logDailySummaryFailure(manualTrigger ? "manual-summary-retry" : "auto-summary", e);
            return failDailySummary(day, slot, Math.max(0, entryCount),
                    "Summary generation failed.",
                    "Summary generation failed due to an internal error: " + defaultString(e.getMessage()));
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
            + " Automatic summary generation is paused until an ADMIN runs manual summary generation.";

        if (summaryStore != null) {
            summaryStore.upsertSummary(
                day,
                slot,
                "error",
                100,
                pausedMessage,
                defaultIfBlank(overall, "Summary generation failed. Manual summary generation is required to continue."),
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
        summaryAutoPausedReason = defaultIfBlank(reason, "Automatic summary generation paused until manual summary generation succeeds.");
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
            logWarningWithDiagnostics(
                    "summary-list-widgets-failed",
                    "Unable to list widgets for daily summary",
                    "day=" + String.valueOf(day),
                    ex
            );
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

                int remainingRows = maxRows - out.size();
                if (remainingRows <= 0) {
                    return out;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC LIMIT ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, start);
                    ps.setTimestamp(2, end);
                    ps.setInt(3, remainingRows);

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
            logWarningWithDiagnostics(
                    "summary-load-day-entries-failed",
                    "Unable to load day entries for daily summary",
                    "day=" + String.valueOf(day) + "\nmaxRows=" + maxRows,
                    ex
            );
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

    private boolean shouldUseLocalSummaryFallback(int statusCode) {
        return shouldUseLocalSummaryFallback(statusCode, null);
    }

    private boolean shouldUseLocalSummaryFallback(int statusCode, String upstreamReason) {
        if (isUpstreamSummaryRequired()) {
            return false;
        }

        // Surface auth/workspace configuration failures directly instead of masking
        // them with local fallback summaries.
        if (statusCode == HttpServletResponse.SC_UNAUTHORIZED || statusCode == HttpServletResponse.SC_FORBIDDEN) {
            return false;
        }

        String reason = defaultString(upstreamReason).toLowerCase(Locale.ROOT);
        if (reason.contains("not a valid workspace") || reason.contains("no valid api key")) {
            return false;
        }

        return statusCode >= 400;
    }

    private String buildUserFacingSummaryFailureMessage(
            int statusCode,
            String upstreamReason,
            String workspaceSlug,
            String targetUrl
    ) {
        String reason = defaultString(upstreamReason);
        String lower = reason.toLowerCase(Locale.ROOT);
        String workspace = defaultIfBlank(workspaceSlug, "(blank)");

        if (statusCode == HttpServletResponse.SC_UNAUTHORIZED || statusCode == HttpServletResponse.SC_FORBIDDEN
                || lower.contains("no valid api key")
                || lower.contains("unauthorized")
                || lower.contains("forbidden")) {
            return "Summary failed: API key was rejected by the workspace server. "
                    + "Verify API key in Server and Workspace for workspace '" + workspace + "'.";
        }

        if (lower.contains("not a valid workspace") || (lower.contains("workspace") && lower.contains("invalid"))) {
            return "Summary failed: workspace '" + workspace + "' is not valid on the target server. "
                    + "Update workspace name in Server and Workspace settings.";
        }

        if (lower.contains("cannot read properties of null") && lower.contains("reading 'id'")) {
            return "Summary failed: upstream chat session initialization error (null chat id). "
                + "This is a server-side workspace API issue; verify workspace chat API behavior and proxy path handling."
                + appendReasonSuffix(reason);
        }

        if (statusCode == HttpServletResponse.SC_GATEWAY_TIMEOUT) {
            return "Summary failed: upstream gateway timeout (HTTP 504). "
                + "The workspace server did not return in time; check reverse-proxy timeout and upstream capacity."
                + appendReasonSuffix(reason);
        }

        if (statusCode == HttpServletResponse.SC_BAD_REQUEST) {
            return "Summary failed: upstream rejected the request with HTTP 400. "
                    + "Verify server URL, workspace name, and reverse-proxy rules."
                    + appendReasonSuffix(reason);
        }

        return "Summary request failed with upstream status " + statusCode + "."
                + appendReasonSuffix(reason)
                + " Target=" + defaultString(targetUrl);
    }

    private String appendReasonSuffix(String reason) {
        String trimmed = defaultString(reason).trim();
        if (trimmed.isBlank()) {
            return "";
        }
        return " Reason: " + (trimmed.length() > 320 ? trimmed.substring(0, 320) : trimmed);
    }

    private void persistLocalFallbackSummary(
            LocalDate day,
            int slot,
            List<SelectedEntry> entries,
            String markdown,
            int upstreamStatus,
            String upstreamReason
    ) {
        String overall = section(markdown, "Overall");
        String quality = section(markdown, "Quality");
        String response = section(markdown, "Response");
        String usage = section(markdown, "Usage");

        summaryStore.upsertSummary(
                day,
                slot,
                "success",
                100,
                "Summary generated using local fallback because upstream returned HTTP " + upstreamStatus + ".",
                defaultIfBlank(overall, markdown),
                defaultIfBlank(quality, "No specific quality notes generated."),
                defaultIfBlank(response, "No specific response notes generated."),
                defaultIfBlank(usage, "No specific usage notes generated."),
                entries == null ? 0 : entries.size(),
                false,
                true
        );

        ServerDiagnosticsLog.write(
                "widget-sync",
                "daily-summary-local-fallback-" + day + "-slot-" + slot,
                "summary-response-local-fallback",
                "upstreamStatus=" + upstreamStatus
                        + "\nupstreamReason=" + defaultString(upstreamReason)
                        + "\nentryCount=" + (entries == null ? 0 : entries.size())
        );

        resumeAutomaticSummaryGeneration("Local fallback summary generated successfully.");
    }

    private String buildLocalSummaryMarkdown(
            List<SelectedEntry> entries,
            List<TermDefinition> termDefinitions,
            int upstreamStatus,
            String upstreamReason
    ) {
        int total = entries == null ? 0 : entries.size();
        Map<String, Integer> sessionCounts = new LinkedHashMap<>();
        Map<String, Integer> keywordCounts = new LinkedHashMap<>();
        Map<String, Integer> termCounts = matchTermsInEntries(entries, termDefinitions);
        int matchedTermsUnique = termCounts.size();
        int matchedTermMentions = sumCounts(termCounts);
        int termMatchedChats = countChatsWithTermMatch(entries, termDefinitions);

        Map<String, Integer> frustrationPointCounts = new LinkedHashMap<>();
        int frustrationSignals = 0;
        int frustratedChats = 0;
        int likelyAcceptedChats = 0;

        int potentialQualityIssues = 0;
        int emptyResponses = 0;
        int totalPromptChars = 0;
        int totalResponseChars = 0;

        if (entries != null) {
            for (SelectedEntry entry : entries) {
                if (entry == null) {
                    continue;
                }

                String sessionId = normalizeSummarySnippet(entry.getSessionId(), 128);
                if (!sessionId.isBlank()) {
                    sessionCounts.merge(sessionId, 1, Integer::sum);
                }

                String prompt = normalizeSummarySnippet(entry.getPrompt(), 1200);
                String response = normalizeSummarySnippet(entry.getResponse(), 3000);

                totalPromptChars += prompt.length();
                totalResponseChars += response.length();

                if (response.isBlank()) {
                    emptyResponses++;
                }

                String combined = (prompt + " " + response).toLowerCase(Locale.ROOT);
                int signalCountForChat = countFrustrationSignals(combined, frustrationPointCounts);
                frustrationSignals += signalCountForChat;
                if (signalCountForChat > 0) {
                    frustratedChats++;
                }

                if (isLikelyAcceptedAnswer(response, combined)) {
                    likelyAcceptedChats++;
                }

                String lowerResponse = response.toLowerCase(Locale.ROOT);
                if (lowerResponse.contains("name and email")
                        || lowerResponse.contains("professional services")
                        || lowerResponse.contains("unable to find")
                        || lowerResponse.contains("cannot")) {
                    potentialQualityIssues++;
                }

                countKeywords(keywordCounts, prompt + " " + response);
            }
        }

        String topKeywords = joinTopKeywords(keywordCounts, 5);
        String topMatchedTerms = joinTopKeywords(termCounts, 5);
        String frustrationLevel = deriveFrustrationLevel(total, frustratedChats, frustrationSignals);
        String topFrustrationPoint = topCategory(frustrationPointCounts);
        String otherFrustrationPoints = otherCategories(frustrationPointCounts, topFrustrationPoint, 2);
        double avgPrompt = total == 0 ? 0.0 : ((double) totalPromptChars / (double) total);
        double avgResponse = total == 0 ? 0.0 : ((double) totalResponseChars / (double) total);

        double qualityScore = computeQualityScore(total, potentialQualityIssues, emptyResponses, frustratedChats, frustrationLevel);
        double responseScore = computeResponseScore(total, likelyAcceptedChats, potentialQualityIssues, frustratedChats);
        double usageScore = computeUsageScore(total, termMatchedChats, matchedTermMentions, keywordCounts);
        int overallScore = computeOverallSummaryScore(qualityScore, responseScore, usageScore);

        List<String> articleSuggestions = suggestArticleTopics(termCounts, frustrationPointCounts, keywordCounts);
        String articleSuggestionText = articleSuggestions.isEmpty()
                ? "No specific article suggestion identified from today's chat signals."
                : String.join("; ", articleSuggestions);

        StringBuilder md = new StringBuilder(1400);
        md.append("## Overall\n")
            .append("- Overall effectiveness score: ").append(overallScore).append("/100.\n")
            .append("- Section scores: Quality ").append(formatSectionScore(qualityScore))
            .append(", Response ").append(formatSectionScore(responseScore))
            .append(", Usage ").append(formatSectionScore(usageScore)).append(".\n")
                .append("- Chats used in summary: ").append(total).append("\n")
                .append("- Distinct sessions: ").append(sessionCounts.size()).append("\n")
                .append("- Terms found from DB term list: ").append(matchedTermsUnique)
                .append(" unique term(s), ").append(matchedTermMentions).append(" total mention(s), ")
                .append(termMatchedChats).append(" chat(s) with at least one term match.\n")
                .append("- Top matched terms: ").append(defaultIfBlank(topMatchedTerms, "none matched")).append(".\n\n");

        md.append("## Quality\n")
            .append("- Score: ").append(formatSectionScore(qualityScore)).append(".\n")
                .append("- Potential quality concern signals: ").append(potentialQualityIssues).append(" chat(s).\n")
                .append("- Empty responses detected: ").append(emptyResponses).append(" chat(s).\n")
                .append("- Frustration detected: ").append(frustratedChats > 0 ? "Yes" : "No")
                .append(" (level: ").append(frustrationLevel).append(").\n")
                .append("- Most frustrated point: ").append(defaultIfBlank(topFrustrationPoint, "none detected")).append(".\n")
                .append("- Other frustration points: ").append(defaultIfBlank(otherFrustrationPoints, "none detected")).append(".\n")
                .append("- Recommendation: review chats flagged for escalation language or missing direct answers.\n\n");

        md.append("## Response\n")
            .append("- Score: ").append(formatSectionScore(responseScore)).append(".\n")
                .append("- Average prompt length: ").append(Math.round(avgPrompt)).append(" chars.\n")
                .append("- Average response length: ").append(Math.round(avgResponse)).append(" chars.\n")
                .append("- Likely accepted answers: ").append(likelyAcceptedChats).append("/").append(total).append(" chat(s).\n")
                .append("- Frustration signal count: ").append(frustrationSignals).append(".\n")
                .append("- Recommendation: keep first response concise and task-focused before escalation guidance.\n\n");

        md.append("## Usage\n")
            .append("- Score: ").append(formatSectionScore(usageScore)).append(".\n")
                .append("- Top observed keywords: ").append(defaultIfBlank(topKeywords, "no strong repeated terms")).append(".\n")
                .append("- Suggested article or content improvements: ").append(articleSuggestionText).append("\n")
                .append("- Recommendation: monitor repeated question categories to improve default guidance coverage and response quality.\n");

        return md.toString();
    }

    private List<TermDefinition> loadSummaryTerms() {
        try {
            TermsStore store = termsStore();
            if (store == null) {
                return List.of();
            }
            List<TermDefinition> terms = store.listAll();
            return terms == null ? List.of() : terms;
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.FINE, "Unable to load term definitions for summary prompt", ex);
            return List.of();
        }
    }

    private String buildSummaryPromptWithTerms(String basePrompt, List<TermDefinition> terms) {
        String normalizedBase = normalizeSummaryPrompt(basePrompt);
        StringBuilder out = new StringBuilder(Math.min(MAX_SUMMARY_PROMPT_CHARS, normalizedBase.length() + 3000));
        out.append(normalizedBase).append("\n\n")
                .append("Additional required reporting details:\n")
                .append("- Report chats used in summary as an explicit count.\n")
            .append("- Report scores: Overall effectiveness as 0-100, plus section scores for Quality, Response, and Usage as 0.0-5.0.\n")
                .append("- Report term coverage from the DB term list: unique terms matched and total mentions in chats used.\n")
                .append("- Report frustration detection as Yes/No with overall level (none/low/medium/high).\n")
                .append("- If frustration exists, report the most frustrated point and any other notable frustration points.\n")
                .append("- Assess answer quality and whether users likely accepted the answer.\n")
                .append("- Provide feedback suggestions, including at least one article topic to improve user experience.\n");

        if (terms != null && !terms.isEmpty()) {
            out.append("\nDB term catalog for matching in chats:\n");
            int appended = 0;
            for (TermDefinition term : terms) {
                if (term == null || term.isSystemFlag()) {
                    continue;
                }
                if (appended >= SUMMARY_MAX_TERMS_FOR_PROMPT) {
                    out.append("- ... additional terms omitted for brevity\n");
                    break;
                }

                String name = normalizeSummarySnippet(term.getName(), 120);
                String pattern = normalizeSummarySnippet(term.getMatchPattern(), 120);
                String type = normalizeSummarySnippet(term.getMatchType(), 32);
                if (name.isBlank()) {
                    continue;
                }

                out.append("- ").append(name)
                        .append(" | type=").append(defaultIfBlank(type, "WILDCARD"))
                        .append(" | pattern=").append(defaultIfBlank(pattern, name))
                        .append("\n");
                appended++;
            }
        }

        String combined = out.toString();
        if (combined.length() > MAX_SUMMARY_PROMPT_CHARS) {
            return combined.substring(0, MAX_SUMMARY_PROMPT_CHARS);
        }
        return combined;
    }

    private Map<String, Integer> matchTermsInEntries(List<SelectedEntry> entries, List<TermDefinition> terms) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (entries == null || entries.isEmpty() || terms == null || terms.isEmpty()) {
            return counts;
        }

        for (SelectedEntry entry : entries) {
            if (entry == null) {
                continue;
            }

            String prompt = TextSanitizer.sanitizeForMatching(defaultString(entry.getPrompt()));
            String response = TextSanitizer.sanitizeForMatching(defaultString(entry.getResponse()));
            String combined = (prompt + " " + response).trim();
            if (combined.isBlank()) {
                continue;
            }

            for (TermDefinition term : terms) {
                if (term == null || term.isSystemFlag()) {
                    continue;
                }
                try {
                    if (TermMatcher.matches(term, combined)) {
                        String name = normalizeSummarySnippet(term.getName(), 120);
                        if (!name.isBlank()) {
                            counts.merge(name, 1, Integer::sum);
                        }
                    }
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    log.log(Level.FINE, "Term match evaluation failed during summary fallback", ex);
                }
            }
        }

        return counts;
    }

    private int countChatsWithTermMatch(List<SelectedEntry> entries, List<TermDefinition> terms) {
        if (entries == null || entries.isEmpty() || terms == null || terms.isEmpty()) {
            return 0;
        }

        int matchedChats = 0;
        for (SelectedEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            String combined = TextSanitizer.sanitizeForMatching(defaultString(entry.getPrompt()) + " " + defaultString(entry.getResponse()));
            if (combined.isBlank()) {
                continue;
            }

            boolean matched = false;
            for (TermDefinition term : terms) {
                if (term == null || term.isSystemFlag()) {
                    continue;
                }
                try {
                    if (TermMatcher.matches(term, combined)) {
                        matched = true;
                        break;
                    }
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    log.log(Level.FINE, "Term chat coverage evaluation failed", ex);
                }
            }
            if (matched) {
                matchedChats++;
            }
        }

        return matchedChats;
    }

    private int sumCounts(Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Integer count : counts.values()) {
            if (count != null && count > 0) {
                sum += count;
            }
        }
        return sum;
    }

    private double computeQualityScore(
            int totalChats,
            int potentialQualityIssues,
            int emptyResponses,
            int frustratedChats,
            String frustrationLevel
    ) {
        if (totalChats <= 0) {
            return 1.0d;
        }

        double issueRatio = ratio(potentialQualityIssues, totalChats);
        double emptyRatio = ratio(emptyResponses, totalChats);
        double frustratedRatio = ratio(frustratedChats, totalChats);

        double score = 5.0d;
        score -= Math.min(1.8d, issueRatio * 3.0d);
        score -= Math.min(1.2d, emptyRatio * 4.0d);
        score -= Math.min(1.0d, frustratedRatio * 2.0d);

        String level = defaultString(frustrationLevel).toLowerCase(Locale.ROOT);
        if ("high".equals(level)) {
            score -= 0.8d;
        } else if ("medium".equals(level)) {
            score -= 0.5d;
        } else if ("low".equals(level)) {
            score -= 0.2d;
        }

        return clampSectionScore(score);
    }

    private double computeResponseScore(
            int totalChats,
            int likelyAcceptedChats,
            int potentialQualityIssues,
            int frustratedChats
    ) {
        if (totalChats <= 0) {
            return 1.0d;
        }

        double acceptedRatio = ratio(likelyAcceptedChats, totalChats);
        double issueRatio = ratio(potentialQualityIssues, totalChats);
        double frustratedRatio = ratio(frustratedChats, totalChats);

        double score = 1.0d + (acceptedRatio * 4.0d);
        score -= Math.min(1.0d, issueRatio * 1.8d);
        score -= Math.min(0.8d, frustratedRatio * 1.5d);

        return clampSectionScore(score);
    }

    private double computeUsageScore(
            int totalChats,
            int termMatchedChats,
            int matchedTermMentions,
            Map<String, Integer> keywordCounts
    ) {
        if (totalChats <= 0) {
            return 1.0d;
        }

        double chatCoverage = ratio(termMatchedChats, totalChats);
        double mentionDensity = Math.min(1.0d, (double) matchedTermMentions / (double) Math.max(1, totalChats));
        double keywordSignal = keywordCounts == null || keywordCounts.isEmpty() ? 0.15d : 0.55d;

        double score = 1.0d + (chatCoverage * 2.2d) + (mentionDensity * 1.3d) + keywordSignal;
        return clampSectionScore(score);
    }

    private int computeOverallSummaryScore(double qualityScore, double responseScore, double usageScore) {
        double weighted = (qualityScore * 0.40d) + (responseScore * 0.40d) + (usageScore * 0.20d);
        int normalized = (int) Math.round((weighted / 5.0d) * 100.0d);
        if (normalized < 0) {
            return 0;
        }
        return Math.min(normalized, 100);
    }

    private String formatSectionScore(double score) {
        return String.format(Locale.ROOT, "%.1f/5.0", clampSectionScore(score));
    }

    private double clampSectionScore(double score) {
        double safe = Double.isFinite(score) ? score : 1.0d;
        if (safe < 1.0d) {
            return 1.0d;
        }
        return Math.min(safe, 5.0d);
    }

    private double ratio(int numerator, int denominator) {
        if (denominator <= 0 || numerator <= 0) {
            return 0.0d;
        }
        return (double) numerator / (double) denominator;
    }

    private int countFrustrationSignals(String combinedLower, Map<String, Integer> frustrationPointCounts) {
        if (combinedLower == null || combinedLower.isBlank()) {
            return 0;
        }

        int signals = 0;

        signals += registerFrustrationPoint(combinedLower, frustrationPointCounts,
                "Authentication or access problems",
                "401", "403", "no valid api key", "authorization", "auth", "forbidden", "unauthorized");

        signals += registerFrustrationPoint(combinedLower, frustrationPointCounts,
                "Connectivity or platform failures",
                "400", "error", "failed", "cannot", "can't", "unable", "timeout", "not working", "issue", "problem", "retry");

        signals += registerFrustrationPoint(combinedLower, frustrationPointCounts,
                "Answer quality or clarity gaps",
                "unclear", "wrong", "incomplete", "not helpful", "still", "confusing", "does not work", "didn't work");

        return signals;
    }

    private int registerFrustrationPoint(String text, Map<String, Integer> categoryCounts, String category, String... needles) {
        if (text == null || text.isBlank() || categoryCounts == null || needles == null || needles.length == 0) {
            return 0;
        }

        for (String needle : needles) {
            if (needle != null && !needle.isBlank() && text.contains(needle)) {
                categoryCounts.merge(category, 1, Integer::sum);
                return 1;
            }
        }
        return 0;
    }

    private String deriveFrustrationLevel(int totalChats, int frustratedChats, int frustrationSignals) {
        if (totalChats <= 0 || frustratedChats <= 0 || frustrationSignals <= 0) {
            return "none";
        }

        double ratio = (double) frustratedChats / (double) totalChats;
        if (ratio >= 0.60d || frustrationSignals >= Math.max(4, totalChats)) {
            return "high";
        }
        if (ratio >= 0.30d || frustrationSignals >= 2) {
            return "medium";
        }
        return "low";
    }

    private String topCategory(Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return "";
        }
        String best = "";
        int bestCount = -1;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            int value = e.getValue() == null ? 0 : e.getValue();
            if (value > bestCount) {
                bestCount = value;
                best = defaultString(e.getKey());
            }
        }
        return best;
    }

    private String otherCategories(Map<String, Integer> counts, String topCategory, int maxItems) {
        if (counts == null || counts.isEmpty() || maxItems <= 0) {
            return "";
        }

        List<Map.Entry<String, Integer>> ordered = new ArrayList<>(counts.entrySet());
        ordered.sort((a, b) -> Integer.compare(b.getValue() == null ? 0 : b.getValue(), a.getValue() == null ? 0 : a.getValue()));

        StringBuilder sb = new StringBuilder();
        int added = 0;
        for (Map.Entry<String, Integer> entry : ordered) {
            String category = defaultString(entry.getKey());
            if (category.isBlank() || category.equals(topCategory)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(category);
            added++;
            if (added >= maxItems) {
                break;
            }
        }
        return sb.toString();
    }

    private boolean isLikelyAcceptedAnswer(String response, String combinedLower) {
        if (response == null || response.isBlank()) {
            return false;
        }

        String lowerResponse = response.toLowerCase(Locale.ROOT);
        if (lowerResponse.contains("professional services")
                || lowerResponse.contains("unable to")
                || lowerResponse.contains("cannot")
                || lowerResponse.contains("can't")) {
            return false;
        }

        if (combinedLower != null && (combinedLower.contains("still") || combinedLower.contains("not working") || combinedLower.contains("didn't work"))) {
            return false;
        }

        return lowerResponse.contains("short answer")
                || lowerResponse.contains("recommended")
                || lowerResponse.contains("steps")
                || lowerResponse.contains("1.")
                || lowerResponse.contains("2.")
                || response.length() > 120;
    }

    private List<String> suggestArticleTopics(
            Map<String, Integer> termCounts,
            Map<String, Integer> frustrationPointCounts,
            Map<String, Integer> keywordCounts
    ) {
        List<String> suggestions = new ArrayList<>();

        String topTerm = topCategory(termCounts);
        String topFrustration = topCategory(frustrationPointCounts);
        String topKeyword = topCategory(keywordCounts);

        if (!topTerm.isBlank()) {
            suggestions.add("Create or improve a troubleshooting article for '" + topTerm + "' with concrete step-by-step examples.");
        }

        if (!topFrustration.isBlank()) {
            suggestions.add("Publish a quick-resolution guide focused on '" + topFrustration + "' symptoms and fixes.");
        }

        if (!topKeyword.isBlank()) {
            suggestions.add("Add an FAQ entry covering common '" + topKeyword + "' questions with validated examples and expected outcomes.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Add a daily support trends article that captures recurring questions, quality gaps, and correction notes.");
        }

        return suggestions;
    }

    private void countKeywords(Map<String, Integer> counter, String text) {
        if (counter == null || text == null || text.isBlank()) {
            return;
        }

        String[] words = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+");

        for (String word : words) {
            if (word == null || word.isBlank() || word.length() < 4) {
                continue;
            }
            if (isStopKeyword(word)) {
                continue;
            }
            counter.merge(word, 1, Integer::sum);
        }
    }

    private boolean isStopKeyword(String word) {
        return switch (word) {
            case "this", "that", "with", "from", "your", "have", "will", "what", "when", "where", "which",
                 "could", "should", "would", "into", "then", "them", "they", "their", "there", "about", "after",
                 "before", "because", "while", "using", "used", "need", "needs", "help", "please", "also", "only",
                 "today", "chat", "chats", "response", "prompt", "summary", "section", "sections" -> true;
            default -> false;
        };
    }

    private String joinTopKeywords(Map<String, Integer> counter, int maxItems) {
        if (counter == null || counter.isEmpty() || maxItems <= 0) {
            return "";
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counter.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(maxItems, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(" (").append(e.getValue()).append(')');
        }

        return sb.toString();
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
    private List<JsonObject> fetchWidgetChatsWithRetry(ServerConfig config, String widgetId) {
        IllegalStateException lastFailure = null;

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
            } catch (IllegalStateException ex) {
                lastFailure = ex;
                if (causedByInterrupted(ex)) {
                    log.log(Level.FINE, "Widget sync fetch interrupted", ex);
                    Thread.currentThread().interrupt();
                    break;
                }

                boolean retryable = isRetryable(ex);
                if (!retryable || attempt == HTTP_MAX_ATTEMPTS) {
                    break;
                }

                long backoff = computeBackoffWithJitterMs(attempt);
                log.log(Level.WARNING,
                    "Transient sync fetch failure for widget {0} (attempt {1}/{2}), retrying in {3}ms: {4}",
                    new Object[]{widgetId, attempt, HTTP_MAX_ATTEMPTS, backoff, ex.getMessage()});
                try {
                    TimeUnit.MILLISECONDS.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Widget sync retry backoff interrupted", ie);
                }
            }
        }

        throw lastFailure == null
                ? new IllegalStateException("Sync fetch failed with unknown error")
                : lastFailure;
    }

    private List<JsonObject> fetchWidgetChatsOnce(ServerConfig config, String widgetId) {
        URI uri = buildSyncUri(config, widgetId);
        if (uri == null) {
            throw new IllegalStateException("Sync API URL is missing");
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
            throw new IllegalStateException("API key configured but sync URL is not HTTPS");
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
                    throw new IllegalStateException("Sync API transient server error " + statusCode
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
                    throw new IllegalStateException("Sync API returned " + statusCode
                        + " (contentType=" + contentType + ", upstreamBody=" + upstreamBody + ')');
                }

                if (!contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
                    throw new IllegalStateException(String.format("Unexpected content type '%s'", contentType));
                }

                try {
                    JsonNode root = OBJECT_MAPPER.readTree(bodyStream);
                    return normalizeResponse(root);
                } catch (JsonProcessingException je) {
                    throw new IllegalStateException("Invalid JSON received from sync API", je);
                }
            }
        } catch (IOException | RuntimeException e) {
            if (causedByInterrupted(e)) {
                Thread.currentThread().interrupt();
            }
            ServerDiagnosticsLog.write(
                    "widget-sync",
                    requestId,
                    "sync-error",
                    "url=" + uri + "\nwidgetId=" + defaultString(widgetId) + "\nmessage=" + defaultString(e.getMessage()),
                    e
            );
            if (e instanceof RuntimeException runtimeEx) {
                throw runtimeEx;
            }
            throw new IllegalStateException("Sync API communication failed", e);
        }
    }

    private SyncHttpResult sendSyncRequest(URI uri, String apiKey) {
        ApiAuthResolver.ResolvedApiAuth primaryAuth = ApiAuthResolver.resolveForOutbound(apiKey);
        ApiAuthResolver.ResolvedApiAuth secondaryAuth = ApiAuthResolver.resolveForOutbound(null);
        List<ApiAuthResolver.ResolvedApiAuth> candidates = buildAuthCandidates(primaryAuth, secondaryAuth);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Sync API key is required.");
        }

        SyncHttpResult lastResult = null;
        for (int authIndex = 0; authIndex < candidates.size(); authIndex++) {
            ApiAuthResolver.ResolvedApiAuth auth = candidates.get(authIndex);
            AuthHeaderMode mode = resolvePrimaryAuthMode(auth.preferredHeaderName());
            HttpRequest request = buildSyncRequest(uri, auth, mode);
            HttpResponse<InputStream> response;
            try {
                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Sync request interrupted", e);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to call sync API", e);
            }
            int status = response.statusCode();

            boolean hasMoreAuth = authIndex < (candidates.size() - 1);
            if ((status == 401 || status == 403) && hasMoreAuth) {
                InputStream ignored = response.body();
                if (ignored != null) {
                    try {
                        ignored.close();
                    } catch (IOException closeErr) {
                        log.log(Level.FINE, "Unable to close auth fallback response body", closeErr);
                    }
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
            throw new IllegalStateException("Sync request failed before receiving a response.");
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

    private void throttleSyncRequestRate() {
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
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Sync request throttling interrupted", e);
            }
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
                default -> builder.header("Authorization", "Bearer " + token);
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

    private boolean isRetryable(Throwable e) {
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

    private synchronized void loadSyncSettings() {
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            SyncSettings settings = readSyncSettings(conn);
            if (settings.intervalSeconds > 0) {
                syncIntervalSeconds = settings.intervalSeconds;
            }
            summaryIntervalSeconds = clampSummaryIntervalSeconds(settings.summaryIntervalSeconds);
            summaryAutoEnabled = settings.summaryAutoEnabled;
            summaryMaxRows = clampSummaryMaxRows(settings.summaryMaxRows);
            summaryMaxUpstreamEntries = clampSummaryMaxUpstreamEntries(settings.summaryMaxUpstreamEntries);
            summaryMaxMessageChars = clampSummaryMaxMessageChars(settings.summaryMaxMessageChars);
            summaryMaxRequestBytes = clampSummaryMaxRequestBytes(settings.summaryMaxRequestBytes);
            summaryPromptTemplate = normalizeSummaryPrompt(settings.summaryPrompt);
            lastSynced = settings.lastSynced;
            summaryLastRun = settings.summaryLastRun;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load sync settings", e);
        }
    }

    private void persistSyncSettings() {
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            ensureSyncSettingsTable(conn);
            upsertSyncSettings(conn, syncIntervalSeconds, lastSynced);
        } catch (SQLException e) {
            logWarningWithDiagnostics(
                    "persist-sync-settings-failed",
                    "Unable to persist sync settings",
                    "intervalSeconds=" + syncIntervalSeconds + "\nlastSynced=" + String.valueOf(lastSynced),
                    e
            );
        }
    }

    private void ensureSyncSettingsTable(Connection conn) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS widget_sync_settings ("
                    + "id INTEGER PRIMARY KEY, interval_seconds BIGINT NOT NULL, last_synced TIMESTAMP, "
                    + "summary_interval_seconds BIGINT NOT NULL DEFAULT " + DEFAULT_SUMMARY_INTERVAL_SECONDS + ", "
                + "summary_auto_enabled BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "summary_prompt TEXT, "
                    + "summary_max_rows INTEGER NOT NULL DEFAULT " + DEFAULT_SUMMARY_MAX_ROWS + ", "
                + "summary_max_upstream_entries INTEGER NOT NULL DEFAULT " + DEFAULT_SUMMARY_MAX_UPSTREAM_ENTRIES + ", "
                + "summary_max_message_chars INTEGER NOT NULL DEFAULT " + DEFAULT_SUMMARY_MAX_MESSAGE_CHARS + ", "
                + "summary_max_request_bytes INTEGER NOT NULL DEFAULT " + DEFAULT_SUMMARY_MAX_REQUEST_BYTES + ", "
                    + "summary_last_run TIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.execute();
            }

            addColumnIfMissing(conn,
                    "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_interval_seconds BIGINT");
            addColumnIfMissing(conn,
                "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_auto_enabled BOOLEAN");
            addColumnIfMissing(conn,
                    "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_prompt TEXT");
            addColumnIfMissing(conn,
                    "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_rows INTEGER");
            addColumnIfMissing(conn,
                "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_upstream_entries INTEGER");
            addColumnIfMissing(conn,
                "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_message_chars INTEGER");
            addColumnIfMissing(conn,
                "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_request_bytes INTEGER");
            addColumnIfMissing(conn,
                    "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_last_run TIMESTAMP");

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_interval_seconds = ? WHERE summary_interval_seconds IS NULL")) {
                ps.setLong(1, DEFAULT_SUMMARY_INTERVAL_SECONDS);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_auto_enabled = ? WHERE summary_auto_enabled IS NULL")) {
                ps.setBoolean(1, DEFAULT_SUMMARY_AUTO_ENABLED);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_max_rows = ? WHERE summary_max_rows IS NULL")) {
                ps.setInt(1, DEFAULT_SUMMARY_MAX_ROWS);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_max_upstream_entries = ? WHERE summary_max_upstream_entries IS NULL")) {
                ps.setInt(1, DEFAULT_SUMMARY_MAX_UPSTREAM_ENTRIES);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_max_message_chars = ? WHERE summary_max_message_chars IS NULL")) {
                ps.setInt(1, DEFAULT_SUMMARY_MAX_MESSAGE_CHARS);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE widget_sync_settings SET summary_max_request_bytes = ? WHERE summary_max_request_bytes IS NULL")) {
                ps.setInt(1, DEFAULT_SUMMARY_MAX_REQUEST_BYTES);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ensure sync settings table", e);
        }
    }

    private void addColumnIfMissing(Connection conn, String ddl) {
        try (PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to add sync settings column", e);
        }
    }

    private SyncSettings readSyncSettings(Connection conn) {
        try {
            String sql = "SELECT interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
                + "summary_max_upstream_entries, summary_max_message_chars, summary_max_request_bytes, summary_last_run"
                    + " FROM widget_sync_settings WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long persistedInterval = readPersistedIntervalSeconds(rs, "interval_seconds", syncIntervalSeconds);
                    if (persistedInterval < MIN_INTERVAL_SECONDS || persistedInterval > TimeUnit.DAYS.toSeconds(30)) {
                        persistedInterval = syncIntervalSeconds;
                    }

                    long persistedSummaryInterval = readPersistedIntervalSeconds(
                            rs,
                            "summary_interval_seconds",
                            summaryIntervalSeconds
                    );
                    boolean persistedSummaryAutoEnabled = readPersistedBoolean(
                        rs,
                        "summary_auto_enabled",
                        DEFAULT_SUMMARY_AUTO_ENABLED
                    );

                    int persistedSummaryMaxRows = readPersistedInt(rs, "summary_max_rows", summaryMaxRows);
                    int persistedSummaryMaxUpstreamEntries = readPersistedInt(
                        rs,
                        "summary_max_upstream_entries",
                        summaryMaxUpstreamEntries
                    );
                    int persistedSummaryMaxMessageChars = readPersistedInt(
                        rs,
                        "summary_max_message_chars",
                        summaryMaxMessageChars
                    );
                    int persistedSummaryMaxRequestBytes = readPersistedInt(
                        rs,
                        "summary_max_request_bytes",
                        summaryMaxRequestBytes
                    );

                    String persistedSummaryPrompt = readDbText(rs, "summary_prompt", MAX_SUMMARY_PROMPT_CHARS);
                    if (persistedSummaryPrompt != null && persistedSummaryPrompt.isBlank()) {
                        persistedSummaryPrompt = DEFAULT_SUMMARY_PROMPT;
                    }

                    Timestamp persistedLastSynced = sanitizePersistedTimestamp(readDbTimestamp(rs, "last_synced"));
                    Timestamp persistedSummaryLastRun = sanitizePersistedTimestamp(readDbTimestamp(rs, "summary_last_run"));
                    return new SyncSettings(
                            persistedInterval,
                            persistedLastSynced,
                            persistedSummaryInterval,
                            persistedSummaryAutoEnabled,
                            persistedSummaryPrompt,
                            persistedSummaryMaxRows,
                            persistedSummaryMaxUpstreamEntries,
                            persistedSummaryMaxMessageChars,
                            persistedSummaryMaxRequestBytes,
                            persistedSummaryLastRun
                    );
                }
            }
            upsertSyncSettings(
                    conn,
                    syncIntervalSeconds,
                    lastSynced,
                    summaryIntervalSeconds,
                    summaryAutoEnabled,
                    resolveSummaryPrompt(),
                    summaryMaxRows,
                    summaryMaxUpstreamEntries,
                    summaryMaxMessageChars,
                    summaryMaxRequestBytes,
                    summaryLastRun
            );
            return new SyncSettings(
                    syncIntervalSeconds,
                    lastSynced,
                    summaryIntervalSeconds,
                    summaryAutoEnabled,
                    resolveSummaryPrompt(),
                    summaryMaxRows,
                    summaryMaxUpstreamEntries,
                    summaryMaxMessageChars,
                    summaryMaxRequestBytes,
                    summaryLastRun
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read sync settings", e);
        }
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

    private void upsertSyncSettings(Connection conn, long intervalSeconds, Timestamp lastSynced) {
        upsertSyncSettings(
            conn,
            intervalSeconds,
            lastSynced,
            summaryIntervalSeconds,
            summaryAutoEnabled,
            resolveSummaryPrompt(),
            summaryMaxRows,
            summaryMaxUpstreamEntries,
            summaryMaxMessageChars,
            summaryMaxRequestBytes,
            summaryLastRun
        );
    }

    private void upsertSyncSettings(
            Connection conn,
            long intervalSeconds,
            Timestamp lastSynced,
            long summaryInterval,
            boolean summaryAuto,
            String summaryPrompt,
            int summaryRows,
            int summaryUpstreamEntries,
            int summaryMessageChars,
            int summaryRequestBytes,
            Timestamp lastSummaryRun
        ) {
        try {
            String sql = "INSERT INTO widget_sync_settings (id, interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
                + "summary_max_upstream_entries, summary_max_message_chars, summary_max_request_bytes, summary_last_run) "
                + "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "interval_seconds = EXCLUDED.interval_seconds, "
                + "last_synced = EXCLUDED.last_synced, "
                + "summary_interval_seconds = EXCLUDED.summary_interval_seconds, "
                + "summary_auto_enabled = EXCLUDED.summary_auto_enabled, "
                + "summary_prompt = EXCLUDED.summary_prompt, "
                + "summary_max_rows = EXCLUDED.summary_max_rows, "
                + "summary_max_upstream_entries = EXCLUDED.summary_max_upstream_entries, "
                + "summary_max_message_chars = EXCLUDED.summary_max_message_chars, "
                + "summary_max_request_bytes = EXCLUDED.summary_max_request_bytes, "
                + "summary_last_run = EXCLUDED.summary_last_run";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, intervalSeconds);
                ps.setTimestamp(2, lastSynced);
                ps.setLong(3, clampSummaryIntervalSeconds(summaryInterval));
                ps.setBoolean(4, summaryAuto);
                ps.setString(5, normalizeSummaryPrompt(summaryPrompt));
                ps.setInt(6, clampSummaryMaxRows(summaryRows));
                ps.setInt(7, clampSummaryMaxUpstreamEntries(summaryUpstreamEntries));
                ps.setInt(8, clampSummaryMaxMessageChars(summaryMessageChars));
                ps.setInt(9, clampSummaryMaxRequestBytes(summaryRequestBytes));
                ps.setTimestamp(10, lastSummaryRun);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to persist sync settings", e);
        }
    }

    private boolean authorizeAdmin(HttpServletRequest req, HttpServletResponse resp) {
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

    private void ensureTable(Connection conn, String tableName) {
        if (ensuredTables.contains(tableName)) {
            return;
        }

        try {
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

            String createdAtIdxName = (tableName + "_created_at_idx");
            if (createdAtIdxName.length() > 63) {
                createdAtIdxName = createdAtIdxName.substring(0, 63);
            }
            String quotedCreatedAtIdx = quoteIdentifier(createdAtIdxName);

            try (PreparedStatement ps = conn.prepareStatement("CREATE INDEX IF NOT EXISTS " + quotedCreatedAtIdx
                    + " ON " + quotedTable + " (created_at DESC)")) {
                ps.execute();
            } catch (SQLException createdAtIndexErr) {
                log.log(Level.FINE, "Created_at index creation failed for {0}", tableName);
                log.log(Level.FINEST, "Created_at index error", createdAtIndexErr);
            }

            ensuredTables.add(tableName);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ensure widget table " + tableName, e);
        }
    }

    private void dedupeByWidgetChatId(Connection conn, String tableName) {
        try {
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
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to dedupe widget table " + tableName, e);
        }
    }

    private List<JsonObject> findNewWidgetChats(Connection conn, String tableName, List<JsonObject> chats) {
        if (chats == null || chats.isEmpty()) {
            return List.of();
        }

        Map<String, JsonObject> uniqueById = new LinkedHashMap<>();
        for (JsonObject chat : chats) {
            String chatId = getString(chat, "id");
            if (chatId == null || chatId.isBlank()) {
                continue;
            }
            uniqueById.putIfAbsent(chatId, chat);
        }

        if (uniqueById.isEmpty()) {
            return List.of();
        }

        List<String> candidateIds = new ArrayList<>(uniqueById.keySet());
        Set<String> existingIds = fetchExistingWidgetChatIds(conn, tableName, candidateIds);

        List<JsonObject> newChats = new ArrayList<>();
        for (Map.Entry<String, JsonObject> entry : uniqueById.entrySet()) {
            if (!existingIds.contains(entry.getKey())) {
                newChats.add(entry.getValue());
            }
        }
        return newChats;
    }

    private Set<String> fetchExistingWidgetChatIds(Connection conn, String tableName, List<String> candidateIds) {
        Set<String> existingIds = new LinkedHashSet<>();
        if (candidateIds == null || candidateIds.isEmpty()) {
            return existingIds;
        }

        try {
            final int chunkSize = 500;
            String quotedTable = quoteIdentifier(tableName);

            for (int start = 0; start < candidateIds.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, candidateIds.size());
                List<String> chunk = candidateIds.subList(start, end);
                String sql = "SELECT widget_chat_id FROM " + quotedTable + " WHERE widget_chat_id IN ("
                        + buildInClausePlaceholders(chunk.size()) + ")";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (int i = 0; i < chunk.size(); i++) {
                        ps.setString(i + 1, chunk.get(i));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String existing = rs.getString(1);
                            if (existing != null && !existing.isBlank()) {
                                existingIds.add(existing);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to fetch existing widget chat IDs for table " + tableName, e);
        }

        return existingIds;
    }

    private String buildInClausePlaceholders(int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count * 2);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    private int insertWidgetChats(Connection conn, String tableName, List<JsonObject> chats) {
        if (chats == null || chats.isEmpty()) {
            return 0;
        }

        // De-duplicate IDs from the upstream payload before hitting the DB.
        Map<String, JsonObject> uniqueById = new LinkedHashMap<>();
        for (JsonObject chat : chats) {
            String chatId = getString(chat, "id");
            if (chatId == null || chatId.isBlank()) {
                continue;
            }
            uniqueById.putIfAbsent(chatId, chat);
        }

        if (uniqueById.isEmpty()) {
            return 0;
        }

        String sql = "INSERT INTO " + quoteIdentifier(tableName)
                + " (widget_chat_id, prompt, response_text, created_at, session_id, username) "
                + "VALUES (?,?,?,?,?,?) ON CONFLICT (widget_chat_id) DO NOTHING";

        boolean originalAutoCommit;
        int inserted = 0;

        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to begin widget chat insert transaction for table " + tableName, e);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (JsonObject chat : uniqueById.values()) {
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
            }
            int[] batchResult = ps.executeBatch();
            for (int result : batchResult) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                    inserted++;
                }
            }
            conn.commit();
            return inserted;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw new IllegalStateException("Unable to insert widget chats into table " + tableName, e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException resetError) {
                log.log(Level.FINE, "Unable to restore auto-commit for table {0}", tableName);
                log.log(Level.FINEST, "Auto-commit restore error", resetError);
            }
        }
    }

    private List<String> collectUniqueChatIds(List<JsonObject> chats) {
        if (chats == null || chats.isEmpty()) {
            return List.of();
        }

        Set<String> ids = new LinkedHashSet<>();
        for (JsonObject chat : chats) {
            String chatId = getString(chat, "id");
            if (chatId == null || chatId.isBlank()) {
                continue;
            }
            ids.add(chatId);
        }
        return new ArrayList<>(ids);
    }

    private List<JsonObject> filterChatsByIds(List<JsonObject> chats, Set<String> allowedIds) {
        if (chats == null || chats.isEmpty() || allowedIds == null || allowedIds.isEmpty()) {
            return List.of();
        }

        Set<String> remaining = new LinkedHashSet<>(allowedIds);
        List<JsonObject> filtered = new ArrayList<>(Math.min(chats.size(), allowedIds.size()));
        for (JsonObject chat : chats) {
            String chatId = getString(chat, "id");
            if (chatId == null || chatId.isBlank()) {
                continue;
            }
            if (remaining.remove(chatId)) {
                filtered.add(chat);
                if (remaining.isEmpty()) {
                    break;
                }
            }
        }
        return filtered;
    }

    private RecentChatIdCache recentChatIdCacheForWidget(String tableName) {
        if (SYNC_RECENT_CHAT_ID_CACHE_SIZE <= 0 || tableName == null || tableName.isBlank()) {
            return null;
        }
        return recentChatIdsByWidget.computeIfAbsent(
                tableName,
                key -> new RecentChatIdCache(SYNC_RECENT_CHAT_ID_CACHE_SIZE)
        );
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

    private boolean tableExists(Connection conn, String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return false;
        }
        if (ensuredTables.contains(tableName)) {
            return true;
        }

        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        ensuredTables.add(tableName);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, e);
        }
        return false;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private void jsonError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON error response", e);
            sendErrorSafe(resp, status, message == null ? "Request failed." : message);
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON response", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
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
        String raw = readEnvSanitized(envName, 32);
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
            log.log(Level.FINE, "Invalid integer environment value for {0}: {1}",
                    new Object[]{envName, raw});
            return fallback;
        }
    }

    private static long parseBoundedLongEnv(String envName, long fallback, long min, long max) {
        String raw = readEnvSanitized(envName, 32);
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
            log.log(Level.FINE, "Invalid long environment value for {0}: {1}",
                    new Object[]{envName, raw});
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
            builder.append(resolvePreferredScheme(config)).append("://").append(normalized);
        }

        boolean hasPort = normalized.matches(".*:\\d+$");
        if (!hasPort && config.getServerPort() > 0) {
            builder.append(':').append(config.getServerPort());
        }

        return stripTrailingSlash(builder.toString());
    }

    private String resolvePreferredScheme(ServerConfig config) {
        String schemeFromConnectionInfo = extractHttpScheme(config == null ? null : config.getConnectionInfo());
        if (!schemeFromConnectionInfo.isBlank()) {
            return schemeFromConnectionInfo;
        }

        int port = config == null ? -1 : config.getServerPort();
        if (port == 80) {
            return "http";
        }
        if (port == 443) {
            return "https";
        }

        return isHttpsRequiredWithAuth() ? "https" : "http";
    }

    private String extractHttpScheme(String candidateUrl) {
        if (candidateUrl == null || candidateUrl.isBlank()) {
            return "";
        }
        try {
            URI parsed = URI.create(candidateUrl.trim());
            String scheme = parsed.getScheme();
            if (scheme == null || scheme.isBlank()) {
                return "";
            }
            String normalized = scheme.toLowerCase(Locale.ROOT);
            if ("http".equals(normalized) || "https".equals(normalized)) {
                return normalized;
            }
            return "";
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to infer scheme from connectionInfo", e);
            return "";
        }
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

    private WorkspaceResponse runDirectSummaryChat(
            String targetUrl,
            String apiKey,
            String summaryPrompt,
            List<SelectedEntry> entries,
            String requestId
        ) {
        if (workspaceClient == null) {
            throw new IllegalStateException("Workspace client unavailable for direct summary fallback.");
        }

        String directMessage = buildDirectSummaryMessage(summaryPrompt, entries);
        String directSessionId = "summary-direct-" + UUID.randomUUID();
        WorkspaceResponse response = sendChatHandled(
                targetUrl,
                apiKey,
                directMessage,
                "chat",
                directSessionId,
                false,
                Json.createArrayBuilder().build(),
                requestId + "-direct"
        );

        if (shouldRetryCompactDirectSummary(response)) {
            String compactMessage = buildDirectSummaryMessage(
                summaryPrompt,
                entries,
                SUMMARY_DIRECT_COMPACT_MAX_MESSAGE_CHARS,
                SUMMARY_DIRECT_COMPACT_PROMPT_CHARS,
                SUMMARY_DIRECT_COMPACT_RESPONSE_CHARS
            );

            log.log(Level.INFO,
                "Retrying direct summary with compact payload after upstream status={0}",
                new Object[]{response == null ? -1 : response.statusCode()});

            response = sendChatHandled(
                targetUrl,
                apiKey,
                compactMessage,
                "chat",
                directSessionId,
                    false,
                Json.createArrayBuilder().build(),
                requestId + "-direct-compact"
            );
        }

        if (shouldRetryTinyDirectSummary(response)) {
            String tinyMessage = buildTinyDirectSummaryMessage(entries);

            log.log(Level.INFO,
                    "Retrying direct summary with tiny sanitized payload after upstream status={0}",
                    new Object[]{response == null ? -1 : response.statusCode()});

                response = sendChatHandled(
                    targetUrl,
                    apiKey,
                    tinyMessage,
                    "chat",
                    directSessionId,
                    false,
                    Json.createArrayBuilder().build(),
                    requestId + "-direct-tiny"
            );
        }

        if (shouldRetryIncrementalDirectSummary(response)) {
            log.log(Level.INFO,
                    "Retrying summary using incremental API session batches after upstream status={0}",
                    new Object[]{response == null ? -1 : response.statusCode()});
            response = runIncrementalSummaryChat(targetUrl, apiKey, entries, requestId);
        }

        return response;
    }

    private WorkspaceResponse runSinglePassSummaryChat(
            String targetUrl,
            String apiKey,
            String message,
            String requestId
    ) {
        if (workspaceClient == null) {
            throw new IllegalStateException("Workspace client unavailable for single-pass summary call.");
        }

        String sessionId = "summary-single-" + UUID.randomUUID();
        return sendChatBearerCompatHandled(
                targetUrl,
                apiKey,
                defaultIfBlank(message, "Summarize today's chats."),
                "chat",
                sessionId,
                true,
                Json.createArrayBuilder().build(),
                requestId + "-single"
        );
    }

    private boolean shouldRetryDirectSummaryFallback(WorkspaceResponse response) {
        if (response == null) {
            return true;
        }

        int status = response.statusCode();
        String reason = defaultString(extractPrimaryText(response.body())).toLowerCase(Locale.ROOT);

        if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN) {
            return false;
        }

        if (reason.contains("not a valid workspace") || reason.contains("no valid api key")) {
            return false;
        }

        if (reason.contains("cannot read properties of null") && reason.contains("reading 'id'")) {
            return true;
        }

        if (workspaceClient != null && workspaceClient.isLikelyContextTooLarge(response)) {
            return true;
        }

        return status == HttpServletResponse.SC_TOO_MANY_REQUESTS
                || status == HttpServletResponse.SC_GATEWAY_TIMEOUT
                || status == HttpServletResponse.SC_BAD_GATEWAY
                || status == HttpServletResponse.SC_SERVICE_UNAVAILABLE
                || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private List<SelectedEntry> limitSummaryEntriesForUpstream(List<SelectedEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        int limit = Math.min(clampSummaryMaxUpstreamEntries(summaryMaxUpstreamEntries), entries.size());
        if (limit == entries.size()) {
            return entries;
        }
        return new ArrayList<>(entries.subList(0, limit));
    }

    private SummaryPayloadPlan buildPerformanceSafeSummaryRequestMessage(
            String promptGuide,
            List<SelectedEntry> entries,
            int totalEntries
    ) {
        List<SelectedEntry> source = entries == null ? List.of() : entries;
        int candidateSize = source.size();
        boolean budgetReduced = false;
        int safeRequestBudgetBytes = clampSummaryMaxRequestBytes(summaryMaxRequestBytes);

        String message = buildGentleSummaryRequestMessage(promptGuide, source, totalEntries);
        int bytes = utf8Length(message);

        while (bytes > safeRequestBudgetBytes && candidateSize > 1) {
            budgetReduced = true;
            int shrinkBy = Math.max(1, candidateSize / 5);
            candidateSize = Math.max(1, candidateSize - shrinkBy);
            message = buildGentleSummaryRequestMessage(
                    promptGuide,
                    source.subList(0, candidateSize),
                    totalEntries
            );
            bytes = utf8Length(message);
        }

        if (bytes > safeRequestBudgetBytes) {
            budgetReduced = true;
            int compactMessageChars = Math.min(clampSummaryMaxMessageChars(summaryMaxMessageChars), 1400);
            int compactPromptChars = Math.min(SUMMARY_GENTLE_PROMPT_CHARS, 96);
            int compactResponseChars = Math.min(SUMMARY_GENTLE_RESPONSE_CHARS, 140);
            int compactGuidanceChars = Math.min(SUMMARY_GENTLE_GUIDANCE_CHARS, 220);

            message = buildGentleSummaryRequestMessage(
                    promptGuide,
                    source.subList(0, Math.max(1, candidateSize)),
                    totalEntries,
                    compactMessageChars,
                    compactPromptChars,
                    compactResponseChars,
                    compactGuidanceChars
            );
            bytes = utf8Length(message);
        }

        if (bytes > safeRequestBudgetBytes) {
            budgetReduced = true;
            message = trimToUtf8Bytes(message, safeRequestBudgetBytes);
            bytes = utf8Length(message);
        }

        int includedEntries = extractSummaryIncludedCount(message);
        if (includedEntries < 0) {
            includedEntries = Math.min(candidateSize, source.size());
        }

        return new SummaryPayloadPlan(message, includedEntries, bytes, budgetReduced);
    }

    private String buildGentleSummaryRequestMessage(String promptGuide, List<SelectedEntry> entries, int totalEntries) {
        return buildGentleSummaryRequestMessage(
            promptGuide,
            entries,
            totalEntries,
            clampSummaryMaxMessageChars(summaryMaxMessageChars),
            SUMMARY_GENTLE_PROMPT_CHARS,
            SUMMARY_GENTLE_RESPONSE_CHARS,
            SUMMARY_GENTLE_GUIDANCE_CHARS
        );
        }

        private String buildGentleSummaryRequestMessage(
            String promptGuide,
            List<SelectedEntry> entries,
            int totalEntries,
            int maxMessageChars,
            int promptChars,
            int responseChars,
            int guidanceChars
        ) {
        int safeMaxMessageChars = Math.max(600, maxMessageChars);
        int safePromptChars = Math.max(48, promptChars);
        int safeResponseChars = Math.max(72, responseChars);
        int safeGuidanceChars = Math.max(120, guidanceChars);

        StringBuilder message = new StringBuilder(safeMaxMessageChars);
        message.append("Create a concise daily chat summary.\n")
                .append("Return markdown with exactly these sections: ## Overall, ## Quality, ## Response, ## Usage.\n")
            .append("Include scoring: Overall effectiveness (0-100) and section scores for Quality, Response, Usage (0.0-5.0).\n")
                .append("Use only evidence in this request. If uncertain, say so briefly.\n");

        String safeGuide = normalizeSummarySnippet(promptGuide, safeGuidanceChars);
        if (!safeGuide.isBlank()) {
            message.append("\nGuidance:\n").append(safeGuide).append("\n");
        }

        int total = Math.max(0, totalEntries);
        int sent = entries == null ? 0 : entries.size();
        message.append("\nChat sample: ").append(sent).append(" of ").append(total).append(" newest chats.\n")
                .append("Evidence:\n");

        if (entries != null) {
            int included = 0;
            for (SelectedEntry entry : entries) {
                if (entry == null) {
                    continue;
                }

                String prompt = sanitizeTinySummarySnippet(entry.getPrompt(), safePromptChars);
                String answer = sanitizeTinySummarySnippet(entry.getResponse(), safeResponseChars);
                String line = "- q=" + defaultIfBlank(prompt, "(empty)")
                        + " | a=" + defaultIfBlank(answer, "(empty)")
                        + "\n";

                if (message.length() + line.length() > safeMaxMessageChars) {
                    break;
                }

                message.append(line);
                included++;
            }

            int omitted = Math.max(0, sent - included);
            message.append("coverage: included=").append(included)
                    .append(", omitted=").append(omitted)
                    .append(", totalSent=").append(sent)
                    .append("\n");
        }

        return message.toString();
    }

    private int extractSummaryIncludedCount(String message) {
        if (message == null || message.isBlank()) {
            return -1;
        }
        var matcher = SUMMARY_INCLUDED_COUNT_PATTERN.matcher(message);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Unable to parse included entry count from summary text.", ex);
            return -1;
        }
    }

    private int utf8Length(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private String trimToUtf8Bytes(String value, int maxBytes) {
        if (value == null || value.isBlank() || maxBytes <= 0) {
            return "";
        }
        if (utf8Length(value) <= maxBytes) {
            return value;
        }

        StringBuilder out = new StringBuilder(value.length());
        int used = 0;
        for (int i = 0; i < value.length();) {
            int codePoint = value.codePointAt(i);
            String cp = new String(Character.toChars(codePoint));
            int cpBytes = cp.getBytes(StandardCharsets.UTF_8).length;
            if (used + cpBytes > maxBytes) {
                break;
            }
            out.appendCodePoint(codePoint);
            used += cpBytes;
            i += Character.charCount(codePoint);
        }
        return out.toString();
    }

    private WorkspaceResponse runIncrementalSummaryChat(
            String targetUrl,
            String apiKey,
            List<SelectedEntry> entries,
            String requestId
    ) {
        if (workspaceClient == null) {
            throw new IllegalStateException("Workspace client unavailable for incremental summary fallback.");
        }

        String sessionId = "summary-stream-" + UUID.randomUUID();

        WorkspaceResponse initResp = sendChatHandled(
                targetUrl,
                apiKey,
                "You are preparing a daily chat summary. I will send chat batches. Reply only: OK.",
                "chat",
                sessionId,
            false,
                Json.createArrayBuilder().build(),
                requestId + "-direct-incremental-init"
        );
        if (initResp == null || initResp.statusCode() < 200 || initResp.statusCode() >= 300) {
            return initResp;
        }

        List<String> batches = buildIncrementalSummaryBatches(entries);
        int sendCount = Math.min(batches.size(), SUMMARY_INCREMENTAL_MAX_BATCHES);
        for (int i = 0; i < sendCount; i++) {
            String batchMessage = "Batch " + (i + 1) + " of " + sendCount + " for today's chats:\n"
                    + batches.get(i)
                    + "\nReply only: OK.";

                WorkspaceResponse batchResp = sendChatHandled(
                    targetUrl,
                    apiKey,
                    batchMessage,
                    "chat",
                    sessionId,
                    false,
                    Json.createArrayBuilder().build(),
                    requestId + "-direct-incremental-batch-" + (i + 1)
            );

            if (batchResp == null || batchResp.statusCode() < 200 || batchResp.statusCode() >= 300) {
                return batchResp;
            }
        }

        String finalPrompt = "Now produce the final daily summary in markdown using exactly these sections: "
            + "## Overall, ## Quality, ## Response, ## Usage. "
            + "Use only the batches I provided. Provide evidence-based analysis of what is working, "
            + "how well responses answered user intent, and concrete prioritized improvements and next steps. "
            + "Include scoring: Overall effectiveness (0-100) and section scores for Quality, Response, and Usage (0.0-5.0).";

        return sendChatHandled(
                targetUrl,
                apiKey,
                finalPrompt,
                "chat",
                sessionId,
                false,
                Json.createArrayBuilder().build(),
                requestId + "-direct-incremental-final"
        );
    }

    private List<String> buildIncrementalSummaryBatches(List<SelectedEntry> entries) {
        List<String> batches = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return batches;
        }

        StringBuilder current = new StringBuilder(SUMMARY_INCREMENTAL_BATCH_MAX_CHARS);

        for (SelectedEntry entry : entries) {
            if (entry == null) {
                continue;
            }

            String id = sanitizeTinySummarySnippet(entry.getChatId(), 64);
            if (id.isBlank()) {
                id = "unknown";
            }

            String prompt = sanitizeTinySummarySnippet(entry.getPrompt(), SUMMARY_INCREMENTAL_PROMPT_CHARS);
            String response = sanitizeTinySummarySnippet(entry.getResponse(), SUMMARY_INCREMENTAL_RESPONSE_CHARS);

            String line = "- id=" + id
                    + " | q=" + defaultIfBlank(prompt, "(empty)")
                    + " | a=" + defaultIfBlank(response, "(empty)")
                    + "\n";

            if (line.length() > SUMMARY_INCREMENTAL_BATCH_MAX_CHARS) {
                line = line.substring(0, SUMMARY_INCREMENTAL_BATCH_MAX_CHARS - 1) + "\n";
            }

            if (current.length() + line.length() > SUMMARY_INCREMENTAL_BATCH_MAX_CHARS) {
                if (current.length() > 0) {
                    batches.add(current.toString());
                }
                current.setLength(0);
            }

            current.append(line);
        }

        if (current.length() > 0) {
            batches.add(current.toString());
        }

        return batches;
    }

    private String buildTinyDirectSummaryMessage(List<SelectedEntry> entries) {
        StringBuilder message = new StringBuilder(SUMMARY_DIRECT_TINY_MAX_MESSAGE_CHARS);

        message.append("Summarize today's chats using only provided evidence.\n")
            .append("Return markdown sections exactly: ## Overall, ## Quality, ## Response, ## Usage.\n")
            .append("In each section, explain what is working, what is failing, and how to improve it.\n")
            .append("Include scoring: Overall effectiveness (0-100) and section scores for Quality, Response, and Usage (0.0-5.0).\n")
            .append("Include concrete next-step asks users should provide next time.\n\n")
                .append("Chats:\n");

        int total = entries == null ? 0 : entries.size();
        int included = 0;

        if (entries != null) {
            for (SelectedEntry entry : entries) {
                if (entry == null) {
                    continue;
                }

                String chatId = sanitizeTinySummarySnippet(entry.getChatId(), 64);
                if (chatId.isBlank()) {
                    chatId = "unknown";
                }

                String prompt = sanitizeTinySummarySnippet(entry.getPrompt(), SUMMARY_DIRECT_TINY_PROMPT_CHARS);
                String response = sanitizeTinySummarySnippet(entry.getResponse(), SUMMARY_DIRECT_TINY_RESPONSE_CHARS);

                String block = "- chat_id=" + chatId
                        + " | prompt=" + defaultIfBlank(prompt, "(empty)")
                        + " | response=" + defaultIfBlank(response, "(empty)")
                        + "\n";

                if (message.length() + block.length() > SUMMARY_DIRECT_TINY_MAX_MESSAGE_CHARS) {
                    break;
                }

                message.append(block);
                included++;
            }
        }

        int omitted = Math.max(0, total - included);
        message.append("included=").append(included)
                .append(", omitted=").append(omitted)
                .append(", total=").append(total)
                .append("\n");

        return message.toString();
    }

    private String buildDirectSummaryMessage(String summaryPrompt, List<SelectedEntry> entries) {
        return buildDirectSummaryMessage(
            summaryPrompt,
            entries,
            SUMMARY_DIRECT_MAX_MESSAGE_CHARS,
            SUMMARY_DIRECT_PROMPT_CHARS,
            SUMMARY_DIRECT_RESPONSE_CHARS
        );
        }

        private String buildDirectSummaryMessage(
            String summaryPrompt,
            List<SelectedEntry> entries,
            int maxMessageChars,
            int promptChars,
            int responseChars
        ) {
        int safeMaxMessageChars = Math.max(1200, maxMessageChars);
        int safePromptChars = Math.max(120, promptChars);
        int safeResponseChars = Math.max(220, responseChars);

        StringBuilder message = new StringBuilder(Math.max(1024, Math.min(safeMaxMessageChars, 8192)));

        message.append(defaultIfBlank(summaryPrompt, DEFAULT_SUMMARY_PROMPT).trim())
                .append("\n\nUse only the provided chats from today. Do not invent missing details.\n")
            .append("Return evidence-based markdown with the required sections, including concrete improvement recommendations and scoring.\n\n")
                .append("Today's chat evidence:\n");

        int included = 0;
        int total = entries == null ? 0 : entries.size();
        if (entries != null) {
            for (SelectedEntry entry : entries) {
                String block = formatDirectSummaryEntry(entry, included + 1, safePromptChars, safeResponseChars);
                if (message.length() + block.length() > safeMaxMessageChars) {
                    break;
                }
                message.append(block);
                included++;
            }
        }

        int omitted = Math.max(0, total - included);
        message.append("\nCoverage notes:\n")
                .append("- total_entries_collected_today: ").append(total).append("\n")
                .append("- entries_included_in_request: ").append(included).append("\n")
                .append("- entries_omitted_due_to_size: ").append(omitted).append("\n");

        return message.toString();
    }

    private String formatDirectSummaryEntry(SelectedEntry entry, int index) {
        return formatDirectSummaryEntry(entry, index, SUMMARY_DIRECT_PROMPT_CHARS, SUMMARY_DIRECT_RESPONSE_CHARS);
    }

    private String formatDirectSummaryEntry(SelectedEntry entry, int index, int promptChars, int responseChars) {
        if (entry == null) {
            return "";
        }

        String chatId = normalizeSummarySnippet(entry.getChatId(), 128);
        if (chatId.isBlank()) {
            chatId = "unknown";
        }

        String createdAt = normalizeSummarySnippet(entry.getCreatedAt(), 128);
        String sessionId = normalizeSummarySnippet(entry.getSessionId(), 128);
        String prompt = normalizeSummarySnippet(entry.getPrompt(), Math.max(120, promptChars));
        String response = normalizeSummarySnippet(entry.getResponse(), Math.max(220, responseChars));

        return "\n### Chat " + index + "\n"
                + "- chat_id: " + chatId + "\n"
                + "- created_at: " + defaultIfBlank(createdAt, "unknown") + "\n"
                + "- session_id: " + defaultIfBlank(sessionId, "unknown") + "\n"
                + "- prompt: " + defaultIfBlank(prompt, "(empty)") + "\n"
                + "- response: " + defaultIfBlank(response, "(empty)") + "\n";
    }

    private boolean shouldRetryCompactDirectSummary(WorkspaceResponse response) {
        if (response == null) {
            return true;
        }

        int status = response.statusCode();
        if (status == 413) {
            return true;
        }
        if (workspaceClient != null && workspaceClient.isLikelyContextTooLarge(response)) {
            return true;
        }
        return false;
    }

    private boolean shouldRetryTinyDirectSummary(WorkspaceResponse response) {
        if (response == null) {
            return true;
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return false;
        }

        if (workspaceClient != null && workspaceClient.isLikelyContextTooLarge(response)) {
            return true;
        }

        int status = response.statusCode();
        if (status == 413 || status == 429) {
            return true;
        }
        return status >= 500;
    }

    private boolean shouldRetryIncrementalDirectSummary(WorkspaceResponse response) {
        if (response == null) {
            return true;
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return false;
        }

        if (status == 413 || status == 429) {
            return true;
        }
        return status >= 500;
    }

    private String sanitizeTinySummarySnippet(String value, int maxChars) {
        String normalized = normalizeSummarySnippet(value, Math.max(64, maxChars * 2));
        if (normalized.isBlank()) {
            return "";
        }

        // Keep tiny fallback payload ASCII-safe to avoid upstream parser/WAF edge cases.
        normalized = normalized.replaceAll("[^\\x20-\\x7E]", " ")
                .replaceAll("[\\$\\{\\}<>`|]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars));
    }

    private String normalizeSummarySnippet(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        normalized = stripControlCharacters(normalized)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars));
    }

    private boolean shouldAttemptDirectSummaryFallback(WorkspaceResponse response) {
        if (response == null) {
            return true;
        }
        if (response.statusCode() >= 400) {
            // For generic 400 responses, skip direct fallback cascades and rely on
            // deterministic local summary fallback to avoid repetitive upstream noise.
            if (response.statusCode() == 400
                    && (workspaceClient == null || !workspaceClient.isLikelyContextTooLarge(response))) {
                return false;
            }
            return true;
        }
        if (isAbortResponse(response.body())) {
            return true;
        }
        String primaryText = extractPrimaryText(response.body());
        return primaryText == null || primaryText.isBlank();
    }

    private boolean isUsableSummaryResponse(WorkspaceResponse response) {
        if (response == null) {
            return false;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return false;
        }
        String primaryText = extractPrimaryText(response.body());
        return primaryText != null && !primaryText.isBlank() && !isAbortResponse(response.body());
    }

    private boolean isAbortResponse(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject payload = reader.readObject();
            String type = payload.getString("type", "");
            if (!"abort".equalsIgnoreCase(type.trim())) {
                return false;
            }
            String text = payload.getString("textResponse", "");
            return text.isBlank();
        } catch (JsonException | ClassCastException ex) {
            log.log(Level.FINE, "Unable to parse abort response payload.", ex);
            return false;
        }
    }

    private String summarizeBodyForDiagnostics(String body) {
        String safeBody = stripControlCharacters(defaultString(body));
        if (safeBody.length() <= SUMMARY_DIAGNOSTIC_BODY_CHARS) {
            return safeBody;
        }
        return safeBody.substring(0, SUMMARY_DIAGNOSTIC_BODY_CHARS) + "...(truncated)";
    }

    private boolean isSummaryTargetFromConfiguredServer(String targetUrl, ServerConfig config) {
        if (targetUrl == null || targetUrl.isBlank() || config == null) {
            return false;
        }

        String configuredBase = sanitizeBaseUrl(buildBaseUrl(config));
        if (configuredBase == null || configuredBase.isBlank()) {
            return false;
        }

        try {
            URI target = URI.create(targetUrl.trim());
            URI base = URI.create(configuredBase.trim());

            String targetHost = target.getHost();
            String baseHost = base.getHost();
            if (targetHost == null || baseHost == null) {
                return false;
            }

            if (!targetHost.equalsIgnoreCase(baseHost)) {
                return false;
            }

            int targetPort = effectivePort(target);
            int basePort = effectivePort(base);
            return targetPort == basePort;
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Unable to compare summary target against configured server", ex);
            return false;
        }
    }

    private int effectivePort(URI uri) {
        if (uri == null) {
            return -1;
        }

        int explicitPort = uri.getPort();
        if (explicitPort >= 0) {
            return explicitPort;
        }

        String scheme = uri.getScheme();
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        return -1;
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

    private boolean isSummaryRunDueNow() {
        if (summaryIntervalSeconds <= 0L) {
            return true;
        }
        Timestamp lastRun = summaryLastRun;
        if (lastRun == null) {
            return true;
        }
        Instant threshold = lastRun.toInstant().plusSeconds(summaryIntervalSeconds);
        return !Instant.now().isBefore(threshold);
    }

    private String computeNextSummaryRunAtIso() {
        Timestamp lastRun = summaryLastRun;
        if (lastRun == null || summaryIntervalSeconds <= 0L) {
            return "";
        }
        return lastRun.toInstant().plusSeconds(summaryIntervalSeconds).toString();
    }

    private long clampSummaryIntervalSeconds(long value) {
        if (value < MIN_SUMMARY_INTERVAL_SECONDS) {
            return MIN_SUMMARY_INTERVAL_SECONDS;
        }
        return Math.min(value, MAX_SUMMARY_INTERVAL_SECONDS);
    }

    private int clampSummaryMaxRows(int value) {
        if (value < MIN_SUMMARY_MAX_ROWS) {
            return MIN_SUMMARY_MAX_ROWS;
        }
        return Math.min(value, MAX_SUMMARY_MAX_ROWS);
    }

    private int clampSummaryMaxUpstreamEntries(int value) {
        if (value < MIN_SUMMARY_MAX_UPSTREAM_ENTRIES) {
            return MIN_SUMMARY_MAX_UPSTREAM_ENTRIES;
        }
        return Math.min(value, MAX_SUMMARY_MAX_UPSTREAM_ENTRIES);
    }

    private int clampSummaryMaxMessageChars(int value) {
        if (value < MIN_SUMMARY_MAX_MESSAGE_CHARS) {
            return MIN_SUMMARY_MAX_MESSAGE_CHARS;
        }
        return Math.min(value, MAX_SUMMARY_MAX_MESSAGE_CHARS);
    }

    private int clampSummaryMaxRequestBytes(int value) {
        if (value < MIN_SUMMARY_MAX_REQUEST_BYTES) {
            return MIN_SUMMARY_MAX_REQUEST_BYTES;
        }
        return Math.min(value, MAX_SUMMARY_MAX_REQUEST_BYTES);
    }

    private String resolveSummaryPrompt() {
        return normalizeSummaryPrompt(summaryPromptTemplate);
    }

    private String normalizeSummaryPrompt(String prompt) {
        String normalized = prompt == null ? "" : prompt.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalized.isBlank()) {
            normalized = DEFAULT_SUMMARY_PROMPT;
        }
        if (isLegacyDefaultSummaryPrompt(normalized)) {
            normalized = DEFAULT_SUMMARY_PROMPT;
        }
        if (normalized.length() > MAX_SUMMARY_PROMPT_CHARS) {
            normalized = normalized.substring(0, MAX_SUMMARY_PROMPT_CHARS);
        }
        return normalized;
    }

    private boolean isLegacyDefaultSummaryPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false;
        }
        String normalizedPrompt = prompt.replace("\r\n", "\n").replace('\r', '\n').trim();
        String normalizedLegacy = LEGACY_DEFAULT_SUMMARY_PROMPT.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalizedLegacy.equals(normalizedPrompt)) {
            return true;
        }
        String normalizedPrevious = PREVIOUS_DEFAULT_SUMMARY_PROMPT.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalizedPrevious.equals(normalizedPrompt)) {
            return true;
        }
        String normalizedPreviousV2 = PREVIOUS_DEFAULT_SUMMARY_PROMPT_V2.replace("\r\n", "\n").replace('\r', '\n').trim();
        return normalizedPreviousV2.equals(normalizedPrompt);
    }

    private String readMultilineParam(HttpServletRequest req, String name, int maxLen) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String value = ServletRequestParamUtil.firstParam(
                req,
                name,
                Math.max(maxLen, MAX_SUMMARY_PROMPT_CHARS),
                true,
                false);
        if (value == null) {
            return null;
        }
        String normalized = sanitizeConfigToken(value, Math.max(maxLen, MAX_SUMMARY_PROMPT_CHARS))
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
        if (maxLen > 0 && normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }
        return normalized;
    }

    private boolean isHttpsRequiredWithAuth() {
        String env = readEnvSanitized(REQUIRE_HTTPS_WITH_AUTH_ENV, 16);
        if (env == null || env.isBlank()) {
            return false;
        }
        return Boolean.parseBoolean(env.trim());
    }

    private boolean isUpstreamSummaryRequired() {
        String env = readEnvSanitized(REQUIRE_UPSTREAM_SUMMARY_ENV, 16);
        if (env == null || env.isBlank()) {
            // Default to strict mode so summaries come from AnythingLLM unless explicitly relaxed.
            return true;
        }
        return Boolean.parseBoolean(env.trim());
    }

    private long readPersistedIntervalSeconds(ResultSet rs, String columnName, long fallback) {
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

    private int readPersistedInt(ResultSet rs, String columnName, int fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 32).trim();
        if (trimmed.isBlank() || !trimmed.matches("^-?\\d{1,10}$")) {
            return fallback;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid persisted integer value", ex);
            return fallback;
        }
    }

    private boolean readPersistedBoolean(ResultSet rs, String columnName, boolean fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 16).trim();
        if (trimmed.isBlank()) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(trimmed) || "t".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed) || "f".equalsIgnoreCase(trimmed) || "0".equals(trimmed)) {
            return false;
        }
        return fallback;
    }

    private Timestamp readDbTimestamp(ResultSet rs, String columnName) {
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

    private String readDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return "";
        }
        String text;
        try {
            text = rs.getString(columnName);
        } catch (SQLException ex) {
            log.log(Level.FINE, "ResultSet#getString failed for column " + columnName, ex);
            text = null;
        }
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
        if (name == null || name.isBlank()) {
            return null;
        }
        if (req == null) {
            return null;
        }
        String value = ServletRequestParamUtil.firstParam(req, name, 256, false, false);
        if (value == null) {
            return null;
        }
        String trimmed = sanitizeConfigToken(value, 256);
        if (trimmed == null) {
            return null;
        }
        trimmed = trimmed.replace("\r", "").replace("\n", "").trim();
        return trimmed.length() > 256 ? trimmed.substring(0, 256) : trimmed;
    }

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    void setDataSourceHolder(AppDataSourceHolder dsHolder) {
        WidgetSyncServlet.dsHolder = dsHolder;
    }

    private WorkspaceResponse sendChatHandled(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean allowLegacy,
            jakarta.json.JsonArray priorMessages,
            String requestId
    ) {
        try {
            return workspaceClient.sendChat(
                    targetUrl,
                    apiKey,
                    message,
                    mode,
                    sessionId,
                    allowLegacy,
                    priorMessages,
                    requestId
            );
        } catch (IOException e) {
            throw new IllegalStateException("Workspace chat request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Workspace chat request interrupted", e);
        }
    }

    private WorkspaceResponse sendChatBearerCompatHandled(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean allowLegacy,
            jakarta.json.JsonArray priorMessages,
            String requestId
    ) {
        try {
            return workspaceClient.sendChatBearerCompat(
                    targetUrl,
                    apiKey,
                    message,
                    mode,
                    sessionId,
                    allowLegacy,
                    priorMessages,
                    requestId
            );
        } catch (IOException e) {
            throw new IllegalStateException("Workspace chat request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Workspace chat request interrupted", e);
        }
    }

    private TermsStore termsStore() {
        if (termsStore != null) {
            return termsStore;
        }
        return CDI.current().select(TermsStore.class).get();
    }

    void setTermsStore(TermsStore termsStore) {
        this.termsStore = termsStore;
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

    private static String readSystemPropertySanitized(String propertyName, int maxLen) {
        if (propertyName == null || propertyName.isBlank()) {
            return null;
        }
        String mappedEnv = propertyName.toUpperCase(Locale.ROOT).replace('.', '_');
        return readEnvSanitized(mappedEnv, maxLen);
    }

    private static String readEnvSanitized(String envName, int maxLen) {
        if (envName == null || envName.isBlank()) {
            return null;
        }
        String raw = System.getenv().get(envName);
        return sanitizeConfigToken(raw, maxLen);
    }

    private static String sanitizeConfigToken(String raw, int maxLen) {
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        StringBuilder sanitized = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || !Character.isISOControl(c)) {
                sanitized.append(c);
            }
        }
        String value = sanitized.toString().trim();
        if (maxLen > 0 && value.length() > maxLen) {
            return value.substring(0, maxLen);
        }
        return value;
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

        final long intervalSeconds;
        final Timestamp lastSynced;
        final long summaryIntervalSeconds;
        final boolean summaryAutoEnabled;
        final String summaryPrompt;
        final int summaryMaxRows;
        final int summaryMaxUpstreamEntries;
        final int summaryMaxMessageChars;
        final int summaryMaxRequestBytes;
        final Timestamp summaryLastRun;

        private SyncSettings(
                long intervalSeconds,
                Timestamp lastSynced,
                long summaryIntervalSeconds,
                boolean summaryAutoEnabled,
                String summaryPrompt,
                int summaryMaxRows,
                int summaryMaxUpstreamEntries,
                int summaryMaxMessageChars,
                int summaryMaxRequestBytes,
                Timestamp summaryLastRun
        ) {
            this.intervalSeconds = intervalSeconds;
            this.lastSynced = lastSynced;
            this.summaryIntervalSeconds = summaryIntervalSeconds;
            this.summaryAutoEnabled = summaryAutoEnabled;
            this.summaryPrompt = summaryPrompt;
            this.summaryMaxRows = summaryMaxRows;
            this.summaryMaxUpstreamEntries = summaryMaxUpstreamEntries;
            this.summaryMaxMessageChars = summaryMaxMessageChars;
            this.summaryMaxRequestBytes = summaryMaxRequestBytes;
            this.summaryLastRun = summaryLastRun;
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
