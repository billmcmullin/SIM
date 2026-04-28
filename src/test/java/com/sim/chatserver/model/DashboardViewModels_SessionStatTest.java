package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.SessionStat;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for SessionStat
 *
 * @see com.sim.chatserver.model.DashboardViewModels.SessionStat
 * @author bmcmullin
 */
public class DashboardViewModels_SessionStatTest
{

    /**
     * Parasoft Jtest UTA: Test for getCount()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionStat#getCount()
     * @author bmcmullin
     */
    @Test
    public void testGetCount() throws Throwable
    {
        // Given
        String sessionId = "sessionId"; // UTA: default value
        int count = 1; // UTA: default value
        String lastEntry = "lastEntry"; // UTA: default value
        SessionStat underTest = new SessionStat(sessionId, count, lastEntry);

        // When
        int result = underTest.getCount();

        // Then - assertions for result of method getCount()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.SessionStat
        assertEquals("sessionId", underTest.getSessionId());

    }

    /**
     * Parasoft Jtest UTA: Test for getLastEntry()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionStat#getLastEntry()
     * @author bmcmullin
     */
    @Test
    public void testGetLastEntry() throws Throwable
    {
        // Given
        String sessionId = "sessionId"; // UTA: default value
        int count = 1; // UTA: default value
        String lastEntry = "lastEntry"; // UTA: default value
        SessionStat underTest = new SessionStat(sessionId, count, lastEntry);

        // When
        String result = underTest.getLastEntry();

        // Then - assertions for result of method getLastEntry()
        assertEquals("lastEntry", result);

        // Then - assertions for this instance of DashboardViewModels.SessionStat
        assertEquals("sessionId", underTest.getSessionId());

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionStat#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        String sessionId = "sessionId"; // UTA: default value
        int count = 1; // UTA: default value
        String lastEntry = "lastEntry"; // UTA: default value
        SessionStat underTest = new SessionStat(sessionId, count, lastEntry);

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertEquals("sessionId", result);

    }
}
