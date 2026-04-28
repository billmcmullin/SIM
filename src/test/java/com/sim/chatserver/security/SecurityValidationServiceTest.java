package com.sim.chatserver.security;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.SecurityValidationService.UrlValidationResult;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SecurityValidationService
 *
 * @see com.sim.chatserver.security.SecurityValidationService
 * @author bmcmullin
 */
public class SecurityValidationServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for extractClientIp(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#extractClientIp(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testExtractClientIp() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = null; // UTA: configured value
        String result = underTest.extractClientIp(req);

        // Then - assertions for result of method extractClientIp(HttpServletRequest)
        assertEquals("(unknown)", result);

    }

    /**
     * Parasoft Jtest UTA: Test for extractClientIp(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#extractClientIp(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testExtractClientIp2() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = "getHeaderResult2"; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2);
        String result = underTest.extractClientIp(req);

        // Then - assertions for result of method extractClientIp(HttpServletRequest)
        assertEquals("getHeaderResult2", result);

    }

    /**
     * Parasoft Jtest UTA: Test for extractClientIp(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#extractClientIp(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testExtractClientIp3() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = "getHeaderResult"; // UTA: configured value
        String getHeaderResult2 = "getHeaderResult2"; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2);
        String result = underTest.extractClientIp(req);

        // Then - assertions for result of method extractClientIp(HttpServletRequest)
        assertEquals("getHeaderResult", result);

    }

    /**
     * Parasoft Jtest UTA: Test for extractClientIp(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#extractClientIp(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testExtractClientIp4() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2);

        String getRemoteAddrResult = null; // UTA: configured value
        when(req.getRemoteAddr()).thenReturn(getRemoteAddrResult);
        String result = underTest.extractClientIp(req);

        // Then - assertions for result of method extractClientIp(HttpServletRequest)
        assertEquals("(unknown)", result);

    }

    /**
     * Parasoft Jtest UTA: Test for isAllowedUpstreamUrl(String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isAllowedUpstreamUrl(String)
     * @author bmcmullin
     */
    @Test
    public void testIsAllowedUpstreamUrl() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        String baseUrl = null; // UTA: configured value
        boolean result = underTest.isAllowedUpstreamUrl(baseUrl);

        // Then - assertions for result of method isAllowedUpstreamUrl(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isAllowedUpstreamUrl(String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isAllowedUpstreamUrl(String)
     * @author bmcmullin
     */
    @Test
    public void testIsAllowedUpstreamUrl2() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        String baseUrl = "baseUrl"; // UTA: configured value
        boolean result = underTest.isAllowedUpstreamUrl(baseUrl);

        // Then - assertions for result of method isAllowedUpstreamUrl(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isJsonRequest(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isJsonRequest(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testIsJsonRequest() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = null; // UTA: configured value
        boolean result = underTest.isJsonRequest(req);

        // Then - assertions for result of method isJsonRequest(HttpServletRequest)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isJsonRequest(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isJsonRequest(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testIsJsonRequest2() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContentTypeResult = null; // UTA: configured value
        when(req.getContentType()).thenReturn(getContentTypeResult);
        boolean result = underTest.isJsonRequest(req);

        // Then - assertions for result of method isJsonRequest(HttpServletRequest)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isJsonRequest(HttpServletRequest)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isJsonRequest(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testIsJsonRequest3() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContentTypeResult = "getContentTypeResult"; // UTA: default value
        when(req.getContentType()).thenReturn(getContentTypeResult);
        boolean result = underTest.isJsonRequest(req);

        // Then - assertions for result of method isJsonRequest(HttpServletRequest)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isModeAllowed(String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isModeAllowed(String)
     * @author bmcmullin
     */
    @Test
    public void testIsModeAllowed() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        String mode = null; // UTA: configured value
        boolean result = underTest.isModeAllowed(mode);

        // Then - assertions for result of method isModeAllowed(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isModeAllowed(String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#isModeAllowed(String)
     * @author bmcmullin
     */
    @Test
    public void testIsModeAllowed2() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        String mode = "mode"; // UTA: configured value
        boolean result = underTest.isModeAllowed(mode);

        // Then - assertions for result of method isModeAllowed(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for normalizeModeOrDefault(String, String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService#normalizeModeOrDefault(String, String)
     * @author bmcmullin
     */
    @Test
    public void testNormalizeModeOrDefault() throws Throwable
    {
        // Given
        Set<String> allowedUpstreamHosts = null; // UTA: configured value
        Set<String> allowedModes = new HashSet<String>(); // UTA: default value
        SecurityValidationService underTest = new SecurityValidationService(allowedUpstreamHosts, allowedModes);

        // When
        String mode = null; // UTA: configured value
        String defaultMode = null; // UTA: configured value
        String result = underTest.normalizeModeOrDefault(mode, defaultMode);

        // Then - assertions for result of method normalizeModeOrDefault(String, String)
        assertEquals("chat", result);

    }

}
