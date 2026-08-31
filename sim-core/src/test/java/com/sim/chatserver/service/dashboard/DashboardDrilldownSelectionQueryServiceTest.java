package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
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

class DashboardDrilldownSelectionQueryServiceTest {

    @Test
    void collectDateEntries_returnsEmptyWhenWidgetsMissing() {
        DashboardDrilldownSelectionQueryService service = new DashboardDrilldownSelectionQueryService(Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());
            List<TermChatSnapshot> out = service.collectDateEntries(LocalDate.now());
            assertTrue(out.isEmpty());
        }
    }

    @Test
    void collectDateEntries_readsRowsAndSkipsMissingTables() throws Exception {
        DashboardDrilldownSelectionQueryService service = new DashboardDrilldownSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        Timestamp ts = Timestamp.from(Instant.now());
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("widget_chat_id")).thenReturn("c1", null);
        when(rs.getString("prompt")).thenReturn("p1", null);
        when(rs.getString("response_text")).thenReturn("r1", null);
        when(rs.getTimestamp("created_at")).thenReturn(ts, ts);
        when(rs.getString("session_id")).thenReturn("s1", null);

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            WidgetEntry w1 = newWidgetEntry(1, "w1", "W1", Instant.now());
            WidgetEntry w2 = newWidgetEntry(2, "w2", "W2", Instant.now());
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(w1, w2));

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("w1_table");
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w2")).thenReturn("w2_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(eq(conn), eq("w1_table"), any())).thenReturn(true);
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(eq(conn), eq("w2_table"), any())).thenReturn(false);

            List<TermChatSnapshot> out = service.collectDateEntries(LocalDate.of(2026, 8, 27));

            assertEquals(2, out.size());
            assertEquals("c1", out.get(0).getChatId());
            assertEquals("", out.get(1).getChatId());
            assertEquals("", out.get(1).getPrompt());
            assertEquals("", out.get(1).getSessionId());
        }
    }

    @Test
    void collectLatestChats_sortsDescendingAndAppliesLimit() throws Exception {
        DashboardDrilldownSelectionQueryService service = new DashboardDrilldownSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        Timestamp older = Timestamp.from(Instant.parse("2026-08-27T00:00:00Z"));
        Timestamp newer = Timestamp.from(Instant.parse("2026-08-27T10:00:00Z"));
        when(rs.next()).thenReturn(true, true, false, true, false);
        when(rs.getString("widget_chat_id")).thenReturn("c-old", "c-new", "c-second-widget");
        when(rs.getString("prompt")).thenReturn("p", "p", "p");
        when(rs.getString("response_text")).thenReturn("r", "r", "r");
        when(rs.getTimestamp("created_at")).thenReturn(older, newer, older);
        when(rs.getString("session_id")).thenReturn("s", "s", "s");

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(
                    newWidgetEntry(1, "w1", "W1", Instant.now()),
                    newWidgetEntry(2, "w2", "W2", Instant.now())));

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("w1_table");
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w2")).thenReturn("w2_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            List<TermChatSnapshot> out = service.collectLatestChats(2);

            assertEquals(2, out.size());
            assertEquals("c-new", out.get(0).getChatId());
            assertEquals("c-old", out.get(1).getChatId());
        }
    }

    @Test
    void collectSessionEntries_continuesAfterWidgetQueryFailure() throws Exception {
        DashboardDrilldownSelectionQueryService service = new DashboardDrilldownSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement okPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("w1_table")) {
                throw new SQLException("fail first");
            }
            return okPs;
        });

        when(okPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("c2");
        when(rs.getString("prompt")).thenReturn("p2");
        when(rs.getString("response_text")).thenReturn("r2");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.now()));

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(
                    newWidgetEntry(1, "w1", "W1", Instant.now()),
                    newWidgetEntry(2, "w2", "W2", Instant.now())));

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("w1_table");
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w2")).thenReturn("w2_table");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            List<TermChatSnapshot> out = service.collectSessionEntries("session-x");
            assertEquals(1, out.size());
            assertEquals("c2", out.get(0).getChatId());
        }
    }

    @Test
    void collectSessionEntries_returnsEmptyWhenConnectionFails() throws Exception {
        DashboardDrilldownSelectionQueryService service = new DashboardDrilldownSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(
                    newWidgetEntry(1, "w1", "W1", Instant.now())));

            List<TermChatSnapshot> out = service.collectSessionEntries("session-x");
            assertTrue(out.isEmpty());
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

    private static WidgetEntry newWidgetEntry(int id, String widgetId, String displayName, Instant createdAt) {
        try {
            Constructor<WidgetEntry> ctor = WidgetEntry.class.getDeclaredConstructor(int.class, String.class, String.class, Instant.class);
            ctor.setAccessible(true);
            return ctor.newInstance(Integer.valueOf(id), widgetId, displayName, createdAt);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate WidgetEntry for test", ex);
        }
    }
}