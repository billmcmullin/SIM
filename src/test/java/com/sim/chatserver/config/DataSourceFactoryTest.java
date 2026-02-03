package com.sim.chatserver.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

class DataSourceFactoryTest {

    private static final Map<String, String> BASE_ENV = Map.of(
            "DB_HOST", "localhost",
            "DB_PORT", "5432",
            "DB_NAME", "chat",
            "DB_USER", "postgres");

    @AfterEach
    void restoreEnv() throws Exception {
        setEnv(BASE_ENV);
    }

    @Test
    void createFromEnv_usesUrlWhenProvided() throws Exception {
        Map<String, String> env = new HashMap<>(BASE_ENV);
        env.put("DB_URL", "jdbc:postgresql://custom:1234/appdb");
        env.put("DB_PASSWORD", "secret");
        setEnv(env);

        List<HikariConfig> captured = new ArrayList<>();
        try (MockedConstruction<HikariDataSource> mocked = mockConstruction(HikariDataSource.class,
                (mock, context) -> captured.add((HikariConfig) context.arguments().get(0)))) {

            DataSourceFactory.createFromEnv();
        }

        HikariConfig cfg = captured.get(0);
        assertEquals("jdbc:postgresql://custom:1234/appdb", cfg.getJdbcUrl());
        assertEquals("postgres", cfg.getUsername());
        assertEquals("secret", cfg.getPassword());
        assertEquals("org.postgresql.Driver", cfg.getDriverClassName());
        assertEquals(10, cfg.getMaximumPoolSize());
        assertEquals("chatserver-hikari", cfg.getPoolName());
    }

    @Test
    void createFromEnv_buildsUrlWhenMissing() throws Exception {
        Map<String, String> env = new HashMap<>(BASE_ENV);
        env.put("DB_HOST", "dbhost");
        env.put("DB_PORT", "6543");
        env.put("DB_NAME", "example");
        env.remove("DB_URL");
        env.remove("DB_PASSWORD");
        setEnv(env);

        List<HikariConfig> captured = new ArrayList<>();
        try (MockedConstruction<HikariDataSource> mocked = mockConstruction(HikariDataSource.class,
                (mock, context) -> captured.add((HikariConfig) context.arguments().get(0)))) {

            DataSourceFactory.createFromEnv();
        }

        HikariConfig cfg = captured.get(0);
        assertEquals("jdbc:postgresql://dbhost:6543/example", cfg.getJdbcUrl());
        assertEquals("postgres", cfg.getUsername());
        assertNull(cfg.getPassword());
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
}
