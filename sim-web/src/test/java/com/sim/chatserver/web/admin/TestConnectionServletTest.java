package com.sim.chatserver.web.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.service.ApiAuthResolver;
import jakarta.json.Json;
import jakarta.json.JsonObject;
/**
 * Parasoft Jtest UTA: Test class for TestConnectionServlet
 *
 * @see com.sim.chatserver.web.admin.TestConnectionServlet
 * @author bmcmullin
 */
public class TestConnectionServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost8() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        String getParameterResult3 = "getParameterResult3"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost9() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "http://"; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost10() throws Throwable
    {
        // Given
        TestConnectionServlet underTest = new TestConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    private static void mockJsonOutput(HttpServletResponse resp) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener)
            {
                // No-op for unit test stream.
            }

            @Override
            public void write(int b) throws IOException
            {
                out.write(b);
            }
        };
        when(resp.getOutputStream()).thenReturn(servletOut);
    }



    // Merged from TestConnectionServletCoverageTest
    
    
        @Test
        void doPost_whenUnauthenticated_returns401() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase(Locale.ROOT).contains("authentication required"));
        }
    
        @Test
        void doPost_whenHostPortMissing_returns400() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = adminSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("serverHost")).thenReturn(null);
            when(req.getParameterValues("serverPort")).thenReturn(null);
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            servlet.doPost(req, resp);
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase(Locale.ROOT).contains("host and port"));
        }
    
        @Test
        void doPost_whenApiKeyMissing_returns400() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = adminSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("serverHost")).thenReturn(new String[]{"example.com"});
            when(req.getParameterValues("serverPort")).thenReturn(new String[]{"8080"});
            when(req.getParameterValues("apiKey")).thenReturn(new String[]{""});
            when(req.getParameterValues("workspaceName")).thenReturn(new String[]{"workspace"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (MockedStatic<EncryptedDbConfigStore> cfgMock = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                cfgMock.when(EncryptedDbConfigStore::load).thenReturn(null);
                servlet.doPost(req, resp);
            }
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase(Locale.ROOT).contains("api key is required"));
        }
    
        @Test
        void doPost_whenHostOrPortInvalid_returns400() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = adminSession();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getParameterValues("serverHost")).thenReturn(new String[]{"bad host"});
            when(req.getParameterValues("serverPort")).thenReturn(new String[]{"9000"});
            when(req.getParameterValues("apiKey")).thenReturn(new String[]{"abc"});
            when(req.getParameterValues("workspaceName")).thenReturn(new String[]{"workspace"});
            when(resp.getOutputStream()).thenReturn(servletOutput(out));
    
            try (MockedStatic<EncryptedDbConfigStore> cfgMock = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
                cfgMock.when(EncryptedDbConfigStore::load).thenReturn(null);
                servlet.doPost(req, resp);
            }
    
            JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
            assertEquals("error", body.getString("status"));
            assertTrue(body.getString("message").toLowerCase(Locale.ROOT).contains("invalid host or port"));
        }
    
        @Test
        void authAndRequestHelpers_coverHeaderModesAndDedupe() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
    
            Object bearerMode = invoke(servlet, "resolvePrimaryAuthMode", new Class[]{String.class}, "authorization");
            assertEquals("AUTH_BEARER", ((Enum<?>) bearerMode).name());
    
            Object apiKeyMode = invoke(servlet, "resolvePrimaryAuthMode", new Class[]{String.class}, "x-api-key");
            assertEquals("X_API_KEY", ((Enum<?>) apiKeyMode).name());
    
            Object customMode = invoke(servlet, "resolvePrimaryAuthMode", new Class[]{String.class}, "X-Custom-Header");
            assertEquals("CUSTOM_HEADER", ((Enum<?>) customMode).name());
    
            assertEquals("token", invoke(servlet, "normalizeRawAuthorizationValue", new Class[]{String.class, String.class}, "Authorization: token", "fallback"));
            assertEquals("fallback", invoke(servlet, "normalizeRawAuthorizationValue", new Class[]{String.class, String.class}, " ", "fallback"));
    
            ApiAuthResolver.ResolvedApiAuth auth = ApiAuthResolver.resolveForServerConfigOutbound("Authorization: Bearer token123");
            Class<?> modeClass = Class.forName("com.sim.chatserver.web.admin.TestConnectionServlet$AuthHeaderMode");
            Class<?> kindClass = Class.forName("com.sim.chatserver.web.admin.TestConnectionServlet$ProbeKind");
    
            Object mode = enumValue(modeClass, "AUTH_BEARER_AND_X_API_KEY");
            Object kind = enumValue(kindClass, "CHAT");
    
            HttpRequest request = (HttpRequest) invoke(
                    servlet,
                    "buildProbeRequest",
                    new Class[]{String.class, ApiAuthResolver.ResolvedApiAuth.class, modeClass, kindClass, String.class},
                    "https://example.com/api/v1/workspace/demo/chat",
                    auth,
                    mode,
                    kind,
                    "{\"message\":\"x\"}"
            );
    
            assertEquals("POST", request.method());
            assertTrue(request.headers().firstValue("Authorization").orElse("").contains("Bearer token123"));
            assertEquals("token123", request.headers().firstValue("X-API-Key").orElse(""));
    
            ApiAuthResolver.ResolvedApiAuth a1 = newResolvedAuth("Authorization: Bearer abc", "abc", "Authorization", "REQUEST");
            ApiAuthResolver.ResolvedApiAuth a2 = newResolvedAuth("abc", "abc", "Authorization", "SERVER_CONFIG");
            @SuppressWarnings("unchecked")
            List<ApiAuthResolver.ResolvedApiAuth> deduped = (List<ApiAuthResolver.ResolvedApiAuth>) invoke(
                    servlet,
                    "buildAuthCandidates",
                    new Class[]{ApiAuthResolver.ResolvedApiAuth.class, ApiAuthResolver.ResolvedApiAuth.class},
                    a1,
                    a2
            );
            assertEquals(1, deduped.size());
    
            String summary = (String) invoke(servlet, "summarizeAuthCandidates", new Class[]{List.class}, deduped);
            assertTrue(summary.contains("REQUEST|Authorization"));
        }
    
        @Test
        void failureAndReasonHelpers_coverBranches() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            Class<?> kindClass = Class.forName("com.sim.chatserver.web.admin.TestConnectionServlet$ProbeKind");
            Object systemKind = enumValue(kindClass, "SYSTEM");
            Object chatKind = enumValue(kindClass, "CHAT");
    
            String authFailure = (String) invoke(
                    servlet,
                    "buildProbeFailureMessage",
                    new Class[]{kindClass, int.class, String.class, String.class, String.class},
                    systemKind,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"error\":\"Unauthorized\"}",
                    "workspace",
                    "http://example/api/v1/system"
            );
            assertTrue(authFailure.toLowerCase(Locale.ROOT).contains("authentication failed"));
    
            String badRequest = (String) invoke(
                    servlet,
                    "buildProbeFailureMessage",
                    new Class[]{kindClass, int.class, String.class, String.class, String.class},
                    chatKind,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"message\":\"invalid workspace\"}",
                    "bad-workspace",
                    "http://example/api/v1/chat"
            );
            assertTrue(badRequest.toLowerCase(Locale.ROOT).contains("workspace is invalid"));
    
            String generic = (String) invoke(
                    servlet,
                    "buildProbeFailureMessage",
                    new Class[]{kindClass, int.class, String.class, String.class, String.class},
                    systemKind,
                    502,
                    "plain body",
                    "workspace",
                    "http://example/api/v1/system"
            );
            assertTrue(generic.toLowerCase(Locale.ROOT).contains("upstream http 502"));
    
            assertEquals("error-a", invoke(servlet, "extractUpstreamReason", new Class[]{String.class}, "{\"error\":\"error-a\"}"));
            assertEquals("message-b", invoke(servlet, "extractUpstreamReason", new Class[]{String.class}, "{\"message\":\"message-b\"}"));
    
            String longReason = "x".repeat(400);
            String extracted = (String) invoke(servlet, "extractUpstreamReason", new Class[]{String.class}, longReason);
            assertEquals(300, extracted.length());
    
            assertEquals("", invoke(servlet, "suffixFromReason", new Class[]{String.class}, " "));
            assertTrue(((String) invoke(servlet, "suffixFromReason", new Class[]{String.class}, "bad")).contains("Reason: bad"));
    
            assertEquals("fallback", invoke(servlet, "defaultIfBlank", new Class[]{String.class, String.class}, "", "fallback"));
            assertEquals("x", invoke(servlet, "safe", new Class[]{String.class}, "x"));
            assertEquals("", invoke(servlet, "safe", new Class[]{String.class}, new Object[]{null}));
            assertTrue(((String) invoke(servlet, "truncate", new Class[]{String.class}, "x".repeat(600))).endsWith("..."));
        }
    
        @Test
        void hostSlugInterruptAndAbortHelpers_coverBranches() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
    
            assertEquals("http://example.com:8080", invoke(servlet, "buildBaseUrl", new Class[]{String.class, String.class}, "example.com", "8080"));
            assertEquals("https://example.com:443", invoke(servlet, "buildBaseUrl", new Class[]{String.class, String.class}, "https://example.com", "443"));
    
            assertEquals("demo-workspace", invoke(servlet, "buildSlug", new Class[]{String.class}, " Demo Workspace "));
            assertEquals("", invoke(servlet, "buildSlug", new Class[]{String.class}, "!!!"));
    
            assertEquals("example.com", invoke(servlet, "sanitizeHost", new Class[]{String.class}, "https://example.com:8443/path"));
            assertEquals("https", invoke(servlet, "extractScheme", new Class[]{String.class}, "https://server"));
            assertEquals("http", invoke(servlet, "extractScheme", new Class[]{String.class}, "http://server"));
            assertEquals(null, invoke(servlet, "extractScheme", new Class[]{String.class}, "server"));
    
            assertTrue((Boolean) invoke(servlet, "isAuthFailureStatus", new Class[]{int.class}, HttpServletResponse.SC_UNAUTHORIZED));
            assertFalse((Boolean) invoke(servlet, "isAuthFailureStatus", new Class[]{int.class}, 500));
    
            assertTrue((Boolean) invoke(
                    servlet,
                    "causedByInterrupted",
                    new Class[]{Throwable.class},
                    new RuntimeException(new IllegalStateException(new InterruptedException("stop")))
            ));
            assertFalse((Boolean) invoke(servlet, "causedByInterrupted", new Class[]{Throwable.class}, new RuntimeException("x")));
    
            Class<?> modeClass = Class.forName("com.sim.chatserver.web.admin.TestConnectionServlet$AuthHeaderMode");
            Class<?> responseClass = Class.forName("com.sim.chatserver.web.admin.TestConnectionServlet$ProbeResponse");
            Object authBearer = enumValue(modeClass, "AUTH_BEARER");
            Object probe = newProbeResponse(responseClass, modeClass, 500,
                    "Cannot read properties of null while reading 'id'", authBearer, "REQUEST");
            assertTrue((Boolean) invoke(servlet, "isNullIdAbortResponse", new Class[]{responseClass}, probe));
    
            Object notAbort = newProbeResponse(responseClass, modeClass, 200, "ok", authBearer, "REQUEST");
            assertFalse((Boolean) invoke(servlet, "isNullIdAbortResponse", new Class[]{responseClass}, notAbort));
        }
    
        @Test
        void writeJson_whenOutputFails_usesSendErrorFallback() throws Exception {
            TestConnectionServlet servlet = new TestConnectionServlet();
            HttpServletResponse resp = mock(HttpServletResponse.class);
            JsonObject payload = Json.createObjectBuilder().add("status", "ok").build();
    
            when(resp.getOutputStream()).thenThrow(new IOException("write fail"));
            when(resp.isCommitted()).thenReturn(false);
    
            invoke(servlet, "writeJson", new Class[]{HttpServletResponse.class, int.class, JsonObject.class}, resp, 200, payload);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
        }
    
        private static HttpSession adminSession() {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("admin");
            return session;
        }
    
        private static ServletOutputStream servletOutput(ByteArrayOutputStream out) {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
    
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // no-op for tests
                }
    
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };
        }
    
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Object enumValue(Class<?> enumClass, String name) {
            return Enum.valueOf((Class<? extends Enum>) enumClass, name);
        }
    
        private static ApiAuthResolver.ResolvedApiAuth newResolvedAuth(String raw, String token, String header, String source)
                throws Exception {
            Constructor<ApiAuthResolver.ResolvedApiAuth> ctor = ApiAuthResolver.ResolvedApiAuth.class
                    .getDeclaredConstructor(String.class, String.class, String.class, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(raw, token, header, source);
        }
    
        private static Object newProbeResponse(
                Class<?> responseClass,
                Class<?> modeClass,
                int status,
                String body,
                Object mode,
                String source
        ) throws Exception {
            Constructor<?> ctor = responseClass.getDeclaredConstructor(int.class, String.class, modeClass, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(status, body, mode, source);
        }
    
        private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
            Method m = target.getClass().getDeclaredMethod(name, types);
            m.setAccessible(true);
            try {
                return m.invoke(target, args);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Exception e) {
                    throw e;
                }
                if (cause instanceof Error e) {
                    throw e;
                }
                throw ite;
            }
        }
}
