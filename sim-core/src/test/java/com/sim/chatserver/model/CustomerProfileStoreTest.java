package com.sim.chatserver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.sim.chatserver.startup.AppDataSourceHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.sql.DataSource;
/**
 * Parasoft Jtest UTA: Test class for CustomerProfileStore
 *
 * @see com.sim.chatserver.model.CustomerProfileStore
 * @author bmcmullin
 */
public class CustomerProfileStoreTest
{

    private static final String ENC_KEY_ENV = "CONFIG_ENCRYPTION_KEY";
    private static final String ENC_SALT_ENV = "CONFIG_ENCRYPTION_SALT";
    private static final String ENC_TRANSFORM_ENV = "CONFIG_ENCRYPTION_TRANSFORMATION";

    private String oldEncKey;
    private String oldEncSalt;
    private String oldEncTransform;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void rememberEnvValues() throws Exception {
        Map<String, String> env = (Map<String, String>) getPrivateStaticField("ENV");
        oldEncKey = env.get(ENC_KEY_ENV);
        oldEncSalt = env.get(ENC_SALT_ENV);
        oldEncTransform = env.get(ENC_TRANSFORM_ENV);
    }

    @AfterEach
    @SuppressWarnings("unchecked")
    public void restoreEnvValues() throws Exception {
        Map<String, String> env = (Map<String, String>) getPrivateStaticField("ENV");
        restoreEnvVar(env, ENC_KEY_ENV, oldEncKey);
        restoreEnvVar(env, ENC_SALT_ENV, oldEncSalt);
        restoreEnvVar(env, ENC_TRANSFORM_ENV, oldEncTransform);
        CustomerProfileStore.setAppDataSourceHolder(null);
    }

    /**
     * Parasoft Jtest UTA: Test for setAppDataSourceHolder(AppDataSourceHolder)
     *
     * @see com.sim.chatserver.model.CustomerProfileStore#setAppDataSourceHolder(AppDataSourceHolder)
     * @author bmcmullin
     */
    @Test
    public void testSetAppDataSourceHolder() throws Throwable
    {
        // When
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        CustomerProfileStore.setAppDataSourceHolder(holder);

    }

    @Test
    public void upsert_nullProfile_throwsIllegalArgumentException() throws Throwable {
        CustomerProfileStore.setAppDataSourceHolder(createHolderForEnsureTableOnly());

        assertThrows(IllegalArgumentException.class, () -> CustomerProfileStore.upsert(null));
    }

    @Test
    public void upsert_blankSessionId_throwsIllegalArgumentException() throws Throwable {
        CustomerProfileStore.setAppDataSourceHolder(createHolderForEnsureTableOnly());
        CustomerProfile profile = new CustomerProfile();
        profile.setSessionId("   ");

        assertThrows(IllegalArgumentException.class, () -> CustomerProfileStore.upsert(profile));
    }

    @Test
    public void loadBySessionId_blankValue_returnsNull() throws Throwable {
        CustomerProfileStore.setAppDataSourceHolder(createHolderForEnsureTableOnly());

        CustomerProfile loaded = CustomerProfileStore.loadBySessionId("   ");

        assertNull(loaded);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void encryptThenDecrypt_roundTripsText() throws Throwable {
        Map<String, String> env = (Map<String, String>) getPrivateStaticField("ENV");
        env.put(ENC_KEY_ENV, Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)));
        env.put(ENC_SALT_ENV, "sim-test-salt");
        env.put(ENC_TRANSFORM_ENV, "AES/GCM/NoPadding");

        String encrypted = (String) invokePrivateStatic("encryptIfPresent", new Class<?>[]{String.class}, "hello-world");
        String decrypted = (String) invokePrivateStatic("decryptIfNeeded", new Class<?>[]{String.class}, encrypted);

