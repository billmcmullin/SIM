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

    private static final Logger log = Logger.getLogger(ServerDiagnosticsLog.class.getCanonicalName());

    private static final String EMPTY = "";
    private static final String DEFAULT_COMPONENT = "server";
    private static final String DEFAULT_EVENT = "event";
    private static final String LOG_SUFFIX = ".log";
    private static final String MSG_WRITE_FAILURE_PREFIX = "Failed writing server diagnostics log. Disable with ";
    private static final String MSG_WRITE_FAILURE_MIDDLE = "=false or fix ";
    private static final String ENTRY_SEPARATOR = "================================================================================";
    private static final String ENTRY_TIMESTAMP = "Timestamp : ";
    private static final String ENTRY_COMPONENT = "Component : ";
    private static final String ENTRY_REQUEST_ID = "RequestId : ";
    private static final String ENTRY_NO_REQUEST = "(none)";
    private static final String ENTRY_EVENT = "Event     : ";
    private static final String ENTRY_DETAILS = "Details:";
    private static final String ENTRY_ERROR = "Error:";
    private static final String ENTRY_COLON_SPACE = ": ";
    private static final String ENTRY_STACK_TRACE = "Stack Trace:";
    private static final String TRUTHY_TRUE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
    private static final String TRUTHY_ONE = "1";
    private static final String TRUTHY_YES = com.sim.chatserver.util.CommonLiteralConstants.VALUE_YES;
    private static final String TRUTHY_Y = "y";
    private static final String TRUTHY_ON = "on";

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String ENV_DIR = "SIM_SERVER_DIAGNOSTIC_LOG_DIR";

    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final int MAX_CONFIG_VALUE_LEN = 512;
    private static final Object LOCK = new Object();
    private static final Pattern SAFE_BOOL_TEXT = Pattern.compile("^(?i:true|false|1|0|yes|no|y|n|on|off)$");
    private static final Pattern SAFE_DIR_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private static final Map<String, String> ENV_VALUES = new ProcessBuilder().environment();
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

        String comp = safeToken(component, DEFAULT_COMPONENT);
        String req = safeToken(requestId, EMPTY);
        String evt = safeToken(event, DEFAULT_EVENT);

        synchronized (LOCK) {
            try {
                Path dir = resolveLogDirectory();
                Files.createDirectories(dir);

                Path file = resolveDailyLogPath(dir, comp);
                String entry = buildEntry(comp, req, evt, details, error);
                appendEntry(file, entry);
            } catch (IOException ex) {
                if (!warnedWriteFailure) {
                    warnedWriteFailure = true;
                    log.log(Level.WARNING,
                            MSG_WRITE_FAILURE_PREFIX + ENV_ENABLED + MSG_WRITE_FAILURE_MIDDLE + ENV_DIR,
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
        Path base = Paths.get(EMPTY).toAbsolutePath().normalize();
        Path resolved = base.resolve(dirName).normalize();
        if (!resolved.startsWith(base)) {
            return base.resolve(DEFAULT_DIR_NAME).normalize();
        }
        return resolved;
    }

    private static Path resolveDailyLogPath(Path dir, String component) {
        String fileName = TextIoSanitizerUtil.safeFileToken(component, DEFAULT_COMPONENT) + '-' + LocalDate.now() + LOG_SUFFIX;
        return dir.resolve(fileName);
    }

    private static String buildEntry(String component, String requestId, String event, String details, Throwable error) {
        String lineSep = System.lineSeparator();
        StringBuilder entry = new StringBuilder(1024);
        entry.append(ENTRY_SEPARATOR)
            .append(lineSep)
            .append(ENTRY_TIMESTAMP).append(Instant.now()).append(lineSep)
            .append(ENTRY_COMPONENT).append(component).append(lineSep)
            .append(ENTRY_REQUEST_ID).append(requestId.isBlank() ? ENTRY_NO_REQUEST : requestId).append(lineSep)
            .append(ENTRY_EVENT).append(event).append(lineSep);

        if (details != null && !details.isBlank()) {
            entry.append(ENTRY_DETAILS).append(lineSep)
                .append(details)
                .append(lineSep);
        }

        if (error != null) {
            entry.append(ENTRY_ERROR)
                .append(lineSep)
                .append(error.getClass().getCanonicalName())
                .append(ENTRY_COLON_SPACE)
                .append(safeErrorMessage(error))
                .append(lineSep);

            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            entry.append(ENTRY_STACK_TRACE)
                .append(lineSep)
                .append(sw)
                .append(lineSep);
        }

        entry.append(ENTRY_SEPARATOR)
            .append(lineSep)
            .append(lineSep);
        return entry.toString();
    }

    private static void appendEntry(Path file, String entry) throws IOException {
        Files.writeString(
                file,
                entry,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
    }

    private static boolean isTruthy(String value) {
    String normalized = value == null ? EMPTY : value.trim().toLowerCase(Locale.ROOT);
    return TRUTHY_TRUE.equals(normalized)
        || TRUTHY_ONE.equals(normalized)
        || TRUTHY_YES.equals(normalized)
        || TRUTHY_Y.equals(normalized)
        || TRUTHY_ON.equals(normalized);
    }

    private static String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace('\r', ' ').replace('\n', ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
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
            return TextIoSanitizerUtil.safeSlice(trimmed, 0, MAX_CONFIG_VALUE_LEN);
        }
        return trimmed;
    }

    private static String safeErrorMessage(Throwable error) {
        if (error == null) {
            return EMPTY;
        }
        String msg = error.getMessage();
        if (msg == null) {
            return EMPTY;
        }
        return msg.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
