package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EmailConfigTest {

    @Test
    void recordFields_areExposedAndComparable() {
        EmailConfig config = new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "user",
                "pass",
                "noreply@example.com"
        );

        assertEquals("smtp.example.com", config.host());
        assertEquals(587, config.port());
        assertEquals(true, config.auth());
        assertEquals(true, config.startTls());
        assertEquals(false, config.ssl());
        assertEquals("user", config.username());
        assertEquals("pass", config.password());
        assertEquals("noreply@example.com", config.defaultFrom());

        EmailConfig same = new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "user",
                "pass",
                "noreply@example.com"
        );
        assertEquals(config, same);
        assertEquals(config.hashCode(), same.hashCode());
    }
}
