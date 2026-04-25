CREATE TABLE scoring_config (
    id          BIGSERIAL PRIMARY KEY,
    config_key  VARCHAR(60)  NOT NULL UNIQUE,
    points      INT          NOT NULL,
    description VARCHAR(200) NOT NULL
);

INSERT INTO scoring_config (config_key, points, description) VALUES
('GROUP_EXACT_SCORE',                    10, 'Placar exato na fase de grupos'),
('GROUP_CORRECT_WINNER_AND_DIFF',         7, 'Vencedor correto e saldo de gols correto nos grupos'),
('GROUP_CORRECT_WINNER',                  3, 'Apenas vencedor correto nos grupos'),
('GROUP_CORRECT_DRAW',                    5, 'Empate correto na fase de grupos'),
('KNOCKOUT_EXACT_SCORE',                 15, 'Placar exato no mata-mata (resultado de 90 min)'),
('KNOCKOUT_CORRECT_WINNER_AND_DIFF',     10, 'Vencedor e saldo correto no mata-mata'),
('KNOCKOUT_CORRECT_WINNER',               5, 'Apenas vencedor correto no mata-mata'),
('GROUP_CLASSIFICATION_CORRECT_PER_TEAM', 3, 'Por seleção correta no top-2 de cada grupo'),
('SEMIFINALISTS_CORRECT_PER_TEAM',       10, 'Por semifinalista correto'),
('TOP_SCORER_CORRECT',                   40, 'Artilheiro correto');
