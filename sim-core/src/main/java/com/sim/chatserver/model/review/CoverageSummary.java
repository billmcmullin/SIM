// src/main/java/com/sim/chatserver/model/review/CoverageSummary.java
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
 * Immutable coverage accounting summary for map-reduce reporting.
 *
 * Deterministic source of truth: - allSelectedChatIds is authoritative input
 * set - usedChatIds are IDs successfully covered - notUsedChatIds = allSelected
 * - used
 */
public final class CoverageSummary {

    private final int chatsProvided;
    private final int chatsUsedInAnalysis;
    private final int chatsNotUsed;

    private final List<String> allSelectedChatIds;
    private final List<String> usedChatIds;
    private final List<String> notUsedChatIds;
    private final List<String> reasonsChatsNotUsed;

    private final int totalBatches;
    private final int successfulBatches;
    private final List<Integer> failedBatchIndexes;

    private final boolean coverageComplete;
    private final int coveragePercent;

    private CoverageSummary(Builder b) {
        this.allSelectedChatIds = immutableDistinctStringListLower(b.allSelectedChatIds);
        this.usedChatIds = immutableDistinctStringListLower(b.usedChatIds);
        this.reasonsChatsNotUsed = immutableDistinctStringListKeepCase(b.reasonsChatsNotUsed);

        int provided = b.chatsProvided >= 0 ? b.chatsProvided : this.allSelectedChatIds.size();
        this.chatsProvided = requireNonNegative(provided, "chatsProvided");

        this.chatsUsedInAnalysis = b.chatsUsedInAnalysis >= 0
                ? Math.max(0, b.chatsUsedInAnalysis)
                : this.usedChatIds.size();

        List<String> computedNotUsed = (b.notUsedChatIds == null || b.notUsedChatIds.isEmpty())
                ? computeNotUsed(this.allSelectedChatIds, this.usedChatIds)
                : immutableDistinctStringListLower(b.notUsedChatIds);

        this.notUsedChatIds = computedNotUsed;

        int inferredNotUsed = b.chatsNotUsed >= 0
                ? Math.max(0, b.chatsNotUsed)
                : this.notUsedChatIds.size();

        this.chatsNotUsed = inferredNotUsed;

        this.totalBatches = Math.max(0, b.totalBatches);
        this.successfulBatches = Math.max(0, b.successfulBatches);
        this.failedBatchIndexes = immutableDistinctIntList(b.failedBatchIndexes);

        this.coverageComplete = b.coverageCompleteOverrideSet
            ? b.coverageCompleteOverride
                : this.notUsedChatIds.isEmpty() && this.chatsNotUsed == 0;

        this.coveragePercent = computeCoveragePercent(this.chatsProvided, this.chatsUsedInAnalysis);

        validateCountsAndConsistency();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Build summary from authoritative all IDs and used IDs.
     */
    static CoverageSummary fromIds(
            List<String> allChatIds,
            List<String> usedChatIds,
            List<String> reasonsChatsNotUsed,
            int totalBatches,
            int successfulBatches,
            List<Integer> failedBatchIndexes
    ) {
        Set<String> all = toOrderedSetLower(allChatIds);
        Set<String> used = toOrderedSetLower(usedChatIds);

        Set<String> notUsed = new LinkedHashSet<>(all);
        notUsed.removeAll(used);

        return CoverageSummary.builder()
                .allSelectedChatIds(new ArrayList<>(all))
                .chatsProvided(all.size())
                .chatsUsedInAnalysis(used.size())
                .chatsNotUsed(notUsed.size())
                .usedChatIds(new ArrayList<>(used))
                .notUsedChatIds(new ArrayList<>(notUsed))
                .reasonsChatsNotUsed(reasonsChatsNotUsed)
                .totalBatches(totalBatches)
                .successfulBatches(successfulBatches)
                .failedBatchIndexes(failedBatchIndexes)
                .coverageComplete(notUsed.isEmpty())
                .build();
    }

    int getChatsProvided() {
        return chatsProvided;
    }

    int getChatsUsedInAnalysis() {
        return chatsUsedInAnalysis;
    }

    int getChatsNotUsed() {
        return chatsNotUsed;
    }

    List<String> getAllSelectedChatIds() {
        return allSelectedChatIds;
    }

    List<String> getUsedChatIds() {
        return usedChatIds;
    }

    public List<String> getNotUsedChatIds() {
        return notUsedChatIds;
    }

    List<String> getReasonsChatsNotUsed() {
        return reasonsChatsNotUsed;
    }

    int getTotalBatches() {
        return totalBatches;
    }

    int getSuccessfulBatches() {
        return successfulBatches;
    }

    int getFailedBatchCount() {
        return failedBatchIndexes.size();
    }

    List<Integer> getFailedBatchIndexes() {
        return failedBatchIndexes;
    }

    public boolean isCoverageComplete() {
        return coverageComplete;
    }

    int getCoveragePercent() {
        return coveragePercent;
    }

    boolean hasFailures() {
        return !failedBatchIndexes.isEmpty();
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("chatsProvided", chatsProvided)
                .add("chatsUsedInAnalysis", chatsUsedInAnalysis)
                .add("chatsNotUsed", chatsNotUsed)
                .add("allSelectedChatIds", toJsonArray(allSelectedChatIds))
                .add("usedChatIds", toJsonArray(usedChatIds))
                .add("notUsedChatIds", toJsonArray(notUsedChatIds))
                .add("reasonsChatsNotUsed", toJsonArray(reasonsChatsNotUsed))
                .add("totalBatches", totalBatches)
                .add("successfulBatches", successfulBatches)
                .add("failedBatchCount", getFailedBatchCount())
                .add("failedBatchIndexes", toJsonArrayInt(failedBatchIndexes))
                .add("coverageComplete", coverageComplete)
                .add("coveragePercent", coveragePercent)
                .build();
    }

    @Override
    public String toString() {
        return "CoverageSummary{"
                + "chatsProvided=" + chatsProvided
                + ", chatsUsedInAnalysis=" + chatsUsedInAnalysis
                + ", chatsNotUsed=" + chatsNotUsed
                + ", totalBatches=" + totalBatches
                + ", successfulBatches=" + successfulBatches
                + ", failedBatchIndexes=" + failedBatchIndexes
                + ", coverageComplete=" + coverageComplete
                + ", coveragePercent=" + coveragePercent
                + '}';
    }

    public static final class Builder {

        private int chatsProvided = -1;
        private int chatsUsedInAnalysis = -1;
        private int chatsNotUsed = -1;

        private List<String> allSelectedChatIds = new ArrayList<>();
        private List<String> usedChatIds = new ArrayList<>();
        private List<String> notUsedChatIds = new ArrayList<>();
        private List<String> reasonsChatsNotUsed = new ArrayList<>();

        private int totalBatches = 0;
        private int successfulBatches = 0;
        private List<Integer> failedBatchIndexes = new ArrayList<>();

        private boolean coverageCompleteOverride;
        private boolean coverageCompleteOverrideSet;

        private Builder() {
        }

        public Builder chatsProvided(int chatsProvided) {
            this.chatsProvided = chatsProvided;
            return this;
        }

        public Builder chatsUsedInAnalysis(int chatsUsedInAnalysis) {
            this.chatsUsedInAnalysis = chatsUsedInAnalysis;
            return this;
        }

        public Builder chatsNotUsed(int chatsNotUsed) {
            this.chatsNotUsed = chatsNotUsed;
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

        public Builder notUsedChatIds(List<String> notUsedChatIds) {
            this.notUsedChatIds = notUsedChatIds == null ? new ArrayList<>() : notUsedChatIds;
            return this;
        }

        public Builder reasonsChatsNotUsed(List<String> reasonsChatsNotUsed) {
            this.reasonsChatsNotUsed = reasonsChatsNotUsed == null ? new ArrayList<>() : reasonsChatsNotUsed;
            return this;
        }

        public Builder totalBatches(int totalBatches) {
            this.totalBatches = totalBatches;
            return this;
        }

        public Builder successfulBatches(int successfulBatches) {
            this.successfulBatches = successfulBatches;
            return this;
        }

        public Builder failedBatchIndexes(List<Integer> failedBatchIndexes) {
            this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
            return this;
        }

        public Builder coverageComplete(boolean coverageComplete) {
            this.coverageCompleteOverride = coverageComplete;
            this.coverageCompleteOverrideSet = true;
            return this;
        }

        public CoverageSummary build() {
            return new CoverageSummary(this);
        }
    }

    private void validateCountsAndConsistency() {
        if (chatsProvided < 0) {
            throw new IllegalArgumentException("chatsProvided must be >= 0");
        }
        if (chatsUsedInAnalysis < 0) {
            throw new IllegalArgumentException("chatsUsedInAnalysis must be >= 0");
        }
        if (chatsNotUsed < 0) {
            throw new IllegalArgumentException("chatsNotUsed must be >= 0");
        }
        if (chatsUsedInAnalysis > chatsProvided) {
            throw new IllegalArgumentException("chatsUsedInAnalysis cannot exceed chatsProvided");
        }
        if (chatsNotUsed > chatsProvided) {
            throw new IllegalArgumentException("chatsNotUsed cannot exceed chatsProvided");
        }
        if (totalBatches < successfulBatches) {
            throw new IllegalArgumentException("successfulBatches cannot exceed totalBatches");
        }
        if (!allSelectedChatIds.isEmpty() && chatsProvided != allSelectedChatIds.size()) {
            throw new IllegalArgumentException("chatsProvided must match allSelectedChatIds.size when allSelectedChatIds supplied");
        }

        if (!allSelectedChatIds.isEmpty()) {
            Set<String> expectedNotUsed = new LinkedHashSet<>(allSelectedChatIds);
            expectedNotUsed.removeAll(usedChatIds);
            if (!expectedNotUsed.containsAll(notUsedChatIds)) {
                throw new IllegalArgumentException("notUsedChatIds must be subset of allSelectedChatIds - usedChatIds");
            }
        }

        if (coverageComplete && (!notUsedChatIds.isEmpty() || chatsNotUsed != 0)) {
            throw new IllegalArgumentException("coverageComplete=true requires no not-used chats");
        }
    }

    private static int requireNonNegative(int v, String field) {
        if (v < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
        return v;
    }

    private static int computeCoveragePercent(int provided, int used) {
        if (provided <= 0) {
            return 0;
        }
        int pct = Math.toIntExact(Math.round((used * 100.0) / provided));
        if (pct < 0) {
            return 0;
        }
        return Math.min(100, pct);
    }

    private static Set<String> toOrderedSetLower(List<String> values) {
        Set<String> set = new LinkedHashSet<>();
        if (values != null) {
            for (String v : values) {
                if (v != null && !v.isBlank()) {
                    set.add(v.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return set;
    }

    private static List<String> computeNotUsed(List<String> allSelected, List<String> used) {
        Set<String> all = new LinkedHashSet<>(allSelected == null ? List.of() : allSelected);
        Set<String> u = new LinkedHashSet<>(used == null ? List.of() : used);
        all.removeAll(u);
        return new ArrayList<>(all);
    }

    private static List<String> immutableDistinctStringListLower(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> set = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                set.add(v.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(set));
    }

    private static List<String> immutableDistinctStringListKeepCase(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> set = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                set.add(v.trim());
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(set));
    }

    private static List<Integer> immutableDistinctIntList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<Integer> set = new LinkedHashSet<>();
        for (Integer v : values) {
            if (v != null) {
                int value = v.intValue();
                if (value > 0) {
                    set.add(value);
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(set));
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

    private static jakarta.json.JsonArray toJsonArrayInt(List<Integer> values) {
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
}
