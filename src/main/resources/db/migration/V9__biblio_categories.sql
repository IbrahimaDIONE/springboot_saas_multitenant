CREATE TABLE categories (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(255) NOT NULL,
                            nom VARCHAR(100) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT now(),
                            updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_categories_tenant_id ON categories(tenant_id);