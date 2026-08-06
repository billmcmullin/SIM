package com.sim.chatserver.web.dashboard.newuser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardNewUsersDrilldownServlet
 *
 * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet
 * @author bmcmullin
 */
public class DashboardNewUsersDrilldownServletTest
{
    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDownCdiMock()
    {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    private void mockDataSourceHolderCdi(AppDataSourceHolder dsHolderValue)
    {
        if (cdiMock != null) {
            cdiMock.close();
        }
        cdiMock = org.mockito.Mockito.mockStatic(CDI.class);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> dsHolderInstance = mock(Instance.class);

        when(cdi.select(AppDataSourceHolder.class)).thenReturn(dsHolderInstance);
        when(dsHolderInstance.get()).thenReturn(dsHolderValue);
        cdiMock.when(CDI::current).thenReturn(cdi);
    }


    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardNewUsersDrilldownServlet underTest = new DashboardNewUsersDrilldownServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardNewUsersDrilldownServlet underTest = new DashboardNewUsersDrilldownServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DashboardNewUsersDrilldownServlet underTest = new DashboardNewUsersDrilldownServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getDataSource()).thenThrow(IllegalStateException.class);
        mockDataSourceHolderCdi(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        assertThrows(IllegalStateException.class, () -> {
            underTest.doGet(req, resp);
        });

    }

}
