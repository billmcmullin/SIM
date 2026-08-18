package com.sim.chatserver.config;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
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
            + "salesforce_refresh_token TEXT, "
            + "salesforce_username TEXT, "
            + "salesforce_password TEXT, "
            + "salesforce_api_token TEXT, "
            + "aws_region TEXT, "
            + "aws_instance_id TEXT, "
            + "aws_access_key_id TEXT, "
            + "aws_secret_access_key TEXT)";
    private static final String SELECT_SQL
            = "SELECT server_host, server_port, connection_info, api_key, workspace_name, "
            + "salesforce_instance_url, salesforce_api_key, "
            + "salesforce_login_url, salesforce_client_id, salesforce_client_secret, salesforce_refresh_token, "
            + "salesforce_username, salesforce_password, salesforce_api_token, "
            + "aws_region, aws_instance_id, aws_access_key_id, aws_secret_access_key "
            + "FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 1";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME;
    private static final String INSERT_SQL
            = "INSERT INTO " + TABLE_NAME
            + " (server_host, server_port, connection_info, api_key, workspace_name, "
            + "salesforce_instance_url, salesforce_api_key, salesforce_login_url, salesforce_client_id, salesforce_client_secret, salesforce_refresh_token, "
            + "salesforce_username, salesforce_password, salesforce_api_token, "
            + "aws_region, aws_instance_id, aws_access_key_id, aws_secret_access_key) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String ENC_KEY_ENV = "CONFIG_ENCRYPTION_KEY";
    private static final String ENC_SALT_ENV = "CONFIG_ENCRYPTION_SALT";
    private static final String ENC_TRANSFORM_ENV = "CONFIG_ENCRYPTION_TRANSFORMATION";
    private static final String ENC_PREFIX = "ENCv1:";
    private static final String DEFAULT_AES_MODE = buildDefaultCipherTransformation();
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int AES_KEY_BYTES = 32; // AES-256
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static volatile AppDataSourceHolder dsHolder;

    private EncryptedDbConfigStore() {
    }

    public static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        dsHolder = holder;
        log.log(Level.INFO, "setAppDataSourceHolder called. holder={0}",
            holder == null ? "null" : holder.getClass().getName());
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
            ensureSalesforceUsernameColumn(conn);
            ensureSalesforcePasswordColumn(conn);
            ensureSalesforceApiTokenColumn(conn);
            ensureAwsRegionColumn(conn);
            ensureAwsInstanceIdColumn(conn);
            ensureAwsAccessKeyIdColumn(conn);
            ensureAwsSecretAccessKeyColumn(conn);
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

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {

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
                config.setSalesforceUsername(rs.getString("salesforce_username"));
                config.setSalesforcePassword(decryptIfNeeded(rs.getString("salesforce_password")));
                config.setSalesforceApiToken(decryptIfNeeded(rs.getString("salesforce_api_token")));
                config.setAwsRegion(rs.getString("aws_region"));
                config.setAwsInstanceId(rs.getString("aws_instance_id"));
                config.setAwsAccessKeyId(decryptIfNeeded(rs.getString("aws_access_key_id")));
                config.setAwsSecretAccessKey(decryptIfNeeded(rs.getString("aws_secret_access_key")));

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
        String encryptedSalesforcePassword = encryptIfPresent(config != null ? config.getSalesforcePassword() : null);
        String encryptedSalesforceApiToken = encryptIfPresent(config != null ? config.getSalesforceApiToken() : null);
        String encryptedAwsAccessKeyId = encryptIfPresent(config != null ? config.getAwsAccessKeyId() : null);
        String encryptedAwsSecretAccessKey = encryptIfPresent(config != null ? config.getAwsSecretAccessKey() : null);

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection(); PreparedStatement deleteStmt = conn.prepareStatement(DELETE_SQL)) {
            int deleted = deleteStmt.executeUpdate();
            log.log(Level.FINE, "save: deleted existing rows count={0}", deleted);
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
            insertStmt.setString(12, config != null ? config.getSalesforceUsername() : null);
            insertStmt.setString(13, encryptedSalesforcePassword);
            insertStmt.setString(14, encryptedSalesforceApiToken);
            insertStmt.setString(15, config != null ? config.getAwsRegion() : null);
            insertStmt.setString(16, config != null ? config.getAwsInstanceId() : null);
            insertStmt.setString(17, encryptedAwsAccessKeyId);
            insertStmt.setString(18, encryptedAwsSecretAccessKey);

            int inserted = insertStmt.executeUpdate();
            log.log(Level.FINE, "save: insert complete, rows={0}", inserted);
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

    /**
     * Encrypts a secret value using the same ENCv1 format used by server_config.
     */
    public static String encryptSecretForStorage(String plainValue) throws SQLException {
        return encryptIfPresent(plainValue);
    }

    /**
     * Decrypts an ENCv1 secret value; returns input unchanged if it is plaintext.
     */
    public static String decryptSecretIfNeeded(String storedValue) throws SQLException {
        return decryptIfNeeded(storedValue);
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

    private static void ensureSalesforceUsernameColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_username", "TEXT");
    }

    private static void ensureSalesforcePasswordColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_password", "TEXT");
    }

    private static void ensureSalesforceApiTokenColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "salesforce_api_token", "TEXT");
    }

    private static void ensureAwsRegionColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "aws_region", "TEXT");
    }

    private static void ensureAwsInstanceIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "aws_instance_id", "TEXT");
    }

    private static void ensureAwsAccessKeyIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "aws_access_key_id", "TEXT");
    }

    private static void ensureAwsSecretAccessKeyColumn(Connection conn) throws SQLException {
        ensureColumn(conn, "aws_secret_access_key", "TEXT");
    }

    private static void ensureColumn(Connection conn, String columnName, String sqlType) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!columns.next()) {
                log.log(Level.INFO, "ensureColumn: adding missing column {0} {1}", new Object[]{columnName, sqlType});
                try (PreparedStatement alter = conn.prepareStatement(
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + sqlType)) {
                    alter.execute();
                }
            } else {
                log.log(Level.FINE, "ensureColumn: exists -> {0}", columnName);
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

            Cipher cipher = newCipher();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String encB64 = Base64.getEncoder().encodeToString(encrypted);
            return ENC_PREFIX + ivB64 + ":" + encB64;
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "encryptIfPresent: encryption key resolution failed", e);
            throw new SQLException("Unable to encrypt configuration value", e);
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
            Cipher cipher = newCipher();
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "decryptIfNeeded: encryption key resolution failed", e);
            throw new SQLException("Unable to decrypt configuration value", e);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "decryptIfNeeded: decryption failed for encrypted value", e);
            throw new SQLException("Unable to decrypt configuration value", e);
        }
    }

    /**
     * Derives an AES key from CONFIG_ENCRYPTION_KEY:
     * - Preferred: Base64-encoded AES key bytes (16/24/32 bytes)
     * - Fallback: SHA-256 derived key (32 bytes)
     */
    private static byte[] getAesKeyBytes() {
        String secret = readEnvCanonical(ENC_KEY_ENV, 4096);
        if (secret == null) {
            IllegalStateException ex = new IllegalStateException(
                    "Environment variable " + ENC_KEY_ENV + " is required for config encryption.");
            log.log(Level.SEVERE, "getAesKeyBytes: missing " + ENC_KEY_ENV, ex);
            throw ex;
        }

        String trimmed = secret.trim();
        byte[] salt = resolveKdfSalt();

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);

            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
                log.log(Level.FINE, "getAesKeyBytes: using Base64-decoded AES key length={0}", decoded.length);
                return decoded;
            }

            log.log(Level.FINE, "getAesKeyBytes: Base64 decoded length={0}, deriving AES-256 key via PBKDF2", decoded.length);
            return deriveAesKey(decoded, salt);

        } catch (IllegalArgumentException notBase64) {
            log.fine("getAesKeyBytes: env not Base64, deriving AES-256 key via PBKDF2 of UTF-8 string");
            return deriveAesKey(trimmed.getBytes(StandardCharsets.UTF_8), salt);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "getAesKeyBytes: key derivation failed", e);
            throw new IllegalStateException("Unable to derive encryption key", e);
        }
    }

    private static byte[] deriveAesKey(byte[] keyMaterial, byte[] salt) {
        String material = Base64.getEncoder().encodeToString(keyMaterial);
        PBEKeySpec spec = new PBEKeySpec(material.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BYTES * 8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to derive encryption key", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] resolveKdfSalt() {
        String configuredSalt = readEnvCanonical(ENC_SALT_ENV, 1024);
        if (configuredSalt != null) {
            return configuredSalt.getBytes(StandardCharsets.UTF_8);
        }
        return "sim-config-store-kdf-salt-v1".getBytes(StandardCharsets.UTF_8);
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
        log.warning("Unsupported " + ENC_TRANSFORM_ENV + " value, using default transformation.");
        return DEFAULT_AES_MODE;
    }

    private static String buildDefaultCipherTransformation() {
        return new StringBuilder(32).append("AES").append("/GCM/NoPadding").toString();
    }

    private static String readEnvCanonical(String envName, int maxChars) {
        String raw = new ProcessBuilder().environment().get(envName);
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

    private static DataSource requireDataSource() throws SQLException {
        try {
            return getDataSourceOrThrow();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "requireDataSource: datasource resolution failed", e);
            throw new SQLException("DataSource is not initialized", e);
        }
    }

    private static DataSource getDataSourceOrThrow() {
        AppDataSourceHolder holder = dsHolder;
        if (holder == null) {
            log.fine("getDataSourceOrThrow: dsHolder null, resolving via CDI");
            try {
                holder = CDI.current().select(AppDataSourceHolder.class).get();
                dsHolder = holder;
            } catch (IllegalStateException | IllegalArgumentException e) {
                throw new IllegalStateException("CDI lookup failed for AppDataSourceHolder", e);
            }
        }

        try {
            DataSource ds = holder.getDataSource();
            if (ds == null) {
                throw new IllegalStateException("AppDataSourceHolder returned null DataSource");
            }
            log.fine("getDataSourceOrThrow: datasource acquired");
            return ds;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to get DataSource from AppDataSourceHolder", e);
        }
    }
}
