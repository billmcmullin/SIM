package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UpstreamRequestService.UpstreamResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
