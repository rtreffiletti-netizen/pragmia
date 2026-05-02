
CREATE TABLE IF NOT EXISTS minos_policies (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(512),
    condition   TEXT         NOT NULL,
    effect      VARCHAR(16)  NOT NULL,
    priority    INTEGER      NOT NULL DEFAULT 100,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255)
);

-- Policy di default: blocca tutto dal localhost tranne admin
INSERT INTO minos_policies (id, name, description, condition, effect, priority, active, created_by)
VALUES (
    gen_random_uuid()::text,
    'allow-admin-scope',
    'Permette qualsiasi azione agli utenti con scope admin',
    '#claims[''scope''] != null && #claims[''scope''].contains(''admin'')',
    'PERMIT',
    1,
    true,
    'system'
) ON CONFLICT (name) DO NOTHING;
