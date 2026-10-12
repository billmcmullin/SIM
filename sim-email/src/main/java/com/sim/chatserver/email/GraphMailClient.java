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

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sends mail through Microsoft Graph API.
 */
public class GraphMailClient {

    private static final Logger LOG = Logger.getLogger(GraphMailClient.class.getCanonicalName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Object DIAG_LOCK = new Object();
    private static final String LS = System.lineSeparator();

    private static final String ENV_ENABLED = "SIM_SERVER_DIAGNOSTIC_LOG_ENABLED";
    private static final String DEFAULT_DIR_NAME = "sim-diagnostics";
    private static final String PROP_ENABLED = "sim.server.diagnostic.log.enabled";
    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = "unused";
    private static final String COMPONENT_GRAPH_MAIL_CLIENT = "graph-mail-client";
    private static final String GRAPH_ENDPOINT_PREFIX = "https://graph.microsoft.com/v1.0/users/";
    private static final String GRAPH_ENDPOINT_SEND_MAIL = "/sendMail";
    private static final String METHOD_POST = "POST";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_HTML = "HTML";
    private static final String CONTENT_TYPE_TEXT = "Text";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_BODY = "body";
    private static final String KEY_TO_RECIPIENTS = "toRecipients";
    private static final String KEY_CC_RECIPIENTS = "ccRecipients";
    private static final String KEY_BCC_RECIPIENTS = "bccRecipients";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_SAVE_TO_SENT_ITEMS = "saveToSentItems";
    private static final String EVENT_SEND_REQUEST = "send-request";
    private static final String EVENT_SEND_RESPONSE = "send-response";
    private static final String EVENT_SEND_ERROR = "send-error";
    private static final String KV_METHOD_POST = "method=POST";
    private static final String KV_URL = "url=";
    private static final String KV_TO_COUNT = "toCount=";
    private static final String KV_CC_COUNT = "ccCount=";
    private static final String KV_BCC_COUNT = "bccCount=";
    private static final String KV_SUBJECT = "subject=";
    private static final String KV_STATUS = "status=";
    private static final String KV_ERROR_BODY = "errorBody=";
    private static final String ERR_BODY_SEGMENT = " body=";
    private static final String KV_MESSAGE = "message=";
    private static final String MSG_GRAPH_SENDMAIL_FAILED_PREFIX = "Graph sendMail failed. HTTP ";
    private static final String GRAPH_SEND_HTTP_PREFIX = "graph_send_http_";
    private static final String MSG_GRAPH_MAIL_SEND_FAILED = "Graph mail send failed";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMAIL_ADDRESS = "emailAddress";
    private static final String ELLIPSIS = "...";
    private static final String FALLBACK_EVENT = "event";
    private static final String LOG_DIAG_WRITE_FAILURE_PREFIX = "Failed writing graph mail diagnostics log. Disable with ";
    private static final String KV_TIMESTAMP = "timestamp=";
    private static final String KV_COMPONENT = "component=";
    private static final String KV_REQUEST_ID = "requestId=";
    private static final String KV_EVENT = "event=";
    private static final String KV_DETAILS = "details=";
    private static final String KV_ERROR = "error=";
    private static final String ENTRY_SEPARATOR = "---";
    private static final String REQUEST_ID_NONE = "(none)";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_ONE = "1";
    private static final String BOOLEAN_YES = "yes";
    private static final String BOOLEAN_Y = "y";
    private static final String BOOLEAN_ON = "on";
    private static final String FILE_TOKEN_PATTERN = "[^A-Za-z0-9_-]";
    private static final String FILE_TOKEN_REPLACEMENT = "_";
    private static final String LOG_EXTENSION = ".log";
    private static final String DIAG_DISABLED_SUFFIX = "=false";

    private static volatile boolean warnedDiagFailure;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    final void sendMail(String accessToken, GraphEmailConfig config, EmailMessage message, MarkdownRenderer markdownRenderer) {
        HttpsURLConnection conn = null;
        String requestId = UUID.randomUUID().toString();
        try {
            String sender = config.senderUser().trim();
            String endpoint = GRAPH_ENDPOINT_PREFIX + sender + GRAPH_ENDPOINT_SEND_MAIL;

            URL url = URI.create(endpoint).toURL();
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_POST);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken);
            conn.setRequestProperty(HEADER_CONTENT_TYPE, APPLICATION_JSON);

            String html = firstNonBlank(message.htmlBody(), markdownRenderer.toHtml(message.markdownBody()));
            String text = message.textBody();
            String contentType = (html != null && !html.isBlank()) ? CONTENT_TYPE_HTML : CONTENT_TYPE_TEXT;
            String content = (html != null && !html.isBlank()) ? html : (text == null ? EMPTY : text);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put(KEY_CONTENT_TYPE, contentType);
            body.put(KEY_CONTENT, content);

            Map<String, Object> graphMessage = new LinkedHashMap<>();
            graphMessage.put(KEY_SUBJECT, nvl(message.subject()));
            graphMessage.put(KEY_BODY, body);
            graphMessage.put(KEY_TO_RECIPIENTS, recipients(message.to()));
            graphMessage.put(KEY_CC_RECIPIENTS, recipients(message.cc()));
            graphMessage.put(KEY_BCC_RECIPIENTS, recipients(message.bcc()));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put(KEY_MESSAGE, graphMessage);
            payload.put(KEY_SAVE_TO_SENT_ITEMS, Boolean.TRUE);

                    writeDiagnostics(COMPONENT_GRAPH_MAIL_CLIENT, requestId, EVENT_SEND_REQUEST,
                        KV_METHOD_POST + LS + KV_URL + endpoint
                            + LS + KV_TO_COUNT + size(message.to())
                            + LS + KV_CC_COUNT + size(message.cc())
                            + LS + KV_BCC_COUNT + size(message.bcc())
                            + LS + KV_SUBJECT + nvl(message.subject()), null);

            byte[] json = MAPPER.writeValueAsBytes(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }

            int status = conn.getResponseCode();
            String err = (conn.getErrorStream() == null)
                    ? EMPTY
                    : new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                    writeDiagnostics(COMPONENT_GRAPH_MAIL_CLIENT, requestId, EVENT_SEND_RESPONSE,
                        KV_STATUS + status + LS + KV_ERROR_BODY + truncate(err), null);

            // Graph sendMail commonly returns 202 Accepted
            if (status != 202 && (status < 200 || status >= 300)) {
                throw new EmailException(
                        MSG_GRAPH_SENDMAIL_FAILED_PREFIX + status + ERR_BODY_SEGMENT + err,
                        new RuntimeException(GRAPH_SEND_HTTP_PREFIX + status)
                );
            }
        } catch (EmailException e) {
                writeDiagnostics(COMPONENT_GRAPH_MAIL_CLIENT, requestId, EVENT_SEND_ERROR, KV_MESSAGE + safe(e.getMessage()), e);
            throw e;
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, MSG_GRAPH_MAIL_SEND_FAILED, e);
                writeDiagnostics(COMPONENT_GRAPH_MAIL_CLIENT, requestId, EVENT_SEND_ERROR, KV_MESSAGE + safe(e.getMessage()), e);
            throw new EmailException(MSG_GRAPH_MAIL_SEND_FAILED, e);
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
            emailAddress.put(KEY_ADDRESS, e.trim());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put(KEY_EMAIL_ADDRESS, emailAddress);

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
        return s == null ? EMPTY : s;
    }

    private int size(List<String> values) {
        return values == null ? 0 : values.size();
    }

    private String truncate(String body) {
        if (body == null) {
            return EMPTY;
        }
        if (body.length() <= 512) {
            return body;
        }
        return new String(body.toCharArray(), 0, 512) + ELLIPSIS;
    }

    private String safe(String value) {
        return value == null ? EMPTY : value;
    }

    private void writeDiagnostics(String component, String requestId, String event, String details, Throwable error) {
        if (!diagnosticsEnabled()) {
            return;
        }

        String comp = safeToken(component, COMPONENT_GRAPH_MAIL_CLIENT);
        String req = safeToken(requestId, EMPTY);
        String evt = safeToken(event, FALLBACK_EVENT);

        synchronized (DIAG_LOCK) {
            try {
                Path dir = resolveDiagnosticsDir();
                Files.createDirectories(dir);

                Path file = dir.resolve(diagnosticFileName(comp));
                String entry = formatDiagnosticsEntry(comp, req, evt, details, error);
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
                            LOG_DIAG_WRITE_FAILURE_PREFIX + ENV_ENABLED + DIAG_DISABLED_SUFFIX,
                            ex);
                }
            }
        }
    }

    private String formatDiagnosticsEntry(
            String component,
            String requestId,
            String event,
            String details,
            Throwable error) {
        String lineSep = System.lineSeparator();
        StringBuilder entry = new StringBuilder(512);
        entry.append(KV_TIMESTAMP).append(Instant.now()).append(lineSep)
                .append(KV_COMPONENT).append(component).append(lineSep)
                .append(KV_REQUEST_ID).append(requestId.isBlank() ? REQUEST_ID_NONE : requestId).append(lineSep)
                .append(KV_EVENT).append(event).append(lineSep);

        if (details != null && !details.isBlank()) {
            entry.append(KV_DETAILS).append(details).append(lineSep);
        }

        if (error != null) {
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            entry.append(KV_ERROR).append(sw).append(lineSep);
        }

        entry.append(ENTRY_SEPARATOR).append(lineSep);
        return entry.toString();
    }

    private boolean diagnosticsEnabled() {
        String enabledRaw = trimToNull(ENV.get(ENV_ENABLED));
        return isTruthy(enabledRaw);
    }

    private Path resolveDiagnosticsDir() {
        return Paths.get(DEFAULT_DIR_NAME).toAbsolutePath().normalize();
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? EMPTY : value.trim().toLowerCase(Locale.ROOT);
        return BOOLEAN_TRUE.equals(normalized)
            || BOOLEAN_ONE.equals(normalized)
            || BOOLEAN_YES.equals(normalized)
            || BOOLEAN_Y.equals(normalized)
            || BOOLEAN_ON.equals(normalized);
    }

    private String safeToken(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.replace(System.lineSeparator().charAt(0), ' ').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ').trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String diagnosticFileName(String component) {
        String token = component == null || component.isBlank() ? COMPONENT_GRAPH_MAIL_CLIENT : component.trim();
        String sanitized = token.replaceAll(FILE_TOKEN_PATTERN, FILE_TOKEN_REPLACEMENT);
        String fileToken = sanitized.isBlank() ? COMPONENT_GRAPH_MAIL_CLIENT : sanitized;
        return fileToken + '-' + LocalDate.now() + LOG_EXTENSION;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}

