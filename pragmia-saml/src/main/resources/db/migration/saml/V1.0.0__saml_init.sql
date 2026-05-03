-- PRAGMIA SAML — Schema iniziale v1.0.0
-- Prefisso: pragmia_saml_*

CREATE TABLE IF NOT EXISTS pragmia_saml_service_providers (
    id                      VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::text,
    entity_id               VARCHAR(512)  NOT NULL UNIQUE,
    name                    VARCHAR(255)  NOT NULL,
    acs_url                 VARCHAR(1024) NOT NULL,
    slo_url                 VARCHAR(1024),
    metadata_url            VARCHAR(1024),
    signing_certificate     TEXT,
    attribute_mapping       TEXT,
    allowed_flow            VARCHAR(32)   NOT NULL DEFAULT 'both',
    enabled                 BOOLEAN       NOT NULL DEFAULT TRUE,
    require_signed_requests BOOLEAN       NOT NULL DEFAULT TRUE,
    encrypt_assertions      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS pragmia_saml_sessions (
    id             VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id        VARCHAR(255)  NOT NULL,
    sp_entity_id   VARCHAR(512)  NOT NULL,
    name_id        VARCHAR(512),
    name_id_format VARCHAR(255)  DEFAULT 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    session_index  VARCHAR(255),
    issued_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    expires_at     TIMESTAMPTZ,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    auth_type      VARCHAR(64)   DEFAULT 'local',
    client_ip      VARCHAR(64)
);

CREATE INDEX idx_saml_sessions_user_active   ON pragmia_saml_sessions (user_id, active);
CREATE INDEX idx_saml_sessions_session_index ON pragmia_saml_sessions (session_index);
CREATE INDEX idx_saml_sessions_expires_at    ON pragmia_saml_sessions (expires_at);

-- Audit append-only (no UPDATE/DELETE permessi in prod — applicare row security)
CREATE TABLE IF NOT EXISTS pragmia_saml_audit (
    id           VARCHAR(36)   PRIMARY KEY DEFAULT gen_random_uuid()::text,
    timestamp    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    event_type   VARCHAR(64)   NOT NULL,
    user_id      VARCHAR(255),
    sp_entity_id VARCHAR(512),
    idp_entity_id VARCHAR(512),
    session_index VARCHAR(255),
    client_ip    VARCHAR(64),
    result       VARCHAR(32),
    details      TEXT
);

CREATE INDEX idx_saml_audit_timestamp   ON pragmia_saml_audit (timestamp DESC);
CREATE INDEX idx_saml_audit_user_id     ON pragmia_saml_audit (user_id);
CREATE INDEX idx_saml_audit_sp_entity   ON pragmia_saml_audit (sp_entity_id);
