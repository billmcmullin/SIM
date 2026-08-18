// src/main/java/com/sim/chatserver/model/review/MapBatchResult.java
package com.sim.chatserver.model.review;

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

    private MapBatchResult(Builder b) {
        this.requestId = requireNonBlank(b.requestId, "requestId");
        this.batchIndex = requirePositive(b.batchIndex, "batchIndex");
        this.totalBatches = requirePositive(b.totalBatches, "totalBatches");
        this.batchId = defaultIfBlank(b.batchId, "batch-" + this.batchIndex);

        this.httpStatus = b.httpStatus;
        this.retryUsed = b.retryUsed;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.modelOutput = defaultIfNull(b.modelOutput, "");
        this.errorMessage = defaultIfNull(b.errorMessage, "");

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
            throw new IllegalArgumentException("batchIndex cannot be greater than totalBatches");
        }

        if (usedEntriesCount > inputEntriesCount && inputEntriesCount > 0) {
            throw new IllegalArgumentException("usedEntriesCount cannot exceed inputEntriesCount");
        }

        Set<String> expected = new LinkedHashSet<>(expectedChatIds);
        Set<String> found = new LinkedHashSet<>(foundChatIds);

        if (!expected.containsAll(found)) {
            throw new IllegalArgumentException("foundChatIds must be subset of expectedChatIds");
        }

        List<String> recomputedMissing = subtractOrdered(expectedChatIds, foundChatIds);
        if (!recomputedMissing.equals(missingExpectedChatIds)) {
            throw new IllegalArgumentException("missingExpectedChatIds must equal expectedChatIds - foundChatIds");
        }

        if (coverageComplete != missingExpectedChatIds.isEmpty()) {
            throw new IllegalArgumentException("coverageComplete must match missingExpectedChatIds emptiness");
        }

        if (!usedChatIds.equals(foundChatIds)) {
            throw new IllegalArgumentException("usedChatIds must match foundChatIds in strict mode");
        }

        if (!omittedChatIds.equals(missingExpectedChatIds)) {
            throw new IllegalArgumentException("omittedChatIds must match missingExpectedChatIds in strict mode");
        }

        if (success && (!coverageComplete || httpStatus >= 400)) {
            throw new IllegalArgumentException("success=true requires coverageComplete=true and httpStatus<400");
        }
    }

    public static Builder builder() {
        return new Builder();
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
                .add("requestId", requestId)
                .add("batchIndex", batchIndex)
                .add("totalBatches", totalBatches)
                .add("batchId", batchId)
                .add("httpStatus", httpStatus)
                .add("success", success)
                .add("retryUsed", retryUsed)
                .add("contextTooLargeDetected", contextTooLargeDetected)
                .add("modelOutput", modelOutput)
                .add("errorMessage", errorMessage)
                .add("inputEntriesCount", inputEntriesCount)
                .add("usedEntriesCount", usedEntriesCount)
                .add("omittedEntriesCount", omittedEntriesCount)
                .add("batchChatIds", toJsonArray(batchChatIds))
                .add("usedChatIds", toJsonArray(usedChatIds))
                .add("omittedChatIds", toJsonArray(omittedChatIds))
                .add("expectedChatIds", toJsonArray(expectedChatIds))
                .add("foundChatIds", toJsonArray(foundChatIds))
                .add("missingExpectedChatIds", toJsonArray(missingExpectedChatIds))
                .add("coverageComplete", coverageComplete)
                .add("latencyMs", latencyMs)
                .build();
    }

    @Override
    public String toString() {
        return "MapBatchResult{"
                + "requestId='" + requestId + '\''
                + ", batchIndex=" + batchIndex
                + ", totalBatches=" + totalBatches
                + ", batchId='" + batchId + '\''
                + ", httpStatus=" + httpStatus
                + ", success=" + success
                + ", retryUsed=" + retryUsed
                + ", contextTooLargeDetected=" + contextTooLargeDetected
                + ", inputEntriesCount=" + inputEntriesCount
                + ", usedEntriesCount=" + usedEntriesCount
                + ", omittedEntriesCount=" + omittedEntriesCount
                + ", coverageComplete=" + coverageComplete
                + ", missingExpectedChatIds=" + missingExpectedChatIds.size()
                + ", latencyMs=" + latencyMs
                + '}';
    }

    public static final class Builder {

        private String requestId;
        private int batchIndex;
        private int totalBatches;
        private String batchId;

        private int httpStatus;
        private boolean success;
        private boolean retryUsed;
        private boolean contextTooLargeDetected;

        private String modelOutput;
        private String errorMessage;

        private int inputEntriesCount;
        private int usedEntriesCount;
        private int omittedEntriesCount;

        private List<String> batchChatIds = new ArrayList<>();
        private List<String> usedChatIds = new ArrayList<>();
        private List<String> omittedChatIds = new ArrayList<>();

        private List<String> expectedChatIds = new ArrayList<>();
        private List<String> foundChatIds = new ArrayList<>();
        private List<String> missingExpectedChatIds = new ArrayList<>();
        private Boolean coverageCompleteOverride; // compatibility only (ignored in strict derive)

        private long latencyMs;

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

        public Builder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder retryUsed(boolean retryUsed) {
            this.retryUsed = retryUsed;
            return this;
        }

        public Builder contextTooLargeDetected(boolean contextTooLargeDetected) {
            this.contextTooLargeDetected = contextTooLargeDetected;
            return this;
        }

        public Builder modelOutput(String modelOutput) {
            this.modelOutput = modelOutput;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder inputEntriesCount(int inputEntriesCount) {
            this.inputEntriesCount = inputEntriesCount;
            return this;
        }

        public Builder usedEntriesCount(int usedEntriesCount) {
            this.usedEntriesCount = usedEntriesCount;
            return this;
        }

        public Builder omittedEntriesCount(int omittedEntriesCount) {
            this.omittedEntriesCount = omittedEntriesCount;
            return this;
        }

        public Builder batchChatIds(List<String> batchChatIds) {
            this.batchChatIds = batchChatIds == null ? new ArrayList<>() : batchChatIds;
            return this;
        }

        public Builder usedChatIds(List<String> usedChatIds) {
            this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
            return this;
        }

        public Builder omittedChatIds(List<String> omittedChatIds) {
            this.omittedChatIds = omittedChatIds == null ? new ArrayList<>() : omittedChatIds;
            return this;
        }

        public Builder expectedChatIds(List<String> expectedChatIds) {
            this.expectedChatIds = expectedChatIds == null ? new ArrayList<>() : expectedChatIds;
            return this;
        }

        public Builder foundChatIds(List<String> foundChatIds) {
            this.foundChatIds = foundChatIds == null ? new ArrayList<>() : foundChatIds;
            return this;
        }

        public Builder missingExpectedChatIds(List<String> missingExpectedChatIds) {
            this.missingExpectedChatIds = missingExpectedChatIds == null ? new ArrayList<>() : missingExpectedChatIds;
            return this;
        }

        public Builder coverageComplete(boolean coverageComplete) {
            this.coverageCompleteOverride = coverageComplete;
            return this;
        }

        public Builder latencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public MapBatchResult build() {
            return new MapBatchResult(this);
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
