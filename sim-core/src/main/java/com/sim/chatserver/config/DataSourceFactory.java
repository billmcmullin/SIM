package com.sim.chatserver.config;

import java.text.Normalizer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Creates a HikariCP DataSource from environment variables.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static HikariDataSource createFromEnv() {
        String jdbcUrl = readEnvOrDefault("DB_URL", null, 4096);
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            String host = readEnvOrDefault("DB_HOST", "localhost", 256);
            String port = readEnvOrDefault("DB_PORT", "5432", 16);
            String db = readEnvOrDefault("DB_NAME", "chat", 256);
            jdbcUrl = "jdbc:postgresql://" + host + ':' + port + '/' + db;
        }
        String user = readEnvOrDefault("DB_USER", "postgres", 256);
        String pass = readEnvOrDefault("DB_PASSWORD", null, 4096);

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

    private static String readEnvOrDefault(String key, String fallback, int maxChars) {
        String raw = System.getenv(key);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .trim();
        if (maxChars > 0 && normalized.length() > maxChars) {
            normalized = normalized.substring(0, maxChars);
        }
        return normalized;
    }
}
