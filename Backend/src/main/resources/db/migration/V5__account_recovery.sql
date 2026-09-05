ALTER TABLE users ADD COLUMN credential_version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE password_recovery_limits (
    key VARCHAR(80) PRIMARY KEY,
    window_start TIMESTAMPTZ NOT NULL,
    last_request TIMESTAMPTZ NOT NULL,
    count INTEGER NOT NULL
);
