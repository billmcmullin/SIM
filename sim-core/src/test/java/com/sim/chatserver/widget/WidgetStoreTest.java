package com.sim.chatserver.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 class WidgetStoreTest {

    @Test
    void normalizeRequired_trimsAndRejectsBlank() throws Exception {
        assertEquals("abc", invoke("normalizeRequired", new Class<?>[] { String.class, String.class }, " abc ", "name"));

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> invoke("normalizeRequired", new Class<?>[] { String.class, String.class }, " ", "name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void sanitizeDbText_handlesNullAndMaxLength() throws Exception {
        assertEquals("", invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, null, 10));
        assertEquals("trim", invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, "  trim  ", 10));
        assertEquals("abc", invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, "abcdef", 3));
    }

    @Test
    void readNonNegativeInt_coversTypedAndFallbackPaths() throws Exception {
        ResultSet typed = mock(ResultSet.class);
        when(typed.getInt("id")).thenReturn(7);
        when(typed.wasNull()).thenReturn(false);
        assertEquals(7, invoke("readNonNegativeInt", new Class<?>[] { ResultSet.class, String.class }, typed, "id"));

        ResultSet negative = mock(ResultSet.class);
        when(negative.getInt("id")).thenReturn(-9);
        when(negative.wasNull()).thenReturn(false);
        assertEquals(0, invoke("readNonNegativeInt", new Class<?>[] { ResultSet.class, String.class }, negative, "id"));

        ResultSet fallback = mock(ResultSet.class);
        when(fallback.getInt("id")).thenReturn(0);
        when(fallback.wasNull()).thenReturn(true);
        when(fallback.getString("id")).thenReturn("17");
        assertEquals(17, invoke("readNonNegativeInt", new Class<?>[] { ResultSet.class, String.class }, fallback, "id"));
    }

    @Test
    void readCreatedAt_usesTimestampAndFallbackParsing() throws Exception {
        Instant now = Instant.parse("2026-08-07T10:20:30Z");

        ResultSet typed = mock(ResultSet.class);
        when(typed.getTimestamp("created_at")).thenReturn(Timestamp.from(now));
        assertEquals(now, ((Instant) invoke("readCreatedAt", new Class<?>[] { ResultSet.class }, typed)));

        ResultSet fallback = mock(ResultSet.class);
        when(fallback.getTimestamp("created_at")).thenThrow(new SQLException("no typed timestamp"));
        when(fallback.getString("created_at")).thenReturn("2026-08-07T10:20:30Z");
        assertEquals(now, ((Instant) invoke("readCreatedAt", new Class<?>[] { ResultSet.class }, fallback)));

        ResultSet invalid = mock(ResultSet.class);
        when(invalid.getTimestamp("created_at")).thenThrow(new SQLException("no typed timestamp"));
        when(invalid.getString("created_at")).thenReturn("bad");
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> invoke("readCreatedAt", new Class<?>[] { ResultSet.class }, invalid));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void mapRow_andFlags_coverUtilityBranches() throws Exception {
        Instant now = Instant.parse("2026-08-07T10:20:30Z");
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id")).thenReturn(5);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("widget_id")).thenReturn(" wid ");
        when(rs.getString("display_name")).thenReturn(" name ");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(now));

        WidgetEntry entry = (WidgetEntry) invoke("mapRow", new Class<?>[] { ResultSet.class }, rs);
        assertEquals(5, entry.getId());
        assertEquals("wid", entry.getWidgetId());
        assertEquals("name", entry.getDisplayName());
        assertEquals(now, entry.getCreatedAt());

        SQLException undefinedTable = new SQLException("x", "42P01");
        SQLException uniqueViolation = new SQLException("x", "23505");
        assertEquals(true, invoke("isUndefinedTable", new Class<?>[] { SQLException.class }, undefinedTable));
        assertEquals(true, invoke("isUniqueViolation", new Class<?>[] { SQLException.class }, uniqueViolation));
    }

    @Test
    void readSanitizedDbText_returnsEmptyWhenColumnReadFails() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("widget_id")).thenThrow(new SQLException("boom"));

        assertEquals("", invoke("readSanitizedDbText", new Class<?>[] { ResultSet.class, String.class, int.class }, rs, "widget_id", 10));
    }

    private static Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = WidgetStore.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}

