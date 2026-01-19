package com.sim.chatserver.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class DbBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(DbBootstrapService.class);

    private HikariDataSource ds;
    private EntityManagerFactory emf;

    public DataSource createAndTestDataSource(String jdbcUrl, String user, String pass, String driverClass) throws Exception {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        if (driverClass != null && !driverClass.isEmpty()) {
            cfg.setDriverClassName(driverClass);
        }
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(1);
        cfg.setAutoCommit(true);

        ds = new HikariDataSource(cfg);

        // test connection
        try (Connection c = ds.getConnection()) {
            c.createStatement().executeQuery("SELECT 1");
        }

        return ds;
    }

    public void runFlywayMigrations(DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        log.info("Running Flyway migration...");
        flyway.migrate();
        log.info("Flyway migration complete");
    }

    public EntityManagerFactory buildEntityManagerFactory(DataSource ds) {
        // Pass the DataSource instance in the properties map. Hibernate accepts the DataSource instance under this key.
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.nonJtaDataSource", ds);
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "validate");
        emf = Persistence.createEntityManagerFactory("ChatsPU-Local", props);
        return emf;
    }

    public void shutdown() {
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
    }

    /**
     * Try to create the target database on the Postgres server if it does not
     * exist. Requires the supplied user to have CREATE DATABASE privileges.
     */
    public void createDatabaseIfNotExists(String jdbcUrl, String user, String pass) throws Exception {
        // Parse jdbc:postgresql://host:port/dbname[?params]
        String url = jdbcUrl;
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("Only postgresql JDBC URLs are supported for auto-create");
        }
        String withoutPrefix = url.substring("jdbc:postgresql://".length());
        // withoutPrefix = host:port/dbname...?params
        String hostPortAndRest;
        int slashIdx = withoutPrefix.indexOf('/');
        if (slashIdx < 0) {
            throw new IllegalArgumentException("Invalid JDBC URL, missing database name: " + jdbcUrl);
        }
        hostPortAndRest = withoutPrefix.substring(0, slashIdx);
        String rest = withoutPrefix.substring(slashIdx + 1);
        String dbName;
        int qIdx = rest.indexOf('?');
        if (qIdx >= 0) {
            dbName = rest.substring(0, qIdx); 
        }else {
            dbName = rest;
        }

        // build maintenance URL connecting to 'postgres' database
        String maintUrl = "jdbc:postgresql://" + hostPortAndRest + "/postgres";
        try (var conn = DriverManager.getConnection(maintUrl, user, pass)) {
            // check if db exists
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, dbName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // database exists
                        return;
                    }
                }
            }
            // create database
            String createSql = "CREATE DATABASE \"" + dbName.replace("\"", "\"\"") + "\"";
            try (Statement s = conn.createStatement()) {
                s.executeUpdate(createSql);
            }
        }
    }

}
