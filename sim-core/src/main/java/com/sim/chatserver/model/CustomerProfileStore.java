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
import com.sim.chatserver.util.SqlLiteralConstants;

import jakarta.enterprise.inject.spi.CDI;

public final class CustomerProfileStore {

    private static final String EMPTY = "";

    private static final Logger LOG = Logger.getLogger(CustomerProfileStore.class.getCanonicalName());
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    private static final String TABLE_NAME = "customer_profile_cache";
    private static final String COL_SESSION_ID = SqlLiteralConstants.COL_SESSION_ID;
    private static final String COL_FRIENDLY_NAME = "friendly_name";
    private static final String COL_SALESFORCE_CONTACT_ID = "salesforce_contact_id";
    private static final String COL_SALESFORCE_ACCOUNT_ID = "salesforce_account_id";
    private static final String COL_EMAIL_ENC = "email_enc";
    private static final String COL_PHONE_ENC = "phone_enc";
    private static final String COL_TITLE_ENC = "title_enc";
    private static final String COL_DEPARTMENT_ENC = "department_enc";
    private static final String COL_RAW_JSON_ENC = "raw_json_enc";
    private static final String COL_LAST_SYNCED_AT = "last_synced_at";

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
    private static final String ENC_KEY_ENV_LOG = "CONFIG_ENCRYPTION_KEY is not Base64, deriving key from UTF-8 value";
    private static final String ENC_PREFIX = "ENCv1:";
    private static final String COLUMN_TYPE_TEXT = "TEXT";
    private static final String COLUMN_TYPE_TIMESTAMPTZ = "TIMESTAMPTZ";
    private static final String CRYPTO_ALGORITHM_AES = "AES";
    private static final String CIPHER_MODE_GCM_NO_PADDING = "/GCM/NoPadding";
    private static final String DEFAULT_AES_MODE = buildDefaultCipherTransformation();
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int AES_KEY_BYTES = 32;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String MSG_PROFILE_SESSION_ID_REQUIRED = "profile.sessionId is required";
    private static final String MSG_ENCRYPTION_FAILED = "Unable to encrypt customer profile value";
    private static final String MSG_INVALID_ENCRYPTED_FORMAT = "Invalid encrypted value format";
    private static final String MSG_DECRYPTION_FAILED = "Unable to decrypt customer profile value";
    private static final String MSG_ENV_VAR_PREFIX = "Environment variable ";
    private static final String MSG_PROFILE_ENCRYPTION_REQUIRED_SUFFIX = " is required for profile encryption.";
    private static final String PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
    private static final String MSG_DERIVE_KEY_FAILED = "Unable to derive profile encryption key";
    private static final String DEFAULT_KDF_SALT = "sim-customer-profile-kdf-salt-v1";
    private static final String MSG_ENV_VAR_TOO_LONG = "Environment variable too long: ";
    private static final String MSG_ENV_VAR_INVALID_CHARS = "Environment variable contains invalid control characters: ";
    private static final String MSG_TYPED_DB_TEXT_READ_FAILED = "Typed DB text read failed for column ";
    private static final String MSG_TYPED_DB_TIMESTAMP_READ_FAILED = "Typed DB timestamp read failed for column ";
    private static final String MSG_USING_TEXT_PARSING = ", using text parsing";
    private static final String MSG_PARSE_TIMESTAMP_FAILED = "Unable to parse timestamp for customer profile column ";
    private static final String MSG_APP_DATASOURCE_HOLDER_NOT_INITIALIZED = "AppDataSourceHolder is not initialized";

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
            ensureColumn(conn, COL_FRIENDLY_NAME, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_SALESFORCE_CONTACT_ID, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_SALESFORCE_ACCOUNT_ID, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_EMAIL_ENC, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_PHONE_ENC, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_TITLE_ENC, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_DEPARTMENT_ENC, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_RAW_JSON_ENC, COLUMN_TYPE_TEXT);
            ensureColumn(conn, COL_LAST_SYNCED_AT, COLUMN_TYPE_TIMESTAMPTZ);
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
                p.setSessionId(readDbText(rs, COL_SESSION_ID, 256));
                p.setFriendlyName(readDbText(rs, COL_FRIENDLY_NAME, 512));
                p.setSalesforceContactId(readDbText(rs, COL_SALESFORCE_CONTACT_ID, 256));
                p.setSalesforceAccountId(readDbText(rs, COL_SALESFORCE_ACCOUNT_ID, 256));
                p.setEmail(decryptIfNeeded(readDbRawText(rs, COL_EMAIL_ENC)));
                p.setPhone(decryptIfNeeded(readDbRawText(rs, COL_PHONE_ENC)));
                p.setTitle(decryptIfNeeded(readDbRawText(rs, COL_TITLE_ENC)));
                p.setDepartment(decryptIfNeeded(readDbRawText(rs, COL_DEPARTMENT_ENC)));
                p.setRawJson(decryptIfNeeded(readDbRawText(rs, COL_RAW_JSON_ENC)));

