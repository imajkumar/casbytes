-- All application users (API login).
-- Root admin (API): admin@casbytes.com / Admin@bytes — bcrypt matches Spring delegating PasswordEncoder ({bcrypt}...).
-- If you previously applied older CasBytes migrations (platform_users / V4–V5), reset this DB or repair Flyway before applying this script set.
CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

INSERT INTO users (id, email, password_hash, role, enabled, created_at, updated_at)
SELECT
    'a0000000-0000-4000-8000-000000000001',
    'admin@casbytes.com',
    '{bcrypt}$2b$10$DwaqVslAr56fJ77gTjFshO6U5mfPeSdczmM9eSz5waV07eSxtVdyW',
    'PLATFORM_OWNER',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(email) = LOWER('admin@casbytes.com'));
