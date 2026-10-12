package com.sim.chatserver.service;

import java.util.List;

import com.sim.chatserver.util.CommonLiteralConstants;

public final class JobResult {

    private static final String EMPTY = "";
    private static final String MIME_APPLICATION_JSON = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;

    private final int httpStatus;
    private final boolean success;
    private final String message;
    private final String errorMessage;

    private final int totalBatches;
    private final int completedBatches;
    private final int failedBatches;
    private final int retries;

    private final List<String> allSelectedChatIds;
    private final List<String> usedChatIds;
    private final List<String> missingChatIds;

    private final List<Integer> failedBatchIndexes;
    private final List<String> warnings;

    private final String finalReport;
    private final String rawResponseBody;
    private final String contentType;

    public JobResult(
            int httpStatus,
            boolean success,
            String message,
            String errorMessage,
            int totalBatches,
            int completedBatches,
            int failedBatches,
            int retries,
            List<String> allSelectedChatIds,
            List<String> usedChatIds,
            List<String> missingChatIds,
            List<Integer> failedBatchIndexes,
            List<String> warnings,
            String finalReport,
            String rawResponseBody,
            String contentType
    ) {
        this.httpStatus = httpStatus;
        this.success = success;
        this.message = message == null ? EMPTY : message;
        this.errorMessage = errorMessage == null ? EMPTY : errorMessage;

        this.totalBatches = Math.max(0, totalBatches);
        this.completedBatches = Math.max(0, completedBatches);
        this.failedBatches = Math.max(0, failedBatches);
        this.retries = Math.max(0, retries);

        this.allSelectedChatIds = allSelectedChatIds == null ? List.of() : List.copyOf(allSelectedChatIds);
        this.usedChatIds = usedChatIds == null ? List.of() : List.copyOf(usedChatIds);
        this.missingChatIds = missingChatIds == null ? List.of() : List.copyOf(missingChatIds);

        this.failedBatchIndexes = failedBatchIndexes == null ? List.of() : List.copyOf(failedBatchIndexes);
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);

        this.finalReport = finalReport == null ? EMPTY : finalReport;
        this.rawResponseBody = rawResponseBody == null ? EMPTY : rawResponseBody;
        this.contentType = contentType == null || contentType.isBlank() ? MIME_APPLICATION_JSON : contentType;
    }

    int httpStatus() {
        return httpStatus;
    }

    boolean success() {
        return success;
    }

    String message() {
        return message;
    }

    String errorMessage() {
        return errorMessage;
    }

    int totalBatches() {
        return totalBatches;
    }

    int completedBatches() {
        return completedBatches;
    }

    int failedBatches() {
        return failedBatches;
    }

    int retries() {
        return retries;
    }

    int coveragePercent() {
        return 0;
    }

    boolean coverageComplete() {
        return missingChatIds.isEmpty();
    }

    List<String> allSelectedChatIds() {
        return allSelectedChatIds;
    }

    List<String> usedChatIds() {
        return usedChatIds;
    }

    List<String> missingChatIds() {
        return missingChatIds;
    }

    List<Integer> failedBatchIndexes() {
        return failedBatchIndexes;
    }

    List<String> warnings() {
        return warnings;
    }

    String finalReport() {
        return finalReport;
    }

    String rawResponseBody() {
        return rawResponseBody;
    }

    String contentType() {
        return contentType;
    }
}
