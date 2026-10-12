// src/main/java/com/sim/chatserver/model/review/ReduceResult.java
package com.sim.chatserver.model.review;

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
 * Immutable result model for reduce-phase synthesis.
 *
 * Deterministic coverage fields: - allSelectedChatIds - usedChatIds -
 * missingChatIds - coverageComplete
 *
 * Strict rules: - missingChatIds is always derived as allSelected - used -
 * success can be true only when coverageComplete == true and httpStatus < 400 -
 * exact missing IDs are preserved in output
 */
public final class ReduceResult {

    private static final String EMPTY = "";
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_HTTP_STATUS = "httpStatus";
    private static final String FIELD_SUCCESS = CommonLiteralConstants.FIELD_SUCCESS;
    private static final String FIELD_RETRY_USED = "retryUsed";
    private static final String FIELD_CONTEXT_TOO_LARGE_DETECTED = "contextTooLargeDetected";
    private static final String FIELD_FINAL_REPORT = "finalReport";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";
    private static final String FIELD_TOTAL_SELECTED = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL_SELECTED;
    private static final String FIELD_TOTAL_BATCHES = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_MAP_OUTPUTS_RECEIVED = "mapOutputsReceived";
    private static final String FIELD_FAILED_BATCH_COUNT = "failedBatchCount";
    private static final String FIELD_FAILED_BATCH_INDEXES = com.sim.chatserver.util.CommonLiteralConstants.FIELD_FAILED_BATCH_INDEXES;
    private static final String FIELD_FAILED_BATCH_REASONS = "failedBatchReasons";
    private static final String FIELD_ALL_SELECTED_CHAT_IDS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ALL_SELECTED_CHAT_IDS;
    private static final String FIELD_USED_CHAT_IDS = CommonLiteralConstants.FIELD_USED_CHAT_IDS;
    private static final String FIELD_MISSING_CHAT_IDS = CommonLiteralConstants.FIELD_MISSING_CHAT_IDS;
    private static final String FIELD_COVERAGE_COMPLETE = CommonLiteralConstants.FIELD_COVERAGE_COMPLETE;
    private static final String FIELD_COVERAGE_PERCENT = "coveragePercent";
    private static final String FIELD_LATENCY_MS = CommonLiteralConstants.FIELD_LATENCY_MS;
    private static final String ERR_MAP_OUTPUTS_GT_BATCHES = "mapOutputsReceived cannot exceed totalBatches";
    private static final String ERR_MISSING_IDS_MISMATCH = "missingChatIds must exactly equal allSelectedChatIds - usedChatIds";
    private static final String ERR_COVERAGE_COMPLETE_MISMATCH = "coverageComplete must equal missingChatIds.isEmpty()";
    private static final String ERR_SUCCESS_CONSTRAINT = "success=true requires coverageComplete=true and httpStatus<400";
    private static final String ERR_REQUIRED_SUFFIX = com.sim.chatserver.util.CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String TOSTRING_PREFIX = "ReduceResult{";
    private static final String TOSTRING_REQUEST_ID_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_HTTP_STATUS_PREFIX = ", httpStatus=";
    private static final String TOSTRING_SUCCESS_PREFIX = ", success=";
    private static final String TOSTRING_RETRY_USED_PREFIX = ", retryUsed=";
    private static final String TOSTRING_CONTEXT_TOO_LARGE_PREFIX = ", contextTooLargeDetected=";
    private static final String TOSTRING_TOTAL_SELECTED_PREFIX = ", totalSelected=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_MAP_OUTPUTS_PREFIX = ", mapOutputsReceived=";
    private static final String TOSTRING_FAILED_BATCHES_PREFIX = ", failedBatchIndexes=";
    private static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.TOSTRING_COVERAGE_COMPLETE_PREFIX;
    private static final String TOSTRING_COVERAGE_PERCENT_PREFIX = ", coveragePercent=";
    private static final String TOSTRING_MISSING_IDS_PREFIX = ", missingChatIds=";
    private static final String TOSTRING_LATENCY_PREFIX = ", latencyMs=";

    private final String requestId;

    private final int httpStatus;
    private final boolean success;
    private final boolean retryUsed;
    private final boolean contextTooLargeDetected;

