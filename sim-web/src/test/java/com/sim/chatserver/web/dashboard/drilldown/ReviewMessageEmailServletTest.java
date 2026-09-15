package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
import com.sim.chatserver.service.translation.TranslationService;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class ReviewMessageEmailServletTest {

    private static final Pattern READABLE_UTC_TS = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} UTC");

    @Test
    void doPost_unauthenticated_returns401() throws Exception {
        ReviewMessageEmailServlet servlet = new ReviewMessageEmailServlet();
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
    void doPost_invalidRecipient_returns400() throws Exception {
        EmailService emailService = mock(EmailService.class);
        ReviewMessageEmailServlet servlet = new TestableReviewMessageEmailServlet(validResolvedConfig(), emailService, null);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String payload = "{" +
                "\"to\":\"not-an-email\"," +
                "\"prompt\":\"hola\"" +
                "}";

        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentLengthLong()).thenReturn((long) payload.getBytes(StandardCharsets.UTF_8).length);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(payload)));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("recipient"));
        verify(emailService, never()).send(any());
    }

    @Test
    void doPost_validPayload_sendsEmailAndReturnsOk() throws Exception {
        EmailService emailService = mock(EmailService.class);
        doNothing().when(emailService).send(any(EmailMessage.class));
        ReviewMessageEmailServlet servlet = new TestableReviewMessageEmailServlet(validResolvedConfig(), emailService, null);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String payload = "{" +
            "\"to\":\"teammate@example.com\"," +
            "\"chatId\":\"12345\"," +
            "\"createdAt\":\"2026-08-10T13:14:15Z\"," +
            "\"sessionId\":\"s-1\"," +
            "\"subjectLabel\":\"Latest Chats\"," +
            "\"subjectType\":\"Widget\"," +
            "\"prompt\":\"How do I reset my password?\"," +
            "\"response\":\"Use the reset link from your profile page.\"," +
            "\"customMessage\":\"Please use this in the enhancement write-up.\"," +
            "\"translateToEnglish\":false" +
            "}";

        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getContentLengthLong()).thenReturn((long) payload.getBytes(StandardCharsets.UTF_8).length);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(payload)));
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("ok", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("sent"));
        assertTrue(body.containsKey("translatedPrompt"));
        assertTrue(body.containsKey("translatedResponse"));

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService).send(messageCaptor.capture());

        String textBody = extractTextBody(messageCaptor.getValue());
        assertTrue(textBody.contains("Chat Timestamp: 2026-08-10 13:14:15 UTC"));
        assertTrue(textBody.contains("Custom Note from Reviewer"));
        assertTrue(textBody.contains("Please use this in the enhancement write-up."));
        assertTrue(READABLE_UTC_TS.matcher(extractLineValue(textBody, "Shared At: ")).matches());
    }

    @Test
    void doPost_invalidRequestEncoding_returns400() throws Exception {
        ReviewMessageEmailServlet servlet = new ReviewMessageEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        doThrow(new UnsupportedEncodingException("bad-encoding"))
                .when(req)
                .setCharacterEncoding(anyString());
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        servlet.doPost(req, resp);

        JsonObject body = jsonBody(out);
        assertEquals("error", body.getString("status"));
        assertTrue(body.getString("message").toLowerCase().contains("encoding"));
    }

    @Test
    void doGet_whenJsonWriteFails_fallsBackToSendError() throws Exception {
        ReviewMessageEmailServlet servlet = new ReviewMessageEmailServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(resp.getOutputStream()).thenThrow(new IOException("stream unavailable"));

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private static String extractTextBody(EmailMessage message) {
        try {
            Field field = EmailMessage.class.getDeclaredField("textBody");
            field.setAccessible(true);
            Object value = field.get(message);
            return value == null ? "" : String.valueOf(value);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new AssertionError("Could not inspect email text body", ex);
        }
    }

    private static String extractLineValue(String text, String prefix) {
        for (String line : String.valueOf(text).split("\\R")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private static ResolvedEmailConfig validResolvedConfig() {
        EmailConfig smtp = new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "smtp-user",
                "smtp-pass",
                "default@example.com");
        return ResolvedEmailConfig.smtp(smtp, EmailConfigSource.DATABASE, true, "ok");
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

    private static final class TestableReviewMessageEmailServlet extends ReviewMessageEmailServlet {

        private final ResolvedEmailConfig resolved;
        private final EmailService emailService;
        private final TranslationService translationService;

        private TestableReviewMessageEmailServlet(
                ResolvedEmailConfig resolved,
                EmailService emailService,
                TranslationService translationService) {
            this.resolved = resolved;
            this.emailService = emailService;
            this.translationService = translationService;
        }

        @Override
        DbEmailConfigProvider resolveDbProvider() {
            return null;
        }

        @Override
        ResolvedEmailConfig resolveEmailConfig() {
            return resolved;
        }

        @Override
        EmailService resolveEmailService(ResolvedEmailConfig ignored) {
            return emailService;
        }

        @Override
        TranslationService resolveTranslationService() {
            return translationService;
        }
    }
}
