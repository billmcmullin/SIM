package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for UpstreamResponse
 *
 * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse
 * @author bmcmullin
 */
public class UpstreamRequestService_UpstreamResponseTest
{

    /**
     * Parasoft Jtest UTA: Test for body()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#body()
     * @author bmcmullin
     */
    @Test
    public void testBody() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.body();

        // Then - assertions for result of method body()
        assertEquals("body", result);

    }

    /**
     * Parasoft Jtest UTA: Test for contentType()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#contentType()
     * @author bmcmullin
     */
    @Test
    public void testContentType() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.contentType();

        // Then - assertions for result of method contentType()
        assertEquals("contentType", result);

    }

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        Object arg0 = new Object(); // UTA: default value
        boolean result = underTest.equals(arg0);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for hashCode()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#hashCode()
     * @author bmcmullin
     */
    @Test
    public void testHashCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        int result = underTest.hashCode();

        // Then - assertions for result of method hashCode()
        // assertEquals(1, result);// UTA: Expected value may be unstable

    }

    /**
     * Parasoft Jtest UTA: Test for statusCode()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#statusCode()
     * @author bmcmullin
     */
    @Test
    public void testStatusCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        int result = underTest.statusCode();

        // Then - assertions for result of method statusCode()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String contentType = "contentType"; // UTA: default value
        String body = "body"; // UTA: default value
        UpstreamResponse underTest = new UpstreamResponse(statusCode, contentType, body);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("UpstreamResponse[statusCode=1, contentType=contentType, body=body]", result);

    }
}
