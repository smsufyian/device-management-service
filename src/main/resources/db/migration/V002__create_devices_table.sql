CREATE TABLE IF NOT EXISTS devices (                                    device_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL,
    brand          VARCHAR(50) NOT NULL,
    state          VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()

CONSTRAINT chk_state CHECK (state IN ('AVAILABLE', 'IN_USE', 'INACTIVE'))

);

CREATE INDEX idx_devices_brand ON devices (brand);

CREATE INDEX idx_devices_state ON devices (state);