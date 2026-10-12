// src/main/java/com/sim/chatserver/model/review/ReviewJobStatus.java
package com.sim.chatserver.model.review;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Tracks async review job progress for UI polling/progress bar.
 */
public final class ReviewJobStatus {
    private static final String CONTENT_TYPE_APPLICATION_JSON = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String EMPTY = "";
    private static final String FIELD_JOB_ID = "jobId";
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_PHASE = "phase";
    private static final String FIELD_DONE = "done";
    private static final String FIELD_SUCCESS = CommonLiteralConstants.FIELD_SUCCESS;
    private static final String FIELD_PROGRESS_PERCENT = "progressPercent";
    private static final String FIELD_BATCH_PROGRESS_PERCENT = "batchProgressPercent";
    private static final String FIELD_TOTAL_SELECTED = CommonLiteralConstants.FIELD_TOTAL_SELECTED;
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_COMPLETED_BATCHES = "completedBatches";
    private static final String FIELD_FAILED_BATCHES = "failedBatches";
    private static final String FIELD_RETRIES = "retries";
    private static final String FIELD_ALL_SELECTED_CHAT_IDS = CommonLiteralConstants.FIELD_ALL_SELECTED_CHAT_IDS;
    private static final String FIELD_USED_CHAT_IDS = CommonLiteralConstants.FIELD_USED_CHAT_IDS;
    private static final String FIELD_MISSING_CHAT_IDS = CommonLiteralConstants.FIELD_MISSING_CHAT_IDS;
    private static final String FIELD_COVERAGE_PERCENT = "coveragePercent";
    private static final String FIELD_COVERAGE_COMPLETE = CommonLiteralConstants.FIELD_COVERAGE_COMPLETE;
    private static final String FIELD_STARTED_AT_EPOCH_MS = "startedAtEpochMs";
    private static final String FIELD_UPDATED_AT_EPOCH_MS = "updatedAtEpochMs";
    private static final String FIELD_FINISHED_AT_EPOCH_MS = "finishedAtEpochMs";
    private static final String FIELD_HTTP_STATUS = "httpStatus";
    private static final String FIELD_MESSAGE = CommonLiteralConstants.FIELD_MESSAGE;
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";
    private static final String FIELD_FINAL_REPORT = "finalReport";
    private static final String FIELD_RAW_RESPONSE_BODY = "rawResponseBody";
    private static final String FIELD_CONTENT_TYPE = "contentType";
    private static final String FIELD_FAILED_BATCH_INDEXES = CommonLiteralConstants.FIELD_FAILED_BATCH_INDEXES;
    private static final String FIELD_WARNINGS = "warnings";
    private static final String MSG_REQUIRED_SUFFIX = CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String ERR_FINISHED_AT_REQUIRED = "finishedAtEpochMs must be set when done=true";
    private static final String ERR_COMPLETED_EXCEEDS_TOTAL = "completedBatches cannot exceed totalBatches";
    private static final String ERR_MISSING_CHAT_IDS_FORMULA = "missingChatIds must equal allSelectedChatIds - usedChatIds";
    private static final String ERR_COVERAGE_COMPLETE_MISSING = "coverageComplete=true requires empty missingChatIds";
    private static final String ERR_RUNNING_DONE_CONFLICT = "running=true cannot be set when done=true";
    private static final String MSG_QUEUED = "Queued";
    private static final String MSG_QUEUED_ELLIPSIS = "Queued...";
    private static final String MSG_CANCELLED = "Cancelled";
    private static final String MSG_ANALYZING_CHATS_PREFIX = "Analyzing chats: batch ";
    private static final String MSG_ANALYZING_CHATS_ELLIPSIS = "Analyzing chats...";
    private static final String MSG_FAILURES_PREFIX = " (failures: ";
    private static final String MSG_SYNTHESIZING_FINAL_REPORT = "Synthesizing final report...";
    private static final String TOSTRING_PREFIX = "ReviewJobStatus{";
    private static final String TOSTRING_JOB_ID_PREFIX = "jobId='";
    private static final String TOSTRING_REQUEST_ID_PREFIX = ", " + CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_PHASE_PREFIX = ", phase=";
    private static final String TOSTRING_DONE_PREFIX = ", done=";
    private static final String TOSTRING_SUCCESS_PREFIX = ", success=";
    private static final String TOSTRING_RUNNING_PREFIX = ", running=";
    private static final String TOSTRING_BATCH_PROGRESS_PREFIX = ", batchProgressPercent=";
    private static final String TOSTRING_TOTAL_SELECTED_PREFIX = ", totalSelected=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_COMPLETED_BATCHES_PREFIX = ", completedBatches=";
    private static final String TOSTRING_FAILED_BATCHES_PREFIX = ", failedBatches=";
    private static final String TOSTRING_RETRIES_PREFIX = ", retries=";
    private static final String TOSTRING_ALL_SELECTED_PREFIX = ", allSelectedChatIds=";
    private static final String TOSTRING_USED_PREFIX = ", usedChatIds=";
    private static final String TOSTRING_MISSING_PREFIX = ", missingChatIds=";
    private static final String TOSTRING_COVERAGE_PERCENT_PREFIX = ", coveragePercent=";
    private static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.TOSTRING_COVERAGE_COMPLETE_PREFIX;
    private static final String TOSTRING_HTTP_STATUS_PREFIX = ", httpStatus=";
    private static final String JSON_FIELD_RUNNING = CommonLiteralConstants.STATUS_RUNNING;
    private static final String JSON_FIELD_ACTIVITY = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ACTIVITY;
    private static final String ACTIVITY_COMPLETED = "Completed";
    private static final String ACTIVITY_FAILED = "Failed";
    private static final String NUL_CHAR = com.sim.chatserver.util.CommonLiteralConstants.NULL_CHAR_STRING;

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

