package com.sim.chatserver.email;

public final class EmailFactory {

    private static final String MSG_NO_VALID_EMAIL_CONFIG = "No valid email configuration resolved";
    private static final String MSG_RESOLVED_SMTP_CONFIG_NULL = "Resolved SMTP config is null";
    private static final String MSG_RESOLVED_PROVIDER_CONFIG_NOT_GRAPH = "Resolved provider config is not GraphEmailConfig";
    private static final String MSG_UNSUPPORTED_EMAIL_PROVIDER_PREFIX = "Unsupported email provider: ";

    private EmailFactory() {
        // utility class
    }

    /**
     * Backward-compatible path: load SMTP from env/properties.
     */
    static EmailService fromEnvOrProperties() {
        EmailConfig config = EmailConfigLoader.load();
        return smtp(config);
    }

    /**
     * Builds SMTP email service with provided config.
     */
    public static EmailService smtp(EmailConfig config) {
        return new SmtpEmailService(config, new MarkdownRenderer());
    }

    /**
     * Builds Graph email service with provided config.
     */
    private static EmailService graph(GraphEmailConfig config) {
        return new GraphEmailService(
                config,
                new GraphTokenClient(config),
                new GraphMailClient(),
                new MarkdownRenderer()
        );
    }

    /**
     * Provider-based factory entrypoint.
     */
    private static EmailService forProvider(ResolvedEmailConfig resolved) {
        if (resolved == null || !resolved.valid()) {
            throw new IllegalArgumentException(MSG_NO_VALID_EMAIL_CONFIG);
        }

        EmailProviderType provider = resolved.providerType() == null
                ? EmailProviderType.SMTP
                : resolved.providerType();

        return switch (provider) {
            case SMTP -> {
                EmailConfig smtp = resolved.config();
                if (smtp == null) {
                    throw new IllegalArgumentException(MSG_RESOLVED_SMTP_CONFIG_NULL);
                }
                yield smtp(smtp);
            }
            case GRAPH -> {
                if (!(resolved.providerConfig() instanceof GraphEmailConfig gcfg)) {
                    throw new IllegalArgumentException(MSG_RESOLVED_PROVIDER_CONFIG_NOT_GRAPH);
                }
                yield graph(gcfg);
            }
            default -> throw new IllegalStateException(MSG_UNSUPPORTED_EMAIL_PROVIDER_PREFIX + provider);
        };
    }

    public static EmailService createForProvider(ResolvedEmailConfig resolved) {
        return forProvider(resolved);
    }
}
