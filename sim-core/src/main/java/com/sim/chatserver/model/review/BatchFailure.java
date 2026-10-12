// src/main/java/com/sim/chatserver/model/review/BatchFailure.java
package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Immutable model describing one failed map batch.
 */
public final class BatchFailure {

    private static final String EMPTY = "";
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_BATCH_INDEX = "batchIndex";
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_BATCH_ID = "batchId";
    private static final String FIELD_REASON_CODE = "reasonCode";
    private static final String FIELD_MESSAGE = CommonLiteralConstants.FIELD_MESSAGE;
    private static final String FIELD_HTTP_STATUS = "httpStatus";
    private static final String FIELD_RETRY_ATTEMPTED = "retryAttempted";
    private static final String FIELD_RETRY_SUCCEEDED = "retrySucceeded";
    private static final String FIELD_CONTEXT_TOO_LARGE_DETECTED = "contextTooLargeDetected";
    private static final String FIELD_LATENCY_MS = CommonLiteralConstants.FIELD_LATENCY_MS;
    private static final String FIELD_BATCH_CHAT_IDS = "batchChatIds";
    private static final String ERR_BATCH_INDEX_GT_TOTAL = "batchIndex cannot be greater than totalBatches";
    private static final String BATCH_ID_PREFIX = "batch-";
    private static final String REASON_CONTEXT_TOO_LARGE = "context_too_large";
    private static final String REASON_UPSTREAM_4XX = "upstream_4xx";
    private static final String REASON_UPSTREAM_5XX = "upstream_5xx";
    private static final String REASON_TIMEOUT = "timeout";
    private static final String REASON_NETWORK_ERROR = "network_error";
    private static final String REASON_PARSE_ERROR = "parse_error";
    private static final String REASON_TEXT_CONTEXT_LIMIT = "token/context limit";
    private static final String REASON_TEXT_UPSTREAM_CLIENT = "upstream client error";
    private static final String REASON_TEXT_UPSTREAM_SERVER = "upstream server error";
    private static final String REASON_TEXT_TIMEOUT = "batch processing timeout";
    private static final String REASON_TEXT_NETWORK = "network error";
    private static final String REASON_TEXT_PARSE = "malformed content";
    private static final String REASON_TEXT_DEFAULT = "batch processing failure";
    private static final String TOSTRING_PREFIX = "BatchFailure{";
    private static final String TOSTRING_REQUEST_ID_PREFIX = CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_BATCH_INDEX_PREFIX = ", batchIndex=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_BATCH_ID_PREFIX = ", batchId='";
    private static final String TOSTRING_REASON_CODE_PREFIX = ", reasonCode='";
    private static final String TOSTRING_HTTP_STATUS_PREFIX = ", httpStatus=";
    private static final String TOSTRING_RETRY_ATTEMPTED_PREFIX = ", retryAttempted=";
    private static final String TOSTRING_RETRY_SUCCEEDED_PREFIX = ", retrySucceeded=";
    private static final String TOSTRING_CONTEXT_TOO_LARGE_PREFIX = ", contextTooLargeDetected=";
    private static final String TOSTRING_LATENCY_PREFIX = ", latencyMs=";
    private static final String ERR_REQUIRED_SUFFIX = CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String ERR_MUST_BE_POSITIVE_SUFFIX = " must be > 0";

    private final String requestId;
    private final int batchIndex;
    private final int totalBatches;
    private final String batchId;

    private final String reasonCode;   // e.g. context_too_large, upstream_4xx, upstream_5xx, timeout, parse_error
    private final String message;

    private final int httpStatus;      // 0 when not applicable
    private final boolean retryAttempted;
    private final boolean retrySucceeded;
    private final boolean contextTooLargeDetected;

    private final long latencyMs;

    private final List<String> batchChatIds;

