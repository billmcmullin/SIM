package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WidgetSyncServlet
 *
 * @see com.sim.chatserver.web.admin.WidgetSyncServlet
 * @author bmcmullin
 */
public class WidgetSyncServletTest
{

    /**
     * Parasoft Jtest UTA: Test for destroy()
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#destroy()
     * @author bmcmullin
     */
    @Test
    public void testDestroy() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        underTest.destroy();

    }

    /**
     * Parasoft Jtest UTA: Test for destroy()
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#destroy()
     * @author bmcmullin
     */
    @Test
    public void testDestroy2() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        underTest.destroy();

    }

    /**
     * Parasoft Jtest UTA: Helper method to set private field scheduledFuture
     */
    private static <T> void setPrivateField(Object object, Class<?> fieldClass, String fieldName, T value)
    {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException e) {
            throw (AssertionError) new AssertionError("No such field found").initCause(e);
        } catch (IllegalAccessException e) {
            throw (AssertionError) new AssertionError("Unable to access the specified private field").initCause(e);
        } catch (SecurityException e) {
            throw (AssertionError) new AssertionError("There was a security exception when attempting to access a private field").initCause(e);
        }
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = ""; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

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
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

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
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = "/timer"; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

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
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        String getRequestURIResult = null; // UTA: configured value
        when(req.getRequestURI()).thenReturn(getRequestURIResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        Object getAttributeResult3 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2, getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        long getLongResult = 0; // UTA: configured value
        when(executeQueryResult.getLong(anyInt())).thenReturn(getLongResult);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit2() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        long getLongResult = 1; // UTA: configured value
        when(executeQueryResult.getLong(anyInt())).thenReturn(getLongResult);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit3() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        long getLongResult = 0; // UTA: configured value
        when(executeQueryResult.getLong(anyInt())).thenReturn(getLongResult);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit4() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        long getLongResult = 1; // UTA: configured value
        when(executeQueryResult.getLong(anyInt())).thenReturn(getLongResult);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit5() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit6() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getDataSource()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit7() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        doThrow(SQLException.class).when(createStatementResult).close();
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit8() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(createStatementResult.execute(nullable(String.class))).thenThrow(SQLException.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit9() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.next()).thenThrow(SQLException.class);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit10() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getLong(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit11() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getTimestamp(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit12() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getTimestamp(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit13() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit14() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit15() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit16() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit17() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getTimestamp(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit18() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        when(executeQueryResult.getTimestamp(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit19() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.getTimestamp(anyInt())).thenThrow(SQLException.class);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit20() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit21() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit22() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit23() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }

    /**
     * Parasoft Jtest UTA: Test for init(ServletConfig)
     *
     * @see com.sim.chatserver.web.admin.WidgetSyncServlet#init(ServletConfig)
     * @author bmcmullin
     */
    @Test
    public void testInit24() throws Throwable
    {
        // Given
        WidgetSyncServlet underTest = new WidgetSyncServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        Statement createStatementResult = mock(Statement.class);
        when(getConnectionResult.createStatement()).thenReturn(createStatementResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;
        ScheduledFuture scheduledFutureValue = mock(ScheduledFuture.class);
        setPrivateField(underTest, WidgetSyncServlet.class, "scheduledFuture", scheduledFutureValue);

        // When
        ServletConfig config = mock(ServletConfig.class);
        String getInitParameterResult = "getInitParameterResult"; // UTA: default value
        when(config.getInitParameter(nullable(String.class))).thenReturn(getInitParameterResult);
        underTest.init(config);

    }
}
