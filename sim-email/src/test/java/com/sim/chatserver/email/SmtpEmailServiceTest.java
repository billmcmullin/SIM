package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SmtpEmailServiceTest {

    @Test
    void send_usesMessageFrom_whenPresent() throws Exception {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .from("sender@example.com")
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(any(MimeMessage.class)), times(1));
            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    Address[] from = mm.getFrom();
                    return from != null && from.length == 1 && from[0].toString().contains("sender@example.com");
                } catch (MessagingException e) {
                    return false;
                }
            })));
        }

        verify(renderer, never()).toHtml(any());
    }

    @Test
    void send_usesDefaultFrom_whenMessageFromBlank() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .from("   ")
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    Address[] from = mm.getFrom();
                    return from != null && from.length == 1 && from[0].toString().contains("default@example.com");
                } catch (MessagingException e) {
                    return false;
                }
            })));
        }
    }

    @Test
    void send_throwsIllegalArgumentException_whenNoFromAnywhere() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "   ");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(message));
        assertEquals("No from address provided (message.from or config.defaultFrom)", ex.getMessage());
    }

    @Test
    void send_wrapsMessagingException_fromTransport() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            transportMock.when(() -> Transport.send(any(MimeMessage.class)))
                    .thenThrow(new MessagingException("SMTP down"));

            EmailException ex = assertThrows(EmailException.class, () -> service.send(message));
            assertEquals("Failed to send email", ex.getMessage());
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof MessagingException);
        }
    }

    @Test
    void send_buildsRecipients_skippingNullOrBlank() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .to("to1@example.com")
                .to((String) null)
                .to("   ")
                .cc("cc1@example.com")
                .bcc("bcc1@example.com")
                .subject("Subject")
                .textBody("Hello")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    Address[] to = mm.getRecipients(Message.RecipientType.TO);
                    Address[] cc = mm.getRecipients(Message.RecipientType.CC);
                    Address[] bcc = mm.getRecipients(Message.RecipientType.BCC);

                    return to != null && to.length == 1
                            && cc != null && cc.length == 1
                            && bcc != null && bcc.length == 1;
                } catch (MessagingException e) {
                    return false;
                }
            })));
        }
    }

    @Test
    void send_buildsAlternativeBody_withTextAndMarkdownRenderedHtml() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        when(renderer.toHtml("**Hello**")).thenReturn("<p><strong>Hello</strong></p>");

        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello text")
                .markdownBody("**Hello**")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    Object content = mm.getContent();
                    if (!(content instanceof MimeMultipart mixed)) {
                        return false;
                    }
                    if (mixed.getCount() < 1) {
                        return false;
                    }

                    var firstBodyPart = mixed.getBodyPart(0);
                    Object altObj = firstBodyPart.getContent();
                    if (!(altObj instanceof MimeMultipart alt)) {
                        return false;
                    }

                    // should contain text and html
                    return alt.getCount() == 2;
                } catch (Exception e) {
                    return false;
                }
            })));
        }

        verify(renderer, times(1)).toHtml("**Hello**");
    }

    @Test
    void send_prefersHtmlBody_overMarkdownRenderedHtml() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        when(renderer.toHtml(any())).thenReturn("<p>from markdown</p>");

        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("Subject")
                .htmlBody("<p>explicit html</p>")
                .markdownBody("**ignored for preference**")
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    MimeMultipart mixed = (MimeMultipart) mm.getContent();
                    MimeMultipart alt = (MimeMultipart) mixed.getBodyPart(0).getContent();

                    if (alt.getCount() != 1) {
                        return false;
                    }
                    String html = (String) alt.getBodyPart(0).getContent();
                    return html.contains("explicit html");
                } catch (Exception e) {
                    return false;
                }
            })));
        }

        verify(renderer, times(1)).toHtml("**ignored for preference**");
    }

    @Test
    void send_includesAttachments_inMixedMultipart() {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, false, true, false, "", "", "default@example.com");
        MarkdownRenderer renderer = mock(MarkdownRenderer.class);
        SmtpEmailService service = new SmtpEmailService(config, renderer);

        EmailAttachment a1 = new EmailAttachment("a.txt", "text/plain", "abc".getBytes());
        EmailAttachment a2 = new EmailAttachment("b.json", "application/json", "{\"x\":1}".getBytes());

        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("Subject")
                .textBody("Hello")
                .attachment(a1)
                .attachment(a2)
                .build();

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            service.send(message);

            transportMock.verify(() -> Transport.send(argThat(m -> {
                try {
                    MimeMessage mm = (MimeMessage) m;
                    Object content = mm.getContent();
                    if (!(content instanceof MimeMultipart mixed)) {
                        return false;
                    }

                    // 1 body part + 2 attachments
                    return mixed.getCount() == 3;
                } catch (Exception e) {
                    return false;
                }
            })));
        }
    }
}
