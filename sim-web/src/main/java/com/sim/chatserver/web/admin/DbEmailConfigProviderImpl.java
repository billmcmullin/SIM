package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.security.email.EmailSecretCrypto;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(DbEmailConfigProviderImpl.class.getName());
    private static final int SINGLE_ROW_ID = 1;

    @Override
    public EmailConfig load() {
        final String sql = """
                SELECT host, port, auth, starttls, ssl, username, password_enc, default_from
                FROM email_smtp_config
                WHERE id = ?
                """;

        try (Connection con = newConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SINGLE_ROW_ID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String host = trimToEmpty(rs.getString("host"));
                int port = rs.getInt("port");
                boolean auth = rs.getBoolean("auth");
                boolean startTls = rs.getBoolean("starttls");
                boolean ssl = rs.getBoolean("ssl");
                String username = trimToEmpty(rs.getString("username"));
                String passwordEnc = trimToEmpty(rs.getString("password_enc"));
                String defaultFrom = trimToEmpty(rs.getString("default_from"));

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
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load SMTP config from DB", e);
            return null;
        }
    }

    @Override
    public void save(EmailConfig config, String updatedBy) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        validate(config);

        // If incoming password is blank, retain existing password via interface helper.
        EmailConfig merged = mergeKeepingExistingPasswordIfBlank(config);

        final String updateSql = """
                UPDATE email_smtp_config
                SET host = ?, port = ?, auth = ?, starttls = ?, ssl = ?,
                    username = ?, password_enc = ?, default_from = ?, updated_by = ?, updated_at = ?
                WHERE id = ?
                """;

        final String insertSql = """
                INSERT INTO email_smtp_config
                (id, host, port, auth, starttls, ssl, username, password_enc, default_from, updated_by, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String encPassword = encrypt(nullToEmpty(merged.password()));
        Timestamp now = Timestamp.from(Instant.now());
        String actor = trimToEmpty(updatedBy);

        try (Connection con = newConnection()) {
            con.setAutoCommit(false);

            try {
                int updated;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    bindCommonForUpdate(ps, merged, encPassword, actor, now);
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                        bindCommonForInsert(ps, merged, encPassword, actor, now);
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (Exception e) {
                rollbackQuietly(con);
                throw e;
            } finally {
                setAutoCommitQuietly(con, true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save SMTP config", e);
        }
    }

    private Connection newConnection() throws SQLException {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String db = env("DB_NAME", "chat");
        String user = env("DB_USER", "postgres");
        String pass = env("DB_PASSWORD", "");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        return DriverManager.getConnection(url, user, pass);
    }

    private String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private void bindCommonForUpdate(PreparedStatement ps, EmailConfig c, String encPassword, String updatedBy, Timestamp now)
            throws SQLException {
        ps.setString(1, c.host());
        ps.setInt(2, c.port());
        ps.setBoolean(3, c.auth());
        ps.setBoolean(4, c.startTls());
        ps.setBoolean(5, c.ssl());
        ps.setString(6, nullToEmpty(c.username()));
        ps.setString(7, encPassword);
        ps.setString(8, nullToEmpty(c.defaultFrom()));
        ps.setString(9, updatedBy);
        ps.setTimestamp(10, now);
        ps.setInt(11, SINGLE_ROW_ID);
    }

    private void bindCommonForInsert(PreparedStatement ps, EmailConfig c, String encPassword, String updatedBy, Timestamp now)
            throws SQLException {
        ps.setInt(1, SINGLE_ROW_ID);
        ps.setString(2, c.host());
        ps.setInt(3, c.port());
        ps.setBoolean(4, c.auth());
        ps.setBoolean(5, c.startTls());
        ps.setBoolean(6, c.ssl());
        ps.setString(7, nullToEmpty(c.username()));
        ps.setString(8, encPassword);
        ps.setString(9, nullToEmpty(c.defaultFrom()));
        ps.setString(10, updatedBy);
        ps.setTimestamp(11, now);
    }

    private void validate(EmailConfig c) {
        if (!hasText(c.host())) {
            throw new IllegalArgumentException("SMTP host is required");
        }
        if (c.port() < 1 || c.port() > 65535) {
            throw new IllegalArgumentException("SMTP port must be between 1 and 65535");
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
        } catch (Exception ex) {
            log.log(Level.WARNING, "Rollback failed", ex);
        }
    }

    private void setAutoCommitQuietly(Connection con, boolean value) {
        try {
            con.setAutoCommit(value);
        } catch (Exception ex) {
            log.log(Level.FINE, "setAutoCommit failed", ex);
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
