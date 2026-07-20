package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.util.List;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import jakarta.json.Json;

final class SummaryOrchestrationRunner {

    private SummaryOrchestrationRunner() {
    }

    static WorkspaceResponse run(
            WidgetReviewMapReduceOrchestrator orchestrator,
            String targetUrl,
            String apiKey,
            String summaryPrompt,
            List<SelectedEntry> entries,
            String requestId
    ) throws IOException, InterruptedException {
        try {
            return orchestrator.run(
                    targetUrl,
                    apiKey,
                    summaryPrompt,
                    "chat",
                    "dashboard-daily-summary",
                    true,
                    Json.createArrayBuilder().build(),
                    entries,
                    requestId
            ).finalResponse();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            throw new IOException("Summary orchestration failed", e);
        }
    }
}
