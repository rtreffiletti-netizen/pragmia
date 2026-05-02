-- PRAGMIA :: VIRGILIO — Schema iniziale
CREATE TABLE pragmia_virgilio_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pragmia_virgilio_users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username       VARCHAR(255) NOT NULL UNIQUE,
    email          VARCHAR(255) UNIQUE,
    password_hash  VARCHAR(255),
    full_name      VARCHAR(255),
    totp_secret    VARCHAR(255),
    totp_enabled   BOOLEAN NOT NULL DEFAULT FALSE,
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    locked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at  TIMESTAMPTZ,
    login_attempts INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE pragmia_virgilio_user_roles (
    user_id UUID NOT NULL REFERENCES pragmia_virgilio_users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES pragmia_virgilio_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_vg_username ON pragmia_virgilio_users(username);
CREATE INDEX idx_vg_email    ON pragmia_virgilio_users(email);

INSERT INTO pragmia_virgilio_roles (name, description) VALUES
    ('PRAGMIA_ADMIN',    'Amministratore — accesso completo'),
    ('PRAGMIA_OPERATOR', 'Operatore — gestione utenti e sessioni'),
    ('PRAGMIA_VIEWER',   'Sola lettura — monitoring e audit'),
    ('PRAGMIA_APPROVER', 'Approvatore dual-approval per BEATRICE');
