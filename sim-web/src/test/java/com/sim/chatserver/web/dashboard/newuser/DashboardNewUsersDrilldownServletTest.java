package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardNewUsersDrilldownServletTest {

    @Test
    void doGet_unauthenticated_forwardsToLogin() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_missingUser_forwardsToLogin() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(session.getAttribute("user")).thenReturn(null);
        when(req.getSession(false)).thenReturn(session);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void parsePositiveIntOrDefault_handlesInvalidValues() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();

        assertEquals(7, invoke(servlet, "parsePositiveIntOrDefault", new Class<?>[]{String.class, int.class}, "7", 10));
        assertEquals(10, invoke(servlet, "parsePositiveIntOrDefault", new Class<?>[]{String.class, int.class}, "0", 10));
        assertEquals(10, invoke(servlet, "parsePositiveIntOrDefault", new Class<?>[]{String.class, int.class}, "x", 10));
        assertEquals(10, invoke(servlet, "parsePositiveIntOrDefault", new Class<?>[]{String.class, int.class}, "  ", 10));
    }

    @Test
    void parseDateOrNull_returnsDateOnlyForIsoInput() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();

        assertEquals(LocalDate.of(2026, 8, 27), invoke(servlet, "parseDateOrNull", new Class<?>[]{String.class}, "2026-08-27"));
        assertNull(invoke(servlet, "parseDateOrNull", new Class<?>[]{String.class}, "08/27/2026"));
    }

    @Test
    void buildRowsJsonBase64_encodesExpectedJsonPayload() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();

        Class<?> rowType = Class.forName("com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet$Row");
        Constructor<?> ctor = rowType.getDeclaredConstructor(String.class, String.class, int.class, String.class);
        ctor.setAccessible(true);
        Object row = ctor.newInstance("User A", "2026-08-27 10:11:12", 3, "/u/1");

        String b64 = (String) invoke(servlet, "buildRowsJsonBase64", new Class<?>[]{List.class, int.class}, List.of(row), 5);
        String json = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);

        assertTrue(json.contains("\"rank\":6"));
        assertTrue(json.contains("\"display\":\"User A\""));
        assertTrue(json.contains("\"totalChats\":3"));
        assertTrue(json.contains("\"chatEntriesUrl\":\"/u/1\""));
    }

    @Test
    void loadTemplate_returnsEmptyWhenContextOrResourceMissing() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();
        HttpServletRequest reqWithoutContext = mock(HttpServletRequest.class);
        when(reqWithoutContext.getServletContext()).thenReturn(null);

        String first = (String) invoke(servlet, "loadTemplate", new Class<?>[]{HttpServletRequest.class, String.class}, reqWithoutContext, "/a.html");
        assertEquals("", first);

        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext context = mock(ServletContext.class);
        when(context.getResourceAsStream("/a.html")).thenReturn(null);
        when(req.getServletContext()).thenReturn(context);

        String second = (String) invoke(servlet, "loadTemplate", new Class<?>[]{HttpServletRequest.class, String.class}, req, "/a.html");
        assertEquals("", second);
    }

    @Test
    void helperMethods_coverSanitizationAndEscaping() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();

        String sanitized = (String) invoke(servlet, "sanitizeForLog", new Class<?>[]{String.class}, "line1\r\nline2");
        assertEquals("line1__line2", sanitized);

        String escaped = (String) invoke(servlet, "escapeHtml", new Class<?>[]{String.class}, "<x>&'\"");
        assertEquals("&lt;x&gt;&amp;&#39;&quot;", escaped);

        assertEquals("", invoke(servlet, "safeJsonText", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("ok", invoke(servlet, "safeJsonText", new Class<?>[]{String.class}, "ok"));
        assertEquals(0, invoke(servlet, "safeInt", new Class<?>[]{Integer.class}, new Object[]{null}));
        assertEquals(9, invoke(servlet, "safeInt", new Class<?>[]{Integer.class}, Integer.valueOf(9)));
        assertNotNull(invoke(servlet, "urlEncode", new Class<?>[]{String.class}, "a b"));
    }

    @Test
    void sendErrorSafe_skipsCommittedResponse() throws Exception {
        DashboardNewUsersDrilldownServlet servlet = new DashboardNewUsersDrilldownServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(true);

        invoke(servlet, "sendErrorSafe", new Class<?>[]{HttpServletResponse.class, int.class, String.class},
                resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "x");

        verify(resp, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "x");
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
