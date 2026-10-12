package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public final class Database {

    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+$");
    private static final Pattern DB_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final String MSG_ENV_PREFIX = "Environment variable ";
    private static final String MSG_REQUIRED_SUFFIX = " is required.";
    private static final String MSG_INVALID_CONTROL_CHARS_SUFFIX = " contains invalid control characters.";
    private static final String MSG_TOO_LONG_SUFFIX = " is too long.";
    private static final String MSG_INVALID_HOST_SUFFIX = " contains invalid host characters.";
    private static final String MSG_PORT_RANGE_SUFFIX = " must be between 1 and 65535.";
    private static final String MSG_INVALID_INTEGER_SUFFIX = " must be a valid integer.";
    private static final String MSG_INVALID_DB_NAME_SUFFIX = " must contain only letters, digits, or underscore.";

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
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{host});
        dataSource.setPortNumbers(new int[]{port});
        dataSource.setDatabaseName(dbName);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    private static String requireEnv(String name) {
        String value = ENV.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(MSG_ENV_PREFIX + name + MSG_REQUIRED_SUFFIX);
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            throw new IllegalStateException(MSG_ENV_PREFIX + name + MSG_REQUIRED_SUFFIX);
        }
        if (CONTROL_CHARS.matcher(normalized).find()) {
            throw new IllegalStateException(MSG_ENV_PREFIX + name + MSG_INVALID_CONTROL_CHARS_SUFFIX);
        }
        if (normalized.length() > 1024) {
            throw new IllegalStateException(MSG_ENV_PREFIX + name + MSG_TOO_LONG_SUFFIX);
        }
        return normalized;
    }

    private static String requireValidHost(String name) {
        String host = requireEnv(name);
        if (!HOST_PATTERN.matcher(host).matches()) {
            throw new IllegalStateException(
                MSG_ENV_PREFIX + name + MSG_INVALID_HOST_SUFFIX);
        }
        return host;
    }

    private static int requireValidPort(String name) {
        String portValue = requireEnv(name);
        try {
            int port = Integer.parseInt(portValue);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException(
                    MSG_ENV_PREFIX + name + MSG_PORT_RANGE_SUFFIX);
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                MSG_ENV_PREFIX + name + MSG_INVALID_INTEGER_SUFFIX, ex);
        }
    }

    private static String requireValidDbName(String name) {
        String dbName = requireEnv(name);
        if (!DB_NAME_PATTERN.matcher(dbName).matches()) {
            throw new IllegalStateException(
                MSG_ENV_PREFIX + name + MSG_INVALID_DB_NAME_SUFFIX);
        }
        return dbName;
    }
}
