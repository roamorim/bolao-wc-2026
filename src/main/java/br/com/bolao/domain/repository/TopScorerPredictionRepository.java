package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.TopScorerPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopScorerPredictionRepository extends JpaRepository<TopScorerPrediction, Long> {

    Optional<TopScorerPrediction> findByUserId(Long userId);

    @Query("SELECT p.user.id FROM TopScorerPrediction p")
    List<Long> findAllUserIds();
}
