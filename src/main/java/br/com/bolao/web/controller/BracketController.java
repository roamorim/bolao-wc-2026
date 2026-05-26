package br.com.bolao.web.controller;

import br.com.bolao.domain.model.*;
import br.com.bolao.domain.repository.BracketPickRepository;
import br.com.bolao.domain.repository.MatchRepository;
import br.com.bolao.domain.repository.TeamRepository;
import br.com.bolao.service.BracketAssemblyService;
import br.com.bolao.service.EmailService;
import br.com.bolao.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bracket")
public class BracketController {

    private static final List<String> STAGE_ORDER =
        List.of("R32", "R16", "QF", "SEMI", "SF", "FINAL");

    private final MatchRepository matchRepository;
    private final BracketPickRepository bracketPickRepository;
    private final TeamRepository teamRepository;
    private final BracketAssemblyService bracketAssemblyService;
    private final UserService userService;
    private final EmailService emailService;

    public BracketController(MatchRepository matchRepository,
                              BracketPickRepository bracketPickRepository,
                              TeamRepository teamRepository,
                              BracketAssemblyService bracketAssemblyService,
                              UserService userService,
                              EmailService emailService) {
        this.matchRepository = matchRepository;
        this.bracketPickRepository = bracketPickRepository;
        this.teamRepository = teamRepository;
        this.bracketAssemblyService = bracketAssemblyService;
        this.userService = userService;
        this.emailService = emailService;
    }

    public record MatchRow(Match match, BracketPick pick, boolean canPick) {}
    public record StageSection(String code, String name, List<MatchRow> rows) {}

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.findByUsername(principal.getUsername());
        boolean bracketReady = bracketAssemblyService.isR32Complete();

        List<Match> knockoutMatches = matchRepository.findAllKnockoutMatchesWithStage();
        Map<Long, BracketPick> pickByMatchId = bracketPickRepository
            .findByUserIdWithDetails(user.getId()).stream()
            .collect(Collectors.toMap(p -> p.getMatch().getId(), p -> p));

        List<StageSection> sections = STAGE_ORDER.stream()
            .map(code -> {
                List<MatchRow> rows = knockoutMatches.stream()
                    .filter(m -> m.getStage().getCode().equals(code))
                    .map(m -> {
                        BracketPick pick = pickByMatchId.get(m.getId());
                        boolean canPick = bracketReady
                            && m.getHomeTeam() != null
                            && m.getAwayTeam() != null
                            && !m.isFinished()
                            && Instant.now().isBefore(m.getPredictionDeadline());
                        return new MatchRow(m, pick, canPick);
                    })
                    .toList();
                if (rows.isEmpty()) return null;
                String name = rows.get(0).match().getStage().getName();
                return new StageSection(code, name, rows);
            })
            .filter(Objects::nonNull)
            .toList();

        model.addAttribute("sections", sections);
        model.addAttribute("bracketReady", bracketReady);
        return "bracket";
    }

    @PostMapping("/{matchId}/pick")
    public String savePick(@PathVariable Long matchId,
                           @RequestParam Long winnerId,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getUsername());

        Match match = matchRepository.findAllKnockoutMatchesWithStage().stream()
            .filter(m -> m.getId().equals(matchId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + matchId));

        if (!bracketAssemblyService.isR32Complete()) {
            ra.addFlashAttribute("errorMessage", "O bracket ainda não está definido.");
            return "redirect:/bracket";
        }
        if (match.isFinished()) {
            ra.addFlashAttribute("errorMessage", "Este jogo já foi encerrado.");
            return "redirect:/bracket";
        }
        if (Instant.now().isAfter(match.getPredictionDeadline())) {
            ra.addFlashAttribute("errorMessage", "Prazo para apostas deste jogo encerrado.");
            return "redirect:/bracket";
        }
        if (!winnerId.equals(match.getHomeTeam().getId()) && !winnerId.equals(match.getAwayTeam().getId())) {
            ra.addFlashAttribute("errorMessage", "Time inválido para este jogo.");
            return "redirect:/bracket";
        }

        Team winner = teamRepository.findById(winnerId).orElseThrow();
        BracketPick pick = bracketPickRepository
            .findByUserIdAndMatchId(user.getId(), matchId)
            .orElseGet(() -> {
                BracketPick p = new BracketPick();
                p.setUser(user);
                p.setMatch(match);
                return p;
            });
        pick.setPredictedWinner(winner);
        pick.setSubmittedAt(Instant.now());
        pick.setPointsEarned(null);
        bracketPickRepository.save(pick);
        long totalAvailable = matchRepository.countAvailableKnockoutMatches();
        long userPicks = bracketPickRepository.countByUserId(user.getId());
        if (userPicks == totalAvailable) {
            emailService.sendBracketSummaryConfirmation(user, bracketPickRepository.findByUserIdWithDetails(user.getId()));
        }

        ra.addFlashAttribute("successMessage", "Palpite salvo: " + winner.getName() + " vence o jogo " + match.getMatchNumber() + ".");
        return "redirect:/bracket";
    }
}
