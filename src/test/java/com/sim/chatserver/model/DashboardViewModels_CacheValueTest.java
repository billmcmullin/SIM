package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.CacheValue;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for CacheValue
 *
 * @see com.sim.chatserver.model.DashboardViewModels.CacheValue
 * @author bmcmullin
 */
public class DashboardViewModels_CacheValueTest
{

    /**
     * Parasoft Jtest UTA: Test for getExpiresAt()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.CacheValue#getExpiresAt()
     * @author bmcmullin
     */
    @Test
    public void testGetExpiresAt() throws Throwable
    {
        // Given
        Object value = new Object(); // UTA: default value
        long expiresAt = 1L; // UTA: default value
        CacheValue underTest = new CacheValue(value, expiresAt);

        // When
        long result = underTest.getExpiresAt();

        // Then - assertions for result of method getExpiresAt()
        assertEquals(1L, result);

        // Then - assertions for this instance of DashboardViewModels.CacheValue
        assertNotNull(underTest.getValue());

    }

    /**
     * Parasoft Jtest UTA: Test for getValue()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.CacheValue#getValue()
     * @author bmcmullin
     */
    @Test
    public void testGetValue() throws Throwable
    {
        // Given
        Object value = new Object(); // UTA: default value
        long expiresAt = 1L; // UTA: default value
        CacheValue underTest = new CacheValue(value, expiresAt);

        // When
        Object result = underTest.getValue();

        // Then - assertions for result of method getValue()
        assertNotNull(result);

        // Then - assertions for this instance of DashboardViewModels.CacheValue
        assertEquals(1L, underTest.getExpiresAt());

    }

    /**
     * Parasoft Jtest UTA: Test for isExpired(long)
     *
     * @see com.sim.chatserver.model.DashboardViewModels.CacheValue#isExpired(long)
     * @author bmcmullin
     */
    @Test
    public void testIsExpired() throws Throwable
    {
        // Given
        Object value = new Object(); // UTA: default value
        long expiresAt = 1L; // UTA: default value
        CacheValue underTest = new CacheValue(value, expiresAt);

        // When
        long now = 1L; // UTA: default value
        boolean result = underTest.isExpired(now);

        // Then - assertions for result of method isExpired(long)
        assertTrue(result);

        // Then - assertions for this instance of DashboardViewModels.CacheValue
        assertAll(() -> {
            assertNotNull(underTest.getValue());
        }, () -> {
            assertEquals(1L, underTest.getExpiresAt());
        });

    }
}
