UPDATE matches
SET prediction_deadline = (
    SELECT match_datetime - INTERVAL '30 minutes'
    FROM matches WHERE match_number = 73
)
WHERE stage_id IN (SELECT id FROM tournament_stages WHERE code != 'GROUP');
