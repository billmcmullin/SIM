package com.sim.chatserver.util;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parasoft Jtest UTA: Test class for HashUtil
 *
 * @see com.sim.chatserver.util.HashUtil
 * @author bmcmullin
 */
public class HashUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for sha1Hex(String)
     *
     * @see com.sim.chatserver.util.HashUtil#sha1Hex(String)
     * @author bmcmullin
     */
    @Test
    public void testSha1Hex() throws Throwable
    {
        // When
        String value = "value"; // UTA: default value
        String result = HashUtil.sha1Hex(value);

    }

    /**
     * Parasoft Jtest UTA: Test for sha1Hex(String)
     *
     * @see com.sim.chatserver.util.HashUtil#sha1Hex(String)
     * @author bmcmullin
     */
    @Test
    public void testSha1Hex2() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        String result = HashUtil.sha1Hex(value);

    }

    /**
     * Parasoft Jtest UTA: Test for sha256Hex(String)
     *
     * @see com.sim.chatserver.util.HashUtil#sha256Hex(String)
     * @author bmcmullin
     */
    @Test
    public void testSha256Hex() throws Throwable
    {
        // When
        String value = "value"; // UTA: default value
        String result = HashUtil.sha256Hex(value);

    }

    /**
     * Parasoft Jtest UTA: Test for sha256Hex(String)
     *
     * @see com.sim.chatserver.util.HashUtil#sha256Hex(String)
     * @author bmcmullin
     */
    @Test
    public void testSha256Hex2() throws Throwable
    {
        // When
        String value = null; // UTA: configured value
        String result = HashUtil.sha256Hex(value);

    }

    @Test
    public void testDigestHex_invalidAlgorithm_returnsErrorToken() throws Throwable
    {
        Method digestHex = HashUtil.class.getDeclaredMethod("digestHex", String.class, String.class);
        digestHex.setAccessible(true);

        String result = (String) digestHex.invoke(null, "SHA-999", "value");

        assertEquals("sha-999_error", result);
    }
}
