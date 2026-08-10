package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Date;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.startsWith;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.csv.CSVFormat;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
/**
 * Parasoft Jtest UTA: Test class for DatabaseImportServlet
 *
 * @see com.sim.chatserver.web.admin.DatabaseImportServlet
 * @author bmcmullin
 */
public class DatabaseImportServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = null; // UTA: configured value
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = mock(Part.class);
        long getSizeResult = 0; // UTA: configured value
        when(getPartResult.getSize()).thenReturn(getSizeResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = mock(Part.class);
        long getSizeResult = 1; // UTA: configured value
        when(getPartResult.getSize()).thenReturn(getSizeResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

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
     * @see com.sim.chatserver.web.admin.DatabaseImportServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        DatabaseImportServlet underTest = new DatabaseImportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        Part getPartResult = mock(Part.class);
        when(getPartResult.getInputStream()).thenThrow(IOException.class);

        long getSizeResult = 1; // UTA: configured value
        when(getPartResult.getSize()).thenReturn(getSizeResult);
        when(req.getPart(nullable(String.class))).thenReturn(getPartResult);

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

    @Test
    public void testParseDateStrictAcceptsIsoTimestampForLegacyBackups() throws Throwable
    {
        DatabaseImportServlet underTest = new DatabaseImportServlet();
        Method parseDateStrict = DatabaseImportServlet.class.getDeclaredMethod("parseDateStrict", String.class);
        parseDateStrict.setAccessible(true);

        Date parsed = (Date) parseDateStrict.invoke(underTest, "2026-05-20T07:00:00Z");

        assertEquals(Date.valueOf("2026-05-20"), parsed);
    }

    @Test
    public void testParseDateStrictRejectsInvalidInput() throws Throwable
    {
        DatabaseImportServlet underTest = new DatabaseImportServlet();
        Method parseDateStrict = DatabaseImportServlet.class.getDeclaredMethod("parseDateStrict", String.class);
        parseDateStrict.setAccessible(true);

        Object parsed = parseDateStrict.invoke(underTest, "not-a-date");

        assertEquals(null, parsed);
    }



    // Merged from DatabaseImportServletCoverageTest
    
    
        @Test
        void doPost_whenUnauthenticated_returns401Json() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("admin authentication"));
        }
    
        @Test
        void doPost_invalidAction_returns400Json() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpSession admin = adminSession();
            when(req.getSession(false)).thenReturn(admin);
            when(req.getParameterValues("action")).thenReturn(new String[]{"nope"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("invalid action"));
        }
    
        @Test
        void doPost_precheckMissingFile_returns400Json() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpSession admin = adminSession();
            when(req.getSession(false)).thenReturn(admin);
            when(req.getParameterValues("action")).thenReturn(new String[]{"precheck"});
            when(req.getPart("file")).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("upload backup zip"));
        }
    
        @Test
        void doPost_runMissingFile_returns400Json() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpSession admin = adminSession();
            when(req.getSession(false)).thenReturn(admin);
            when(req.getParameterValues("action")).thenReturn(new String[]{"run"});
            when(req.getPart("file")).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("upload backup zip"));
        }
    
        @Test
        void helperMethods_parseAndSanitize_coverCoreBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            assertEquals("", invoke(servlet, "truncate", new Class[]{String.class}, new Object[]{null}));
            assertTrue(((String) invoke(servlet, "truncate", new Class[]{String.class}, "a".repeat(600))).endsWith("..."));
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
            assertEquals("w_123", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123"));
    
            assertEquals("\"ok_name\"", invoke(servlet, "q", new Class[]{String.class}, "ok_name"));
            assertThrowsIllegalArgument(() -> invoke(servlet, "q", new Class[]{String.class}, "bad-name"));
    
            assertEquals("abc", invokeStatic(DatabaseImportServlet.class, "sanitizeRequestValue", new Class[]{String.class}, "\u0000abc\n"));
            assertNull(invokeStatic(DatabaseImportServlet.class, "sanitizeRequestValue", new Class[]{String.class}, new Object[]{null}));
    
            assertEquals("ab", invokeStatic(DatabaseImportServlet.class, "stripControlChars", new Class[]{String.class}, "a\u0000\rb\n"));
    
            assertNotNull(invoke(servlet, "parseTimestampStrict", new Class[]{String.class}, "2026-08-01T10:15:30Z"));
            assertNotNull(invoke(servlet, "parseTimestampStrict", new Class[]{String.class}, "2026-08-01 10:15:30"));
            assertNull(invoke(servlet, "parseTimestampStrict", new Class[]{String.class}, "not-a-ts"));
    
            assertNotNull(invoke(servlet, "parseDateStrict", new Class[]{String.class}, "2026-08-01"));
            assertNotNull(invoke(servlet, "parseDateStrict", new Class[]{String.class}, "2026-08-01T10:15:30Z"));
            assertNull(invoke(servlet, "parseDateStrict", new Class[]{String.class}, "not-a-date"));
    
            assertTrue((Boolean) invoke(servlet, "isSafeSyncEndpoint", new Class[]{URI.class}, URI.create("https://example.com/path")));
            assertFalse((Boolean) invoke(servlet, "isSafeSyncEndpoint", new Class[]{URI.class}, URI.create("ftp://example.com")));
    
            assertEquals(Types.VARCHAR, invoke(servlet, "sanitizeSqlType", new Class[]{int.class}, Integer.MIN_VALUE));
            assertEquals(Types.INTEGER, invoke(servlet, "sanitizeSqlType", new Class[]{int.class}, Types.INTEGER));
    
            assertEquals("widget_1", invoke(servlet, "sanitizeWidgetId", new Class[]{String.class}, "widget_1"));
            assertNull(invoke(servlet, "sanitizeWidgetId", new Class[]{String.class}, "bad id"));
        }
    
        @Test
        void helperMethods_tableAndMetadataReaders_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, "public", "widget_entries", new String[]{"TABLE"})).thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
    
            assertTrue((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, conn, "widget_entries"));
    
            Connection badConn = mock(Connection.class);
            when(badConn.getMetaData()).thenThrow(new SQLException("meta fail"));
            assertFalse((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget_entries"));
    
            ResultSet rsInt = mock(ResultSet.class);
            when(rsInt.getInt("DATA_TYPE")).thenReturn(12);
            when(rsInt.wasNull()).thenReturn(false);
            assertEquals(Integer.valueOf(12), invoke(servlet, "readMetadataInt", new Class[]{ResultSet.class, String.class}, rsInt, "DATA_TYPE"));
    
            ResultSet rsIntFallback = mock(ResultSet.class);
            when(rsIntFallback.getInt("DATA_TYPE")).thenThrow(new SQLException("x"));
            when(rsIntFallback.getString("DATA_TYPE")).thenReturn("34");
            assertEquals(Integer.valueOf(34), invoke(servlet, "readMetadataInt", new Class[]{ResultSet.class, String.class}, rsIntFallback, "DATA_TYPE"));
    
            ResultSet rsSafe = mock(ResultSet.class);
            when(rsSafe.getString("widget_id")).thenReturn(" widget_1 ");
            assertEquals("widget_1", invoke(servlet, "readSafeDbText", new Class[]{ResultSet.class, String.class, int.class}, rsSafe, "widget_id", 80));
    
            ResultSet rsUnsafe = mock(ResultSet.class);
            when(rsUnsafe.getString("widget_id")).thenReturn("widgetâ‚¬id");
            assertNull(invoke(servlet, "readSafeDbText", new Class[]{ResultSet.class, String.class, int.class}, rsUnsafe, "widget_id", 80));
    
            ResultSet rsIdent = mock(ResultSet.class);
            when(rsIdent.getString("COLUMN_NAME")).thenReturn("Created_At");
            assertEquals("created_at", invoke(servlet, "readMetadataIdentifier", new Class[]{ResultSet.class, String.class}, rsIdent, "COLUMN_NAME"));
        }
    
        @Test
        void helperMethods_bindTyped_and_batchContext_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            PreparedStatement ps = mock(PreparedStatement.class);
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 1, "42", Types.INTEGER);
            verify(ps).setInt(1, 42);
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 2, "true", Types.BOOLEAN);
            verify(ps).setBoolean(2, true);
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 3, "", Types.VARCHAR);
            verify(ps).setString(3, "");
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 4, null, Types.BIGINT);
            verify(ps).setNull(4, Types.BIGINT);
    
            String b64 = java.util.Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 5, b64, Types.BINARY);
            verify(ps).setBytes(5, "abc".getBytes(StandardCharsets.UTF_8));
    
            assertThrows(IllegalStateException.class, () -> invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 6, "xx", Types.INTEGER));
    
            PreparedStatement okBatch = mock(PreparedStatement.class);
            invoke(servlet, "executeBatchWithContext", new Class[]{PreparedStatement.class, String.class, int.class}, okBatch, "tbl", 9);
            verify(okBatch).executeBatch();
    
            PreparedStatement badBatch = mock(PreparedStatement.class);
            BatchUpdateException bue = new BatchUpdateException("batch", new int[0]);
            bue.setNextException(new SQLException("root failure"));
            when(badBatch.executeBatch()).thenThrow(bue);
            assertThrows(DatabaseImportServlet.ImportException.class, () -> invoke(servlet, "executeBatchWithContext", new Class[]{PreparedStatement.class, String.class, int.class}, badBatch, "tbl", 11));
        }
    
        @Test
        void helperMethods_readCsvAndZipTables_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            byte[] csvBytes = "a,b\n1,2\n".getBytes(StandardCharsets.UTF_8);
            Object csvData = invoke(servlet, "readCsv", new Class[]{java.io.InputStream.class}, new ByteArrayInputStream(csvBytes));
            assertNotNull(csvData);
    
            ByteArrayOutputStream zipped = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipped, StandardCharsets.UTF_8)) {
                zos.putNextEntry(new ZipEntry("tables/server_config.csv"));
                zos.write("k,v\na,b\n".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
    
                zos.putNextEntry(new ZipEntry("misc/ignore.txt"));
                zos.write("x".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
    
            @SuppressWarnings("unchecked")
            Map<String, Object> tables = (Map<String, Object>) invoke(servlet, "readZipTables", new Class[]{java.io.InputStream.class}, new ByteArrayInputStream(zipped.toByteArray()));
            assertEquals(1, tables.size());
            assertTrue(tables.containsKey("server_config"));
    
            @SuppressWarnings("unchecked")
            List<String> widgetTables = (List<String>) invoke(servlet, "findWidgetTables", new Class[]{java.util.Set.class}, java.util.Set.of("server_config", "widget_x"));
            assertEquals(List.of("widget_x"), widgetTables);
        }
    
        @Test
        void helperMethods_jsonBuilders_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            List<String> values = new java.util.ArrayList<>();
            values.add("a");
            values.add(null);
            values.add("b");
    
            @SuppressWarnings("unchecked")
            var arr = (jakarta.json.JsonArray) invoke(servlet, "toJsonArray", new Class[]{List.class}, values);
            assertEquals(3, arr.size());
            assertEquals("", arr.getString(1));
    
            Map<String, Integer> counts = new HashMap<>();
            counts.put("x", Integer.valueOf(2));
            counts.put("y", null);
            JsonObject obj = (JsonObject) invoke(servlet, "toJsonObject", new Class[]{Map.class}, counts);
            assertEquals(2, obj.getInt("x"));
            assertEquals(0, obj.getInt("y"));
    
            JsonObject err = (JsonObject) invoke(servlet, "err", new Class[]{String.class}, "oops");
            assertEquals("error", err.getString("status"));
            assertEquals("oops", err.getString("message"));
        }
    
        @Test
        void doPost_whenPartReadFails_returns400Json() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            HttpServletRequest precheckReq = mock(HttpServletRequest.class);
            HttpServletResponse precheckResp = mock(HttpServletResponse.class);
            ByteArrayOutputStream precheckOut = new ByteArrayOutputStream();
            HttpSession precheckSession = adminSession();
            when(precheckReq.getSession(false)).thenReturn(precheckSession);
            when(precheckReq.getParameterValues("action")).thenReturn(new String[]{"precheck"});
            when(precheckReq.getPart("file")).thenThrow(new IOException("broken upload"));
            when(precheckResp.getOutputStream()).thenReturn(servletOutput(precheckOut));
    
            servlet.doPost(precheckReq, precheckResp);
    
            JsonObject precheckBody = Json.createReader(new StringReader(precheckOut.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", precheckBody.getString("status"));
            assertTrue(precheckBody.getString("message").toLowerCase().contains("upload backup zip"));
    
            HttpServletRequest runReq = mock(HttpServletRequest.class);
            HttpServletResponse runResp = mock(HttpServletResponse.class);
            ByteArrayOutputStream runOut = new ByteArrayOutputStream();
            HttpSession runSession = adminSession();
            when(runReq.getSession(false)).thenReturn(runSession);
            when(runReq.getParameterValues("action")).thenReturn(new String[]{"run"});
            when(runReq.getPart("file")).thenThrow(new jakarta.servlet.ServletException("broken multipart"));
            when(runResp.getOutputStream()).thenReturn(servletOutput(runOut));
    
            servlet.doPost(runReq, runResp);
    
            JsonObject runBody = Json.createReader(new StringReader(runOut.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", runBody.getString("status"));
            assertTrue(runBody.getString("message").toLowerCase().contains("upload backup zip"));
        }
    
        @Test
        void helperMethods_widgetTableNormalizationAndMerging_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet tablesRs = mock(ResultSet.class);
            PreparedStatement widgetPs = mock(PreparedStatement.class);
            ResultSet widgetRs = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, "public", "widget_entries", new String[]{"TABLE"})).thenReturn(tablesRs);
            when(tablesRs.next()).thenReturn(true);
    
            when(conn.prepareStatement("SELECT widget_id FROM widget_entries")).thenReturn(widgetPs);
            when(widgetPs.executeQuery()).thenReturn(widgetRs);
            when(widgetRs.next()).thenReturn(true, true, false);
            when(widgetRs.getString("widget_id")).thenReturn("Widget-1", "Widget_1");
    
            Object csvA = newCsvTableData(List.of("c1"), List.of(List.of("1")));
            Object csvB = newCsvTableData(List.of("c1"), List.of(List.of("2")));
            Object baseline = newCsvTableData(List.of("k", "v"), List.of(List.of("a", "b")));
    
            Map<String, Object> zipTables = new LinkedHashMap<>();
            zipTables.put("Widget-1", csvA);
            zipTables.put("Widget_1", csvB);
            zipTables.put("server_config", baseline);
    
            @SuppressWarnings("unchecked")
            Map<String, Object> normalized = (Map<String, Object>) invoke(servlet,
                    "normalizeZipWidgetTables",
                    new Class[]{Connection.class, Map.class},
                    conn,
                    zipTables);
    
            assertTrue(normalized.containsKey("Widget_1"));
            assertTrue(normalized.containsKey("server_config"));
    
            Object merged = normalized.get("Widget_1");
            Field rowsField = merged.getClass().getDeclaredField("rows");
            rowsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<List<String>> rows = (List<List<String>>) rowsField.get(merged);
            assertEquals(2, rows.size());
        }
    
        @Test
        void helperMethods_createTablePaths_coverKnownAndFallback() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
    
            when(conn.prepareStatement(anyString())).thenReturn(ps);
    
            invoke(servlet, "createKnownTable", new Class[]{Connection.class, String.class}, conn, "customer_identity");
            invoke(servlet, "createKnownTable", new Class[]{Connection.class, String.class}, conn, "custom_table");
            invoke(servlet, "createTableFromCsvHeader", new Class[]{Connection.class, String.class, List.class}, conn, "widget_custom", List.of("chat_id", "prompt"));
            invoke(servlet, "createTableFromCsvHeader", new Class[]{Connection.class, String.class, List.class}, conn, "widget_empty", List.of());
    
            verify(ps, atLeast(4)).execute();
        }
    
        @Test
        void helperMethods_replaceTableData_branches_coverHeaderAndMismatch() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            Object emptyCsv = newCsvTableData(List.of(), List.of());
            int none = (Integer) invoke(servlet, "replaceTableData", new Class[]{Connection.class, String.class, emptyCsv.getClass()}, mock(Connection.class), "tbl", emptyCsv);
            assertEquals(0, none);
    
            Connection conn = mock(Connection.class);
            PreparedStatement truncatePs = mock(PreparedStatement.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet columns = mock(ResultSet.class);
    
            when(conn.prepareStatement("TRUNCATE TABLE \"mytable\" RESTART IDENTITY CASCADE")).thenReturn(truncatePs);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getColumns(null, "public", "mytable", null)).thenReturn(columns);
            when(columns.next()).thenReturn(false);
    
            Object csv = newCsvTableData(List.of("unknown_col"), List.of(List.of("x")));
    
            Exception ex = assertThrows(Exception.class,
                    () -> invoke(servlet, "replaceTableData", new Class[]{Connection.class, String.class, csv.getClass()}, conn, "mytable", csv));
            assertTrue(ex.getClass().getSimpleName().contains("ImportException"));
        }
    
        @Test
        void helperMethods_adminAndJsonFallback_coverBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            HttpServletRequest reqNoSession = mock(HttpServletRequest.class);
            when(reqNoSession.getSession(false)).thenReturn(null);
            assertFalse((Boolean) invoke(servlet, "isAdmin", new Class[]{HttpServletRequest.class}, reqNoSession));
    
            HttpServletRequest reqAdmin = mock(HttpServletRequest.class);
            HttpSession session = adminSession();
            when(reqAdmin.getSession(false)).thenReturn(session);
            assertTrue((Boolean) invoke(servlet, "isAdmin", new Class[]{HttpServletRequest.class}, reqAdmin));
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.getOutputStream()).thenThrow(new IOException("write fail"));
            when(resp.isCommitted()).thenReturn(false);
    
            invoke(servlet, "json", new Class[]{HttpServletResponse.class, int.class, JsonObject.class},
                    resp,
                    HttpServletResponse.SC_OK,
                    Json.createObjectBuilder().add("status", "ok").build());
    
            verify(resp).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
        }
    
            @Test
            void helperMethods_bindTypedTemporalAndNumericBranches_coverMoreCases() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
            PreparedStatement ps = mock(PreparedStatement.class);
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 1, "12.5", Types.DOUBLE);
            verify(ps).setDouble(1, 12.5d);
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 2, "123.45", Types.DECIMAL);
            verify(ps).setBigDecimal(2, new BigDecimal("123.45"));
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 3, "2026-08-01T10:15:30+01:00", Types.TIMESTAMP_WITH_TIMEZONE);
            verify(ps).setTimestamp(eq(3), any(Timestamp.class));
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 4, "2026-08-01T10:15:30", Types.DATE);
            verify(ps).setDate(eq(4), any(java.sql.Date.class));
    
            invoke(servlet, "bindTyped", new Class[]{PreparedStatement.class, int.class, String.class, int.class}, ps, 5, "raw-value", Types.OTHER);
            verify(ps).setString(5, "raw-value");
    
            PreparedStatement tsFail = mock(PreparedStatement.class);
            doThrow(new SQLException("ts fail")).when(tsFail).setTimestamp(eq(1), any(Timestamp.class));
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "bindTimestamp", new Class[]{PreparedStatement.class, int.class, String.class}, tsFail, 1, "2026-08-01T10:15:30Z"));
    
            PreparedStatement dateFail = mock(PreparedStatement.class);
            doThrow(new SQLException("date fail")).when(dateFail).setDate(eq(1), any(java.sql.Date.class));
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "bindDate", new Class[]{PreparedStatement.class, int.class, String.class}, dateFail, 1, "2026-08-01"));
            }
    
            @Test
            void helperMethods_parsersAndSanitizers_coverAdditionalBranches() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            assertNotNull(invoke(servlet, "parseTimestampStrict", new Class[]{String.class}, "2026-08-01T10:15:30+01:00"));
            assertNotNull(invoke(servlet, "parseTimestampStrict", new Class[]{String.class}, "2026-08-01 10:15:30"));
    
            assertEquals(java.sql.Date.valueOf("2026-08-01"),
                invoke(servlet, "parseDateStrict", new Class[]{String.class}, "2026-08-01T10:15:30+01:00"));
            assertEquals(java.sql.Date.valueOf("2026-08-01"),
                invoke(servlet, "parseDateStrict", new Class[]{String.class}, "2026-08-01T10:15:30"));
            assertEquals(java.sql.Date.valueOf("2026-08-01"),
                invoke(servlet, "parseDateStrict", new Class[]{String.class}, "2026-08-01junk"));
    
            assertEquals("", invokeStatic(DatabaseImportServlet.class, "readEnv", new Class[]{String.class}, (Object) null));
            assertEquals("", invokeStatic(DatabaseImportServlet.class, "readEnv", new Class[]{String.class}, ""));
            assertNotNull(invokeStatic(DatabaseImportServlet.class, "readEnv", new Class[]{String.class}, "PATH"));
    
            assertEquals("", invokeStatic(DatabaseImportServlet.class, "sanitizeSyncUrlValue", new Class[]{String.class}, ""));
            assertEquals("", invokeStatic(DatabaseImportServlet.class, "sanitizeSyncUrlValue", new Class[]{String.class}, "ftp://example.com"));
            assertEquals("https://example.com/x", invokeStatic(DatabaseImportServlet.class,
                "sanitizeSyncUrlValue", new Class[]{String.class}, "https://example.com/x"));
    
            String tooLong = "https://" + "a".repeat(600) + ".com";
            assertEquals("", invokeStatic(DatabaseImportServlet.class,
                "sanitizeSyncUrlValue", new Class[]{String.class}, tooLong));
    
            assertNull(invoke(servlet, "sanitizeWidgetId", new Class[]{String.class}, new Object[]{null}));
            assertNull(invoke(servlet, "sanitizeWidgetId", new Class[]{String.class}, "   "));
            assertEquals("a".repeat(80), invoke(servlet, "sanitizeWidgetId", new Class[]{String.class}, "a".repeat(81)));
            }
    
            @Test
            void helperMethods_streamAndParserErrorBranches_coverFailures() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            InputStream brokenZip = new InputStream() {
                @Override
                public int read() throws IOException {
                throw new IOException("zip read fail");
                }
            };
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "readZipTables", new Class[]{InputStream.class}, brokenZip));
    
            BufferedReader brokenReader = new BufferedReader(new Reader() {
                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("csv read fail");
                }
    
                @Override
                public void close() {
                // no-op
                }
            });
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet,
                    "parseCsvSafe",
                    new Class[]{CSVFormat.class, BufferedReader.class},
                    CSVFormat.DEFAULT.builder().setHeader().build(),
                    brokenReader));
    
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "openPartInputStream", new Class[]{Part.class, String.class}, null, "phase"));
    
            Part badPart = mock(Part.class);
            when(badPart.getInputStream()).thenThrow(new IOException("part fail"));
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "openPartInputStream", new Class[]{Part.class, String.class}, badPart, "phase"));
            }
    
            @Test
            void helperMethods_statementAndDdlFailureBranches_coverPaths() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            Connection prepFailConn = mock(Connection.class);
            when(prepFailConn.prepareStatement(anyString())).thenThrow(new SQLException("prep fail"));
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "prepareStatementSafe", new Class[]{Connection.class, String.class}, prepFailConn, "SELECT 1"));
    
            Connection ddlConn = mock(Connection.class);
            PreparedStatement ddlPs = mock(PreparedStatement.class);
            when(ddlConn.prepareStatement(anyString())).thenReturn(ddlPs);
            doThrow(new SQLException("ddl fail")).when(ddlPs).execute();
    
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet, "createKnownTable", new Class[]{Connection.class, String.class}, ddlConn, "customer_identity_session"));
    
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet,
                    "createTableFromCsvHeader",
                    new Class[]{Connection.class, String.class, List.class},
                    ddlConn,
                    "widget_empty",
                    List.of()));
    
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet,
                    "createTableFromCsvHeader",
                    new Class[]{Connection.class, String.class, List.class},
                    ddlConn,
                    "widget_named",
                    List.of("col_a")));
            }
    
            @Test
            void helperMethods_replaceAndSequenceBranches_coverSuccessAndFailures() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            Connection conn = mock(Connection.class);
            PreparedStatement truncatePs = mock(PreparedStatement.class);
            PreparedStatement insertPs = mock(PreparedStatement.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet columns = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getColumns(null, "public", "my_table", null)).thenReturn(columns);
            when(columns.next()).thenReturn(true, false);
            when(columns.getString("COLUMN_NAME")).thenReturn("name");
            when(columns.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR);
            when(columns.getInt("NULLABLE")).thenReturn(ResultSetMetaData.columnNullable);
            when(columns.wasNull()).thenReturn(false, false);
    
            when(conn.prepareStatement(anyString())).thenAnswer(inv -> {
                String sql = inv.getArgument(0, String.class);
                if (sql.startsWith("TRUNCATE TABLE")) {
                return truncatePs;
                }
                if (sql.startsWith("INSERT INTO")) {
                return insertPs;
                }
                throw new SQLException("Unexpected SQL: " + sql);
            });
    
            Object okCsv = newCsvTableData(List.of("name"), List.of(List.of("alpha")));
            int inserted = (Integer) invoke(servlet,
                "replaceTableData",
                new Class[]{Connection.class, String.class, okCsv.getClass()},
                conn,
                "my_table",
                okCsv);
            assertEquals(1, inserted);
            verify(insertPs).setString(1, "alpha");
            verify(insertPs).executeBatch();
    
            Connection badConn = mock(Connection.class);
            PreparedStatement badTruncate = mock(PreparedStatement.class);
            PreparedStatement badInsert = mock(PreparedStatement.class);
            DatabaseMetaData badMeta = mock(DatabaseMetaData.class);
            ResultSet badColumns = mock(ResultSet.class);
            when(badConn.getMetaData()).thenReturn(badMeta);
            when(badMeta.getColumns(null, "public", "my_table", null)).thenReturn(badColumns);
            when(badColumns.next()).thenReturn(true, false);
            when(badColumns.getString("COLUMN_NAME")).thenReturn("count_col");
            when(badColumns.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
            when(badColumns.getInt("NULLABLE")).thenReturn(ResultSetMetaData.columnNullable);
            when(badColumns.wasNull()).thenReturn(false, false);
    
            when(badConn.prepareStatement(anyString())).thenAnswer(inv -> {
                String sql = inv.getArgument(0, String.class);
                if (sql.startsWith("TRUNCATE TABLE")) {
                return badTruncate;
                }
                if (sql.startsWith("INSERT INTO")) {
                return badInsert;
                }
                throw new SQLException("Unexpected SQL: " + sql);
            });
    
            Object badCsv = newCsvTableData(List.of("count_col"), List.of(List.of("not-an-int")));
            Exception bindFailure = assertThrows(Exception.class,
                () -> invoke(servlet,
                    "replaceTableData",
                    new Class[]{Connection.class, String.class, badCsv.getClass()},
                    badConn,
                    "my_table",
                    badCsv));
            assertTrue(bindFailure.getClass().getSimpleName().contains("ImportException"));
    
            Connection seqConn = mock(Connection.class);
            PreparedStatement lookupPs = mock(PreparedStatement.class);
            PreparedStatement setPs = mock(PreparedStatement.class);
            ResultSet seqRs = mock(ResultSet.class);
            when(lookupPs.executeQuery()).thenReturn(seqRs);
            when(seqRs.next()).thenReturn(true, true, false);
            when(seqRs.getString("column_name")).thenReturn(null, "id");
            when(seqRs.getString("seq_name")).thenReturn("public.my_seq", "public.my_seq");
    
            when(seqConn.prepareStatement(startsWith("SELECT column_name,"))).thenReturn(lookupPs);
            when(seqConn.prepareStatement(startsWith("SELECT setval("))).thenReturn(setPs);
    
            invoke(servlet, "realignSequenceBackedColumns", new Class[]{Connection.class, String.class}, seqConn, "my_table");
            verify(setPs).setString(1, "public.my_seq");
            verify(setPs).execute();
    
            Connection seqFailConn = mock(Connection.class);
            when(seqFailConn.prepareStatement(startsWith("SELECT column_name,"))).thenThrow(new SQLException("lookup fail"));
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet,
                    "realignSequenceBackedColumns",
                    new Class[]{Connection.class, String.class},
                    seqFailConn,
                    "my_table"));
            }
    
            @Test
            void helperMethods_widgetMapAndSyncResultBranches_coverFallbacks() throws Exception {
            DatabaseImportServlet servlet = new DatabaseImportServlet();
    
            Connection noWidgetConn = mock(Connection.class);
            DatabaseMetaData noWidgetMeta = mock(DatabaseMetaData.class);
            ResultSet noWidgetTables = mock(ResultSet.class);
            when(noWidgetConn.getMetaData()).thenReturn(noWidgetMeta);
            when(noWidgetMeta.getTables(null, "public", "widget_entries", new String[]{"TABLE"})).thenReturn(noWidgetTables);
            when(noWidgetTables.next()).thenReturn(false);
    
            @SuppressWarnings("unchecked")
            Map<String, String> emptyMap = (Map<String, String>) invoke(servlet,
                "buildWidgetIdToTableMap",
                new Class[]{Connection.class},
                noWidgetConn);
            assertTrue(emptyMap.isEmpty());
    
            Connection mapFailConn = mock(Connection.class);
            DatabaseMetaData mapMeta = mock(DatabaseMetaData.class);
            ResultSet tablesExist = mock(ResultSet.class);
            PreparedStatement widgetPs = mock(PreparedStatement.class);
    
            when(mapFailConn.getMetaData()).thenReturn(mapMeta);
            when(mapMeta.getTables(null, "public", "widget_entries", new String[]{"TABLE"})).thenReturn(tablesExist);
            when(tablesExist.next()).thenReturn(true);
            when(mapFailConn.prepareStatement("SELECT widget_id FROM widget_entries")).thenReturn(widgetPs);
            when(widgetPs.executeQuery()).thenThrow(new SQLException("widget query fail"));
    
            assertThrows(IllegalStateException.class,
                () -> invoke(servlet,
                    "buildWidgetIdToTableMap",
                    new Class[]{Connection.class},
                    mapFailConn));
    
            Object syncResult = invoke(servlet, "triggerPostImportSync", new Class[]{});
            Field triggered = syncResult.getClass().getDeclaredField("triggered");
            Field ok = syncResult.getClass().getDeclaredField("ok");
            Field statusCode = syncResult.getClass().getDeclaredField("statusCode");
            triggered.setAccessible(true);
            ok.setAccessible(true);
            statusCode.setAccessible(true);
    
            assertFalse((Boolean) triggered.get(syncResult));
            assertTrue((Boolean) ok.get(syncResult));
            assertEquals(0, ((Integer) statusCode.get(syncResult)).intValue());
            }
    
        private static HttpSession adminSession() {
            HttpSession session = mock(HttpSession.class);
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("user", "admin");
            attrs.put("role", "ADMIN");
    
            when(session.getAttribute(anyString())).thenAnswer(inv -> attrs.get(inv.getArgument(0)));
            doAnswer(inv -> {
                attrs.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(session).setAttribute(anyString(), any());
            return session;
        }
    
        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Exception ex) {
                    throw ex;
                }
                throw ite;
            }
        }
    
        private static Object invokeStatic(Class<?> owner, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = owner.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(null, args);
        }
    
        private static Object newCsvTableData(List<String> headers, List<List<String>> rows) throws Exception {
            Class<?> csvClass = Class.forName("com.sim.chatserver.web.admin.DatabaseImportServlet$CsvTableData");
            Constructor<?> ctor = csvClass.getDeclaredConstructor(List.class, List.class);
            ctor.setAccessible(true);
            return ctor.newInstance(headers, rows);
        }
    
        private static void assertThrowsIllegalArgument(ThrowingRunnable action) {
            try {
                action.run();
            } catch (Exception ex) {
                Throwable cause = ex;
                if (ex instanceof InvocationTargetException ite && ite.getCause() != null) {
                    cause = ite.getCause();
                } else if (ex.getCause() != null) {
                    cause = ex.getCause();
                }
                assertTrue(cause instanceof IllegalArgumentException);
                return;
            }
            throw new AssertionError("Expected IllegalArgumentException");
        }
    
        @FunctionalInterface
        private interface ThrowingRunnable {
            void run() throws Exception;
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
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };
        }
}
