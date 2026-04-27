package com.sim.chatserver.web.dashboard.newuser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
class DashboardNewUsersDrilldownServletTest
{

    private DashboardNewUsersDrilldownServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private ServletContext servletContext;

    @BeforeEach
    void setUp() throws Exception
    {
        servlet = new DashboardNewUsersDrilldownServlet();

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);

        when(req.getServletContext()).thenReturn(servletContext);
        when(req.getContextPath()).thenReturn("/chat-server");

        // inject dsHolder with mock datasource/connection
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(holder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Field f = DashboardNewUsersDrilldownServlet.class.getDeclaredField("dsHolder");
        f.setAccessible(true);
        f.set(servlet, holder);
    }

    @Test
    void redirectsToLoginWhenNotAuthenticated() throws Exception
    {
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/chat-server/login");
    }

    @Test
    void parsePositiveIntWorks() throws Exception
    {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("parsePositiveInt", String.class);
        m.setAccessible(true);

        assertEquals(Optional.of(10), m.invoke(servlet, "10"));
        assertEquals(Optional.empty(), m.invoke(servlet, "0"));
        assertEquals(Optional.empty(), m.invoke(servlet, "-1"));
        assertEquals(Optional.empty(), m.invoke(servlet, "abc"));
        assertEquals(Optional.empty(), m.invoke(servlet, ""));
    }

    @Test
    void parseDateWorks() throws Exception
    {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("parseDate", String.class);
        m.setAccessible(true);

        assertEquals(Optional.of(LocalDate.of(2026, 3, 25)), m.invoke(servlet, "2026-03-25"));
        assertEquals(Optional.empty(), m.invoke(servlet, "03/25/2026"));
        assertEquals(Optional.empty(), m.invoke(servlet, "bad"));
    }

    @Test
    void sanitizeWidgetTableNameNormalizes() throws Exception
    {
        Method m = DashboardNewUsersDrilldownServlet.class.getDeclaredMethod("sanitizeWidgetTableName", String.class);
        m.setAccessible(true);

        String result = (String) m.invoke(servlet, "123 bad-id");
        assertTrue(result.startsWith("w_"));
        assertFalse(result.contains("-"));
        assertFalse(result.contains(" "));
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
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getContextPathResult = null; // UTA: configured value
        when(req2.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = mock(InputStream.class);
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req2.getServletContext()).thenReturn(getServletContextResult);

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
     * @see com.sim.chatserver.web.dashboard.newuser.DashboardNewUsersDrilldownServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardNewUsersDrilldownServlet underTest = new DashboardNewUsersDrilldownServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getDataSource()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req2.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req2.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        assertThrows(ServletException.class, () -> {
            underTest.doGet(req2, resp2);
        });

    }

}
