// src/main/java/com/sim/chatserver/model/review/ReviewJobStatus.java
package com.sim.chatserver.model.review;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Tracks async review job progress for UI polling/progress bar.
 */
public final class ReviewJobStatus {

    public enum Phase {
        QUEUED,
        MAP,
        REDUCE,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    final String jobId;
    final String requestId;

    final Phase phase;
    final boolean done;
    final boolean success;

    final int totalSelected;
    final int totalBatches;
    final int completedBatches;
    final int failedBatches;
    final int retries;

    // richer deterministic coverage tracking
    final List<String> allSelectedChatIds;
    final List<String> usedChatIds;
    final List<String> missingChatIds;

    final int coveragePercent;
    final boolean coverageComplete;

    final long startedAtEpochMs;
    final long updatedAtEpochMs;
    final long finishedAtEpochMs;

    final int httpStatus;
    final String message;
    final String errorMessage;

    // final output payload for async completion UX
    final String finalReport;
    final String rawResponseBody;
    final String contentType;

    final List<Integer> failedBatchIndexes;
    final List<String> warnings;

    // NEW UI-friendly progress/activity fields
    final String activity;
    final int batchProgressPercent;
    final boolean running;

    ReviewJobStatus(Builder b) {
        this.jobId = requireNonBlank(b.jobId, "jobId");
        this.requestId = defaultIfBlank(b.requestId, "");

        this.phase = b.phase == null ? Phase.QUEUED : b.phase;
        this.done = b.done;
        this.success = b.success;

        this.totalSelected = Math.max(0, b.totalSelected);
        this.totalBatches = Math.max(0, b.totalBatches);
        this.completedBatches = Math.max(0, b.completedBatches);
        this.failedBatches = Math.max(0, b.failedBatches);
        this.retries = Math.max(0, b.retries);

        this.allSelectedChatIds = immutableDistinctStringsLower(b.allSelectedChatIds);
        this.usedChatIds = immutableDistinctStringsLower(b.usedChatIds);

        // missing is deterministic: explicit if provided, else all - used
        if (b.missingChatIds == null || b.missingChatIds.isEmpty()) {
            Set<String> missing = new LinkedHashSet<>(this.allSelectedChatIds);
            missing.removeAll(this.usedChatIds);
            this.missingChatIds = Collections.unmodifiableList(new ArrayList<>(missing));
        } else {
            this.missingChatIds = immutableDistinctStringsLower(b.missingChatIds);
        }

        // coverage derived if not explicitly supplied
        int derivedCoverage = deriveCoveragePercent(this.allSelectedChatIds, this.usedChatIds, this.missingChatIds, b.coveragePercent);
        this.coveragePercent = clampPercent(derivedCoverage);

        boolean derivedComplete = this.missingChatIds.isEmpty();
        this.coverageComplete = b.coverageComplete || derivedComplete;

        long now = Instant.now().toEpochMilli();
        this.startedAtEpochMs = b.startedAtEpochMs > 0 ? b.startedAtEpochMs : now;
        this.updatedAtEpochMs = b.updatedAtEpochMs > 0 ? b.updatedAtEpochMs : now;
        this.finishedAtEpochMs = Math.max(0L, b.finishedAtEpochMs);

        this.httpStatus = b.httpStatus;
        this.message = defaultIfBlank(b.message, "");
        this.errorMessage = defaultIfBlank(b.errorMessage, "");

        this.finalReport = defaultIfBlank(b.finalReport, "");
        this.rawResponseBody = defaultIfBlank(b.rawResponseBody, "");
        this.contentType = defaultIfBlank(b.contentType, "application/json");

        this.failedBatchIndexes = immutableDistinctPositiveInts(b.failedBatchIndexes);
        this.warnings = immutableDistinctStringsKeepCase(b.warnings);

        if (b.running == null) {
            this.running = !this.done;
        } else {
            this.running = b.running.booleanValue();
        }
        this.batchProgressPercent = b.batchProgressPercent >= 0
                ? clampPercent(b.batchProgressPercent)
                : deriveBatchProgressPercent(this.totalBatches, this.completedBatches, this.phase, this.done);

        String defaultActivity = deriveDefaultActivity(this.phase, this.done, this.totalBatches, this.completedBatches, this.failedBatches);
        this.activity = defaultIfBlank(b.activity, defaultActivity);

        validateConsistency();
    }

