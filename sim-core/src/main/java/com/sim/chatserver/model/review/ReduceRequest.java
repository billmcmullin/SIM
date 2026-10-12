// src/main/java/com/sim/chatserver/model/review/ReduceRequest.java
package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Immutable request model for reduce-phase synthesis.
 *
 * Strict coverage metadata: - allSelectedChatIds - usedChatIds - missingChatIds
 * - coverageComplete
 */
public final class ReduceRequest {

    private static final String EMPTY = "";
    private static final String DEFAULT_MODE = CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_TARGET_URL = "targetUrl";
    private static final String FIELD_MODE = CommonLiteralConstants.FIELD_MODE;
    private static final String FIELD_SESSION_ID = CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String FIELD_RESET = com.sim.chatserver.util.CommonLiteralConstants.VALUE_RESET;
    private static final String FIELD_CONTROLLED_PROMPT = "controlledPrompt";
    private static final String FIELD_TOTAL_SELECTED = CommonLiteralConstants.FIELD_TOTAL_SELECTED;
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_MAP_OUTPUTS_COUNT = "mapOutputsCount";
    private static final String FIELD_FAILED_BATCH_COUNT = "failedBatchCount";
    private static final String FIELD_MAP_OUTPUTS = "mapOutputs";
    private static final String FIELD_FAILED_BATCH_INDEXES = CommonLiteralConstants.FIELD_FAILED_BATCH_INDEXES;
    private static final String FIELD_FAILED_BATCH_REASONS = "failedBatchReasons";
    private static final String FIELD_ALL_SELECTED_CHAT_IDS = CommonLiteralConstants.FIELD_ALL_SELECTED_CHAT_IDS;
    private static final String FIELD_USED_CHAT_IDS = CommonLiteralConstants.FIELD_USED_CHAT_IDS;
    private static final String FIELD_MISSING_CHAT_IDS = CommonLiteralConstants.FIELD_MISSING_CHAT_IDS;
    private static final String FIELD_COVERAGE_COMPLETE = CommonLiteralConstants.FIELD_COVERAGE_COMPLETE;
    private static final String FIELD_MAP_OUTPUTS_PARAM = "mapOutputs";
    private static final String ERR_TOTAL_SELECTED_LT_ALL = "totalSelected cannot be less than allSelectedChatIds.size()";
    private static final String ERR_MISSING_CHAT_IDS_MISMATCH = "missingChatIds must exactly equal allSelectedChatIds - usedChatIds";
    private static final String ERR_COVERAGE_COMPLETE_MISMATCH = "coverageComplete must equal missingChatIds.isEmpty()";
    private static final String ERR_REQUIRED_SUFFIX = CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String ERR_NON_NEGATIVE_SUFFIX = " must be >= 0";
    private static final String TOSTRING_PREFIX = "ReduceRequest{";
    private static final String TOSTRING_REQUEST_ID_PREFIX = CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_MODE_PREFIX = ", mode='";
    private static final String TOSTRING_RESET_PREFIX = ", reset=";
    private static final String TOSTRING_TOTAL_SELECTED_PREFIX = ", totalSelected=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_MAP_OUTPUTS_COUNT_PREFIX = ", mapOutputsCount=";
    private static final String TOSTRING_FAILED_BATCH_INDEXES_PREFIX = ", failedBatchIndexes=";
    private static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.TOSTRING_COVERAGE_COMPLETE_PREFIX;
    private static final String TOSTRING_ALL_IDS_PREFIX = ", allSelectedChatIds=";
    private static final String TOSTRING_USED_IDS_PREFIX = ", usedChatIds=";
    private static final String TOSTRING_MISSING_IDS_PREFIX = ", " + CommonLiteralConstants.FIELD_MISSING_CHAT_IDS + '=';

    private final String requestId;
    private final String targetUrl;
    private final String mode;
    private final String sessionId;
    private final boolean reset;

    private final String controlledPrompt;
    private final int totalSelected;
    private final int totalBatches;

    private final List<String> mapOutputs;
    private final List<Integer> failedBatchIndexes;
    private final List<String> failedBatchReasons;

    // deterministic coverage fields
    private final List<String> allSelectedChatIds;
    private final List<String> usedChatIds;
    private final List<String> missingChatIds;
    private final boolean coverageComplete;

    ReduceRequest(ReduceRequestBuilder b) {
        this.requestId = requireNonBlank(b.requestId, FIELD_REQUEST_ID);
        this.targetUrl = requireNonBlank(b.targetUrl, FIELD_TARGET_URL);
        this.mode = defaultIfBlank(b.mode, DEFAULT_MODE);
        this.sessionId = defaultIfNull(b.sessionId, EMPTY);
        this.reset = b.reset;

        this.controlledPrompt = requireNonBlank(b.controlledPrompt, FIELD_CONTROLLED_PROMPT);
        this.totalSelected = requireNonNegative(b.totalSelected, FIELD_TOTAL_SELECTED);
        this.totalBatches = requireNonNegative(b.totalBatches, FIELD_TOTAL_BATCHES);

        this.mapOutputs = immutableStringListKeepCase(Objects.requireNonNull(b.mapOutputs, FIELD_MAP_OUTPUTS_PARAM));
        this.failedBatchIndexes = immutableDistinctPositiveIntList(defaultIfNullList(b.failedBatchIndexes));
        this.failedBatchReasons = immutableStringListKeepCase(defaultIfNullList(b.failedBatchReasons));

        this.allSelectedChatIds = immutableDistinctStringListLower(defaultIfNullList(b.allSelectedChatIds));
        this.usedChatIds = immutableDistinctStringListLower(defaultIfNullList(b.usedChatIds));

        // Always recompute missing as all - used for deterministic consistency.
        Set<String> missing = new LinkedHashSet<>(this.allSelectedChatIds);
        for (String usedId : this.usedChatIds) {
            missing.remove(usedId);
        }
        this.missingChatIds = Collections.unmodifiableList(new ArrayList<>(missing));

        // Always compute coverageComplete from missing list (ignore override for strict deterministic behavior)
        this.coverageComplete = this.missingChatIds.isEmpty();

        validateConsistency();
    }

