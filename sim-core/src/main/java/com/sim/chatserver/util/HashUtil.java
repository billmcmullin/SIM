package com.sim.chatserver.util;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
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

    static String sha1Hex(String value) {
        return digestHex("SHA-1", value);
    }

    static String sha256Hex(String value) {
        return digestHex("SHA-256", value);
    }

    private static String digestHex(String algorithm, String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.WARNING, "Hash algorithm unavailable: " + algorithm, ex);
            return algorithm.toLowerCase(Locale.ROOT) + "_error";
        }
    }
}
