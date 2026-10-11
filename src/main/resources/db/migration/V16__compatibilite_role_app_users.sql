ALTER TABLE app_users ADD COLUMN role VARCHAR(30);

UPDATE app_users u
SET role = r.code
FROM roles r
WHERE r.id = u.role_id;

ALTER TABLE app_users ALTER COLUMN role SET NOT NULL;
ALTER TABLE app_users ALTER COLUMN role_id DROP NOT NULL;