-- Casbin policy storage (PostgreSQL). Used when casbytes.casbin.policy-store=jdbc.
CREATE TABLE IF NOT EXISTS casbin_rule (
    id BIGSERIAL PRIMARY KEY,
    ptype VARCHAR(100) NOT NULL,
    v0 VARCHAR(256),
    v1 VARCHAR(256),
    v2 VARCHAR(256),
    v3 VARCHAR(256),
    v4 VARCHAR(256),
    v5 VARCHAR(256)
);

CREATE INDEX IF NOT EXISTS idx_casbin_rule_ptype ON casbin_rule (ptype);

-- Bootstrap policies (idempotent inserts)
INSERT INTO casbin_rule (ptype, v0, v1, v2)
SELECT 'p', 'role:admin', '*', '*'
WHERE NOT EXISTS (
    SELECT 1 FROM casbin_rule WHERE ptype = 'p' AND v0 = 'role:admin' AND v1 = '*' AND v2 = '*'
);

INSERT INTO casbin_rule (ptype, v0, v1, v2)
SELECT 'p', 'role:user', '/api/v1/reference/items', 'GET'
WHERE NOT EXISTS (
    SELECT 1 FROM casbin_rule
    WHERE ptype = 'p' AND v0 = 'role:user' AND v1 = '/api/v1/reference/items' AND v2 = 'GET'
);

INSERT INTO casbin_rule (ptype, v0, v1)
SELECT 'g', 'alice', 'role:admin'
WHERE NOT EXISTS (SELECT 1 FROM casbin_rule WHERE ptype = 'g' AND v0 = 'alice' AND v1 = 'role:admin');

INSERT INTO casbin_rule (ptype, v0, v1)
SELECT 'g', 'bob', 'role:user'
WHERE NOT EXISTS (SELECT 1 FROM casbin_rule WHERE ptype = 'g' AND v0 = 'bob' AND v1 = 'role:user');
