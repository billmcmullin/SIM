package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for WorkspaceResponse
 *
 * @see com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse
 * @author bmcmullin
 */
public class WorkspaceClient_WorkspaceResponseTest
{

    /**
     * Parasoft Jtest UTA: Test for body()
     *
     * @see com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse#body()
     * @author bmcmullin
     */
    @Test
    public void testBody() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String body = "body"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        WorkspaceResponse underTest = new WorkspaceResponse(statusCode, body, contentType);

        // When
        String result = underTest.body();

        // Then - assertions for result of method body()
        assertEquals("body", result);

        // Then - assertions for this instance of WorkspaceClient.WorkspaceResponse
        assertAll(() -> {
            assertEquals(1, underTest.statusCode());
        }, () -> {
            assertEquals("contentType", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for contentType()
     *
     * @see com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse#contentType()
     * @author bmcmullin
     */
    @Test
    public void testContentType() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String body = "body"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        WorkspaceResponse underTest = new WorkspaceResponse(statusCode, body, contentType);

        // When
        String result = underTest.contentType();

        // Then - assertions for result of method contentType()
        assertEquals("contentType", result);

        // Then - assertions for this instance of WorkspaceClient.WorkspaceResponse
        assertAll(() -> {
            assertEquals(1, underTest.statusCode());
        }, () -> {
            assertEquals("body", underTest.body());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isError()
     *
     * @see com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse#isError()
     * @author bmcmullin
     */
    @Test
    public void testIsError() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String body = "body"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        WorkspaceResponse underTest = new WorkspaceResponse(statusCode, body, contentType);

        // When
        boolean result = underTest.isError();

        // Then - assertions for result of method isError()
        assertFalse(result);

        // Then - assertions for this instance of WorkspaceClient.WorkspaceResponse
        assertAll(() -> {
            assertEquals(1, underTest.statusCode());
        }, () -> {
            assertEquals("body", underTest.body());
        }, () -> {
            assertEquals("contentType", underTest.contentType());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for statusCode()
     *
     * @see com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse#statusCode()
     * @author bmcmullin
     */
    @Test
    public void testStatusCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String body = "body"; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        WorkspaceResponse underTest = new WorkspaceResponse(statusCode, body, contentType);

        // When
        int result = underTest.statusCode();

        // Then - assertions for result of method statusCode()
        assertEquals(1, result);

        // Then - assertions for this instance of WorkspaceClient.WorkspaceResponse
        assertAll(() -> {
            assertEquals("body", underTest.body());
        }, () -> {
            assertEquals("contentType", underTest.contentType());
        });

    }
}
