CREATE TABLE IF NOT EXISTS test_table (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);
