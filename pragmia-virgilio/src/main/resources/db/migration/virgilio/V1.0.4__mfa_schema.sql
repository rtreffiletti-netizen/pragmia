-- PRAGMIA MFA — Totp + WebAuthn/FIDO2 Schema
-- TotpCredential: TOTP standard OATH RFC 6238
CREATE TABLE IF NOT EXISTS pragmia_totp_credential (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES pragmia_virgilio_users(id) ON DELETE CASCADE,
    secret          VARCHAR(255) NOT NULL,
    algorithm       VARCHAR(16) NOT NULL DEFAULT 'SHA1',      -- SHA1, SHA256, SHA512
    digits          INT NOT NULL DEFAULT 6,                   -- 6 o 8
    window          INT NOT NULL DEFAULT 1,                   -- tolleranza periodi
    period          INT NOT NULL DEFAULT 30,                  -- secondi
    enabled         BOOLEAN NOT NULL DEFAULT true,
    last_verified_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_totp_user ON pragmia_totp_credential(user_id);

-- WebAuthnCredential: credenziali FIDO2 (security key, autenticatore built-in, passkey)
CREATE TABLE IF NOT EXISTS pragmia_webauthn_credential (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID REFERENCES pragmia_virgilio_users(id) ON DELETE CASCADE,
    credential_id         BYTEA NOT NULL,
    public_key            BYTEA NOT NULL,
    counter               BIGINT NOT NULL DEFAULT 0,
    attestation_object    BYTEA,
    user_label            VARCHAR(255),
    transport             VARCHAR(255),        -- JSON array of transports
    aa_guid               UUID,                -- AAGUID identificativo produttore
    authenticator_type    VARCHAR(32) DEFAULT 'platform',
    verify_instant        TIMESTAMPTZ,
    backup_eligible       BOOLEAN DEFAULT false,
    backup_state          BOOLEAN DEFAULT false,
    userless              BOOLEAN NOT NULL DEFAULT false,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at          TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_webauthn_cred ON pragmia_webauthn_credential(credential_id);
CREATE INDEX IF NOT EXISTS idx_webauthn_user ON pragmia_webauthn_credential(user_id);
CREATE INDEX IF NOT EXISTS idx_webauthn_aaguid ON pragmia_webauthn_credential(aa_guid);

-- Colonna userless in Users per opt-in userless login
ALTER TABLE pragmia_virgilio_users
    ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE pragmia_virgilio_users
    ADD COLUMN IF NOT EXISTS mfa_method VARCHAR(32);  -- 'TOTP', 'WEBAUTHN', 'BOTH'
