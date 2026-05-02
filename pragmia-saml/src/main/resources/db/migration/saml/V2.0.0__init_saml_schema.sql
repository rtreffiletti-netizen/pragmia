-- PRAGMIA SAML Module: Initial Schema

CREATE TABLE IF NOT EXISTS pragmia_saml_idp (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    entity_id       VARCHAR(512) NOT NULL UNIQUE,
    sso_url         VARCHAR(512) NOT NULL,
    slo_url         VARCHAR(512),
    certificate     TEXT,
    private_key     TEXT,
    attribute_mapping TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pragmia_saml_sp (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    entity_id           VARCHAR(512) NOT NULL UNIQUE,
    acs_url             VARCHAR(512),
    logout_url          VARCHAR(512),
    metadata            TEXT,
    attribute_mapping   TEXT,
    jit_provisioning    BOOLEAN NOT NULL DEFAULT TRUE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saml_idp_entity_id ON pragmia_saml_idp(entity_id);
CREATE INDEX IF NOT EXISTS idx_saml_sp_entity_id ON pragmia_saml_sp(entity_id);
CREATE INDEX IF NOT EXISTS idx_saml_idp_active ON pragmia_saml_idp(active) WHERE active = TRUE;
CREATE INDEX IF NOT EXISTS idx_saml_sp_active ON pragmia_saml_sp(active) WHERE active = TRUE;
