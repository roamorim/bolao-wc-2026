UPDATE matches
SET prediction_deadline = match_datetime - INTERVAL '24 hours'
WHERE stage_id = (SELECT id FROM tournament_stages WHERE name = 'Fase de Grupos');
