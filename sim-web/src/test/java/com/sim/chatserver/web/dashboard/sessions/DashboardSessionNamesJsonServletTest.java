package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardSessionNamesJsonServletTest {

    @Test
    void doGet_unauthorizedWhenSessionMissing() throws Exception {
        DashboardSessionNamesJsonServlet servlet = new DashboardSessionNamesJsonServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        when(context.getContextPath()).thenReturn("/ctx");
        when(req.getServletContext()).thenReturn(context);
        when(req.getSession(false)).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> json = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doGet(req, resp);
            json.verify(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.any(JsonObject.class)));
        }
    }

    @Test
    void doGet_missingContext_sendsFallbackServerError() throws Exception {
        DashboardSessionNamesJsonServlet servlet = new DashboardSessionNamesJsonServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getServletContext()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void helperMethods_coverParsingAndFormatting() throws Exception {
        DashboardSessionNamesJsonServlet servlet = new DashboardSessionNamesJsonServlet();

        assertEquals("", invoke(servlet, "nullSafe", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("x", invoke(servlet, "nullSafe", new Class<?>[]{String.class}, "x"));

        assertEquals(10, invoke(servlet, "parsePositiveInteger", new Class<?>[]{String.class, int.class}, " ", 10));
        assertEquals(10, invoke(servlet, "parsePositiveInteger", new Class<?>[]{String.class, int.class}, "-1", 10));
        assertEquals(5, invoke(servlet, "parsePositiveInteger", new Class<?>[]{String.class, int.class}, "5", 10));

        assertEquals(-1, invoke(servlet, "parseNonNegativeInteger", new Class<?>[]{String.class, int.class}, "-2", -1));
        assertEquals(3, invoke(servlet, "parseNonNegativeInteger", new Class<?>[]{String.class, int.class}, "3", -1));

        String reviewUrl = (String) invoke(servlet, "buildReviewUrl", new Class<?>[]{String.class, String.class}, "/ctx", "a b");
        assertTrue(reviewUrl.contains("/ctx/dashboard/sessions/drilldown/session-review?sessionId="));
        assertTrue(reviewUrl.contains("a+b"));

        String nullFormatted = (String) invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, new Object[]{null});
        assertTrue(!nullFormatted.isBlank());

        Timestamp ts = Timestamp.from(Instant.parse("2026-08-27T10:00:00Z"));
        String formatted = (String) invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, ts);
        assertEquals("2026-08-27T10:00:00Z", formatted);
    }

    @Test
    void writeJson_fallbacksToSendErrorWhenUtilityFails() throws Exception {
        DashboardSessionNamesJsonServlet servlet = new DashboardSessionNamesJsonServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        JsonObject payload = Json.createObjectBuilder().add("status", "ok").build();

        try (MockedStatic<ServletJsonResponseUtil> json = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            json.when(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_OK), Mockito.eq(payload)))
                    .thenThrow(new IOException("io"));

            invoke(servlet, "writeJson", new Class<?>[]{HttpServletResponse.class, int.class, JsonObject.class},
                    resp, HttpServletResponse.SC_OK, payload);

            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
