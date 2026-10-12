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
import java.util.Objects;
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

    private static final Logger log = Logger.getLogger(EncryptedDbConfigStore.class.getCanonicalName());

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
    private static final String EMPTY = "";
    private static final String VALUE_NULL_TEXT = "null";
    private static final String ALGO_AES = "AES";
    private static final String ALGO_PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
    private static final String AES_GCM_NOPADDING = "/GCM/NoPadding";
    private static final String DEFAULT_KDF_SALT = "sim-config-store-kdf-salt-v1";

    private static final String COL_SERVER_HOST = "server_host";
    private static final String COL_SERVER_PORT = "server_port";
    private static final String COL_CONNECTION_INFO = "connection_info";
    private static final String COL_API_KEY = "api_key";
    private static final String COL_WORKSPACE_NAME = "workspace_name";
    private static final String COL_SALESFORCE_INSTANCE_URL = "salesforce_instance_url";
    private static final String COL_SALESFORCE_API_KEY = "salesforce_api_key";
    private static final String COL_SALESFORCE_LOGIN_URL = "salesforce_login_url";
    private static final String COL_SALESFORCE_CLIENT_ID = "salesforce_client_id";
    private static final String COL_SALESFORCE_CLIENT_SECRET = "salesforce_client_secret";
    private static final String COL_SALESFORCE_REFRESH_TOKEN = "salesforce_refresh_token";
    private static final String COL_SALESFORCE_USERNAME = "salesforce_username";
    private static final String COL_SALESFORCE_PASSWORD = "salesforce_password";
    private static final String COL_SALESFORCE_API_TOKEN = "salesforce_api_token";
    private static final String COL_AWS_REGION = "aws_region";
    private static final String COL_AWS_INSTANCE_ID = "aws_instance_id";
    private static final String COL_AWS_ACCESS_KEY_ID = "aws_access_key_id";
    private static final String COL_AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";

    private static final String MSG_SET_DATASOURCE_CALLED = "setAppDataSourceHolder called. dataSource={0}";
    private static final String MSG_ENSURE_TABLE_START = "ensureTable: start";
    private static final String MSG_ENSURE_TABLE_EXECUTED = "ensureTable: create table statement executed";
    private static final String MSG_ENSURE_TABLE_SQL_FAILED = "ensureTable: failed executing create table SQL";
    private static final String MSG_ENSURE_TABLE_COLUMNS_COMPLETE = "ensureTable: column checks complete";
    private static final String MSG_ENSURE_TABLE_COLUMNS_FAILED = "ensureTable: failed during column checks";
    private static final String MSG_LOAD_START = "load: start";
    private static final String MSG_LOAD_ROW_FOUND = "load: row found in server_config";
    private static final String MSG_LOAD_SUCCESS = "load: config loaded successfully";
    private static final String MSG_LOAD_SQL_FAILURE = "load: SQL failure";
    private static final String MSG_LOAD_NO_ROWS = "load: no rows found, returning new ServerConfig()";
    private static final String MSG_SAVE_START = "save: start";
    private static final String MSG_SAVE_DELETED_COUNT_PREFIX = "save: deleted existing rows count=";
    private static final String MSG_SAVE_DELETE_FAILED = "save: failed deleting old config rows";
    private static final String MSG_SAVE_INSERT_COMPLETE_PREFIX = "save: insert complete, rows=";
    private static final String MSG_SAVE_INSERT_FAILED = "save: failed inserting config row";
    private static final String MSG_SAVE_WORKSPACE_START = "saveWorkspaceName: start";
    private static final String MSG_SAVE_WORKSPACE_COMPLETE = "saveWorkspaceName: complete";
    private static final String MSG_ENSURE_COLUMN_ADDING = "ensureColumn: adding missing column {0} {1}";
    private static final String MSG_ENSURE_COLUMN_EXISTS = "ensureColumn: exists -> {0}";
    private static final String SQL_ALTER_TABLE_PREFIX = "ALTER TABLE ";
    private static final String SQL_ADD_COLUMN = " ADD COLUMN ";
    private static final String MSG_ENCRYPT_VALUE_ALREADY = "encryptIfPresent: value already encrypted, skipping";
    private static final String MSG_ENCRYPT_KEY_RESOLUTION_FAILED = "encryptIfPresent: encryption key resolution failed";
    private static final String MSG_ENCRYPT_FAILED = "encryptIfPresent: encryption failed";
    private static final String MSG_INVALID_ENCRYPTED_FORMAT = "Invalid encrypted configuration format";
    private static final String MSG_DECRYPT_KEY_RESOLUTION_FAILED = "decryptIfNeeded: encryption key resolution failed";
    private static final String MSG_DECRYPT_FAILED = "decryptIfNeeded: decryption failed for encrypted value";
    private static final String PREFIX_ENV_VAR_REQUIRED = "Environment variable ";
    private static final String MSG_REQUIRED_FOR_CONFIG_ENCRYPTION = " is required for config encryption.";
    private static final String MSG_AES_KEY_MISSING_PREFIX = "getAesKeyBytes: missing ";
    private static final String MSG_AES_KEY_USING_BASE64_PREFIX = "getAesKeyBytes: using Base64-decoded AES key length=";
    private static final String MSG_AES_KEY_BASE64_LEN_PREFIX = "getAesKeyBytes: Base64 decoded length=";
    private static final String MSG_AES_KEY_BASE64_LEN_SUFFIX = ", deriving AES-256 key via PBKDF2";
    private static final String MSG_AES_KEY_NOT_BASE64 = "getAesKeyBytes: env not Base64, deriving AES-256 key via PBKDF2 of UTF-8 string";
    private static final String MSG_AES_KEY_DERIVATION_FAILED = "getAesKeyBytes: key derivation failed";
    private static final String MSG_UNSUPPORTED_PREFIX = "Unsupported ";
    private static final String MSG_UNSUPPORTED_TRANSFORM = " value, using default transformation.";
    private static final String MSG_ENV_TOO_LONG_PREFIX = "Environment variable too long: ";
    private static final String MSG_ENV_CONTROL_CHARS_PREFIX = "Environment variable contains invalid control characters: ";
    private static final String MSG_REQUIRE_DATASOURCE_FAILED = "requireDataSource: datasource resolution failed";
    private static final String MSG_DATASOURCE_NOT_INITIALIZED = "DataSource is not initialized";
    private static final String MSG_GET_DATASOURCE_RESOLVING_CDI = "getDataSourceOrThrow: configuredDataSource null, resolving via CDI";
    private static final String MSG_CDI_LOOKUP_FAILED = "CDI lookup failed for AppDataSourceHolder";
    private static final String MSG_GET_DATASOURCE_ACQUIRED = "getDataSourceOrThrow: datasource acquired";
    private static final String SQL_TYPE_TEXT = "TEXT";
    private static final String MSG_UNABLE_TO_ENCRYPT_CONFIG = "Unable to encrypt configuration value";
    private static final String MSG_UNABLE_TO_DECRYPT_CONFIG = "Unable to decrypt configuration value";
    private static final String MSG_UNABLE_TO_DERIVE_KEY = "Unable to derive encryption key";
    private static final String MSG_FAILED_GET_DATASOURCE = "Failed to get DataSource from AppDataSourceHolder";
    private static final String MSG_NULL_DATASOURCE = "AppDataSourceHolder returned null DataSource";
    private static final String DEFAULT_AES_MODE = buildDefaultCipherTransformation();
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int AES_KEY_BYTES = 32; // AES-256
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static volatile DataSource configuredDataSource;

    private EncryptedDbConfigStore() {
    }

    public static void setAppDataSourceHolder(AppDataSourceHolder holder) {
        DataSource resolvedDataSource = null;
        if (holder != null) {
            try {
                resolvedDataSource = Objects.requireNonNull(
                        holder.getDataSource(),
                        MSG_NULL_DATASOURCE);
            } catch (IllegalStateException | IllegalArgumentException e) {
                throw new IllegalStateException(MSG_FAILED_GET_DATASOURCE, e);
            }
        }
        configuredDataSource = resolvedDataSource;
        log.log(Level.INFO, MSG_SET_DATASOURCE_CALLED,
            resolvedDataSource == null ? VALUE_NULL_TEXT : resolvedDataSource.getClass().getCanonicalName());
    }

    public static void ensureTable() throws SQLException {
        log.fine(MSG_ENSURE_TABLE_START);

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.execute();
            log.fine(MSG_ENSURE_TABLE_EXECUTED);
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_ENSURE_TABLE_SQL_FAILED, e);
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
            log.fine(MSG_ENSURE_TABLE_COLUMNS_COMPLETE);
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_ENSURE_TABLE_COLUMNS_FAILED, e);
            throw e;
        }
    }

    public static ServerConfig load() throws SQLException {
        log.fine(MSG_LOAD_START);
        ensureTable();

        DataSource ds = requireDataSource();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                log.fine(MSG_LOAD_ROW_FOUND);
                ServerConfig config = new ServerConfig();
                config.setServerHost(rs.getString(COL_SERVER_HOST));
                config.setServerPort(rs.getInt(COL_SERVER_PORT));
                config.setConnectionInfo(rs.getString(COL_CONNECTION_INFO));

                config.setApiKey(decryptIfNeeded(rs.getString(COL_API_KEY)));
                config.setWorkspaceName(rs.getString(COL_WORKSPACE_NAME));

                config.setSalesforceInstanceUrl(rs.getString(COL_SALESFORCE_INSTANCE_URL));
                config.setSalesforceApiKey(decryptIfNeeded(rs.getString(COL_SALESFORCE_API_KEY)));

                config.setSalesforceLoginUrl(rs.getString(COL_SALESFORCE_LOGIN_URL));
                config.setSalesforceClientId(decryptIfNeeded(rs.getString(COL_SALESFORCE_CLIENT_ID)));
                config.setSalesforceClientSecret(decryptIfNeeded(rs.getString(COL_SALESFORCE_CLIENT_SECRET)));
                config.setSalesforceRefreshToken(decryptIfNeeded(rs.getString(COL_SALESFORCE_REFRESH_TOKEN)));
                config.setSalesforceUsername(rs.getString(COL_SALESFORCE_USERNAME));
                config.setSalesforcePassword(decryptIfNeeded(rs.getString(COL_SALESFORCE_PASSWORD)));
                config.setSalesforceApiToken(decryptIfNeeded(rs.getString(COL_SALESFORCE_API_TOKEN)));
                config.setAwsRegion(rs.getString(COL_AWS_REGION));
                config.setAwsInstanceId(rs.getString(COL_AWS_INSTANCE_ID));
                config.setAwsAccessKeyId(decryptIfNeeded(rs.getString(COL_AWS_ACCESS_KEY_ID)));
                config.setAwsSecretAccessKey(decryptIfNeeded(rs.getString(COL_AWS_SECRET_ACCESS_KEY)));

                log.fine(MSG_LOAD_SUCCESS);
                return config;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_LOAD_SQL_FAILURE, e);
            throw e;
        }

        log.fine(MSG_LOAD_NO_ROWS);
        return new ServerConfig();
    }

    public static void save(ServerConfig config) throws SQLException {
        log.fine(MSG_SAVE_START);
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
            log.fine(() -> MSG_SAVE_DELETED_COUNT_PREFIX + deleted);
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_SAVE_DELETE_FAILED, e);
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
            log.fine(() -> MSG_SAVE_INSERT_COMPLETE_PREFIX + inserted);
        } catch (SQLException e) {
            log.log(Level.SEVERE, MSG_SAVE_INSERT_FAILED, e);
            throw e;
        }
    }

    public static void saveWorkspaceName(String workspaceName) throws SQLException {
        log.fine(MSG_SAVE_WORKSPACE_START);
        ServerConfig config = load();
        config.setWorkspaceName(workspaceName);
        save(config);
        log.fine(MSG_SAVE_WORKSPACE_COMPLETE);
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
        ensureColumn(conn, COL_WORKSPACE_NAME, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceInstanceUrlColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_INSTANCE_URL, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceApiKeyColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_API_KEY, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceLoginUrlColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_LOGIN_URL, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceClientIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_CLIENT_ID, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceClientSecretColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_CLIENT_SECRET, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceRefreshTokenColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_REFRESH_TOKEN, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceUsernameColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_USERNAME, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforcePasswordColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_PASSWORD, SQL_TYPE_TEXT);
    }

    private static void ensureSalesforceApiTokenColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_SALESFORCE_API_TOKEN, SQL_TYPE_TEXT);
    }

    private static void ensureAwsRegionColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_AWS_REGION, SQL_TYPE_TEXT);
    }

    private static void ensureAwsInstanceIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_AWS_INSTANCE_ID, SQL_TYPE_TEXT);
    }

    private static void ensureAwsAccessKeyIdColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_AWS_ACCESS_KEY_ID, SQL_TYPE_TEXT);
    }

    private static void ensureAwsSecretAccessKeyColumn(Connection conn) throws SQLException {
        ensureColumn(conn, COL_AWS_SECRET_ACCESS_KEY, SQL_TYPE_TEXT);
    }

    private static void ensureColumn(Connection conn, String columnName, String sqlType) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!columns.next()) {
                log.log(Level.INFO, MSG_ENSURE_COLUMN_ADDING, new Object[]{columnName, sqlType});
                try (PreparedStatement alter = conn.prepareStatement(
                        new StringBuilder(64)
                                .append(SQL_ALTER_TABLE_PREFIX)
                                .append(TABLE_NAME)
                                .append(SQL_ADD_COLUMN)
                                .append(columnName)
                                .append(' ')
                                .append(sqlType)
                                .toString())) {
                    alter.execute();
                }
            } else {
                log.log(Level.FINE, MSG_ENSURE_COLUMN_EXISTS, columnName);
            }
        }
    }

    private static String encryptIfPresent(String plainValue) throws SQLException {
        if (isNullOrBlank(plainValue)) {
            return plainValue == null ? null : EMPTY;
        }
        if (isEncryptedValue(plainValue)) {
            log.fine(MSG_ENCRYPT_VALUE_ALREADY);
            return plainValue;
        }
        try {
            byte[] key = getAesKeyBytes();
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = newCipher();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGO_AES), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String encB64 = Base64.getEncoder().encodeToString(encrypted);
            return ENC_PREFIX + ivB64 + (char) 58 + encB64;
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, MSG_ENCRYPT_KEY_RESOLUTION_FAILED, e);
            throw new SQLException(MSG_UNABLE_TO_ENCRYPT_CONFIG, e);
        } catch (GeneralSecurityException e) {
            log.log(Level.SEVERE, MSG_ENCRYPT_FAILED, e);
            throw new SQLException(MSG_UNABLE_TO_ENCRYPT_CONFIG, e);
        }
    }

    private static String decryptIfNeeded(String storedValue) throws SQLException {
        if (isNullOrBlank(storedValue)) {
            return storedValue;
        }
        if (!isEncryptedValue(storedValue)) {
            return storedValue;
        }

        try {
            String payload = storedValue.substring(ENC_PREFIX.length());
            String[] parts = payload.split(String.valueOf((char) 58), 2);
            if (parts.length != 2) {
                throw new SQLException(MSG_INVALID_ENCRYPTED_FORMAT);
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] enc = Base64.getDecoder().decode(parts[1]);

            byte[] key = getAesKeyBytes();
            Cipher cipher = newCipher();
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGO_AES), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(enc);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, MSG_DECRYPT_KEY_RESOLUTION_FAILED, e);
            throw new SQLException(MSG_UNABLE_TO_DECRYPT_CONFIG, e);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.log(Level.SEVERE, MSG_DECRYPT_FAILED, e);
            throw new SQLException(MSG_UNABLE_TO_DECRYPT_CONFIG, e);
        }
    }

    private static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isEncryptedValue(String value) {
        return value != null && value.startsWith(ENC_PREFIX);
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
                PREFIX_ENV_VAR_REQUIRED + ENC_KEY_ENV + MSG_REQUIRED_FOR_CONFIG_ENCRYPTION);
            log.log(Level.SEVERE, MSG_AES_KEY_MISSING_PREFIX + ENC_KEY_ENV, ex);
            throw ex;
        }

        String trimmed = secret.trim();
        byte[] salt = resolveKdfSalt();

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);

            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
                log.fine(() -> MSG_AES_KEY_USING_BASE64_PREFIX + decoded.length);
                return decoded;
            }

            log.fine(() -> MSG_AES_KEY_BASE64_LEN_PREFIX + decoded.length + MSG_AES_KEY_BASE64_LEN_SUFFIX);
            return deriveAesKey(decoded, salt);

        } catch (IllegalArgumentException notBase64) {
            log.fine(MSG_AES_KEY_NOT_BASE64);
            return deriveAesKey(trimmed.getBytes(StandardCharsets.UTF_8), salt);
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, MSG_AES_KEY_DERIVATION_FAILED, e);
            throw new IllegalStateException(MSG_UNABLE_TO_DERIVE_KEY, e);
        }
    }

    private static byte[] deriveAesKey(byte[] keyMaterial, byte[] salt) {
        String material = Base64.getEncoder().encodeToString(keyMaterial);
        PBEKeySpec spec = new PBEKeySpec(material.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BYTES * 8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGO_PBKDF2_HMAC_SHA256);
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            throw new IllegalStateException(MSG_UNABLE_TO_DERIVE_KEY, e);
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
        log.warning(MSG_UNSUPPORTED_PREFIX + ENC_TRANSFORM_ENV + MSG_UNSUPPORTED_TRANSFORM);
        return DEFAULT_AES_MODE;
    }

    private static String buildDefaultCipherTransformation() {
        return new StringBuilder(32).append(ALGO_AES).append(AES_GCM_NOPADDING).toString();
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
            throw new IllegalStateException(MSG_ENV_TOO_LONG_PREFIX + envName);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new IllegalStateException(MSG_ENV_CONTROL_CHARS_PREFIX + envName);
        }
        return normalized;
    }

    private static DataSource requireDataSource() throws SQLException {
        try {
            return getDataSourceOrThrow();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, MSG_REQUIRE_DATASOURCE_FAILED, e);
            throw new SQLException(MSG_DATASOURCE_NOT_INITIALIZED, e);
        }
    }

    private static DataSource getDataSourceOrThrow() {
        DataSource local = configuredDataSource;
        if (local != null) {
            return local;
        }

        AppDataSourceHolder holder;
        log.fine(MSG_GET_DATASOURCE_RESOLVING_CDI);
        try {
            holder = CDI.current().select(AppDataSourceHolder.class).get();
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new IllegalStateException(MSG_CDI_LOOKUP_FAILED, e);
        }

        try {
            DataSource ds = Objects.requireNonNull(
                    holder.getDataSource(),
                    MSG_NULL_DATASOURCE);
            configuredDataSource = ds;
            log.fine(MSG_GET_DATASOURCE_ACQUIRED);
            return ds;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new IllegalStateException(MSG_FAILED_GET_DATASOURCE, e);
        }
    }
}
