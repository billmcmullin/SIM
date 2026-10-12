// src/main/java/com/sim/chatserver/model/review/MapBatchResult.java
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
 * Immutable result model for one map batch execution.
 *
 * Strict derived coverage metadata: - expectedChatIds (authoritative) -
 * foundChatIds (parsed/detected, constrained to expected) -
 * missingExpectedChatIds (expected - found, always recomputed) -
 * coverageComplete (always derived from missingExpectedChatIds)
 *
 * Strict success rule: - success can only be true when coverageComplete is true
 * and httpStatus < 400
 */
public final class MapBatchResult {

    private static final String EMPTY = "";
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_BATCH_INDEX = "batchIndex";
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_BATCH_ID = "batchId";
    private static final String FIELD_HTTP_STATUS = "httpStatus";
    private static final String FIELD_SUCCESS = CommonLiteralConstants.FIELD_SUCCESS;
    private static final String FIELD_RETRY_USED = "retryUsed";
    private static final String FIELD_CONTEXT_TOO_LARGE_DETECTED = "contextTooLargeDetected";
    private static final String FIELD_MODEL_OUTPUT = "modelOutput";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";
    private static final String FIELD_INPUT_ENTRIES_COUNT = "inputEntriesCount";
    private static final String FIELD_USED_ENTRIES_COUNT = "usedEntriesCount";
    private static final String FIELD_OMITTED_ENTRIES_COUNT = "omittedEntriesCount";
    private static final String FIELD_BATCH_CHAT_IDS = "batchChatIds";
    private static final String FIELD_USED_CHAT_IDS = CommonLiteralConstants.FIELD_USED_CHAT_IDS;
    private static final String FIELD_OMITTED_CHAT_IDS = "omittedChatIds";
    private static final String FIELD_EXPECTED_CHAT_IDS = "expectedChatIds";
    private static final String FIELD_FOUND_CHAT_IDS = "foundChatIds";
    private static final String FIELD_MISSING_EXPECTED_CHAT_IDS = "missingExpectedChatIds";
    private static final String FIELD_COVERAGE_COMPLETE = CommonLiteralConstants.FIELD_COVERAGE_COMPLETE;
    private static final String FIELD_LATENCY_MS = CommonLiteralConstants.FIELD_LATENCY_MS;
    private static final String BATCH_ID_PREFIX = "batch-";
    private static final String ERR_BATCH_INDEX_GT_TOTAL = "batchIndex cannot be greater than totalBatches";
    private static final String ERR_USED_GT_INPUT = "usedEntriesCount cannot exceed inputEntriesCount";
    private static final String ERR_FOUND_SUBSET_EXPECTED = "foundChatIds must be subset of expectedChatIds";
    private static final String ERR_MISSING_EQUALS_DIFF = "missingExpectedChatIds must equal expectedChatIds - foundChatIds";
    private static final String ERR_COVERAGE_COMPLETE_MISMATCH = "coverageComplete must match missingExpectedChatIds emptiness";
    private static final String ERR_USED_EQUALS_FOUND = "usedChatIds must match foundChatIds in strict mode";
    private static final String ERR_OMITTED_EQUALS_MISSING = "omittedChatIds must match missingExpectedChatIds in strict mode";
    private static final String ERR_SUCCESS_RULE = "success=true requires coverageComplete=true and httpStatus<400";
    private static final String ERR_REQUIRED_SUFFIX = CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String ERR_MUST_BE_POSITIVE_SUFFIX = " must be > 0";
    private static final String TOSTRING_PREFIX = "MapBatchResult{";
    private static final String TOSTRING_REQUEST_ID_PREFIX = CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_BATCH_INDEX_PREFIX = ", batchIndex=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_BATCH_ID_PREFIX = ", batchId='";
    private static final String TOSTRING_HTTP_STATUS_PREFIX = ", httpStatus=";
    private static final String TOSTRING_SUCCESS_PREFIX = ", success=";
    private static final String TOSTRING_RETRY_USED_PREFIX = ", retryUsed=";
    private static final String TOSTRING_CONTEXT_TOO_LARGE_PREFIX = ", contextTooLargeDetected=";
    private static final String TOSTRING_INPUT_COUNT_PREFIX = ", inputEntriesCount=";
    private static final String TOSTRING_USED_COUNT_PREFIX = ", usedEntriesCount=";
    private static final String TOSTRING_OMITTED_COUNT_PREFIX = ", omittedEntriesCount=";
    private static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = CommonLiteralConstants.TOSTRING_COVERAGE_COMPLETE_PREFIX;
    private static final String TOSTRING_MISSING_SIZE_PREFIX = ", missingExpectedChatIds=";
    private static final String TOSTRING_LATENCY_PREFIX = ", latencyMs=";

    private final String requestId;
    private final int batchIndex;
    private final int totalBatches;
    private final String batchId;

