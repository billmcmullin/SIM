package com.sim.chatserver.web.dashboard.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class InactiveUsersListPageServletTest {

    @Test
    void doGet_unauthenticated_forwardsToLogin() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_nullRequest_throwsNullPointerException() {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        assertThrows(NullPointerException.class, () -> servlet.doGet(null, resp));
    }

    @Test
    void detectFrustration_scoresStrongSignals() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();

        @SuppressWarnings("unchecked")
        InactiveUsersListPageServlet.FrustrationResult result =
                (InactiveUsersListPageServlet.FrustrationResult) invoke(servlet, "detectFrustration", new Class<?>[]{List.class},
                        List.of("This is terrible!!!", "you don't understand"));

        assertTrue(result.detected);
        assertTrue(result.score >= 0.4);
        assertFalse(result.reason.isBlank());
    }

    @Test
    void helperMethods_coverSanitizationAndFormatting() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();

        assertEquals("widget_1", invoke(servlet, "sanitizeWidgetId", new Class<?>[]{String.class}, " widget_1 "));
        assertEquals("", invoke(servlet, "sanitizeWidgetId", new Class<?>[]{String.class}, "drop table;"));

        String longSearch = "x".repeat(200);
        String sanitized = (String) invoke(servlet, "sanitizeSearch", new Class<?>[]{String.class}, longSearch);
        assertEquals(128, sanitized.length());

        assertEquals(9, invoke(servlet, "parseInt", new Class<?>[]{String.class, int.class}, "9", 3));
        assertEquals(3, invoke(servlet, "parseInt", new Class<?>[]{String.class, int.class}, "abc", 3));

        assertEquals("", invoke(servlet, "nvl", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("v", invoke(servlet, "nvl", new Class<?>[]{String.class}, "v"));

        String escaped = (String) invoke(servlet, "escapeHtml", new Class<?>[]{String.class}, "<x>&'\"");
        assertEquals("&lt;x&gt;&amp;&#39;&quot;", escaped);

        String js = (String) invoke(servlet, "escapeForJs", new Class<?>[]{String.class}, "a\\b'c\n");
        assertEquals("a\\\\b\\'c\\n", js);

        assertEquals("", invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, new Object[]{null}));
        Timestamp ts = Timestamp.from(Instant.parse("2026-08-27T10:00:00Z"));
        assertTrue(((String) invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, ts)).contains("2026-08-27T10:00:00Z"));
    }

    @Test
    void safeSessionUser_andTemplateSanitization_coverEdgeCases() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();

        HttpSession nonStringUser = mock(HttpSession.class);
        when(nonStringUser.getAttribute("user")).thenReturn(Integer.valueOf(3));
        assertEquals("", invoke(servlet, "safeSessionUser", new Class<?>[]{HttpSession.class}, nonStringUser));

        HttpSession textUser = mock(HttpSession.class);
        when(textUser.getAttribute("user")).thenReturn(" user\r\nname ");
        assertEquals("username", invoke(servlet, "safeSessionUser", new Class<?>[]{HttpSession.class}, textUser));

        assertEquals(null, invoke(servlet, "sanitizeTemplate", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("ab", invoke(servlet, "sanitizeTemplate", new Class<?>[]{String.class}, "a\u0000b\r"));

        ServletContext context = mock(ServletContext.class);
        when(context.getResourceAsStream("/missing")).thenReturn(null);
        assertEquals("", invoke(servlet, "loadTemplate", new Class<?>[]{ServletContext.class, String.class}, context, "/missing"));
    }

    @Test
    void buildJson_serializesRows() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();
        InactiveUsersListPageServlet.Row row = new InactiveUsersListPageServlet.Row();
        row.sessionId = "s-1";
        row.displayLabel = "User One";
        row.widgetId = "w1";
        row.widgetLabel = "Widget One";
        row.chatCount = 4;
        row.lastEntry = Timestamp.from(Instant.parse("2026-08-27T10:00:00Z"));
        row.frustrationDetected = true;
        row.frustrationScore = 0.9;
        row.frustrationReason = "keyword:terrible";

        String json = (String) invoke(servlet, "buildJson", new Class<?>[]{List.class}, List.of(row));
        assertTrue(json.contains("\"sessionId\":\"s-1\""));
        assertTrue(json.contains("\"displayLabel\":\"User One\""));
        assertTrue(json.contains("\"chatCount\":4"));
        assertTrue(json.contains("\"frustrationDetected\":true"));
    }

    @Test
    void sendErrorSafe_skipsCommittedResponses() throws Exception {
        InactiveUsersListPageServlet servlet = new InactiveUsersListPageServlet();
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
