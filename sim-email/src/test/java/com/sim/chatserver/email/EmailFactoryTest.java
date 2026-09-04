package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class EmailFactoryTest {

    @Test
    @DisplayName("fromEnvOrProperties returns SMTP service")
    void fromEnvOrProperties_returnsSmtpService() {
        EmailConfig config = mock(EmailConfig.class);

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
        EmailConfig smtpConfig = mock(EmailConfig.class);
        ResolvedEmailConfig resolved = new ResolvedEmailConfig(
            smtpConfig,
                EmailConfigSource.NONE,
                false,
                "not valid",
                EmailProviderType.SMTP,
            smtpConfig
        );

        assertThrows(IllegalArgumentException.class, () -> EmailFactory.forProvider(resolved));
    }

    @Test
    @DisplayName("forProvider returns SMTP service")
    void forProvider_smtp_returnsSmtpService() {
        EmailConfig config = mock(EmailConfig.class);
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
    @DisplayName("forProvider defaults null provider type to SMTP")
    void forProvider_nullProviderType_defaultsToSmtp() {
        EmailConfig config = mock(EmailConfig.class);
        ResolvedEmailConfig resolved = mock(ResolvedEmailConfig.class);
        when(resolved.valid()).thenReturn(true);
        when(resolved.providerType()).thenReturn(null);
        when(resolved.config()).thenReturn(config);

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
        GraphEmailConfig graph = mock(GraphEmailConfig.class);
        ResolvedEmailConfig resolved = ResolvedEmailConfig.graph(
                graph,
                EmailConfigSource.PROPERTIES,
                true,
                "ok"
        );

        EmailService service = EmailFactory.forProvider(resolved);

        assertInstanceOf(GraphEmailService.class, service);
    }

    @Test
    @DisplayName("createForProvider delegates to provider factory")
    void createForProvider_returnsSmtpService() {
        EmailConfig smtpConfig = mock(EmailConfig.class);
        ResolvedEmailConfig resolved = ResolvedEmailConfig.smtp(
            smtpConfig,
                EmailConfigSource.PROPERTIES,
                true,
                "ok"
        );

        EmailService service = EmailFactory.createForProvider(resolved);

        assertInstanceOf(SmtpEmailService.class, service);
    }
}