    private final int httpStatus;
    private final boolean success;
    private final boolean retryUsed;
    private final boolean contextTooLargeDetected;

    private final String modelOutput;
    private final String errorMessage;

    private final int inputEntriesCount;
    private final int usedEntriesCount;
    private final int omittedEntriesCount;

    private final List<String> batchChatIds;
    private final List<String> usedChatIds;
    private final List<String> omittedChatIds;

    private final List<String> expectedChatIds;
    private final List<String> foundChatIds;
    private final List<String> missingExpectedChatIds;
    private final boolean coverageComplete;

    private final long latencyMs;

    MapBatchResult(MapBatchResultBuilder b) {
        this.requestId = requireNonBlank(b.requestId, FIELD_REQUEST_ID);
        this.batchIndex = requirePositive(b.batchIndex, FIELD_BATCH_INDEX);
        this.totalBatches = requirePositive(b.totalBatches, FIELD_TOTAL_BATCHES);
        this.batchId = defaultIfBlank(b.batchId, BATCH_ID_PREFIX + this.batchIndex);

        this.httpStatus = b.httpStatus;
        this.retryUsed = b.retryUsed;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.modelOutput = defaultIfNull(b.modelOutput, EMPTY);
        this.errorMessage = defaultIfNull(b.errorMessage, EMPTY);

        this.inputEntriesCount = Math.max(0, b.inputEntriesCount);

        this.batchChatIds = immutableCopyDistinct(b.batchChatIds);

        this.expectedChatIds = immutableCopyDistinct(
                (b.expectedChatIds == null || b.expectedChatIds.isEmpty()) ? this.batchChatIds : b.expectedChatIds
        );

        List<String> foundRaw = immutableCopyDistinct(
                (b.foundChatIds == null || b.foundChatIds.isEmpty()) ? b.usedChatIds : b.foundChatIds
        );

        // found must be subset of expected
        this.foundChatIds = intersectOrdered(foundRaw, this.expectedChatIds);

        // strict aliases
        this.usedChatIds = this.foundChatIds;

        // always derived
        this.missingExpectedChatIds = subtractOrdered(this.expectedChatIds, this.foundChatIds);
        this.omittedChatIds = this.missingExpectedChatIds;

        this.coverageComplete = this.missingExpectedChatIds.isEmpty();

        this.usedEntriesCount = this.foundChatIds.size();
        this.omittedEntriesCount = this.missingExpectedChatIds.size();

        // strict success derivation (ignore inconsistent overrides)
        this.success = b.success && this.coverageComplete && this.httpStatus < 400;

        this.latencyMs = Math.max(0L, b.latencyMs);

        validateConsistency();
    }

    private void validateConsistency() {
        if (batchIndex > totalBatches) {
            throw new IllegalArgumentException(ERR_BATCH_INDEX_GT_TOTAL);
        }

        if (usedEntriesCount > inputEntriesCount && inputEntriesCount > 0) {
            throw new IllegalArgumentException(ERR_USED_GT_INPUT);
        }

        Set<String> expected = new LinkedHashSet<>(expectedChatIds);
        Set<String> found = new LinkedHashSet<>(foundChatIds);

        if (!expected.containsAll(found)) {
            throw new IllegalArgumentException(ERR_FOUND_SUBSET_EXPECTED);
        }

        List<String> recomputedMissing = subtractOrdered(expectedChatIds, foundChatIds);
        if (!recomputedMissing.equals(missingExpectedChatIds)) {
            throw new IllegalArgumentException(ERR_MISSING_EQUALS_DIFF);
        }

        if (coverageComplete != missingExpectedChatIds.isEmpty()) {
            throw new IllegalArgumentException(ERR_COVERAGE_COMPLETE_MISMATCH);
        }

        if (!usedChatIds.equals(foundChatIds)) {
            throw new IllegalArgumentException(ERR_USED_EQUALS_FOUND);
        }

        if (!omittedChatIds.equals(missingExpectedChatIds)) {
            throw new IllegalArgumentException(ERR_OMITTED_EQUALS_MISSING);
        }

        if (success && (!coverageComplete || httpStatus >= 400)) {
            throw new IllegalArgumentException(ERR_SUCCESS_RULE);
        }
    }

    public static MapBatchResultBuilder builder() {
        return new MapBatchResultBuilder();
    }

