package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for CustomerProfileStore
 *
 * @see com.sim.chatserver.model.CustomerProfileStore
 * @author bmcmullin
 */
public class CustomerProfileStoreTest
{

    /**
     * Parasoft Jtest UTA: Test for setAppDataSourceHolder(AppDataSourceHolder)
     *
     * @see com.sim.chatserver.model.CustomerProfileStore#setAppDataSourceHolder(AppDataSourceHolder)
     * @author bmcmullin
     */
    @Test
    public void testSetAppDataSourceHolder() throws Throwable
    {
        // When
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        CustomerProfileStore.setAppDataSourceHolder(holder);

    }

}
