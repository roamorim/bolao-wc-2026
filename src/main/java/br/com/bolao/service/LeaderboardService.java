package br.com.bolao.service;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.*;
import br.com.bolao.web.dto.response.LeaderboardEntryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final GroupClassificationPredictionRepository groupClassificationPredictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final BracketPickRepository bracketPickRepository;

    @Value("${bolao.ranking.self-alias.enabled:false}")
    private boolean selfAliasEnabled;

    @Value("${bolao.ranking.self-alias.name:Rato}")
    private String selfAliasName;

    public LeaderboardService(
            UserRepository userRepository,
            MatchPredictionRepository matchPredictionRepository,
            GroupClassificationPredictionRepository groupClassificationPredictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository,
            BracketPickRepository bracketPickRepository) {
        this.userRepository = userRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.groupClassificationPredictionRepository = groupClassificationPredictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
        this.bracketPickRepository = bracketPickRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(Long viewerUserId) {
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.isActive() && u.getRole().name().equals("USER"))
            .toList();

        return users.stream()
            .map(this::toEntry)
            .map(entry -> applySelfAlias(entry, viewerUserId))
            .sorted(Comparator.comparingInt(LeaderboardEntryResponse::totalPoints).reversed())
            .toList();
    }

    private LeaderboardEntryResponse applySelfAlias(LeaderboardEntryResponse entry, Long viewerUserId) {
        if (!selfAliasEnabled || viewerUserId == null || !entry.userId().equals(viewerUserId)) {
            return entry;
        }
        return new LeaderboardEntryResponse(
            entry.userId(),
            selfAliasName,
            entry.totalPoints(),
            entry.matchPoints(),
            entry.groupPoints(),
            entry.topScorerPoints(),
            entry.bracketPoints()
        );
    }

    private LeaderboardEntryResponse toEntry(User user) {
        int matchPoints = matchPredictionRepository.findByUserIdWithMatch(user.getId()).stream()
            .mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .sum();

        int groupPoints = groupClassificationPredictionRepository.findByUserId(user.getId()).stream()
            .mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .sum();

        int topScorerPoints = topScorerPredictionRepository.findByUserId(user.getId())
            .map(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .orElse(0);

        int bracketPoints = bracketPickRepository.findByUserIdWithDetails(user.getId()).stream()
            .mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0)
            .sum();

        int total = matchPoints + groupPoints + topScorerPoints + bracketPoints;

        return new LeaderboardEntryResponse(
            user.getId(),
            user.getDisplayName(),
            total,
            matchPoints,
            groupPoints,
            topScorerPoints,
            bracketPoints
        );
    }
}
