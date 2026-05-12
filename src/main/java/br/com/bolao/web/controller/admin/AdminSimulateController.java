package br.com.bolao.web.controller.admin;

import br.com.bolao.service.SimulationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/simulate")
public class AdminSimulateController {

    private final SimulationService simulationService;

    public AdminSimulateController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("status", simulationService.getStatus());
        return "admin/simulate";
    }

    @PostMapping("/seed")
    public String seed(@RequestParam(defaultValue = "10") int users,
                       RedirectAttributes ra) {
        int created = simulationService.seedUsers(users);
        int seeded = simulationService.seedPredictions();
        ra.addFlashAttribute("successMessage",
            created + " usuário(s) criado(s), palpites gerados para " + seeded + " participante(s).");
        return "redirect:/admin/simulate";
    }

    @PostMapping("/advance")
    public String advance(@RequestParam int upToMatch, RedirectAttributes ra) {
        int advanced = simulationService.advanceResults(upToMatch);
        ra.addFlashAttribute("successMessage",
            advanced + " jogo(s) encerrado(s) com resultados aleatórios e pontuação calculada.");
        return "redirect:/admin/simulate";
    }

    @PostMapping("/reset-results")
    public String resetResults(RedirectAttributes ra) {
        simulationService.resetResults();
        ra.addFlashAttribute("successMessage",
            "Resultados apagados. Todos os jogos voltaram para SCHEDULED e pontuações zeradas.");
        return "redirect:/admin/simulate";
    }

    @PostMapping("/advance-knockout")
    public String advanceKnockout(@RequestParam String stage, RedirectAttributes ra) {
        int advanced = simulationService.advanceKnockoutStage(stage);
        ra.addFlashAttribute("successMessage",
            advanced + " jogo(s) do mata-mata encerrado(s) com resultados aleatórios e pontuação calculada.");
        return "redirect:/admin/simulate";
    }

    @PostMapping("/clear")
    public String clear(RedirectAttributes ra) {
        int cleared = simulationService.clearSimulationData();
        ra.addFlashAttribute("successMessage",
            cleared + " participante(s) de simulação removido(s).");
        return "redirect:/admin/simulate";
    }
}
