package com.sim.chatserver.service.widget;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DateTimeException;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.TextIoSanitizerUtil;

/**
 * Stores/retrieves widget availability health-check configuration in
 * PostgreSQL.
 *
 * Single-row model (id=1) for now.
 */
public class WidgetHealthConfigStore {

    private static final Logger log = Logger.getLogger(WidgetHealthConfigStore.class.getCanonicalName());
    private static final String DEFAULT_HEALTHCHECK_URL = "http://anythingllm:3001/api/v1/system";
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 300;
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_HEAD = "HEAD";
    private static final String HTTP_METHOD_POST = com.sim.chatserver.util.CommonLiteralConstants.HTTP_METHOD_POST;
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String SQL_ADD_REQUEST_ORIGIN = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_origin TEXT";
    private static final String SQL_ADD_REQUEST_REFERER = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_referer TEXT";
    private static final String SQL_ADD_REQUEST_USER_AGENT = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_user_agent TEXT";
    private static final String SQL_ADD_REQUEST_COOKIE = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_cookie TEXT";
    private static final String SQL_ADD_API_KEY_HEADER_NAME = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS api_key_header_name VARCHAR(255)";
    private static final String SQL_ADD_API_KEY_VALUE = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS api_key_value TEXT";
    private static final String SQL_ADD_HEALTHCHECK_ENABLED = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS healthcheck_enabled BOOLEAN NOT NULL DEFAULT TRUE";
    private static final String SQL_ADD_CHECK_INTERVAL_SECONDS = "ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS check_interval_seconds INT NOT NULL DEFAULT 300";
    private static final String MSG_CONFIG_NULL = "WidgetHealthConfig cannot be null";
    private static final String MSG_FAILED_SAVE_CONFIG = "Failed to save widget health config";
    private static final String MSG_INVALID_INTEGER_TEXT = "Invalid integer text for column ";
    private static final String MSG_INVALID_POSITIVE_INTEGER_TEXT = "Invalid positive integer text for column ";
    private static final String MSG_INSTANT_PARSE_FALLBACK = "Instant timestamp parse fallback for column ";
    private static final String MSG_TIMESTAMP_PARSE_FALLBACK_FAILED = "Timestamp parse fallback failed for column ";
    private static final String MSG_INVALID_TIMESTAMP_VALUE = "Invalid timestamp value for column ";
    private static final String MSG_RESULTSET_CHAR_STREAM_FAILED = "ResultSet character-stream read failed for column ";
    private static final String MSG_CHAR_STREAM_DECODE_FAILED = "Character-stream decode failed for column ";
    private static final String MSG_RESULTSET_BINARY_FAILED = "ResultSet binary read failed for column ";
    private static final String MSG_RESULTSET_STRING_FAILED = "ResultSet string read failed for column ";
    private static final String MSG_RESULTSET_OBJECT_FAILED = "ResultSet object read failed for column ";
    private static final String MSG_NEGATIVE_NUMERIC_VALUE = "Negative numeric value for column ";
    private static final String MSG_OUT_OF_RANGE_TIMESTAMP = "Out-of-range timestamp value for column ";
    private static final String MSG_FALLING_BACK = ", falling back";
    private static final String COL_ID = "id";
    private static final String COL_HEALTHCHECK_URL = "healthcheck_url";
    private static final String COL_HEALTHCHECK_ENABLED = "healthcheck_enabled";
    private static final String COL_CHECK_INTERVAL_SECONDS = "check_interval_seconds";
    private static final String COL_METHOD = "method";
    private static final String COL_TIMEOUT_MS = "timeout_ms";
    private static final String COL_EXPECT_JSON_FIELD = "expect_json_field";
    private static final String COL_EXPECT_JSON_VALUE = "expect_json_value";
    private static final String COL_WIDGET_ID = "widget_id";
    private static final String COL_REQUEST_ORIGIN = "request_origin";
    private static final String COL_REQUEST_REFERER = "request_referer";
    private static final String COL_REQUEST_USER_AGENT = "request_user_agent";
    private static final String COL_REQUEST_COOKIE = "request_cookie";
    private static final String COL_API_KEY_HEADER_NAME = "api_key_header_name";
    private static final String COL_API_KEY_VALUE = "api_key_value";
    private static final String COL_UPDATED_BY = "updated_by";
    private static final String COL_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_UPDATED_AT;
    private static final String BOOL_TRUE = CommonLiteralConstants.VALUE_TRUE;
    private static final String BOOL_ONE = "1";
    private static final String BOOL_YES = CommonLiteralConstants.VALUE_YES;
    private static final String BOOL_Y = "y";
    private static final String BOOL_FALSE = CommonLiteralConstants.VALUE_FALSE;
    private static final String BOOL_ZERO = "0";
    private static final String BOOL_NO = "no";
    private static final String BOOL_N = "n";
    private static final String SIGNED_INTEGER_PATTERN = "^-?\\d+$";
    private static final Instant MIN_ALLOWED_INSTANT = Instant.parse("1970-01-01T00:00:00Z");
    private static final Instant MAX_ALLOWED_INSTANT = Instant.parse("3000-12-31T23:59:59Z");
    private static final byte[] EMPTY_BYTES = new byte[0];

