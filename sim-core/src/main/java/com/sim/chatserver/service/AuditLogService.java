package com.sim.chatserver.service;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized, safe audit logging for manual-message workflow.
 *
 * Avoids logging secrets or full message/attachment contents.
 */
public class AuditLogService {

    private static final int MAX_FIELD_CHARS = 180;
    private static final int MAX_REASON_CHARS = 300;

    private final Logger log;

    public AuditLogService(Class<?> owner) {
        this.log = Logger.getLogger(owner == null ? AuditLogService.class.getName() : owner.getName());
    }

    public void logManualMessageRequest(ManualMessageAuditEvent event) {
        if (event == null) {
            return;
        }

        // Severity by status class
        Level level = toLevel(event.statusCode());

        String line = String.format(
                Locale.ROOT,
                "manual-message requestId=%s user=%s ip=%s mode=%s reset=%s retried=%s "
                + "selected=%d sampled=%d msgChars=%d ctxChars=%d attach=%d status=%d latencyMs=%d "
                + "ua=\"%s\" origin=\"%s\" referer=\"%s\"",
                safe(event.requestId()),
                safe(event.username()),
                safe(event.clientIp()),
                safe(event.mode()),
                event.requestReset(),
                event.retried(),
                nonNegative(event.selectedCount()),
                nonNegative(event.sampledCount()),
                nonNegative(event.messageChars()),
                nonNegative(event.contextChars()),
                nonNegative(event.attachmentCount()),
                event.statusCode(),
                Math.max(0L, event.latencyMs()),
                truncate(scrub(event.userAgent()), MAX_FIELD_CHARS),
                truncate(scrub(event.origin()), MAX_FIELD_CHARS),
                truncate(scrub(event.referer()), MAX_FIELD_CHARS)
        );

        log.log(level, line);
    }

    public void logValidationFailure(String requestId, String username, String clientIp, String reason) {
        log.warning(String.format(
                Locale.ROOT,
                "manual-message validation-failure requestId=%s user=%s ip=%s reason=\"%s\"",
                safe(requestId),
                safe(username),
                safe(clientIp),
                truncate(scrub(reason), MAX_REASON_CHARS)
        ));
    }

    public void logUpstreamFailure(String requestId, String username, String clientIp, int statusCode, String summary) {
        Level level = toLevel(statusCode);
        log.log(level, String.format(
                Locale.ROOT,
                "manual-message upstream-failure requestId=%s user=%s ip=%s status=%d summary=\"%s\"",
                safe(requestId),
                safe(username),
                safe(clientIp),
                statusCode,
                truncate(scrub(summary), MAX_REASON_CHARS)
        ));
    }

    private Level toLevel(int statusCode) {
        if (statusCode >= 500) {
            return Level.WARNING;
        }
        if (statusCode >= 400) {
            return Level.INFO;
        }
        return Level.INFO;
    }

    private int nonNegative(int value) {
        return Math.max(0, value);
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "(unknown)" : value;
    }

    /**
     * Removes CRLF and non-printable control chars to prevent log injection.
     */
    private String scrub(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .trim();
    }

    private String truncate(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars
                ? value
                : value.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    public record ManualMessageAuditEvent(
            String requestId,
            String username,
            String clientIp,
            String mode,
            boolean requestReset,
            boolean retried,
            int selectedCount,
            int sampledCount,
            int messageChars,
            int contextChars,
            int attachmentCount,
            int statusCode,
            long latencyMs,
            String userAgent,
            String origin,
            String referer
            ) {

    }
}
