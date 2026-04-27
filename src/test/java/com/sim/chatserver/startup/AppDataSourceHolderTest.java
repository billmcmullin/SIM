package com.sim.chatserver.startup;

import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.DbConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AppDataSourceHolder
 *
 * @see com.sim.chatserver.startup.AppDataSourceHolder
 * @author bmcmullin
 */
public class AppDataSourceHolderTest
{

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        underTest.close();

    }

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose2() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        underTest.setEmf(emf);

        // When
        underTest.close();

    }

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose3() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

        // When
        underTest.close();

    }

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose4() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        doThrow(RuntimeException.class).when(emf).close();
        underTest.setEmf(emf);

        // When
        underTest.close();

    }

    /**
     * Parasoft Jtest UTA: Test for getActiveJdbcUrl()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#getActiveJdbcUrl()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveJdbcUrl() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

        // When
        String result = underTest.getActiveJdbcUrl();

        // Then - assertions for result of method getActiveJdbcUrl()
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getDataSource()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#getDataSource()
     * @author bmcmullin
     */
    @Test
    public void testGetDataSource() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        assertThrows(IllegalStateException.class, () -> {
            underTest.getDataSource();
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmf()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#getEmf()
     * @author bmcmullin
     */
    @Test
    public void testGetEmf() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        underTest.setEmf(emf);

        // When
        EntityManagerFactory result = underTest.getEmf();

        // Then - assertions for result of method getEmf()
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getEmf()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#getEmf()
     * @author bmcmullin
     */
    @Test
    public void testGetEmf2() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        assertThrows(IllegalStateException.class, () -> {
            underTest.getEmf();
        });

    }

    /**
     * Parasoft Jtest UTA: Test for init()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#init()
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        assertThrows(IllegalStateException.class, () -> {
            underTest.init();
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDataSource(DataSource)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#setDataSource(DataSource)
     * @author bmcmullin
     */
    @Test
    public void testSetDataSource() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        HikariDataSource dataSource = mock(HikariDataSource.class);
        underTest.setDataSource(dataSource);

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getDbNameResult = "getDbNameResult"; // UTA: default value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = "getHostResult"; // UTA: default value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        int getMaxPoolSizeResult = 0; // UTA: configured value
        when(cfg.getMaxPoolSize()).thenReturn(getMaxPoolSizeResult);

        String getPasswordResult = null; // UTA: configured value
        when(cfg.getPassword()).thenReturn(getPasswordResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);

        String getUsernameResult = "getUsernameResult"; // UTA: default value
        when(cfg.getUsername()).thenReturn(getUsernameResult);
        Consumer<String> callback = mock(Consumer.class);
        underTest.switchToExternalAndPersist(cfg, callback);

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist2() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getHostResult = null; // UTA: configured value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);
        Consumer<String> callback = mock(Consumer.class);
        underTest.switchToExternalAndPersist(cfg, callback);

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist3() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getDbNameResult = "getDbNameResult"; // UTA: default value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = "getHostResult"; // UTA: default value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            underTest.switchToExternalAndPersist(cfg, callback);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist4() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getDbNameResult = null; // UTA: configured value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = "getHostResult"; // UTA: default value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            underTest.switchToExternalAndPersist(cfg, callback);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist5() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getDbNameResult = "getDbNameResult"; // UTA: default value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = "getHostResult"; // UTA: default value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = "getJdbcUrlResult"; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            underTest.switchToExternalAndPersist(cfg, callback);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist6() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DbConfig cfg = mock(DbConfig.class);
        String getDbNameResult = "getDbNameResult"; // UTA: default value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = "getHostResult"; // UTA: default value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        int getMaxPoolSizeResult = 0; // UTA: configured value
        when(cfg.getMaxPoolSize()).thenReturn(getMaxPoolSizeResult);

        String getPasswordResult = "getPasswordResult"; // UTA: default value
        when(cfg.getPassword()).thenReturn(getPasswordResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);

        String getUsernameResult = null; // UTA: configured value
        when(cfg.getUsername()).thenReturn(getUsernameResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            underTest.switchToExternalAndPersist(cfg, callback);
        });

    }
}
