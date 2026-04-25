package br.com.bolao.service;

import br.com.bolao.domain.enums.ScoringKey;
import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final ScoringConfigRepository scoringConfigRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final GroupClassificationPredictionRepository groupClassificationPredictionRepository;
    private final SemifinalistsPredictionRepository semifinalistsPredictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;

    public ScoringService(
            ScoringConfigRepository scoringConfigRepository,
            MatchPredictionRepository matchPredictionRepository,
            GroupClassificationPredictionRepository groupClassificationPredictionRepository,
            SemifinalistsPredictionRepository semifinalistsPredictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository) {
        this.scoringConfigRepository = scoringConfigRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.groupClassificationPredictionRepository = groupClassificationPredictionRepository;
        this.semifinalistsPredictionRepository = semifinalistsPredictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
    }

    @Transactional
    public void calculateMatchPredictions(Match match) {
        Map<ScoringKey, Integer> config = loadConfig();
        boolean isKnockout = !match.getStage().isGroupStage();

        List<MatchPrediction> predictions = matchPredictionRepository.findByMatchId(match.getId());
        predictions.forEach(p -> p.setPointsEarned(
            computeMatchPoints(
                match.getHomeScore(), match.getAwayScore(),
                p.getHomeScorePred(), p.getAwayScorePred(),
                isKnockout, config
            )
        ));
    }

    @Transactional
    public void calculateGroupClassification(String groupName, Long firstPlaceTeamId, Long secondPlaceTeamId) {
        int pointsPerTeam = loadConfig().get(ScoringKey.GROUP_CLASSIFICATION_CORRECT_PER_TEAM);

        List<GroupClassificationPrediction> predictions =
            groupClassificationPredictionRepository.findByGroupName(groupName);

        predictions.forEach(p -> {
            int points = 0;
            if (p.getFirstPlaceTeam().getId().equals(firstPlaceTeamId)) points += pointsPerTeam;
            if (p.getSecondPlaceTeam().getId().equals(secondPlaceTeamId)) points += pointsPerTeam;
            p.setPointsEarned(points);
        });
    }

    @Transactional
    public void calculateSemifinalists(List<Long> actualSemifinalistIds) {
        int pointsPerTeam = loadConfig().get(ScoringKey.SEMIFINALISTS_CORRECT_PER_TEAM);

        semifinalistsPredictionRepository.findAll().forEach(p -> {
            long correct = List.of(p.getTeam1().getId(), p.getTeam2().getId(),
                                   p.getTeam3().getId(), p.getTeam4().getId())
                .stream()
                .filter(actualSemifinalistIds::contains)
                .count();
            p.setPointsEarned((int) correct * pointsPerTeam);
        });
    }

    @Transactional
    public void calculateTopScorer(String actualScorerName) {
        int points = loadConfig().get(ScoringKey.TOP_SCORER_CORRECT);
        String normalized = actualScorerName.trim().toLowerCase();

        topScorerPredictionRepository.findAll().forEach(p -> {
            boolean correct = p.getPlayerName().trim().toLowerCase().equals(normalized);
            p.setPointsEarned(correct ? points : 0);
        });
    }

    @Transactional
    public void recalculateAllMatchPredictions(List<Match> finishedMatches) {
        Map<ScoringKey, Integer> config = loadConfig();
        for (Match match : finishedMatches) {
            boolean isKnockout = !match.getStage().isGroupStage();
            matchPredictionRepository.findByMatchId(match.getId()).forEach(p ->
                p.setPointsEarned(computeMatchPoints(
                    match.getHomeScore(), match.getAwayScore(),
                    p.getHomeScorePred(), p.getAwayScorePred(),
                    isKnockout, config
                ))
            );
        }
    }

    // Pure function — no I/O, easily unit-testable
    int computeMatchPoints(
            int actualHome, int actualAway,
            int predHome, int predAway,
            boolean isKnockout,
            Map<ScoringKey, Integer> config) {

        boolean exactScore = predHome == actualHome && predAway == actualAway;
        if (exactScore) {
            return isKnockout
                ? config.get(ScoringKey.KNOCKOUT_EXACT_SCORE)
                : config.get(ScoringKey.GROUP_EXACT_SCORE);
        }

        int actualDiff = actualHome - actualAway;
        int predDiff   = predHome   - predAway;
        boolean correctResult = Integer.signum(actualDiff) == Integer.signum(predDiff);

        if (!correctResult) return 0;

        boolean isDraw = actualDiff == 0;
        if (isDraw && !isKnockout) {
            return config.get(ScoringKey.GROUP_CORRECT_DRAW);
        }

        if (actualDiff == predDiff) {
            return isKnockout
                ? config.get(ScoringKey.KNOCKOUT_CORRECT_WINNER_AND_DIFF)
                : config.get(ScoringKey.GROUP_CORRECT_WINNER_AND_DIFF);
        }

        return isKnockout
            ? config.get(ScoringKey.KNOCKOUT_CORRECT_WINNER)
            : config.get(ScoringKey.GROUP_CORRECT_WINNER);
    }

    private Map<ScoringKey, Integer> loadConfig() {
        return scoringConfigRepository.findAll().stream()
            .collect(Collectors.toMap(
                c -> ScoringKey.valueOf(c.getConfigKey()),
                ScoringConfig::getPoints
            ));
    }
}
