package br.com.bolao.web.controller;

import br.com.bolao.service.LeaderboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public String leaderboard(Model model) {
        model.addAttribute("entries", leaderboardService.getLeaderboard());
        return "leaderboard";
    }

    @GetMapping("/fragment")
    public String leaderboardFragment(Model model) {
        model.addAttribute("entries", leaderboardService.getLeaderboard());
        return "fragments/leaderboard-table :: leaderboardTable";
    }
}
