package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DatabaseBackupServiceTest {

    @Test
    void sqlAndIdentifierHelpers_validateInputs() throws Exception {
        DatabaseBackupService service = new DatabaseBackupService();

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdent", new Class<?>[]{String.class}, "good_name"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> invokeObject(service, "quoteIdent", new Class<?>[]{String.class}, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);

        assertEquals("safe_name",
                invokeString(service, "sanitizeIdentifier", new Class<?>[]{String.class}, "safe_name"));
        assertNull(invokeObject(service, "sanitizeIdentifier", new Class<?>[]{String.class}, "bad name"));

        assertEquals("line1\nline2",
                invokeString(service, "sanitizeCellText", new Class<?>[]{String.class}, "line1\r\nline2"));
    }

    @Test
    void parserHelpers_supportMultipleTimestampAndDateFormats() throws Exception {
        DatabaseBackupService service = new DatabaseBackupService();

        Timestamp isoTs = (Timestamp) invokeObject(service, "parseTimestamp", new Class<?>[]{String.class}, "2026-08-26T10:15:30Z");
        assertEquals(Timestamp.from(java.time.Instant.parse("2026-08-26T10:15:30Z")), isoTs);

        Timestamp fallbackTs = (Timestamp) invokeObject(service, "parseTimestamp", new Class<?>[]{String.class}, "2026-08-26 10:15:30");
        assertEquals(Timestamp.valueOf("2026-08-26 10:15:30"), fallbackTs);

        assertNull(invokeObject(service, "parseTimestamp", new Class<?>[]{String.class}, "not-a-time"));

        LocalDate localDate = (LocalDate) invokeObject(service, "parseLocalDate", new Class<?>[]{String.class}, "2026-08-26");
        assertEquals(LocalDate.of(2026, 8, 26), localDate);

        LocalDate fromInstant = (LocalDate) invokeObject(service, "parseLocalDate", new Class<?>[]{String.class}, "2026-08-26T10:15:30Z");
        assertEquals(LocalDate.of(2026, 8, 26), fromInstant);

        assertNull(invokeObject(service, "parseLocalDate", new Class<?>[]{String.class}, "1969-12-31"));
    }

    @Test
    void binaryAndEscapingHelpers_handleEdgeCases() throws Exception {
        DatabaseBackupService service = new DatabaseBackupService();

        byte[] tiny = new byte[]{1, 2, 3};
        assertArrayEquals(tiny, (byte[]) invokeObject(service, "sanitizeBinary", new Class<?>[]{byte[].class}, (Object) tiny));

        byte[] huge = new byte[2 * 1024 * 1024 + 10];
        byte[] trimmed = (byte[]) invokeObject(service, "sanitizeBinary", new Class<?>[]{byte[].class}, (Object) huge);
        assertEquals(2 * 1024 * 1024, trimmed.length);

        assertEquals("\"a,b\"",
                invokeString(service, "csvEscape", new Class<?>[]{String.class}, "a,b"));
        assertEquals("\"a\"\"b\"",
                invokeString(service, "csvEscape", new Class<?>[]{String.class}, "a\"b"));
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
