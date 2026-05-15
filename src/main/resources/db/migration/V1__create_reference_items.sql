-- CasBytes Core: baseline reference module table (PostgreSQL-compatible DDL)
CREATE TABLE reference_items (
    id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_reference_items PRIMARY KEY (id),
    CONSTRAINT uq_reference_items_code UNIQUE (code)
);

CREATE INDEX idx_reference_items_active ON reference_items (active);
