package com.sim.chatserver.web.admin;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
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
public final class AutoEmailAlertConfigStore {

    private static final Logger log = Logger.getLogger(AutoEmailAlertConfigStore.class.getCanonicalName());
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String EMPTY = "";
    private static final String COL_ID = "id";
    private static final String COL_HEALTH_ENABLED = "health_enabled";
    private static final String COL_HEALTH_CHECK_INTERVAL_SECONDS = "health_check_interval_seconds";
    private static final String COL_HEALTH_OFFLINE_DELAY_SECONDS = "health_offline_delay_seconds";
    private static final String COL_HEALTH_RESEND_INTERVAL_SECONDS = "health_resend_interval_seconds";
    private static final String COL_HEALTH_RECIPIENTS = "health_recipients";
    private static final String COL_HEALTH_SUBJECT = "health_subject";
    private static final String COL_HEALTH_MESSAGE = "health_message";
    private static final String COL_HEALTH_RUNBOOK_URL = "health_runbook_url";
    private static final String COL_HEALTH_RUNBOOK_ATTACHMENT_PATH = "health_runbook_attachment_path";
    private static final String COL_TERM_ENABLED = "term_enabled";
    private static final String COL_TERM_CHECK_INTERVAL_SECONDS = "term_check_interval_seconds";
    private static final String COL_TERM_NAME = "term_name";
    private static final String COL_TERM_RECIPIENTS = "term_recipients";
    private static final String COL_TERM_SUBJECT = "term_subject";
    private static final String COL_TERM_MESSAGE = "term_message";
    private static final String COL_HEALTH_LAST_STATUS = "health_last_status";
    private static final String COL_HEALTH_LAST_CHECKED_AT = "health_last_checked_at";
    private static final String COL_HEALTH_OFFLINE_SINCE = "health_offline_since";
    private static final String COL_HEALTH_LAST_ALERT_AT = "health_last_alert_at";
    private static final String COL_HEALTH_LAST_RESTART_ATTEMPT_AT = "health_last_restart_attempt_at";
    private static final String COL_TERM_LAST_CHECKED_AT = "term_last_checked_at";
    private static final String COL_TERM_LAST_COUNT = "term_last_count";
    private static final String COL_TERM_LAST_ALERT_AT = "term_last_alert_at";
    private static final String COL_UPDATED_BY = "updated_by";
    private static final String COL_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_UPDATED_AT;
    private static final String RE_LONG = "^-?\\d{1,18}$";
    private static final String RE_INT = "^-?\\d{1,10}$";
    private static final String BOOL_TRUE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
    private static final String BOOL_T = "t";
    private static final String BOOL_ONE = "1";
    private static final String BOOL_YES = com.sim.chatserver.util.CommonLiteralConstants.VALUE_YES;
    private static final String BOOL_Y = "y";
    private static final String LOG_READ_INT_PREFIX = "Unable to read integer column ";
    private static final String LOG_READ_LONG_PREFIX = "Unable to read long column ";
    private static final String LOG_PARSE_INT_PREFIX = "Unable to parse integer column ";
    private static final String LOG_TS_FALLBACK_PREFIX = "Falling back to SQL timestamp parse for column ";
    private static final String LOG_READ_TS_PREFIX = "Unable to read timestamp column ";
    private static final String LOG_READ_TEXT_PREFIX = "Unable to read text column ";

    static final int SINGLETON_ID = 1;

    private static final int MIN_INTERVAL_SECONDS = 30;
    private static final int MAX_INTERVAL_SECONDS = 86_400;

    private final DataSource dataSource;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    AutoEmailAlertConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    final void ensureTable() throws SQLException {
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
                health_runbook_url TEXT,
                health_runbook_attachment_path TEXT,

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
                health_last_restart_attempt_at TIMESTAMP,

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

        ensureOptionalColumns();
    }

