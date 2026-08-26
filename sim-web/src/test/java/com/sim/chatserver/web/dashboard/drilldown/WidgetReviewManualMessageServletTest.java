package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.model.review.BatchFailure;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class WidgetReviewManualMessageServletTest {

    @Test
    void doPost_withoutSession_returnsUnauthorizedJson() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Authentication required"));
    }

    @Test
    void doPost_withInvalidPayloadLength_returnsBadRequestJson() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Invalid JSON payload"));
    }

    @Test
    void doPost_withBlankMessage_returnsBadRequestJson() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter out = new StringWriter();

        String body = "{\"message\":\"   \",\"mode\":\"chat\"}";
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("message is required"));
    }

    @Test
    void selectionAndIdHelpers_coverDistinctIntersectSubtractAndSort() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        List<SelectedEntry> sampleEntries = List.of(
                new SelectedEntry("  A  ", "p", "r", "2026-08-01T00:00:00Z", "s1"),
                new SelectedEntry("", "", "", "", ""),
                new SelectedEntry("b", "p2", "r2", "2026-08-02T00:00:00Z", "s2")
        );

        @SuppressWarnings("unchecked")
        List<String> allIds = (List<String>) invoke(servlet, "extractAllIds", new Class<?>[]{List.class}, sampleEntries);
        assertEquals(List.of("a", "b"), allIds);

        @SuppressWarnings("unchecked")
        List<String> distinct = (List<String>) invoke(servlet, "distinctIds", new Class<?>[]{List.class}, java.util.Arrays.asList(" A ", "a", "B", "", null, "b"));
        assertEquals(List.of("a", "b"), distinct);

        @SuppressWarnings("unchecked")
        List<String> subtracted = (List<String>) invoke(servlet, "subtract", new Class<?>[]{List.class, List.class}, List.of("a", "b", "c"), List.of("B", "c"));
        assertEquals(List.of("a"), subtracted);

        @SuppressWarnings("unchecked")
        List<String> intersected = (List<String>) invoke(servlet, "intersect", new Class<?>[]{List.class, List.class}, List.of("A", "b", "d"), List.of("b", "c", "a"));
        assertEquals(List.of("a", "b"), intersected);

        JsonObject payload = Json.createObjectBuilder()
                .add("selectedEntries", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("chatId", "c1").add("prompt", "p1").add("response", "r1").add("createdAt", "2026-08-24T10:00:00Z").add("sessionId", "s1"))
                        .add(Json.createObjectBuilder().add("chatId", "").add("prompt", "").add("response", ""))
                        .add(Json.createObjectBuilder().add("chatId", "c2").add("prompt", "p2").add("response", "r2").add("createdAt", "2026-08-25T10:00:00Z").add("sessionId", "s2")))
                .build();

        @SuppressWarnings("unchecked")
        List<SelectedEntry> parsed = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class<?>[]{JsonObject.class}, payload);
        assertEquals(2, parsed.size());
        assertEquals("c2", parsed.get(0).getChatId());
        assertEquals("c1", parsed.get(1).getChatId());

        assertEquals("hello", invoke(servlet, "stripClientInjectedContext", new Class<?>[]{String.class}, "hello\n\nSelected chats context:\nextra"));
    }

    @Test
    void payloadAndUtilityHelpers_coverNormalizationAndBounds() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        JsonObject payload = Json.createObjectBuilder()
                .add("attachments", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("name", "doc.txt").add("mime", "text/plain").add("contentString", "abc"))
                        .add(Json.createObjectBuilder().add("name", " ").add("mime", "text/plain").add("contentString", "abc"))
                        .add("not-object"))
                .build();

        JsonArray normalized = (JsonArray) invoke(servlet, "normalizeAttachments", new Class<?>[]{JsonObject.class}, payload);
        assertEquals(1, normalized.size());
        assertEquals("doc.txt", normalized.getJsonObject(0).getString("name"));

        String outbound = (String) invoke(servlet, "buildOutboundMessage", new Class<?>[]{String.class, String.class, int.class}, "hello", "ctx", 200);
        assertTrue(outbound.contains("Selected chats context"));

        String truncatedBase = (String) invoke(servlet, "buildOutboundMessage", new Class<?>[]{String.class, String.class, int.class}, "abcdefgh", "ctx", 5);
        assertEquals("abcde", truncatedBase);

        assertEquals("7", invoke(servlet, "str", new Class<?>[]{JsonObject.class, String.class}, Json.createObjectBuilder().add("n", 7).build(), "n"));
        assertEquals("", invoke(servlet, "str", new Class<?>[]{JsonObject.class, String.class}, Json.createObjectBuilder().build(), "missing"));

        BatchFailure failure = BatchFailure.builder()
                .requestId("req-1")
                .batchIndex(1)
                .totalBatches(2)
                .reasonCode("timeout")
                .message("timed out")
                .build();

        @SuppressWarnings("unchecked")
        List<String> reasons = (List<String>) invoke(servlet, "buildCoverageReasons", new Class<?>[]{List.class, List.class}, List.of(failure), List.of("chat-a"));
        assertTrue(reasons.get(0).contains("batch 1"));
        assertTrue(reasons.get(1).contains("missing inline evidence"));

        assertTrue((boolean) invoke(servlet, "causedByInterrupted", new Class<?>[]{Throwable.class}, new IllegalStateException(new IOException(new InterruptedException("stop")))));
        assertFalse((boolean) invoke(servlet, "causedByInterrupted", new Class<?>[]{Throwable.class}, new IllegalArgumentException("x")));

        assertEquals("message text", invoke(servlet, "extractPrimaryText", new Class<?>[]{String.class}, "{\"message\":\"message text\"}"));
        assertEquals("raw body", invoke(servlet, "extractPrimaryText", new Class<?>[]{String.class}, "raw body"));
    }

    @Test
    void urlAndSanitizationHelpers_coverBaseUriAndTextRules() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        ServerConfig cfgConnection = new ServerConfig("", 0, "https://api.example.com/", "k", "ws");
        assertEquals("https://api.example.com", invoke(servlet, "buildBaseUrl", new Class<?>[]{ServerConfig.class}, cfgConnection));

        ServerConfig cfgHost = new ServerConfig("api.example.com", 8443, null, "k", "ws");
        assertEquals("https://api.example.com:8443", invoke(servlet, "buildBaseUrl", new Class<?>[]{ServerConfig.class}, cfgHost));

        assertEquals("https://example.com", invoke(servlet, "sanitizeBaseUrl", new Class<?>[]{String.class}, "https://https://example.com/"));
        assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class<?>[]{String.class}, "::bad-url::"));

        assertNotNull(invoke(servlet, "toSafeUri", new Class<?>[]{String.class}, "https://api.example.com/v1"));
        assertNull(invoke(servlet, "toSafeUri", new Class<?>[]{String.class}, "ftp://api.example.com"));

        assertEquals("AB\nC", invoke(servlet, "canonicalizeForValidation", new Class<?>[]{String.class}, " A\u0000B\r\nC "));
        assertEquals("ab\ncd", invoke(servlet, "validateTaintedRequestBody", new Class<?>[]{String.class}, "ab\u0001\ncd"));

        assertEquals("workspace-name", invoke(servlet, "buildSlug", new Class<?>[]{String.class}, " Workspace Name "));
        assertEquals("https://api.example.com", invoke(servlet, "stripTrailingSlash", new Class<?>[]{String.class}, "https://api.example.com/"));
        assertEquals("abc", invoke(servlet, "trimTo", new Class<?>[]{String.class, int.class}, "abcdef", 3));

        @SuppressWarnings("unchecked")
        Set<String> csvSet = (Set<String>) invokeStatic("parseCsvToSet", new Class<?>[]{String.class}, " A, b, A ,, c ");
        assertEquals(Set.of("a", "b", "c"), csvSet);
        assertEquals("fallback", invokeStatic("defaultIfBlank", new Class<?>[]{String.class, String.class}, " ", "fallback"));
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object invokeStatic(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = WidgetReviewManualMessageServlet.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}
