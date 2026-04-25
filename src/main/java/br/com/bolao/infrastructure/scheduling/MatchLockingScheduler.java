package br.com.bolao.infrastructure.scheduling;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class MatchLockingScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchLockingScheduler.class);

    private final MatchRepository matchRepository;

    public MatchLockingScheduler(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    // Runs every minute, locks matches past their prediction deadline
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void lockPastDeadlineMatches() {
        List<Match> toLock = matchRepository.findByStatusAndPredictionDeadlineBefore(
            MatchStatus.SCHEDULED, Instant.now()
        );

        if (!toList(toLock).isEmpty()) {
            toLock.forEach(m -> m.setStatus(MatchStatus.LOCKED));
            log.info("Locked {} match(es) past deadline", toLock.size());
        }
    }

    private static <T> List<T> toList(List<T> list) {
        return list;
    }
}
