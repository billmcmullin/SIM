package com.sim.chatserver.model;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;

public final class CustomerProfileStore {

    private static final Logger LOG = Logger.getLogger(CustomerProfileStore.class.getName());
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    private static final String TABLE_NAME = "customer_profile_cache";

    private static final String CREATE_TABLE_SQL
            = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + "id BIGSERIAL PRIMARY KEY, "
            + "session_id TEXT UNIQUE NOT NULL, "
            + "friendly_name TEXT, "
            + "salesforce_contact_id TEXT, "
            + "salesforce_account_id TEXT, "
            + "email_enc TEXT, "
            + "phone_enc TEXT, "
            + "title_enc TEXT, "
            + "department_enc TEXT, "
            + "raw_json_enc TEXT, "
            + "last_synced_at TIMESTAMPTZ)";

    private static final String SELECT_BY_SESSION_SQL
            = "SELECT session_id, friendly_name, salesforce_contact_id, salesforce_account_id, "
            + "email_enc, phone_enc, title_enc, department_enc, raw_json_enc, last_synced_at "
            + "FROM " + TABLE_NAME + " WHERE session_id = ? LIMIT 1";

    private static final String UPSERT_SQL
            = "INSERT INTO " + TABLE_NAME + " ("
            + "session_id, friendly_name, salesforce_contact_id, salesforce_account_id, "
            + "email_enc, phone_enc, title_enc, department_enc, raw_json_enc, last_synced_at"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (session_id) DO UPDATE SET "
            + "friendly_name = EXCLUDED.friendly_name, "
            + "salesforce_contact_id = EXCLUDED.salesforce_contact_id, "
            + "salesforce_account_id = EXCLUDED.salesforce_account_id, "
            + "email_enc = EXCLUDED.email_enc, "
            + "phone_enc = EXCLUDED.phone_enc, "
            + "title_enc = EXCLUDED.title_enc, "
            + "department_enc = EXCLUDED.department_enc, "
            + "raw_json_enc = EXCLUDED.raw_json_enc, "
            + "last_synced_at = EXCLUDED.last_synced_at";

    private static volatile AppDataSourceHolder dsHolder;

    // Encryption settings
    private static final String ENC_KEY_ENV = "CONFIG_ENCRYPTION_KEY";
    private static final String ENC_SALT_ENV = "CONFIG_ENCRYPTION_SALT";
    private static final String ENC_TRANSFORM_ENV = "CONFIG_ENCRYPTION_TRANSFORMATION";
    private static final String ENC_PREFIX = "ENCv1:";
    private static final String DEFAULT_AES_MODE = buildDefaultCipherTransformation();
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int AES_KEY_BYTES = 32;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CustomerProfileStore() {
    }

