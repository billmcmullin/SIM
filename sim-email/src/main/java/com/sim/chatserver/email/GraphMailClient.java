package com.sim.chatserver.email;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sends mail through Microsoft Graph API.
 */
public class GraphMailClient {

    private static final Logger LOG = Logger.getLogger(GraphMailClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Object DIAG_LOCK = new Object();

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String ENV_DIR = "SIM_SERVER_DIAGNOSTIC_LOG_DIR";
    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final int MAX_CONFIG_VALUE_LEN = 512;
    private static final Pattern SAFE_BOOL_TEXT = Pattern.compile("^(?i:true|false|1|0|yes|no|y|n|on|off)$");
    private static final Pattern SAFE_DIR_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private static volatile boolean warnedDiagFailure;

    final void sendMail(String accessToken, GraphEmailConfig config, EmailMessage message, MarkdownRenderer markdownRenderer) {
        HttpsURLConnection conn = null;
        String requestId = UUID.randomUUID().toString();
        try {
            String sender = config.senderUser().trim();
            String endpoint = "https://graph.microsoft.com/v1.0/users/" + sender + "/sendMail";

            URL url = URI.create(endpoint).toURL();
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            String html = firstNonBlank(message.htmlBody(), markdownRenderer.toHtml(message.markdownBody()));
            String text = message.textBody();
            String contentType = (html != null && !html.isBlank()) ? "HTML" : "Text";
            String content = (html != null && !html.isBlank()) ? html : (text == null ? "" : text);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contentType", contentType);
            body.put("content", content);

            Map<String, Object> graphMessage = new LinkedHashMap<>();
            graphMessage.put("subject", nvl(message.subject()));
            graphMessage.put("body", body);
            graphMessage.put("toRecipients", recipients(message.to()));
            graphMessage.put("ccRecipients", recipients(message.cc()));
            graphMessage.put("bccRecipients", recipients(message.bcc()));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", graphMessage);
            payload.put("saveToSentItems", Boolean.TRUE);

                    writeDiagnostics("graph-mail-client", requestId, "send-request",
                        "method=POST\nurl=" + endpoint
                            + "\ntoCount=" + size(message.to())
                            + "\nccCount=" + size(message.cc())
                            + "\nbccCount=" + size(message.bcc())
                            + "\nsubject=" + nvl(message.subject()), null);

            byte[] json = MAPPER.writeValueAsBytes(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }

            int status = conn.getResponseCode();
            String err = (conn.getErrorStream() == null)
                    ? ""
                    : new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                    writeDiagnostics("graph-mail-client", requestId, "send-response",
                        "status=" + status + "\nerrorBody=" + truncate(err), null);

            // Graph sendMail commonly returns 202 Accepted
            if (status != 202 && (status < 200 || status >= 300)) {
                throw new EmailException(
                        "Graph sendMail failed. HTTP " + status + " body=" + err,
                        new RuntimeException("graph_send_http_" + status)
                );
            }
        } catch (EmailException e) {
                writeDiagnostics("graph-mail-client", requestId, "send-error", "message=" + safe(e.getMessage()), e);
            throw e;
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Graph mail send failed", e);
                writeDiagnostics("graph-mail-client", requestId, "send-error", "message=" + safe(e.getMessage()), e);
            throw new EmailException("Graph mail send failed", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private List<Map<String, Object>> recipients(List<String> emails) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (emails == null) {
            return result;
        }

        for (String e : emails) {
            if (e == null || e.isBlank()) {
                continue;
            }

            Map<String, Object> emailAddress = new LinkedHashMap<>();
            emailAddress.put("address", e.trim());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("emailAddress", emailAddress);

            result.add(recipient);
        }
        return result;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private int size(List<String> values) {
        return values == null ? 0 : values.size();
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 512 ? body.substring(0, 512) + "..." : body;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void writeDiagnostics(String component, String requestId, String event, String details, Throwable error) {
        if (!diagnosticsEnabled()) {
            return;
        }

        String comp = safeToken(component, "graph-mail-client");
        String req = safeToken(requestId, "");
        String evt = safeToken(event, "event");

        synchronized (DIAG_LOCK) {
            try {
                Path dir = resolveDiagnosticsDir();
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
                if (!warnedDiagFailure) {
                    warnedDiagFailure = true;
                    LOG.log(Level.WARNING,
                            "Failed writing graph mail diagnostics log. Disable with " + ENV_ENABLED + "=false or fix " + ENV_DIR,
                            ex);
                }
            }
        }
    }

    private boolean diagnosticsEnabled() {
        String enabledRaw = readValidatedBooleanEnv(ENV_ENABLED);
        String dirRaw = readValidatedDirectoryTokenEnv(ENV_DIR);
        if (enabledRaw == null) {
            return dirRaw != null;
        }
        return isTruthy(enabledRaw);
    }

    private Path resolveDiagnosticsDir() {
        String configured = readValidatedDirectoryTokenEnv(ENV_DIR);
        String dirName = configured == null ? DEFAULT_DIR_NAME : configured;
        return Paths.get(dirName).toAbsolutePath().normalize();
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "on".equals(normalized);
    }

    private String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace('\r', ' ').replace('\n', ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String safeFileToken(String value) {
        String input = value == null ? "graph-mail-client" : value;
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
        return out.isEmpty() ? "graph-mail-client" : out.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String readValidatedBooleanEnv(String name) {
        String sanitized = sanitizeConfigValue(System.getenv(name));
        if (sanitized == null) {
            return null;
        }
        if (!SAFE_BOOL_TEXT.matcher(sanitized).matches()) {
            LOG.log(Level.WARNING, "Ignoring unsafe diagnostics config from env {0}", name);
            return null;
        }
        return sanitized;
    }

    private String readValidatedDirectoryTokenEnv(String name) {
        String sanitized = sanitizeConfigValue(System.getenv(name));
        if (sanitized == null) {
            return null;
        }
        if (!SAFE_DIR_TOKEN.matcher(sanitized).matches()) {
            LOG.log(Level.WARNING, "Ignoring unsafe diagnostics directory token from env {0}", name);
            return null;
        }
        return sanitized;
    }

    private String sanitizeConfigValue(String value) {
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
}
