CREATE TABLE sim_workflow_records (
    record_kind VARCHAR(256) NOT NULL,
    record_id VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    touched_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    PRIMARY KEY (record_kind, record_id)
);
CREATE INDEX sim_workflow_expiry_idx ON sim_workflow_records (expires_at);