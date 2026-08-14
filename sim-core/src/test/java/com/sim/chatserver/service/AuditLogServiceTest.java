package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import static org.junit.jupiter.api.Assertions.assertFalse;
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


    // Merged from AuditLogServiceBranchTest
    @Test
        void toLevel_returnsInfoForStatusBelow400() throws Exception {
            AuditLogService service = new AuditLogService(AuditLogServiceTest.class);
            Method toLevel = AuditLogService.class.getDeclaredMethod("toLevel", int.class);
            toLevel.setAccessible(true);
    
            Level level = (Level) toLevel.invoke(service, 200);
    
            assertEquals(Level.INFO, level);
        }
    
        @Test
        void truncate_returnsEmptyForNullOrNonPositiveMax() throws Exception {
            AuditLogService service = new AuditLogService(AuditLogServiceTest.class);
            Method truncate = AuditLogService.class.getDeclaredMethod("truncate", String.class, int.class);
            truncate.setAccessible(true);
    
            assertEquals("", truncate.invoke(service, null, 10));
            assertEquals("", truncate.invoke(service, "value", 0));
        }
    
        @Test
        void readObject_throwsNotSerializableException() throws Exception {
            AuditLogService service = new AuditLogService(AuditLogServiceTest.class);
            Method readObject = AuditLogService.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
            readObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> readObject.invoke(service, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(AuditLogService.class.getName(), cause.getMessage());
        }
    
        @Test
        void writeObject_throwsNotSerializableException() throws Exception {
            AuditLogService service = new AuditLogService(AuditLogServiceTest.class);
            Method writeObject = AuditLogService.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
            writeObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> writeObject.invoke(service, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(AuditLogService.class.getName(), cause.getMessage());
        }


    /**
     * Consolidated from AuditLogService_ManualMessageAuditEventTest.java to keep one test class per production source file.
     */
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

    /**
     * Parasoft Jtest UTA: Test for clientIp()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#clientIp()
     * @author bmcmullin
     */
    @Test
    public void testClientIp() throws Throwable
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
        String result = underTest.clientIp();

        // Then - assertions for result of method clientIp()
        assertEquals("clientIp", result);

    }

    /**
     * Parasoft Jtest UTA: Test for contextChars()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#contextChars()
     * @author bmcmullin
     */
    @Test
    public void testContextChars() throws Throwable
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
        int result = underTest.contextChars();

        // Then - assertions for result of method contextChars()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals() throws Throwable
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
        Object arg0 = new Object(); // UTA: default value
        boolean result = underTest.equals(arg0);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for hashCode()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#hashCode()
     * @author bmcmullin
     */
    @Test
    public void testHashCode() throws Throwable
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
        int result = underTest.hashCode();

        // Then - assertions for result of method hashCode()
        // assertEquals(1, result);// UTA: Expected value may be unstable

    }

    /**
     * Parasoft Jtest UTA: Test for latencyMs()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#latencyMs()
     * @author bmcmullin
     */
    @Test
    public void testLatencyMs() throws Throwable
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
        long result = underTest.latencyMs();

        // Then - assertions for result of method latencyMs()
        assertEquals(1L, result);

    }

    /**
     * Parasoft Jtest UTA: Test for messageChars()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#messageChars()
     * @author bmcmullin
     */
    @Test
    public void testMessageChars() throws Throwable
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
        int result = underTest.messageChars();

        // Then - assertions for result of method messageChars()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for mode()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#mode()
     * @author bmcmullin
     */
    @Test
    public void testMode() throws Throwable
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
        String result = underTest.mode();

        // Then - assertions for result of method mode()
        assertEquals("mode", result);

    }

    /**
     * Parasoft Jtest UTA: Test for origin()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#origin()
     * @author bmcmullin
     */
    @Test
    public void testOrigin() throws Throwable
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
        String result = underTest.origin();

        // Then - assertions for result of method origin()
        assertEquals("origin", result);

    }

    /**
     * Parasoft Jtest UTA: Test for referer()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#referer()
     * @author bmcmullin
     */
    @Test
    public void testReferer() throws Throwable
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
        String result = underTest.referer();

        // Then - assertions for result of method referer()
        assertEquals("referer", result);

    }

    /**
     * Parasoft Jtest UTA: Test for requestId()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#requestId()
     * @author bmcmullin
     */
    @Test
    public void testRequestId() throws Throwable
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
        String result = underTest.requestId();

        // Then - assertions for result of method requestId()
        assertEquals("requestId", result);

    }

    /**
     * Parasoft Jtest UTA: Test for requestReset()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#requestReset()
     * @author bmcmullin
     */
    @Test
    public void testRequestReset() throws Throwable
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
        boolean result = underTest.requestReset();

        // Then - assertions for result of method requestReset()
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for retried()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#retried()
     * @author bmcmullin
     */
    @Test
    public void testRetried() throws Throwable
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
        boolean result = underTest.retried();

        // Then - assertions for result of method retried()
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for sampledCount()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#sampledCount()
     * @author bmcmullin
     */
    @Test
    public void testSampledCount() throws Throwable
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
        int result = underTest.sampledCount();

        // Then - assertions for result of method sampledCount()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for selectedCount()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#selectedCount()
     * @author bmcmullin
     */
    @Test
    public void testSelectedCount() throws Throwable
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
        int result = underTest.selectedCount();

        // Then - assertions for result of method selectedCount()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for statusCode()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#statusCode()
     * @author bmcmullin
     */
    @Test
    public void testStatusCode() throws Throwable
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
        int result = underTest.statusCode();

        // Then - assertions for result of method statusCode()
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
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
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("ManualMessageAuditEvent[requestId=requestId, username=username, clientIp=clientIp, mode=mode, requestReset=false, retried=false, selectedCount=1, sampledCount=1, messageChars=1, contextChars=1, attachmentCount=1, statusCode=1, latencyMs=1, userAgent=userAgent, origin=origin, referer=referer]", result);

    }

    /**
     * Parasoft Jtest UTA: Test for userAgent()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#userAgent()
     * @author bmcmullin
     */
    @Test
    public void testUserAgent() throws Throwable
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
        String result = underTest.userAgent();

        // Then - assertions for result of method userAgent()
        assertEquals("userAgent", result);

    }

    /**
     * Parasoft Jtest UTA: Test for username()
     *
     * @see com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent#username()
     * @author bmcmullin
     */
    @Test
    public void testUsername() throws Throwable
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
        String result = underTest.username();

        // Then - assertions for result of method username()
        assertEquals("username", result);

    }
}



