-- Baseline schema for customer-service.

CREATE TABLE customers (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    email VARCHAR(255),
    full_name VARCHAR(255),
    preferred_currency VARCHAR(255)
);