    private final String finalReport;
    private final String errorMessage;

    private final int totalSelected;
    private final int totalBatches;
    private final int mapOutputsReceived;

    private final List<Integer> failedBatchIndexes;
    private final List<String> failedBatchReasons;

    private final List<String> allSelectedChatIds;
    private final List<String> usedChatIds;
    private final List<String> missingChatIds;
    private final boolean coverageComplete;
    private final int coveragePercent;

    private final long latencyMs;

    ReduceResult(ReduceResultBuilder b) {
        this.requestId = requireNonBlank(b.requestId, FIELD_REQUEST_ID);

        this.httpStatus = b.httpStatus;
        this.retryUsed = b.retryUsed;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.finalReport = defaultIfNull(b.finalReport, EMPTY);
        this.errorMessage = defaultIfNull(b.errorMessage, EMPTY);

        this.totalSelected = Math.max(0, b.totalSelected);
        this.totalBatches = Math.max(0, b.totalBatches);
        this.mapOutputsReceived = Math.max(0, b.mapOutputsReceived);

        this.failedBatchIndexes = immutableIntListDistinct(b.failedBatchIndexes);
        this.failedBatchReasons = immutableStringListDistinctKeepCase(b.failedBatchReasons);

        this.allSelectedChatIds = immutableStringListDistinctLower(b.allSelectedChatIds);
        this.usedChatIds = immutableStringListDistinctLower(b.usedChatIds);

        // strict deterministic: always derive missing from all-used
        Set<String> missing = new LinkedHashSet<>(this.allSelectedChatIds);
        for (String usedId : this.usedChatIds) {
            missing.remove(usedId);
        }
        this.missingChatIds = Collections.unmodifiableList(new ArrayList<>(missing));

        this.coverageComplete = this.missingChatIds.isEmpty();
        this.coveragePercent = computeCoveragePercent(this.allSelectedChatIds.size(), this.usedChatIds.size());

        // success is derived strictly; cannot override coverage gate
        this.success = b.success && this.coverageComplete && this.httpStatus < 400;

        this.latencyMs = Math.max(0L, b.latencyMs);

        validateConsistency();
    }

