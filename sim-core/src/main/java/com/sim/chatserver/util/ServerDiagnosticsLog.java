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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Object LOCK = new Object();

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

    public static boolean isEnabled() {
        String enabledRaw = trimToNull(System.getenv(ENV_ENABLED));
        String dirRaw = trimToNull(System.getenv(ENV_DIR));

        if (enabledRaw == null) {
            return dirRaw != null;
        }
        return isTruthy(enabledRaw);
    }

    private static Path resolveLogDirectory() {
        String configured = trimToNull(System.getenv(ENV_DIR));
        if (configured != null) {
            return Paths.get(configured);
        }

        String jbossLogDir = trimToNull(System.getProperty("jboss.server.log.dir"));
        if (jbossLogDir != null) {
            return Paths.get(jbossLogDir, DEFAULT_DIR_NAME);
        }

        return Paths.get(DEFAULT_DIR_NAME);
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
}
