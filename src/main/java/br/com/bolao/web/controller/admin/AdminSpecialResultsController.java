package br.com.bolao.web.controller.admin;

import br.com.bolao.domain.model.GroupResult;
import br.com.bolao.domain.model.Player;
import br.com.bolao.domain.model.Team;
import br.com.bolao.domain.model.TopScorerResult;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.PlayerRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.domain.repository.TopScorerResultRepository;
import br.com.bolao.service.ScoringService;
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
@RequestMapping("/admin/special-results")
public class AdminSpecialResultsController {

    private static final List<String> GROUPS =
        List.of("A","B","C","D","E","F","G","H","I","J","K","L");

    private final GroupResultRepository groupResultRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TopScorerResultRepository topScorerResultRepository;
    private final ScoringService scoringService;

    public AdminSpecialResultsController(
            GroupResultRepository groupResultRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            TopScorerResultRepository topScorerResultRepository,
            ScoringService scoringService) {
        this.groupResultRepository = groupResultRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.topScorerResultRepository = topScorerResultRepository;
        this.scoringService = scoringService;
    }

    public record GroupRow(String name, List<Team> teams, GroupResult result) {}

    @GetMapping
    public String page(Model model) {
        Map<String, GroupResult> resultsByGroup = groupResultRepository.findAllWithTeams().stream()
            .collect(Collectors.toMap(GroupResult::getGroupName, r -> r));

        List<GroupRow> groupRows = GROUPS.stream()
            .map(g -> new GroupRow(
                g,
                teamRepository.findByGroupNameOrderByName(g),
                resultsByGroup.get(g)))
            .toList();

        Map<String, List<Player>> playersByTeam = playerRepository.findAllWithTeamOrdered().stream()
            .collect(Collectors.groupingBy(
                p -> p.getTeam().getName(),
                LinkedHashMap::new,
                Collectors.toList()));

        TopScorerResult topScorerResult = topScorerResultRepository
            .findTopByOrderByRecordedAtDesc().orElse(null);

        model.addAttribute("groupRows", groupRows);
        model.addAttribute("playersByTeam", playersByTeam);
        model.addAttribute("topScorerResult", topScorerResult);
        return "admin/special-results";
    }

    @PostMapping("/group/{groupName}")
    public String saveGroupResult(
            @PathVariable String groupName,
            @RequestParam Long firstTeamId,
            @RequestParam Long secondTeamId,
            @RequestParam Long thirdTeamId,
            @RequestParam(defaultValue = "false") boolean thirdQualifies,
            RedirectAttributes redirectAttributes) {

        var first  = teamRepository.findById(firstTeamId).orElseThrow();
        var second = teamRepository.findById(secondTeamId).orElseThrow();
        var third  = teamRepository.findById(thirdTeamId).orElseThrow();

        GroupResult result = groupResultRepository.findById(groupName)
            .orElseGet(() -> { GroupResult r = new GroupResult(); r.setGroupName(groupName); return r; });

        result.setFirstTeam(first);
        result.setSecondTeam(second);
        result.setThirdTeam(third);
        result.setThirdQualifies(thirdQualifies);
        result.setRecordedAt(Instant.now());
        groupResultRepository.save(result);

        scoringService.calculateGroupClassification(
            groupName,
            firstTeamId, secondTeamId, thirdTeamId, thirdQualifies);

        redirectAttributes.addFlashAttribute("successMessage",
            "Resultado do Grupo " + groupName + " salvo e pontos calculados.");
        return "redirect:/admin/special-results";
    }

    @PostMapping("/top-scorer")
    public String saveTopScorerResult(
            @RequestParam Long playerId,
            RedirectAttributes redirectAttributes) {

        Player player = playerRepository.findByIdWithTeam(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

        TopScorerResult result = topScorerResultRepository
            .findTopByOrderByRecordedAtDesc()
            .orElseGet(TopScorerResult::new);

        result.setPlayerName(player.getName());
        result.setRecordedAt(Instant.now());
        topScorerResultRepository.save(result);

        scoringService.calculateTopScorer(player.getName());

        redirectAttributes.addFlashAttribute("successMessage",
            "Artilheiro registrado: " + player.getName() + ". Pontos calculados.");
        return "redirect:/admin/special-results";
    }
}
