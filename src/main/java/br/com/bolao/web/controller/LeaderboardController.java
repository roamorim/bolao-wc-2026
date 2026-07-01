package br.com.bolao.web.controller;

import br.com.bolao.service.LeaderboardService;
import br.com.bolao.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private static final String WARM = "hsl(42,80%,86%)";
    private static final String COOL = "hsl(56,80%,86%)";

    private static final Map<String, Integer> FINGERPRINT_MAP = Map.ofEntries(
        Map.entry("dguarana",  0),
        Map.entry("epocler",   1),
        Map.entry("fmonteiro", 2),
        Map.entry("pgarcia",   3),
        Map.entry("jrwenke",   4),
        Map.entry("leonardo",  5),
        Map.entry("clopes",    6),
        Map.entry("marco",     7),
        Map.entry("roamorim",  8),
        Map.entry("theo",      9),
        Map.entry("jaafreire", 10)
    );

    private final LeaderboardService leaderboardService;
    private final UserService userService;

    @Value("${bolao.fingerprint.enabled:false}")
    private boolean fingerprintEnabled;

    public LeaderboardController(LeaderboardService leaderboardService, UserService userService) {
        this.leaderboardService = leaderboardService;
        this.userService = userService;
    }

    @GetMapping
    public String leaderboard(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("entries", leaderboardService.getLeaderboard(currentUserId(principal)));
        model.addAttribute("fpRowColors", computeFpRowColors(principal));
        return "leaderboard";
    }

    @GetMapping("/fragment")
    public String leaderboardFragment(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("entries", leaderboardService.getLeaderboard(currentUserId(principal)));
        model.addAttribute("fpRowColors", computeFpRowColors(principal));
        return "fragments/leaderboard-table :: leaderboardTable";
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) return null;
        return userService.findByUsername(principal.getUsername()).getId();
    }

    private String[] computeFpRowColors(UserDetails principal) {
        if (!fingerprintEnabled || principal == null) return null;
        Integer idx = FINGERPRINT_MAP.get(principal.getUsername());
        if (idx == null) return null;
        String[] colors = new String[4];
        for (int bit = 0; bit < 4; bit++) {
            colors[bit] = ((idx >> bit) & 1) == 0 ? WARM : COOL;
        }
        return colors;
    }
}
