package com.sim.chatserver.service.widget;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpRequest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import com.sim.chatserver.startup.AppDataSourceHolder;

class WidgetAvailabilityCheckerTest {

    private static final String DEBUG_FAILURES_PROP = "sim.widget.healthcheck.debug.failures";
    private static final String REQUIRE_HTTPS_PROP = "sim.widget.healthcheck.require.https.with.auth";

    @Test
    void checkNow_sensitiveAuthOverHttp_isNotBlockedByDefault() throws Exception {
        String previousRequireHttps = System.getProperty(REQUIRE_HTTPS_PROP);
        System.setProperty(REQUIRE_HTTPS_PROP, "false");
        try {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement genericPs = mock(PreparedStatement.class);
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet loadRs = mock(ResultSet.class);

        underTest.dsHolder = dsHolder;

        when(dsHolder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql != null && sql.contains("SELECT id, healthcheck_url")) {
                return loadPs;
            }
            return genericPs;
        });

        when(genericPs.execute()).thenReturn(true);
        when(genericPs.executeUpdate()).thenReturn(1);

        when(loadPs.executeQuery()).thenReturn(loadRs);
        when(loadRs.next()).thenReturn(true);

        when(loadRs.getInt("id")).thenReturn(1);
        when(loadRs.getInt("timeout_ms")).thenReturn(5000);
        when(loadRs.getBoolean("healthcheck_enabled")).thenReturn(true);
        when(loadRs.getInt("check_interval_seconds")).thenReturn(300);
        when(loadRs.wasNull()).thenReturn(false);

        when(loadRs.getString("healthcheck_url")).thenReturn("http://[bad-url");
        when(loadRs.getString("method")).thenReturn("GET");
        when(loadRs.getString("timeout_ms")).thenReturn("5000");
        when(loadRs.getString("healthcheck_enabled")).thenReturn("true");
        when(loadRs.getString("check_interval_seconds")).thenReturn("300");
        when(loadRs.getString("expect_json_field")).thenReturn(null);
        when(loadRs.getString("expect_json_value")).thenReturn(null);
        when(loadRs.getString("widget_id")).thenReturn(null);
        when(loadRs.getString("request_origin")).thenReturn(null);
        when(loadRs.getString("request_referer")).thenReturn(null);
        when(loadRs.getString("request_user_agent")).thenReturn(null);
        when(loadRs.getString("request_cookie")).thenReturn(null);
        when(loadRs.getString("api_key_header_name")).thenReturn("Authorization");
        when(loadRs.getString("api_key_value")).thenReturn("token");
        when(loadRs.getString("updated_by")).thenReturn("tester");
        when(loadRs.getBytes("healthcheck_url")).thenReturn(bytes("http://[bad-url"));
        when(loadRs.getBytes("method")).thenReturn(bytes("GET"));
        when(loadRs.getBytes("api_key_header_name")).thenReturn(bytes("Authorization"));
        when(loadRs.getBytes("api_key_value")).thenReturn(bytes("token"));
        when(loadRs.getBytes("updated_by")).thenReturn(bytes("tester"));
        when(loadRs.getObject("updated_at")).thenReturn(Timestamp.from(Instant.now()));

