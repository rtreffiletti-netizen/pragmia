
-- PRAGMIA VIRGILIO — Password Management v1.3.0

CREATE TABLE IF NOT EXISTS pragmia_virgilio_password_policy (
    id                    VARCHAR(16)  PRIMARY KEY DEFAULT 'default',
    min_length            INT          NOT NULL DEFAULT 12,
    max_length            INT          NOT NULL DEFAULT 128,
    require_uppercase     BOOLEAN      NOT NULL DEFAULT TRUE,
    require_lowercase     BOOLEAN      NOT NULL DEFAULT TRUE,
    require_digit         BOOLEAN      NOT NULL DEFAULT TRUE,
    require_special       BOOLEAN      NOT NULL DEFAULT TRUE,
    history_count         INT          NOT NULL DEFAULT 5,
    expiry_days           INT          NOT NULL DEFAULT 90,
    expiry_warning_days   INT          NOT NULL DEFAULT 14,
    reset_token_ttl_minutes INT        NOT NULL DEFAULT 30,
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Inserisce la policy di default se non esiste
INSERT INTO pragmia_virgilio_password_policy (id) VALUES ('default')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS pragmia_virgilio_password_history (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    password_hash TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pwh_user_id ON pragmia_virgilio_password_history (user_id);
CREATE INDEX IF NOT EXISTS idx_pwh_created ON pragmia_virgilio_password_history (created_at DESC);

CREATE TABLE IF NOT EXISTS pragmia_virgilio_password_reset_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMPTZ,
    request_ip  VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_prt_token     ON pragmia_virgilio_password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_prt_user_id   ON pragmia_virgilio_password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_prt_expires   ON pragmia_virgilio_password_reset_tokens (expires_at);
