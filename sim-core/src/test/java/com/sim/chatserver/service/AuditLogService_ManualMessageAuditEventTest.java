package com.sim.chatserver.service;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.AuditLogService.ManualMessageAuditEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
