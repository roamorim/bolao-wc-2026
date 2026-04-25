package br.com.bolao.web.controller;

import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.service.PredictionService;
import br.com.bolao.service.UserService;
import br.com.bolao.web.dto.request.MatchPredictionRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final TeamRepository teamRepository;
    private final PredictionService predictionService;
    private final UserService userService;

    public MatchController(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            TeamRepository teamRepository,
            PredictionService predictionService,
            UserService userService) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
        this.teamRepository = teamRepository;
        this.predictionService = predictionService;
        this.userService = userService;
    }

    @GetMapping
    public String matchList(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.findByUsername(principal.getUsername());
        List<Match> matches = matchRepository.findAllWithTeamsOrderByDatetime();

        Map<Long, MatchPrediction> predictionsByMatchId = matchPredictionRepository
            .findByUserIdWithMatch(user.getId()).stream()
            .collect(Collectors.toMap(p -> p.getMatch().getId(), p -> p));

        var matchesByStage = matches.stream()
            .collect(Collectors.groupingBy(
                m -> m.getStage().getName(),
                java.util.LinkedHashMap::new,
                Collectors.toList()
            ));

        List<Team> teams = teamRepository.findAllByOrderByGroupNameAscNameAsc();
        var teamsByGroup = teams.stream()
            .filter(t -> t.getGroupName() != null)
            .collect(Collectors.groupingBy(
                Team::getGroupName,
                java.util.LinkedHashMap::new,
                Collectors.toList()
            ));

        List<String> distinctDates = matches.stream()
            .map(m -> m.getMatchDatetime().atZone(ZoneOffset.UTC).toLocalDate().toString())
            .distinct()
            .collect(Collectors.toList());

        model.addAttribute("matchesByStage", matchesByStage);
        model.addAttribute("predictionsByMatchId", predictionsByMatchId);
        model.addAttribute("teamsByGroup", teamsByGroup);
        model.addAttribute("distinctDates", distinctDates);
        model.addAttribute("user", user);
        return "matches/list";
    }

    @GetMapping("/{id}")
    public String matchDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        var user = userService.findByUsername(principal.getUsername());
        Match match = matchRepository.findByIdWithTeams(id)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        MatchPrediction existing = matchPredictionRepository
            .findByUserAndMatch(user, match)
            .orElse(null);

        model.addAttribute("match", match);
        model.addAttribute("existingPrediction", existing);
        model.addAttribute("predictionRequest", new MatchPredictionRequest(
            existing != null ? existing.getHomeScorePred() : 0,
            existing != null ? existing.getAwayScorePred() : 0
        ));
        return "matches/detail";
    }

    @PostMapping("/{id}/predict")
    public String submitPrediction(
            @PathVariable Long id,
            @Valid @ModelAttribute("predictionRequest") MatchPredictionRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            Model model) {

        var user = userService.findByUsername(principal.getUsername());
        boolean isHtmx = "true".equals(httpRequest.getHeader("HX-Request"));

        if (bindingResult.hasErrors()) {
            Match match = matchRepository.findByIdWithTeams(id).orElseThrow();
            if (isHtmx) {
                MatchPrediction pred = matchPredictionRepository.findByUserAndMatch(user, match).orElse(null);
                model.addAttribute("match", match);
                model.addAttribute("pred", pred);
                return "matches/match-row :: row";
            }
            model.addAttribute("match", match);
            return "matches/detail";
        }

        predictionService.saveMatchPrediction(user, id, request);

        if (isHtmx) {
            Match match = matchRepository.findByIdWithTeams(id).orElseThrow();
            MatchPrediction pred = matchPredictionRepository.findByUserAndMatch(user, match).orElse(null);
            model.addAttribute("match", match);
            model.addAttribute("pred", pred);
            return "matches/match-row :: row";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Aposta registrada com sucesso!");
        return "redirect:/matches/" + id;
    }
}
