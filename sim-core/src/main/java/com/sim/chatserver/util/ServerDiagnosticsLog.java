package com.sim.chatserver.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * File-backed diagnostics sink for high-detail server events.
 *
 * Intended usage:
 * - Keep WildFly console/server log concise.
 * - Persist full diagnostic context to dedicated files when enabled.
 */
public final class ServerDiagnosticsLog {

    private static final Logger log = Logger.getLogger(ServerDiagnosticsLog.class.getName());

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String ENV_DIR = "SIM_SERVER_DIAGNOSTIC_LOG_DIR";

    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final int MAX_CONFIG_VALUE_LEN = 512;
    private static final Object LOCK = new Object();
    private static final Pattern SAFE_BOOL_TEXT = Pattern.compile("^(?i:true|false|1|0|yes|no|y|n|on|off)$");
    private static final Pattern SAFE_DIR_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
        private static final Map<String, String> ENV_VALUES = System.getenv();
        private static final String ENABLED_ENV_VALUE = readValidatedEnvValue(ENV_ENABLED, SAFE_BOOL_TEXT,
            "Ignoring unsafe diagnostics config from env {0}");
        private static final String DIR_ENV_VALUE = readValidatedEnvValue(ENV_DIR, SAFE_DIR_TOKEN,
            "Ignoring unsafe diagnostics directory token from env {0}");

    private static volatile boolean warnedWriteFailure;

    private ServerDiagnosticsLog() {
    }

    public static void write(String component, String requestId, String event, String details) {
        write(component, requestId, event, details, null);
    }

    public static void write(String component, String requestId, String event, String details, Throwable error) {
        if (!isEnabled()) {
            return;
        }

        String comp = safeToken(component, "server");
        String req = safeToken(requestId, "");
        String evt = safeToken(event, "event");

        synchronized (LOCK) {
            try {
                Path dir = resolveLogDirectory();
                Files.createDirectories(dir);

                String fileName = safeFileToken(comp) + '-' + LocalDate.now() + ".log";
                Path file = dir.resolve(fileName);

                String lineSep = System.lineSeparator();
                StringBuilder entry = new StringBuilder(1024);
                entry.append("================================================================================")
                    .append(lineSep)
                    .append("Timestamp : ").append(Instant.now()).append(lineSep)
                    .append("Component : ").append(comp).append(lineSep)
                    .append("RequestId : ").append(req.isBlank() ? "(none)" : req).append(lineSep)
                    .append("Event     : ").append(evt).append(lineSep);

                if (details != null && !details.isBlank()) {
                    entry.append("Details:").append(lineSep)
                        .append(details)
                        .append(lineSep);
                }

                if (error != null) {
                    entry.append("Error:")
                        .append(lineSep)
                        .append(error.getClass().getName())
                        .append(": ")
                        .append(safeErrorMessage(error))
                        .append(lineSep);

                    StringWriter sw = new StringWriter();
                    error.printStackTrace(new PrintWriter(sw));
                    entry.append("Stack Trace:")
                        .append(lineSep)
                            .append(sw)
                        .append(lineSep);
                }

                entry.append("================================================================================")
                    .append(lineSep)
                    .append(lineSep);

                Files.writeString(
                        file,
                        entry,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE
                );
            } catch (IOException ex) {
                if (!warnedWriteFailure) {
                    warnedWriteFailure = true;
                    log.log(Level.WARNING,
                            "Failed writing server diagnostics log. Disable with " + ENV_ENABLED + "=false or fix " + ENV_DIR,
                            ex);
                }
            }
        }
    }

    private static boolean isEnabled() {
        String enabledRaw = ENABLED_ENV_VALUE;
        String dirRaw = DIR_ENV_VALUE;

        if (enabledRaw == null) {
            return dirRaw != null;
        }
        return isTruthy(enabledRaw);
    }

    private static Path resolveLogDirectory() {
        String configured = DIR_ENV_VALUE;
        String dirName = configured == null ? DEFAULT_DIR_NAME : configured;
        Path base = Paths.get("").toAbsolutePath().normalize();
        Path resolved = base.resolve(dirName).normalize();
        if (!resolved.startsWith(base)) {
            return base.resolve(DEFAULT_DIR_NAME).normalize();
        }
        return resolved;
    }

    private static boolean isTruthy(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "on".equals(normalized);
    }

    private static String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace('\r', ' ').replace('\n', ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private static String safeFileToken(String value) {
        String input = value == null ? "server" : value;
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.isEmpty() ? "server" : out.toString();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String readValidatedEnvValue(String name, Pattern pattern, String warningMessage) {
        String sanitized = sanitizeConfigValue(ENV_VALUES.get(name));
        if (sanitized == null) {
            return null;
        }
        if (!pattern.matcher(sanitized).matches()) {
            log.log(Level.WARNING, warningMessage, name);
            return null;
        }
        return sanitized;
    }

    private static String sanitizeConfigValue(String value) {
        if (value == null) {
            return null;
        }

        StringBuilder cleaned = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isISOControl(c)) {
                cleaned.append(c);
            }
        }

        String trimmed = trimToNull(cleaned.toString());
        if (trimmed == null) {
            return null;
        }
        if (trimmed.length() > MAX_CONFIG_VALUE_LEN) {
            return trimmed.substring(0, MAX_CONFIG_VALUE_LEN);
        }
        return trimmed;
    }

    private static String safeErrorMessage(Throwable error) {
        if (error == null) {
            return "";
        }
        String msg = error.getMessage();
        if (msg == null) {
            return "";
        }
        return msg.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