    private void validateConsistency() {
        if (mapOutputsReceived > totalBatches && totalBatches > 0) {
            throw new IllegalArgumentException(ERR_MAP_OUTPUTS_GT_BATCHES);
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        for (String usedId : usedChatIds) {
            expectedMissing.remove(usedId);
        }
        if (!expectedMissing.equals(new LinkedHashSet<>(missingChatIds))) {
            throw new IllegalArgumentException(ERR_MISSING_IDS_MISMATCH);
        }

        if (coverageComplete != missingChatIds.isEmpty()) {
            throw new IllegalArgumentException(ERR_COVERAGE_COMPLETE_MISMATCH);
        }

        if (success && (!coverageComplete || httpStatus >= 400)) {
            throw new IllegalArgumentException(ERR_SUCCESS_CONSTRAINT);
        }
    }

    public static ReduceResultBuilder builder() {
        return new ReduceResultBuilder();
    }

    static ReduceResult failed(
            String requestId,
            int httpStatus,
            String errorMessage,
            int totalSelected,
            int totalBatches,
            int mapOutputsReceived,
            List<Integer> failedBatchIndexes
    ) {
        return ReduceResult.builder()
                .requestId(requestId)
                .httpStatus(httpStatus)
                .success(false)
                .errorMessage(errorMessage)
                .totalSelected(totalSelected)
                .totalBatches(totalBatches)
                .mapOutputsReceived(mapOutputsReceived)
                .failedBatchIndexes(failedBatchIndexes)
                .build();
    }

    String getRequestId() {
        return requestId;
    }

    int getHttpStatus() {
        return httpStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    boolean isRetryUsed() {
        return retryUsed;
    }

    public boolean isContextTooLargeDetected() {
        return contextTooLargeDetected;
    }

    public String getFinalReport() {
        return finalReport;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    int getTotalSelected() {
        return totalSelected;
    }

    int getTotalBatches() {
        return totalBatches;
    }

    int getMapOutputsReceived() {
        return mapOutputsReceived;
    }

    List<Integer> getFailedBatchIndexes() {
        return failedBatchIndexes;
    }

    List<String> getFailedBatchReasons() {
        return failedBatchReasons;
    }

    List<String> getAllSelectedChatIds() {
        return allSelectedChatIds;
    }

    List<String> getUsedChatIds() {
        return usedChatIds;
    }

    public List<String> getMissingChatIds() {
        return missingChatIds;
    }

    public boolean isCoverageComplete() {
        return coverageComplete;
    }

    int getCoveragePercent() {
        return coveragePercent;
    }

    long getLatencyMs() {
        return latencyMs;
    }

    private int getFailedBatchCount() {
        return failedBatchIndexes.size();
    }

    boolean hasFailures() {
        return !failedBatchIndexes.isEmpty();
    }

    boolean hasFinalReport() {
        return !finalReport.isBlank();
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
            .add(FIELD_REQUEST_ID, requestId)
            .add(FIELD_HTTP_STATUS, httpStatus)
            .add(FIELD_SUCCESS, success)
            .add(FIELD_RETRY_USED, retryUsed)
            .add(FIELD_CONTEXT_TOO_LARGE_DETECTED, contextTooLargeDetected)
            .add(FIELD_FINAL_REPORT, finalReport)
            .add(FIELD_ERROR_MESSAGE, errorMessage)
            .add(FIELD_TOTAL_SELECTED, totalSelected)
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_MAP_OUTPUTS_RECEIVED, mapOutputsReceived)
            .add(FIELD_FAILED_BATCH_COUNT, getFailedBatchCount())
            .add(FIELD_FAILED_BATCH_INDEXES, toJsonIntArray(failedBatchIndexes))
            .add(FIELD_FAILED_BATCH_REASONS, toJsonStringArray(failedBatchReasons))
            .add(FIELD_ALL_SELECTED_CHAT_IDS, toJsonStringArray(allSelectedChatIds))
            .add(FIELD_USED_CHAT_IDS, toJsonStringArray(usedChatIds))
            .add(FIELD_MISSING_CHAT_IDS, toJsonStringArray(missingChatIds)) // exact derived missing IDs preserved
            .add(FIELD_COVERAGE_COMPLETE, coverageComplete)
            .add(FIELD_COVERAGE_PERCENT, coveragePercent)
            .add(FIELD_LATENCY_MS, latencyMs)
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_HTTP_STATUS_PREFIX + httpStatus
            + TOSTRING_SUCCESS_PREFIX + success
            + TOSTRING_RETRY_USED_PREFIX + retryUsed
            + TOSTRING_CONTEXT_TOO_LARGE_PREFIX + contextTooLargeDetected
            + TOSTRING_TOTAL_SELECTED_PREFIX + totalSelected
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_MAP_OUTPUTS_PREFIX + mapOutputsReceived
            + TOSTRING_FAILED_BATCHES_PREFIX + failedBatchIndexes
            + TOSTRING_COVERAGE_COMPLETE_PREFIX + coverageComplete
            + TOSTRING_COVERAGE_PERCENT_PREFIX + coveragePercent
            + TOSTRING_MISSING_IDS_PREFIX + missingChatIds.size()
            + TOSTRING_LATENCY_PREFIX + latencyMs
                + '}';
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + ERR_REQUIRED_SUFFIX);
        }
        return value;
    }

    private static String defaultIfNull(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static int computeCoveragePercent(int total, int used) {
        if (total <= 0) {
            return 0;
        }
        int p = Math.toIntExact(Math.round((used * 100.0) / total));
        if (p < 0) {
            return 0;
        }
        return Math.min(100, p);
    }

    private static List<Integer> immutableIntListDistinct(List<Integer> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        Set<Integer> out = new LinkedHashSet<>();
        for (String text : toNumberTexts(src)) {
            out.add(Integer.valueOf(Integer.parseInt(text)));
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

    private static List<String> immutableStringListDistinctLower(List<String> src) {
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

    private static List<String> immutableStringListDistinctKeepCase(List<String> src) {
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

    private static jakarta.json.JsonArray toJsonIntArray(List<Integer> values) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for (String text : toNumberTexts(values)) {
            b.add(Integer.parseInt(text));
        }
        return b.build();
    }

    private static jakarta.json.JsonArray toJsonStringArray(List<String> values) {
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

}

