package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailService;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
 class AdminEmailServletTest {

    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDown() {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    @Test
    void doPost_whenUnauthenticated_returns401() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("authentication"));
    }

    @Test
    void doPost_whenNonAdmin_returns403() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("USER");
        when(req.getSession(false)).thenReturn(session);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("admin"));
    }

    @Test
    void doPost_invalidContentLength_returns400() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("invalid json"));
    }

    @Test
    void doPost_malformedJson_returns400() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(16L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{bad json")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("invalid json"));
    }

    @Test
    void doPost_missingToRecipients_returns400() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService emailService = mock(EmailService.class);
        mockProvider(provider);
        EmailConfig resolvedConfig = validSmtpConfig("default@example.com");
        when(provider.load()).thenReturn(resolvedConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(128L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"subject\":\"Subject\"," +
                "\"textBody\":\"Hello\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(resolvedConfig)).thenReturn(emailService);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("recipient"));
    }

    @Test
    void doPost_blankSubject_returns400() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService emailService = mock(EmailService.class);
        mockProvider(provider);
        EmailConfig resolvedConfig = validSmtpConfig("default@example.com");
        when(provider.load()).thenReturn(resolvedConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(196L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"to\":[\"ops@example.com\"]," +
                "\"subject\":\"\"," +
                "\"textBody\":\"Hello\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(resolvedConfig)).thenReturn(emailService);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("subject"));
    }

    @Test
    void doPost_invalidRecipientFormat_returns400() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService emailService = mock(EmailService.class);
        mockProvider(provider);
        EmailConfig resolvedConfig = validSmtpConfig("default@example.com");
        when(provider.load()).thenReturn(resolvedConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(220L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"to\":[\"bad-email\"]," +
                "\"subject\":\"Subject\"," +
                "\"textBody\":\"Hello\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(resolvedConfig)).thenReturn(emailService);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("invalid email request"));
    }

    @Test
    void doPost_sendSuccess_returnsOk() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService emailService = mock(EmailService.class);
        mockProvider(provider);

        EmailConfig resolvedConfig = validSmtpConfig("default@example.com");
        when(provider.load()).thenReturn(resolvedConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(320L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"from\":\"sender@example.com\"," +
                "\"to\":[\"ops@example.com\"]," +
                "\"cc\":[\"cc@example.com\"]," +
                "\"bcc\":[\"bcc@example.com\"]," +
                "\"subject\":\"Subject\"," +
                "\"textBody\":\"Hello\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(resolvedConfig)).thenReturn(emailService);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("ok", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("sent"));
        verify(emailService, times(1)).send(any());
    }

    @Test
    void doPost_sendFailure_returns500() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService emailService = mock(EmailService.class);
        mockProvider(provider);

        EmailConfig resolvedConfig = validSmtpConfig("default@example.com");
        when(provider.load()).thenReturn(resolvedConfig);
        org.mockito.Mockito.doThrow(new IllegalStateException("mail down")).when(emailService).send(any());

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(280L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"to\":[\"ops@example.com\"]," +
                "\"subject\":\"Subject\"," +
                "\"textBody\":\"Hello\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(resolvedConfig)).thenReturn(emailService);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("failed to send"));
    }

    @Test
    void privateHelpers_coverValidationHelpers() throws Exception {
        AdminEmailServlet servlet = new AdminEmailServlet();

        Method validateEmails = AdminEmailServlet.class.getDeclaredMethod("validateEmails", String.class, java.util.List.class);
        validateEmails.setAccessible(true);
        validateEmails.invoke(servlet, "to", java.util.List.of("a@example.com"));
        assertThrowsIllegalArgument(() -> validateEmails.invoke(servlet, "to", java.util.List.of("bad-email")));

        Method safe = AdminEmailServlet.class.getDeclaredMethod("safe", String.class);
        safe.setAccessible(true);
        assertEquals("", safe.invoke(servlet, new Object[]{null}));
        assertEquals("a\\\"b", safe.invoke(servlet, "a\"b"));

        Method toList = AdminEmailServlet.class.getDeclaredMethod("toList", jakarta.json.JsonArray.class);
        toList.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> values = (java.util.List<String>) toList.invoke(servlet, Json.createArrayBuilder()
                .add(" one@example.com ")
                .add("   ")
                .build());
        assertEquals(1, values.size());
        assertEquals("one@example.com", values.get(0));

        Method isAdmin = AdminEmailServlet.class.getDeclaredMethod("isAdmin", HttpServletRequest.class, HttpServletResponse.class);
        isAdmin.setAccessible(true);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));
        boolean admin = (Boolean) isAdmin.invoke(servlet, req, resp);
        assertFalse(admin);
    }

    private void mockProvider(DbEmailConfigProvider provider) {
        if (cdiMock != null) {
            cdiMock.close();
        }
        cdiMock = org.mockito.Mockito.mockStatic(CDI.class);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<DbEmailConfigProvider> instance = mock(Instance.class);
        when(cdi.select(DbEmailConfigProvider.class)).thenReturn(instance);
        when(instance.get()).thenReturn(provider);
        cdiMock.when(CDI::current).thenReturn(cdi);
    }

    private static EmailConfig validSmtpConfig(String defaultFrom) {
        return new EmailConfig("smtp.example.com", 587, true, true, false, "smtp-user", "smtp-pass", defaultFrom);
    }

    private static HttpSession adminSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        return session;
    }

    private static void assertThrowsIllegalArgument(ThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static JsonObject jsonBody(ByteArrayOutputStream out) {
        String text = out.toString(StandardCharsets.UTF_8);
        return Json.createReader(new StringReader(text)).readObject();
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