    public static ReduceRequestBuilder builder() {
        return new ReduceRequestBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getMode() {
        return mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isReset() {
        return reset;
    }

    public String getControlledPrompt() {
        return controlledPrompt;
    }

    public int getTotalSelected() {
        return totalSelected;
    }

    public int getTotalBatches() {
        return totalBatches;
    }

    List<String> getMapOutputs() {
        return mapOutputs;
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

    List<String> getMissingChatIds() {
        return missingChatIds;
    }

    boolean isCoverageComplete() {
        return coverageComplete;
    }

    int getMapOutputsCount() {
        return mapOutputs.size();
    }

    int getFailedBatchCount() {
        return failedBatchIndexes.size();
    }

    boolean hasFailures() {
        return !failedBatchIndexes.isEmpty();
    }

    boolean hasMapOutputs() {
        return !mapOutputs.isEmpty();
    }

    JsonObject toJson() {
        int failedBatchCount = failedBatchIndexes == null ? 0 : failedBatchIndexes.size();

        JsonArrayBuilder outputs = Json.createArrayBuilder();
        for (String out : mapOutputs) {
            outputs.add(out == null ? EMPTY : out);
        }

        JsonArrayBuilder failedIdx = Json.createArrayBuilder();
        if (failedBatchIndexes != null) {
            for (String text : toNumberTexts(failedBatchIndexes)) {
                failedIdx.add(Integer.parseInt(text));
            }
        }

        JsonArrayBuilder failedReasons = Json.createArrayBuilder();
        for (String reason : failedBatchReasons) {
            failedReasons.add(reason == null ? EMPTY : reason);
        }

        JsonArrayBuilder allIds = Json.createArrayBuilder();
        for (String id : allSelectedChatIds) {
            allIds.add(id);
        }

        JsonArrayBuilder usedIds = Json.createArrayBuilder();
        for (String id : usedChatIds) {
            usedIds.add(id);
        }

        JsonArrayBuilder missingIds = Json.createArrayBuilder();
        for (String id : missingChatIds) {
            missingIds.add(id);
        }

        return Json.createObjectBuilder()
            .add(FIELD_REQUEST_ID, requestId)
            .add(FIELD_TARGET_URL, targetUrl)
            .add(FIELD_MODE, mode)
            .add(FIELD_SESSION_ID, sessionId)
            .add(FIELD_RESET, reset)
            .add(FIELD_CONTROLLED_PROMPT, controlledPrompt)
            .add(FIELD_TOTAL_SELECTED, totalSelected)
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_MAP_OUTPUTS_COUNT, mapOutputs.size())
            .add(FIELD_FAILED_BATCH_COUNT, failedBatchCount)
            .add(FIELD_MAP_OUTPUTS, outputs)
            .add(FIELD_FAILED_BATCH_INDEXES, failedIdx)
            .add(FIELD_FAILED_BATCH_REASONS, failedReasons)
            .add(FIELD_ALL_SELECTED_CHAT_IDS, allIds)
            .add(FIELD_USED_CHAT_IDS, usedIds)
            .add(FIELD_MISSING_CHAT_IDS, missingIds)
            .add(FIELD_COVERAGE_COMPLETE, coverageComplete)
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_MODE_PREFIX + mode + '\''
            + TOSTRING_RESET_PREFIX + reset
            + TOSTRING_TOTAL_SELECTED_PREFIX + totalSelected
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_MAP_OUTPUTS_COUNT_PREFIX + mapOutputs.size()
            + TOSTRING_FAILED_BATCH_INDEXES_PREFIX + failedBatchIndexes
            + TOSTRING_COVERAGE_COMPLETE_PREFIX + coverageComplete
            + TOSTRING_ALL_IDS_PREFIX + allSelectedChatIds.size()
            + TOSTRING_USED_IDS_PREFIX + usedChatIds.size()
            + TOSTRING_MISSING_IDS_PREFIX + missingChatIds.size()
                + '}';
    }

    private void validateConsistency() {
        if (totalSelected < allSelectedChatIds.size()) {
            throw new IllegalArgumentException(ERR_TOTAL_SELECTED_LT_ALL);
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        for (String usedId : usedChatIds) {
            expectedMissing.remove(usedId);
        }

        if (!expectedMissing.equals(new LinkedHashSet<>(missingChatIds))) {
            throw new IllegalArgumentException(ERR_MISSING_CHAT_IDS_MISMATCH);
        }

        if (coverageComplete != missingChatIds.isEmpty()) {
            throw new IllegalArgumentException(ERR_COVERAGE_COMPLETE_MISMATCH);
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + ERR_REQUIRED_SUFFIX);
        }
        return value;
    }

    private static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + ERR_NON_NEGATIVE_SUFFIX);
        }
        return value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String defaultIfNull(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static <T> List<T> defaultIfNullList(List<T> v) {
        return v == null ? new ArrayList<>() : v;
    }

    private static List<String> immutableStringListKeepCase(List<String> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String s : src) {
            if (s != null) {
                out.add(s);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private static List<String> immutableDistinctStringListLower(List<String> src) {
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

    private static List<Integer> immutableDistinctPositiveIntList(List<Integer> src) {
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
}

