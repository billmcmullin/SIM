package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for WidgetStat
 *
 * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat
 * @author bmcmullin
 */
public class DashboardViewModels_WidgetStatTest
{

    /**
     * Parasoft Jtest UTA: Test for getCount()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getCount()
     * @author bmcmullin
     */
    @Test
    public void testGetCount() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        int result = underTest.getCount();

        // Then - assertions for result of method getCount()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDelta()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getDelta()
     * @author bmcmullin
     */
    @Test
    public void testGetDelta() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        int result = underTest.getDelta();

        // Then - assertions for result of method getDelta()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDirection()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getDirection()
     * @author bmcmullin
     */
    @Test
    public void testGetDirection() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        String result = underTest.getDirection();

        // Then - assertions for result of method getDirection()
        assertEquals("flat", result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLabel()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getLabel()
     * @author bmcmullin
     */
    @Test
    public void testGetLabel() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        String result = underTest.getLabel();

        // Then - assertions for result of method getLabel()
        assertEquals("label", result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getProgression()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetProgression() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        ProgressStat result = underTest.getProgression();

        // Then - assertions for result of method getProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTodayCount()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getTodayCount()
     * @author bmcmullin
     */
    @Test
    public void testGetTodayCount() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        int result = underTest.getTodayCount();

        // Then - assertions for result of method getTodayCount()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertEquals(0, underTest.getYesterdayCount());

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetId()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getWidgetId()
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetId() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        String result = underTest.getWidgetId();

        // Then - assertions for result of method getWidgetId()
        assertEquals("widgetId", result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertAll(() -> {
            assertEquals(0, underTest.getTodayCount());
        }, () -> {
            assertEquals(0, underTest.getYesterdayCount());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getYesterdayCount()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.WidgetStat#getYesterdayCount()
     * @author bmcmullin
     */
    @Test
    public void testGetYesterdayCount() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String label = "label"; // UTA: default value
        int count = 1; // UTA: default value
        WidgetStat underTest = new WidgetStat(widgetId, label, count);

        // When
        int result = underTest.getYesterdayCount();

        // Then - assertions for result of method getYesterdayCount()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.WidgetStat
        assertEquals(0, underTest.getTodayCount());

    }
}
