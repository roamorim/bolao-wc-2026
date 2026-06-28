-- Mostra quantos palpites do mata-mata completo (32 jogos: R32 até a Final)
-- cada usuário já fez. Roda direto, sem precisar de deploy:
--   docker exec bolao-postgres  psql -U bolao  -d bolao  -f scripts/check-bracket-picks.sql
--   docker exec bolao2-postgres psql -U bolao2 -d bolao2 -f scripts/check-bracket-picks.sql

WITH total AS (
    SELECT COUNT(*) AS n FROM matches m
    JOIN tournament_stages ts ON ts.id = m.stage_id
    WHERE ts.code != 'GROUP'
)
SELECT
    u.display_name,
    u.username,
    COUNT(bp.id) AS palpites_feitos,
    total.n AS total_jogos,
    total.n - COUNT(bp.id) AS faltando,
    CASE WHEN COUNT(bp.id) = total.n THEN 'completo' ELSE 'incompleto' END AS status
FROM users u
CROSS JOIN total
LEFT JOIN bracket_picks bp ON bp.user_id = u.id
WHERE u.role = 'USER' AND u.active = TRUE
GROUP BY u.id, u.display_name, u.username, total.n
ORDER BY faltando DESC, u.display_name;
