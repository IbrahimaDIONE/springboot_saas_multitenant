ALTER TABLE notifications ADD COLUMN group_id UUID;
UPDATE notifications SET group_id = id;
ALTER TABLE notifications ALTER COLUMN group_id SET NOT NULL;
CREATE INDEX idx_notifications_group ON notifications (tenant_id, group_id);

ALTER TABLE emprunts ALTER COLUMN etudiant_id DROP NOT NULL;
ALTER TABLE penalites ALTER COLUMN etudiant_id DROP NOT NULL;
