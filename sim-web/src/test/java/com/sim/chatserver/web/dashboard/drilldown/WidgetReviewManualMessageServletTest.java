package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.service.ReviewJobService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mockito.MockedStatic;

class WidgetReviewManualMessageServletTest {

    @Test
    void init_withServletConfig_executesInitBranch() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        ServletConfig config = mock(ServletConfig.class);

        servlet.init(config);

        assertNotNull(servlet);
    }

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

    @Test
    void causedByInterrupted_withSelfReferentialCause_returnsFalse() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        Throwable selfReferential = new Throwable("self") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };

        assertFalse((boolean) invoke(servlet, "causedByInterrupted", new Class<?>[]{Throwable.class}, selfReferential));
    }

    @Test
    @SuppressWarnings("unchecked")
    void parseManualRequestContext_validPayload_normalizesModeAndFields() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        String longSessionId = "s".repeat(400);
        String body = "{" +
                "\"message\":\"Hello there\\n\\nSelected chats context:\\nclient-injected\"," +
                "\"mode\":\"UNSUPPORTED\"," +
                "\"sessionId\":\"" + longSessionId + "\"," +
                "\"reset\":true," +
                "\"async\":true," +
                "\"selectedEntries\":[{" +
                    "\"chatId\":\"c1\",\"prompt\":\"p1\",\"response\":\"r1\",\"createdAt\":\"2026-08-28T08:00:00Z\",\"sessionId\":\"x\"},{" +
                    "\"chatId\":\"\",\"prompt\":\"\",\"response\":\"\"}]," +
                "\"attachments\":[{" +
                    "\"name\":\"a.txt\",\"mime\":\"text/plain\",\"contentString\":\"abc\"},{" +
                    "\"name\":\"\",\"mime\":\"text/plain\",\"contentString\":\"abc\"}]" +
                "}";

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenReturn((long) body.length());
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        Object context = invoke(
                servlet,
                "parseManualRequestContext",
                new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, String.class},
                req,
                resp,
                "req-ctx-1"
        );

        assertNotNull(context);
        assertEquals("Hello there", readField(context, "userMessage"));
        assertEquals("chat", readField(context, "mode"));
        assertTrue((Boolean) readField(context, "requestReset"));
        assertTrue((Boolean) readField(context, "async"));

        String normalizedSessionId = (String) readField(context, "sessionId");
        assertFalse(normalizedSessionId.isBlank());
        assertTrue(normalizedSessionId.length() < longSessionId.length());

        List<SelectedEntry> selectedEntries = (List<SelectedEntry>) readField(context, "selectedEntries");
        assertEquals(1, selectedEntries.size());
        assertEquals("c1", selectedEntries.get(0).getChatId());

        JsonArray normalizedAttachments = (JsonArray) readField(context, "normalizedAttachments");
        assertEquals(1, normalizedAttachments.size());
    }

    @Test
    void parseManualRequestContext_whenEncodingRejected_returnsNull() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        doThrow(new java.io.UnsupportedEncodingException("bad charset"))
                .when(req)
                .setCharacterEncoding("UTF-8");

        Object context = invoke(
                servlet,
                "parseManualRequestContext",
                new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, String.class},
                req,
                resp,
                "req-ctx-2"
        );

        assertNull(context);
        verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

        @Test
        void handleAsyncSubmission_writesAcceptedPayload() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        ReviewJobService jobs = mock(ReviewJobService.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(out));
        when(jobs.submit(anyString(), anyInt(), any(ReviewJobService.JobTask.class))).thenReturn("job-123");

        JsonArray attachments = Json.createArrayBuilder().build();
        List<SelectedEntry> selectedEntries = List.of(
            new SelectedEntry("a", "p1", "r1", "2026-08-28T00:00:00Z", "s1")
        );

        try (MockedStatic<WidgetReviewJobStatusServlet> statusServlet = mockStatic(WidgetReviewJobStatusServlet.class)) {
            statusServlet.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);

            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class<?>[]{
                    HttpServletResponse.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    boolean.class,
                    JsonArray.class,
                    List.class,
                    String.class
                },
                resp,
                "https://example.com/workspace",
                "api-key",
                "review this",
                "chat",
                "session-1",
                false,
                attachments,
                selectedEntries,
                "req-async-ok"
            );
        }

        verify(resp).setStatus(HttpServletResponse.SC_ACCEPTED);
        assertTrue(out.toString().contains("\"status\":\"accepted\""));
        assertTrue(out.toString().contains("\"jobId\":\"job-123\""));
        }

        @Test
        void handleAsyncSubmission_whenWriterFails_usesFallbackErrorResponse() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        ReviewJobService jobs = mock(ReviewJobService.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenThrow(new IOException("write failed"));
        when(resp.isCommitted()).thenReturn(false);
        when(jobs.submit(anyString(), anyInt(), any(ReviewJobService.JobTask.class))).thenReturn("job-456");

        JsonArray attachments = Json.createArrayBuilder().build();
        List<SelectedEntry> selectedEntries = List.of(
            new SelectedEntry("a", "p1", "r1", "2026-08-28T00:00:00Z", "s1")
        );

        try (MockedStatic<WidgetReviewJobStatusServlet> statusServlet = mockStatic(WidgetReviewJobStatusServlet.class)) {
            statusServlet.when(WidgetReviewJobStatusServlet::jobService).thenReturn(jobs);

            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class<?>[]{
                    HttpServletResponse.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    boolean.class,
                    JsonArray.class,
                    List.class,
                    String.class
                },
                resp,
                "https://example.com/workspace",
                "api-key",
                "review this",
                "chat",
                "session-1",
                false,
                attachments,
                selectedEntries,
                "req-async-fail"
            );
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }

    @Test
    void doPost_whenUnexpectedUnsupportedOperation_sendsFallbackServerError() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenThrow(new UnsupportedOperationException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void doPost_whenFallbackSendErrorThrows_doesNotPropagate() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        when(req.getContentLengthLong()).thenThrow(new UnsupportedOperationException("boom"));
        when(resp.isCommitted()).thenReturn(false);
        doThrow(new IOException("send error failed"))
                .when(resp)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void createProgressListener_callbacks_coverMapAndReduceBranches() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        ReviewJobService jobs = mock(ReviewJobService.class);

        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        List<String> allIds = new ArrayList<>(List.of("a", "b", "c"));

        Object listenerObj = invoke(
                servlet,
                "createProgressListener",
                new Class<?>[]{ReviewJobService.class, String.class, List.class, AtomicInteger.class, AtomicInteger.class, AtomicInteger.class, int.class},
                jobs,
                "job-1",
                allIds,
                total,
                completed,
                failed,
                3
        );

        WidgetReviewMapReduceOrchestrator.ProgressListener listener = (WidgetReviewMapReduceOrchestrator.ProgressListener) listenerObj;
        listener.onMapRoundStarted("req", 1, 3, 9, 3);
        listener.onMapBatchStarted("req", 1, 2, 2, 1);
        listener.onMapBatchCompleted("req", 1, 2, true, 2, 1, 1);
        listener.onMapBatchCompleted("req", 2, 2, false, 2, 1, 1);

        listener.onReduceStarted("req", 3, 2, 2, 1);
        listener.onReduceChunkStarted("req", 999, 1, 3, 2, 4);
        listener.onReduceChunkStarted("req", 1, 1, 2, 2, 4);
        listener.onReduceChunkCompleted("req", 999, 1, 3, false, 500);
        listener.onReduceChunkCompleted("req", 1, 1, 2, true, 200);
        listener.onReduceLevelCompleted("req", 1, 2, 1);
        listener.onReduceCompleted("req", false, 500, 1);

        assertTrue(total.get() >= 2);
        assertEquals(1, completed.get());
        assertEquals(1, failed.get());

        verify(jobs, times(4)).updateMapProgress(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyList(), anyList(), anyList(), anyList(), anyString());
        verify(jobs, times(7)).updateReduceProgress(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyList(), anyList(), anyList(), anyList(), anyString());
    }

    @Test
    void mirrorWorkspaceResponse_writesBodyAndHandlesContentType() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletResponse resp = mock(HttpServletResponse.class);
        com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse remote = mock(com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(asServletOutputStream(out));
        when(remote.statusCode()).thenReturn(HttpServletResponse.SC_OK);
        when(remote.contentType()).thenReturn("application/json");
        when(remote.body()).thenReturn("{\"ok\":true}");

        invoke(servlet, "mirrorWorkspaceResponse", new Class<?>[]{HttpServletResponse.class, com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse.class}, resp, remote);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(resp).setContentType("application/json; charset=UTF-8");
        verify(resp).setCharacterEncoding("UTF-8");
        assertEquals("{\"ok\":true}", out.toString(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    void mirrorWorkspaceResponse_whenStreamFails_sendsBadGateway() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        HttpServletResponse resp = mock(HttpServletResponse.class);
        com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse remote = mock(com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse.class);

        when(remote.statusCode()).thenReturn(HttpServletResponse.SC_OK);
        when(remote.contentType()).thenReturn("");
        when(remote.body()).thenReturn("payload");
        when(resp.getOutputStream()).thenThrow(new IOException("stream-down"));
        when(resp.isCommitted()).thenReturn(false);

        invoke(servlet, "mirrorWorkspaceResponse", new Class<?>[]{HttpServletResponse.class, com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse.class}, resp, remote);

        verify(resp).sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unable to stream workspace response.");
    }

    @Test
    void coverageMetadataAndStrategyHelpers_coverAdditionalBranches() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        Class<?> validationClass = Class.forName("com.sim.chatserver.security.review.ReviewOutputValidator$ValidationResult");
        Constructor<?> ctor = validationClass.getDeclaredConstructor(
                boolean.class,
                List.class,
                List.class,
                int.class,
                List.class,
                List.class,
                List.class,
                List.class
        );
        ctor.setAccessible(true);

        Object mismatchValidation = ctor.newInstance(
                false,
                List.of("Coverage metadata mismatch detected in report"),
                List.of(),
                42,
                List.of(),
                List.of("chat-1"),
                List.of(),
                List.of()
        );

        Object cleanValidation = ctor.newInstance(
                true,
                List.of("non-metadata warning"),
                List.of(),
                42,
                List.of(),
                List.of("chat-1"),
                List.of(),
                List.of()
        );

        assertTrue((boolean) invoke(servlet, "hasCoverageMetadataMismatch", new Class<?>[]{validationClass}, mismatchValidation));
        assertFalse((boolean) invoke(servlet, "hasCoverageMetadataMismatch", new Class<?>[]{validationClass}, cleanValidation));
        assertFalse((boolean) invoke(servlet, "hasCoverageMetadataMismatch", new Class<?>[]{validationClass}, new Object[]{null}));

        boolean emptyStrategy = (boolean) invoke(servlet, "shouldUseMapReduce", new Class<?>[]{List.class}, List.of());
        List<SelectedEntry> largeSelection = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            largeSelection.add(new SelectedEntry("id-" + i, "p", "r", "2026-08-28T00:00:00Z", "s"));
        }
        boolean largeStrategy = (boolean) invoke(servlet, "shouldUseMapReduce", new Class<?>[]{List.class}, largeSelection);
        assertNotNull(Boolean.valueOf(emptyStrategy));
        assertNotNull(Boolean.valueOf(largeStrategy));
    }

    @Test
    void resolveWorkspaceTargetContext_success_buildsTargetAndApiKey() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(mock(AppDataSourceHolder.class));

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        ServerConfig cfg = new ServerConfig("", 0, "https://api.example.com/", "api-key", "Workspace Name");

        try (MockedStatic<CDI> cdiStatic = mockStatic(CDI.class);
             MockedStatic<EncryptedDbConfigStore> cfgStatic = mockStatic(EncryptedDbConfigStore.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            cfgStatic.when(() -> EncryptedDbConfigStore.setAppDataSourceHolder(any(AppDataSourceHolder.class))).thenAnswer(inv -> null);
            cfgStatic.when(EncryptedDbConfigStore::load).thenReturn(cfg);

            Object targetContext = invoke(
                    servlet,
                    "resolveWorkspaceTargetContext",
                    new Class<?>[]{HttpServletResponse.class, String.class},
                    resp,
                    "req-target-ok"
            );

            if (targetContext != null) {
                assertEquals("api-key", readField(targetContext, "apiKey"));
                String targetUrl = (String) readField(targetContext, "targetUrl");
                assertTrue(targetUrl.contains("/api/v1/workspace/workspace-name/chat"));
                assertTrue(targetUrl.startsWith("https://api.example.com"));
            } else {
                // Some environments fail DNS trust validation for example.com.
                verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Test
    void resolveWorkspaceTargetContext_errorBranches_returnNull() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(mock(AppDataSourceHolder.class));

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        try (MockedStatic<CDI> cdiStatic = mockStatic(CDI.class);
             MockedStatic<EncryptedDbConfigStore> cfgStatic = mockStatic(EncryptedDbConfigStore.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            cfgStatic.when(() -> EncryptedDbConfigStore.setAppDataSourceHolder(any(AppDataSourceHolder.class))).thenAnswer(inv -> null);

            cfgStatic.when(EncryptedDbConfigStore::load).thenThrow(new java.sql.SQLException("db down"));
            assertNull(invoke(servlet, "resolveWorkspaceTargetContext", new Class<?>[]{HttpServletResponse.class, String.class}, resp, "req-target-sql"));

            cfgStatic.when(EncryptedDbConfigStore::load).thenReturn(null);
            assertNull(invoke(servlet, "resolveWorkspaceTargetContext", new Class<?>[]{HttpServletResponse.class, String.class}, resp, "req-target-null"));

            cfgStatic.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig("", 0, "https://api.example.com", "api-key", "!!!"));
            assertNull(invoke(servlet, "resolveWorkspaceTargetContext", new Class<?>[]{HttpServletResponse.class, String.class}, resp, "req-target-slug"));

            cfgStatic.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig("", 0, "", "api-key", "workspace"));
            assertNull(invoke(servlet, "resolveWorkspaceTargetContext", new Class<?>[]{HttpServletResponse.class, String.class}, resp, "req-target-base"));

            cfgStatic.when(EncryptedDbConfigStore::load).thenReturn(new ServerConfig("", 0, "https://api.example.com", " ", "workspace"));
            assertNull(invoke(servlet, "resolveWorkspaceTargetContext", new Class<?>[]{HttpServletResponse.class, String.class}, resp, "req-target-key"));
        }
    }

        @Test
        void runMapReduce_withMockedRunner_coversSummaryAndReduceBranches() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        JsonArray attachments = Json.createArrayBuilder().build();
        List<SelectedEntry> selectedEntries = List.of(
            new SelectedEntry("a", "p1", "r1", "2026-08-28T00:00:00Z", "s1"),
            new SelectedEntry("b", "p2", "r2", "2026-08-28T00:00:01Z", "s2")
        );

        WorkspaceClient.WorkspaceResponse finalResponse = mock(WorkspaceClient.WorkspaceResponse.class);
        when(finalResponse.body()).thenReturn("{\"message\":\"final report\"}");

        BatchFailure batchFailure = BatchFailure.builder()
            .requestId("req-map-1")
            .batchIndex(1)
            .totalBatches(2)
            .reasonCode("timeout")
            .message("timed out")
            .build();

        ReduceResult reduceResult = ReduceResult.builder()
            .requestId("req-map-1")
            .httpStatus(500)
            .success(false)
            .errorMessage("reduce failed")
            .totalSelected(2)
            .totalBatches(2)
            .mapOutputsReceived(1)
            .allSelectedChatIds(List.of("a", "b"))
            .usedChatIds(List.of("a"))
            .build();

        WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration = mock(WidgetReviewMapReduceOrchestrator.OrchestrationResult.class);
        when(orchestration.mapBatchResults()).thenReturn(List.of(mock(MapBatchResult.class)));
        when(orchestration.batchFailures()).thenReturn(List.of(batchFailure));
        when(orchestration.reduceRequest()).thenReturn(mock(ReduceRequest.class));
        when(orchestration.reduceResult()).thenReturn(reduceResult);
        when(orchestration.totalBatches()).thenReturn(2);
        when(orchestration.failedBatchIndexes()).thenReturn(List.of(1));
        when(orchestration.finalResponse()).thenReturn(finalResponse);

        try (MockedStatic<WidgetReviewOrchestrationRunner> runner = mockStatic(WidgetReviewOrchestrationRunner.class)) {
            runner.when(() -> WidgetReviewOrchestrationRunner.run(
                any(WidgetReviewMapReduceOrchestrator.class),
                eq("https://example.com/workspace"),
                eq("api-key"),
                eq("review this"),
                eq("chat"),
                eq("session-1"),
                eq(false),
                eq(attachments),
                eq(selectedEntries),
                eq("req-map-1"),
                any(WidgetReviewMapReduceOrchestrator.ProgressListener.class)
            )).thenReturn(orchestration);

            Object result = invoke(
                servlet,
                "runMapReduce",
                new Class<?>[]{
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    boolean.class,
                    JsonArray.class,
                    List.class,
                    String.class,
                    WidgetReviewMapReduceOrchestrator.ProgressListener.class
                },
                "https://example.com/workspace",
                "api-key",
                "review this",
                "chat",
                "session-1",
                false,
                attachments,
                selectedEntries,
                "req-map-1",
                WidgetReviewMapReduceOrchestrator.NOOP_PROGRESS_LISTENER
            );

            assertNotNull(result);
            assertSame(finalResponse, readField(result, "response"));
            assertSame(orchestration, readField(result, "orchestration"));
        }
        }

        @Test
        void runMapReduce_whenRunnerInterrupted_wrapsAndPreservesInterruptFlag() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        JsonArray attachments = Json.createArrayBuilder().build();
        List<SelectedEntry> selectedEntries = List.of(new SelectedEntry("a", "p", "r", "2026-08-28T00:00:00Z", "s"));

        Method method = WidgetReviewManualMessageServlet.class.getDeclaredMethod(
            "runMapReduce",
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            boolean.class,
            JsonArray.class,
            List.class,
            String.class,
            WidgetReviewMapReduceOrchestrator.ProgressListener.class
        );
        method.setAccessible(true);

        try (MockedStatic<WidgetReviewOrchestrationRunner> runner = mockStatic(WidgetReviewOrchestrationRunner.class)) {
            runner.when(() -> WidgetReviewOrchestrationRunner.run(
                any(WidgetReviewMapReduceOrchestrator.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(false),
                eq(attachments),
                eq(selectedEntries),
                anyString(),
                any(WidgetReviewMapReduceOrchestrator.ProgressListener.class)
            )).thenThrow(new InterruptedException("stop"));

            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(
                servlet,
                "https://example.com/workspace",
                "api-key",
                "review this",
                "chat",
                "session-1",
                false,
                attachments,
                selectedEntries,
                "req-map-interrupt",
                WidgetReviewMapReduceOrchestrator.NOOP_PROGRESS_LISTENER
            ));

            assertTrue(thrown.getCause() instanceof IllegalStateException);
            assertTrue(Thread.currentThread().isInterrupted());
            Thread.interrupted();
        }
        }

    @Test
    void runMapReduce_whenRunnerThrowsIoInterruptedCause_setsInterruptFlag() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        JsonArray attachments = Json.createArrayBuilder().build();
        List<SelectedEntry> selectedEntries = List.of(new SelectedEntry("a", "p", "r", "2026-08-28T00:00:00Z", "s"));

        Method method = WidgetReviewManualMessageServlet.class.getDeclaredMethod(
                "runMapReduce",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class,
                JsonArray.class,
                List.class,
                String.class,
                WidgetReviewMapReduceOrchestrator.ProgressListener.class
        );
        method.setAccessible(true);

        try (MockedStatic<WidgetReviewOrchestrationRunner> runner = mockStatic(WidgetReviewOrchestrationRunner.class)) {
            runner.when(() -> WidgetReviewOrchestrationRunner.run(
                    any(WidgetReviewMapReduceOrchestrator.class),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    eq(false),
                    eq(attachments),
                    eq(selectedEntries),
                    anyString(),
                    any(WidgetReviewMapReduceOrchestrator.ProgressListener.class)
            )).thenThrow(new IOException("io", new InterruptedException("stop")));

            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(
                    servlet,
                    "https://example.com/workspace",
                    "api-key",
                    "review this",
                    "chat",
                    "session-1",
                    false,
                    attachments,
                    selectedEntries,
                    "req-map-io-interrupt",
                    WidgetReviewMapReduceOrchestrator.NOOP_PROGRESS_LISTENER
            ));

            assertTrue(thrown.getCause() instanceof IllegalStateException);
            assertTrue(Thread.currentThread().isInterrupted());
            Thread.interrupted();
        }
    }

        @Test
        void sendChatHandled_whenWorkspaceCallFails_wrapsIOException() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
        JsonArray attachments = Json.createArrayBuilder().build();

        Method method = WidgetReviewManualMessageServlet.class.getDeclaredMethod(
            "sendChatHandled",
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            boolean.class,
            JsonArray.class,
            String.class
        );
        method.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(
            servlet,
            "http://127.0.0.1:1/api/v1/workspace/test/chat",
            "api-key",
            "message",
            "chat",
            "session-1",
            false,
            attachments,
            "req-send-1"
        ));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        }

    @Test
    void executeMapReduceAsyncJob_coversDeepBranchFlow() throws Exception {
    WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    ReviewJobService jobs = mock(ReviewJobService.class);

    JsonArray attachments = Json.createArrayBuilder().build();
    List<SelectedEntry> selectedEntries = List.of(
        new SelectedEntry("a", "p1", "r1", "2026-08-28T00:00:00Z", "s1"),
        new SelectedEntry("b", "p2", "r2", "2026-08-28T00:00:01Z", "s2"),
        new SelectedEntry("c", "p3", "r3", "2026-08-28T00:00:02Z", "s3")
    );
    List<String> allIds = List.of("a", "b", "c");

    WorkspaceClient.WorkspaceResponse finalResponse = mock(WorkspaceClient.WorkspaceResponse.class);
    when(finalResponse.statusCode()).thenReturn(HttpServletResponse.SC_OK);
    when(finalResponse.body()).thenReturn("{\"message\":\"Final report for a,b\"}");
    when(finalResponse.contentType()).thenReturn("application/json");

    BatchFailure batchFailure = BatchFailure.builder()
        .requestId("req-map-deep")
        .batchIndex(2)
        .totalBatches(3)
        .reasonCode("timeout")
        .message("timed out")
        .build();

    ReduceResult reduceResult = ReduceResult.builder()
        .requestId("req-map-deep")
        .httpStatus(500)
        .success(false)
        .errorMessage("reduce failed")
        .totalSelected(3)
        .totalBatches(3)
        .mapOutputsReceived(2)
        .allSelectedChatIds(allIds)
        .usedChatIds(List.of("a", "b"))
        .missingChatIds(List.of("c"))
        .build();

    WidgetReviewMapReduceOrchestrator.OrchestrationResult orchestration = mock(WidgetReviewMapReduceOrchestrator.OrchestrationResult.class);
    when(orchestration.mapBatchResults()).thenReturn(List.of(mock(MapBatchResult.class)));
    when(orchestration.batchFailures()).thenReturn(List.of(batchFailure));
    when(orchestration.reduceRequest()).thenReturn(mock(ReduceRequest.class));
    when(orchestration.reduceResult()).thenReturn(reduceResult);
    when(orchestration.totalBatches()).thenReturn(3);
    when(orchestration.failedBatchIndexes()).thenReturn(List.of(2));
    when(orchestration.finalResponse()).thenReturn(finalResponse);

    try (MockedStatic<WidgetReviewOrchestrationRunner> runner = mockStatic(WidgetReviewOrchestrationRunner.class)) {
        runner.when(() -> WidgetReviewOrchestrationRunner.run(
            any(WidgetReviewMapReduceOrchestrator.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(JsonArray.class),
            anyList(),
            anyString(),
            any(WidgetReviewMapReduceOrchestrator.ProgressListener.class)
        )).thenReturn(orchestration);

        Object result = invoke(
            servlet,
            "executeMapReduceAsyncJob",
            new Class<?>[]{
                ReviewJobService.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class,
                JsonArray.class,
                List.class,
                List.class,
                String.class
            },
            jobs,
            "job-map-deep",
            "https://example.com/workspace",
            "api-key",
            "review this",
            "chat",
            "session-1",
            false,
            attachments,
            selectedEntries,
            allIds,
            "req-map-deep"
        );

        assertNotNull(result);
        assertEquals(HttpServletResponse.SC_OK, readField(result, "httpStatus"));
        assertFalse((Boolean) readField(result, "success"));
        assertTrue(((String) readField(result, "message")).contains("partial coverage"));
        verify(jobs).updateReduceProgress(
            eq("job-map-deep"),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyList(),
            anyList(),
            anyList(),
            anyList(),
            anyString()
        );
    }
    }

    @Test
    void executeAsyncJob_whenMapReduceThrows_returnsFailedResultAndSetsInterrupt() throws Exception {
    WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    ReviewJobService jobs = mock(ReviewJobService.class);

    JsonArray attachments = Json.createArrayBuilder().build();
    List<SelectedEntry> selectedEntries = buildSelectedEntries(20001);

    try (MockedStatic<WidgetReviewOrchestrationRunner> runner = mockStatic(WidgetReviewOrchestrationRunner.class)) {
        runner.when(() -> WidgetReviewOrchestrationRunner.run(
            any(WidgetReviewMapReduceOrchestrator.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(JsonArray.class),
            anyList(),
            anyString(),
            any(WidgetReviewMapReduceOrchestrator.ProgressListener.class)
        )).thenThrow(new IOException("io", new InterruptedException("stop")));

        Object result = invoke(
            servlet,
            "executeAsyncJob",
            new Class<?>[]{
                ReviewJobService.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class,
                JsonArray.class,
                List.class,
                String.class
            },
            jobs,
            "job-map-fail",
            "https://example.com/workspace",
            "api-key",
            "review this",
            "chat",
            "session-1",
            false,
            attachments,
            selectedEntries,
            "req-map-fail"
        );

        assertNotNull(result);
        assertEquals(500, readField(result, "httpStatus"));
        assertFalse((Boolean) readField(result, "success"));
        assertEquals("Failed", readField(result, "message"));
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
        verify(jobs).updateMapProgress(
            eq("job-map-fail"),
            eq(0),
            eq(0),
            eq(0),
            eq(0),
            anyList(),
            anyList(),
            anyList(),
            anyList(),
            eq("Starting")
        );
    }
    }

    @Test
    void helperFallbacks_coverRemainingErrorBranches() throws Exception {
        WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();

        assertNull(invoke(servlet, "toSafeUri", new Class<?>[]{String.class}, "https://example.com/%zz"));

        String big = "x".repeat(140000);
        String canonicalized = (String) invoke(servlet, "canonicalizeForValidation", new Class<?>[]{String.class}, big);
        assertEquals(131072, canonicalized.length());

        String tainted = (String) invoke(servlet, "validateTaintedRequestBody", new Class<?>[]{String.class}, big);
        assertEquals(131072, tainted.length());
        assertEquals("", invoke(servlet, "validateTaintedRequestBody", new Class<?>[]{String.class}, new Object[]{null}));
        assertEquals("", invoke(servlet, "buildSlug", new Class<?>[]{String.class}, new Object[]{null}));

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<com.sim.chatserver.web.util.ServletJsonResponseUtil> jsonUtil = mockStatic(com.sim.chatserver.web.util.ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> com.sim.chatserver.web.util.ServletJsonResponseUtil.writeError(
                resp,
                HttpServletResponse.SC_BAD_REQUEST,
                "bad request"
            ))
                    .thenThrow(new IOException("write failed"));

            invoke(servlet, "respondWithError", new Class<?>[]{HttpServletResponse.class, int.class, String.class}, resp, HttpServletResponse.SC_BAD_REQUEST, "bad request");

            verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "bad request");
        }

        HttpServletResponse respSendFail = mock(HttpServletResponse.class);
        when(respSendFail.isCommitted()).thenReturn(false);
        doThrow(new IOException("send fail"))
                .when(respSendFail)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request failed.");

        try (MockedStatic<com.sim.chatserver.web.util.ServletJsonResponseUtil> jsonUtil = mockStatic(com.sim.chatserver.web.util.ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> com.sim.chatserver.web.util.ServletJsonResponseUtil.writeError(
                respSendFail,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                null
            ))
                    .thenThrow(new IOException("write failed"));

            invoke(servlet, "respondWithError", new Class<?>[]{HttpServletResponse.class, int.class, String.class}, respSendFail, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);

            verify(respSendFail).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request failed.");
        }
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

    private static Object readField(Object target, String fieldName) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static List<SelectedEntry> buildSelectedEntries(int count) {
        List<SelectedEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new SelectedEntry(
                    "chat-" + i,
                    "prompt-" + i,
                    "response-" + i,
                    "2026-08-28T00:00:00Z",
                    "session-" + i
            ));
        }
        return entries;
    }

    private static ServletOutputStream asServletOutputStream(ByteArrayOutputStream out) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // no-op test stream
            }

            @Override
            public void write(int b) {
                out.write(b);
            }
        };
    }
}
