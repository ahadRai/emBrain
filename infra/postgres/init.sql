-- Auth schema
CREATE SCHEMA IF NOT EXISTS auth;

-- User schema
CREATE SCHEMA IF NOT EXISTS users;

-- Placeholder tables (fully built in Phase 2)
CREATE TABLE IF NOT EXISTS auth.users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'student',
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id),
  name TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);
