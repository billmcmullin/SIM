package com.sim.chatserver.service;

import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.WorkspaceResponse;

final class ReduceExecutionResult {

    final WorkspaceResponse response;
    final ReduceRequest reduceRequest;
    final ReduceResult reduceResult;

    ReduceExecutionResult(WorkspaceResponse response, ReduceRequest reduceRequest, ReduceResult reduceResult) {
        this.response = response;
        this.reduceRequest = reduceRequest;
        this.reduceResult = reduceResult;
    }
}

