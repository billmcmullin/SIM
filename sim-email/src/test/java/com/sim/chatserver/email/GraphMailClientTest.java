package com.sim.chatserver.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphMailClientTest {

    private final GraphMailClient client = new GraphMailClient();

    @Test
    @DisplayName("sendMail success with 202 and explicit HTML body")
    void sendMail_success_202_withHtmlBody() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig("tenant", "client", "secret", "sender@example.com", null);
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        when(renderer.toHtml(any())).thenReturn("<p>md-html</p>");

        EmailMessage message = msg(
                List.of("to1@example.com", "  to2@example.com  "),
                List.of("cc@example.com"),
                List.of("bcc@example.com"),
                "Subject A",
                "text body",
                "<p>explicit html</p>",
                null
        );

        HttpURLConnection conn = mock(HttpURLConnection.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(conn.getOutputStream()).thenReturn(out);
        when(conn.getResponseCode()).thenReturn(202);

        try (MockedConstruction<URL> mockedUrl = mockConstruction(
                URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(conn)
        )) {
            assertDoesNotThrow(() -> client.sendMail("token123", config, message, renderer));

            verify(conn).setRequestMethod("POST");
            verify(conn).setDoOutput(true);
            verify(conn).setConnectTimeout(10000);
            verify(conn).setReadTimeout(20000);
            verify(conn).setRequestProperty("Authorization", "Bearer token123");
            verify(conn).setRequestProperty("Content-Type", "application/json");
            verify(conn).disconnect();

            String json = out.toString();
            assertTrue(json.contains("\"subject\":\"Subject A\""));
            assertTrue(json.contains("\"contentType\":\"HTML\""));
            assertTrue(json.contains("\"content\":\"<p>explicit html</p>\""));
            assertTrue(json.contains("\"toRecipients\""));
            assertTrue(json.contains("\"ccRecipients\""));
            assertTrue(json.contains("\"bccRecipients\""));

            assertTrue(json.contains("\"address\":\"to1@example.com\""));
            assertTrue(json.contains("\"address\":\"to2@example.com\""));
        }
    }

    @Test
    @DisplayName("sendMail uses markdown HTML when htmlBody is blank")
    void sendMail_usesMarkdownHtmlWhenHtmlBlank() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig("tenant", "client", "secret", "sender@example.com", null);
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        when(renderer.toHtml("## md")).thenReturn("<h2>md</h2>");

        EmailMessage message = msg(
                List.of("to@example.com"),
                null,
                null,
                "Sub",
                "text body",
                "   ",
                "## md"
        );

        HttpURLConnection conn = mock(HttpURLConnection.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(conn.getOutputStream()).thenReturn(out);
        when(conn.getResponseCode()).thenReturn(202);

        try (MockedConstruction<URL> mockedUrl = mockConstruction(
                URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(conn)
        )) {
            client.sendMail("token", config, message, renderer);

            String json = out.toString();
            assertTrue(json.contains("\"contentType\":\"HTML\""));
            assertTrue(json.contains("\"content\":\"<h2>md</h2>\""));
            verify(renderer).toHtml("## md");
        }
    }

    @Test
    @DisplayName("sendMail falls back to text when no HTML/markdown HTML")
    void sendMail_fallbackToText() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig("tenant", "client", "secret", "sender@example.com", null);
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        when(renderer.toHtml(any())).thenReturn("   ");

        EmailMessage message = msg(
                List.of("to@example.com"),
                null,
                null,
                null,
                "plain text",
                null,
                "md"
        );

        HttpURLConnection conn = mock(HttpURLConnection.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(conn.getOutputStream()).thenReturn(out);
        when(conn.getResponseCode()).thenReturn(202);

        try (MockedConstruction<URL> mockedUrl = mockConstruction(
                URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(conn)
        )) {
            client.sendMail("token", config, message, renderer);

            String json = out.toString();
            assertTrue(json.contains("\"subject\":\"\""));
            assertTrue(json.contains("\"contentType\":\"Text\""));
            assertTrue(json.contains("\"content\":\"plain text\""));
        }
    }

    @Test
    @DisplayName("sendMail throws EmailException on non-2xx/non-202 response with error body")
    void sendMail_httpFailure_throwsEmailException() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig("tenant", "client", "secret", "sender@example.com", null);
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        EmailMessage message = msg(List.of("to@example.com"), null, null, "S", "T", null, null);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(conn.getOutputStream()).thenReturn(out);
        when(conn.getResponseCode()).thenReturn(400);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream("{\"error\":\"bad\"}".getBytes()));

        try (MockedConstruction<URL> mockedUrl = mockConstruction(
                URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(conn)
        )) {
            EmailException ex = assertThrows(EmailException.class,
                    () -> client.sendMail("token", config, message, renderer));

            assertTrue(ex.getMessage().contains("Graph sendMail failed. HTTP 400"));
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause().getMessage().contains("graph_send_http_400"));
            verify(conn).disconnect();
        }
    }

    @Test
    @DisplayName("sendMail wraps unexpected exception in EmailException")
    void sendMail_unexpectedException_wrapped() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig("tenant", "client", "secret", "sender@example.com", null);
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        EmailMessage message = msg(List.of("to@example.com"), null, null, "S", "T", null, null);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getOutputStream()).thenThrow(new RuntimeException("boom"));

        try (MockedConstruction<URL> mockedUrl = mockConstruction(
                URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(conn)
        )) {
            EmailException ex = assertThrows(EmailException.class,
                    () -> client.sendMail("token", config, message, renderer));

            assertEquals("Graph mail send failed", ex.getMessage());
            assertNotNull(ex.getCause());
            assertEquals("boom", ex.getCause().getMessage());
            verify(conn).disconnect();
        }
    }

    private EmailMessage msg(
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String subject,
            String textBody,
            String htmlBody,
            String markdownBody
    ) {
        return EmailMessage.builder()
                .to(to)
                .cc(cc)
                .bcc(bcc)
                .subject(subject)
                .textBody(textBody)
                .htmlBody(htmlBody)
                .markdownBody(markdownBody)
                .build();
    }
}