    BatchFailure(BatchFailureBuilder b) {
        this.requestId = requireNonBlank(b.requestId, FIELD_REQUEST_ID);
        this.batchIndex = requirePositive(b.batchIndex, FIELD_BATCH_INDEX);
        this.totalBatches = requirePositive(b.totalBatches, FIELD_TOTAL_BATCHES);
        if (this.batchIndex > this.totalBatches) {
            throw new IllegalArgumentException(ERR_BATCH_INDEX_GT_TOTAL);
        }

        this.batchId = defaultIfBlank(b.batchId, BATCH_ID_PREFIX + this.batchIndex);

        this.reasonCode = normalizeReasonCode(requireNonBlank(b.reasonCode, FIELD_REASON_CODE));
        this.message = defaultIfNull(b.message, EMPTY);

        this.httpStatus = Math.max(0, b.httpStatus);
        this.retryAttempted = b.retryAttempted;
        this.retrySucceeded = b.retrySucceeded;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.latencyMs = Math.max(0L, b.latencyMs);
        this.batchChatIds = immutableStringList(b.batchChatIds);
    }

    public static BatchFailureBuilder builder() {
        return new BatchFailureBuilder();
    }

    static BatchFailure of(
            String requestId,
            int batchIndex,
            int totalBatches,
            String reasonCode,
            String message
    ) {
        return builder()
                .requestId(requestId)
                .batchIndex(batchIndex)
                .totalBatches(totalBatches)
                .reasonCode(reasonCode)
                .message(message)
                .build();
    }

    String getRequestId() {
        return requestId;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    int getTotalBatches() {
        return totalBatches;
    }

    String getBatchId() {
        return batchId;
    }

    String getReasonCode() {
        return reasonCode;
    }

    String getMessage() {
        return message;
    }

    int getHttpStatus() {
        return httpStatus;
    }

    boolean isRetryAttempted() {
        return retryAttempted;
    }

    boolean isRetrySucceeded() {
        return retrySucceeded;
    }

    boolean isContextTooLargeDetected() {
        return contextTooLargeDetected;
    }

    long getLatencyMs() {
        return latencyMs;
    }

    List<String> getBatchChatIds() {
        return batchChatIds;
    }

    public String reasonForCoverage() {
        // Human-friendly reason for "Coverage and Carry-Forward" section.
        return switch (reasonCode) {
            case REASON_CONTEXT_TOO_LARGE ->
                REASON_TEXT_CONTEXT_LIMIT;
            case REASON_UPSTREAM_4XX ->
                REASON_TEXT_UPSTREAM_CLIENT;
            case REASON_UPSTREAM_5XX ->
                REASON_TEXT_UPSTREAM_SERVER;
            case REASON_TIMEOUT ->
                REASON_TEXT_TIMEOUT;
            case REASON_NETWORK_ERROR ->
                REASON_TEXT_NETWORK;
            case REASON_PARSE_ERROR ->
                REASON_TEXT_PARSE;
            default ->
                REASON_TEXT_DEFAULT;
        };
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
            .add(FIELD_REQUEST_ID, requestId)
            .add(FIELD_BATCH_INDEX, batchIndex)
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_BATCH_ID, batchId)
            .add(FIELD_REASON_CODE, reasonCode)
            .add(FIELD_MESSAGE, message)
            .add(FIELD_HTTP_STATUS, httpStatus)
            .add(FIELD_RETRY_ATTEMPTED, retryAttempted)
            .add(FIELD_RETRY_SUCCEEDED, retrySucceeded)
            .add(FIELD_CONTEXT_TOO_LARGE_DETECTED, contextTooLargeDetected)
            .add(FIELD_LATENCY_MS, latencyMs)
            .add(FIELD_BATCH_CHAT_IDS, toJsonArray(batchChatIds))
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_BATCH_INDEX_PREFIX + batchIndex
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_BATCH_ID_PREFIX + batchId + '\''
            + TOSTRING_REASON_CODE_PREFIX + reasonCode + '\''
            + TOSTRING_HTTP_STATUS_PREFIX + httpStatus
            + TOSTRING_RETRY_ATTEMPTED_PREFIX + retryAttempted
            + TOSTRING_RETRY_SUCCEEDED_PREFIX + retrySucceeded
            + TOSTRING_CONTEXT_TOO_LARGE_PREFIX + contextTooLargeDetected
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

    private static String normalizeReasonCode(String reasonCode) {
        return reasonCode.trim().toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }

    private static List<String> immutableStringList(List<String> src) {
        if (src == null || src.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String s : src) {
            if (s != null && !s.isBlank()) {
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

