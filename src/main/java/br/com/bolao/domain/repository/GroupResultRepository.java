package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.GroupResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupResultRepository extends JpaRepository<GroupResult, String> {

    @Query("SELECT r FROM GroupResult r JOIN FETCH r.firstTeam JOIN FETCH r.secondTeam LEFT JOIN FETCH r.thirdTeam")
    List<GroupResult> findAllWithTeams();
}
