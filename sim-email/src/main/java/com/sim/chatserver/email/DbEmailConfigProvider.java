package com.sim.chatserver.email;

import java.util.Optional;

/**
 * Database provider for SMTP configuration.
 *
 * Implement this interface in sim-web/sim-core (JDBC/JPA) against your
 * email_smtp_config table.
 */
public interface DbEmailConfigProvider {

    /**
     * Loads SMTP config from DB.
     *
     * @return EmailConfig if present and usable, otherwise null.
     */
    EmailConfig load();

    /**
     * Saves (insert/update) SMTP config in DB.
     *
     * @param config SMTP configuration to persist
     * @param updatedBy user/system making the change
     */
    void save(EmailConfig config, String updatedBy);

    /**
     * Optional helper for implementations: return current DB config wrapped in
     * Optional.
     */
    default Optional<EmailConfig> loadOptional() {
        return Optional.ofNullable(load());
    }

    /**
     * Optional helper for password-retention workflows: If incoming password is
     * blank, keep existing password from DB.
     */
    default EmailConfig mergeKeepingExistingPasswordIfBlank(EmailConfig incoming) {
        if (incoming == null) {
            return null;
        }

        String incomingPassword = incoming.password();
        if (incomingPassword != null && !incomingPassword.trim().isEmpty()) {
            return incoming;
        }

        EmailConfig existing = load();
        String existingPassword = existing == null ? "" : nullToEmpty(existing.password());

        return new EmailConfig(
                incoming.host(),
                incoming.port(),
                incoming.auth(),
                incoming.startTls(),
                incoming.ssl(),
                nullToEmpty(incoming.username()),
                existingPassword,
                nullToEmpty(incoming.defaultFrom())
        );
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
