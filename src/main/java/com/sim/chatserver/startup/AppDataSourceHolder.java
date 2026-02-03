package com.sim.chatserver.startup;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.DataSourceFactory;
import com.sim.chatserver.config.DbConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Initializes EntityManagerFactory from environment variables (Postgres via
 * Hikari) or falls back to H2 when Postgres is not configured. Guard rails
 * prevent falling back to H2 when Postgres configuration exists but fails.
 */
@ApplicationScoped
public class AppDataSourceHolder {

    private static final Logger log = Logger.getLogger(AppDataSourceHolder.class.getName());

    private EntityManagerFactory emf;
    private HikariDataSource ds;

    @PostConstruct
    public void init() {
        String dbUrl = System.getenv("DB_URL");
        String host = System.getenv("DB_HOST");

        boolean postgresConfigured = dbUrl != null || host != null;

        if (dbUrl == null && host != null) {
            String user = System.getenv().getOrDefault("DB_USER", "postgres");
            String pass = System.getenv().get("DB_PASSWORD");
            String name = System.getenv().getOrDefault("DB_NAME", "chat");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            dbUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        }

        if (dbUrl != null && dbUrl.startsWith("jdbc:postgresql")) {
            log.info("Attempting to initialize Postgres datasource: " + dbUrl);
            try {
                ds = DataSourceFactory.createFromEnv();
                Map<String, Object> props = new HashMap<>();
                props.put("jakarta.persistence.jdbc.url", dbUrl);
                props.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
                props.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));
                props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
                props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                props.put("hibernate.hbm2ddl.auto", "update");
                emf = Persistence.createEntityManagerFactory("ChatsPU-Local", props);
                log.info("EntityManagerFactory initialized (Postgres)");
                return;
            } catch (Exception e) {
                String msg = "Failed to init Postgres datasource/emf: " + e.getMessage();
                log.log(Level.SEVERE, msg, e);
                if (postgresConfigured) {
                    throw new IllegalStateException(msg, e);
                }
            }
        }

        if (!postgresConfigured) {
            try {
                log.info("Falling back to embedded H2 database");
                Map<String, Object> props = new HashMap<>();
                props.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:chat;DB_CLOSE_DELAY=-1");
                props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
                props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                props.put("hibernate.hbm2ddl.auto", "update");
                emf = Persistence.createEntityManagerFactory("ChatsPU-Local", props);
                log.info("EntityManagerFactory initialized (H2)");
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Failed to init fallback H2 EMF: " + ex.getMessage(), ex);
            }
        } else {
            String msg = "Postgres configuration found but initialization failed; application cannot start.";
            log.severe(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Return the active EntityManagerFactory.
     */
    public synchronized EntityManagerFactory getEmf() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory not initialized. Configure DB or check logs.");
        }
        return emf;
    }

    /**
     * Set a DataSource instance (wrap or assign Hikari).
     */
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

    /**
     * Return the underlying JDBC DataSource (Hikari).
     */
    public synchronized DataSource getDataSource() {
        if (this.ds == null) {
            throw new IllegalStateException("DataSource not initialized. Configure DB or check logs.");
        }
        return this.ds;
    }

    /**
     * Set the EntityManagerFactory (used when switching DBs at runtime).
     */
    public synchronized void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("EntityManagerFactory updated on AppDataSourceHolder");
    }

    /**
     * Return the active JDBC URL if available (Hikari); otherwise return DB_URL
     * env var.
     */
    public synchronized String getActiveJdbcUrl() {
        if (this.ds != null) {
            try {
                return this.ds.getJdbcUrl();
            } catch (Exception ignored) {
            }
        }
        return System.getenv("DB_URL");
    }

    /**
     * Minimal implementation for switching to an external DB and persisting the
     * configuration. This is an administrative operation: validate
     * connectivity, create HikariDataSource, create new EMF properties, and set
     * them on the holder.
     *
     * The real implementation should also persist the config to an application
     * table.
     */
    public synchronized void switchToExternalAndPersist(DbConfig cfg, Consumer<String> callback) {
        callback.accept("Starting switchToExternalAndPersist...");
        try {
            String jdbcUrl = cfg.getJdbcUrl();
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                String host = cfg.getHost() != null ? cfg.getHost() : System.getenv().getOrDefault("DB_HOST", "localhost");
                String port = cfg.getPort() != null ? cfg.getPort() : System.getenv().getOrDefault("DB_PORT", "5432");
                String name = cfg.getDbName() != null ? cfg.getDbName() : System.getenv().getOrDefault("DB_NAME", "chat");
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name;
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
}
