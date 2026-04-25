package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.GroupClassificationPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupClassificationPredictionRepository extends JpaRepository<GroupClassificationPrediction, Long> {

    Optional<GroupClassificationPrediction> findByUserIdAndGroupName(Long userId, String groupName);

    List<GroupClassificationPrediction> findByGroupName(String groupName);

    List<GroupClassificationPrediction> findByUserId(Long userId);
}
