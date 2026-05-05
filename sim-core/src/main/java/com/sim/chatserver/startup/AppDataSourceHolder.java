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

    private static final String DRIVER = "org.postgresql.Driver";
    private static final String DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
    private static final String PU_NAME = "ChatsPU-Local";

    // Volatile for fast, thread-safe reads without synchronized getters.
    private volatile EntityManagerFactory emf;
    private volatile HikariDataSource ds;

    // Cache env-derived defaults once (startup-time constants for this process).
    private final String envDbUrl = trimToNull(System.getenv("DB_URL"));
    private final String envDbHost = trimToNull(System.getenv("DB_HOST"));
    private final String envDbUser = trimToNull(System.getenv("DB_USER"));
    private final String envDbPassword = System.getenv("DB_PASSWORD"); // allow empty, but not null
    private final String envDbName = defaultIfBlank(System.getenv("DB_NAME"), "chat");
    private final String envDbPort = defaultIfBlank(System.getenv("DB_PORT"), "5432");

    @PostConstruct
    public synchronized void init() {
        String dbUrl = envDbUrl;
        String host = envDbHost;
        String user = envDbUser;
        String pass = envDbPassword;

        if (dbUrl == null && host == null) {
            throw new IllegalStateException("PostgreSQL required: set DB_URL or DB_HOST.");
        }

        if (dbUrl == null) {
            dbUrl = String.format("jdbc:postgresql://%s:%s/%s", host, envDbPort, envDbName);
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
            HikariDataSource newDs = createHikariDataSource(dbUrl, user, pass, 10, 2, 15000);

            try (var conn = newDs.getConnection()) {
                log.log(Level.INFO, "PostgreSQL connectivity check succeeded: {0}", dbUrl);
            }

            Map<String, Object> props = createJpaPropsWithDataSource(newDs, "update");
            EntityManagerFactory newEmf = Persistence.createEntityManagerFactory(PU_NAME, props);

            this.ds = newDs;
            this.emf = newEmf;

            log.info("EntityManagerFactory initialized (PostgreSQL only)");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize PostgreSQL datasource/EMF", e);
            throw new IllegalStateException("Failed to initialize PostgreSQL datasource/EMF", e);
        }
    }

    public EntityManagerFactory getEmf() {
        EntityManagerFactory local = emf;
        if (local == null) {
            throw new IllegalStateException("EntityManagerFactory not initialized.");
        }
        return local;
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

    public DataSource getDataSource() {
        DataSource local = this.ds;
        if (local == null) {
            throw new IllegalStateException("DataSource not initialized.");
        }
        return local;
    }

    public synchronized void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
        log.info("EntityManagerFactory updated on AppDataSourceHolder");
    }

    public String getActiveJdbcUrl() {
        HikariDataSource localDs = this.ds;
        if (localDs != null) {
            try {
                return localDs.getJdbcUrl();
            } catch (Exception ignored) {
                // fall back below
            }
        }
        return envDbUrl;
    }

    public void switchToExternalAndPersist(DbConfig cfg, Consumer<String> callback) {
        callback.accept("Starting switchToExternalAndPersist...");
        HikariDataSource newDs = null;
        EntityManagerFactory newEmf = null;

        try {
            String jdbcUrl = cfg.getJdbcUrl();
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                String host = cfg.getHost() != null ? cfg.getHost() : defaultIfBlank(envDbHost, "localhost");
                String port = cfg.getPort() != null ? cfg.getPort() : defaultIfBlank(envDbPort, "5432");
                String name = cfg.getDbName() != null ? cfg.getDbName() : defaultIfBlank(envDbName, "chat");
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name;
            }

            if (!jdbcUrl.startsWith("jdbc:postgresql:")) {
                throw new IllegalArgumentException("Only PostgreSQL is supported. jdbcUrl=" + jdbcUrl);
            }

            if (cfg.getUsername() == null || cfg.getUsername().isBlank()) {
                throw new IllegalArgumentException("Username is required.");
            }
            if (cfg.getPassword() == null) {
                throw new IllegalArgumentException("Password is required.");
            }

            callback.accept("Creating HikariDataSource for: " + jdbcUrl);

            int maxPool = cfg.getMaxPoolSize() > 0 ? cfg.getMaxPoolSize() : 10;
            newDs = createHikariDataSource(jdbcUrl, cfg.getUsername(), cfg.getPassword(), maxPool, 2, 15000);

            try (var conn = newDs.getConnection()) {
                callback.accept("Connection test succeeded");
            }

            String hbm2ddl = "update";


            Map<String, Object> props = createJpaPropsWithDataSource(newDs, hbm2ddl);
            newEmf = Persistence.createEntityManagerFactory(PU_NAME, props);

            HikariDataSource oldDs;
            EntityManagerFactory oldEmf;

            // Minimal critical section: atomic swap only.
            synchronized (this) {
                oldDs = this.ds;
                oldEmf = this.emf;
                this.ds = newDs;
                this.emf = newEmf;
            }

            // Close old resources outside lock.
            closeQuietly(oldEmf);
            closeQuietly(oldDs);

            callback.accept("switchToExternalAndPersist: success; datasource and EMF updated.");
        } catch (Exception e) {
            // If partially created, clean up new resources on failure.
            closeQuietly(newEmf);
            closeQuietly(newDs);

            log.log(Level.SEVERE, "switchToExternalAndPersist failed: " + e.getMessage(), e);
            callback.accept("switchToExternalAndPersist failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public synchronized void close() {
        EntityManagerFactory localEmf = this.emf;
        HikariDataSource localDs = this.ds;
        this.emf = null;
        this.ds = null;

        closeQuietly(localEmf);
        closeQuietly(localDs);

        log.info("AppDataSourceHolder closed resources");
    }

    private static HikariDataSource createHikariDataSource(
            String jdbcUrl,
            String username,
            String password,
            int maxPoolSize,
            int minIdle,
            long connectionTimeoutMs) {

        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(jdbcUrl);
        if (username != null) {
            hc.setUsername(username);
        }
        if (password != null) {
            hc.setPassword(password);
        }
        hc.setDriverClassName(DRIVER);
        hc.setMaximumPoolSize(maxPoolSize);
        hc.setMinimumIdle(minIdle);
        hc.setConnectionTimeout(connectionTimeoutMs);

        // Fail fast
        hc.setInitializationFailTimeout(1);

        return new HikariDataSource(hc);
    }

    private static Map<String, Object> createJpaPropsWithDataSource(DataSource dataSource, String hbm2ddl) {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.nonJtaDataSource", dataSource);
        props.put("hibernate.dialect", DIALECT);
        props.put("hibernate.hbm2ddl.auto", hbm2ddl);
        return props;
    }

    private static void closeQuietly(EntityManagerFactory factory) {
        if (factory != null) {
            try {
                factory.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void closeQuietly(HikariDataSource dataSource) {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception ignored) {
            }
        }
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
