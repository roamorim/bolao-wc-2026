package br.com.bolao.web.dto.response;

import java.time.Instant;
import java.util.List;

public record UserAuditDto(
    Long userId,
    String username,
    String displayName,
    List<MatchPredRow> matchPredictions,
    List<GroupClassRow> groupClassifications,
    TopScorerRow topScorer,
    int matchPointsTotal,
    int groupPointsTotal,
    int topScorerPoints
) {
    public int totalPoints() {
        return matchPointsTotal + groupPointsTotal + topScorerPoints;
    }

    public record MatchPredRow(
        int matchNumber,
        String homeTeam,
        String homeTeamFlag,
        String awayTeam,
        String awayTeamFlag,
        Instant matchDatetime,
        String stageName,
        int homeScorePred,
        int awayScorePred,
        Integer homeScoreActual,
        Integer awayScoreActual,
        boolean finished,
        Integer pointsEarned
    ) {}

    public record GroupClassRow(
        String groupName,
        String predictedFirst,
        String predictedSecond,
        String predictedThird,
        boolean predictedThirdQualifies,
        String actualFirst,
        String actualSecond,
        String actualThird,
        boolean actualThirdQualifies,
        boolean resultRecorded,
        Integer pointsEarned
    ) {}

    public record TopScorerRow(
        String playerName,
        String teamName,
        Integer pointsEarned
    ) {}
}
