package com.sim.chatserver.web.dashboard.drilldown;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardDateSelectionServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet
 * @author bmcmullin
 */
public class DashboardDateSelectionServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.DashboardDateSelectionServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        DashboardDateSelectionServlet underTest = new DashboardDateSelectionServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }
}
