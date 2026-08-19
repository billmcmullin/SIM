package com.sim.chatserver.web.dashboard.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

class InactiveUsersListQueryServiceTest {

    @Test
    void sanitizeWidgetTableName_normalizesAndBoundsValue() throws Exception {
        InactiveUsersListQueryService service = new InactiveUsersListQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));

        assertEquals("widget", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, (Object) null));
        assertEquals("w_123", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, "123"));
        assertEquals("abc___", invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-+="));

        String longName = "a".repeat(90);
        String normalized = (String) invoke(service, "sanitizeWidgetTableName", new Class[]{String.class}, longName);
        assertEquals(60, normalized.length());
    }

    @Test
    void quoteIdentifier_rejectsUnsafeNames() throws Exception {
        InactiveUsersListQueryService service = new InactiveUsersListQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));

        assertEquals("\"ok_name\"", invoke(service, "quoteIdentifier", new Class[]{String.class}, "ok_name"));

        Exception ex = assertThrows(Exception.class,
                () -> invoke(service, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void readNonNegativeLong_andInstantParser_handleFallbacks() throws Exception {
        InactiveUsersListQueryService service = new InactiveUsersListQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("total")).thenReturn("-5");
        long nonNegative = (long) invoke(service, "readNonNegativeLong", new Class[]{ResultSet.class, String.class}, rs, "total");
        assertEquals(0L, nonNegative);

        when(rs.getString("last_entry")).thenReturn("2026-08-01T10:15:30Z");
        Instant iso = (Instant) invoke(service, "readSafeInstant", new Class[]{ResultSet.class, String.class}, rs, "last_entry");
        assertNotNull(iso);

        when(rs.getString("last_entry")).thenReturn("2026-08-01 10:15:30");
        Instant sqlText = (Instant) invoke(service, "readSafeInstant", new Class[]{ResultSet.class, String.class}, rs, "last_entry");
        assertNotNull(sqlText);

        when(rs.getString("last_entry")).thenReturn("not-a-date");
        Instant invalid = (Instant) invoke(service, "readSafeInstant", new Class[]{ResultSet.class, String.class}, rs, "last_entry");
        assertNull(invalid);
    }

    @Test
    void readAtMostChars_readsWithinLimit() throws Exception {
        InactiveUsersListQueryService service = new InactiveUsersListQueryService(mock(AppDataSourceHolder.class), Logger.getLogger("test"));
        String value = (String) invoke(service, "readAtMostChars", new Class[]{java.io.Reader.class, int.class}, new StringReader("abcdef"), 3);
        assertEquals("abc", value);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
