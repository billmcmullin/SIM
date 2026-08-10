package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceApiKey()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceApiKey()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceApiKey() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceApiKey();

        // Then - assertions for result of method getSalesforceApiKey()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceClientId()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceClientId()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceClientId() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceClientId();

        // Then - assertions for result of method getSalesforceClientId()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceClientSecret()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceClientSecret()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceClientSecret() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceClientSecret();

        // Then - assertions for result of method getSalesforceClientSecret()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceInstanceUrl()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceInstanceUrl()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceInstanceUrl() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceInstanceUrl();

        // Then - assertions for result of method getSalesforceInstanceUrl()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceLoginUrl()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceLoginUrl()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceLoginUrl() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceLoginUrl();

        // Then - assertions for result of method getSalesforceLoginUrl()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceRefreshToken()
     *
     * @see com.sim.chatserver.config.ServerConfig#getSalesforceRefreshToken()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceRefreshToken() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String result = underTest.getSalesforceRefreshToken();

        // Then - assertions for result of method getSalesforceRefreshToken()
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
        }, () -> {
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceApiKey(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceApiKey(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceApiKey() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceApiKey = "salesforceApiKey"; // UTA: default value
        underTest.setSalesforceApiKey(salesforceApiKey);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertEquals("salesforceApiKey", underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceClientId(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceClientId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceClientId() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceClientId = "salesforceClientId"; // UTA: default value
        underTest.setSalesforceClientId(salesforceClientId);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertEquals("salesforceClientId", underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceClientSecret(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceClientSecret(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceClientSecret() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceClientSecret = "salesforceClientSecret"; // UTA: default value
        underTest.setSalesforceClientSecret(salesforceClientSecret);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertEquals("salesforceClientSecret", underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceInstanceUrl(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceInstanceUrl(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceInstanceUrl() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceInstanceUrl = "salesforceInstanceUrl"; // UTA: default value
        underTest.setSalesforceInstanceUrl(salesforceInstanceUrl);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertEquals("salesforceInstanceUrl", underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceLoginUrl(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceLoginUrl(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceLoginUrl() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceLoginUrl = "salesforceLoginUrl"; // UTA: default value
        underTest.setSalesforceLoginUrl(salesforceLoginUrl);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertEquals("salesforceLoginUrl", underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceRefreshToken(String)
     *
     * @see com.sim.chatserver.config.ServerConfig#setSalesforceRefreshToken(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceRefreshToken() throws Throwable
    {
        // Given
        ServerConfig underTest = new ServerConfig();

        // When
        String salesforceRefreshToken = "salesforceRefreshToken"; // UTA: default value
        underTest.setSalesforceRefreshToken(salesforceRefreshToken);

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
            assertNull(underTest.getWorkspaceName());
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertEquals("salesforceRefreshToken", underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
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
        }, () -> {
            assertNull(underTest.getSalesforceInstanceUrl());
        }, () -> {
            assertNull(underTest.getSalesforceApiKey());
        }, () -> {
            assertNull(underTest.getSalesforceLoginUrl());
        }, () -> {
            assertNull(underTest.getSalesforceClientId());
        }, () -> {
            assertNull(underTest.getSalesforceClientSecret());
        }, () -> {
            assertNull(underTest.getSalesforceRefreshToken());
        });

    }


    // Merged from ServerConfigSerializationGuardTest
    
    
        @Test
        void readObject_throwsNotSerializableException() throws Exception {
            ServerConfig value = new ServerConfig();
            Method readObject = ServerConfig.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
            readObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> readObject.invoke(value, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(ServerConfig.class.getName(), cause.getMessage());
        }
    
        @Test
        void writeObject_throwsNotSerializableException() throws Exception {
            ServerConfig value = new ServerConfig();
            Method writeObject = ServerConfig.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
            writeObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> writeObject.invoke(value, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(ServerConfig.class.getName(), cause.getMessage());
        }
}
