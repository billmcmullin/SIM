package com.sim.chatserver.email;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

    private static final Logger log = Logger.getLogger(EmailConfigLoader.class.getCanonicalName());

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
    private static final Pattern SAFE_HOST = Pattern.compile("^[a-zA-Z0-9.-]{1,253}$");
    private static final Pattern SAFE_PORT_TEXT = Pattern.compile("^\\d{1,5}$");
    private static final Pattern SAFE_BOOL_TEXT = Pattern.compile("^(?i:true|false|1|0|yes|no|y|n|on|off)$");
    private static final String SAFE_EMAIL_REGEX_LOCAL_PART = "^[^\\s@]+";
    private static final String SAFE_EMAIL_REGEX_DOMAIN_PART = "@[^\\s@]+\\.[^\\s@]+$";
    private static final Pattern SAFE_EMAIL = Pattern.compile(SAFE_EMAIL_REGEX_LOCAL_PART + SAFE_EMAIL_REGEX_DOMAIN_PART);
    private static final String MSG_FALLBACK_TO_LOCALHOST = "No valid SMTP config found in ENV or properties. Falling back to localhost:25.";
    private static final String SOURCE_ENV_PREFIX = "ENV ";
    private static final String SOURCE_PROPERTIES_PREFIX = "properties ";
    private static final String EMPTY = "";
    private static final String MSG_INVALID_CONFIG_PATH = "MAIL_CONFIG_FILE is set to an invalid path.";
    private static final String MSG_CONFIG_PATH_NOT_FILE = "MAIL_CONFIG_FILE is set but file does not exist or is not a regular file.";
    private static final String MSG_LOADED_EXTERNAL_PROPS = "Loaded SMTP properties from external file.";
    private static final String MSG_FAILED_EXTERNAL_PROPS = "Failed to load SMTP properties from external file.";
    private static final String MSG_LOADED_CLASSPATH_PROPS = "Loaded SMTP properties from classpath: email.properties";
    private static final String MSG_FAILED_CLASSPATH_PROPS = "Failed loading classpath email.properties";
    private static final String MSG_INVALID_PORT_RANGE = "Invalid SMTP port out of range in {0}.";
    private static final String MSG_INVALID_PORT = "Invalid SMTP port in {0}.";
    private static final String BOOL_TRUE = "true";
    private static final String BOOL_ONE = "1";
    private static final String BOOL_YES = "yes";
    private static final String BOOL_Y = "y";
    private static final String BOOL_ON = "on";

    private static final EmailConfig DEFAULT_EMAIL_CONFIG = new EmailConfig(
            "localhost",
            25,
            false,
            false,
            false,
            EMPTY,
            EMPTY,
            EMPTY
        );

    @FunctionalInterface
    interface EnvAccessor {
        String get(String key);
    }

    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static volatile EnvAccessor envAccessor = ENV::get;

    private EmailConfigLoader() {
        // utility
    }

    static void setEnvAccessorForTests(EnvAccessor accessor) {
        envAccessor = accessor == null ? ENV::get : accessor;
    }

    static void resetEnvAccessorForTests() {
        envAccessor = ENV::get;
    }

    private static String env(String key) {
        return envAccessor.get(key);
    }

    /**
     * Backward-compatible convenience loader: ENV -> PROPERTIES -> fallback
     * localhost:25
     */
    static EmailConfig load() {
        EmailConfig env = loadEnvOnly();
        if (env != null) {
            return env;
        }

        EmailConfig props = loadPropertiesOnly();
        if (props != null) {
            return props;
        }

        log.warning(MSG_FALLBACK_TO_LOCALHOST);
        return DEFAULT_EMAIL_CONFIG;
    }

    /**
     * Loads SMTP config from environment variables only. Required: MAIL_HOST,
     * MAIL_PORT Returns null if missing/invalid.
     */
    static EmailConfig loadEnvOnly() {
        String host = trimToNull(envSanitized(ENV_HOST, MAX_ENV_TEXT_LEN));
        int port = parsePort(trimToNull(envSanitized(ENV_PORT, 6)), SOURCE_ENV_PREFIX + ENV_PORT);

        if (host == null || port < 1) {
            return null;
        }

        boolean auth = parseBoolean(envSanitized(ENV_AUTH, 16), false);
        boolean starttls = parseBoolean(envSanitized(ENV_STARTTLS, 16), false);
        boolean ssl = parseBoolean(envSanitized(ENV_SSL, 16), false);

        String username = defaultString(envSanitized(ENV_USERNAME, MAX_ENV_TEXT_LEN), EMPTY);
        String password = defaultString(envSanitized(ENV_PASSWORD, MAX_ENV_SECRET_LEN), EMPTY);
        String from = defaultString(envSanitized(ENV_FROM, MAX_ENV_TEXT_LEN), EMPTY);

        return new EmailConfig(host, port, auth, starttls, ssl, username, password, from);
    }

    /**
     * Loads SMTP config from properties only: 1) External file from
     * MAIL_CONFIG_FILE 2) Classpath email.properties
     *
     * Required: mail.host, mail.port Returns null if missing/invalid.
     */
    static EmailConfig loadPropertiesOnly() {
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
        int port = parsePort(trimToNull(p.getProperty(PROP_PORT)), SOURCE_PROPERTIES_PREFIX + PROP_PORT);

        if (host == null || port < 1) {
            return null;
        }

        boolean auth = parseBoolean(p.getProperty(PROP_AUTH), false);
        boolean starttls = parseBoolean(p.getProperty(PROP_STARTTLS), false);
        boolean ssl = parseBoolean(p.getProperty(PROP_SSL), false);

        String username = defaultString(p.getProperty(PROP_USERNAME), EMPTY);
        String password = defaultString(p.getProperty(PROP_PASSWORD), EMPTY);
        String from = defaultString(p.getProperty(PROP_FROM), EMPTY);

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
            log.warning(MSG_INVALID_CONFIG_PATH);
            return p;
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.warning(MSG_CONFIG_PATH_NOT_FILE);
            return p;
        }

        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            log.info(MSG_LOADED_EXTERNAL_PROPS);
        } catch (IOException e) {
            log.log(Level.WARNING, MSG_FAILED_EXTERNAL_PROPS, e);
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
            log.info(MSG_LOADED_CLASSPATH_PROPS);
        } catch (IOException e) {
            log.log(Level.WARNING, MSG_FAILED_CLASSPATH_PROPS, e);
        }
        return p;
    }

    private static int parsePort(String value, String sourceLabel) {
        if (value == null) {
            return -1;
        }
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 1 || port > 65535) {
                log.log(Level.WARNING, MSG_INVALID_PORT_RANGE, sourceLabel);
                return -1;
            }
            return port;
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, MSG_INVALID_PORT, sourceLabel);
            return -1;
        }
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        String v = trimToNull(value);
        if (v == null) {
            return defaultValue;
        }
        return BOOL_TRUE.equalsIgnoreCase(v)
                || BOOL_ONE.equals(v)
                || BOOL_YES.equalsIgnoreCase(v)
                || BOOL_Y.equalsIgnoreCase(v)
                || BOOL_ON.equalsIgnoreCase(v);
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
        String canonicalKey = canonicalizeEnvKey(key);
        if (canonicalKey == null) {
            return null;
        }
        return validateEnvValue(canonicalKey, sanitizeUntrustedText(env(canonicalKey), maxLength));
    }

    private static String canonicalizeEnvKey(String key) {
        String normalized = sanitizeUntrustedText(key, 64);
        if (normalized == null) {
            return null;
        }

        return switch (normalized) {
            case ENV_HOST,
                    ENV_PORT,
                    ENV_AUTH,
                    ENV_STARTTLS,
                    ENV_SSL,
                    ENV_USERNAME,
                    ENV_PASSWORD,
                    ENV_FROM,
                    ENV_CONFIG_FILE -> normalized;
            default -> null;
        };
    }

    private static String validateEnvValue(String key, String value) {
        if (value == null) {
            return null;
        }

        return switch (key) {
            case ENV_HOST -> SAFE_HOST.matcher(value).matches() ? value : null;
            case ENV_PORT -> SAFE_PORT_TEXT.matcher(value).matches() ? value : null;
            case ENV_AUTH, ENV_STARTTLS, ENV_SSL -> SAFE_BOOL_TEXT.matcher(value).matches() ? value : null;
            case ENV_FROM -> SAFE_EMAIL.matcher(value).matches() ? value : null;
            case ENV_CONFIG_FILE -> isSafePathText(value) ? value : null;
            case ENV_USERNAME, ENV_PASSWORD -> value;
            default -> null;
        };
    }

    private static boolean isSafePathText(String value) {
        if (value == null || value.length() > MAX_PATH_LEN) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isISOControl(c)) {
                return false;
            }
        }
        return true;
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
        return new String(trimmed.toCharArray(), 0, effectiveMaxLength);
    }
}
