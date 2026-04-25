package br.com.bolao.web.controller.admin;

import br.com.bolao.domain.repository.ScoringConfigRepository;
import br.com.bolao.service.TournamentAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/scoring")
public class AdminScoringConfigController {

    private final ScoringConfigRepository scoringConfigRepository;
    private final TournamentAdminService adminService;

    public AdminScoringConfigController(
            ScoringConfigRepository scoringConfigRepository,
            TournamentAdminService adminService) {
        this.scoringConfigRepository = scoringConfigRepository;
        this.adminService = adminService;
    }

    @GetMapping
    public String scoringConfig(Model model) {
        model.addAttribute("configs", scoringConfigRepository.findAll());
        return "admin/scoring";
    }

    @PostMapping("/{id}")
    public String updateConfig(
            @PathVariable Long id,
            @RequestParam int points,
            RedirectAttributes redirectAttributes) {
        adminService.updateScoringConfig(id, points);
        redirectAttributes.addFlashAttribute("successMessage", "Pontuação atualizada.");
        return "redirect:/admin/scoring";
    }

    @PostMapping("/recalculate")
    public String recalculateAll(RedirectAttributes redirectAttributes) {
        adminService.recalculateAll();
        redirectAttributes.addFlashAttribute("successMessage", "Recálculo completo. Todos os pontos atualizados.");
        return "redirect:/admin/scoring";
    }
}
