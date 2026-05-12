-- Reset all match results back to SCHEDULED and clear all scoring
-- Run: docker exec -i bolao-postgres psql -U bolao -d bolao < scripts/reset-results.sql

BEGIN;

-- Reset match results and status
UPDATE matches
SET home_score  = NULL,
    away_score  = NULL,
    status      = 'SCHEDULED'
WHERE status IN ('FINISHED', 'LOCKED');

-- Clear scored points from all prediction types
UPDATE match_predictions              SET points_earned = NULL;
UPDATE group_classification_predictions SET points_earned = NULL;
UPDATE semifinalists_prediction        SET points_earned = NULL;
UPDATE top_scorer_prediction           SET points_earned = NULL;

-- Clear recorded group results
DELETE FROM group_results;

COMMIT;

SELECT 'Reset concluído.' AS resultado;
