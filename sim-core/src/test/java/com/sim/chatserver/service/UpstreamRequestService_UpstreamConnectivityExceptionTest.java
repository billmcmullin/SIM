package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for UpstreamConnectivityException
 *
 * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException
 * @author bmcmullin
 */
public class UpstreamRequestService_UpstreamConnectivityExceptionTest
{

    /**
     * Parasoft Jtest UTA: Test for code()
     *
     * @see com.sim.chatserver.service.UpstreamRequestService.UpstreamConnectivityException#code()
     * @author bmcmullin
     */
    @Test
    public void testCode() throws Throwable
    {
        // Given
        String code = "code"; // UTA: default value
        String message = "message"; // UTA: default value
        Throwable cause = mock(Throwable.class);
        UpstreamConnectivityException underTest = new UpstreamConnectivityException(code, message, cause);

        // When
        String result = underTest.code();

        // Then - assertions for result of method code()
        assertEquals("code", result);

    }
}
