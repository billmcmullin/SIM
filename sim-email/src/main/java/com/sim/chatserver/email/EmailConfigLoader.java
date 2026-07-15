package com.sim.chatserver.email;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads SMTP configuration from: 1) Environment variables (MAIL_*) 2) External
 * properties file pointed by MAIL_CONFIG_FILE 3) Classpath email.properties
 *
 * Notes: - loadEnvOnly(): env only, returns null if required values are
 * missing/invalid. - loadPropertiesOnly(): properties only, returns null if
 * required values are missing/invalid. - load(): backward-compatible
 * convenience (ENV first, then properties, else localhost:25 defaults).
 */
public final class EmailConfigLoader {

    private static final Logger log = Logger.getLogger(EmailConfigLoader.class.getName());

    // Env keys
    private static final String ENV_HOST = "MAIL_HOST";
    private static final String ENV_PORT = "MAIL_PORT";
    private static final String ENV_AUTH = "MAIL_AUTH";
    private static final String ENV_STARTTLS = "MAIL_STARTTLS";
    private static final String ENV_SSL = "MAIL_SSL";
    private static final String ENV_USERNAME = "MAIL_USERNAME";
    private static final String ENV_PASSWORD = "MAIL_PASSWORD";
    private static final String ENV_FROM = "MAIL_FROM";
    private static final String ENV_CONFIG_FILE = "MAIL_CONFIG_FILE";

    // Properties keys
    private static final String PROP_HOST = "mail.host";
    private static final String PROP_PORT = "mail.port";
    private static final String PROP_AUTH = "mail.auth";
    private static final String PROP_STARTTLS = "mail.starttls";
    private static final String PROP_SSL = "mail.ssl";
    private static final String PROP_USERNAME = "mail.username";
    private static final String PROP_PASSWORD = "mail.password";
    private static final String PROP_FROM = "mail.from";

    private static final int MAX_ENV_TEXT_LEN = 512;
    private static final int MAX_ENV_SECRET_LEN = 4096;
    private static final int MAX_PATH_LEN = 2048;

    @FunctionalInterface
    interface EnvAccessor {
        String get(String key);
    }

    private static volatile EnvAccessor envAccessor = System::getenv;

    private EmailConfigLoader() {
        // utility
    }

    static void setEnvAccessorForTests(EnvAccessor accessor) {
        envAccessor = accessor == null ? System::getenv : accessor;
    }

    static void resetEnvAccessorForTests() {
        envAccessor = System::getenv;
    }

    private static String env(String key) {
        return envAccessor.get(key);
    }

    /**
     * Backward-compatible convenience loader: ENV -> PROPERTIES -> fallback
     * localhost:25
     */
    public static EmailConfig load() {
        EmailConfig env = loadEnvOnly();
        if (env != null) {
            return env;
        }

        EmailConfig props = loadPropertiesOnly();
        if (props != null) {
            return props;
        }

        log.warning("No valid SMTP config found in ENV or properties. Falling back to localhost:25.");
        return new EmailConfig(
                "localhost",
                25,
                false,
                false,
                false,
                "",
                "",
                ""
        );
    }

    /**
     * Loads SMTP config from environment variables only. Required: MAIL_HOST,
     * MAIL_PORT Returns null if missing/invalid.
     */
    public static EmailConfig loadEnvOnly() {
        String host = trimToNull(envSanitized(ENV_HOST, MAX_ENV_TEXT_LEN));
        Integer port = parsePort(trimToNull(envSanitized(ENV_PORT, 6)), "ENV " + ENV_PORT);

        if (host == null || port == null) {
            return null;
        }

        boolean auth = parseBoolean(env(ENV_AUTH), false);
        boolean starttls = parseBoolean(env(ENV_STARTTLS), false);
        boolean ssl = parseBoolean(env(ENV_SSL), false);

        String username = defaultString(envSanitized(ENV_USERNAME, MAX_ENV_TEXT_LEN), "");
        String password = defaultString(envSanitized(ENV_PASSWORD, MAX_ENV_SECRET_LEN), "");
        String from = defaultString(envSanitized(ENV_FROM, MAX_ENV_TEXT_LEN), "");

        return new EmailConfig(host, port, auth, starttls, ssl, username, password, from);
    }

