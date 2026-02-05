package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.startup.AppDataSourceHolder;

class ConfigStoreTest {

    private AppDataSourceHolder holder;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        holder = mock(AppDataSourceHolder.class);
        dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        ConfigStore.setAppDataSourceHolder(holder);
    }

    @Test
    void ensureTable_createsTableAndAddsColumnIfMissing() throws Exception {
        Connection createConn = mock(Connection.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);
        Connection columnConn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columns = mock(ResultSet.class);
        PreparedStatement alterStmt = mock(PreparedStatement.class);

        stubConnections(createConn, columnConn);
        when(createConn.prepareStatement(startsWith("CREATE TABLE IF NOT EXISTS server_config"))).thenReturn(createStmt);
        when(columnConn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, null, "server_config", "workspace_name")).thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(columnConn.prepareStatement(startsWith("ALTER TABLE server_config"))).thenReturn(alterStmt);

        ConfigStore.ensureTable();

        verify(createStmt).execute();
        verify(createStmt).close();
        verify(columnConn).prepareStatement(startsWith("ALTER TABLE server_config"));
        verify(alterStmt).execute();
        verify(alterStmt).close();
        verify(columnConn).close();
    }

    @Test
    void load_retrievesAllValuesIncludingWorkspace() throws Exception {
        Connection createConn = mock(Connection.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);
        Connection columnConn = mock(Connection.class);
        PreparedStatement selectStmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columnRs = mock(ResultSet.class);
        Connection selectConn = mock(Connection.class);

        stubConnections(createConn, columnConn, selectConn);
        when(createConn.prepareStatement(startsWith("CREATE TABLE IF NOT EXISTS server_config"))).thenReturn(createStmt);
        when(columnConn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, null, "server_config", "workspace_name")).thenReturn(columnRs);
        when(columnRs.next()).thenReturn(true);
        when(selectConn.prepareStatement(startsWith("SELECT server_host"))).thenReturn(selectStmt);

        when(selectStmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("server_host")).thenReturn("example.com");
        when(resultSet.getInt("server_port")).thenReturn(8080);
        when(resultSet.getString("connection_info")).thenReturn("info");
        when(resultSet.getString("api_key")).thenReturn("secret");
        when(resultSet.getString("workspace_name")).thenReturn("workspace-a");

        ServerConfig config = ConfigStore.load();

        assertNotNull(config);
        assertEquals("example.com", config.getServerHost());
        assertEquals(8080, config.getServerPort());
        assertEquals("info", config.getConnectionInfo());
        assertEquals("secret", config.getApiKey());
        assertEquals("workspace-a", config.getWorkspaceName());
    }

    @Test
    void load_returnsNullDefaultsWhenEmpty() throws Exception {
        Connection createConn = mock(Connection.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);
        Connection columnConn = mock(Connection.class);
        PreparedStatement selectStmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columnRs = mock(ResultSet.class);
        Connection selectConn = mock(Connection.class);

        stubConnections(createConn, columnConn, selectConn);
        when(createConn.prepareStatement(startsWith("CREATE TABLE IF NOT EXISTS server_config"))).thenReturn(createStmt);
        when(columnConn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, null, "server_config", "workspace_name")).thenReturn(columnRs);
        when(columnRs.next()).thenReturn(true);
        when(selectConn.prepareStatement(startsWith("SELECT server_host"))).thenReturn(selectStmt);
        when(selectStmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ServerConfig config = ConfigStore.load();

        assertNotNull(config);
        assertEquals(null, config.getServerHost());
        assertEquals(0, config.getServerPort());
        assertEquals(null, config.getConnectionInfo());
        assertEquals(null, config.getApiKey());
        assertEquals(null, config.getWorkspaceName());
    }

    @Test
    void save_writesRowAndDeletesPrevious() throws Exception {
        Connection createConn = mock(Connection.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);
        Connection columnConn = mock(Connection.class);
        PreparedStatement deleteStmt = mock(PreparedStatement.class);
        Connection deleteConn = mock(Connection.class);
        Connection insertConn = mock(Connection.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet columnRs = mock(ResultSet.class);

        stubConnections(createConn, columnConn, deleteConn, insertConn);
        when(createConn.prepareStatement(startsWith("CREATE TABLE IF NOT EXISTS server_config"))).thenReturn(createStmt);
        when(columnConn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(null, null, "server_config", "workspace_name")).thenReturn(columnRs);
        when(columnRs.next()).thenReturn(true);
        when(deleteConn.prepareStatement(startsWith("DELETE FROM server_config"))).thenReturn(deleteStmt);
        when(insertConn.prepareStatement(startsWith("INSERT INTO server_config"))).thenReturn(insertStmt);

        ServerConfig config = new ServerConfig("example.com", 9090, "info", "secret", "workspace-a");

        ConfigStore.save(config);

        verify(deleteStmt).executeUpdate();
        verify(deleteStmt).close();
        verify(insertStmt).setString(1, "example.com");
        verify(insertStmt).setInt(2, 9090);
        verify(insertStmt).setString(3, "info");
        verify(insertStmt).setString(4, "secret");
        verify(insertStmt).setString(5, "workspace-a");
        verify(insertStmt).executeUpdate();
        verify(insertStmt).close();
        verify(insertConn).close();
    }

    private void stubConnections(Connection... connections) throws SQLException {
        AtomicInteger counter = new AtomicInteger(0);
        when(dataSource.getConnection()).thenAnswer(invocation -> connections[counter.getAndIncrement()]);
    }
}
