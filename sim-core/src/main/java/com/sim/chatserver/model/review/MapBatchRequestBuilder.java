package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

import com.sim.chatserver.model.SelectedEntry;

public final class MapBatchRequestBuilder {

    String requestId;
    int totalSelected;
    int totalBatches;
    int batchIndex;
    String batchId;
    List<SelectedEntry> entries = new ArrayList<>();

    String targetUrl;
    String mode;
    String sessionId;
    boolean reset;
    String controlledPrompt;

    List<String> authoritativeAllSelectedChatIds = new ArrayList<>();
    List<String> expectedChatIds = new ArrayList<>();

    MapBatchRequestBuilder() {
    }

    public MapBatchRequestBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public MapBatchRequestBuilder totalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
        return this;
    }

    public MapBatchRequestBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public MapBatchRequestBuilder batchIndex(int batchIndex) {
        this.batchIndex = batchIndex;
        return this;
    }

    public MapBatchRequestBuilder batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    public MapBatchRequestBuilder entries(List<SelectedEntry> entries) {
        this.entries = entries == null ? new ArrayList<>() : entries;
        return this;
    }

    public MapBatchRequestBuilder targetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        return this;
    }

    public MapBatchRequestBuilder mode(String mode) {
        this.mode = mode;
        return this;
    }

    public MapBatchRequestBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public MapBatchRequestBuilder reset(boolean reset) {
        this.reset = reset;
        return this;
    }

    public MapBatchRequestBuilder controlledPrompt(String controlledPrompt) {
        this.controlledPrompt = controlledPrompt;
        return this;
    }

    public MapBatchRequestBuilder authoritativeAllSelectedChatIds(List<String> authoritativeAllSelectedChatIds) {
        this.authoritativeAllSelectedChatIds
                = authoritativeAllSelectedChatIds == null ? new ArrayList<>() : authoritativeAllSelectedChatIds;
        return this;
    }

    public MapBatchRequestBuilder expectedChatIds(List<String> expectedChatIds) {
        this.expectedChatIds = expectedChatIds == null ? new ArrayList<>() : expectedChatIds;
        return this;
    }

    public MapBatchRequest build() {
        return new MapBatchRequest(this);
    }
}
