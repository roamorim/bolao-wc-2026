package br.com.bolao.domain.repository;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m JOIN FETCH m.stage JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.id = :id")
    Optional<Match> findByIdWithTeams(@Param("id") Long id);

    @Query("SELECT m FROM Match m JOIN FETCH m.stage JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam ORDER BY m.matchDatetime ASC")
    List<Match> findAllWithTeamsOrderByDatetime();

    @Query("SELECT m FROM Match m JOIN FETCH m.stage JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.stage.code = :stageCode ORDER BY m.matchNumber ASC")
    List<Match> findByStageCode(@Param("stageCode") String stageCode);

    Optional<Match> findByMatchNumber(int matchNumber);

    List<Match> findByStatusAndPredictionDeadlineBefore(MatchStatus status, Instant deadline);

    @Query("SELECT m FROM Match m JOIN FETCH m.stage JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.matchDatetime BETWEEN :from AND :to ORDER BY m.matchDatetime ASC")
    List<Match> findByDatetimeBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(m) FROM Match m JOIN m.stage s WHERE s.code = 'GROUP'")
    long countGroupStageMatches();

    @Query("SELECT COUNT(m) FROM Match m WHERE m.stage.code != 'GROUP' AND m.homeTeam IS NOT NULL AND m.awayTeam IS NOT NULL")
    long countAvailableKnockoutMatches();

    @Query("SELECT m FROM Match m JOIN FETCH m.stage LEFT JOIN FETCH m.homeTeam LEFT JOIN FETCH m.awayTeam WHERE m.stage.code = :stageCode ORDER BY m.matchNumber ASC")
    List<Match> findByStageCodeWithNullableTeams(@Param("stageCode") String stageCode);

    @Query("SELECT m FROM Match m JOIN FETCH m.stage LEFT JOIN FETCH m.homeTeam LEFT JOIN FETCH m.awayTeam WHERE m.stage.code != 'GROUP' ORDER BY m.stage.displayOrder ASC, m.matchNumber ASC")
    List<Match> findAllKnockoutMatchesWithStage();

    @Modifying
    @Query(value = "UPDATE matches SET home_score = NULL, away_score = NULL, status = 'SCHEDULED' WHERE status != 'SCHEDULED'", nativeQuery = true)
    int resetAllResults();

    @Modifying
    @Query(value = "UPDATE matches SET home_team_id = NULL, away_team_id = NULL WHERE stage_id IN (SELECT id FROM tournament_stages WHERE code != 'GROUP')", nativeQuery = true)
    int clearKnockoutTeams();
}
