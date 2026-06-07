package br.com.bolao.web.controller;

import br.com.bolao.domain.model.GroupClassificationPrediction;
import br.com.bolao.domain.model.Player;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.model.TopScorerPrediction;
import br.com.bolao.domain.repository.GroupClassificationPredictionRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.PlayerRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.domain.repository.TopScorerPredictionRepository;
import br.com.bolao.service.PredictionService;
import br.com.bolao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/special-predictions")
public class SpecialPredictionsController {

    private static final List<String> GROUPS =
        List.of("A","B","C","D","E","F","G","H","I","J","K","L");

    private static final Instant FALLBACK_DEADLINE =
        Instant.parse("2026-06-11T14:30:00Z");

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final GroupClassificationPredictionRepository predictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final PlayerRepository playerRepository;
    private final PredictionService predictionService;
    private final UserService userService;

    public SpecialPredictionsController(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            GroupClassificationPredictionRepository predictionRepository,
            TopScorerPredictionRepository topScorerPredictionRepository,
            PlayerRepository playerRepository,
            PredictionService predictionService,
            UserService userService) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.predictionRepository = predictionRepository;
        this.topScorerPredictionRepository = topScorerPredictionRepository;
        this.playerRepository = playerRepository;
        this.predictionService = predictionService;
        this.userService = userService;
    }

    record PlayerDto(Long id, String name, String team) {}

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.findByUsername(principal.getUsername());

        Map<String, List<Team>> teamsByGroup = teamsByGroup();
        Map<String, GroupClassificationPrediction> predsByGroup = predictionRepository
            .findByUserId(user.getId()).stream()
            .collect(Collectors.toMap(GroupClassificationPrediction::getGroupName, p -> p));

        long thirdQualifiesCount = predsByGroup.values().stream()
            .filter(GroupClassificationPrediction::isThirdQualifies).count();

        List<PlayerDto> players = playerRepository.findAllWithTeamOrdered().stream()
            .map(p -> new PlayerDto(p.getId(), p.getName(), p.getTeam().getName()))
            .toList();

        TopScorerPrediction topScorerPred = topScorerPredictionRepository
            .findByUserIdWithTeam(user.getId()).orElse(null);

        Instant deadline = specialDeadline();

        model.addAttribute("groups", GROUPS);
        model.addAttribute("teamsByGroup", teamsByGroup);
        model.addAttribute("predsByGroup", predsByGroup);
        model.addAttribute("deadline", deadline);
        model.addAttribute("deadlinePassed", Instant.now().isAfter(deadline));
        model.addAttribute("thirdQualifiesCount", thirdQualifiesCount);
        model.addAttribute("players", players);
        model.addAttribute("topScorerPred", topScorerPred);
        return "special-predictions";
    }

    @PostMapping("/group/{groupName}")
    public String saveGroup(
            @PathVariable String groupName,
            @RequestParam Long firstTeamId,
            @RequestParam Long secondTeamId,
            @RequestParam(required = false) Long thirdTeamId,
            @RequestParam(defaultValue = "false") boolean thirdQualifies,
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            Model model) {

        var user = userService.findByUsername(principal.getUsername());
        boolean isHtmx = "true".equals(httpRequest.getHeader("HX-Request"));

        Instant deadline = specialDeadline();
        Map<String, List<Team>> allTeamsByGroup = teamsByGroup();
        List<Team> groupTeams = allTeamsByGroup.get(groupName);

        try {
            predictionService.saveGroupClassification(
                user, groupName,
                firstTeamId, secondTeamId, thirdTeamId, thirdQualifies,
                deadline);
        } catch (PredictionService.PredictionClosedException e) {
            if (isHtmx) {
                model.addAttribute("groupName", groupName);
                model.addAttribute("teams", groupTeams);
                model.addAttribute("pred", predictionRepository.findByUserIdAndGroupName(user.getId(), groupName).orElse(null));
                model.addAttribute("deadline", deadline);
                model.addAttribute("deadlinePassed", true);
                model.addAttribute("groupError", e.getMessage());
                return "special-predictions/group-card :: groupCard";
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/special-predictions";
        } catch (IllegalArgumentException e) {
            if (isHtmx) {
                model.addAttribute("groupName", groupName);
                model.addAttribute("teams", groupTeams);
                model.addAttribute("pred", predictionRepository.findByUserIdAndGroupName(user.getId(), groupName).orElse(null));
                model.addAttribute("deadline", deadline);
                model.addAttribute("deadlinePassed", false);
                model.addAttribute("groupError", e.getMessage());
                return "special-predictions/group-card :: groupCard";
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/special-predictions";
        }

        if (isHtmx) {
            GroupClassificationPrediction pred = predictionRepository
                .findByUserIdAndGroupName(user.getId(), groupName).orElse(null);
            long thirdQualifiesCount = predictionRepository.findByUserId(user.getId()).stream()
                .filter(GroupClassificationPrediction::isThirdQualifies).count();

            model.addAttribute("groupName", groupName);
            model.addAttribute("teams", groupTeams);
            model.addAttribute("pred", pred);
            model.addAttribute("deadline", deadline);
            model.addAttribute("deadlinePassed", false);
            model.addAttribute("thirdQualifiesCount", thirdQualifiesCount);
            return "special-predictions/group-card :: groupCard";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Grupo " + groupName + " salvo!");
        return "redirect:/special-predictions";
    }

    @PostMapping("/top-scorer")
    public String saveTopScorer(
            @RequestParam Long playerId,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {

        var user = userService.findByUsername(principal.getUsername());
        Player player = playerRepository.findByIdWithTeam(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

        try {
            predictionService.saveTopScorer(user, player.getName(), player.getTeam().getId(), specialDeadline());
            redirectAttributes.addFlashAttribute("successMessage",
                "Artilheiro salvo: " + player.getName() + " (" + player.getTeam().getName() + ")");
        } catch (PredictionService.PredictionClosedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/special-predictions";
    }

    private Map<String, List<Team>> teamsByGroup() {
        return teamRepository.findAllByOrderByGroupNameAscNameAsc().stream()
            .filter(t -> t.getGroupName() != null)
            .collect(Collectors.groupingBy(Team::getGroupName, LinkedHashMap::new, Collectors.toList()));
    }

    private Instant specialDeadline() {
        return matchRepository.findByMatchNumber(1)
            .map(m -> m.getPredictionDeadline())
            .orElse(FALLBACK_DEADLINE);
    }
}
