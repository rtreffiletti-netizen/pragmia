-- PRAGMIA :: VIRGILIO — Utente admin di default
-- Password: Admin@Pragmia1! (BCrypt $2a$12$...)
INSERT INTO pragmia_virgilio_users (username, email, password_hash, full_name, enabled)
VALUES ('admin', 'admin@pragmia.local',
        '$2a$12$1GbqFkiZ7VFdZl3fAT9DLeF6XDMY2S2cBt8JyL3u9Y.SvQS0cEi2.',
        'PRAGMIA Administrator', true);

INSERT INTO pragmia_virgilio_user_roles (user_id, role_id)
SELECT u.id, r.id FROM pragmia_virgilio_users u, pragmia_virgilio_roles r
WHERE u.username = 'admin' AND r.name = 'PRAGMIA_ADMIN';
