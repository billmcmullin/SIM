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

    private static final Logger log = Logger.getLogger(AppDataSourceHolder.class.getCanonicalName());
    private static final String DEFAULT_DATASOURCE_JNDI = "java:jboss/datasources/ExampleDS";
    private static final String UNUSED_SUPPRESSION = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String JDBC_URL_UNKNOWN = com.sim.chatserver.util.CommonLiteralConstants.VALUE_UNKNOWN_LOWER;
    private static final String MSG_DS_CONNECTIVITY_OK = "WildFly datasource connectivity check succeeded: {0}";
    private static final String MSG_VALIDATE_DS_FAILED = "Failed to validate WildFly datasource";
    private static final String MSG_DS_OVERRIDE_SET = "DataSource override set on AppDataSourceHolder";
    private static final String MSG_CUSTOM_EMF_REMOVED = "Custom EntityManagerFactory bootstrap has been removed; use container-managed JPA.";
    private static final String MSG_NO_ACTIVE_DS_FOR_PROBE = "No active datasource available for JDBC URL probe";
    private static final String MSG_READ_ACTIVE_JDBC_URL_FAILED = "Unable to read active JDBC URL from datasource";
    private static final String MSG_DS_SWITCH_DISABLED = "Datasource switching is disabled. Use WildFly datasource configuration.";
    private static final String MSG_SWITCH_EXTERNAL_UNSUPPORTED = "switchToExternalAndPersist is not supported in WildFly-managed datasource mode.";
    private static final String MSG_RUNTIME_REFS_RELEASED = "AppDataSourceHolder released runtime references";
    private static final String MSG_DS_NOT_AVAILABLE_PREFIX = "WildFly datasource not available at ";
    private static final String MSG_JNDI_LOOKUP_FAILED_PREFIX = "JNDI lookup failed for datasource ";
    private static final String MSG_CLOSE_JNDI_CONTEXT_FAILED = "Failed to close JNDI context";

    private DataSource managedDataSource;

    // Test-time/manual override only.
    private volatile DataSource overrideDataSource;

    // Legacy test compatibility only.
    private volatile EntityManagerFactory legacyEmf;

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @PostConstruct
    private void init() {
        DataSource dataSource = requireDataSource();
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = JDBC_URL_UNKNOWN;
            if (conn.getMetaData() != null && conn.getMetaData().getURL() != null) {
                jdbcUrl = conn.getMetaData().getURL();
            }
            log.log(Level.INFO, MSG_DS_CONNECTIVITY_OK, jdbcUrl);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, MSG_VALIDATE_DS_FAILED, ex);
            throw new IllegalStateException(MSG_VALIDATE_DS_FAILED, ex);
        }
    }

    @Resource(lookup = DEFAULT_DATASOURCE_JNDI)
    void setManagedDataSource(DataSource managedDataSource) {
        this.managedDataSource = managedDataSource;
    }

    public DataSource getDataSource() {
        return requireDataSource();
    }

    public void setDataSource(DataSource dataSource) {
        this.overrideDataSource = dataSource;
        log.info(MSG_DS_OVERRIDE_SET);
    }

    /**
     * Legacy compatibility method for existing tests.
     */
    private void setEmf(EntityManagerFactory emf) {
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
                MSG_CUSTOM_EMF_REMOVED);
    }

    private String getActiveJdbcUrl() {
        DataSource dataSource;
        try {
            dataSource = requireDataSource();
        } catch (IllegalStateException ex) {
            log.log(Level.FINE, MSG_NO_ACTIVE_DS_FOR_PROBE, ex);
            return JDBC_URL_UNKNOWN;
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                return JDBC_URL_UNKNOWN;
            }
            if (conn.getMetaData() != null && conn.getMetaData().getURL() != null) {
                return conn.getMetaData().getURL();
            }
        } catch (SQLException | IllegalStateException | UnsupportedOperationException ex) {
            log.log(Level.FINE, MSG_READ_ACTIVE_JDBC_URL_FAILED, ex);
        }
        return JDBC_URL_UNKNOWN;
    }

    /**
     * Legacy compatibility method retained for API stability.
     * Runtime datasource switching is intentionally unsupported in the
     * single WildFly-managed datasource model.
     */
    private void switchToExternalAndPersist(DbConfig cfg, Consumer<String> callback) {
        if (callback != null) {
            callback.accept(MSG_DS_SWITCH_DISABLED);
        }
        throw new UnsupportedOperationException(
                MSG_SWITCH_EXTERNAL_UNSUPPORTED);
    }

    @PreDestroy
    private void close() {
        this.overrideDataSource = null;
        this.legacyEmf = null;
        log.info(MSG_RUNTIME_REFS_RELEASED);
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
            return lookedUp;
        }

        throw new IllegalStateException(MSG_DS_NOT_AVAILABLE_PREFIX + DEFAULT_DATASOURCE_JNDI);
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
            log.log(Level.FINE, MSG_JNDI_LOOKUP_FAILED_PREFIX + jndiName, ex);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException closeEx) {
                    log.log(Level.FINE, MSG_CLOSE_JNDI_CONTEXT_FAILED, closeEx);
                }
            }
        }
    }
}
