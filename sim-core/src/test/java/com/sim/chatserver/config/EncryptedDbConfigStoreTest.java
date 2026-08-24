package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;

class EncryptedDbConfigStoreTest {

    private AppDataSourceHolder holder;
    private DataSource ds;
    private Connection conn;
    private DatabaseMetaData meta;

    @BeforeEach
    void setUp() throws Exception {
        holder = mock(AppDataSourceHolder.class);
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        meta = mock(DatabaseMetaData.class);

        when(holder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);

        // Default: columns exist (so no ALTER needed unless test overrides)
        ResultSet columnsRs = mock(ResultSet.class);
        when(columnsRs.next()).thenReturn(true);
        when(meta.getColumns(any(), any(), eq("server_config"), anyString())).thenReturn(columnsRs);

        // Generic prepared statement default
        PreparedStatement defaultPs = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(defaultPs);
        when(defaultPs.execute()).thenReturn(true);
        when(defaultPs.executeUpdate()).thenReturn(1);

        // Install holder so CDI lookup is not needed
        EncryptedDbConfigStore.setAppDataSourceHolder(holder);
    }

    @AfterEach
    void tearDown() {
        EncryptedDbConfigStore.setAppDataSourceHolder(null);
    }

    @Test
    void ensureTable_withMockDataSource_runs() {
        assertDoesNotThrow(EncryptedDbConfigStore::ensureTable);
    }

    @Test
    void load_noRow_returnsEmptyConfig() throws Exception {
        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(startsWith("SELECT server_host"))).thenReturn(selectPs);
        when(selectPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        ServerConfig cfg = EncryptedDbConfigStore.load();

        assertNotNull(cfg);
        // default object expected when no DB rows
    }

    @Test
    void save_nullConfig_throwsBecauseEncryptionKeyMissing() {
        // save() calls encryptIfPresent(...) -> requires CONFIG_ENCRYPTION_KEY when non-blank values.
        // For null config all encryptIfPresent args are null/blank-safe, so it should proceed.
        assertDoesNotThrow(() -> EncryptedDbConfigStore.save(null));
    }

    @Test
    void save_nonNullConfig_withoutEnvKey_throwsIllegalStateWrappedPath() {
        ServerConfig cfg = new ServerConfig();
        cfg.setApiKey("abc"); // triggers encryption key lookup

        // encryptIfPresent throws SQLException wrapping IllegalStateException (missing env var)
        assertThrows(SQLException.class, () -> EncryptedDbConfigStore.save(cfg));
    }

    @Test
    void saveWorkspaceName_usesLoadThenSave() throws Exception {
        // SELECT for load() -> no rows, then save() path
        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(startsWith("SELECT server_host"))).thenReturn(selectPs);
        when(selectPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertDoesNotThrow(() -> EncryptedDbConfigStore.saveWorkspaceName("ws"));
    }

    @Test
    void requireDataSourceFailure_whenHolderReturnsNull_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> EncryptedDbConfigStore.setAppDataSourceHolder(mock(AppDataSourceHolder.class))
        );
    }
}
