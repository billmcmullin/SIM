package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;
/**
 * Parasoft Jtest UTA: Test class for DataSourceFactory
 *
 * @see com.sim.chatserver.config.DataSourceFactory
 * @author bmcmullin
 */
public class DataSourceFactoryTest
{

    /**
     * Parasoft Jtest UTA: Test for createFromEnv()
     *
     * @see com.sim.chatserver.config.DataSourceFactory#createFromEnv()
     * @author bmcmullin
     */
    @Test
    public void testCreateFromEnv() throws Throwable
    {
        // When
        HikariDataSource result = DataSourceFactory.createFromEnv();

    }
}
