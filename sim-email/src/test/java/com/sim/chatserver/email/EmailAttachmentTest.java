package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmailAttachmentTest {

    @Test
    void constructor_acceptsValidValues() {
        byte[] bytes = new byte[]{1, 2, 3};

        EmailAttachment attachment = new EmailAttachment("report.txt", "text/plain", bytes);

        assertEquals("report.txt", attachment.fileName());
        assertEquals("text/plain", attachment.contentType());
        assertArrayEquals(bytes, attachment.content());
    }

    @Test
    void constructor_rejectsNullFileName() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment(null, "text/plain", new byte[]{1})
        );
        assertEquals("fileName is required", ex.getMessage());
    }

    @Test
    void constructor_rejectsNullContentType() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment("a.txt", null, new byte[]{1})
        );
        assertEquals("contentType is required", ex.getMessage());
    }

    @Test
    void constructor_rejectsNullContent() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment("a.txt", "text/plain", null)
        );
        assertEquals("content is required", ex.getMessage());
    }
}
