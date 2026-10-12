package com.sim.chatserver.service;

import java.util.List;

import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.WorkspaceResponse;

public final class OrchestrationResult {

    private final WorkspaceResponse finalResponse;
    private final List<String> mapOutputs;
    private final List<Integer> failedBatchIndexes;
    private final List<MapBatchResult> mapBatchResults;
    private final ReduceRequest reduceRequest;
    private final ReduceResult reduceResult;
    private final int totalSelected;
    private final int totalBatches;
    private final List<BatchFailure> batchFailures;

    private final List<String> allSelectedChatIds;
    private final List<String> missingChatIds;
    private final boolean coverageComplete;
    private final int coveragePassesUsed;

    OrchestrationResult(
            WorkspaceResponse finalResponse,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            List<MapBatchResult> mapBatchResults,
            ReduceRequest reduceRequest,
            ReduceResult reduceResult,
            int totalSelected,
            int totalBatches
    ) {
        this(finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                totalSelected, totalBatches, List.of(), List.of(), List.of(), false, 0);
    }

    OrchestrationResult(
            WorkspaceResponse finalResponse,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            List<MapBatchResult> mapBatchResults,
            ReduceRequest reduceRequest,
            ReduceResult reduceResult,
            int totalSelected,
            int totalBatches,
            List<BatchFailure> batchFailures,
            List<String> allSelectedChatIds,
            List<String> missingChatIds,
            boolean coverageComplete,
            int coveragePassesUsed
    ) {
        this.finalResponse = finalResponse;
        this.mapOutputs = mapOutputs == null ? List.of() : List.copyOf(mapOutputs);
        this.failedBatchIndexes = failedBatchIndexes == null ? List.of() : List.copyOf(failedBatchIndexes);
        this.mapBatchResults = mapBatchResults == null ? List.of() : List.copyOf(mapBatchResults);
        this.reduceRequest = reduceRequest;
        this.reduceResult = reduceResult;
        this.totalSelected = totalSelected;
        this.totalBatches = totalBatches;
        this.batchFailures = batchFailures == null ? List.of() : List.copyOf(batchFailures);

        this.allSelectedChatIds = allSelectedChatIds == null ? List.of() : List.copyOf(allSelectedChatIds);
        this.missingChatIds = missingChatIds == null ? List.of() : List.copyOf(missingChatIds);
        this.coverageComplete = coverageComplete;
        this.coveragePassesUsed = Math.max(0, coveragePassesUsed);
    }

    final OrchestrationResult withBatchFailures(List<BatchFailure> failures) {
        return new OrchestrationResult(
                finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                totalSelected, totalBatches, failures, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed
        );
    }

    final OrchestrationResult withCoverage(
            List<String> allSelectedChatIds,
            List<String> missingChatIds,
            boolean coverageComplete,
            int coveragePassesUsed
    ) {
        return new OrchestrationResult(
                finalResponse, mapOutputs, failedBatchIndexes, mapBatchResults, reduceRequest, reduceResult,
                totalSelected, totalBatches, batchFailures, allSelectedChatIds, missingChatIds, coverageComplete, coveragePassesUsed
        );
    }

    public WorkspaceResponse finalResponse() {
        return finalResponse;
    }

    List<String> mapOutputs() {
        return mapOutputs;
    }

    public List<Integer> failedBatchIndexes() {
        return failedBatchIndexes;
    }

    public List<MapBatchResult> mapBatchResults() {
        return mapBatchResults;
    }

    public ReduceRequest reduceRequest() {
        return reduceRequest;
    }

    public ReduceResult reduceResult() {
        return reduceResult;
    }

    int totalSelected() {
        return totalSelected;
    }

    public int totalBatches() {
        return totalBatches;
    }

    public List<BatchFailure> batchFailures() {
        return batchFailures;
    }

    List<String> allSelectedChatIds() {
        return allSelectedChatIds;
    }

    public List<String> missingChatIds() {
        return missingChatIds;
    }

    boolean coverageComplete() {
        return coverageComplete;
    }

    int coveragePassesUsed() {
        return coveragePassesUsed;
    }

    boolean hasFailures() {
        return !failedBatchIndexes.isEmpty();
    }
}

