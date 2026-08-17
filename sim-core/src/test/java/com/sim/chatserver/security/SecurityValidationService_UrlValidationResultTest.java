package com.sim.chatserver.security;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.security.SecurityValidationService.UrlValidationResult;
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
        UrlValidationResult result = invokeAllowed(host, scheme, reason);

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
        UrlValidationResult result = invokeBlocked(reason);

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
        UrlValidationResult underTest = invokeBlocked(reason);

        // When
        String result = underTest.getHost();

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
        UrlValidationResult underTest = invokeBlocked(reason);

        // When
        String result = underTest.getReason();

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
        UrlValidationResult underTest = invokeBlocked(reason);

        // When
        String result = underTest.getScheme();

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
        UrlValidationResult underTest = invokeBlocked(reason);

        // When
        boolean result = invokeIsAllowed(underTest);

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
        UrlValidationResult underTest = invokeBlocked(reason);

        // When
        String result = underTest.toString();

    }

    private static UrlValidationResult invokeAllowed(String host, String scheme, String reason) throws Exception {
        Method method = UrlValidationResult.class.getDeclaredMethod("allowed", String.class, String.class, String.class);
        method.setAccessible(true);
        return (UrlValidationResult) method.invoke(null, host, scheme, reason);
    }

    private static UrlValidationResult invokeBlocked(String reason) throws Exception {
        Method method = UrlValidationResult.class.getDeclaredMethod("blocked", String.class);
        method.setAccessible(true);
        return (UrlValidationResult) method.invoke(null, reason);
    }

    private static boolean invokeIsAllowed(UrlValidationResult underTest) throws Exception {
        Method method = UrlValidationResult.class.getDeclaredMethod("isAllowed");
        method.setAccessible(true);
        return ((Boolean) method.invoke(underTest)).booleanValue();
    }
}
