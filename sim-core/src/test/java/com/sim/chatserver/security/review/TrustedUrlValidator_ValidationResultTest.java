package com.sim.chatserver.security.review;

import java.lang.reflect.Method;

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
        ValidationResult underTest = invokeInvalid(reason);

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
        ValidationResult underTest = invokeInvalid(reason);

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
        ValidationResult underTest = invokeInvalid(reason);

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
        ValidationResult underTest = invokeInvalid(reason);

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
        ValidationResult result = invokeInvalid(reason);

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
        ValidationResult result = invokeInvalid(reason);

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
        ValidationResult underTest = invokeInvalid(reason);

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
        ValidationResult underTest = invokeValid(host, scheme, port);

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
        ValidationResult result = invokeValid(host, scheme, port);

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
        ValidationResult result = invokeValid(host, scheme, port);

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
        ValidationResult result = invokeValid(host, scheme, port);

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
        ValidationResult result = invokeValid(host, scheme, port);

    }

    private static ValidationResult invokeInvalid(String reason) throws Exception {
        Method method = ValidationResult.class.getDeclaredMethod("invalid", String.class);
        method.setAccessible(true);
        return (ValidationResult) method.invoke(null, reason);
    }

    private static ValidationResult invokeValid(String host, String scheme, int port) throws Exception {
        Method method = ValidationResult.class.getDeclaredMethod("valid", String.class, String.class, int.class);
        method.setAccessible(true);
        return (ValidationResult) method.invoke(null, host, scheme, Integer.valueOf(port));
    }
}
