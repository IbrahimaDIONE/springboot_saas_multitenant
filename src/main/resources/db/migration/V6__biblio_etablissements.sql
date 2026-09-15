CREATE TABLE etablissements (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(150) NOT NULL,
    statut VARCHAR(20) NOT NULL,
    parametres JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_etablissements_statut ON etablissements(statut);

INSERT INTO etablissements (id, code, nom, statut, created_at, updated_at)
VALUES
    ('41000000-0000-0000-0000-000000000001', 'tenant-a', 'Etablissement A', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('41000000-0000-0000-0000-000000000002', 'tenant-b', 'Etablissement B', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('41000000-0000-0000-0000-000000000003', 'tenant-c', 'Etablissement C', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);