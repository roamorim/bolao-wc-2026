-- #22 COL x UZB: corrigido de 13:00 para 22:00 CLT (17/jun)
UPDATE matches
SET match_datetime      = '2026-06-18T02:00:00Z',
    prediction_deadline = '2026-06-18T01:30:00Z'
WHERE match_number = 22;

-- #31 BRA x HAI: corrigido de 18:30 para 20:30 CLT (19/jun)
UPDATE matches
SET match_datetime      = '2026-06-20T00:30:00Z',
    prediction_deadline = '2026-06-20T00:00:00Z'
WHERE match_number = 31;
