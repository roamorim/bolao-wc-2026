package br.com.bolao.infrastructure.scheduling;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.infrastructure.external.FootballDataClient;
import br.com.bolao.infrastructure.external.FootballDataClient.FdMatch;
import br.com.bolao.service.TournamentAdminService;
import br.com.bolao.web.dto.request.MatchResultRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MatchResultFetcherScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchResultFetcherScheduler.class);

    private final MatchRepository matchRepository;
    private final FootballDataClient footballDataClient;
    private final TournamentAdminService tournamentAdminService;
    private final int matchEndOffsetMinutes;

    public MatchResultFetcherScheduler(
            MatchRepository matchRepository,
            FootballDataClient footballDataClient,
            TournamentAdminService tournamentAdminService,
            @Value("${football-data.match-end-offset-minutes:110}") int matchEndOffsetMinutes) {
        this.matchRepository = matchRepository;
        this.footballDataClient = footballDataClient;
        this.tournamentAdminService = tournamentAdminService;
        this.matchEndOffsetMinutes = matchEndOffsetMinutes;
    }

    @Scheduled(fixedDelay = 300_000)
    public void fetchAndApplyResults() {
        Instant cutoff = Instant.now().minusSeconds(matchEndOffsetMinutes * 60L);
        List<Match> candidates = matchRepository.findLockedMatchesPastCutoff(MatchStatus.LOCKED, cutoff);

        if (candidates.isEmpty()) {
            return;
        }

        log.debug("Checking results for {} locked match(es) via football-data.org", candidates.size());

        Map<LocalDate, List<Match>> byDate = candidates.stream()
                .collect(Collectors.groupingBy(m ->
                        m.getMatchDatetime().atZone(ZoneOffset.UTC).toLocalDate()));

        for (Map.Entry<LocalDate, List<Match>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<FdMatch> apiMatches = footballDataClient.fetchMatchesByDate(date);

            for (Match match : entry.getValue()) {
                tryApplyResult(match, apiMatches);
            }
        }
    }

    private void tryApplyResult(Match match, List<FdMatch> apiMatches) {
        String homeTla = match.getHomeTeam().getCode();
        String awayTla = match.getAwayTeam().getCode();

        for (FdMatch api : apiMatches) {
            if (!api.isFinished()) continue;
            String apiHome = tla(api.homeTeam());
            String apiAway = tla(api.awayTeam());

            boolean straight  = homeTla.equalsIgnoreCase(apiHome) && awayTla.equalsIgnoreCase(apiAway);
            boolean flipped   = homeTla.equalsIgnoreCase(apiAway) && awayTla.equalsIgnoreCase(apiHome);

            if (straight || flipped) {
                applyResult(match, api, flipped);
                return;
            }
        }
        log.debug("No finished result yet for match #{} ({} vs {})", match.getMatchNumber(), homeTla, awayTla);
    }

    private void applyResult(Match match, FdMatch api, boolean flipped) {
        var score = api.score();
        if (score == null) {
            log.warn("Match #{} reported FINISHED but score object is null in API response", match.getMatchNumber());
            return;
        }

        // Use regularTime as the field score base — in PENALTY_SHOOTOUT, fullTime includes
        // converted penalty goals and is NOT a field score. Fall back to fullTime only when
        // regularTime is absent (typical for REGULAR-duration matches in this API).
        var base = (score.regularTime() != null
                && score.regularTime().home() != null
                && score.regularTime().away() != null)
                ? score.regularTime()
                : score.fullTime();

        if (base == null || base.home() == null || base.away() == null) {
            log.warn("Match #{}: score incompleto na API", match.getMatchNumber());
            return;
        }

        // extraTime carries only the additional goals scored in ET (not cumulative).
        int etHome = 0, etAway = 0;
        if (score.extraTime() != null
                && score.extraTime().home() != null
                && score.extraTime().away() != null) {
            etHome = score.extraTime().home();
            etAway = score.extraTime().away();
        }

        int rawHome = base.home() + etHome;
        int rawAway = base.away() + etAway;
        int home = flipped ? rawAway : rawHome;
        int away = flipped ? rawHome : rawAway;

        Long penaltyWinnerTeamId = null;
        if (home == away && !match.getStage().isGroupStage()) {
            String winnerSide = score.winner();
            boolean apiHomeWon = "HOME_TEAM".equals(winnerSide);
            boolean apiAwayWon = "AWAY_TEAM".equals(winnerSide);
            if (apiHomeWon || apiAwayWon) {
                boolean ourHomeWon = flipped ? apiAwayWon : apiHomeWon;
                penaltyWinnerTeamId = ourHomeWon ? match.getHomeTeam().getId() : match.getAwayTeam().getId();
            } else {
                log.warn("Match #{}: empatado mas winner não disponível na API — aguardando admin.",
                        match.getMatchNumber());
                return;
            }
        }

        try {
            tournamentAdminService.submitMatchResult(match.getId(), new MatchResultRequest(home, away, penaltyWinnerTeamId));
            log.info("Auto-applied result for match #{}: {} {} x {} {}",
                    match.getMatchNumber(),
                    match.getHomeTeam().getCode(), home,
                    away, match.getAwayTeam().getCode());
        } catch (IllegalStateException e) {
            log.debug("Result already set for match #{}: {}", match.getMatchNumber(), e.getMessage());
        }
    }

    private static String tla(FootballDataClient.FdTeam team) {
        return team != null && team.tla() != null ? team.tla() : "";
    }
}
