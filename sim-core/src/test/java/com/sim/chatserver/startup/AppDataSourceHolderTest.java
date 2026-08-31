package com.sim.chatserver.startup;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.DbConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

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

    private static void invokeClose(AppDataSourceHolder underTest) {
        invokeVoidUnchecked(underTest, "close", new Class<?>[]{});
    }

    private static void invokeSetEmf(AppDataSourceHolder underTest, EntityManagerFactory emf) {
        invokeVoidUnchecked(underTest, "setEmf", new Class<?>[]{EntityManagerFactory.class}, emf);
    }

    private static String invokeGetActiveJdbcUrl(AppDataSourceHolder underTest) throws Throwable {
        return (String) invokeChecked(underTest, "getActiveJdbcUrl", new Class<?>[]{});
    }

    private static void invokeSwitchToExternalAndPersist(AppDataSourceHolder underTest, DbConfig cfg, Consumer<String> callback) {
        invokeVoidUnchecked(underTest, "switchToExternalAndPersist", new Class<?>[]{DbConfig.class, Consumer.class}, cfg, callback);
    }

    private static void invokeInit(AppDataSourceHolder underTest) {
        invokeVoidUnchecked(underTest, "init", new Class<?>[]{});
    }

    private static Object invokeChecked(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Throwable {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        }
    }

    private static void invokeVoidUnchecked(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            invokeChecked(target, methodName, parameterTypes, args);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

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
        invokeClose(underTest);

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
        invokeSetEmf(underTest, emf);

        // When
        invokeClose(underTest);

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
        invokeClose(underTest);

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
        invokeSetEmf(underTest, emf);
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

        // When
        invokeClose(underTest);

    }

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose5() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        doThrow(RuntimeException.class).when(emf).close();
        invokeSetEmf(underTest, emf);

        // When
        invokeClose(underTest);

    }

    /**
     * Parasoft Jtest UTA: Test for close()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#close()
     * @author bmcmullin
     */
    @Test
    public void testClose6() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        doThrow(RuntimeException.class).when(emf).close();
        invokeSetEmf(underTest, emf);
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

        // When
        invokeClose(underTest);

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
        String result = invokeGetActiveJdbcUrl(underTest);

    }

    /**
     * Parasoft Jtest UTA: Test for getActiveJdbcUrl()
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#getActiveJdbcUrl()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveJdbcUrl2() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        String result = invokeGetActiveJdbcUrl(underTest);

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
        invokeSetEmf(underTest, emf);

        // When
        EntityManagerFactory result = underTest.getEmf();

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
            invokeInit(underTest);
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
     * Parasoft Jtest UTA: Test for setDataSource(DataSource)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#setDataSource(DataSource)
     * @author bmcmullin
     */
    @Test
    public void testSetDataSource2() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

    }

    /**
     * Parasoft Jtest UTA: Test for setDataSource(DataSource)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#setDataSource(DataSource)
     * @author bmcmullin
     */
    @Test
    public void testSetDataSource3() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        DataSource dataSource = null; // UTA: configured value
        underTest.setDataSource(dataSource);

    }

    /**
     * Parasoft Jtest UTA: Test for setEmf(EntityManagerFactory)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#setEmf(EntityManagerFactory)
     * @author bmcmullin
     */
    @Test
    public void testSetEmf() throws Throwable
    {
        // Given
        AppDataSourceHolder underTest = new AppDataSourceHolder();

        // When
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        invokeSetEmf(underTest, emf);

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

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
        });

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
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
        });

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

        String getPortResult = null; // UTA: configured value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
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
        String getDbNameResult = "getDbNameResult"; // UTA: default value
        when(cfg.getDbName()).thenReturn(getDbNameResult);

        String getHostResult = null; // UTA: configured value
        when(cfg.getHost()).thenReturn(getHostResult);

        String getJdbcUrlResult = null; // UTA: configured value
        when(cfg.getJdbcUrl()).thenReturn(getJdbcUrlResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
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
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
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

        String getPasswordResult = null; // UTA: configured value
        when(cfg.getPassword()).thenReturn(getPasswordResult);

        String getPortResult = "getPortResult"; // UTA: default value
        when(cfg.getPort()).thenReturn(getPortResult);

        String getUsernameResult = null; // UTA: configured value
        when(cfg.getUsername()).thenReturn(getUsernameResult);
        Consumer<String> callback = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for switchToExternalAndPersist(DbConfig, Consumer)
     *
     * @see com.sim.chatserver.startup.AppDataSourceHolder#switchToExternalAndPersist(DbConfig, Consumer)
     * @author bmcmullin
     */
    @Test
    public void testSwitchToExternalAndPersist7() throws Throwable
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
            invokeSwitchToExternalAndPersist(underTest, cfg, callback);
        });

    }
}
