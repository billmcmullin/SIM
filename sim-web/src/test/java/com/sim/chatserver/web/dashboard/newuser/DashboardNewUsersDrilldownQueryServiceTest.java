package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
 class DashboardNewUsersDrilldownQueryServiceTest {

    @Test
    void findEarliestBySession_happyPath_choosesEarliestPerSession() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getString("session_id")).thenReturn("s1", " s1 ", " ");
        when(rs.getTimestamp("first_seen")).thenReturn(
                Timestamp.from(Instant.parse("2026-08-03T10:00:00Z")),
                Timestamp.from(Instant.parse("2026-08-03T09:00:00Z")),
                Timestamp.from(Instant.parse("2026-08-03T08:00:00Z")));

        DashboardNewUsersDrilldownQueryService service =
                new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));

        List<WidgetEntry> widgets = new ArrayList<>();
        widgets.add(null);
        widgets.add(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));
        widgets.add(new WidgetEntry(2, "   ", "Ignored", Instant.now()));

        Map<String, Timestamp> earliest = service.findEarliestBySession(widgets);
        assertEquals(1, earliest.size());
        assertEquals(Timestamp.from(Instant.parse("2026-08-03T09:00:00Z")), earliest.get("s1"));
    }

    @Test
    void findTotalChatsBySession_happyPath_accumulatesAcrossRows() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, true, true, false);
        when(rs.getString("session_id")).thenReturn("s1", "s1", "s2", " ");
        when(rs.getInt("c")).thenReturn(2, 3, 1, 9);

        DashboardNewUsersDrilldownQueryService service =
                new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));

        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));
        Map<String, Integer> totals = service.findTotalChatsBySession(widgets);

        assertEquals(Integer.valueOf(5), totals.get("s1"));
        assertEquals(Integer.valueOf(1), totals.get("s2"));
    }

    @Test
    void findMethods_whenPrepareOrQueryOrConnectionFails_returnEmptyMaps() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        doThrow(new SQLException("prepare failed")).when(conn).prepareStatement(anyString());

        DashboardNewUsersDrilldownQueryService service =
                new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));

        assertTrue(service.findEarliestBySession(widgets).isEmpty());

        doReturn(ps).when(conn).prepareStatement(anyString());
        when(ps.executeQuery()).thenThrow(new SQLException("execute failed"));
        assertTrue(service.findTotalChatsBySession(widgets).isEmpty());

        when(dataSource.getConnection()).thenThrow(new SQLException("connection failed"));
        assertTrue(service.findEarliestBySession(widgets).isEmpty());
        assertTrue(service.findTotalChatsBySession(widgets).isEmpty());
    }

        @Test
        void findEarliestBySession_whenTableMissingAndExecuteFails_returnsEmpty() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet noTableRs = mock(ResultSet.class);
        ResultSet yesTableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"})))
            .thenReturn(noTableRs, noTableRs, noTableRs, yesTableRs);
        when(noTableRs.next()).thenReturn(false, false, false);
        when(yesTableRs.next()).thenReturn(true);
        doReturn(ps).when(conn).prepareStatement(anyString());
        when(ps.executeQuery()).thenThrow(new SQLException("execute failed"));

        DashboardNewUsersDrilldownQueryService service =
            new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));

        List<WidgetEntry> widgets = List.of(
            new WidgetEntry(1, "missing-table", "Missing", Instant.now()),
            new WidgetEntry(2, "widget-2", "Widget 2", Instant.now()));

        assertTrue(service.findEarliestBySession(widgets).isEmpty());
        }

        @Test
        void findEarliestBySession_whenStatementCloseFails_hitsOuterCatch() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        doReturn(ps).when(conn).prepareStatement(anyString());
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("session_id")).thenReturn(" ");
        when(rs.getTimestamp("first_seen")).thenReturn(Timestamp.from(Instant.parse("2026-08-03T10:00:00Z")));
        doThrow(new SQLException("close failed")).when(ps).close();

        DashboardNewUsersDrilldownQueryService service =
            new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));

        assertTrue(service.findEarliestBySession(widgets).isEmpty());
        }

        @Test
        void findTotalChatsBySession_whenNullMissingAndPrepareFails_returnsEmpty() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet noTableRs = mock(ResultSet.class);
        ResultSet yesTableRs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"})))
            .thenReturn(noTableRs, noTableRs, noTableRs, yesTableRs);
        when(noTableRs.next()).thenReturn(false, false, false);
        when(yesTableRs.next()).thenReturn(true);
        doThrow(new SQLException("prepare failed")).when(conn).prepareStatement(anyString());

        DashboardNewUsersDrilldownQueryService service =
            new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));

        List<WidgetEntry> widgets = new ArrayList<>();
        widgets.add(null);
        widgets.add(new WidgetEntry(1, "   ", "Blank", Instant.now()));
        widgets.add(new WidgetEntry(2, "missing-table", "Missing", Instant.now()));
        widgets.add(new WidgetEntry(3, "widget-3", "Widget 3", Instant.now()));

        assertTrue(service.findTotalChatsBySession(widgets).isEmpty());
        }

        @Test
        void findTotalChatsBySession_whenStatementCloseAndMetadataFail_hitsCatches() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        doReturn(ps).when(conn).prepareStatement(anyString());
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        doThrow(new SQLException("close failed")).when(ps).close();

        DashboardNewUsersDrilldownQueryService service =
            new DashboardNewUsersDrilldownQueryService(holder, Logger.getLogger("test"));
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));

        assertTrue(service.findTotalChatsBySession(widgets).isEmpty());

        Connection badConn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(badConn);
        when(badConn.getMetaData()).thenThrow(new SQLException("metadata failed"));
        assertTrue(service.findTotalChatsBySession(widgets).isEmpty());
        }

    @Test
    void privateHelpers_coverIdentifierSanitizeAndMetadataFailure() throws Exception {
        DashboardNewUsersDrilldownQueryService service =
                new DashboardNewUsersDrilldownQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));

        assertEquals("widget", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, (Object) null));
        assertEquals("widget", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
        assertEquals("w_123_bad___", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, "123-bad.!@"));

        String sixtyFiveChars = "a".repeat(65);
        String sanitized = (String) invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, sixtyFiveChars);
        assertEquals(60, sanitized.length());

        assertEquals("\"a\"\"b\"", invoke(service, "quoteIdentifier", new Class[]{String.class}, "a\"b"));
        assertEquals(0, invoke(service, "safeInt", new Class[]{Integer.class}, new Object[]{null}));
        assertEquals(3, invoke(service, "safeInt", new Class[]{Integer.class}, Integer.valueOf(3)));

        Connection badConn = mock(Connection.class);
        when(badConn.getMetaData()).thenThrow(new SQLException("metadata failure"));
        boolean exists = (Boolean) invoke(service, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget");
        assertFalse(exists);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw ex;
        }
    }
}

