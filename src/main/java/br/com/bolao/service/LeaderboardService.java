package br.com.bolao.service;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.*;
import br.com.bolao.web.dto.response.LeaderboardEntryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final GroupClassificationPredictionRepository groupClassificationPredictionRepository;
    private final SemifinalistsPredictionRepository semifinalistsPredictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;

    public LeaderboardService(
            UserRepository userRepository,
            MatchPredictionRepository matchPredictionRepository,
            GroupClassificationPredictionRepository groupClassificationPredictionRepository,
            SemifinalistsPredictionRepository semifinalistsPredictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository) {
        this.userRepository = userRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.groupClassificationPredictionRepository = groupClassificationPredictionRepository;
        this.semifinalistsPredictionRepository = semifinalistsPredictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.isActive() && u.getRole().name().equals("USER"))
            .toList();

        return users.stream()
            .map(this::toEntry)
            .sorted(Comparator.comparingInt(LeaderboardEntryResponse::totalPoints).reversed())
            .toList();
    }

    private LeaderboardEntryResponse toEntry(User user) {
        int matchPoints = matchPredictionRepository.findByUserIdWithMatch(user.getId()).stream()
            .mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .sum();

        int groupPoints = groupClassificationPredictionRepository.findByUserId(user.getId()).stream()
            .mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .sum();

        int semifinalistsPoints = semifinalistsPredictionRepository.findByUserId(user.getId())
            .map(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .orElse(0);

        int topScorerPoints = topScorerPredictionRepository.findByUserId(user.getId())
            .map(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .orElse(0);

        int total = matchPoints + groupPoints + semifinalistsPoints + topScorerPoints;

        return new LeaderboardEntryResponse(
            user.getId(),
            user.getDisplayName(),
            total,
            matchPoints,
            groupPoints,
            semifinalistsPoints,
            topScorerPoints
        );
    }
}
