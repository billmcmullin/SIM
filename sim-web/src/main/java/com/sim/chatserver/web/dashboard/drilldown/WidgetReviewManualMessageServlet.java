// src/main/java/com/sim/chatserver/web/dashboard/drilldown/WidgetReviewManualMessageServlet.java
package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.MapReduceConfig;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.CoverageSummary;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.security.review.ReviewOutputValidator;
import com.sim.chatserver.security.review.TrustedUrlValidator;
import com.sim.chatserver.service.PromptTemplateService;
import com.sim.chatserver.service.ReviewContextBuilderService;
import com.sim.chatserver.service.ReviewJobService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewManualMessageServlet", urlPatterns = {"/dashboard/drilldown/widget-review/manual-message"})
public class WidgetReviewManualMessageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewManualMessageServlet.class.getName());
    private static final String CHAT_API_PATH_TEMPLATE = "/api/v1/workspace/%s/chat";

    private static final int MAX_CONTEXT_ENTRIES_HARD_CAP = Integer.MAX_VALUE;
    private static final int MAX_SESSION_ID_CHARS = 200;
    private static final int MAX_JSON_PAYLOAD_BYTES = 128 * 1024;
    private static final Set<String> ALLOWED_MODES = Set.of("chat", "query", "automatic");
    private static final Object INIT_LOCK = new Object();
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    private static volatile HttpClient httpClient;
    private static volatile MapReduceConfig mrConfig;
    private static volatile WorkspaceClient workspaceClient;
    private static volatile WidgetReviewMapReduceOrchestrator orchestrator;
    private static volatile PromptTemplateService promptTemplateService;
    private static volatile ReviewContextBuilderService reviewContextBuilderService;
    private static volatile ReviewOutputValidator reviewOutputValidator;
    private static volatile TrustedUrlValidator trustedUrlValidator;

    AppDataSourceHolder dsHolder;

    @Override
    public void init() throws ServletException {
        super.init();
        synchronized (INIT_LOCK) {
            if (mrConfig != null && orchestrator != null && trustedUrlValidator != null && workspaceClient != null) {
            return;
            }

            MapReduceConfig loadedConfig = MapReduceConfig.load();
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

            WorkspaceClient configuredWorkspaceClient = new WorkspaceClient(
                client,
                loadedConfig.getWorkspaceMaxRetries(),
                loadedConfig.getWorkspaceTimeout()
            );

            PromptTemplateService configuredPromptTemplateService = new PromptTemplateService();
            ReviewContextBuilderService configuredReviewContextBuilderService = new ReviewContextBuilderService();
            ReviewOutputValidator configuredReviewOutputValidator = new ReviewOutputValidator();

            Set<String> allowedHosts = parseCsvToSet(ENV.get("REVIEW_TRUSTED_HOSTS"));
            Set<String> allowedSuffixes = parseCsvToSet(ENV.get("REVIEW_TRUSTED_HOST_SUFFIXES"));
            boolean allowPrivate = Boolean.parseBoolean(defaultIfBlank(ENV.get("REVIEW_ALLOW_PRIVATE_NETWORKS"), "false"));
            TrustedUrlValidator configuredTrustedUrlValidator = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivate);

            WidgetReviewMapReduceOrchestrator configuredOrchestrator = new WidgetReviewMapReduceOrchestrator(
                configuredWorkspaceClient,
                configuredReviewContextBuilderService,
                configuredPromptTemplateService,
                configuredReviewOutputValidator,
                loadedConfig.getBatchSize(),
                loadedConfig.getMaxParallel(),
                loadedConfig.getMapMessageMaxChars(),
                loadedConfig.getMapContextMaxChars(),
                loadedConfig.getReduceMessageMaxChars(),
                loadedConfig.getReduceContextMaxChars(),
                loadedConfig.getRetryContextChars(),
                loadedConfig.getRetryMessageMaxChars(),
                loadedConfig.getMaxCoveragePasses(),
                loadedConfig.getMinBatchSize(),
                loadedConfig.getSegmentPromptChars(),
                loadedConfig.getSegmentResponseChars(),
                loadedConfig.getReduceInitialChunkSize(),
                loadedConfig.getReduceMinChunkSize(),
                loadedConfig.getReduceMaxLevels(),
                loadedConfig.getReduceChunkSummaryMaxChars(),
                loadedConfig.getFinalReduceMaxSummaries(),
                loadedConfig.getFinalReduceSummaryMaxChars(),
                loadedConfig.getFinalReduceMaxAttempts()
            );

            mrConfig = loadedConfig;
            httpClient = client;
            workspaceClient = configuredWorkspaceClient;
            promptTemplateService = configuredPromptTemplateService;
            reviewContextBuilderService = configuredReviewContextBuilderService;
            reviewOutputValidator = configuredReviewOutputValidator;
            trustedUrlValidator = configuredTrustedUrlValidator;
            orchestrator = configuredOrchestrator;
        }

        log.log(Level.INFO, "[manual-message][init] loaded config: {0}", mrConfig);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        final String requestId = UUID.randomUUID().toString();
        final long startMs = System.currentTimeMillis();

        if (!isLoggedIn(req, resp)) {
            return;
        }

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonObject payload;
        try (var reader = Json.createReader(new StringReader(readRequestBody(req)))) {
            payload = reader.readObject();
        } catch (JsonException | ClassCastException ex) {
            log.log(Level.FINE, "Invalid manual-message payload", ex);
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String userMessage = payload.getString("message", "").trim();
        if (userMessage.isEmpty()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "message is required.");
            return;
        }
        userMessage = stripClientInjectedContext(userMessage);

        String mode = payload.getString("mode", "chat").trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_MODES.contains(mode)) {
            mode = "chat";
        }

        String sessionId = payload.getString("sessionId", "").trim();
        if (sessionId.length() > MAX_SESSION_ID_CHARS) {
            sessionId = sessionId.substring(0, MAX_SESSION_ID_CHARS);
        }

        boolean requestReset = payload.getBoolean("requestReset", payload.getBoolean("reset", false));
        boolean async = payload.getBoolean("async", false);

        List<SelectedEntry> selectedEntries = parseSelectedEntries(payload);
        JsonArray normalizedAttachments = normalizeAttachments(payload);

        log.log(Level.INFO, "[manual-message][{0}] selectedEntriesParsed={1}", new Object[]{requestId, selectedEntries.size()});

        EncryptedDbConfigStore.setAppDataSourceHolder(dataSourceHolder());
        ServerConfig config;
        try {
            config = EncryptedDbConfigStore.load();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.SEVERE, "[manual-message][" + requestId + "] Unable to load server configuration", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration not available.");
            return;
        }

        if (config == null) {
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration missing.");
            return;
        }

        String workspaceSlug = buildSlug(config.getWorkspaceName());
        if (workspaceSlug == null || workspaceSlug.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace slug not configured.");
            return;
        }

        String baseUrl = sanitizeBaseUrl(buildBaseUrl(config));
        if (baseUrl == null || baseUrl.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Server connection information is incomplete.");
            return;
        }

        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "API key not configured.");
            return;
        }

        String encodedSlug = URLEncoder.encode(workspaceSlug, StandardCharsets.UTF_8);
        String targetUrl = stripTrailingSlash(baseUrl) + String.format(CHAT_API_PATH_TEMPLATE, encodedSlug);
        targetUrl = canonicalizeForValidation(targetUrl);

        TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(targetUrl);
        if (!trust.isValid()) {
            log.log(Level.WARNING, "[manual-message][{0}] blocked untrusted targetUrl reason={1}", new Object[]{requestId, trust.getReason()});
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace URL failed trust validation.");
            return;
        }

        try {
            if (async) {
                handleAsyncSubmission(
                        resp, targetUrl, apiKey, userMessage, mode, sessionId, requestReset,
                        normalizedAttachments, selectedEntries, requestId
                );
                return;
            }

            boolean useMapReduce = shouldUseMapReduce(selectedEntries);

            WorkspaceResponse upstream = useMapReduce
                    ? runMapReduce(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, normalizedAttachments, selectedEntries, requestId).response
                    : runSinglePass(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, normalizedAttachments, selectedEntries, requestId);

            mirrorWorkspaceResponse(resp, upstream);

                String completionLog = "[manual-message][" + requestId + "] completed"
                        + " status=" + upstream.statusCode()
                        + " latencyMs=" + (System.currentTimeMillis() - startMs)
                        + " mode=" + mode
                        + " selected=" + selectedEntries.size()
                        + " strategy=" + (useMapReduce ? "map-reduce-orchestrator" : "single-pass");
                log.info(completionLog);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.SEVERE, "[manual-message][" + requestId + "] execution failed", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process manual message.");
        }
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean shouldUseMapReduce(List<SelectedEntry> selectedEntries) {
        int n = selectedEntries == null ? 0 : selectedEntries.size();

        if (mrConfig.isExhaustiveMode()) {
            boolean use = n > 0;
                log.log(
                    Level.INFO,
                    "[manual-message][strategy] exhaustiveMode=true selected={0} singlePassMaxSelected={1} -> useMapReduce={2}",
                    new Object[]{n, mrConfig.getSinglePassMaxSelected(), use}
                );
            return use;
        }

        boolean use = n > mrConfig.getSinglePassMaxSelected();
            log.log(
                Level.INFO,
                "[manual-message][strategy] exhaustiveMode=false selected={0} singlePassMaxSelected={1} -> useMapReduce={2}",
                new Object[]{n, mrConfig.getSinglePassMaxSelected(), use}
            );
        return use;
    }

    private void handleAsyncSubmission(
            HttpServletResponse resp,
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId
    ) throws IOException {

        ReviewJobService jobs = WidgetReviewJobStatusServlet.jobService();

        String jobId = jobs.submit(requestId, selectedEntries.size(), (jid) -> {
            try {
                List<String> allIds = distinctIds(extractAllIds(selectedEntries));

                jobs.updateMapProgress(
                        jid, 0, 0, 0, 0,
                        allIds, List.of(), allIds, List.of(), "Starting"
                );

                boolean useMapReduce = shouldUseMapReduce(selectedEntries);

                if (!useMapReduce) {
                    WorkspaceResponse upstream = runSinglePass(
                            targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId
                    );

                    int httpStatus = upstream == null ? 500 : upstream.statusCode();
                    String body = upstream == null ? "" : upstream.body();
                    String contentType = upstream == null ? "application/json" : defaultIfBlank(upstream.contentType(), "application/json");
                    String finalReport = extractPrimaryText(body);
                        String finalReportForValidation = canonicalizeForValidation(finalReport);

                        List<String> usedIdsFromText = extractUsedIdsFromText(finalReportForValidation, allIds);
                    List<String> usedIds = !usedIdsFromText.isEmpty() ? usedIdsFromText : List.of();
                    List<String> missingIds = subtract(allIds, usedIds);

                    ReviewOutputValidator.ValidationResult finalValidation
                            = reviewOutputValidator.validateFinalReportHierarchical(finalReportForValidation, allIds, mrConfig.getReduceMessageMaxChars());
                    boolean metadataMismatch = hasCoverageMetadataMismatch(finalValidation);

                    boolean coverageComplete = missingIds.isEmpty() && !metadataMismatch;
                    boolean httpOk = upstream != null && httpStatus < 400;
                    boolean ok = httpOk && coverageComplete;

                    String completionMsg = ok
                            ? "Completed"
                            : (httpOk
                                ? ("Completed with partial coverage (missing=" + missingIds.size() + ')')
                                : ("Completed with upstream errors (status=" + httpStatus + ')'));

                    String errMsg = ok ? ""
                            : (!httpOk
                                    ? "Upstream returned status " + httpStatus
                                    : (metadataMismatch ? "Coverage metadata mismatch." : "Coverage incomplete."));

                    List<String> warnings = new ArrayList<>();
                    if (!coverageComplete) {
                        warnings.add("coverage incomplete");
                    }
                    if (metadataMismatch) {
                        warnings.add("coverage metadata mismatch");
                    }

                    jobs.updateReduceProgress(
                            jid, 1, 1, ok ? 0 : 1, 0,
                            allIds, usedIds, missingIds, List.of(), completionMsg
                    );

                    return new ReviewJobService.JobResult(
                            httpStatus, ok, completionMsg, errMsg,
                            1, 1, ok ? 0 : 1, 0,
                            allIds, usedIds, missingIds, List.of(), warnings,
                            finalReport, body, contentType
                    );
                }

                AtomicInteger liveTotalBatches = new AtomicInteger(0);
                AtomicInteger liveCompletedBatches = new AtomicInteger(0);
                AtomicInteger liveFailedBatches = new AtomicInteger(0);

                final int reduceFinalAttemptTotal = mrConfig.getFinalReduceMaxAttempts();

                WidgetReviewMapReduceOrchestrator.ProgressListener progressListener = new WidgetReviewMapReduceOrchestrator.ProgressListener() {
                    @Override
                    public void onMapRoundStarted(String reqId, int round, int totalRounds, int remainingBeforeRound, int entriesInRound) {
                        jobs.updateMapProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), allIds, List.of(),
                                "Map round " + round + '/' + totalRounds + " started â€¢ remaining=" + remainingBeforeRound
                        );
                    }

                    @Override
                    public void onMapBatchStarted(String reqId, int batchIndex, int totalBatchesSoFar, int expectedIdsInBatch, int round) {
                        liveTotalBatches.set(Math.max(liveTotalBatches.get(), totalBatchesSoFar));
                        jobs.updateMapProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), allIds, List.of(),
                                "Sending batch " + batchIndex + " (round " + round + ", size=" + expectedIdsInBatch + ")..."
                        );
                    }

                    @Override
                    public void onMapBatchCompleted(String reqId, int batchIndex, int totalBatchesSoFar, boolean success, int usedSoFar, int missingSoFar, int round) {
                        liveTotalBatches.set(Math.max(liveTotalBatches.get(), totalBatchesSoFar));
                        if (success) {
                            liveCompletedBatches.incrementAndGet(); 
                        }else {
                            liveFailedBatches.incrementAndGet();
                        }

                        jobs.updateMapProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                "Batch " + batchIndex + ' ' + (success ? "completed" : "failed")
                                + " â€¢ " + liveCompletedBatches.get() + '/' + Math.max(1, liveTotalBatches.get())
                                + " complete"
                        );
                    }

                    @Override
                    public void onReduceStarted(String reqId, int totalSelected, int totalBatches, int mapOutputsCount, int missingCount) {
                        liveTotalBatches.set(Math.max(liveTotalBatches.get(), totalBatches));
                        jobs.updateReduceProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                "Synthesizing final report..."
                        );
                    }

                    @Override
                    public void onReduceChunkStarted(String reqId, int level, int chunkIndex, int totalChunksAtLevel, int chunkSize, int currentChunkSizeConfig) {
                        if (level == 999) {
                            jobs.updateReduceProgress(
                                    jid,
                                    Math.max(liveTotalBatches.get(), 0),
                                    Math.max(liveCompletedBatches.get(), 0),
                                    Math.max(liveFailedBatches.get(), 0),
                                    0,
                                    allIds, List.of(), List.of(), List.of(),
                                    "Final synthesis attempt " + chunkIndex + '/' + reduceFinalAttemptTotal + " â€¢ summaries=" + chunkSize
                            );
                            return;
                        }

                        jobs.updateReduceProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                "Synthesis L" + level + " â€¢ chunk " + chunkIndex + '/' + totalChunksAtLevel
                                + " â€¢ size=" + chunkSize + " â€¢ cfg=" + currentChunkSizeConfig
                        );
                    }

                    @Override
                    public void onReduceChunkCompleted(String reqId, int level, int chunkIndex, int totalChunksAtLevel, boolean success, int httpStatus) {
                        String phaseText = (level == 999)
                                ? ("Final synthesis attempt " + chunkIndex + '/' + reduceFinalAttemptTotal)
                                : ("Synthesis L" + level + " chunk " + chunkIndex + '/' + totalChunksAtLevel);

                        jobs.updateReduceProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                phaseText + ' ' + (success ? "completed" : "failed") + " (HTTP " + httpStatus + ')'
                        );
                    }

                    @Override
                    public void onReduceLevelCompleted(String reqId, int level, int totalChunksAtLevel, int producedSummaries) {
                        jobs.updateReduceProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                "Synthesis level " + level + " complete â€¢ chunks=" + totalChunksAtLevel + " â€¢ summaries=" + producedSummaries
                        );
                    }

                    @Override
                    public void onReduceCompleted(String reqId, boolean success, int httpStatus, int missingCount) {
                        jobs.updateReduceProgress(
                                jid,
                                Math.max(liveTotalBatches.get(), 0),
                                Math.max(liveCompletedBatches.get(), 0),
                                Math.max(liveFailedBatches.get(), 0),
                                0,
                                allIds, List.of(), List.of(), List.of(),
                                "Reduce " + (success ? "completed" : "failed") + " (status=" + httpStatus + ')'
                        );
                    }
                };

                MapReduceExecutionResult mr = runMapReduce(
                        targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progressListener
                );

                WorkspaceResponse upstream = mr.response;
                WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration = mr.orchestration;
                ReduceResult reduceResult = orchestration == null ? null : orchestration.reduceResult();

                int httpStatus = upstream == null ? 500 : upstream.statusCode();
                String body = upstream == null ? "" : upstream.body();
                String contentType = upstream == null ? "application/json" : defaultIfBlank(upstream.contentType(), "application/json");
                String finalReport = extractPrimaryText(body);
                String finalReportForValidation = canonicalizeForValidation(finalReport);

                List<String> missingIds = reduceResult != null
                        ? distinctIds(reduceResult.getMissingChatIds())
                        : distinctIds(orchestration == null ? allIds : orchestration.missingChatIds());

                missingIds = intersect(allIds, missingIds);
                List<String> usedIds = subtract(allIds, missingIds);

                ReviewOutputValidator.ValidationResult finalValidation
                    = reviewOutputValidator.validateFinalReportHierarchical(finalReportForValidation, allIds, mrConfig.getReduceMessageMaxChars());
                boolean metadataMismatch = hasCoverageMetadataMismatch(finalValidation);

                boolean coverageComplete = missingIds.isEmpty() && !metadataMismatch;
                boolean httpOk = upstream != null && httpStatus < 400;
                boolean ok = httpOk && coverageComplete;

                String completionMsg = ok
                        ? "Completed"
                        : (httpOk
                        ? ("Completed with partial coverage (missing=" + missingIds.size() + ')')
                        : ("Completed with upstream errors (status=" + httpStatus + ')'));

                String errMsg = ok ? ""
                        : (!httpOk
                                ? "Upstream returned status " + httpStatus
                                : (metadataMismatch ? "Coverage metadata mismatch." : "Coverage incomplete."));

                List<String> warnings = new ArrayList<>();
                if (!coverageComplete) {
                    warnings.add("coverage incomplete");
                }
                if (metadataMismatch) {
                    warnings.add("coverage metadata mismatch");
                }

                jobs.updateReduceProgress(
                        jid,
                        orchestration == null ? 0 : orchestration.totalBatches(),
                        orchestration == null ? 0 : (orchestration.totalBatches() - orchestration.failedBatchIndexes().size()),
                        orchestration == null ? 1 : orchestration.failedBatchIndexes().size(),
                        0,
                        allIds, usedIds, missingIds,
                        orchestration == null ? List.of() : orchestration.failedBatchIndexes(),
                        completionMsg
                );

                return new ReviewJobService.JobResult(
                        httpStatus, ok, completionMsg, errMsg,
                        orchestration == null ? 0 : orchestration.totalBatches(),
                        orchestration == null ? 0 : (orchestration.totalBatches() - orchestration.failedBatchIndexes().size()),
                        orchestration == null ? 1 : orchestration.failedBatchIndexes().size(),
                        0,
                        allIds, usedIds, missingIds,
                        orchestration == null ? List.of() : orchestration.failedBatchIndexes(),
                        warnings,
                        finalReport, body, contentType
                );
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.log(Level.WARNING, "[manual-message][" + requestId + "] async execution failed", e);
                return new ReviewJobService.JobResult(
                        500, false, "Failed",
                        e.getMessage() == null ? "Async execution failed." : e.getMessage(),
                        0, 0, 1, 0,
                        List.of(), List.of(), List.of(),
                        List.of(), List.of(),
                        "", "", "application/json"
                );
            }
        });

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(Json.createObjectBuilder()
                .add("status", "accepted")
                .add("requestId", requestId)
                .add("jobId", jobId)
                .build()
                .toString());
    }

    private WorkspaceResponse runSinglePass(
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId
    ) throws IOException, InterruptedException {
        String controlledPrompt = promptTemplateService.buildControlledPrompt(userMessage, true, false, true);
        String deterministicHeader = "Deterministic metadata (use exactly; do not estimate):\n"
            + "- exact_total_selected: " + selectedEntries.size() + '\n'
            + "- execution_mode: single-pass\n";

        String promptWithMeta = controlledPrompt + "\n\n" + deterministicHeader;
        String context = reviewContextBuilderService.buildContext(promptWithMeta, selectedEntries, mrConfig.getSinglePassContextMaxChars());
        String outbound = buildOutboundMessage(promptWithMeta, context, mrConfig.getSinglePassMessageMaxChars());

        WorkspaceResponse response = workspaceClient.sendChat(
                targetUrl, apiKey, outbound, mode, sessionId, requestReset, attachments, requestId
        );

        if (workspaceClient.isLikelyContextTooLarge(response)) {
            String retryContext = reviewContextBuilderService.buildContext(promptWithMeta, selectedEntries, mrConfig.getRetryContextChars());
            String retryMsg = buildOutboundMessage(promptWithMeta, retryContext, mrConfig.getRetryMessageMaxChars());
            response = workspaceClient.sendChat(
                    targetUrl, apiKey, retryMsg, mode, sessionId, true, attachments, requestId
            );
        }

        String text = extractPrimaryText(response.body());
        String textForValidation = canonicalizeForValidation(text);
        ReviewOutputValidator.ValidationResult validation = reviewOutputValidator.validateFinalReport(textForValidation, mrConfig.getSinglePassMessageMaxChars());
        if (!validation.isValid()) {
            log.log(Level.WARNING, "[manual-message][{0}][single-pass] final validation errors={1}", new Object[]{requestId, validation.getErrors()});
        } else if (!validation.getWarnings().isEmpty()) {
            log.log(Level.INFO, "[manual-message][{0}][single-pass] final validation warnings={1}", new Object[]{requestId, validation.getWarnings()});
        }

        return response;
    }

    private MapReduceExecutionResult runMapReduce(
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId
    ) throws IOException, InterruptedException {
        return runMapReduce(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, WidgetReviewMapReduceOrchestrator.NOOP_PROGRESS_LISTENER);
    }

    private MapReduceExecutionResult runMapReduce(
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId,
            WidgetReviewMapReduceOrchestrator.ProgressListener progressListener
    ) throws IOException, InterruptedException {

        WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration =
            WidgetReviewOrchestrationRunner.run(
                orchestrator,
                targetUrl,
                apiKey,
                userMessage,
                mode,
                sessionId,
                requestReset,
                attachments,
                selectedEntries,
                requestId,
                progressListener
            );

        List<MapBatchResult> mapBatchResults = orchestration.mapBatchResults();
        List<BatchFailure> failures = orchestration.batchFailures();
        ReduceRequest reduceRequest = orchestration.reduceRequest();
        ReduceResult reduceResult = orchestration.reduceResult();

        List<String> allIds = distinctIds(extractAllIds(selectedEntries));

        List<String> missingIds = reduceResult != null
                ? distinctIds(reduceResult.getMissingChatIds())
                : distinctIds(orchestration.missingChatIds());

        missingIds = intersect(allIds, missingIds);
        List<String> usedIds = subtract(allIds, missingIds);

        CoverageSummary coverage = CoverageSummary.builder()
                .allSelectedChatIds(allIds)
                .chatsProvided(allIds.size())
                .usedChatIds(usedIds)
                .chatsUsedInAnalysis(usedIds.size())
                .notUsedChatIds(missingIds)
                .chatsNotUsed(missingIds.size())
                .reasonsChatsNotUsed(buildCoverageReasons(failures, missingIds))
                .totalBatches(orchestration.totalBatches())
                .successfulBatches(Math.max(0, orchestration.totalBatches() - orchestration.failedBatchIndexes().size()))
                .failedBatchIndexes(orchestration.failedBatchIndexes())
                .coverageComplete(missingIds.isEmpty())
                .build();

        WorkspaceResponse finalResp = orchestration.finalResponse();

        String finalText = extractPrimaryText(finalResp.body());
        String finalTextForValidation = canonicalizeForValidation(finalText);
        ReviewOutputValidator.ValidationResult finalValidation
            = reviewOutputValidator.validateFinalReportHierarchical(finalTextForValidation, allIds, mrConfig.getReduceMessageMaxChars());

        if (!finalValidation.isValid()) {
            log.log(Level.WARNING, "[manual-message][{0}][reduce-validation] errors={1}", new Object[]{requestId, finalValidation.getErrors()});
        } else if (!finalValidation.getWarnings().isEmpty()) {
            log.log(Level.INFO, "[manual-message][{0}][reduce-validation] warnings={1}", new Object[]{requestId, finalValidation.getWarnings()});
        }

        boolean metadataMismatch = hasCoverageMetadataMismatch(finalValidation);
        if (metadataMismatch) {
            log.log(Level.WARNING, "[manual-message][{0}][coverage] metadata mismatch detected in final report.", requestId);
        }

        if (reduceResult != null) {
            if (!reduceResult.isSuccess() && reduceResult.getErrorMessage() != null && !reduceResult.getErrorMessage().isBlank()) {
                log.log(Level.WARNING, "[manual-message][{0}][reduce-validation] errors={1}", new Object[]{requestId, reduceResult.getErrorMessage()});
            }
            if (!reduceResult.isCoverageComplete()) {
                log.log(Level.WARNING, "[manual-message][{0}][coverage] incomplete missing={1}", new Object[]{requestId, reduceResult.getMissingChatIds()});
            }
        }

            String summaryLog = "[manual-message][" + requestId + "][map-reduce-summary]"
                    + " reduceRequest=" + reduceRequest
                    + " reduceResultSuccess=" + (reduceResult != null && reduceResult.isSuccess())
                    + " coverage=" + coverage
                    + " mapBatchResults=" + mapBatchResults.size()
                    + " failures=" + failures.size()
                    + " coverageComplete=" + coverage.isCoverageComplete()
                    + " missingCount=" + coverage.getNotUsedChatIds().size()
                    + " metadataMismatch=" + metadataMismatch
                    + " strictFixedBatchMode=" + mrConfig.isStrictFixedBatchMode()
                    + " fixedBatchSize=" + mrConfig.getFixedBatchSize()
                    + " reduceInitialChunkSize=" + mrConfig.getReduceInitialChunkSize()
                    + " reduceMaxLevels=" + mrConfig.getReduceMaxLevels()
                    + " finalReduceMaxAttempts=" + mrConfig.getFinalReduceMaxAttempts();
            log.info(summaryLog);

        return new MapReduceExecutionResult(finalResp, orchestration);
    }

    private void mirrorWorkspaceResponse(HttpServletResponse resp, WorkspaceResponse remote) {
        resp.setStatus(remote.statusCode());

        String contentType = remote.contentType();
        if (contentType != null && !contentType.isBlank()) {
            if (!contentType.toLowerCase(Locale.ROOT).contains("charset")) {
                contentType = contentType + "; charset=UTF-8";
            }
            resp.setContentType(contentType);
        } else {
            resp.setContentType("application/json; charset=UTF-8");
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        byte[] bytes = remote.body() == null ? new byte[0] : remote.body().getBytes(StandardCharsets.UTF_8);
        try {
            resp.getOutputStream().write(bytes);
            resp.getOutputStream().flush();
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to mirror workspace response", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unable to stream workspace response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
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
            log.log(Level.FINE, "Unable to parse upstream response payload", ex);
            return body;
        }
    }

    private List<String> extractAllIds(List<SelectedEntry> entries) {
        List<String> out = new ArrayList<>();
        if (entries == null) {
            return out;
        }
        for (SelectedEntry e : entries) {
            if (e == null) {
                continue;
            }
            String id = e.getChatId();
            if (id != null && !id.isBlank()) {
                out.add(id.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private List<String> extractUsedIdsFromText(String text, List<String> expected) {
        ReviewOutputValidator.ValidationResult v
                = reviewOutputValidator.validateFinalReportHierarchical(text == null ? "" : text, expected, mrConfig.getReduceMessageMaxChars());
        return distinctIds(v.getFoundChatIds());
    }

    private boolean hasCoverageMetadataMismatch(ReviewOutputValidator.ValidationResult validation) {
        if (validation == null || validation.getErrors() == null) {
            return false;
        }
        for (String e : validation.getErrors()) {
            if (e != null && e.toLowerCase(Locale.ROOT).contains("coverage metadata mismatch")) {
                return true;
            }
        }
        return false;
    }

    private List<String> buildCoverageReasons(List<BatchFailure> failures, List<String> missingIds) {
        List<String> reasons = new ArrayList<>();
        if (failures != null) {
            for (BatchFailure bf : failures) {
                reasons.add(bf.reasonForCoverage() + " (batch " + bf.getBatchIndex() + ')');
            }
        }
        if (missingIds != null && !missingIds.isEmpty()) {
            reasons.add("missing inline evidence / truncated or absent per-chat content");
        }
        return reasons;
    }

    private List<String> distinctIds(List<String> ids) {
        Set<String> s = new LinkedHashSet<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null && !id.isBlank()) {
                    s.add(id.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return new ArrayList<>(s);
    }

    private List<String> subtract(List<String> all, List<String> used) {
        Set<String> a = new LinkedHashSet<>(all == null ? List.of() : distinctIds(all));
        Set<String> u = new LinkedHashSet<>(used == null ? List.of() : distinctIds(used));
        a.removeAll(u);
        return new ArrayList<>(a);
    }

    private List<String> intersect(List<String> a, List<String> b) {
        Set<String> sa = new LinkedHashSet<>(a == null ? List.of() : distinctIds(a));
        Set<String> sb = new LinkedHashSet<>(b == null ? List.of() : distinctIds(b));
        sa.retainAll(sb);
        return new ArrayList<>(sa);
    }

    private String buildOutboundMessage(String userMessage, String context, int maxTotalChars) {
        String base = userMessage == null ? "" : userMessage.trim();
        if (context == null || context.isBlank()) {
            return trimTo(base, maxTotalChars);
        }

        String suffix = "\n\nSelected chats context:\n" + context;
        String combined = base + suffix;
        if (combined.length() <= maxTotalChars) {
            return combined;
        }

        int roomForSuffix = Math.max(0, maxTotalChars - base.length());
        if (roomForSuffix <= 0) {
            return trimTo(base, maxTotalChars);
        }

        return base + trimTo(suffix, roomForSuffix);
    }

    private JsonArray normalizeAttachments(JsonObject payload) {
        if (payload == null || !payload.containsKey("attachments")) {
            return Json.createArrayBuilder().build();
        }

        JsonValue raw = payload.get("attachments");
        if (raw == null || raw.getValueType() != JsonValue.ValueType.ARRAY) {
            return Json.createArrayBuilder().build();
        }

        JsonArray input = payload.getJsonArray("attachments");
        var out = Json.createArrayBuilder();

        for (JsonValue v : input) {
            if (v == null || v.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }
            JsonObject o = v.asJsonObject();

            String name = o.getString("name", "").trim();
            String mime = o.getString("mime", "").trim();
            String contentString = o.getString("contentString", "").trim();

            if (name.isBlank() || mime.isBlank() || contentString.isBlank()) {
                continue;
            }

            out.add(Json.createObjectBuilder()
                    .add("name", name)
                    .add("mime", mime)
                    .add("contentString", contentString)
                    .build());
        }
        return out.build();
    }

    private String stripClientInjectedContext(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "";
        }
        String marker = "\n\nSelected chats context:\n";
        int idx = userMessage.indexOf(marker);
        return idx >= 0 ? userMessage.substring(0, idx).trim() : userMessage.trim();
    }

    private List<SelectedEntry> parseSelectedEntries(JsonObject payload) {
        List<SelectedEntry> entries = new ArrayList<>();
        if (payload == null || !payload.containsKey("selectedEntries")) {
            return entries;
        }

        JsonValue raw = payload.get("selectedEntries");
        if (raw == null || raw.getValueType() != JsonValue.ValueType.ARRAY) {
            return entries;
        }

        JsonArray arr = payload.getJsonArray("selectedEntries");
        if (arr == null) {
            return entries;
        }

        int count = 0;
        for (JsonValue value : arr) {
            if (count >= MAX_CONTEXT_ENTRIES_HARD_CAP) {
                break;
            }
            if (value == null || value.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            JsonObject obj = value.asJsonObject();
            String chatId = str(obj, "chatId");
            String prompt = str(obj, "prompt");
            String response = str(obj, "response");
            String createdAt = str(obj, "createdAt");
            String sid = str(obj, "sessionId");

            if (chatId.isBlank() && prompt.isBlank() && response.isBlank()) {
                continue;
            }

            entries.add(new SelectedEntry(chatId, prompt, response, createdAt, sid));
            count++;
        }

        entries.sort(Comparator.comparing((SelectedEntry e) -> e.getCreatedAt() == null ? "" : e.getCreatedAt()).reversed());
        return entries;
    }

    private String str(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return "";
        }
        JsonValue v = obj.get(key);
        if (v == null) {
            return "";
        }
        if (v.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) v).getString();
        }
        return v.toString();
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

        String s = canonicalizeForValidation(raw);
        if (s.isBlank()) {
            return "";
        }
        s = s.replaceFirst("^(https?://)+(https?://)", "$2");
        s = s.replaceFirst("^(https?://)(https?://)+", "$1");
        s = s.replace("https://https://", "https://")
                .replace("http://http://", "http://")
                .replace("http://https://", "https://")
                .replace("https://http://", "http://");

        try {
            if (trustedUrlValidator != null) {
                TrustedUrlValidator.ValidationResult trust = trustedUrlValidator.validate(s);
                if (!trust.isValid()) {
                    return "";
                }
            }

            URI u = toSafeUri(s);
            if (u == null) {
                return "";
            }
            String scheme = u.getScheme();
            String host = u.getHost();
            int port = u.getPort();

            if (scheme == null || host == null || host.isBlank()) {
                return "";
            }

            return port > 0
                    ? scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT) + ':' + port
                    : scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid base URL", e);
            return "";
        }
    }

    private URI toSafeUri(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = canonicalizeForValidation(value);
        if (normalized.isBlank()) {
            return null;
        }
        String lowered = normalized.toLowerCase(Locale.ROOT);
        if (!lowered.startsWith("http://") && !lowered.startsWith("https://")) {
            return null;
        }
        if (normalized.contains(" ") || normalized.contains("\t")) {
            return null;
        }
        try {
            return URI.create(normalized).normalize();
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid URI syntax", ex);
            return null;
        }
    }

    private String readRequestBody(HttpServletRequest req) {
        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            return "";
        }
        try (java.io.InputStream in = req.getInputStream(); java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
            if (reader == null) {
                return "";
            }
            char[] buffer = new char[4096];
            StringBuilder builder = new StringBuilder();
            int total = 0;
            int read;
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (total > MAX_JSON_PAYLOAD_BYTES) {
                    return "";
                }
                builder.append(buffer, 0, read);
            }
            String body = builder.toString();
            if (body.length() > MAX_JSON_PAYLOAD_BYTES) {
                return "";
            }
            return canonicalizeForValidation(body);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to read manual-message request body", e);
            return "";
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String canonicalizeForValidation(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        normalized = ServletRequestParamUtil.normalizeBodyText(normalized, 0, false);
        if (normalized == null) {
            return "";
        }
        if (normalized.length() > MAX_JSON_PAYLOAD_BYTES) {
            return normalized.substring(0, MAX_JSON_PAYLOAD_BYTES);
        }
        return normalized;
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
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            respondWithError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        return true;
    }

    private void respondWithError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write manual-message error response", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? "Request failed." : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    private Set<String> parseCsvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        String[] parts = csv.split(",");
        java.util.Set<String> out = new java.util.HashSet<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                out.add(p.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static final class MapReduceExecutionResult {

        final WorkspaceResponse response;
        final WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration;

        private MapReduceExecutionResult(WorkspaceResponse response, WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration) {
            this.response = response;
            this.orchestration = orchestration;
        }
    }
}
