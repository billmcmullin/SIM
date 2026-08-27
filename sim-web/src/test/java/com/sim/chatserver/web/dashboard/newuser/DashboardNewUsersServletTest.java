package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class DashboardNewUsersServletTest {

    @Test
    void doGet_unauthenticatedDataPath_returnsUnauthorizedJson() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/new-users/data");
        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void doGet_unauthenticatedPagePath_forwardsToLogin() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/new-users");
        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_nullRequest_writesBadRequestJsonError() {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doGet(null, resp);
            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request."));
        }
    }

    @Test
    void parseDays_acceptsOnlyWhitelistedValues() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        OptionalInt valid = (OptionalInt) invoke(servlet, "parseDays", new Class<?>[]{String.class}, "14");
        OptionalInt invalid = (OptionalInt) invoke(servlet, "parseDays", new Class<?>[]{String.class}, "15");
        OptionalInt badNumber = (OptionalInt) invoke(servlet, "parseDays", new Class<?>[]{String.class}, "abc");
        OptionalInt blank = (OptionalInt) invoke(servlet, "parseDays", new Class<?>[]{String.class}, "   ");

        assertTrue(valid.isPresent());
        assertEquals(14, valid.getAsInt());
        assertFalse(invalid.isPresent());
        assertFalse(badNumber.isPresent());
        assertFalse(blank.isPresent());
    }

    @Test
    void parseLocalDate_handlesValidAndInvalidInput() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        @SuppressWarnings("unchecked")
        Optional<LocalDate> valid = (Optional<LocalDate>) invoke(servlet, "parseLocalDate", new Class<?>[]{String.class}, "2026-08-27");
        @SuppressWarnings("unchecked")
        Optional<LocalDate> invalid = (Optional<LocalDate>) invoke(servlet, "parseLocalDate", new Class<?>[]{String.class}, "not-a-date");

        assertTrue(valid.isPresent());
        assertEquals(LocalDate.of(2026, 8, 27), valid.get());
        assertTrue(invalid.isEmpty());
    }

    @Test
    void stringAndPathHelpers_coverEscapingAndDefaults() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        assertEquals("&lt;a&gt;&amp;&#39;&quot;", invoke(servlet, "escapeHtml", new Class<?>[]{String.class}, "<a>&'\""));
        assertEquals("a\\\\b\\'c\\n", invoke(servlet, "escapeForJs", new Class<?>[]{String.class}, "a\\b'c\n"));
        assertEquals("", invoke(servlet, "safe", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("x", invoke(servlet, "safe", new Class<?>[]{String.class}, "x"));
        assertEquals("/dashboard/new-users", invoke(servlet, "normalizeServletPath", new Class<?>[]{String.class}, "/unknown"));
        assertEquals("/dashboard/new-users/day-data", invoke(servlet, "normalizeServletPath", new Class<?>[]{String.class}, "/dashboard/new-users/day-data"));
    }

    @Test
    void resolveRequestPath_defaultsWhenRequestOrMappingMissing() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        assertEquals("/dashboard/new-users", invoke(servlet, "resolveRequestPath", new Class<?>[]{HttpServletRequest.class}, new Object[]{null}));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHttpServletMapping()).thenReturn(null);
        assertEquals("/dashboard/new-users", invoke(servlet, "resolveRequestPath", new Class<?>[]{HttpServletRequest.class}, req));
    }

    @Test
    void staticSafeIntegerValue_handlesNullAndInvalidIntegerText() throws Exception {
        assertEquals(0, invokeStatic(DashboardNewUsersServlet.class, "safeIntegerValue", new Class<?>[]{Integer.class}, new Object[]{null}));
        assertEquals(12, invokeStatic(DashboardNewUsersServlet.class, "safeIntegerValue", new Class<?>[]{Integer.class}, Integer.valueOf(12)));
    }

    @Test
    void writePlainTextError_writesMessageToResponseWriter() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        invoke(servlet, "writePlainTextError", new Class<?>[]{HttpServletResponse.class, int.class, String.class},
                resp, HttpServletResponse.SC_BAD_REQUEST, "bad request");

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp).setContentType("text/plain;charset=UTF-8");
        assertTrue(out.toString().contains("bad request"));
    }

    @Test
    void loadTemplate_handlesMissingAndPresentResources() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();

        HttpServletRequest missingReq = mock(HttpServletRequest.class);
        ServletContext missingContext = mock(ServletContext.class);
        when(missingReq.getServletContext()).thenReturn(missingContext);
        when(missingContext.getResourceAsStream("/missing.html")).thenReturn(null);
        assertEquals("", invoke(servlet, "loadTemplate", new Class<?>[]{HttpServletRequest.class, String.class}, missingReq, "/missing.html"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext context = mock(ServletContext.class);
        when(req.getServletContext()).thenReturn(context);
        when(context.getResourceAsStream("/ok.html"))
                .thenReturn(new ByteArrayInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8)));

        String loaded = (String) invoke(servlet, "loadTemplate", new Class<?>[]{HttpServletRequest.class, String.class}, req, "/ok.html");
        assertTrue(loaded.contains("line1"));
        assertTrue(loaded.contains("line2"));
    }

    @Test
    void writeJson_andWriteJsonError_fallbackToSendError() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_OK), Mockito.any()))
                    .thenThrow(new java.io.IOException("io"));
            invoke(servlet, "writeJson", new Class<?>[]{HttpServletResponse.class, int.class, jakarta.json.JsonObject.class},
                    resp, HttpServletResponse.SC_OK, jakarta.json.Json.createObjectBuilder().add("status", "ok").build());
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }

        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        when(resp2.isCommitted()).thenReturn(false);
        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> ServletJsonResponseUtil.writeError(resp2, HttpServletResponse.SC_BAD_REQUEST, "bad"))
                    .thenThrow(new java.io.IOException("io"));
            invoke(servlet, "writeJsonError", new Class<?>[]{HttpServletResponse.class, int.class, String.class},
                    resp2, HttpServletResponse.SC_BAD_REQUEST, "bad");
            verify(resp2).sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");
        }
    }

    @Test
    void handleDay_withMissingDay_writesBadRequestError() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getParameterValues("day")).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            invoke(servlet, "handleDay", new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class}, req, resp);
            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid day."));
        }
    }

    @Test
    void metrics_incrementDay_andTrendJson_coverRangeLogic() throws Exception {
        Class<?> metricsClass = Class.forName("com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet$Metrics");
        Constructor<?> ctor = metricsClass.getDeclaredConstructor(LocalDate.class, LocalDate.class);
        ctor.setAccessible(true);
        Object metrics = ctor.newInstance(LocalDate.of(2026, 8, 25), LocalDate.of(2026, 8, 27));

        Method incrementDay = metricsClass.getDeclaredMethod("incrementDay", LocalDate.class);
        incrementDay.setAccessible(true);
        incrementDay.invoke(metrics, LocalDate.of(2026, 8, 25));
        incrementDay.invoke(metrics, LocalDate.of(2026, 8, 26));
        incrementDay.invoke(metrics, LocalDate.of(2026, 8, 30));

        Method toTrendJson = metricsClass.getDeclaredMethod("toTrendJson");
        toTrendJson.setAccessible(true);
        String trendJson = (String) toTrendJson.invoke(metrics);

        assertTrue(trendJson.contains("2026-08-25"));
        assertTrue(trendJson.contains("2026-08-27"));
        assertTrue(trendJson.contains("\"values\""));
    }

    @Test
    void getTotalChats_returnsSafeDefaults() throws Exception {
        DashboardNewUsersServlet servlet = new DashboardNewUsersServlet();
        java.util.Map<String, Integer> withNull = new java.util.HashMap<>();
        withNull.put("s1", null);
        assertEquals(0, invoke(servlet, "getTotalChats", new Class<?>[]{java.util.Map.class, String.class}, null, "s1"));
        assertEquals(0, invoke(servlet, "getTotalChats", new Class<?>[]{java.util.Map.class, String.class}, withNull, "s1"));
        assertEquals(3, invoke(servlet, "getTotalChats", new Class<?>[]{java.util.Map.class, String.class}, java.util.Map.of("s1", 3), "s1"));
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object invokeStatic(Class<?> type, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}
