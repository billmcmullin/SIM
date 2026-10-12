package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class MapBatchResultBuilder {

    String requestId;
    int batchIndex;
    int totalBatches;
    String batchId;

    int httpStatus;
    boolean success;
    boolean retryUsed;
    boolean contextTooLargeDetected;

    String modelOutput;
    String errorMessage;

    int inputEntriesCount;
    int usedEntriesCount;
    int omittedEntriesCount;

    List<String> batchChatIds = new ArrayList<>();
    List<String> usedChatIds = new ArrayList<>();
    List<String> omittedChatIds = new ArrayList<>();

    List<String> expectedChatIds = new ArrayList<>();
    List<String> foundChatIds = new ArrayList<>();
    List<String> missingExpectedChatIds = new ArrayList<>();
    Boolean coverageCompleteOverride;

    long latencyMs;

    MapBatchResultBuilder() {
    }

    public MapBatchResultBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public MapBatchResultBuilder batchIndex(int batchIndex) {
        this.batchIndex = batchIndex;
        return this;
    }

    public MapBatchResultBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public MapBatchResultBuilder batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    public MapBatchResultBuilder httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public MapBatchResultBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public MapBatchResultBuilder retryUsed(boolean retryUsed) {
        this.retryUsed = retryUsed;
        return this;
    }

    public MapBatchResultBuilder contextTooLargeDetected(boolean contextTooLargeDetected) {
        this.contextTooLargeDetected = contextTooLargeDetected;
        return this;
    }

    public MapBatchResultBuilder modelOutput(String modelOutput) {
        this.modelOutput = modelOutput;
        return this;
    }

    public MapBatchResultBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public MapBatchResultBuilder inputEntriesCount(int inputEntriesCount) {
        this.inputEntriesCount = inputEntriesCount;
        return this;
    }

    public MapBatchResultBuilder usedEntriesCount(int usedEntriesCount) {
        this.usedEntriesCount = usedEntriesCount;
        return this;
    }

    public MapBatchResultBuilder omittedEntriesCount(int omittedEntriesCount) {
        this.omittedEntriesCount = omittedEntriesCount;
        return this;
    }

    public MapBatchResultBuilder batchChatIds(List<String> batchChatIds) {
        this.batchChatIds = batchChatIds == null ? new ArrayList<>() : batchChatIds;
        return this;
    }

    public MapBatchResultBuilder usedChatIds(List<String> usedChatIds) {
        this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
        return this;
    }

    public MapBatchResultBuilder omittedChatIds(List<String> omittedChatIds) {
        this.omittedChatIds = omittedChatIds == null ? new ArrayList<>() : omittedChatIds;
        return this;
    }

    public MapBatchResultBuilder expectedChatIds(List<String> expectedChatIds) {
        this.expectedChatIds = expectedChatIds == null ? new ArrayList<>() : expectedChatIds;
        return this;
    }

    public MapBatchResultBuilder foundChatIds(List<String> foundChatIds) {
        this.foundChatIds = foundChatIds == null ? new ArrayList<>() : foundChatIds;
        return this;
    }

    public MapBatchResultBuilder missingExpectedChatIds(List<String> missingExpectedChatIds) {
        this.missingExpectedChatIds = missingExpectedChatIds == null ? new ArrayList<>() : missingExpectedChatIds;
        return this;
    }

    public MapBatchResultBuilder coverageComplete(boolean coverageComplete) {
        this.coverageCompleteOverride = Boolean.valueOf(coverageComplete);
        return this;
    }

    public MapBatchResultBuilder latencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
        return this;
    }

    public MapBatchResult build() {
        return new MapBatchResult(this);
    }
}
