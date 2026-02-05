-- create the database if it does not already exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'chat') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE chat');
    END IF;
END;
$$ LANGUAGE plpgsql;

-- switch into the chat database (psql meta-command)
\c chat

-- create the user_account table
CREATE TABLE IF NOT EXISTS user_account (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  email VARCHAR(255),
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- insert the admin user (role ADMIN)
INSERT INTO user_account (username, password, role, created_at)
VALUES ('admin', 'admin', 'ADMIN', now())
ON CONFLICT (username) DO NOTHING;

-- create the admin_settings table
CREATE TABLE IF NOT EXISTS admin_settings (
  id SERIAL PRIMARY KEY,
  api_key BYTEA,
  base_url VARCHAR(255),
  terms TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