                Timestamp ts = readDbTimestamp(rs, COL_LAST_SYNCED_AT);
                if (ts != null) {
                    p.setLastSyncedAt(ts.toInstant().atOffset(ZoneOffset.UTC));
                }

                return p;
            }
        }
    }

    private static void upsert(CustomerProfile profile) throws SQLException {
        ensureTable();

        if (profile == null || isBlank(profile.getSessionId())) {
            throw new IllegalArgumentException(MSG_PROFILE_SESSION_ID_REQUIRED);
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

    public static void saveProfile(CustomerProfile profile) throws SQLException {
        upsert(profile);
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
            return plainValue == null ? null : EMPTY;
        }
        if (plainValue.startsWith(ENC_PREFIX)) {
            return plainValue; // already encrypted
        }

        try {
            byte[] key = getAesKeyBytes();
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = newCipher();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CRYPTO_ALGORITHM_AES), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            return ENC_PREFIX
                    + Base64.getEncoder().encodeToString(iv)
                    + (char) 58
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new SQLException(MSG_ENCRYPTION_FAILED, e);
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
            String[] parts = payload.split(String.valueOf((char) 58), 2);
            if (parts.length != 2) {
                throw new SQLException(MSG_INVALID_ENCRYPTED_FORMAT);
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] enc = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = newCipher();
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getAesKeyBytes(), CRYPTO_ALGORITHM_AES), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new SQLException(MSG_DECRYPTION_FAILED, e);
        }
    }

    private static byte[] getAesKeyBytes() {
        String secret = readEnvCanonical(ENC_KEY_ENV, 4096);
        if (secret == null) {
            throw new IllegalStateException(MSG_ENV_VAR_PREFIX + ENC_KEY_ENV + MSG_PROFILE_ENCRYPTION_REQUIRED_SUFFIX);
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
            LOG.log(Level.FINE, ENC_KEY_ENV_LOG, notBase64);
            return deriveAesKey(trimmed.getBytes(StandardCharsets.UTF_8), salt);
        }
    }

    private static byte[] deriveAesKey(byte[] keyMaterial, byte[] salt) {
        String material = Base64.getEncoder().encodeToString(keyMaterial);
        PBEKeySpec spec = new PBEKeySpec(material.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BYTES * 8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_HMAC_SHA256);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(MSG_DERIVE_KEY_FAILED, e);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] resolveKdfSalt() {
        String configuredSalt = readEnvCanonical(ENC_SALT_ENV, 1024);
        if (configuredSalt != null) {
            return configuredSalt.getBytes(StandardCharsets.UTF_8);
        }
        return DEFAULT_KDF_SALT.getBytes(StandardCharsets.UTF_8);
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
            throw new IllegalStateException(MSG_ENV_VAR_TOO_LONG + envName);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new IllegalStateException(MSG_ENV_VAR_INVALID_CHARS + envName);
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
            LOG.log(Level.FINE, MSG_TYPED_DB_TEXT_READ_FAILED + column, ex);
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
        return new String(normalized.toCharArray(), 0, maxChars);
    }

    private static Timestamp readDbTimestamp(ResultSet rs, String column) throws SQLException {
        try {
            Timestamp typed = rs.getTimestamp(column);
            if (typed != null) {
                return typed;
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, MSG_TYPED_DB_TIMESTAMP_READ_FAILED + column + MSG_USING_TEXT_PARSING, ex);
        }
        String raw = readDbText(rs, column, 128);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(raw.trim().replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, MSG_PARSE_TIMESTAMP_FAILED + column, ex);
            return null;
        }
    }

    private static String buildDefaultCipherTransformation() {
        return new StringBuilder(32).append(CRYPTO_ALGORITHM_AES).append(CIPHER_MODE_GCM_NO_PADDING).toString();
    }

    private static DataSource getDataSource() {
        AppDataSourceHolder holder = dsHolder;
        if (holder == null) {
            holder = CDI.current().select(AppDataSourceHolder.class).get();
            dsHolder = holder;
        }
        if (holder == null) {
            throw new IllegalStateException(MSG_APP_DATASOURCE_HOLDER_NOT_INITIALIZED);
        }
        return holder.getDataSource();
    }
}