package com.sim.chatserver.service;

import com.sim.chatserver.dto.DbConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigStore {
    private static final String STORE_FILE = "app-db-config.enc";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // AES-GCM; key must be 32 bytes (Base64 encoded in env CHAT_APP_KEY)
    private static byte[] getKey() {
        try {
            String b64 = System.getenv("CHAT_APP_KEY");
            if (b64 == null || b64.isEmpty()) return null;
            return Base64.getDecoder().decode(b64);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean saveEncrypted(DbConfig cfg) throws Exception {
        byte[] key = getKey();
        if (key == null) return false;
        byte[] payload = MAPPER.writeValueAsBytes(cfg);

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] cipherText = cipher.doFinal(payload);

        // store iv + ciphertext as Base64
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        String out = Base64.getEncoder().encodeToString(combined);
        Files.write(new File(STORE_FILE).toPath(), out.getBytes());
        return true;
    }

    public DbConfig loadEncrypted() throws Exception {
        byte[] key = getKey();
        if (key == null) return null;
        File f = new File(STORE_FILE);
        if (!f.exists()) return null;
        byte[] all = Files.readAllBytes(f.toPath());
        byte[] decoded = Base64.getDecoder().decode(new String(all));
        byte[] iv = new byte[12];
        System.arraycopy(decoded, 0, iv, 0, 12);
        byte[] cipherText = new byte[decoded.length - 12];
        System.arraycopy(decoded, 12, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        byte[] plain = cipher.doFinal(cipherText);
        return MAPPER.readValue(plain, DbConfig.class);
    }
}
