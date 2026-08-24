package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

class WidgetTableSelectIdsQueryServiceTest {

    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDownCdiMock() {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    @Test
    void sanitizeWidgetTableName_normalizesAndTruncatesUnsafeInput() throws Exception {
        WidgetTableSelectIdsQueryService underTest = new WidgetTableSelectIdsQueryService(Logger.getLogger("test"));

        assertEquals("widget", invokePrivateString(underTest, "sanitizeWidgetTableName", (Object) null));
        assertEquals("w_9bad_id_", invokePrivateString(underTest, "sanitizeWidgetTableName", " 9bad-id! "));

        String longId = "1" + "x".repeat(100);
        String sanitized = invokePrivateString(underTest, "sanitizeWidgetTableName", longId);
        assertTrue(sanitized.startsWith("w_"));
        assertTrue(sanitized.length() <= 60);
    }

    @Test
    void quoteIdentifier_escapesDoubleQuotes() throws Exception {
        WidgetTableSelectIdsQueryService underTest = new WidgetTableSelectIdsQueryService(Logger.getLogger("test"));

        assertEquals("\"ab\"\"cd\"", invokePrivateString(underTest, "quoteIdentifier", "ab\"cd"));
    }

    @Test
    void selectIds_throwsIllegalStateExceptionWhenConnectionFails() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("boom"));
        mockDataSourceHolderCdi(dataSource);

        WidgetTableSelectIdsQueryService underTest = new WidgetTableSelectIdsQueryService(Logger.getLogger("test"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> underTest.selectIds("widget_1", null, null, null, null));

        assertTrue(ex.getMessage().contains("Unable to fetch chat IDs"));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void selectIds_throwsNoSuchElementExceptionWhenWidgetTableIsMissing() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(any(), any(), anyString(), any(String[].class))).thenReturn(tableRs, tableRs, tableRs);
        when(tableRs.next()).thenReturn(false, false, false);

        mockDataSourceHolderCdi(dataSource);
        WidgetTableSelectIdsQueryService underTest = new WidgetTableSelectIdsQueryService(Logger.getLogger("test"));

        assertThrows(NoSuchElementException.class,
                () -> underTest.selectIds("widget-1", null, null, null, null));

        verify(conn, never()).prepareStatement(anyString());
    }

    @Test
    void selectIds_returnsTrimmedIdsAndBindsFilters() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet dataRs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(any(), any(), anyString(), any(String[].class))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(dataRs);
        when(dataRs.next()).thenReturn(true, true, true, false);
        when(dataRs.getString("widget_chat_id")).thenReturn(" chat-1 ", "  ", "chat-2");

        mockDataSourceHolderCdi(dataSource);
        WidgetTableSelectIdsQueryService underTest = new WidgetTableSelectIdsQueryService(Logger.getLogger("test"));

        List<String> result = underTest.selectIds(
                "widget-1",
                "abc",
                "prompt",
                "response",
                LocalDate.of(2026, 8, 24));

        assertEquals(List.of("chat-1", "chat-2"), result);

        verify(ps).setString(1, "%abc%");
        verify(ps).setString(2, "%abc%");
        verify(ps).setString(3, "%abc%");
        verify(ps).setString(4, "%prompt%");
        verify(ps).setString(5, "%response%");
        verify(ps).setTimestamp(Mockito.eq(6), any(java.sql.Timestamp.class));
        verify(ps).setTimestamp(Mockito.eq(7), any(java.sql.Timestamp.class));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(conn).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("\"widget_1\""));
        assertTrue(sql.contains("ORDER BY created_at DESC"));
    }

    private void mockDataSourceHolderCdi(DataSource dataSource) {
        if (cdiMock != null) {
            cdiMock.close();
        }
        cdiMock = Mockito.mockStatic(CDI.class);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> dsHolderInstance = mock(Instance.class);
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dsHolderInstance.get()).thenReturn(holder);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(dsHolderInstance);
        cdiMock.when(CDI::current).thenReturn(cdi);
    }

    private static String invokePrivateString(WidgetTableSelectIdsQueryService target,
            String methodName,
            Object arg) throws Exception {
        Method method = WidgetTableSelectIdsQueryService.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(target, arg);
    }
}
