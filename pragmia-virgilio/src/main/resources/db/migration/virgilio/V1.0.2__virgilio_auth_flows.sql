-- PRAGMIA :: VIRGILIO — Auth flows table e flow di default
CREATE TABLE IF NOT EXISTS pragmia_virgilio_auth_flows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    flow_definition TEXT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO pragmia_virgilio_auth_flows (name, flow_definition, is_active)
VALUES (
    'Default Login Flow',
    '{"id": "default-login-flow", "name": "Default Login Flow", "version": "1.0", "startNodeId": "start", "nodes": [{"id": "start", "type": "USERNAME_PASSWORD", "name": "Login", "config": {}, "transitions": {"SUCCESS": "allow", "FAILURE": "deny"}}, {"id": "allow", "type": "ALLOW", "name": "Accesso consentito", "config": {}, "transitions": {}}, {"id": "deny", "type": "DENY", "name": "Accesso negato", "config": {"reason": "Credenziali non valide"}, "transitions": {}}]}',
    true
) ON CONFLICT DO NOTHING;
