package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

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
 class AdminEmailConfigServletTest {

    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDown() {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    @Test
    void doGet_whenUnauthenticated_returns401() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("authentication"));
    }

    @Test
    void doGet_whenNonAdmin_returns403() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("USER");
        when(req.getSession(false)).thenReturn(session);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("admin"));
    }

    @Test
    void doGet_success_returnsEffectiveConfig() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        mockProvider(provider);

        EmailConfig dbConfig = new EmailConfig(
                "smtp.example.com", 587, true, true, false, "mailer", "secret", "noreply@example.com");
        when(provider.load()).thenReturn(dbConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doGet(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("ok", body.getString("status"));
        assertTrue(body.getBoolean("dbConfigured"));
        assertEquals("smtp.example.com", body.getJsonObject("effective").getString("host"));
        assertTrue(body.getJsonObject("effective").getBoolean("passwordConfigured"));
    }

    @Test
    void doPost_invalidLength_returns400() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
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
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
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
    void doPost_save_missingHost_returns400() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        mockProvider(provider);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(32L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"host\":\"\"," +
                "\"port\":25" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("host"));
    }

    @Test
    void doPost_save_blankPassword_fallsBackToExistingPassword() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        mockProvider(provider);

        when(provider.load()).thenReturn(new EmailConfig(
                "smtp.existing.com", 2525, true, false, false, "existing-user", "existing-pass", "existing@example.com"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AtomicReference<EmailConfig> captured = new AtomicReference<>();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(256L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"host\":\"smtp.example.com\"," +
                "\"port\":587," +
                "\"auth\":true," +
                "\"starttls\":true," +
                "\"ssl\":false," +
                "\"username\":\"new-user\"," +
                "\"password\":\"\"," +
                "\"defaultFrom\":\"notify@example.com\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        org.mockito.Mockito.doAnswer(invocation -> {
            captured.set(invocation.getArgument(0, EmailConfig.class));
            return null;
        }).when(provider).save(any(EmailConfig.class), anyString());

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("ok", body.getString("status"));
        assertNotNull(captured.get());
        assertEquals("existing-pass", captured.get().password());
        verify(provider, times(1)).save(any(EmailConfig.class), anyString());
    }

    @Test
    void doPost_save_providerFailure_returns500() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        mockProvider(provider);

        org.mockito.Mockito.doThrow(new IllegalStateException("db down"))
                .when(provider)
                .save(any(EmailConfig.class), anyString());

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(256L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"host\":\"smtp.example.com\"," +
                "\"port\":587," +
                "\"auth\":true," +
                "\"starttls\":true," +
                "\"ssl\":false," +
                "\"username\":\"user\"," +
                "\"password\":\"pw\"," +
                "\"defaultFrom\":\"notify@example.com\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("failed to save"));
    }

    @Test
    void doPost_testAction_invalidRecipient_returns400() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        mockProvider(provider);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(256L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"action\":\"test\"," +
                "\"host\":\"smtp.example.com\"," +
                "\"port\":587," +
                "\"auth\":false," +
                "\"starttls\":true," +
                "\"ssl\":false," +
                "\"username\":\"user\"," +
                "\"password\":\"pw\"," +
                "\"defaultFrom\":\"from@example.com\"," +
                "\"testTo\":\"bad-address\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("valid testto"));
    }

    @Test
    void doPost_testAction_sendsEmailAndReturnsOk() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService service = mock(EmailService.class);
        mockProvider(provider);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(256L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"action\":\"test\"," +
                "\"host\":\"smtp.example.com\"," +
                "\"port\":587," +
                "\"auth\":false," +
                "\"starttls\":true," +
                "\"ssl\":false," +
                "\"username\":\"user\"," +
                "\"password\":\"pw\"," +
                "\"defaultFrom\":\"from@example.com\"," +
                "\"testTo\":\"ops@example.com\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        EmailConfig expected = new EmailConfig(
                "smtp.example.com", 587, false, true, false, "user", "pw", "from@example.com");
        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(expected)).thenReturn(service);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("ok", body.getString("status"));
        verify(service, times(1)).send(any());
    }

    @Test
    void doPost_testAction_emailFailure_returns500() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();
        DbEmailConfigProvider provider = mock(DbEmailConfigProvider.class);
        EmailService service = mock(EmailService.class);
        mockProvider(provider);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpSession admin = adminSession();
        when(req.getSession(false)).thenReturn(admin);
        when(req.getContentLengthLong()).thenReturn(256L);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{" +
                "\"action\":\"test\"," +
                "\"host\":\"smtp.example.com\"," +
                "\"port\":587," +
                "\"auth\":false," +
                "\"starttls\":true," +
                "\"ssl\":false," +
                "\"username\":\"user\"," +
                "\"password\":\"pw\"," +
                "\"defaultFrom\":\"from@example.com\"," +
                "\"testTo\":\"ops@example.com\"" +
                "}")));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));
        org.mockito.Mockito.doThrow(new IllegalStateException("send failed")).when(service).send(any());

        EmailConfig expected = new EmailConfig(
                "smtp.example.com", 587, false, true, false, "user", "pw", "from@example.com");
        try (MockedStatic<EmailFactory> emailFactoryMock = org.mockito.Mockito.mockStatic(EmailFactory.class)) {
            emailFactoryMock.when(() -> EmailFactory.smtp(expected)).thenReturn(service);

            servlet.doPost(req, resp);
        }

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("smtp test failed"));
    }

    @Test
    void privateHelpers_coverValidationAndReadPayloadGuard() throws Exception {
        AdminEmailConfigServlet servlet = new AdminEmailConfigServlet();

        assertTrue(invokeBoolean(servlet, "hasText", new Class<?>[]{String.class}, " value "));
        assertFalse(invokeBoolean(servlet, "hasText", new Class<?>[]{String.class}, "   "));
        assertTrue(invokeBoolean(servlet, "isValidEmail", new Class<?>[]{String.class}, "a@b.com"));
        assertFalse(invokeBoolean(servlet, "isValidEmail", new Class<?>[]{String.class}, "bad"));
        assertEquals("", invokeObject(servlet, "safe", new Class<?>[]{String.class}, new Object[]{null}));

        Method m = AdminEmailConfigServlet.class.getDeclaredMethod("readValidatedJsonPayload", HttpServletRequest.class);
        m.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> m.invoke(servlet, new Object[]{null}));
        assertNotNull(ex);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
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

    private static HttpSession adminSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        return session;
    }

    private static boolean invokeBoolean(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, types);
        m.setAccessible(true);
        return (Boolean) m.invoke(target, args);
    }

    private static Object invokeObject(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, types);
        m.setAccessible(true);
        return m.invoke(target, args);
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

