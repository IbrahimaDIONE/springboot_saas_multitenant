UPDATE app_users
SET password_hash = '{bcrypt}$2b$10$vf9wVZD0ngYcnvpa8KmwdOH1Yvek8Rz8d6iBJIGqxkPi0VxjQVNQC'
WHERE username = 'ibzodione20@gmail.com'
  AND tenant_id = 'tenant-platform';