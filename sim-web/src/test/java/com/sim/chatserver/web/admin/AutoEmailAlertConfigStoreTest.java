package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
 class AutoEmailAlertConfigStoreTest {

    @Test
    void updateHealthState_andUpdateTermState_executeAndNormalizeInputs() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection c1 = mock(Connection.class);
        Connection c2 = mock(Connection.class);
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        when(ds.getConnection()).thenReturn(c1, c2);
        when(c1.prepareStatement(anyString())).thenReturn(ps1);
        when(c2.prepareStatement(anyString())).thenReturn(ps2);

        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(ds);
        Instant now = Instant.parse("2026-08-07T12:10:00Z");

        store.updateHealthState(now, "down", now.minusSeconds(60), null);
        verify(ps1).setString(2, "DOWN");

        store.updateTermState(now, -10L, now.minusSeconds(30));
        verify(ps2).setLong(2, 0L);
    }

    @Test
    void ensureTable_andEnsureDefaultRow_executeExpectedStatements() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection c1 = mock(Connection.class);
        Connection c2 = mock(Connection.class);
        Connection c3 = mock(Connection.class);
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        PreparedStatement ps3 = mock(PreparedStatement.class);
        PreparedStatement ps4 = mock(PreparedStatement.class);

        when(ds.getConnection()).thenReturn(c1, c2, c3);
        when(c1.prepareStatement(anyString())).thenReturn(ps1);
        when(c2.prepareStatement(anyString())).thenReturn(ps2, ps3);
        when(c3.prepareStatement(anyString())).thenReturn(ps4);

        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(ds);
        store.ensureTable();
        store.ensureDefaultRow();

        verify(ps1).execute();
        verify(ps2).execute();
        verify(ps3).execute();
        verify(ps4).setInt(1, AutoEmailAlertConfigStore.SINGLETON_ID);
        verify(ps4).executeUpdate();
    }

    @Test
    void load_returnsNullWhenNoRow() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection c = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(c);
        when(c.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(ds);
        assertNull(store.load());
    }

    @Test
    void load_mapsRowWithSanitizationAndClamps() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection c = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(c);
        when(c.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getCharacterStream(anyString())).thenAnswer(invocation -> {
            String column = invocation.getArgument(0, String.class);
            String value = rs.getString(column);
            return value == null ? null : new StringReader(value);
        });

        when(rs.getString("id")).thenReturn("5");
        when(rs.getString("health_enabled")).thenReturn("yes");
        when(rs.getString("health_check_interval_seconds")).thenReturn("5");
        when(rs.getString("health_offline_delay_seconds")).thenReturn("-1");
        when(rs.getString("health_resend_interval_seconds")).thenReturn("99999999");
        when(rs.getString("health_recipients")).thenReturn("a@b.com\u0000");
        when(rs.getString("health_subject")).thenReturn("  subject  ");
        when(rs.getString("health_message")).thenReturn(" msg\r ");
        when(rs.getString("health_runbook_url")).thenReturn(" https://runbook.example ");
        when(rs.getString("health_runbook_attachment_path")).thenReturn(" /tmp/runbook.pdf ");
        when(rs.getString("term_enabled")).thenReturn("true");
        when(rs.getString("term_check_interval_seconds")).thenReturn("60");
        when(rs.getString("term_name")).thenReturn("  TopicA ");
        when(rs.getString("term_recipients")).thenReturn("ops@example.com");
        when(rs.getString("term_subject")).thenReturn("term subject");
        when(rs.getString("term_message")).thenReturn("term message");
        when(rs.getString("health_last_status")).thenReturn("down");
        when(rs.getString("term_last_count")).thenReturn("-100");
        when(rs.getString("updated_by")).thenReturn(" admin ");
        when(rs.getString("updated_at")).thenReturn("2026-08-07T10:00:00Z");
        when(rs.getString("term_last_checked_at")).thenReturn(null);

        Timestamp nowTs = Timestamp.from(Instant.parse("2026-08-07T09:30:00Z"));
        when(rs.getString("health_last_checked_at")).thenReturn(nowTs.toInstant().toString());
        when(rs.getString("health_offline_since")).thenReturn("2026-08-07T09:20:00Z");
        when(rs.getString("health_last_alert_at")).thenReturn("2026-08-07T09:25:00Z");

        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(ds);
        AutoEmailAlertConfigStore.AutoEmailAlertConfig cfg = store.load();

        assertNotNull(cfg);
        assertEquals(5, cfg.getId());
        assertTrue(cfg.isHealthEnabled());
        assertEquals(30, cfg.getHealthCheckIntervalSeconds());
        assertEquals(0, cfg.getHealthOfflineDelaySeconds());
        assertEquals(86_400, cfg.getHealthResendIntervalSeconds());
        assertEquals("a@b.com", cfg.getHealthRecipients());
        assertEquals("subject", cfg.getHealthSubject());
        assertEquals("msg", cfg.getHealthMessage());
        assertEquals("https://runbook.example", cfg.getHealthRunbookUrl());
        assertEquals("/tmp/runbook.pdf", cfg.getHealthRunbookAttachmentPath());
        assertTrue(cfg.isTermEnabled());
        assertEquals(60, cfg.getTermCheckIntervalSeconds());
        assertEquals("TopicA", cfg.getTermName());
        assertEquals("down", cfg.getHealthLastStatus());
        assertEquals(0L, cfg.getTermLastCount());
        assertEquals("admin", cfg.getUpdatedBy());
        assertEquals(nowTs.toInstant(), cfg.getHealthLastCheckedAt());
        assertNotNull(cfg.getHealthOfflineSince());
        assertNotNull(cfg.getHealthLastAlertAt());
        assertNull(cfg.getTermLastCheckedAt());
        assertNotNull(cfg.getUpdatedAt());
    }

    @Test
    void saveConfig_whenTermNameChanges_clearsTermStateAndReturnsLoadedValue() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection c1 = mock(Connection.class);
        Connection c2 = mock(Connection.class);
        Connection c3 = mock(Connection.class);
        Connection c4 = mock(Connection.class);
        PreparedStatement psLoad1 = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        PreparedStatement psClear = mock(PreparedStatement.class);
        PreparedStatement psLoad2 = mock(PreparedStatement.class);
        ResultSet rsLoad1 = mock(ResultSet.class);
        ResultSet rsLoad2 = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(c1, c2, c3, c4);
        when(c1.prepareStatement(anyString())).thenReturn(psLoad1);
        when(c2.prepareStatement(anyString())).thenReturn(psUpdate);
        when(c3.prepareStatement(anyString())).thenReturn(psClear);
        when(c4.prepareStatement(anyString())).thenReturn(psLoad2);

        when(psLoad1.executeQuery()).thenReturn(rsLoad1);
        when(psLoad2.executeQuery()).thenReturn(rsLoad2);

        when(rsLoad1.next()).thenReturn(true);
        when(rsLoad2.next()).thenReturn(true);
        when(rsLoad1.getCharacterStream(anyString())).thenAnswer(invocation -> {
            String column = invocation.getArgument(0, String.class);
            String value = rsLoad1.getString(column);
            return value == null ? null : new StringReader(value);
        });
        when(rsLoad2.getCharacterStream(anyString())).thenAnswer(invocation -> {
            String column = invocation.getArgument(0, String.class);
            String value = rsLoad2.getString(column);
            return value == null ? null : new StringReader(value);
        });

        // First load (current config)
        when(rsLoad1.getString("id")).thenReturn("1");
        when(rsLoad1.getString("term_name")).thenReturn("oldTerm");
        when(rsLoad1.getString("health_check_interval_seconds")).thenReturn("300");
        when(rsLoad1.getString("health_offline_delay_seconds")).thenReturn("300");
        when(rsLoad1.getString("health_resend_interval_seconds")).thenReturn("1800");
        when(rsLoad1.getString("term_check_interval_seconds")).thenReturn("600");

        // Final load (saved config)
        when(rsLoad2.getString("id")).thenReturn("1");
        when(rsLoad2.getString("term_name")).thenReturn("newTerm");
        when(rsLoad2.getString("health_check_interval_seconds")).thenReturn("300");
        when(rsLoad2.getString("health_offline_delay_seconds")).thenReturn("300");
        when(rsLoad2.getString("health_resend_interval_seconds")).thenReturn("1800");
        when(rsLoad2.getString("term_check_interval_seconds")).thenReturn("600");

        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(ds);
        AutoEmailAlertConfigStore.AutoEmailAlertConfig incoming = new AutoEmailAlertConfigStore.AutoEmailAlertConfig();
        incoming.setTermName("newTerm");
        incoming.setHealthEnabled(true);
        incoming.setTermEnabled(true);

        AutoEmailAlertConfigStore.AutoEmailAlertConfig saved = store.saveConfig(incoming, "tester");
        assertNotNull(saved);
        assertEquals("newTerm", saved.getTermName());
        verify(psClear).executeUpdate();
    }

    @Test
    void helperMethods_coverSanitizeAndBooleanParsers() throws Exception {
        AutoEmailAlertConfigStore store = new AutoEmailAlertConfigStore(mock(DataSource.class));
        assertEquals("ABC", invokePrivate(store, "sanitizeStatus", new Class<?>[]{String.class}, "abc"));
        assertEquals("text", invokePrivate(store, "sanitizeText", new Class<?>[]{String.class, int.class}, " text\u0000 ", 10));
        assertNull(invokePrivate(store, "sanitizeText", new Class<?>[]{String.class, int.class}, "\r\n", 10));
        assertEquals(true, invokePrivate(store, "eqIgnoreCaseTrim", new Class<?>[]{String.class, String.class}, " A ", "a"));

        ResultSet rsTs = mock(ResultSet.class);
        when(rsTs.getString("ts")).thenReturn("2026-08-07 12:11:12");
        when(rsTs.getCharacterStream("ts")).thenReturn(new StringReader("2026-08-07 12:11:12"));
        assertNotNull(invokePrivate(store, "readSafeInstant", new Class<?>[]{ResultSet.class, String.class}, rsTs, "ts"));

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("flag")).thenReturn("y");
        when(rs.getCharacterStream("flag")).thenAnswer(invocation -> {
            String value = rs.getString("flag");
            return value == null ? null : new StringReader(value);
        });
        assertTrue((boolean) invokePrivate(store, "readSafeBoolean", new Class<?>[]{ResultSet.class, String.class, boolean.class}, rs, "flag", false));
        when(rs.getString("flag")).thenReturn("0");
        assertFalse((boolean) invokePrivate(store, "readSafeBoolean", new Class<?>[]{ResultSet.class, String.class, boolean.class}, rs, "flag", true));
    }

    private Object invokePrivate(Object target, String method, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(method, paramTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }
}

