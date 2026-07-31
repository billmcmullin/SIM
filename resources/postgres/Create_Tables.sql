-- create the user_account table
CREATE TABLE IF NOT EXISTS user_account (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  email VARCHAR(255),
  full_name VARCHAR(255),
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Ensure older databases created before full_name was introduced are aligned.
ALTER TABLE IF EXISTS user_account
  ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

-- create Admin User
CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO user_account (username, password, role, created_at)
VALUES ('admin', crypt('admin', gen_salt('bf')), 'ADMIN', now())
ON CONFLICT (username) DO NOTHING;
-- create widget tables
CREATE TABLE IF NOT EXISTS widget_entries (
    id SERIAL PRIMARY KEY,
    widget_id VARCHAR(128) NOT NULL UNIQUE,
    display_name VARCHAR(256) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- setup EMAIL SMTP table --
CREATE TABLE IF NOT EXISTS email_smtp_config (
  id INT PRIMARY KEY,
  host VARCHAR(255) NOT NULL,
  port INT NOT NULL,
  auth BOOLEAN NOT NULL,
  starttls BOOLEAN NOT NULL,
  ssl BOOLEAN NOT NULL,
  username VARCHAR(255),
  password_enc TEXT,
  default_from VARCHAR(255),
  updated_by VARCHAR(100),
  updated_at TIMESTAMP NOT NULL
);
-- setup Healthcheck --
CREATE TABLE IF NOT EXISTS widget_health_config (
  id INT PRIMARY KEY,
  healthcheck_url TEXT NOT NULL,
  healthcheck_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  check_interval_seconds INT NOT NULL DEFAULT 300,
  method VARCHAR(10) NOT NULL DEFAULT 'GET',
  timeout_ms INT NOT NULL DEFAULT 8000,
  expect_json_field VARCHAR(100),
  expect_json_value VARCHAR(255),
  widget_id VARCHAR(255),
  request_origin TEXT,
  request_referer TEXT,
  request_user_agent TEXT,
  request_cookie TEXT,
  api_key_header_name VARCHAR(255),
  api_key_value TEXT,
  updated_by VARCHAR(100),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
