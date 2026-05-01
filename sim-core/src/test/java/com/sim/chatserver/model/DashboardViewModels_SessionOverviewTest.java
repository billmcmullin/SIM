package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for SessionOverview
 *
 * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview
 * @author bmcmullin
 */
public class DashboardViewModels_SessionOverviewTest
{

    /**
     * Parasoft Jtest UTA: Test for getActiveDays()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getActiveDays()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveDays() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getActiveDays();

        // Then - assertions for result of method getActiveDays()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getActiveUsers()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getActiveUsers()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveUsers() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getActiveUsers();

        // Then - assertions for result of method getActiveUsers()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getActiveUsersProgression()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getActiveUsersProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveUsersProgression() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        ProgressStat result = underTest.getActiveUsersProgression();

        // Then - assertions for result of method getActiveUsersProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getActiveUsersYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getActiveUsersYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetActiveUsersYesterday() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getActiveUsersYesterday();

        // Then - assertions for result of method getActiveUsersYesterday()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getInactiveUsers()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getInactiveUsers()
     * @author bmcmullin
     */
    @Test
    public void testGetInactiveUsers() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getInactiveUsers();

        // Then - assertions for result of method getInactiveUsers()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getNewSessionsProgression()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getNewSessionsProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetNewSessionsProgression() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        ProgressStat result = underTest.getNewSessionsProgression();

        // Then - assertions for result of method getNewSessionsProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getNewSessionsToday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getNewSessionsToday()
     * @author bmcmullin
     */
    @Test
    public void testGetNewSessionsToday() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getNewSessionsToday();

        // Then - assertions for result of method getNewSessionsToday()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getNewSessionsYesterday()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getNewSessionsYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetNewSessionsYesterday() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getNewSessionsYesterday();

        // Then - assertions for result of method getNewSessionsYesterday()
        assertEquals(0, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTimeline()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getTimeline()
     * @author bmcmullin
     */
    @Test
    public void testGetTimeline() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        SessionTimeline result = underTest.getTimeline();

        // Then - assertions for result of method getTimeline()
        assertNotNull(result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTopSessions()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getTopSessions()
     * @author bmcmullin
     */
    @Test
    public void testGetTopSessions() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        List<SessionStat> result = underTest.getTopSessions();

        // Then - assertions for result of method getTopSessions()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getTotalUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTotalUsers()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionOverview#getTotalUsers()
     * @author bmcmullin
     */
    @Test
    public void testGetTotalUsers() throws Throwable
    {
        // Given
        List<SessionStat> topSessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        topSessions.add(item);
        SessionTimeline timeline = mock(SessionTimeline.class);
        int totalUsers = 1; // UTA: default value
        int activeUsers = 1; // UTA: default value
        int inactiveUsers = 1; // UTA: default value
        int activeDays = 1; // UTA: default value
        SessionOverview underTest = new SessionOverview(topSessions, timeline, totalUsers, activeUsers, inactiveUsers, activeDays);

        // When
        int result = underTest.getTotalUsers();

        // Then - assertions for result of method getTotalUsers()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardViewModels.SessionOverview
        assertAll(() -> {
            assertNotNull(underTest.getTopSessions());
            assertEquals(1, underTest.getTopSessions().size());
        }, () -> {
            assertNotNull(underTest.getTimeline());
        }, () -> {
            assertEquals(1, underTest.getActiveUsers());
        }, () -> {
            assertEquals(1, underTest.getInactiveUsers());
        }, () -> {
            assertEquals(1, underTest.getActiveDays());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsToday());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(underTest.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, underTest.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(underTest.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, underTest.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, underTest.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", underTest.getActiveUsersProgression().getDirection());
        });

    }
}
