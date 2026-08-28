-- Trois comptes de démonstration. Le mot de passe commun est "password".
-- Il est stocké avec BCrypt et le préfixe compris par DelegatingPasswordEncoder.
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_app_users_tenant ON app_users (tenant_id);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_products_tenant ON products (tenant_id);

-- Hash BCrypt officiel d'exemple Spring Security pour le mot de passe "password".
INSERT INTO app_users (id, username, password_hash, tenant_id, role, enabled) VALUES
('10000000-0000-0000-0000-000000000001', 'client-a', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'tenant-a', 'CLIENT', TRUE),
('10000000-0000-0000-0000-000000000002', 'client-b', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'tenant-b', 'CLIENT', TRUE),
('10000000-0000-0000-0000-000000000003', 'client-c', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'tenant-c', 'CLIENT', TRUE);

INSERT INTO products (id, tenant_id, name, price, stock, created_at, updated_at) VALUES
('20000000-0000-0000-0000-000000000001', 'tenant-a', 'Clavier Acme', 79.90, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000002', 'tenant-a', 'Souris Acme', 39.90, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000003', 'tenant-b', 'Écran Globex', 249.00, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000004', 'tenant-b', 'Webcam Globex', 89.00, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000005', 'tenant-c', 'Casque Initech', 119.50, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000006', 'tenant-c', 'Micro Initech', 149.90, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
