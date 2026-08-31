package com.sim.chatserver.web.dashboard.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class WidgetTableViewServletTest {

    @Test
    void doGet_unauthenticated_forwardsToLogin() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_missingWidgetId_returnsBadRequest() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("widgetId")).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
    }

    @Test
    void doGet_invalidWidgetIdFormat_returnsBadRequest() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("widgetId")).thenReturn(new String[] { "bad id" });

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId format.");
    }

    @Test
    void doGet_invalidDate_returnsBadRequest() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("widgetId")).thenReturn(new String[] { "widget-1" });
        when(req.getParameterValues("date")).thenReturn(new String[] { "not-a-date" });

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Expected YYYY-MM-DD.");
    }

    @Test
    void doGet_missingTemplate_returnsInternalServerError() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("widgetId")).thenReturn(new String[] { "widget-1" });
        when(req.getParameterValues("date")).thenReturn(null);
        when(req.getServletContext()).thenReturn(context);
        when(context.getResourceAsStream("/WEB-INF/views/widget_table_view.html")).thenReturn(null);

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());
            servlet.doGet(req, resp);
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template not found: /WEB-INF/views/widget_table_view.html");
    }

    @Test
    void doGet_success_rendersTemplate() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(" user<1> ");
        when(session.getAttribute("role")).thenReturn(" ADMIN ");
        when(req.getParameterValues("widgetId")).thenReturn(new String[] { "widget-1" });
        when(req.getParameterValues("date")).thenReturn(new String[] { "2020-01-02" });
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getServletContext()).thenReturn(context);
        when(context.getResourceAsStream("/WEB-INF/views/widget_table_view.html"))
                .thenReturn(new ByteArrayInputStream("${user}|${role}|${contextPath}|${widgetId}|${widgetName}|${selectedDate}|${selectedDateLabel}".getBytes(StandardCharsets.UTF_8)));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        WidgetEntry widget = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "widget-1", "Widget Display", Instant.parse("2026-08-27T00:00:00Z"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            servlet.doGet(req, resp);
        }

        String body = out.toString(StandardCharsets.UTF_8);
        verify(resp).setContentType("text/html; charset=UTF-8");
        assertTrue(body.contains("user&lt;1&gt;"));
        assertTrue(body.contains("ADMIN"));
        assertTrue(body.contains("/ctx"));
        assertTrue(body.contains("widget-1"));
        assertTrue(body.contains("Widget Display"));
        assertTrue(body.contains("2020-01-02"));
    }

    @Test
    void doGet_unhandledException_sendsFallbackError() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void helperMethods_loadTemplate_safeSessionAttribute_sanitizeForLog() throws Exception {
        WidgetTableViewServlet servlet = new WidgetTableViewServlet();

        ServletContext context = mock(ServletContext.class);
        when(context.getResourceAsStream("/ok")).thenReturn(new ByteArrayInputStream("a\nb".getBytes(StandardCharsets.UTF_8)));
        String loaded = (String) invoke(servlet, "loadTemplate", new Class<?>[] {ServletContext.class, String.class}, context, "/ok");
        assertTrue(loaded.contains("a"));
        assertTrue(loaded.contains("b"));

        ServletContext broken = mock(ServletContext.class);
        when(broken.getResourceAsStream("/broken")).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("io");
            }
        });
        assertEquals(null, invoke(servlet, "loadTemplate", new Class<?>[] {ServletContext.class, String.class}, broken, "/broken"));

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(" user ");
        assertEquals("user", invoke(servlet, "safeSessionAttribute", new Class<?>[] {HttpSession.class, String.class, String.class}, session, "user", "x"));
        assertEquals("fallback", invoke(servlet, "safeSessionAttribute", new Class<?>[] {HttpSession.class, String.class, String.class}, session, "", "fallback"));
        assertEquals("", invoke(servlet, "safeSessionAttribute", new Class<?>[] {HttpSession.class, String.class, String.class}, null, "user", null));

        assertEquals("", invoke(servlet, "sanitizeForLog", new Class<?>[] {String.class}, new Object[] {null}));
        assertEquals("a_b", invoke(servlet, "sanitizeForLog", new Class<?>[] {String.class}, "a\nb"));
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // no-op for tests
            }

            @Override
            public void write(int b) {
                out.write(b);
            }
        };
    }
}
