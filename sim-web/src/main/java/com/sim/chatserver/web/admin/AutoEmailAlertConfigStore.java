package com.sim.chatserver.web.admin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DB-backed singleton configuration/state for automatic admin email alerts.
 */
public class AutoEmailAlertConfigStore {

    private static final Logger log = Logger.getLogger(AutoEmailAlertConfigStore.class.getName());

    static final int SINGLETON_ID = 1;

    private static final int MIN_INTERVAL_SECONDS = 30;
    private static final int MAX_INTERVAL_SECONDS = 86_400;

    private final DataSource dataSource;

    public AutoEmailAlertConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void ensureTable() throws SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS admin_auto_email_alert_config (
                id INT PRIMARY KEY,
                health_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                health_check_interval_seconds INT NOT NULL DEFAULT 300,
                health_offline_delay_seconds INT NOT NULL DEFAULT 300,
                health_resend_interval_seconds INT NOT NULL DEFAULT 1800,
                health_recipients TEXT,
                health_subject TEXT,
                health_message TEXT,

                term_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                term_check_interval_seconds INT NOT NULL DEFAULT 600,
                term_name TEXT,
                term_recipients TEXT,
                term_subject TEXT,
                term_message TEXT,

                health_last_status VARCHAR(32),
                health_last_checked_at TIMESTAMP,
                health_offline_since TIMESTAMP,
                health_last_alert_at TIMESTAMP,

                term_last_checked_at TIMESTAMP,
                term_last_count BIGINT NOT NULL DEFAULT 0,
                term_last_alert_at TIMESTAMP,

                updated_by VARCHAR(100),
                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }
    }

    public void ensureDefaultRow() throws SQLException {
        final String sql = """
            INSERT INTO admin_auto_email_alert_config (
                id,
                health_enabled,
                health_check_interval_seconds,
                health_offline_delay_seconds,
                health_resend_interval_seconds,
                term_enabled,
                term_check_interval_seconds,
                term_last_count,
                updated_by,
                updated_at
            )
            VALUES (?, FALSE, 300, 300, 1800, FALSE, 600, 0, 'system', NOW())
            ON CONFLICT (id) DO NOTHING
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, SINGLETON_ID);
            ps.executeUpdate();
        }
    }

    public AutoEmailAlertConfig load() throws SQLException {
        final String sql = """
            SELECT id,
                   health_enabled,
                   health_check_interval_seconds,
                   health_offline_delay_seconds,
                   health_resend_interval_seconds,
                   health_recipients,
                   health_subject,
                   health_message,
                   term_enabled,
                   term_check_interval_seconds,
                   term_name,
                   term_recipients,
                   term_subject,
                   term_message,
                   health_last_status,
                   health_last_checked_at,
                   health_offline_since,
                   health_last_alert_at,
                   term_last_checked_at,
                   term_last_count,
                   term_last_alert_at,
                   updated_by,
                   updated_at
            FROM admin_auto_email_alert_config
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, SINGLETON_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    public AutoEmailAlertConfig saveConfig(AutoEmailAlertConfig incoming, String updatedBy) throws SQLException {
        AutoEmailAlertConfig current = load();
        if (current == null) {
            ensureDefaultRow();
            current = load();
        }

        AutoEmailAlertConfig normalized = normalizeForConfigSave(incoming);
        boolean termNameChanged = !eqIgnoreCaseTrim(current == null ? null : current.getTermName(), normalized.getTermName());

        final String updateSql = """
            UPDATE admin_auto_email_alert_config
            SET health_enabled = ?,
                health_check_interval_seconds = ?,
                health_offline_delay_seconds = ?,
                health_resend_interval_seconds = ?,
                health_recipients = ?,
                health_subject = ?,
                health_message = ?,
                term_enabled = ?,
                term_check_interval_seconds = ?,
                term_name = ?,
                term_recipients = ?,
                term_subject = ?,
                term_message = ?,
                updated_by = ?,
                updated_at = ?
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(updateSql)) {
            ps.setBoolean(1, normalized.isHealthEnabled());
            ps.setInt(2, normalized.getHealthCheckIntervalSeconds());
            ps.setInt(3, normalized.getHealthOfflineDelaySeconds());
            ps.setInt(4, normalized.getHealthResendIntervalSeconds());
            setNullableString(ps, 5, normalized.getHealthRecipients());
            setNullableString(ps, 6, normalized.getHealthSubject());
            setNullableString(ps, 7, normalized.getHealthMessage());

            ps.setBoolean(8, normalized.isTermEnabled());
            ps.setInt(9, normalized.getTermCheckIntervalSeconds());
            setNullableString(ps, 10, normalized.getTermName());
            setNullableString(ps, 11, normalized.getTermRecipients());
            setNullableString(ps, 12, normalized.getTermSubject());
            setNullableString(ps, 13, normalized.getTermMessage());

            setNullableString(ps, 14, sanitizeText(updatedBy, 100));
            ps.setTimestamp(15, Timestamp.from(Instant.now()));
            ps.setInt(16, SINGLETON_ID);
            ps.executeUpdate();
        }

        if (termNameChanged) {
            clearTermState();
        }

        return load();
    }

    public void updateHealthState(Instant checkedAt, String status, Instant offlineSince, Instant alertAt) throws SQLException {
        final String sql = """
            UPDATE admin_auto_email_alert_config
            SET health_last_checked_at = ?,
                health_last_status = ?,
                health_offline_since = ?,
                health_last_alert_at = ?
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableInstant(ps, 1, checkedAt);
            setNullableString(ps, 2, sanitizeStatus(status));
            setNullableInstant(ps, 3, offlineSince);
            setNullableInstant(ps, 4, alertAt);
            ps.setInt(5, SINGLETON_ID);
            ps.executeUpdate();
        }
    }

    public void updateTermState(Instant checkedAt, long termCount, Instant alertAt) throws SQLException {
        final String sql = """
            UPDATE admin_auto_email_alert_config
            SET term_last_checked_at = ?,
                term_last_count = ?,
                term_last_alert_at = ?
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableInstant(ps, 1, checkedAt);
            ps.setLong(2, Math.max(0L, termCount));
            setNullableInstant(ps, 3, alertAt);
            ps.setInt(4, SINGLETON_ID);
            ps.executeUpdate();
        }
    }

    private void clearTermState() throws SQLException {
        final String sql = """
            UPDATE admin_auto_email_alert_config
            SET term_last_checked_at = NULL,
                term_last_count = 0,
                term_last_alert_at = NULL
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, SINGLETON_ID);
            ps.executeUpdate();
        }
    }

    private AutoEmailAlertConfig mapRow(ResultSet rs) throws SQLException {
        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setId(readNonNegativeInt(rs, "id"));

        cfg.setHealthEnabled(rs.getBoolean("health_enabled"));
        cfg.setHealthCheckIntervalSeconds(readIntervalSeconds(rs, "health_check_interval_seconds", 300));
        cfg.setHealthOfflineDelaySeconds(readDelaySeconds(rs, "health_offline_delay_seconds", 300));
        cfg.setHealthResendIntervalSeconds(readIntervalSeconds(rs, "health_resend_interval_seconds", 1800));
        cfg.setHealthRecipients(readSafeText(rs, "health_recipients", 4000));
        cfg.setHealthSubject(readSafeText(rs, "health_subject", 500));
        cfg.setHealthMessage(readSafeText(rs, "health_message", 8000));

        cfg.setTermEnabled(rs.getBoolean("term_enabled"));
        cfg.setTermCheckIntervalSeconds(readIntervalSeconds(rs, "term_check_interval_seconds", 600));
        cfg.setTermName(readSafeText(rs, "term_name", 255));
        cfg.setTermRecipients(readSafeText(rs, "term_recipients", 4000));
        cfg.setTermSubject(readSafeText(rs, "term_subject", 500));
        cfg.setTermMessage(readSafeText(rs, "term_message", 8000));

        cfg.setHealthLastStatus(readSafeText(rs, "health_last_status", 32));
        cfg.setHealthLastCheckedAt(readSafeInstant(rs, "health_last_checked_at"));
        cfg.setHealthOfflineSince(readSafeInstant(rs, "health_offline_since"));
        cfg.setHealthLastAlertAt(readSafeInstant(rs, "health_last_alert_at"));

        cfg.setTermLastCheckedAt(readSafeInstant(rs, "term_last_checked_at"));
        cfg.setTermLastCount(Math.max(0L, rs.getLong("term_last_count")));
        cfg.setTermLastAlertAt(readSafeInstant(rs, "term_last_alert_at"));

        cfg.setUpdatedBy(readSafeText(rs, "updated_by", 100));
        cfg.setUpdatedAt(readSafeInstant(rs, "updated_at"));

        return cfg;
    }

    private AutoEmailAlertConfig normalizeForConfigSave(AutoEmailAlertConfig in) {
        AutoEmailAlertConfig out = new AutoEmailAlertConfig();
        out.setId(SINGLETON_ID);

        out.setHealthEnabled(in != null && in.isHealthEnabled());
        out.setHealthCheckIntervalSeconds(clampIntervalSeconds(in == null ? 300 : in.getHealthCheckIntervalSeconds(), 300));
        out.setHealthOfflineDelaySeconds(clampDelaySeconds(in == null ? 300 : in.getHealthOfflineDelaySeconds(), 300));
        out.setHealthResendIntervalSeconds(clampIntervalSeconds(in == null ? 1800 : in.getHealthResendIntervalSeconds(), 1800));
        out.setHealthRecipients(sanitizeText(in == null ? null : in.getHealthRecipients(), 4000));
        out.setHealthSubject(sanitizeText(in == null ? null : in.getHealthSubject(), 500));
        out.setHealthMessage(sanitizeText(in == null ? null : in.getHealthMessage(), 8000));

        out.setTermEnabled(in != null && in.isTermEnabled());
        out.setTermCheckIntervalSeconds(clampIntervalSeconds(in == null ? 600 : in.getTermCheckIntervalSeconds(), 600));
        out.setTermName(sanitizeText(in == null ? null : in.getTermName(), 255));
        out.setTermRecipients(sanitizeText(in == null ? null : in.getTermRecipients(), 4000));
        out.setTermSubject(sanitizeText(in == null ? null : in.getTermSubject(), 500));
        out.setTermMessage(sanitizeText(in == null ? null : in.getTermMessage(), 8000));

        return out;
    }

    private int readNonNegativeInt(ResultSet rs, String column) {
        try {
            Integer value = rs.getObject(column, Integer.class);
            return value == null ? 0 : Math.max(0, value);
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to read integer column " + column, e);
            return 0;
        }
    }

    private int readIntervalSeconds(ResultSet rs, String column, int fallback) {
        return clampIntervalSeconds(readNonNegativeInt(rs, column), fallback);
    }

    private int readDelaySeconds(ResultSet rs, String column, int fallback) {
        return clampDelaySeconds(readNonNegativeInt(rs, column), fallback);
    }

    private int clampIntervalSeconds(int value, int fallback) {
        int v = value <= 0 ? fallback : value;
        if (v < MIN_INTERVAL_SECONDS) {
            v = MIN_INTERVAL_SECONDS;
        }
        if (v > MAX_INTERVAL_SECONDS) {
            v = MAX_INTERVAL_SECONDS;
        }
        return v;
    }

    private int clampDelaySeconds(int value, int fallback) {
        int v = value < 0 ? fallback : value;
        if (v > MAX_INTERVAL_SECONDS) {
            v = MAX_INTERVAL_SECONDS;
        }
        return v;
    }

    private Instant readSafeInstant(ResultSet rs, String column) {
        try {
            Timestamp ts = rs.getObject(column, Timestamp.class);
            if (ts == null) {
                return null;
            }
            return ts.toInstant();
        } catch (SQLException | DateTimeException e) {
            log.log(Level.FINE, "Unable to read timestamp column " + column, e);
            return null;
        }
    }

    private String readSafeText(ResultSet rs, String column, int maxChars) {
        try {
            String value = rs.getObject(column, String.class);
            return sanitizeText(value, maxChars);
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to read text column " + column, e);
            return null;
        }
    }

    private String sanitizeStatus(String value) {
        String sanitized = sanitizeText(value, 32);
        return sanitized == null ? null : sanitized.toUpperCase(Locale.ROOT);
    }

    private String sanitizeText(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u0000', ' ')
                .replace("\r", "")
                .trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (maxChars > 0 && normalized.length() > maxChars) {
            return normalized.substring(0, maxChars);
        }
        return normalized;
    }

    private boolean eqIgnoreCaseTrim(String a, String b) {
        String aa = a == null ? "" : a.trim();
        String bb = b == null ? "" : b.trim();
        return aa.equalsIgnoreCase(bb);
    }

    private void setNullableString(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, java.sql.Types.VARCHAR);
        } else {
            ps.setString(idx, value);
        }
    }

    private void setNullableInstant(PreparedStatement ps, int idx, Instant value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(idx, Timestamp.from(value));
        }
    }

    public static final class AutoEmailAlertConfig {

        private int id;

        private boolean healthEnabled;
        private int healthCheckIntervalSeconds;
        private int healthOfflineDelaySeconds;
        private int healthResendIntervalSeconds;
        private String healthRecipients;
        private String healthSubject;
        private String healthMessage;

        private boolean termEnabled;
        private int termCheckIntervalSeconds;
        private String termName;
        private String termRecipients;
        private String termSubject;
        private String termMessage;

        private String healthLastStatus;
        private Instant healthLastCheckedAt;
        private Instant healthOfflineSince;
        private Instant healthLastAlertAt;

        private Instant termLastCheckedAt;
        private long termLastCount;
        private Instant termLastAlertAt;

        private String updatedBy;
        private Instant updatedAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isHealthEnabled() {
            return healthEnabled;
        }

        public void setHealthEnabled(boolean healthEnabled) {
            this.healthEnabled = healthEnabled;
        }

        public int getHealthCheckIntervalSeconds() {
            return healthCheckIntervalSeconds;
        }

        public void setHealthCheckIntervalSeconds(int healthCheckIntervalSeconds) {
            this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
        }

        public int getHealthOfflineDelaySeconds() {
            return healthOfflineDelaySeconds;
        }

        public void setHealthOfflineDelaySeconds(int healthOfflineDelaySeconds) {
            this.healthOfflineDelaySeconds = healthOfflineDelaySeconds;
        }

        public int getHealthResendIntervalSeconds() {
            return healthResendIntervalSeconds;
        }

        public void setHealthResendIntervalSeconds(int healthResendIntervalSeconds) {
            this.healthResendIntervalSeconds = healthResendIntervalSeconds;
        }

        public String getHealthRecipients() {
            return healthRecipients;
        }

        public void setHealthRecipients(String healthRecipients) {
            this.healthRecipients = healthRecipients;
        }

        public String getHealthSubject() {
            return healthSubject;
        }

        public void setHealthSubject(String healthSubject) {
            this.healthSubject = healthSubject;
        }

        public String getHealthMessage() {
            return healthMessage;
        }

        public void setHealthMessage(String healthMessage) {
            this.healthMessage = healthMessage;
        }

        public boolean isTermEnabled() {
            return termEnabled;
        }

        public void setTermEnabled(boolean termEnabled) {
            this.termEnabled = termEnabled;
        }

        public int getTermCheckIntervalSeconds() {
            return termCheckIntervalSeconds;
        }

        public void setTermCheckIntervalSeconds(int termCheckIntervalSeconds) {
            this.termCheckIntervalSeconds = termCheckIntervalSeconds;
        }

        public String getTermName() {
            return termName;
        }

        public void setTermName(String termName) {
            this.termName = termName;
        }

        public String getTermRecipients() {
            return termRecipients;
        }

        public void setTermRecipients(String termRecipients) {
            this.termRecipients = termRecipients;
        }

        public String getTermSubject() {
            return termSubject;
        }

        public void setTermSubject(String termSubject) {
            this.termSubject = termSubject;
        }

        public String getTermMessage() {
            return termMessage;
        }

        public void setTermMessage(String termMessage) {
            this.termMessage = termMessage;
        }

        public String getHealthLastStatus() {
            return healthLastStatus;
        }

        public void setHealthLastStatus(String healthLastStatus) {
            this.healthLastStatus = healthLastStatus;
        }

        public Instant getHealthLastCheckedAt() {
            return healthLastCheckedAt;
        }

        public void setHealthLastCheckedAt(Instant healthLastCheckedAt) {
            this.healthLastCheckedAt = healthLastCheckedAt;
        }

        public Instant getHealthOfflineSince() {
            return healthOfflineSince;
        }

        public void setHealthOfflineSince(Instant healthOfflineSince) {
            this.healthOfflineSince = healthOfflineSince;
        }

        public Instant getHealthLastAlertAt() {
            return healthLastAlertAt;
        }

        public void setHealthLastAlertAt(Instant healthLastAlertAt) {
            this.healthLastAlertAt = healthLastAlertAt;
        }

        public Instant getTermLastCheckedAt() {
            return termLastCheckedAt;
        }

        public void setTermLastCheckedAt(Instant termLastCheckedAt) {
            this.termLastCheckedAt = termLastCheckedAt;
        }

        public long getTermLastCount() {
            return termLastCount;
        }

        public void setTermLastCount(long termLastCount) {
            this.termLastCount = termLastCount;
        }

        public Instant getTermLastAlertAt() {
            return termLastAlertAt;
        }

        public void setTermLastAlertAt(Instant termLastAlertAt) {
            this.termLastAlertAt = termLastAlertAt;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
