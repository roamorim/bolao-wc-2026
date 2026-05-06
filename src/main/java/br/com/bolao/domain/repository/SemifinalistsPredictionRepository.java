package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.SemifinalistsPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SemifinalistsPredictionRepository extends JpaRepository<SemifinalistsPrediction, Long> {

    Optional<SemifinalistsPrediction> findByUserId(Long userId);

    List<SemifinalistsPrediction> findAll();

    @Query("SELECT p.user.id FROM SemifinalistsPrediction p")
    List<Long> findAllUserIds();
}
