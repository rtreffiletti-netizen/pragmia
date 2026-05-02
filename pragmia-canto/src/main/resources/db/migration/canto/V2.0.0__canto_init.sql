
CREATE TABLE IF NOT EXISTS canto_flow_trees (
    id               VARCHAR(36)  PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    description      VARCHAR(512),
    definition_json  TEXT         NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT FALSE,
    version          INTEGER      NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_canto_flow_trees_active ON canto_flow_trees (active);
