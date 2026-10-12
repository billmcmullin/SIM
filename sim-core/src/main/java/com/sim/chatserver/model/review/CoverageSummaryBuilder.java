package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class CoverageSummaryBuilder {

    int chatsProvided = -1;
    int chatsUsedInAnalysis = -1;
    int chatsNotUsed = -1;

    List<String> allSelectedChatIds = new ArrayList<>();
    List<String> usedChatIds = new ArrayList<>();
    List<String> notUsedChatIds = new ArrayList<>();
    List<String> reasonsChatsNotUsed = new ArrayList<>();

    int totalBatches = 0;
    int successfulBatches = 0;
    List<Integer> failedBatchIndexes = new ArrayList<>();

    boolean coverageCompleteOverride;
    boolean coverageCompleteOverrideSet;

    CoverageSummaryBuilder() {
    }

    public CoverageSummaryBuilder chatsProvided(int chatsProvided) {
        this.chatsProvided = chatsProvided;
        return this;
    }

    public CoverageSummaryBuilder chatsUsedInAnalysis(int chatsUsedInAnalysis) {
        this.chatsUsedInAnalysis = chatsUsedInAnalysis;
        return this;
    }

    public CoverageSummaryBuilder chatsNotUsed(int chatsNotUsed) {
        this.chatsNotUsed = chatsNotUsed;
        return this;
    }

    public CoverageSummaryBuilder allSelectedChatIds(List<String> allSelectedChatIds) {
        this.allSelectedChatIds = allSelectedChatIds == null ? new ArrayList<>() : allSelectedChatIds;
        return this;
    }

    public CoverageSummaryBuilder usedChatIds(List<String> usedChatIds) {
        this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
        return this;
    }

    public CoverageSummaryBuilder notUsedChatIds(List<String> notUsedChatIds) {
        this.notUsedChatIds = notUsedChatIds == null ? new ArrayList<>() : notUsedChatIds;
        return this;
    }

    public CoverageSummaryBuilder reasonsChatsNotUsed(List<String> reasonsChatsNotUsed) {
        this.reasonsChatsNotUsed = reasonsChatsNotUsed == null ? new ArrayList<>() : reasonsChatsNotUsed;
        return this;
    }

    public CoverageSummaryBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public CoverageSummaryBuilder successfulBatches(int successfulBatches) {
        this.successfulBatches = successfulBatches;
        return this;
    }

    public CoverageSummaryBuilder failedBatchIndexes(List<Integer> failedBatchIndexes) {
        this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
        return this;
    }

    public CoverageSummaryBuilder coverageComplete(boolean coverageComplete) {
        this.coverageCompleteOverride = coverageComplete;
        this.coverageCompleteOverrideSet = true;
        return this;
    }

    public CoverageSummary build() {
        return new CoverageSummary(this);
    }
}
