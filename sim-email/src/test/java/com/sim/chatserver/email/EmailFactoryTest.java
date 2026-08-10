package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class EmailFactoryTest {

    @Test
    @DisplayName("fromEnvOrProperties returns SMTP service")
    void fromEnvOrProperties_returnsSmtpService() {
        EmailConfig config = smtpConfig();

        try (MockedStatic<EmailConfigLoader> loaderMock = mockStatic(EmailConfigLoader.class)) {
            loaderMock.when(EmailConfigLoader::load).thenReturn(config);

            EmailService service = EmailFactory.fromEnvOrProperties();

            assertInstanceOf(SmtpEmailService.class, service);
        }
    }

    @Test
    @DisplayName("forProvider throws for null resolved config")
    void forProvider_nullResolved_throws() {
        assertThrows(IllegalArgumentException.class, () -> EmailFactory.forProvider(null));
    }

    @Test
    @DisplayName("forProvider throws for invalid resolved config")
    void forProvider_invalidResolved_throws() {
        ResolvedEmailConfig resolved = new ResolvedEmailConfig(
                smtpConfig(),
                EmailConfigSource.NONE,
                false,
                "not valid",
                EmailProviderType.SMTP,
                smtpConfig()
        );

        assertThrows(IllegalArgumentException.class, () -> EmailFactory.forProvider(resolved));
    }

    @Test
    @DisplayName("forProvider returns SMTP service")
    void forProvider_smtp_returnsSmtpService() {
        EmailConfig config = smtpConfig();
        ResolvedEmailConfig resolved = ResolvedEmailConfig.smtp(
                config,
                EmailConfigSource.DATABASE,
                true,
                "ok"
        );

        EmailService service = EmailFactory.forProvider(resolved);

        assertInstanceOf(SmtpEmailService.class, service);
    }

    @Test
    @DisplayName("forProvider throws when SMTP config is missing")
    void forProvider_smtpMissingConfig_throws() {
        ResolvedEmailConfig resolved = new ResolvedEmailConfig(
                null,
                EmailConfigSource.NONE,
                true,
                "ok",
                EmailProviderType.SMTP,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> EmailFactory.forProvider(resolved));
    }

    @Test
    @DisplayName("forProvider throws when GRAPH provider config has wrong type")
    void forProvider_graphWrongProviderConfigType_throws() {
        ResolvedEmailConfig resolved = new ResolvedEmailConfig(
                null,
                EmailConfigSource.NONE,
                true,
                "ok",
                EmailProviderType.GRAPH,
                "not-a-graph-config"
        );

        assertThrows(IllegalArgumentException.class, () -> EmailFactory.forProvider(resolved));
    }

    @Test
    @DisplayName("forProvider returns Graph service")
    void forProvider_graph_returnsGraphService() {
        GraphEmailConfig graph = new GraphEmailConfig(
                "tenant-id",
                "client-id",
                "client-secret",
                "sender@example.com",
                "login.microsoftonline.com"
        );
        ResolvedEmailConfig resolved = ResolvedEmailConfig.graph(
                graph,
                EmailConfigSource.PROPERTIES,
                true,
                "ok"
        );

        EmailService service = EmailFactory.forProvider(resolved);

        assertInstanceOf(GraphEmailService.class, service);
    }

    private static EmailConfig smtpConfig() {
        return new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "smtp-user",
                "smtp-pass",
                "noreply@example.com"
        );
    }
}
