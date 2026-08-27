package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

class WidgetTableSelectionQueryServiceTest {

    @Test
    void selectChatIds_returnsFilteredIds_andBuildsExpectedSql() throws Exception {
        WidgetTableSelectionQueryService service = new WidgetTableSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, true, true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1", " ", null, "chat-2");

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("widgetA")).thenReturn("widget_a");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            List<String> result = service.selectChatIds("widgetA", "p", "r", "g");

            assertEquals(List.of("chat-1", "chat-2"), result);
            verify(ps).setString(1, "%p%");
            verify(ps).setString(2, "%r%");
            verify(ps).setString(3, "%g%");
            verify(ps).setString(4, "%g%");
            verify(ps).setString(5, "%g%");

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(conn).prepareStatement(sql.capture());
            String built = sql.getValue();
            org.junit.jupiter.api.Assertions.assertTrue(built.contains("prompt ILIKE ?"));
            org.junit.jupiter.api.Assertions.assertTrue(built.contains("response_text ILIKE ?"));
            org.junit.jupiter.api.Assertions.assertTrue(built.contains("session_id ILIKE ?"));
        }
    }

    @Test
    void selectChatIds_throwsWhenWidgetTableMissing() throws Exception {
        WidgetTableSelectionQueryService service = new WidgetTableSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("widgetA")).thenReturn("widget_a");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(false);

            assertThrows(java.util.NoSuchElementException.class,
                    () -> service.selectChatIds("widgetA", null, null, null));
        }
    }

    @Test
    void selectChatIds_wrapsSqlExceptions() throws Exception {
        WidgetTableSelectionQueryService service = new WidgetTableSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("widgetA")).thenReturn("widget_a");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> service.selectChatIds("widgetA", null, null, null));
        }
    }

    @Test
    void selectChatIds_rejectsInvalidSanitizedIdentifier() {
        WidgetTableSelectionQueryService service = new WidgetTableSelectionQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        try {
            when(holder.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(conn);
        } catch (SQLException ex) {
            throw new AssertionError(ex);
        }

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("widgetA")).thenReturn("bad-name");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> service.selectChatIds("widgetA", null, null, null));
        }
    }

    @Test
    void selectChatIds_withoutFilters_bindsNoStringParams() throws Exception {
        WidgetTableSelectionQueryService service = new WidgetTableSelectionQueryService(Logger.getLogger("test"));

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
        when(rs.getString("widget_chat_id")).thenReturn("chat-only");

        try (MockedStatic<CDI> cdi = mockCdi(holder);
             MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class)) {

            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("widgetA")).thenReturn("widget_a");
            dbUtil.when(DashboardDbUtil::newRequestTableCache).thenReturn(new HashMap<>());
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any()))
                    .thenReturn(true);

            List<String> result = service.selectChatIds("widgetA", " ", null, " ");
            assertEquals(List.of("chat-only"), result);
            org.mockito.Mockito.verify(ps, org.mockito.Mockito.never()).setString(anyInt(), anyString());
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