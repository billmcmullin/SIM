package com.sim.chatserver.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class WorkflowRecordStoreTest {
    @Test
    void recordsSurviveNewStoreInstances_andUpdatesAreCompareAndSet() throws Exception {
        JdbcDataSource dataSource = database();
        AtomicLong clock = new AtomicLong(1000);
        WorkflowRecordStore first = new WorkflowRecordStore(() -> dataSource, clock::get);
        WorkflowRecordStore second = new WorkflowRecordStore(() -> dataSource, clock::get);
        first.insert("job", "one", "queued", 10000);
        assertEquals("queued", second.find("job", "one").payload());
        assertTrue(first.replace("job", "one", "queued", "cancelled", 10000, Long.MAX_VALUE));
        assertFalse(second.replace("job", "one", "queued", "completed", 10000, Long.MAX_VALUE));
        assertEquals("cancelled", second.find("job", "one").payload());
        assertNull(second.find("draft:another-user", "one"));
        clock.set(12000);
        assertNull(first.find("job", "one"));
        first.purgeExpired();
        assertTrue(first.list("job").isEmpty());
    }

    @Test
    void heartbeatPreventsStaleWorkerRecovery() throws Exception {
        JdbcDataSource dataSource = database();
        AtomicLong clock = new AtomicLong(1000);
        WorkflowRecordStore store = new WorkflowRecordStore(() -> dataSource, clock::get);
        store.insert("job", "one", "running", 100000);
        clock.set(10000);
        store.touch("job", "one");
        assertFalse(store.replace("job", "one", "running", "interrupted", 100000, 5000));
        assertTrue(store.replace("job", "one", "running", "interrupted", 100000, 11000));
    }

    private JdbcDataSource database() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE sim_workflow_records(record_kind VARCHAR(256),record_id VARCHAR(128),payload TEXT,touched_at BIGINT,expires_at BIGINT,PRIMARY KEY(record_kind,record_id))");
        }
        return dataSource;
    }
}