package com.sim.chatserver.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        // Then - assertions for result of method newRequestTableCache()
        assertNotNull(result);
        assertEquals(0, result.size());

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

        // Then - assertions for result of method quoteIdentifier(String)
        assertEquals("", result);

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

        // Then - assertions for result of method quoteIdentifier(String)
        assertEquals("identifier", result);

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

        // Then - assertions for result of method sanitizeWidgetTableName(String)
        assertEquals("widget", result);

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

        // Then - assertions for result of method sanitizeWidgetTableName(String)
        assertEquals("widgetId", result);

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
        String widgetId = "@"; // UTA: configured value
        String result = DashboardDbUtil.sanitizeWidgetTableName(widgetId);

        // Then - assertions for result of method sanitizeWidgetTableName(String)
        assertEquals("w__", result);

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

        // Then - assertions for result of method tableExists(Connection, String)
        assertTrue(result);

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

        // Then - assertions for result of method tableExists(Connection, String)
        assertFalse(result);

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

        // Then - assertions for result of method tableExistsCached(Connection, String, Map)
        assertFalse(result);

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
        String tableName = "tableName"; // UTA: default value
        Map<String, Boolean> requestCache = null; // UTA: configured value
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

        // Then - assertions for result of method tableExistsCached(Connection, String, Map)
        assertTrue(result);

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

        // Then - assertions for result of method tableExistsCached(Connection, String, Map)
        assertTrue(result);

    }

}
