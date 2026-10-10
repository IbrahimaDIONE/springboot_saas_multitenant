UPDATE app_users
SET password_hash = '{bcrypt}$2b$10$yv1tWswMY0ady7NzhyLyZOhs5y65mN5pbsvS/EAYU9XJHsqFD//C2'
WHERE username IN (
    'direction@ipd.sn',
    'direction@ucad.sn',
    'abdoulaye.diop@ipd.edu.sn',
    'sylvie.diallo@ucad.edu.sn'
)
AND enabled = TRUE;