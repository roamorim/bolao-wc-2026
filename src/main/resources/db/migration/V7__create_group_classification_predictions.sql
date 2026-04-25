CREATE TABLE group_classification_predictions (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT      NOT NULL REFERENCES users(id),
    group_name           VARCHAR(1)  NOT NULL,
    first_place_team_id  BIGINT      NOT NULL REFERENCES teams(id),
    second_place_team_id BIGINT      NOT NULL REFERENCES teams(id),
    submitted_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points_earned        INT,
    deadline             TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_group_classification UNIQUE (user_id, group_name)
);

CREATE INDEX idx_gcp_user_id ON group_classification_predictions(user_id);
