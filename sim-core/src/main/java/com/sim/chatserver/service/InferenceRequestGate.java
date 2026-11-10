package com.sim.chatserver.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public final class InferenceRequestGate {
    private static final String TEXT_ANOTHER_SIM_INFERENCE_REQUEST_IS_ACTIVE_FOR_THIS_SERVER_NO_REQUE = "Another SIM inference request is active for this server; no request was sent.";
    private static final String TEXT_HTTPS = "https";
    private static final String TEXT_EMPTY = ":";
    private static final String TEXT_SHA_256 = "SHA-256";
    private static final String TEXT_INFERENCE = "inference:";
    private static final String TEXT_INFERENCE_IS_PAUSED_FOR_THIS_SERVER_AFTER_AN_UNCERTAIN_OR_INTERR = "Inference is paused for this server after an uncertain or interrupted request. Confirm upstream is idle before manually resuming Full Analysis.";
    private static final String TEXT_AN_INFERENCE_REQUEST_IS_ACTIVE_OR_ITS_OUTCOME_IS_UNCERTAIN_REVIE = "An inference request is active or its outcome is uncertain. Review upstream logs before confirming the server is idle.";

    private static final String KIND = "inference-active";
    private static final long RETENTION = 30L * 24 * 60 * 60 * 1000;
    private static final ConcurrentHashMap<String, Semaphore> PERMITS = new ConcurrentHashMap<>();

    private InferenceRequestGate() { }

    public static final class BusyException extends IllegalStateException {
        public BusyException() {
            super(TEXT_ANOTHER_SIM_INFERENCE_REQUEST_IS_ACTIVE_FOR_THIS_SERVER_NO_REQUE);
        }
    }

    public static String serverKey(String target) {
        URI uri = URI.create(target);
        int port = uri.getPort() < 0 ? (TEXT_HTTPS.equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
        String server = uri.getHost().toLowerCase(java.util.Locale.ROOT) + TEXT_EMPTY + port;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(TEXT_SHA_256).digest(server.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    public static Lease acquire(URI target) throws InterruptedException {
        if (!WorkflowRuntime.isInitialized()) {
            return new Lease(null, null, null, null);
        }
        WorkflowRecordStore store = WorkflowRuntime.records();
        String key = serverKey(target.toString());
        Semaphore permit = PERMITS.computeIfAbsent(key, unused -> new Semaphore(1, true));
        permit.acquire();
        DatabaseTaskLock lock = null;
        try {
            lock = store.tryTaskLock(TEXT_INFERENCE + key);
            if (lock == null) {
                throw new BusyException();
            }
            if (store.find(KIND, key) != null) {
                throw new IllegalStateException(TEXT_INFERENCE_IS_PAUSED_FOR_THIS_SERVER_AFTER_AN_UNCERTAIN_OR_INTERR);
            }
            store.insert(KIND, key, TEXT_AN_INFERENCE_REQUEST_IS_ACTIVE_OR_ITS_OUTCOME_IS_UNCERTAIN_REVIE, RETENTION);
            return new Lease(store, key, permit, lock);
        } catch (RuntimeException error) {
            try {
                if (lock != null) {
                    lock.close();
                }
            } finally {
                permit.release();
            }
            throw error;
        }
    }

    public static void confirmUpstreamIdle(String target) {
        WorkflowRecordStore store = WorkflowRuntime.records();
        String key = serverKey(target);
        Semaphore permit = PERMITS.computeIfAbsent(key, unused -> new Semaphore(1, true));
        if (!permit.tryAcquire()) {
            throw new BusyException();
        }
        try (DatabaseTaskLock lock = store.tryTaskLock("inference:" + key)) {
            if (lock == null) {
                throw new BusyException();
            }
            store.delete(KIND, key);
        } finally {
            permit.release();
        }
    }

    public static final class Lease implements AutoCloseable {
        private final WorkflowRecordStore store;
        private final String key;
        private final Semaphore permit;
        private final DatabaseTaskLock lock;

        Lease(WorkflowRecordStore store, String key, Semaphore permit, DatabaseTaskLock lock) {
            this.store = store;
            this.key = key;
            this.permit = permit;
            this.lock = lock;
        }

        public void complete() {
            if (store != null) {
                store.delete(KIND, key);
            }
        }

        @Override
        public void close() {
            try {
                if (lock != null) {
                    lock.close();
                }
            } finally {
                if (permit != null) {
                    permit.release();
                }
            }
        }
    }
}