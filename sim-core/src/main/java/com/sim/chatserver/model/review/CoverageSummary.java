// src/main/java/com/sim/chatserver/model/review/CoverageSummary.java
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
 * Immutable coverage accounting summary for map-reduce reporting.
 *
 * Deterministic source of truth: - allSelectedChatIds is authoritative input
 * set - usedChatIds are IDs successfully covered - notUsedChatIds = allSelected
 * - used
 */
public final class CoverageSummary {

    private static final String FIELD_CHATS_PROVIDED = "chatsProvided";
    private static final String FIELD_CHATS_USED_IN_ANALYSIS = "chatsUsedInAnalysis";
    private static final String FIELD_CHATS_NOT_USED = "chatsNotUsed";
    private static final String FIELD_ALL_SELECTED_CHAT_IDS = CommonLiteralConstants.FIELD_ALL_SELECTED_CHAT_IDS;
    private static final String FIELD_USED_CHAT_IDS = CommonLiteralConstants.FIELD_USED_CHAT_IDS;
    private static final String FIELD_NOT_USED_CHAT_IDS = "notUsedChatIds";
    private static final String FIELD_REASONS_CHATS_NOT_USED = "reasonsChatsNotUsed";
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_SUCCESSFUL_BATCHES = "successfulBatches";
    private static final String FIELD_FAILED_BATCH_COUNT = "failedBatchCount";
    private static final String FIELD_FAILED_BATCH_INDEXES = CommonLiteralConstants.FIELD_FAILED_BATCH_INDEXES;
    private static final String FIELD_COVERAGE_COMPLETE = CommonLiteralConstants.FIELD_COVERAGE_COMPLETE;
    private static final String FIELD_COVERAGE_PERCENT = CommonLiteralConstants.FIELD_COVERAGE_PERCENT;
    private static final String ERR_REQUIRED_NON_NEGATIVE_SUFFIX = " must be >= 0";
    private static final String ERR_CHATS_PROVIDED_NON_NEGATIVE = "chatsProvided must be >= 0";
    private static final String ERR_CHATS_USED_NON_NEGATIVE = "chatsUsedInAnalysis must be >= 0";
    private static final String ERR_CHATS_NOT_USED_NON_NEGATIVE = "chatsNotUsed must be >= 0";
    private static final String ERR_CHATS_USED_GT_PROVIDED = "chatsUsedInAnalysis cannot exceed chatsProvided";
    private static final String ERR_CHATS_NOT_USED_GT_PROVIDED = "chatsNotUsed cannot exceed chatsProvided";
    private static final String ERR_SUCCESSFUL_BATCHES_GT_TOTAL = "successfulBatches cannot exceed totalBatches";
    private static final String ERR_CHATS_PROVIDED_MISMATCH_ALL_IDS = "chatsProvided must match allSelectedChatIds.size when allSelectedChatIds supplied";
    private static final String ERR_NOT_USED_SUBSET = "notUsedChatIds must be subset of allSelectedChatIds - usedChatIds";
    private static final String ERR_COVERAGE_COMPLETE_REQUIRES_NONE = "coverageComplete=true requires no not-used chats";
    private static final String TOSTRING_PREFIX = "CoverageSummary{";
    private static final String TOSTRING_CHATS_PROVIDED_PREFIX = "chatsProvided=";
    private static final String TOSTRING_CHATS_USED_PREFIX = ", chatsUsedInAnalysis=";
    private static final String TOSTRING_CHATS_NOT_USED_PREFIX = ", chatsNotUsed=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_SUCCESSFUL_BATCHES_PREFIX = ", successfulBatches=";
    private static final String TOSTRING_FAILED_INDEXES_PREFIX = ", failedBatchIndexes=";
    private static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = CommonLiteralConstants.TOSTRING_COVERAGE_COMPLETE_PREFIX;
    private static final String TOSTRING_COVERAGE_PERCENT_PREFIX = ", coveragePercent=";

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

