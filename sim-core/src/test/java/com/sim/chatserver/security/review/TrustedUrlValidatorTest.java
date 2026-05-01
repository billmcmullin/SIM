package com.sim.chatserver.security.review;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult;
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

    }

    /**
     * Parasoft Jtest UTA: Test for isTrusted(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#isTrusted(String)
     * @author bmcmullin
     */
    @Test
    public void testIsTrusted3() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = true; // UTA: configured value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = "rawUrl"; // UTA: configured value
        boolean result = underTest.isTrusted(rawUrl);

    }

    /**
     * Parasoft Jtest UTA: Test for validate(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#validate(String)
     * @author bmcmullin
     */
    @Test
    public void testValidate() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = false; // UTA: default value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = null; // UTA: configured value
        ValidationResult result = underTest.validate(rawUrl);

    }

    /**
     * Parasoft Jtest UTA: Test for validate(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#validate(String)
     * @author bmcmullin
     */
    @Test
    public void testValidate2() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = false; // UTA: default value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = "rawUrl"; // UTA: configured value
        ValidationResult result = underTest.validate(rawUrl);

    }

    /**
     * Parasoft Jtest UTA: Test for validate(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator#validate(String)
     * @author bmcmullin
     */
    @Test
    public void testValidate3() throws Throwable
    {
        // Given
        Set<String> allowedHosts = null; // UTA: configured value
        Set<String> allowedSuffixes = null; // UTA: configured value
        boolean allowPrivateNetworks = true; // UTA: configured value
        TrustedUrlValidator underTest = new TrustedUrlValidator(allowedHosts, allowedSuffixes, allowPrivateNetworks);

        // When
        String rawUrl = "rawUrl"; // UTA: configured value
        ValidationResult result = underTest.validate(rawUrl);

    }
}
