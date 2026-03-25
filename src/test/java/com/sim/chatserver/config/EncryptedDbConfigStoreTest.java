package com.sim.chatserver.config;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.sim.chatserver.startup.AppDataSourceHolder;
/**
 * Parasoft Jtest UTA: Test class for ConfigStore
 *
 * @see com.sim.chatserver.config.ConfigStore
 * @author bmcmullin
 */
public class EncryptedDbConfigStoreTest
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
        EncryptedDbConfigStore.setAppDataSourceHolder(holder);

    }

}
