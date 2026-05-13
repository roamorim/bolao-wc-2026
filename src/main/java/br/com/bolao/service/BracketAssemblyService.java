package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.GroupResult;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BracketAssemblyService {

    // Copa 2026 R32: matchNumber → {homeSlot, awaySlot}
    // Slots: "1X" = 1st place of group X, "2X" = 2nd place, "T3-N" = Nth qualifying 3rd (sorted A→L)
    private static final Map<Integer, String[]> R32_FORMULA = Map.ofEntries(
        Map.entry(73,  new String[]{"1A", "2B"}),
        Map.entry(74,  new String[]{"1B", "2A"}),
        Map.entry(75,  new String[]{"1C", "2D"}),
        Map.entry(76,  new String[]{"1D", "2C"}),
        Map.entry(77,  new String[]{"1E", "2F"}),
        Map.entry(78,  new String[]{"1F", "2E"}),
        Map.entry(79,  new String[]{"1G", "2H"}),
        Map.entry(80,  new String[]{"1H", "2G"}),
        Map.entry(81,  new String[]{"1I", "2J"}),
        Map.entry(82,  new String[]{"1J", "2I"}),
        Map.entry(83,  new String[]{"1K", "2L"}),
        Map.entry(84,  new String[]{"1L", "2K"}),
        Map.entry(85,  new String[]{"T3-1", "T3-2"}),
        Map.entry(86,  new String[]{"T3-3", "T3-4"}),
        Map.entry(87,  new String[]{"T3-5", "T3-6"}),
        Map.entry(88,  new String[]{"T3-7", "T3-8"})
    );

    // Next-stage bracket progression: matchNumber → {homeSourceMatch, awaySourceMatch}
    // Negative values indicate "loser of match N" (used for 3rd-place match 103)
    private static final Map<Integer, int[]> PROGRESSION = Map.ofEntries(
        Map.entry(89,  new int[]{73, 74}),
        Map.entry(90,  new int[]{75, 76}),
        Map.entry(91,  new int[]{77, 78}),
        Map.entry(92,  new int[]{79, 80}),
        Map.entry(93,  new int[]{81, 82}),
        Map.entry(94,  new int[]{83, 84}),
        Map.entry(95,  new int[]{85, 86}),
        Map.entry(96,  new int[]{87, 88}),
        Map.entry(97,  new int[]{89, 90}),
        Map.entry(98,  new int[]{91, 92}),
        Map.entry(99,  new int[]{93, 94}),
        Map.entry(100, new int[]{95, 96}),
        Map.entry(101, new int[]{97, 98}),
        Map.entry(102, new int[]{99, 100}),
        Map.entry(103, new int[]{-101, -102}),  // losers of SEMI
        Map.entry(104, new int[]{101, 102})
    );

    private static final Map<String, String> NEXT_STAGE = Map.of(
        "R32",  "R16",
        "R16",  "QF",
        "QF",   "SEMI",
        "SEMI", "SF_AND_FINAL"
    );

    private final GroupResultRepository groupResultRepository;
    private final MatchRepository matchRepository;

    public BracketAssemblyService(GroupResultRepository groupResultRepository,
                                  MatchRepository matchRepository) {
        this.groupResultRepository = groupResultRepository;
        this.matchRepository = matchRepository;
    }

    public boolean isAllGroupResultsIn() {
        return groupResultRepository.count() == 12;
    }

    public boolean isR32Complete() {
        List<Match> r32 = matchRepository.findByStageCodeWithNullableTeams("R32");
        return r32.size() == 16 && r32.stream()
            .allMatch(m -> m.getHomeTeam() != null && m.getAwayTeam() != null);
    }

    @Transactional
    public void assembleR32() {
        if (!isAllGroupResultsIn()) {
            throw new IllegalStateException("Ainda faltam resultados de grupo para montar o bracket.");
        }
        if (isR32Complete()) {
            throw new IllegalStateException("O bracket do R32 já foi montado.");
        }

        List<GroupResult> results = groupResultRepository.findAllWithTeams();
        Map<String, GroupResult> byGroup = results.stream()
            .collect(Collectors.toMap(GroupResult::getGroupName, r -> r));

        // Build 3rd-place qualifying teams list sorted by group A→L
        List<Team> thirdPlace = List.of("A","B","C","D","E","F","G","H","I","J","K","L").stream()
            .map(byGroup::get)
            .filter(r -> r != null && r.isThirdQualifies() && r.getThirdTeam() != null)
            .map(GroupResult::getThirdTeam)
            .toList();

        Map<Integer, Match> matchByNumber = matchRepository.findByStageCodeWithNullableTeams("R32")
            .stream().collect(Collectors.toMap(Match::getMatchNumber, m -> m));

        // Find the earliest R32 match datetime to use as prediction deadline
        var deadlineMatch = matchByNumber.get(73);

        for (var entry : R32_FORMULA.entrySet()) {
            int matchNum = entry.getKey();
            String[] slots = entry.getValue();
            Match match = matchByNumber.get(matchNum);
            if (match == null) continue;

            match.setHomeTeam(resolveSlot(slots[0], byGroup, thirdPlace));
            match.setAwayTeam(resolveSlot(slots[1], byGroup, thirdPlace));
            if (deadlineMatch != null) {
                match.setPredictionDeadline(deadlineMatch.getMatchDatetime());
            }
        }
    }

    @Transactional
    public void advanceStageIfComplete(String stageCode) {
        List<Match> stageMatches = matchRepository.findByStageCodeWithNullableTeams(stageCode);
        boolean allFinished = !stageMatches.isEmpty()
            && stageMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);
        if (!allFinished) return;

        if ("SEMI".equals(stageCode)) {
            advanceFromSemi(stageMatches);
        } else {
            String nextStage = NEXT_STAGE.get(stageCode);
            if (nextStage == null) return;
            advanceWinners(stageMatches, nextStage);
        }
    }

    private void advanceWinners(List<Match> finishedMatches, String nextStageCode) {
        Map<Integer, Match> byNumber = finishedMatches.stream()
            .collect(Collectors.toMap(Match::getMatchNumber, m -> m));

        List<Match> nextMatches = matchRepository.findByStageCodeWithNullableTeams(nextStageCode);
        Map<Integer, Match> nextByNumber = nextMatches.stream()
            .collect(Collectors.toMap(Match::getMatchNumber, m -> m));

        // find earliest datetime in next stage for deadline
        var deadlineInstant = nextMatches.stream()
            .map(Match::getMatchDatetime)
            .min(java.time.Instant::compareTo)
            .orElse(null);

        for (var entry : PROGRESSION.entrySet()) {
            int targetMatchNum = entry.getKey();
            int[] sources = entry.getValue();
            Match target = nextByNumber.get(targetMatchNum);
            if (target == null) continue;

            Match homeSource = byNumber.get(Math.abs(sources[0]));
            Match awaySource = byNumber.get(Math.abs(sources[1]));
            if (homeSource == null || awaySource == null) continue;

            target.setHomeTeam(winner(homeSource));
            target.setAwayTeam(winner(awaySource));
            if (deadlineInstant != null) {
                target.setPredictionDeadline(deadlineInstant);
            }
        }
    }

    private void advanceFromSemi(List<Match> semiMatches) {
        Map<Integer, Match> byNumber = semiMatches.stream()
            .collect(Collectors.toMap(Match::getMatchNumber, m -> m));

        List<Match> sfAndFinal = matchRepository.findByStageCodeWithNullableTeams("SF");
        sfAndFinal.addAll(matchRepository.findByStageCodeWithNullableTeams("FINAL"));
        Map<Integer, Match> targetByNumber = sfAndFinal.stream()
            .collect(Collectors.toMap(Match::getMatchNumber, m -> m));

        for (var entry : PROGRESSION.entrySet()) {
            int targetMatchNum = entry.getKey();
            int[] sources = entry.getValue();
            Match target = targetByNumber.get(targetMatchNum);
            if (target == null) continue;

            int homeSourceNum = Math.abs(sources[0]);
            int awaySourceNum = Math.abs(sources[1]);
            Match homeSource = byNumber.get(homeSourceNum);
            Match awaySource = byNumber.get(awaySourceNum);
            if (homeSource == null || awaySource == null) continue;

            boolean homeIsLoser = sources[0] < 0;
            boolean awayIsLoser = sources[1] < 0;
            target.setHomeTeam(homeIsLoser ? loser(homeSource) : winner(homeSource));
            target.setAwayTeam(awayIsLoser ? loser(awaySource) : winner(awaySource));
        }
    }

    private Team resolveSlot(String slot, Map<String, GroupResult> byGroup, List<Team> thirdPlace) {
        if (slot.startsWith("T3-")) {
            int idx = Integer.parseInt(slot.substring(3)) - 1;
            return idx < thirdPlace.size() ? thirdPlace.get(idx) : null;
        }
        String pos = slot.substring(0, 1);
        String group = slot.substring(1);
        GroupResult r = byGroup.get(group);
        if (r == null) return null;
        return switch (pos) {
            case "1" -> r.getFirstTeam();
            case "2" -> r.getSecondTeam();
            default -> null;
        };
    }

    private Team winner(Match match) {
        if (match.getHomeScore() == null || match.getAwayScore() == null) return null;
        return match.getHomeScore() > match.getAwayScore() ? match.getHomeTeam() : match.getAwayTeam();
    }

    private Team loser(Match match) {
        if (match.getHomeScore() == null || match.getAwayScore() == null) return null;
        return match.getHomeScore() > match.getAwayScore() ? match.getAwayTeam() : match.getHomeTeam();
    }
}
