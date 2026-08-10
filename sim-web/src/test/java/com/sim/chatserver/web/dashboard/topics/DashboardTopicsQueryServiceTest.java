package com.sim.chatserver.web.dashboard.topics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
 class DashboardTopicsQueryServiceTest {

    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDown() {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    @Test
    void collectTopicCounts_happyPath_countsTopicsAndWidgetBuckets() throws Exception {
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
        when(rs.getTimestamp("created_at")).thenReturn(
                Timestamp.from(Instant.parse("2026-08-03T09:00:00Z")),
                Timestamp.from(Instant.parse("2026-08-03T10:00:00Z")),
                null,
                Timestamp.from(Instant.parse("2026-08-03T11:00:00Z")));
        when(rs.getString("prompt")).thenReturn("prompt-a", " ", "prompt-b", "prompt-c");

        mockCdi(holder);

        DashboardTopicsQueryService service = new DashboardTopicsQueryService();
        List<WidgetEntry> widgets = new ArrayList<>();
        widgets.add(null);
        widgets.add(new WidgetEntry(1, "widget-1", "", Instant.now()));
        widgets.add(new WidgetEntry(2, "   ", "ignored", Instant.now()));

        DashboardTopicsQueryService.TopicCountResult result = service.collectTopicCounts(
                widgets,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                prompt -> {
                    if (prompt == null) {
                        return null;
                    }
                    if (prompt.contains("a")) {
                        return new LinkedHashSet<>(Set.of("TopicA", ""));
                    }
                    if (prompt.contains("b")) {
                        LinkedHashSet<String> set = new LinkedHashSet<>();
                        set.add("TopicA");
                        set.add(null);
                        set.add("TopicB");
                        set.add(" ");
                        return set;
                    }
                    return Set.of();
                });

        assertEquals(Integer.valueOf(2), result.globalCounts().get("TopicA"));
        assertEquals(Integer.valueOf(1), result.globalCounts().get("TopicB"));
        assertTrue(result.byWidgetCounts().containsKey("widget-1"));
        assertEquals(Integer.valueOf(2), result.byWidgetCounts().get("widget-1").get("TopicA"));
        assertEquals(Integer.valueOf(1), result.byWidgetCounts().get("widget-1").get("TopicB"));
    }

    @Test
    void collectTopicCounts_whenTableMissingOrMatcherEmpty_returnsNoCounts() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet noTableRs = mock(ResultSet.class);
        ResultSet yesTableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(noTableRs, noTableRs, noTableRs, yesTableRs);
        when(noTableRs.next()).thenReturn(false, false, false);
        when(yesTableRs.next()).thenReturn(true);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-03T09:00:00Z")));
        when(rs.getString("prompt")).thenReturn("prompt-x");

        mockCdi(holder);

        DashboardTopicsQueryService service = new DashboardTopicsQueryService();
        List<WidgetEntry> widgets = List.of(
                new WidgetEntry(1, "missing-table", "Missing", Instant.now()),
                new WidgetEntry(2, "widget-2", "Widget 2", Instant.now()));

        DashboardTopicsQueryService.TopicCountResult result = service.collectTopicCounts(
                widgets,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                prompt -> null);

        assertTrue(result.globalCounts().isEmpty());
        assertTrue(result.byWidgetCounts().containsKey("Widget 2"));
        assertTrue(result.byWidgetCounts().get("Widget 2").isEmpty());
    }

    @Test
    void collectTopicCounts_whenPrepareFails_throwsSQLException() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), eq(new String[]{"TABLE"}))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

        mockCdi(holder);

        DashboardTopicsQueryService service = new DashboardTopicsQueryService();
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now()));

        assertThrows(SQLException.class, () -> service.collectTopicCounts(
                widgets,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                prompt -> Set.of("TopicA")));
    }

    @Test
    void privateHelpers_coverIncrementTableExistsSanitizeAndIdentifierValidation() throws Exception {
        DashboardTopicsQueryService service = new DashboardTopicsQueryService();

        Map<String, Integer> counts = new LinkedHashMap<>();
        invoke(service, "incrementCount", new Class[]{Map.class, String.class}, counts, "k");
        invoke(service, "incrementCount", new Class[]{Map.class, String.class}, counts, "k");
        assertEquals(Integer.valueOf(2), counts.get("k"));

        Connection badConn = mock(Connection.class);
        when(badConn.getMetaData()).thenThrow(new SQLException("meta failed"));
        boolean exists = (Boolean) invoke(service, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget");
        assertFalse(exists);

        assertEquals("widget", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, (Object) null));
        assertEquals("w_123_bad___", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, "123-bad.!@"));

        String longName = "a".repeat(75);
        String sanitized = (String) invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, longName);
        assertEquals(60, sanitized.length());

        assertEquals("\"valid_name\"", invoke(service, "quoteIdentifier", new Class[]{String.class}, "valid_name"));

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> rawInvoke(service, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        assertTrue(thrown.getCause() instanceof IllegalArgumentException);

        Map<String, Integer> global = Map.of("A", Integer.valueOf(1));
        Map<String, Map<String, Integer>> byWidget = Map.of("W", Map.of("A", Integer.valueOf(1)));
        DashboardTopicsQueryService.TopicCountResult result =
                new DashboardTopicsQueryService.TopicCountResult(global, byWidget);
        assertEquals(global, result.globalCounts());
        assertEquals(byWidget, result.byWidgetCounts());
    }

    private void mockCdi(AppDataSourceHolder holder) {
        if (cdiMock != null) {
            cdiMock.close();
        }
        cdiMock = org.mockito.Mockito.mockStatic(CDI.class);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = mock(Instance.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);
        when(instance.get()).thenReturn(holder);
        cdiMock.when(CDI::current).thenReturn(cdi);
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

    private static Object rawInvoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}

