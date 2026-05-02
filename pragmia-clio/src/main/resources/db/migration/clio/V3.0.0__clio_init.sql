
CREATE TABLE IF NOT EXISTS clio_audit_log (
    event_id       VARCHAR(36)  PRIMARY KEY,
    event_type     VARCHAR(64)  NOT NULL,
    ts             TIMESTAMPTZ  NOT NULL,
    user_id        VARCHAR(255),
    admin_id       VARCHAR(255),
    session_id     VARCHAR(255),
    client_id      VARCHAR(255),
    remote_ip      VARCHAR(64),
    resource       VARCHAR(255),
    action         VARCHAR(64),
    result         VARCHAR(32),
    failure_reason VARCHAR(512),
    metadata       JSONB
);

-- Immutabilità: nessun UPDATE o DELETE permesso a livello applicativo
-- (enforced via updatable=false in JPA e policy di DB role in produzione)
