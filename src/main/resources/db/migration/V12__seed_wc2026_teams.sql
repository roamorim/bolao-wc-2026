-- 48 qualified teams for FIFA World Cup 2026 (USA, Canada, Mexico)
-- Groups A-L | 12 groups of 4 teams
-- NOTE: Verify this list against official FIFA draw (December 2025) before production use.
INSERT INTO teams (name, code, group_name) VALUES
-- Group A (USA host group)
('Estados Unidos',     'USA', 'A'),
('Jamaica',            'JAM', 'A'),
('Panamá',             'PAN', 'A'),
('Honduras',           'HON', 'A'),
-- Group B
('México',             'MEX', 'B'),
('Equador',            'ECU', 'B'),
('Venezuela',          'VEN', 'B'),
('Nova Zelândia',      'NZL', 'B'),
-- Group C (Canada host group)
('Canadá',             'CAN', 'C'),
('Marrocos',           'MAR', 'C'),
('Bélgica',            'BEL', 'C'),
('Croácia',            'CRO', 'C'),
-- Group D
('Brasil',             'BRA', 'D'),
('Colômbia',           'COL', 'D'),
('Costa Rica',         'CRC', 'D'),
('Paraguai',           'PAR', 'D'),
-- Group E
('Argentina',          'ARG', 'E'),
('Uruguai',            'URU', 'E'),
('Peru',               'PER', 'E'),
('Nigéria',            'NGA', 'E'),
-- Group F
('Espanha',            'ESP', 'F'),
('Portugal',           'POR', 'F'),
('Senegal',            'SEN', 'F'),
('Camarões',           'CMR', 'F'),
-- Group G
('França',             'FRA', 'G'),
('Alemanha',           'GER', 'G'),
('Tunísia',            'TUN', 'G'),
('Gana',               'GHA', 'G'),
-- Group H
('Inglaterra',         'ENG', 'H'),
('Países Baixos',      'NED', 'H'),
('Irã',                'IRN', 'H'),
('Arábia Saudita',     'KSA', 'H'),
-- Group I
('Japão',              'JPN', 'I'),
('Coreia do Sul',      'KOR', 'I'),
('Austrália',          'AUS', 'I'),
('Cazaquistão',        'KAZ', 'I'),
-- Group J
('Itália',             'ITA', 'J'),
('Suíça',              'SUI', 'J'),
('Turquia',            'TUR', 'J'),
('Albânia',            'ALB', 'J'),
-- Group K
('Dinamarca',          'DEN', 'K'),
('Sérvia',             'SRB', 'K'),
('Eslováquia',         'SVK', 'K'),
('Rep. Dem. do Congo', 'COD', 'K'),
-- Group L
('Romênia',            'ROU', 'L'),
('Áustria',            'AUT', 'L'),
('Egito',              'EGY', 'L'),
('Bolívia',            'BOL', 'L');
