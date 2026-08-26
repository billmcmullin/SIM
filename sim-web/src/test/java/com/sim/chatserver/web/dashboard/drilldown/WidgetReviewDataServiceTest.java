package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermChatSnapshot;

class WidgetReviewDataServiceTest {

    @Test
    void parseAndIdentifierHelpers_coverSafetyAndDefaults() throws Exception {
        WidgetReviewDataService service = new WidgetReviewDataService();

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> invokeObject(service, "quoteIdentifier", new Class<?>[]{String.class}, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);

        assertEquals("prompt", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "PROMPT"));
        assertEquals("created_at", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "bad"));

        assertEquals("ASC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "asc"));
        assertEquals("DESC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "other"));

        assertEquals(7, invokeInt(service, "parseInteger", new Class<?>[]{String.class, int.class}, "7", 2));
        assertEquals(2, invokeInt(service, "parseInteger", new Class<?>[]{String.class, int.class}, "x", 2));

        assertEquals(50, invokeInt(service, "clampLimit", new Class<?>[]{int.class}, 0));
        assertEquals(100, invokeInt(service, "clampLimit", new Class<?>[]{int.class}, 100));

        assertTrue(invokeBoolean(service, "isUnlimitedLimit", new Class<?>[]{String.class, Integer.class}, "all", Integer.valueOf(0)));
        assertTrue(invokeBoolean(service, "isUnlimitedLimit", new Class<?>[]{String.class, Integer.class}, "-1", Integer.valueOf(-1)));
        assertTrue(!invokeBoolean(service, "isUnlimitedLimit", new Class<?>[]{String.class, Integer.class}, "10", Integer.valueOf(10)));

        assertEquals(1, invokeInt(service, "computeTotalPages", new Class<?>[]{int.class, int.class}, 0, 10));
        assertEquals(3, invokeInt(service, "computeTotalPages", new Class<?>[]{int.class, int.class}, 101, 50));
    }

    @Test
    void snapshotFiltersAndSorting_coverSearchAndOrderPaths() throws Exception {
        WidgetReviewDataService service = new WidgetReviewDataService();

        List<TermChatSnapshot> input = new ArrayList<>();
        input.add(new TermChatSnapshot("Term", "w1", "c1", "alpha issue", "ok", ts("2026-08-25T10:00:00Z"), "session-a"));
        input.add(new TermChatSnapshot("Term", "w1", "c2", "beta", "error response", ts("2026-08-26T10:00:00Z"), "session-b"));
        input.add(new TermChatSnapshot("Term", "w1", "c3", "gamma", "ok", ts("2026-08-24T10:00:00Z"), "session-error"));

        @SuppressWarnings("unchecked")
        List<TermChatSnapshot> filtered = (List<TermChatSnapshot>) invokeObject(
                service,
                "filterSnapshots",
                new Class<?>[]{List.class, String.class},
                input,
                "error");

        assertEquals(2, filtered.size());

        @SuppressWarnings("unchecked")
        List<TermChatSnapshot> allWhenBlank = (List<TermChatSnapshot>) invokeObject(
                service,
                "filterSnapshots",
                new Class<?>[]{List.class, String.class},
                input,
                "   ");

        assertEquals(3, allWhenBlank.size());

        invokeObject(service, "sortSnapshots", new Class<?>[]{List.class, String.class, String.class}, input, "created_at", "DESC");
        assertEquals("c2", input.get(0).getChatId());

        invokeObject(service, "sortSnapshots", new Class<?>[]{List.class, String.class, String.class}, input, "prompt", "ASC");
        assertEquals("alpha issue", input.get(0).getPrompt());
    }

        @Test
        void additionalHelperPaths_coverSelectionAndUtilityBranches() throws Exception {
        WidgetReviewDataService service = new WidgetReviewDataService();

        assertEquals(Integer.valueOf(12), invokeObject(service, "parseIntegerOrNull", new Class<?>[]{String.class}, "12"));
        assertEquals(null, invokeObject(service, "parseIntegerOrNull", new Class<?>[]{String.class}, "oops"));

        assertEquals(9, invokeInt(service, "valueOrDefault", new Class<?>[]{Integer.class, int.class}, null, 9));
        assertEquals(2, invokeInt(service, "valueOrDefault", new Class<?>[]{Integer.class, int.class}, Integer.valueOf(2), 9));

        assertEquals(
            "selection_1",
            invokeObject(service, "sanitizeSelectionId", new Class<?>[]{String.class}, "selection_1"));
        assertEquals(
            null,
            invokeObject(service, "sanitizeSelectionId", new Class<?>[]{String.class}, "bad id"));

        assertEquals(
            Boolean.TRUE,
            invokeObject(service, "containsIgnoreCase", new Class<?>[]{String.class, String.class}, "Hello", "ell"));
        assertEquals(
            Boolean.FALSE,
            invokeObject(service, "containsIgnoreCase", new Class<?>[]{String.class, String.class}, "Hello", "xyz"));

        assertEquals(null, invokeObject(service, "trimToNull", new Class<?>[]{String.class}, "   "));
        assertEquals("value", invokeObject(service, "trimToNull", new Class<?>[]{String.class}, " value "));
        assertEquals("", invokeObject(service, "nullToEmpty", new Class<?>[]{String.class}, (Object) null));

        assertEquals("1.23", invokeObject(service, "twoDecimals", new Class<?>[]{double.class}, 1.234));
        assertEquals(
            Integer.MAX_VALUE,
            invokeInt(service, "computeTotalPages", new Class<?>[]{int.class, int.class}, Integer.MAX_VALUE, 0));

        List<TermChatSnapshot> defaultSorted = new ArrayList<>();
        defaultSorted.add(new TermChatSnapshot("Term", "w1", "c2", "z", "r", ts("2026-08-26T10:00:00Z"), "s2"));
        defaultSorted.add(new TermChatSnapshot("Term", "w1", "c1", "a", "r", ts("2026-08-26T11:00:00Z"), "s1"));

        invokeObject(service, "sortSnapshots", new Class<?>[]{List.class, String.class, String.class}, defaultSorted, "unknown", "ASC");
        assertEquals("c1", defaultSorted.get(0).getChatId());
        }

    private Timestamp ts(String isoInstant) {
        return Timestamp.from(Instant.parse(isoInstant));
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private int invokeInt(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Integer) invokeObject(target, methodName, paramTypes, args)).intValue();
    }

    private boolean invokeBoolean(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Boolean) invokeObject(target, methodName, paramTypes, args)).booleanValue();
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }
}
