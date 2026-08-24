package com.sim.chatserver.web.dashboard.drilldown;

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

class WidgetReviewManualMessageServletTest {

    @Test
    void doPost_withoutSession_returnsUnauthorizedJson() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void doPost_withInvalidPayloadLength_returnsBadRequestJson() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Invalid JSON payload"));
    }
}
