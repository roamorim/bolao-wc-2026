package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.TopScorerResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopScorerResultRepository extends JpaRepository<TopScorerResult, Long> {
    Optional<TopScorerResult> findTopByOrderByRecordedAtDesc();
}
