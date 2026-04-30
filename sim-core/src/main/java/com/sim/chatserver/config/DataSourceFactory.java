package com.sim.chatserver.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates a HikariCP DataSource from environment variables.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static HikariDataSource createFromEnv() {
        String jdbcUrl = System.getenv("DB_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            String db = System.getenv().getOrDefault("DB_NAME", "chat");
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        }
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String pass = System.getenv("DB_PASSWORD");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        if (pass != null) {
            cfg.setPassword(pass);
        }
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setMaximumPoolSize(10);
        cfg.setPoolName("chatserver-hikari");
        return new HikariDataSource(cfg);
    }
}
