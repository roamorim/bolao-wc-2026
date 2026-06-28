package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.model.TournamentStage;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.web.dto.request.MatchResultRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TournamentAdminServiceTest {

    private MatchRepository matchRepository;
    private TeamRepository teamRepository;
    private TournamentAdminService service;

    private Team home;
    private Team away;
    private Team someOtherTeam;
    private Match knockoutMatch;

    @BeforeEach
    void setUp() {
        matchRepository = mock(MatchRepository.class);
        teamRepository = mock(TeamRepository.class);
        service = new TournamentAdminService(matchRepository, teamRepository, mock(), mock(), mock());

        home = new Team();
        home.setId(1L);
        away = new Team();
        away.setId(2L);
        someOtherTeam = new Team();
        someOtherTeam.setId(3L);

        TournamentStage r32 = new TournamentStage();
        r32.setCode("R32");

        knockoutMatch = new Match();
        knockoutMatch.setId(10L);
        knockoutMatch.setStage(r32);
        knockoutMatch.setHomeTeam(home);
        knockoutMatch.setAwayTeam(away);
        knockoutMatch.setStatus(MatchStatus.LOCKED);
        knockoutMatch.setMatchDatetime(Instant.now());
        knockoutMatch.setPredictionDeadline(Instant.now());

        when(matchRepository.findById(10L)).thenReturn(Optional.of(knockoutMatch));
    }

    @Test
    void knockoutMatch_tiedWithoutPenaltyWinner_isRejected() {
        assertThatThrownBy(() ->
            service.submitMatchResult(10L, new MatchResultRequest(1, 1, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("pênaltis");
    }

    @Test
    void knockoutMatch_tiedWithPenaltyWinner_isAccepted_andPenaltyWinnerIsRecorded() {
        when(teamRepository.findById(2L)).thenReturn(Optional.of(away));

        service.submitMatchResult(10L, new MatchResultRequest(1, 1, 2L));

        assertThat(knockoutMatch.getPenaltyWinner()).isEqualTo(away);
        assertThat(knockoutMatch.isFinished()).isTrue();
    }

    @Test
    void penaltyWinner_mustBeOneOfTheTwoTeamsInTheMatch() {
        when(teamRepository.findById(3L)).thenReturn(Optional.of(someOtherTeam));

        assertThatThrownBy(() ->
            service.submitMatchResult(10L, new MatchResultRequest(1, 1, 3L)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void knockoutMatch_notTied_doesNotRequirePenaltyWinner() {
        service.submitMatchResult(10L, new MatchResultRequest(2, 0, null));

        assertThat(knockoutMatch.getPenaltyWinner()).isNull();
        assertThat(knockoutMatch.isFinished()).isTrue();
    }

    @Test
    void groupStageMatch_tiedWithoutPenaltyWinner_isAccepted_drawsAreValidInGroups() {
        TournamentStage group = new TournamentStage();
        group.setCode("GROUP");
        Match groupMatch = new Match();
        groupMatch.setId(20L);
        groupMatch.setStage(group);
        groupMatch.setHomeTeam(home);
        groupMatch.setAwayTeam(away);
        groupMatch.setStatus(MatchStatus.LOCKED);
        groupMatch.setMatchDatetime(Instant.now());
        groupMatch.setPredictionDeadline(Instant.now());
        when(matchRepository.findById(20L)).thenReturn(Optional.of(groupMatch));

        service.submitMatchResult(20L, new MatchResultRequest(1, 1, null));

        assertThat(groupMatch.isFinished()).isTrue();
        assertThat(groupMatch.getPenaltyWinner()).isNull();
    }

    @Test
    void alreadyFinishedMatch_isRejected() {
        knockoutMatch.setStatus(MatchStatus.FINISHED);

        assertThatThrownBy(() ->
            service.submitMatchResult(10L, new MatchResultRequest(2, 0, null)))
            .isInstanceOf(IllegalStateException.class);
    }
}
