package com.sim.chatserver.web.dashboard.newuser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
class DashboardNewUsersServletTest
{

    private DashboardNewUsersServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private ServletContext servletContext;

    private AppDataSourceHolder dsHolder;
    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception
    {
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
    void unauthenticatedJsonRequestReturns401() throws Exception
    {
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
    void unauthenticatedPageRedirectsToLogin() throws Exception
    {
        when(req.getSession(false)).thenReturn(null);
        when(req.getServletPath()).thenReturn("/dashboard/new-users");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/chat-server/login");
    }

    @Test
    void dayEndpointBadInputReturns400() throws Exception
    {
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

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String getServletPathResult = "/data"; // UTA: configured value
        when(req2.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp2.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getServletPathResult = "getServletPathResult"; // UTA: default value
        when(req2.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        underTest.doGet(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req2.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        boolean isCommittedResult = true; // UTA: configured value
        when(resp2.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp2).sendRedirect(nullable(String.class));
        underTest.doGet(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = ""; // UTA: configured value
        when(req2.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp2.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp2.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp2).sendRedirect(nullable(String.class));
        underTest.doGet(req2, resp2);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = "/data"; // UTA: configured value
        when(req2.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp2.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp2.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp2).sendRedirect(nullable(String.class));
        underTest.doGet(req2, resp2);

    }

}
