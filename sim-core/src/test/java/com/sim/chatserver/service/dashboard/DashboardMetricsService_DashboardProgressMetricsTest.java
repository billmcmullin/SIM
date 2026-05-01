package com.sim.chatserver.service.dashboard;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for DashboardProgressMetrics
 *
 * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics
 * @author bmcmullin
 */
public class DashboardMetricsService_DashboardProgressMetricsTest
{

    /**
     * Parasoft Jtest UTA: Test for getChatsProgression()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getChatsProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsProgression() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        ProgressStat result = underTest.getChatsProgression();

        // Then - assertions for result of method getChatsProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(2, result.getDelta());
        }, () -> {
            assertEquals(-200.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", result.getDirection());
        });

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.getChatsToday());
        }, () -> {
            assertEquals(-1, underTest.getChatsYesterday());
        }, () -> {
            assertEquals(1, underTest.getTermsToday());
        }, () -> {
            assertEquals(-1, underTest.getTermsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsToday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getChatsToday()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsToday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.getChatsToday();

        // Then - assertions for result of method getChatsToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(-1, underTest.getChatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.getTermsToday());
        }, () -> {
            assertEquals(-1, underTest.getTermsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsYesterday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getChatsYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsYesterday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.getChatsYesterday();

        // Then - assertions for result of method getChatsYesterday()
        assertEquals(-1, result);

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.getChatsToday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.getTermsToday());
        }, () -> {
            assertEquals(-1, underTest.getTermsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsProgression()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getTermsProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsProgression() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        ProgressStat result = underTest.getTermsProgression();

        // Then - assertions for result of method getTermsProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(2, result.getDelta());
        }, () -> {
            assertEquals(-200.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", result.getDirection());
        });

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.getChatsToday());
        }, () -> {
            assertEquals(-1, underTest.getChatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.getTermsToday());
        }, () -> {
            assertEquals(-1, underTest.getTermsYesterday());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsToday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getTermsToday()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsToday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.getTermsToday();

        // Then - assertions for result of method getTermsToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.getChatsToday());
        }, () -> {
            assertEquals(-1, underTest.getChatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(-1, underTest.getTermsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsYesterday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics#getTermsYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsYesterday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = new DashboardProgressMetrics(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.getTermsYesterday();

        // Then - assertions for result of method getTermsYesterday()
        assertEquals(-1, result);

        // Then - assertions for this instance of DashboardMetricsService.DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.getChatsToday());
        }, () -> {
            assertEquals(-1, underTest.getChatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.getTermsToday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }
}
