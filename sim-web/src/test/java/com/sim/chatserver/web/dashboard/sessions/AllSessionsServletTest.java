package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AllSessionsServlet
 *
 * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet
 * @author bmcmullin
 */
public class AllSessionsServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
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
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/chats"; // UTA: configured value
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
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = null; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = ""; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost9() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost10() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.sessions.AllSessionsServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost11() throws Throwable
    {
        // Given
        AllSessionsServlet underTest = new AllSessionsServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getServletPathResult = "/select"; // UTA: configured value
        when(req.getServletPath()).thenReturn(getServletPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

}
