package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
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
}
