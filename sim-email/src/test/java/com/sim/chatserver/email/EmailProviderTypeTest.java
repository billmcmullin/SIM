package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class EmailProviderTypeTest {

    @Test
    void enumValues_areStable() {
        assertArrayEquals(
                new EmailProviderType[]{EmailProviderType.SMTP, EmailProviderType.GRAPH},
                EmailProviderType.values()
        );
    }
}
