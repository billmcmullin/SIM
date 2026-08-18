package com.sim.chatserver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 class CustomerIdentityStoreTest {

    @Test
    void q_quotesValidIdentifier_andRejectsInvalidIdentifier() throws Exception {
        assertEquals("\"valid_name\"", invoke("q", new Class<?>[] { String.class }, "valid_name"));

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> invoke("q", new Class<?>[] { String.class }, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void blankHelpers_andSanitizeDbText_coverCoreBranches() throws Exception {
        assertEquals(true, invoke("isBlank", new Class<?>[] { String.class }, (Object) null));
        assertEquals(false, invoke("isBlank", new Class<?>[] { String.class }, "x"));
        assertNull(invoke("nullIfBlank", new Class<?>[] { String.class }, "  "));
        assertEquals("x", invoke("nullIfBlank", new Class<?>[] { String.class }, " x "));

        assertNull(invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, null, 10));
        assertEquals("trim", invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, " trim ", 10));
        assertEquals("abc", invoke("sanitizeDbText", new Class<?>[] { String.class, int.class }, "abcdef", 3));
    }

    @Test
    void readNonNegativeLongObject_coversTypedAndFallbackPaths() throws Exception {
        ResultSet typed = mock(ResultSet.class);
        when(typed.getString("identity_id")).thenReturn("15");
        assertEquals(15L, invoke("readNonNegativeLongObject", new Class<?>[] { ResultSet.class, String.class }, typed, "identity_id"));

        ResultSet negative = mock(ResultSet.class);
        when(negative.getString("identity_id")).thenReturn("-1");
        assertEquals(0L, invoke("readNonNegativeLongObject", new Class<?>[] { ResultSet.class, String.class }, negative, "identity_id"));

        ResultSet fallback = mock(ResultSet.class);
        when(fallback.getString("identity_id")).thenReturn("42");
        assertEquals(42L, invoke("readNonNegativeLongObject", new Class<?>[] { ResultSet.class, String.class }, fallback, "identity_id"));
    }

    @Test
    void readSafeTimestamp_usesTypedAndTextFallback() throws Exception {
        Timestamp ts = Timestamp.from(Instant.parse("2026-08-07T10:20:30Z"));
        ResultSet typed = mock(ResultSet.class);
        when(typed.getString("created_at")).thenReturn("2026-08-07T10:20:30Z");
        assertEquals(ts, invoke("readSafeTimestamp", new Class<?>[] { ResultSet.class, String.class }, typed, "created_at"));

        ResultSet textFallback = mock(ResultSet.class);
        when(textFallback.getString("created_at")).thenReturn("2026-08-07T10:20:30Z");
        assertEquals(ts, invoke("readSafeTimestamp", new Class<?>[] { ResultSet.class, String.class }, textFallback, "created_at"));

        ResultSet invalid = mock(ResultSet.class);
        when(invalid.getString("created_at")).thenReturn("bad");
        assertNull(invoke("readSafeTimestamp", new Class<?>[] { ResultSet.class, String.class }, invalid, "created_at"));

        ResultSet nullText = mock(ResultSet.class);
        when(nullText.getString("created_at")).thenReturn(null);
        assertNull(invoke("readSafeTimestamp", new Class<?>[] { ResultSet.class, String.class }, nullText, "created_at"));
    }

    @Test
    void readSanitizedDbText_returnsNullWhenReadFails() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("canonical_name")).thenThrow(new SQLException("boom"));

        assertNull(invoke("readSanitizedDbText", new Class<?>[] { ResultSet.class, String.class, int.class }, rs, "canonical_name", 128));
    }

    @Test
    void mapIdentity_setsCoreFieldsFromResultSet() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("identity_id")).thenReturn("101");
        when(rs.getString("canonical_email")).thenReturn(" user@example.com ");
        when(rs.getString("canonical_name")).thenReturn(" Jane ");
        when(rs.getString("salesforce_contact_id")).thenReturn("c1");
        when(rs.getString("salesforce_account_id")).thenReturn("a1");
        when(rs.getString("email_enc")).thenReturn("e");
        when(rs.getString("phone_enc")).thenReturn("p");
        when(rs.getString("title_enc")).thenReturn("t");
        when(rs.getString("department_enc")).thenReturn("d");
        when(rs.getString("raw_json_enc")).thenReturn("{}");
        when(rs.getString("confidence")).thenReturn("high");

        Timestamp created = Timestamp.from(Instant.parse("2026-08-07T10:20:30Z"));
        Timestamp updated = Timestamp.from(Instant.parse("2026-08-07T11:20:30Z"));
        Timestamp synced = Timestamp.from(Instant.parse("2026-08-07T12:20:30Z"));
        when(rs.getString("created_at")).thenReturn("2026-08-07T10:20:30Z");
        when(rs.getString("updated_at")).thenReturn("2026-08-07T11:20:30Z");
        when(rs.getString("last_synced_at")).thenReturn("2026-08-07T12:20:30Z");

        CustomerIdentity identity = (CustomerIdentity) invoke("mapIdentity", new Class<?>[] { ResultSet.class }, rs);

        assertEquals(101L, identity.getIdentityId());
        assertEquals("user@example.com", identity.getCanonicalEmail());
        assertEquals("Jane", identity.getCanonicalName());
        assertEquals("c1", identity.getSalesforceContactId());
        assertEquals("a1", identity.getSalesforceAccountId());
        assertEquals("high", identity.getConfidence());
        assertEquals(created.toInstant().atOffset(java.time.ZoneOffset.UTC), identity.getCreatedAt());
        assertEquals(updated.toInstant().atOffset(java.time.ZoneOffset.UTC), identity.getUpdatedAt());
        assertEquals(synced.toInstant().atOffset(java.time.ZoneOffset.UTC), identity.getLastSyncedAt());
    }

    @Test
    void ensureNowDefault_handlesSQLExceptionWithoutThrowing() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        invoke("ensureNowDefault", new Class<?>[] { Connection.class, String.class, String.class }, conn, "customer_identity", "updated_at");

        Connection failing = mock(Connection.class);
        when(failing.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenThrow(new SQLException("fail"));

        invoke("ensureNowDefault", new Class<?>[] { Connection.class, String.class, String.class }, failing, "customer_identity", "updated_at");
    }

    private static Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = CustomerIdentityStore.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}

