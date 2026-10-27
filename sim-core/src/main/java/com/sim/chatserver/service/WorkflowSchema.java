package com.sim.chatserver.service;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class WorkflowSchema {
    private static final String HISTORY_TABLE = "sim_workflow_schema_history";
    private static final String MIGRATION_LOCATION = "classpath:db/workflows";
    private static final String BASELINE_VERSION = "0";
    private WorkflowSchema() { }

    public static void migrate(DataSource dataSource) {
        Flyway.configure().dataSource(dataSource)
                .table(HISTORY_TABLE)
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(true).baselineVersion(BASELINE_VERSION)
                .cleanDisabled(true).load().migrate();
    }
}