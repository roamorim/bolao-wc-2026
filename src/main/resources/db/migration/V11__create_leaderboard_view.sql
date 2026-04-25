CREATE VIEW leaderboard AS
SELECT
    u.id,
    u.display_name,
    u.username,
    COALESCE(SUM(mp.points_earned), 0)
        + COALESCE(MAX(sp.points_earned), 0)
        + COALESCE(MAX(tsp.points_earned), 0)
        + COALESCE(SUM(gcp.points_earned), 0) AS total_points,
    COUNT(DISTINCT mp.match_id)               AS predictions_made,
    COUNT(DISTINCT CASE WHEN mp.points_earned IS NOT NULL THEN mp.match_id END) AS predictions_scored
FROM users u
LEFT JOIN match_predictions mp ON mp.user_id = u.id
LEFT JOIN semifinalists_prediction sp ON sp.user_id = u.id
LEFT JOIN top_scorer_prediction tsp ON tsp.user_id = u.id
LEFT JOIN group_classification_predictions gcp ON gcp.user_id = u.id
WHERE u.role = 'USER' AND u.active = TRUE
GROUP BY u.id, u.display_name, u.username
ORDER BY total_points DESC;
