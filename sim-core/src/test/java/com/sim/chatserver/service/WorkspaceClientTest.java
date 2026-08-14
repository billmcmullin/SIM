package com.sim.chatserver.service;

import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WorkspaceClient and its lightweight response DTO.
 *
 * @see com.sim.chatserver.service.WorkspaceClient
 */
public class WorkspaceClientTest
{

    @Test
    public void isLikelyContextTooLarge_returnsFalse_whenResponseIsNull()
    {
        WorkspaceClient underTest = createUnderTest();

        boolean result = underTest.isLikelyContextTooLarge(null);

        assertFalse(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenStatusCodeIs413()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(413);
        when(response.body()).thenReturn(null);

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenBodyContainsContextSignal()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("Maximum context length exceeded for model");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenJsonErrorContainsTokenLimitPhrase()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("{\"error\":\"token limit reached\"}");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsFalse_whenNoKnownSignalExists()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(414);
        when(response.body()).thenReturn("short request URI");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertFalse(result);
    }

    @Test
    public void workspaceResponse_bodyGetter_returnsConstructorBody()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        String result = underTest.body();

        assertEquals("body", result);
    }

    @Test
    public void workspaceResponse_contentTypeGetter_returnsConstructorContentType()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        String result = underTest.contentType();

        assertEquals("application/json", result);
    }

    @Test
    public void workspaceResponse_statusCodeGetter_returnsConstructorStatusCode()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(201, "body", "application/json");

        int result = underTest.statusCode();

        assertEquals(201, result);
    }

    @Test
    public void workspaceResponse_isError_returnsFalse_forNonErrorStatus()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        boolean result = underTest.isError();

        assertFalse(result);
    }

    @Test
    public void workspaceResponse_isError_returnsTrue_forErrorStatus()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(500, "body", "application/json");

        boolean result = underTest.isError();

        assertTrue(result);
    }

    @Test
    public void workspaceResponse_constructor_normalizesNullValues()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, null, null);

        assertAll(
                () -> assertEquals(200, underTest.statusCode()),
                () -> assertEquals("", underTest.body()),
                () -> assertEquals("", underTest.contentType())
        );
    }

    private WorkspaceClient createUnderTest()
    {
        HttpClient httpClient = mock(HttpClient.class);
        Duration requestTimeout = mock(Duration.class);
        return new WorkspaceClient(httpClient, 1, requestTimeout);

    }
}


