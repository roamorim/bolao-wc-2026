-- Reaplica a fórmula do V28 (prazo = horário do jogo 73 menos 30min) agora que o
-- match_datetime do R32 foi corrigido para o calendário real da Copa (fetch-official-
-- bracket.sh). O V28 tinha calculado isso sobre a data placeholder antiga (01/07).
UPDATE matches
SET prediction_deadline = (
    SELECT match_datetime - INTERVAL '30 minutes'
    FROM matches WHERE match_number = 73
)
WHERE stage_id IN (SELECT id FROM tournament_stages WHERE code != 'GROUP');
