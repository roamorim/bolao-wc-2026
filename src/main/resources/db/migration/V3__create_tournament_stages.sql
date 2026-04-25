CREATE TABLE tournament_stages (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(30) NOT NULL UNIQUE,
    name          VARCHAR(50) NOT NULL,
    display_order INT         NOT NULL,
    scored_on_ninety_minutes BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tournament_stages (code, name, display_order, scored_on_ninety_minutes) VALUES
('GROUP',  'Fase de Grupos',      1, TRUE),
('R32',    'Oitavas de Final',    2, TRUE),
('R16',    'Quartas de Final',    3, TRUE),
('QF',     'Semifinal',          4, TRUE),
('SF',     'Disputa 3º Lugar',   5, TRUE),
('FINAL',  'Final',              6, TRUE);
