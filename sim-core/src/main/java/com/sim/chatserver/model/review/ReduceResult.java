// src/main/java/com/sim/chatserver/model/review/ReduceResult.java
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

    private ReduceResult(Builder b) {
        this.requestId = requireNonBlank(b.requestId, "requestId");

        this.httpStatus = b.httpStatus;
        this.retryUsed = b.retryUsed;
        this.contextTooLargeDetected = b.contextTooLargeDetected;

        this.finalReport = defaultIfNull(b.finalReport, "");
        this.errorMessage = defaultIfNull(b.errorMessage, "");

        this.totalSelected = Math.max(0, b.totalSelected);
        this.totalBatches = Math.max(0, b.totalBatches);
        this.mapOutputsReceived = Math.max(0, b.mapOutputsReceived);

        this.failedBatchIndexes = immutableIntListDistinct(b.failedBatchIndexes);
        this.failedBatchReasons = immutableStringListDistinctKeepCase(b.failedBatchReasons);

        this.allSelectedChatIds = immutableStringListDistinctLower(b.allSelectedChatIds);
        this.usedChatIds = immutableStringListDistinctLower(b.usedChatIds);

        // strict deterministic: always derive missing from all-used
        Set<String> missing = new LinkedHashSet<>(this.allSelectedChatIds);
        missing.removeAll(this.usedChatIds);
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
            throw new IllegalArgumentException("mapOutputsReceived cannot exceed totalBatches");
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        expectedMissing.removeAll(usedChatIds);
        if (!expectedMissing.equals(new LinkedHashSet<>(missingChatIds))) {
            throw new IllegalArgumentException("missingChatIds must exactly equal allSelectedChatIds - usedChatIds");
        }

        if (coverageComplete != missingChatIds.isEmpty()) {
            throw new IllegalArgumentException("coverageComplete must equal missingChatIds.isEmpty()");
        }

        if (success && (!coverageComplete || httpStatus >= 400)) {
            throw new IllegalArgumentException("success=true requires coverageComplete=true and httpStatus<400");
        }
    }

    public static Builder builder() {
        return new Builder();
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
                .add("requestId", requestId)
                .add("httpStatus", httpStatus)
                .add("success", success)
                .add("retryUsed", retryUsed)
                .add("contextTooLargeDetected", contextTooLargeDetected)
                .add("finalReport", finalReport)
                .add("errorMessage", errorMessage)
                .add("totalSelected", totalSelected)
                .add("totalBatches", totalBatches)
                .add("mapOutputsReceived", mapOutputsReceived)
                .add("failedBatchCount", getFailedBatchCount())
                .add("failedBatchIndexes", toJsonIntArray(failedBatchIndexes))
                .add("failedBatchReasons", toJsonStringArray(failedBatchReasons))
                .add("allSelectedChatIds", toJsonStringArray(allSelectedChatIds))
                .add("usedChatIds", toJsonStringArray(usedChatIds))
                .add("missingChatIds", toJsonStringArray(missingChatIds)) // exact derived missing IDs preserved
                .add("coverageComplete", coverageComplete)
                .add("coveragePercent", coveragePercent)
                .add("latencyMs", latencyMs)
                .build();
    }

    @Override
    public String toString() {
        return "ReduceResult{"
                + "requestId='" + requestId + '\''
                + ", httpStatus=" + httpStatus
                + ", success=" + success
                + ", retryUsed=" + retryUsed
                + ", contextTooLargeDetected=" + contextTooLargeDetected
                + ", totalSelected=" + totalSelected
                + ", totalBatches=" + totalBatches
                + ", mapOutputsReceived=" + mapOutputsReceived
                + ", failedBatchIndexes=" + failedBatchIndexes
                + ", coverageComplete=" + coverageComplete
                + ", coveragePercent=" + coveragePercent
                + ", missingChatIds=" + missingChatIds.size()
                + ", latencyMs=" + latencyMs
                + '}';
    }

    public static final class Builder {

        private String requestId;

        private int httpStatus;
        private boolean success;
        private boolean retryUsed;
        private boolean contextTooLargeDetected;

        private String finalReport;
        private String errorMessage;

        private int totalSelected;
        private int totalBatches;
        private int mapOutputsReceived;

        private List<Integer> failedBatchIndexes = new ArrayList<>();
        private List<String> failedBatchReasons = new ArrayList<>();

        private List<String> allSelectedChatIds = new ArrayList<>();
        private List<String> usedChatIds = new ArrayList<>();
        private List<String> missingChatIds = new ArrayList<>(); // compatibility only (ignored in strict derive)
        private boolean coverageCompleteOverride; // compatibility only (ignored in strict derive)

        private long latencyMs;

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
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

        public Builder finalReport(String finalReport) {
            this.finalReport = finalReport;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
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

        public Builder mapOutputsReceived(int mapOutputsReceived) {
            this.mapOutputsReceived = mapOutputsReceived;
            return this;
        }

        public Builder failedBatchIndexes(List<Integer> failedBatchIndexes) {
            this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
            return this;
        }

        public Builder failedBatchReasons(List<String> failedBatchReasons) {
            this.failedBatchReasons = failedBatchReasons == null ? new ArrayList<>() : failedBatchReasons;
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

        public Builder coverageComplete(boolean coverageComplete) {
            this.coverageCompleteOverride = coverageComplete;
            return this;
        }

        public Builder latencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public ReduceResult build() {
            return new ReduceResult(this);
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
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
        for (Integer i : src) {
            if (i != null) {
                out.add(Integer.valueOf(i.intValue()));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
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
        if (values != null) {
            for (Integer v : values) {
                if (v != null) {
                    b.add(v.intValue());
                }
            }
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
