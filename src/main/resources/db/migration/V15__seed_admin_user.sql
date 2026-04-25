-- Hash BCrypt cost=12 da senha "admin123"
INSERT INTO users (username, email, password_hash, display_name, role, active)
VALUES ('admin', 'admin@bolao.local', '$2b$12$BSAhj0kCWdcG1jzfAzdXEeElDMXtrFx9SRA/ltwBSZppY86OmU5Um', 'Administrador', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;
