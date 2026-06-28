package br.com.bolao.web.controller;

import br.com.bolao.domain.repository.BracketPickRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.service.BracketAssemblyService;
import br.com.bolao.service.LeaderboardService;
import br.com.bolao.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.Comparator;

@Controller
public class DashboardController {

    private final MatchPredictionRepository matchPredictionRepository;
    private final BracketPickRepository bracketPickRepository;
    private final MatchRepository matchRepository;
    private final BracketAssemblyService bracketAssemblyService;
    private final LeaderboardService leaderboardService;
    private final UserService userService;

    public DashboardController(
            MatchPredictionRepository matchPredictionRepository,
            BracketPickRepository bracketPickRepository,
            MatchRepository matchRepository,
            BracketAssemblyService bracketAssemblyService,
            LeaderboardService leaderboardService,
            UserService userService) {
        this.matchPredictionRepository = matchPredictionRepository;
        this.bracketPickRepository = bracketPickRepository;
        this.matchRepository = matchRepository;
        this.bracketAssemblyService = bracketAssemblyService;
        this.leaderboardService = leaderboardService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.findByUsername(principal.getUsername());

        var predictions = matchPredictionRepository.findByUserIdWithMatch(user.getId());
        var leaderboard = leaderboardService.getLeaderboard();
        boolean bracketReady = bracketAssemblyService.isR32Complete();
        var knockoutMatches = matchRepository.findAllKnockoutMatchesWithStage();
        int totalBracketMatches = knockoutMatches.size();
        long myBracketPicks = bracketPickRepository.countByUserId(user.getId());
        boolean bracketDeadlinePassed = knockoutMatches.stream()
            .map(m -> m.getPredictionDeadline())
            .min(Comparator.naturalOrder())
            .map(d -> Instant.now().isAfter(d))
            .orElse(false);

        model.addAttribute("user", user);
        model.addAttribute("myPredictions", predictions);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("bracketReady", bracketReady);
        model.addAttribute("bracketDeadlinePassed", bracketDeadlinePassed);
        model.addAttribute("totalBracketMatches", totalBracketMatches);
        model.addAttribute("myBracketPicks", myBracketPicks);

        return "dashboard";
    }
}
