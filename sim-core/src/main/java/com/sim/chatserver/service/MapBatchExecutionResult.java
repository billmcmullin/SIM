package com.sim.chatserver.service;

import java.util.List;

import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;

final class MapBatchExecutionResult {

    final String outputText;
    final MapBatchResult result;
    final BatchFailure failure;
    final List<String> usedIdsDetected;

    MapBatchExecutionResult(
            String outputText,
            MapBatchResult result,
            BatchFailure failure,
            List<String> usedIdsDetected
    ) {
        this.outputText = outputText;
        this.result = result;
        this.failure = failure;
        this.usedIdsDetected = usedIdsDetected == null ? List.of() : List.copyOf(usedIdsDetected);
    }
}
