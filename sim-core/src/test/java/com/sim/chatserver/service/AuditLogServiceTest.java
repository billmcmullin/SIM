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

        String modeResult = "modeResult"; // UTA: configured value
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
    public void testLogManualMessageRequest4() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = "clientIpResult"; // UTA: configured value
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

        String refererResult = null; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 500; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = null; // UTA: configured value
        when(event.userAgent()).thenReturn(userAgentResult);

        String usernameResult = "usernameResult"; // UTA: configured value
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
    public void testLogManualMessageRequest6() throws Throwable
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

        String requestIdResult = "requestIdResult"; // UTA: configured value
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
    public void testLogManualMessageRequest7() throws Throwable
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
    public void testLogManualMessageRequest8() throws Throwable
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
    public void testLogManualMessageRequest9() throws Throwable
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

        String originResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest10() throws Throwable
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

        String userAgentResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest11() throws Throwable
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest12() throws Throwable
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

        String originResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest13() throws Throwable
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

        int statusCodeResult = 501; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest14() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = "modeResult"; // UTA: configured value
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
    public void testLogManualMessageRequest15() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = "modeResult"; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest16() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = "modeResult"; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = null; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 500; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest17() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = "modeResult"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest18() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        ManualMessageAuditEvent event = mock(ManualMessageAuditEvent.class);
        String clientIpResult = null; // UTA: configured value
        when(event.clientIp()).thenReturn(clientIpResult);

        String modeResult = "modeResult"; // UTA: configured value
        when(event.mode()).thenReturn(modeResult);

        String originResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest19() throws Throwable
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

        String originResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest20() throws Throwable
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

        String originResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = null; // UTA: configured value
        when(event.referer()).thenReturn(refererResult);

        String requestIdResult = null; // UTA: configured value
        when(event.requestId()).thenReturn(requestIdResult);

        int statusCodeResult = 500; // UTA: configured value
        when(event.statusCode()).thenReturn(statusCodeResult);

        String userAgentResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest21() throws Throwable
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

        String userAgentResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
    public void testLogManualMessageRequest22() throws Throwable
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

        String originResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest23() throws Throwable
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

        String originResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
        when(event.origin()).thenReturn(originResult);

        String refererResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest24() throws Throwable
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

        String originResult = "*************************************************************************************************************************************************************************************"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logManualMessageRequest(ManualMessageAuditEvent)
     *
     * @see com.sim.chatserver.service.AuditLogService#logManualMessageRequest(ManualMessageAuditEvent)
     * @author bmcmullin
     */
    @Test
    public void testLogManualMessageRequest25() throws Throwable
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

        String userAgentResult = "************************************************************************************************************************************************************************************"; // UTA: configured value
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
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure2() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure3() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure4() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure5() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure6() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure7() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure8() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 499; // UTA: configured value
        String summary = null; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure9() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure10() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure11() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure12() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure13() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure14() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 500; // UTA: configured value
        String summary = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure15() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure16() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure17() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure18() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure19() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure20() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        int statusCode = 501; // UTA: configured value
        String summary = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure21() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: default value
        String username = "username"; // UTA: default value
        String clientIp = "clientIp"; // UTA: default value
        int statusCode = 499; // UTA: configured value
        String summary = "summary"; // UTA: default value
        underTest.logUpstreamFailure(requestId, username, clientIp, statusCode, summary);

    }

    /**
     * Parasoft Jtest UTA: Test for logUpstreamFailure(String, String, String, int, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logUpstreamFailure(String, String, String, int, String)
     * @author bmcmullin
     */
    @Test
    public void testLogUpstreamFailure22() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = "username"; // UTA: default value
        String clientIp = "clientIp"; // UTA: default value
        int statusCode = 500; // UTA: configured value
        String summary = "summary"; // UTA: default value
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

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure2() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure3() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure4() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure5() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure6() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure7() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure8() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure9() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure10() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure11() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure12() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = "*************************************************************************************************************************************************************************************************************************************************************************************************************"; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure13() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure14() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure15() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure16() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = null; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure17() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure18() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = null; // UTA: configured value
        String clientIp = "clientIp"; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure19() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = "username"; // UTA: configured value
        String clientIp = null; // UTA: configured value
        String reason = null; // UTA: configured value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }

    /**
     * Parasoft Jtest UTA: Test for logValidationFailure(String, String, String, String)
     *
     * @see com.sim.chatserver.service.AuditLogService#logValidationFailure(String, String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testLogValidationFailure20() throws Throwable
    {
        // Given
        Class owner = Class.forName("java.lang.Object"); // UTA: default value
        AuditLogService underTest = new AuditLogService(owner);

        // When
        String requestId = "requestId"; // UTA: configured value
        String username = "username"; // UTA: default value
        String clientIp = "clientIp"; // UTA: default value
        String reason = "reason"; // UTA: default value
        underTest.logValidationFailure(requestId, username, clientIp, reason);

    }
}
