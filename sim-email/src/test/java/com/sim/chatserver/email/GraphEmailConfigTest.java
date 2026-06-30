package com.sim.chatserver.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphEmailConfigTest {

    @Test
    @DisplayName("effectiveAuthorityHost returns default when authorityHost is null")
    void effectiveAuthorityHost_null_returnsDefault() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant",
                "client",
                "secret",
                "sender@company.com",
                null
        );

        assertEquals("login.microsoftonline.com", config.effectiveAuthorityHost());
    }

    @Test
    @DisplayName("effectiveAuthorityHost returns default when authorityHost is blank")
    void effectiveAuthorityHost_blank_returnsDefault() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant",
                "client",
                "secret",
                "sender@company.com",
                "   "
        );

        assertEquals("login.microsoftonline.com", config.effectiveAuthorityHost());
    }

    @Test
    @DisplayName("effectiveAuthorityHost returns trimmed custom host when provided")
    void effectiveAuthorityHost_custom_trimmed() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant",
                "client",
                "secret",
                "sender@company.com",
                "  login.microsoftonline.us  "
        );

        assertEquals("login.microsoftonline.us", config.effectiveAuthorityHost());
    }

    @Test
    @DisplayName("isUsable returns true when all required fields have text")
    void isUsable_allRequiredPresent_true() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-id",
                "client-id",
                "client-secret",
                "sender@company.com",
                null
        );

        assertTrue(config.isUsable());
    }

    @Test
    @DisplayName("isUsable returns false when tenantId is null")
    void isUsable_tenantNull_false() {
        GraphEmailConfig config = new GraphEmailConfig(
                null,
                "client-id",
                "client-secret",
                "sender@company.com",
                null
        );

        assertFalse(config.isUsable());
    }

    @Test
    @DisplayName("isUsable returns false when clientId is blank")
    void isUsable_clientBlank_false() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-id",
                "   ",
                "client-secret",
                "sender@company.com",
                null
        );

        assertFalse(config.isUsable());
    }

    @Test
    @DisplayName("isUsable returns false when clientSecret is empty")
    void isUsable_secretEmpty_false() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-id",
                "client-id",
                "",
                "sender@company.com",
                null
        );

        assertFalse(config.isUsable());
    }

    @Test
    @DisplayName("isUsable returns false when senderUser is whitespace")
    void isUsable_senderWhitespace_false() {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-id",
                "client-id",
                "client-secret",
                "   ",
                null
        );

        assertFalse(config.isUsable());
    }
}
