package br.com.bolao.service;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.*;
import br.com.bolao.web.dto.request.MatchPredictionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PredictionService {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final TeamRepository teamRepository;
    private final GroupClassificationPredictionRepository groupClassificationPredictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final EmailService emailService;

    public PredictionService(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            TeamRepository teamRepository,
            GroupClassificationPredictionRepository groupClassificationPredictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository,
            EmailService emailService) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.teamRepository = teamRepository;
        this.groupClassificationPredictionRepository = groupClassificationPredictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
        this.emailService = emailService;
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

        MatchPrediction saved = matchPredictionRepository.save(prediction);
        long total = matchRepository.countGroupStageMatches();
        long filled = matchPredictionRepository.countGroupStageByUserId(user.getId());
        if (filled == total) {
            List<MatchPrediction> all = matchPredictionRepository.findByUserIdWithMatchAndStage(user.getId())
                .stream().filter(p -> p.getMatch().getStage().isGroupStage()).toList();
            emailService.sendGroupStageSummaryConfirmation(user, all);
        }
        return saved;
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

        var existing = groupClassificationPredictionRepository
            .findByUserIdAndGroupName(user.getId(), groupName);
        boolean isNewGroup = existing.isEmpty();

        GroupClassificationPrediction prediction = existing.orElseGet(() -> {
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

        if (isNewGroup) {
            long newTotal = groupClassificationPredictionRepository.countByUserId(user.getId()) + 1;
            boolean topScorerSaved = topScorerPredictionRepository.findByUserId(user.getId()).isPresent();
            if (newTotal == TOTAL_GROUPS && topScorerSaved) {
                long thirds = groupClassificationPredictionRepository
                    .countByUserIdAndThirdQualifies(user.getId(), true)
                    + (thirdQualifies ? 1 : 0);
                if (thirds != 8) throw new IllegalArgumentException(
                    "Para finalizar, exatamente 8 seleções de 3º lugar devem avançar como melhor terceiro. Você marcou " + thirds + ".");
            }
        }

        GroupClassificationPrediction saved = groupClassificationPredictionRepository.save(prediction);
        if (isNewGroup) checkAndSendSpecialCompletionEmail(user);
        return saved;
    }

    @Transactional
    public TopScorerPrediction saveTopScorer(User user, String playerName, Long teamId, Instant deadline) {
        if (Instant.now().isAfter(deadline)) {
            throw new PredictionClosedException("Prazo para aposta do artilheiro encerrado.");
        }

        Team team = teamRepository.findById(teamId).orElseThrow();

        var existingTopScorer = topScorerPredictionRepository.findByUserId(user.getId());
        boolean isNewTopScorer = existingTopScorer.isEmpty();

        TopScorerPrediction prediction = existingTopScorer.orElseGet(() -> {
            TopScorerPrediction p = new TopScorerPrediction();
            p.setUser(user);
            p.setDeadline(deadline);
            return p;
        });

        prediction.setPlayerName(playerName.trim());
        prediction.setTeam(team);
        prediction.setSubmittedAt(Instant.now());
        prediction.setPointsEarned(null);

        if (isNewTopScorer) {
            long groupCount = groupClassificationPredictionRepository.countByUserId(user.getId());
            if (groupCount == TOTAL_GROUPS) {
                long thirds = groupClassificationPredictionRepository
                    .countByUserIdAndThirdQualifies(user.getId(), true);
                if (thirds != 8) throw new IllegalArgumentException(
                    "Para finalizar, exatamente 8 seleções de 3º lugar devem avançar como melhor terceiro. Você marcou " + thirds + ".");
            }
        }

        TopScorerPrediction saved = topScorerPredictionRepository.save(prediction);
        if (isNewTopScorer) checkAndSendSpecialCompletionEmail(user);
        return saved;
    }

    private static final int TOTAL_GROUPS = 12;

    private void checkAndSendSpecialCompletionEmail(User user) {
        long groupCount = groupClassificationPredictionRepository.countByUserId(user.getId());
        if (groupCount < TOTAL_GROUPS) return;
        boolean hasTopScorer = topScorerPredictionRepository.findByUserId(user.getId()).isPresent();
        if (!hasTopScorer) return;
        emailService.sendSpecialPredictionsCompletionEmail(user);
    }

    public static class PredictionClosedException extends RuntimeException {
        public PredictionClosedException(String message) {
            super(message);
        }
    }
}
