CREATE TABLE players (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL,
    team_id BIGINT       NOT NULL REFERENCES teams(id)
);

CREATE INDEX idx_players_team_id ON players(team_id);
