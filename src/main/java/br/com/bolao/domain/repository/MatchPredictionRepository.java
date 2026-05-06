package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {

    Optional<MatchPrediction> findByUserAndMatch(User user, Match match);

    List<MatchPrediction> findByMatchId(Long matchId);

    @Query("SELECT p FROM MatchPrediction p JOIN FETCH p.match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE p.user.id = :userId ORDER BY m.matchDatetime ASC")
    List<MatchPrediction> findByUserIdWithMatch(@Param("userId") Long userId);

    @Query("SELECT p.user.id, COUNT(p) FROM MatchPrediction p JOIN p.match m JOIN m.stage s WHERE s.code = 'GROUP' GROUP BY p.user.id")
    List<Object[]> countGroupStagePredictionsPerUser();
}
