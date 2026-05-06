-- Permite deletar usuários mesmo que tenham apostas (apostas são deletadas em cascata)
ALTER TABLE match_predictions
    DROP CONSTRAINT match_predictions_user_id_fkey,
    ADD CONSTRAINT match_predictions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE group_classification_predictions
    DROP CONSTRAINT group_classification_predictions_user_id_fkey,
    ADD CONSTRAINT group_classification_predictions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE semifinalists_prediction
    DROP CONSTRAINT semifinalists_prediction_user_id_fkey,
    ADD CONSTRAINT semifinalists_prediction_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE top_scorer_prediction
    DROP CONSTRAINT top_scorer_prediction_user_id_fkey,
    ADD CONSTRAINT top_scorer_prediction_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
