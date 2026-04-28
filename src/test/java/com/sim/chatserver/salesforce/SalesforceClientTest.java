package com.sim.chatserver.salesforce;

import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for SalesforceClient
 *
 * @see com.sim.chatserver.salesforce.SalesforceClient
 * @author bmcmullin
 */
public class SalesforceClientTest
{

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = new SalesforceClient(httpClient);

        // When
        String friendlyName = null; // UTA: configured value
        SalesforceCustomerMatch result = underTest.findBestCustomerMatch(friendlyName);

        // Then - assertions for result of method findBestCustomerMatch(String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch2() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = new SalesforceClient(httpClient);

        // When
        String friendlyName = ""; // UTA: configured value
        SalesforceCustomerMatch result = underTest.findBestCustomerMatch(friendlyName);

        // Then - assertions for result of method findBestCustomerMatch(String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String, String, String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch3() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = new SalesforceClient(httpClient);

        // When
        String friendlyName = "*"; // UTA: configured value
        String instanceUrl = "instanceUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        SalesforceCustomerMatch result = underTest.findBestCustomerMatch(friendlyName, instanceUrl, apiKey);

    }

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String, String, String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch4() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = new SalesforceClient(httpClient);

        // When
        String friendlyName = "*"; // UTA: configured value
        String instanceUrl = ""; // UTA: configured value
        String apiKey = null; // UTA: configured value
        assertThrows(IllegalStateException.class, () -> {
            underTest.findBestCustomerMatch(friendlyName, instanceUrl, apiKey);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String, String, String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch5() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = new SalesforceClient(httpClient);

        // When
        String friendlyName = "*"; // UTA: configured value
        String instanceUrl = null; // UTA: configured value
        String apiKey = ""; // UTA: configured value
        assertThrows(IllegalStateException.class, () -> {
            underTest.findBestCustomerMatch(friendlyName, instanceUrl, apiKey);
        });

    }
}
