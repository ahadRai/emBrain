-- Phase 2 migration — runs after 01-init.sql
-- Uses IF NOT EXISTS / IF EXISTS guards so it is safe to run multiple times

-- ── auth schema additions ─────────────────────────────────────────────────────

ALTER TABLE auth.users
  ADD COLUMN IF NOT EXISTS is_verified  BOOLEAN      NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS updated_at   TIMESTAMPTZ           DEFAULT now();

CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  token_hash  TEXT        NOT NULL UNIQUE,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN     NOT NULL DEFAULT false,
  created_at  TIMESTAMPTZ          DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);

-- ── users schema additions ────────────────────────────────────────────────────

ALTER TABLE users.profiles
  ADD COLUMN IF NOT EXISTS bio        TEXT,
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

CREATE TABLE IF NOT EXISTS users.enrolments (
  id          UUID  PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID  NOT NULL,
  subject     TEXT  NOT NULL,
  enrolled_at TIMESTAMPTZ DEFAULT now(),
  CONSTRAINT uq_enrolment UNIQUE (user_id, subject)
);

CREATE INDEX IF NOT EXISTS idx_enrolments_user_id ON users.enrolments(user_id);
