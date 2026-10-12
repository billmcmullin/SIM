package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class BatchFailureBuilder {

    String requestId;
    int batchIndex;
    int totalBatches;
    String batchId;

    String reasonCode;
    String message;

    int httpStatus = 0;
    boolean retryAttempted;
    boolean retrySucceeded;
    boolean contextTooLargeDetected;

    long latencyMs = 0L;
    List<String> batchChatIds = new ArrayList<>();

    BatchFailureBuilder() {
    }

    public BatchFailureBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public BatchFailureBuilder batchIndex(int batchIndex) {
        this.batchIndex = batchIndex;
        return this;
    }

    public BatchFailureBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public BatchFailureBuilder batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    public BatchFailureBuilder reasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    public BatchFailureBuilder message(String message) {
        this.message = message;
        return this;
    }

    public BatchFailureBuilder httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public BatchFailureBuilder retryAttempted(boolean retryAttempted) {
        this.retryAttempted = retryAttempted;
        return this;
    }

    public BatchFailureBuilder retrySucceeded(boolean retrySucceeded) {
        this.retrySucceeded = retrySucceeded;
        return this;
    }

    public BatchFailureBuilder contextTooLargeDetected(boolean contextTooLargeDetected) {
        this.contextTooLargeDetected = contextTooLargeDetected;
        return this;
    }

    public BatchFailureBuilder latencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
        return this;
    }

    public BatchFailureBuilder batchChatIds(List<String> batchChatIds) {
        this.batchChatIds = batchChatIds == null ? new ArrayList<>() : batchChatIds;
        return this;
    }

    public BatchFailure build() {
        return new BatchFailure(this);
    }
}
