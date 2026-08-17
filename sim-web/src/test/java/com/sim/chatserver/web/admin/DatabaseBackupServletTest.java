package com.sim.chatserver.web.admin;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipOutputStream;
import jakarta.servlet.WriteListener;
/**
 * Parasoft Jtest UTA: Test class for DatabaseBackupServlet
 *
 * @see com.sim.chatserver.web.admin.DatabaseBackupServlet
 * @author bmcmullin
 */
public class DatabaseBackupServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);

        boolean isCommittedResult = true; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);

        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseBackupServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        boolean isCommittedResult = false; // UTA: configured value
        when(resp.isCommitted()).thenReturn(isCommittedResult);
        underTest.doGet(req, resp);

    }

    @Test
    public void testReadCellAsTextFormatsDateAsIsoLocalDate() throws Throwable
    {
        DatabaseBackupServlet underTest = new DatabaseBackupServlet();
        Method readCellAsText = DatabaseBackupServlet.class.getDeclaredMethod("readCellAsText", ResultSet.class, ResultSetMetaData.class, int.class);
        readCellAsText.setAccessible(true);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData md = mock(ResultSetMetaData.class);

        when(md.getColumnType(1)).thenReturn(Types.DATE);
        when(rs.getDate(1)).thenReturn(Date.valueOf("2026-05-20"));

        String value = (String) readCellAsText.invoke(underTest, rs, md, 1);

        assertEquals("2026-05-20", value);
    }


    // Merged from DatabaseBackupServletCoverageTest
    
    
        @Test
        void doGet_unauthorized_returns401() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            when(req.getSession(anyBoolean())).thenReturn(null);
    
            servlet.doGet(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
        }
    
        @Test
        void doGet_unauthorized_sendErrorThrows_isHandled() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            when(req.getSession(anyBoolean())).thenReturn(null);
            doThrow(new IOException("fail")).when(resp)
                    .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
    
            servlet.doGet(req, resp);
        }
    
        @Test
        void doGet_authenticatedNonAdmin_returns401() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpSession session = mock(HttpSession.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(req.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn("admin");
            when(session.getAttribute("role")).thenReturn("USER");
    
            servlet.doGet(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
        }
    
        @Test
        void doGet_outerCatch_whenSessionReadThrows_returnsFallback500() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
            when(resp.isCommitted()).thenReturn(false);
    
            servlet.doGet(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    
        @Test
        void privateHelpers_sanitizeQuoteJsonAndCsv() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
    
            assertNull(invoke(servlet, "sanitizeIdentifier", new Class[]{String.class}, (Object) null));
            assertNull(invoke(servlet, "sanitizeIdentifier", new Class[]{String.class}, "  "));
            assertNull(invoke(servlet, "sanitizeIdentifier", new Class[]{String.class}, "bad-name"));
            assertEquals("good_name", invoke(servlet, "sanitizeIdentifier", new Class[]{String.class}, " good_name "));
    
            assertNull(invoke(servlet, "sanitizeCellText", new Class[]{String.class}, (Object) null));
            assertEquals("a b", invoke(servlet, "sanitizeCellText", new Class[]{String.class}, "a\u0000b"));
            assertEquals("hello", invoke(servlet, "sanitizeCellText", new Class[]{String.class}, "hello\r"));
    
            byte[] shortBytes = new byte[]{1, 2, 3};
            assertArrayEquals(shortBytes, (byte[]) invoke(servlet, "sanitizeBinary", new Class[]{byte[].class}, (Object) shortBytes));
            assertArrayEquals(new byte[0], (byte[]) invoke(servlet, "sanitizeBinary", new Class[]{byte[].class}, new Object[]{null}));
    
            byte[] big = new byte[(2 * 1024 * 1024) + 10];
            byte[] truncated = (byte[]) invoke(servlet, "sanitizeBinary", new Class[]{byte[].class}, (Object) big);
            assertEquals(2 * 1024 * 1024, truncated.length);
    
            assertEquals("\"good_name\"", invoke(servlet, "quoteIdent", new Class[]{String.class}, "good_name"));
            assertThrowsCause(IllegalArgumentException.class,
                    () -> invoke(servlet, "quoteIdent", new Class[]{String.class}, "bad-name"));
    
            assertEquals("", invoke(servlet, "csvEscape", new Class[]{String.class}, new Object[]{null}));
            assertEquals("plain", invoke(servlet, "csvEscape", new Class[]{String.class}, "plain"));
            assertEquals("\"a,b\"", invoke(servlet, "csvEscape", new Class[]{String.class}, "a,b"));
            assertEquals("\"a\"\"b\"", invoke(servlet, "csvEscape", new Class[]{String.class}, "a\"b"));
    
            assertEquals("", invoke(servlet, "jsonEscape", new Class[]{String.class}, new Object[]{null}));
            assertEquals("a\\\\b\\\"c", invoke(servlet, "jsonEscape", new Class[]{String.class}, "a\\b\"c"));
            assertEquals("[\"a\", \"b\"]", invoke(servlet, "toJsonArray", new Class[]{List.class}, List.of("a", "b")));
        }
    
        @Test
        void privateHelpers_parseTimestampAndDate_coverFallbacks() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
    
            assertNull(invoke(servlet, "parseTimestamp", new Class[]{String.class}, (Object) null));
            assertNotNull(invoke(servlet, "parseTimestamp", new Class[]{String.class}, "2026-08-07T10:11:12Z"));
            assertNotNull(invoke(servlet, "parseTimestamp", new Class[]{String.class}, "2026-08-07T10:11:12+00:00"));
            assertNotNull(invoke(servlet, "parseTimestamp", new Class[]{String.class}, "2026-08-07T10:11:12"));
            assertNull(invoke(servlet, "parseTimestamp", new Class[]{String.class}, "not-a-ts"));
    
            assertNull(invoke(servlet, "parseDate", new Class[]{String.class}, (Object) null));
            assertEquals(Date.valueOf("2026-08-07"), invoke(servlet, "parseDate", new Class[]{String.class}, "2026-08-07"));
            assertEquals(Date.valueOf("2026-08-07"), invoke(servlet, "parseDate", new Class[]{String.class}, "2026-08-07T10:11:12+00:00"));
            assertEquals(Date.valueOf("2026-08-07"), invoke(servlet, "parseDate", new Class[]{String.class}, "2026-08-07T10:11:12Z"));
            assertNull(invoke(servlet, "parseDate", new Class[]{String.class}, "not-a-date"));
        }
    
        @Test
        void privateHelpers_readCellAsTextAndTypedReaders_coverBranches() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
            ResultSet rs = mock(ResultSet.class);
            ResultSetMetaData md = mock(ResultSetMetaData.class);
    
            when(md.getColumnType(1)).thenReturn(Types.BINARY);
            when(rs.getBinaryStream(1)).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));
            assertEquals("AQI=", invoke(servlet, "readCellAsText", new Class[]{ResultSet.class, ResultSetMetaData.class, int.class}, rs, md, 1));
    
            when(md.getColumnType(2)).thenReturn(Types.TIMESTAMP);
            when(rs.getObject(2)).thenReturn("2026-08-07T10:11:12Z");
            assertEquals("2026-08-07T10:11:12Z", invoke(servlet, "readCellAsText", new Class[]{ResultSet.class, ResultSetMetaData.class, int.class}, rs, md, 2));
    
            when(md.getColumnType(3)).thenReturn(Types.DATE);
            when(rs.getDate(3)).thenReturn(Date.valueOf("2026-08-07"));
            assertEquals("2026-08-07", invoke(servlet, "readCellAsText", new Class[]{ResultSet.class, ResultSetMetaData.class, int.class}, rs, md, 3));
    
            when(md.getColumnType(4)).thenReturn(Types.VARCHAR);
            when(rs.getObject(4)).thenReturn("value");
            assertEquals("value", invoke(servlet, "readCellAsText", new Class[]{ResultSet.class, ResultSetMetaData.class, int.class}, rs, md, 4));
    
            ResultSetMetaData badMd = mock(ResultSetMetaData.class);
            when(badMd.getColumnType(1)).thenThrow(new SQLException("type fail"));
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "readCellAsText", new Class[]{ResultSet.class, ResultSetMetaData.class, int.class}, rs, badMd, 1));
    
            ResultSet tsFallback = mock(ResultSet.class);
                when(tsFallback.getObject(1)).thenReturn("2026-08-07T10:11:12Z");
            assertNotNull(invoke(servlet, "readValidatedTimestamp", new Class[]{ResultSet.class, int.class}, tsFallback, 1));
    
            ResultSet dateFallback = mock(ResultSet.class);
                when(dateFallback.getObject(1)).thenReturn("2026-08-07");
            assertEquals(Date.valueOf("2026-08-07"), invoke(servlet, "readValidatedDate", new Class[]{ResultSet.class, int.class}, dateFallback, 1));
    
            ResultSet badTextRow = mock(ResultSet.class);
                when(badTextRow.getObject(1)).thenThrow(new SQLException("text fail"));
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "readValidatedCellText", new Class[]{ResultSet.class, int.class}, badTextRow, 1));
        }
    
        @Test
        void privateHelpers_listTableMetadataAndManifest_coverSuccessAndFailures() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
    
            Connection listConn = mock(Connection.class);
            PreparedStatement listPs = mock(PreparedStatement.class);
            ResultSet listRs = mock(ResultSet.class);
            when(listConn.prepareStatement(contains("FROM pg_catalog.pg_tables"))).thenReturn(listPs);
            when(listPs.executeQuery()).thenReturn(listRs);
            when(listRs.next()).thenReturn(true, true, true, true, false);
            when(listRs.getObject(1)).thenReturn("good_table", "flyway_schema_history", "bad-name", "   ");
    
            @SuppressWarnings("unchecked")
            List<String> exportable = (List<String>) invoke(servlet, "listExportableTables", new Class[]{Connection.class}, listConn);
            assertEquals(List.of("good_table"), exportable);
    
            Connection badListConn = mock(Connection.class);
            when(badListConn.prepareStatement(anyString())).thenThrow(new SQLException("list fail"));
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "listExportableTables", new Class[]{Connection.class}, badListConn));
    
            Connection colConn = mock(Connection.class);
            PreparedStatement colPs = mock(PreparedStatement.class);
            ResultSet colRs = mock(ResultSet.class);
            when(colConn.prepareStatement(contains("FROM information_schema.columns"))).thenReturn(colPs);
            when(colPs.executeQuery()).thenReturn(colRs);
            when(colRs.next()).thenReturn(true, true, false);
            when(colRs.getObject(1)).thenReturn("col_a", "bad-col");
    
            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) invoke(servlet, "listTableColumns", new Class[]{Connection.class, String.class}, colConn, "table_a");
            assertEquals(List.of("col_a"), columns);
    
            Connection badColConn = mock(Connection.class);
            when(badColConn.prepareStatement(anyString())).thenThrow(new SQLException("cols fail"));
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "listTableColumns", new Class[]{Connection.class, String.class}, badColConn, "table_a"));
    
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
            invoke(servlet, "writeManifest", new Class[]{ZipOutputStream.class, List.class, List.class, String.class},
                    zip, List.of("a"), List.of("b"), "ts");
            zip.finish();
            assertTrue(out.size() > 0);
    
            ZipOutputStream closedZip = new ZipOutputStream(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
            closedZip.close();
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "writeManifest", new Class[]{ZipOutputStream.class, List.class, List.class, String.class},
                            closedZip, List.of("a"), List.of(), "ts"));
        }
    
        @Test
        void privateHelper_exportTableAsCsv_emptyColumnsAndQueryFailureBranches() throws Exception {
            DatabaseBackupServlet servlet = new DatabaseBackupServlet();
    
            Connection emptyConn = mock(Connection.class);
            PreparedStatement colPs = mock(PreparedStatement.class);
            ResultSet colRs = mock(ResultSet.class);
            when(emptyConn.prepareStatement(contains("FROM information_schema.columns"))).thenReturn(colPs);
            when(colPs.executeQuery()).thenReturn(colRs);
            when(colRs.next()).thenReturn(false);
    
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
            invoke(servlet, "exportTableAsCsv", new Class[]{Connection.class, ZipOutputStream.class, String.class}, emptyConn, zip, "tbl");
            zip.finish();
    
            Connection failConn = mock(Connection.class);
            PreparedStatement failColPs = mock(PreparedStatement.class);
            ResultSet failColRs = mock(ResultSet.class);
            when(failConn.prepareStatement(contains("FROM information_schema.columns"))).thenReturn(failColPs);
            when(failColPs.executeQuery()).thenReturn(failColRs);
            when(failColRs.next()).thenReturn(true, false);
            when(failColRs.getObject(1)).thenReturn("col_1");
            when(failConn.prepareStatement(startsWith("SELECT "), anyInt(), anyInt())).thenThrow(new SQLException("select fail"));
    
            ZipOutputStream failZip = new ZipOutputStream(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
            assertThrowsCause(IllegalStateException.class,
                    () -> invoke(servlet, "exportTableAsCsv", new Class[]{Connection.class, ZipOutputStream.class, String.class}, failConn, failZip, "tbl"));
        }
    
        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        }
    
        private static <T extends Throwable> void assertThrowsCause(Class<T> expectedType, ThrowingRunnable runnable) {
            try {
                runnable.run();
                fail("Expected exception: " + expectedType.getSimpleName());
            } catch (Throwable t) {
                Throwable cause = t;
                if (t instanceof InvocationTargetException ite && ite.getCause() != null) {
                    cause = ite.getCause();
                } else if (t.getCause() != null) {
                    cause = t.getCause();
                }
                assertTrue(expectedType.isInstance(cause),
                        "Expected " + expectedType.getSimpleName() + " but got " + cause.getClass().getSimpleName());
            }
        }
    
        @FunctionalInterface
        private interface ThrowingRunnable {
            void run() throws Throwable;
        }
    
        private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
    
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // no-op
                }
    
                @Override
                public void write(int b) {
                    out.write(b);
                }
            };
        }
}