    private void validateConsistency() {
        if (done && finishedAtEpochMs <= 0) {
            throw new IllegalArgumentException("finishedAtEpochMs must be set when done=true");
        }
        if (completedBatches > totalBatches && totalBatches > 0) {
            throw new IllegalArgumentException("completedBatches cannot exceed totalBatches");
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        expectedMissing.removeAll(usedChatIds);

        Set<String> providedMissing = new LinkedHashSet<>(missingChatIds);
        if (!allSelectedChatIds.isEmpty() && !expectedMissing.equals(providedMissing)) {
            throw new IllegalArgumentException("missingChatIds must equal allSelectedChatIds - usedChatIds");
        }

        if (coverageComplete && !missingChatIds.isEmpty()) {
            throw new IllegalArgumentException("coverageComplete=true requires empty missingChatIds");
        }

        if (running && done) {
            throw new IllegalArgumentException("running=true cannot be set when done=true");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ReviewJobStatus queued(String jobId, String requestId, int totalSelected) {
        long now = Instant.now().toEpochMilli();
        return builder()
                .jobId(jobId)
                .requestId(requestId)
                .phase(Phase.QUEUED)
                .done(false)
                .success(false)
                .totalSelected(totalSelected)
                .allSelectedChatIds(List.of())
                .usedChatIds(List.of())
                .missingChatIds(List.of())
                .coveragePercent(0)
                .coverageComplete(false)
                .startedAtEpochMs(now)
                .updatedAtEpochMs(now)
                .message("Queued")
                .activity("Queued...")
                .running(true)
                .batchProgressPercent(0)
                .build();
    }

    public String getJobId() {
        return jobId;
    }

    public String getRequestId() {
        return requestId;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTotalSelected() {
        return totalSelected;
    }

    public int getTotalBatches() {
        return totalBatches;
    }

    public int getCompletedBatches() {
        return completedBatches;
    }

    public int getFailedBatches() {
        return failedBatches;
    }

    public int getRetries() {
        return retries;
    }

    public List<String> getAllSelectedChatIds() {
        return allSelectedChatIds;
    }

    public List<String> getUsedChatIds() {
        return usedChatIds;
    }

    public List<String> getMissingChatIds() {
        return missingChatIds;
    }

    public int getCoveragePercent() {
        return coveragePercent;
    }

    public boolean isCoverageComplete() {
        return coverageComplete;
    }

    public long getStartedAtEpochMs() {
        return startedAtEpochMs;
    }

    public long getUpdatedAtEpochMs() {
        return updatedAtEpochMs;
    }

    public long getFinishedAtEpochMs() {
        return finishedAtEpochMs;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFinalReport() {
        return finalReport;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public String getContentType() {
        return contentType;
    }

    public List<Integer> getFailedBatchIndexes() {
        return failedBatchIndexes;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public String getActivity() {
        return activity;
    }

    public int getBatchProgressPercent() {
        return batchProgressPercent;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean hasFinalReport() {
        return !finalReport.isBlank();
    }

    /**
     * UI progress percent across phases.
     *
     * QUEUED => 0..5 MAP => 5..80 REDUCE => 80..95 DONE => 100
     */
    public int progressPercent() {
        if (done) {
            return 100;
        }

        return switch (phase) {
            case QUEUED ->
                5;
            case MAP -> {
                if (totalBatches <= 0) {
                    yield 20;
                }
                double ratio = Math.min(1.0, completedBatches / (double) totalBatches);
                yield 5 + (int) Math.round(ratio * 75.0);
            }
            case REDUCE ->
                90;
            case COMPLETED, FAILED, CANCELLED ->
                100;
            default ->
                100;
        };
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("jobId", jobId)
                .add("requestId", requestId)
                .add("phase", phase.name())
                .add("done", done)
                .add("success", success)
                .add("running", running)
                .add("progressPercent", progressPercent())
                .add("batchProgressPercent", batchProgressPercent)
                .add("activity", activity)
                .add("totalSelected", totalSelected)
                .add("totalBatches", totalBatches)
                .add("completedBatches", completedBatches)
                .add("failedBatches", failedBatches)
                .add("retries", retries)
                .add("allSelectedChatIds", toJsonArrayStrings(allSelectedChatIds))
                .add("usedChatIds", toJsonArrayStrings(usedChatIds))
                .add("missingChatIds", toJsonArrayStrings(missingChatIds))
                .add("coveragePercent", coveragePercent)
                .add("coverageComplete", coverageComplete)
                .add("startedAtEpochMs", startedAtEpochMs)
                .add("updatedAtEpochMs", updatedAtEpochMs)
                .add("finishedAtEpochMs", finishedAtEpochMs)
                .add("httpStatus", httpStatus)
                .add("message", message)
                .add("errorMessage", errorMessage)
                .add("finalReport", finalReport)
                .add("rawResponseBody", safeBodyPreview(rawResponseBody))
                .add("contentType", contentType)
                .add("failedBatchIndexes", toJsonArrayInts(failedBatchIndexes))
                .add("warnings", toJsonArrayStrings(warnings))
                .build();
    }

    @Override
    public String toString() {
        return "ReviewJobStatus{"
                + "jobId='" + jobId + '\''
                + ", requestId='" + requestId + '\''
                + ", phase=" + phase
                + ", done=" + done
                + ", success=" + success
                + ", running=" + running
                + ", batchProgressPercent=" + batchProgressPercent
                + ", totalSelected=" + totalSelected
                + ", totalBatches=" + totalBatches
                + ", completedBatches=" + completedBatches
                + ", failedBatches=" + failedBatches
                + ", retries=" + retries
                + ", allSelectedChatIds=" + allSelectedChatIds.size()
                + ", usedChatIds=" + usedChatIds.size()
                + ", missingChatIds=" + missingChatIds.size()
                + ", coveragePercent=" + coveragePercent
                + ", coverageComplete=" + coverageComplete
                + ", httpStatus=" + httpStatus
                + '}';
    }

    public static final class Builder {

        String jobId;
        String requestId;

        Phase phase;
        boolean done;
        boolean success;

        int totalSelected;
        int totalBatches;
        int completedBatches;
        int failedBatches;
        int retries;

        List<String> allSelectedChatIds = new ArrayList<>();
        List<String> usedChatIds = new ArrayList<>();
        List<String> missingChatIds = new ArrayList<>();

        int coveragePercent;
        boolean coverageComplete;

        long startedAtEpochMs;
        long updatedAtEpochMs;
        long finishedAtEpochMs;

        int httpStatus;
        String message;
        String errorMessage;

        String finalReport;
        String rawResponseBody;
        String contentType;

        List<Integer> failedBatchIndexes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String activity;
        int batchProgressPercent = -1;
        Boolean running;

        Builder() {
        }

        public Builder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder phase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public Builder done(boolean done) {
            this.done = done;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder totalSelected(int totalSelected) {
            this.totalSelected = totalSelected;
            return this;
        }

        public Builder totalBatches(int totalBatches) {
            this.totalBatches = totalBatches;
            return this;
        }

        public Builder completedBatches(int completedBatches) {
            this.completedBatches = completedBatches;
            return this;
        }

        public Builder failedBatches(int failedBatches) {
            this.failedBatches = failedBatches;
            return this;
        }

        public Builder retries(int retries) {
            this.retries = retries;
            return this;
        }

        public Builder allSelectedChatIds(List<String> allSelectedChatIds) {
            this.allSelectedChatIds = allSelectedChatIds == null ? new ArrayList<>() : allSelectedChatIds;
            return this;
        }

        public Builder usedChatIds(List<String> usedChatIds) {
            this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
            return this;
        }

        public Builder missingChatIds(List<String> missingChatIds) {
            this.missingChatIds = missingChatIds == null ? new ArrayList<>() : missingChatIds;
            return this;
        }

        public Builder coveragePercent(int coveragePercent) {
            this.coveragePercent = coveragePercent;
            return this;
        }

        public Builder coverageComplete(boolean coverageComplete) {
            this.coverageComplete = coverageComplete;
            return this;
        }

        public Builder startedAtEpochMs(long startedAtEpochMs) {
            this.startedAtEpochMs = startedAtEpochMs;
            return this;
        }

        public Builder updatedAtEpochMs(long updatedAtEpochMs) {
            this.updatedAtEpochMs = updatedAtEpochMs;
            return this;
        }

        public Builder finishedAtEpochMs(long finishedAtEpochMs) {
            this.finishedAtEpochMs = finishedAtEpochMs;
            return this;
        }

        public Builder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder finalReport(String finalReport) {
            this.finalReport = finalReport;
            return this;
        }

        public Builder rawResponseBody(String rawResponseBody) {
            this.rawResponseBody = rawResponseBody;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder failedBatchIndexes(List<Integer> failedBatchIndexes) {
            this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings == null ? new ArrayList<>() : warnings;
            return this;
        }

        public Builder activity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder batchProgressPercent(int batchProgressPercent) {
            this.batchProgressPercent = batchProgressPercent;
            return this;
        }

        public Builder running(boolean running) {
            this.running = running ? Boolean.TRUE : Boolean.FALSE;
            return this;
        }

        public ReviewJobStatus build() {
            return new ReviewJobStatus(this);
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static int clampPercent(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(100, value);
    }

    private static int deriveCoveragePercent(List<String> allIds, List<String> usedIds, List<String> missingIds, int fallback) {
        int total = allIds == null ? 0 : allIds.size();
        if (total <= 0) {
            return fallback;
        }
        int used = usedIds == null ? 0 : usedIds.size();
        if (missingIds != null && !missingIds.isEmpty()) {
            used = Math.max(0, total - missingIds.size());
        }
        return (int) Math.round((used * 100.0) / total);
    }

    private static int deriveBatchProgressPercent(int totalBatches, int completedBatches, Phase phase, boolean done) {
        if (done || phase == Phase.COMPLETED || phase == Phase.FAILED || phase == Phase.CANCELLED) {
            return 100;
        }
        if (phase == Phase.REDUCE) {
            return 95;
        }
        if (phase == Phase.QUEUED) {
            return 0;
        }
        if (totalBatches <= 0) {
            return phase == Phase.MAP ? 20 : 0;
        }
        return clampPercent((int) Math.round((Math.max(0, completedBatches) * 100.0) / totalBatches));
    }

    private static String deriveDefaultActivity(Phase phase, boolean done, int totalBatches, int completedBatches, int failedBatches) {
        if (done) {
            return switch (phase) {
                case COMPLETED ->
                    "Completed";
                case FAILED ->
                    "Failed";
                case CANCELLED ->
                    "Cancelled";
                default ->
                    "Completed";
            };
        }

        return switch (phase == null ? Phase.QUEUED : phase) {
            case QUEUED ->
                "Queued...";
            case MAP ->
                totalBatches > 0
                ? "Analyzing chats: batch " + Math.max(0, completedBatches) + '/' + totalBatches
                + (failedBatches > 0 ? " (failures: " + failedBatches + ')' : "")
                : "Analyzing chats...";
            case REDUCE ->
                "Synthesizing final report...";
            case COMPLETED ->
                "Completed";
            case FAILED ->
                "Failed";
            case CANCELLED ->
                "Cancelled";
            default ->
                "Queued...";
        };
    }

    private static List<String> immutableDistinctStringsLower(List<String> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String s : src) {
            if (s != null && !s.isBlank()) {
                out.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static List<String> immutableDistinctStringsKeepCase(List<String> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String s : src) {
            if (s != null && !s.isBlank()) {
                out.add(s.trim());
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static List<Integer> immutableDistinctPositiveInts(List<Integer> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        Set<Integer> out = new LinkedHashSet<>();
        for (Integer boxed : src) {
            if (boxed == null) {
                continue;
            }
            int value = boxed.intValue();
            if (value > 0) {
                out.add(Integer.valueOf(value));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static jakarta.json.JsonArray toJsonArrayStrings(List<String> values) {
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

    private static jakarta.json.JsonArray toJsonArrayInts(List<Integer> values) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        if (values != null) {
            for (Integer v : values) {
                if (v != null) {
                    b.add(v.intValue());
                }
            }
        }
        return b.build();
    }

    private static String safeBodyPreview(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String normalized = body.replace("\u0000", "").replace("\r", "").trim();
        int max = 512;
        return normalized.length() > max ? normalized.substring(0, max) : normalized;
    }
}