    private void ensureOptionalColumns() throws SQLException {
        final String addRunbookUrl = """
            ALTER TABLE admin_auto_email_alert_config
            ADD COLUMN IF NOT EXISTS health_runbook_url TEXT
            """;

        final String addRunbookAttachmentPath = """
            ALTER TABLE admin_auto_email_alert_config
            ADD COLUMN IF NOT EXISTS health_runbook_attachment_path TEXT
            """;

        final String addHealthLastRestartAttemptAt = """
            ALTER TABLE admin_auto_email_alert_config
            ADD COLUMN IF NOT EXISTS health_last_restart_attempt_at TIMESTAMP
            """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement psUrl = c.prepareStatement(addRunbookUrl);
             PreparedStatement psPath = c.prepareStatement(addRunbookAttachmentPath);
             PreparedStatement psRestartAttempt = c.prepareStatement(addHealthLastRestartAttemptAt)) {
            psUrl.execute();
            psPath.execute();
            psRestartAttempt.execute();
        }
    }

    final void ensureDefaultRow() throws SQLException {
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

    final AutoEmailAlertConfig load() throws SQLException {
        final String sql = """
            SELECT id,
                   health_enabled,
                   health_check_interval_seconds,
                   health_offline_delay_seconds,
                   health_resend_interval_seconds,
                   health_recipients,
                   health_subject,
                   health_message,
                   health_runbook_url,
                   health_runbook_attachment_path,
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
                   health_last_restart_attempt_at,
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

    final AutoEmailAlertConfig saveConfig(AutoEmailAlertConfig incoming, String updatedBy) throws SQLException {
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
                health_runbook_url = ?,
                health_runbook_attachment_path = ?,
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
            setNullableString(ps, 8, normalized.getHealthRunbookUrl());
            setNullableString(ps, 9, normalized.getHealthRunbookAttachmentPath());

            ps.setBoolean(10, normalized.isTermEnabled());
            ps.setInt(11, normalized.getTermCheckIntervalSeconds());
            setNullableString(ps, 12, normalized.getTermName());
            setNullableString(ps, 13, normalized.getTermRecipients());
            setNullableString(ps, 14, normalized.getTermSubject());
            setNullableString(ps, 15, normalized.getTermMessage());

            setNullableString(ps, 16, sanitizeText(updatedBy, 100));
            ps.setTimestamp(17, Timestamp.from(Instant.now()));
            ps.setInt(18, SINGLETON_ID);
            ps.executeUpdate();
        }

        if (termNameChanged) {
            clearTermState();
        }

        return load();
    }

    final void updateHealthState(
            Instant checkedAt,
            String status,
            Instant offlineSince,
            Instant alertAt,
            Instant restartAttemptAt
    ) throws SQLException {
        final String sql = """
            UPDATE admin_auto_email_alert_config
            SET health_last_checked_at = ?,
                health_last_status = ?,
                health_offline_since = ?,
                health_last_alert_at = ?,
                health_last_restart_attempt_at = ?
            WHERE id = ?
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableInstant(ps, 1, checkedAt);
            setNullableString(ps, 2, sanitizeStatus(status));
            setNullableInstant(ps, 3, offlineSince);
            setNullableInstant(ps, 4, alertAt);
            setNullableInstant(ps, 5, restartAttemptAt);
            ps.setInt(6, SINGLETON_ID);
            ps.executeUpdate();
        }
    }

    final void updateTermState(Instant checkedAt, long termCount, Instant alertAt) throws SQLException {
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
        cfg.setId(readNonNegativeInt(rs, COL_ID));

        cfg.setHealthEnabled(readSafeBoolean(rs, COL_HEALTH_ENABLED, false));
        cfg.setHealthCheckIntervalSeconds(readIntervalSeconds(rs, COL_HEALTH_CHECK_INTERVAL_SECONDS, 300));
        cfg.setHealthOfflineDelaySeconds(readDelaySeconds(rs, COL_HEALTH_OFFLINE_DELAY_SECONDS, 300));
        cfg.setHealthResendIntervalSeconds(readIntervalSeconds(rs, COL_HEALTH_RESEND_INTERVAL_SECONDS, 1800));
        cfg.setHealthRecipients(readSafeText(rs, COL_HEALTH_RECIPIENTS, 4000));
        cfg.setHealthSubject(readSafeText(rs, COL_HEALTH_SUBJECT, 500));
        cfg.setHealthMessage(readSafeText(rs, COL_HEALTH_MESSAGE, 8000));
        cfg.setHealthRunbookUrl(readSafeText(rs, COL_HEALTH_RUNBOOK_URL, 2000));
        cfg.setHealthRunbookAttachmentPath(readSafeText(rs, COL_HEALTH_RUNBOOK_ATTACHMENT_PATH, 1024));

        cfg.setTermEnabled(readSafeBoolean(rs, COL_TERM_ENABLED, false));
        cfg.setTermCheckIntervalSeconds(readIntervalSeconds(rs, COL_TERM_CHECK_INTERVAL_SECONDS, 600));
        cfg.setTermName(readSafeText(rs, COL_TERM_NAME, 255));
        cfg.setTermRecipients(readSafeText(rs, COL_TERM_RECIPIENTS, 4000));
        cfg.setTermSubject(readSafeText(rs, COL_TERM_SUBJECT, 500));
        cfg.setTermMessage(readSafeText(rs, COL_TERM_MESSAGE, 8000));

        cfg.setHealthLastStatus(readSafeText(rs, COL_HEALTH_LAST_STATUS, 32));
        cfg.setHealthLastCheckedAt(readSafeInstant(rs, COL_HEALTH_LAST_CHECKED_AT));
        cfg.setHealthOfflineSince(readSafeInstant(rs, COL_HEALTH_OFFLINE_SINCE));
        cfg.setHealthLastAlertAt(readSafeInstant(rs, COL_HEALTH_LAST_ALERT_AT));
        cfg.setHealthLastRestartAttemptAt(readSafeInstant(rs, COL_HEALTH_LAST_RESTART_ATTEMPT_AT));

        cfg.setTermLastCheckedAt(readSafeInstant(rs, COL_TERM_LAST_CHECKED_AT));
        cfg.setTermLastCount(readNonNegativeLong(rs, COL_TERM_LAST_COUNT));
        cfg.setTermLastAlertAt(readSafeInstant(rs, COL_TERM_LAST_ALERT_AT));

        cfg.setUpdatedBy(readSafeText(rs, COL_UPDATED_BY, 100));
        cfg.setUpdatedAt(readSafeInstant(rs, COL_UPDATED_AT));

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
        out.setHealthRunbookUrl(sanitizeText(in == null ? null : in.getHealthRunbookUrl(), 2000));
        out.setHealthRunbookAttachmentPath(sanitizeText(in == null ? null : in.getHealthRunbookAttachmentPath(), 1024));

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
            Object value = readSafeInteger(rs, column);
            int normalized = 0;
            if (value != null) {
                normalized = Integer.parseInt(value.toString());
            }
            return Math.max(0, normalized);
        } catch (SQLException e) {
            log.log(Level.FINE, LOG_READ_INT_PREFIX + column, e);
            return 0;
        }
    }

    private long readNonNegativeLong(ResultSet rs, String column) {
        try {
            String raw = readSafeText(rs, column, 32);
            if (raw == null || raw.isBlank() || !raw.matches(RE_LONG)) {
                return 0L;
            }
            long value = Long.parseLong(raw);
            return Math.max(0L, value);
        } catch (NumberFormatException e) {
            log.log(Level.FINE, LOG_READ_LONG_PREFIX + column, e);
            return 0L;
        }
    }

    private Integer readSafeInteger(ResultSet rs, String column) throws SQLException {
        String raw = readSafeText(rs, column, 16);
        if (raw == null || raw.isBlank() || !raw.matches(RE_INT)) {
            return null;
        }
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, LOG_PARSE_INT_PREFIX + column, ex);
            return null;
        }
    }

    private boolean readSafeBoolean(ResultSet rs, String column, boolean fallback) {
        String text = readSafeText(rs, column, 16);
        if (text == null) {
            return fallback;
        }
        text = text.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return fallback;
        }
        return BOOL_TRUE.equals(text) || BOOL_T.equals(text) || BOOL_ONE.equals(text)
            || BOOL_YES.equals(text) || BOOL_Y.equals(text);
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
            String text = readSafeText(rs, column, 128);
            if (text == null) {
                return null;
            }
            text = text.trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return Instant.parse(text);
            } catch (DateTimeException ex) {
                log.log(Level.FINE, LOG_TS_FALLBACK_PREFIX + column, ex);
                return Timestamp.valueOf(text.replace('T', ' ')).toInstant();
            }
        } catch (DateTimeException e) {
            log.log(Level.FINE, LOG_READ_TS_PREFIX + column, e);
            return null;
        }
    }

    private String readSafeText(ResultSet rs, String column, int maxChars) {
        try {
            try (Reader reader = rs.getCharacterStream(column)) {
                if (reader == null) {
                    return null;
                }

                char[] buffer = new char[256];
                StringBuilder out = new StringBuilder(Math.max(64, Math.min(maxChars, 512)));
                int total = 0;
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    total += read;
                    if (maxChars > 0 && total > maxChars) {
                        int remaining = Math.max(0, maxChars - (total - read));
                        if (remaining > 0) {
                            out.append(buffer, 0, remaining);
                        }
                        break;
                    }
                    out.append(buffer, 0, read);
                }
                return sanitizeText(out.toString(), maxChars);
            }
        } catch (SQLException | IOException e) {
            log.log(Level.FINE, LOG_READ_TEXT_PREFIX + column, e);
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
            .replace(System.lineSeparator(), EMPTY)
                .trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (maxChars > 0 && normalized.length() > maxChars) {
            return new String(normalized.toCharArray(), 0, maxChars);
        }
        return normalized;
    }

    private boolean eqIgnoreCaseTrim(String a, String b) {
        String aa = a == null ? EMPTY : a.trim();
        String bb = b == null ? EMPTY : b.trim();
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

}

