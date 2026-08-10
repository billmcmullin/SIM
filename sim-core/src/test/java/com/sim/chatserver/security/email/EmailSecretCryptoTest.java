package com.sim.chatserver.security.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 class EmailSecretCryptoTest {

    private static final String KEY_ENV = "SIM_EMAIL_CRYPTO_KEY";
    private static final String TRANSFORM_ENV = "SIM_EMAIL_CRYPTO_TRANSFORMATION";

    private String originalKey;
    private String originalTransform;

    @BeforeEach
    void captureEnv() throws Exception {
        Map<String, String> env = env();
        originalKey = env.get(KEY_ENV);
        originalTransform = env.get(TRANSFORM_ENV);
    }

    @AfterEach
    void restoreEnv() throws Exception {
        Map<String, String> env = env();
        putOrRemove(env, KEY_ENV, originalKey);
        putOrRemove(env, TRANSFORM_ENV, originalTransform);
    }

    @Test
    void generateBase64Key_produces32ByteAesKey() {
        byte[] keyBytes = Base64.getDecoder().decode(EmailSecretCrypto.generateBase64Key());
        assertEquals(32, keyBytes.length);
    }

    @Test
    void encryptDecrypt_roundTripWorksWithConfiguredKey() throws Exception {
        Map<String, String> env = env();
        env.put(KEY_ENV, EmailSecretCrypto.generateBase64Key());
        env.put(TRANSFORM_ENV, "AES/GCM/NoPadding");

        String encrypted = EmailSecretCrypto.encrypt("smtp-secret");
        String decrypted = EmailSecretCrypto.decrypt(encrypted);

        assertEquals("smtp-secret", decrypted);
    }

    @Test
    void decrypt_returnsLegacyPlaintextWhenInputIsNotBase64() {
        assertEquals("legacy-plain", EmailSecretCrypto.decrypt("legacy-plain"));
    }

    @Test
    void decrypt_returnsOriginalWhenPayloadIsTooShortOrWrongVersion() {
        String shortPayload = Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3, 4 });
        assertEquals(shortPayload, EmailSecretCrypto.decrypt(shortPayload));

        byte[] wrongVersion = new byte[1 + 12 + 16];
        wrongVersion[0] = 2;
        String wrongVersionPayload = Base64.getEncoder().encodeToString(wrongVersion);
        assertEquals(wrongVersionPayload, EmailSecretCrypto.decrypt(wrongVersionPayload));
    }

    @Test
    void encrypt_failsWhenKeyIsMissing() throws Exception {
        Map<String, String> env = env();
        env.remove(KEY_ENV);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> EmailSecretCrypto.encrypt("x"));
        assertTrue(ex.getMessage().contains("Missing required environment variable"));
    }

    @Test
    void encrypt_failsWhenKeyLengthIsInvalid() throws Exception {
        Map<String, String> env = env();
        env.put(KEY_ENV, Base64.getEncoder().encodeToString(new byte[16]));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> EmailSecretCrypto.encrypt("x"));
        assertTrue(ex.getMessage().contains("exactly 32 bytes"));
    }

    @Test
    void decrypt_acceptsEncryptedPayloadBuiltWithKnownKey() throws Exception {
        Map<String, String> env = env();
        byte[] fixedKey = new byte[32];
        for (int i = 0; i < fixedKey.length; i++) {
            fixedKey[i] = (byte) i;
        }
        env.put(KEY_ENV, Base64.getEncoder().encodeToString(fixedKey));
        env.put(TRANSFORM_ENV, "unsupported-value");

        String encrypted = EmailSecretCrypto.encrypt("fixed-value");
        String decrypted = EmailSecretCrypto.decrypt(encrypted);

        assertEquals("fixed-value", decrypted);
        assertArrayEquals(fixedKey, Base64.getDecoder().decode(env.get(KEY_ENV)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> env() throws Exception {
        Field field = EmailSecretCrypto.class.getDeclaredField("ENV");
        field.setAccessible(true);
        return (Map<String, String>) field.get(null);
    }

    private static void putOrRemove(Map<String, String> env, String key, String value) {
        if (value == null) {
            env.remove(key);
        } else {
            env.put(key, value);
        }
    }
}

