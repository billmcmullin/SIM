package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public final class Database {

    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+$");
    private static final Pattern DB_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");

    private static final String DB_HOST = requireValidHost("DB_HOST");
    private static final int DB_PORT = requireValidPort("DB_PORT");
    private static final String DB_NAME = requireValidDbName("DB_NAME");
    private static final String JDBC_USER = requireEnv("DB_USER");
    private static final String JDBC_PASSWORD = requireEnv("DB_PASSWORD");
    private static final DataSource DATA_SOURCE = buildDataSource(DB_HOST, DB_PORT, DB_NAME, JDBC_USER, JDBC_PASSWORD);

    private Database() {
        // utility class
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    private static DataSource buildDataSource(String host, int port, String dbName, String user, String password) {
        try {
            Class<?> dataSourceClass = Class.forName("org.postgresql.ds.PGSimpleDataSource");
            Object dataSourceObject = dataSourceClass.getDeclaredConstructor().newInstance();

            dataSourceClass.getMethod("setServerNames", String[].class).invoke(dataSourceObject, (Object) new String[]{host});
            dataSourceClass.getMethod("setPortNumbers", int[].class).invoke(dataSourceObject, (Object) new int[]{port});
            dataSourceClass.getMethod("setDatabaseName", String.class).invoke(dataSourceObject, dbName);
            dataSourceClass.getMethod("setUser", String.class).invoke(dataSourceObject, user);
            dataSourceClass.getMethod("setPassword", String.class).invoke(dataSourceObject, password);

            if (!(dataSourceObject instanceof DataSource dataSource)) {
                throw new IllegalStateException("Configured PostgreSQL data source is not a javax.sql.DataSource.");
            }
            return dataSource;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to initialize PostgreSQL data source.", ex);
        }
    }

    private static String requireEnv(String name) {
        String value = ENV.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is required.");
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is required.");
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new IllegalStateException("Environment variable " + name + " contains invalid control characters.");
        }
        if (normalized.length() > 1024) {
            throw new IllegalStateException("Environment variable " + name + " is too long.");
        }
        return normalized;
    }

    private static String requireValidHost(String name) {
        String host = requireEnv(name);
        if (!HOST_PATTERN.matcher(host).matches()) {
            throw new IllegalStateException(
                "Environment variable " + name + " contains invalid host characters.");
        }
        return host;
    }

    private static int requireValidPort(String name) {
        String portValue = requireEnv(name);
        try {
            int port = Integer.parseInt(portValue);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException(
                    "Environment variable " + name + " must be between 1 and 65535.");
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                "Environment variable " + name + " must be a valid integer.", ex);
        }
    }

    private static String requireValidDbName(String name) {
        String dbName = requireEnv(name);
        if (!DB_NAME_PATTERN.matcher(dbName).matches()) {
            throw new IllegalStateException(
                "Environment variable " + name + " must contain only letters, digits, or underscore.");
        }
        return dbName;
    }
}
