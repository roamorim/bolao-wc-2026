package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.ScoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoringConfigRepository extends JpaRepository<ScoringConfig, Long> {

    Optional<ScoringConfig> findByConfigKey(String configKey);
}
