-- ERP-oriented profile fields for API users (login payload + future HR screens).
-- One ALTER per column for H2 (tests) and PostgreSQL compatibility.
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(128);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(128);
ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(32);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(64);
ALTER TABLE users ADD COLUMN IF NOT EXISTS mobile VARCHAR(64);
ALTER TABLE users ADD COLUMN IF NOT EXISTS job_title VARCHAR(128);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(128);
ALTER TABLE users ADD COLUMN IF NOT EXISTS locale VARCHAR(32);
ALTER TABLE users ADD COLUMN IF NOT EXISTS time_zone VARCHAR(64);

UPDATE users
SET
    first_name = COALESCE(first_name, 'Platform'),
    last_name = COALESCE(last_name, 'Administrator'),
    display_name = COALESCE(display_name, 'Platform Administrator'),
    gender = COALESCE(gender, 'UNSPECIFIED'),
    locale = COALESCE(locale, 'en'),
    time_zone = COALESCE(time_zone, 'UTC')
WHERE LOWER(email) = LOWER('admin@casbytes.com');
