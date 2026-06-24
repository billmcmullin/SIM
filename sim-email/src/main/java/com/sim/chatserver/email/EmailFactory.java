package com.sim.chatserver.email;

public final class EmailFactory {
    private EmailFactory() {
        // utility class
    }

    /**
     * Loads config from env vars first, then email.properties fallback.
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
}
