package com.sim.chatserver.web.dashboard.topics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardTopicsServletTest {

    @Test
    void doGet_unauthenticated_forwardsToLogin() throws Exception {
        DashboardTopicsServlet servlet = new DashboardTopicsServlet();
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
    void doGet_authenticated_rendersEscapedTemplate() throws Exception {
        DashboardTopicsServlet servlet = new DashboardTopicsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getContextPath()).thenReturn("/ctx/");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("u<name>&\"");
        when(req.getServletContext()).thenReturn(context);
        when(context.getResourceAsStream("/WEB-INF/views/dashboard_topics.html"))
                .thenReturn(new ByteArrayInputStream(
                        "${contextPath}|${user}|${globalTopicRows}|${perWidgetTopicTables}".getBytes(StandardCharsets.UTF_8)));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        String body = out.toString(StandardCharsets.UTF_8);
        verify(resp).setContentType("text/html; charset=UTF-8");
        assertTrue(body.contains("/ctx"));
        assertTrue(body.contains("u&lt;name&gt;&amp;&quot;"));
        assertTrue(body.endsWith("||"));
    }

    @Test
    void doGet_exceptionPath_sendsInternalServerError() throws Exception {
        DashboardTopicsServlet servlet = new DashboardTopicsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void loadTemplate_escapeHtml_andValidateCanonicalizedRenderedTemplate() throws Exception {
        DashboardTopicsServlet servlet = new DashboardTopicsServlet();

        assertEquals("", invoke(servlet, "loadTemplate", new Class<?>[] {ServletContext.class, String.class}, null, "/x"));

        ServletContext missing = mock(ServletContext.class);
        when(missing.getResourceAsStream("/missing")).thenReturn(null);
        assertEquals("", invoke(servlet, "loadTemplate", new Class<?>[] {ServletContext.class, String.class}, missing, "/missing"));

        ServletContext broken = mock(ServletContext.class);
        when(broken.getResourceAsStream("/broken")).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("io");
            }
        });
        assertEquals("", invoke(servlet, "loadTemplate", new Class<?>[] {ServletContext.class, String.class}, broken, "/broken"));

        assertEquals("&lt;x&gt;&amp;&quot;&#39;", invoke(servlet, "escapeHtml", new Class<?>[] {String.class}, "<x>&\"'"));
        assertEquals("", invoke(servlet, "escapeHtml", new Class<?>[] {String.class}, new Object[] {null}));
        assertEquals("", invoke(servlet, "validateCanonicalizedRenderedTemplate", new Class<?>[] {String.class}, new Object[] {null}));
        assertEquals("abc\n\t", invoke(servlet, "validateCanonicalizedRenderedTemplate", new Class<?>[] {String.class}, "abc\u0001\n\t"));
    }

    @Test
    void sendErrorSafe_noopsWhenCommittedOrNull() throws Exception {
        DashboardTopicsServlet servlet = new DashboardTopicsServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(true);

        invoke(servlet, "sendErrorSafe", new Class<?>[] {HttpServletResponse.class, int.class, String.class},
                resp, HttpServletResponse.SC_BAD_REQUEST, "bad");
        verify(resp, never()).sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");

        invoke(servlet, "sendErrorSafe", new Class<?>[] {HttpServletResponse.class, int.class, String.class},
                null, HttpServletResponse.SC_BAD_REQUEST, "bad");
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
                // no-op for unit tests
            }

            @Override
            public void write(int b) {
                out.write(b);
            }
        };
    }
}
