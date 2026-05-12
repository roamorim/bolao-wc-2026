package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.SemifinalistsPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SemifinalistsPredictionRepository extends JpaRepository<SemifinalistsPrediction, Long> {

    Optional<SemifinalistsPrediction> findByUserId(Long userId);

    List<SemifinalistsPrediction> findAll();

    @Query("SELECT p.user.id FROM SemifinalistsPrediction p")
    List<Long> findAllUserIds();

    @Query("SELECT p FROM SemifinalistsPrediction p JOIN FETCH p.team1 JOIN FETCH p.team2 JOIN FETCH p.team3 JOIN FETCH p.team4 WHERE p.user.id = :userId")
    Optional<SemifinalistsPrediction> findByUserIdWithTeams(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE SemifinalistsPrediction p SET p.pointsEarned = null")
    void clearAllPointsEarned();
}
