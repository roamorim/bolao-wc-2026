package br.com.bolao.web.controller.admin;

import br.com.bolao.domain.model.GroupResult;
import br.com.bolao.domain.repository.GroupResultRepository;
import br.com.bolao.domain.repository.TeamRepository;
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
    private final ScoringService scoringService;

    public AdminSpecialResultsController(
            GroupResultRepository groupResultRepository,
            TeamRepository teamRepository,
            ScoringService scoringService) {
        this.groupResultRepository = groupResultRepository;
        this.teamRepository = teamRepository;
        this.scoringService = scoringService;
    }

    @GetMapping
    public String page(Model model) {
        Map<String, GroupResult> resultsByGroup = groupResultRepository.findAll().stream()
            .collect(Collectors.toMap(GroupResult::getGroupName, r -> r));

        var teamsByGroup = teamRepository.findAllByOrderByGroupNameAscNameAsc().stream()
            .filter(t -> t.getGroupName() != null)
            .collect(Collectors.groupingBy(
                t -> t.getGroupName(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        model.addAttribute("groups", GROUPS);
        model.addAttribute("resultsByGroup", resultsByGroup);
        model.addAttribute("teamsByGroup", teamsByGroup);
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
}
