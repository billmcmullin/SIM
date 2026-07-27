package com.sim.chatserver.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hash helper utilities.
 */
public final class HashUtil {

    private static final Logger LOG = Logger.getLogger(HashUtil.class.getName());

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
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.WARNING, "Hash algorithm unavailable: " + algorithm, ex);
            return algorithm.toLowerCase(Locale.ROOT) + "_error";
        }
    }
}
