-- create the user_account table
CREATE TABLE IF NOT EXISTS user_account (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  email VARCHAR(255),
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO user_account (username, password, role, created_at)
VALUES ('admin', crypt('admin', gen_salt('bf')), 'ADMIN', now())
ON CONFLICT (username) DO NOTHING;
