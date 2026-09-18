ALTER TABLE ouvrages ADD COLUMN actif BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX idx_ouvrages_tenant_actif ON ouvrages(tenant_id, actif);