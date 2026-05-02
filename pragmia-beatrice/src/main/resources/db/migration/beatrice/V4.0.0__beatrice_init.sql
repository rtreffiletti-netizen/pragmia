
CREATE TABLE IF NOT EXISTS beatrice_nlp_commands (
    id               VARCHAR(36)  PRIMARY KEY,
    prompt           TEXT         NOT NULL,
    resolved_action  TEXT,
    status           VARCHAR(32)  NOT NULL,
    requested_by     VARCHAR(255),
    approved_by      VARCHAR(255),
    rejected_by      VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    decided_at       TIMESTAMPTZ,
    notes            VARCHAR(512)
);

CREATE INDEX IF NOT EXISTS idx_beatrice_status       ON beatrice_nlp_commands (status);
CREATE INDEX IF NOT EXISTS idx_beatrice_requested_by ON beatrice_nlp_commands (requested_by);
