CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    price_cents     BIGINT       NOT NULL,
    stock           INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_email  VARCHAR(200) NOT NULL,
    status          VARCHAR(30)  NOT NULL,
    total_cents     BIGINT       NOT NULL DEFAULT 0,
    enriched_at     TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT       NOT NULL,
    product_id      BIGINT       NOT NULL,
    quantity        INT          NOT NULL,
    unit_price_cents BIGINT      NOT NULL,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE app_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(200) NOT NULL,
    role            VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed: bcrypt hash for password 'admin123'
INSERT INTO app_users (username, password_hash, role) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
    ('staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STAFF');

INSERT INTO products (sku, name, price_cents, stock) VALUES
    ('SKU-001', 'Wireless Mouse',     129900, 100),
    ('SKU-002', 'Mechanical Keyboard', 599900, 50),
    ('SKU-003', '27-inch Monitor',   2999900, 25),
    ('SKU-004', 'USB-C Hub',          249900, 200),
    ('SKU-005', 'Webcam HD',          449900, 75);
