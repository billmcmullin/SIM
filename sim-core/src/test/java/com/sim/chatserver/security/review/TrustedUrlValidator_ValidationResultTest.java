package com.sim.chatserver.security.review;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult;
/**
 * Parasoft Jtest UTA: Test class for ValidationResult
 *
 * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult
 * @author bmcmullin
 */
public class TrustedUrlValidator_ValidationResultTest
{

    /**
     * Parasoft Jtest UTA: Test for getHost()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#getHost()
     * @author bmcmullin
     */
    @Test
    public void testGetHost() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        ValidationResult underTest = ValidationResult.invalid(reason);

        // When
        String result = underTest.getHost();

    }

    /**
     * Parasoft Jtest UTA: Test for getPort()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#getPort()
     * @author bmcmullin
     */
    @Test
    public void testGetPort() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        ValidationResult underTest = ValidationResult.invalid(reason);

        // When
        int result = underTest.getPort();

    }

    /**
     * Parasoft Jtest UTA: Test for getReason()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#getReason()
     * @author bmcmullin
     */
    @Test
    public void testGetReason() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        ValidationResult underTest = ValidationResult.invalid(reason);

        // When
        String result = underTest.getReason();

    }

    /**
     * Parasoft Jtest UTA: Test for getScheme()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#getScheme()
     * @author bmcmullin
     */
    @Test
    public void testGetScheme() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        ValidationResult underTest = ValidationResult.invalid(reason);

        // When
        String result = underTest.getScheme();

    }

    /**
     * Parasoft Jtest UTA: Test for invalid(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#invalid(String)
     * @author bmcmullin
     */
    @Test
    public void testInvalid() throws Throwable
    {
        // When
        String reason = "reason"; // UTA: default value
        ValidationResult result = ValidationResult.invalid(reason);

    }

    /**
     * Parasoft Jtest UTA: Test for invalid(String)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#invalid(String)
     * @author bmcmullin
     */
    @Test
    public void testInvalid2() throws Throwable
    {
        // When
        String reason = null; // UTA: configured value
        ValidationResult result = ValidationResult.invalid(reason);

    }

    /**
     * Parasoft Jtest UTA: Test for isValid()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#isValid()
     * @author bmcmullin
     */
    @Test
    public void testIsValid() throws Throwable
    {
        // Given
        String reason = "reason"; // UTA: default value
        ValidationResult underTest = ValidationResult.invalid(reason);

        // When
        boolean result = underTest.isValid();

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        String host = "host"; // UTA: default value
        String scheme = "scheme"; // UTA: default value
        int port = 1; // UTA: default value
        ValidationResult underTest = ValidationResult.valid(host, scheme, port);

        // When
        String result = underTest.toString();

    }

    /**
     * Parasoft Jtest UTA: Test for valid(String, String, int)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#valid(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testValid() throws Throwable
    {
        // When
        String host = "host"; // UTA: default value
        String scheme = "scheme"; // UTA: default value
        int port = 1; // UTA: default value
        ValidationResult result = ValidationResult.valid(host, scheme, port);

    }

    /**
     * Parasoft Jtest UTA: Test for valid(String, String, int)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#valid(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testValid2() throws Throwable
    {
        // When
        String host = "host"; // UTA: default value
        String scheme = null; // UTA: configured value
        int port = 1; // UTA: default value
        ValidationResult result = ValidationResult.valid(host, scheme, port);

    }

    /**
     * Parasoft Jtest UTA: Test for valid(String, String, int)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#valid(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testValid3() throws Throwable
    {
        // When
        String host = null; // UTA: configured value
        String scheme = "scheme"; // UTA: default value
        int port = 1; // UTA: default value
        ValidationResult result = ValidationResult.valid(host, scheme, port);

    }

    /**
     * Parasoft Jtest UTA: Test for valid(String, String, int)
     *
     * @see com.sim.chatserver.security.review.TrustedUrlValidator.ValidationResult#valid(String, String, int)
     * @author bmcmullin
     */
    @Test
    public void testValid4() throws Throwable
    {
        // When
        String host = null; // UTA: configured value
        String scheme = null; // UTA: configured value
        int port = 1; // UTA: default value
        ValidationResult result = ValidationResult.valid(host, scheme, port);

    }
}
