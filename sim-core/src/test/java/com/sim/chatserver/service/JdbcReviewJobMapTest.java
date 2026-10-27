package com.sim.chatserver.service;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import com.sim.chatserver.model.review.ReviewJobStatus;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class JdbcReviewJobMapTest {
    @Test
    void jobOwnershipIsSharedBySeparateServiceInstances() throws Exception {
        JdbcDataSource dataSource = database();
        ReviewJobService first = ReviewJobService.createWithPoolSize(1);
        ReviewJobService second = ReviewJobService.createWithPoolSize(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            first.enablePersistence(new WorkflowRecordStore(() -> dataSource));
            second.enablePersistence(new WorkflowRecordStore(() -> dataSource));
            String jobId = first.submitForUser("alice", "request", 1, currentJobId -> {
                try {
                    release.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            assertNotNull(second.getStatusForUser(jobId, "alice"));
            assertNull(second.getStatusForUser(jobId, "bob"));
            assertFalse(second.cancelForUser(jobId, "bob"));
            assertTrue(second.cancelForUser(jobId, "alice"));
            assertEquals(ReviewJobStatus.Phase.CANCELLED, first.getStatusForUser(jobId, "alice").getPhase());
        } finally {
            release.countDown();
            first.close();
            second.close();
        }
    }

    @Test
    void anotherReplicaCanReadAndCancel_andLateCompletionCannotOverwriteIt() throws Exception {
        JdbcDataSource dataSource = database();
        WorkflowRecordStore store = new WorkflowRecordStore(() -> dataSource);
        JdbcReviewJobMap first = new JdbcReviewJobMap(store);
        JdbcReviewJobMap second = new JdbcReviewJobMap(store);
        ReviewJobStatus queued = ReviewJobStatus.builder().jobId("one").requestId("request")
                .rawResponseBody("x".repeat(20000)).build();
        first.put("one", queued);
        ReviewJobStatus remote = second.get("one");
        assertEquals(queued.getRawResponseBody(), remote.getRawResponseBody());
        ReviewJobStatus previous = second.put("one", queued);
        assertEquals(queued.getJobId(), previous.getJobId());
        assertEquals(queued.getRawResponseBody(), previous.getRawResponseBody());
        ReviewJobStatus cancelled = ReviewJobStatus.builder().jobId("one").phase(ReviewJobStatus.Phase.CANCELLED)
                .done(true).finishedAtEpochMs(System.currentTimeMillis()).build();
        assertTrue(second.replace("one", remote, cancelled));
        assertFalse(first.replace("one", queued, queued));
        assertEquals(ReviewJobStatus.Phase.CANCELLED, first.get("one").getPhase());
    }

    @Test
    void expiredHeartbeatBecomesExplicitInterruptedFailure() throws Exception {
        JdbcDataSource dataSource = database();
        WorkflowRecordStore store = new WorkflowRecordStore(() -> dataSource);
        JdbcReviewJobMap jobs = new JdbcReviewJobMap(store);
        jobs.put("one", ReviewJobStatus.builder().jobId("one").build());
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("UPDATE sim_workflow_records SET touched_at=1");
        }
        ReviewJobStatus recovered = jobs.get("one");
        assertEquals(ReviewJobStatus.Phase.FAILED, recovered.getPhase());
        assertTrue(recovered.isDone());
        assertTrue(recovered.getMessage().contains("Worker stopped"));
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