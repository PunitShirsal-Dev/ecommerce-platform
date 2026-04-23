-- Baseline schema for order-service (managed by Flyway; Hibernate ddl-auto=validate in production).

CREATE TABLE orders (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19, 2),
    created_at TIMESTAMPTZ
);

CREATE TABLE order_lines (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2),
    order_id VARCHAR(255) NOT NULL REFERENCES orders (id) ON DELETE CASCADE
);

CREATE TABLE processed_saga_events (
    event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
