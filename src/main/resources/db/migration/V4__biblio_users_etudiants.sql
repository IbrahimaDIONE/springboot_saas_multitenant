ALTER TABLE app_users ADD COLUMN nom VARCHAR(100);
ALTER TABLE app_users ADD COLUMN prenom VARCHAR(100);
ALTER TABLE app_users ADD COLUMN email VARCHAR(150);

CREATE TABLE etudiants (
                           id UUID PRIMARY KEY,
                           utilisateur_id UUID NOT NULL UNIQUE REFERENCES app_users(id) ON DELETE CASCADE,
                           filiere_id UUID,
                           niveau_id UUID
);

-- Mise à jour des comptes de test existants avec les nouveaux rôles BiblioUniv
UPDATE app_users SET role = 'ADMIN_PLATEFORME', nom = 'Admin', prenom = 'Plateforme', email = 'admin@bibliouniv.sn' WHERE username = 'client-a';
UPDATE app_users SET role = 'ADMIN_ETABLISSEMENT', nom = 'Diop', prenom = 'Awa', email = 'admin@ucad.sn' WHERE username = 'client-b';
UPDATE app_users SET role = 'ETUDIANT', nom = 'Fall', prenom = 'Moussa', email = 'etudiant@ucad.sn' WHERE username = 'client-c';
UPDATE app_users SET role = 'ETUDIANT', tenant_id = 'tenant-b', nom = 'Fall', prenom = 'Moussa', email = 'etudiant@ucad.sn' WHERE username = 'client-c';

INSERT INTO etudiants (id, utilisateur_id)
SELECT gen_random_uuid(), id FROM app_users WHERE role = 'ETUDIANT';