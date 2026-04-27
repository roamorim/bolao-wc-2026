package br.com.bolao.service;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.*;
import br.com.bolao.web.dto.request.MatchPredictionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PredictionService {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final TeamRepository teamRepository;
    private final GroupClassificationPredictionRepository groupClassificationPredictionRepository;
    private final SemifinalistsPredictionRepository semifinalistsPredictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;

    public PredictionService(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            TeamRepository teamRepository,
            GroupClassificationPredictionRepository groupClassificationPredictionRepository,
            SemifinalistsPredictionRepository semifinalistsPredictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.teamRepository = teamRepository;
        this.groupClassificationPredictionRepository = groupClassificationPredictionRepository;
        this.semifinalistsPredictionRepository = semifinalistsPredictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
    }

    @Transactional
    public MatchPrediction saveMatchPrediction(User user, Long matchId, MatchPredictionRequest request) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + matchId));

        if (!match.isOpen()) {
            throw new PredictionClosedException("Apostas encerradas para este jogo.");
        }

        MatchPrediction prediction = matchPredictionRepository
            .findByUserAndMatch(user, match)
            .orElseGet(() -> {
                MatchPrediction p = new MatchPrediction();
                p.setUser(user);
                p.setMatch(match);
                return p;
            });

        prediction.setHomeScorePred(request.homeScore());
        prediction.setAwayScorePred(request.awayScore());
        prediction.setSubmittedAt(Instant.now());
        prediction.setPointsEarned(null);

        return matchPredictionRepository.save(prediction);
    }

    @Transactional
    public GroupClassificationPrediction saveGroupClassification(
            User user, String groupName,
            Long firstTeamId, Long secondTeamId, Long thirdTeamId, boolean thirdQualifies,
            Instant deadline) {

        if (Instant.now().isAfter(deadline)) {
            throw new PredictionClosedException("Prazo para apostas de classificação do grupo " + groupName + " encerrado.");
        }

        long distinctCount = java.util.stream.Stream.of(firstTeamId, secondTeamId, thirdTeamId)
            .filter(java.util.Objects::nonNull).distinct().count();
        long nonNullCount  = java.util.stream.Stream.of(firstTeamId, secondTeamId, thirdTeamId)
            .filter(java.util.Objects::nonNull).count();
        if (distinctCount < nonNullCount) {
            throw new IllegalArgumentException("1º, 2º e 3º lugar devem ser seleções diferentes.");
        }

        Team first  = teamRepository.findById(firstTeamId).orElseThrow();
        Team second = teamRepository.findById(secondTeamId).orElseThrow();
        Team third  = thirdTeamId != null ? teamRepository.findById(thirdTeamId).orElseThrow() : null;

        GroupClassificationPrediction prediction = groupClassificationPredictionRepository
            .findByUserIdAndGroupName(user.getId(), groupName)
            .orElseGet(() -> {
                GroupClassificationPrediction p = new GroupClassificationPrediction();
                p.setUser(user);
                p.setGroupName(groupName);
                p.setDeadline(deadline);
                return p;
            });

        prediction.setFirstPlaceTeam(first);
        prediction.setSecondPlaceTeam(second);
        prediction.setThirdPlaceTeam(third);
        prediction.setThirdQualifies(thirdQualifies);
        prediction.setSubmittedAt(Instant.now());
        prediction.setPointsEarned(null);

        return groupClassificationPredictionRepository.save(prediction);
    }

    @Transactional
    public SemifinalistsPrediction saveSemifinalists(
            User user, Long t1, Long t2, Long t3, Long t4, Instant deadline) {

        if (Instant.now().isAfter(deadline)) {
            throw new PredictionClosedException("Prazo para aposta dos semifinalistas encerrado.");
        }

        Team team1 = teamRepository.findById(t1).orElseThrow();
        Team team2 = teamRepository.findById(t2).orElseThrow();
        Team team3 = teamRepository.findById(t3).orElseThrow();
        Team team4 = teamRepository.findById(t4).orElseThrow();

        SemifinalistsPrediction prediction = semifinalistsPredictionRepository
            .findByUserId(user.getId())
            .orElseGet(() -> {
                SemifinalistsPrediction p = new SemifinalistsPrediction();
                p.setUser(user);
                p.setDeadline(deadline);
                return p;
            });

        prediction.setTeam1(team1);
        prediction.setTeam2(team2);
        prediction.setTeam3(team3);
        prediction.setTeam4(team4);
        prediction.setSubmittedAt(Instant.now());
        prediction.setPointsEarned(null);

        return semifinalistsPredictionRepository.save(prediction);
    }

    @Transactional
    public TopScorerPrediction saveTopScorer(User user, String playerName, Long teamId, Instant deadline) {
        if (Instant.now().isAfter(deadline)) {
            throw new PredictionClosedException("Prazo para aposta do artilheiro encerrado.");
        }

        Team team = teamRepository.findById(teamId).orElseThrow();

        TopScorerPrediction prediction = topScorerPredictionRepository
            .findByUserId(user.getId())
            .orElseGet(() -> {
                TopScorerPrediction p = new TopScorerPrediction();
                p.setUser(user);
                p.setDeadline(deadline);
                return p;
            });

        prediction.setPlayerName(playerName.trim());
        prediction.setTeam(team);
        prediction.setSubmittedAt(Instant.now());
        prediction.setPointsEarned(null);

        return topScorerPredictionRepository.save(prediction);
    }

    public static class PredictionClosedException extends RuntimeException {
        public PredictionClosedException(String message) {
            super(message);
        }
    }
}
