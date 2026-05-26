ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT TRUE;

-- Admin seed não precisa trocar senha no primeiro acesso
UPDATE users SET must_change_password = FALSE WHERE username = 'admin';
