// src/main/java/com/sim/chatserver/web/dashboard/drilldown/WidgetReviewJobStatusServlet.java
package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.service.ReviewJobService;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Polling endpoint for async review job progress.
 *
 * GET /dashboard/drilldown/widget-review/job-status?jobId=... DELETE
 * /dashboard/drilldown/widget-review/job-status?jobId=... (cancel)
 */
@WebServlet(name = "WidgetReviewJobStatusServlet", urlPatterns = {"/dashboard/drilldown/widget-review/job-status"})
public class WidgetReviewJobStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Shared in-memory job service instance.
     */
    private static final ReviewJobService JOB_SERVICE = new ReviewJobService();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void destroy() {
        super.destroy();
        // Optional:
        // JOB_SERVICE.shutdownNow();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);
        String jobId = safe(requestContext.first("jobId", 256));
        if (jobId.isBlank()) {
            respondError(resp, HttpServletResponse.SC_BAD_REQUEST, "jobId is required.");
            return;
        }

        ReviewJobStatus status = JOB_SERVICE.getStatus(jobId);
        if (status == null) {
            respondError(resp, HttpServletResponse.SC_NOT_FOUND, "Job not found.");
            return;
        }

        List<String> allIds = normalizeIds(status.getAllSelectedChatIds());
        List<String> usedIdsRaw = normalizeIds(status.getUsedChatIds());
        List<String> missingIdsRaw = normalizeIds(status.getMissingChatIds());

        List<String> missingIds;
        List<String> usedIds;
        if (!allIds.isEmpty()) {
            missingIds = subtract(allIds, usedIdsRaw);
            usedIds = subtract(allIds, missingIds);
        } else {
            missingIds = missingIdsRaw;
            usedIds = usedIdsRaw;
        }

        int totalFromStatus = Math.max(0, status.getTotalSelected());
        int total = !allIds.isEmpty() ? allIds.size() : totalFromStatus;
        int missing = missingIds.size();
        int used = !allIds.isEmpty() ? usedIds.size() : Math.max(0, total - missing);

        int percent = total <= 0
                ? Math.max(0, Math.min(100, status.getCoveragePercent()))
                : (int) Math.round((used * 100.0) / total);
        percent = Math.max(0, Math.min(100, percent));

        boolean coverageComplete = missing == 0;

        List<String> warnings = normalizeStrings(status.getWarnings());
        boolean metadataMismatch = containsCoverageMetadataMismatch(warnings);

        String phase = String.valueOf(status.getPhase());
        int totalBatches = Math.max(0, status.getTotalBatches());
        int completedBatches = Math.max(0, status.getCompletedBatches());
        int failedBatches = Math.max(0, status.getFailedBatches());
        int retries = Math.max(0, status.getRetries());

        // New UX-friendly fields derived from existing status model (no model changes required)
        String activity = deriveActivityText(
                phase,
                safe(status.getMessage()),
                totalBatches,
                completedBatches,
                failedBatches
        );
        int batchPercent = totalBatches <= 0 ? 0 : Math.max(0, Math.min(100, (int) Math.round((completedBatches * 100.0) / totalBatches)));
        boolean running = !status.isDone();

        JsonObjectBuilder coverage = Json.createObjectBuilder()
                .add("totalSelected", total)
                .add("usedCount", used)
                .add("missingCount", missing)
                .add("coveragePercentDerived", percent)
                .add("coveragePercentReported", status.getCoveragePercent())
                .add("coverageCompleteDerived", coverageComplete)
                .add("coverageCompleteReported", status.isCoverageComplete())
                .add("coverageMetadataMismatch", metadataMismatch)
                .add("allSelectedChatIds", toJsonArray(allIds))
                .add("usedChatIds", toJsonArray(usedIds))
                .add("missingChatIds", toJsonArray(missingIds));

        JsonObjectBuilder progress = Json.createObjectBuilder()
                .add("phase", phase)
                .add("running", running)
                .add("done", status.isDone())
                .add("success", status.isSuccess())
                .add("message", safe(status.getMessage()))
                .add("activity", activity)
                .add("totalBatches", totalBatches)
                .add("completedBatches", completedBatches)
                .add("failedBatches", failedBatches)
                .add("batchProgressPercent", batchPercent)
                .add("retries", retries)
                .add("failedBatchIndexes", toJsonIntArray(status.getFailedBatchIndexes()))
                .add("warnings", toJsonArray(warnings))
                .add("startedAtEpochMs", Math.max(0L, status.getStartedAtEpochMs()))
                .add("updatedAtEpochMs", Math.max(0L, status.getUpdatedAtEpochMs()))
                .add("finishedAtEpochMs", Math.max(0L, status.getFinishedAtEpochMs()));

        JsonObjectBuilder job = Json.createObjectBuilder(status.toJson())
                .add("allSelectedChatIds", toJsonArray(allIds))
                .add("usedChatIds", toJsonArray(usedIds))
                .add("missingChatIds", toJsonArray(missingIds))
                .add("coveragePercent", percent)
                .add("coverageComplete", coverageComplete)
                .add("warnings", toJsonArray(warnings))
                // mirror UX keys directly under job for backward/forward compatibility
                .add("activity", activity)
                .add("running", running)
                .add("batchProgressPercent", batchPercent);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(Json.createObjectBuilder()
                .add("status", "ok")
                .add("job", job)
                .add("progress", progress)
                .add("coverage", coverage)
                .build()
                .toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);
        String jobId = safe(requestContext.first("jobId", 256));
        if (jobId.isBlank()) {
            respondError(resp, HttpServletResponse.SC_BAD_REQUEST, "jobId is required.");
            return;
        }

        if (!JOB_SERVICE.exists(jobId)) {
            respondError(resp, HttpServletResponse.SC_NOT_FOUND, "Job not found.");
            return;
        }

        boolean cancelled = JOB_SERVICE.cancel(jobId);
        ReviewJobStatus updated = JOB_SERVICE.getStatus(jobId);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(Json.createObjectBuilder()
                .add("status", "ok")
                .add("cancelled", cancelled)
                .add("job", updated == null ? Json.createObjectBuilder().build() : updated.toJson())
                .build()
                .toString());
    }

    static ReviewJobService jobService() {
        return JOB_SERVICE;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            respondError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        return true;
    }

    private void respondError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(Json.createObjectBuilder()
                .add("status", "error")
                .add("message", safe(message))
                .build()
                .toString());
    }

    private jakarta.json.JsonArray toJsonArray(List<String> values) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        if (values != null) {
            for (String v : values) {
                if (v != null) {
                    b.add(v);
                }
            }
        }
        return b.build();
    }

    private jakarta.json.JsonArray toJsonIntArray(List<Integer> values) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        if (values != null) {
            for (Integer v : values) {
                if (v != null) {
                    int intValue = v;
                    b.add(intValue);
                }
            }
        }
        return b.build();
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                continue;
            }
            out.add(id.trim().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(out);
    }

    private List<String> normalizeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                out.add(v.trim());
            }
        }
        return out;
    }

    private boolean containsCoverageMetadataMismatch(List<String> warnings) {
        if (warnings == null) {
            return false;
        }
        for (String w : warnings) {
            if (w != null && w.toLowerCase(Locale.ROOT).contains("coverage metadata mismatch")) {
                return true;
            }
        }
        return false;
    }

    private List<String> subtract(List<String> all, List<String> remove) {
        Set<String> a = new LinkedHashSet<>(normalizeIds(all));
        Set<String> r = new LinkedHashSet<>(normalizeIds(remove));
        a.removeAll(r);
        return new ArrayList<>(a);
    }

    private String deriveActivityText(
            String phase,
            String message,
            int totalBatches,
            int completedBatches,
            int failedBatches
    ) {
        String p = phase == null ? "" : phase.toUpperCase(Locale.ROOT);
        if (!message.isBlank()) {
            return message;
        }

        return switch (p) {
            case "QUEUED" -> "Queued...";
            case "MAP" -> {
                if (totalBatches > 0) {
                    StringBuilder activity = new StringBuilder("Analyzing chats: batch ")
                            .append(Math.max(0, completedBatches))
                            .append('/')
                            .append(totalBatches);
                    if (failedBatches > 0) {
                        activity.append(" (failures: ").append(failedBatches).append(')');
                    }
                    yield activity.toString();
                }
                yield "Analyzing chats...";
            }
            case "REDUCE" -> "Synthesizing final report...";
            case "COMPLETED" -> "Completed";
            case "FAILED" -> "Failed";
            case "CANCELLED" -> "Cancelled";
            default -> "Processing...";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class RequestParamContext {
        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            String normalized = normalize(value, maxLen);
            if (normalized != null) {
                return normalized;
            }
            return null;
        }

        private String normalize(String value, int maxLen) {
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            int effectiveMax = maxLen <= 0 ? 256 : maxLen;
            return trimmed.length() > effectiveMax ? trimmed.substring(0, effectiveMax) : trimmed;
        }
    }
}
