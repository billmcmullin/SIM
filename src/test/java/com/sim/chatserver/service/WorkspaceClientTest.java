package com.sim.chatserver.service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WorkspaceClient
 *
 * @see com.sim.chatserver.service.WorkspaceClient
 * @author bmcmullin
 */
public class WorkspaceClientTest
{

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(WorkspaceResponse)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#isLikelyContextTooLarge(WorkspaceResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        WorkspaceResponse response = null; // UTA: configured value
        boolean result = underTest.isLikelyContextTooLarge(response);

        // Then - assertions for result of method isLikelyContextTooLarge(WorkspaceClient.WorkspaceResponse)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(WorkspaceResponse)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#isLikelyContextTooLarge(WorkspaceResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge2() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        String bodyResult = null; // UTA: configured value
        when(response.body()).thenReturn(bodyResult);

        int statusCodeResult = 413; // UTA: configured value
        when(response.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(response);

        // Then - assertions for result of method isLikelyContextTooLarge(WorkspaceClient.WorkspaceResponse)
        assertTrue(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(WorkspaceResponse)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#isLikelyContextTooLarge(WorkspaceResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge3() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        String bodyResult = null; // UTA: configured value
        when(response.body()).thenReturn(bodyResult);

        int statusCodeResult = 414; // UTA: configured value
        when(response.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(response);

        // Then - assertions for result of method isLikelyContextTooLarge(WorkspaceClient.WorkspaceResponse)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(WorkspaceResponse)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#isLikelyContextTooLarge(WorkspaceResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge4() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        String bodyResult = "bodyResult"; // UTA: default value
        when(response.body()).thenReturn(bodyResult);
        boolean result = underTest.isLikelyContextTooLarge(response);

        // Then - assertions for result of method isLikelyContextTooLarge(WorkspaceClient.WorkspaceResponse)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = "message"; // UTA: default value
        String mode = null; // UTA: configured value
        String sessionId = null; // UTA: configured value
        boolean reset = false; // UTA: default value
        JsonArray attachments = null; // UTA: configured value
        String requestId = "requestId"; // UTA: default value
        WorkspaceResponse result = underTest.sendChat(targetUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat2() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = null; // UTA: configured value
        String mode = null; // UTA: configured value
        String sessionId = "sessionId"; // UTA: configured value
        boolean reset = false; // UTA: default value
        JsonArray attachments = null; // UTA: configured value
        String requestId = "requestId"; // UTA: default value
        WorkspaceResponse result = underTest.sendChat(targetUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);

    }

    /**
     * Parasoft Jtest UTA: Test for sendChat(String, String, String, String, String, boolean, JsonArray, String)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#sendChat(String, String, String, String, String, boolean,
     *      JsonArray, String)
     * @author bmcmullin
     */
    @Test
    public void testSendChat3() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        int maxRetries = 1; // UTA: default value
        Duration requestTimeout = mock(Duration.class);
        WorkspaceClient underTest = new WorkspaceClient(httpClient, maxRetries, requestTimeout);

        // When
        String targetUrl = "targetUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        String message = null; // UTA: configured value
        String mode = null; // UTA: configured value
        String sessionId = null; // UTA: configured value
        boolean reset = false; // UTA: default value
        JsonArray attachments = mock(JsonArray.class);
        boolean isEmptyResult = false; // UTA: configured value
        when(attachments.isEmpty()).thenReturn(isEmptyResult);
        String requestId = "requestId"; // UTA: default value
        WorkspaceResponse result = underTest.sendChat(targetUrl, apiKey, message, mode, sessionId, reset, attachments, requestId);

    }

}
