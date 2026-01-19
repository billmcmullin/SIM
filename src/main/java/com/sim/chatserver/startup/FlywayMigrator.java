package com.sim.chatserver.startup;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlywayMigrator {
    private static final Logger log = LoggerFactory.getLogger(FlywayMigrator.class);

    @PostConstruct
    public void migrate() {
        DataSource dataSource = null;
        try {
            InitialContext ctx = new InitialContext();
            // JNDI name used in persistence.xml and WildFly datasource configuration
            dataSource = (DataSource) ctx.lookup("java:jboss/datasources/ChatDS");
        } catch (NamingException ne) {
            log.error("Failed to lookup DataSource via JNDI", ne);
            throw new RuntimeException(ne);
        }

        try {
            log.info("Running Flyway migrations");
            Flyway flyway = Flyway.configure().dataSource(dataSource).load();
            flyway.migrate();
            log.info("Flyway migrations complete");
        } catch (Exception e) {
            log.error("Flyway migration failed", e);
            throw new RuntimeException(e);
        }
    }
}
