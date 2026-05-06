package br.com.bolao.service;

import br.com.bolao.domain.repository.GroupClassificationPredictionRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.SemifinalistsPredictionRepository;
import br.com.bolao.domain.repository.TopScorerPredictionRepository;
import br.com.bolao.web.dto.response.UserPicksStatusDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AdminPicksService {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final GroupClassificationPredictionRepository groupClassRepo;
    private final SemifinalistsPredictionRepository semifinalistsRepo;
    private final TopScorerPredictionRepository topScorerRepo;

    public AdminPicksService(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            GroupClassificationPredictionRepository groupClassRepo,
            SemifinalistsPredictionRepository semifinalistsRepo,
            TopScorerPredictionRepository topScorerRepo) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.groupClassRepo = groupClassRepo;
        this.semifinalistsRepo = semifinalistsRepo;
        this.topScorerRepo = topScorerRepo;
    }

    @Transactional(readOnly = true)
    public long getTotalGroupMatches() {
        return matchRepository.countGroupStageMatches();
    }

    @Transactional(readOnly = true)
    public Map<Long, UserPicksStatusDto> getPicksStatuses() {
        long totalGroupMatches = matchRepository.countGroupStageMatches();

        Map<Long, Long> matchCountByUser = new HashMap<>();
        for (Object[] row : matchPredictionRepository.countGroupStagePredictionsPerUser()) {
            matchCountByUser.put((Long) row[0], (Long) row[1]);
        }

        Map<Long, Long> groupCountByUser = new HashMap<>();
        for (Object[] row : groupClassRepo.countPredictionsPerUser()) {
            groupCountByUser.put((Long) row[0], (Long) row[1]);
        }

        Set<Long> semifinalistsUserIds = new HashSet<>(semifinalistsRepo.findAllUserIds());
        Set<Long> topScorerUserIds = new HashSet<>(topScorerRepo.findAllUserIds());

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(matchCountByUser.keySet());
        allUserIds.addAll(groupCountByUser.keySet());
        allUserIds.addAll(semifinalistsUserIds);
        allUserIds.addAll(topScorerUserIds);

        Map<Long, UserPicksStatusDto> result = new HashMap<>();
        for (Long userId : allUserIds) {
            result.put(userId, new UserPicksStatusDto(
                matchCountByUser.getOrDefault(userId, 0L),
                totalGroupMatches,
                groupCountByUser.getOrDefault(userId, 0L),
                semifinalistsUserIds.contains(userId),
                topScorerUserIds.contains(userId)
            ));
        }
        return result;
    }
}
