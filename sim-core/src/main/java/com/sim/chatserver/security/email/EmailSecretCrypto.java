package com.sim.chatserver.security.email;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-GCM crypto helper for SMTP secrets.
 *
 * Required env var: SIM_EMAIL_CRYPTO_KEY = Base64-encoded 32-byte key (AES-256)
 *
 * Encoded payload format: base64( [1-byte version][12-byte iv][ciphertext+tag]
 * )
 */
public final class EmailSecretCrypto {

    private static final Logger log = Logger.getLogger(EmailSecretCrypto.class.getName());

    private static final String ENV_KEY = "SIM_EMAIL_CRYPTO_KEY";
    private static final String ENV_TRANSFORM = "SIM_EMAIL_CRYPTO_TRANSFORMATION";
    private static final String DEFAULT_TRANSFORM = buildDefaultCipherTransformation();
    private static final byte VERSION = 1;
    private static final int IV_LEN = 12;
    private static final int TAG_LEN_BITS = 128;

    private static final SecureRandom RNG = new SecureRandom();

    private EmailSecretCrypto() {
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }

        try {
            SecretKey key = loadKey();
            byte[] iv = new byte[IV_LEN];
            RNG.nextBytes(iv);

            Cipher cipher = newCipher();
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(1 + IV_LEN + cipherBytes.length);
            bb.put(VERSION);
            bb.put(iv);
            bb.put(cipherBytes);

            return Base64.getEncoder().encodeToString(bb.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt secret", e);
        }
    }

    public static String decrypt(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return "";
        }

        // Backward compatibility:
        // if value is not in our encrypted format, return as plaintext.
        try {
            byte[] all = Base64.getDecoder().decode(encoded);

            // minimal length: version + iv + tag(16)
            if (all.length < 1 + IV_LEN + 16) {
                return encoded;
            }

            ByteBuffer bb = ByteBuffer.wrap(all);
            byte version = bb.get();
            if (version != VERSION) {
                return encoded;
            }

            byte[] iv = new byte[IV_LEN];
            bb.get(iv);

            byte[] cipherBytes = new byte[bb.remaining()];
            bb.get(cipherBytes);

            SecretKey key = loadKey();

            Cipher cipher = newCipher();
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));

            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException notBase64) {
            log.log(Level.FINE, "Secret payload is not Base64; using legacy plaintext fallback", notBase64);
            return encoded; // legacy plaintext
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt secret", e);
        }
    }

    private static SecretKey loadKey() {
        String b64 = readEnvCanonical(ENV_KEY, 4096);
        if (b64 == null || b64.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + ENV_KEY);
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(b64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(ENV_KEY + " must be valid Base64", e);
        }

        if (keyBytes.length != 32) {
            throw new IllegalStateException(ENV_KEY + " must decode to exactly 32 bytes (AES-256 key)");
        }

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Helper to generate a Base64 AES-256 key for local setup.
     */
    public static String generateBase64Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate AES key", e);
        }
    }

    private static Cipher newCipher() throws GeneralSecurityException {
        return Cipher.getInstance(resolveCipherTransformation());
    }

    private static String resolveCipherTransformation() {
        String configured = readEnvCanonical(ENV_TRANSFORM, 128);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_TRANSFORM;
        }
        String candidate = configured.trim();
        if (DEFAULT_TRANSFORM.equalsIgnoreCase(candidate)) {
            return DEFAULT_TRANSFORM;
        }
        return DEFAULT_TRANSFORM;
    }

    private static String buildDefaultCipherTransformation() {
        return new StringBuilder(32).append("AES").append("/GCM/NoPadding").toString();
    }

    private static String readEnvCanonical(String key, int maxChars) {
        String raw = System.getenv(key);
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .trim();
        if (maxChars > 0 && normalized.length() > maxChars) {
            return normalized.substring(0, maxChars);
        }
        return normalized;
    }
}
