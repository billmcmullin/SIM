package com.sim.chatserver.email;

public final class EmailFactory {

    private EmailFactory() {
        // utility class
    }

    /**
     * Backward-compatible path: load SMTP from env/properties.
     */
    public static EmailService fromEnvOrProperties() {
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
    public static EmailService graph(GraphEmailConfig config) {
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
    public static EmailService forProvider(ResolvedEmailConfig resolved) {
        if (resolved == null || !resolved.valid()) {
            throw new IllegalArgumentException("No valid email configuration resolved");
        }

        EmailProviderType provider = resolved.providerType() == null
                ? EmailProviderType.SMTP
                : resolved.providerType();

        return switch (provider) {
            case SMTP -> {
                EmailConfig smtp = resolved.config();
                if (smtp == null) {
                    throw new IllegalArgumentException("Resolved SMTP config is null");
                }
                yield smtp(smtp);
            }
            case GRAPH -> {
                if (!(resolved.providerConfig() instanceof GraphEmailConfig gcfg)) {
                    throw new IllegalArgumentException("Resolved provider config is not GraphEmailConfig");
                }
                yield graph(gcfg);
            }
        };
    }
}
