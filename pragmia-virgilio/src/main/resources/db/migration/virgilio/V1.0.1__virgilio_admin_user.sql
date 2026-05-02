-- PRAGMIA :: VIRGILIO — Utente admin di default
-- Password: Admin@Pragmia1!
INSERT INTO pragmia_virgilio_users (username, email, password_hash, full_name, enabled)
VALUES ('admin', 'admin@pragmia.local',
        '$2a$12$J1FHX.kgmMa5frY.ZyzVrexAH6i7soWSfa0Oij/F4trdsXnVqmRnO',
        'PRAGMIA Administrator', true)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash;

INSERT INTO pragmia_virgilio_user_roles (user_id, role_id)
SELECT u.id, r.id FROM pragmia_virgilio_users u, pragmia_virgilio_roles r
WHERE u.username = 'admin' AND r.name = 'PRAGMIA_ADMIN'
ON CONFLICT DO NOTHING;
