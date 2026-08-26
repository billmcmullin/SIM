// src/main/java/com/sim/chatserver/model/review/BatchFailure.java
package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Immutable model describing one failed map batch.
 */
public final class BatchFailure {

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

    private BatchFailure(Builder b) {
        this.requestId = requireNonBlank(b.requestId, "requestId");
        this.batchIndex = requirePositive(b.batchIndex, "batchIndex");
        this.totalBatches = requirePositive(b.totalBatches, "totalBatches");
        if (this.batchIndex > this.totalBatches) {
            throw new IllegalArgumentException("batchIndex cannot be greater than totalBatches");
        }

        this.batchId = defaultIfBlank(b.batchId, "batch-" + this.batchIndex);

        this.reasonCode = normalizeReasonCode(requireNonBlank(b.reasonCode, "reasonCode"));
        this.message = defaultIfNull(b.message, "");

        this.httpStatus = Math.max(0, b.httpStatus);
        this.retryAttempted = b.retryAttempted;
        this.retrySucceeded = b.retrySucceeded;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.latencyMs = Math.max(0L, b.latencyMs);
        this.batchChatIds = immutableStringList(b.batchChatIds);
    }

    public static Builder builder() {
        return new Builder();
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
            case "context_too_large" ->
                "token/context limit";
            case "upstream_4xx" ->
                "upstream client error";
            case "upstream_5xx" ->
                "upstream server error";
            case "timeout" ->
                "batch processing timeout";
            case "network_error" ->
                "network error";
            case "parse_error" ->
                "malformed content";
            default ->
                "batch processing failure";
        };
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("requestId", requestId)
                .add("batchIndex", batchIndex)
                .add("totalBatches", totalBatches)
                .add("batchId", batchId)
                .add("reasonCode", reasonCode)
                .add("message", message)
                .add("httpStatus", httpStatus)
                .add("retryAttempted", retryAttempted)
                .add("retrySucceeded", retrySucceeded)
                .add("contextTooLargeDetected", contextTooLargeDetected)
                .add("latencyMs", latencyMs)
                .add("batchChatIds", toJsonArray(batchChatIds))
                .build();
    }

    @Override
    public String toString() {
        return "BatchFailure{"
                + "requestId='" + requestId + '\''
                + ", batchIndex=" + batchIndex
                + ", totalBatches=" + totalBatches
                + ", batchId='" + batchId + '\''
                + ", reasonCode='" + reasonCode + '\''
                + ", httpStatus=" + httpStatus
                + ", retryAttempted=" + retryAttempted
                + ", retrySucceeded=" + retrySucceeded
                + ", contextTooLargeDetected=" + contextTooLargeDetected
                + ", latencyMs=" + latencyMs
                + '}';
    }

    public static final class Builder {

        private String requestId;
        private int batchIndex;
        private int totalBatches;
        private String batchId;

        private String reasonCode;
        private String message;

        private int httpStatus = 0;
        private boolean retryAttempted;
        private boolean retrySucceeded;
        private boolean contextTooLargeDetected;

        private long latencyMs = 0L;
        private List<String> batchChatIds = new ArrayList<>();

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder batchIndex(int batchIndex) {
            this.batchIndex = batchIndex;
            return this;
        }

        public Builder totalBatches(int totalBatches) {
            this.totalBatches = totalBatches;
            return this;
        }

        public Builder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public Builder reasonCode(String reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder retryAttempted(boolean retryAttempted) {
            this.retryAttempted = retryAttempted;
            return this;
        }

        public Builder retrySucceeded(boolean retrySucceeded) {
            this.retrySucceeded = retrySucceeded;
            return this;
        }

        public Builder contextTooLargeDetected(boolean contextTooLargeDetected) {
            this.contextTooLargeDetected = contextTooLargeDetected;
            return this;
        }

        public Builder latencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public Builder batchChatIds(List<String> batchChatIds) {
            this.batchChatIds = batchChatIds == null ? new ArrayList<>() : batchChatIds;
            return this;
        }

        public BatchFailure build() {
            return new BatchFailure(this);
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be > 0");
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
