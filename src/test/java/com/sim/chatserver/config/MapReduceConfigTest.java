package com.sim.chatserver.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for MapReduceConfig
 *
 * @see com.sim.chatserver.config.MapReduceConfig
 * @author bmcmullin
 */
public class MapReduceConfigTest
{

    /**
     * Parasoft Jtest UTA: Test for getBatchSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getBatchSize()
     * @author bmcmullin
     */
    @Test
    public void testGetBatchSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getBatchSize();

        // Then - assertions for result of method getBatchSize()
        assertEquals(50, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMapContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMapContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetMapContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMapContextMaxChars();

        // Then - assertions for result of method getMapContextMaxChars()
        assertEquals(38000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMapMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMapMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetMapMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMapMessageMaxChars();

        // Then - assertions for result of method getMapMessageMaxChars()
        assertEquals(45000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMaxCoveragePasses()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMaxCoveragePasses()
     * @author bmcmullin
     */
    @Test
    public void testGetMaxCoveragePasses() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMaxCoveragePasses();

        // Then - assertions for result of method getMaxCoveragePasses()
        assertEquals(3, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMaxParallel()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMaxParallel()
     * @author bmcmullin
     */
    @Test
    public void testGetMaxParallel() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMaxParallel();

        // Then - assertions for result of method getMaxParallel()
        assertEquals(3, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMinBatchSize()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getMinBatchSize()
     * @author bmcmullin
     */
    @Test
    public void testGetMinBatchSize() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getMinBatchSize();

        // Then - assertions for result of method getMinBatchSize()
        assertEquals(1, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceContextMaxChars();

        // Then - assertions for result of method getReduceContextMaxChars()
        assertEquals(48000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getReduceMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getReduceMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetReduceMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getReduceMessageMaxChars();

        // Then - assertions for result of method getReduceMessageMaxChars()
        assertEquals(55000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRetryContextChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getRetryContextChars()
     * @author bmcmullin
     */
    @Test
    public void testGetRetryContextChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getRetryContextChars();

        // Then - assertions for result of method getRetryContextChars()
        assertEquals(18000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRetryMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getRetryMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetRetryMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getRetryMessageMaxChars();

        // Then - assertions for result of method getRetryMessageMaxChars()
        assertEquals(24000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassContextMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassContextMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassContextMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassContextMaxChars();

        // Then - assertions for result of method getSinglePassContextMaxChars()
        assertEquals(52000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassMaxSelected()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassMaxSelected()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassMaxSelected() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassMaxSelected();

        // Then - assertions for result of method getSinglePassMaxSelected()
        assertEquals(200, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSinglePassMessageMaxChars()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getSinglePassMessageMaxChars()
     * @author bmcmullin
     */
    @Test
    public void testGetSinglePassMessageMaxChars() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getSinglePassMessageMaxChars();

        // Then - assertions for result of method getSinglePassMessageMaxChars()
        assertEquals(60000, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWorkspaceMaxRetries()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getWorkspaceMaxRetries()
     * @author bmcmullin
     */
    @Test
    public void testGetWorkspaceMaxRetries() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        int result = underTest.getWorkspaceMaxRetries();

        // Then - assertions for result of method getWorkspaceMaxRetries()
        assertEquals(1, result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWorkspaceTimeout()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#getWorkspaceTimeout()
     * @author bmcmullin
     */
    @Test
    public void testGetWorkspaceTimeout() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        Duration result = underTest.getWorkspaceTimeout();

        // Then - assertions for result of method getWorkspaceTimeout()
        assertNotNull(result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isExhaustiveMode()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isExhaustiveMode()
     * @author bmcmullin
     */
    @Test
    public void testIsExhaustiveMode() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isExhaustiveMode();

        // Then - assertions for result of method isExhaustiveMode()
        assertTrue(result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRebatchOnContextLimit()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isRebatchOnContextLimit()
     * @author bmcmullin
     */
    @Test
    public void testIsRebatchOnContextLimit() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isRebatchOnContextLimit();

        // Then - assertions for result of method isRebatchOnContextLimit()
        assertTrue(result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isRetryReduceOnContextLimit()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#isRetryReduceOnContextLimit()
     * @author bmcmullin
     */
    @Test
    public void testIsRetryReduceOnContextLimit() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        boolean result = underTest.isRetryReduceOnContextLimit();

        // Then - assertions for result of method isRetryReduceOnContextLimit()
        assertTrue(result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.config.MapReduceConfig#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        MapReduceConfig underTest = MapReduceConfig.load();

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("MapReduceConfig{singlePassMaxSelected=200, batchSize=50, minBatchSize=1, maxParallel=3, maxCoveragePasses=3, exhaustiveMode=true, rebatchOnContextLimit=true, retryReduceOnContextLimit=true, singlePassMessageMaxChars=60000, singlePassContextMaxChars=52000, mapMessageMaxChars=45000, mapContextMaxChars=38000, reduceMessageMaxChars=55000, reduceContextMaxChars=48000, retryContextChars=18000, retryMessageMaxChars=24000, workspaceTimeout=PT1M30S, workspaceMaxRetries=1}", result);

        // Then - assertions for this instance of MapReduceConfig
        assertAll(() -> {
            assertEquals(200, underTest.getSinglePassMaxSelected());
        }, () -> {
            assertEquals(50, underTest.getBatchSize());
        }, () -> {
            assertEquals(1, underTest.getMinBatchSize());
        }, () -> {
            assertEquals(3, underTest.getMaxParallel());
        }, () -> {
            assertEquals(3, underTest.getMaxCoveragePasses());
        }, () -> {
            assertTrue(underTest.isExhaustiveMode());
        }, () -> {
            assertTrue(underTest.isRebatchOnContextLimit());
        }, () -> {
            assertTrue(underTest.isRetryReduceOnContextLimit());
        }, () -> {
            assertEquals(60000, underTest.getSinglePassMessageMaxChars());
        }, () -> {
            assertEquals(52000, underTest.getSinglePassContextMaxChars());
        }, () -> {
            assertEquals(45000, underTest.getMapMessageMaxChars());
        }, () -> {
            assertEquals(38000, underTest.getMapContextMaxChars());
        }, () -> {
            assertEquals(55000, underTest.getReduceMessageMaxChars());
        }, () -> {
            assertEquals(48000, underTest.getReduceContextMaxChars());
        }, () -> {
            assertEquals(18000, underTest.getRetryContextChars());
        }, () -> {
            assertEquals(24000, underTest.getRetryMessageMaxChars());
        }, () -> {
            assertNotNull(underTest.getWorkspaceTimeout());
        }, () -> {
            assertEquals(1, underTest.getWorkspaceMaxRetries());
        });

    }
}
