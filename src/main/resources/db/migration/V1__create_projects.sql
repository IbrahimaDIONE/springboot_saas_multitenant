CREATE TABLE projects (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_projects_tenant ON projects (tenant_id);
