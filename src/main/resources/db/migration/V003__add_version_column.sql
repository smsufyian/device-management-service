-- Add optimistic locking version column
ALTER TABLE devices
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Ensure new rows start from 0 and incremented by JPA @Version