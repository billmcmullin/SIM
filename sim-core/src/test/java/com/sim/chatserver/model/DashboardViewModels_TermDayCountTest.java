package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.TermDayCount;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for TermDayCount
 *
 * @see com.sim.chatserver.model.DashboardViewModels.TermDayCount
 * @author bmcmullin
 */
public class DashboardViewModels_TermDayCountTest
{

    /**
     * Parasoft Jtest UTA: Test for getToday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermDayCount#getToday()
     * @author bmcmullin
     */
    @Test
    public void testGetToday() throws Throwable
    {
        // Given
        TermDayCount underTest = new TermDayCount();

        // When
        int result = underTest.getToday();

        // Then - assertions for result of method getToday()
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for getYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermDayCount#getYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetYesterday() throws Throwable
    {
        // Given
        TermDayCount underTest = new TermDayCount();

        // When
        int result = underTest.getYesterday();

        // Then - assertions for result of method getYesterday()
        assertEquals(0, result);

    }

    /**
     * Parasoft Jtest UTA: Test for incToday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermDayCount#incToday()
     * @author bmcmullin
     */
    @Test
    public void testIncToday() throws Throwable
    {
        // Given
        TermDayCount underTest = new TermDayCount();

        // When
        underTest.incToday();

    }

    /**
     * Parasoft Jtest UTA: Test for incYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermDayCount#incYesterday()
     * @author bmcmullin
     */
    @Test
    public void testIncYesterday() throws Throwable
    {
        // Given
        TermDayCount underTest = new TermDayCount();

        // When
        underTest.incYesterday();

    }
}
