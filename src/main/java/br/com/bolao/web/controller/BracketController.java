package br.com.bolao.web.controller;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.BracketPickRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.service.BracketAssemblyService;
import br.com.bolao.service.EmailService;
import br.com.bolao.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bracket")
public class BracketController {

    private final MatchRepository matchRepository;
    private final BracketPickRepository bracketPickRepository;
    private final BracketAssemblyService bracketAssemblyService;
    private final UserService userService;
    private final EmailService emailService;

    public BracketController(MatchRepository matchRepository,
                              BracketPickRepository bracketPickRepository,
                              BracketAssemblyService bracketAssemblyService,
                              UserService userService,
                              EmailService emailService) {
        this.matchRepository = matchRepository;
        this.bracketPickRepository = bracketPickRepository;
        this.bracketAssemblyService = bracketAssemblyService;
        this.userService = userService;
        this.emailService = emailService;
    }

    public record TeamDto(Long id, String name, String code, String flagCode) {
        static TeamDto from(Team t) {
            return t == null ? null : new TeamDto(t.getId(), t.getName(), t.getCode(), t.getFlagCode());
        }
    }

    public record MatchDto(
        int number, Long matchId, String stageCode, String stageName,
        TeamDto homeTeam, TeamDto awayTeam,
        boolean finished, Integer homeScore, Integer awayScore,
        long matchDatetimeMs, long deadlineMs,
        Long myPickTeamId, Integer pointsEarned
    ) {
        static MatchDto from(Match m, BracketPick pick) {
            return new MatchDto(
                m.getMatchNumber(), m.getId(), m.getStage().getCode(), m.getStage().getName(),
                TeamDto.from(m.getHomeTeam()), TeamDto.from(m.getAwayTeam()),
                m.isFinished(), m.getHomeScore(), m.getAwayScore(),
                m.getMatchDatetime().toEpochMilli(), m.getPredictionDeadline().toEpochMilli(),
                pick != null && pick.getPredictedWinner() != null ? pick.getPredictedWinner().getId() : null,
                pick != null ? pick.getPointsEarned() : null
            );
        }
    }

    public record OtherPickDto(String teamName, Integer pointsEarned) {}
    public record UserBracketDto(String displayName, Map<Integer, OtherPickDto> picks) {}

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.findByUsername(principal.getUsername());
        boolean bracketReady = bracketAssemblyService.isR32Complete();

        List<Match> knockoutMatches = matchRepository.findAllKnockoutMatchesWithStage();
        Map<Long, BracketPick> pickByMatchId = bracketPickRepository
            .findByUserIdWithDetails(user.getId()).stream()
            .collect(Collectors.toMap(p -> p.getMatch().getId(), p -> p));

        List<MatchDto> matches = knockoutMatches.stream()
            .sorted(Comparator.comparingInt(Match::getMatchNumber))
            .map(m -> MatchDto.from(m, pickByMatchId.get(m.getId())))
            .toList();

        boolean deadlinePassed = knockoutMatches.stream()
            .map(Match::getPredictionDeadline)
            .min(Comparator.naturalOrder())
            .map(d -> java.time.Instant.now().isAfter(d))
            .orElse(false);

        model.addAttribute("bracketReady", bracketReady);
        model.addAttribute("matches", matches);
        model.addAttribute("progression", bracketAssemblyService.getProgressionMap());
        model.addAttribute("allPicks", deadlinePassed ? allUsersPicks() : List.of());
        return "bracket";
    }

    private List<UserBracketDto> allUsersPicks() {
        return bracketPickRepository.findAllWithUserAndWinner().stream()
            .collect(Collectors.groupingBy(p -> p.getUser().getDisplayName()))
            .entrySet().stream()
            .map(e -> new UserBracketDto(e.getKey(), e.getValue().stream()
                .collect(Collectors.toMap(
                    p -> p.getMatch().getMatchNumber(),
                    p -> new OtherPickDto(
                        p.getPredictedWinner() != null ? p.getPredictedWinner().getName() : null,
                        p.getPointsEarned())))))
            .sorted(Comparator.comparing(UserBracketDto::displayName))
            .toList();
    }

    @PostMapping("/save-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveAll(@RequestBody Map<String, Long> picks,
                                                         @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByUsername(principal.getUsername());

        if (!bracketAssemblyService.isR32Complete()) {
            return ResponseEntity.badRequest().body(Map.of("error", "O bracket ainda não está definido."));
        }

        Map<Integer, Long> byMatchNumber = picks.entrySet().stream()
            .collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));

        try {
            int saved = bracketAssemblyService.saveAllPicks(user, byMatchNumber);
            long totalAvailable = matchRepository.countAvailableKnockoutMatches();
            long userPicks = bracketPickRepository.countByUserId(user.getId());
            if (userPicks == totalAvailable) {
                emailService.sendBracketSummaryConfirmation(user, bracketPickRepository.findByUserIdWithDetails(user.getId()));
            }
            return ResponseEntity.ok(Map.of("saved", saved));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
