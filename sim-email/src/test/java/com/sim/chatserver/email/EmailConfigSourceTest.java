package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class EmailConfigSourceTest {

    @Test
    void enumValues_areStable() {
        assertArrayEquals(
                new EmailConfigSource[]{
                        EmailConfigSource.ENV,
                        EmailConfigSource.PROPERTIES,
                        EmailConfigSource.DATABASE,
                        EmailConfigSource.NONE
                },
                EmailConfigSource.values()
        );
    }
}
