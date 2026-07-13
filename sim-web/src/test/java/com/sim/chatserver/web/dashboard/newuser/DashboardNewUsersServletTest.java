package com.sim.chatserver.web.dashboard.newuser;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardNewUsersServlet
 *
 * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet
 * @author bmcmullin
 */
public class DashboardNewUsersServletTest
{

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

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

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
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

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
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

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
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

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
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet13() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = "/data"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet14() throws Throwable
    {
        // Given
        DashboardNewUsersServlet underTest = new DashboardNewUsersServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = ""; // UTA: configured value
        String getServletPathResult2 = ""; // UTA: configured value
        String getServletPathResult3 = "/day"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult, getServletPathResult2, getServletPathResult3);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);

        doThrow(IOException.class).when(resp).sendRedirect(nullable(String.class));
        underTest.doGet(req, resp);

    }

}
