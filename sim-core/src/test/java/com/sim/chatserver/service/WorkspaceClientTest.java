package com.sim.chatserver.service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import jakarta.json.JsonArray;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String bodyResult = "bodyResult"; // UTA: default value
        when(response.body()).thenReturn(bodyResult);

        int statusCodeResult = 413; // UTA: configured value
        when(response.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(response);

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
        String bodyResult = null; // UTA: configured value
        when(response.body()).thenReturn(bodyResult);

        int statusCodeResult = 414; // UTA: configured value
        when(response.statusCode()).thenReturn(statusCodeResult);
        boolean result = underTest.isLikelyContextTooLarge(response);

    }

    /**
     * Parasoft Jtest UTA: Test for isLikelyContextTooLarge(WorkspaceResponse)
     *
     * @see com.sim.chatserver.service.WorkspaceClient#isLikelyContextTooLarge(WorkspaceResponse)
     * @author bmcmullin
     */
    @Test
    public void testIsLikelyContextTooLarge5() throws Throwable
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

    }

}
