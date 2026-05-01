package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for ProgressStat
 *
 * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat
 * @author bmcmullin
 */
public class DashboardViewModels_ProgressStatTest
{

    /**
     * Parasoft Jtest UTA: Test for getDelta()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat#getDelta()
     * @author bmcmullin
     */
    @Test
    public void testGetDelta() throws Throwable
    {
        // Given
        int today = 1; // UTA: configured value
        int yesterday = -1; // UTA: configured value
        ProgressStat underTest = new ProgressStat(today, yesterday);

        // When
        int result = underTest.getDelta();

        // Then - assertions for result of method getDelta()
        assertEquals(2, result);

        // Then - assertions for this instance of DashboardViewModels.ProgressStat
        assertAll(() -> {
            assertEquals(-200.0d, underTest.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDirection()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat#getDirection()
     * @author bmcmullin
     */
    @Test
    public void testGetDirection() throws Throwable
    {
        // Given
        int today = 1; // UTA: configured value
        int yesterday = -1; // UTA: configured value
        ProgressStat underTest = new ProgressStat(today, yesterday);

        // When
        String result = underTest.getDirection();

        // Then - assertions for result of method getDirection()
        assertEquals("up", result);

        // Then - assertions for this instance of DashboardViewModels.ProgressStat
        assertAll(() -> {
            assertEquals(2, underTest.getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getPctDelta(), 0.0);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPctDelta()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat#getPctDelta()
     * @author bmcmullin
     */
    @Test
    public void testGetPctDelta() throws Throwable
    {
        // Given
        int today = 1; // UTA: configured value
        int yesterday = -1; // UTA: configured value
        ProgressStat underTest = new ProgressStat(today, yesterday);

        // When
        double result = underTest.getPctDelta();

        // Then - assertions for result of method getPctDelta()
        assertEquals(-200.0d, result, 0.0);

        // Then - assertions for this instance of DashboardViewModels.ProgressStat
        assertAll(() -> {
            assertEquals(2, underTest.getDelta());
        }, () -> {
            assertEquals("up", underTest.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getToday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat#getToday()
     * @author bmcmullin
     */
    @Test
    public void testGetToday() throws Throwable
    {
        // Given
        int today = 1; // UTA: configured value
        int yesterday = -1; // UTA: configured value
        ProgressStat underTest = new ProgressStat(today, yesterday);

        // When
        int result = underTest.getToday();

        // Then - assertions for result of method getToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.ProgressStat
        assertAll(() -> {
            assertEquals(2, underTest.getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.ProgressStat#getYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetYesterday() throws Throwable
    {
        // Given
        int today = 1; // UTA: configured value
        int yesterday = -1; // UTA: configured value
        ProgressStat underTest = new ProgressStat(today, yesterday);

        // When
        int result = underTest.getYesterday();

        // Then - assertions for result of method getYesterday()
        assertEquals(-1, result);

        // Then - assertions for this instance of DashboardViewModels.ProgressStat
        assertAll(() -> {
            assertEquals(2, underTest.getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getDirection());
        });

    }
}
