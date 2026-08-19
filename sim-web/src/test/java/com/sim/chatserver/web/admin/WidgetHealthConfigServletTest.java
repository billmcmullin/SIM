package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

class WidgetHealthConfigServletTest {

    @Test
    void parseJson_blankBody_returnsEmptyObject() throws Exception {
        WidgetHealthConfigServlet servlet = new WidgetHealthConfigServlet();
        JsonObject parsed = (JsonObject) invoke(servlet, "parseJson", new Class[]{String.class}, "   ");
        assertNotNull(parsed);
        assertTrue(parsed.isEmpty());
    }

    @Test
    void stringIntBoolParsers_handleMixedJsonTypes() throws Exception {
        WidgetHealthConfigServlet servlet = new WidgetHealthConfigServlet();
        JsonObject obj = Json.createObjectBuilder()
                .add("name", " value ")
                .add("numberAsText", "7")
                .add("truthyAsText", "yes")
                .add("falsyAsText", "0")
                .build();

        assertEquals("value", invoke(servlet, "stringOrNull", new Class[]{JsonObject.class, String.class}, obj, "name"));
        assertNull(invoke(servlet, "stringOrNull", new Class[]{JsonObject.class, String.class}, obj, "missing"));

        assertEquals(7, invoke(servlet, "intOrDefault", new Class[]{JsonObject.class, String.class, int.class}, obj, "numberAsText", 3));
        assertEquals(3, invoke(servlet, "intOrDefault", new Class[]{JsonObject.class, String.class, int.class}, obj, "missing", 3));

        assertTrue((boolean) invoke(servlet, "boolOrDefault", new Class[]{JsonObject.class, String.class, boolean.class}, obj, "truthyAsText", false));
        assertFalse((boolean) invoke(servlet, "boolOrDefault", new Class[]{JsonObject.class, String.class, boolean.class}, obj, "falsyAsText", true));
        assertTrue((boolean) invoke(servlet, "boolOrDefault", new Class[]{JsonObject.class, String.class, boolean.class}, obj, "missing", true));
    }

    @Test
    void intervalConverters_applyBoundsAndFallbacks() throws Exception {
        WidgetHealthConfigServlet servlet = new WidgetHealthConfigServlet();

        assertEquals(300, invoke(servlet, "minutesToSeconds", new Class[]{int.class, int.class}, 0, 5));
        assertEquals(Integer.MAX_VALUE, invoke(servlet, "minutesToSeconds", new Class[]{int.class, int.class}, Integer.MAX_VALUE, 5));

        assertEquals(1, invoke(servlet, "secondsToMinutes", new Class[]{int.class}, -10));
        assertEquals(2, invoke(servlet, "secondsToMinutes", new Class[]{int.class}, 120));
    }

    @Test
    void readRequestBody_returnsNullOnIOException() throws Exception {
        WidgetHealthConfigServlet servlet = new WidgetHealthConfigServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getReader()).thenThrow(new IOException("boom"));

        Object result = invoke(servlet, "readRequestBody", new Class[]{HttpServletRequest.class}, req);
        assertNull(result);
    }

    @Test
    void readRequestBody_readsAndNormalizesBody() throws Exception {
        WidgetHealthConfigServlet servlet = new WidgetHealthConfigServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getReader()).thenReturn(new java.io.BufferedReader(new StringReader("{\"x\":1}")));

        String body = (String) invoke(servlet, "readRequestBody", new Class[]{HttpServletRequest.class}, req);
        assertEquals("{\"x\":1}", body);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
