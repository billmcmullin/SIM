package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

class DashboardSessionAggregationQueryServiceCoreTest {

    @Test
    void collectAccumulators_returnsEmptyForNullOrEmptyWidgets() {
        DashboardSessionAggregationQueryService service = new DashboardSessionAggregationQueryService(Logger.getLogger("test"));
        assertTrue(service.collectAccumulators(null, null).isEmpty());
        assertTrue(service.collectAccumulators(List.of(), null).isEmpty());
    }

    @Test
    void collectAccumulators_mergesCountsAndTracksLatestTimestamp() throws Exception {
        DashboardSessionAggregationQueryService service = new DashboardSessionAggregationQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        Timestamp older = Timestamp.from(Instant.parse("2026-08-25T01:00:00Z"));
        Timestamp newer = Timestamp.from(Instant.parse("2026-08-26T01:00:00Z"));

        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getString("session_id")).thenReturn("s1", "s1", " ");
        when(rs.getInt("total")).thenReturn(2, 3, 99);
        when(rs.getTimestamp("last_entry")).thenReturn(older, newer, newer);

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("w1_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            Map<String, DashboardSessionAggregationQueryService.SessionAccumulatorData> out = service.collectAccumulators(
                    List.of(DashboardWidgetEntryTestFactory.newWidgetEntry(1, "w1", "W1", Instant.now())),
                    "sid");

            assertEquals(1, out.size());
            DashboardSessionAggregationQueryService.SessionAccumulatorData acc = out.get("s1");
            assertEquals(5, acc.count);
            assertEquals(Integer.valueOf(5), acc.widgetCounts.get("w1"));
            assertEquals(newer, acc.lastEntry);
            verify(ps).setString(1, "%sid%");
        }
    }

    @Test
    void collectAccumulators_continuesWhenSingleWidgetQueryFails() throws Exception {
        DashboardSessionAggregationQueryService service = new DashboardSessionAggregationQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);

        PreparedStatement okPs = mock(PreparedStatement.class);
        ResultSet okRs = mock(ResultSet.class);
        when(okPs.executeQuery()).thenReturn(okRs);
        when(okRs.next()).thenReturn(true, false);
        when(okRs.getString("session_id")).thenReturn("session-b");
        when(okRs.getInt("total")).thenReturn(4);
        when(okRs.getTimestamp("last_entry")).thenReturn(Timestamp.from(Instant.now()));

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("w1_table")) {
                throw new SQLException("fail first table");
            }
            return okPs;
        });

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("w1_table");
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w2")).thenReturn("w2_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            Map<String, DashboardSessionAggregationQueryService.SessionAccumulatorData> out = service.collectAccumulators(
                    List.of(
                            DashboardWidgetEntryTestFactory.newWidgetEntry(1, "w1", "W1", Instant.now()),
                            DashboardWidgetEntryTestFactory.newWidgetEntry(2, "w2", "W2", Instant.now())),
                    null);

            assertEquals(1, out.size());
            assertTrue(out.containsKey("session-b"));
            assertEquals(4, out.get("session-b").count);
        }
    }

    @Test
    void collectAccumulators_wrapsConnectionErrors() throws Exception {
        DashboardSessionAggregationQueryService service = new DashboardSessionAggregationQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            assertThrows(IllegalStateException.class,
                    () -> service.collectAccumulators(List.of(DashboardWidgetEntryTestFactory.newWidgetEntry(1, "w1", "W1", Instant.now())), null));
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