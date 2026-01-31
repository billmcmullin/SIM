package com.sim.chatserver.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String DB_HOST = requireEnv("DB_HOST");
    private static final String DB_PORT = requireEnv("DB_PORT");
    private static final String DB_NAME = requireEnv("DB_NAME");
    private static final String JDBC_USER = requireEnv("DB_USER");
    private static final String JDBC_PASSWORD = requireEnv("DB_PASSWORD");
    private static final String JDBC_URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL driver not available");
        }
    }

    private Database() {
        // utility
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is required.");
        }
        return value;
    }
}
