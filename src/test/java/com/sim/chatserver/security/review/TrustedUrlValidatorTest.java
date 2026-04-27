package com.sim.chatserver.security.review;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * Parasoft Jtest UTA: Test class for TrustedUrlValidator
 *
 * @see com.sim.chatserver.security.review.TrustedUrlValidator
 * @author bmcmullin
 */
public class TrustedUrlValidatorTest
{

    /**
     * Parasoft Jtest UTA: Test for isTrusted(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#isTrusted(String)
     * @author bmcmullin
     */
    @Test
    public void testIsTrusted() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = false; // UTA: default value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = null; // UTA: configured value
        boolean result = underTest.isTrusted(rawUrl);

        // Then - assertions for result of method isTrusted(String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isTrusted(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#isTrusted(String)
     * @author bmcmullin
     */
    @Test
    public void testIsTrusted2() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = false; // UTA: default value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = "rawUrl"; // UTA: configured value
        boolean result = underTest.isTrusted(rawUrl);

        // Then - assertions for result of method isTrusted(String)
        assertFalse(result);

    }

}
