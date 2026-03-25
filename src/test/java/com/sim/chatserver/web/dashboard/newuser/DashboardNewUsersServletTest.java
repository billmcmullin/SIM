package com.sim.chatserver.web.dashboard.newuser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardNewUsersServletTest {

    private DashboardNewUsersServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private ServletContext servletContext;

    private AppDataSourceHolder dsHolder;
    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardNewUsersServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);

        when(req.getServletContext()).thenReturn(servletContext);
        when(req.getContextPath()).thenReturn("/chat-server");

        dsHolder = mock(AppDataSourceHolder.class);
        ds = mock(DataSource.class);
        conn = mock(Connection.class);

        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Field f = DashboardNewUsersServlet.class.getDeclaredField("dsHolder");
        f.setAccessible(true);
        f.set(servlet, dsHolder);
    }

    @Test
    void unauthenticatedJsonRequestReturns401() throws Exception {
        when(req.getSession(false)).thenReturn(null);
        when(req.getServletPath()).thenReturn("/dashboard/new-users/data");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(resp).setContentType("application/json; charset=UTF-8");
        assertTrue(sw.toString().contains("\"status\":\"error\""));
        assertTrue(sw.toString().contains("Authentication required"));
    }

    @Test
    void unauthenticatedPageRedirectsToLogin() throws Exception {
        when(req.getSession(false)).thenReturn(null);
        when(req.getServletPath()).thenReturn("/dashboard/new-users");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/chat-server/login");
    }

    @Test
    void dayEndpointBadInputReturns400() throws Exception {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getServletPath()).thenReturn("/dashboard/new-users/day");
        when(req.getParameter("day")).thenReturn("bad-date");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp).setContentType("application/json; charset=UTF-8");
        assertTrue(sw.toString().contains("\"status\":\"error\""));
        assertTrue(sw.toString().contains("Missing or invalid day"));
    }

}
