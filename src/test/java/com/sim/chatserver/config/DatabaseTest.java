package com.sim.chatserver.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class DatabaseTest {

    private static final Map<String, String> BASE_ENV = Map.of(
            "DB_HOST", "localhost",
            "DB_PORT", "5432",
            "DB_NAME", "chat",
            "DB_USER", "user",
            "DB_PASSWORD", "pass");

    @BeforeAll
    static void setupEnv() throws Exception {
        setEnv(BASE_ENV);
    }

    @Test
    void getConnection_delegatesToDriverManager() throws SQLException, ReflectiveOperationException {
        String jdbcUrl = getStaticField("JDBC_URL");
        String user = getStaticField("JDBC_USER");
        String password = getStaticField("JDBC_PASSWORD");

        Connection connection = mock(Connection.class);
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(jdbcUrl, user, password))
                    .thenReturn(connection);

            Connection result = Database.getConnection();

            assertSame(connection, result);
            driverManager.verify(() -> DriverManager.getConnection(jdbcUrl, user, password));
        }
    }

    @Test
    void requireEnv_returnsValue_whenPresent() throws Exception {
        Method method = Database.class.getDeclaredMethod("requireEnv", String.class);
        method.setAccessible(true);

        String host = (String) method.invoke(null, "DB_HOST");
        assertEquals("localhost", host);
    }

    @Test
    void requireEnv_throws_whenBlank() throws Exception {
        Map<String, String> modified = new HashMap<>(BASE_ENV);
        modified.put("DB_HOST", " ");

        setEnv(modified);
        try {
            Method method = Database.class.getDeclaredMethod("requireEnv", String.class);
            method.setAccessible(true);

            assertThrows(IllegalStateException.class, () -> {
                try {
                    method.invoke(null, "DB_HOST");
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        } finally {
            setEnv(BASE_ENV);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");
            Field envField = processEnvironment.getDeclaredField("theEnvironment");
            envField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) envField.get(null);
            env.clear();
            env.putAll(newenv);

            Field cienvField = processEnvironment.getDeclaredField("theCaseInsensitiveEnvironment");
            cienvField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) cienvField.get(null);
            cienv.clear();
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Map<String, String> env = System.getenv();
            Class<?> clazz = env.getClass();
            Field field = clazz.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> modifiableEnv = (Map<String, String>) field.get(env);
            modifiableEnv.clear();
            modifiableEnv.putAll(newenv);
        }
    }

    private static String getStaticField(String name) throws ReflectiveOperationException {
        Field field = Database.class.getDeclaredField(name);
        field.setAccessible(true);
        return (String) field.get(null);
    }
}
