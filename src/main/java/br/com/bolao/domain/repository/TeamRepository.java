package br.com.bolao.domain.repository;

import br.com.bolao.domain.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByGroupNameOrderByName(String groupName);

    List<Team> findAllByOrderByGroupNameAscNameAsc();
}
