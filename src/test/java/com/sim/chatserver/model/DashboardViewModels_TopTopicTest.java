package com.sim.chatserver.model;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.TopTopic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for TopTopic
 *
 * @see com.sim.chatserver.model.DashboardViewModels.TopTopic
 * @author bmcmullin
 */
public class DashboardViewModels_TopTopicTest
{

    /**
     * Parasoft Jtest UTA: Test for getLabel()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TopTopic#getLabel()
     * @author bmcmullin
     */
    @Test
    public void testGetLabel() throws Throwable
    {
        // Given
        String label = "label"; // UTA: default value
        int today = 1; // UTA: default value
        int yesterday = 1; // UTA: default value
        TopTopic underTest = new TopTopic(label, today, yesterday);

        // When
        String result = underTest.getLabel();

        // Then - assertions for result of method getLabel()
        assertEquals("label", result);

        // Then - assertions for this instance of DashboardViewModels.TopTopic
        assertAll(() -> {
            assertEquals(1, underTest.getToday());
        }, () -> {
            assertEquals(1, underTest.getYesterday());
        }, () -> {
            assertEquals(2, underTest.getTotal());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getToday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TopTopic#getToday()
     * @author bmcmullin
     */
    @Test
    public void testGetToday() throws Throwable
    {
        // Given
        String label = "label"; // UTA: default value
        int today = 1; // UTA: default value
        int yesterday = 1; // UTA: default value
        TopTopic underTest = new TopTopic(label, today, yesterday);

        // When
        int result = underTest.getToday();

        // Then - assertions for result of method getToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.TopTopic
        assertAll(() -> {
            assertEquals("label", underTest.getLabel());
        }, () -> {
            assertEquals(1, underTest.getYesterday());
        }, () -> {
            assertEquals(2, underTest.getTotal());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotal()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TopTopic#getTotal()
     * @author bmcmullin
     */
    @Test
    public void testGetTotal() throws Throwable
    {
        // Given
        String label = "label"; // UTA: default value
        int today = 1; // UTA: default value
        int yesterday = 1; // UTA: default value
        TopTopic underTest = new TopTopic(label, today, yesterday);

        // When
        int result = underTest.getTotal();

        // Then - assertions for result of method getTotal()
        assertEquals(2, result);

        // Then - assertions for this instance of DashboardViewModels.TopTopic
        assertAll(() -> {
            assertEquals("label", underTest.getLabel());
        }, () -> {
            assertEquals(1, underTest.getToday());
        }, () -> {
            assertEquals(1, underTest.getYesterday());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TopTopic#getYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetYesterday() throws Throwable
    {
        // Given
        String label = "label"; // UTA: default value
        int today = 1; // UTA: default value
        int yesterday = 1; // UTA: default value
        TopTopic underTest = new TopTopic(label, today, yesterday);

        // When
        int result = underTest.getYesterday();

        // Then - assertions for result of method getYesterday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.TopTopic
        assertAll(() -> {
            assertEquals("label", underTest.getLabel());
        }, () -> {
            assertEquals(1, underTest.getToday());
        }, () -> {
            assertEquals(2, underTest.getTotal());
        });

    }
}