    ReviewJobStatus(ReviewJobStatusBuilder b) {
        this.jobId = requireNonBlank(b.jobId, FIELD_JOB_ID);
        this.requestId = defaultIfBlank(b.requestId, EMPTY);

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
            for (String usedId : this.usedChatIds) {
                missing.remove(usedId);
            }
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
        this.message = defaultIfBlank(b.message, EMPTY);
        this.errorMessage = defaultIfBlank(b.errorMessage, EMPTY);

        this.finalReport = defaultIfBlank(b.finalReport, EMPTY);
        this.rawResponseBody = defaultIfBlank(b.rawResponseBody, EMPTY);
        this.contentType = defaultIfBlank(b.contentType, CONTENT_TYPE_APPLICATION_JSON);

        this.failedBatchIndexes = immutableDistinctPositiveInts(b.failedBatchIndexes);
        this.warnings = immutableDistinctStringsKeepCase(b.warnings);

        this.running = b.runningSet ? b.running : !this.done;
        this.batchProgressPercent = b.batchProgressPercent >= 0
                ? clampPercent(b.batchProgressPercent)
                : deriveBatchProgressPercent(this.totalBatches, this.completedBatches, this.phase, this.done);

        String defaultActivity = deriveDefaultActivity(this.phase, this.done, this.totalBatches, this.completedBatches, this.failedBatches);
        this.activity = defaultIfBlank(b.activity, defaultActivity);

        validateConsistency();
    }

