package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ResolvedEmailConfigTest {

    @Test
    void canonicalConstructor_appliesDefaultsAndSmtpProviderConfigFallback() {
        EmailConfig smtp = new EmailConfig("smtp.example.com", 587, true, true, false, "user", "pass", "from@example.com");

        ResolvedEmailConfig resolved = new ResolvedEmailConfig(smtp, null, true, null, null, null);

        assertSame(smtp, resolved.config());
        assertEquals(EmailConfigSource.NONE, resolved.source());
        assertEquals("", resolved.message());
        assertEquals(EmailProviderType.SMTP, resolved.providerType());
        assertSame(smtp, resolved.providerConfig());
        assertNull(resolved.graphConfigOrNull());
    }

    @Test
    void smtpFactory_setsSmtpFieldsAsExpected() {
        EmailConfig smtp = new EmailConfig("smtp.example.com", 25, false, false, false, "", "", "from@example.com");

        ResolvedEmailConfig resolved = ResolvedEmailConfig.smtp(smtp, EmailConfigSource.PROPERTIES, true, "ok");

        assertEquals(EmailProviderType.SMTP, resolved.providerType());
        assertEquals(EmailConfigSource.PROPERTIES, resolved.source());
        assertSame(smtp, resolved.config());
        assertSame(smtp, resolved.providerConfig());
        assertNull(resolved.graphConfigOrNull());
    }

    @Test
    void graphFactory_setsGraphConfigAndGraphAccessor() {
        GraphEmailConfig graph = new GraphEmailConfig("tenant", "client", "secret", "sender@x.com", "");

        ResolvedEmailConfig resolved = ResolvedEmailConfig.graph(graph, EmailConfigSource.DATABASE, false, "graph invalid");

        assertEquals(EmailProviderType.GRAPH, resolved.providerType());
        assertEquals(EmailConfigSource.DATABASE, resolved.source());
        assertNull(resolved.config());
        assertSame(graph, resolved.providerConfig());
        assertSame(graph, resolved.graphConfigOrNull());
        assertEquals("graph invalid", resolved.message());
    }
}
