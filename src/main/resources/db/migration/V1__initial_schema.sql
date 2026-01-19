-- V1__initial_schema.sql
-- Create required extensions (if allowed). If your DB user cannot create extensions,
-- run these as a superuser on the DB manually before running the app.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS vector;

-- Users table
CREATE TABLE IF NOT EXISTS user_account (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  username text NOT NULL UNIQUE,
  password_hash text NOT NULL,
  role text NOT NULL,
  full_name text,
  email text,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- Widgets mapping
CREATE TABLE IF NOT EXISTS widget_mapping (
  embed_uuid text PRIMARY KEY,
  display_name text,
  category text,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- Chats (chat messages / prompts / responses)
CREATE TABLE IF NOT EXISTS chat (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  embed_uuid text REFERENCES widget_mapping(embed_uuid) ON DELETE SET NULL,
  prompt text,
  text text,
  metadata jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  -- embedding vector column for pgvector (adjust dimension if needed)
  embedding vector(1536),
  -- tsvector column for full-text indexing (keeps simple)
  indexed_tsv tsvector
);

-- Populate indexed_tsv from prompt and text (if database supports generated columns, else use trigger/migration)
-- We'll create an index on the expression to be safe.
CREATE INDEX IF NOT EXISTS idx_chat_embed ON chat (embed_uuid);
CREATE INDEX IF NOT EXISTS idx_chat_tsv_gin ON chat USING GIN (indexed_tsv);
-- trigram indexes for substring search on prompt and text
CREATE INDEX IF NOT EXISTS idx_chat_prompt_trgm ON chat USING GIN (prompt gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_chat_text_trgm ON chat USING GIN (text gin_trgm_ops);

-- Optionally, an ivfflat index for vector similarity (works with pgvector)
-- Note: ivfflat requires tuning (lists) and the embedding column must be populated.
-- Uncomment and adjust lists parameter if you want to use this index:
-- CREATE INDEX IF NOT EXISTS idx_chat_embedding_ivf ON chat USING ivfflat (embedding) WITH (lists = 100);

-- Simple jobs table for background processing
CREATE TABLE IF NOT EXISTS job_entity (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  job_type text NOT NULL,
  payload jsonb,
  status text NOT NULL DEFAULT 'PENDING',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

-- Terms / match table (optional)
CREATE TABLE IF NOT EXISTS term_entity (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  term text NOT NULL,
  doc_count bigint DEFAULT 0
);

CREATE TABLE IF NOT EXISTS match_entity (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  chat_id uuid REFERENCES chat(id) ON DELETE CASCADE,
  term_id uuid REFERENCES term_entity(id) ON DELETE CASCADE,
  score double precision,
  created_at timestamptz NOT NULL DEFAULT now()
);
