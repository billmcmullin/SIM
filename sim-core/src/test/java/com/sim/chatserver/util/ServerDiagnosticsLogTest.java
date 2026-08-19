package com.sim.chatserver.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class ServerDiagnosticsLogTest {

    @Test
    void isTruthy_acceptsExpectedTokens() throws Exception {
        assertTrue((boolean) invokeStatic("isTruthy", new Class[]{String.class}, "true"));
        assertTrue((boolean) invokeStatic("isTruthy", new Class[]{String.class}, " YES "));
        assertTrue((boolean) invokeStatic("isTruthy", new Class[]{String.class}, "1"));
        assertTrue((boolean) invokeStatic("isTruthy", new Class[]{String.class}, "on"));
        assertEquals(false, invokeStatic("isTruthy", new Class[]{String.class}, "off"));
    }

    @Test
    void safeToken_stripsLineBreaksAndFallsBack() throws Exception {
        assertEquals("abc def", invokeStatic("safeToken", new Class[]{String.class, String.class}, "abc\ndef", "x"));
        assertEquals("fallback", invokeStatic("safeToken", new Class[]{String.class, String.class}, "   ", "fallback"));
        assertEquals("fallback", invokeStatic("safeToken", new Class[]{String.class, String.class}, null, "fallback"));
    }

    @Test
    void sanitizeConfigValue_stripsControlCharsAndTruncates() throws Exception {
        String cleaned = (String) invokeStatic("sanitizeConfigValue", new Class[]{String.class}, "ab\u0000c\r\nd");
        assertEquals("abcd", cleaned);

        String longInput = "x".repeat(700);
        String truncated = (String) invokeStatic("sanitizeConfigValue", new Class[]{String.class}, longInput);
        assertEquals(512, truncated.length());
    }

    @Test
    void safeErrorMessage_flattensMultilineMessages() throws Exception {
        RuntimeException error = new RuntimeException("line1\nline2\rline3");
        String value = (String) invokeStatic("safeErrorMessage", new Class[]{Throwable.class}, error);
        assertEquals("line1 line2 line3", value);
    }

    private static Object invokeStatic(String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = ServerDiagnosticsLog.class.getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}
