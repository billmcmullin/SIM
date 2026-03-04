package com.sim.chatserver.web.dashboard;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Parasoft Jtest UTA: Test class for AllSessionsServlet
 *
 * @see com.sim.chatserver.web.dashboard.AllSessionsServlet
 */
public class AllSessionsServletTest {

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest,
     * HttpServletResponse)
     *
     * @see
     * com.sim.chatserver.web.dashboard.AllSessionsServlet#doGet(HttpServletRequest,
     * HttpServletResponse)
     */
    @Test
    public void testDoGet() throws Throwable {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        // Ensure getWriter() returns a real PrintWriter to avoid NPE in servlet error paths
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        underTest.doGet(req, resp);
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest,
     * HttpServletResponse)
     *
     * @see
     * com.sim.chatserver.web.dashboard.AllSessionsServlet#doGet(HttpServletRequest,
     * HttpServletResponse)
     */
    @Test
    public void testDoGet2() throws Throwable {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        // Ensure getWriter() returns a real PrintWriter to avoid NPE in servlet error paths
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        underTest.doGet(req, resp);
    }
}
