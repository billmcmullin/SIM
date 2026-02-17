package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.dto.DbConfig;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for ConfigStore
 *
 * @see com.sim.chatserver.service.ConfigStore
 * @author bmcmullin
 */
public class ConfigStoreTest
{

    /**
     * Parasoft Jtest UTA: Test for loadEncrypted()
     *
     * @see com.sim.chatserver.service.ConfigStore#loadEncrypted()
     * @author bmcmullin
     */
    @Test
    public void testLoadEncrypted() throws Throwable
    {
        // Given
        ConfigStore underTest = new ConfigStore();

        // When
        DbConfig result = underTest.loadEncrypted();

        // Then - assertions for result of method loadEncrypted()
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for saveEncrypted(DbConfig)
     *
     * @see com.sim.chatserver.service.ConfigStore#saveEncrypted(DbConfig)
     * @author bmcmullin
     */
    @Test
    public void testSaveEncrypted() throws Throwable
    {
        // Given
        ConfigStore underTest = new ConfigStore();

        // When
        DbConfig cfg = mock(DbConfig.class);
        boolean result = underTest.saveEncrypted(cfg);

        // Then - assertions for result of method saveEncrypted(DbConfig)
        assertFalse(result);

    }
}
