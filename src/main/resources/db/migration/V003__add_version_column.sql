-- Add optimistic locking version column
ALTER TABLE devices
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;