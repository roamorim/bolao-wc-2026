CREATE TABLE semifinalists_prediction (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) UNIQUE,
    team1_id     BIGINT      NOT NULL REFERENCES teams(id),
    team2_id     BIGINT      NOT NULL REFERENCES teams(id),
    team3_id     BIGINT      NOT NULL REFERENCES teams(id),
    team4_id     BIGINT      NOT NULL REFERENCES teams(id),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points_earned INT,
    deadline     TIMESTAMPTZ NOT NULL
);
