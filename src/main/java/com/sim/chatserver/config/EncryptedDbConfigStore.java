package com.sim.chatserver.config;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;

public final class EncryptedDbConfigStore {

    private static final Logger log = Logger.getLogger(EncryptedDbConfigStore.class.getName());

    private static final String TABLE_NAME = "server_config";
    private static final String CREATE_TABLE_SQL
            = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + "id BIGSERIAL PRIMARY KEY, "
            + "server_host TEXT, "
            + "server_port INTEGER, "
            + "connection_info TEXT, "
            + "api_key TEXT, "
            + "workspace_name TEXT, "
            + "salesforce_instance_url TEXT, "
            + "salesforce_api_key TEXT, "
            + "salesforce_login_url TEXT, "
            + "salesforce_client_id TEXT, "
            + "salesforce_client_secret TEXT, "
            + "salesforce_refresh_token TEXT)";
    private static final String SELECT_SQL
            = "SELECT server_host, server_port, connection_info, api_key, workspace_name, "
            + "salesforce_instance_url, salesforce_api_key, "
            + "salesforce_login_url, salesforce_client_id, salesforce_client_secret, salesforce_refresh_token "
            + "FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 1";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME;
    private static final String INSERT_SQL
            = "INSERT INTO " + TABLE_NAME
            + " (server_host, server_port, connection_info, api_key, workspace_name, "
            + "salesforce_instance_url, salesforce_api_key, salesforce_login_url, salesforce_client_id, salesforce_client_secret, salesforce_refresh_token) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String ENC_KEY_ENV = "CONFIG_ENCRYPTION_KEY";
    private static final String ENC_PREFIX = "ENCv1:";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int AES_KEY_BYTES = 32; // AES-256
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static volatile AppDataSourceHolder dsHolder;

    private EncryptedDbConfigStore() {
    }

