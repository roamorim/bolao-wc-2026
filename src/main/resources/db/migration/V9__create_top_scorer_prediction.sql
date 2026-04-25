CREATE TABLE top_scorer_prediction (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) UNIQUE,
    player_name  VARCHAR(150) NOT NULL,
    team_id      BIGINT       NOT NULL REFERENCES teams(id),
    submitted_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    points_earned INT,
    deadline     TIMESTAMPTZ  NOT NULL
);
