package com.sim.chatserver.service.widget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Stores/retrieves widget availability health-check configuration in
 * PostgreSQL.
 *
 * Single-row model (id=1) for now.
 */
public class WidgetHealthConfigStore {

    private static final Logger log = Logger.getLogger(WidgetHealthConfigStore.class.getName());

    public static final int SINGLETON_ID = 1;

    private final DataSource dataSource;

    public WidgetHealthConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void ensureTable() throws java.sql.SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS widget_health_config (
                id INT PRIMARY KEY,
                healthcheck_url TEXT NOT NULL,
                method VARCHAR(10) NOT NULL DEFAULT 'GET',
                timeout_ms INT NOT NULL DEFAULT 8000,
                expect_json_field VARCHAR(100),
                expect_json_value VARCHAR(255),
                widget_id VARCHAR(255),
                request_origin TEXT,
                request_referer TEXT,
                request_user_agent TEXT,
                request_cookie TEXT,
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
                id, healthcheck_url, method, timeout_ms, expect_json_field, expect_json_value, widget_id,
                request_origin, request_referer, request_user_agent, request_cookie,
                updated_by, updated_at
            )
            VALUES (?, ?, 'GET', 8000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system', NOW())
            ON CONFLICT (id) DO NOTHING
            """;

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, SINGLETON_ID);
            ps.setString(2, "http://anythingllm:3001/api/health");
            ps.executeUpdate();
        }
    }

    public WidgetHealthConfig load() throws java.sql.SQLException {
        final String sql = """
            SELECT id, healthcheck_url, method, timeout_ms,
                   expect_json_field, expect_json_value, widget_id,
                   request_origin, request_referer, request_user_agent, request_cookie,
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
                expect_json_field, expect_json_value, widget_id,
                request_origin, request_referer, request_user_agent, request_cookie,
                updated_by, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                healthcheck_url = EXCLUDED.healthcheck_url,
                method = EXCLUDED.method,
                timeout_ms = EXCLUDED.timeout_ms,
                expect_json_field = EXCLUDED.expect_json_field,
                expect_json_value = EXCLUDED.expect_json_value,
                widget_id = EXCLUDED.widget_id,
                request_origin = EXCLUDED.request_origin,
                request_referer = EXCLUDED.request_referer,
                request_user_agent = EXCLUDED.request_user_agent,
                request_cookie = EXCLUDED.request_cookie,
                updated_by = EXCLUDED.updated_by,
                updated_at = EXCLUDED.updated_at
            """;

        WidgetHealthConfig normalized = normalize(in);

        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, SINGLETON_ID);
            ps.setString(2, normalized.getHealthcheckUrl());
            ps.setString(3, normalized.getMethod());
            ps.setInt(4, normalized.getTimeoutMs());

            if (normalized.getExpectJsonField() == null) {
                ps.setNull(5, Types.VARCHAR); 
            }else {
                ps.setString(5, normalized.getExpectJsonField());
            }

            if (normalized.getExpectJsonValue() == null) {
                ps.setNull(6, Types.VARCHAR); 
            }else {
                ps.setString(6, normalized.getExpectJsonValue());
            }

            if (normalized.getWidgetId() == null) {
                ps.setNull(7, Types.VARCHAR); 
            }else {
                ps.setString(7, normalized.getWidgetId());
            }

            if (normalized.getRequestOrigin() == null) {
                ps.setNull(8, Types.VARCHAR); 
            }else {
                ps.setString(8, normalized.getRequestOrigin());
            }

            if (normalized.getRequestReferer() == null) {
                ps.setNull(9, Types.VARCHAR); 
            }else {
                ps.setString(9, normalized.getRequestReferer());
            }

            if (normalized.getRequestUserAgent() == null) {
                ps.setNull(10, Types.VARCHAR); 
            }else {
                ps.setString(10, normalized.getRequestUserAgent());
            }

            if (normalized.getRequestCookie() == null) {
                ps.setNull(11, Types.VARCHAR); 
            }else {
                ps.setString(11, normalized.getRequestCookie());
            }

            if (normalized.getUpdatedBy() == null) {
                ps.setNull(12, Types.VARCHAR); 
            }else {
                ps.setString(12, normalized.getUpdatedBy());
            }

            ps.setTimestamp(13, Timestamp.from(
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
        cfg.setMethod(defaultIfBlank(readSanitizedDbText(rs, "method", 16), "GET"));
        cfg.setTimeoutMs(readPositiveInt(rs, "timeout_ms", 1));
        cfg.setExpectJsonField(trimToNull(readSanitizedDbText(rs, "expect_json_field", 100)));
        cfg.setExpectJsonValue(trimToNull(readSanitizedDbText(rs, "expect_json_value", 255)));
        cfg.setWidgetId(trimToNull(readSanitizedDbText(rs, "widget_id", 255)));
        cfg.setRequestOrigin(trimToNull(readSanitizedDbText(rs, "request_origin", 2048)));
        cfg.setRequestReferer(trimToNull(readSanitizedDbText(rs, "request_referer", 2048)));
        cfg.setRequestUserAgent(trimToNull(readSanitizedDbText(rs, "request_user_agent", 1024)));
        cfg.setRequestCookie(trimToNull(readSanitizedDbText(rs, "request_cookie", 4096)));
        cfg.setUpdatedBy(trimToNull(readSanitizedDbText(rs, "updated_by", 100)));

        Timestamp ts = readSafeTimestamp(rs, "updated_at");
        cfg.setUpdatedAt(ts == null ? null : ts.toInstant());

        return normalize(cfg);
    }

    private WidgetHealthConfig normalize(WidgetHealthConfig in) {
        WidgetHealthConfig out = new WidgetHealthConfig();

        out.setId(SINGLETON_ID);

        String url = trimToNull(in.getHealthcheckUrl());
        out.setHealthcheckUrl(url == null ? "http://anythingllm:3001/api/health" : url);

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
        int value = rs.getInt(column);
        if (rs.wasNull()) {
            return 0;
        }
        return Math.max(0, value);
    }

    private int readPositiveInt(ResultSet rs, String column, int fallback) throws java.sql.SQLException {
        int value = rs.getInt(column);
        if (rs.wasNull()) {
            return fallback;
        }
        return value > 0 ? value : fallback;
    }

    private String readSanitizedDbText(ResultSet rs, String column, int maxChars) throws java.sql.SQLException {
        return sanitizeDbText(rs.getString(column), maxChars);
    }

    private Timestamp readSafeTimestamp(ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp value = rs.getTimestamp(column);
        if (value == null) {
            return null;
        }
        try {
            Instant instant = value.toInstant();
            return instant == null ? null : Timestamp.from(instant);
        } catch (DateTimeException | IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid timestamp value for column " + column, e);
            return null;
        }
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

        private String updatedBy;
        private Instant updatedAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getHealthcheckUrl() {
            return healthcheckUrl;
        }

        public void setHealthcheckUrl(String healthcheckUrl) {
            this.healthcheckUrl = healthcheckUrl;
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
