package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class ReduceRequestBuilder {

    String requestId;
    String targetUrl;
    String mode;
    String sessionId;
    boolean reset;
    String controlledPrompt;

    int totalSelected;
    int totalBatches;
    List<String> mapOutputs = new ArrayList<>();
    List<Integer> failedBatchIndexes = new ArrayList<>();
    List<String> failedBatchReasons = new ArrayList<>();

    List<String> allSelectedChatIds = new ArrayList<>();
    List<String> usedChatIds = new ArrayList<>();

    List<String> missingChatIds = new ArrayList<>();
    boolean coverageCompleteOverride;

    ReduceRequestBuilder() {
    }

    public ReduceRequestBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ReduceRequestBuilder targetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        return this;
    }

    public ReduceRequestBuilder mode(String mode) {
        this.mode = mode;
        return this;
    }

    public ReduceRequestBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ReduceRequestBuilder reset(boolean reset) {
        this.reset = reset;
        return this;
    }

    public ReduceRequestBuilder controlledPrompt(String controlledPrompt) {
        this.controlledPrompt = controlledPrompt;
        return this;
    }

    public ReduceRequestBuilder totalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
        return this;
    }

    public ReduceRequestBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public ReduceRequestBuilder mapOutputs(List<String> mapOutputs) {
        this.mapOutputs = mapOutputs == null ? new ArrayList<>() : mapOutputs;
        return this;
    }

    public ReduceRequestBuilder failedBatchIndexes(List<Integer> failedBatchIndexes) {
        this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
        return this;
    }

    public ReduceRequestBuilder failedBatchReasons(List<String> failedBatchReasons) {
        this.failedBatchReasons = failedBatchReasons == null ? new ArrayList<>() : failedBatchReasons;
        return this;
    }

    public ReduceRequestBuilder allSelectedChatIds(List<String> allSelectedChatIds) {
        this.allSelectedChatIds = allSelectedChatIds == null ? new ArrayList<>() : allSelectedChatIds;
        return this;
    }

    public ReduceRequestBuilder usedChatIds(List<String> usedChatIds) {
        this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
        return this;
    }

    public ReduceRequestBuilder missingChatIds(List<String> missingChatIds) {
        this.missingChatIds = missingChatIds == null ? new ArrayList<>() : missingChatIds;
        return this;
    }

    public ReduceRequestBuilder coverageComplete(boolean coverageComplete) {
        this.coverageCompleteOverride = coverageComplete;
        return this;
    }

    public ReduceRequest build() {
        return new ReduceRequest(this);
    }
}
