package com.sim.chatserver.service;

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

    private final Logger ownerLogger;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    AuditLogService(Class<?> owner) {
        this.ownerLogger = Logger.getLogger(owner == null ? AuditLogService.class.getName() : owner.getName());
    }

    final void logManualMessageRequest(ManualMessageAuditEvent event) {
        if (event == null) {
            return;
        }

        // Severity by status class
        Level level = toLevel(event.statusCode());

        ownerLogger.log(
            level,
            "manual-message requestId={0} user={1} ip={2} mode={3} reset={4} retried={5} selected={6} sampled={7} msgChars={8} ctxChars={9} attach={10} status={11} latencyMs={12} ua=\"{13}\" origin=\"{14}\" referer=\"{15}\"",
            new Object[]{
                safe(event.requestId()),
                safe(event.username()),
                safe(event.clientIp()),
                safe(event.mode()),
                Boolean.toString(event.requestReset()),
                Boolean.toString(event.retried()),
                Integer.toString(nonNegative(event.selectedCount())),
                Integer.toString(nonNegative(event.sampledCount())),
                Integer.toString(nonNegative(event.messageChars())),
                Integer.toString(nonNegative(event.contextChars())),
                Integer.toString(nonNegative(event.attachmentCount())),
                Integer.toString(event.statusCode()),
                Long.toString(Math.max(0L, event.latencyMs())),
                truncate(scrub(event.userAgent()), MAX_FIELD_CHARS),
                truncate(scrub(event.origin()), MAX_FIELD_CHARS),
                truncate(scrub(event.referer()), MAX_FIELD_CHARS)
            }
        );
    }

    final void logValidationFailure(String requestId, String username, String clientIp, String reason) {
        ownerLogger.log(
            Level.WARNING,
            "manual-message validation-failure requestId={0} user={1} ip={2} reason=\"{3}\"",
            new Object[]{
                safe(requestId),
                safe(username),
                safe(clientIp),
                truncate(scrub(reason), MAX_REASON_CHARS)
            }
        );
    }

    final void logUpstreamFailure(String requestId, String username, String clientIp, int statusCode, String summary) {
        Level level = toLevel(statusCode);
        ownerLogger.log(
            level,
            "manual-message upstream-failure requestId={0} user={1} ip={2} status={3} summary=\"{4}\"",
            new Object[]{
                safe(requestId),
                safe(username),
                safe(clientIp),
                Integer.toString(statusCode),
                truncate(scrub(summary), MAX_REASON_CHARS)
            }
        );
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
                : value.substring(0, Math.max(0, maxChars - 1)) + "Ã¢â‚¬Â¦";
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
