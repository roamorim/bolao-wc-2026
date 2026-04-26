-- Reseed teams with official FIFA WC 2026 draw (December 2025)
-- Source: https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos

-- Clear dependent data first
DELETE FROM match_predictions;
DELETE FROM matches;
DELETE FROM teams;

INSERT INTO teams (name, code, group_name, flag_code) VALUES
-- Grupo A
('México',            'MEX', 'A', 'mx'),
('África do Sul',     'RSA', 'A', 'za'),
('Coreia do Sul',     'KOR', 'A', 'kr'),
('Rep. Tcheca',       'CZE', 'A', 'cz'),
-- Grupo B
('Canadá',            'CAN', 'B', 'ca'),
('Bósnia',            'BIH', 'B', 'ba'),
('Suíça',             'SUI', 'B', 'ch'),
('Catar',             'QAT', 'B', 'qa'),
-- Grupo C
('Brasil',            'BRA', 'C', 'br'),
('Marrocos',          'MAR', 'C', 'ma'),
('Escócia',           'SCO', 'C', 'gb-sct'),
('Haiti',             'HAI', 'C', 'ht'),
-- Grupo D
('Estados Unidos',    'USA', 'D', 'us'),
('Paraguai',          'PAR', 'D', 'py'),
('Austrália',         'AUS', 'D', 'au'),
('Turquia',           'TUR', 'D', 'tr'),
-- Grupo E
('Alemanha',          'GER', 'E', 'de'),
('Curaçao',           'CUW', 'E', 'cw'),
('Costa do Marfim',   'CIV', 'E', 'ci'),
('Equador',           'ECU', 'E', 'ec'),
-- Grupo F
('Holanda',           'NED', 'F', 'nl'),
('Japão',             'JPN', 'F', 'jp'),
('Tunísia',           'TUN', 'F', 'tn'),
('Suécia',            'SWE', 'F', 'se'),
-- Grupo G
('Bélgica',           'BEL', 'G', 'be'),
('Egito',             'EGY', 'G', 'eg'),
('Irã',               'IRN', 'G', 'ir'),
('Nova Zelândia',     'NZL', 'G', 'nz'),
-- Grupo H
('Espanha',           'ESP', 'H', 'es'),
('Cabo Verde',        'CPV', 'H', 'cv'),
('Arábia Saudita',    'KSA', 'H', 'sa'),
('Uruguai',           'URU', 'H', 'uy'),
-- Grupo I
('França',            'FRA', 'I', 'fr'),
('Senegal',           'SEN', 'I', 'sn'),
('Noruega',           'NOR', 'I', 'no'),
('Iraque',            'IRQ', 'I', 'iq'),
-- Grupo J
('Argentina',         'ARG', 'J', 'ar'),
('Argélia',           'ALG', 'J', 'dz'),
('Áustria',           'AUT', 'J', 'at'),
('Jordânia',          'JOR', 'J', 'jo'),
-- Grupo K
('Portugal',          'POR', 'K', 'pt'),
('Rep. Dem. do Congo','COD', 'K', 'cd'),
('Colômbia',          'COL', 'K', 'co'),
('Uzbequistão',       'UZB', 'K', 'uz'),
-- Grupo L
('Inglaterra',        'ENG', 'L', 'gb-eng'),
('Croácia',           'CRO', 'L', 'hr'),
('Gana',              'GHA', 'L', 'gh'),
('Panamá',            'PAN', 'L', 'pa');
