package com.sim.chatserver.service.widget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;

class WidgetAvailabilityCheckerTest {

    private static final String DEBUG_FAILURES_PROP = "sim.widget.healthcheck.debug.failures";

    @Test
    void checkNow_sensitiveAuthOverHttp_isNotBlockedByDefault() throws Exception {
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

        when(loadRs.getString("healthcheck_url")).thenReturn("http://widget.local/api/health");
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

        assertFalse(result.available());
        assertEquals("DOWN", result.status());
        assertFalse(result.details().contains("Sensitive auth material configured"), result.details());
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
        when(loadRs.getString("api_key_header_name")).thenReturn(null);
        when(loadRs.getString("api_key_value")).thenReturn(null);
        when(loadRs.getString("updated_by")).thenReturn("tester");
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
        when(loadRs.getObject("api_key_header_name", String.class)).thenReturn(null);
        when(loadRs.getObject("api_key_value", String.class)).thenReturn(null);
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
        System.setProperty(DEBUG_FAILURES_PROP, "true");
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
            when(loadRs.getString("api_key_header_name")).thenReturn(null);
            when(loadRs.getString("api_key_value")).thenReturn(null);
            when(loadRs.getString("updated_by")).thenReturn("tester");
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
            when(loadRs.getObject("api_key_header_name", String.class)).thenReturn(null);
            when(loadRs.getObject("api_key_value", String.class)).thenReturn(null);
            when(loadRs.getObject("updated_by", String.class)).thenReturn("tester");
            when(loadRs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.now()));

            WidgetAvailabilityChecker.WidgetAvailabilityResult result = underTest.checkNow();

            assertFalse(result.available());
            assertEquals("DOWN", result.status());
            assertTrue(result.details().contains("IllegalArgumentException"));
        } finally {
            if (previous == null) {
                System.clearProperty(DEBUG_FAILURES_PROP);
            } else {
                System.setProperty(DEBUG_FAILURES_PROP, previous);
            }
        }
    }
}
