package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

class WidgetSyncJdbcStoreTest {

    @Test
    void normalizeSummaryPrompt_trimsNormalizesAndTruncates() throws Exception {
        WidgetSyncJdbcStore store = new WidgetSyncJdbcStore(300, true, 200, 200, 2000, 200000, 10);

        String normalized = (String) invoke(store, "normalizeSummaryPrompt", new Class[]{String.class}, "\r\n  hello\rworld\n  ");
        assertEquals("hello\nworl", normalized);
    }

    @Test
    void quoteIdentifier_allowsSafeNamesAndRejectsUnsafeOnes() throws Exception {
        WidgetSyncJdbcStore store = new WidgetSyncJdbcStore(300, true, 200, 200, 2000, 200000, 5000);

        assertEquals("\"safe_name\"", invoke(store, "quoteIdentifier", new Class[]{String.class}, "safe_name"));

        Exception ex = assertThrows(Exception.class,
                () -> invoke(store, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void persistedValueReaders_applyValidationAndFallbacks() throws Exception {
        WidgetSyncJdbcStore store = new WidgetSyncJdbcStore(300, true, 200, 200, 2000, 200000, 5000);
        ResultSet rs = mock(ResultSet.class);

        when(rs.getCharacterStream("interval")).thenReturn(null);
        when(rs.getBytes("interval")).thenReturn(null);
        when(rs.getString("interval")).thenReturn("600");

        when(rs.getCharacterStream("badInterval")).thenReturn(null);
        when(rs.getBytes("badInterval")).thenReturn(null);
        when(rs.getString("badInterval")).thenReturn("abc");

        when(rs.getCharacterStream("enabled")).thenReturn(null);
        when(rs.getBytes("enabled")).thenReturn(null);
        when(rs.getString("enabled")).thenReturn("t");

        when(rs.getCharacterStream("badBool")).thenReturn(null);
        when(rs.getBytes("badBool")).thenReturn(null);
        when(rs.getString("badBool")).thenReturn("maybe");

        long interval = (long) invoke(store, "readPersistedIntervalSeconds", new Class[]{ResultSet.class, String.class, long.class}, rs, "interval", 300L);
        long badInterval = (long) invoke(store, "readPersistedIntervalSeconds", new Class[]{ResultSet.class, String.class, long.class}, rs, "badInterval", 300L);
        boolean enabled = (boolean) invoke(store, "readPersistedBoolean", new Class[]{ResultSet.class, String.class, boolean.class}, rs, "enabled", false);
        boolean fallbackBool = (boolean) invoke(store, "readPersistedBoolean", new Class[]{ResultSet.class, String.class, boolean.class}, rs, "badBool", true);

        assertEquals(600L, interval);
        assertEquals(300L, badInterval);
        assertEquals(true, enabled);
        assertEquals(true, fallbackBool);
    }

    @Test
    void readDbText_returnsEmptyWhenResultSetReadFails() throws Exception {
        WidgetSyncJdbcStore store = new WidgetSyncJdbcStore(300, true, 200, 200, 2000, 200000, 5000);
        ResultSet rs = mock(ResultSet.class);

        when(rs.getCharacterStream("c")).thenThrow(new SQLException("boom"));
        when(rs.getBytes("c")).thenThrow(new SQLException("boom"));
        when(rs.getString("c")).thenThrow(new SQLException("boom"));
        when(rs.getObject("c")).thenThrow(new SQLException("boom"));

        String value = (String) invoke(store, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, rs, "c", 10);
        assertEquals("", value);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
