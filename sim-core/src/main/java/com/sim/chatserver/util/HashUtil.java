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

    private static final String EMPTY = "";
    private static final String ALGO_SHA_1 = "SHA-1";
    private static final String ALGO_SHA_256 = "SHA-256";
    private static final String LOG_HASH_ALGO_UNAVAILABLE_PREFIX = "Hash algorithm unavailable: ";
    private static final String HASH_ERROR_SUFFIX = "_error";

    private static final Logger LOG = Logger.getLogger(HashUtil.class.getCanonicalName());

    private HashUtil() {
        // util
    }

    static String sha1Hex(String value) {
        return digestHex(ALGO_SHA_1, value);
    }

    static String sha256Hex(String value) {
        return digestHex(ALGO_SHA_256, value);
    }

    private static String digestHex(String algorithm, String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest((value == null ? EMPTY : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.WARNING, LOG_HASH_ALGO_UNAVAILABLE_PREFIX + algorithm, ex);
            return algorithm.toLowerCase(Locale.ROOT) + HASH_ERROR_SUFFIX;
        }
    }
}