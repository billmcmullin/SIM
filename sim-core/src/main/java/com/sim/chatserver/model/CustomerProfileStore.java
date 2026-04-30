package com.sim.chatserver.model;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;

public final class CustomerProfileStore {

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
    private static final String ENC_PREFIX = "ENCv1:";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CustomerProfileStore() {
    }

    public static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        dsHolder = holder;
    }

    public static void ensureTable() throws SQLException {
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
                p.setSessionId(rs.getString("session_id"));
                p.setFriendlyName(rs.getString("friendly_name"));
                p.setSalesforceContactId(rs.getString("salesforce_contact_id"));
                p.setSalesforceAccountId(rs.getString("salesforce_account_id"));
                p.setEmail(decryptIfNeeded(rs.getString("email_enc")));
                p.setPhone(decryptIfNeeded(rs.getString("phone_enc")));
                p.setTitle(decryptIfNeeded(rs.getString("title_enc")));
                p.setDepartment(decryptIfNeeded(rs.getString("department_enc")));
                p.setRawJson(decryptIfNeeded(rs.getString("raw_json_enc")));

                Timestamp ts = rs.getTimestamp("last_synced_at");
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
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + columnType)) {
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

            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            return ENC_PREFIX
                    + Base64.getEncoder().encodeToString(iv)
                    + ":"
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

            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getAesKeyBytes(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new SQLException("Unable to decrypt customer profile value", e);
        }
    }

    private static byte[] getAesKeyBytes() {
        String secret = System.getenv(ENC_KEY_ENV);
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Environment variable " + ENC_KEY_ENV + " is required for profile encryption.");
        }

        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32]; // AES-256 sized key buffer
        for (int i = 0; i < key.length; i++) {
            key[i] = i < raw.length ? raw[i] : 0;
        }
        return key;
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
