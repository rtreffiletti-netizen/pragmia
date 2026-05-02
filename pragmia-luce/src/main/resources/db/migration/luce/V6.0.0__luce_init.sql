
CREATE TABLE IF NOT EXISTS luce_compliance_controls (
    id          VARCHAR(64)  PRIMARY KEY,
    framework   VARCHAR(16)  NOT NULL,
    article_ref VARCHAR(64)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    check_bean  VARCHAR(128) NOT NULL,
    automated   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS luce_compliance_reports (
    id             VARCHAR(36)  PRIMARY KEY,
    framework      VARCHAR(16)  NOT NULL,
    generated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    generated_by   VARCHAR(255),
    total_controls INTEGER      NOT NULL DEFAULT 0,
    passed_controls INTEGER     NOT NULL DEFAULT 0,
    failed_controls INTEGER     NOT NULL DEFAULT 0,
    not_applicable  INTEGER     NOT NULL DEFAULT 0,
    results        JSONB
);

CREATE INDEX IF NOT EXISTS idx_luce_reports_fw  ON luce_compliance_reports (framework);
CREATE INDEX IF NOT EXISTS idx_luce_reports_gen ON luce_compliance_reports (generated_at DESC);

-- Seed controlli
INSERT INTO luce_compliance_controls VALUES
  ('NIS2-ART21-MFA',    'NIS2', 'Art.21(2)(a)', 'Autenticazione a più fattori', 'MFA obbligatorio per tutti gli utenti', 'MFA_ENABLED_CHECK', true),
  ('NIS2-ART21-AUDIT',  'NIS2', 'Art.21(2)(j)', 'Audit log e monitoraggio', 'Retention log >= 12 mesi', 'AUDIT_RETENTION_CHECK', true),
  ('DORA-ART9-SESSION', 'DORA', 'Art.9(4)(d)',  'Timeout sessione', 'Idle session max 30 min', 'SESSION_TIMEOUT_CHECK', true),
  ('AGID-PWD-POLICY',   'AGID', 'LLGG-2020',    'Password policy', 'Complessità e lunghezza minima password', 'PASSWORD_POLICY_CHECK', false)
ON CONFLICT (id) DO NOTHING;