        assertTrue(encrypted.startsWith("ENCv1:"));
        assertEquals("hello-world", decrypted);
    }

    @Test
    public void decryptIfNeeded_invalidPayload_throwsSqlException() throws Throwable {
        SQLException ex = assertThrows(SQLException.class, () -> invokePrivateStaticChecked(
                "decryptIfNeeded",
                new Class<?>[]{String.class},
                "ENCv1:invalid"));

        assertTrue(ex.getMessage().contains("Invalid encrypted value format"));
    }

    @Test
    public void readDbRawText_handlesTypedValuesAndSqlFailure() throws Throwable {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("text_col")).thenReturn("abc");
        when(rs.getObject("bytes_col")).thenReturn("xyz".getBytes(StandardCharsets.UTF_8));
        when(rs.getObject("err_col")).thenThrow(new SQLException("db read failed"));

        String text = (String) invokePrivateStatic("readDbRawText", new Class<?>[]{ResultSet.class, String.class}, rs, "text_col");
        String bytes = (String) invokePrivateStatic("readDbRawText", new Class<?>[]{ResultSet.class, String.class}, rs, "bytes_col");
        String failed = (String) invokePrivateStatic("readDbRawText", new Class<?>[]{ResultSet.class, String.class}, rs, "err_col");

        assertEquals("abc", text);
        assertEquals("xyz", bytes);
        assertNull(failed);
    }

    @Test
    public void readDbText_normalizesAndTruncates() throws Throwable {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("name_col")).thenReturn("  abcde  ");

        String value = (String) invokePrivateStatic(
                "readDbText",
                new Class<?>[]{ResultSet.class, String.class, int.class},
                rs,
                "name_col",
                3);

        assertEquals("abc", value);
    }

    @Test
    public void readDbTimestamp_supportsTypedAndStringFallback() throws Throwable {
        ResultSet rsTyped = mock(ResultSet.class);
        Timestamp ts = Timestamp.from(Instant.parse("2026-08-24T12:30:00Z"));
        when(rsTyped.getTimestamp("last_synced_at")).thenReturn(ts);
        Timestamp typed = (Timestamp) invokePrivateStatic("readDbTimestamp", new Class<?>[]{ResultSet.class, String.class}, rsTyped, "last_synced_at");

        ResultSet rsFallback = mock(ResultSet.class);
        when(rsFallback.getTimestamp("last_synced_at")).thenReturn(null);
        when(rsFallback.getObject("last_synced_at")).thenReturn("2026-08-24 05:01:02");
        Timestamp parsed = (Timestamp) invokePrivateStatic("readDbTimestamp", new Class<?>[]{ResultSet.class, String.class}, rsFallback, "last_synced_at");

        assertNotNull(typed);
        assertNotNull(parsed);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void readEnvCanonical_rejectsControlCharacters() throws Throwable {
        Map<String, String> env = (Map<String, String>) getPrivateStaticField("ENV");
        env.put("SIM_TEST_BAD_ENV", "bad\u0001value");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invokePrivateStaticChecked(
                "readEnvCanonical",
                new Class<?>[]{String.class, int.class},
                "SIM_TEST_BAD_ENV",
                100));

        assertTrue(ex.getMessage().contains("invalid control characters"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resolveCipherTransformation_fallsBackToDefaultForUnsupportedValue() throws Throwable {
        Map<String, String> env = (Map<String, String>) getPrivateStaticField("ENV");
        env.put(ENC_TRANSFORM_ENV, "AES/CBC/PKCS5Padding");

        String resolved = (String) invokePrivateStatic("resolveCipherTransformation", new Class<?>[]{});

        assertEquals("AES/GCM/NoPadding", resolved);
    }

    private static AppDataSourceHolder createHolderForEnsureTableOnly() throws SQLException {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        Connection createConn = mock(Connection.class);
        PreparedStatement createPs = mock(PreparedStatement.class);
        Connection migrateConn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);

        when(holder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(createConn, migrateConn);
        when(createConn.prepareStatement(anyString())).thenReturn(createPs);
        when(migrateConn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(any(), any(), any(), any())).thenAnswer(invocation -> {
            ResultSet cols = mock(ResultSet.class);
            when(cols.next()).thenReturn(true);
            return cols;
        });

        return holder;
    }

    private static Object getPrivateStaticField(String fieldName) throws Exception {
        Field f = CustomerProfileStore.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }

    private static Object invokePrivateStatic(String methodName, Class<?>[] paramTypes, Object... args) throws Throwable {
        try {
            Method m = CustomerProfileStore.class.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(null, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private static Throwable invokePrivateStaticChecked(String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            invokePrivateStatic(methodName, paramTypes, args);
            return null;
        } catch (Throwable t) {
            throwUnchecked(t);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(Throwable t) throws T {
        throw (T) t;
    }

    private static void restoreEnvVar(Map<String, String> env, String key, String value) {
        if (value == null) {
            env.remove(key);
        } else {
            env.put(key, value);
        }
    }

}
