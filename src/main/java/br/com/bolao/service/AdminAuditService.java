package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.GroupClassificationPrediction;
import br.com.bolao.domain.model.GroupResult;
import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.repository.GroupClassificationPredictionRepository;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.domain.repository.TopScorerPredictionRepository;
import br.com.bolao.web.dto.response.UserAuditDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAuditService {

    private final UserService userService;
    private final MatchPredictionRepository matchPredictionRepo;
    private final GroupClassificationPredictionRepository groupClassRepo;
    private final GroupResultRepository groupResultRepo;
    private final TopScorerPredictionRepository topScorerRepo;

    public AdminAuditService(
            UserService userService,
            MatchPredictionRepository matchPredictionRepo,
            GroupClassificationPredictionRepository groupClassRepo,
            GroupResultRepository groupResultRepo,
            TopScorerPredictionRepository topScorerRepo) {
        this.userService = userService;
        this.matchPredictionRepo = matchPredictionRepo;
        this.groupClassRepo = groupClassRepo;
        this.groupResultRepo = groupResultRepo;
        this.topScorerRepo = topScorerRepo;
    }

    @Transactional(readOnly = true)
    public UserAuditDto getAudit(Long userId) {
        var user = userService.findById(userId);

        List<MatchPrediction> matchPreds = matchPredictionRepo.findByUserIdWithMatchAndStage(userId);
        List<UserAuditDto.MatchPredRow> matchRows = matchPreds.stream()
            .map(p -> new UserAuditDto.MatchPredRow(
                p.getMatch().getMatchNumber(),
                p.getMatch().getHomeTeam() != null ? p.getMatch().getHomeTeam().getName() : "?",
                p.getMatch().getHomeTeam() != null ? p.getMatch().getHomeTeam().getFlagCode() : null,
                p.getMatch().getAwayTeam() != null ? p.getMatch().getAwayTeam().getName() : "?",
                p.getMatch().getAwayTeam() != null ? p.getMatch().getAwayTeam().getFlagCode() : null,
                p.getMatch().getMatchDatetime(),
                p.getMatch().getStage().getName(),
                p.getHomeScorePred(),
                p.getAwayScorePred(),
                p.getMatch().getHomeScore(),
                p.getMatch().getAwayScore(),
                MatchStatus.FINISHED.equals(p.getMatch().getStatus()),
                p.getPointsEarned()
            ))
            .toList();

        List<GroupClassificationPrediction> groupPreds = groupClassRepo.findByUserIdWithTeams(userId);
        Map<String, GroupResult> groupResultMap = groupResultRepo.findAllWithTeams()
            .stream().collect(Collectors.toMap(GroupResult::getGroupName, gr -> gr));

        List<UserAuditDto.GroupClassRow> groupRows = groupPreds.stream()
            .map(p -> {
                GroupResult result = groupResultMap.get(p.getGroupName());
                return new UserAuditDto.GroupClassRow(
                    p.getGroupName(),
                    p.getFirstPlaceTeam().getName(),
                    p.getSecondPlaceTeam().getName(),
                    p.getThirdPlaceTeam() != null ? p.getThirdPlaceTeam().getName() : null,
                    p.isThirdQualifies(),
                    result != null ? result.getFirstTeam().getName() : null,
                    result != null ? result.getSecondTeam().getName() : null,
                    result != null && result.getThirdTeam() != null ? result.getThirdTeam().getName() : null,
                    result != null && result.isThirdQualifies(),
                    result != null,
                    p.getPointsEarned()
                );
            })
            .toList();

        var topRow = topScorerRepo.findByUserIdWithTeam(userId)
            .map(t -> new UserAuditDto.TopScorerRow(
                t.getPlayerName(), t.getTeam().getName(), t.getPointsEarned()
            ))
            .orElse(null);

        int matchTotal = matchRows.stream()
            .mapToInt(r -> r.pointsEarned() != null ? r.pointsEarned() : 0).sum();
        int groupTotal = groupRows.stream()
            .mapToInt(r -> r.pointsEarned() != null ? r.pointsEarned() : 0).sum();
        int topPts = topRow != null && topRow.pointsEarned() != null ? topRow.pointsEarned() : 0;

        return new UserAuditDto(
            user.getId(), user.getUsername(), user.getDisplayName(),
            matchRows, groupRows, topRow,
            matchTotal, groupTotal, topPts
        );
    }
}
