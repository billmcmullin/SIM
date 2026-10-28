// src/main/java/com/sim/chatserver/web/dashboard/drilldown/WidgetReviewManualMessageServlet.java
package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.Reader;
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
import com.sim.chatserver.security.review.ReviewValidationResult;
import com.sim.chatserver.security.review.TrustedUrlValidator;
import com.sim.chatserver.security.review.ValidationResult;
import com.sim.chatserver.service.JobResult;
import com.sim.chatserver.service.OrchestrationResult;
import com.sim.chatserver.service.PromptTemplateService;
import com.sim.chatserver.service.ReviewContextBuilderService;
import com.sim.chatserver.service.ReviewSamplingService;
import com.sim.chatserver.service.ReviewJobService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceResponse;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.util.ThrowableUtil;
import com.sim.chatserver.web.util.JsonPrimaryTextUtil;
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


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "WidgetReviewManualMessageServlet", urlPatterns = {"/dashboard/drilldown/widget-review/manual-message"})
public class WidgetReviewManualMessageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewManualMessageServlet.class.getCanonicalName());
    private static final String CHAT_API_PATH_TEMPLATE = "/api/v1/workspace/%s/chat";
    private static final String LS = System.lineSeparator();

    private static final int MAX_CONTEXT_ENTRIES_HARD_CAP = Integer.MAX_VALUE;
    private static final int MAX_SESSION_ID_CHARS = 200;
    private static final int MAX_JSON_PAYLOAD_BYTES = 128 * 1024;
    private static final Set<String> ALLOWED_MODES = Set.of(com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT, com.sim.chatserver.util.CommonLiteralConstants.FIELD_QUERY, "automatic");
    private static final String CSV_SEPARATOR = ",";

        private static ReviewRuntime runtime() {
        return RuntimeHolder.INSTANCE;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.log(Level.INFO, "[manual-message][init] loaded config: {0}", RuntimeHolder.INSTANCE.mrConfig);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final String requestId = UUID.randomUUID().toString();
            final long startNanos = System.nanoTime();

            ManualRequestContext requestContext = parseManualRequestContext(req, resp, requestId);
            if (requestContext == null) {
                return;
            }

            WorkspaceTargetContext targetContext = resolveWorkspaceTargetContext(resp, requestId);
            if (targetContext == null) {
                return;
            }

            String owner = String.valueOf(req.getSession(false).getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));
            executeManualRequest(resp, requestId, startNanos, requestContext, targetContext, owner);
    
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private ManualRequestContext parseManualRequestContext(HttpServletRequest req, HttpServletResponse resp, String requestId) {
        if (!isLoggedIn(req, resp)) {
            return null;
        }

        try {
            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException ex) {
            log.log(Level.WARNING, "UTF-8 character encoding was not accepted", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
            return null;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return null;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return null;
        }

        String userMessage = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "").trim();
        if (userMessage.isEmpty()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "message is required.");
            return null;
        }

        String mode = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MODE, com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT).trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_MODES.contains(mode)) {
            mode = com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT;
        }

        String sessionId = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, "").trim();
        if (sessionId.length() > MAX_SESSION_ID_CHARS) {
            sessionId = TextIoSanitizerUtil.safeSlice(sessionId, 0, MAX_SESSION_ID_CHARS);
        }

        boolean requestReset = payload.getBoolean("requestReset", payload.getBoolean(com.sim.chatserver.util.CommonLiteralConstants.VALUE_RESET, false));
        boolean async = payload.getBoolean("async", false);
        List<SelectedEntry> selectedEntries = parseSelectedEntries(payload);
        JsonArray normalizedAttachments = normalizeAttachments(payload);

        log.log(Level.INFO, "[manual-message][{0}] selectedEntriesParsed={1}", new Object[]{requestId, selectedEntries.size()});

        return new ManualRequestContext(
                stripClientInjectedContext(userMessage),
                mode,
                sessionId,
                requestReset,
                async,
                selectedEntries,
                normalizedAttachments
        );
    }

    private WorkspaceTargetContext resolveWorkspaceTargetContext(HttpServletResponse resp, String requestId) {
        EncryptedDbConfigStore.setAppDataSourceHolder(dataSourceHolder());
        ServerConfig config;
        try {
            config = EncryptedDbConfigStore.load();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.SEVERE, "[manual-message][" + requestId + "] Unable to load server configuration", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration not available.");
            return null;
        }

        if (config == null) {
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration missing.");
            return null;
        }

        String workspaceSlug = buildSlug(config.getWorkspaceName());
        if (workspaceSlug == null || workspaceSlug.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace slug not configured.");
            return null;
        }

        String baseUrl = sanitizeBaseUrl(buildBaseUrl(config));
        if (baseUrl == null || baseUrl.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Server connection information is incomplete.");
            return null;
        }

        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "API key not configured.");
            return null;
        }

        String encodedSlug = URLEncoder.encode(workspaceSlug, StandardCharsets.UTF_8);
        String targetUrl = stripTrailingSlash(baseUrl) + String.format(CHAT_API_PATH_TEMPLATE, encodedSlug);
        targetUrl = canonicalizeForValidation(targetUrl);

        ValidationResult trust = runtime().trustedUrlValidator.validate(targetUrl);
        if (!trust.isValid()) {
            log.log(Level.WARNING, "[manual-message][{0}] blocked untrusted targetUrl reason={1}", new Object[]{requestId, trust.getReason()});
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace URL failed trust validation.");
            return null;
        }

        return new WorkspaceTargetContext(targetUrl, apiKey);
    }

    private void executeManualRequest(
            HttpServletResponse resp,
            String requestId,
            long startNanos,
            ManualRequestContext requestContext,
            WorkspaceTargetContext targetContext,
            String owner
    ) {
        try {
            if (requestContext.async) {
                handleAsyncSubmission(
                        resp,
                        targetContext.targetUrl,
                        targetContext.apiKey,
                        requestContext.userMessage,
                        requestContext.mode,
                        requestContext.sessionId,
                        requestContext.requestReset,
                        requestContext.normalizedAttachments,
                        requestContext.selectedEntries,
                        requestId,
                        owner
                );
                return;
            }

            boolean useMapReduce = shouldUseMapReduce(requestContext.selectedEntries);
            WorkspaceResponse upstream = useMapReduce
                    ? runMapReduce(
                        targetContext.targetUrl,
                        targetContext.apiKey,
                        requestContext.userMessage,
                        requestContext.mode,
                        requestContext.sessionId,
                        requestContext.requestReset,
                        requestContext.normalizedAttachments,
                        requestContext.selectedEntries,
                        requestId
                    ).response
                    : runSinglePass(
                        targetContext.targetUrl,
                        targetContext.apiKey,
                        requestContext.userMessage,
                        requestContext.mode,
                        requestContext.sessionId,
                        requestContext.requestReset,
                        requestContext.normalizedAttachments,
                        requestContext.selectedEntries,
                        requestId
                    );

            mirrorWorkspaceResponse(resp, upstream);
            String completionLog = "[manual-message][" + requestId + "] completed"
                    + " status=" + upstream.statusCode()
                    + " latencyMs=" + ((System.nanoTime() - startNanos) / 1_000_000L)
                    + " mode=" + requestContext.mode
                    + " selected=" + requestContext.selectedEntries.size()
                    + " strategy=" + (useMapReduce ? "map-reduce-runtime().orchestrator" : "single-pass");
            log.info(completionLog);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
            if (causedByInterrupted(ex)) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.SEVERE, "[manual-message][" + requestId + "] execution failed", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process manual message.");
        }
    }

    private boolean shouldUseMapReduce(List<SelectedEntry> selectedEntries) {
        int n = selectedEntries == null ? 0 : selectedEntries.size();

        if (runtime().mrConfig.isExhaustiveMode()) {
            boolean use = n > 0;
                log.log(
                    Level.INFO,
                    "[manual-message][strategy] exhaustiveMode=true selected={0} singlePassMaxSelected={1} -> useMapReduce={2}",
                    new Object[]{n, runtime().mrConfig.getSinglePassMaxSelected(), use}
                );
            return use;
        }

        boolean use = n > runtime().mrConfig.getSinglePassMaxSelected();
            log.log(
                Level.INFO,
                "[manual-message][strategy] exhaustiveMode=false selected={0} singlePassMaxSelected={1} -> useMapReduce={2}",
                new Object[]{n, runtime().mrConfig.getSinglePassMaxSelected(), use}
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
            String requestId,
            String owner
            ) {

        ReviewJobService jobs = WidgetReviewJobStatusServlet.jobService();

        String jobId;
        try {
        jobId = jobs.submitForUser(owner, requestId, selectedEntries.size(), (jid) -> executeAsyncJob(
            jobs,
            jid,
            targetUrl,
            apiKey,
            userMessage,
            mode,
            sessionId,
            requestReset,
            attachments,
            selectedEntries,
            requestId
        ));
        } catch (java.util.concurrent.RejectedExecutionException ex) {
            log.log(Level.FINE, "Review job submission rejected because capacity is full", ex);
            resp.setHeader("Retry-After", "5");
            respondWithError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Review capacity is full. Please retry shortly.");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType(CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            resp.getWriter().write(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "accepted")
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_REQUEST_ID, requestId)
                    .add("jobId", jobId)
                    .build()
                    .toString());
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write async accepted response", e);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
        }
    }

        private JobResult executeAsyncJob(
            ReviewJobService jobs,
            String jobId,
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId
        ) {
        try {
            List<String> allIds = distinctIds(extractAllIds(selectedEntries));
            jobs.updateMapProgress(
                jobId, 0, 0, 0, 0,
                allIds, List.of(), allIds, List.of(), "Starting"
            );

            if (!shouldUseMapReduce(selectedEntries)) {
            return executeSinglePassAsyncJob(
                jobs,
                jobId,
                targetUrl,
                apiKey,
                userMessage,
                mode,
                sessionId,
                requestReset,
                attachments,
                selectedEntries,
                allIds,
                requestId
            );
            }

            return executeMapReduceAsyncJob(
                jobs,
                jobId,
                targetUrl,
                apiKey,
                userMessage,
                mode,
                sessionId,
                requestReset,
                attachments,
                selectedEntries,
                allIds,
                requestId
            );
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            if (causedByInterrupted(e)) {
            Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "[manual-message][" + requestId + "] async execution failed", e);
            return new JobResult(
                500, false, "Failed",
                e.getMessage() == null ? "Async execution failed." : e.getMessage(),
                0, 0, 1, 0,
                List.of(), List.of(), List.of(),
                List.of(), List.of(),
                "", "", com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON
            );
        }
        }

        private JobResult executeSinglePassAsyncJob(
            ReviewJobService jobs,
            String jobId,
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            List<String> allIds,
            String requestId
        ) {
        WorkspaceResponse upstream = runSinglePass(
            targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId
        );

        ReviewJobComputation completion = computeJobComputation(upstream, allIds, null, null);

        jobs.updateReduceProgress(
            jobId, 1, 1, completion.ok ? 0 : 1, 0,
            allIds, completion.usedIds, completion.missingIds, List.of(), completion.completionMsg
        );

        return completion.toJobResult(1, 1, completion.ok ? 0 : 1, 0, allIds, List.of());
        }

    private JobResult executeMapReduceAsyncJob(
            ReviewJobService jobs,
            String jobId,
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            List<String> allIds,
            String requestId
    ) {
        AtomicInteger liveTotalBatches = new AtomicInteger(0);
        AtomicInteger liveCompletedBatches = new AtomicInteger(0);
        AtomicInteger liveFailedBatches = new AtomicInteger(0);
        int reduceFinalAttemptTotal = runtime().mrConfig.getFinalReduceMaxAttempts();

        WidgetReviewMapReduceOrchestrator.ProgressListener progressListener = createProgressListener(
                jobs,
                jobId,
                allIds,
                liveTotalBatches,
                liveCompletedBatches,
                liveFailedBatches,
                reduceFinalAttemptTotal
        );

        MapReduceExecutionResult mr = runMapReduce(
                targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progressListener
        );

        WorkspaceResponse upstream = mr.response;
        OrchestrationResult orchestration = mr.orchestration;
        ReduceResult reduceResult = orchestration == null ? null : orchestration.reduceResult();

        List<String> missingIds = reduceResult != null
                ? distinctIds(reduceResult.getMissingChatIds())
                : distinctIds(orchestration == null ? allIds : orchestration.missingChatIds());
        missingIds = intersect(allIds, missingIds);
        List<String> usedIds = subtract(allIds, missingIds);
        ReviewJobComputation completion = computeJobComputation(upstream, allIds, usedIds, missingIds);

        int totalBatches = orchestration == null ? 0 : orchestration.totalBatches();
        int failedBatches = orchestration == null ? 1 : orchestration.failedBatchIndexes().size();
        int completedBatches = orchestration == null ? 0 : (orchestration.totalBatches() - orchestration.failedBatchIndexes().size());
        List<Integer> failedBatchIndexes = orchestration == null ? List.of() : orchestration.failedBatchIndexes();

        jobs.updateReduceProgress(
                jobId,
            totalBatches,
            completedBatches,
            failedBatches,
                0,
            allIds, completion.usedIds, completion.missingIds,
            failedBatchIndexes,
            completion.completionMsg
        );

        return completion.toJobResult(totalBatches, completedBatches, failedBatches, 0, allIds, failedBatchIndexes);
    }

        private ReviewJobComputation computeJobComputation(
            WorkspaceResponse upstream,
            List<String> allIds,
            List<String> usedIdsOverride,
            List<String> missingIdsOverride
        ) {
        int httpStatus = upstream == null ? 500 : upstream.statusCode();
        String body = upstream == null ? "" : upstream.body();
        String contentType = upstream == null ? com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON : defaultIfBlank(upstream.contentType(), com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON);
        String finalReport = extractPrimaryText(body);
        String normalizedFinalReport = Normalizer.normalize(defaultIfBlank(finalReport, ""), Normalizer.Form.NFKC);
        String finalReportForValidation = validateTaintedRequestBody(canonicalizeForValidation(normalizedFinalReport));

        List<String> usedIds;
        List<String> missingIds;
        if (usedIdsOverride == null || missingIdsOverride == null) {
            List<String> usedIdsFromText = extractUsedIdsFromText(finalReportForValidation, allIds);
            usedIds = !usedIdsFromText.isEmpty() ? usedIdsFromText : List.of();
            missingIds = subtract(allIds, usedIds);
        } else {
            usedIds = distinctIds(usedIdsOverride);
            missingIds = intersect(allIds, distinctIds(missingIdsOverride));
        }

        boolean metadataMismatch = hasMetadataMismatch(finalReportForValidation, allIds);
        boolean coverageComplete = missingIds.isEmpty() && !metadataMismatch;
        boolean httpOk = upstream != null && httpStatus < 400;
        boolean ok = httpOk && coverageComplete;

        String completionMsg = completionMessage(ok, httpOk, httpStatus, missingIds.size());
        String errMsg = errorMessage(ok, httpOk, httpStatus, metadataMismatch);
        List<String> warnings = coverageWarnings(coverageComplete, metadataMismatch);

        return new ReviewJobComputation(
            httpStatus,
            ok,
            completionMsg,
            errMsg,
            usedIds,
            missingIds,
            warnings,
            finalReport,
            body,
            contentType
        );
        }

    private boolean hasMetadataMismatch(String finalReportForValidation, List<String> allIds) {
            String normalizedReportForValidation = canonicalizeForValidation(defaultIfBlank(finalReportForValidation, ""));
        ReviewValidationResult finalValidation = runtime()
                .reviewOutputValidator
                .validateFinalReportHierarchical(
                normalizedReportForValidation,
                        allIds,
                        runtime().mrConfig.getReduceMessageMaxChars());
        return hasCoverageMetadataMismatch(finalValidation);
    }

    private String completionMessage(boolean ok, boolean httpOk, int httpStatus, int missingCount) {
        if (ok) {
            return "Completed";
        }
        if (httpOk) {
            return "Completed with partial coverage (missing=" + missingCount + ')';
        }
        return "Completed with upstream errors (status=" + httpStatus + ')';
    }

    private String errorMessage(boolean ok, boolean httpOk, int httpStatus, boolean metadataMismatch) {
        if (ok) {
            return "";
        }
        if (!httpOk) {
            return "Upstream returned status " + httpStatus;
        }
        return metadataMismatch ? "Coverage metadata mismatch." : "Coverage incomplete.";
    }

    private List<String> coverageWarnings(boolean coverageComplete, boolean metadataMismatch) {
        List<String> warnings = new ArrayList<>();
        if (!coverageComplete) {
            warnings.add("coverage incomplete");
        }
        if (metadataMismatch) {
            warnings.add("coverage metadata mismatch");
        }
        return warnings;
    }

    private WidgetReviewMapReduceOrchestrator.ProgressListener createProgressListener(
            ReviewJobService jobs,
            String jobId,
            List<String> allIds,
            AtomicInteger liveTotalBatches,
            AtomicInteger liveCompletedBatches,
            AtomicInteger liveFailedBatches,
            int reduceFinalAttemptTotal
    ) {
        return new WidgetReviewMapReduceOrchestrator.ProgressListener() {
            @Override
            public void onMapRoundStarted(String reqId, int round, int totalRounds, int remainingBeforeRound, int entriesInRound) {
                jobs.updateMapProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), allIds, List.of(),
                        "Map round " + round + '/' + totalRounds + " started - remaining=" + remainingBeforeRound
                );
            }

            @Override
            public void onMapBatchStarted(String reqId, int batchIndex, int totalBatchesSoFar, int expectedIdsInBatch, int round) {
                liveTotalBatches.set(Math.max(liveTotalBatches.get(), totalBatchesSoFar));
                jobs.updateMapProgress(
                        jobId,
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
                } else {
                    liveFailedBatches.incrementAndGet();
                }

                jobs.updateMapProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), List.of(), List.of(),
                        "Batch " + batchIndex + ' ' + (success ? "completed" : com.sim.chatserver.util.CommonLiteralConstants.STATUS_FAILED)
                        + " - " + liveCompletedBatches.get() + '/' + Math.max(1, liveTotalBatches.get())
                        + " complete"
                );
            }

            @Override
            public void onReduceStarted(String reqId, int totalSelected, int totalBatches, int mapOutputsCount, int missingCount) {
                liveTotalBatches.set(Math.max(liveTotalBatches.get(), totalBatches));
                jobs.updateReduceProgress(
                        jobId,
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
                            jobId,
                            Math.max(liveTotalBatches.get(), 0),
                            Math.max(liveCompletedBatches.get(), 0),
                            Math.max(liveFailedBatches.get(), 0),
                            0,
                            allIds, List.of(), List.of(), List.of(),
                            "Final synthesis attempt " + chunkIndex + '/' + reduceFinalAttemptTotal + " - summaries=" + chunkSize
                    );
                    return;
                }

                jobs.updateReduceProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), List.of(), List.of(),
                        "Synthesis L" + level + " - chunk " + chunkIndex + '/' + totalChunksAtLevel
                        + " - size=" + chunkSize + " - cfg=" + currentChunkSizeConfig
                );
            }

            @Override
            public void onReduceChunkCompleted(String reqId, int level, int chunkIndex, int totalChunksAtLevel, boolean success, int httpStatus) {
                String phaseText = (level == 999)
                        ? ("Final synthesis attempt " + chunkIndex + '/' + reduceFinalAttemptTotal)
                        : ("Synthesis L" + level + " chunk " + chunkIndex + '/' + totalChunksAtLevel);

                jobs.updateReduceProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), List.of(), List.of(),
                        phaseText + ' ' + (success ? "completed" : com.sim.chatserver.util.CommonLiteralConstants.STATUS_FAILED) + " (HTTP " + httpStatus + ')'
                );
            }

            @Override
            public void onReduceLevelCompleted(String reqId, int level, int totalChunksAtLevel, int producedSummaries) {
                jobs.updateReduceProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), List.of(), List.of(),
                        "Synthesis level " + level + " complete - chunks=" + totalChunksAtLevel + " - summaries=" + producedSummaries
                );
            }

            @Override
            public void onReduceCompleted(String reqId, boolean success, int httpStatus, int missingCount) {
                jobs.updateReduceProgress(
                        jobId,
                        Math.max(liveTotalBatches.get(), 0),
                        Math.max(liveCompletedBatches.get(), 0),
                        Math.max(liveFailedBatches.get(), 0),
                        0,
                        allIds, List.of(), List.of(), List.of(),
                        "Reduce " + (success ? "completed" : com.sim.chatserver.util.CommonLiteralConstants.STATUS_FAILED) + " (status=" + httpStatus + ')'
                );
            }
        };
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
            ) {
        if (selectedEntries == null) {
            selectedEntries = List.of();
        }
        String controlledPrompt = runtime().promptTemplateService.buildControlledPrompt(userMessage, true, false, true);
        String deterministicHeader = "Deterministic metadata (use exactly; do not estimate):" + LS
            + "- exact_total_selected: " + selectedEntries.size() + com.sim.chatserver.util.LineSeparatorUtil.LINE_FEED
            + "- execution_mode: single-pass" + LS;

        String promptWithMeta = controlledPrompt + LS + LS + deterministicHeader;
        String context = runtime().reviewContextBuilderService.buildContext(promptWithMeta, selectedEntries, runtime().mrConfig.getSinglePassContextMaxChars());
        String outbound = buildOutboundMessage(promptWithMeta, context, runtime().mrConfig.getSinglePassMessageMaxChars());

        WorkspaceResponse response = sendChatHandled(
                targetUrl, apiKey, outbound, mode, sessionId, requestReset, attachments, requestId
        );

        if (runtime().workspaceClient.isLikelyContextTooLarge(response)) {
            String retryContext = runtime().reviewContextBuilderService.buildContext(promptWithMeta, selectedEntries, runtime().mrConfig.getRetryContextChars());
            String retryMsg = buildOutboundMessage(promptWithMeta, retryContext, runtime().mrConfig.getRetryMessageMaxChars());
                response = sendChatHandled(
                    targetUrl, apiKey, retryMsg, mode, sessionId, true, attachments, requestId
            );
        }

        String text = extractPrimaryText(response.body());
        String textForValidation = canonicalizeForValidation(text);
        ReviewValidationResult validation = runtime().reviewOutputValidator.validateFinalReport(textForValidation, runtime().mrConfig.getSinglePassMessageMaxChars());
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
    ) {
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
    ) {

        OrchestrationResult orchestration;
        try {
            orchestration = WidgetReviewOrchestrationRunner.run(
                    runtime().orchestrator,
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
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Map-reduce orchestration failed", ex);
        } catch (IOException ex) {
            if (causedByInterrupted(ex)) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Map-reduce orchestration failed", ex);
        }

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
        ReviewValidationResult finalValidation
            = runtime().reviewOutputValidator.validateFinalReportHierarchical(finalTextForValidation, allIds, runtime().mrConfig.getReduceMessageMaxChars());

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
                    + " strictFixedBatchMode=" + runtime().mrConfig.isStrictFixedBatchMode()
                    + " fixedBatchSize=" + runtime().mrConfig.getFixedBatchSize()
                    + " reduceInitialChunkSize=" + runtime().mrConfig.getReduceInitialChunkSize()
                    + " reduceMaxLevels=" + runtime().mrConfig.getReduceMaxLevels()
                    + " finalReduceMaxAttempts=" + runtime().mrConfig.getFinalReduceMaxAttempts();
            log.info(summaryLog);

        return new MapReduceExecutionResult(finalResp, orchestration);
    }

    private WorkspaceResponse sendChatHandled(
            String targetUrl,
            String apiKey,
            String message,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            String requestId
    ) {
        try {
            return runtime().workspaceClient.sendChat(
                    targetUrl,
                    apiKey,
                    message,
                    mode,
                    sessionId,
                    requestReset,
                    attachments,
                    requestId
            );
        } catch (IOException e) {
            throw new IllegalStateException("Workspace chat request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Workspace chat request interrupted", e);
        }
    }

    private boolean causedByInterrupted(Throwable throwable) {
        return ThrowableUtil.hasInterruptedCause(throwable);
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
            resp.setContentType(CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        byte[] bytes = remote.body() == null ? new byte[0] : remote.body().getBytes(StandardCharsets.UTF_8);
        try {
            resp.getOutputStream().write(bytes);
            resp.getOutputStream().flush();
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to mirror workspace response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unable to stream workspace response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private String extractPrimaryText(String body) {
        return JsonPrimaryTextUtil.extractPrimaryText(body, log, "Unable to parse upstream response payload");
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
        ReviewValidationResult v
                = runtime().reviewOutputValidator.validateFinalReportHierarchical(text == null ? "" : text, expected, runtime().mrConfig.getReduceMessageMaxChars());
        return distinctIds(v.getFoundChatIds());
    }

    private boolean hasCoverageMetadataMismatch(ReviewValidationResult validation) {
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
        for (String usedId : u) {
            a.remove(usedId);
        }
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

        String suffix = LS + LS + "Selected chats context:" + LS + context;
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
        if (payload == null || !payload.containsKey(com.sim.chatserver.util.CommonLiteralConstants.FIELD_ATTACHMENTS)) {
            return Json.createArrayBuilder().build();
        }

        JsonValue raw = payload.get(com.sim.chatserver.util.CommonLiteralConstants.FIELD_ATTACHMENTS);
        if (raw == null || raw.getValueType() != JsonValue.ValueType.ARRAY) {
            return Json.createArrayBuilder().build();
        }

        JsonArray input = payload.getJsonArray(com.sim.chatserver.util.CommonLiteralConstants.FIELD_ATTACHMENTS);
        var out = Json.createArrayBuilder();

        for (JsonValue v : input) {
            if (v == null || v.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }
            JsonObject o = v.asJsonObject();

            String name = o.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_NAME, "").trim();
            String mime = o.getString("mime", "").trim();
            String contentString = o.getString("contentString", "").trim();

            if (name.isBlank() || mime.isBlank() || contentString.isBlank()) {
                continue;
            }

            out.add(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_NAME, name)
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
        String marker = LS + LS + "Selected chats context:" + LS;
        int idx = userMessage.indexOf(marker);
        return idx >= 0 ? TextIoSanitizerUtil.safeSlice(userMessage, 0, idx).trim() : userMessage.trim();
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
            String chatId = str(obj, com.sim.chatserver.util.CommonLiteralConstants.FIELD_CHAT_ID);
            String prompt = str(obj, com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT);
            String response = str(obj, com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE);
            String createdAt = str(obj, com.sim.chatserver.util.CommonLiteralConstants.FIELD_CREATED_AT);
            String sid = str(obj, com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID);

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
        if (normalized.contains(com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR)) {
            builder.append(normalized); 
        }else {
            builder.append(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS).append(normalized);
        }

        boolean hasPort = normalized.matches(".*:\\d+$");
        if (!hasPort && config.getServerPort() > 0) {
            builder.append((char) 58).append(config.getServerPort());
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
        s = s.replace("https://https://", com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)
                .replace("http://http://", com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP)
                .replace("http://https://", com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)
                .replace("https://http://", com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP);

        try {
            if (runtime().trustedUrlValidator != null) {
                ValidationResult trust = runtime().trustedUrlValidator.validate(s);
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
                    ? scheme.toLowerCase(Locale.ROOT) + com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR + host.toLowerCase(Locale.ROOT) + (char) 58 + port
                    : scheme.toLowerCase(Locale.ROOT) + com.sim.chatserver.util.CommonLiteralConstants.SCHEME_SEPARATOR + host.toLowerCase(Locale.ROOT);
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
        if (!lowered.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTP) && !lowered.startsWith(com.sim.chatserver.util.CommonLiteralConstants.URL_PREFIX_HTTPS)) {
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

    private AppDataSourceHolder dataSourceHolder() {
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
            return TextIoSanitizerUtil.safeSlice(normalized, 0, MAX_JSON_PAYLOAD_BYTES);
        }
        return normalized;
    }

    private String validateTaintedRequestBody(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder safe = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isISOControl(ch) && ch != System.lineSeparator().charAt(System.lineSeparator().length() - 1) && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        String normalized = safe.toString();
        if (normalized.length() > MAX_JSON_PAYLOAD_BYTES) {
            return TextIoSanitizerUtil.safeSlice(normalized, 0, MAX_JSON_PAYLOAD_BYTES);
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
        return value.endsWith("/") ? TextIoSanitizerUtil.safeSlice(value, 0, value.length() - 1) : value;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            respondWithError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return false;
        }
        return true;
    }

    private void respondWithError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write manual-message error response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : TextIoSanitizerUtil.safeSlice(value, 0, maxChars);
    }

    private static Set<String> parseCsvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        String[] parts = csv.split(CSV_SEPARATOR);
        java.util.Set<String> out = new java.util.HashSet<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                out.add(p.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}

final class WorkspaceTargetContext {

    final String targetUrl;
    final String apiKey;

    WorkspaceTargetContext(String targetUrl, String apiKey) {
        this.targetUrl = targetUrl;
        this.apiKey = apiKey;
    }
}

final class RuntimeHolder {
    static final ReviewRuntime INSTANCE = new ReviewRuntime();
}

final class ReviewRuntime {
    private static final String ENV_REVIEW_TRUSTED_HOSTS = "REVIEW_TRUSTED_HOSTS";
    private static final String ENV_REVIEW_TRUSTED_HOST_SUFFIXES = "REVIEW_TRUSTED_HOST_SUFFIXES";
    private static final String ENV_REVIEW_ALLOW_PRIVATE_NETWORKS = "REVIEW_ALLOW_PRIVATE_NETWORKS";
    private static final String DEFAULT_ALLOW_PRIVATE_NETWORKS = com.sim.chatserver.util.CommonLiteralConstants.VALUE_FALSE;
    private static final String CSV_SEPARATOR = ",";
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    final MapReduceConfig mrConfig;
    final WorkspaceClient workspaceClient;
    final WidgetReviewMapReduceOrchestrator orchestrator;
    final PromptTemplateService promptTemplateService;
    final ReviewContextBuilderService reviewContextBuilderService;
    final ReviewOutputValidator reviewOutputValidator;
    final TrustedUrlValidator trustedUrlValidator;

    ReviewRuntime() {
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
        ReviewContextBuilderService configuredReviewContextBuilderService = new ReviewContextBuilderService(new ReviewSamplingService());
        ReviewOutputValidator configuredReviewOutputValidator = new ReviewOutputValidator();

        Set<String> allowedHosts = parseCsvToSet(ENV.get(ENV_REVIEW_TRUSTED_HOSTS));
        Set<String> allowedSuffixes = parseCsvToSet(ENV.get(ENV_REVIEW_TRUSTED_HOST_SUFFIXES));
        boolean allowPrivate = Boolean.parseBoolean(defaultIfBlank(ENV.get(ENV_REVIEW_ALLOW_PRIVATE_NETWORKS), DEFAULT_ALLOW_PRIVATE_NETWORKS));
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

        this.mrConfig = loadedConfig;
        this.workspaceClient = configuredWorkspaceClient;
        this.promptTemplateService = configuredPromptTemplateService;
        this.reviewContextBuilderService = configuredReviewContextBuilderService;
        this.reviewOutputValidator = configuredReviewOutputValidator;
        this.trustedUrlValidator = configuredTrustedUrlValidator;
        this.orchestrator = configuredOrchestrator;
    }

    private static Set<String> parseCsvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        String[] parts = csv.split(CSV_SEPARATOR);
        java.util.Set<String> out = new java.util.HashSet<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                out.add(p.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

final class MapReduceExecutionResult {

    final WorkspaceResponse response;
    final OrchestrationResult orchestration;

    MapReduceExecutionResult(WorkspaceResponse response, OrchestrationResult orchestration) {
        this.response = response;
        this.orchestration = orchestration;
    }
}

final class ReviewJobComputation {

    final int httpStatus;
    final boolean ok;
    final String completionMsg;
    final String errMsg;
    final List<String> usedIds;
    final List<String> missingIds;
    final List<String> warnings;
    final String finalReport;
    final String body;
    final String contentType;

    ReviewJobComputation(
            int httpStatus,
            boolean ok,
            String completionMsg,
            String errMsg,
            List<String> usedIds,
            List<String> missingIds,
            List<String> warnings,
            String finalReport,
            String body,
            String contentType
    ) {
        this.httpStatus = httpStatus;
        this.ok = ok;
        this.completionMsg = completionMsg;
        this.errMsg = errMsg;
        this.usedIds = usedIds;
        this.missingIds = missingIds;
        this.warnings = warnings;
        this.finalReport = finalReport;
        this.body = body;
        this.contentType = contentType;
    }

    JobResult toJobResult(
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allIds,
            List<Integer> failedBatchIndexes
    ) {
        return new JobResult(
                httpStatus,
                ok,
                completionMsg,
                errMsg,
                totalBatches,
                completedBatches,
                failedBatches,
                retries,
                allIds,
                usedIds,
                missingIds,
                failedBatchIndexes,
                warnings,
                finalReport,
                body,
                contentType
        );
    }
}

final class ManualRequestContext {

    final String userMessage;
    final String mode;
    final String sessionId;
    final boolean requestReset;
    final boolean async;
    final List<SelectedEntry> selectedEntries;
    final JsonArray normalizedAttachments;

    ManualRequestContext(
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            boolean async,
            List<SelectedEntry> selectedEntries,
            JsonArray normalizedAttachments
    ) {
        this.userMessage = userMessage;
        this.mode = mode;
        this.sessionId = sessionId;
        this.requestReset = requestReset;
        this.async = async;
        this.selectedEntries = selectedEntries;
        this.normalizedAttachments = normalizedAttachments;
    }
}


