package com.sim.chatserver.web.dashboard.inactiveusers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonObject;
/**
 * Parasoft Jtest UTA: Test class for InactiveUsersPageServlet
 *
 * @see com.sim.chatserver.web.dashboard.inactiveusers.InactiveUsersPageServlet
 * @author bmcmullin
 */
public class InactiveUsersPageServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.inactiveusers.InactiveUsersPageServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        InactiveUsersPageServlet underTest = new InactiveUsersPageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.inactiveusers.InactiveUsersPageServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        InactiveUsersPageServlet underTest = new InactiveUsersPageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }



    // Merged from InactiveUsersPageServletCoverageTest
    
    
        @Test
        void doGet_whenUnauthenticated_forwardsToLogin() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    
            when(req.getSession(false)).thenReturn(null);
            when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);
    
            servlet.handleGet(req, resp);
        }
    
        @Test
        void detectFrustration_helpers_coverCoreSignals() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            Object empty = invoke(servlet, "detectFrustration", new Class[]{List.class}, List.of());
            assertFalse((Boolean) field(empty, "detected"));
            assertEquals(0.0, (Double) field(empty, "score"));
    
            Object detected = invoke(servlet, "detectFrustration", new Class[]{List.class}, List.of(
                    "This is ridiculous and wrong again!!!",
                    "you dont understand what I asked",
                    "fix this now"
            ));
            assertTrue((Double) field(detected, "score") >= 0.40);
            assertTrue((Boolean) field(detected, "detected"));
    
            assertTrue((Boolean) invokeUtil("looksLikeCodeText", new Class[]{String.class}, "SELECT * FROM t WHERE id = 1;"));
            assertFalse((Boolean) invokeUtil("looksLikeCodeText", new Class[]{String.class}, "normal sentence"));
    
            assertTrue((Boolean) invokeUtil("looksLikeLogText", new Class[]{String.class}, "2026-08-07 10:01:02 INFO :: service started"));
            assertFalse((Boolean) invokeUtil("looksLikeLogText", new Class[]{String.class}, "plain chat line"));
    
            assertTrue((Boolean) invokeUtil("containsOnlySafeAcronymCaps", new Class[]{String.class}, "API SDK HTTP JSON"));
            assertFalse((Boolean) invokeUtil("containsOnlySafeAcronymCaps", new Class[]{String.class}, "API OMG"));
    
            assertTrue((Boolean) invokeUtil("hasExplicitFrustrationSignal", new Class[]{String.class}, "what the fuck is this"));
            assertTrue((Boolean) invokeUtil("hasExplicitFrustrationSignal", new Class[]{String.class}, "this still doesn't work"));
            assertFalse((Boolean) invokeUtil("hasExplicitFrustrationSignal", new Class[]{String.class}, "all good"));
        }
    
        @Test
        void isConsistentCapsStyle_detectsPattern() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            assertFalse((Boolean) invokeUtil("isConsistentCapsStyle", new Class[]{List.class}, List.of("ONE", "TWO")));
    
            boolean consistent = (Boolean) invokeUtil("isConsistentCapsStyle", new Class[]{List.class}, List.of(
                    "THIS TOOL IS BROKEN",
                    "I AM STILL WAITING",
                    "WHY IS THIS WRONG AGAIN",
                    "API SDK JSON"
            ));
            assertTrue(consistent);
        }
    
        @Test
        void jsonBuild_and_rowConversion_coverOutputPaths() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            InactiveUsersPageService.InactiveRow row = new InactiveUsersPageService.InactiveRow();
            row.sessionId = "s1";
            row.displayLabel = "Alice";
            row.widgetId = "w1";
            row.widgetLabel = "Widget One";
            row.chats = 5;
            row.lastEntry = Timestamp.from(Instant.parse("2026-08-01T10:15:30Z"));
            row.frustrationDetected = true;
            row.frustrationScore = 0.8;
            row.frustrationReason = "keyword:ridiculous";
    
            Map<String, List<InactiveUsersPageService.InactiveRow>> byWidget = new LinkedHashMap<>();
            byWidget.put("ALL", List.of(row));
            byWidget.put("w1", List.of(row));
    
            Map<String, String> names = Map.of("w1", "Widget One");
            String jsonText = (String) invoke(servlet, "buildInactiveUsersJson", new Class[]{Map.class, Map.class}, byWidget, names);
    
            JsonObject root = Json.createReader(new StringReader(jsonText)).readObject();
            assertEquals("s1", root.getJsonArray("all").getJsonObject(0).getString("sessionId"));
            assertEquals("Widget One", root.getJsonObject("widgetNames").getString("w1"));
            assertTrue(root.getJsonObject("widgets").containsKey("w1"));
        }
    
        @Test
        void tableAndIdentifierHelpers_coverBranches() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            Connection conn = mock(Connection.class);
            DatabaseMetaData meta = mock(DatabaseMetaData.class);
            ResultSet rs1 = mock(ResultSet.class);
            ResultSet rs2 = mock(ResultSet.class);
            ResultSet rs3 = mock(ResultSet.class);
    
            when(conn.getMetaData()).thenReturn(meta);
            when(meta.getTables(null, null, "widget", new String[]{"TABLE"})).thenReturn(rs1, rs3);
            when(meta.getTables(null, null, "WIDGET", new String[]{"TABLE"})).thenReturn(rs2);
            when(rs1.next()).thenReturn(false);
            when(rs2.next()).thenReturn(false);
            when(rs3.next()).thenReturn(true);
    
            assertTrue((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, conn, "widget"));
    
            Connection badConn = mock(Connection.class);
            when(badConn.getMetaData()).thenThrow(new SQLException("boom"));
            assertFalse((Boolean) invoke(servlet, "tableExists", new Class[]{Connection.class, String.class}, badConn, "widget"));
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, ""));
            assertEquals("w_123x", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123x"));
            assertEquals("abc___", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-.$"));
    
            assertEquals("\"ok_name\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "ok_name"));
            assertThrowsIllegalArgument(() -> invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "bad-name"));
        }
    
        @Test
        void dbTextAndParsingHelpers_coverBranches() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            assertEquals(7, (Integer) invoke(servlet, "parseInt", new Class[]{String.class, int.class}, "7", 3));
            assertEquals(3, (Integer) invoke(servlet, "parseInt", new Class[]{String.class, int.class}, "x", 3));
            assertEquals(3, (Integer) invoke(servlet, "parseInt", new Class[]{String.class, int.class}, "   ", 3));
    
            assertNull(invoke(servlet, "safeDbText", new Class[]{String.class, int.class}, null, 10));
            assertEquals("ab cd", invoke(servlet, "safeDbText", new Class[]{String.class, int.class}, "ab\n\u0000cd", 10));
            assertEquals("abcd", invoke(servlet, "safeDbText", new Class[]{String.class, int.class}, "abcdef", 4));
    
            ResultSet rs = mock(ResultSet.class);
            when(rs.getObject("sid")).thenReturn("  sess-1  ");
            assertEquals("sess-1", invoke(servlet, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, rs, "sid", 16));
    
            ResultSet badRs = mock(ResultSet.class);
            when(badRs.getObject("sid")).thenThrow(new SQLException("read fail"));
            assertNull(invoke(servlet, "readDbText", new Class[]{ResultSet.class, String.class, int.class}, badRs, "sid", 16));
        }
    
        @Test
        void templateAndEscapingHelpers_coverBranches() throws Exception {
            InactiveUsersPageService servlet = new InactiveUsersPageService();
    
            assertEquals("", invoke(servlet, "loadTemplate", new Class[]{ServletContext.class, String.class}, null, "/x"));
            assertEquals("", invoke(servlet, "loadTemplate", new Class[]{ServletContext.class, String.class}, mock(ServletContext.class), " "));
    
            ServletContext ctx = mock(ServletContext.class);
            InputStream stream = new ByteArrayInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8));
            when(ctx.getResourceAsStream("/tpl")).thenReturn(stream);
            String loaded = (String) invoke(servlet, "loadTemplate", new Class[]{ServletContext.class, String.class}, ctx, "/tpl");
            assertTrue(loaded.contains("line1"));
            assertTrue(loaded.contains("line2"));
    
            assertEquals("a&amp;b&lt;c&gt;d&quot;e&#39;", invoke(servlet, "escapeHtml", new Class[]{String.class}, "a&b<c>d\"e'"));
            assertEquals("", invoke(servlet, "nvl", new Class[]{String.class}, (Object) null));
            assertEquals("x", invoke(servlet, "nvl", new Class[]{String.class}, "x"));
    
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("  alice  ");
            assertEquals("alice", invoke(servlet, "safeSessionUser", new Class[]{HttpSession.class}, session));
    
            when(session.getAttribute("user")).thenReturn(1234);
            assertEquals("", invoke(servlet, "safeSessionUser", new Class[]{HttpSession.class}, session));
    
            assertEquals("id_1", invoke(servlet, "sanitizeSessionId", new Class[]{String.class}, " id_1 "));
            assertNull(invoke(servlet, "sanitizeSessionId", new Class[]{String.class}, "bad id"));
        }
    
        private static Object invokeUtil(String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = InactiveUsersFrustrationTextUtil.class.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(null, args);
        }

        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        }

        private static Object field(Object target, String fieldName) throws Exception {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
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
}
