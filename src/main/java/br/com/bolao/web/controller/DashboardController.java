package br.com.bolao.web.controller;

import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.service.LeaderboardService;
import br.com.bolao.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class DashboardController {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final LeaderboardService leaderboardService;
    private final UserService userService;

    public DashboardController(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            LeaderboardService leaderboardService,
            UserService userService) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.leaderboardService = leaderboardService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.findByUsername(principal.getUsername());
        var now = Instant.now();

        var upcoming = matchRepository.findByDatetimeBetween(now, now.plus(7, ChronoUnit.DAYS));
        var predictions = matchPredictionRepository.findByUserIdWithMatch(user.getId());
        var leaderboard = leaderboardService.getLeaderboard();

        model.addAttribute("user", user);
        model.addAttribute("upcomingMatches", upcoming);
        model.addAttribute("myPredictions", predictions);
        model.addAttribute("leaderboard", leaderboard);

        return "dashboard";
    }
}
