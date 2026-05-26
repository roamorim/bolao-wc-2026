package br.com.bolao.service;

import br.com.bolao.domain.enums.MatchStatus;
import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.model.TournamentStage;
import br.com.bolao.domain.model.User;
import br.com.bolao.domain.repository.GroupClassificationPredictionRepository;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.domain.repository.TopScorerPredictionRepository;
import br.com.bolao.domain.repository.UserRepository;
import br.com.bolao.web.dto.request.CreateUserRequest;
import br.com.bolao.web.dto.request.MatchResultRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private static final Instant FAR_FUTURE = Instant.parse("2026-12-31T23:59:59Z");

    private static final List<String> PLAYER_NAMES = List.of(
        "Vinicius Jr.", "Kylian Mbappé", "Lionel Messi", "Harry Kane",
        "Lautaro Martínez", "Julián Álvarez", "Cristiano Ronaldo",
        "Bruno Fernandes", "Lamine Yamal", "Pedri", "Jude Bellingham",
        "Bukayo Saka", "Florian Wirtz", "Jamal Musiala", "Rodrygo"
    );

    // Knockout stage order for deriving previous stage
    private static final List<String> KNOCKOUT_ORDER =
        List.of("R32", "R16", "QF", "SEMI", "SF", "FINAL");

    private final Random random = new Random();

    private final UserService userService;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final GroupClassificationPredictionRepository groupClassRepo;
    private final TopScorerPredictionRepository topScorerRepo;
    private final GroupResultRepository groupResultRepository;
    private final TeamRepository teamRepository;
    private final PredictionService predictionService;
    private final TournamentAdminService tournamentAdminService;

    public SimulationService(
            UserService userService,
            UserRepository userRepository,
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            GroupClassificationPredictionRepository groupClassRepo,
            TopScorerPredictionRepository topScorerRepo,
            GroupResultRepository groupResultRepository,
            TeamRepository teamRepository,
            PredictionService predictionService,
            TournamentAdminService tournamentAdminService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.groupClassRepo = groupClassRepo;
        this.topScorerRepo = topScorerRepo;
        this.groupResultRepository = groupResultRepository;
        this.teamRepository = teamRepository;
        this.predictionService = predictionService;
        this.tournamentAdminService = tournamentAdminService;
    }

    public record KnockoutStageStatus(String code, String name, int displayOrder,
                                      long finished, long total, boolean teamsAssigned) {
        public boolean isComplete() { return total > 0 && finished >= total; }
        public boolean isPending()  { return finished == 0 && !teamsAssigned; }
    }

    public record SimulationStatus(
        long simUserCount,
        long finishedMatches,
        long totalGroupMatches,
        List<KnockoutStageStatus> knockoutStages
    ) {
        public boolean groupStageComplete() { return finishedMatches >= totalGroupMatches && totalGroupMatches > 0; }
        public boolean hasProgress() {
            return finishedMatches > 0 || knockoutStages.stream().anyMatch(KnockoutStageStatus::teamsAssigned);
        }
    }

    @Transactional(readOnly = true)
    public SimulationStatus getStatus() {
        long simUsers = userRepository.findByUsernameStartingWith("sim_").size();

        List<Match> groupMatches = matchRepository.findByStageCode("GROUP");
        long finished = groupMatches.stream().filter(m -> m.getStatus() == MatchStatus.FINISHED).count();

        List<Match> knockoutMatches = matchRepository.findAllKnockoutMatchesWithStage();
        List<KnockoutStageStatus> knockoutStages = buildKnockoutStageStatus(knockoutMatches);

        return new SimulationStatus(simUsers, finished, groupMatches.size(), knockoutStages);
    }

    private List<KnockoutStageStatus> buildKnockoutStageStatus(List<Match> knockoutMatches) {
        Map<String, List<Match>> byStage = knockoutMatches.stream()
            .collect(Collectors.groupingBy(m -> m.getStage().getCode(), LinkedHashMap::new, Collectors.toList()));

        return byStage.entrySet().stream()
            .map(e -> {
                List<Match> ms = e.getValue();
                TournamentStage stage = ms.get(0).getStage();
                long fin = ms.stream().filter(m -> m.getStatus() == MatchStatus.FINISHED).count();
                boolean teamsAssigned = ms.stream().allMatch(m -> m.getHomeTeam() != null);
                return new KnockoutStageStatus(
                    stage.getCode(), stage.getName(), stage.getDisplayOrder(),
                    fin, ms.size(), teamsAssigned);
            })
            .sorted(Comparator.comparingInt(KnockoutStageStatus::displayOrder))
            .collect(Collectors.toList());
    }

    @Transactional
    public int seedUsers(int count) {
        int created = 0;
        for (int i = 1; i <= count; i++) {
            String username = String.format("sim_%02d", i);
            if (!userRepository.existsByUsername(username)) {
                CreateUserRequest req = new CreateUserRequest();
                req.setUsername(username);
                req.setDisplayName("Participante " + String.format("%02d", i));
                req.setEmail(username + "@simulacao.test");
                req.setPassword("senha123");
                userService.createUser(req);
                created++;
            }
        }
        return created;
    }

    @Transactional
    public int seedPredictions() {
        List<User> simUsers = userRepository.findByUsernameStartingWith("sim_");
        if (simUsers.isEmpty()) return 0;

        List<Match> groupMatches = matchRepository.findByStageCode("GROUP");
        List<Team> allTeams = teamRepository.findAllByOrderByGroupNameAscNameAsc();

        Map<String, List<Team>> teamsByGroup = allTeams.stream()
            .filter(t -> t.getGroupName() != null)
            .collect(Collectors.groupingBy(Team::getGroupName));

        for (User user : simUsers) {
            // Match predictions — only for open matches (not yet started)
            for (Match match : groupMatches) {
                if (match.isOpen()) {
                    MatchPrediction pred = matchPredictionRepository
                        .findByUserAndMatch(user, match)
                        .orElseGet(() -> {
                            MatchPrediction p = new MatchPrediction();
                            p.setUser(user);
                            p.setMatch(match);
                            return p;
                        });
                    pred.setHomeScorePred(randomScore());
                    pred.setAwayScorePred(randomScore());
                    pred.setSubmittedAt(Instant.now());
                    pred.setPointsEarned(null);
                    matchPredictionRepository.save(pred);
                }
            }

            // Group classification (1st, 2nd, 3rd per group A–L)
            for (Map.Entry<String, List<Team>> entry : teamsByGroup.entrySet()) {
                List<Team> teams = new ArrayList<>(entry.getValue());
                Collections.shuffle(teams, random);
                Long thirdId = teams.size() > 2 ? teams.get(2).getId() : null;
                predictionService.saveGroupClassification(
                    user, entry.getKey(),
                    teams.get(0).getId(), teams.get(1).getId(), thirdId,
                    random.nextBoolean(), FAR_FUTURE);
            }

            // Top scorer
            Team team = allTeams.get(random.nextInt(allTeams.size()));
            String playerName = PLAYER_NAMES.get(random.nextInt(PLAYER_NAMES.size()));
            predictionService.saveTopScorer(user, playerName, team.getId(), FAR_FUTURE);
        }

        return simUsers.size();
    }

    @Transactional
    public int advanceResults(int upToMatchNumber) {
        List<Match> toAdvance = matchRepository.findByStageCode("GROUP").stream()
            .filter(m -> m.getMatchNumber() <= upToMatchNumber)
            .filter(m -> m.getStatus() != MatchStatus.FINISHED)
            .toList();

        for (Match match : toAdvance) {
            tournamentAdminService.submitMatchResult(
                match.getId(), new MatchResultRequest(randomScore(), randomScore()));
        }
        return toAdvance.size();
    }

    @Transactional
    public int advanceKnockoutStage(String stageCode) {
        List<Match> stageMatches = matchRepository.findByStageCodeWithNullableTeams(stageCode);
        if (stageMatches.isEmpty()) return 0;

        // Assign teams if any match is missing them
        if (stageMatches.stream().anyMatch(m -> m.getHomeTeam() == null)) {
            assignTeamsToStage(stageCode, stageMatches);
            stageMatches = matchRepository.findByStageCodeWithNullableTeams(stageCode);
        }

        List<User> simUsers = userRepository.findByUsernameStartingWith("sim_");
        int advanced = 0;

        for (Match match : stageMatches) {
            if (match.getStatus() == MatchStatus.FINISHED) continue;
            if (match.getHomeTeam() == null || match.getAwayTeam() == null) continue;

            // Seed predictions for sim users
            for (User user : simUsers) {
                MatchPrediction pred = matchPredictionRepository
                    .findByUserAndMatch(user, match)
                    .orElseGet(() -> {
                        MatchPrediction p = new MatchPrediction();
                        p.setUser(user);
                        p.setMatch(match);
                        return p;
                    });
                pred.setHomeScorePred(randomScore());
                pred.setAwayScorePred(randomScore());
                pred.setSubmittedAt(Instant.now());
                pred.setPointsEarned(null);
                matchPredictionRepository.save(pred);
            }

            // Knockout scores must not be draws
            int[] score = randomKnockoutScore();
            tournamentAdminService.submitMatchResult(
                match.getId(), new MatchResultRequest(score[0], score[1]));
            advanced++;
        }

        return advanced;
    }

    private void assignTeamsToStage(String stageCode, List<Match> currentMatches) {
        List<Team> teams;
        if ("R32".equals(stageCode)) {
            List<Team> all = new ArrayList<>(teamRepository.findAll());
            Collections.shuffle(all, random);
            teams = all.subList(0, Math.min(32, all.size()));
        } else if ("SF".equals(stageCode)) {
            // 3rd-place match: losers from the semi-finals
            teams = getLosersFromStage("SEMI");
        } else {
            String prev = prevStageCode(stageCode);
            teams = getWinnersFromStage(prev);
        }

        List<Match> unassigned = currentMatches.stream()
            .filter(m -> m.getHomeTeam() == null)
            .sorted(Comparator.comparingInt(Match::getMatchNumber))
            .collect(Collectors.toList());

        for (int i = 0; i < unassigned.size(); i++) {
            int homeIdx = i * 2;
            int awayIdx = i * 2 + 1;
            if (awayIdx >= teams.size()) break;
            Match m = unassigned.get(i);
            m.setHomeTeam(teams.get(homeIdx));
            m.setAwayTeam(teams.get(awayIdx));
            matchRepository.save(m);
        }
    }

    private List<Team> getWinnersFromStage(String stageCode) {
        return matchRepository.findByStageCodeWithNullableTeams(stageCode).stream()
            .filter(Match::isFinished)
            .sorted(Comparator.comparingInt(Match::getMatchNumber))
            .map(this::getWinner)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<Team> getLosersFromStage(String stageCode) {
        return matchRepository.findByStageCodeWithNullableTeams(stageCode).stream()
            .filter(Match::isFinished)
            .sorted(Comparator.comparingInt(Match::getMatchNumber))
            .map(this::getLoser)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Team getWinner(Match m) {
        if (m.getHomeScore() == null || m.getAwayScore() == null) return null;
        if (m.getHomeScore() > m.getAwayScore()) return m.getHomeTeam();
        if (m.getAwayScore() > m.getHomeScore()) return m.getAwayTeam();
        return random.nextBoolean() ? m.getHomeTeam() : m.getAwayTeam();
    }

    private Team getLoser(Match m) {
        if (m.getHomeScore() == null || m.getAwayScore() == null) return null;
        if (m.getHomeScore() > m.getAwayScore()) return m.getAwayTeam();
        if (m.getAwayScore() > m.getHomeScore()) return m.getHomeTeam();
        return random.nextBoolean() ? m.getHomeTeam() : m.getAwayTeam();
    }

    private String prevStageCode(String code) {
        int idx = KNOCKOUT_ORDER.indexOf(code);
        if (idx <= 0) throw new IllegalArgumentException("No previous stage for: " + code);
        return KNOCKOUT_ORDER.get(idx - 1);
    }

    @Transactional
    public void resetResults() {
        matchRepository.resetAllResults();
        matchRepository.clearKnockoutTeams();
        matchPredictionRepository.clearAllPointsEarned();
        groupClassRepo.clearAllPointsEarned();
        topScorerRepo.clearAllPointsEarned();
        groupResultRepository.deleteAll();
    }

    @Transactional
    public int clearSimulationData() {
        List<User> simUsers = userRepository.findByUsernameStartingWith("sim_");
        simUsers.forEach(u -> userService.deleteUser(u.getId()));
        return simUsers.size();
    }

    private int randomScore() {
        int roll = random.nextInt(10);
        return roll < 3 ? 0 : roll < 6 ? 1 : roll < 8 ? 2 : 3;
    }

    private int[] randomKnockoutScore() {
        int home = randomScore();
        int away = randomScore();
        if (home == away) {
            if (random.nextBoolean()) home++; else away++;
        }
        return new int[]{home, away};
    }

}
