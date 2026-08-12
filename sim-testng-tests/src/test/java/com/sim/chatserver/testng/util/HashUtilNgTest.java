package com.sim.chatserver.testng.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.sim.chatserver.util.HashUtil;

public class HashUtilNgTest {

    @Test
    public void sha1Hex_ReturnsKnownDigest_ForValue() {
        String result = HashUtil.sha1Hex("value");

        assertEquals(result, "f32b67c7e26342af42efabc674d441dca0a281c5");
        assertEquals(result.length(), 40);
    }

    @Test
    public void sha256Hex_ReturnsKnownDigest_ForValue() {
        String result = HashUtil.sha256Hex("value");

        assertEquals(result, "cd42404d52ad55ccfa9aca4adc828aa5800ad9d385a0671fbcbf724118320619");
        assertEquals(result.length(), 64);
    }

    @Test
    public void sha1Hex_TreatsNullAsEmptyString() {
        String result = HashUtil.sha1Hex(null);

        assertEquals(result, "da39a3ee5e6b4b0d3255bfef95601890afd80709");
    }

    @Test
    public void sha256Hex_TreatsNullAsEmptyString() {
        String result = HashUtil.sha256Hex(null);

        assertEquals(result, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    public void digestHex_InvalidAlgorithm_ReturnsErrorToken() throws Exception {
        Method digestHex = HashUtil.class.getDeclaredMethod("digestHex", String.class, String.class);
        digestHex.setAccessible(true);

        String result = (String) digestHex.invoke(null, "SHA-999", "value");

        assertTrue(result.endsWith("_error"));
        assertEquals(result, "sha-999_error");
    }
}
