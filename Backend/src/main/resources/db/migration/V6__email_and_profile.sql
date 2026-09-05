ALTER TABLE users ADD COLUMN recovery_email VARCHAR(254);
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN avatar_base64 TEXT;
CREATE UNIQUE INDEX users_recovery_email_unique ON users(lower(recovery_email)) WHERE recovery_email IS NOT NULL;
CREATE TABLE account_challenges (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    purpose VARCHAR(8) NOT NULL,
    target_email VARCHAR(254) NOT NULL,
    phone_snapshot VARCHAR(20) NOT NULL,
    credential_version BIGINT NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    mail_payload TEXT,
    mail_status VARCHAR(8) NOT NULL DEFAULT 'QUEUED',
    attempts INTEGER NOT NULL DEFAULT 0,
    consumed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_challenge_user ON account_challenges(user_id, purpose, created_at DESC);
CREATE INDEX account_challenge_queue ON account_challenges(created_at) WHERE mail_status='QUEUED';
