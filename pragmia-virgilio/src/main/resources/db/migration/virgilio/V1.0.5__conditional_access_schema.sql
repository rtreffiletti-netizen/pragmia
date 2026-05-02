-- PRAGMIA Conditional Access — Risk-Based Authentication Schema

-- RiskFactor: fattori di rischio configurabili (geo, IP, device, velocity, time)
CREATE TABLE IF NOT EXISTS pragmia_risk_factor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(64) NOT NULL UNIQUE,      -- 'GEO', 'IP_REPUTATION', 'DEVICE_FINGERPRINT', etc.
    weight          INT NOT NULL DEFAULT 10,          -- peso nel calcolo rischio (0-100)
    enabled         BOOLEAN NOT NULL DEFAULT true,
    description     TEXT,
    config          JSONB,                            -- configurazione fattore (es. allowed_countries)
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RiskProfile: profilo rischio associabile a utente/gruppo
CREATE TABLE IF NOT EXISTS pragmia_risk_profile (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(128) NOT NULL,
    risk_threshold      INT NOT NULL DEFAULT 50,      -- score > threshold = action
    allow_action        VARCHAR(32) NOT NULL DEFAULT 'ALLOW',
    mfa_stepup_action   VARCHAR(32) NOT NULL DEFAULT 'MFA_STEPUP',
    block_action        VARCHAR(32) NOT NULL DEFAULT 'BLOCK',
    is_default          BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Riscontrazioni: mappa fattore -> profilo
CREATE TABLE IF NOT EXISTS pragmia_risk_profile_factors (
    profile_id          UUID NOT NULL REFERENCES pragmia_risk_profile(id) ON DELETE CASCADE,
    factor_id           UUID NOT NULL REFERENCES pragmia_risk_factor(id) ON DELETE CASCADE,
    weight_override     INT,                          -- NULL = usa weight default fattore
    PRIMARY KEY (profile_id, factor_id)
);

-- LoginContext: storico tentativi login per valutazione rischio
CREATE TABLE IF NOT EXISTS pragmia_login_context (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES pragmia_virgilio_users(id),
    username            VARCHAR(255) NOT NULL,
    ip_address          VARCHAR(45) NOT NULL,
    user_agent          TEXT,
    geo_country         VARCHAR(2),
    geo_city            VARCHAR(128),
    device_fingerprint  VARCHAR(255),
    login_timestamp     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    risk_score          INT,                          -- 0-100
    risk_factors        JSONB,                        -- fattori che hanno contribuito
    decision            VARCHAR(32) NOT NULL DEFAULT 'PENDING',  -- PENDING, ALLOW, MFA_STEPUP, BLOCK
    mfa_completed       BOOLEAN DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- AllowedCountry: paesi consentiti per utente
ALTER TABLE pragmia_virgilio_users
    ADD COLUMN IF NOT EXISTS allowed_countries VARCHAR(255);  -- JSON array di ISO country codes

-- Coefficient risk evaluation columns
CREATE INDEX IF NOT EXISTS idx_login_context_user ON pragmia_login_context(user_id);
CREATE INDEX IF NOT EXISTS idx_login_context_time ON pragmia_login_context(login_timestamp);
CREATE INDEX IF NOT EXISTS idx_login_context_ip ON pragmia_login_context(ip_address);
CREATE INDEX IF NOT EXISTS idx_login_context_decision ON pragmia_login_context(decision);

-- Seed fattori di rischio standard
INSERT INTO pragmia_risk_factor (name, weight, description, config)
VALUES 
    ('GEO', 25, 'Geolocation country risk', '{"allowed_countries": ["IT"], "blocked_countries": ["XX"]}'),
    ('IP_REPUTATION', 30, 'IP reputation: proxy/tor/darknet', '{"darknet_lists": [], "proxy_detection": true}'),
    ('DEVICE_FINGERPRINT', 15, 'Known device vs unknown', '{"max_devices": 5}'),
    ('VELOCITY', 20, 'Login frequency anomaly', '{"max_attempts_per_5min": 5, "max_attempts_per_hour": 20}'),
    ('TIME_OF_DAY', 10, 'Unusual login time', '{"allowed_hours_start": 6, "allowed_hours_end": 23}');

-- Seed profilo default
INSERT INTO pragmia_risk_profile (name, risk_threshold, allow_action, mfa_stepup_action, block_action, is_default)
VALUES ('Default', 50, 'ALLOW', 'MFA_STEPUP', 'BLOCK', true);

-- Associa tutti i fattori al profilo default
INSERT INTO pragmia_risk_profile_factors (profile_id, factor_id, weight_override)
SELECT (SELECT id FROM pragmia_risk_profile WHERE name = 'Default'), id, NULL
FROM pragmia_risk_factor;
