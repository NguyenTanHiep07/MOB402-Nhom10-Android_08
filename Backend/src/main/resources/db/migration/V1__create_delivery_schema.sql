CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'DELIVERY', 'ADMIN')),
    license_plate VARCHAR(30),
    driver_availability VARCHAR(20) NOT NULL DEFAULT 'OFFLINE'
        CHECK (driver_availability IN ('AVAILABLE', 'BUSY', 'OFFLINE')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_requests (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    delivery_person_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    distance_km NUMERIC(10,2) NOT NULL CHECK (distance_km > 0),
    base_fee NUMERIC(14,2) NOT NULL CHECK (base_fee >= 0),
    distance_fee NUMERIC(14,2) NOT NULL CHECK (distance_fee >= 0),
    weight_fee NUMERIC(14,2) NOT NULL CHECK (weight_fee >= 0),
    fragile_charge NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (fragile_charge >= 0),
    total_cost NUMERIC(14,2) NOT NULL CHECK (total_cost >= 0),
    status VARCHAR(30) NOT NULL CHECK (status IN (
        'CHO_TIEP_NHAN', 'DA_CHAP_NHAN', 'DA_DEN_NHA_HANG', 'DA_LAY_HANG',
        'DA_DEN_KHACH_HANG', 'DA_GIAO', 'DA_HUY'
    )),
    pickup_address VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    sender_name VARCHAR(120) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    recipient_name VARCHAR(120) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    note VARCHAR(1000),
    scheduled_pickup_time TIMESTAMPTZ,
    actual_delivery_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_requests_status ON delivery_requests(status);
CREATE INDEX idx_delivery_requests_client ON delivery_requests(client_id);
CREATE INDEX idx_delivery_requests_driver ON delivery_requests(delivery_person_id);

CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL REFERENCES delivery_requests(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    package_type VARCHAR(50),
    weight_kg NUMERIC(10,2) NOT NULL CHECK (weight_kg > 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    notes VARCHAR(500),
    is_fragile BOOLEAN NOT NULL DEFAULT FALSE,
    is_express BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_packages_request ON packages(delivery_request_id);

CREATE TABLE status_histories (
    id BIGSERIAL PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL REFERENCES delivery_requests(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(500)
);
CREATE INDEX idx_status_histories_request ON status_histories(delivery_request_id, timestamp, id);

CREATE TABLE rejection_reasons (
    code VARCHAR(50) PRIMARY KEY,
    label VARCHAR(200) NOT NULL,
    is_valid BOOLEAN NOT NULL,
    penalty_points INTEGER NOT NULL DEFAULT 0 CHECK (penalty_points >= 0),
    requires_note BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE order_rejections (
    id BIGSERIAL PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL REFERENCES delivery_requests(id) ON DELETE CASCADE,
    driver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason_code VARCHAR(50) NOT NULL REFERENCES rejection_reasons(code) ON DELETE RESTRICT,
    note VARCHAR(500),
    penalty_applied BOOLEAN NOT NULL DEFAULT FALSE,
    rejected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rejection_request_driver UNIQUE (delivery_request_id, driver_id)
);
CREATE INDEX idx_order_rejections_driver_time ON order_rejections(driver_id, rejected_at);

CREATE TABLE driver_statistics (
    driver_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_accepted INTEGER NOT NULL DEFAULT 0 CHECK (total_accepted >= 0),
    total_rejected INTEGER NOT NULL DEFAULT 0 CHECK (total_rejected >= 0),
    penalized_rejections INTEGER NOT NULL DEFAULT 0 CHECK (penalized_rejections >= 0),
    reliability_score NUMERIC(5,2) NOT NULL DEFAULT 100 CHECK (reliability_score BETWEEN 0 AND 100),
    locked_until TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
