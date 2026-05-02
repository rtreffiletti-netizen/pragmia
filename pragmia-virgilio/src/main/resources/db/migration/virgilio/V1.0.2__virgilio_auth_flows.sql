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
    '{"id": "default-login-flow", "name": "Default Login Flow", "version": "1.0", "startNodeId": "start", "nodes": [{"id": "start_node", "type": "START", "data": {}}, {"id": "auth_node", "type": "USERNAME_PASSWORD", "data": {}}, {"id": "allow_node", "type": "ALLOW", "data": {}}, {"id": "deny_node", "type": "DENY", "data": {}}], "edges": [{"source": "start_node", "sourceHandle": "", "target": "auth_node"}, {"source": "auth_node", "sourceHandle": "success", "target": "allow_node"}, {"source": "auth_node", "sourceHandle": "failure", "target": "deny_node"}]}',
    true
) ON CONFLICT DO NOTHING;
