package com.sim.chatserver.email;

import java.util.logging.Logger;

public final class EmailConfigResolver {

    private static final Logger log = Logger.getLogger(EmailConfigResolver.class.getName());

    private final DbEmailConfigProvider dbProvider;

    public EmailConfigResolver(DbEmailConfigProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public ResolvedEmailConfig resolve() {
        // 1) ENV
        EmailConfig env = EmailConfigLoader.loadEnvOnly();
        if (isUsable(env)) {
            return new ResolvedEmailConfig(env, EmailConfigSource.ENV, true, "Using SMTP config from ENV");
        }

        // 2) properties (external MAIL_CONFIG_FILE or classpath email.properties)
        EmailConfig props = EmailConfigLoader.loadPropertiesOnly();
        if (isUsable(props)) {
            return new ResolvedEmailConfig(props, EmailConfigSource.PROPERTIES, true, "Using SMTP config from properties");
        }

        // 3) DB
        EmailConfig db = null;
        if (dbProvider != null) {
            try {
                db = dbProvider.load();
            } catch (Exception e) {
                log.warning("Failed loading SMTP config from database: " + e.getMessage());
            }
        }
        if (isUsable(db)) {
            return new ResolvedEmailConfig(db, EmailConfigSource.DATABASE, true, "Using SMTP config from database");
        }

        // 4) none
        return new ResolvedEmailConfig(
                null,
                EmailConfigSource.NONE,
                false,
                "No valid SMTP configuration found in ENV, properties, or database."
        );
    }

    private boolean isUsable(EmailConfig c) {
        return c != null
                && hasText(c.host())
                && c.port() > 0 && c.port() <= 65535;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
