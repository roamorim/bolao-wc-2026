UPDATE matches
SET prediction_deadline = match_datetime - INTERVAL '30 minutes'
WHERE stage_id = (SELECT id FROM tournament_stages WHERE name = 'Fase de Grupos');
