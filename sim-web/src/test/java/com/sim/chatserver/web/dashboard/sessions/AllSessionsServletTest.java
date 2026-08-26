package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AllSessionsServletTest {

    @Test
    void doGet_withoutSession_returnsUnauthorizedJson() throws Exception {
        AllSessionsServlet servlet = new AllSessionsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/sessions/data");
        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void doPost_onNonSelectPath_returnsMethodNotAllowedJson() throws Exception {
        AllSessionsServlet servlet = new AllSessionsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/sessions/data");
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        assertTrue(out.toString().contains("Method not allowed"));
    }

    @Test
    void doGet_onChatsPath_withoutSession_returnsUnauthorizedJson() throws Exception {
        AllSessionsServlet servlet = new AllSessionsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/sessions/chats");
        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void doPost_onSelectPath_withoutSession_returnsUnauthorizedJson() throws Exception {
        AllSessionsServlet servlet = new AllSessionsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpServletMapping mapping = mock(HttpServletMapping.class);
        StringWriter out = new StringWriter();

        when(req.getHttpServletMapping()).thenReturn(mapping);
        when(mapping.getPattern()).thenReturn("/dashboard/sessions/select");
        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }
}
