-- Novas chaves de pontuação por posição (substituem a lógica de GROUP_CLASSIFICATION_CORRECT_PER_TEAM)
INSERT INTO scoring_config (config_key, points, description) VALUES
    ('GROUP_CLASSIFICATION_CORRECT_POSITION', 5,
     'Seleção classificada na posição correta (1º ou 2º do grupo)'),
    ('GROUP_CLASSIFICATION_WRONG_POSITION', 2,
     'Seleção classificada, mas na posição errada (apostou 1º e ficou 2º ou vice-versa)'),
    ('GROUP_THIRD_QUALIFIES', 4,
     '3º do grupo avança como melhor terceiro (equipe correta e avanço correto)');

-- 3º lugar e flag de avanço na predição de classificação de grupo
ALTER TABLE group_classification_predictions
    ADD COLUMN third_place_team_id BIGINT REFERENCES teams(id),
    ADD COLUMN third_qualifies     BOOLEAN NOT NULL DEFAULT FALSE;

-- Resultados oficiais por grupo — preenchido pelo admin após cada grupo terminar
CREATE TABLE group_results (
    group_name      VARCHAR(1)   PRIMARY KEY,
    first_team_id   BIGINT       REFERENCES teams(id),
    second_team_id  BIGINT       REFERENCES teams(id),
    third_team_id   BIGINT       REFERENCES teams(id),
    third_qualifies BOOLEAN      NOT NULL DEFAULT FALSE,
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
