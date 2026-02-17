package com.sim.chatserver.startup;

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
        underTest.setEmf(emf);
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
    public void testClose5() throws Throwable
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
        underTest.setEmf(emf);
        DataSource dataSource = mock(DataSource.class);
        underTest.setDataSource(dataSource);

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
        String result = underTest.getActiveJdbcUrl();

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
        underTest.init();

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
        underTest.setEmf(emf);

    }

}
