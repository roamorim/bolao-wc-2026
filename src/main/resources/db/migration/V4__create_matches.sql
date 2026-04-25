CREATE TABLE matches (
    id                  BIGSERIAL PRIMARY KEY,
    stage_id            BIGINT       NOT NULL REFERENCES tournament_stages(id),
    match_number        INT          NOT NULL,
    home_team_id        BIGINT       REFERENCES teams(id),
    away_team_id        BIGINT       REFERENCES teams(id),
    match_datetime      TIMESTAMPTZ  NOT NULL,
    prediction_deadline TIMESTAMPTZ  NOT NULL,
    venue               VARCHAR(200),
    home_score          INT,
    away_score          INT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT chk_match_status CHECK (status IN ('SCHEDULED', 'LOCKED', 'FINISHED'))
);

CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_stage_id ON matches(stage_id);
CREATE INDEX idx_matches_match_datetime ON matches(match_datetime);
