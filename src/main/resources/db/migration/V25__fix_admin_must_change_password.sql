-- Admins não precisam trocar senha no primeiro acesso
UPDATE users SET must_change_password = FALSE WHERE role = 'ADMIN';
