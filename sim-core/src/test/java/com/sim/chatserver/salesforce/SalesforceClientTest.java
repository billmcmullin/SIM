package com.sim.chatserver.salesforce;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

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
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = null; // UTA: configured value
        SalesforceCustomerMatch result = underTest.findBestCustomerMatch(friendlyName);

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
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = ""; // UTA: configured value
        SalesforceCustomerMatch result = underTest.findBestCustomerMatch(friendlyName);

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
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = null; // UTA: configured value
        String instanceUrl = "instanceUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        SalesforceCustomerMatch result = invokeFindBestCustomerMatch(underTest, friendlyName, instanceUrl, apiKey);

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
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = ""; // UTA: configured value
        String instanceUrl = "instanceUrl"; // UTA: default value
        String apiKey = "apiKey"; // UTA: default value
        SalesforceCustomerMatch result = invokeFindBestCustomerMatch(underTest, friendlyName, instanceUrl, apiKey);

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
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = "*"; // UTA: configured value
        String instanceUrl = ""; // UTA: configured value
        String apiKey = null; // UTA: configured value
        assertThrows(SQLException.class, () -> {
            invokeFindBestCustomerMatch(underTest, friendlyName, instanceUrl, apiKey);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for findBestCustomerMatch(String, String, String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceClient#findBestCustomerMatch(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testFindBestCustomerMatch6() throws Throwable
    {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        SalesforceClient underTest = newSalesforceClient(httpClient);

        // When
        String friendlyName = "*"; // UTA: configured value
        String instanceUrl = null; // UTA: configured value
        String apiKey = ""; // UTA: configured value
        assertThrows(SQLException.class, () -> {
            invokeFindBestCustomerMatch(underTest, friendlyName, instanceUrl, apiKey);
        });

    }

    private static SalesforceCustomerMatch invokeFindBestCustomerMatch(
            SalesforceClient underTest,
            String friendlyName,
            String instanceUrl,
            String apiKey
    ) throws Exception {
        Method method = SalesforceClient.class.getDeclaredMethod(
                "findBestCustomerMatch",
                String.class,
                String.class,
                String.class
        );
        method.setAccessible(true);
        try {
            return (SalesforceCustomerMatch) method.invoke(underTest, friendlyName, instanceUrl, apiKey);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw ex;
        }
    }

    private static SalesforceClient newSalesforceClient(HttpClient httpClient) {
        try {
            Constructor<SalesforceClient> ctor = SalesforceClient.class.getDeclaredConstructor(HttpClient.class);
            ctor.setAccessible(true);
            return ctor.newInstance(httpClient);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate SalesforceClient for test", ex);
        }
    }
}
