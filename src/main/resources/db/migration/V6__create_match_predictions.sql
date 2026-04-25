CREATE TABLE match_predictions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id),
    match_id        BIGINT      NOT NULL REFERENCES matches(id),
    home_score_pred INT         NOT NULL,
    away_score_pred INT         NOT NULL,
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points_earned   INT,
    CONSTRAINT uq_match_prediction UNIQUE (user_id, match_id),
    CONSTRAINT chk_pred_scores CHECK (home_score_pred >= 0 AND away_score_pred >= 0)
);

CREATE INDEX idx_match_predictions_user_id ON match_predictions(user_id);
CREATE INDEX idx_match_predictions_match_id ON match_predictions(match_id);
