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

        apiMatches.stream()
                .filter(FdMatch::isFinished)
                .filter(api -> homeTla.equalsIgnoreCase(tla(api.homeTeam()))
                        && awayTla.equalsIgnoreCase(tla(api.awayTeam())))
                .findFirst()
                .ifPresentOrElse(
                        api -> applyResult(match, api),
                        () -> log.debug("No finished result yet for match #{} ({} vs {})",
                                match.getMatchNumber(), homeTla, awayTla));
    }

    private void applyResult(Match match, FdMatch api) {
        if (api.score() == null || api.score().fullTime() == null
                || api.score().fullTime().home() == null || api.score().fullTime().away() == null) {
            log.warn("Match #{} reported FINISHED but score is incomplete in API response", match.getMatchNumber());
            return;
        }

        int home = api.score().fullTime().home();
        int away = api.score().fullTime().away();

        try {
            tournamentAdminService.submitMatchResult(match.getId(), new MatchResultRequest(home, away));
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
