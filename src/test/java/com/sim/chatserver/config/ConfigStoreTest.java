package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for ConfigStore
 *
 * @see com.sim.chatserver.config.ConfigStore
 * @author bmcmullin
 */
public class ConfigStoreTest
{

    /**
     * Parasoft Jtest UTA: Test for setAppDataSourceHolder(AppDataSourceHolder)
     *
     * @see com.sim.chatserver.config.ConfigStore#setAppDataSourceHolder(AppDataSourceHolder)
     * @author bmcmullin
     */
    @Test
    public void testSetAppDataSourceHolder() throws Throwable
    {
        // When
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        ConfigStore.setAppDataSourceHolder(holder);

    }
}