    /**
     * Loads SMTP config from properties only: 1) External file from
     * MAIL_CONFIG_FILE 2) Classpath email.properties
     *
     * Required: mail.host, mail.port Returns null if missing/invalid.
     */
    public static EmailConfig loadPropertiesOnly() {
        Properties p = loadExternalPropsFromEnvPath();
        if (p.isEmpty()) {
            p = loadClasspathProps();
        }
        return fromProperties(p);
    }

    private static EmailConfig fromProperties(Properties p) {
        if (p == null || p.isEmpty()) {
            return null;
        }

        String host = trimToNull(p.getProperty(PROP_HOST));
        Integer port = parsePort(trimToNull(p.getProperty(PROP_PORT)), "properties " + PROP_PORT);

        if (host == null || port == null) {
            return null;
        }

        boolean auth = parseBoolean(p.getProperty(PROP_AUTH), false);
        boolean starttls = parseBoolean(p.getProperty(PROP_STARTTLS), false);
        boolean ssl = parseBoolean(p.getProperty(PROP_SSL), false);

        String username = defaultString(p.getProperty(PROP_USERNAME), "");
        String password = defaultString(p.getProperty(PROP_PASSWORD), "");
        String from = defaultString(p.getProperty(PROP_FROM), "");

        return new EmailConfig(host, port, auth, starttls, ssl, username, password, from);
    }

    private static Properties loadExternalPropsFromEnvPath() {
        Properties p = new Properties();
        String pathValue = trimToNull(envSanitized(ENV_CONFIG_FILE, MAX_PATH_LEN));
        if (pathValue == null) {
            return p;
        }

        Path path;
        try {
            path = Path.of(pathValue).normalize();
        } catch (InvalidPathException ex) {
            log.warning("MAIL_CONFIG_FILE is set to an invalid path.");
            return p;
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.warning("MAIL_CONFIG_FILE is set but file does not exist or is not a regular file.");
            return p;
        }

        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            log.info("Loaded SMTP properties from external file.");
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load SMTP properties from external file.", e);
        }

        return p;
    }

    private static Properties loadClasspathProps() {
        Properties p = new Properties();
        try (InputStream in = EmailConfigLoader.class.getResourceAsStream("/email.properties")) {

            if (in == null) {
                return p;
            }
            p.load(in);
            log.info("Loaded SMTP properties from classpath: email.properties");
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed loading classpath email.properties", e);
        }
        return p;
    }

    private static Integer parsePort(String value, String sourceLabel) {
        if (value == null) {
            return null;
        }
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 1 || port > 65535) {
                log.log(Level.WARNING, "Invalid SMTP port out of range in {0}.", sourceLabel);
                return null;
            }
            return port;
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "Invalid SMTP port in {0}.", sourceLabel);
            return null;
        }
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        String v = trimToNull(value);
        if (v == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v)
                || "1".equals(v)
                || "yes".equalsIgnoreCase(v)
                || "y".equalsIgnoreCase(v)
                || "on".equalsIgnoreCase(v);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String defaultString(String s, String def) {
        return s == null ? def : s.trim();
    }

    private static String envSanitized(String key, int maxLength) {
        return sanitizeUntrustedText(env(key), maxLength);
    }

    private static String sanitizeUntrustedText(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        int effectiveMaxLength = Math.max(1, maxLength);
        StringBuilder cleaned = new StringBuilder(Math.min(value.length(), effectiveMaxLength));

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isISOControl(c)) {
                cleaned.append(c);
            }
        }

        String trimmed = cleaned.toString().trim();
        if (trimmed.length() <= effectiveMaxLength) {
            return trimmed;
        }
        return trimmed.substring(0, effectiveMaxLength);
    }
}
