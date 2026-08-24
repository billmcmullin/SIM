package com.sim.chatserver.web.dashboard.widgets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class WidgetTableSelectIdsServletTest {

    @Test
    void doGet_withoutSession_returnsUnauthorized() throws Exception {
        WidgetTableSelectIdsServlet servlet = new WidgetTableSelectIdsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doGet_missingWidgetId_returnsBadRequest() throws Exception {
        WidgetTableSelectIdsServlet servlet = new WidgetTableSelectIdsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getParameterValues("widgetId")).thenReturn(null);
        when(req.getParameter("widgetId")).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
    }

    @Test
    void doGet_invalidDate_returnsBadRequestJson() throws Exception {
        WidgetTableSelectIdsServlet servlet = new WidgetTableSelectIdsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");

        when(req.getParameterValues("widgetId")).thenReturn(new String[]{"widget_1"});
        when(req.getParameter("widgetId")).thenReturn("widget_1");

        when(req.getParameterValues("date")).thenReturn(new String[]{"not-a-date"});
        when(req.getParameter("date")).thenReturn("not-a-date");
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Invalid date"));
    }
}
