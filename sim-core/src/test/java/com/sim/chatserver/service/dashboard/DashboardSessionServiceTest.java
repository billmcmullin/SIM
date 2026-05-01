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
        boolean isAfterResult = false; // UTA: configured value
        boolean isAfterResult2 = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult, isAfterResult2);
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
        LocalDate rangeStart = mock(LocalDate.class);
        boolean isAfterResult = false; // UTA: configured value
        boolean isAfterResult2 = true; // UTA: configured value
        when(rangeStart.isAfter(nullable(ChronoLocalDate.class))).thenReturn(isAfterResult, isAfterResult2);
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

    }
}
