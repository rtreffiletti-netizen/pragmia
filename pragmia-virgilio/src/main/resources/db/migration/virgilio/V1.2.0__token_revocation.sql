
-- PRAGMIA VIRGILIO — Token Revocation & Rotation v1.2.0
-- Aggiunge colonne mancanti per revoca e rotation dei token

-- Colonna revokedAt su access token (se non esiste già)
ALTER TABLE oauth_access_tokens
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMPTZ;

-- Colonne mancanti su refresh token
ALTER TABLE oauth_refresh_tokens
    ADD COLUMN IF NOT EXISTS access_token_id UUID,
    ADD COLUMN IF NOT EXISTS revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS revoked_at      TIMESTAMPTZ;

-- Indici per cleanup e lookup veloci
CREATE INDEX IF NOT EXISTS idx_at_revoked_expires ON oauth_access_tokens (revoked, expires_at);
CREATE INDEX IF NOT EXISTS idx_rt_revoked_expires ON oauth_refresh_tokens (revoked, expires_at);
CREATE INDEX IF NOT EXISTS idx_rt_client_id       ON oauth_refresh_tokens (client_id);
