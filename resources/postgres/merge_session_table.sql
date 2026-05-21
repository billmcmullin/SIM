-- 001_customer_identity.sql

CREATE TABLE IF NOT EXISTS customer_identity (
    identity_id BIGSERIAL PRIMARY KEY,
    canonical_email VARCHAR(320),
    canonical_name VARCHAR(256),

    salesforce_contact_id VARCHAR(64),
    salesforce_account_id VARCHAR(64),

    email_enc TEXT,
    phone_enc TEXT,
    title_enc TEXT,
    department_enc TEXT,
    raw_json_enc TEXT,

    confidence VARCHAR(24) NOT NULL DEFAULT 'high', -- high|medium|low
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMPTZ
);

-- Unique person key by email when present
CREATE UNIQUE INDEX IF NOT EXISTS ux_customer_identity_email
    ON customer_identity (LOWER(canonical_email))
    WHERE canonical_email IS NOT NULL AND canonical_email <> '';

-- Fallback lookup by name (not unique)
CREATE INDEX IF NOT EXISTS ix_customer_identity_name
    ON customer_identity (LOWER(canonical_name));


CREATE TABLE IF NOT EXISTS customer_identity_session (
    session_id TEXT PRIMARY KEY,
    identity_id BIGINT NOT NULL REFERENCES customer_identity(identity_id) ON DELETE CASCADE,

    display_name_snapshot VARCHAR(256),
    contact_email_snapshot VARCHAR(320),

    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_customer_identity_session_identity
    ON customer_identity_session (identity_id);

CREATE INDEX IF NOT EXISTS ix_customer_identity_session_email
    ON customer_identity_session (LOWER(contact_email_snapshot));

ALTER TABLE customer_identity
  ALTER COLUMN session_id DROP NOT NULL;
