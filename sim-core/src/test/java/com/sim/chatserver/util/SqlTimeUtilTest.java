package com.sim.chatserver.util;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void safeTimestamp_returnsTypedTimestampWhenAvailable() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        Timestamp ts = Timestamp.from(Instant.parse("2026-08-24T12:34:56Z"));
        when(rs.getTimestamp("created_at")).thenReturn(ts);

        Timestamp result = SqlTimeUtil.safeTimestamp(rs, "created_at");

        assertEquals(ts, result);
    }

    @Test
    void safeTimestamp_parsesIsoTextFromBytesWhenTypedReadFails() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("created_at")).thenThrow(new SQLException("typed read failed"));
        when(rs.getCharacterStream("created_at")).thenReturn(null);
        when(rs.getBytes("created_at")).thenReturn("2026-08-24T12:34:56Z".getBytes(StandardCharsets.UTF_8));

        Timestamp result = SqlTimeUtil.safeTimestamp(rs, "created_at");

        assertEquals(Timestamp.from(Instant.parse("2026-08-24T12:34:56Z")), result);
    }

    @Test
    void safeTimestamp_prefersCharacterStreamOverBytes() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("created_at")).thenThrow(new SQLException("typed read failed"));
        when(rs.getCharacterStream("created_at")).thenReturn(new StringReader("2026-08-24T12:34:56Z"));
        when(rs.getBytes("created_at")).thenReturn("2025-01-01T00:00:00Z".getBytes(StandardCharsets.UTF_8));

        Timestamp result = SqlTimeUtil.safeTimestamp(rs, "created_at");

        assertEquals(Timestamp.from(Instant.parse("2026-08-24T12:34:56Z")), result);
    }

    @Test
    void safeTimestamp_returnsNullForUnsafeText() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("created_at")).thenThrow(new SQLException("typed read failed"));
        when(rs.getCharacterStream("created_at")).thenReturn(null);
        when(rs.getBytes("created_at")).thenReturn("2026-08-24T12:34:56Z;DROP".getBytes(StandardCharsets.UTF_8));

        Timestamp result = SqlTimeUtil.safeTimestamp(rs, "created_at");

        assertNull(result);
    }

    @Test
    void privateParsers_coverNormalizationAndFallbackBranches() throws Exception {
        Method normalize = SqlTimeUtil.class.getDeclaredMethod("normalizeTimestampText", String.class);
        normalize.setAccessible(true);
        String normalized = (String) normalize.invoke(null, "2026-08-24 12:34:56Z");
        assertEquals("2026-08-24T12:34:56Z", normalized);

        Method parse = SqlTimeUtil.class.getDeclaredMethod("parseTimestampString", String.class);
        parse.setAccessible(true);
        Timestamp offsetParsed = (Timestamp) parse.invoke(null, "2026-08-24T12:34:56+02:00");
        assertNotNullTimestamp(offsetParsed);

        Timestamp invalid = (Timestamp) parse.invoke(null, "bad-value");
        assertNull(invalid);

        Method sanitize = SqlTimeUtil.class.getDeclaredMethod("sanitizeTimestampCandidate", String.class);
        sanitize.setAccessible(true);
        String unsafe = (String) sanitize.invoke(null, "2026-08-24T12:34:56Z<script>");
        assertEquals("", unsafe);

        String longSafe = "2".repeat(120);
        String trimmed = (String) sanitize.invoke(null, longSafe);
        assertTrue(trimmed.length() <= 96 || trimmed.isEmpty());
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

    private static void assertNotNullTimestamp(Timestamp ts) {
        assertTrue(ts != null);
    }

}
