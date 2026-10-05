CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

INSERT INTO roles (id, code, description, actif, created_at, updated_at) VALUES
('51000000-0000-0000-0000-000000000001', 'ADMIN_PLATEFORME', 'Administration globale de la plateforme', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('51000000-0000-0000-0000-000000000002', 'ADMIN_ETABLISSEMENT', 'Administration d''un établissement', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('51000000-0000-0000-0000-000000000003', 'ETUDIANT', 'Accès étudiant', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('51000000-0000-0000-0000-000000000004', 'CLIENT', 'Rôle historique de compatibilité', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE app_users ADD COLUMN role_id UUID;
UPDATE app_users u
SET role_id = r.id
FROM roles r
WHERE r.code = u.role;

ALTER TABLE app_users ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE app_users ADD CONSTRAINT fk_app_users_role FOREIGN KEY (role_id) REFERENCES roles(id);
CREATE INDEX idx_app_users_role ON app_users(role_id);
ALTER TABLE app_users DROP COLUMN role;
