package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.BracketPick;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.model.TournamentStage;
import br.com.bolao.domain.model.User;
import br.com.bolao.domain.repository.BracketPickRepository;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BracketAssemblyServiceTest {

    private MatchRepository matchRepository;
    private BracketPickRepository bracketPickRepository;
    private BracketAssemblyService service;
    private final List<Match> matches = new ArrayList<>();
    private final List<BracketPick> picks = new ArrayList<>();

    private static final Long USER_ID = 1L;
    private static final TournamentStage R32 = stage("R32");
    private static final TournamentStage R16 = stage("R16");
    private static final TournamentStage QF = stage("QF");
    private static final TournamentStage SEMI = stage("SEMI");
    private static final TournamentStage SF = stage("SF");
    private static final TournamentStage FINAL = stage("FINAL");

    @BeforeEach
    void setUp() {
        matchRepository = mock(MatchRepository.class);
        bracketPickRepository = mock(BracketPickRepository.class);
        service = new BracketAssemblyService(mock(GroupResultRepository.class), matchRepository, bracketPickRepository);

        when(matchRepository.findAllKnockoutMatchesWithStage()).thenReturn(matches);
        when(bracketPickRepository.findByUserIdWithDetails(USER_ID)).thenReturn(picks);
    }

    private static TournamentStage stage(String code) {
        TournamentStage s = new TournamentStage();
        s.setCode(code);
        return s;
    }

    private static Team team(long id, String name) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setCode("T" + id);
        return t;
    }

    private Match addMatch(int number, TournamentStage stage, Team home, Team away) {
        Match m = new Match();
        m.setId((long) number);
        m.setMatchNumber(number);
        m.setStage(stage);
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setMatchDatetime(Instant.now());
        m.setPredictionDeadline(Instant.now().plusSeconds(3600));
        matches.add(m);
        return m;
    }

    private void pick(Match match, Team predictedWinner) {
        User user = new User();
        user.setId(USER_ID);
        BracketPick p = new BracketPick();
        p.setUser(user);
        p.setMatch(match);
        p.setPredictedWinner(predictedWinner);
        picks.add(p);
    }

    @Test
    void r32MatchWithRealTeams_projectsTheRealTeamsDirectly() {
        Team brazil = team(1, "Brazil");
        Team japan = team(2, "Japan");
        addMatch(73, R32, brazil, japan);

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(73).home()).isEqualTo(brazil);
        assertThat(projected.get(73).away()).isEqualTo(japan);
    }

    @Test
    void r16Match_projectsTeamsFromUsersOwnPicksOnTheSourceMatches() {
        Team brazil = team(1, "Brazil");
        Team japan = team(2, "Japan");
        Team germany = team(3, "Germany");
        Team paraguay = team(4, "Paraguay");
        Match m75 = addMatch(75, R32, brazil, japan);
        Match m78 = addMatch(78, R32, germany, paraguay);
        addMatch(89, R16, null, null);

        pick(m75, brazil);
        pick(m78, paraguay);

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(89).home()).isEqualTo(brazil);
        assertThat(projected.get(89).away()).isEqualTo(paraguay);
    }

    @Test
    void r16Match_isUnresolvedWhenOneSourcePickIsMissing() {
        Team brazil = team(1, "Brazil");
        Team japan = team(2, "Japan");
        Team germany = team(3, "Germany");
        Team paraguay = team(4, "Paraguay");
        Match m75 = addMatch(75, R32, brazil, japan);
        addMatch(78, R32, germany, paraguay);
        addMatch(89, R16, null, null);

        pick(m75, brazil);
        // no pick recorded for match 78

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(89).home()).isEqualTo(brazil);
        assertThat(projected.get(89).away()).isNull();
    }

    @Test
    void realResultOverridesTheUsersOwnGuess_oncePreviousRoundIsActuallyPlayed() {
        Team brazil = team(1, "Brazil");
        Team japan = team(2, "Japan");
        Match m75 = addMatch(75, R32, brazil, japan);
        m75.setHomeScore(1);
        m75.setAwayScore(3); // Japan actually won, even though the user guessed Brazil
        Match m78 = addMatch(78, R32, team(3, "Germany"), team(4, "Paraguay"));
        m78.setHomeScore(2);
        m78.setAwayScore(0);
        addMatch(89, R16, null, null);

        pick(m75, brazil);
        pick(m78, m78.getHomeTeam());

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(89).home()).isEqualTo(japan);
        assertThat(projected.get(89).away()).isEqualTo(m78.getHomeTeam());
    }

    @Test
    void quarterFinal_cascadesThroughTwoLevelsOfUserPicks() {
        Team t1 = team(1, "T1");
        Team t2 = team(2, "T2");
        Team t3 = team(3, "T3");
        Team t4 = team(4, "T4");
        Match m75 = addMatch(75, R32, t1, t2);
        Match m78 = addMatch(78, R32, t3, t4);
        Match m89 = addMatch(89, R16, null, null);
        addMatch(97, QF, null, null);

        pick(m75, t1);
        pick(m78, t3);
        pick(m89, t1); // user picks t1 to win the (virtual) R16 match too

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(89).home()).isEqualTo(t1);
        assertThat(projected.get(89).away()).isEqualTo(t3);
        assertThat(projected.get(97).home()).isEqualTo(t1);
    }

    @Test
    void r16Match93_isFedByMatch74AndMatch77_notByMatch73() {
        // Real-world bug report: Brazil (home of match 74) must face the winner of
        // Ivory Coast x Norway (match 77) in the R16 — NOT the winner of South
        // Africa x Canada (match 73), which is on the other half of the bracket.
        Team brazil = team(1, "Brazil");
        Team japan = team(2, "Japan");
        Team ivoryCoast = team(3, "Ivory Coast");
        Team norway = team(4, "Norway");
        Team southAfrica = team(5, "South Africa");
        Team canada = team(6, "Canada");
        Match m74 = addMatch(74, R32, brazil, japan);
        Match m77 = addMatch(77, R32, ivoryCoast, norway);
        addMatch(73, R32, southAfrica, canada);
        addMatch(93, R16, null, null);

        pick(m74, brazil);
        pick(m77, norway);

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(93).home()).isEqualTo(brazil);
        assertThat(projected.get(93).away()).isEqualTo(norway);
    }

    @Test
    void advanceStageIfComplete_setsRealTeams_butNeverTouchesThePredictionDeadline() {
        // Bug found while investigating the auto-result-fetcher: the bracket-wide
        // deadline must stay fixed forever once set (everyone picks the whole
        // bracket before match 73) — advancing a stage must not push it forward
        // to the next stage's still-stale seeded match_datetime.
        Team t1 = team(1, "T1");
        Team t2 = team(2, "T2");
        Team t3 = team(3, "T3");
        Team t4 = team(4, "T4");
        Match m75 = addMatch(75, R32, t1, t2);
        m75.setStatus(MatchStatus.FINISHED);
        m75.setHomeScore(2);
        m75.setAwayScore(0);
        Match m78 = addMatch(78, R32, t3, t4);
        m78.setStatus(MatchStatus.FINISHED);
        m78.setHomeScore(1);
        m78.setAwayScore(3);

        Match m89 = addMatch(89, R16, null, null);
        m89.setMatchDatetime(Instant.now().plus(10, ChronoUnit.DAYS)); // stale seeded placeholder
        Instant fixedDeadline = Instant.parse("2026-06-28T18:30:00Z");
        m89.setPredictionDeadline(fixedDeadline);

        when(matchRepository.findByStageCodeWithNullableTeams("R32")).thenReturn(List.of(m75, m78));
        when(matchRepository.findByStageCodeWithNullableTeams("R16")).thenReturn(List.of(m89));

        service.advanceStageIfComplete("R32");

        assertThat(m89.getHomeTeam()).isEqualTo(t1);  // winner of m75
        assertThat(m89.getAwayTeam()).isEqualTo(t4);  // winner of m78
        assertThat(m89.getPredictionDeadline()).isEqualTo(fixedDeadline);
    }

    @Test
    void thirdPlaceMatch_projectsTheLosersOfBothSemifinalPicks() {
        Team t1 = team(1, "T1");
        Team t2 = team(2, "T2");
        Team t3 = team(3, "T3");
        Team t4 = team(4, "T4");
        Match m101 = addMatch(101, SEMI, t1, t2);
        Match m102 = addMatch(102, SEMI, t3, t4);
        addMatch(103, SF, null, null);
        addMatch(104, FINAL, null, null);

        pick(m101, t1); // user thinks T1 beats T2 → T2 plays the third-place match
        pick(m102, t4); // user thinks T4 beats T3 → T3 plays the third-place match

        var projected = service.projectForUser(USER_ID);

        assertThat(projected.get(103).home()).isEqualTo(t2);
        assertThat(projected.get(103).away()).isEqualTo(t3);
        assertThat(projected.get(104).home()).isEqualTo(t1);
        assertThat(projected.get(104).away()).isEqualTo(t4);
    }
}
