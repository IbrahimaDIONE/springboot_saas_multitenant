UPDATE app_users
SET password_hash = '{bcrypt}$2b$10$vf9wVZD0ngYcnvpa8KmwdOH1Yvek8Rz8d6iBJIGqxkPi0VxjQVNQC',
    enabled = TRUE
WHERE username = 'ibzodione20@gmail.com'
  AND tenant_id = 'tenant-platform'
  AND role = 'ADMIN_PLATEFORME';

UPDATE app_users
SET password_hash = '{bcrypt}$2b$10$yv1tWswMY0ady7NzhyLyZOhs5y65mN5pbsvS/EAYU9XJHsqFD//C2',
    enabled = TRUE
WHERE (username, tenant_id, role) IN (
    ('direction@ipd.sn', 'tenant-ipd', 'ADMIN_ETABLISSEMENT'),
    ('direction@ucad.sn', 'tenant-ucad', 'ADMIN_ETABLISSEMENT'),
    ('abdoulaye.diop@ipd.edu.sn', 'tenant-ipd', 'ETUDIANT'),
    ('sylvie.diallo@ucad.edu.sn', 'tenant-ucad', 'ETUDIANT')
);