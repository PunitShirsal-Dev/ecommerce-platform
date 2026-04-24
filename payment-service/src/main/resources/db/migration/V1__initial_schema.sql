-- Baseline schema for payment-service.

CREATE TABLE payments (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    order_id VARCHAR(255),
    amount NUMERIC(19, 2),
    status VARCHAR(32),
    provider_reference VARCHAR(255),
    created_at TIMESTAMPTZ
);

CREATE TABLE processed_saga_events (
    event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
