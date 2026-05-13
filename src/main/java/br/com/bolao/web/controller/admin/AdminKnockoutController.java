package br.com.bolao.web.controller.admin;

import br.com.bolao.domain.model.Match;
import br.com.bolao.domain.model.TournamentStage;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.service.BracketAssemblyService;
import br.com.bolao.service.TournamentAdminService;
import br.com.bolao.web.dto.request.MatchResultRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/knockout")
public class AdminKnockoutController {

    private final MatchRepository matchRepository;
    private final BracketAssemblyService bracketAssemblyService;
    private final TournamentAdminService tournamentAdminService;

    public AdminKnockoutController(MatchRepository matchRepository,
                                   BracketAssemblyService bracketAssemblyService,
                                   TournamentAdminService tournamentAdminService) {
        this.matchRepository = matchRepository;
        this.bracketAssemblyService = bracketAssemblyService;
        this.tournamentAdminService = tournamentAdminService;
    }

    public record StageGroup(TournamentStage stage, List<Match> matches) {}

    @GetMapping
    public String page(Model model) {
        List<Match> allKnockout = matchRepository.findAllKnockoutMatchesWithStage();

        Map<String, StageGroup> stageGroups = new LinkedHashMap<>();
        for (Match m : allKnockout) {
            String code = m.getStage().getCode();
            stageGroups.computeIfAbsent(code, k -> new StageGroup(m.getStage(), new java.util.ArrayList<>()))
                .matches().add(m);
        }

        model.addAttribute("stageGroups", stageGroups.values());
        model.addAttribute("allGroupResultsIn", bracketAssemblyService.isAllGroupResultsIn());
        model.addAttribute("bracketReady", bracketAssemblyService.isR32Complete());
        model.addAttribute("groupResultCount", bracketAssemblyService.isAllGroupResultsIn() ? 12 :
            allKnockout.stream().filter(m -> m.getHomeTeam() != null).count());
        return "admin/knockout";
    }

    @PostMapping("/assemble")
    public String assemble(RedirectAttributes ra) {
        try {
            bracketAssemblyService.assembleR32();
            ra.addFlashAttribute("successMessage", "Bracket do R32 montado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/knockout";
    }

    @PostMapping("/{id}/result")
    public String submitResult(@PathVariable Long id,
                               @Valid @ModelAttribute MatchResultRequest request,
                               RedirectAttributes ra) {
        try {
            tournamentAdminService.submitMatchResult(id, request);
            ra.addFlashAttribute("successMessage", "Resultado registrado e pontos calculados.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/knockout";
    }
}
