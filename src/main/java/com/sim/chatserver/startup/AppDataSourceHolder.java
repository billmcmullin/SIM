package com.sim.chatserver.startup;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.DbConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Initializes EntityManagerFactory from PostgreSQL environment variables only.
 * Fails fast when PostgreSQL config is missing or invalid.
 */
@ApplicationScoped
public class AppDataSourceHolder {

    private static final Logger log = Logger.getLogger(AppDataSourceHolder.class.getName());

    private EntityManagerFactory emf;
    private HikariDataSource ds;

    @PostConstruct
    public void init() {
        String dbUrl = trimToNull(System.getenv("DB_URL"));
        String host = trimToNull(System.getenv("DB_HOST"));
        String user = trimToNull(System.getenv("DB_USER"));
        String pass = System.getenv("DB_PASSWORD"); // allow empty, but not null
        String name = defaultIfBlank(System.getenv("DB_NAME"), "chat");
        String port = defaultIfBlank(System.getenv("DB_PORT"), "5432");

        if (dbUrl == null && host == null) {
            throw new IllegalStateException("PostgreSQL required: set DB_URL or DB_HOST.");
        }

        if (dbUrl == null) {
            dbUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        }

        if (!dbUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("Only PostgreSQL is supported. DB_URL=" + dbUrl);
        }

        if (user == null) {
            throw new IllegalStateException("PostgreSQL required: DB_USER is missing.");
        }
        if (pass == null) {
            throw new IllegalStateException("PostgreSQL required: DB_PASSWORD is missing.");
        }

        try {
            HikariConfig hc = new HikariConfig();
            hc.setJdbcUrl(dbUrl);
            hc.setUsername(user);
            hc.setPassword(pass);
            hc.setDriverClassName("org.postgresql.Driver");
            hc.setMaximumPoolSize(10);
            hc.setMinimumIdle(2);
            hc.setConnectionTimeout(15000);

            this.ds = new HikariDataSource(hc);

            try (var conn = this.ds.getConnection()) {
                log.info("PostgreSQL connectivity check succeeded: " + dbUrl);
            }

            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", dbUrl);
            props.put("jakarta.persistence.jdbc.user", user);
            props.put("jakarta.persistence.jdbc.password", pass);
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "update");

            this.emf = Persistence.createEntityManagerFactory("ChatsPU-Local", props);
            log.info("EntityManagerFactory initialized (PostgreSQL only)");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize PostgreSQL datasource/EMF", e);
            throw new IllegalStateException("Failed to initialize PostgreSQL datasource/EMF", e);
        }
    }

    public synchronized EntityManagerFactory getEmf() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory not initialized.");
        }
        return emf;
    }

    public synchronized void setDataSource(DataSource dataSource) {
        try {
            if (dataSource instanceof HikariDataSource) {
                this.ds = (HikariDataSource) dataSource;
            } else {
                HikariConfig cfg = new HikariConfig();
                cfg.setDataSource(dataSource);
                this.ds = new HikariDataSource(cfg);
            }
            log.info("DataSource set on AppDataSourceHolder");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to set DataSource on AppDataSourceHolder: " + e.getMessage(), e);
        }
    }

    public synchronized DataSource getDataSource() {
        if (this.ds == null) {
            throw new IllegalStateException("DataSource not initialized.");
        }
        return this.ds;
    }

    public synchronized void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("EntityManagerFactory updated on AppDataSourceHolder");
    }

    public synchronized String getActiveJdbcUrl() {
        if (this.ds != null) {
            try {
                return this.ds.getJdbcUrl();
            } catch (Exception ignored) {
            }
        }
        return System.getenv("DB_URL");
    }

    public synchronized void switchToExternalAndPersist(DbConfig cfg, Consumer<String> callback) {
        callback.accept("Starting switchToExternalAndPersist...");
        try {
            String jdbcUrl = cfg.getJdbcUrl();
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                String host = cfg.getHost() != null ? cfg.getHost() : defaultIfBlank(System.getenv("DB_HOST"), "localhost");
                String port = cfg.getPort() != null ? cfg.getPort() : defaultIfBlank(System.getenv("DB_PORT"), "5432");
                String name = cfg.getDbName() != null ? cfg.getDbName() : defaultIfBlank(System.getenv("DB_NAME"), "chat");
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name;
            }

            if (!jdbcUrl.startsWith("jdbc:postgresql:")) {
                throw new IllegalArgumentException("Only PostgreSQL is supported. jdbcUrl=" + jdbcUrl);
            }

            callback.accept("Creating HikariDataSource for: " + jdbcUrl);
            HikariConfig cfgH = new HikariConfig();
            cfgH.setJdbcUrl(jdbcUrl);
            if (cfg.getUsername() != null) {
                cfgH.setUsername(cfg.getUsername());
            }
            if (cfg.getPassword() != null) {
                cfgH.setPassword(cfg.getPassword());
            }
            cfgH.setDriverClassName("org.postgresql.Driver");
            cfgH.setMaximumPoolSize(cfg.getMaxPoolSize() > 0 ? cfg.getMaxPoolSize() : 10);

            HikariDataSource newDs = new HikariDataSource(cfgH);

            try (var conn = newDs.getConnection()) {
                callback.accept("Connection test succeeded");
            }

            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", jdbcUrl);
            props.put("jakarta.persistence.jdbc.user", cfg.getUsername());
            props.put("jakarta.persistence.jdbc.password", cfg.getPassword());
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "update");

            EntityManagerFactory newEmf = Persistence.createEntityManagerFactory("ChatsPU-Local", props);

            if (this.ds != null) {
                try {
                    this.ds.close();
                } catch (Exception ignored) {
                }
            }
            if (this.emf != null) {
                try {
                    this.emf.close();
                } catch (Exception ignored) {
                }
            }

            this.ds = newDs;
            this.emf = newEmf;

            callback.accept("switchToExternalAndPersist: success; datasource and EMF updated.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "switchToExternalAndPersist failed: " + e.getMessage(), e);
            callback.accept("switchToExternalAndPersist failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public synchronized void close() {
        try {
            if (emf != null) {
                emf.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (ds != null) {
                ds.close();
            }
        } catch (Exception ignored) {
        }
        log.info("AppDataSourceHolder closed resources");
    }

    private static String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String defaultIfBlank(String v, String fallback) {
        String t = trimToNull(v);
        return t == null ? fallback : t;
    }
}
