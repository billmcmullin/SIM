package com.sim.chatserver.service.widget;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;

/**
 * Stores/retrieves widget availability health-check configuration in
 * PostgreSQL.
 *
 * Single-row model (id=1) for now.
 */
public class WidgetHealthConfigStore {

    private static final Logger log = Logger.getLogger(WidgetHealthConfigStore.class.getName());
    private static final String DEFAULT_HEALTHCHECK_URL = "http://anythingllm:3001/api/v1/system";
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 300;

    public static final int SINGLETON_ID = 1;

    private final DataSource dataSource;

    public WidgetHealthConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public void ensureTable() throws java.sql.SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS widget_health_config (
                id INT PRIMARY KEY,
                healthcheck_url TEXT NOT NULL,
                healthcheck_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                check_interval_seconds INT NOT NULL DEFAULT 300,
                method VARCHAR(10) NOT NULL DEFAULT 'GET',
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
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }

        // Backward-compatible migrations for existing DBs
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_origin TEXT");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_referer TEXT");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_user_agent TEXT");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS request_cookie TEXT");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS api_key_header_name VARCHAR(255)");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS api_key_value TEXT");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS healthcheck_enabled BOOLEAN NOT NULL DEFAULT TRUE");
        addColumnIfMissing("ALTER TABLE widget_health_config ADD COLUMN IF NOT EXISTS check_interval_seconds INT NOT NULL DEFAULT 300");
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
            VALUES (?, ?, TRUE, 300, 'GET', 8000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system', NOW())
            ON CONFLICT (id) DO NOTHING
            """;

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
            throw new IllegalArgumentException("WidgetHealthConfig cannot be null");
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
            log.log(Level.SEVERE, "Failed to save widget health config", e);
            throw e;
        }

        return load();
    }

    private WidgetHealthConfig mapRow(ResultSet rs) throws java.sql.SQLException {
        WidgetHealthConfig cfg = new WidgetHealthConfig();
        cfg.setId(readNonNegativeInt(rs, "id"));
        cfg.setHealthcheckUrl(trimToNull(readSanitizedDbText(rs, "healthcheck_url", 2048)));
        cfg.setHealthcheckEnabled(readSafeBoolean(rs, "healthcheck_enabled", true));
        cfg.setCheckIntervalSeconds(readPositiveInt(rs, "check_interval_seconds", DEFAULT_CHECK_INTERVAL_SECONDS));
        cfg.setMethod(defaultIfBlank(readSanitizedDbText(rs, "method", 16), "GET"));
        cfg.setTimeoutMs(readPositiveInt(rs, "timeout_ms", 1));
        cfg.setExpectJsonField(trimToNull(readSanitizedDbText(rs, "expect_json_field", 100)));
        cfg.setExpectJsonValue(trimToNull(readSanitizedDbText(rs, "expect_json_value", 255)));
        cfg.setWidgetId(trimToNull(readSanitizedDbText(rs, "widget_id", 255)));
        cfg.setRequestOrigin(trimToNull(readSanitizedDbText(rs, "request_origin", 2048)));
        cfg.setRequestReferer(trimToNull(readSanitizedDbText(rs, "request_referer", 2048)));
        cfg.setRequestUserAgent(trimToNull(readSanitizedDbText(rs, "request_user_agent", 1024)));
        cfg.setRequestCookie(trimToNull(readDecryptedSecret(rs, "request_cookie", 4096)));
        cfg.setApiKeyHeaderName(trimToNull(readSanitizedDbText(rs, "api_key_header_name", 255)));
        cfg.setApiKeyValue(trimToNull(readDecryptedSecret(rs, "api_key_value", 4096)));
        cfg.setUpdatedBy(trimToNull(readSanitizedDbText(rs, "updated_by", 100)));

        Timestamp ts = readSafeTimestamp(rs, "updated_at");
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

        String method = defaultIfBlank(in.getMethod(), "GET").trim().toUpperCase();
        if (!"GET".equals(method) && !"HEAD".equals(method) && !"POST".equals(method)) {
            method = "GET";
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
            apiKeyHeaderName = "Authorization";
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
        try {
            int typed = rs.getInt(column);
            if (!rs.wasNull()) {
                return Math.max(0, typed);
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getInt(String) failed for column " + column, ex);
        }

        String text = readRawDbText(rs, column);
        if (text == null || text.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(text.trim()));
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer text for column " + column, ex);
            return 0;
        }
    }

    private int readPositiveInt(ResultSet rs, String column, int fallback) throws java.sql.SQLException {
        try {
            int typed = rs.getInt(column);
            if (!rs.wasNull()) {
                if (typed > 0) {
                    return typed;
                }
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getInt(String) failed for column " + column, ex);
        }

        String text = readRawDbText(rs, column);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : fallback;
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid positive integer text for column " + column, ex);
            return fallback;
        }
    }

    private boolean readSafeBoolean(ResultSet rs, String column, boolean fallback) throws java.sql.SQLException {
        try {
            boolean typed = rs.getBoolean(column);
            if (!rs.wasNull()) {
                return typed;
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getBoolean(String) failed for column " + column, ex);
        }

        String text = readRawDbText(rs, column);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        text = text.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(text) || "1".equals(text) || "yes".equals(text) || "y".equals(text)) {
            return true;
        }
        if ("false".equals(text) || "0".equals(text) || "no".equals(text) || "n".equals(text)) {
            return false;
        }
        return fallback;
    }

    private String readSanitizedDbText(ResultSet rs, String column, int maxChars) throws java.sql.SQLException {
        String value = readRawDbText(rs, column);
        return sanitizeDbText(value, maxChars);
    }

    private Timestamp readSafeTimestamp(ResultSet rs, String column) throws java.sql.SQLException {
        try {
            Timestamp typed = rs.getTimestamp(column);
            if (typed != null) {
                return typed;
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getTimestamp(String) failed for column " + column, ex);
        }

        String text = readSanitizedDbText(rs, column, 128);
        if (text == null || text.isBlank()) {
            return null;
        }

        Timestamp value;
        try {
            value = Timestamp.from(Instant.parse(text.trim()));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Instant timestamp parse fallback for column " + column, ex);
            value = Timestamp.valueOf(text.replace('T', ' '));
        }

        try {
            Instant instant = value.toInstant();
            return instant == null ? null : Timestamp.from(instant);
        } catch (DateTimeException | IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid timestamp value for column " + column, e);
            return null;
        }
    }

    private String readDecryptedSecret(ResultSet rs, String column, int maxChars) throws java.sql.SQLException {
        String value = readRawDbText(rs, column);
        String decrypted = EncryptedDbConfigStore.decryptSecretIfNeeded(value);
        return sanitizeDbText(decrypted, maxChars);
    }

    private String readRawDbText(ResultSet rs, String column) throws java.sql.SQLException {
        try {
            Object raw = rs.getObject(column);
            if (raw == null) {
                String fallback = rs.getString(column);
                return fallback == null ? null : sanitizeDbText(fallback, 4096);
            }
            if (raw instanceof byte[] bytes) {
                return sanitizeDbText(new String(bytes, StandardCharsets.UTF_8), 4096);
            }
            return sanitizeDbText(String.valueOf(raw), 4096);
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getObject(String) failed for column " + column + ", trying stream fallback", ex);
        }

        try (Reader reader = rs.getCharacterStream(column)) {
            if (reader != null) {
                return sanitizeDbText(readBoundedText(reader, 4096), 4096);
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getCharacterStream(String) failed for column " + column, ex);
        } catch (IOException ex) {
            log.log(Level.FINE, "Character stream read failed for column " + column, ex);
        }

        try (Reader reader = rs.getNCharacterStream(column)) {
            if (reader != null) {
                return sanitizeDbText(readBoundedText(reader, 4096), 4096);
            }
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet#getNCharacterStream(String) failed for column " + column, ex);
        } catch (IOException ex) {
            log.log(Level.FINE, "NCharacter stream read failed for column " + column, ex);
        }

        try {
            String fallback = rs.getString(column);
            return fallback == null ? null : sanitizeDbText(fallback, 4096);
        } catch (java.sql.SQLException ex) {
            log.log(Level.FINE, "ResultSet textual read failed for column " + column, ex);
            return null;
        }
    }

    private String readBoundedText(Reader reader, int maxChars) throws IOException {
        if (reader == null || maxChars <= 0) {
            return "";
        }
        char[] buffer = new char[256];
        StringBuilder out = new StringBuilder(Math.max(64, Math.min(maxChars, 512)));
        int total = 0;
        int read;
        while ((read = reader.read(buffer)) != -1) {
            total += read;
            if (total > maxChars) {
                int remaining = Math.max(0, maxChars - (total - read));
                if (remaining > 0) {
                    out.append(buffer, 0, remaining);
                }
                break;
            }
            out.append(buffer, 0, read);
        }
        return out.toString();
    }

    private String sanitizeDbText(String s, int maxChars) {
        if (s == null) {
            return null;
        }
        String trimmed = Normalizer.normalize(s, Normalizer.Form.NFKC).trim();
        if (maxChars <= 0 || trimmed.length() <= maxChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxChars);
    }

    /**
     * Model object for DB-backed widget health config.
     */
    public static class WidgetHealthConfig {

        private int id;
        private String healthcheckUrl;
        private boolean healthcheckEnabled = true;
        private int checkIntervalSeconds = DEFAULT_CHECK_INTERVAL_SECONDS;
        private String method;
        private int timeoutMs;
        private String expectJsonField;
        private String expectJsonValue;
        private String widgetId;

        // New: optional request-shaping headers for synthetic probe
        private String requestOrigin;
        private String requestReferer;
        private String requestUserAgent;
        private String requestCookie;

        private String apiKeyHeaderName;
        private String apiKeyValue;

        private String updatedBy;
        private Instant updatedAt;

        public int getId() {
            return id;
        }

        private final void setId(int id) {
            this.id = id;
        }

        private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
            throw new java.io.NotSerializableException(getClass().getName());
        }

        private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
            throw new java.io.NotSerializableException(getClass().getName());
        }

        public String getHealthcheckUrl() {
            return healthcheckUrl;
        }

        public void setHealthcheckUrl(String healthcheckUrl) {
            this.healthcheckUrl = healthcheckUrl;
        }

        public boolean isHealthcheckEnabled() {
            return healthcheckEnabled;
        }

        public void setHealthcheckEnabled(boolean healthcheckEnabled) {
            this.healthcheckEnabled = healthcheckEnabled;
        }

        public int getCheckIntervalSeconds() {
            return checkIntervalSeconds;
        }

        public void setCheckIntervalSeconds(int checkIntervalSeconds) {
            this.checkIntervalSeconds = checkIntervalSeconds;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public String getExpectJsonField() {
            return expectJsonField;
        }

        public void setExpectJsonField(String expectJsonField) {
            this.expectJsonField = expectJsonField;
        }

        public String getExpectJsonValue() {
            return expectJsonValue;
        }

        public void setExpectJsonValue(String expectJsonValue) {
            this.expectJsonValue = expectJsonValue;
        }

        public String getWidgetId() {
            return widgetId;
        }

        public void setWidgetId(String widgetId) {
            this.widgetId = widgetId;
        }

        public String getRequestOrigin() {
            return requestOrigin;
        }

        public void setRequestOrigin(String requestOrigin) {
            this.requestOrigin = requestOrigin;
        }

        public String getRequestReferer() {
            return requestReferer;
        }

        public void setRequestReferer(String requestReferer) {
            this.requestReferer = requestReferer;
        }

        public String getRequestUserAgent() {
            return requestUserAgent;
        }

        public void setRequestUserAgent(String requestUserAgent) {
            this.requestUserAgent = requestUserAgent;
        }

        public String getRequestCookie() {
            return requestCookie;
        }

        public void setRequestCookie(String requestCookie) {
            this.requestCookie = requestCookie;
        }

        public String getApiKeyHeaderName() {
            return apiKeyHeaderName;
        }

        public void setApiKeyHeaderName(String apiKeyHeaderName) {
            this.apiKeyHeaderName = apiKeyHeaderName;
        }

        public String getApiKeyValue() {
            return apiKeyValue;
        }

        public void setApiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
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
