package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.web.admin.AutoEmailAlertConfigStore.AutoEmailAlertConfig;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
 class AdminAutoEmailAlertsServletTest {

    @Test
    void doGet_whenStoreReturnsConfig_returnsOkJson() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        ServletContext ctx = mock(ServletContext.class);
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        AutoEmailAlertScheduler scheduler = mock(AutoEmailAlertScheduler.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(ctx.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key.endsWith(".store")) {
                return store;
            }
            if (key.endsWith(".scheduler")) {
                return scheduler;
            }
            return null;
        });
        servlet.init(mockConfigWithContext(ctx));

        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setHealthEnabled(true);
        cfg.setHealthCheckIntervalSeconds(300);
        cfg.setTermEnabled(true);
        cfg.setTermCheckIntervalSeconds(600);
        when(store.load()).thenReturn(cfg);

        HttpSession session = adminSession();
        when(req.getSession(false)).thenReturn(session);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("ok", body.getString("status"));
        assertTrue(body.getBoolean("healthEnabled"));
        assertEquals(5, body.getInt("healthCheckIntervalMinutes"));
        assertTrue(body.getBoolean("termEnabled"));
    }

    @Test
    void doGet_whenUnauthenticated_returns401() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("authentication"));
    }

    @Test
    void doPost_whenNonAdmin_returns403() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("USER");
        when(req.getSession(false)).thenReturn(session);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("admin"));
    }

    @Test
    void doPost_whenInvalidJsonRequest_returns400() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = adminSession();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(session);
        when(req.getContentType()).thenReturn("text/plain");
        when(req.getContentLengthLong()).thenReturn(10L);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("invalid json"));
    }

    @Test
    void doPost_whenMalformedJson_returns400() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession session = adminSession();
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentType()).thenReturn("application/json");
        when(req.getContentLengthLong()).thenReturn(10L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{bad json")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("invalid json"));
    }

    @Test
    void doPost_sendTestEmailPath_returnsBadRequestWhenSchedulerReportsFailure() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        ServletContext ctx = mock(ServletContext.class);
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        AutoEmailAlertScheduler scheduler = mock(AutoEmailAlertScheduler.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(ctx.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key.endsWith(".store")) {
                return store;
            }
            if (key.endsWith(".scheduler")) {
                return scheduler;
            }
            return null;
        });
        servlet.init(mockConfigWithContext(ctx));

        HttpSession session = adminSession();
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentType()).thenReturn("application/json");
        when(req.getContentLengthLong()).thenReturn(64L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"sendTestEmail\":true,\"healthRecipients\":\"ops@example.com\"}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        when(scheduler.sendHealthTestEmail(any())).thenReturn(new AutoEmailAlertScheduler.TestEmailResult(false, "failed"));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertEquals("failed", body.getString("message"));
    }

    @Test
    void doPost_saveConfigPath_returnsOk() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        ServletContext ctx = mock(ServletContext.class);
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        AutoEmailAlertScheduler scheduler = mock(AutoEmailAlertScheduler.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(ctx.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key.endsWith(".store")) {
                return store;
            }
            if (key.endsWith(".scheduler")) {
                return scheduler;
            }
            return null;
        });
        servlet.init(mockConfigWithContext(ctx));

        AutoEmailAlertConfig saved = new AutoEmailAlertConfig();
        saved.setHealthEnabled(true);
        saved.setHealthCheckIntervalSeconds(120);
        when(store.saveConfig(any(), anyString())).thenReturn(saved);

        HttpSession session = adminSession();
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentType()).thenReturn("application/json");
        when(req.getContentLengthLong()).thenReturn(128L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"healthEnabled\":true,\"healthCheckIntervalMinutes\":2}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("ok", body.getString("status"));
        assertTrue(body.getBoolean("healthEnabled"));
        assertEquals(2, body.getInt("healthCheckIntervalMinutes"));
    }

    @Test
    void doPost_whenSaveThrows_returns500() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        ServletContext ctx = mock(ServletContext.class);
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        AutoEmailAlertScheduler scheduler = mock(AutoEmailAlertScheduler.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(ctx.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key.endsWith(".store")) {
                return store;
            }
            if (key.endsWith(".scheduler")) {
                return scheduler;
            }
            return null;
        });
        servlet.init(mockConfigWithContext(ctx));

        when(store.saveConfig(any(), anyString())).thenThrow(new SQLException("db failed"));

        HttpSession session = adminSession();
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentType()).thenReturn("application/json");
        when(req.getContentLengthLong()).thenReturn(16L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("unable to save"));
    }

    @Test
    void privateHelpers_coverConversionsAndSanitization() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();

        assertEquals(120, invokePrivate(servlet, "minutesToSeconds", new Class<?>[]{int.class}, 2));
        assertEquals(1, invokePrivate(servlet, "secondsToMinutes", new Class<?>[]{int.class}, 0));
        assertEquals(Integer.MAX_VALUE, invokePrivate(servlet, "minutesToSeconds", new Class<?>[]{int.class}, Integer.MAX_VALUE));

        assertNull(invokePrivate(servlet, "normalizeText", new Class<?>[]{String.class}, "\r\n  \u0000"));
        assertEquals("abc", invokePrivate(servlet, "normalizeText", new Class<?>[]{String.class}, " abc\r"));
        assertEquals("", invokePrivate(servlet, "formatInstant", new Class<?>[]{Instant.class}, new Object[]{null}));
        assertEquals("", invokePrivate(servlet, "safe", new Class<?>[]{String.class}, new Object[]{null}));

        JsonObject payload = Json.createObjectBuilder()
                .add("healthEnabled", true)
                .add("healthCheckIntervalMinutes", "15")
                .add("healthSubject", "  Subject ")
                .add("termEnabled", false)
                .add("termCheckIntervalMinutes", 5)
                .build();
        AutoEmailAlertConfig cfg = (AutoEmailAlertConfig) invokePrivate(servlet, "fromPayload", new Class<?>[]{JsonObject.class}, payload);
        assertTrue(cfg.isHealthEnabled());
        assertEquals(900, cfg.getHealthCheckIntervalSeconds());
        assertEquals("Subject", cfg.getHealthSubject());
        assertFalse(cfg.isTermEnabled());
        assertEquals(300, cfg.getTermCheckIntervalSeconds());
    }

    @Test
    void destroy_clearsAttributesAndStopsScheduler() throws Exception {
        AdminAutoEmailAlertsServlet servlet = new AdminAutoEmailAlertsServlet();
        ServletContext ctx = mock(ServletContext.class);
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        AutoEmailAlertScheduler scheduler = mock(AutoEmailAlertScheduler.class);

        when(ctx.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key.endsWith(".store")) {
                return store;
            }
            if (key.endsWith(".scheduler")) {
                return scheduler;
            }
            return null;
        });

        servlet.init(mockConfigWithContext(ctx));
        servlet.destroy();

        verify(scheduler).stop();
        verify(ctx, times(2)).removeAttribute(anyString());
    }

    private static HttpSession adminSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        return session;
    }

    private static jakarta.servlet.ServletConfig mockConfigWithContext(ServletContext ctx) {
        jakarta.servlet.ServletConfig config = mock(jakarta.servlet.ServletConfig.class);
        when(config.getServletContext()).thenReturn(ctx);
        return config;
    }

    private static Object invokePrivate(Object target, String method, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(method, paramTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
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
}

