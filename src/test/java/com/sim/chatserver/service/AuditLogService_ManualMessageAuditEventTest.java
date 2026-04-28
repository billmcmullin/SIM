package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for ManualMessageAuditEvent
 *
 * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent
 * @author bmcmullin
 */
public class AuditLogService_ManualMessageAuditEventTest
{

    /**
     * Parasoft Jtest UTA: Test for attachmentCount()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#attachmentCount()
     * @author bmcmullin
     */
    @Test
    public void testAttachmentCount() throws Throwable
    {
        // Given
        String requestId = "requestId"; // UTA: default value
        String username = "username"; // UTA: default value
        String clientIp = "clientIp"; // UTA: default value
        String mode = "mode"; // UTA: default value
        boolean requestReset = false; // UTA: default value
        boolean retried = false; // UTA: default value
        int selectedCount = 1; // UTA: default value
        int sampledCount = 1; // UTA: default value
        int messageChars = 1; // UTA: default value
        int contextChars = 1; // UTA: default value
        int attachmentCount = 1; // UTA: default value
        int statusCode = 1; // UTA: default value
        long latencyMs = 1L; // UTA: default value
        String userAgent = "userAgent"; // UTA: default value
        String origin = "origin"; // UTA: default value
        String referer = "referer"; // UTA: default value
        ManualMessageAuditEvent underTest = new ManualMessageAuditEvent(requestId, username, clientIp, mode, requestReset, retried, selectedCount, sampledCount, messageChars, contextChars, attachmentCount, statusCode, latencyMs, userAgent, origin, referer);

        // When
        int result = underTest.attachmentCount();

        // Then - assertions for result of method attachmentCount()
        assertEquals(1, result);

    }

}
