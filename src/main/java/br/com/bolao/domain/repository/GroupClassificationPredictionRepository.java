package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.GroupClassificationPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupClassificationPredictionRepository extends JpaRepository<GroupClassificationPrediction, Long> {

    Optional<GroupClassificationPrediction> findByUserIdAndGroupName(Long userId, String groupName);

    List<GroupClassificationPrediction> findByGroupName(String groupName);

    List<GroupClassificationPrediction> findByUserId(Long userId);

    long countByUserId(Long userId);

    @Query("SELECT p.user.id, COUNT(p) FROM GroupClassificationPrediction p GROUP BY p.user.id")
    List<Object[]> countPredictionsPerUser();

    @Query("SELECT p FROM GroupClassificationPrediction p JOIN FETCH p.firstPlaceTeam JOIN FETCH p.secondPlaceTeam LEFT JOIN FETCH p.thirdPlaceTeam WHERE p.user.id = :userId ORDER BY p.groupName ASC")
    List<GroupClassificationPrediction> findByUserIdWithTeams(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE GroupClassificationPrediction p SET p.pointsEarned = null")
    void clearAllPointsEarned();
}