    public static final int SINGLETON_ID = 1;

    private final DataSource dataSource;

    public WidgetHealthConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    public void ensureTable() throws java.sql.SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS widget_health_config (
                id INT PRIMARY KEY,
                healthcheck_url TEXT NOT NULL,
                healthcheck_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                check_interval_seconds INT NOT NULL DEFAULT 300,
            method VARCHAR(10) NOT NULL DEFAULT '%s',
                timeout_ms INT NOT NULL DEFAULT 8000,
                expect_json_field VARCHAR(100),
                expect_json_value VARCHAR(255),
                widget_id VARCHAR(255),
                request_origin TEXT,
                request_referer TEXT,
                request_user_agent TEXT,
                request_cookie TEXT,
                api_key_header_name VARCHAR(255),
                api_key_value TEXT,
                updated_by VARCHAR(100),
                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(HTTP_METHOD_GET);

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }

        // Backward-compatible migrations for existing DBs
        addColumnIfMissing(SQL_ADD_REQUEST_ORIGIN);
        addColumnIfMissing(SQL_ADD_REQUEST_REFERER);
        addColumnIfMissing(SQL_ADD_REQUEST_USER_AGENT);
        addColumnIfMissing(SQL_ADD_REQUEST_COOKIE);
        addColumnIfMissing(SQL_ADD_API_KEY_HEADER_NAME);
        addColumnIfMissing(SQL_ADD_API_KEY_VALUE);
        addColumnIfMissing(SQL_ADD_HEALTHCHECK_ENABLED);
        addColumnIfMissing(SQL_ADD_CHECK_INTERVAL_SECONDS);
    }

    private void addColumnIfMissing(String sql) throws java.sql.SQLException {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }
    }

    /**
     * Ensure singleton row exists with defaults.
     */
    public void ensureDefaultRow() throws java.sql.SQLException {
        final String sql = """
            INSERT INTO widget_health_config (
                id, healthcheck_url, healthcheck_enabled, check_interval_seconds, method, timeout_ms, expect_json_field, expect_json_value, widget_id,
                request_origin, request_referer, request_user_agent, request_cookie,
                api_key_header_name, api_key_value,
                updated_by, updated_at
            )
            VALUES (?, ?, TRUE, 300, '%s', 8000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system', NOW())
            ON CONFLICT (id) DO NOTHING
            """.formatted(HTTP_METHOD_GET);

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, SINGLETON_ID);
            ps.setString(2, DEFAULT_HEALTHCHECK_URL);
            ps.executeUpdate();
        }
    }

    public WidgetHealthConfig load() throws java.sql.SQLException {
        final String sql = """
            SELECT id, healthcheck_url, method, timeout_ms,
                     healthcheck_enabled, check_interval_seconds,
                   expect_json_field, expect_json_value, widget_id,
                   request_origin, request_referer, request_user_agent, request_cookie,
                     api_key_header_name, api_key_value,
                   updated_by, updated_at
            FROM widget_health_config
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

    public WidgetHealthConfig save(WidgetHealthConfig in) throws java.sql.SQLException {
        if (in == null) {
            throw new IllegalArgumentException(MSG_CONFIG_NULL);
        }

        final String sql = """
            INSERT INTO widget_health_config (
                id, healthcheck_url, method, timeout_ms,
                healthcheck_enabled, check_interval_seconds,
                expect_json_field, expect_json_value, widget_id,
                request_origin, request_referer, request_user_agent, request_cookie,
                api_key_header_name, api_key_value,
                updated_by, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                healthcheck_url = EXCLUDED.healthcheck_url,
                method = EXCLUDED.method,
                timeout_ms = EXCLUDED.timeout_ms,
                healthcheck_enabled = EXCLUDED.healthcheck_enabled,
                check_interval_seconds = EXCLUDED.check_interval_seconds,
                expect_json_field = EXCLUDED.expect_json_field,
                expect_json_value = EXCLUDED.expect_json_value,
                widget_id = EXCLUDED.widget_id,
                request_origin = EXCLUDED.request_origin,
                request_referer = EXCLUDED.request_referer,
                request_user_agent = EXCLUDED.request_user_agent,
                request_cookie = EXCLUDED.request_cookie,
                api_key_header_name = EXCLUDED.api_key_header_name,
                api_key_value = EXCLUDED.api_key_value,
                updated_by = EXCLUDED.updated_by,
                updated_at = EXCLUDED.updated_at
            """;

        WidgetHealthConfig normalized = normalize(in);

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, SINGLETON_ID);
            ps.setString(2, normalized.getHealthcheckUrl());
            ps.setString(3, normalized.getMethod());
            ps.setInt(4, normalized.getTimeoutMs());
            ps.setBoolean(5, normalized.isHealthcheckEnabled());
            ps.setInt(6, normalized.getCheckIntervalSeconds());

            if (normalized.getExpectJsonField() == null) {
                ps.setNull(7, Types.VARCHAR); 
            }else {
                ps.setString(7, normalized.getExpectJsonField());
            }

            if (normalized.getExpectJsonValue() == null) {
                ps.setNull(8, Types.VARCHAR); 
            }else {
                ps.setString(8, normalized.getExpectJsonValue());
            }

            if (normalized.getWidgetId() == null) {
                ps.setNull(9, Types.VARCHAR); 
            }else {
                ps.setString(9, normalized.getWidgetId());
            }

            if (normalized.getRequestOrigin() == null) {
                ps.setNull(10, Types.VARCHAR); 
            }else {
                ps.setString(10, normalized.getRequestOrigin());
            }

            if (normalized.getRequestReferer() == null) {
                ps.setNull(11, Types.VARCHAR); 
            }else {
                ps.setString(11, normalized.getRequestReferer());
            }

            if (normalized.getRequestUserAgent() == null) {
                ps.setNull(12, Types.VARCHAR); 
            }else {
                ps.setString(12, normalized.getRequestUserAgent());
            }

            if (normalized.getRequestCookie() == null) {
                ps.setNull(13, Types.VARCHAR); 
            }else {
                ps.setString(13, EncryptedDbConfigStore.encryptSecretForStorage(normalized.getRequestCookie()));
            }

            if (normalized.getApiKeyHeaderName() == null) {
                ps.setNull(14, Types.VARCHAR);
            } else {
                ps.setString(14, normalized.getApiKeyHeaderName());
            }

            if (normalized.getApiKeyValue() == null) {
                ps.setNull(15, Types.VARCHAR);
            } else {
                ps.setString(15, EncryptedDbConfigStore.encryptSecretForStorage(normalized.getApiKeyValue()));
            }

            if (normalized.getUpdatedBy() == null) {
                ps.setNull(16, Types.VARCHAR); 
            }else {
                ps.setString(16, normalized.getUpdatedBy());
            }

            ps.setTimestamp(17, Timestamp.from(
                    normalized.getUpdatedAt() == null ? Instant.now() : normalized.getUpdatedAt()
            ));

            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            log.log(Level.SEVERE, MSG_FAILED_SAVE_CONFIG, e);
            throw e;
        }

        return load();
    }

    private WidgetHealthConfig mapRow(ResultSet rs) throws java.sql.SQLException {
        WidgetHealthConfig cfg = new WidgetHealthConfig();
        cfg.setId(readNonNegativeInt(rs, COL_ID));
        cfg.setHealthcheckUrl(trimToNull(readSanitizedDbText(rs, COL_HEALTHCHECK_URL, 2048)));
        cfg.setHealthcheckEnabled(readSafeBoolean(rs, COL_HEALTHCHECK_ENABLED, true));
        cfg.setCheckIntervalSeconds(readPositiveInt(rs, COL_CHECK_INTERVAL_SECONDS, DEFAULT_CHECK_INTERVAL_SECONDS));
        cfg.setMethod(defaultIfBlank(readSanitizedDbText(rs, COL_METHOD, 16), HTTP_METHOD_GET));
        cfg.setTimeoutMs(readPositiveInt(rs, COL_TIMEOUT_MS, 1));
        cfg.setExpectJsonField(trimToNull(readSanitizedDbText(rs, COL_EXPECT_JSON_FIELD, 100)));
        cfg.setExpectJsonValue(trimToNull(readSanitizedDbText(rs, COL_EXPECT_JSON_VALUE, 255)));
        cfg.setWidgetId(trimToNull(readSanitizedDbText(rs, COL_WIDGET_ID, 255)));
        cfg.setRequestOrigin(trimToNull(readSanitizedDbText(rs, COL_REQUEST_ORIGIN, 2048)));
        cfg.setRequestReferer(trimToNull(readSanitizedDbText(rs, COL_REQUEST_REFERER, 2048)));
        cfg.setRequestUserAgent(trimToNull(readSanitizedDbText(rs, COL_REQUEST_USER_AGENT, 1024)));
        cfg.setRequestCookie(trimToNull(readDecryptedSecret(rs, COL_REQUEST_COOKIE, 4096)));
        cfg.setApiKeyHeaderName(trimToNull(readSanitizedDbText(rs, COL_API_KEY_HEADER_NAME, 255)));
        cfg.setApiKeyValue(trimToNull(readDecryptedSecret(rs, COL_API_KEY_VALUE, 4096)));
        cfg.setUpdatedBy(trimToNull(readSanitizedDbText(rs, COL_UPDATED_BY, 100)));

        Timestamp ts = readSafeTimestamp(rs, COL_UPDATED_AT);
        cfg.setUpdatedAt(ts == null ? null : ts.toInstant());

        return normalize(cfg);
    }

    private WidgetHealthConfig normalize(WidgetHealthConfig in) {
        WidgetHealthConfig out = new WidgetHealthConfig();

        out.setId(SINGLETON_ID);

        String url = trimToNull(in.getHealthcheckUrl());
        out.setHealthcheckUrl(url == null ? DEFAULT_HEALTHCHECK_URL : url);

        out.setHealthcheckEnabled(in.isHealthcheckEnabled());

        int intervalSeconds = in.getCheckIntervalSeconds() <= 0
                ? DEFAULT_CHECK_INTERVAL_SECONDS
                : in.getCheckIntervalSeconds();
        if (intervalSeconds < 30) {
            intervalSeconds = 30;
        }
        if (intervalSeconds > 86_400) {
            intervalSeconds = 86_400;
        }
        out.setCheckIntervalSeconds(intervalSeconds);

        String method = defaultIfBlank(in.getMethod(), HTTP_METHOD_GET).trim().toUpperCase();
        if (!HTTP_METHOD_GET.equals(method) && !HTTP_METHOD_HEAD.equals(method) && !HTTP_METHOD_POST.equals(method)) {
            method = HTTP_METHOD_GET;
        }
        out.setMethod(method);

        int timeout = in.getTimeoutMs() <= 0 ? 8000 : in.getTimeoutMs();
        if (timeout > 120000) {
            timeout = 120000;
        }
        out.setTimeoutMs(timeout);

        out.setExpectJsonField(trimToNull(in.getExpectJsonField()));
        out.setExpectJsonValue(trimToNull(in.getExpectJsonValue()));
        out.setWidgetId(trimToNull(in.getWidgetId()));

        // New request-shaping fields for synthetic POST probes
        out.setRequestOrigin(trimToNull(in.getRequestOrigin()));
        out.setRequestReferer(trimToNull(in.getRequestReferer()));
        out.setRequestUserAgent(trimToNull(in.getRequestUserAgent()));
        out.setRequestCookie(trimToNull(in.getRequestCookie()));

        String apiKeyValue = trimToNull(in.getApiKeyValue());
        String apiKeyHeaderName = trimToNull(in.getApiKeyHeaderName());
        if (apiKeyValue != null && apiKeyHeaderName == null) {
            apiKeyHeaderName = HEADER_AUTHORIZATION;
        }
        out.setApiKeyHeaderName(apiKeyHeaderName);
        out.setApiKeyValue(apiKeyValue);

        out.setUpdatedBy(trimToNull(in.getUpdatedBy()));
        out.setUpdatedAt(in.getUpdatedAt() == null ? Instant.now() : in.getUpdatedAt());

        return out;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String defaultIfBlank(String s, String d) {
        if (s == null || s.trim().isEmpty()) {
            return d;
        }
        return s;
    }

    private int readNonNegativeInt(ResultSet rs, String column) throws java.sql.SQLException {
        String text = TextIoSanitizerUtil.validateCanonicalized(readRawDbText(rs, column), 64);
        if (text == null || text.isBlank() || !text.matches(SIGNED_INTEGER_PATTERN)) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(text.trim());
            return Math.max(0, validateDbInt(parsed, column, 0));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_INTEGER_TEXT + column, ex);
            return 0;
        }
    }

    private int readPositiveInt(ResultSet rs, String column, int fallback) throws java.sql.SQLException {
        String text = TextIoSanitizerUtil.validateCanonicalized(readRawDbText(rs, column), 64);
        if (text == null || text.isBlank() || !text.matches(SIGNED_INTEGER_PATTERN)) {
            return fallback;
        }
        try {
            int value = validateDbInt(Integer.parseInt(text.trim()), column, fallback);
            return value > 0 ? value : fallback;
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_POSITIVE_INTEGER_TEXT + column, ex);
            return fallback;
        }
    }

    private boolean readSafeBoolean(ResultSet rs, String column, boolean fallback) throws java.sql.SQLException {
        String text = TextIoSanitizerUtil.validateCanonicalized(readRawDbText(rs, column), 16);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        text = text.trim().toLowerCase(Locale.ROOT);
        if (BOOL_TRUE.equals(text) || BOOL_ONE.equals(text) || BOOL_YES.equals(text) || BOOL_Y.equals(text)) {
            return true;
        }
        if (BOOL_FALSE.equals(text) || BOOL_ZERO.equals(text) || BOOL_NO.equals(text) || BOOL_N.equals(text)) {
            return false;
        }
        return fallback;
    }

    private String readSanitizedDbText(ResultSet rs, String column, int maxChars) throws java.sql.SQLException {
        String value = readRawDbText(rs, column);
        return TextIoSanitizerUtil.validateCanonicalized(value, maxChars);
    }

    private Timestamp readSafeTimestamp(ResultSet rs, String column) throws java.sql.SQLException {
        String text = readSanitizedDbText(rs, column, 128);
        if (text == null || text.isBlank()) {
            return null;
        }

        Timestamp value;
        try {
            value = Timestamp.from(Instant.parse(text.trim()));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, MSG_INSTANT_PARSE_FALLBACK + column, ex);
            try {
                value = Timestamp.valueOf(text.replace('T', ' '));
            } catch (IllegalArgumentException parseEx) {
                log.log(Level.FINE, MSG_TIMESTAMP_PARSE_FALLBACK_FAILED + column, parseEx);
                return null;
            }
        }

        return normalizeTimestamp(value, column);
    }

    private Timestamp normalizeTimestamp(Timestamp value, String column) {
        if (value == null) {
            return null;
        }
        try {
            Instant instant = validateDbInstant(value.toInstant(), column);
            return instant == null ? null : Timestamp.from(instant);
        } catch (DateTimeException | IllegalArgumentException e) {
            log.log(Level.FINE, MSG_INVALID_TIMESTAMP_VALUE + column, e);
            return null;
        }
    }

    private String readDecryptedSecret(ResultSet rs, String column, int maxChars) throws java.sql.SQLException {
        String value = readRawDbText(rs, column);
        String decrypted = EncryptedDbConfigStore.decryptSecretIfNeeded(value);
        return TextIoSanitizerUtil.validateCanonicalized(decrypted, maxChars);
    }

    private String readRawDbText(ResultSet rs, String column) throws java.sql.SQLException {
        try {
            Reader reader = rs.getCharacterStream(column);
            if (reader != null) {
                try (Reader closeable = reader) {
                    String raw = TextIoSanitizerUtil.readAtMostChars(closeable, 4096);
                    return TextIoSanitizerUtil.validateCanonicalized(raw, 4096);
                }
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, MSG_RESULTSET_CHAR_STREAM_FAILED + column, ex);
        } catch (IOException ex) {
            log.log(Level.FINE, MSG_CHAR_STREAM_DECODE_FAILED + column, ex);
        }

        try {
            byte[] bytes = rs.getBytes(column);
            if (bytes == null) {
                return null;
            }
            byte[] safeBytes = validateTaintedDbBytes(bytes, 4096);
            String raw = new String(safeBytes, StandardCharsets.UTF_8);
                return TextIoSanitizerUtil.validateCanonicalized(raw, 4096);
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, MSG_RESULTSET_BINARY_FAILED + column, ex);
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return TextIoSanitizerUtil.validateCanonicalized(raw, 4096);
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, MSG_RESULTSET_STRING_FAILED + column, ex);
        }

        try {
            Object raw = rs.getObject(column);
            if (raw == null) {
                return null;
            }
            return TextIoSanitizerUtil.validateCanonicalized(String.valueOf(raw), 4096);
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, MSG_RESULTSET_OBJECT_FAILED + column, ex);
            return null;
        }
    }

    private String sanitizeDbText(String s, int maxChars) {
        return TextIoSanitizerUtil.validateCanonicalized(s, maxChars);
    }

    private byte[] validateTaintedDbBytes(byte[] value, int maxBytes) {
        if (value == null) {
            return EMPTY_BYTES;
        }
        if (maxBytes <= 0 || value.length <= maxBytes) {
            return value;
        }
        return Arrays.copyOf(value, maxBytes);
    }

    private int validateDbInt(int value, String column, int fallback) {
        if (value < 0) {
            log.log(Level.FINE, MSG_NEGATIVE_NUMERIC_VALUE + column + MSG_FALLING_BACK);
            return fallback;
        }
        return value;
    }

    private Instant validateDbInstant(Instant instant, String column) {
        if (instant == null) {
            return null;
        }
        if (instant.isBefore(MIN_ALLOWED_INSTANT) || instant.isAfter(MAX_ALLOWED_INSTANT)) {
            log.log(Level.FINE, MSG_OUT_OF_RANGE_TIMESTAMP + column);
            return null;
        }
        return instant;
    }

}

