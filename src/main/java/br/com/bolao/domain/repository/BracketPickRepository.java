package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.BracketPick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BracketPickRepository extends JpaRepository<BracketPick, Long> {

    Optional<BracketPick> findByUserIdAndMatchId(Long userId, Long matchId);

    List<BracketPick> findByMatchId(Long matchId);

    @Query("SELECT p FROM BracketPick p JOIN FETCH p.match m JOIN FETCH m.stage JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam LEFT JOIN FETCH p.predictedWinner WHERE p.user.id = :userId ORDER BY m.matchNumber ASC")
    List<BracketPick> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM BracketPick p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE BracketPick p SET p.pointsEarned = NULL")
    void clearAllPointsEarned();
}
