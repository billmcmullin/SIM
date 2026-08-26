package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

class WidgetExportQueryServiceTest {

    @Test
    void identifierAndTableNameHelpers_enforceValidation() throws Exception {
        WidgetExportQueryService service = new WidgetExportQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> invokeObject(service, "quoteIdentifier", new Class<?>[]{String.class}, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void chunkHelper_splitsListsByConfiguredSize() throws Exception {
        WidgetExportQueryService service = new WidgetExportQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));

        @SuppressWarnings("unchecked")
        List<List<String>> chunks = (List<List<String>>) invokeObject(
                service,
                "chunk",
                new Class<?>[]{List.class, int.class},
                List.of("a", "b", "c", "d", "e"),
                2);

        assertEquals(3, chunks.size());
        assertEquals(List.of("a", "b"), chunks.get(0));
        assertEquals(List.of("c", "d"), chunks.get(1));
        assertEquals(List.of("e"), chunks.get(2));

        @SuppressWarnings("unchecked")
        List<List<String>> empty = (List<List<String>>) invokeObject(
                service,
                "chunk",
                new Class<?>[]{List.class, int.class},
                List.of(),
                2);
        assertTrue(empty.isEmpty());
    }

    @Test
    void dbReaders_handleTextAndTimestampFallbacks() throws Exception {
        WidgetExportQueryService service = new WidgetExportQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));
        ResultSet rs = mock(ResultSet.class);

        when(rs.getCharacterStream("prompt")).thenReturn(new StringReader("  hello world  "));
        String text = invokeString(service, "readDbText", new Class<?>[]{ResultSet.class, String.class, int.class}, rs, "prompt", 64);
        assertTrue(text.contains("hello world"));

        when(rs.getTimestamp("created_at")).thenReturn(null);
        when(rs.getCharacterStream("created_at")).thenReturn(new StringReader("2026-08-26T10:00:00Z"));
        Timestamp parsed = (Timestamp) invokeObject(service, "readDbTimestamp", new Class<?>[]{ResultSet.class, String.class}, rs, "created_at");
        assertEquals(Timestamp.from(Instant.parse("2026-08-26T10:00:00Z")), parsed);
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }
}
