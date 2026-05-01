package com.sim.chatserver.salesforce;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.salesforce.SalesforceClient.SalesforceClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for SalesforceClientException
 *
 * @see com.sim.chatserver.salesforce.SalesforceClient.SalesforceClientException
 * @author bmcmullin
 */
public class SalesforceClient_SalesforceClientExceptionTest
{

    /**
     * Parasoft Jtest UTA: Test for getStatusCode()
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient.SalesforceClientException#getStatusCode()
     * @author bmcmullin
     */
    @Test
    public void testGetStatusCode() throws Throwable
    {
        // Given
        int statusCode = 1; // UTA: default value
        String message = "message"; // UTA: default value
        SalesforceClientException underTest = new SalesforceClientException(statusCode, message);

        // When
        int result = underTest.getStatusCode();

        // Then - assertions for result of method getStatusCode()
        assertEquals(1, result);

    }
}
