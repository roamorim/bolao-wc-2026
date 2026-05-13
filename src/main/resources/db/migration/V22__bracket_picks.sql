CREATE TABLE bracket_picks (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_id            BIGINT NOT NULL REFERENCES matches(id),
    predicted_winner_id BIGINT REFERENCES teams(id),
    points_earned       INT,
    submitted_at        TIMESTAMPTZ,
    CONSTRAINT uq_bracket_pick UNIQUE (user_id, match_id)
);

INSERT INTO scoring_config (config_key, points, description)
VALUES ('BRACKET_CORRECT_PICK', 5, 'Acerto do vencedor no mata-mata (bracket picker)');
