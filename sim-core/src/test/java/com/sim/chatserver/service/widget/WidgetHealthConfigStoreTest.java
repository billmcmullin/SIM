package com.sim.chatserver.service.widget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WidgetHealthConfigStoreTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;

    private WidgetHealthConfigStore underTest;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        underTest = new WidgetHealthConfigStore(dataSource);
    }

    @Test
    void ensureTable_executesCreateAndMigrations() throws Exception {
        underTest.ensureTable();

        verify(connection, times(7)).prepareStatement(anyString());
        verify(statement, times(7)).execute();
    }

    @Test
    void ensureDefaultRow_insertsSingletonDefaults() throws Exception {
        underTest.ensureDefaultRow();

        verify(statement).setInt(1, WidgetHealthConfigStore.SINGLETON_ID);
        verify(statement).setString(2, "http://anythingllm:3001/api/health");
        verify(statement).executeUpdate();
    }

    @Test
    void load_noRow_returnsNull() throws Exception {
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(contains("SELECT id, healthcheck_url"))).thenReturn(loadPs);
        when(loadPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        WidgetHealthConfigStore.WidgetHealthConfig out = underTest.load();

        assertNull(out);
    }

    @Test
    void load_rowPresent_normalizesFields() throws Exception {
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(contains("SELECT id, healthcheck_url"))).thenReturn(loadPs);
        when(loadPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getObject("id", Integer.class)).thenReturn(1);
        when(rs.getObject("healthcheck_url", String.class)).thenReturn("  http://example.com/health  ");
        when(rs.getObject("method", String.class)).thenReturn("patch");
        when(rs.getObject("timeout_ms", Integer.class)).thenReturn(200_000);
        when(rs.getObject("expect_json_field", String.class)).thenReturn("  status ");
        when(rs.getObject("expect_json_value", String.class)).thenReturn(" up ");
        when(rs.getObject("widget_id", String.class)).thenReturn(" wid-1 ");
        when(rs.getObject("request_origin", String.class)).thenReturn(" https://origin.local ");
        when(rs.getObject("request_referer", String.class)).thenReturn(" https://origin.local/r ");
        when(rs.getObject("request_user_agent", String.class)).thenReturn(" test-agent ");
        when(rs.getObject("request_cookie", String.class)).thenReturn(" session=abc ");
        when(rs.getObject("api_key_header_name", String.class)).thenReturn(null);
        when(rs.getObject("api_key_value", String.class)).thenReturn(" token ");
        when(rs.getObject("updated_by", String.class)).thenReturn(" tester ");
        when(rs.getObject("updated_at", Timestamp.class)).thenReturn(Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));

        WidgetHealthConfigStore.WidgetHealthConfig out = underTest.load();

        assertNotNull(out);
        assertEquals(WidgetHealthConfigStore.SINGLETON_ID, out.getId());
        assertEquals("http://example.com/health", out.getHealthcheckUrl());
        assertEquals("GET", out.getMethod());
        assertEquals(120_000, out.getTimeoutMs());
        assertEquals("status", out.getExpectJsonField());
        assertEquals("up", out.getExpectJsonValue());
        assertEquals("wid-1", out.getWidgetId());
        assertEquals("https://origin.local", out.getRequestOrigin());
        assertEquals("https://origin.local/r", out.getRequestReferer());
        assertEquals("test-agent", out.getRequestUserAgent());
        assertEquals("session=abc", out.getRequestCookie());
        assertEquals("Authorization", out.getApiKeyHeaderName());
        assertEquals("token", out.getApiKeyValue());
        assertEquals("tester", out.getUpdatedBy());
    }

    @Test
    void save_nullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> underTest.save(null));
    }

    @Test
    void save_normalizesAndReturnsPersistedConfig() throws Exception {
        PreparedStatement savePs = mock(PreparedStatement.class);
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet loadRs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql != null && sql.trim().startsWith("INSERT INTO widget_health_config")) {
                return savePs;
            }
            if (sql != null && sql.contains("SELECT id, healthcheck_url")) {
                return loadPs;
            }
            return statement;
        });

        when(savePs.executeUpdate()).thenReturn(1);
        when(loadPs.executeQuery()).thenReturn(loadRs);
        when(loadRs.next()).thenReturn(true);

        when(loadRs.getObject("id", Integer.class)).thenReturn(1);
        when(loadRs.getObject("healthcheck_url", String.class)).thenReturn("http://anythingllm:3001/api/health");
        when(loadRs.getObject("method", String.class)).thenReturn("GET");
        when(loadRs.getObject("timeout_ms", Integer.class)).thenReturn(8000);
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

        WidgetHealthConfigStore.WidgetHealthConfig in = new WidgetHealthConfigStore.WidgetHealthConfig();
        in.setHealthcheckUrl("   ");
        in.setMethod("patch");
        in.setTimeoutMs(-1);
        in.setExpectJsonField("  ");
        in.setExpectJsonValue("  ");
        in.setWidgetId(null);
        in.setRequestOrigin(null);
        in.setRequestReferer(null);
        in.setRequestUserAgent(null);
        in.setRequestCookie(null);
        in.setApiKeyHeaderName(null);
        in.setApiKeyValue(null);
        in.setUpdatedBy("tester");
        in.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        WidgetHealthConfigStore.WidgetHealthConfig out = underTest.save(in);

        verify(savePs).setString(2, "http://anythingllm:3001/api/health");
        verify(savePs).setString(3, "GET");
        verify(savePs).setInt(4, 8000);
        verify(savePs).setNull(5, Types.VARCHAR);
        verify(savePs).setNull(6, Types.VARCHAR);
        assertNotNull(out);
        assertEquals("GET", out.getMethod());
        assertEquals(8000, out.getTimeoutMs());
    }
}
