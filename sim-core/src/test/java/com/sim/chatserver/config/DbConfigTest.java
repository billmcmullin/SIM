package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Parasoft Jtest UTA: Test class for DbConfig
 *
 * @see com.sim.chatserver.config.DbConfig
 * @author bmcmullin
 */
public class DbConfigTest
{

    /**
     * Parasoft Jtest UTA: Test for getDbName()
     *
     * @see com.sim.chatserver.config.DbConfig#getDbName()
     * @author bmcmullin
     */
    @Test
    public void testGetDbName() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getDbName();

        // Then - assertions for result of method getDbName()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getHost()
     *
     * @see com.sim.chatserver.config.DbConfig#getHost()
     * @author bmcmullin
     */
    @Test
    public void testGetHost() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getHost();

        // Then - assertions for result of method getHost()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getJdbcUrl()
     *
     * @see com.sim.chatserver.config.DbConfig#getJdbcUrl()
     * @author bmcmullin
     */
    @Test
    public void testGetJdbcUrl() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getJdbcUrl();

        // Then - assertions for result of method getJdbcUrl()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMaxPoolSize()
     *
     * @see com.sim.chatserver.config.DbConfig#getMaxPoolSize()
     * @author bmcmullin
     */
    @Test
    public void testGetMaxPoolSize() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        int result = underTest.getMaxPoolSize();

        // Then - assertions for result of method getMaxPoolSize()
        assertEquals(10, result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPassword()
     *
     * @see com.sim.chatserver.config.DbConfig#getPassword()
     * @author bmcmullin
     */
    @Test
    public void testGetPassword() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getPassword();

        // Then - assertions for result of method getPassword()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPort()
     *
     * @see com.sim.chatserver.config.DbConfig#getPort()
     * @author bmcmullin
     */
    @Test
    public void testGetPort() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getPort();

        // Then - assertions for result of method getPort()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsername()
     *
     * @see com.sim.chatserver.config.DbConfig#getUsername()
     * @author bmcmullin
     */
    @Test
    public void testGetUsername() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String result = underTest.getUsername();

        // Then - assertions for result of method getUsername()
        assertNull(result);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDbName(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setDbName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDbName() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String dbName = "dbName"; // UTA: default value
        underTest.setDbName(dbName);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertEquals("dbName", underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setHost(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setHost(String)
     * @author bmcmullin
     */
    @Test
    public void testSetHost() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String host = "host"; // UTA: default value
        underTest.setHost(host);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertEquals("host", underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setJdbcUrl(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setJdbcUrl(String)
     * @author bmcmullin
     */
    @Test
    public void testSetJdbcUrl() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String jdbcUrl = "jdbcUrl"; // UTA: default value
        underTest.setJdbcUrl(jdbcUrl);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertEquals("jdbcUrl", underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setMaxPoolSize(int)
     *
     * @see com.sim.chatserver.config.DbConfig#setMaxPoolSize(int)
     * @author bmcmullin
     */
    @Test
    public void testSetMaxPoolSize() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        int maxPoolSize = 1; // UTA: default value
        underTest.setMaxPoolSize(maxPoolSize);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(1, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPassword(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setPassword(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPassword() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String password = "password"; // UTA: default value
        underTest.setPassword(password);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertEquals("password", underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPort(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setPort(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPort() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String port = "port"; // UTA: default value
        underTest.setPort(port);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertEquals("port", underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setUsername(String)
     *
     * @see com.sim.chatserver.config.DbConfig#setUsername(String)
     * @author bmcmullin
     */
    @Test
    public void testSetUsername() throws Throwable
    {
        // Given
        DbConfig underTest = new DbConfig();

        // When
        String username = "username"; // UTA: default value
        underTest.setUsername(username);

        // Then - assertions for this instance of DbConfig
        assertAll(() -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getPort());
        }, () -> {
            assertNull(underTest.getDbName());
        }, () -> {
            assertNull(underTest.getJdbcUrl());
        }, () -> {
            assertEquals("username", underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPassword());
        }, () -> {
            assertEquals(10, underTest.getMaxPoolSize());
        });

    }
}