    static MapBatchResult failed(
            String requestId,
            int batchIndex,
            int totalBatches,
            String batchId,
            int httpStatus,
            String errorMessage
    ) {
        return builder()
                .requestId(requestId)
                .batchIndex(batchIndex)
                .totalBatches(totalBatches)
                .batchId(batchId)
                .httpStatus(httpStatus)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    String getRequestId() {
        return requestId;
    }

    int getBatchIndex() {
        return batchIndex;
    }

    int getTotalBatches() {
        return totalBatches;
    }

    String getBatchId() {
        return batchId;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isRetryUsed() {
        return retryUsed;
    }

    public boolean isContextTooLargeDetected() {
        return contextTooLargeDetected;
    }

    String getModelOutput() {
        return modelOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    int getInputEntriesCount() {
        return inputEntriesCount;
    }

    int getUsedEntriesCount() {
        return usedEntriesCount;
    }

    int getOmittedEntriesCount() {
        return omittedEntriesCount;
    }

    List<String> getBatchChatIds() {
        return batchChatIds;
    }

    List<String> getUsedChatIds() {
        return usedChatIds;
    }

    List<String> getOmittedChatIds() {
        return omittedChatIds;
    }

    public List<String> getExpectedChatIds() {
        return expectedChatIds;
    }

    List<String> getFoundChatIds() {
        return foundChatIds;
    }

    List<String> getMissingExpectedChatIds() {
        return missingExpectedChatIds;
    }

    boolean isCoverageComplete() {
        return coverageComplete;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    boolean hasModelOutput() {
        return !modelOutput.isBlank();
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
            .add(FIELD_REQUEST_ID, requestId)
            .add(FIELD_BATCH_INDEX, batchIndex)
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_BATCH_ID, batchId)
            .add(FIELD_HTTP_STATUS, httpStatus)
            .add(FIELD_SUCCESS, success)
            .add(FIELD_RETRY_USED, retryUsed)
            .add(FIELD_CONTEXT_TOO_LARGE_DETECTED, contextTooLargeDetected)
            .add(FIELD_MODEL_OUTPUT, modelOutput)
            .add(FIELD_ERROR_MESSAGE, errorMessage)
            .add(FIELD_INPUT_ENTRIES_COUNT, inputEntriesCount)
            .add(FIELD_USED_ENTRIES_COUNT, usedEntriesCount)
            .add(FIELD_OMITTED_ENTRIES_COUNT, omittedEntriesCount)
            .add(FIELD_BATCH_CHAT_IDS, toJsonArray(batchChatIds))
            .add(FIELD_USED_CHAT_IDS, toJsonArray(usedChatIds))
            .add(FIELD_OMITTED_CHAT_IDS, toJsonArray(omittedChatIds))
            .add(FIELD_EXPECTED_CHAT_IDS, toJsonArray(expectedChatIds))
            .add(FIELD_FOUND_CHAT_IDS, toJsonArray(foundChatIds))
            .add(FIELD_MISSING_EXPECTED_CHAT_IDS, toJsonArray(missingExpectedChatIds))
            .add(FIELD_COVERAGE_COMPLETE, coverageComplete)
            .add(FIELD_LATENCY_MS, latencyMs)
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_BATCH_INDEX_PREFIX + batchIndex
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_BATCH_ID_PREFIX + batchId + '\''
            + TOSTRING_HTTP_STATUS_PREFIX + httpStatus
            + TOSTRING_SUCCESS_PREFIX + success
            + TOSTRING_RETRY_USED_PREFIX + retryUsed
            + TOSTRING_CONTEXT_TOO_LARGE_PREFIX + contextTooLargeDetected
            + TOSTRING_INPUT_COUNT_PREFIX + inputEntriesCount
            + TOSTRING_USED_COUNT_PREFIX + usedEntriesCount
            + TOSTRING_OMITTED_COUNT_PREFIX + omittedEntriesCount
            + TOSTRING_COVERAGE_COMPLETE_PREFIX + coverageComplete
            + TOSTRING_MISSING_SIZE_PREFIX + missingExpectedChatIds.size()
            + TOSTRING_LATENCY_PREFIX + latencyMs
                + '}';
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + ERR_REQUIRED_SUFFIX);
        }
        return value;
    }

    private static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + ERR_MUST_BE_POSITIVE_SUFFIX);
        }
        return value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String defaultIfNull(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static List<String> immutableCopyDistinct(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        Set<String> clean = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                clean.add(v.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(clean));
    }

    private static List<String> intersectOrdered(List<String> a, List<String> allowed) {
        if (a == null || a.isEmpty()) {
            return List.of();
        }
        Set<String> allow = new LinkedHashSet<>(allowed == null ? List.of() : allowed);
        List<String> out = new ArrayList<>();
        for (String s : a) {
            if (allow.contains(s)) {
                out.add(s);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private static List<String> subtractOrdered(List<String> base, List<String> remove) {
        if (base == null || base.isEmpty()) {
            return List.of();
        }
        Set<String> removeSet = new LinkedHashSet<>(remove == null ? List.of() : remove);
        List<String> out = new ArrayList<>();
        for (String s : base) {
            if (!removeSet.contains(s)) {
                out.add(s);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private static jakarta.json.JsonArray toJsonArray(List<String> values) {
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

