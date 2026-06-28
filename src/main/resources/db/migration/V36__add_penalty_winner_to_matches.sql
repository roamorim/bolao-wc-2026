ALTER TABLE matches ADD COLUMN penalty_winner_team_id BIGINT REFERENCES teams(id);
