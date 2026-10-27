package com.sim.chatserver.service;

import java.io.StringReader;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jakarta.json.Json;
import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.util.CommonLiteralConstants;

final class JdbcReviewJobMap extends AbstractMap<String, ReviewJobStatus> {
    private static final String KIND = "review-job";
    private static final String PHASE_FAILED = "FAILED";
    private static final String ACTIVITY_INTERRUPTED = "Interrupted";
    private static final String MSG_WORKER_STOPPED = "Worker stopped; reopen the saved selection to retry.";
    private static final String MSG_HEARTBEAT_EXPIRED = "Worker heartbeat expired. This job was not replayed automatically.";
    private static final long RETENTION_MILLIS = 7L * 24 * 60 * 60 * 1000;
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 120_000;
    private final WorkflowRecordStore store;

    JdbcReviewJobMap(WorkflowRecordStore store) {
        this.store = store;
    }

    @Override
    public ReviewJobStatus get(Object key) {
        if (!(key instanceof String id)) {
            return null;
        }
        while (true) {
            WorkflowRecordStore.StoredRecord record = store.find(KIND, id);
            if (record == null) {
                return null;
            }
            ReviewJobStatus status = ReviewJobStatusCodec.decode(record.payload());
            long now = store.currentTimeMillis();
            if (status.isDone() || record.touchedAt() > now - HEARTBEAT_TIMEOUT_MILLIS) {
                return status;
            }
            try (var reader = Json.createReader(new StringReader(record.payload()))) {
                ReviewJobStatus interrupted = ReviewJobStatusCodec.decode(Json.createObjectBuilder(reader.readObject())
                        .add(CommonLiteralConstants.FIELD_PHASE, PHASE_FAILED).add(CommonLiteralConstants.FIELD_DONE, true)
                        .add(CommonLiteralConstants.FIELD_SUCCESS, false).add(CommonLiteralConstants.STATUS_RUNNING, false)
                        .add(CommonLiteralConstants.FIELD_HTTP_STATUS, 503).add(CommonLiteralConstants.FIELD_UPDATED_AT_EPOCH_MS, now)
                        .add(CommonLiteralConstants.FIELD_FINISHED_AT_EPOCH_MS, now)
                        .add(CommonLiteralConstants.FIELD_ACTIVITY, ACTIVITY_INTERRUPTED).add(CommonLiteralConstants.FIELD_MESSAGE, MSG_WORKER_STOPPED)
                        .add(CommonLiteralConstants.FIELD_ERROR_MESSAGE, MSG_HEARTBEAT_EXPIRED).build());
                if (store.replace(KIND, id, record.payload(), ReviewJobStatusCodec.encode(interrupted), RETENTION_MILLIS,
                        now - HEARTBEAT_TIMEOUT_MILLIS)) {
                    return interrupted;
                }
            }
        }
    }

    void heartbeat(String id) {
        store.touch(KIND, id);
    }

    @Override
    public ReviewJobStatus put(String id, ReviewJobStatus status) {
        String payload = ReviewJobStatusCodec.encode(status);
        while (true) {
            WorkflowRecordStore.StoredRecord previous = store.find(KIND, id);
            if (previous == null) {
                store.insert(KIND, id, payload, RETENTION_MILLIS);
                return null;
            }
            if (store.replace(KIND, id, previous.payload(), payload, RETENTION_MILLIS, Long.MAX_VALUE)) {
                return ReviewJobStatusCodec.decode(previous.payload());
            }
        }
    }

    @Override
    public boolean replace(String id, ReviewJobStatus expected, ReviewJobStatus replacement) {
        return store.replace(KIND, id, ReviewJobStatusCodec.encode(expected), ReviewJobStatusCodec.encode(replacement),
                RETENTION_MILLIS, Long.MAX_VALUE);
    }

    @Override
    public boolean containsKey(Object id) {
        return get(id) != null;
    }

    @Override
    public ReviewJobStatus remove(Object key) {
        ReviewJobStatus status = get(key);
        if (key instanceof String id) {
            store.delete(KIND, id);
        }
        return status;
    }

    @Override
    public Set<Map.Entry<String, ReviewJobStatus>> entrySet() {
        Map<String, ReviewJobStatus> entries = new LinkedHashMap<>();
        store.list(KIND).forEach((id, record) -> entries.put(id, ReviewJobStatusCodec.decode(record.payload())));
        return entries.entrySet();
    }
}