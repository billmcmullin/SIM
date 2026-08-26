package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class WidgetTableDataServiceTest {

    @Test
    void parseHelpers_handleLimitsPagesAndSorting() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();

        assertEquals(10, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, (Object) null));
        assertEquals(25, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, "25"));
        assertEquals(10, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, "999"));

        assertEquals(1, invokeInt(service, "parsePage", new Class<?>[]{String.class}, (Object) null));
        assertEquals(3, invokeInt(service, "parsePage", new Class<?>[]{String.class}, "3"));
        assertEquals(1, invokeInt(service, "parsePage", new Class<?>[]{String.class}, "-20"));

        assertEquals("prompt", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "PROMPT"));
        assertEquals("created_at", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "unknown"));

        assertEquals("ASC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "asc"));
        assertEquals("DESC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "zzz"));
    }

    @Test
    void sqlIdentifierAndTableNameHelpers_areSafe() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));
    }

    @Test
    void filterState_buildsExpectedWhereClauseAndParams() throws Exception {
        Class<?> filterStateClass = Class.forName("com.sim.chatserver.web.dashboard.drilldown.WidgetTableDataService$FilterState");
        Constructor<?> ctor = filterStateClass.getDeclaredConstructor(String.class, String.class, String.class, LocalDate.class);
        ctor.setAccessible(true);

        Object filterState = ctor.newInstance("promptTerm", "responseTerm", "globalTerm", LocalDate.of(2026, 8, 26));

        Method buildWhere = filterStateClass.getDeclaredMethod("buildWhereClause");
        buildWhere.setAccessible(true);
        String where = (String) buildWhere.invoke(filterState);

        assertTrue(where.contains("prompt ILIKE ?"));
        assertTrue(where.contains("response_text ILIKE ?"));
        assertTrue(where.contains("session_id ILIKE ?"));
        assertTrue(where.contains("created_at >= ? AND created_at < ?"));

        Method paramsMethod = filterStateClass.getDeclaredMethod("params");
        paramsMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> params = (List<Object>) paramsMethod.invoke(filterState);

        assertEquals(7, params.size());
        assertEquals("%promptTerm%", params.get(0));
        assertEquals("%responseTerm%", params.get(1));
        assertEquals("%globalTerm%", params.get(2));
        assertTrue(params.get(5) instanceof Timestamp);
        assertTrue(params.get(6) instanceof Timestamp);
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private int invokeInt(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Integer) invokeObject(target, methodName, paramTypes, args)).intValue();
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }
}
