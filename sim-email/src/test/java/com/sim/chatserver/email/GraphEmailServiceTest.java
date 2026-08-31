package com.sim.chatserver.email;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphEmailServiceTest {

    @Mock
    private GraphTokenClient tokenClient;

    @Mock
    private GraphMailClient mailClient;

    @Mock
    private MarkdownRenderer markdownRenderer;

    @Mock
    private GraphEmailConfig config;

    private GraphEmailService service;

    @BeforeEach
    void setUp() {
        service = new GraphEmailService(config, tokenClient, mailClient, markdownRenderer);
    }

    @Test
    @DisplayName("constructor throws when config is null")
    void constructor_nullConfig_throws() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new GraphEmailService(null, tokenClient, mailClient, markdownRenderer)
        );
        assertEquals("GraphEmailConfig is required", ex.getMessage());
    }

    @Test
    @DisplayName("constructor throws when tokenClient is null")
    void constructor_nullTokenClient_throws() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new GraphEmailService(config, null, mailClient, markdownRenderer)
        );
        assertEquals("GraphTokenClient is required", ex.getMessage());
    }

    @Test
    @DisplayName("constructor throws when mailClient is null")
    void constructor_nullMailClient_throws() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new GraphEmailService(config, tokenClient, null, markdownRenderer)
        );
        assertEquals("GraphMailClient is required", ex.getMessage());
    }

    @Test
    @DisplayName("constructor throws when markdownRenderer is null")
    void constructor_nullMarkdownRenderer_throws() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new GraphEmailService(config, tokenClient, mailClient, null)
        );
        assertEquals("MarkdownRenderer is required", ex.getMessage());
    }

    @Test
    @DisplayName("send throws when config is not usable")
    void send_configNotUsable_throwsIllegalArgument() {
        when(config.isUsable()).thenReturn(false);

        EmailMessage message = msg(List.of("to@example.com"), null, null, "Subject", "Body", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(message));
        assertTrue(ex.getMessage().contains("Graph email config is incomplete"));

        verifyNoInteractions(tokenClient, mailClient);
    }

    @Test
    @DisplayName("send throws when message is null")
    void send_nullMessage_throwsIllegalArgument() {
        when(config.isUsable()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(null));
        assertEquals("EmailMessage is null", ex.getMessage());

        verifyNoInteractions(tokenClient, mailClient);
    }

    @Test
    @DisplayName("send throws when TO is missing")
    void send_missingTo_throwsIllegalArgument() {
        when(config.isUsable()).thenReturn(true);

        EmailMessage message = msg(List.of(), List.of("cc@example.com"), null, "Subject", "Body", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(message));
        assertEquals("At least one TO recipient is required", ex.getMessage());

        verifyNoInteractions(tokenClient, mailClient);
    }

    @Test
    @DisplayName("send throws when TO has only blanks")
    void send_blankToOnly_throwsIllegalArgument() {
        when(config.isUsable()).thenReturn(true);

        EmailMessage message = msg(List.of("   ", ""), null, null, "Subject", "Body", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(message));
        assertEquals("At least one TO recipient is required", ex.getMessage());

        verifyNoInteractions(tokenClient, mailClient);
    }

    @Test
    @DisplayName("send throws when subject is blank")
    void send_blankSubject_throwsIllegalArgument() {
        when(config.isUsable()).thenReturn(true);

        EmailMessage message = msg(List.of("to@example.com"), null, null, "   ", "Body", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.send(message));
        assertEquals("Email subject is required", ex.getMessage());

        verifyNoInteractions(tokenClient, mailClient);
    }

    @Test
    @DisplayName("send success: gets token and sends mail")
    void send_success_callsTokenAndMailClient() {
        when(config.isUsable()).thenReturn(true);
        when(tokenClient.getAccessToken()).thenReturn("access-token");
        when(config.senderUser()).thenReturn("sender@company.com");

        EmailMessage message = msg(List.of("to@example.com"), null, null, "Hello", "Body", null, null);

        assertDoesNotThrow(() -> service.send(message));

        verify(tokenClient).getAccessToken();
        verify(mailClient).sendMail("access-token", config, message, markdownRenderer);
    }

    @Test
    @DisplayName("send rethrows EmailException from dependency unchanged")
    void send_emailExceptionRethrown() {
        when(config.isUsable()).thenReturn(true);
        when(tokenClient.getAccessToken())
                .thenThrow(new EmailException("token failed", new RuntimeException("token failed")));

        EmailMessage message = msg(List.of("to@example.com"), null, null, "Hello", "Body", null, null);

        EmailException ex = assertThrows(EmailException.class, () -> service.send(message));
        assertEquals("token failed", ex.getMessage());

        verify(tokenClient).getAccessToken();
        verifyNoInteractions(mailClient);
    }

    @Test
    @DisplayName("send propagates non-EmailException runtime failures")
    void send_nonEmailException_propagated() {
        when(config.isUsable()).thenReturn(true);
        when(tokenClient.getAccessToken()).thenThrow(new RuntimeException("boom"));

        EmailMessage message = msg(List.of("to@example.com"), null, null, "Hello", "Body", null, null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.send(message));
        assertEquals("boom", ex.getMessage());

        verify(tokenClient).getAccessToken();
        verifyNoInteractions(mailClient);
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
        EmailMessageTestBuilder builder = EmailMessageTestBuilder.builder()
                .subject(subject)
                .textBody(textBody)
                .htmlBody(htmlBody)
                .markdownBody(markdownBody);
        if (to != null) {
            builder.to(to);
        }
        if (cc != null) {
            builder.cc(cc);
        }
        if (bcc != null) {
            builder.bcc(bcc);
        }
        return builder.build();
    }
}