    public static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        dsHolder = holder;
        log.info("setAppDataSourceHolder called. holder=" + (holder == null ? "null" : holder.getClass().getName()));
    }

    public static void ensureTable() throws SQLException {
        log.fine("ensureTable: start");

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.execute();
            log.fine("ensureTable: create table statement executed");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "ensureTable: failed executing create table SQL", e);
            throw e;
        }

        try (Connection conn = ds.getConnection()) {
            ensureWorkspaceColumn(conn);
            ensureSalesforceInstanceUrlColumn(conn);
            ensureSalesforceApiKeyColumn(conn);
            ensureSalesforceLoginUrlColumn(conn);
            ensureSalesforceClientIdColumn(conn);
            ensureSalesforceClientSecretColumn(conn);
            ensureSalesforceRefreshTokenColumn(conn);
            log.fine("ensureTable: column checks complete");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "ensureTable: failed during column checks", e);
            throw e;
        }
    }

    public static ServerConfig load() throws SQLException {
        log.fine("load: start");
        ensureTable();

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_SQL); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                log.fine("load: row found in server_config");
                ServerConfig config = new ServerConfig();
                config.setServerHost(rs.getString("server_host"));
                config.setServerPort(rs.getInt("server_port"));
                config.setConnectionInfo(rs.getString("connection_info"));

                config.setApiKey(decryptIfNeeded(rs.getString("api_key")));
                config.setWorkspaceName(rs.getString("workspace_name"));

                config.setSalesforceInstanceUrl(rs.getString("salesforce_instance_url"));
                config.setSalesforceApiKey(decryptIfNeeded(rs.getString("salesforce_api_key")));

                config.setSalesforceLoginUrl(rs.getString("salesforce_login_url"));
                config.setSalesforceClientId(decryptIfNeeded(rs.getString("salesforce_client_id")));
                config.setSalesforceClientSecret(decryptIfNeeded(rs.getString("salesforce_client_secret")));
                config.setSalesforceRefreshToken(decryptIfNeeded(rs.getString("salesforce_refresh_token")));

                log.fine("load: config loaded successfully");
                return config;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "load: SQL failure", e);
            throw e;
        }

        log.fine("load: no rows found, returning new ServerConfig()");
        return new ServerConfig();
    }

    public static void save(ServerConfig config) throws SQLException {
        log.fine("save: start");
        ensureTable();

        String encryptedApiKey = encryptIfPresent(config != null ? config.getApiKey() : null);
        String encryptedSalesforceApiKey = encryptIfPresent(config != null ? config.getSalesforceApiKey() : null);

        String encryptedSalesforceClientId = encryptIfPresent(config != null ? config.getSalesforceClientId() : null);
        String encryptedSalesforceClientSecret = encryptIfPresent(config != null ? config.getSalesforceClientSecret() : null);
        String encryptedSalesforceRefreshToken = encryptIfPresent(config != null ? config.getSalesforceRefreshToken() : null);

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection(); PreparedStatement deleteStmt = conn.prepareStatement(DELETE_SQL)) {
            int deleted = deleteStmt.executeUpdate();
            log.fine("save: deleted existing rows count=" + deleted);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "save: failed deleting old config rows", e);
            throw e;
        }

        try (Connection conn = ds.getConnection(); PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL)) {
            insertStmt.setString(1, config != null ? config.getServerHost() : null);
            insertStmt.setInt(2, config != null ? config.getServerPort() : 0);
            insertStmt.setString(3, config != null ? config.getConnectionInfo() : null);
            insertStmt.setString(4, encryptedApiKey);
            insertStmt.setString(5, config != null ? config.getWorkspaceName() : null);

            insertStmt.setString(6, config != null ? config.getSalesforceInstanceUrl() : null);
            insertStmt.setString(7, encryptedSalesforceApiKey);
            insertStmt.setString(8, config != null ? config.getSalesforceLoginUrl() : null);
            insertStmt.setString(9, encryptedSalesforceClientId);
            insertStmt.setString(10, encryptedSalesforceClientSecret);
            insertStmt.setString(11, encryptedSalesforceRefreshToken);

            int inserted = insertStmt.executeUpdate();
            log.fine("save: insert complete, rows=" + inserted);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "save: failed inserting config row", e);
            throw e;
        }
    }

    public static void saveWorkspaceName(String workspaceName) throws SQLException {
        log.fine("saveWorkspaceName: start");
        ServerConfig config = load();
        config.setWorkspaceName(workspaceName);
        save(config);
        log.fine("saveWorkspaceName: complete");
    }

    private static void ensureWorkspaceColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "workspace_name", "TEXT");
    }

    private static void ensureSalesforceInstanceUrlColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_instance_url", "TEXT");
    }

    private static void ensureSalesforceApiKeyColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_api_key", "TEXT");
    }

    private static void ensureSalesforceLoginUrlColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_login_url", "TEXT");
    }

    private static void ensureSalesforceClientIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_client_id", "TEXT");
    }

    private static void ensureSalesforceClientSecretColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_client_secret", "TEXT");
    }

    private static void ensureSalesforceRefreshTokenColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_refresh_token", "TEXT");
    }

    private static void ensureColumn(Connection conn, String columnName, String sqlType) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!columns.next()) {
                log.info("ensureColumn: adding missing column " + columnName + " " + sqlType);
                try (PreparedStatement alter = conn.prepareStatement(
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + sqlType)) {
                    alter.execute();
                }
            } else {
                log.fine("ensureColumn: exists -> " + columnName);
            }
        }
    }

    private static String encryptIfPresent(String plainValue) throws SQLException {
        if (plainValue == null || plainValue.isBlank()) {
            return plainValue == null ? null : "";
        }
        if (plainValue.startsWith(ENC_PREFIX)) {
            log.fine("encryptIfPresent: value already encrypted, skipping");
            return plainValue;
        }
        try {
            byte[] key = getAesKeyBytes();
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String encB64 = Base64.getEncoder().encodeToString(encrypted);
            return ENC_PREFIX + ivB64 + ":" + encB64;
        } catch (GeneralSecurityException e) {
            log.log(Level.SEVERE, "encryptIfPresent: encryption failed", e);
            throw new SQLException("Unable to encrypt configuration value", e);
        }
    }

    private static String decryptIfNeeded(String storedValue) throws SQLException {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        if (!storedValue.startsWith(ENC_PREFIX)) {
            return storedValue;
        }

        try {
            String payload = storedValue.substring(ENC_PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                throw new SQLException("Invalid encrypted configuration format");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] enc = Base64.getDecoder().decode(parts[1]);

            byte[] key = getAesKeyBytes();
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "decryptIfNeeded: decryption failed for encrypted value", e);
            throw new SQLException("Unable to decrypt configuration value", e);
        }
    }

    /**
     * Derives an AES-256 key from CONFIG_ENCRYPTION_KEY with improved handling:
     * Preferred format: - Base64-encoded 32-byte key (recommended for
     * production) Backward-compatible fallback: - SHA-256 hash of raw env
     * string bytes (stable deterministic key)
     */
    private static byte[] getAesKeyBytes() {
        String secret = System.getenv(ENC_KEY_ENV);
        if (secret == null || secret.isBlank()) {
            IllegalStateException ex = new IllegalStateException(
                    "Environment variable " + ENC_KEY_ENV + " is required for config encryption.");
            log.log(Level.SEVERE, "getAesKeyBytes: missing " + ENC_KEY_ENV, ex);
            throw ex;
        }

        String trimmed = secret.trim();

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);

            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
                log.fine("getAesKeyBytes: using Base64-decoded AES key length=" + decoded.length);
                return decoded;
            }

            log.fine("getAesKeyBytes: Base64 decoded length=" + decoded.length
                    + ", deriving AES-256 key via SHA-256");
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(decoded);

        } catch (IllegalArgumentException notBase64) {
            try {
                log.fine("getAesKeyBytes: env not Base64, deriving AES-256 key via SHA-256 of UTF-8 string");
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                return sha256.digest(trimmed.getBytes(StandardCharsets.UTF_8));
            } catch (GeneralSecurityException e) {
                log.log(Level.SEVERE, "getAesKeyBytes: key derivation failed", e);
                throw new IllegalStateException("Unable to derive encryption key", e);
            }
        } catch (GeneralSecurityException e) {
            log.log(Level.SEVERE, "getAesKeyBytes: key derivation failed", e);
            throw new IllegalStateException("Unable to derive encryption key", e);
        }
    }

    // NEW: clear, null-safe datasource resolution to avoid NPEs in tests/scheduler paths
    private static DataSource requireDataSource() throws SQLException {
        DataSource ds = getDataSource();
        if (ds == null) {
            IllegalStateException ex = new IllegalStateException(
                    "DataSource is not initialized. Ensure AppDataSourceHolder is set before using EncryptedDbConfigStore.");
            log.log(Level.SEVERE, "requireDataSource: datasource is null", ex);
            throw new SQLException("DataSource is not initialized", ex);
        }
        return ds;
    }

    private static DataSource getDataSource() {
        AppDataSourceHolder holder = dsHolder;
        if (holder == null) {
            log.fine("getDataSource: dsHolder null, resolving via CDI");
            try {
                holder = CDI.current().select(AppDataSourceHolder.class).get();
                dsHolder = holder;
            } catch (Exception e) {
                log.log(Level.WARNING, "getDataSource: CDI lookup failed for AppDataSourceHolder", e);
                return null;
            }
        }
        if (holder == null) {
            log.severe("getDataSource: holder is null after CDI lookup");
            return null;
        }

        try {
            DataSource ds = holder.getDataSource();
            if (ds == null) {
                log.severe("getDataSource: holder returned null DataSource");
            } else {
                log.fine("getDataSource: datasource acquired");
            }
            return ds;
        } catch (Exception e) {
            log.log(Level.SEVERE, "getDataSource: failed to get datasource from holder", e);
            return null;
        }
    }
}
