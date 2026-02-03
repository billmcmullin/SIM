package com.sim.chatserver.config;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigStoreTest {

    @BeforeAll
    static void ensureDatabaseEnv() throws Exception {
        setEnv(Map.of(
                "DB_HOST", "localhost",
                "DB_PORT", "5432",
                "DB_NAME", "chat",
                "DB_USER", "test",
                "DB_PASSWORD", "test"));
    }

    @Test
    void ensureTable_executesCreateStatement() throws Exception {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);

        try (MockedStatic<Database> db = mockStatic(Database.class)) {
            db.when(Database::getConnection).thenReturn(connection);

            ConfigStore.ensureTable();
        }

        verify(statement).execute(anyString());
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void load_returnsConfigWhenRowPresent() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(
                "SELECT server_host, server_port, connection_info, api_key FROM server_config ORDER BY id DESC LIMIT 1"))
                .thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("server_host")).thenReturn("example.com");
        when(rs.getInt("server_port")).thenReturn(8080);
        when(rs.getString("connection_info")).thenReturn("info");
        when(rs.getString("api_key")).thenReturn("secret");

        try (MockedStatic<Database> db = mockStatic(Database.class)) {
            db.when(Database::getConnection).thenReturn(connection);

            ServerConfig config = ConfigStore.load();

            assertNotNull(config);
            assertEquals("example.com", config.getServerHost());
            assertEquals(8080, config.getServerPort());
            assertEquals("info", config.getConnectionInfo());
            assertEquals("secret", config.getApiKey());
        }

        verify(ps).executeQuery();
        verify(rs).close();
        verify(ps).close();
        verify(connection).close();
    }

    @Test
    void load_returnsNullWhenNoRows() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<Database> db = mockStatic(Database.class)) {
            db.when(Database::getConnection).thenReturn(connection);

            assertNull(ConfigStore.load());
        }

        verify(rs).close();
        verify(ps).close();
        verify(connection).close();
    }

    @Test
    void save_clearsExistingRowsAndInsertsNewConfig() throws Exception {
        Connection deleteConnection = mock(Connection.class);
        Statement deleteStatement = mock(Statement.class);
        when(deleteConnection.createStatement()).thenReturn(deleteStatement);

        Connection insertConnection = mock(Connection.class);
        PreparedStatement insertStatement = mock(PreparedStatement.class);
        when(insertConnection.prepareStatement(
                "INSERT INTO server_config (server_host, server_port, connection_info, api_key) VALUES (?, ?, ?, ?)"))
                .thenReturn(insertStatement);

        ServerConfig config = new ServerConfig("example.com", 9090, "info", "secret");

        try (MockedStatic<Database> db = mockStatic(Database.class)) {
            db.when(Database::getConnection).thenReturn(deleteConnection, insertConnection);

            ConfigStore.save(config);
        }

        verify(deleteStatement).executeUpdate("DELETE FROM server_config");
        verify(deleteStatement).close();
        verify(deleteConnection).close();

        verify(insertStatement).setString(1, "example.com");
        verify(insertStatement).setInt(2, 9090);
        verify(insertStatement).setString(3, "info");
        verify(insertStatement).setString(4, "secret");
        verify(insertStatement).executeUpdate();
        verify(insertStatement).close();
        verify(insertConnection).close();
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = pe.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            ((Map<String, String>) theEnvironmentField.get(null)).putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            ((Map<String, String>) theCaseInsensitiveEnvironmentField.get(null)).putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class<?>[] classes = java.util.Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    ((Map<String, String>) field.get(env)).putAll(newenv);
                }
            }
        }
    }
}