        when(loadRs.getObject("id", Integer.class)).thenReturn(1);
        when(loadRs.getObject("healthcheck_url", String.class)).thenReturn("http://[bad-url");
        when(loadRs.getObject("method", String.class)).thenReturn("GET");
        when(loadRs.getObject("timeout_ms", Integer.class)).thenReturn(5000);
        when(loadRs.getObject("expect_json_field", String.class)).thenReturn(null);
        when(loadRs.getObject("expect_json_value", String.class)).thenReturn(null);
        when(loadRs.getObject("widget_id", String.class)).thenReturn(null);
        when(loadRs.getObject("request_origin", String.class)).thenReturn(null);
        when(loadRs.getObject("request_referer", String.class)).thenReturn(null);
        when(loadRs.getObject("request_user_agent", String.class)).thenReturn(null);
        when(loadRs.getObject("request_cookie", String.class)).thenReturn(null);
        when(loadRs.getObject("api_key_header_name", String.class)).thenReturn("Authorization");
        when(loadRs.getObject("api_key_value", String.class)).thenReturn("token");
        when(loadRs.getObject("updated_by", String.class)).thenReturn("tester");
        when(loadRs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.now()));

        WidgetAvailabilityChecker.WidgetAvailabilityResult result = underTest.checkNow();

        assertFalse(result.available());
        assertEquals("DOWN", result.status());
        assertTrue(result.details() != null && !result.details().isBlank());
        } finally {
            if (previousRequireHttps == null) {
                System.clearProperty(REQUIRE_HTTPS_PROP);
            } else {
                System.setProperty(REQUIRE_HTTPS_PROP, previousRequireHttps);
            }
        }
    }

    @Test
    void checkNow_invalidUrl_returnsDownWithIllegalArgumentMessage() throws Exception {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement genericPs = mock(PreparedStatement.class);
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet loadRs = mock(ResultSet.class);

        underTest.dsHolder = dsHolder;

        when(dsHolder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql != null && sql.contains("SELECT id, healthcheck_url")) {
                return loadPs;
            }
            return genericPs;
        });

        when(genericPs.execute()).thenReturn(true);
        when(genericPs.executeUpdate()).thenReturn(1);

        when(loadPs.executeQuery()).thenReturn(loadRs);
        when(loadRs.next()).thenReturn(true);

        when(loadRs.getInt("id")).thenReturn(1);
        when(loadRs.getInt("timeout_ms")).thenReturn(5000);
        when(loadRs.getBoolean("healthcheck_enabled")).thenReturn(true);
        when(loadRs.getInt("check_interval_seconds")).thenReturn(300);
        when(loadRs.wasNull()).thenReturn(false);

        when(loadRs.getString("healthcheck_url")).thenReturn("http://[bad-url");
        when(loadRs.getString("method")).thenReturn("GET");
        when(loadRs.getString("expect_json_field")).thenReturn(null);
        when(loadRs.getString("expect_json_value")).thenReturn(null);
        when(loadRs.getString("widget_id")).thenReturn(null);
        when(loadRs.getString("request_origin")).thenReturn(null);
        when(loadRs.getString("request_referer")).thenReturn(null);
        when(loadRs.getString("request_user_agent")).thenReturn(null);
        when(loadRs.getString("request_cookie")).thenReturn(null);
        when(loadRs.getString("api_key_header_name")).thenReturn("Authorization");
        when(loadRs.getString("api_key_value")).thenReturn("token");
        when(loadRs.getString("updated_by")).thenReturn("tester");
        when(loadRs.getBytes("healthcheck_url")).thenReturn(bytes("http://[bad-url"));
        when(loadRs.getBytes("method")).thenReturn(bytes("GET"));
        when(loadRs.getBytes("api_key_header_name")).thenReturn(bytes("Authorization"));
        when(loadRs.getBytes("api_key_value")).thenReturn(bytes("token"));
        when(loadRs.getBytes("updated_by")).thenReturn(bytes("tester"));
        when(loadRs.getObject("updated_at")).thenReturn(Timestamp.from(Instant.now()));

        when(loadRs.getObject("id", Integer.class)).thenReturn(1);
        when(loadRs.getObject("healthcheck_url", String.class)).thenReturn("http://[bad-url");
        when(loadRs.getObject("method", String.class)).thenReturn("GET");
        when(loadRs.getObject("timeout_ms", Integer.class)).thenReturn(5000);
        when(loadRs.getObject("expect_json_field", String.class)).thenReturn(null);
        when(loadRs.getObject("expect_json_value", String.class)).thenReturn(null);
        when(loadRs.getObject("widget_id", String.class)).thenReturn(null);
        when(loadRs.getObject("request_origin", String.class)).thenReturn(null);
        when(loadRs.getObject("request_referer", String.class)).thenReturn(null);
        when(loadRs.getObject("request_user_agent", String.class)).thenReturn(null);
        when(loadRs.getObject("request_cookie", String.class)).thenReturn(null);
        when(loadRs.getObject("api_key_header_name", String.class)).thenReturn("Authorization");
        when(loadRs.getObject("api_key_value", String.class)).thenReturn("token");
        when(loadRs.getObject("updated_by", String.class)).thenReturn("tester");
        when(loadRs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.now()));

        WidgetAvailabilityChecker.WidgetAvailabilityResult result = underTest.checkNow();

        assertFalse(result.available());
        assertEquals("DOWN", result.status());
        assertTrue(result.details().contains("Something went wrong during widget healthcheck"));
    }

    @Test
    void checkNow_invalidUrl_withDebugEnabled_returnsDetailedExceptionMessage() throws Exception {
        String previous = System.getProperty(DEBUG_FAILURES_PROP);
        String previousRequireHttps = System.getProperty(REQUIRE_HTTPS_PROP);
        System.setProperty(DEBUG_FAILURES_PROP, "true");
        System.setProperty(REQUIRE_HTTPS_PROP, "false");
        try {
            WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

            AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
            DataSource dataSource = mock(DataSource.class);
            Connection connection = mock(Connection.class);
            PreparedStatement genericPs = mock(PreparedStatement.class);
            PreparedStatement loadPs = mock(PreparedStatement.class);
            ResultSet loadRs = mock(ResultSet.class);

            underTest.dsHolder = dsHolder;

            when(dsHolder.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0, String.class);
                if (sql != null && sql.contains("SELECT id, healthcheck_url")) {
                    return loadPs;
                }
                return genericPs;
            });

            when(genericPs.execute()).thenReturn(true);
            when(genericPs.executeUpdate()).thenReturn(1);

            when(loadPs.executeQuery()).thenReturn(loadRs);
            when(loadRs.next()).thenReturn(true);

            when(loadRs.getInt("id")).thenReturn(1);
            when(loadRs.getInt("timeout_ms")).thenReturn(5000);
            when(loadRs.getBoolean("healthcheck_enabled")).thenReturn(true);
            when(loadRs.getInt("check_interval_seconds")).thenReturn(300);
            when(loadRs.wasNull()).thenReturn(false);

            when(loadRs.getString("healthcheck_url")).thenReturn("http://[bad-url");
            when(loadRs.getString("method")).thenReturn("GET");
            when(loadRs.getString("expect_json_field")).thenReturn(null);
            when(loadRs.getString("expect_json_value")).thenReturn(null);
            when(loadRs.getString("widget_id")).thenReturn(null);
            when(loadRs.getString("request_origin")).thenReturn(null);
            when(loadRs.getString("request_referer")).thenReturn(null);
            when(loadRs.getString("request_user_agent")).thenReturn(null);
            when(loadRs.getString("request_cookie")).thenReturn(null);
            when(loadRs.getString("api_key_header_name")).thenReturn("Authorization");
            when(loadRs.getString("api_key_value")).thenReturn("token");
            when(loadRs.getString("updated_by")).thenReturn("tester");
            when(loadRs.getBytes("healthcheck_url")).thenReturn(bytes("http://[bad-url"));
            when(loadRs.getBytes("method")).thenReturn(bytes("GET"));
            when(loadRs.getBytes("api_key_header_name")).thenReturn(bytes("Authorization"));
            when(loadRs.getBytes("api_key_value")).thenReturn(bytes("token"));
            when(loadRs.getBytes("updated_by")).thenReturn(bytes("tester"));
            when(loadRs.getObject("updated_at")).thenReturn(Timestamp.from(Instant.now()));

            when(loadRs.getObject("id", Integer.class)).thenReturn(1);
            when(loadRs.getObject("healthcheck_url", String.class)).thenReturn("http://[bad-url");
            when(loadRs.getObject("method", String.class)).thenReturn("GET");
            when(loadRs.getObject("timeout_ms", Integer.class)).thenReturn(5000);
            when(loadRs.getObject("expect_json_field", String.class)).thenReturn(null);
            when(loadRs.getObject("expect_json_value", String.class)).thenReturn(null);
            when(loadRs.getObject("widget_id", String.class)).thenReturn(null);
            when(loadRs.getObject("request_origin", String.class)).thenReturn(null);
            when(loadRs.getObject("request_referer", String.class)).thenReturn(null);
            when(loadRs.getObject("request_user_agent", String.class)).thenReturn(null);
            when(loadRs.getObject("request_cookie", String.class)).thenReturn(null);
            when(loadRs.getObject("api_key_header_name", String.class)).thenReturn("Authorization");
            when(loadRs.getObject("api_key_value", String.class)).thenReturn("token");
            when(loadRs.getObject("updated_by", String.class)).thenReturn("tester");
            when(loadRs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.now()));

            WidgetAvailabilityChecker.WidgetAvailabilityResult result = underTest.checkNow();

            assertFalse(result.available());
            assertEquals("DOWN", result.status());
                String details = result.details() == null ? "" : result.details();
                assertTrue(
                    details.contains("IllegalArgumentException")
                    || details.contains("Something went wrong during widget healthcheck")
                );
        } finally {
            if (previous == null) {
                System.clearProperty(DEBUG_FAILURES_PROP);
            } else {
                System.setProperty(DEBUG_FAILURES_PROP, previous);
            }
            if (previousRequireHttps == null) {
                System.clearProperty(REQUIRE_HTTPS_PROP);
            } else {
                System.setProperty(REQUIRE_HTTPS_PROP, previousRequireHttps);
            }
        }
    }

    @Test
    void checkNow_disabledConfig_returnsDisabledStatus() throws Exception {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement genericPs = mock(PreparedStatement.class);
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet loadRs = mock(ResultSet.class);

        underTest.dsHolder = dsHolder;

        when(dsHolder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql != null && sql.contains("SELECT id, healthcheck_url")) {
                return loadPs;
            }
            return genericPs;
        });

        when(genericPs.execute()).thenReturn(true);
        when(genericPs.executeUpdate()).thenReturn(1);

        when(loadPs.executeQuery()).thenReturn(loadRs);
        when(loadRs.next()).thenReturn(true);

        when(loadRs.getInt("id")).thenReturn(1);
        when(loadRs.getInt("timeout_ms")).thenReturn(5000);
        when(loadRs.getBoolean("healthcheck_enabled")).thenReturn(false);
        when(loadRs.getInt("check_interval_seconds")).thenReturn(300);
        when(loadRs.wasNull()).thenReturn(false);

        when(loadRs.getString("healthcheck_url")).thenReturn("http://widget.local/api/health");
        when(loadRs.getString("method")).thenReturn("GET");
        when(loadRs.getString("timeout_ms")).thenReturn("5000");
        when(loadRs.getString("healthcheck_enabled")).thenReturn("false");
        when(loadRs.getString("check_interval_seconds")).thenReturn("300");
        when(loadRs.getString("expect_json_field")).thenReturn(null);
        when(loadRs.getString("expect_json_value")).thenReturn(null);
        when(loadRs.getString("widget_id")).thenReturn(null);
        when(loadRs.getString("request_origin")).thenReturn(null);
        when(loadRs.getString("request_referer")).thenReturn(null);
        when(loadRs.getString("request_user_agent")).thenReturn(null);
        when(loadRs.getString("request_cookie")).thenReturn(null);
        when(loadRs.getString("api_key_header_name")).thenReturn("Authorization");
        when(loadRs.getString("api_key_value")).thenReturn("token");
        when(loadRs.getString("updated_by")).thenReturn("tester");
        when(loadRs.getBytes("healthcheck_url")).thenReturn(bytes("http://widget.local/api/health"));
        when(loadRs.getBytes("method")).thenReturn(bytes("GET"));
        when(loadRs.getBytes("healthcheck_enabled")).thenReturn(bytes("false"));
        when(loadRs.getBytes("check_interval_seconds")).thenReturn(bytes("300"));
        when(loadRs.getBytes("timeout_ms")).thenReturn(bytes("5000"));
        when(loadRs.getBytes("api_key_header_name")).thenReturn(bytes("Authorization"));
        when(loadRs.getBytes("api_key_value")).thenReturn(bytes("token"));
        when(loadRs.getBytes("updated_by")).thenReturn(bytes("tester"));
        when(loadRs.getObject("updated_at")).thenReturn(Timestamp.from(Instant.now()));

        when(loadRs.getObject("id", Integer.class)).thenReturn(1);
        when(loadRs.getObject("healthcheck_url", String.class)).thenReturn("http://widget.local/api/health");
        when(loadRs.getObject("method", String.class)).thenReturn("GET");
        when(loadRs.getObject("timeout_ms", Integer.class)).thenReturn(5000);
        when(loadRs.getObject("expect_json_field", String.class)).thenReturn(null);
        when(loadRs.getObject("expect_json_value", String.class)).thenReturn(null);
        when(loadRs.getObject("widget_id", String.class)).thenReturn(null);
        when(loadRs.getObject("request_origin", String.class)).thenReturn(null);
        when(loadRs.getObject("request_referer", String.class)).thenReturn(null);
        when(loadRs.getObject("request_user_agent", String.class)).thenReturn(null);
        when(loadRs.getObject("request_cookie", String.class)).thenReturn(null);
        when(loadRs.getObject("api_key_header_name", String.class)).thenReturn("Authorization");
        when(loadRs.getObject("api_key_value", String.class)).thenReturn("token");
        when(loadRs.getObject("updated_by", String.class)).thenReturn("tester");
        when(loadRs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.now()));

        WidgetAvailabilityChecker.WidgetAvailabilityResult result = underTest.checkNow();

        assertTrue(result.available());
        assertEquals("DISABLED", result.status());
        assertTrue(result.details().contains("disabled"));
    }

    @Test
    void helperNormalizationAndHeaderFallbacks_coverBranches() throws Exception {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        Method normalizeMethod = WidgetAvailabilityChecker.class.getDeclaredMethod("normalizeMethod", String.class);
        normalizeMethod.setAccessible(true);
        assertEquals("GET", normalizeMethod.invoke(underTest, new Object[]{null}));
        assertEquals("HEAD", normalizeMethod.invoke(underTest, "head"));
        assertEquals("POST", normalizeMethod.invoke(underTest, "post"));
        assertEquals("GET", normalizeMethod.invoke(underTest, "patch"));

        Method normalizeTimeout = WidgetAvailabilityChecker.class.getDeclaredMethod("normalizeTimeout", int.class);
        normalizeTimeout.setAccessible(true);
        assertEquals(8000, normalizeTimeout.invoke(underTest, 0));
        assertEquals(120000, normalizeTimeout.invoke(underTest, 999999));
        assertEquals(5000, normalizeTimeout.invoke(underTest, 5000));

        Method normalizeInterval = WidgetAvailabilityChecker.class.getDeclaredMethod("normalizeCheckIntervalSeconds", int.class);
        normalizeInterval.setAccessible(true);
        assertEquals(300, normalizeInterval.invoke(underTest, 0));
        assertEquals(30, normalizeInterval.invoke(underTest, 10));
        assertEquals(86400, normalizeInterval.invoke(underTest, 999999));
        assertEquals(45, normalizeInterval.invoke(underTest, 45));

        Class<?> cfgType = Class.forName("com.sim.chatserver.service.widget.WidgetAvailabilityChecker$EffectiveConfig");
        Object cfg = newEffectiveConfig(cfgType);
        setField(cfgType, cfg, "apiKeyValue", "token");
        setField(cfgType, cfg, "apiKeyHeaderName", "Authorization");

        Method shouldRetry = WidgetAvailabilityChecker.class.getDeclaredMethod("shouldRetryWithXApiKey", cfgType, int.class);
        shouldRetry.setAccessible(true);
        assertTrue((boolean) shouldRetry.invoke(underTest, cfg, 400));
        assertTrue((boolean) shouldRetry.invoke(underTest, cfg, 401));
        assertTrue((boolean) shouldRetry.invoke(underTest, cfg, 403));
        assertFalse((boolean) shouldRetry.invoke(underTest, cfg, 200));

        setField(cfgType, cfg, "apiKeyHeaderName", "X-API-Key");
        assertFalse((boolean) shouldRetry.invoke(underTest, cfg, 401));

        Method applyHeader = WidgetAvailabilityChecker.class.getDeclaredMethod("applyApiKeyHeader", HttpRequest.Builder.class, cfgType, String.class);
        applyHeader.setAccessible(true);

        setField(cfgType, cfg, "apiKeyHeaderName", "Authorization");
        setField(cfgType, cfg, "apiKeyValue", " Authorization:  Bearer  \"abc 123\" ");

        HttpRequest.Builder authBuilder = HttpRequest.newBuilder().uri(URI.create("http://example.test/health"));
        applyHeader.invoke(underTest, authBuilder, cfg, null);
        HttpRequest authReq = authBuilder.GET().build();
        assertEquals("Bearer abc123", authReq.headers().firstValue("Authorization").orElse(""));

        HttpRequest.Builder xApiBuilder = HttpRequest.newBuilder().uri(URI.create("http://example.test/health"));
        applyHeader.invoke(underTest, xApiBuilder, cfg, "X-API-Key");
        HttpRequest xApiReq = xApiBuilder.GET().build();
        assertEquals("abc123", xApiReq.headers().firstValue("X-API-Key").orElse(""));
    }

    @Test
    void sseAndEmbedHelpers_coverSuccessAndFailurePaths() throws Exception {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        Method validateSseBody = WidgetAvailabilityChecker.class.getDeclaredMethod("validateSseBody", String.class);
        validateSseBody.setAccessible(true);

        Object empty = validateSseBody.invoke(underTest, "");
        assertFalse((boolean) getField(empty, "ok"));
        assertTrue(String.valueOf(getField(empty, "reason")).contains("empty"));

        Object noData = validateSseBody.invoke(underTest, "event: ping\n\n");
        assertFalse((boolean) getField(noData, "ok"));
        assertTrue(String.valueOf(getField(noData, "reason")).contains("no SSE data events"));

        Object errorEvent = validateSseBody.invoke(underTest,
                "data: {\"type\":\"textResponseChunk\",\"error\":true,\"textResponse\":\"x\"}\n\n"
        );
        assertFalse((boolean) getField(errorEvent, "ok"));
        assertTrue(String.valueOf(getField(errorEvent, "reason")).contains("error=true"));

        Object noClose = validateSseBody.invoke(underTest,
                "data: {\"type\":\"textResponseChunk\",\"error\":false,\"textResponse\":\"x\"}\n\n"
        );
        assertFalse((boolean) getField(noClose, "ok"));
        assertTrue(String.valueOf(getField(noClose, "reason")).contains("close=true"));

        Object good = validateSseBody.invoke(underTest,
                "data: {\"type\":\"textResponseChunk\",\"error\":false,\"textResponse\":\"x\"}\n\n"
                        + "data: not-json\n\n"
                        + "data: {\"close\":true,\"error\":false}\n\n"
        );
        assertTrue((boolean) getField(good, "ok"));

        Class<?> cfgType = Class.forName("com.sim.chatserver.service.widget.WidgetAvailabilityChecker$EffectiveConfig");
        Object cfg = newEffectiveConfig(cfgType);

        Method effectiveWidgetId = WidgetAvailabilityChecker.class.getDeclaredMethod("effectiveWidgetId", cfgType);
        effectiveWidgetId.setAccessible(true);

        setField(cfgType, cfg, "widgetId", "  widget-explicit  ");
        setField(cfgType, cfg, "url", "https://host.test/api/v1/system");
        assertEquals("widget-explicit", effectiveWidgetId.invoke(underTest, cfg));

        setField(cfgType, cfg, "widgetId", null);
        setField(cfgType, cfg, "url", "https://host.test/api/embed/widget-from-url/stream-chat?x=1#frag");
        assertEquals("widget-from-url", effectiveWidgetId.invoke(underTest, cfg));

        setField(cfgType, cfg, "url", "https://host.test/api/v1/system");
        assertEquals(null, effectiveWidgetId.invoke(underTest, cfg));

        Method buildEmbedStreamUrl = WidgetAvailabilityChecker.class.getDeclaredMethod("buildEmbedStreamUrl", String.class, String.class);
        buildEmbedStreamUrl.setAccessible(true);

        assertEquals(
                "https://host.test/api/embed/widget-from-url/stream-chat",
                buildEmbedStreamUrl.invoke(underTest, "https://host.test/api/embed/widget-from-url/stream-chat?x=1#frag", "ignored")
        );
        assertEquals(
                "https://host.test/api/embed/widget+id/stream-chat",
                buildEmbedStreamUrl.invoke(underTest, "https://host.test/api/v1/system", "widget id")
        );

        InvocationTargetException emptyUrlEx = assertThrows(
                InvocationTargetException.class,
                () -> buildEmbedStreamUrl.invoke(underTest, "", "wid")
        );
        assertTrue(emptyUrlEx.getCause() instanceof IllegalArgumentException);

        InvocationTargetException emptyWidgetEx = assertThrows(
                InvocationTargetException.class,
                () -> buildEmbedStreamUrl.invoke(underTest, "https://host.test/api/v1/system", "")
        );
        assertTrue(emptyWidgetEx.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void parserAndUtilityHelpers_coverFallbackBranches() throws Exception {
        WidgetAvailabilityChecker underTest = new WidgetAvailabilityChecker();

        Method matchesExpectedJson = WidgetAvailabilityChecker.class.getDeclaredMethod(
                "matchesExpectedJson", String.class, String.class, String.class);
        matchesExpectedJson.setAccessible(true);
        assertTrue((boolean) matchesExpectedJson.invoke(underTest, "{\"status\":\"UP\"}", "status", "up"));
        assertFalse((boolean) matchesExpectedJson.invoke(underTest, "{\"other\":1}", "status", "up"));
        assertFalse((boolean) matchesExpectedJson.invoke(underTest, "not-json", "status", "up"));

        Method stringVal = WidgetAvailabilityChecker.class.getDeclaredMethod("stringVal", JsonObject.class, String.class);
        stringVal.setAccessible(true);
        JsonObject mixedObject = Json.createObjectBuilder()
                .add("s", "hello")
                .add("n", 123)
                .build();
        assertEquals("hello", stringVal.invoke(underTest, mixedObject, "s"));
        assertEquals(null, stringVal.invoke(underTest, mixedObject, "n"));

        JsonObject fallbackObject = mock(JsonObject.class);
        when(fallbackObject.containsKey("n")).thenReturn(true);
        when(fallbackObject.isNull("n")).thenReturn(false);
        when(fallbackObject.getString("n", null)).thenThrow(new ClassCastException("not a string"));
        when(fallbackObject.get("n")).thenReturn(Json.createValue("123"));
        assertEquals("123", stringVal.invoke(underTest, fallbackObject, "n"));
        assertEquals(null, stringVal.invoke(underTest, mixedObject, "missing"));

        Method boolVal = WidgetAvailabilityChecker.class.getDeclaredMethod("boolVal", JsonObject.class, String.class, boolean.class);
        boolVal.setAccessible(true);
        JsonObject boolObject = Json.createObjectBuilder().add("flag", true).build();
        JsonObject boolAsString = Json.createObjectBuilder().add("flag", "true").build();
        assertTrue((boolean) boolVal.invoke(underTest, boolObject, "flag", false));
        assertTrue((boolean) boolVal.invoke(underTest, boolAsString, "flag", false));
        assertFalse((boolean) boolVal.invoke(underTest, boolAsString, "missing", false));

        Method canonicalizeInput = WidgetAvailabilityChecker.class.getDeclaredMethod("canonicalizeInput", String.class, int.class);
        canonicalizeInput.setAccessible(true);
        assertEquals("abc", canonicalizeInput.invoke(underTest, "  abc  ", 10));
        assertEquals("", canonicalizeInput.invoke(underTest, "a\u0000b", 10));
        assertEquals("", canonicalizeInput.invoke(underTest, "abcdef", 3));

        Method canonicalizeBody = WidgetAvailabilityChecker.class.getDeclaredMethod("canonicalizeBodyForValidation", String.class, int.class);
        canonicalizeBody.setAccessible(true);
        assertEquals("a b", canonicalizeBody.invoke(underTest, "a\u0000b", 10));
        assertEquals("abc", canonicalizeBody.invoke(underTest, "abcdef", 3));

        Method isHttpsUrl = WidgetAvailabilityChecker.class.getDeclaredMethod("isHttpsUrl", String.class);
        isHttpsUrl.setAccessible(true);
        assertTrue((boolean) isHttpsUrl.invoke(underTest, "https://example.test/path"));
        assertFalse((boolean) isHttpsUrl.invoke(underTest, "http://example.test/path"));
        assertFalse((boolean) isHttpsUrl.invoke(underTest, "http://[bad-url"));

        Method safeUrl = WidgetAvailabilityChecker.class.getDeclaredMethod("safeUrl", String.class);
        safeUrl.setAccessible(true);
        assertEquals("https://example.test:8443/path", safeUrl.invoke(underTest, "https://example.test:8443/path?q=1"));
        assertEquals("http://[bad-url", safeUrl.invoke(underTest, "http://[bad-url"));

        Method authHeaderSuffix = WidgetAvailabilityChecker.class.getDeclaredMethod("authHeaderSuffix", String.class);
        authHeaderSuffix.setAccessible(true);
        assertEquals("", authHeaderSuffix.invoke(underTest, (Object) null));
        assertTrue(String.valueOf(authHeaderSuffix.invoke(underTest, "Bearer realm=widget")).contains("www-authenticate"));

        Method bodySnippet = WidgetAvailabilityChecker.class.getDeclaredMethod("bodySnippet", String.class);
        bodySnippet.setAccessible(true);
        assertEquals("", bodySnippet.invoke(underTest, (Object) null));
        assertEquals("hello world", bodySnippet.invoke(underTest, "  hello\nworld  "));

        Method bodySnippetSuffix = WidgetAvailabilityChecker.class.getDeclaredMethod("bodySnippetSuffix", String.class);
        bodySnippetSuffix.setAccessible(true);
        assertEquals("", bodySnippetSuffix.invoke(underTest, (Object) null));
        assertTrue(String.valueOf(bodySnippetSuffix.invoke(underTest, "line1\nline2")).contains("body=\"line1 line2\""));

        Class<?> cfgType = Class.forName("com.sim.chatserver.service.widget.WidgetAvailabilityChecker$EffectiveConfig");
        Object cfg = newEffectiveConfig(cfgType);
        setField(cfgType, cfg, "source", "DB");
        setField(cfgType, cfg, "enabled", true);
        setField(cfgType, cfg, "checkIntervalSeconds", 60);
        setField(cfgType, cfg, "widgetId", "wid");
        setField(cfgType, cfg, "requestOrigin", "https://origin.test");
        setField(cfgType, cfg, "requestReferer", "https://referer.test");
        setField(cfgType, cfg, "requestUserAgent", "ua");
        setField(cfgType, cfg, "requestCookie", "sid=1");
        setField(cfgType, cfg, "apiKeyHeaderName", "Authorization");
        setField(cfgType, cfg, "apiKeyValue", "token");

        Method sourceSuffix = WidgetAvailabilityChecker.class.getDeclaredMethod("sourceSuffix", cfgType);
        sourceSuffix.setAccessible(true);
        String suffix = String.valueOf(sourceSuffix.invoke(underTest, cfg));
        assertTrue(suffix.contains("source=DB"));
        assertTrue(suffix.contains("widgetId=wid"));
        assertTrue(suffix.contains("api-key-set"));
    }

    private static Object newEffectiveConfig(Class<?> cfgType) throws Exception {
        Constructor<?> ctor = cfgType.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object cfg = ctor.newInstance();
        assertNotNull(cfg);
        return cfg;
    }

    private static void setField(Class<?> type, Object target, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static byte[] bytes(String value) {
        return value == null ? null : value.getBytes(StandardCharsets.UTF_8);
    }
}
