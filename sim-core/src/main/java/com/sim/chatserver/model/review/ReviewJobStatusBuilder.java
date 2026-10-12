package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.List;

public final class ReviewJobStatusBuilder {

    String jobId;
    String requestId;

    ReviewJobStatus.Phase phase;
    boolean done;
    boolean success;

    int totalSelected;
    int totalBatches;
    int completedBatches;
    int failedBatches;
    int retries;

    List<String> allSelectedChatIds = new ArrayList<>();
    List<String> usedChatIds = new ArrayList<>();
    List<String> missingChatIds = new ArrayList<>();

    int coveragePercent;
    boolean coverageComplete;

    long startedAtEpochMs;
    long updatedAtEpochMs;
    long finishedAtEpochMs;

    int httpStatus;
    String message;
    String errorMessage;

    String finalReport;
    String rawResponseBody;
    String contentType;

    List<Integer> failedBatchIndexes = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    String activity;
    int batchProgressPercent = -1;
    boolean running;
    boolean runningSet;

    ReviewJobStatusBuilder() {
    }

    public ReviewJobStatusBuilder jobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public ReviewJobStatusBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ReviewJobStatusBuilder phase(ReviewJobStatus.Phase phase) {
        this.phase = phase;
        return this;
    }

    public ReviewJobStatusBuilder done(boolean done) {
        this.done = done;
        return this;
    }

    public ReviewJobStatusBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public ReviewJobStatusBuilder totalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
        return this;
    }

    public ReviewJobStatusBuilder totalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
        return this;
    }

    public ReviewJobStatusBuilder completedBatches(int completedBatches) {
        this.completedBatches = completedBatches;
        return this;
    }

    public ReviewJobStatusBuilder failedBatches(int failedBatches) {
        this.failedBatches = failedBatches;
        return this;
    }

    public ReviewJobStatusBuilder retries(int retries) {
        this.retries = retries;
        return this;
    }

    public ReviewJobStatusBuilder allSelectedChatIds(List<String> allSelectedChatIds) {
        this.allSelectedChatIds = allSelectedChatIds == null ? new ArrayList<>() : allSelectedChatIds;
        return this;
    }

    public ReviewJobStatusBuilder usedChatIds(List<String> usedChatIds) {
        this.usedChatIds = usedChatIds == null ? new ArrayList<>() : usedChatIds;
        return this;
    }

    public ReviewJobStatusBuilder missingChatIds(List<String> missingChatIds) {
        this.missingChatIds = missingChatIds == null ? new ArrayList<>() : missingChatIds;
        return this;
    }

    public ReviewJobStatusBuilder coveragePercent(int coveragePercent) {
        this.coveragePercent = coveragePercent;
        return this;
    }

    public ReviewJobStatusBuilder coverageComplete(boolean coverageComplete) {
        this.coverageComplete = coverageComplete;
        return this;
    }

    public ReviewJobStatusBuilder startedAtEpochMs(long startedAtEpochMs) {
        this.startedAtEpochMs = startedAtEpochMs;
        return this;
    }

    public ReviewJobStatusBuilder updatedAtEpochMs(long updatedAtEpochMs) {
        this.updatedAtEpochMs = updatedAtEpochMs;
        return this;
    }

    public ReviewJobStatusBuilder finishedAtEpochMs(long finishedAtEpochMs) {
        this.finishedAtEpochMs = finishedAtEpochMs;
        return this;
    }

    public ReviewJobStatusBuilder httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ReviewJobStatusBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ReviewJobStatusBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ReviewJobStatusBuilder finalReport(String finalReport) {
        this.finalReport = finalReport;
        return this;
    }

    public ReviewJobStatusBuilder rawResponseBody(String rawResponseBody) {
        this.rawResponseBody = rawResponseBody;
        return this;
    }

    public ReviewJobStatusBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public ReviewJobStatusBuilder failedBatchIndexes(List<Integer> failedBatchIndexes) {
        this.failedBatchIndexes = failedBatchIndexes == null ? new ArrayList<>() : failedBatchIndexes;
        return this;
    }

    public ReviewJobStatusBuilder warnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
        return this;
    }

    ReviewJobStatusBuilder activity(String activity) {
        this.activity = activity;
        return this;
    }

    ReviewJobStatusBuilder batchProgressPercent(int batchProgressPercent) {
        this.batchProgressPercent = batchProgressPercent;
        return this;
    }

    ReviewJobStatusBuilder running(boolean running) {
        this.running = running;
        this.runningSet = true;
        return this;
    }

    public ReviewJobStatus build() {
        return new ReviewJobStatus(this);
    }
}
