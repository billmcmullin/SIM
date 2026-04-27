package com.sim.chatserver.util;

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

        // Then - assertions for result of method sha1Hex(String)
        assertEquals("f32b67c7e26342af42efabc674d441dca0a281c5", result);

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

        // Then - assertions for result of method sha256Hex(String)
        assertEquals("cd42404d52ad55ccfa9aca4adc828aa5800ad9d385a0671fbcbf724118320619", result);

    }

}
