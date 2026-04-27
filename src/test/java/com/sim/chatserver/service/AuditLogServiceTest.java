package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AuditLogService
 *
 * @see com.sim.chatserver.service.AuditLogService
 * @author bmcmullin
 */
public class AuditLogServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = null; // UTA: configured value
        underTest.logManualMessageRequest(event);

    }

    /**
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest2() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = null; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = null; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 500; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = null; // UTA: configured value
        when(event.userAgent()).thenReturn(userAgentResult);

        String usernameResult = null; // UTA: configured value
        when(event.username()).thenReturn(usernameResult);
        underTest.logManualMessageRequest(event);

    }

    /**
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest3() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = null; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = null; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 499; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = null; // UTA: configured value
        when(event.userAgent()).thenReturn(userAgentResult);

        String usernameResult = null; // UTA: configured value
        when(event.username()).thenReturn(usernameResult);
        underTest.logManualMessageRequest(event);

    }

    /**
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest4() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = null; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = null; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 500; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = null; // UTA: configured value
        when(event.userAgent()).thenReturn(userAgentResult);

        String usernameResult = null; // UTA: configured value
        when(event.username()).thenReturn(usernameResult);
        underTest.logManualMessageRequest(event);

    }

    /**
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest5() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = null; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = null; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 501; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = null; // UTA: configured value
        when(event.userAgent()).thenReturn(userAgentResult);

        String usernameResult = null; // UTA: configured value
        when(event.username()).thenReturn(usernameResult);
        underTest.logManualMessageRequest(event);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

}
