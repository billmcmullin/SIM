package com.sim.chatserver.security;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.SecurityValidationService.UrlValidationResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for UrlValidationResult
 *
 * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult
 * @author bmcmullin
 */
public class SecurityValidationService_UrlValidationResultTest
{

    /**
     * Parasoft Jtest UTA: Test for allowed(String, String, String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#allowed(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testAllowed() throws Throwable
    {
        // When
        String host = "host"; // UTA: default value
        String scheme = "scheme"; // UTA: default value
        String reason = "reason"; // UTA: default value
        UrlValidationResult result = UrlValidationResult.allowed(host, scheme, reason);

        // Then - assertions for result of method allowed(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isAllowed());
        }, () -> {
            assertEquals("reason", result.getReason());
        }, () -> {
            assertEquals("host", result.getHost());
        }, () -> {
            assertEquals("scheme", result.getScheme());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for blocked(String)
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#blocked(String)
     * @author bmcmullin
     */
    @Test
    public void testBlocked() throws Throwable
    {
        // When
        String reason = "reason"; // UTA: default value
        UrlValidationResult result = UrlValidationResult.blocked(reason);

        // Then - assertions for result of method blocked(String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isAllowed());
        }, () -> {
            assertEquals("reason", result.getReason());
        }, () -> {
            assertNull(result.getHost());
        }, () -> {
            assertNull(result.getScheme());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getHost()
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#getHost()
     * @author bmcmullin
     */
    @Test
    public void testGetHost() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        UrlValidationResult underTest = UrlValidationResult.blocked(reason);

        // When
        String result = underTest.getHost();

        // Then - assertions for result of method getHost()
        assertNull(result);

        // Then - assertions for this instance of SecurityValidationService.UrlValidationResult
        assertAll(() -> {
            assertFalse(underTest.isAllowed());
        }, () -> {
            assertEquals("reason", underTest.getReason());
        }, () -> {
            assertNull(underTest.getScheme());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getReason()
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#getReason()
     * @author bmcmullin
     */
    @Test
    public void testGetReason() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        UrlValidationResult underTest = UrlValidationResult.blocked(reason);

        // When
        String result = underTest.getReason();

        // Then - assertions for result of method getReason()
        assertEquals("reason", result);

        // Then - assertions for this instance of SecurityValidationService.UrlValidationResult
        assertAll(() -> {
            assertFalse(underTest.isAllowed());
        }, () -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getScheme());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getScheme()
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#getScheme()
     * @author bmcmullin
     */
    @Test
    public void testGetScheme() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        UrlValidationResult underTest = UrlValidationResult.blocked(reason);

        // When
        String result = underTest.getScheme();

        // Then - assertions for result of method getScheme()
        assertNull(result);

        // Then - assertions for this instance of SecurityValidationService.UrlValidationResult
        assertAll(() -> {
            assertFalse(underTest.isAllowed());
        }, () -> {
            assertEquals("reason", underTest.getReason());
        }, () -> {
            assertNull(underTest.getHost());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isAllowed()
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#isAllowed()
     * @author bmcmullin
     */
    @Test
    public void testIsAllowed() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        UrlValidationResult underTest = UrlValidationResult.blocked(reason);

        // When
        boolean result = underTest.isAllowed();

        // Then - assertions for result of method isAllowed()
        assertFalse(result);

        // Then - assertions for this instance of SecurityValidationService.UrlValidationResult
        assertAll(() -> {
            assertEquals("reason", underTest.getReason());
        }, () -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getScheme());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.security.SecurityValidationService.UrlValidationResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        UrlValidationResult underTest = UrlValidationResult.blocked(reason);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("UrlValidationResult{allowed=false, reason='reason', host='null', scheme='null'}", result);

        // Then - assertions for this instance of SecurityValidationService.UrlValidationResult
        assertAll(() -> {
            assertFalse(underTest.isAllowed());
        }, () -> {
            assertEquals("reason", underTest.getReason());
        }, () -> {
            assertNull(underTest.getHost());
        }, () -> {
            assertNull(underTest.getScheme());
        });

    }
}
