package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.security.email.EmailSecretCrypto;

import jakarta.enterprise.context.ApplicationScoped;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Plain JDBC implementation for email_smtp_config single-row table (id=1),
 * using direct PostgreSQL connection from environment variables.
 *
 * Expected table columns:
 * id INT PRIMARY KEY
 * host VARCHAR(255) NOT NULL
 * port INT NOT NULL
 * auth BOOLEAN NOT NULL
 * starttls BOOLEAN NOT NULL
 * ssl BOOLEAN NOT NULL
 * username VARCHAR(255)
 * password_enc TEXT
 * default_from VARCHAR(255)
 * updated_by VARCHAR(100)
 * updated_at TIMESTAMP NOT NULL
 *
 * Required env vars:
 * DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
 */
@ApplicationScoped
public class DbEmailConfigProviderImpl implements DbEmailConfigProvider {

    private static final Logger log = Logger.getLogger(DbEmailConfigProviderImpl.class.getCanonicalName());
    private static final int SINGLE_ROW_ID = 1;
    private static final int DEFAULT_DB_PORT_INT = 5432;
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String EMPTY = "";
    private static final String SQL_LOAD = """
            SELECT host, port, auth, starttls, ssl, username, password_enc, default_from
            FROM email_smtp_config
            WHERE id = ?
            """;
    private static final String SQL_UPDATE = """
            UPDATE email_smtp_config
            SET host = ?, port = ?, auth = ?, starttls = ?, ssl = ?,
                username = ?, password_enc = ?, default_from = ?, updated_by = ?, updated_at = ?
            WHERE id = ?
            """;
    private static final String SQL_INSERT = """
            INSERT INTO email_smtp_config
            (id, host, port, auth, starttls, ssl, username, password_enc, default_from, updated_by, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String COL_HOST = "host";
    private static final String COL_PORT = "port";
    private static final String COL_AUTH = "auth";
    private static final String COL_STARTTLS = "starttls";
    private static final String COL_SSL = "ssl";
    private static final String COL_USERNAME = com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME;
    private static final String COL_PASSWORD_ENC = "password_enc";
    private static final String COL_DEFAULT_FROM = "default_from";
    private static final String ERR_LOAD_SMTP_CONFIG = "Failed to load SMTP config from DB";
    private static final String ERR_CONFIG_NULL = "config must not be null";
    private static final String ERR_SAVE_SMTP_CONFIG = "Failed to save SMTP config";
    private static final String ENV_DB_HOST = "DB_HOST";
    private static final String ENV_DB_PORT = "DB_PORT";
    private static final String ENV_DB_NAME = "DB_NAME";
    private static final String ENV_DB_USER = "DB_USER";
    private static final String ENV_DB_PASSWORD = "DB_PASSWORD";
    private static final String DEF_DB_HOST = com.sim.chatserver.util.CommonLiteralConstants.HOST_LOCALHOST;
    private static final String DEF_DB_PORT = "5432";
    private static final String DEF_DB_NAME = com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String DEF_DB_USER = "postgres";
    private static final String LOG_INVALID_DB_PORT = "Invalid DB_PORT value; using fallback";
    private static final String LOG_ROLLBACK_FAILED = "Rollback failed";
    private static final String LOG_SET_AUTOCOMMIT_FAILED = "setAutoCommit failed";
    private static final String ERR_SMTP_HOST_REQUIRED = "SMTP host is required";
    private static final String ERR_SMTP_PORT_RANGE = "SMTP port must be between 1 and 65535";
    private static final String NUL_CHAR = com.sim.chatserver.util.CommonLiteralConstants.NULL_CHAR_STRING;
    private static final int SECRET_MAX_LENGTH = 1024;
    private static final Pattern SAFE_DB_HOST = Pattern.compile("^[A-Za-z0-9.-]{1,255}$");
    private static final Pattern SAFE_DB_PORT = Pattern.compile("^\\d{1,5}$");
    private static final Pattern SAFE_DB_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
    private static final Pattern SAFE_DB_USER = Pattern.compile("^[A-Za-z0-9_.@-]{1,128}$");
    private static final java.util.Map<String, String> ENV = new ProcessBuilder().environment();

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @Override
    public EmailConfig load() {
        try (Connection con = newConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LOAD)) {

            ps.setInt(1, SINGLE_ROW_ID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String host = trimToEmpty(rs.getString(COL_HOST));
                int port = rs.getInt(COL_PORT);
                boolean auth = rs.getBoolean(COL_AUTH);
                boolean startTls = rs.getBoolean(COL_STARTTLS);
                boolean ssl = rs.getBoolean(COL_SSL);
                String username = trimToEmpty(rs.getString(COL_USERNAME));
                String passwordEnc = trimToEmpty(rs.getString(COL_PASSWORD_ENC));
                String defaultFrom = trimToEmpty(rs.getString(COL_DEFAULT_FROM));

                String password = decrypt(passwordEnc);

                return new EmailConfig(
                        host,
                        port,
                        auth,
                        startTls,
                        ssl,
                        username,
                        password,
                        defaultFrom
                );
            }
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, ERR_LOAD_SMTP_CONFIG, e);
            return null;
        }
    }

    @Override
    public void save(EmailConfig config, String updatedBy) {
        if (config == null) {
            throw new IllegalArgumentException(ERR_CONFIG_NULL);
        }

        validate(config);

        // If incoming password is blank, retain existing password via interface helper.
        EmailConfig merged = mergeKeepingExistingPasswordIfBlank(config);

        String encPassword = encrypt(nullToEmpty(merged.password()));
        Timestamp now = Timestamp.from(Instant.now());
        String actor = trimToEmpty(updatedBy);

        try (Connection con = newConnection()) {
            con.setAutoCommit(false);

            try {
                int updated;
                try (PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {
                    bindCommonForUpdate(ps, merged, encPassword, actor, now);
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    try (PreparedStatement ps = con.prepareStatement(SQL_INSERT)) {
                        bindCommonForInsert(ps, merged, encPassword, actor, now);
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException e) {
                rollbackQuietly(con);
                throw new IllegalStateException(ERR_SAVE_SMTP_CONFIG, e);
            } catch (IllegalStateException | IllegalArgumentException e) {
                rollbackQuietly(con);
                throw new IllegalStateException(ERR_SAVE_SMTP_CONFIG, e);
            } finally {
                setAutoCommitQuietly(con, true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(ERR_SAVE_SMTP_CONFIG, e);
        }
    }

    private Connection newConnection() throws SQLException {
        String host = envOrDefault(ENV_DB_HOST, DEF_DB_HOST, SAFE_DB_HOST, 255);
        String portRaw = envOrDefault(ENV_DB_PORT, DEF_DB_PORT, SAFE_DB_PORT, 5);
        String db = envOrDefault(ENV_DB_NAME, DEF_DB_NAME, SAFE_DB_NAME, 128);
        String user = envOrDefault(ENV_DB_USER, DEF_DB_USER, SAFE_DB_USER, 128);
        String pass = envSecretOrDefault(ENV_DB_PASSWORD, EMPTY);

        int port;
        try {
            port = Integer.parseInt(portRaw);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, LOG_INVALID_DB_PORT, ex);
            port = DEFAULT_DB_PORT_INT;
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{host});
        dataSource.setPortNumbers(new int[]{port});
        dataSource.setDatabaseName(db);
        dataSource.setUser(user);
        dataSource.setPassword(pass);
        return dataSource.getConnection();
    }

    private String envOrDefault(String key, String def, Pattern allowed, int maxLen) {
        String v = readEnv(key);
        if (v == null || v.isBlank()) {
            return def;
        }
        String normalized = stripControls(v).trim();
        if (normalized.isEmpty() || normalized.length() > maxLen) {
            return def;
        }
        if (allowed != null && !allowed.matcher(normalized).matches()) {
            return def;
        }
        return normalized;
    }

    private String envSecretOrDefault(String key, String def) {
        String v = readEnv(key);
        if (v == null) {
            return def;
        }
        String normalized = stripControls(v);
        if (normalized.length() > SECRET_MAX_LENGTH) {
            return new String(normalized.toCharArray(), 0, SECRET_MAX_LENGTH);
        }
        return normalized;
    }

    private String stripControls(String value) {
        if (value == null) {
            return EMPTY;
        }
        return value.replace(NUL_CHAR, EMPTY)
                .replace("\r", EMPTY)
                .replace("\n", EMPTY);
    }

    private String readEnv(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return ENV.get(key);
    }

    private void bindCommonForUpdate(PreparedStatement ps, EmailConfig c, String encPassword, String updatedBy, Timestamp now)
            throws SQLException {
        bindSmtpFields(ps, 1, c, encPassword);
        bindAuditFields(ps, 9, updatedBy, now);
        ps.setInt(11, SINGLE_ROW_ID);
    }

    private void bindCommonForInsert(PreparedStatement ps, EmailConfig c, String encPassword, String updatedBy, Timestamp now)
            throws SQLException {
        ps.setInt(1, SINGLE_ROW_ID);
        bindSmtpFields(ps, 2, c, encPassword);
        bindAuditFields(ps, 10, updatedBy, now);
    }

    private void bindSmtpFields(PreparedStatement ps, int startIndex, EmailConfig c, String encPassword)
            throws SQLException {
        ps.setString(startIndex, c.host());
        ps.setInt(startIndex + 1, c.port());
        ps.setBoolean(startIndex + 2, c.auth());
        ps.setBoolean(startIndex + 3, c.startTls());
        ps.setBoolean(startIndex + 4, c.ssl());
        ps.setString(startIndex + 5, nullToEmpty(c.username()));
        ps.setString(startIndex + 6, encPassword);
        ps.setString(startIndex + 7, nullToEmpty(c.defaultFrom()));
    }

    private void bindAuditFields(PreparedStatement ps, int startIndex, String updatedBy, Timestamp now)
            throws SQLException {
        ps.setString(startIndex, updatedBy);
        ps.setTimestamp(startIndex + 1, now);
    }

    private void validate(EmailConfig c) {
        if (!hasText(c.host())) {
            throw new IllegalArgumentException(ERR_SMTP_HOST_REQUIRED);
        }
        if (c.port() < 1 || c.port() > 65535) {
            throw new IllegalArgumentException(ERR_SMTP_PORT_RANGE);
        }
    }

    /**
     * Encrypt SMTP password using AES-GCM utility.
     */
    private String encrypt(String plain) {
        return EmailSecretCrypto.encrypt(plain);
    }

    /**
     * Decrypt SMTP password using AES-GCM utility. Utility supports
     * legacy/plaintext fallback if configured that way.
     */
    private String decrypt(String cipher) {
        return EmailSecretCrypto.decrypt(cipher);
    }

    private void rollbackQuietly(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            log.log(Level.WARNING, LOG_ROLLBACK_FAILED, ex);
        }
    }

    private void setAutoCommitQuietly(Connection con, boolean value) {
        try {
            con.setAutoCommit(value);
        } catch (SQLException ex) {
            log.log(Level.FINE, LOG_SET_AUTOCOMMIT_FAILED, ex);
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimToEmpty(String s) {
        return s == null ? EMPTY : s.trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? EMPTY : s;
    }

}