    CoverageSummary(CoverageSummaryBuilder b) {
        this.allSelectedChatIds = immutableDistinctStringListLower(b.allSelectedChatIds);
        this.usedChatIds = immutableDistinctStringListLower(b.usedChatIds);
        this.reasonsChatsNotUsed = immutableDistinctStringListKeepCase(b.reasonsChatsNotUsed);

        int provided = b.chatsProvided >= 0 ? b.chatsProvided : this.allSelectedChatIds.size();
        this.chatsProvided = requireNonNegative(provided, FIELD_CHATS_PROVIDED);

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

    public static CoverageSummaryBuilder builder() {
        return new CoverageSummaryBuilder();
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
        for (String usedId : used) {
            notUsed.remove(usedId);
        }

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

    private int getFailedBatchCount() {
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
            .add(FIELD_CHATS_PROVIDED, chatsProvided)
            .add(FIELD_CHATS_USED_IN_ANALYSIS, chatsUsedInAnalysis)
            .add(FIELD_CHATS_NOT_USED, chatsNotUsed)
            .add(FIELD_ALL_SELECTED_CHAT_IDS, toJsonArray(allSelectedChatIds))
            .add(FIELD_USED_CHAT_IDS, toJsonArray(usedChatIds))
            .add(FIELD_NOT_USED_CHAT_IDS, toJsonArray(notUsedChatIds))
            .add(FIELD_REASONS_CHATS_NOT_USED, toJsonArray(reasonsChatsNotUsed))
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_SUCCESSFUL_BATCHES, successfulBatches)
            .add(FIELD_FAILED_BATCH_COUNT, getFailedBatchCount())
            .add(FIELD_FAILED_BATCH_INDEXES, toJsonArrayInt(failedBatchIndexes))
            .add(FIELD_COVERAGE_COMPLETE, coverageComplete)
            .add(FIELD_COVERAGE_PERCENT, coveragePercent)
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_CHATS_PROVIDED_PREFIX + chatsProvided
            + TOSTRING_CHATS_USED_PREFIX + chatsUsedInAnalysis
            + TOSTRING_CHATS_NOT_USED_PREFIX + chatsNotUsed
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_SUCCESSFUL_BATCHES_PREFIX + successfulBatches
            + TOSTRING_FAILED_INDEXES_PREFIX + failedBatchIndexes
            + TOSTRING_COVERAGE_COMPLETE_PREFIX + coverageComplete
            + TOSTRING_COVERAGE_PERCENT_PREFIX + coveragePercent
                + '}';
    }

    private void validateCountsAndConsistency() {
        if (chatsProvided < 0) {
            throw new IllegalArgumentException(ERR_CHATS_PROVIDED_NON_NEGATIVE);
        }
        if (chatsUsedInAnalysis < 0) {
            throw new IllegalArgumentException(ERR_CHATS_USED_NON_NEGATIVE);
        }
        if (chatsNotUsed < 0) {
            throw new IllegalArgumentException(ERR_CHATS_NOT_USED_NON_NEGATIVE);
        }
        if (chatsUsedInAnalysis > chatsProvided) {
            throw new IllegalArgumentException(ERR_CHATS_USED_GT_PROVIDED);
        }
        if (chatsNotUsed > chatsProvided) {
            throw new IllegalArgumentException(ERR_CHATS_NOT_USED_GT_PROVIDED);
        }
        if (totalBatches < successfulBatches) {
            throw new IllegalArgumentException(ERR_SUCCESSFUL_BATCHES_GT_TOTAL);
        }
        if (!allSelectedChatIds.isEmpty() && chatsProvided != allSelectedChatIds.size()) {
            throw new IllegalArgumentException(ERR_CHATS_PROVIDED_MISMATCH_ALL_IDS);
        }

        if (!allSelectedChatIds.isEmpty()) {
            Set<String> expectedNotUsed = new LinkedHashSet<>(allSelectedChatIds);
            for (String usedId : usedChatIds) {
                expectedNotUsed.remove(usedId);
            }
            if (!expectedNotUsed.containsAll(notUsedChatIds)) {
                throw new IllegalArgumentException(ERR_NOT_USED_SUBSET);
            }
        }

        if (coverageComplete && (!notUsedChatIds.isEmpty() || chatsNotUsed != 0)) {
            throw new IllegalArgumentException(ERR_COVERAGE_COMPLETE_REQUIRES_NONE);
        }
    }

    private static int requireNonNegative(int v, String field) {
        if (v < 0) {
            throw new IllegalArgumentException(field + ERR_REQUIRED_NON_NEGATIVE_SUFFIX);
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
        for (String usedId : u) {
            all.remove(usedId);
        }
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
        for (String text : toNumberTexts(values)) {
            int value = Integer.parseInt(text);
            if (value > 0) {
                set.add(Integer.valueOf(value));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(set));
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
        for (String text : toNumberTexts(values)) {
            b.add(Integer.parseInt(text));
        }
        return b.build();
    }

}

