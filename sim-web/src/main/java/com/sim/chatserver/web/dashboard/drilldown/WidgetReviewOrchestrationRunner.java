package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.util.List;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;

import jakarta.json.JsonArray;

final class WidgetReviewOrchestrationRunner {

    private WidgetReviewOrchestrationRunner() {
    }

    static WidgetReviewMapReduceOrchestrator.OrchestrationResult run(
            WidgetReviewMapReduceOrchestrator orchestrator,
            String targetUrl,
            String apiKey,
            String userMessage,
            String mode,
            String sessionId,
            boolean requestReset,
            JsonArray attachments,
            List<SelectedEntry> selectedEntries,
            String requestId,
            WidgetReviewMapReduceOrchestrator.ProgressListener progressListener
    ) throws IOException, InterruptedException {
        try {
            return orchestrator.run(
                    targetUrl,
                    apiKey,
                    userMessage,
                    mode,
                    sessionId,
                    requestReset,
                    attachments,
                    selectedEntries,
                    requestId,
                    progressListener
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Throwable e) {
            throw new IOException("Map-reduce orchestration failed", e);
        }
    }
}
