package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.util.List;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceResponse;

import jakarta.json.Json;

final class SummaryOrchestrationRunner {

    private static final String MODE_CHAT = com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String SESSION_DASHBOARD_DAILY_SUMMARY = "dashboard-daily-summary";
    private static final String MSG_SUMMARY_ORCHESTRATION_FAILED = "Summary orchestration failed";

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
                    MODE_CHAT,
                    SESSION_DASHBOARD_DAILY_SUMMARY,
                    true,
                    Json.createArrayBuilder().build(),
                    entries,
                    requestId
            ).finalResponse();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            throw new IOException(MSG_SUMMARY_ORCHESTRATION_FAILED, e);
        }
    }
}

