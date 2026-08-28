CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, name)
);
CREATE INDEX idx_categories_tenant ON categories(tenant_id);

INSERT INTO categories(id, tenant_id, name, created_at, updated_at) VALUES
('30000000-0000-0000-0000-000000000001','tenant-a','Informatique',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('30000000-0000-0000-0000-000000000002','tenant-b','Bureautique',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('30000000-0000-0000-0000-000000000003','tenant-c','Audio',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

ALTER TABLE products ADD COLUMN image_url VARCHAR(2048);
ALTER TABLE products ADD COLUMN category_id UUID;
UPDATE products SET image_url='https://placehold.co/600x400?text=Produit', category_id=CASE tenant_id
 WHEN 'tenant-a' THEN '30000000-0000-0000-0000-000000000001'::uuid
 WHEN 'tenant-b' THEN '30000000-0000-0000-0000-000000000002'::uuid
 ELSE '30000000-0000-0000-0000-000000000003'::uuid END;
ALTER TABLE products ALTER COLUMN image_url SET NOT NULL;
ALTER TABLE products ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT fk_product_category FOREIGN KEY(category_id) REFERENCES categories(id);
