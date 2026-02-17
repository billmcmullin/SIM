package com.sim.chatserver.web.dashboard.drilldown;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewManualMessageServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet
 * @author bmcmullin
 */
public class WidgetReviewManualMessageServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        when(getConnectionResult2.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        PreparedStatement prepareStatementResult3 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult3.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult3);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        String getStringResult = "getStringResult"; // UTA: default value
        String getStringResult2 = "getStringResult2"; // UTA: default value
        String getStringResult3 = "getStringResult3"; // UTA: default value
        String getStringResult4 = null; // UTA: configured value
        when(executeQueryResult.getString(nullable(String.class))).thenReturn(getStringResult, getStringResult2, getStringResult3, getStringResult4);

        boolean nextResult2 = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getDataSource()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost9() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(prepareStatementResult.execute()).thenThrow(SQLException.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost10() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost11() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getConnectionResult.getMetaData()).thenThrow(SQLException.class);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost12() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        when(getColumnsResult.next()).thenThrow(SQLException.class);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost13() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost14() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(getColumnsResult).close();

        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost15() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        when(getColumnsResult.next()).thenThrow(SQLException.class);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.next()).thenThrow(SQLException.class);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost16() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult3).close();

        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost17() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost18() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        DataSource getDataSourceResult2 = mock(DataSource.class);
        Connection getConnectionResult2 = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getColumnsResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getColumnsResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(getColumnsResult);
        when(getConnectionResult2.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult2.getConnection()).thenReturn(getConnectionResult2);
        DataSource getDataSourceResult3 = mock(DataSource.class);
        Connection getConnectionResult3 = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult3).close();

        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult2).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult3.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult2);
        when(getDataSourceResult3.getConnection()).thenReturn(getConnectionResult3);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult, getDataSourceResult2, getDataSourceResult3);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * Parasoft Jtest UTA: Test for init()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#init()
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        underTest.init();

    }
}
