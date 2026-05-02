-- PRAGMIA OIDC — Authorized Client Schema
CREATE TABLE IF NOT EXISTS authorized_client (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id                 VARCHAR(255) NOT NULL UNIQUE,
    client_secret             VARCHAR(512),
    client_name               VARCHAR(255) NOT NULL,
    authorization_grant_type  VARCHAR(255) NOT NULL,
    redirect_uri              VARCHAR(512),
    scope                     VARCHAR(512),
    post_logout_redirect_uri  VARCHAR(512),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed client admin OIDC
INSERT INTO authorized_client (client_id, client_secret, client_name, authorization_grant_type, redirect_uri, scope, post_logout_redirect_uri)
SELECT 'pragmia-admin', '{bcrypt}$2a$10$J1FHX.kgmMa5frY.ZyzVrexAH6i7soWSfa0Oij/F4trdsXnVqmRnO',
    'PRAGMIA Admin Client', 'authorization_code',
    'http://localhost:8080/authorized,http://localhost:8080/login/oauth2/code/pragmia-admin',
    'openid,profile,email',
    'http://localhost:8080'
WHERE NOT EXISTS (SELECT 1 FROM authorized_client WHERE client_id = 'pragmia-admin');

CREATE INDEX IF NOT EXISTS idx_auth_client_id ON authorized_client(client_id);
