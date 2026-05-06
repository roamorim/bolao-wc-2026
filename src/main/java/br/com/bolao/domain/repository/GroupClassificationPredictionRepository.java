package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.GroupClassificationPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupClassificationPredictionRepository extends JpaRepository<GroupClassificationPrediction, Long> {

    Optional<GroupClassificationPrediction> findByUserIdAndGroupName(Long userId, String groupName);

    List<GroupClassificationPrediction> findByGroupName(String groupName);

    List<GroupClassificationPrediction> findByUserId(Long userId);

    @Query("SELECT p.user.id, COUNT(p) FROM GroupClassificationPrediction p GROUP BY p.user.id")
    List<Object[]> countPredictionsPerUser();
}
