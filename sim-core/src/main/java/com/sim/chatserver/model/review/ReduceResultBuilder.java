package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class ReduceResultBuilder {

    String requestId;

    int httpStatus;
    boolean success;
    boolean retryUsed;
    boolean contextTooLargeDetected;

    String finalReport;
    String errorMessage;

    int totalSelected;
    int totalBatches;
    int mapOutputsReceived;

    List<Integer> failedBatchIndexes = new ArrayList<>();
    List<String> failedBatchReasons = new ArrayList<>();

    List<String> allSelectedChatIds = new ArrayList<>();
    List<String> usedChatIds = new ArrayList<>();
    List<String> missingChatIds = new ArrayList<>();
    boolean coverageCompleteOverride;

    long latencyMs;

    ReduceResultBuilder() {
    }

    public ReduceResultBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ReduceResultBuilder httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ReduceResultBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public ReduceResultBuilder retryUsed(boolean retryUsed) {
        this.retryUsed = retryUsed;
        return this;
    }

    public ReduceResultBuilder contextTooLargeDetected(boolean contextTooLargeDetected) {
        this.contextTooLargeDetected = contextTooLargeDetected;
        return this;
    }

    public ReduceResultBuilder finalReport(String finalReport) {
        this.finalReport = finalReport;
        return this;
    }

    public ReduceResultBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ReduceResultBuilder totalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
        return this;
    }

    public ReduceResultBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public ReduceResultBuilder mapOutputsReceived(int mapOutputsReceived) {
        this.mapOutputsReceived = mapOutputsReceived;
        return this;
    }

    public ReduceResultBuilder failedBatchIndexes(List<Integer> failedBatchIndexes) {
        this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
        return this;
    }

    public ReduceResultBuilder failedBatchReasons(List<String> failedBatchReasons) {
        this.failedBatchReasons = failedBatchReasons == null ? new ArrayList<>() : failedBatchReasons;
        return this;
    }

    public ReduceResultBuilder allSelectedChatIds(List<String> allSelectedChatIds) {
        this.allSelectedChatIds = allSelectedChatIds == null ? new ArrayList<>() : allSelectedChatIds;
        return this;
    }

    public ReduceResultBuilder usedChatIds(List<String> usedChatIds) {
        this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
        return this;
    }

    public ReduceResultBuilder missingChatIds(List<String> missingChatIds) {
        this.missingChatIds = missingChatIds == null ? new ArrayList<>() : missingChatIds;
        return this;
    }

    public ReduceResultBuilder coverageComplete(boolean coverageComplete) {
        this.coverageCompleteOverride = coverageComplete;
        return this;
    }

    public ReduceResultBuilder latencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
        return this;
    }

    public ReduceResult build() {
        return new ReduceResult(this);
    }
}
