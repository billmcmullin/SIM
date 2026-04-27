package com.sim.chatserver.security;

import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.SalesforceAuthClient.AuthResult;

import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for SalesforceAuthClient
 *
 * @see com.sim.chatserver.security.SalesforceAuthClient
 * @author bmcmullin
 */
public class SalesforceAuthClientTest
{

    /**
     * Parasoft Jtest UTA: Test for refreshAccessToken()
     *
     * @see com.sim.chatserver.security.SalesforceAuthClient#refreshAccessToken()
     * @author bmcmullin
     */
    @Test
    public void testRefreshAccessToken() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceAuthClient underTest = new SalesforceAuthClient(httpClient);

        // When
        AuthResult result = underTest.refreshAccessToken();

    }
}
