package com.sim.chatserver.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;

/**
 * Centralized, safe audit logging for manual-message workflow.
 *
 * Avoids logging secrets or full message/attachment contents.
 */
public class AuditLogService {

    private static final String EMPTY = "";
    private static final String VALUE_UNKNOWN = CommonLiteralConstants.MARKER_UNKNOWN;
    private static final String LOG_MANUAL_MESSAGE_REQUEST =
        "manual-message requestId={0} user={1} ip={2} mode={3} reset={4} retried={5} selected={6} sampled={7} msgChars={8} ctxChars={9} attach={10} status={11} latencyMs={12} ua=\"{13}\" origin=\"{14}\" referer=\"{15}\"";
    private static final String LOG_MANUAL_MESSAGE_VALIDATION_FAILURE =
        "manual-message validation-failure requestId={0} user={1} ip={2} reason=\"{3}\"";
    private static final String LOG_MANUAL_MESSAGE_UPSTREAM_FAILURE =
        "manual-message upstream-failure requestId={0} user={1} ip={2} status={3} summary=\"{4}\"";
    private static final String ELLIPSIS = CommonLiteralConstants.ELLIPSIS;


    private static final int MAX_FIELD_CHARS = 180;
    private static final int MAX_REASON_CHARS = 300;

    private final Logger ownerLogger;

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    AuditLogService(Class<?> owner) {
        String ownerName = owner == null
                ? AuditLogService.class.getCanonicalName()
                : (owner.getCanonicalName() != null ? owner.getCanonicalName() : owner.getTypeName());
        this.ownerLogger = Logger.getLogger(ownerName);
    }

    final void logManualMessageRequest(ManualMessageAuditEvent event) {
        if (event == null) {
            return;
        }

        // Severity by status class
        Level level = toLevel(event.statusCode());

        ownerLogger.log(
            level,
            LOG_MANUAL_MESSAGE_REQUEST,
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
            LOG_MANUAL_MESSAGE_VALIDATION_FAILURE,
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
            LOG_MANUAL_MESSAGE_UPSTREAM_FAILURE,
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
        return (value == null || value.isBlank()) ? VALUE_UNKNOWN : value;
    }

    /**
     * Removes CRLF and non-printable control chars to prevent log injection.
     */
    private String scrub(String value) {
        if (value == null) {
            return EMPTY;
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == 0x0D) {
                out.append(' ');
                if (i + 1 < value.length() && value.charAt(i + 1) == 0x0A) {
                    i++;
                }
                continue;
            }
            if (ch == 0x0A || ch == 0x09 || Character.isISOControl(ch)) {
                out.append(' ');
                continue;
            }
            out.append(ch);
        }
        return out.toString().trim();
    }

    private String truncate(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return EMPTY;
        }
        if (value.length() <= maxChars) {
            return value;
        }
        if (maxChars <= ELLIPSIS.length()) {
            return new String(ELLIPSIS.toCharArray(), 0, Math.max(0, maxChars));
        }
        return new String(value.toCharArray(), 0, maxChars - ELLIPSIS.length()) + ELLIPSIS;
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