-- Remove chaves de pontuação inválidas ou obsoletas:
-- KNOCKOUT_EXACT_SCORE, KNOCKOUT_CORRECT_WINNER_AND_DIFF, KNOCKOUT_CORRECT_WINNER:
--   o mata-mata é pontuado exclusivamente via bracket picks.
-- GROUP_CLASSIFICATION_CORRECT_PER_TEAM:
--   substituída pelas chaves de posição adicionadas em V19.
DELETE FROM scoring_config
WHERE config_key IN (
    'KNOCKOUT_EXACT_SCORE',
    'KNOCKOUT_CORRECT_WINNER_AND_DIFF',
    'KNOCKOUT_CORRECT_WINNER',
    'GROUP_CLASSIFICATION_CORRECT_PER_TEAM'
);
