package com.sim.chatserver.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Hash helper utilities.
 */
public final class HashUtil {

    private HashUtil() {
        // util
    }

    public static String sha1Hex(String value) {
        return digestHex("SHA-1", value);
    }

    public static String sha256Hex(String value) {
        return digestHex("SHA-256", value);
    }

    private static String digestHex(String algorithm, String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return algorithm.toLowerCase() + "_error";
        }
    }
}
