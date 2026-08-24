package com.sim.chatserver.web.dashboard.trends;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardTrendsServlet
 *
 * @see com.sim.chatserver.web.dashboard.trends.DashboardTrendsServlet
 * @author bmcmullin
 */
public class DashboardTrendsServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.trends.DashboardTrendsServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardTrendsServlet underTest = new DashboardTrendsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
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
     * @see com.sim.chatserver.web.dashboard.trends.DashboardTrendsServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardTrendsServlet underTest = new DashboardTrendsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
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
     * @see com.sim.chatserver.web.dashboard.trends.DashboardTrendsServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DashboardTrendsServlet underTest = new DashboardTrendsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getDataSource()).thenThrow(IllegalStateException.class);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    private DashboardTrendsServlet servletWithDataSourceHolder(AppDataSourceHolder dsHolder) {
        return new DashboardTrendsServlet() {
            protected AppDataSourceHolder dataSourceHolder() {
                return dsHolder;
            }
        };
    }
}

