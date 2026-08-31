// src/main/java/com/sim/chatserver/model/review/ReduceRequest.java
package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

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

    private ReduceRequest(Builder b) {
        this.requestId = requireNonBlank(b.requestId, "requestId");
        this.targetUrl = requireNonBlank(b.targetUrl, "targetUrl");
        this.mode = defaultIfBlank(b.mode, "chat");
        this.sessionId = defaultIfNull(b.sessionId, "");
        this.reset = b.reset;

        this.controlledPrompt = requireNonBlank(b.controlledPrompt, "controlledPrompt");
        this.totalSelected = requireNonNegative(b.totalSelected, "totalSelected");
        this.totalBatches = requireNonNegative(b.totalBatches, "totalBatches");

        this.mapOutputs = immutableStringListKeepCase(Objects.requireNonNull(b.mapOutputs, "mapOutputs"));
        this.failedBatchIndexes = immutableDistinctPositiveIntList(defaultIfNullList(b.failedBatchIndexes));
        this.failedBatchReasons = immutableStringListKeepCase(defaultIfNullList(b.failedBatchReasons));

        this.allSelectedChatIds = immutableDistinctStringListLower(defaultIfNullList(b.allSelectedChatIds));
        this.usedChatIds = immutableDistinctStringListLower(defaultIfNullList(b.usedChatIds));

        // Always recompute missing as all - used for deterministic consistency.
        Set<String> missing = new LinkedHashSet<>(this.allSelectedChatIds);
        missing.removeAll(this.usedChatIds);
        this.missingChatIds = Collections.unmodifiableList(new ArrayList<>(missing));

        // Always compute coverageComplete from missing list (ignore override for strict deterministic behavior)
        this.coverageComplete = this.missingChatIds.isEmpty();

        validateConsistency();
    }

    public static Builder builder() {
        return new Builder();
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
        JsonArrayBuilder outputs = Json.createArrayBuilder();
        for (String out : mapOutputs) {
            outputs.add(out == null ? "" : out);
        }

        JsonArrayBuilder failedIdx = Json.createArrayBuilder();
        for (Integer i : failedBatchIndexes) {
            if (i != null) {
                failedIdx.add(i.intValue());
            }
        }

        JsonArrayBuilder failedReasons = Json.createArrayBuilder();
        for (String r : failedBatchReasons) {
            failedReasons.add(r == null ? "" : r);
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
                .add("requestId", requestId)
                .add("targetUrl", targetUrl)
                .add("mode", mode)
                .add("sessionId", sessionId)
                .add("reset", reset)
                .add("controlledPrompt", controlledPrompt)
                .add("totalSelected", totalSelected)
                .add("totalBatches", totalBatches)
                .add("mapOutputsCount", mapOutputs.size())
                .add("failedBatchCount", failedBatchIndexes.size())
                .add("mapOutputs", outputs)
                .add("failedBatchIndexes", failedIdx)
                .add("failedBatchReasons", failedReasons)
                .add("allSelectedChatIds", allIds)
                .add("usedChatIds", usedIds)
                .add("missingChatIds", missingIds)
                .add("coverageComplete", coverageComplete)
                .build();
    }

    @Override
    public String toString() {
        return "ReduceRequest{"
                + "requestId='" + requestId + '\''
                + ", mode='" + mode + '\''
                + ", reset=" + reset
                + ", totalSelected=" + totalSelected
                + ", totalBatches=" + totalBatches
                + ", mapOutputsCount=" + mapOutputs.size()
                + ", failedBatchIndexes=" + failedBatchIndexes
                + ", coverageComplete=" + coverageComplete
                + ", allSelectedChatIds=" + allSelectedChatIds.size()
                + ", usedChatIds=" + usedChatIds.size()
                + ", missingChatIds=" + missingChatIds.size()
                + '}';
    }

    public static final class Builder {

        private String requestId;
        private String targetUrl;
        private String mode;
        private String sessionId;
        private boolean reset;

        private String controlledPrompt;
        private int totalSelected;
        private int totalBatches;

        private List<String> mapOutputs = new ArrayList<>();
        private List<Integer> failedBatchIndexes = new ArrayList<>();
        private List<String> failedBatchReasons = new ArrayList<>();

        private List<String> allSelectedChatIds = new ArrayList<>();
        private List<String> usedChatIds = new ArrayList<>();
        private List<String> missingChatIds = new ArrayList<>();
        private boolean coverageCompleteOverride; // retained for backward compatibility, ignored in strict build

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder reset(boolean reset) {
            this.reset = reset;
            return this;
        }

        public Builder controlledPrompt(String controlledPrompt) {
            this.controlledPrompt = controlledPrompt;
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

        public Builder mapOutputs(List<String> mapOutputs) {
            this.mapOutputs = mapOutputs == null ? new ArrayList<>() : mapOutputs;
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
            // accepted for compatibility; ignored in strict deterministic construction
            this.missingChatIds = missingChatIds == null ? new ArrayList<>() : missingChatIds;
            return this;
        }

        public Builder coverageComplete(boolean coverageComplete) {
            // accepted for compatibility; ignored in strict deterministic construction
            this.coverageCompleteOverride = coverageComplete;
            return this;
        }

        public ReduceRequest build() {
            return new ReduceRequest(this);
        }
    }

    private void validateConsistency() {
        if (totalSelected < allSelectedChatIds.size()) {
            throw new IllegalArgumentException("totalSelected cannot be less than allSelectedChatIds.size()");
        }

        Set<String> expectedMissing = new LinkedHashSet<>(allSelectedChatIds);
        expectedMissing.removeAll(usedChatIds);

        if (!expectedMissing.equals(new LinkedHashSet<>(missingChatIds))) {
            throw new IllegalArgumentException("missingChatIds must exactly equal allSelectedChatIds - usedChatIds");
        }

        if (coverageComplete != missingChatIds.isEmpty()) {
            throw new IllegalArgumentException("coverageComplete must equal missingChatIds.isEmpty()");
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
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
        for (Integer i : src) {
            if (i != null) {
                int value = i.intValue();
                if (value > 0) {
                    out.add(Integer.valueOf(value));
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }
}
