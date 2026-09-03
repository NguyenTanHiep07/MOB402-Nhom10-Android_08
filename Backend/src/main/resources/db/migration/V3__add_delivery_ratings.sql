CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL REFERENCES delivery_requests(id) ON DELETE CASCADE,
    client_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    driver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ratings_delivery_request UNIQUE (delivery_request_id)
);

CREATE INDEX idx_ratings_client_created_at ON ratings(client_id, created_at DESC);
CREATE INDEX idx_ratings_driver_created_at ON ratings(driver_id, created_at DESC);
