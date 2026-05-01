package com.sim.chatserver.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.OrchestrationResult;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator.ProgressListener;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewMapReduceOrchestrator
 *
 * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator
 * @author bmcmullin
 */
public class WidgetReviewMapReduceOrchestratorTest
{

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String)
     * @author bmcmullin
     */
    @Test
    public void testRun() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        OrchestrationResult result = underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String)
     * @author bmcmullin
     */
    @Test
    public void testRun2() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String)
     * @author bmcmullin
     */
    @Test
    public void testRun3() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = new ArrayList<SelectedEntry>(); // UTA: default value
        String requestId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun4() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = "requestId"; // UTA: configured value
        ProgressListener progress = null; // UTA: configured value
        OrchestrationResult result = underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun5() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        ProgressListener progress = mock(ProgressListener.class);
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun6() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = null; // UTA: configured value
        String requestId = null; // UTA: configured value
        ProgressListener progress = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for run(String, String, String, String, String, boolean, JsonArray, List, String,
     * ProgressListener)
     *
     * @see com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator#run(String, String, String, String, String,
     *      boolean, JsonArray, List, String, ProgressListener)
     * @author bmcmullin
     */
    @Test
    public void testRun7() throws Throwable
    {
        // Given
        WorkspaceClient workspaceClient = mock(WorkspaceClient.class);
        ReviewContextBuilderService contextBuilderService = mock(ReviewContextBuilderService.class);
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        WidgetReviewMapReduceOrchestrator underTest = new WidgetReviewMapReduceOrchestrator(workspaceClient, contextBuilderService, promptTemplateService);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String userMessage = "userMessage"; // UTA: default value
        String mode = "mode"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        List<SelectedEntry> selectedEntries = new ArrayList<SelectedEntry>(); // UTA: default value
        String requestId = null; // UTA: configured value
        ProgressListener progress = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.run(targetUrl, apiKey, userMessage, mode, sessionId, requestReset, attachments, selectedEntries, requestId, progress);
        });

    }
}
