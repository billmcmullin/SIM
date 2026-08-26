package com.sim.chatserver.startup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sim.chatserver.config.DbConfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;

/**
 * Holds the application datasource provided by WildFly.
 *
 * This class no longer creates a custom EntityManagerFactory or datasource.
 * JPA is container-managed and JDBC access uses the server-managed datasource.
 */
@ApplicationScoped
public class AppDataSourceHolder {

    private static final Logger log = Logger.getLogger(AppDataSourceHolder.class.getName());
    private static final String DEFAULT_DATASOURCE_JNDI = "java:jboss/datasources/ExampleDS";

    @Resource(lookup = DEFAULT_DATASOURCE_JNDI)
    private DataSource managedDataSource;

    // Test-time/manual override only.
    private volatile DataSource overrideDataSource;

    // Legacy test compatibility only.
    private volatile EntityManagerFactory legacyEmf;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @PostConstruct
    synchronized void init() {
        DataSource dataSource = requireDataSource();
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = "unknown";
            if (conn.getMetaData() != null && conn.getMetaData().getURL() != null) {
                jdbcUrl = conn.getMetaData().getURL();
            }
            log.log(Level.INFO, "WildFly datasource connectivity check succeeded: {0}", jdbcUrl);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Failed to validate WildFly datasource", ex);
            throw new IllegalStateException("Failed to validate WildFly datasource", ex);
        }
    }

    public DataSource getDataSource() {
        return requireDataSource();
    }

    public synchronized void setDataSource(DataSource dataSource) {
        this.overrideDataSource = dataSource;
        log.info("DataSource override set on AppDataSourceHolder");
    }

    /**
     * Legacy compatibility method for existing tests.
     */
    synchronized void setEmf(EntityManagerFactory emf) {
        this.legacyEmf = emf;
    }

    /**
     * Legacy compatibility method for existing tests.
     * Runtime code must use container-managed JPA.
     */
    public EntityManagerFactory getEmf() {
        EntityManagerFactory local = legacyEmf;
        if (local != null) {
            return local;
        }
        throw new IllegalStateException(
                "Custom EntityManagerFactory bootstrap has been removed; use container-managed JPA.");
    }

    String getActiveJdbcUrl() {
        DataSource dataSource;
        try {
            dataSource = requireDataSource();
        } catch (IllegalStateException ex) {
            log.log(Level.FINE, "No active datasource available for JDBC URL probe", ex);
            return "unknown";
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                return "unknown";
            }
            if (conn.getMetaData() != null && conn.getMetaData().getURL() != null) {
                return conn.getMetaData().getURL();
            }
        } catch (SQLException | IllegalStateException | UnsupportedOperationException ex) {
            log.log(Level.FINE, "Unable to read active JDBC URL from datasource", ex);
        }
        return "unknown";
    }

    /**
     * Legacy compatibility method retained for API stability.
     * Runtime datasource switching is intentionally unsupported in the
     * single WildFly-managed datasource model.
     */
    void switchToExternalAndPersist(DbConfig cfg, Consumer<String> callback) {
        if (callback != null) {
            callback.accept("Datasource switching is disabled. Use WildFly datasource configuration.");
        }
        throw new UnsupportedOperationException(
                "switchToExternalAndPersist is not supported in WildFly-managed datasource mode.");
    }

    @PreDestroy
    synchronized void close() {
        this.overrideDataSource = null;
        this.legacyEmf = null;
        log.info("AppDataSourceHolder released runtime references");
    }

    private DataSource requireDataSource() {
        DataSource localOverride = overrideDataSource;
        if (localOverride != null) {
            return localOverride;
        }

        DataSource localManaged = managedDataSource;
        if (localManaged != null) {
            return localManaged;
        }

        DataSource lookedUp = lookupDataSource(DEFAULT_DATASOURCE_JNDI);
        if (lookedUp != null) {
            managedDataSource = lookedUp;
            return lookedUp;
        }

        throw new IllegalStateException("WildFly datasource not available at " + DEFAULT_DATASOURCE_JNDI);
    }

    private static DataSource lookupDataSource(String jndiName) {
        InitialContext context = null;
        try {
            context = new InitialContext();
            Object resolved = context.lookup(jndiName);
            if (resolved instanceof DataSource dataSource) {
                return dataSource;
            }
            return null;
        } catch (NamingException ex) {
            log.log(Level.FINE, "JNDI lookup failed for datasource " + jndiName, ex);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException closeEx) {
                    log.log(Level.FINE, "Failed to close JNDI context", closeEx);
                }
            }
        }
    }
}
