package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p JOIN FETCH p.team ORDER BY p.team.name ASC, p.name ASC")
    List<Player> findAllWithTeamOrdered();

    @Query("SELECT p FROM Player p JOIN FETCH p.team WHERE p.id = :id")
    Optional<Player> findByIdWithTeam(@Param("id") Long id);
}
