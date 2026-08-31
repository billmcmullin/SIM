package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EmailMessageTest {

    @Test
    void build_success_withTextBodyAndSingleTo() {
        EmailMessage msg = EmailMessageTestBuilder.builder()
                .from("sender@example.com")
                .to("to@example.com")
                .subject("Test Subject")
                .textBody("Hello")
                .build();

        assertEquals("sender@example.com", msg.from());
        assertEquals(List.of("to@example.com"), msg.to());
        assertTrue(msg.cc().isEmpty());
        assertTrue(msg.bcc().isEmpty());
        assertEquals("Test Subject", msg.subject());
        assertEquals("Hello", msg.textBody());
        assertNull(msg.htmlBody());
        assertNull(msg.markdownBody());
        List<EmailAttachment> attachments = new ArrayList<>();
        msg.forEachAttachment(attachments::add);
        assertTrue(attachments.isEmpty());
    }

    @Test
    void build_success_withHtmlOrMarkdownBody() {
        EmailMessage htmlMsg = EmailMessageTestBuilder.builder()
                .to("to@example.com")
                .subject("HTML")
                .htmlBody("<p>Hello</p>")
                .build();

        assertEquals("HTML", htmlMsg.subject());
        assertEquals("<p>Hello</p>", htmlMsg.htmlBody());

        EmailMessage mdMsg = EmailMessageTestBuilder.builder()
                .to("to@example.com")
                .subject("MD")
                .markdownBody("**Hello**")
                .build();

        assertEquals("MD", mdMsg.subject());
        assertEquals("**Hello**", mdMsg.markdownBody());
    }

    @Test
    void build_throwsNpe_whenSubjectMissing() {
        NullPointerException ex = assertThrows(NullPointerException.class, ()
                -> EmailMessageTestBuilder.builder()
                        .to("to@example.com")
                        .textBody("Hello")
                        .build()
        );
        assertEquals("subject is required", ex.getMessage());
    }

    @Test
    void build_throwsIllegalArgument_whenNoRecipients() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()
                -> EmailMessageTestBuilder.builder()
                        .subject("No recipients")
                        .textBody("Hello")
                        .build()
        );
        assertEquals("At least one recipient is required", ex.getMessage());
    }

    @Test
    void build_throwsIllegalArgument_whenAllBodiesBlankOrMissing() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, ()
                -> EmailMessageTestBuilder.builder()
                        .to("to@example.com")
                        .subject("No body")
                        .build()
        );
        assertEquals("At least one body (text/html/markdown) is required", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, ()
                -> EmailMessageTestBuilder.builder()
                        .to("to@example.com")
                        .subject("Blank body")
                        .textBody("   ")
                        .htmlBody("\n")
                        .markdownBody("\t")
                        .build()
        );
        assertEquals("At least one body (text/html/markdown) is required", ex2.getMessage());
    }

    @Test
    void builder_accumulatesRecipients_fromSingleAndListMethods() {
        EmailMessage msg = EmailMessageTestBuilder.builder()
                .to("to1@example.com")
                .to(List.of("to2@example.com", "to3@example.com"))
                .cc("cc1@example.com")
                .cc(List.of("cc2@example.com"))
                .bcc("bcc1@example.com")
                .bcc(List.of("bcc2@example.com"))
                .subject("Recipients")
                .textBody("Hello")
                .build();

        assertEquals(List.of("to1@example.com", "to2@example.com", "to3@example.com"), msg.to());
        assertEquals(List.of("cc1@example.com", "cc2@example.com"), msg.cc());
        assertEquals(List.of("bcc1@example.com", "bcc2@example.com"), msg.bcc());
    }

    @Test
    void allRecipientsReadOnly_returnsCombinedInOrder_andUnmodifiable() {
        EmailMessage msg = EmailMessageTestBuilder.builder()
                .to(List.of("to1@example.com", "to2@example.com"))
                .cc("cc1@example.com")
                .bcc(List.of("bcc1@example.com", "bcc2@example.com"))
                .subject("All recipients")
                .textBody("Hello")
                .build();

        List<String> all = msg.allRecipientsReadOnly();

        assertEquals(
                List.of("to1@example.com", "to2@example.com", "cc1@example.com", "bcc1@example.com", "bcc2@example.com"),
                all
        );

        assertThrows(UnsupportedOperationException.class, () -> all.add("x@example.com"));
    }

    @Test
    void builtCollections_areImmutable() {
        EmailMessage msg = EmailMessageTestBuilder.builder()
                .to("to@example.com")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .subject("Immutable")
                .textBody("Hello")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> msg.to().add("x@example.com"));
        assertThrows(UnsupportedOperationException.class, () -> msg.cc().add("x@example.com"));
        assertThrows(UnsupportedOperationException.class, () -> msg.bcc().add("x@example.com"));
    }
}
