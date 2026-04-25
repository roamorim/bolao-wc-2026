package br.com.bolao.web.controller.admin;

import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.service.TournamentAdminService;
import br.com.bolao.web.dto.request.MatchResultRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/matches")
public class AdminMatchController {

    private final MatchRepository matchRepository;
    private final TournamentAdminService adminService;

    public AdminMatchController(MatchRepository matchRepository, TournamentAdminService adminService) {
        this.matchRepository = matchRepository;
        this.adminService = adminService;
    }

    @GetMapping
    public String listMatches(Model model) {
        model.addAttribute("matches", matchRepository.findAllWithTeamsOrderByDatetime());
        return "admin/matches";
    }

    @PostMapping("/{id}/result")
    public String submitResult(
            @PathVariable Long id,
            @Valid @ModelAttribute MatchResultRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.submitMatchResult(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Resultado registrado e pontos calculados.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/matches";
    }

    @PostMapping("/{id}/lock")
    public String lockMatch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.lockMatch(id);
        redirectAttributes.addFlashAttribute("successMessage", "Apostas encerradas para o jogo.");
        return "redirect:/admin/matches";
    }
}
