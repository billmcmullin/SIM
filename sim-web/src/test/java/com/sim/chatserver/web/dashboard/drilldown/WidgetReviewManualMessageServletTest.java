package com.sim.chatserver.web.dashboard.drilldown;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.MapReduceConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.model.review.BatchFailure;
import com.sim.chatserver.model.review.MapBatchResult;
import com.sim.chatserver.model.review.ReduceRequest;
import com.sim.chatserver.model.review.ReduceResult;
import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.security.review.ReviewOutputValidator;
import com.sim.chatserver.security.review.TrustedUrlValidator;
import com.sim.chatserver.service.PromptTemplateService;
import com.sim.chatserver.service.ReviewContextBuilderService;
import com.sim.chatserver.service.ReviewJobService;
import com.sim.chatserver.service.WidgetReviewMapReduceOrchestrator;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkspaceClient.WorkspaceResponse;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.WriteListener;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewManualMessageServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet
 * @author bmcmullin
 */
public class WidgetReviewManualMessageServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

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
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        mockBody(req, "{}");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        underTest = servletWithDataSourceHolder(dsHolderValue);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        mockBody(req, "{}");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        underTest = servletWithDataSourceHolder(dsHolderValue);
        underTest.init();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        mockBody(req, "{}");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for init()
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.WidgetReviewManualMessageServlet#init()
     * @author bmcmullin
     */
    @Test
    public void testInit() throws Throwable
    {
        // Given
        WidgetReviewManualMessageServlet underTest = new WidgetReviewManualMessageServlet();

        // When
        underTest.init();

    }

    private static void mockBody(HttpServletRequest req, String body) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        ServletInputStream inputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException
            {
                return in.read();
            }

            @Override
            public boolean isFinished()
            {
                return in.available() == 0;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {
                // No-op for unit tests.
            }
        };
        when(req.getInputStream()).thenReturn(inputStream);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
    }


    // Merged from WidgetReviewManualMessageServletCoverageTest
    
    
        @Test
        void doPost_whenUnauthenticated_returns401Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = readJson(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("authentication required"));
        }
    
        @Test
        void doPost_whenUnauthenticatedAndWriteFails_fallsBackToSendError() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenThrow(new IOException("stream down"));
            when(resp.isCommitted()).thenReturn(false);
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        }
    
        @Test
        void doPost_whenContentLengthInvalid_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn(128L * 1024L + 1L);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = readJson(out);
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase().contains("invalid json payload"));
        }
    
        @Test
        void doPost_whenJsonInvalid_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{ not-json";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("invalid json payload"));
        }
    
        @Test
        void doPost_whenMessageMissing_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"   \"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("message is required"));
        }
    
        @Test
        void doPost_whenServerConfigUnavailable_returns500Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            String longSessionId = "x".repeat(260);
            String body = """
                {
                  "message":"hello\\n\\nSelected chats context:\\nclient-added",
                  "mode":"UNSUPPORTED_MODE",
                  "sessionId":"%s",
                  "requestReset":true,
                  "selectedEntries":[
                    {"chatId":"A","prompt":"p","response":"r","createdAt":"2026-08-07T10:00:00Z","sessionId":"s1"},
                    {"chatId":"","prompt":"","response":""}
                  ],
                  "attachments":[
                    {"name":"a.txt","mime":"text/plain","contentString":"abc"},
                    {"name":"bad","mime":"","contentString":"x"}
                  ]
                }
                """.formatted(longSessionId);
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("configuration"));
        }
    
        @Test
        void doPost_whenConfigLoadReturnsNull_returns500MissingConfig() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(null);
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("missing"));
        }
    
        @Test
        void doPost_whenWorkspaceSlugBlank_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                        new ServerConfig("example.com", 443, null, "k", "   ")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("workspace slug"));
        }
    
        @Test
        void doPost_whenBaseUrlMissing_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                        new ServerConfig("   ", 0, null, "k", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("connection information"));
        }
    
        @Test
        void doPost_whenApiKeyMissing_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                        new ServerConfig("example.com", 443, null, "   ", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("api key"));
        }
    
        @Test
        void doPost_whenTrustValidationFails_returns400Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString()))
                .thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443))
                .thenReturn(TrustedUrlValidator.ValidationResult.invalid("blocked"));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                        new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("trust validation"));
        }
    
            @Test
            void doPost_whenAsyncTrue_returnsAcceptedPayload() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(800);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(600);
            when(cfg.getReduceMessageMaxChars()).thenReturn(2000);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10));
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10, List.of("a"), List.of("a"), List.of()));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse upstream = workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json");
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(upstream);
            when(client.isLikelyContextTooLarge(any())).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            String body = "{\"message\":\"hello\",\"async\":true,\"selectedEntries\":[{\"chatId\":\"a\",\"prompt\":\"p\",\"response\":\"r\"}]}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                    new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            assertEquals("accepted", accepted.getString("status"));
            assertTrue(accepted.containsKey("jobId"));
            }
    
            @Test
            void doPost_whenSyncSinglePassSucceeds_mirrorsUpstreamResponse() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(800);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(600);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse upstream = workspaceResponse(200, "{\"textResponse\":\"done\"}", "application/json");
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(upstream);
            when(client.isLikelyContextTooLarge(any())).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                    new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            assertEquals("{\"textResponse\":\"done\"}", out.toString(StandardCharsets.UTF_8));
            }
    
            @Test
            void doPost_whenSyncExecutionThrows_returns500Json() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(800);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(600);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenThrow(new IOException("downstream down"));
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                    new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("failed to process manual message"));
            }
    
            @Test
            void doPost_whenRequestEncodingThrows_sendsFallback500() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(resp.isCommitted()).thenReturn(false);
            java.io.UnsupportedEncodingException encodingError = new java.io.UnsupportedEncodingException("bad-enc");
            org.mockito.Mockito.doThrow(encodingError).when(req).setCharacterEncoding(anyString());
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
            }
    
            @Test
            void doPost_whenRequestEncodingAndSendErrorFail_swallowFallbackFailure() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(resp.isCommitted()).thenReturn(false);
            org.mockito.Mockito.doThrow(new java.io.UnsupportedEncodingException("bad-enc")).when(req).setCharacterEncoding(anyString());
            org.mockito.Mockito.doThrow(new IOException("send down")).when(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    
            servlet.doPost(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
            }
    
            @Test
            void doPost_whenSyncExecutionInterrupted_setsInterruptAndReturnsError() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(800);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(600);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenThrow(new InterruptedException("stop"));
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\"}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            JsonObject payload = readJson(out);
            assertEquals("error", payload.getString("status"));
            assertTrue(payload.getString("message").toLowerCase().contains("failed to process manual message"));
            assertTrue(Thread.interrupted());
            }
    
            @Test
            void doPost_whenSyncMapReduceSucceeds_mirrorsResponseAndUsesDefaultContentType() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            servlet = servletWithDataSourceHolder(mock(AppDataSourceHolder.class));
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of("warn"), 10));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WidgetReviewMapReduceOrchestrator orch = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orch.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String reqId = invocation.getArgument(8);
                    return buildOrchestrationResult(
                        workspaceResponse(200, "{\"textResponse\":\"map-ok\"}", ""),
                        ReduceResult.builder()
                            .requestId(reqId)
                            .httpStatus(200)
                            .success(true)
                            .totalSelected(1)
                            .totalBatches(1)
                            .mapOutputsReceived(1)
                            .allSelectedChatIds(List.of("a"))
                            .usedChatIds(List.of("a"))
                            .build(),
                        List.of(),
                        List.of(),
                        List.of("a"),
                        List.of(),
                        1
                    );
                });
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orch);
    
            TrustedUrlValidator validator = mock(TrustedUrlValidator.class);
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.valid("example.com", "https", 443));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", validator);
    
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String body = "{\"message\":\"hello\",\"selectedEntries\":[{\"chatId\":\"a\",\"prompt\":\"p\",\"response\":\"r\"}]}";
    
            HttpSession session = authedSession();
            when(req.getSession(false)).thenReturn(session);
            when(req.getContentLengthLong()).thenReturn((long) body.getBytes(StandardCharsets.UTF_8).length);
            when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (org.mockito.MockedStatic<EncryptedDbConfigStore> configStore = org.mockito.Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                configStore.when(EncryptedDbConfigStore::load).thenReturn(
                new ServerConfig("example.com", 443, null, "key-1", "workspace")
                );
    
                servlet.doPost(req, resp);
            }
    
            assertEquals("{\"textResponse\":\"map-ok\"}", out.toString(StandardCharsets.UTF_8));
            verify(resp).setContentType("application/json; charset=UTF-8");
            }
    
        @Test
        void helperMethods_payloadAndCollectionUtilities_coverBranches() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            String stripped = (String) invoke(servlet, "stripClientInjectedContext", new Class[]{String.class},
                    "hello\n\nSelected chats context:\nnoise");
            assertEquals("hello", stripped);
            assertEquals("plain", invoke(servlet, "stripClientInjectedContext", new Class[]{String.class}, "plain"));
    
            String outbound = (String) invoke(servlet, "buildOutboundMessage", new Class[]{String.class, String.class, int.class},
                    "hello", "ctx", 9);
            assertTrue(outbound.startsWith("hello"));
    
            JsonObject payload = Json.createObjectBuilder()
                    .add("selectedEntries", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("chatId", "b")
                                    .add("prompt", "p2")
                                    .add("response", "r2")
                                    .add("createdAt", "2026-01-01T00:00:00Z")
                                    .add("sessionId", "s2"))
                            .add(Json.createObjectBuilder()
                                    .add("chatId", "a")
                                    .add("prompt", "p1")
                                    .add("response", "r1")
                                    .add("createdAt", "2026-02-01T00:00:00Z")
                                    .add("sessionId", "s1"))
                            .add(Json.createObjectBuilder()))
                    .add("attachments", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("name", "x.txt")
                                    .add("mime", "text/plain")
                                    .add("contentString", "abc"))
                            .add(Json.createObjectBuilder()
                                    .add("name", "bad")
                                    .add("mime", "")
                                    .add("contentString", "abc")))
                    .build();
    
            @SuppressWarnings("unchecked")
            List<SelectedEntry> selected = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, payload);
            assertEquals(2, selected.size());
            assertEquals("a", selected.get(0).getChatId());
    
            JsonArray attachments = (JsonArray) invoke(servlet, "normalizeAttachments", new Class[]{JsonObject.class}, payload);
            assertEquals(1, attachments.size());
    
            assertEquals("123", invoke(servlet, "str", new Class[]{JsonObject.class, String.class},
                    Json.createObjectBuilder().add("n", 123).build(), "n"));
    
            @SuppressWarnings("unchecked")
            Set<String> parsed = (Set<String>) invoke(servlet, "parseCsvToSet", new Class[]{String.class}, " A, b ,,C ");
            assertTrue(parsed.containsAll(Set.of("a", "b", "c")));
    
            assertEquals("fallback", invoke(servlet, "defaultIfBlank", new Class[]{String.class, String.class}, "", "fallback"));
            assertEquals("my-workspace", invoke(servlet, "buildSlug", new Class[]{String.class}, " My Workspace !! "));
    
            @SuppressWarnings("unchecked")
            List<String> distinct = (List<String>) invoke(servlet, "distinctIds", new Class[]{List.class},
                    List.of(" A ", "a", "B", "", "b"));
            assertEquals(List.of("a", "b"), distinct);
    
            @SuppressWarnings("unchecked")
            List<String> sub = (List<String>) invoke(servlet, "subtract", new Class[]{List.class, List.class},
                    List.of("a", "b", "c"), List.of("b"));
            assertEquals(List.of("a", "c"), sub);
    
            @SuppressWarnings("unchecked")
            List<String> inter = (List<String>) invoke(servlet, "intersect", new Class[]{List.class, List.class},
                    List.of("a", "b"), List.of("b", "c"));
            assertEquals(List.of("b"), inter);
    
            BatchFailure failure = BatchFailure.of("req", 1, 1, "timeout", "x");
            @SuppressWarnings("unchecked")
            List<String> reasons = (List<String>) invoke(servlet, "buildCoverageReasons", new Class[]{List.class, List.class},
                    List.of(failure), List.of("id-1"));
            assertTrue(reasons.stream().anyMatch(r -> r.contains("batch processing timeout")));
            assertTrue(reasons.stream().anyMatch(r -> r.contains("missing inline evidence")));
    
            ReviewOutputValidator.ValidationResult mismatch = createValidationResult(
                    false,
                    List.of("Coverage metadata mismatch detected."),
                    List.of(),
                    10
            );
            assertTrue((Boolean) invoke(servlet, "hasCoverageMetadataMismatch", new Class[]{ReviewOutputValidator.ValidationResult.class}, mismatch));
        }
    
        @Test
        void helperMethods_urlAndBodyUtilities_coverBranches() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", null);
    
            assertEquals("https://example.com:8443", invoke(servlet, "buildBaseUrl", new Class[]{com.sim.chatserver.config.ServerConfig.class},
                    new com.sim.chatserver.config.ServerConfig("example.com", 8443, null, "k", "ws")));
            assertEquals("https://host", invoke(servlet, "buildBaseUrl", new Class[]{com.sim.chatserver.config.ServerConfig.class},
                    new com.sim.chatserver.config.ServerConfig("host", 0, "https://host/", "k", "ws")));
    
            assertEquals("https://example.com", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "https://EXAMPLE.com/path"));
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "ftp://example.com"));
    
            assertNotNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "https://example.com/a"));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "noscheme.example.com"));
    
            assertEquals("", invoke(servlet, "validateTaintedRequestBody", new Class[]{String.class}, new Object[]{null}));
            String cleaned = (String) invoke(servlet, "validateTaintedRequestBody", new Class[]{String.class}, "  ab\r\n\u0000c  ");
            assertTrue(cleaned.contains("ab"));
            assertTrue(cleaned.contains("c"));
            assertFalse(cleaned.contains("\u0000"));
        }
    
        @Test
        void helperMethods_loginAndExtractText_coverBranches() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            HttpServletRequest unauthReq = mock(HttpServletRequest.class);
            HttpServletResponse unauthResp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            when(unauthReq.getSession(false)).thenReturn(null);
            when(unauthResp.getOutputStream()).thenReturn(servletOutput(out));
    
            assertFalse((Boolean) invoke(servlet, "isLoggedIn", new Class[]{HttpServletRequest.class, HttpServletResponse.class}, unauthReq, unauthResp));
            assertEquals("error", readJson(out).getString("status"));
    
            HttpServletRequest authReq = mock(HttpServletRequest.class);
            HttpSession authSession = authedSession();
            when(authReq.getSession(false)).thenReturn(authSession);
            assertTrue((Boolean) invoke(servlet, "isLoggedIn", new Class[]{HttpServletRequest.class, HttpServletResponse.class}, authReq, mock(HttpServletResponse.class)));
    
            assertEquals("x", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{\"textResponse\":\"x\"}"));
            assertEquals("raw", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "raw"));
    
            List<SelectedEntry> entries = new java.util.ArrayList<>();
            entries.add(new SelectedEntry("A", "p", "r", "t", "s"));
            entries.add(new SelectedEntry("", "", "", "", ""));
            entries.add(null);
    
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) invoke(servlet, "extractAllIds", new Class[]{List.class},
                entries);
            assertEquals(List.of("a"), ids);
    
            assertEquals("abc", invoke(servlet, "trimTo", new Class[]{String.class, int.class}, "abcdef", 3));
            assertEquals("", invoke(servlet, "trimTo", new Class[]{String.class, int.class}, null, 3));
        }
    
        @Test
        void strategyAndWorkspaceHelpers_coverBranches() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig nonExhaustive = mock(MapReduceConfig.class);
            when(nonExhaustive.isExhaustiveMode()).thenReturn(false);
            when(nonExhaustive.getSinglePassMaxSelected()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", nonExhaustive);
    
            @SuppressWarnings("unchecked")
            List<SelectedEntry> many = List.of(
                    new SelectedEntry("1", "p", "r", "t", "s"),
                    new SelectedEntry("2", "p", "r", "t", "s"),
                    new SelectedEntry("3", "p", "r", "t", "s")
            );
            @SuppressWarnings("unchecked")
            List<SelectedEntry> one = List.of(new SelectedEntry("1", "p", "r", "t", "s"));
    
            assertTrue((Boolean) invoke(servlet, "shouldUseMapReduce", new Class[]{List.class}, many));
            assertFalse((Boolean) invoke(servlet, "shouldUseMapReduce", new Class[]{List.class}, one));
    
            MapReduceConfig exhaustive = mock(MapReduceConfig.class);
            when(exhaustive.isExhaustiveMode()).thenReturn(true);
            when(exhaustive.getSinglePassMaxSelected()).thenReturn(50);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", exhaustive);
    
            assertFalse((Boolean) invoke(servlet, "shouldUseMapReduce", new Class[]{List.class}, List.of()));
            assertTrue((Boolean) invoke(servlet, "shouldUseMapReduce", new Class[]{List.class}, one));
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceClient.WorkspaceResponse ok = workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json");
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                    .thenReturn(ok)
                    .thenThrow(new IOException("io fail"))
                    .thenThrow(new InterruptedException("stop"));
    
            Object first = invoke(servlet, "sendChatHandled", new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, String.class},
                    "https://example.com/api", "k", "m", "chat", "sid", false, Json.createArrayBuilder().build(), "req1");
            assertSame(ok, first);
    
            assertThrows(IllegalStateException.class, () -> invoke(servlet, "sendChatHandled", new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, String.class},
                    "https://example.com/api", "k", "m", "chat", "sid", false, Json.createArrayBuilder().build(), "req2"));
    
            assertThrows(IllegalStateException.class, () -> invoke(servlet, "sendChatHandled", new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, String.class},
                    "https://example.com/api", "k", "m", "chat", "sid", false, Json.createArrayBuilder().build(), "req3"));
            assertTrue(Thread.interrupted());
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            invoke(servlet, "mirrorWorkspaceResponse", new Class[]{HttpServletResponse.class, WorkspaceClient.WorkspaceResponse.class}, resp, ok);
            assertEquals("{\"textResponse\":\"ok\"}", out.toString(StandardCharsets.UTF_8));
    
            HttpServletResponse badResp = mock(HttpServletResponse.class);
            when(badResp.getOutputStream()).thenThrow(new IOException("down"));
            when(badResp.isCommitted()).thenReturn(false);
            invoke(servlet, "mirrorWorkspaceResponse", new Class[]{HttpServletResponse.class, WorkspaceClient.WorkspaceResponse.class}, badResp, ok);
            verify(badResp).sendError(eq(HttpServletResponse.SC_BAD_GATEWAY), anyString());
        }
    
            @Test
            void runSinglePass_whenContextTooLarge_retriesAndReturnsSecondResponse() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(500);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(300);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx-one", "ctx-two");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse first = workspaceResponse(200, "{\"textResponse\":\"first\"}", "application/json");
            WorkspaceResponse second = workspaceResponse(200, "{\"textResponse\":\"second\"}", "application/json");
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(first)
                .thenReturn(second);
            when(client.isLikelyContextTooLarge(first)).thenReturn(true);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(false, List.of("invalid"), List.of(), 10));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            @SuppressWarnings("unchecked")
            WorkspaceResponse actual = (WorkspaceResponse) invoke(
                servlet,
                "runSinglePass",
                new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                "https://example.com/api",
                "k",
                "msg",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-retry"
            );
    
            assertSame(second, actual);
            }
    
            @Test
            void runMapReduce_whenOrchestratorInterrupted_wrapsAndPreservesInterrupt() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            WidgetReviewMapReduceOrchestrator orchestrator = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orchestrator.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenThrow(new InterruptedException("stop"));
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orchestrator);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.getReduceMessageMaxChars()).thenReturn(4000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invoke(
                servlet,
                "runMapReduce",
                new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                "https://example.com/api",
                "k",
                "msg",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-int"
            ));
    
            assertTrue(ex.getMessage().contains("Map-reduce orchestration failed"));
            assertTrue(Thread.interrupted());
            }
    
            @Test
            void handleAsyncSubmission_singlePass_completesJobAndReturnsAccepted() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(5);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(1200);
            when(cfg.getRetryContextChars()).thenReturn(150);
            when(cfg.getRetryMessageMaxChars()).thenReturn(900);
            when(cfg.getReduceMessageMaxChars()).thenReturn(4000);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse upstream = workspaceResponse(
                200,
                "{\"textResponse\":\"## Executive Chat Analysis\\n## Key Metrics\\n## Risks and Opportunities\\n## Recommendations\\n## Coverage and Methodology\\n### Chat a\"}",
                "application/json"
            );
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(upstream);
            when(client.isLikelyContextTooLarge(upstream)).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            ReviewOutputValidator.ValidationResult hierarchical = createValidationResult(
                true,
                List.of(),
                List.of(),
                10,
                List.of("a"),
                List.of("a"),
                List.of()
            );
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 20));
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(hierarchical);
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-single"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            assertEquals("accepted", accepted.getString("status"));
            String jobId = accepted.getString("jobId");
    
            ReviewJobStatus status = awaitJobDone(jobId, 5000L);
            assertTrue(status.isDone());
            assertTrue(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.COMPLETED, status.getPhase());
            }
    
            @Test
            void handleAsyncSubmission_mapReduce_partialCoverage_marksFailed() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            ReviewOutputValidator.ValidationResult mismatch = createValidationResult(
                false,
                List.of("Coverage metadata mismatch detected."),
                List.of("warn"),
                50,
                List.of("a", "b"),
                List.of("a"),
                List.of("b")
            );
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt())).thenReturn(mismatch);
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WidgetReviewMapReduceOrchestrator orchestrator = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orchestrator.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String reqId = invocation.getArgument(8);
                    WidgetReviewMapReduceOrchestrator.ProgressListener listener = invocation.getArgument(9);
                    listener.onMapRoundStarted(reqId, 1, 2, 2, 1);
                    listener.onMapBatchStarted(reqId, 1, 2, 1, 1);
                    listener.onMapBatchCompleted(reqId, 1, 2, false, 1, 1, 1);
                    listener.onReduceStarted(reqId, 2, 2, 1, 1);
                    listener.onReduceChunkStarted(reqId, 999, 1, 1, 1, 1);
                    listener.onReduceChunkCompleted(reqId, 999, 1, 1, false, 502);
                    listener.onReduceLevelCompleted(reqId, 1, 1, 1);
                    listener.onReduceCompleted(reqId, false, 502, 1);
                    return buildOrchestrationResult(
                        workspaceResponse(200, "{\"textResponse\":\"report\"}", "application/json"),
                        ReduceResult.builder()
                            .requestId(reqId)
                            .httpStatus(200)
                            .success(true)
                            .errorMessage("reduce warning")
                            .totalSelected(2)
                            .totalBatches(2)
                            .mapOutputsReceived(1)
                            .allSelectedChatIds(List.of("a", "b"))
                            .usedChatIds(List.of("a"))
                            .failedBatchIndexes(List.of(1))
                            .build(),
                        List.of(1),
                        List.of(BatchFailure.of(reqId, 1, 1, "timeout", "x")),
                        List.of("a", "b"),
                        List.of("b"),
                        2
                    );
                });
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orchestrator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(
                    new SelectedEntry("a", "p", "r", "t", "s"),
                    new SelectedEntry("b", "p", "r", "t", "s")
                ),
                "req-async-mr"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            assertTrue(status.getWarnings().contains("coverage metadata mismatch"));
            }
    
            @Test
            void handleAsyncSubmission_mapReduce_withNullReduceResult_marksPartial() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 20));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WidgetReviewMapReduceOrchestrator orchestrator = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orchestrator.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String reqId = invocation.getArgument(8);
                    WidgetReviewMapReduceOrchestrator.ProgressListener listener = invocation.getArgument(9);
                    listener.onMapBatchCompleted(reqId, 1, 2, true, 1, 1, 1);
                    listener.onReduceChunkStarted(reqId, 1, 1, 1, 2, 2);
                    return buildOrchestrationResult(
                        workspaceResponse(200, "{\"textResponse\":\"report\"}", "application/json"),
                        null,
                        List.of(),
                        List.of(),
                        List.of("a", "b"),
                        List.of("b"),
                        2
                    );
                });
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orchestrator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s"), new SelectedEntry("b", "p", "r", "t", "s")),
                "req-async-null-reduce"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            }
    
            @Test
            void handleAsyncSubmission_mapReduce_whenInterruptedRuntime_returnsFailedJob() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            WidgetReviewMapReduceOrchestrator orchestrator = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orchestrator.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenThrow(new InterruptedException("stop"));
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orchestrator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-interrupt"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            assertTrue(status.getErrorMessage().contains("Map-reduce orchestration failed"));
            }
    
            @Test
            void handleAsyncSubmission_whenAcceptedWriteFails_respondsWithError() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(10);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(1200);
            when(cfg.getRetryContextChars()).thenReturn(100);
            when(cfg.getRetryMessageMaxChars()).thenReturn(800);
            when(cfg.getReduceMessageMaxChars()).thenReturn(3000);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse upstream = workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json");
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(upstream);
            when(client.isLikelyContextTooLarge(any())).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10));
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(
                    true,
                    List.of(),
                    List.of(),
                    10,
                    List.of("a"),
                    List.of("a"),
                    List.of()
                ));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.getWriter()).thenThrow(new IOException("writer down"));
            when(resp.isCommitted()).thenReturn(false);
            when(resp.getOutputStream()).thenThrow(new IOException("stream down"));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-write"
            );
    
            verify(resp).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
            }
    
            @Test
            void extractPrimaryText_handlesAlternateKeys() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            assertEquals("resp", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{\"response\":\"resp\"}"));
            assertEquals("msg", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{\"message\":\"msg\"}"));
            assertEquals("ans", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{\"answer\":\"ans\"}"));
            assertEquals("out", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{\"output\":\"out\"}"));
            assertEquals("", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, ""));
            }
    
            @Test
            void sanitizerAndHolderHelpers_coverTrustAndFallbackBranches() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", mock(TrustedUrlValidator.class));
            TrustedUrlValidator validator = (TrustedUrlValidator) getStaticField(
                WidgetReviewManualMessageServlet.class,
                "trustedUrlValidator"
            );
            when(validator.validate(anyString())).thenReturn(TrustedUrlValidator.ValidationResult.invalid("nope"));
    
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "https://example.com"));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "https://bad host"));
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            servlet = servletWithDataSourceHolder(holder);
            assertSame(holder, invoke(servlet, "dataSourceHolder", new Class[]{}));
    
            assertEquals("", invoke(servlet, "buildSlug", new Class[]{String.class}, new Object[]{null}));
            }
    
            @Test
            void handleAsyncSubmission_singlePass_whenCoverageMismatch_marksPartialWithWarnings() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(10);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(1200);
            when(cfg.getRetryContextChars()).thenReturn(100);
            when(cfg.getRetryMessageMaxChars()).thenReturn(800);
            when(cfg.getReduceMessageMaxChars()).thenReturn(3000);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(workspaceResponse(200, "{\"textResponse\":\"report\"}", "application/json"));
            when(client.isLikelyContextTooLarge(any())).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10));
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(
                    false,
                    List.of("coverage metadata mismatch detected"),
                    List.of(),
                    10,
                    List.of("a", "b"),
                    List.of(),
                    List.of("a", "b")
                ));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s"), new SelectedEntry("b", "p", "r", "t", "s")),
                "req-async-single-mismatch"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            assertTrue(status.getMessage().contains("partial coverage"));
            assertEquals("Coverage metadata mismatch.", status.getErrorMessage());
            assertTrue(status.getWarnings().contains("coverage incomplete"));
            assertTrue(status.getWarnings().contains("coverage metadata mismatch"));
            }
    
            @Test
            void handleAsyncSubmission_mapReduce_whenUpstreamFails_setsUpstreamErrorMessage() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(
                    true,
                    List.of(),
                    List.of(),
                    10,
                    List.of("a"),
                    List.of("a"),
                    List.of()
                ));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WidgetReviewMapReduceOrchestrator orch = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orch.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String reqId = invocation.getArgument(8);
                    return buildOrchestrationResult(
                        workspaceResponse(500, "{\"textResponse\":\"upstream error\"}", "application/json"),
                        ReduceResult.builder()
                            .requestId(reqId)
                            .httpStatus(500)
                            .success(false)
                            .totalSelected(1)
                            .totalBatches(1)
                            .mapOutputsReceived(1)
                            .allSelectedChatIds(List.of("a"))
                            .usedChatIds(List.of("a"))
                            .build(),
                        List.of(1),
                        List.of(),
                        List.of("a"),
                        List.of(),
                        1
                    );
                });
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orch);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-mr-upstream"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            assertTrue(status.getMessage().contains("upstream errors"));
            assertEquals("Upstream returned status 500", status.getErrorMessage());
            }
    
            @Test
            void handleAsyncSubmission_singlePass_whenUpstreamFails_setsUpstreamError() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(false);
            when(cfg.getSinglePassMaxSelected()).thenReturn(10);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(1200);
            when(cfg.getRetryContextChars()).thenReturn(100);
            when(cfg.getRetryMessageMaxChars()).thenReturn(800);
            when(cfg.getReduceMessageMaxChars()).thenReturn(3000);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(workspaceResponse(503, "{\"textResponse\":\"service unavailable\"}", "application/json"));
            when(client.isLikelyContextTooLarge(any())).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of(), 10));
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(
                    true,
                    List.of(),
                    List.of(),
                    10,
                    List.of("a"),
                    List.of("a"),
                    List.of()
                ));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-single-upstream"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertFalse(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.FAILED, status.getPhase());
            assertTrue(status.getMessage().contains("upstream errors"));
            assertEquals("Upstream returned status 503", status.getErrorMessage());
            }
    
            @Test
            void handleAsyncSubmission_mapReduce_whenFullySuccessful_marksCompleted() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.isExhaustiveMode()).thenReturn(true);
            when(cfg.getSinglePassMaxSelected()).thenReturn(50);
            when(cfg.getFinalReduceMaxAttempts()).thenReturn(2);
            when(cfg.getReduceMessageMaxChars()).thenReturn(5000);
            when(cfg.isStrictFixedBatchMode()).thenReturn(true);
            when(cfg.getFixedBatchSize()).thenReturn(5);
            when(cfg.getReduceInitialChunkSize()).thenReturn(3);
            when(cfg.getReduceMaxLevels()).thenReturn(2);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReportHierarchical(anyString(), any(), anyInt()))
                .thenReturn(createValidationResult(
                    true,
                    List.of(),
                    List.of(),
                    10,
                    List.of("a"),
                    List.of("a"),
                    List.of()
                ));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            WidgetReviewMapReduceOrchestrator orch = mock(WidgetReviewMapReduceOrchestrator.class);
            when(orch.run(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String reqId = invocation.getArgument(8);
                    return buildOrchestrationResult(
                        workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json"),
                        ReduceResult.builder()
                            .requestId(reqId)
                            .httpStatus(200)
                            .success(true)
                            .totalSelected(1)
                            .totalBatches(1)
                            .mapOutputsReceived(1)
                            .allSelectedChatIds(List.of("a"))
                            .usedChatIds(List.of("a"))
                            .build(),
                        List.of(),
                        List.of(),
                        List.of("a"),
                        List.of(),
                        1
                    );
                });
            setStaticField(WidgetReviewManualMessageServlet.class, "orchestrator", orch);
    
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter writer = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
    
            invoke(
                servlet,
                "handleAsyncSubmission",
                new Class[]{HttpServletResponse.class, String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                resp,
                "https://example.com/api",
                "k",
                "manual message",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-async-mr-success"
            );
    
            JsonObject accepted = Json.createReader(new StringReader(writer.toString())).readObject();
            ReviewJobStatus status = awaitJobDone(accepted.getString("jobId"), 5000L);
    
            assertTrue(status.isDone());
            assertTrue(status.isSuccess());
            assertEquals(ReviewJobStatus.Phase.COMPLETED, status.getPhase());
            assertEquals("Completed", status.getMessage());
            assertEquals("", status.getErrorMessage());
            }
    
            @Test
            void runSinglePass_whenValidationHasWarnings_coversWarningBranch() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            MapReduceConfig cfg = mock(MapReduceConfig.class);
            when(cfg.getSinglePassContextMaxChars()).thenReturn(200);
            when(cfg.getSinglePassMessageMaxChars()).thenReturn(500);
            when(cfg.getRetryContextChars()).thenReturn(120);
            when(cfg.getRetryMessageMaxChars()).thenReturn(300);
            setStaticField(WidgetReviewManualMessageServlet.class, "mrConfig", cfg);
    
            PromptTemplateService promptService = mock(PromptTemplateService.class);
            when(promptService.buildControlledPrompt(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn("prompt");
            setStaticField(WidgetReviewManualMessageServlet.class, "promptTemplateService", promptService);
    
            ReviewContextBuilderService contextBuilder = mock(ReviewContextBuilderService.class);
            when(contextBuilder.buildContext(anyString(), any(), anyInt())).thenReturn("ctx");
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewContextBuilderService", contextBuilder);
    
            WorkspaceClient client = mock(WorkspaceClient.class);
            WorkspaceResponse response = workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json");
            when(client.sendChat(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString()))
                .thenReturn(response);
            when(client.isLikelyContextTooLarge(response)).thenReturn(false);
            setStaticField(WidgetReviewManualMessageServlet.class, "workspaceClient", client);
    
            ReviewOutputValidator outputValidator = mock(ReviewOutputValidator.class);
            when(outputValidator.validateFinalReport(anyString(), anyInt()))
                .thenReturn(createValidationResult(true, List.of(), List.of("warn"), 10));
            setStaticField(WidgetReviewManualMessageServlet.class, "reviewOutputValidator", outputValidator);
    
            Object result = invoke(
                servlet,
                "runSinglePass",
                new Class[]{String.class, String.class, String.class, String.class, String.class, boolean.class, JsonArray.class, List.class, String.class},
                "https://example.com/api",
                "k",
                "msg",
                "chat",
                "sid",
                false,
                Json.createArrayBuilder().build(),
                List.of(new SelectedEntry("a", "p", "r", "t", "s")),
                "req-warning"
            );
    
            assertSame(response, result);
            }
    
            @Test
            void mirrorWorkspaceResponse_whenFallbackSendErrorFails_swallowIOException() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletResponse resp = mock(HttpServletResponse.class);
            WorkspaceResponse remote = workspaceResponse(200, "{\"textResponse\":\"ok\"}", "application/json");
    
            when(resp.getOutputStream()).thenThrow(new IOException("stream down"));
            when(resp.isCommitted()).thenReturn(false);
            org.mockito.Mockito.doThrow(new IOException("sendError down"))
                .when(resp)
                .sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unable to stream workspace response.");
    
            invoke(servlet, "mirrorWorkspaceResponse", new Class[]{HttpServletResponse.class, WorkspaceResponse.class}, resp, remote);
    
            verify(resp).sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unable to stream workspace response.");
            }
    
            @Test
            void helperMethods_additionalEdgeBranches_coverRemainingUtilities() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
    
            assertEquals("{}", invoke(servlet, "extractPrimaryText", new Class[]{String.class}, "{}"));
    
            @SuppressWarnings("unchecked")
            List<String> none = (List<String>) invoke(servlet, "extractAllIds", new Class[]{List.class}, new Object[]{null});
            assertTrue(none.isEmpty());
    
            assertFalse((Boolean) invoke(servlet, "hasCoverageMetadataMismatch", new Class[]{ReviewOutputValidator.ValidationResult.class}, new Object[]{null}));
            assertFalse((Boolean) invoke(
                servlet,
                "hasCoverageMetadataMismatch",
                new Class[]{ReviewOutputValidator.ValidationResult.class},
                createValidationResult(false, List.of("other error"), List.of(), 10)
            ));
    
            assertEquals("ab", invoke(servlet, "buildOutboundMessage", new Class[]{String.class, String.class, int.class}, "abc", "", 2));
            assertEquals("ab", invoke(servlet, "buildOutboundMessage", new Class[]{String.class, String.class, int.class}, "abc", "ctx", 2));
    
            JsonObject emptyPayload = Json.createObjectBuilder().build();
            JsonArray normalizedEmpty = (JsonArray) invoke(servlet, "normalizeAttachments", new Class[]{JsonObject.class}, emptyPayload);
            assertEquals(0, normalizedEmpty.size());
    
            JsonObject nullAttachmentsPayload = mock(JsonObject.class);
            when(nullAttachmentsPayload.containsKey("attachments")).thenReturn(true);
            when(nullAttachmentsPayload.get("attachments")).thenReturn(null);
            JsonArray normalizedNull = (JsonArray) invoke(servlet, "normalizeAttachments", new Class[]{JsonObject.class}, nullAttachmentsPayload);
            assertEquals(0, normalizedNull.size());
    
            JsonObject oddAttachments = Json.createObjectBuilder()
                .add("attachments", Json.createArrayBuilder()
                    .add("not-object")
                    .add(Json.createObjectBuilder().add("name", " ").add("mime", "text/plain").add("contentString", "x")))
                .build();
            JsonArray normalizedOdd = (JsonArray) invoke(servlet, "normalizeAttachments", new Class[]{JsonObject.class}, oddAttachments);
            assertEquals(0, normalizedOdd.size());
    
            JsonObject blankFieldAttachment = Json.createObjectBuilder()
                .add("attachments", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder().add("name", "ok").add("mime", " ").add("contentString", "x")))
                .build();
            JsonArray normalizedBlankField = (JsonArray) invoke(servlet, "normalizeAttachments", new Class[]{JsonObject.class}, blankFieldAttachment);
            assertEquals(0, normalizedBlankField.size());
    
            assertEquals("", invoke(servlet, "stripClientInjectedContext", new Class[]{String.class}, "   "));
    
            @SuppressWarnings("unchecked")
            List<SelectedEntry> parsedNullPayload = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, new Object[]{null});
            assertTrue(parsedNullPayload.isEmpty());
    
            @SuppressWarnings("unchecked")
            List<SelectedEntry> parsedMissing = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, emptyPayload);
            assertTrue(parsedMissing.isEmpty());
    
            JsonObject mockedPayload = mock(JsonObject.class);
            when(mockedPayload.containsKey("selectedEntries")).thenReturn(true);
            when(mockedPayload.get("selectedEntries")).thenReturn(Json.createArrayBuilder().build());
            when(mockedPayload.getJsonArray("selectedEntries")).thenReturn(null);
            @SuppressWarnings("unchecked")
            List<SelectedEntry> parsedNullArray = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, mockedPayload);
            assertTrue(parsedNullArray.isEmpty());
    
            JsonObject selectedNotArrayPayload = mock(JsonObject.class);
            when(selectedNotArrayPayload.containsKey("selectedEntries")).thenReturn(true);
            when(selectedNotArrayPayload.get("selectedEntries")).thenReturn(jakarta.json.JsonValue.TRUE);
            @SuppressWarnings("unchecked")
            List<SelectedEntry> selectedNotArray = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, selectedNotArrayPayload);
            assertTrue(selectedNotArray.isEmpty());
    
            JsonObject mixedSelectedPayload = Json.createObjectBuilder()
                .add("selectedEntries", Json.createArrayBuilder()
                    .add("skip")
                    .add(Json.createObjectBuilder()
                        .add("chatId", "id-1")
                        .add("prompt", "p")
                        .add("response", "r")
                        .add("createdAt", "2026-01-01T00:00:00Z")
                        .add("sessionId", "s")))
                .build();
            @SuppressWarnings("unchecked")
            List<SelectedEntry> mixedSelected = (List<SelectedEntry>) invoke(servlet, "parseSelectedEntries", new Class[]{JsonObject.class}, mixedSelectedPayload);
            assertEquals(1, mixedSelected.size());
    
            JsonObject strPayload = mock(JsonObject.class);
            when(strPayload.containsKey("k")).thenReturn(true);
            when(strPayload.get("k")).thenReturn(null);
            assertEquals("", invoke(servlet, "str", new Class[]{JsonObject.class, String.class}, strPayload, "k"));
    
            assertEquals("https://host.example", invoke(servlet, "buildBaseUrl", new Class[]{ServerConfig.class},
                new ServerConfig("https://host.example", 0, null, "k", "ws")));
    
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", null);
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "   "));
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "\u0000\u0000"));
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "https:///path"));
    
            TrustedUrlValidator badValidator = mock(TrustedUrlValidator.class);
            when(badValidator.validate(anyString())).thenThrow(new IllegalArgumentException("boom"));
            setStaticField(WidgetReviewManualMessageServlet.class, "trustedUrlValidator", badValidator);
            assertEquals("", invoke(servlet, "sanitizeBaseUrl", new Class[]{String.class}, "https://example.com"));
    
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, ""));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "\u0000\u0000"));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "ftp://example.com"));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "mailto:test@example.com"));
            assertNull(invoke(servlet, "toSafeUri", new Class[]{String.class}, "https://[broken"));
    
            char[] chunk = new char[140_000];
            java.util.Arrays.fill(chunk, 'a');
            String sanitizedLarge = (String) invoke(servlet, "validateTaintedRequestBody", new Class[]{String.class}, new String(chunk));
            assertEquals(128 * 1024, sanitizedLarge.length());
    
            assertEquals("", invoke(servlet, "canonicalizeForValidation", new Class[]{String.class}, new Object[]{null}));
    
            try (org.mockito.MockedStatic<com.sim.chatserver.web.util.ServletRequestParamUtil> utilMock = org.mockito.Mockito.mockStatic(com.sim.chatserver.web.util.ServletRequestParamUtil.class)) {
                utilMock.when(() -> com.sim.chatserver.web.util.ServletRequestParamUtil.normalizeBodyText(anyString(), anyInt(), anyBoolean()))
                    .thenReturn(null);
                assertEquals("", invoke(servlet, "canonicalizeForValidation", new Class[]{String.class}, "abc"));
            }
    
            char[] large = new char[140_000];
            java.util.Arrays.fill(large, 'z');
            String truncated = (String) invoke(servlet, "canonicalizeForValidation", new Class[]{String.class}, new String(large));
            assertEquals(128 * 1024, truncated.length());
    
            AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
            jakarta.enterprise.inject.Instance<AppDataSourceHolder> instance = mock(jakarta.enterprise.inject.Instance.class);
            when(instance.get()).thenReturn(holder);
            @SuppressWarnings("unchecked")
            jakarta.enterprise.inject.spi.CDI<Object> cdi = (jakarta.enterprise.inject.spi.CDI<Object>) mock(jakarta.enterprise.inject.spi.CDI.class);
            when(cdi.select(AppDataSourceHolder.class)).thenReturn((jakarta.enterprise.inject.Instance) instance);
            servlet = new WidgetReviewManualMessageServlet();
            try (org.mockito.MockedStatic<jakarta.enterprise.inject.spi.CDI> cdiMock = org.mockito.Mockito.mockStatic(jakarta.enterprise.inject.spi.CDI.class)) {
                cdiMock.when(jakarta.enterprise.inject.spi.CDI::current).thenReturn(cdi);
                assertSame(holder, invoke(servlet, "dataSourceHolder", new Class[]{}));
            }
            }
    
            @Test
            void respondWithError_whenJsonAndFallbackWriteFail_swallowFallbackFailure() throws Exception {
            WidgetReviewManualMessageServlet servlet = new WidgetReviewManualMessageServlet();
            HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.isCommitted()).thenReturn(false);
            org.mockito.Mockito.doThrow(new IOException("down"))
                .when(resp)
                .sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");
    
            try (org.mockito.MockedStatic<com.sim.chatserver.web.util.ServletJsonResponseUtil> jsonUtil = org.mockito.Mockito.mockStatic(com.sim.chatserver.web.util.ServletJsonResponseUtil.class)) {
                jsonUtil.when(() -> com.sim.chatserver.web.util.ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "bad"))
                    .thenThrow(new IOException("write down"));
    
                invoke(servlet, "respondWithError", new Class[]{HttpServletResponse.class, int.class, String.class}, resp, HttpServletResponse.SC_BAD_REQUEST, "bad");
            }
    
            verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");
            }
    
        private static HttpSession authedSession() {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("admin");
            return session;
        }
    
        private static JsonObject readJson(ByteArrayOutputStream out) {
            String raw = out.toString(StandardCharsets.UTF_8);
            return Json.createReader(new StringReader(raw)).readObject();
        }
    
        private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
    
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // No-op for tests.
                }
    
                @Override
                public void write(int b) {
                    out.write(b);
                }
            };
        }
    
        private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
            Method method = findMethodInHierarchy(target.getClass(), methodName, types);
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

        private static Method findMethodInHierarchy(Class<?> type, String methodName, Class<?>[] types) throws NoSuchMethodException {
            Class<?> current = type;
            while (current != null) {
                try {
                    return current.getDeclaredMethod(methodName, types);
                } catch (NoSuchMethodException ignored) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchMethodException(methodName);
        }
    
            private static Object getStaticField(Class<?> owner, String fieldName) throws Exception {
            try {
                Field field = owner.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(null);
            } catch (NoSuchFieldException ex) {
                Object runtime = getRuntimeHolderInstance(owner);
                Field field = runtime.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(runtime);
            }
            }
    
            private static ReviewJobStatus awaitJobDone(String jobId, long timeoutMs) throws Exception {
            long deadline = System.currentTimeMillis() + timeoutMs;
            ReviewJobService jobs = WidgetReviewJobStatusServlet.jobService();
            while (System.currentTimeMillis() < deadline) {
                ReviewJobStatus status = jobs.getStatus(jobId);
                if (status != null && status.isDone()) {
                return status;
                }
                Thread.sleep(20L);
            }
            throw new AssertionError("Timed out waiting for job completion: " + jobId);
            }
    
            private static WidgetReviewMapReduceOrchestrator.OrchestrationResult buildOrchestrationResult(
                WorkspaceResponse finalResponse,
                ReduceResult reduceResult,
                List<Integer> failedBatchIndexes,
                List<BatchFailure> batchFailures,
                List<String> allIds,
                List<String> missingIds,
                int totalBatches
            ) throws Exception {
            MapBatchResult batchResult = MapBatchResult.builder()
                .requestId("req")
                .batchIndex(1)
                .totalBatches(Math.max(1, totalBatches))
                .batchId("b1")
                .httpStatus(502)
                .success(false)
                .errorMessage("batch failed")
                .expectedChatIds(allIds)
                .foundChatIds(List.of("a"))
                .inputEntriesCount(allIds.size())
                .build();
    
            ReduceRequest reduceRequest = ReduceRequest.builder()
                .requestId("req")
                .targetUrl("https://example.com/api")
                .mode("chat")
                .sessionId("sid")
                .reset(false)
                .controlledPrompt("prompt")
                .totalSelected(allIds.size())
                .totalBatches(Math.max(1, totalBatches))
                .mapOutputs(List.of("map-out"))
                .failedBatchIndexes(failedBatchIndexes)
                .allSelectedChatIds(allIds)
                .usedChatIds(List.of("a"))
                .build();
    
            Constructor<WidgetReviewMapReduceOrchestrator.OrchestrationResult> ctor =
                WidgetReviewMapReduceOrchestrator.OrchestrationResult.class.getDeclaredConstructor(
                    WorkspaceResponse.class,
                    List.class,
                    List.class,
                    List.class,
                    ReduceRequest.class,
                    ReduceResult.class,
                    int.class,
                    int.class,
                    List.class,
                    List.class,
                    List.class,
                    boolean.class,
                    int.class
                );
            ctor.setAccessible(true);
            return ctor.newInstance(
                finalResponse,
                List.of("map-out"),
                failedBatchIndexes,
                List.of(batchResult),
                reduceRequest,
                reduceResult,
                allIds.size(),
                Math.max(1, totalBatches),
                batchFailures,
                allIds,
                missingIds,
                missingIds == null || missingIds.isEmpty(),
                1
            );
            }
    
        private static void setStaticField(Class<?> owner, String fieldName, Object value) throws Exception {
            try {
                Field field = owner.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, value);
            } catch (NoSuchFieldException ex) {
                Object runtime = getRuntimeHolderInstance(owner);
                Field field = runtime.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(runtime, value);
            }
        }

        private static Object getRuntimeHolderInstance(Class<?> owner) throws Exception {
            Class<?> holder = Class.forName(owner.getName() + "$RuntimeHolder");
            Field instance = holder.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            return instance.get(null);
        }
    
        private static WorkspaceClient.WorkspaceResponse workspaceResponse(int code, String body, String contentType) throws Exception {
            Constructor<WorkspaceClient.WorkspaceResponse> ctor = WorkspaceClient.WorkspaceResponse.class
                    .getDeclaredConstructor(int.class, String.class, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(code, body, contentType);
        }

        private static ReviewOutputValidator.ValidationResult createValidationResult(
                boolean success,
                List<String> errors,
                List<String> warnings,
                int length
        ) throws Exception {
            Constructor<ReviewOutputValidator.ValidationResult> ctor = ReviewOutputValidator.ValidationResult.class
                    .getDeclaredConstructor(boolean.class, List.class, List.class, int.class);
            ctor.setAccessible(true);
            return ctor.newInstance(success, errors, warnings, length);
        }

        private static ReviewOutputValidator.ValidationResult createValidationResult(
                boolean success,
                List<String> errors,
                List<String> warnings,
                int length,
                List<String> allSelectedChatIds,
                List<String> usedChatIds,
                List<String> missingChatIds
        ) throws Exception {
            Constructor<ReviewOutputValidator.ValidationResult> ctor = ReviewOutputValidator.ValidationResult.class
                    .getDeclaredConstructor(boolean.class, List.class, List.class, int.class, List.class, List.class, List.class);
            ctor.setAccessible(true);
            return ctor.newInstance(success, errors, warnings, length, allSelectedChatIds, usedChatIds, missingChatIds);
        }

    private WidgetReviewManualMessageServlet servletWithDataSourceHolder(AppDataSourceHolder dsHolder) {
        return new WidgetReviewManualMessageServlet() {
            @Override
            protected AppDataSourceHolder dataSourceHolder() {
                return dsHolder;
            }
        };
    }
}

