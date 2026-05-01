package com.sim.chatserver.model;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.SessionAccumulator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for SessionAccumulator
 *
 * @see com.sim.chatserver.model.DashboardViewModels.SessionAccumulator
 * @author bmcmullin
 */
public class DashboardViewModels_SessionAccumulatorTest
{

    /**
     * Parasoft Jtest UTA: Test for addCount(int)
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionAccumulator#addCount(int)
     * @author bmcmullin
     */
    @Test
    public void testAddCount() throws Throwable
    {
        // Given
        SessionAccumulator underTest = new SessionAccumulator();

        // When
        int delta = 1; // UTA: default value
        underTest.addCount(delta);

        // Then - assertions for this instance of DashboardViewModels.SessionAccumulator
        assertAll(() -> {
            assertEquals(1, underTest.getCount());
        }, () -> {
            assertNull(underTest.getLastEntry());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCount()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionAccumulator#getCount()
     * @author bmcmullin
     */
    @Test
    public void testGetCount() throws Throwable
    {
        // Given
        SessionAccumulator underTest = new SessionAccumulator();

        // When
        int result = underTest.getCount();

        // Then - assertions for result of method getCount()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.SessionAccumulator
        assertNull(underTest.getLastEntry());

    }

    /**
     * Parasoft Jtest UTA: Test for getLastEntry()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionAccumulator#getLastEntry()
     * @author bmcmullin
     */
    @Test
    public void testGetLastEntry() throws Throwable
    {
        // Given
        SessionAccumulator underTest = new SessionAccumulator();

        // When
        Timestamp result = underTest.getLastEntry();

        // Then - assertions for result of method getLastEntry()
        assertNull(result);

        // Then - assertions for this instance of DashboardViewModels.SessionAccumulator
        assertEquals(0, underTest.getCount());

    }

    /**
     * Parasoft Jtest UTA: Test for setLastEntry(Timestamp)
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionAccumulator#setLastEntry(Timestamp)
     * @author bmcmullin
     */
    @Test
    public void testSetLastEntry() throws Throwable
    {
        // Given
        SessionAccumulator underTest = new SessionAccumulator();

        // When
        Timestamp lastEntry = mock(Timestamp.class);
        underTest.setLastEntry(lastEntry);

        // Then - assertions for this instance of DashboardViewModels.SessionAccumulator
        assertAll(() -> {
            assertEquals(0, underTest.getCount());
        }, () -> {
            assertNotNull(underTest.getLastEntry());
        });

    }
}
