package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class EmailExceptionTest {

    @Test
    void constructor_setsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root-cause");

        EmailException ex = new EmailException("send failed", cause);

        assertEquals("send failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
