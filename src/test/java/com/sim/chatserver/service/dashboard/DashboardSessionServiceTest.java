package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.widget.WidgetEntry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardSessionService
 *
 * @see com.sim.chatserver.service.dashboard.DashboardSessionService
 * @author bmcmullin
 */
public class DashboardSessionServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for buildEmptySessionPayload(LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildEmptySessionPayload(LocalDate, LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildEmptySessionPayload() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        String result = underTest.buildEmptySessionPayload(rangeStart, rangeEnd);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionChartPayload(SessionOverview, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionChartPayload(SessionOverview,
     *      LocalDate, LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionChartPayload() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        SessionOverview overview = mock(SessionOverview.class);
        SessionTimeline getTimelineResult = mock(SessionTimeline.class);
        List<String> getLabelsResult = new ArrayList<String>(); // UTA: default value
        doReturn(getLabelsResult).when(getTimelineResult).getLabels();
        when(overview.getTimeline()).thenReturn(getTimelineResult);

        List<SessionStat> getTopSessionsResult = new ArrayList<SessionStat>(); // UTA: default value
        doReturn(getTopSessionsResult).when(overview).getTopSessions();
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        String result = underTest.buildSessionChartPayload(overview, rangeStart, rangeEnd);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionChartPayload(SessionOverview, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionChartPayload(SessionOverview,
     *      LocalDate, LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionChartPayload2() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        SessionOverview overview = mock(SessionOverview.class);
        SessionTimeline getTimelineResult = mock(SessionTimeline.class);
        List<String> getLabelsResult = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        getLabelsResult.add(item);
        doReturn(getLabelsResult).when(getTimelineResult).getLabels();
        when(overview.getTimeline()).thenReturn(getTimelineResult);

        List<SessionStat> getTopSessionsResult = new ArrayList<SessionStat>(); // UTA: default value
        doReturn(getTopSessionsResult).when(overview).getTopSessions();
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        String result = underTest.buildSessionChartPayload(overview, rangeStart, rangeEnd);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionChartPayload(SessionOverview, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionChartPayload(SessionOverview,
     *      LocalDate, LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionChartPayload3() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        SessionOverview overview = mock(SessionOverview.class);
        SessionTimeline getTimelineResult = mock(SessionTimeline.class);
        Map<String, List<Integer>> getCountsBySessionResult = new HashMap<String, List<Integer>>(); // UTA: default value
        String key = "key"; // UTA: default value
        List<Integer> value = new ArrayList<Integer>(); // UTA: default value
        getCountsBySessionResult.put(key, value);
        doReturn(getCountsBySessionResult).when(getTimelineResult).getCountsBySession();

        List<String> getLabelsResult = new ArrayList<String>(); // UTA: default value
        doReturn(getLabelsResult).when(getTimelineResult).getLabels();
        when(overview.getTimeline()).thenReturn(getTimelineResult);

        List<SessionStat> getTopSessionsResult = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        getTopSessionsResult.add(item);
        doReturn(getTopSessionsResult).when(overview).getTopSessions();
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        String result = underTest.buildSessionChartPayload(overview, rangeStart, rangeEnd);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionChartPayload(SessionOverview, LocalDate, LocalDate)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionChartPayload(SessionOverview,
     *      LocalDate, LocalDate)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionChartPayload4() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        SessionOverview overview = mock(SessionOverview.class);
        SessionTimeline getTimelineResult = mock(SessionTimeline.class);
        List<String> getLabelsResult = new ArrayList<String>(); // UTA: default value
        doReturn(getLabelsResult).when(getTimelineResult).getLabels();
        SessionTimeline getTimelineResult2 = mock(SessionTimeline.class);
        Map<String, List<Integer>> getCountsBySessionResult = new HashMap<String, List<Integer>>(); // UTA: default value
        String key = "key"; // UTA: default value
        List<Integer> value = new ArrayList<Integer>(); // UTA: default value
        Integer item = 1; // UTA: default value
        value.add(item);
        getCountsBySessionResult.put(key, value);
        doReturn(getCountsBySessionResult).when(getTimelineResult2).getCountsBySession();
        SessionTimeline getTimelineResult3 = mock(SessionTimeline.class);
        List<String> getLabelsResult2 = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        getLabelsResult2.add(item2);
        doReturn(getLabelsResult2).when(getTimelineResult3).getLabels();
        SessionTimeline getTimelineResult4 = mock(SessionTimeline.class);
        List<String> getLabelsResult3 = new ArrayList<String>(); // UTA: default value
        String item3 = "item3"; // UTA: default value
        getLabelsResult3.add(item3);
        doReturn(getLabelsResult3).when(getTimelineResult4).getLabels();
        when(overview.getTimeline()).thenReturn(getTimelineResult, getTimelineResult2, getTimelineResult3, getTimelineResult4);

        List<SessionStat> getTopSessionsResult = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item4 = mock(SessionStat.class);
        getTopSessionsResult.add(item4);
        doReturn(getTopSessionsResult).when(overview).getTopSessions();
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        String result = underTest.buildSessionChartPayload(overview, rangeStart, rangeEnd);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionOverview(Connection, List,
     *      LocalDate, LocalDate, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionOverview() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = null; // UTA: configured value
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult);
        LocalDate rangeEnd = mock(LocalDate.class);
        int activeDays = 1; // UTA: default value
        SessionOverview result = underTest.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionOverview(Connection, List,
     *      LocalDate, LocalDate, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionOverview2() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult);
        LocalDate rangeEnd = mock(LocalDate.class);
        int activeDays = 1; // UTA: default value
        SessionOverview result = underTest.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionOverview(Connection, List,
     *      LocalDate, LocalDate, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionOverview3() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = null; // UTA: configured value
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = false; // UTA: configured value
        boolean isAfterResult2 = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult, isAfterResult2);
        LocalDate rangeEnd = mock(LocalDate.class);
        int activeDays = 1; // UTA: default value
        SessionOverview result = underTest.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);

        // Then - assertions for result of method buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertNotNull(result.getTopSessions());
            assertEquals(0, result.getTopSessions().size());
        }, () -> {
            assertNotNull(result.getTimeline());
        }, () -> {
            assertNotNull(result.getTimeline().getLabels());
            assertEquals(0, result.getTimeline().getLabels().size());
        }, () -> {
            assertNotNull(result.getTimeline().getCountsBySession());
            assertEquals(0, result.getTimeline().getCountsBySession().size());
        }, () -> {
            assertEquals(0, result.getTotalUsers());
        }, () -> {
            assertEquals(0, result.getActiveUsers());
        }, () -> {
            assertEquals(0, result.getInactiveUsers());
        }, () -> {
            assertEquals(1, result.getActiveDays());
        }, () -> {
            assertEquals(0, result.getNewSessionsToday());
        }, () -> {
            assertEquals(0, result.getNewSessionsYesterday());
        }, () -> {
            assertNotNull(result.getNewSessionsProgression());
        }, () -> {
            assertEquals(0, result.getNewSessionsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getNewSessionsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getNewSessionsProgression().getDirection());
        }, () -> {
            assertEquals(0, result.getActiveUsersYesterday());
        }, () -> {
            assertNotNull(result.getActiveUsersProgression());
        }, () -> {
            assertEquals(0, result.getActiveUsersProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getActiveUsersProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getActiveUsersProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionOverview(Connection, List,
     *      LocalDate, LocalDate, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionOverview4() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = null; // UTA: configured value
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = false; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult);

        LocalDate plusDaysResult = mock(LocalDate.class);
        boolean isAfterResult2 = true; // UTA: configured value
        when(plusDaysResult.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult2);
        when(rangeStart.plusDays(anyLong())).thenReturn(plusDaysResult);
        LocalDate rangeEnd = mock(LocalDate.class);
        int activeDays = 1; // UTA: default value
        SessionOverview result = underTest.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionOverview(Connection, List, LocalDate, LocalDate, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionOverview(Connection, List,
     *      LocalDate, LocalDate, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionOverview5() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        LocalDate rangeStart = mock(LocalDate.class);
        LocalDate rangeEnd = mock(LocalDate.class);
        int activeDays = 1; // UTA: default value
        SessionOverview result = underTest.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);

    }

    /**
     * Parasoft Jtest UTA: Test for buildSessionTimeline(Connection, List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#buildSessionTimeline(Connection, List)
     * @author bmcmullin
     */
    @Test
    public void testBuildSessionTimeline() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Connection conn = mock(Connection.class);
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<String> sessionIds = new ArrayList<String>(); // UTA: default value
        String item2 = "item2"; // UTA: default value
        sessionIds.add(item2);
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult);
        LocalDate rangeEnd = mock(LocalDate.class);
        Map<String, Boolean> tableExistsCache = new HashMap<String, Boolean>(); // UTA: default value
        String key = "key"; // UTA: default value
        Boolean value = false; // UTA: default value
        tableExistsCache.put(key, value);
        SessionTimeline result = underTest.buildSessionTimeline(conn, widgets, sessionIds, rangeStart, rangeEnd, tableExistsCache);

    }

    /**
     * Parasoft Jtest UTA: Test for formatTimestamp(Timestamp)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#formatTimestamp(Timestamp)
     * @author bmcmullin
     */
    @Test
    public void testFormatTimestamp() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Timestamp ts = null; // UTA: configured value
        String result = underTest.formatTimestamp(ts);

        // Then - assertions for result of method formatTimestamp(Timestamp)
        assertEquals("—", result);

    }

    /**
     * Parasoft Jtest UTA: Test for formatTimestamp(Timestamp)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardSessionService#formatTimestamp(Timestamp)
     * @author bmcmullin
     */
    @Test
    public void testFormatTimestamp2() throws Throwable
    {
        // Given
        DashboardSessionService underTest = new DashboardSessionService();

        // When
        Timestamp ts = mock(Timestamp.class);
        Instant toInstantResult = mock(Instant.class);
        ZonedDateTime atZoneResult = mock(ZonedDateTime.class);
        when(toInstantResult.atZone(nullable(ZoneId.class))).thenReturn(atZoneResult);
        when(ts.toInstant()).thenReturn(toInstantResult);
        String result = underTest.formatTimestamp(ts);

        // Then - assertions for result of method formatTimestamp(Timestamp)
        assertNull(result);

    }
}
