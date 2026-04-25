package br.com.bolao.web.controller;

import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.MatchPrediction;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.MatchPredictionRepository;
import br.com.bolao.service.PredictionService;
import br.com.bolao.service.UserService;
import br.com.bolao.web.dto.request.MatchPredictionRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final MatchPredictionRepository matchPredictionRepository;
    private final PredictionService predictionService;
    private final UserService userService;

    public MatchController(
            MatchRepository matchRepository,
            MatchPredictionRepository matchPredictionRepository,
            PredictionService predictionService,
            UserService userService) {
        this.matchRepository = matchRepository;
        this.matchPredictionRepository = matchPredictionRepository;
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

        // Group matches by stage name preserving order
        var matchesByStage = matches.stream()
            .collect(Collectors.groupingBy(
                m -> m.getStage().getName(),
                java.util.LinkedHashMap::new,
                Collectors.toList()
            ));

        model.addAttribute("matchesByStage", matchesByStage);
        model.addAttribute("predictionsByMatchId", predictionsByMatchId);
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
            RedirectAttributes redirectAttributes,
            Model model) {

        var user = userService.findByUsername(principal.getUsername());

        if (bindingResult.hasErrors()) {
            Match match = matchRepository.findByIdWithTeams(id).orElseThrow();
            model.addAttribute("match", match);
            return "matches/detail";
        }

        predictionService.saveMatchPrediction(user, id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Aposta registrada com sucesso!");
        return "redirect:/matches/" + id;
    }
}
