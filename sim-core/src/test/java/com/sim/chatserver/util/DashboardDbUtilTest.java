package com.sim.chatserver.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardDbUtil
 *
 * @see com.sim.chatserver.util.DashboardDbUtil
 * @author bmcmullin
 */
public class DashboardDbUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for newRequestTableCache()
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#newRequestTableCache()
     * @author bmcmullin
     */
    @Test
    public void testNewRequestTableCache() throws Throwable
    {
        // When
        Map<String, Boolean> result = DashboardDbUtil.newRequestTableCache();

    }

    /**
     * Parasoft Jtest UTA: Test for quoteIdentifier(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#quoteIdentifier(String)
     * @author bmcmullin
     */
    @Test
    public void testQuoteIdentifier() throws Throwable
    {
        // When
        String identifier = null; // UTA: configured value
        String result = DashboardDbUtil.quoteIdentifier(identifier);

    }

    /**
     * Parasoft Jtest UTA: Test for quoteIdentifier(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#quoteIdentifier(String)
     * @author bmcmullin
     */
    @Test
    public void testQuoteIdentifier2() throws Throwable
    {
        // When
        String identifier = "identifier"; // UTA: default value
        String result = DashboardDbUtil.quoteIdentifier(identifier);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName2() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName3() throws Throwable
    {
        // When
        String widgetId = ""; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName4() throws Throwable
    {
        // When
        String widgetId = "A"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName5() throws Throwable
    {
        // When
        String widgetId = "B"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName6() throws Throwable
    {
        // When
        String widgetId = "@"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName7() throws Throwable
    {
        // When
        String widgetId = "C*"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName8() throws Throwable
    {
        // When
        String widgetId = "a*******************************"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName9() throws Throwable
    {
        // When
        String widgetId = "[*************************"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName10() throws Throwable
    {
        // When
        String widgetId = "_*****************************"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for sanitizeWidgetTableName(String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#sanitizeWidgetTableName(String)
     * @author bmcmullin
     */
    @Test
    public void testSanitizeWidgetTableName11() throws Throwable
    {
        // When
        String widgetId = "{*********************************************************"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExists(Connection, String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExists(Connection, String)
     * @author bmcmullin
     */
    @Test
    public void testTableExists() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        boolean result = DashboardDbUtil.tableExists(conn, tableName);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExists(Connection, String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExists(Connection, String)
     * @author bmcmullin
     */
    @Test
    public void testTableExists2() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        boolean result = DashboardDbUtil.tableExists(conn, tableName);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExists(Connection, String)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExists(Connection, String)
     * @author bmcmullin
     */
    @Test
    public void testTableExists3() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        ResultSet getTablesResult2 = mock(ResultSet.class);
        boolean nextResult2 = true; // UTA: configured value
        when(getTablesResult2.next()).thenReturn(nextResult2);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult, getTablesResult2);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        boolean result = DashboardDbUtil.tableExists(conn, tableName);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = false; // UTA: default value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached2() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = "getCatalogResult"; // UTA: default value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = null; // UTA: configured value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached3() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = null; // UTA: configured value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached4() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = null; // UTA: configured value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached5() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        ResultSet getTablesResult2 = mock(ResultSet.class);
        boolean nextResult2 = true; // UTA: configured value
        when(getTablesResult2.next()).thenReturn(nextResult2);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult, getTablesResult2);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = null; // UTA: configured value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached6() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = new HashMap<String, Boolean>(); // UTA: default value
        String key = "key"; // UTA: default value
        Boolean value = false; // UTA: default value
        requestCache.put(key, value);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

    /**
     * Parasoft Jtest UTA: Test for tableExistsCached(Connection, String, Map)
     *
     * @see com.sim.chatserver.util.DashboardDbUtil#tableExistsCached(Connection, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testTableExistsCached7() throws Throwable
    {
        // When
        Connection conn = mock(Connection.class);
        String getCatalogResult = "getCatalogResult"; // UTA: default value
        when(conn.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        when(conn.getMetaData()).thenReturn(getMetaDataResult);
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = mock(Map.class);
        Boolean getResult = null; // UTA: configured value
        when(requestCache.get(nullable(Object.class))).thenReturn(getResult);
        boolean result = DashboardDbUtil.tableExistsCached(conn, tableName, requestCache);

    }

}
