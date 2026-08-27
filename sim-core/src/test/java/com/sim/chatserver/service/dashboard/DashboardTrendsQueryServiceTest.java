package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

class DashboardTrendsQueryServiceTest {

    @Test
    void loadTrendData_aggregatesDailyTotalsAndWidgetSeries() throws Exception {
        DashboardTrendsQueryService service = new DashboardTrendsQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        LocalDate start = LocalDate.of(2026, 8, 25);
        LocalDate end = LocalDate.of(2026, 8, 26);
        Timestamp day1 = Timestamp.valueOf(start.atStartOfDay().plusHours(1));
        Timestamp day2 = Timestamp.valueOf(end.atStartOfDay().plusHours(2));

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getTimestamp("created_at")).thenReturn(day1, day2);

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(
                    new WidgetEntry(1, "wid1", "", Instant.now()),
                    new WidgetEntry(2, " ", "ignored", Instant.now())));

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("wid1")).thenReturn("wid1_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            DashboardTrendsQueryService.TrendResult result = service.loadTrendData(start, end);

            assertEquals(Integer.valueOf(1), result.totalDaily.get(start));
            assertEquals(Integer.valueOf(1), result.totalDaily.get(end));
            assertTrue(result.widgetDaily.containsKey("wid1"));
            assertEquals("wid1", result.widgetNameToId.get("wid1"));
            assertEquals(Integer.valueOf(1), result.widgetDaily.get("wid1").get(start));
        }
    }

    @Test
    void loadTrendData_wrapsConnectionFailures() throws Exception {
        DashboardTrendsQueryService service = new DashboardTrendsQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            assertThrows(IllegalStateException.class,
                    () -> service.loadTrendData(LocalDate.now().minusDays(1), LocalDate.now()));
        }
    }

    @Test
    void collectSnapshotsForDay_filtersByWidgetIdWhenProvided() throws Exception {
        DashboardTrendsQueryService service = new DashboardTrendsQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1");
        when(rs.getString("prompt")).thenReturn("prompt");
        when(rs.getString("response_text")).thenReturn("response");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.now()));
        when(rs.getString("session_id")).thenReturn("session-1");

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(
                    new WidgetEntry(1, "wid1", "W1", Instant.now()),
                    new WidgetEntry(2, "wid2", "W2", Instant.now())));

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("wid2")).thenReturn("wid2_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            List<TermChatSnapshot> result = service.collectSnapshotsForDay(LocalDate.now(), "wid2");
            assertEquals(1, result.size());
            assertEquals("wid2", result.get(0).getWidgetId());
            assertEquals("chat-1", result.get(0).getChatId());
        }
    }

    @Test
    void collectSnapshotsForDay_wrapsSqlFailures() throws Exception {
        DashboardTrendsQueryService service = new DashboardTrendsQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            assertThrows(IllegalStateException.class,
                    () -> service.collectSnapshotsForDay(LocalDate.now(), ""));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static MockedStatic<CDI> mockCdi(AppDataSourceHolder holder) {
        MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class);
        CDI<Object> current = mock(CDI.class);
        Instance<AppDataSourceHolder> instance = mock(Instance.class);
        when(instance.get()).thenReturn(holder);
        when(current.select(AppDataSourceHolder.class)).thenReturn((Instance) instance);
        cdi.when(CDI::current).thenReturn(current);
        return cdi;
    }
}