CREATE TABLE top_scorer_result (
    id          BIGSERIAL PRIMARY KEY,
    player_name VARCHAR(150) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
