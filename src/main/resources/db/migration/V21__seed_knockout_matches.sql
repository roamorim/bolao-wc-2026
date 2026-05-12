-- Fix QF stage name (it's quarter-finals, not semi-finals)
UPDATE tournament_stages SET name = 'Quartas de Final' WHERE code = 'QF';

-- Make room for new SEMI stage (actual semi-finals, 2 matches)
UPDATE tournament_stages SET display_order = 6 WHERE code = 'SF';
UPDATE tournament_stages SET display_order = 7 WHERE code = 'FINAL';

INSERT INTO tournament_stages (code, name, display_order, scored_on_ninety_minutes)
VALUES ('SEMI', 'Semifinal', 5, TRUE);

-- Insert 32 knockout matches with NULL teams (assigned by simulation)
-- prediction_deadline set far in future so they stay open for sim predictions
DO $$
DECLARE
    v_r32  BIGINT;
    v_r16  BIGINT;
    v_qf   BIGINT;
    v_semi BIGINT;
    v_sf   BIGINT;
    v_fin  BIGINT;
BEGIN
    SELECT id INTO v_r32  FROM tournament_stages WHERE code = 'R32';
    SELECT id INTO v_r16  FROM tournament_stages WHERE code = 'R16';
    SELECT id INTO v_qf   FROM tournament_stages WHERE code = 'QF';
    SELECT id INTO v_semi FROM tournament_stages WHERE code = 'SEMI';
    SELECT id INTO v_sf   FROM tournament_stages WHERE code = 'SF';
    SELECT id INTO v_fin  FROM tournament_stages WHERE code = 'FINAL';

    -- R32: Oitavas de Final — 16 matches (73–88), July 1–4 2026
    INSERT INTO matches (stage_id, match_number, match_datetime, prediction_deadline, status) VALUES
    (v_r32, 73,  '2026-07-01T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 74,  '2026-07-01T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 75,  '2026-07-01T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 76,  '2026-07-01T23:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 77,  '2026-07-02T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 78,  '2026-07-02T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 79,  '2026-07-02T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 80,  '2026-07-02T23:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 81,  '2026-07-03T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 82,  '2026-07-03T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 83,  '2026-07-03T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 84,  '2026-07-03T23:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 85,  '2026-07-04T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 86,  '2026-07-04T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 87,  '2026-07-04T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r32, 88,  '2026-07-04T23:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),

    -- R16: Quartas de Final — 8 matches (89–96), July 8–11 2026
    (v_r16, 89,  '2026-07-08T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 90,  '2026-07-08T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 91,  '2026-07-09T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 92,  '2026-07-09T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 93,  '2026-07-10T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 94,  '2026-07-10T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 95,  '2026-07-11T14:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_r16, 96,  '2026-07-11T20:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),

    -- QF: Quartas de Final → Semis — 4 matches (97–100), July 15–16 2026
    (v_qf,  97,  '2026-07-15T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_qf,  98,  '2026-07-15T21:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_qf,  99,  '2026-07-16T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_qf,  100, '2026-07-16T21:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),

    -- SEMI: Semifinal — 2 matches (101–102), July 19–20 2026
    (v_semi, 101, '2026-07-19T21:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),
    (v_semi, 102, '2026-07-20T21:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),

    -- SF: Disputa 3º Lugar — 1 match (103), July 23 2026
    (v_sf,  103, '2026-07-23T17:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED'),

    -- FINAL — 1 match (104), July 26 2026
    (v_fin, 104, '2026-07-26T21:00:00Z', '2026-12-31T23:59:59Z', 'SCHEDULED');
END $$;
