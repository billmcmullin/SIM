package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Parasoft Jtest UTA: Test class for ServerConfig
 *
 * @see com.sim.chatserver.config.ServerConfig
 * @author bmcmullin
 */
public class ServerConfigTest
{

    /**
     * Parasoft Jtest UTA: Test for getApiKey()
     *
     * @see com.sim.chatserver.config.ServerConfig#getApiKey()
     * @author bmcmullin
     */
    @Test
    public void testGetApiKey() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getApiKey();

        // Then - assertions for result of method getApiKey()
        assertNull(result);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getConnectionInfo()
     *
     * @see com.sim.chatserver.config.ServerConfig#getConnectionInfo()
     * @author bmcmullin
     */
    @Test
    public void testGetConnectionInfo() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getConnectionInfo();

        // Then - assertions for result of method getConnectionInfo()
        assertNull(result);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getServerHost()
     *
     * @see com.sim.chatserver.config.ServerConfig#getServerHost()
     * @author bmcmullin
     */
    @Test
    public void testGetServerHost() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getServerHost();

        // Then - assertions for result of method getServerHost()
        assertNull(result);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getServerPort()
     *
     * @see com.sim.chatserver.config.ServerConfig#getServerPort()
     * @author bmcmullin
     */
    @Test
    public void testGetServerPort() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        int result = underTest.getServerPort();

        // Then - assertions for result of method getServerPort()
        assertEquals(0, result);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWorkspaceName()
     *
     * @see com.sim.chatserver.config.ServerConfig#getWorkspaceName()
     * @author bmcmullin
     */
    @Test
    public void testGetWorkspaceName() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getWorkspaceName();

        // Then - assertions for result of method getWorkspaceName()
        assertNull(result);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setApiKey(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setApiKey(String)
     * @author bmcmullin
     */
    @Test
    public void testSetApiKey() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String apiKey = "apiKey"; // UTA: default value
        underTest.setApiKey(apiKey);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertEquals("apiKey", underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setConnectionInfo(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setConnectionInfo(String)
     * @author bmcmullin
     */
    @Test
    public void testSetConnectionInfo() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String connectionInfo = "connectionInfo"; // UTA: default value
        underTest.setConnectionInfo(connectionInfo);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertEquals("connectionInfo", underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setServerHost(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setServerHost(String)
     * @author bmcmullin
     */
    @Test
    public void testSetServerHost() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String serverHost = "serverHost"; // UTA: default value
        underTest.setServerHost(serverHost);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertEquals("serverHost", underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setServerPort(int)
     *
     * @see com.sim.chatserver.config.ServerConfig#setServerPort(int)
     * @author bmcmullin
     */
    @Test
    public void testSetServerPort() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        int serverPort = 1; // UTA: default value
        underTest.setServerPort(serverPort);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(1, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setWorkspaceName(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setWorkspaceName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetWorkspaceName() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String workspaceName = "workspaceName"; // UTA: default value
        underTest.setWorkspaceName(workspaceName);

        // Then - assertions for this instance of ServerConfig
        assertAll(() -> {
            assertNull(underTest.getServerHost());
        }, () -> {
            assertEquals(0, underTest.getServerPort());
        }, () -> {
            assertNull(underTest.getConnectionInfo());
        }, () -> {
            assertNull(underTest.getApiKey());
        }, () -> {
            assertEquals("workspaceName", underTest.getWorkspaceName());
        });

    }

}
