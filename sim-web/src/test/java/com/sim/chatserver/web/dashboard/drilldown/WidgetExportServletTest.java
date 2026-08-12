package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ServletOutputStream;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.WriteListener;
/**
 * Parasoft Jtest UTA: Test class for WidgetExportServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet
 * @author bmcmullin
 */
public class WidgetExportServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetExportServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        WidgetExportServlet underTest = new WidgetExportServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(getConnectionResult.getMetaData()).thenReturn(getMetaDataResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolderValue.getDataSource()).thenReturn(getDataSourceResult);
        underTest.dsHolder = dsHolderValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContentLengthLong()).thenReturn(-1L);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
        ServletOutputStream getOutputStreamResult = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(getOutputStreamResult);
        underTest.doPost(req, resp);

    }



    // Merged from WidgetExportServletCoverageTest
    
        @Test
        void doPost_whenUnauthenticated_returns401Json() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("authentication"));
            verify(resp).setHeader("Cache-Control", "no-store");
        }
    
        @Test
        void doPost_invalidContentLength_returns400() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn(-1L);
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }
    
        @Test
        void doPost_invalidJson_returns400() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn(8L);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{bad")));
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }
    
        @Test
        void doPost_missingSelectionId_returns400() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
    
            String payload = "{\"format\":\"csv\"}";
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }
    
        @Test
        void doPost_selectionNotFound_returns404() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
    
            String payload = "{\"selectionId\":\"missing\",\"format\":\"csv\"}";
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
        }
    
        @Test
        void doPost_snapshotSelection_unknownFormat_defaultsToCsv() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
            String selectionId = createSnapshotSelection(session);
    
            String payload = "{" +
                    "\"selectionId\":\"" + selectionId + "\"," +
                    "\"selectedChatIds\":[\"c2\",\"c1\"]," +
                    "\"format\":\"whatever\"" +
                    "}";
    
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            String csv = out.toString(StandardCharsets.UTF_8);
            assertTrue(csv.startsWith("sessionId,sessionIdDisplay,createdAt,prompt,response"));
            assertTrue(csv.contains(",c2 prompt,"));
            assertTrue(csv.contains(",c1 prompt,"));
            assertTrue(csv.indexOf("c2 prompt") < csv.indexOf("c1 prompt"));
        }
    
        @Test
        void doPost_snapshotSelection_jsonFormat_writesJsonArray() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
            String selectionId = createSnapshotSelection(session);
    
            String payload = "{" +
                    "\"selectionId\":\"" + selectionId + "\"," +
                    "\"selectedChatIds\":[\"c1\"]," +
                    "\"format\":\"json\"" +
                    "}";
    
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonArray array = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readArray();
            assertEquals(1, array.size());
            assertEquals("c1", array.getJsonObject(0).getString("sessionId"));
        }
    
        @Test
        void doPost_snapshotSelection_textFormat_writesReadableText() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
            String selectionId = createSnapshotSelection(session);
    
            String payload = "{" +
                    "\"selectionId\":\"" + selectionId + "\"," +
                    "\"selectedChatIds\":[\"c1\"]," +
                    "\"format\":\"text\"" +
                    "}";
    
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            String text = out.toString(StandardCharsets.UTF_8);
            assertTrue(text.contains("Session: c1"));
            assertTrue(text.contains("Prompt:"));
            assertTrue(text.contains("Response:"));
        }
    
        @Test
        void doPost_snapshotSelection_pdfFormat_writesPdfBytes() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
            HttpSession session = mapBackedSession(true);
            String selectionId = createSnapshotSelection(session);
    
            String markdown = "## Executive Chat Analysis\nSummary text\n" +
                    "## Risks and Opportunities\n- Risk A\n" +
                    "## Recommendations\n- Do B\n" +
                    "## Sentiment and Frustration Signals\n- Mild\n" +
                    "## Coverage and Methodology\n- Scoped\n" +
                    "## Key Metrics\n| Metric | Value |\n|---|---|\n| Selected Chats | 2 |";
    
            String payload = "{" +
                    "\"selectionId\":\"" + selectionId + "\"," +
                    "\"selectedChatIds\":[\"c1\",\"c2\"]," +
                    "\"format\":\"pdf\"," +
                    "\"reportMarkdown\":" + Json.createValue(markdown).toString() +
                    "}";
    
            HttpServletRequest req = requestWithBody(session, payload);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            byte[] bytes = out.toByteArray();
            assertTrue(bytes.length > 32);
            String prefix = new String(bytes, 0, Math.min(8, bytes.length), StandardCharsets.ISO_8859_1);
            assertTrue(prefix.startsWith("%PDF"));
        }
    
        @Test
        void privateHelpers_coverParserChunkDbAndPdfFallbackBranches() throws Exception {
            WidgetExportServlet servlet = new WidgetExportServlet();
    
            JsonObject mixedPayload = Json.createObjectBuilder()
                    .add("selectedChatIds", Json.createArrayBuilder().add("a").add(4).add("a").addNull().add(" b "))
                    .build();
            @SuppressWarnings("unchecked")
            List<String> parsed = (List<String>) invoke(servlet, "parseSelectedChatIds", new Class[]{JsonObject.class}, mixedPayload);
            assertEquals(4, parsed.size());
    
            @SuppressWarnings("unchecked")
            List<String> deduped = (List<String>) invoke(servlet, "dedupePreserveOrder", new Class[]{List.class}, List.of("a", "a", "b", "a"));
            assertEquals(List.of("a", "b"), deduped);
    
            assertEquals("csv", invoke(servlet, "normalizeFormat", new Class[]{String.class}, "x"));
            assertEquals("json", invoke(servlet, "normalizeFormat", new Class[]{String.class}, " JSON "));
            assertEquals("pdf", invoke(servlet, "extensionFor", new Class[]{String.class}, "pdf"));
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
            assertEquals("w_123", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123"));
            assertEquals("\"ok_name\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "ok_name"));
            assertThrowsIllegalArgument(() -> invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, null, "tbl", new String[]{"TABLE"})).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            assertTrue((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, conn, "tbl"));
    
            Connection badConn = mock(Connection.class);
            when(badConn.getMetaData()).thenThrow(new SQLException("fail"));
            assertFalse((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, badConn, "tbl"));
    
            @SuppressWarnings("unchecked")
            List<List<String>> chunks = (List<List<String>>) invokeStatic(WidgetExportServlet.class, "chunk", new Class[]{List.class, int.class}, List.of("a", "b", "c"), 2);
            assertEquals(2, chunks.size());
    
            String line = (String) invokeStatic(WidgetExportServlet.class, "csvLine", new Class[]{String[].class}, (Object) new String[]{"a,b", "x\"y"});
            assertTrue(line.contains("\"a,b\""));
            assertTrue(line.contains("\"x\"\"y\""));
    
            ResultSet textRs = mock(ResultSet.class);
            when(textRs.getObject("c")).thenReturn(" x\n\u0000y ");
            assertEquals("x y", invoke(servlet, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, textRs, "c", 10));
    
            ResultSet tsRs = mock(ResultSet.class);
            when(tsRs.getObject("t")).thenReturn("2026-08-01T10:15:30Z");
            assertNotNull(invoke(servlet, "readDbTimestamp", new Class[]{ResultSet.class, String.class}, tsRs, "t"));
    
            ResultSet tsFallbackRs = mock(ResultSet.class);
            when(tsFallbackRs.getObject("t")).thenReturn("2026-08-01 10:15:30");
            assertNotNull(invoke(servlet, "readDbTimestamp", new Class[]{ResultSet.class, String.class}, tsFallbackRs, "t"));
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream fallbackOut = new ByteArrayOutputStream();
            when(resp.getOutputStream()).thenThrow(new IOException("first fail")).thenReturn(servletOutput(fallbackOut));
            invoke(servlet, "writePdf", new Class[]{HttpServletResponse.class, List.class, String.class, String.class},
                    resp, List.of(snapshot("c1", "prompt", "resp")), "sel-1", "## Executive Chat Analysis\nhello");
            assertTrue(fallbackOut.toString(StandardCharsets.UTF_8).contains("Executive"));
        }
    
        private static String createSnapshotSelection(HttpSession session) {
            List<TermChatSnapshot> snapshots = List.of(
                    snapshot("c1", "c1 prompt", "c1 response"),
                    snapshot("c2", "c2 prompt", "c2 response"));
            String selectionId = WidgetReviewStartServlet.createSnapshotSelection(session, "Widget A", snapshots, "/dashboard");
            assertNotNull(selectionId);
            return selectionId;
        }
    
        private static TermChatSnapshot snapshot(String chatId, String prompt, String response) {
            return new TermChatSnapshot(
                    "term",
                    "widgetA",
                    chatId,
                    prompt,
                    response,
                    Timestamp.from(Instant.parse("2026-08-01T10:00:00Z")),
                    chatId);
        }
    
        private static HttpServletRequest requestWithBody(HttpSession session, String body) throws Exception {
            HttpServletRequest req = mock(HttpServletRequest.class);
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            return req;
        }
    
        private static HttpSession mapBackedSession(boolean authenticated) {
            HttpSession session = mock(HttpSession.class);
            Map<String, Object> attrs = new HashMap<>();
            if (authenticated) {
                attrs.put("user", "admin");
            }
    
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
            return method.invoke(target, args);
        }
    
        private static Object invokeStatic(Class<?> owner, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = owner.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(null, args);
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
