-- Baseline schema for inventory-service.

CREATE TABLE stock_items (
    product_id VARCHAR(255) NOT NULL PRIMARY KEY,
    quantity_on_hand INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255),
    product_id VARCHAR(255),
    quantity INTEGER NOT NULL
);

CREATE TABLE processed_saga_events (
    event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_reservations_order_id ON reservations (order_id);