    static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        dsHolder = holder;
    }

    private static void ensureTable() throws SQLException {
        try (Connection conn = getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.execute();
        }

        // Safe column migrations if table existed from older version
        try (Connection conn = getDataSource().getConnection()) {
            ensureColumn(conn, "friendly_name", "TEXT");
            ensureColumn(conn, "salesforce_contact_id", "TEXT");
            ensureColumn(conn, "salesforce_account_id", "TEXT");
            ensureColumn(conn, "email_enc", "TEXT");
            ensureColumn(conn, "phone_enc", "TEXT");
            ensureColumn(conn, "title_enc", "TEXT");
            ensureColumn(conn, "department_enc", "TEXT");
            ensureColumn(conn, "raw_json_enc", "TEXT");
            ensureColumn(conn, "last_synced_at", "TIMESTAMPTZ");
        }
    }

    public static CustomerProfile loadBySessionId(String sessionId) throws SQLException {
        ensureTable();

        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        try (Connection conn = getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_BY_SESSION_SQL)) {
            ps.setString(1, sessionId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                CustomerProfile p = new CustomerProfile();
                p.setSessionId(readDbText(rs, "session_id", 256));
                p.setFriendlyName(readDbText(rs, "friendly_name", 512));
                p.setSalesforceContactId(readDbText(rs, "salesforce_contact_id", 256));
                p.setSalesforceAccountId(readDbText(rs, "salesforce_account_id", 256));
                p.setEmail(decryptIfNeeded(readDbRawText(rs, "email_enc")));
                p.setPhone(decryptIfNeeded(readDbRawText(rs, "phone_enc")));
                p.setTitle(decryptIfNeeded(readDbRawText(rs, "title_enc")));
                p.setDepartment(decryptIfNeeded(readDbRawText(rs, "department_enc")));
                p.setRawJson(decryptIfNeeded(readDbRawText(rs, "raw_json_enc")));

                Timestamp ts = readDbTimestamp(rs, "last_synced_at");
                if (ts != null) {
                    p.setLastSyncedAt(ts.toInstant().atOffset(ZoneOffset.UTC));
                }

                return p;
            }
        }
    }

    public static void upsert(CustomerProfile profile) throws SQLException {
        ensureTable();

        if (profile == null || isBlank(profile.getSessionId())) {
            throw new IllegalArgumentException("profile.sessionId is required");
        }

        OffsetDateTime syncedAt = profile.getLastSyncedAt() != null ? profile.getLastSyncedAt() : OffsetDateTime.now(ZoneOffset.UTC);

        try (Connection conn = getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
            ps.setString(1, profile.getSessionId().trim());
            ps.setString(2, nullIfBlank(profile.getFriendlyName()));
            ps.setString(3, nullIfBlank(profile.getSalesforceContactId()));
            ps.setString(4, nullIfBlank(profile.getSalesforceAccountId()));
            ps.setString(5, encryptIfPresent(profile.getEmail()));
            ps.setString(6, encryptIfPresent(profile.getPhone()));
            ps.setString(7, encryptIfPresent(profile.getTitle()));
            ps.setString(8, encryptIfPresent(profile.getDepartment()));
            ps.setString(9, encryptIfPresent(profile.getRawJson()));
            ps.setTimestamp(10, Timestamp.from(syncedAt.toInstant()));

            ps.executeUpdate();
        }
    }

    private static void ensureColumn(Connection conn, String columnName, String columnType) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!columns.next()) {
                try (PreparedStatement alter = conn.prepareStatement(
                        "ALTER TABLE " + TABLE_NAME + ' ' + "ADD COLUMN " + columnName + ' ' + columnType)) {
                    alter.execute();
                }
            }
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String nullIfBlank(String v) {
        return isBlank(v) ? null : v.trim();
    }

    private static String encryptIfPresent(String plainValue) throws SQLException {
        if (plainValue == null || plainValue.isBlank()) {
            return plainValue == null ? null : "";
        }
        if (plainValue.startsWith(ENC_PREFIX)) {
            return plainValue; // already encrypted
        }

        try {
            byte[] key = getAesKeyBytes();
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = newCipher();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            return ENC_PREFIX
                    + Base64.getEncoder().encodeToString(iv)
                    + ':'
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new SQLException("Unable to encrypt customer profile value", e);
        }
    }

    private static String decryptIfNeeded(String storedValue) throws SQLException {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        if (!storedValue.startsWith(ENC_PREFIX)) {
            return storedValue; // backward compatibility for plaintext
        }

        try {
            String payload = storedValue.substring(ENC_PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                throw new SQLException("Invalid encrypted value format");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] enc = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = newCipher();
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getAesKeyBytes(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new SQLException("Unable to decrypt customer profile value", e);
        }
    }

    private static byte[] getAesKeyBytes() {
        String secret = readEnvCanonical(ENC_KEY_ENV, 4096);
        if (secret == null) {
            throw new IllegalStateException("Environment variable " + ENC_KEY_ENV + " is required for profile encryption.");
        }

        byte[] salt = resolveKdfSalt();
        String trimmed = secret;

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
                return decoded;
            }
            return deriveAesKey(decoded, salt);
        } catch (IllegalArgumentException notBase64) {
            LOG.log(Level.FINE, "CONFIG_ENCRYPTION_KEY is not Base64, deriving key from UTF-8 value", notBase64);
            return deriveAesKey(trimmed.getBytes(StandardCharsets.UTF_8), salt);
        }
    }

    private static byte[] deriveAesKey(byte[] keyMaterial, byte[] salt) {
        String material = Base64.getEncoder().encodeToString(keyMaterial);
        PBEKeySpec spec = new PBEKeySpec(material.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BYTES * 8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to derive profile encryption key", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] resolveKdfSalt() {
        String configuredSalt = readEnvCanonical(ENC_SALT_ENV, 1024);
        if (configuredSalt != null) {
            return configuredSalt.getBytes(StandardCharsets.UTF_8);
        }
        return "sim-customer-profile-kdf-salt-v1".getBytes(StandardCharsets.UTF_8);
    }

    private static Cipher newCipher() throws GeneralSecurityException {
        return Cipher.getInstance(resolveCipherTransformation());
    }

    private static String resolveCipherTransformation() {
        String configured = readEnvCanonical(ENC_TRANSFORM_ENV, 64);
        if (configured == null) {
            return DEFAULT_AES_MODE;
        }
        String candidate = configured;
        if (DEFAULT_AES_MODE.equalsIgnoreCase(candidate)) {
            return DEFAULT_AES_MODE;
        }
        return DEFAULT_AES_MODE;
    }

    private static String readEnvCanonical(String envName, int maxChars) {
        String raw = ENV.get(envName);
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.length() > maxChars) {
            throw new IllegalStateException("Environment variable too long: " + envName);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new IllegalStateException("Environment variable contains invalid control characters: " + envName);
        }
        return normalized;
    }

    private static String readDbRawText(ResultSet rs, String column) throws SQLException {
        try {
            Object raw = rs.getObject(column);
            if (raw == null) {
                return null;
            }
            if (raw instanceof byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return String.valueOf(raw);
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "Typed DB text read failed for column " + column, ex);
            return null;
        }
    }

    private static String readDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        String raw = readDbRawText(rs, column);
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars);
    }

    private static Timestamp readDbTimestamp(ResultSet rs, String column) throws SQLException {
        try {
            Timestamp typed = rs.getTimestamp(column);
            if (typed != null) {
                return typed;
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "Typed DB timestamp read failed for column " + column + ", using text parsing", ex);
        }
        String raw = readDbText(rs, column, 128);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(raw.trim().replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, "Unable to parse timestamp for customer profile column " + column, ex);
            return null;
        }
    }

    private static String buildDefaultCipherTransformation() {
        return new StringBuilder(32).append("AES").append("/GCM/NoPadding").toString();
    }

    private static DataSource getDataSource() {
        AppDataSourceHolder holder = dsHolder;
        if (holder == null) {
            holder = CDI.current().select(AppDataSourceHolder.class).get();
            dsHolder = holder;
        }
        if (holder == null) {
            throw new IllegalStateException("AppDataSourceHolder is not initialized");
        }
        return holder.getDataSource();
    }
}
