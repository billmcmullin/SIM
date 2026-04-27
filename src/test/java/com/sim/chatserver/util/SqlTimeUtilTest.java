package com.sim.chatserver.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SqlTimeUtil
 *
 * @see com.sim.chatserver.util.SqlTimeUtil
 * @author bmcmullin
 */
public class SqlTimeUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for safeTimestamp(ResultSet, String)
     *
     * @see com.sim.chatserver.util.SqlTimeUtil#safeTimestamp(ResultSet, String)
     * @author bmcmullin
     */
    @Test
    public void testSafeTimestamp() throws Throwable
    {
        // When
        ResultSet rs = mock(ResultSet.class);
        String column = "column"; // UTA: default value
        Timestamp result = SqlTimeUtil.safeTimestamp(rs, column);

        // Then - assertions for result of method safeTimestamp(ResultSet, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for safeTimestamp(ResultSet, String)
     *
     * @see com.sim.chatserver.util.SqlTimeUtil#safeTimestamp(ResultSet, String)
     * @author bmcmullin
     */
    @Test
    public void testSafeTimestamp2() throws Throwable
    {
        // When
        ResultSet rs = mock(ResultSet.class);
        String getStringResult = null; // UTA: configured value
        when(rs.getString(nullable(String.class))).thenReturn(getStringResult);

        when(rs.getTimestamp(nullable(String.class))).thenThrow(SQLException.class);
        String column = "column"; // UTA: default value
        Timestamp result = SqlTimeUtil.safeTimestamp(rs, column);

        // Then - assertions for result of method safeTimestamp(ResultSet, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for safeTimestamp(ResultSet, String)
     *
     * @see com.sim.chatserver.util.SqlTimeUtil#safeTimestamp(ResultSet, String)
     * @author bmcmullin
     */
    @Test
    public void testSafeTimestamp3() throws Throwable
    {
        // When
        ResultSet rs = mock(ResultSet.class);
        String getStringResult = "getStringResult"; // UTA: configured value
        when(rs.getString(nullable(String.class))).thenReturn(getStringResult);

        when(rs.getTimestamp(nullable(String.class))).thenThrow(SQLException.class);
        String column = "column"; // UTA: default value
        Timestamp result = SqlTimeUtil.safeTimestamp(rs, column);

    }
}