    private void validateConsistency() {
        if (done && finishedAtEpochMs <= 0) {
            throw new IllegalArgumentException(ERR_FINISHED_AT_REQUIRED);
        }
        if (completedBatches > totalBatches && totalBatches > 0) {
            throw new IllegalArgumentException(ERR_COMPLETED_EXCEEDS_TOTAL);
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        for (String usedId : usedChatIds) {
            expectedMissing.remove(usedId);
        }

        Set<String> providedMissing = new LinkedHashSet<>(missingChatIds);
        if (!allSelectedChatIds.isEmpty() && !expectedMissing.equals(providedMissing)) {
            throw new IllegalArgumentException(ERR_MISSING_CHAT_IDS_FORMULA);
        }

        if (coverageComplete && !missingChatIds.isEmpty()) {
            throw new IllegalArgumentException(ERR_COVERAGE_COMPLETE_MISSING);
        }

        if (running && done) {
            throw new IllegalArgumentException(ERR_RUNNING_DONE_CONFLICT);
        }
    }

    public static ReviewJobStatusBuilder builder() {
        return new ReviewJobStatusBuilder();
    }

    static ReviewJobStatus queued(String jobId, String requestId, int totalSelected) {
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
                .message(MSG_QUEUED)
                .activity(MSG_QUEUED_ELLIPSIS)
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

    int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    String getErrorMessage() {
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

    String getActivity() {
        return activity;
    }

    int getBatchProgressPercent() {
        return batchProgressPercent;
    }

    boolean isRunning() {
        return running;
    }

    boolean hasFinalReport() {
        return !finalReport.isBlank();
    }

    /**
     * UI progress percent across phases.
     *
     * QUEUED => 0..5 MAP => 5..80 REDUCE => 80..95 DONE => 100
     */
    private int progressPercent() {
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
                double ratio = Math.min(
                        1.0d,
                        BigDecimal.valueOf(completedBatches)
                                .divide(BigDecimal.valueOf(totalBatches), 6, RoundingMode.HALF_UP)
                                .doubleValue()
                );
                int mapPercent = safeRoundedPercent(ratio * 75.0);
                if (mapPercent >= 95) {
                    yield 100;
                }
                yield clampPercent(mapPercent + 5);
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
                .add(FIELD_JOB_ID, jobId)
                .add(FIELD_REQUEST_ID, requestId)
                .add(FIELD_PHASE, phase.name())
                .add(FIELD_DONE, done)
                .add(FIELD_SUCCESS, success)
                .add(JSON_FIELD_RUNNING, running)
                .add(FIELD_PROGRESS_PERCENT, progressPercent())
                .add(FIELD_BATCH_PROGRESS_PERCENT, batchProgressPercent)
                .add(JSON_FIELD_ACTIVITY, activity)
                .add(FIELD_TOTAL_SELECTED, totalSelected)
                .add(FIELD_TOTAL_BATCHES, totalBatches)
                .add(FIELD_COMPLETED_BATCHES, completedBatches)
                .add(FIELD_FAILED_BATCHES, failedBatches)
                .add(FIELD_RETRIES, retries)
                .add(FIELD_ALL_SELECTED_CHAT_IDS, toJsonArrayStrings(allSelectedChatIds))
                .add(FIELD_USED_CHAT_IDS, toJsonArrayStrings(usedChatIds))
                .add(FIELD_MISSING_CHAT_IDS, toJsonArrayStrings(missingChatIds))
                .add(FIELD_COVERAGE_PERCENT, coveragePercent)
                .add(FIELD_COVERAGE_COMPLETE, coverageComplete)
                .add(FIELD_STARTED_AT_EPOCH_MS, startedAtEpochMs)
                .add(FIELD_UPDATED_AT_EPOCH_MS, updatedAtEpochMs)
                .add(FIELD_FINISHED_AT_EPOCH_MS, finishedAtEpochMs)
                .add(FIELD_HTTP_STATUS, httpStatus)
                .add(FIELD_MESSAGE, message)
                .add(FIELD_ERROR_MESSAGE, errorMessage)
                .add(FIELD_FINAL_REPORT, finalReport)
                .add(FIELD_RAW_RESPONSE_BODY, safeBodyPreview(rawResponseBody))
                .add(FIELD_CONTENT_TYPE, contentType)
                .add(FIELD_FAILED_BATCH_INDEXES, toJsonArrayInts(failedBatchIndexes))
                .add(FIELD_WARNINGS, toJsonArrayStrings(warnings))
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_JOB_ID_PREFIX + jobId + '\''
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_PHASE_PREFIX + phase
            + TOSTRING_DONE_PREFIX + done
            + TOSTRING_SUCCESS_PREFIX + success
            + TOSTRING_RUNNING_PREFIX + running
            + TOSTRING_BATCH_PROGRESS_PREFIX + batchProgressPercent
            + TOSTRING_TOTAL_SELECTED_PREFIX + totalSelected
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_COMPLETED_BATCHES_PREFIX + completedBatches
            + TOSTRING_FAILED_BATCHES_PREFIX + failedBatches
            + TOSTRING_RETRIES_PREFIX + retries
            + TOSTRING_ALL_SELECTED_PREFIX + allSelectedChatIds.size()
            + TOSTRING_USED_PREFIX + usedChatIds.size()
            + TOSTRING_MISSING_PREFIX + missingChatIds.size()
            + TOSTRING_COVERAGE_PERCENT_PREFIX + coveragePercent
            + TOSTRING_COVERAGE_COMPLETE_PREFIX + coverageComplete
            + TOSTRING_HTTP_STATUS_PREFIX + httpStatus
                + '}';
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + MSG_REQUIRED_SUFFIX);
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
        return safeRoundedPercent((used * 100.0) / total);
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
        return clampPercent(safeRoundedPercent((Math.max(0, completedBatches) * 100.0) / totalBatches));
    }

    private static int safeRoundedPercent(double value) {
        long rounded = Math.round(value);
        if (rounded <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (rounded >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(rounded);
    }

    private static String deriveDefaultActivity(Phase phase, boolean done, int totalBatches, int completedBatches, int failedBatches) {
        if (done) {
            return switch (phase) {
                case COMPLETED ->
                    ACTIVITY_COMPLETED;
                case FAILED ->
                    ACTIVITY_FAILED;
                case CANCELLED ->
                    MSG_CANCELLED;
                default ->
                    ACTIVITY_COMPLETED;
            };
        }

        return switch (phase == null ? Phase.QUEUED : phase) {
            case QUEUED ->
                MSG_QUEUED_ELLIPSIS;
            case MAP ->
                totalBatches > 0
                ? MSG_ANALYZING_CHATS_PREFIX + Math.max(0, completedBatches) + '/' + totalBatches
                + (failedBatches > 0 ? MSG_FAILURES_PREFIX + failedBatches + ')' : EMPTY)
                : MSG_ANALYZING_CHATS_ELLIPSIS;
            case REDUCE ->
                MSG_SYNTHESIZING_FINAL_REPORT;
            case COMPLETED ->
                ACTIVITY_COMPLETED;
            case FAILED ->
                ACTIVITY_FAILED;
            case CANCELLED ->
                MSG_CANCELLED;
            default ->
                MSG_QUEUED_ELLIPSIS;
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
        for (String text : toNumberTexts(src)) {
            int value = Integer.parseInt(text);
            if (value > 0) {
                out.add(Integer.valueOf(value));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static List<String> toNumberTexts(List<? extends Number> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> texts = new ArrayList<>();
        for (Number value : values) {
            if (value != null) {
                texts.add(value.toString());
            }
        }
        return texts;
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
        for (String text : toNumberTexts(values)) {
            b.add(Integer.parseInt(text));
        }
        return b.build();
    }

    private static String safeBodyPreview(String body) {
        if (body == null || body.isBlank()) {
            return EMPTY;
        }
        String normalized = body.replace(NUL_CHAR, EMPTY).replace(System.lineSeparator(), EMPTY).trim();
        int max = 512;
        return normalized.length() > max ? new String(normalized.toCharArray(), 0, max) : normalized;
    }

}

