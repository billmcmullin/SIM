package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;

class AllSessionsServiceTest {

    @Test
    void parseAndSanitizeHelpers_handleValidAndInvalidInputs() throws Exception {
        AllSessionsService service = new AllSessionsService();

        assertEquals(25, invokeInt(service, "parseInteger", new Class<?>[]{String.class, int.class}, "25", 10));
        assertEquals(10, invokeInt(service, "parseInteger", new Class<?>[]{String.class, int.class}, "abc", 10));
        assertEquals(10, invokeInt(service, "parseInteger", new Class<?>[]{String.class, int.class}, "123456789012", 10));

        assertEquals("all", invokeString(service, "sanitizeActivity", new Class<?>[]{String.class}, (Object) null));
        assertEquals("active", invokeString(service, "sanitizeActivity", new Class<?>[]{String.class}, "ACTIVE"));
        assertEquals("all", invokeString(service, "sanitizeActivity", new Class<?>[]{String.class}, "unexpected"));

        assertEquals("session-1", invokeString(service, "sanitizeSessionId", new Class<?>[]{String.class}, "session-1"));
        assertNull(invokeObject(service, "sanitizeSessionId", new Class<?>[]{String.class}, "bad id"));

        assertEquals("chat-1", invokeString(service, "sanitizeChatId", new Class<?>[]{String.class}, "chat-1"));
        assertNull(invokeObject(service, "sanitizeChatId", new Class<?>[]{String.class}, "chat id"));

        assertTrue(invokeBoolean(service, "parseBooleanParam", new Class<?>[]{String.class}, "TRUE"));
        assertTrue(!invokeBoolean(service, "parseBooleanParam", new Class<?>[]{String.class}, "1"));

        assertEquals("hello", invokeString(service, "sanitizeTextParam", new Class<?>[]{String.class, int.class}, "  hello  ", 16));
        assertEquals("abcd", invokeString(service, "sanitizeTextParam", new Class<?>[]{String.class, int.class}, "abcdef", 4));
    }

    @Test
    void resolvePathHelpers_defaultAndRecognizedPaths() throws Exception {
        AllSessionsService service = new AllSessionsService();

        HttpServletRequest reqWithoutMapping = mock(HttpServletRequest.class);
        when(reqWithoutMapping.getHttpServletMapping()).thenReturn(null);
        assertEquals("/dashboard/sessions/data",
                invokeString(service, "resolveRequestPath", new Class<?>[]{HttpServletRequest.class}, reqWithoutMapping));

        HttpServletRequest reqWithChatsPath = mock(HttpServletRequest.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        when(reqWithChatsPath.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/sessions/chats");
        assertEquals("/dashboard/sessions/chats",
                invokeString(service, "resolveRequestPath", new Class<?>[]{HttpServletRequest.class}, reqWithChatsPath));

        assertEquals("/dashboard/sessions/data",
                invokeString(service, "normalizeServletPath", new Class<?>[]{String.class}, "/unknown"));
    }

    @Test
    void sqlAndTableNameHelpers_enforceSafety() throws Exception {
        AllSessionsService service = new AllSessionsService();

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));
        assertTrue(normalized.contains("_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> invokeObject(service, "quoteIdentifier", new Class<?>[]{String.class}, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void applyActivityFilter_keepsExpectedRows() throws Exception {
        AllSessionsService service = new AllSessionsService();

        List<Object> inactiveTarget = new ArrayList<>();
        inactiveTarget.add(newSessionSummary("s-old", Instant.now().minus(30, ChronoUnit.DAYS)));
        inactiveTarget.add(newSessionSummary("s-new", Instant.now()));
        inactiveTarget.add(newSessionSummary("s-null", null));

        String inactive = invokeString(service, "applyActivityFilter", new Class<?>[]{List.class, String.class}, inactiveTarget, "inactive");
        assertEquals("inactive", inactive);
        assertEquals(1, inactiveTarget.size());

        List<Object> activeTarget = new ArrayList<>();
        activeTarget.add(newSessionSummary("s-old", Instant.now().minus(30, ChronoUnit.DAYS)));
        activeTarget.add(newSessionSummary("s-new", Instant.now()));
        activeTarget.add(newSessionSummary("s-null", null));

        String active = invokeString(service, "applyActivityFilter", new Class<?>[]{List.class, String.class}, activeTarget, "active");
        assertEquals("active", active);
        assertEquals(2, activeTarget.size());
    }

        @Test
        void sessionSearchAndFormattingHelpers_coverAdditionalBranches() throws Exception {
        AllSessionsService service = new AllSessionsService();

        Map<String, Object> sessions = new HashMap<>();
        sessions.put("Alpha-Session", newSessionSummary("Alpha-Session", Instant.now()));
        sessions.put("Beta-Session", newSessionSummary("Beta-Session", Instant.now()));

        @SuppressWarnings("unchecked")
        Set<String> idMatches = (Set<String>) invokeObject(
            service,
            "gatherSessionIdsByIdMatch",
            new Class<?>[]{Map.class, String.class},
            sessions,
            "alpha");
        assertEquals(Set.of("Alpha-Session"), idMatches);

        @SuppressWarnings("unchecked")
        Set<String> noMatches = (Set<String>) invokeObject(
            service,
            "gatherSessionIdsByIdMatch",
            new Class<?>[]{Map.class, String.class},
            sessions,
            "");
        assertTrue(noMatches.isEmpty());

        Map<String, Object> labels = new HashMap<>();
        labels.put("s1", newSessionLabel("Alice Smith", ""));
        labels.put("s2", newSessionLabel("", "bob@example.com"));
        labels.put("s3", newSessionLabel("", ""));

        @SuppressWarnings("unchecked")
        Set<String> labelMatches = (Set<String>) invokeObject(
            service,
            "gatherSessionIdsByLabelMatch",
            new Class<?>[]{Map.class, String.class},
            labels,
            "bob");
        assertEquals(Set.of("s2"), labelMatches);

        assertEquals("", invokeString(service, "formatInstant", new Class<?>[]{Instant.class}, (Object) null));
        assertEquals(
            "2026-08-26T00:00:00Z",
            invokeString(
                service,
                "formatTimestamp",
                new Class<?>[]{Timestamp.class},
                Timestamp.from(Instant.parse("2026-08-26T00:00:00Z"))));

        assertEquals(1, invokeInt(service, "clamp", new Class<?>[]{int.class, int.class, int.class}, -5, 1, 10));
        assertEquals(10, invokeInt(service, "clamp", new Class<?>[]{int.class, int.class, int.class}, 99, 1, 10));
        }

    private Object newSessionSummary(String sessionId, Instant lastSeen) throws Exception {
        Class<?> summaryClass = Class.forName("com.sim.chatserver.web.dashboard.sessions.AllSessionsService$SessionSummary");
        Constructor<?> ctor = summaryClass.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        Object summary = ctor.newInstance(sessionId);

        Field lastSeenField = summaryClass.getDeclaredField("lastSeen");
        lastSeenField.setAccessible(true);
        lastSeenField.set(summary, lastSeen);
        return summary;
    }

    private Object newSessionLabel(String displayName, String email) throws Exception {
        Class<?> labelClass = Class.forName("com.sim.chatserver.util.SessionLabelStore$SessionLabel");
        Constructor<?> ctor = labelClass.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(displayName, email);
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }

    private int invokeInt(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Integer) invokeObject(target, methodName, paramTypes, args)).intValue();
    }

    private boolean invokeBoolean(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Boolean) invokeObject(target, methodName, paramTypes, args)).booleanValue();
    }
}
